package com.raidiam.trustframework.bank.auth;

import com.amazonaws.serverless.proxy.model.ApiGatewayAuthorizerContext;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyRequestContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.PlainObject;
import io.micronaut.context.annotation.Primary;
import io.micronaut.function.aws.proxy.MicronautAwsProxyRequest;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.DefaultAuthentication;
import io.micronaut.security.filters.AuthenticationFetcher;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
@Primary
public class SimpleAuthorisation implements AuthenticationFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleAuthorisation.class);
    private static final Pattern BEARER = Pattern.compile("^Bearer\\s(?<token>.+)");
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, String> scopesToRoles = Map.of(
            "user:account", "ACCOUNTS_READ",
            "org:admin", "ACCOUNTS_WRITE",
            "consents", "CONSENTS_MANAGE",
            "user:consent", "CONSENTS_MANAGE",
            "op:consent", "CONSENTS_FULL_MANAGE",
            "payments", "PAYMENTS_MANAGE",
            "op:payments", "PAYMENTS_FULL_MANAGE",
            "user:janitor", "DOESNT_REAL"
    );

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
        LOG.info("Scopes in token: {}", String.join(",", scopes));
        LOG.info("Org ID in token: {}", orgId);
        Map<String, Object> attributes = getRoles(scopes);
        setRequestCallerInfo(request, scopes, clientId, orgId, ssId);
        LOG.info("Returning new Default Authentication");
        return new DefaultAuthentication("ALLOWED", attributes);
    }

    private Authentication handleHttpRequest(HttpRequest<?> request) {
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
            String orgId = String.valueOf(payload.get("org_id"));
            String ssId = String.valueOf(payload.get("software_id"));
            Map<String, Object> attributes = getRoles(scopes);
            setRequestCallerInfo(request, scopes, clientId, orgId, ssId);
            return new DefaultAuthentication("ALLOWED", attributes);
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

    private Map<String, Object> getRoles (String[] scopes) {
        LOG.info("Scopes in token: {}", String.join(",", scopes));
        List<String> roles = Arrays.stream(scopes)
                .map(scopesToRoles::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        LOG.info("Roles inferred: {}", String.join(",", roles));
        return Map.of("roles", roles);
    }

    private void setRequestCallerInfo(HttpRequest<?> request, String[] scopes, String clientId, String orgId, String ssId){
        String consentId = Arrays.stream(scopes)
                .map(this::scopeToConsentId)
                .filter(Objects::nonNull)
                .filter(a -> !a.isEmpty())
                .findFirst().orElse(null);
        LOG.info("Consent Id inferred: {}", consentId);
        if(consentId != null) {
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

    }

    private String scopeToConsentId (String scope) {
        if(scope.startsWith("consent:")) {
            var parts = scope.split(":", 2);
            return parts[1];
        }
        return null;
    }

}
