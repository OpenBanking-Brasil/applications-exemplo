package com.raidiam.trustframework.bank.controllers

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.raidiam.trustframework.bank.AuthHelper
import com.raidiam.trustframework.bank.TestJwtSigner
import com.raidiam.trustframework.bank.exceptions.TrustframeworkException
import com.raidiam.trustframework.bank.models.generated.OBReadConsentResponse
import com.raidiam.trustframework.bank.services.ConsentService
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.function.aws.proxy.MicronautLambdaContainerHandler
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.runtime.Micronaut
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

@MicronautTest
class ConsentControllerSpec extends Specification {

    private static Context lambdaContext = new MockLambdaContext()
    def mapper = new ObjectMapper()

    MicronautLambdaContainerHandler handler

    ConsentService mockService = Mock(ConsentService)

    CreateConsent consentReq

    ResponseConsentFull consentInt
    UpdateConsent updateConsent

    ResponseConsent consentResp
    Page<OBReadConsentResponse> consentPage

    def setup() {
        mapper.findAndRegisterModules();
        handler = new MicronautLambdaContainerHandler(Micronaut.build().singletons(mockService, TestJwtSigner.JWT_SIGNER))

        consentReq = ConsentFactory.createConsent()
        def consentData = consentReq.data

        ResponseConsentData respData = new ResponseConsentData()
        respData.setPermissions([ResponseConsentData.PermissionsEnum.RESOURCES_READ, ResponseConsentData.PermissionsEnum.ACCOUNTS_BALANCES_READ, ResponseConsentData.PermissionsEnum.ACCOUNTS_READ])
        respData.setStatusUpdateDateTime(Instant.now().atOffset(ZoneOffset.UTC))
        respData.setCreationDateTime(Instant.now().atOffset(ZoneOffset.UTC))
        respData.setTransactionFromDateTime(consentData.getTransactionFromDateTime())
        respData.setTransactionToDateTime(consentData.getTransactionToDateTime())
        respData.setExpirationDateTime(consentData.getExpirationDateTime())
        consentResp = new ResponseConsent().data(respData)

        ResponseConsentFullData intData = new ResponseConsentFullData()
        .consentId("abc")
        .status(ResponseConsentFullData.StatusEnum.AWAITING_AUTHORISATION)
        intData.setLinkedAccountIds(List.of("123"))
        intData.setClientId("banana")
        intData.setPermissions([ResponseConsentFullData.PermissionsEnum.ACCOUNTS_READ, ResponseConsentFullData.PermissionsEnum.ACCOUNTS_BALANCES_READ])
        intData.setExpirationDateTime((Instant.now() + Duration.ofDays(3)).atOffset(ZoneOffset.UTC))
        intData.setTransactionFromDateTime((Instant.now() - Duration.ofDays(3)).atOffset(ZoneOffset.UTC))
        intData.setExpirationDateTime((Instant.now() - Duration.ofDays(3)).atOffset(ZoneOffset.UTC))
        intData.setTransactionToDateTime((Instant.now() + Duration.ofDays(3)).atOffset(ZoneOffset.UTC))
        intData.creationDateTime(Instant.now().atOffset(ZoneOffset.UTC))
        intData.statusUpdateDateTime(Instant.now().atOffset(ZoneOffset.UTC))
        intData.loggedUser(new LoggedUser()
                .document(new LoggedUserDocument()
                        .identification("11111111111")
                        .rel("PPP")))
        consentInt = new ResponseConsentFull().data(intData)

        updateConsent = new UpdateConsent()
        updateConsent.setData(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED))

