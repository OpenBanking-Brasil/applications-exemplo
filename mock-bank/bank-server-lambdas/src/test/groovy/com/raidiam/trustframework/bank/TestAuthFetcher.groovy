package com.raidiam.trustframework.bank

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.PlainObject
import io.micronaut.function.aws.proxy.MicronautAwsProxyRequest
import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.filters.AuthenticationFetcher
import io.reactivex.Flowable
import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory

//@Singleton
//@Primary
class TestAuthFetcher implements AuthenticationFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(TestAuthFetcher)
    private ObjectMapper objectMapper = new ObjectMapper()

    private static scopeMap = [
            "user:account": "ACCOUNTS_READ",
            "org:admin": "ACCOUNTS_WRITE",
            "user:consent": "CONSENTS_MANAGE",
            "user:janitor": "DOESNT_REAL"
    ]

    @Override
    Publisher<Authentication> fetchAuthentication(HttpRequest<?> request) {
        LOG.info("Request for auth")
        if (request instanceof MicronautAwsProxyRequest) {
            LOG.info("We're a lambda")
            MicronautAwsProxyRequest<?> micronautRequest = (MicronautAwsProxyRequest) request;
            def authorizer = micronautRequest.getAwsProxyRequest().getRequestContext().getAuthorizer()
            if(!authorizer) {
                LOG.info("No authorizer - returning 403")
                return Flowable.empty()
            }
            def token = authorizer.getContextValue("access_token")
            token = objectMapper.readValue(token, Map.class)
            def scopes = token['scope'].split(' ')
            def roles = scopes.collect { scopeMap.get(it) }
            roles.removeAll([null])
            return Flowable.just(new ClientAuthentication("ALLOWED", [roles: roles]))
        } else {
            def token = request.getHeaders().get("Authorization")
            if(!token) {
                return Flowable.empty()
            }
            def matcher = token =~ /^Bearer\s(?<token>.+)/
            if(!matcher.matches()) {
                return Flowable.empty()
            }
            token = matcher.group('token')
            PlainObject parsed = PlainObject.parse(token)
            def scopes = parsed.payload.toJSONObject().get("scope", "").split(' ')
            def roles = scopes.collect { scopeMap.get(it) }
            roles.removeAll([null])
            return Flowable.just(new ClientAuthentication("ALLOWED", [roles: roles]))
        }

        Flowable.empty()
    }

    @Override
    int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
