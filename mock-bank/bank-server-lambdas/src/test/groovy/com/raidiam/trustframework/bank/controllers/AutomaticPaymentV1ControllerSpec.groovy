package com.raidiam.trustframework.bank.controllers

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.raidiam.trustframework.bank.AuthHelper
import com.raidiam.trustframework.bank.TestJwtSigner
import com.raidiam.trustframework.bank.jwt.DefaultJwksFetcher
import com.raidiam.trustframework.bank.jwt.JwksFetcher
import com.raidiam.trustframework.bank.services.PaymentConsentService
import com.raidiam.trustframework.bank.services.PaymentsService
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.context.annotation.Replaces
import io.micronaut.function.aws.proxy.MicronautLambdaContainerHandler
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpStatus
import io.micronaut.runtime.Micronaut
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Specification

import java.time.LocalDate
import java.time.OffsetDateTime

import static com.raidiam.trustframework.bank.TestRequestDataFactory.*

@MicronautTest
class AutomaticPaymentV1ControllerSpec extends Specification {
    private static Context lambdaContext = new MockLambdaContext()
    def mapper = new ObjectMapper()

    MicronautLambdaContainerHandler handler

    PaymentsService mockService = Mock(PaymentsService)
    PaymentConsentService mockConsentService = Mock(PaymentConsentService)

    @Replaces(DefaultJwksFetcher.class)
    static class MockJwksFetcher implements JwksFetcher {

        @Override
        JWKSet findForOrg(String orgId) {
            return TestJwtSigner.JWKS
        }
    }

    @Shared
    ResponseRecurringConsent responseRecurringConsent

    @Shared
    ResponseRecurringPixPayments responseRecurringPixPayments

    @Shared
    ResponsePaymentConsent responseConsent

    @Shared
    UpdatePaymentConsent updatePaymentConsent

    @Shared
    ResponsePaymentConsentFull consentFull

    def setup() {
        mapper.findAndRegisterModules()
        handler = new MicronautLambdaContainerHandler(Micronaut.build().singletons(mockService, mockConsentService, new MockJwksFetcher(), TestJwtSigner.JWT_SIGNER))

        def creditors = new Creditors()
        creditors.add(new Identification().personType("PESSOA_NATURAL").cpfCnpj("12345678901").name("name"))
        responseRecurringConsent = new ResponseRecurringConsent()
                .data(new ResponseRecurringConsentData()
                        .recurringConsentId("id")
                        .statusUpdateDateTime(OffsetDateTime.now())
                        .loggedUser(new LoggedUser()
                                .document(new LoggedUserDocument()
                                        .rel("CPF")
                                        .identification("12345678901")))
                        .status(EnumAuthorisationStatusType.AUTHORISED)
                        .creditors(creditors)
                        .startDateTime(OffsetDateTime.now())
                        .creationDateTime(OffsetDateTime.now())
                        .expirationDateTime(OffsetDateTime.now())
                        .recurringConfiguration(new RecurringConfiguration()
                                .sweeping(new SweepingSweeping())))

        responseRecurringPixPayments = new ResponseRecurringPixPayments()
                .data(new ResponseRecurringPixPaymentsData()
                        .recurringConsentId("urn:raidiam:C1DD33123")
                        .recurringPaymentId(UUID.randomUUID().toString())
                        .payment(new PaymentPix().amount("100.00").currency("BLR"))
                        .proxy("proxy")
                        .ibgeTownCode("5300108")
                        .remittanceInformation("remittanceInformation")
                        .statusUpdateDateTime(OffsetDateTime.now())
                        .creationDateTime(OffsetDateTime.now()))

        updatePaymentConsent = new UpdatePaymentConsent()
        updatePaymentConsent.setData(new UpdatePaymentConsentData().status(UpdatePaymentConsentData.StatusEnum.AUTHORISED))

        responseConsent = createPaymentConsentResponse("consent1", OffsetDateTime.now(), OffsetDateTime.now(), OffsetDateTime.now(),
                ResponsePaymentConsentData.StatusEnum.AUTHORISED, "LUID1", "LUREL1", "BEID1", "BEREL1",
                EnumCreditorPersonType.NATURAL, "BORKBORKBORK", "Kate Human", "BRL", "1", LocalDate.now(), EnumPaymentType.PIX.toString(),
                "ISPB1", "ISSUER1", "AC1", EnumAccountPaymentsType.CACC)

        consentFull = new ResponsePaymentConsentFull()
                .data(new ResponsePaymentConsentFullData()
                        .status(ResponsePaymentConsentFullData.StatusEnum.AWAITING_AUTHORISATION)
                        .clientId("client1")
                        .expirationDateTime(OffsetDateTime.now())
                        .consentId("somepaymentconsent")
                )
    }

