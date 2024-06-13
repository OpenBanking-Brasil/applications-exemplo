package com.raidiam.trustframework.bank.controllers

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.raidiam.trustframework.bank.AuthHelper
import com.raidiam.trustframework.bank.TestJwtSigner
import com.raidiam.trustframework.bank.services.ConsentService
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.data.model.Pageable
import io.micronaut.function.aws.proxy.MicronautLambdaContainerHandler
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.runtime.Micronaut
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

import static com.raidiam.trustframework.mockbank.models.generated.EnumConsentPermissions.*

@MicronautTest
class ConsentControllerSpec extends Specification {
    private static Context lambdaContext = new MockLambdaContext()
    def mapper = new ObjectMapper()

    MicronautLambdaContainerHandler handler

    ConsentService mockService = Mock(ConsentService)

    CreateConsent consentReq

    ResponseConsentFullV2 consentInt
    UpdateConsent updateConsent

    ResponseConsentV2 consentResp

    ResponseConsentReadExtensionsV3 consentExtensionResp

    def setup() {
        mapper.findAndRegisterModules()
        handler = new MicronautLambdaContainerHandler(Micronaut.build().singletons(mockService, TestJwtSigner.JWT_SIGNER))

        consentReq = ConsentFactory.createConsent()
        def consentData = consentReq.data

        ResponseConsentV2Data respData = new ResponseConsentV2Data()
        respData.setPermissions([RESOURCES_READ, ACCOUNTS_BALANCES_READ, ACCOUNTS_READ])
        respData.setStatusUpdateDateTime(Instant.now().atOffset(ZoneOffset.UTC))
        respData.setCreationDateTime(Instant.now().atOffset(ZoneOffset.UTC))
        respData.setExpirationDateTime(consentData.getExpirationDateTime())
        consentResp = new ResponseConsentV2().data(respData)

        ResponseConsentFullV2Data intData = new ResponseConsentFullV2Data()
                .consentId("abc")
                .status(EnumConsentStatus.AWAITING_AUTHORISATION)
        intData.setLinkedAccountIds(List.of("123"))
        intData.setClientId("banana")
        intData.setPermissions([ACCOUNTS_READ, ACCOUNTS_BALANCES_READ])
        intData.setExpirationDateTime((Instant.now() + Duration.ofDays(3)).atOffset(ZoneOffset.UTC))
        intData.setExpirationDateTime((Instant.now() - Duration.ofDays(3)).atOffset(ZoneOffset.UTC))
        intData.creationDateTime(Instant.now().atOffset(ZoneOffset.UTC))
        intData.statusUpdateDateTime(Instant.now().atOffset(ZoneOffset.UTC))
        intData.loggedUser(new LoggedUser()
                .document(new Document()
                        .identification("11111111111")
                        .rel("PPP")))
        consentInt = new ResponseConsentFullV2().data(intData)

        updateConsent = new UpdateConsent()
        updateConsent.setData(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED))


        def consentExtensionData = new ResponseConsentReadExtensionsV3Data()
        def consentExtensionMeta = new Meta()
        consentExtensionMeta.setTotalRecords(0)
        consentExtensionMeta.setTotalPages(0)

