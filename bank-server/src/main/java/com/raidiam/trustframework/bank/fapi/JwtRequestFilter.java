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
import com.raidiam.trustframework.bank.utils.JwtSigner;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.function.aws.proxy.MicronautAwsProxyRequest;
import io.micronaut.function.aws.proxy.MicronautAwsProxyResponse;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.OncePerRequestHttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;
import io.micronaut.http.hateoas.JsonError;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.*;

@Filter("/payments/**")
public class JwtRequestFilter extends OncePerRequestHttpServerFilter {

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

    private JwksFetcher jwksFetcher;

    public JwtRequestFilter(JwksFetcher jwksFetcher, JwtSigner jwtSigner, ObjectMapper objectMapper, @Value("${trustframework.issuer}") String mockBankIssuer) {
        this.jwksFetcher = jwksFetcher;
        this.jwtSigner = jwtSigner;
        this.objectMapper = objectMapper;
        this.mockBankIssuer = mockBankIssuer;
    }

    @Override
    protected Publisher<MutableHttpResponse<?>> doFilterOnce(HttpRequest<?> request, ServerFilterChain chain) {
        Optional<String> acceptType = Optional.empty();
        Optional<String> contentType = request.getHeaders().getContentType();
        Optional<Object> requestOrgId = request.getAttribute("orgId");

        if (request.getHeaders().contains("accept")) {
            acceptType = Optional.of(request.getHeaders().get("accept"));
        }
        
        boolean jwt = (isJwt(contentType) || isJwt(acceptType));
        try {
           validateRequest(request);
        } catch (FilterException e) {
            MicronautAwsProxyResponse<?> theResponse = ((MicronautAwsProxyRequest) request).getResponse();
            JsonError error = new JsonError(e.getMessage());
            String audience = String.valueOf(requestOrgId.orElse(""));
            String body = jsonToJwt(error, audience);
            theResponse.body(body);
            theResponse.status(HttpStatus.UNAUTHORIZED);
            theResponse.getHeaders().remove(HttpHeaders.CONTENT_TYPE);
            theResponse.contentType(JwtMediaType.JWT_MEDIA_TYPE);
            return Publishers.map(Publishers.just(theResponse), response -> {
                response.status(e.getStatus());
                return response;
            });
        }
        return Publishers.map(chain.proceed(request), response -> {

            if(jwt && isSuccess(response.status())) {
                LOG.info("Request body was a JWT - let's return a JWT");
                Object body = response.getBody().get();
                String audience = requestOrgId.get().toString();
                String jwtResponse = jsonToJwt(body, audience);
                response.getHeaders().remove(HttpHeaders.CONTENT_TYPE);
                response.contentType(JwtMediaType.JWT_MEDIA_TYPE);
                response.body(jwtResponse);
            } else {
                response.header(HttpHeaders.CONTENT_TYPE, "application/json");
            }
            return response;
        });
    }

    private boolean isSuccess(HttpStatus status) {
        return SUCCESS_STATUSES.contains(status);
    }

    private void validateRequest(HttpRequest<?> request) {
        Optional<String> contentType = request.getHeaders().getContentType();
        if(isJwt(contentType)) {
            LOG.info("Request body is a JWT - let's verify it");
            Optional<String> body = request.getBody(String.class);
            String packedJwt = body.orElseThrow(() -> fail("No body present"));

            try {
                SignedJWT signedJWT = SignedJWT.parse(packedJwt);
                verify(signedJWT, request);
            } catch (ParseException e) {
                throw fail("Unable to parse JWT");
            }

        }
    }

    private void verify(SignedJWT signedJWT, HttpRequest<?> request)  {
        JWSHeader header = signedJWT.getHeader();
        JWTClaimsSet claims = null;
        try {
            claims = signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            LOG.error("Failed to parse claims", e);
            throw new FilterException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        String issuer = Optional.ofNullable(claims.getIssuer()).orElseThrow(() -> fail("No issuer present"));
        Optional<Object> orgIdFromAccessToken = request.getAttribute("orgId");
        Object orgId = orgIdFromAccessToken.orElseThrow(() -> fail("Access token does not have org Id"));
        if(!issuer.equals(orgId)) {
            throw fail("Issuer claim and org ID do not match");
        }
        String kid = Optional.ofNullable(header.getKeyID()).orElseThrow(() -> fail("No kid present"));
        JWKSet jwks = jwksFetcher.findForOrg(issuer);
        LOG.info("Getting kid: {}", kid);
        JWK jwk = Optional.ofNullable(jwks.getKeyByKeyId(kid)).orElseThrow(() -> fail("JWK for kid not present", HttpStatus.UNAUTHORIZED));
        KeyUse use = jwk.getKeyUse();
        if(!use.equals(KeyUse.SIGNATURE)) {
            throw fail("Key is not a signing key", HttpStatus.UNAUTHORIZED);
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
        try {
           jwtProcessor.process(signedJWT, null);
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
        if(!contentType.isPresent()) {
            return false;
        }

        for (String contentTypeElement : JWT_CONTENT_TYPES) {
            if (contentType.get().contains(contentTypeElement)) {
                return true;
            }
        }

        return false;
    }

    private FilterException fail(String message) {
        return fail(message, HttpStatus.BAD_REQUEST);
    }

    private FilterException fail(String message, HttpStatus status) {
        LOG.info("Unable to process JWT request: {}", message);
        return new FilterException(status, message);
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