    def signEntity(String entity) {
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("mockbank")
                .issuer("issuer")
                .issueTime(new Date())
                .jwtID("arandomjti")
                .build()
        TestJwtSigner.sign(entity, otherClaims)
    }


    def "we cannot call recurring endpoints without recurring-payments scope"() {
        given:

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/automatic-payments/v1/${endpoint}", method.toString())
        if (request != null) {
            String entity = signEntity(mapper.writeValueAsString(request))
            builder.body(entity)
        }

        if (clientCredential) {
            AuthHelper.authorize(scopes: "payments", builder)
        } else {
            AuthHelper.authorizeAuthorizationCodeGrant(scopes: "payments", builder)
        }
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.FORBIDDEN.code

        where:
        request                                                                  | endpoint                        | method             |  clientCredential
        createRecurringPaymentConsentRequestV1WithSweeping("CPF", "12345678901") | "recurring-consents"            | HttpMethod.POST    |   true
        null                                                                     | "recurring-consents/testId"     | HttpMethod.GET     |   true
        createPatchRecurringPaymentConsentRequestV1Rejected()                    | "recurring-consents/testId"     | HttpMethod.PATCH   |   true
        createRecurringPixPayment("urn:raidiam:C1DD33123")                       | "pix/recurring-payments"        | HttpMethod.POST    |   false
        null                                                                     | "pix/recurring-payments/testId" | HttpMethod.GET     |   true
        createPatchRecurringPixPaymentRequestV1Cancelled("CPF", "12345678901")   | "pix/recurring-payments/testId" | HttpMethod.PATCH   |   true
        null                                                                     | "pix/recurring-payments"        | HttpMethod.GET     |   true
    }

    def "we cannot call recurring endpoints using plain JSON"() {
        given:
        String entity = mapper.writeValueAsString(request)
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/automatic-payments/v1/${endpoint}", method.toString()).body(entity)

        if (clientCredential) {
            AuthHelper.authorize(scopes: "recurring-payments", builder)
        } else {
            AuthHelper.authorizeAuthorizationCodeGrant(scopes: "recurring-payments", builder)
        }
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code
        response.body.contains("Unable to parse JWT")
        where:
        request                                                                  | endpoint                        | method             |  clientCredential
        createRecurringPaymentConsentRequestV1WithSweeping("CPF", "12345678901") | "recurring-consents"            | HttpMethod.POST    |   true
        createPatchRecurringPaymentConsentRequestV1Rejected()                    | "recurring-consents/testId"     | HttpMethod.PATCH   |   true
        createRecurringPixPayment("urn:raidiam:C1DD33123")                       | "pix/recurring-payments"        | HttpMethod.POST    |   false
        createPatchRecurringPixPaymentRequestV1Cancelled("CPF", "12345678901")   | "pix/recurring-payments/testId" | HttpMethod.PATCH   |   true
    }


    def "we cannot call recurring endpoints without x-fapi-interaction-id"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/automatic-payments/v1/${endpoint}", method.toString())
        if (request != null) {
            String entity = signEntity(mapper.writeValueAsString(request))
            builder.body(entity)
        }
        if (clientCredential) {
            AuthHelper.authorize(scopes: "recurring-payments", builder)
        } else {
            AuthHelper.authorizeAuthorizationCodeGrant(scopes: "recurring-payments", builder)
        }

        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")
        builder.header("x-idempotency-key", "idempotencyToken1")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code
        response.body.contains("No x-fapi-interaction-id in the request")

