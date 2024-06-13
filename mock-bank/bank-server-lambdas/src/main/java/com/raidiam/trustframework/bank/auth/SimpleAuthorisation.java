package com.raidiam.trustframework.bank.auth;

import com.amazonaws.serverless.proxy.model.ApiGatewayAuthorizerContext;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyRequestContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.PlainObject;
import com.nimbusds.jose.util.Pair;
import com.raidiam.trustframework.bank.utils.AnnotationsUtil;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Primary;
import io.micronaut.function.aws.proxy.MicronautAwsProxyRequest;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.ClientAuthentication;
import io.micronaut.security.filters.AuthenticationFetcher;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Map.entry;

@Singleton
@Primary
public class SimpleAuthorisation implements AuthenticationFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleAuthorisation.class);
    private static final Pattern BEARER = Pattern.compile("^Bearer\\s(?<token>.+)");
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String ALLOWED = "ALLOWED";

    private final Map<String, String> scopesToRoles = Map.ofEntries(
            entry("openid", "OPENID"),
            entry("accounts", "ACCOUNTS_READ"),
            entry("credit-cards-accounts", "CREDIT_CARDS_ACCOUNTS_READ"),
            entry("consents", "CONSENTS_MANAGE"),
            entry("customers", "CUSTOMERS_READ"),
            entry("invoice-financings", "INVOICE_FINANCINGS_READ"),
            entry("financings", "FINANCINGS_READ"),
            entry("resources", "RESOURCES_READ"),
            entry("unarranged-accounts-overdraft", "UNARRANGED_ACCOUNTS_OVERDRAFT_READ"),
            entry("loans", "LOANS_READ"),
            entry("payments", "PAYMENTS_MANAGE"),
            entry("recurring-payments", "RECURRING_PAYMENTS_MANAGE"),
            entry("bank-fixed-incomes", "BANK_FIXED_INCOMES_READ"),
            entry("credit-fixed-incomes", "CREDIT_FIXED_INCOMES_READ"),
            entry("variable-incomes", "VARIABLE_INCOMES_READ"),
            entry("treasure-titles", "TREASURE_TITLES_READ"),
            entry("funds", "FUNDS_READ"),
            entry("exchanges", "EXCHANGES_READ"),

            // op-related scopes, are these real? They govern the PUT endpoints needed for administration.
            entry("op:consent", "CONSENTS_FULL_MANAGE"),
            entry("op:payments", "PAYMENTS_FULL_MANAGE"),
            entry("op:admin", "ADMIN_FULL_MANAGE"),
            entry("op:recurring-payments", "RECURRING_PAYMENTS_FULL_MANAGE")

    );

    private final ApplicationContext applicationContext;

    private final List<Pair<HttpMethod, String>> requiredClientCredentialsRegexes = new LinkedList<>();
    private final List<Pair<HttpMethod, String>> requiredAuthorisationCodeRegexes = new LinkedList<>();

    @Inject
    public SimpleAuthorisation(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    private void init() {
        AnnotationsUtil.performActionsOnControllerMethodByAnnotation(applicationContext, RequiredAuthenticationGrant.class, (fullPath, httpMethod, extractedAnnotation) ->
                extractedAnnotation.enumValue("value", AuthenticationGrant.class).ifPresent(grant -> {
                    switch (grant) {
                        case AUTHORISATION_CODE:
                            requiredAuthorisationCodeRegexes.add(Pair.of(httpMethod, fullPath));
                            LOG.info("Added required authorisation code regex {} - {}", httpMethod, fullPath);
                            break;
                        case CLIENT_CREDENTIALS:
                            requiredClientCredentialsRegexes.add(Pair.of(httpMethod, fullPath));
                            LOG.info("Added required client credentials regex {} - {}", httpMethod, fullPath);
                            break;
                    }
                }));
    }

    @Override
    public Publisher<Authentication> fetchAuthentication(HttpRequest<?> request) {
        LOG.info("Looking for scopes on a {} request to {}", request.getMethod(), request.getPath());
        if (request instanceof MicronautAwsProxyRequest) {
            return Optional.ofNullable(handleLambdaRequest((MicronautAwsProxyRequest<?>) request))
                    .map(Flowable::just)
                    .orElse(Flowable.empty());
        } else {
            return Optional.ofNullable(handleHttpRequest(request))
                    .map(this::toFlow)
                    .orElse(Flowable.empty());
        }
    }

    private Authentication handleLambdaRequest(MicronautAwsProxyRequest<?> request) {
        LOG.info("We're a lambda");
        if (request.getPath().startsWith("/user")) {
            LOG.info("Request for user endpoint - allowing OP_QUERY_ROLE");
            return makeClientAuthenticationWithRoles(List.of("OP_QUERY_ROLE"));
        }
        ApiGatewayAuthorizerContext authorizer = null;
        try {
            authorizer = getAuthContext(request);
        } catch(ConfigException e) {
            LOG.info("No authorizer - returning 403");
            return null;
        }

        String token = authorizer.getContextValue("access_token");
        Map<String, String> deserialized;
        try {
            deserialized = objectMapper.readValue(token, Map.class);
        } catch (JsonProcessingException e) {
            LOG.error("Exception unpacking access token", e);
            return null;
        }
        String[] scopes = deserialized.getOrDefault("scope", "").split(" ");
        String clientId = deserialized.get("client_id");
        String orgId = deserialized.get("org_id");
        String ssId = deserialized.get("software_id");
        String subject = deserialized.get("sub");
        checkAuthenticationGrant(request, subject);
        LOG.info("Scopes in token: {}", String.join(",", scopes));
        LOG.info("Org ID in token: {}", orgId);
        setRequestCallerInfo(request, scopes, clientId, subject, orgId, ssId);
        LOG.info("Returning new Client Authentication");
        try {
            List<String> roles = getRoles(scopes);
            return makeClientAuthenticationWithRoles(roles);
        } catch (Exception e) {
            LOG.error("Exception setting default auth", e);
            return null;
        }
    }

    private Authentication handleHttpRequest(HttpRequest<?> request) {
        if (request.getPath().startsWith("/user")) {
            LOG.info("Request for user endpoint - allowing OP_QUERY_ROLE");
            return makeClientAuthenticationWithRoles(List.of("OP_QUERY_ROLE"));
        }

        String token = request.getHeaders().get("Authorization");
        if(token == null) {
            LOG.info("No Authorization header");
            return null;
        }
        Matcher matcher = BEARER.matcher(token);
        if(!matcher.matches()) {
            LOG.info("Authorization header doesn't appear to be a bearer token");
            return null;
        }
        token = matcher.group("token");
        PlainObject parsed = null;
        try {
            parsed = PlainObject.parse(token);
            Map<String, Object> payload = parsed.getPayload().toJSONObject();
            String scopesValue = payload.getOrDefault("scope", "").toString();
            String[] scopes = scopesValue.split(" ");
            String clientId = String.valueOf(payload.get("client_id"));
            String subject = String.valueOf(payload.get("subject"));
            String orgId = String.valueOf(payload.get("org_id"));
            String ssId = String.valueOf(payload.get("software_id"));
            setRequestCallerInfo(request, scopes, clientId, subject, orgId, ssId);
            List<String> roles = getRoles(scopes);
            return makeClientAuthenticationWithRoles(roles);
        } catch (ParseException e) {
            return null;
        }

    }

    private Flowable<Authentication> toFlow(Authentication authentication) {
        return Flowable.just(authentication);
    }

    private ApiGatewayAuthorizerContext getAuthContext(MicronautAwsProxyRequest<?> request) throws ConfigException {

        return Optional.ofNullable(request.getAwsProxyRequest())
                .map(AwsProxyRequest::getRequestContext)
                .map(AwsProxyRequestContext::getAuthorizer)
                .orElseThrow(ConfigException::new);
    }

    private static class ConfigException extends Exception {

    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    private List<String> getRoles (String[] scopes) {
        LOG.info("Scopes in token: {}", String.join(",", scopes));
        List<String> roles = Arrays.stream(scopes)
                .map(scopesToRoles::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        LOG.info("Roles inferred: {}", String.join(",", roles));
        return roles;
    }

    private void setRequestCallerInfo(HttpRequest<?> request, String[] scopes, String clientId, String subject, String orgId, String ssId){
        final String dynamicScopePrefix = request.getPath().contains("automatic-payments") ? "recurring-consent:" : "consent:";
        String consentId = Arrays.stream(scopes)
                .filter(Objects::nonNull)
                .filter(a -> !a.isEmpty())
                .filter(a -> a.startsWith(dynamicScopePrefix + "urn:raidiambank:"))
                .findFirst().orElse(null);
        LOG.info("Consent Id inferred: {}", consentId);
        if(consentId != null) {
            consentId = consentId.replace(dynamicScopePrefix,"");
            request.setAttribute("consentId", consentId);

        }
        LOG.info("Setting clientId: {}", clientId);
        request.setAttribute("clientId", clientId);
        if(orgId != null) {
            LOG.info("Setting orgId: {}", clientId);
            request.setAttribute("orgId", orgId);
        }
        if(ssId != null) {
            LOG.info("Setting software statement id: {}", ssId);
            request.setAttribute("ssId", ssId);
        }

        if(subject != null) {
            LOG.info("Setting subject: {}", subject);
            request.setAttribute("sub", ssId);
        }

    }

    private ClientAuthentication makeClientAuthenticationWithRoles(List<String> roles) {
        return new ClientAuthentication(ALLOWED, Map.of("roles", roles));
    }

    private void checkAuthenticationGrant(MicronautAwsProxyRequest<?> request, String subject) {
        //authorization_code grant has sub in the token
        requiredAuthorisationCodeRegexes.forEach(pair -> {
            HttpMethod method = pair.getLeft();
            String regex = pair.getRight();
            if (request.getPath().matches(regex) && request.getMethod().equals(method) && subject == null) {
                String message = String.format("%s %s does not accept client_credentials token - returning 401", method, request.getPath());
                throw new HttpStatusException(HttpStatus.UNAUTHORIZED, message);
            }
        });

        requiredClientCredentialsRegexes.forEach(pair -> {
            HttpMethod method = pair.getLeft();
            String regex = pair.getRight();
            if (request.getPath().matches(regex) && request.getMethod().equals(method) && subject != null) {
                String message = String.format("%s %s does not accept authorization_code token - returning 401", method, request.getPath());
                throw new HttpStatusException(HttpStatus.UNAUTHORIZED, message);
            }
        });
    }
}