        consentPage = Page.of(List.of(consentResp), Pageable.unpaged(), 1)
    }

    def cleanup() {
        handler.close()
    }

    void "we can create a consent"() {
        given:
        mockService.createConsent(_, 'client1') >> consentResp

        String json = mapper.writeValueAsString(consentReq)
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/consents/v1/consents', HttpMethod.POST.toString()).body(json)
        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.CREATED.code
        response.body != null
        ResponseConsent ret = mapper.readValue(response.body, ResponseConsent)
        def requestedPermissions = ret.data.permissions.collect {it.toString()}.sort()
        def createdPermissions = consentReq.data.permissions.collect { it.toString()}.sort()
        requestedPermissions == createdPermissions
        ret.links != null
        ret.meta != null

        and:
        response.multiValueHeaders.containsKey('x-fapi-interaction-id')

    }

    void "we can get all consents"() {
        given:

        mockService.getConsents(_) >> consentPage

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/consents/v1/consents', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "we can get a consent"() {
        given:
        mockService.getConsent(_, _) >> consentResp

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/consents/v1/consents/abc123', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
        ResponseConsent actual = mapper.readValue(response.body, ResponseConsent)
    }

    void "we can't get a consent created by someone else"() {
        given:
        mockService.getConsent(_, _) >> { throw new HttpStatusException(HttpStatus.FORBIDDEN, "") }

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/consents/v1/consents/abc123', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.FORBIDDEN.code
    }

    void "the OP gets a full consent response"() {
        given:
        mockService.getConsentFull(_) >> consentInt

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/consents/v1/consents/abc123', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "op:consent", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
        ResponseConsentFull actual = mapper.readValue(response.body, ResponseConsentFull)
        actual.data.loggedUser != null
        actual.data.linkedAccountIds != null
    }

    void "we can update a consent"() {
        given:
        mockService.updateConsent(_,_) >> consentInt

        String json = mapper.writeValueAsString(updateConsent)
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/consents/v1/consents/abc123', HttpMethod.PUT.toString()).body(json)
        AuthHelper.authorize(scopes: "op:consent", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "we need the correct scope to update a consent"() {
        given:
        mockService.updateConsent(_,_) >> consentInt

        String json = mapper.writeValueAsString(consentInt)
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/consents/v1/consents/abc123', HttpMethod.PUT.toString()).body(json)
        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.FORBIDDEN.code
    }

    void "we can delete a consent"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/consents/v1/consents/abc123', HttpMethod.DELETE.toString())
        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.NO_CONTENT.code
    }

    void "We need the correct scope to proceed"() {

        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/consents/v1/consents/abc123', HttpMethod.GET.toString())
        AuthHelper.authorize(builder, scopes: "org:nobody")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.FORBIDDEN.code

    }

    void "We need a known scope to proceed"() {

        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/consents/v1/consents/abc123', HttpMethod.GET.toString())
        AuthHelper.authorize(builder, scopes: "org:jeff")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.FORBIDDEN.code

    }

    void "We need a scope at all to proceed"() {

        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/consents/v1/consents/abc123', HttpMethod.GET.toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.UNAUTHORIZED.code

    }

    void "we dont need to provide an interaction id"() {
        given:
        mockService.createConsent(_, 'client1') >> consentResp

        String json = mapper.writeValueAsString(consentReq)
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/consents/v1/consents', HttpMethod.POST.toString()).body(json)
        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.CREATED.code
        response.body != null
        response.multiValueHeaders.containsKey('x-fapi-interaction-id')

    }

    void "we can provide an interaction id"() {
        given:
        mockService.createConsent(_, 'client1') >> consentResp
        String interactionId = UUID.randomUUID().toString()

        String json = mapper.writeValueAsString(consentReq)
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/consents/v1/consents', HttpMethod.POST.toString()).body(json)
        builder.header('x-fapi-interaction-id', interactionId)
        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.CREATED.code
        response.body != null
        response.multiValueHeaders.containsKey('x-fapi-interaction-id')
        response.multiValueHeaders.get('x-fapi-interaction-id')[0] == interactionId

    }

    void "date format being wrong is a bad request"() {
        given:
        mockService.createConsent(_, 'client1') >> consentResp

        String json = getClass().classLoader.getResourceAsStream("createConsent_bad_date.json").text
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/consents/v1/consents', HttpMethod.POST.toString()).body(json)
        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code

    }

}
