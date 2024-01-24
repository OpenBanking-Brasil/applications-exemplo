package com.raidiam.trustframework.bank

import com.amazonaws.serverless.proxy.model.ApiGatewayAuthorizerContext
import com.amazonaws.serverless.proxy.model.AwsProxyRequest
import com.amazonaws.serverless.proxy.model.AwsProxyRequestContext
import com.nimbusds.jose.Payload
import com.nimbusds.jose.PlainHeader
import com.nimbusds.jose.PlainObject
import com.raidiam.trustframework.bank.auth.SimpleAuthorisation
import io.micronaut.function.aws.proxy.MicronautAwsProxyRequest
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.simple.SimpleHttpHeaders
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.filters.AuthenticationFetcher
import io.reactivex.Flowable
import org.reactivestreams.Publisher
import spock.lang.Specification
import spock.lang.Unroll

class SimpleAuthorisationSpec extends Specification {

    AuthenticationFetcher auth = new SimpleAuthorisation()

    def "If no token provided, no authorisation is returned"() {

        expect:
        auth.fetchAuthentication(request) == Flowable.empty()

        where:

        request << [Stub(HttpRequest) { getHeaders() >> Mock(HttpHeaders) }, Mock(MicronautAwsProxyRequest) {getPath() >> ""}]

    }

    @Unroll
    def "When a lambda provides a token"() {

        given:
        def orgId = 'd7eb7040-cbd6-473b-a48d-09df37297e4e'
        def ssId = 'a841c374-deb5-4958-8643-b3829192fc95'
        AwsProxyRequestContext requestContext = Stub(AwsProxyRequestContext)
        ApiGatewayAuthorizerContext authorizer = Stub(ApiGatewayAuthorizerContext)
        AwsProxyRequest awsRequest = Stub(AwsProxyRequest)
        awsRequest.getRequestContext() >> requestContext
        requestContext.getAuthorizer() >> authorizer
        authorizer.getContextValue("access_token") >> "{\"scope\": \"$scope\", \"client_id\": \"client1\",\"software_id\": \"$ssId\", \"org_id\": \"$orgId\"}"
        MicronautAwsProxyRequest request = new MicronautAwsProxyRequest("", awsRequest, null, null, null)

        when:
        Publisher<Authentication> result = auth.fetchAuthentication(request)

        then:
        Authentication authentication
        result.subscribe {
            authentication = it
        }
        authentication.name == "ALLOWED"
        authentication.attributes['roles'] == roles
        request.getAttribute('clientId').get() == 'client1'
        request.getAttribute('orgId').get() == orgId
        request.getAttribute('ssId').get() == ssId

        where:
        scope  | roles
        'consents'  | ['CONSENTS_MANAGE']
        'payments'  | ['PAYMENTS_MANAGE']
        'op:payments'  | ['PAYMENTS_FULL_MANAGE']
        'accounts'  | ['ACCOUNTS_READ']
        'consents payments' | ['CONSENTS_MANAGE', 'PAYMENTS_MANAGE']

    }

    def "When http provides a token"() {

        given:
        PlainObject plainObject = new PlainObject(new PlainHeader(), new Payload([scope: scope]))
        def bearerToken = plainObject.serialize()
        HttpRequest request = Stub(HttpRequest)
        request.getHeaders() >> new SimpleHttpHeaders(["Authorization": "Bearer ${bearerToken}".toString()], null)
        when:
        Publisher<Authentication> result = auth.fetchAuthentication(request)

        then:
        Authentication authentication
        result.subscribe {
            authentication = it
        }
        authentication.name == "ALLOWED"
        authentication.attributes['roles'] == roles

        where:
        scope  | roles
        'consents'  | ['CONSENTS_MANAGE']
        'payments'  | ['PAYMENTS_MANAGE']
        'op:payments'  | ['PAYMENTS_FULL_MANAGE']
        'accounts'  | ['ACCOUNTS_READ']
        'consents payments' | ['CONSENTS_MANAGE', 'PAYMENTS_MANAGE']

    }

}