        consentExtensionResp = new ResponseConsentReadExtensionsV3().data(List.of(consentExtensionData))
        consentExtensionResp.setMeta(consentExtensionMeta)

    }

    def cleanup() {
        handler.close()
    }

    void "we can create a consent"() {
        given:
        mockService.createConsentV2('client1', _ as CreateConsentV2) >> consentResp

        String json = mapper.writeValueAsString(consentReq)
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v2/consents', HttpMethod.POST.toString()).body(json)
        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.CREATED.code
        response.body != null
        ResponseConsentV2 ret = mapper.readValue(response.body, ResponseConsentV2)
        def requestedPermissions = ret.data.permissions.collect { it.toString() }.sort()
        def createdPermissions = consentReq.data.permissions.collect { it.toString() }.sort()
        requestedPermissions == createdPermissions
        ret.links != null
        ret.meta != null

        and:
        response.multiValueHeaders.containsKey('x-fapi-interaction-id')

    }

    void "we can get a consent v2"() {
        given:
        mockService.getConsentV2(_ as String, _ as String) >> new ResponseConsentReadV2()

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v2/consents/abc123', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "we cant get a consent v2 with authorisation code grant"() {
        given:
        mockService.getConsentV2(_ as String, _ as String) >> new ResponseConsentReadV2()

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v2/consents/abc123', HttpMethod.GET.toString())
        AuthHelper.authorizeAuthorizationCodeGrant(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.UNAUTHORIZED.code
        response.body != null
    }

    void "we can't get a consent created by someone else"() {
        given:
        mockService.getConsentV2(_ as String, _ as String) >> { throw new HttpStatusException(HttpStatus.FORBIDDEN, "") }

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v2/consents/abc123', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.FORBIDDEN.code
    }

    void "the OP gets a full consent response"() {
        given:
        mockService.getConsentFullV2(_ as String) >> consentInt

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v2/consents/abc123', HttpMethod.GET.toString())
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
        mockService.updateConsentV2(_ as String, _ as UpdateConsent) >> consentInt

        String json = mapper.writeValueAsString(updateConsent)
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v2/consents/abc123', HttpMethod.PUT.toString()).body(json)
        AuthHelper.authorize(scopes: "op:consent", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "we need the correct scope to update a consent"() {
        given:
        mockService.updateConsent(_ as String, _ as UpdateConsent) >> consentInt

        String json = mapper.writeValueAsString(consentInt)
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v2/consents/abc123', HttpMethod.PUT.toString()).body(json)
        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.FORBIDDEN.code
    }

    void "we can delete a consent"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v2/consents/abc123', HttpMethod.DELETE.toString())
        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.NO_CONTENT.code
    }

    void "We need the correct scope to proceed"() {

        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v2/consents/abc123', HttpMethod.GET.toString())
        AuthHelper.authorize(builder, scopes: "org:nobody")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.FORBIDDEN.code

    }

    void "We need a known scope to proceed"() {

        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v2/consents/abc123', HttpMethod.GET.toString())
        AuthHelper.authorize(builder, scopes: "org:jeff")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.FORBIDDEN.code

    }

    void "We need a scope at all to proceed"() {

        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v2/consents/abc123', HttpMethod.GET.toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.UNAUTHORIZED.code

    }

    void "we dont need to provide an interaction id"() {
        given:
        mockService.createConsentV2('client1', _ as CreateConsentV2) >> consentResp

        String json = mapper.writeValueAsString(consentReq)
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v2/consents', HttpMethod.POST.toString()).body(json)
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
        mockService.createConsentV2('client1', _ as CreateConsentV2) >> consentResp
        String interactionId = UUID.randomUUID().toString()

        String json = mapper.writeValueAsString(consentReq)
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v2/consents', HttpMethod.POST.toString()).body(json)
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
        mockService.createConsent('client1', _ as CreateConsent) >> consentResp

        String json = getClass().classLoader.getResourceAsStream("createConsent_bad_date.json").text
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v2/consents', HttpMethod.POST.toString()).body(json)
        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code

    }

    void "errors are json"() {
        given:
        mockService.createConsent('client1', _ as CreateConsent) >> consentResp

        consentReq.getData().loggedUser(null)
        String json = mapper.writeValueAsString(consentReq)
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v2/consents', HttpMethod.POST.toString()).body(json)
        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code
        response.multiValueHeaders.get('Content-Type').contains('application/json')
        def body = response.body
        body

    }

    def "we can create consent extension"() {
        given:
        mockService.createConsentExtension(_ as String, _) >> new ResponseConsentV2()
        def req = mapper.writeValueAsString(ConsentFactory.createConsentExtends())
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v2/consents/testId/extends', HttpMethod.POST.toString())
                .body(req)
                .header("x-fapi-customer-ip-address", "127.0.0.1")
                .header("x-customer-user-agent", "test")

        AuthHelper.authorizeAuthorizationCodeGrant(scopes: "openid", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.CREATED.code
        response.body != null
    }

    def "we cant create consent extension with client credentials grant"() {
        given:
        def req = mapper.writeValueAsString(ConsentFactory.createConsentExtends())
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder(path, HttpMethod.POST.toString()).body(req)
                .header("x-fapi-customer-ip-address", "127.0.0.1")
                .header("x-customer-user-agent", "test")
                .header("x-fapi-interaction-id", UUID.randomUUID().toString())

        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.UNAUTHORIZED.code
        response.body != null
        where:
        path << [
                "/open-banking/consents/v2/consents/testId/extends",
                "/open-banking/consents/v3/consents/testId/extends"
        ]

    }

    def "we cant create consent extension without x-fapi-customer-ip-address header"() {
        given:
        def req = mapper.writeValueAsString(ConsentFactory.createConsentExtends())
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder(path, HttpMethod.POST.toString()).body(req)
                .header("x-customer-user-agent", "test")
                .header("x-fapi-interaction-id", UUID.randomUUID().toString())

        AuthHelper.authorizeAuthorizationCodeGrant(scopes: "openid", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code
        response.body != null
        response.body.contains("No x-fapi-customer-ip-address in the request")

        where:
        path << [
                "/open-banking/consents/v2/consents/testId/extends",
                "/open-banking/consents/v3/consents/testId/extends"
        ]
    }


    def "we cant create consent extension without x-customer-user-agent header"() {
        given:
        def req = mapper.writeValueAsString(ConsentFactory.createConsentExtends())
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder(path, HttpMethod.POST.toString()).body(req)
                .header("x-fapi-customer-ip-address", "127.0.0.1")
                .header("x-fapi-interaction-id", UUID.randomUUID().toString())

        AuthHelper.authorizeAuthorizationCodeGrant(scopes: "openid", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code
        response.body != null
        response.body.contains("No x-customer-user-agent in the request")

        where:
        path << [
                "/open-banking/consents/v2/consents/testId/extends",
                "/open-banking/consents/v3/consents/testId/extends"
        ]
    }


    def "we can get consent extensions without x-fapi-customer-ip-address header"() {
        given:
        mockService.getConsentExtensions(_ as String) >> new ResponseConsentReadExtends().data(List.of())
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v2/consents/testId/extends', HttpMethod.GET.toString())
        AuthHelper.authorizeAuthorizationCodeGrant(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }


    def "we can create consent v3"() {
        given:
        mockService.createConsentV3(_ as String, _) >> new ResponseConsentV3().data(new ResponseConsentV3Data().consentId("testId"))
        def req = mapper.writeValueAsString(ConsentFactory.createConsentV3("12345678901", "CPF", null))
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v3/consents', HttpMethod.POST.toString())
                .body(req)
                .header("x-fapi-interaction-id", UUID.randomUUID().toString())


        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.CREATED.code
        response.body != null
    }

    def "we cant create consent v3 without x-fapi-interaction-id"() {
        given:
        mockService.createConsentV3(_ as String, _) >> new ResponseConsentV3().data(new ResponseConsentV3Data().consentId("testId"))
        def req = mapper.writeValueAsString(ConsentFactory.createConsentV3("12345678901", "CPF", null))
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v3/consents', HttpMethod.POST.toString())
                .body(req)


        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code
        response.body != null
        response.body.contains("No x-fapi-interaction-id in the request")
    }

    def "we cant create consent v3 with client authorisation code"() {
        given:
        mockService.createConsentV3(_ as String, _) >> new ResponseConsentV3().data(new ResponseConsentV3Data().consentId("testId"))
        def req = mapper.writeValueAsString(ConsentFactory.createConsentV3("12345678901", "CPF", null))
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v3/consents', HttpMethod.POST.toString())
                .body(req)
                .header("x-fapi-interaction-id", UUID.randomUUID().toString())


        AuthHelper.authorizeAuthorizationCodeGrant(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.UNAUTHORIZED.code
        response.body != null
    }


    def "we can get consent v3"() {
        given:
        mockService.getConsentV3("testId", _ as String) >> new ResponseConsentReadV3().data(new ResponseConsentReadV3Data().consentId("testId"))
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v3/consents/testId', HttpMethod.GET.toString())
                .header("x-fapi-interaction-id", UUID.randomUUID().toString())


        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    def "we can get full consent v3"() {
        given:
        mockService.getConsentFullV2("testId") >> new ResponseConsentFullV2().data(new ResponseConsentFullV2Data().consentId("testId"))
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v3/consents/testId', HttpMethod.GET.toString())
                .header("x-fapi-interaction-id", UUID.randomUUID().toString())


        AuthHelper.authorize(scopes: "op:consent", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    def "we cant get consent v3 without x-fapi-interaction-id"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v3/consents/testId', HttpMethod.GET.toString())


        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code
        response.body != null
        response.body.contains("No x-fapi-interaction-id in the request")
    }


    def "we cant get consent v3 with invalid x-fapi-interaction-id"() {
        def interactionId = "test"
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v3/consents/testId', HttpMethod.GET.toString())
                .header("x-fapi-interaction-id", interactionId)


        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.UNPROCESSABLE_ENTITY.code
        response.body != null
        response.body.contains(String.format("x-fapi-interaction-id - %s is invalid", interactionId))
        response.multiValueHeaders.get("x-fapi-interaction-id")[0] != null
        response.multiValueHeaders.get("x-fapi-interaction-id")[0] != interactionId
    }

    def "we cant create consent v3 with authorisation code grant"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v3/consents/testId', HttpMethod.GET.toString())
                .header("x-fapi-interaction-id", UUID.randomUUID().toString())


        AuthHelper.authorizeAuthorizationCodeGrant(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.UNAUTHORIZED.code
        response.body != null
    }

    def "we can delete consent v3"() {
        given:
        mockService.deleteConsentV3("testId", _ as String)
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v3/consents/testId', HttpMethod.DELETE.toString())
                .header("x-fapi-interaction-id", UUID.randomUUID().toString())


        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.NO_CONTENT.code
        response.body == null
    }

    def "we cant delete consent v3 without x-fapi-interaction-id"() {
        given:
        mockService.deleteConsentV3("testId", _ as String)
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v3/consents/testId', HttpMethod.DELETE.toString())


        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code
        response.body != null
        response.body.contains("No x-fapi-interaction-id in the request")
    }

    def "we can create consent extension V3"() {
        given:
        mockService.createConsentExtensionV3(_ as String, _, "127.0.0.1", "test") >> new ResponseConsentV3()
        def req = mapper.writeValueAsString(ConsentFactory.createConsentExtendsV3())
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v3/consents/testId/extends', HttpMethod.POST.toString())
                .body(req)
                .header("x-fapi-customer-ip-address", "127.0.0.1")
                .header("x-customer-user-agent", "test")
                .header("x-fapi-interaction-id", UUID.randomUUID().toString())

        AuthHelper.authorizeAuthorizationCodeGrant(scopes: "openid", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.CREATED.code
        response.body != null
    }

    def "we cant create consent extension without x-fapi-interaction-id header"() {
        given:
        def req = mapper.writeValueAsString(ConsentFactory.createConsentExtendsV3())
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/consents/v3/consents/testId/extends", HttpMethod.POST.toString()).body(req)
                .header("x-fapi-customer-ip-address", "127.0.0.1")
                .header("x-customer-user-agent", "test")

        AuthHelper.authorizeAuthorizationCodeGrant(scopes: "openid", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code
        response.body != null
        response.body.contains("No x-fapi-interaction-id in the request")

    }

    def "we cant create get extension without x-fapi-interaction-id header"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/consents/v3/consents/testId/extensions", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code
        response.body != null
        response.body.contains("No x-fapi-interaction-id in the request")

    }

    def "we can get consent extension V3"() {
        given:
        mockService.getConsentExtensionsV3("testId", _ as Pageable) >> consentExtensionResp
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/consents/v3/consents/testId/extensions', HttpMethod.GET.toString())
                .header("x-fapi-interaction-id", UUID.randomUUID().toString())

        AuthHelper.authorize(scopes: "consents", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }


}
