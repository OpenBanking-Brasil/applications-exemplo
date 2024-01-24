package com.raidiam.trustframework.bank.controllers

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.raidiam.trustframework.bank.AuthHelper
import com.raidiam.trustframework.bank.AwsProxyHelper
import com.raidiam.trustframework.bank.TestJwtSigner
import com.raidiam.trustframework.bank.TestRequestDataFactory
import com.raidiam.trustframework.bank.jwt.DefaultJwksFetcher
import com.raidiam.trustframework.bank.jwt.JwksFetcher
import com.raidiam.trustframework.bank.services.PaymentConsentService
import com.raidiam.trustframework.bank.services.PaymentsService
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.context.annotation.Replaces
import io.micronaut.function.aws.proxy.MicronautLambdaContainerHandler
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.runtime.Micronaut
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Shared
import spock.lang.Specification

import java.time.LocalDate
import java.time.OffsetDateTime

@MicronautTest
class PaymentV4ControllerSpec extends Specification {
    @Shared
    Logger log = LoggerFactory.getLogger(PaymentV4ControllerSpec.class)

    private static Context lambdaContext = new MockLambdaContext()
    def mapper = new ObjectMapper()

    MicronautLambdaContainerHandler handler

    PaymentsService mockService = Mock(PaymentsService)
    PaymentConsentService mockConsentService = Mock(PaymentConsentService)

    @Replaces(DefaultJwksFetcher.class)
    static class MockJwksFetcher implements JwksFetcher  {

        @Override
        JWKSet findForOrg(String orgId) {
            return TestJwtSigner.JWKS
        }
    }

    @Shared
    ResponsePaymentConsentFull consentFull

    @Shared
    ResponsePaymentConsentV4 responseConsentV4

    @Shared
    ResponseCreatePaymentConsentV4 responseCreatePaymentConsentV4
    @Shared
    ResponsePixPaymentV4 responsePixPaymentV4

    @Shared
    ResponsePixPaymentReadV4 responsePixPaymentReadV4

    @Shared
    ResponsePatchPixConsentV4 responsePatchPixConsent


    def setup () {
        mapper.findAndRegisterModules()
        handler = new MicronautLambdaContainerHandler(Micronaut.build().singletons(mockService, mockConsentService, new MockJwksFetcher(), TestJwtSigner.JWT_SIGNER))

        responseCreatePaymentConsentV4 = TestRequestDataFactory.createPaymentConsentResponseV4("consent1", OffsetDateTime.now(), OffsetDateTime.now(), OffsetDateTime.now(),
                EnumAuthorisationStatusType.AUTHORISED, "LUID1", "LUREL1", "BEID1", "BEREL1",
                EnumCreditorPersonType.NATURAL, "BORKBORKBORK", "Kate Human", "BRL", "1", LocalDate.now(), EnumPaymentType.PIX,
                "ISPB1", "ISSUER1", "AC1", EnumAccountPaymentsType.CACC)

        responsePixPaymentV4 = TestRequestDataFactory.createPixPaymentResponseV4("paymentId", "E9040088820210128000800123873170", "consentId",
                OffsetDateTime.now(), OffsetDateTime.now(), "proxy", EnumPaymentStatusTypeV2.ACSC, null,
                EnumLocalInstrument.QRES, "1", "BRL", "blah", "ispb", "issuer",
                "acno", EnumAccountPaymentsType.CACC, null)

        responsePixPaymentReadV4 = TestRequestDataFactory.createPixPaymentResponseReadV4("paymentId", "E9040088820210128000800123873170", "consentId",
                OffsetDateTime.now(), OffsetDateTime.now(), "proxy", EnumPaymentStatusTypeV2.ACSC, null,
                EnumLocalInstrument.QRES, "1", "BRL", "blah", "ispb", "issuer",
                "acno", EnumAccountPaymentsType.CACC, null)

        responseConsentV4 = TestRequestDataFactory.createGetPaymentConsentResponseV4("consent1", OffsetDateTime.now(), OffsetDateTime.now(), OffsetDateTime.now(),
                EnumAuthorisationStatusType.AUTHORISED, "LUID1", "LUREL1", "BEID1", "BEREL1",
                EnumCreditorPersonType.NATURAL, "BORKBORKBORK", "Kate Human", "BRL", "1", LocalDate.now(), EnumPaymentType.PIX.toString(),
                "ISPB1", "ISSUER1", "AC1", EnumAccountPaymentsType.CACC)

        responsePatchPixConsent = TestRequestDataFactory.createPatchPixConsentV4Response("paymentId")

        consentFull = new ResponsePaymentConsentFull()
                .data(new ResponsePaymentConsentFullData()
                        .status(ResponsePaymentConsentFullData.StatusEnum.AWAITING_AUTHORISATION)
                        .clientId("client1")
                        .expirationDateTime(OffsetDateTime.now())
                        .consentId("somepaymentconsent")
                )
    }

