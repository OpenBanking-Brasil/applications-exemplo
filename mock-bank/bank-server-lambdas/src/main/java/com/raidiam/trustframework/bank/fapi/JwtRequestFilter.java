package com.raidiam.trustframework.bank.fapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.*;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.raidiam.trustframework.bank.jwt.JwksFetcher;
import com.raidiam.trustframework.bank.jwt.JwtMediaType;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.bank.utils.JwtSigner;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.*;

@Filter("/open-banking/payments/**")
public class JwtRequestFilter implements HttpServerFilter {

    public static final String JTI_ATTRIBUTE = "TF_JTI";
    private static final String ORG_ID_ATTRIBUTE = "orgId";
    private static final String ACCEPT_ATTRIBUTE = "accept";

    private static final Logger LOG = LoggerFactory.getLogger(JwtRequestFilter.class);

    private static final List<String> JWT_CONTENT_TYPES = List.of("application/jwt");

    private static final List<HttpStatus> SUCCESS_STATUSES = List.of(
            HttpStatus.OK,
            HttpStatus.CREATED,
            HttpStatus.NO_RESPONSE,
            HttpStatus.ACCEPTED,
            HttpStatus.FOUND
    );
    private final ObjectMapper objectMapper;
    private final JwtSigner jwtSigner;
    private final String mockBankIssuer;

    private final JwksFetcher jwksFetcher;

    public JwtRequestFilter(JwksFetcher jwksFetcher, JwtSigner jwtSigner, ObjectMapper objectMapper, @Value("${trustframework.issuer}") String mockBankIssuer) {
        this.jwksFetcher = jwksFetcher;
        this.jwtSigner = jwtSigner;
        this.objectMapper = objectMapper;
        this.mockBankIssuer = mockBankIssuer;
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        Optional<String> acceptType = Optional.ofNullable(request.getHeaders().get(ACCEPT_ATTRIBUTE));
        Optional<String> contentType = request.getHeaders().getContentType();
        Optional<Object> requestOrgId = request.getAttribute(ORG_ID_ATTRIBUTE);

        boolean jwt = isJwt(contentType) || isJwt(acceptType) || isNotOp(request);
        LOG.info("Response is a jwt - {}", jwt);
        try {
           validateRequest(request);
        } catch (FilterException e) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return Publishers.map(chain.proceed(request), response -> {

            if(SUCCESS_STATUSES.contains(response.getStatus())) {
                if(jwt) {
                    LOG.info("Request body was a JWT - let's return a JWT");
                    Object body = response.getBody().orElse(null);
                    String audience = requestOrgId.orElse("").toString();
                    String jwtResponse = jsonToJwt(body, audience);
                    response.getHeaders(). remove(HttpHeaders.CONTENT_TYPE);
                    response.contentType(JwtMediaType.JWT_MEDIA_TYPE);
                    response.body(jwtResponse);
                } else {
                    response.getHeaders().remove(HttpHeaders.CONTENT_TYPE);
                    // this may be unnecessary, we may just always be able to set JSON here but this fix shouldn't
                    // accidentally break anything, and there is a refactor of the /payments OP access coming
                    if(isNotOp(request)) {
                        response.contentType(JwtMediaType.JWT_MEDIA_TYPE);
                    } else {
                        response.contentType(MediaType.APPLICATION_JSON);
                    }
                }
            }
            return response;
        });
    }

    private void validateRequest(HttpRequest<?> request) {
        Optional<String> contentType = request.getHeaders().getContentType();
        if (HttpMethod.GET.equals(request.getMethod())){
            LOG.info("Request is a get - Do not check body");
        } else {
            if (isJwt(contentType)) {
                LOG.info("Request body is a JWT - let's verify it");
                Optional<String> body = request.getBody(String.class);
                String packedJwt = body.orElseThrow(() -> fail("No body present"));

                try {
                    SignedJWT signedJWT = SignedJWT.parse(packedJwt);
                    verify(signedJWT, request);
                    captureFrom(signedJWT, request);
                } catch (ParseException e) {
                    throw fail("Unable to parse JWT");
                }
            }
        }
    }