        where:
        request                                                                  | endpoint                         | method             |  clientCredential
        createRecurringPaymentConsentRequestV1WithSweeping("CPF", "12345678901") | "recurring-consents"             | HttpMethod.POST    |  true
        null                                                                     | "recurring-consents/testeId"     | HttpMethod.GET     |  true
        createPatchRecurringPaymentConsentRequestV1Rejected()                    | "recurring-consents/testId"      | HttpMethod.PATCH   |  true
        createRecurringPixPayment("urn:raidiam:C1DD33123")                       | "pix/recurring-payments"         | HttpMethod.POST    |  false
        null                                                                     | "pix/recurring-payments/testeId" | HttpMethod.GET     |  true
        null                                                                     | "pix/recurring-payments"         | HttpMethod.GET     |  true
    }

    def "we can create a payment consent as a signed JWT"() {
        given:

        def req = createRecurringPaymentConsentRequestV1WithSweeping("CPF", "12345678901")

        mockConsentService.createRecurringConsentV1(_ as String, _ as String, _ as String, _ as CreateRecurringConsentV1) >> responseRecurringConsent

        String entity = signEntity(mapper.writeValueAsString(req))


        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/automatic-payments/v1/recurring-consents', HttpMethod.POST.toString()).body(entity)
        AuthHelper.authorize(scopes: "recurring-payments", org_id: "issuer", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.CREATED.code
        response.multiValueHeaders['Content-Type'] == ['application/jwt']
        def body = response.body

        when:
        def jwt = SignedJWT.parse(body)

        then:
        noExceptionThrown()

        when:
        def claims = jwt.getJWTClaimsSet()

        then:
        claims.getAudience().contains('issuer')
        def claimsData = mapper.writeValueAsString(claims.getClaim("data"))
        def responseData = mapper.readValue(claimsData, ResponseRecurringConsentData.class)
        responseData
        responseData.getRecurringConfiguration().getSweeping()
    }

    def "we can create a payment consent without recurringConfiguration"() {
        given:

        def req = createRecurringPaymentConsentRequestV1("CPF", "12345678901")
        mockConsentService.createRecurringConsentV1(_ as String, _ as String, _ as String, _ as CreateRecurringConsentV1) >> responseRecurringConsent
        String entity = signEntity(mapper.writeValueAsString(req))

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/automatic-payments/v1/recurring-consents', HttpMethod.POST.toString()).body(entity)
        AuthHelper.authorize(scopes: "recurring-payments", org_id: "issuer", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.CREATED.code
        response.multiValueHeaders['Content-Type'] == ['application/jwt']
    }

    def "we can retrieve a payment consent"() {
        given:

        def req = createRecurringPaymentConsentRequestV1("CPF", "12345678901")
        mockConsentService.getRecurringConsentsV1(_ as String, _ as String) >> responseRecurringConsent

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/automatic-payments/v1/recurring-consents/testeID', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "recurring-payments", org_id: "issuer", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.multiValueHeaders['Content-Type'] == ['application/jwt']
        response.body != null
    }

    def "we can change a payment consent as a signed JWT"() {
        given:

        def req = createPatchRecurringPaymentConsentRequestV1Rejected()

        mockConsentService.patchRecurringConsentV1(_ as String, _ as String, _ as PatchRecurringConsentV1) >> responseRecurringConsent

        String entity = signEntity(mapper.writeValueAsString(req))


        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/automatic-payments/v1/recurring-consents/testId', HttpMethod.PATCH.toString()).body(entity)
        AuthHelper.authorize(scopes: "recurring-payments", org_id: "issuer", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.multiValueHeaders['Content-Type'] == ['application/jwt']
        def body = response.body

        when:
        def jwt = SignedJWT.parse(body)

        then:
        noExceptionThrown()

        when:
        def claims = jwt.getJWTClaimsSet()

        then:
        claims.getAudience().contains('issuer')
        def claimsData = mapper.writeValueAsString(claims.getClaim("data"))
        def responseData = mapper.readValue(claimsData, ResponseRecurringConsentData.class)
        responseData
        responseData.getRecurringConfiguration().getSweeping()
    }

    def "we cannot call post recurring pix payment with client_credentials"() {
        given:
        def req = createRecurringPixPayment("urn:raidiam:C1DD33123")
        String entity = signEntity(mapper.writeValueAsString(req))
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/automatic-payments/v1/pix/recurring-payments", HttpMethod.POST.toString()).body(entity)

        AuthHelper.authorize(scopes: "recurring-payments", builder)
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")
        builder.header("x-idempotency-key", UUID.randomUUID().toString())
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.UNAUTHORIZED.code
        response.body.contains("does not accept client_credentials token - returning 401")
    }

    def "we can create a pix payment as a signed JWT"() {
        given:
        mockService.createRecurringPixPaymentV1("urn:raidiambank:C1DD33123", _ as String, _ as String, _ as String, _ as CreateRecurringPixPaymentV1) >> responseRecurringPixPayments
        def req = createRecurringPixPayment("urn:raidiambank:C1DD33123")

        String entity = signEntity(mapper.writeValueAsString(req))
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/automatic-payments/v1/pix/recurring-payments', HttpMethod.POST.toString()).body(entity)
        AuthHelper.authorizeAuthorizationCodeGrant(scopes: "recurring-payments consent:urn:raidiambank:C1DD33123", org_id: "issuer", builder)
        builder.header("x-idempotency-key", UUID.randomUUID().toString())
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.CREATED.code
        response.multiValueHeaders['Content-Type'] == ['application/jwt']
        def body = response.body

        when:
        def jwt = SignedJWT.parse(body)

        then:
        noExceptionThrown()

        when:
        def claims = jwt.getJWTClaimsSet()

        then:
        claims.getAudience().contains('issuer')
        def claimsData = mapper.writeValueAsString(claims.getClaim("data"))
        def responseData = mapper.readValue(claimsData, ResponseRecurringPixPaymentsData.class)
        responseData
    }

    def "we can retrieve a recurring pix payment"() {
        given:

        def req = createRecurringPixPayment("urn:raidiambank:C1DD33123")
        mockService.getRecurringPixPaymentV1(_ as String, _ as String) >> responseRecurringPixPayments

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/automatic-payments/v1/pix/recurring-payments/testeID', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "recurring-payments consent:urn:raidiambank:C1DD33123", org_id: "issuer", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.multiValueHeaders['Content-Type'] == ['application/jwt']
        response.body != null
    }

    def "we can change a pix payment as a signed JWT"() {
        given:

        def req = createPatchRecurringPixPaymentRequestV1Cancelled("CPF", "12345678901")

        mockService.patchRecurringPixPaymentV1(_ as String, _ as String, _ as RecurringPatchPixPayment) >> responseRecurringPixPayments

        String entity = signEntity(mapper.writeValueAsString(req))


        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/automatic-payments/v1/pix/recurring-payments/testId', HttpMethod.PATCH.toString()).body(entity)
        AuthHelper.authorize(scopes: "recurring-payments consent:urn:raidiambank:C1DD33123", org_id: "issuer", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.multiValueHeaders['Content-Type'] == ['application/jwt']
        def body = response.body

        when:
        def jwt = SignedJWT.parse(body)

        then:
        noExceptionThrown()

        when:
        def claims = jwt.getJWTClaimsSet()

        then:
        claims.getAudience().contains('issuer')
        def claimsData = mapper.writeValueAsString(claims.getClaim("data"))
        def responseData = mapper.readValue(claimsData, ResponseRecurringPixPaymentsData.class)
        responseData
    }

    def "we can retrieve a recurring pix payment by consentId"() {
        given:

        def req = createRecurringPixPayment("urn:raidiambank:C1DD33123")
        mockService.getRecurringPixPaymentByConsentIdV1(_ as String, _ as LocalDate, _ as LocalDate, _ as String) >> new ResponseRecurringPixPaymentByConsent().data(List.of(responseRecurringPixPayments.getData()))

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/automatic-payments/v1/pix/recurring-payments', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "recurring-payments", org_id: "issuer", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("recurringConsentId", "urn:raidiambank:C1DD33123")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.multiValueHeaders['Content-Type'] == ['application/jwt']
        response.body != null
    }

    def "we cannt retrieve a recurring pix payment by consentId without consentId"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/automatic-payments/v1/pix/recurring-payments', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "recurring-payments", org_id: "issuer", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.UNAUTHORIZED.code
        response.body.contains("Request has no associated recurring consent Id")
    }

    def "we can PUT consent"(){
        given:
        mockConsentService.updateRecurringConsentV1(_ as String, _ as UpdatePaymentConsent) >> responseConsent

        String entity = mapper.writeValueAsString(updatePaymentConsent)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/automatic-payments/v1/recurring-consents/consent1', HttpMethod.PUT.toString()).body(entity)
        AuthHelper.authorize(scopes: "op:payments", org_id: "issuer", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "the OP gets a full recurring payment consent response"() {
        given:
        mockConsentService.getConsentFull(_ as String) >> consentFull

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/automatic-payments/v1/recurring-consents/consentid', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "op:payments", org_id: "issuer", builder)
        builder.header("Accept", "application/jwt")
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null

        def jwt = SignedJWT.parse(response.body)
        def claims = jwt.getJWTClaimsSet()
        def claimsData = mapper.writeValueAsString(claims.getClaim("data"))
        String body = "{\"data\":" + claimsData + "}"
        ResponsePaymentConsentFull actual = mapper.readValue(body, ResponsePaymentConsentFull)
        actual.data.clientId == "client1"
    }

    def cleanup() {
        handler.close()
    }
}