    def "we cannot create a payment consent using plain JSON" () {
        given:
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("12345678901234", "RELA", "12345678901",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.SLRY, "12341234",
                "1234", "12345678", "ABC", "12345678901", EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100.00")

        mockConsentService.createConsentV4(_ as String, _ as String, _ as String, _ as CreatePaymentConsentV4) >> responseCreatePaymentConsentV4

        String entity = mapper.writeValueAsString(paymentConsentRequest)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v4/consents', HttpMethod.POST.toString()).body(entity)
        AuthHelper.authorize(scopes: "payments", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.FORBIDDEN.code

    }

    def "we can create a payment consent as a signed JWT" () {
        given:

        def paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4()

        mockConsentService.createConsentV4(_ as String, _ as String, _ as String, _ as CreatePaymentConsentV4) >> responseCreatePaymentConsentV4

        String entity = mapper.writeValueAsString(paymentConsentRequest)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("mockbank")
                .issuer("issuer")
                .issueTime(new Date())
                .jwtID("arandomjti")
                .build()
        entity = TestJwtSigner.sign(entity, otherClaims)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v4/consents', HttpMethod.POST.toString()).body(entity)
        AuthHelper.authorize(scopes: "payments", org_id: "issuer", builder)
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
        def responseData = mapper.readValue(claimsData, ResponseCreatePaymentConsentV4Data.class)
        responseData
        responseData.getPayment()
    }

    def "when we force a 429 it returns a json error message" () {
        given:

        def paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4("12345678905", "CPF","10429.00")

        mockConsentService.createConsentV4(_ as String, _ as String, _ as String, _ as CreatePaymentConsentV4) >> {  throw new HttpStatusException(HttpStatus.TOO_MANY_REQUESTS,"Too many requests")}

        String entity = mapper.writeValueAsString(paymentConsentRequest)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("mockbank")
                .issuer("issuer")
                .issueTime(new Date())
                .jwtID("arandomjti")
                .build()
        entity = TestJwtSigner.sign(entity, otherClaims)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v4/consents', HttpMethod.POST.toString()).body(entity)
        AuthHelper.authorize(scopes: "payments", org_id: "issuer", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.TOO_MANY_REQUESTS.code
        response.multiValueHeaders['Content-Type'].contains('application/json')
        response.body

        then:
        noExceptionThrown()
    }

    def "we see a 4xx if bad signer and JWT" () {
        given:
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("12345678901234", "RELA", "12345678901",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.SLRY, "12341234",
                "1234","12345678", "ABC", "12345678901", EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100.00")

        mockConsentService.createConsentV4(_ as String, _ as String, _ as String, _ as CreatePaymentConsentV4) >> responseCreatePaymentConsentV4

        String entity = mapper.writeValueAsString(paymentConsentRequest)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("mockbank")
                .issuer("issuer")
                .issueTime(new Date())
                .jwtID("arandomjti")
                .build()
        entity = TestJwtSigner.signBadly(entity, otherClaims)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v4/consents', HttpMethod.POST.toString()).body(entity)
        AuthHelper.authorize(scopes: "payments", org_id: 'issuer', builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code
        response.multiValueHeaders['Content-Type'].contains('application/json')
        response.body

        then:
        noExceptionThrown()

    }

    def "we see a 4xx if missing claim" () {
        given:
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("12345678901234", "RELA", "12345678901",
                "Bob Creditor", EnumCreditorPersonType.NATURAL,EnumAccountPaymentsType.SLRY, "12341234",
                "1234","12345678", "ABC", "12345678901", EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100.00")

        mockConsentService.createConsentV4(_ as String, _ as String, _ as String, _ as CreatePaymentConsentV4) >> responseCreatePaymentConsentV4

        String entity = mapper.writeValueAsString(paymentConsentRequest)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("mockbank")
                .issuer("issuer")
                .jwtID("arandomjti")
                .build()
        entity = TestJwtSigner.signBadly(entity, otherClaims)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v4/consents', HttpMethod.POST.toString()).body(entity)
        AuthHelper.authorize(scopes: "payments", org_id: 'issuer', builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code

        response.multiValueHeaders['Content-Type'].contains('application/json')
        response.body

        then:
        noExceptionThrown()

    }

    def "we see a 4xx if body not jwt" () {
        given:
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4(
                "12345678901234",
                "RELA",
                "12345678901",
                "Bob Creditor",
                EnumCreditorPersonType.NATURAL,
                EnumAccountPaymentsType.SLRY,
                "12341234",
                "1234",
                "12345678",
                "ABC",
                "12345678901",
                EnumPaymentType.PIX.toString(),
                LocalDate.now(),
                "BRL",
                "100.00"
        )

        mockConsentService.createConsentV4(_ as String, _ as String, _ as String, _ as CreatePaymentConsentV4) >> responseCreatePaymentConsentV4

        String entity = mapper.writeValueAsString(paymentConsentRequest)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v4/consents', HttpMethod.POST.toString()).body(entity)
        AuthHelper.authorize(scopes: "payments", org_id: 'org', builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code

        response.multiValueHeaders['Content-Type'].contains('application/json')
        response.body

        then:
        noExceptionThrown()

    }

    def "we see a 4xx if token has a sub and try to get a consent" () {
        given:
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4(
                "12345678901234",
                "RELA",
                "12345678901",
                "Bob Creditor",
                EnumCreditorPersonType.NATURAL,
                EnumAccountPaymentsType.SLRY,
                "12341234",
                "1234",
                "12345678",
                "ABC",
                "12345678901",
                EnumPaymentType.PIX.toString(),
                LocalDate.now(),
                "BRL",
                "100.00"
        )

        mockConsentService.createConsentV4(_ as String, _ as String, _ as String, _ as CreatePaymentConsentV4) >> responseCreatePaymentConsentV4

        String entity = mapper.writeValueAsString(paymentConsentRequest)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v4/consents', HttpMethod.POST.toString()).body(entity)
        AuthHelper.authorizeAuthorizationCodeGrant(scopes: "payments", org_id: 'org', builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.UNAUTHORIZED.code

        response.multiValueHeaders['Content-Type'].contains('application/json')
        response.body

        then:
        noExceptionThrown()

    }

    def "Can create a payment consent if the currency is invalid"() {
        given:
        def paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4()

        paymentConsentRequest.getData().getPayment().setCurrency("ZZZ")
        mockConsentService.createConsentV4(_ as String, _ as String, _ as String, _ as CreatePaymentConsentV4) >> responseCreatePaymentConsentV4


        def entity = AwsProxyHelper.signPayload(paymentConsentRequest)
        AwsProxyRequestBuilder builder = AwsProxyHelper.buildJwtCall('/open-banking/payments/v4/consents', HttpMethod.POST, entity)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.CREATED.getCode()
        response.multiValueHeaders['Content-Type'].contains('application/jwt')
        def body = response.body

        when:
        System.out.println(body)
        def jwt = SignedJWT.parse(body)
        System.out.println(jwt)

        then:
        noExceptionThrown()
    }

    void "get payment consent v4 response"() {
        given:
        mockConsentService.getConsentV4(_ as String, _ as String) >> responseConsentV4
        AwsProxyRequestBuilder builder = AwsProxyHelper.buildJwtCall('/open-banking/payments/v4/consents/consentid', HttpMethod.GET)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:

        def jwt = SignedJWT.parse(response.body)
        def claims = jwt.getJWTClaimsSet()
        def claimsData = mapper.writeValueAsString(claims.getClaim("data"))
        String body = "{\"data\":" + claimsData + "}"

        mapper.readValue(body, ResponsePaymentConsentV3)
        response.statusCode == HttpStatus.OK.code

    }

    def "we can get a payment consent v4 as a signed JWT"() {

        given:
        mockConsentService.getConsentV4(_ as String, _ as String) >> responseConsentV4

        when:
        AwsProxyRequestBuilder builder = AwsProxyHelper.buildJwtCall('/open-banking/payments/v4/consents/consentid', HttpMethod.GET)

        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.multiValueHeaders['Content-Type'] == ['application/jwt']
        def body = response.body

        when:
        def jwt = SignedJWT.parse(body)

        then:
        noExceptionThrown()

        and:
        jwt.getJWTClaimsSet().getAudience().contains('issuer')
    }

    def "we can get a full payment consent v4"() {

        given:
        mockConsentService.getConsentFull(_ as String, _ as String) >> consentFull

        when:
        AwsProxyRequestBuilder builder = AwsProxyHelper.buildManagerJwtCall('/open-banking/payments/v4/consents/consentId', HttpMethod.GET)

        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.multiValueHeaders['Content-Type'] == ['application/jwt']
        def body = response.body

        when:
        def jwt = SignedJWT.parse(body)

        then:
        noExceptionThrown()

        and:
        jwt.getJWTClaimsSet().getAudience().contains('issuer')
    }

    def "Cannot create a payment consent if the date is invalid"() {
        given:
        def paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4()

        paymentConsentRequest.getData().getPayment().setDate(LocalDate.of(2019,12,28))

        mockConsentService.createConsentV4(_ as String, _ as String, _ as String, paymentConsentRequest) >> {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Not a valid date.")
        }

        def entity = AwsProxyHelper.signPayload(paymentConsentRequest)
        AwsProxyRequestBuilder builder = AwsProxyHelper.buildJwtCall('/open-banking/payments/v4/consents', HttpMethod.POST, entity)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.UNPROCESSABLE_ENTITY.getCode()
        response.multiValueHeaders['Content-Type'].contains('application/jwt')
        def body = response.body

        when:
        def jwt = SignedJWT.parse(body)
        System.out.println(jwt)

        then:
        noExceptionThrown()

        when:
        def claims = jwt.getJWTClaimsSet()


        then:
        claims.getAudience().contains('issuer')
        claims.getClaims().containsKey("errors")
        claims.getClaims().get("errors") instanceof ArrayList

    }

    def "we get a 422 if we try to create a payment consent with debtor account type SLRY" () {
        given:
        def paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4()

        mockConsentService.createConsentV4(_ as String, _ as String, _ as String, _ as CreatePaymentConsentV4) >> {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "PARAMENTRO_INVALIDO")
        }

        def entity = AwsProxyHelper.signPayload(paymentConsentRequest)
        AwsProxyRequestBuilder builder = AwsProxyHelper.buildJwtCall('/open-banking/payments/v4/consents', HttpMethod.POST, entity)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.UNPROCESSABLE_ENTITY.code
    }

    def "we can create a payment as a JWT" () {
        given:
        def paymentRequest = TestRequestDataFactory.createPaymentRequestV4("urn:raidiambank:1234", "123456789012", "1234", "12345678", EnumAccountPaymentsType.CACC, EnumLocalInstrument.MANU, "100.00", "BRL", "proxy", "qrcode", "61363003000184","remittanceInfo", null)
        1 * mockService.createPaymentV4("urn:raidiambank:1234", _ as String, _ as String, _ as CreatePixPaymentV4, _ as String) >> responsePixPaymentV4

        def json = AwsProxyHelper.signPayload(paymentRequest)
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v4/pix/payments', HttpMethod.POST.toString()).body(json)
        AuthHelper.authorizeAuthorizationCodeGrant(scopes: "payments consent:urn:raidiambank:1234", org_id: 'issuer', builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.CREATED.code
        response.body
    }

    def "we can get a payment as a signed JWT"() {

        given:
        mockService.getPaymentV4("paymentId", _ as String) >> responsePixPaymentReadV4

        when:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v4/pix/payments/paymentId', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "op:payments", org_id: "issuer", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Accept", "application/jwt")
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.multiValueHeaders['Content-Type'] == ['application/jwt']
        def body = response.body

        when:
        def jwt = SignedJWT.parse(body)

        then:
        noExceptionThrown()

        and:
        jwt.getJWTClaimsSet().getAudience().contains('issuer')
    }

    def "we can cancel a payment by consentId as JWT with client credentials"() {
        given:
        def patchRequest = TestRequestDataFactory.createPatchPixConsentV4Request()
        1 * mockService.patchPaymentByConsentIdV4("consentId", patchRequest) >> responsePatchPixConsent
        def json = AwsProxyHelper.signPayload(patchRequest)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v4/pix/payments/consents/consentId', HttpMethod.PATCH.toString()).body(json)
        AuthHelper.authorize(scopes: "payments consent:urn:raidiambank:1234", org_id: 'issuer', builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code

        when:
        def jwt = SignedJWT.parse(response.body)

        then:
        noExceptionThrown()

        and:
        jwt.getJWTClaimsSet().getAudience().contains('issuer')
    }

    def "we cannot cancel a payment by consentId as JWT with authorisation code"() {
        given:
        def patchRequest = TestRequestDataFactory.createPatchPixConsentV4Request()
        0 * mockService.patchPaymentByConsentIdV4("consentId", patchRequest) >> responsePatchPixConsent
        def json = AwsProxyHelper.signPayload(patchRequest)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v4/pix/payments/consents/consentId', HttpMethod.PATCH.toString()).body(json)
        AuthHelper.authorizeAuthorizationCodeGrant(scopes: "payments consent:urn:raidiambank:1234", org_id: 'issuer', builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.UNAUTHORIZED.code
    }

    def "we can cancel a payment as JWT with client credentials"() {
        given:
        def patchRequest = TestRequestDataFactory.createPatchPixConsentV4Request()
        1 * mockService.patchPaymentV4("paymentId", patchRequest) >> responsePixPaymentReadV4
        def json = AwsProxyHelper.signPayload(patchRequest)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v4/pix/payments/paymentId', HttpMethod.PATCH.toString()).body(json)
        AuthHelper.authorize(scopes: "payments consent:urn:raidiambank:1234", org_id: 'issuer', builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code

        when:
        def jwt = SignedJWT.parse(response.body)

        then:
        noExceptionThrown()

        and:
        jwt.getJWTClaimsSet().getAudience().contains('issuer')
    }

    def "we cannot cancel a payment as JWT with authorisation code"() {
        given:
        def patchRequest = TestRequestDataFactory.createPatchPixConsentV4Request()
        0 * mockService.patchPaymentV4("paymentId", patchRequest) >> responsePixPaymentReadV4
        def json = AwsProxyHelper.signPayload(patchRequest)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v4/pix/payments/paymentId', HttpMethod.PATCH.toString()).body(json)
        AuthHelper.authorizeAuthorizationCodeGrant(scopes: "payments consent:urn:raidiambank:1234", org_id: 'issuer', builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.UNAUTHORIZED.code
    }

    def cleanup() {
        handler.close()
    }
}