    private void captureFrom(SignedJWT signedJWT, HttpRequest<?> request) {
        try {
            Optional<String> jwtidOpt = Optional.ofNullable(signedJWT.getJWTClaimsSet().getJWTID());
            String jwtId = jwtidOpt.orElseThrow(() -> new FilterException(HttpStatus.BAD_REQUEST, "JTI claim not present"));
            request.setAttribute(JTI_ATTRIBUTE, jwtId);
        } catch (ParseException e) {
            LOG.error("Failed to parse claims", e);
            throw new FilterException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private void verify(SignedJWT signedJWT, HttpRequest<?> request)  {
        JWSHeader header = signedJWT.getHeader();
        try {
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            String issuer = Optional.ofNullable(claims.getIssuer()).orElseThrow(() -> fail("No issuer present"));
            Optional<Object> orgIdFromAccessToken = request.getAttribute(ORG_ID_ATTRIBUTE);
            Object orgId = orgIdFromAccessToken.orElseThrow(() -> fail("Access token does not have org Id"));
            if(!issuer.equals(orgId)) {
                throw fail("Issuer claim and org ID do not match");
            }
            String kid = Optional.ofNullable(header.getKeyID()).orElseThrow(() -> fail("No kid present"));
            JWKSet jwks = jwksFetcher.findForOrg(issuer);
            LOG.info("Getting kid: {}", kid);
            JWK jwk = Optional.ofNullable(jwks.getKeyByKeyId(kid)).orElseThrow(() -> fail("JWK for kid not present"));
            KeyUse use = jwk.getKeyUse();
            if(!use.equals(KeyUse.SIGNATURE)) {
                throw fail("Key is not a signing key");
            }
            ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            jwtProcessor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(new JOSEObjectType("JWT")));

            JWKSource<SecurityContext> keySource =  new ImmutableJWKSet<>(jwks);

            JWSAlgorithm expectedJWSAlg = JWSAlgorithm.PS256;

            JWSKeySelector<SecurityContext> keySelector =
                    new JWSVerificationKeySelector<>(expectedJWSAlg, keySource);
            jwtProcessor.setJWSKeySelector(keySelector);
            jwtProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier(
                    new JWTClaimsSet.Builder().build(),
                    new HashSet<>(Arrays.asList("aud", "iss", "iat", "jti"))));
           jwtProcessor.process(signedJWT, null);
        } catch (ParseException e) {
            LOG.error("Failed to parse claims", e);
            throw new FilterException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (BadJOSEException | JOSEException e) {
            throw fail("JWT failed validation");
        }
    }

    private String jsonToJwt(Object body, String audience) throws FilterException {
        try {
            String json = objectMapper.writeValueAsString(body);
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issueTime(new Date())
                    .jwtID(UUID.randomUUID().toString())
                    .audience(audience)
                    .issuer(mockBankIssuer)
                    .build();
            return jwtSigner.sign(json, claims);
        } catch (JsonProcessingException e) {
            throw new FilterException(HttpStatus.INTERNAL_SERVER_ERROR, "unable to serialize response");
        }
    }

    private boolean isJwt(Optional<String> contentType) {
        if(contentType.isEmpty()) {
            return false;
        }

        for (String contentTypeElement : JWT_CONTENT_TYPES) {
            if (contentType.get().contains(contentTypeElement)) {
                return true;
            }
        }

        return false;
    }

    private boolean isNotOp(HttpRequest<?> request) {
        var meta = BankLambdaUtils.getRequestMeta(request);
        return !meta.getRoles().contains("PAYMENTS_FULL_MANAGE");
    }


    private FilterException fail(String message) {
        LOG.info("Unable to process JWT request: {}", message);
        return new FilterException(HttpStatus.BAD_REQUEST, message);
    }

    private static class FilterException extends RuntimeException {

        private final HttpStatus status;

        public FilterException(HttpStatus status, String message) {
            super(message);
            this.status = status;
        }

        public HttpStatus getStatus() {
            return status;
        }
    }

    @Override
    public int getOrder() {
        return ServerFilterPhase.SECURITY.after();
    }
}
