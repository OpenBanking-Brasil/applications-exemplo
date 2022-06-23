package com.raidiam.trustframework.bank.controllers

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.serverless.proxy.model.AwsProxyRequest
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.shaded.json.JSONArray
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.raidiam.trustframework.bank.AuthHelper
import com.raidiam.trustframework.bank.TestJwtSigner
import com.raidiam.trustframework.bank.TestRequestDataFactory
import com.raidiam.trustframework.bank.jwt.DefaultJwksFetcher
import com.raidiam.trustframework.bank.jwt.JwksFetcher
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
class PaymentControllerSpec extends Specification {
    @Shared
    Logger log = LoggerFactory.getLogger(PaymentControllerSpec.class)

    private static Context lambdaContext = new MockLambdaContext()
    def mapper = new ObjectMapper()

    MicronautLambdaContainerHandler handler

    PaymentsService mockService = Mock(PaymentsService)

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
    ResponsePaymentConsent responseConsent

    @Shared
    ResponsePaymentConsentFull responseConsentFull

    @Shared
    ResponsePixPayment responsePixPayment

    def setup () {
        mapper.findAndRegisterModules()
        handler = new MicronautLambdaContainerHandler(Micronaut.build().singletons(mockService, new MockJwksFetcher(), TestJwtSigner.JWT_SIGNER))

        responseConsent = TestRequestDataFactory.createPaymentConsentResponse("consent1", OffsetDateTime.now(), OffsetDateTime.now(), OffsetDateTime.now(),
                ResponsePaymentConsentData.StatusEnum.AUTHORISED, "LUID1", "LUREL1", "BEID1", "BEREL1",
                EnumCreditorPersonType.NATURAL, "BORKBORKBORK", "Kate Human", "BRL", "1", LocalDate.now(), EnumPaymentType.PIX.toString(),
                "ISPB1", "ISSUER1", "AC1", EnumAccountPaymentsType.CACC)

        responseConsentFull = TestRequestDataFactory.createPaymentConsentResponseFull("consent1", OffsetDateTime.now(), OffsetDateTime.now(), OffsetDateTime.now(),
                ResponsePaymentConsentData.StatusEnum.AUTHORISED, "LUID1", "LUREL1", "BEID1", "BEREL1",
                EnumCreditorPersonType.NATURAL, "BORKBORKBORK", "Kate Human", "BRL", "1", LocalDate.now(), EnumPaymentType.PIX.toString(),
                "ISPB1", "ISSUER1", "AC1", EnumAccountPaymentsType.CACC)

        responsePixPayment = TestRequestDataFactory.createPixPaymentResponse("paymentId", "end2endId", "consentId",
                OffsetDateTime.now(), OffsetDateTime.now(), "proxy", EnumPaymentStatusType.ACCC, null,
                EnumLocalInstrument.QRES, "1", "BRL", "blah", "ispb", "issuer",
                "acno", EnumAccountPaymentsType.CACC, null)

        consentFull = new ResponsePaymentConsentFull()
                .data(new ResponsePaymentConsentFullData()
                        .status(ResponsePaymentConsentFullData.StatusEnum.AWAITING_AUTHORISATION)
                        .clientId("client1")
                        .expirationDateTime(OffsetDateTime.now())
                        .consentId("somepaymentconsent")
                )
    }

    def cleanup() {
        handler.close()
    }

    def "we cannot create a payment consent using plain JSON" () {
        given:
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("12345678901234", "RELA", "12345678901",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.SLRY, "12341234",
                "1234", "12345678", "ABC", "12345678901", EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100.00")

        mockService.createConsent(_ as String, _ as String, _ as String, _ as CreatePaymentConsent) >> responseConsent

        String entity = mapper.writeValueAsString(paymentConsentRequest)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v1/consents', HttpMethod.POST.toString()).body(entity)
        AuthHelper.authorize(scopes: "payments", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.FORBIDDEN.code

    }

    def "we can create a payment consent as a signed JWT" () {
        given:

        def paymentConsentRequest = TestRequestDataFactory.testPaymentConsent()

        mockService.createConsent(_ as String, _ as String, _ as String, _ as CreatePaymentConsent) >> responseConsent

        String entity = mapper.writeValueAsString(paymentConsentRequest)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("mockbank")
                .issuer("issuer")
                .issueTime(new Date())
                .jwtID("arandomjti")
                .build()
        entity = TestJwtSigner.sign(entity, otherClaims)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v1/consents', HttpMethod.POST.toString()).body(entity)
        AuthHelper.authorize(scopes: "payments", org_id: "issuer", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")

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
        def responseData = mapper.readValue(claims.getClaim("data").toString(), ResponsePaymentConsentData.class)
        responseData
        responseData.getPayment()
    }

    def "we see a 4xx if bad signer and JWT" () {
        given:
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("12345678901234", "RELA", "12345678901",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.SLRY, "12341234",
                "1234","12345678", "ABC", "12345678901", EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100.00")

        mockService.createConsent(_ as String, _ as String, _ as String, _ as CreatePaymentConsent) >> responseConsent

        String entity = mapper.writeValueAsString(paymentConsentRequest)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("mockbank")
                .issuer("issuer")
                .issueTime(new Date())
                .jwtID("arandomjti")
                .build()
        entity = TestJwtSigner.signBadly(entity, otherClaims)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v1/consents', HttpMethod.POST.toString()).body(entity)
        AuthHelper.authorize(scopes: "payments", org_id: 'issuer', builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")

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
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("12345678901234", "RELA", "12345678901",
                "Bob Creditor", EnumCreditorPersonType.NATURAL,EnumAccountPaymentsType.SLRY, "12341234",
                "1234","12345678", "ABC", "12345678901", EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100.00")

        mockService.createConsent(_ as String, _ as String, _ as String, _ as CreatePaymentConsent) >> responseConsent

        String entity = mapper.writeValueAsString(paymentConsentRequest)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("mockbank")
                .issuer("issuer")
                .jwtID("arandomjti")
                .build()
        entity = TestJwtSigner.signBadly(entity, otherClaims)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v1/consents', HttpMethod.POST.toString()).body(entity)
        AuthHelper.authorize(scopes: "payments", org_id: 'issuer', builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")

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
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest(
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

        mockService.createConsent(_ as String, _ as String, _ as String, _ as CreatePaymentConsent) >> responseConsent

        String entity = mapper.writeValueAsString(paymentConsentRequest)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v1/consents', HttpMethod.POST.toString()).body(entity)
        AuthHelper.authorize(scopes: "payments", org_id: 'org', builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code

        response.multiValueHeaders['Content-Type'].contains('application/json')
        response.body

        then:
        noExceptionThrown()

    }

    def "we cannot create a payment with plain JSON" () {
        given:
        def paymentRequest = TestRequestDataFactory.createPaymentRequest("123456789012", "1234", "12345678", EnumAccountPaymentsType.CACC, EnumLocalInstrument.MANU, "100.00", "BRL", "proxy", "qrcode",  "86325173000103","remittanceInfo", null)

        String json = mapper.writeValueAsString(paymentRequest)
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v1/pix/payments', HttpMethod.POST.toString()).body(json)
        AuthHelper.authorize(scopes: "payments consent:1234", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        0 * mockService.createPayment(_, "1234", _) >> responsePixPayment
        response.statusCode == HttpStatus.FORBIDDEN.code
    }

    def "we can create a payment as a JWT" () {
        given:
        def paymentRequest = TestRequestDataFactory.createPaymentRequest("123456789012", "1234", "12345678", EnumAccountPaymentsType.CACC, EnumLocalInstrument.MANU, "100.00", "BRL", "proxy", "qrcode", "61363003000184","remittanceInfo", null)

        String json = mapper.writeValueAsString(paymentRequest)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("mockbank")
                .issuer("issuer")
                .issueTime(new Date())
                .jwtID(UUID.randomUUID().toString())
                .build()
        json = TestJwtSigner.sign(json, otherClaims)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v1/pix/payments', HttpMethod.POST.toString()).body(json)
        AuthHelper.authorize(scopes: "payments consent:urn:raidiambank:1234", org_id: 'issuer', builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        1 * mockService.createPayment("urn:raidiambank:1234", _ as String, _ as String, _ as CreatePixPayment) >> responsePixPayment
        response.statusCode == HttpStatus.CREATED.code
        response.body
    }

    def "we cannot create a payment if the inbound issuer claim is not the org id" () {
        given:
        def paymentRequest = TestRequestDataFactory.createPaymentRequest("123456789012", "1234", "12345678", EnumAccountPaymentsType.CACC, EnumLocalInstrument.MANU, "100.00", "BRL", "proxy", "qrcode", "61363003000184","remittanceInfo", null)

        String json = mapper.writeValueAsString(paymentRequest)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("audience")
                .issuer("bad_issuer")
                .issueTime(new Date())
                .jwtID("arandomjti")
                .build()
        json = TestJwtSigner.sign(json, otherClaims)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v1/pix/payments', HttpMethod.POST.toString()).body(json)
        AuthHelper.authorize(scopes: "payments consent:1234", org_id: 'real_issuer', software_id: 'ssid', builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        0 * mockService.createPayment(_, "1234", _) >> responsePixPayment
        response.statusCode == HttpStatus.BAD_REQUEST.code
        response.multiValueHeaders['Content-Type'].contains('application/json')
        response.body


        then:
        noExceptionThrown()
    }

    def "we cannot create a payment if the initiating cnpj is bogus" () {
        given:
        mockService.createPayment("urn:raidiambank:1234", "idempotencyToken1", _ as String, _ as CreatePixPayment) >> {  throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unregistered CNPJ")}
        def paymentRequest = TestRequestDataFactory.createPaymentRequest("123456789012", "1234", "12345678", EnumAccountPaymentsType.CACC, EnumLocalInstrument.MANU, "100.00", "BRL", "proxy", "qrcode", "61363003000184","remittanceInfo", null)

        String json = mapper.writeValueAsString(paymentRequest)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("audience")
                .issuer("real_issuer")
                .issueTime(new Date())
                .jwtID(UUID.randomUUID().toString())
                .build()
        json = TestJwtSigner.sign(json, otherClaims)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v1/pix/payments', HttpMethod.POST.toString()).body(json)
        AuthHelper.authorize(scopes: "payments consent:urn:raidiambank:1234", org_id: 'real_issuer', software_id: 'ssid', builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")


        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code
        response.getMultiValueHeaders().get("Content-Type").contains('application/json')

        response.multiValueHeaders['Content-Type'].contains('application/json')
        response.body

        then:
        noExceptionThrown()
    }

    def "we cannot create a payment if the jti has been reused" () {
        given:
        mockService.createPayment("urn:raidiambank:1234", "idempotencyToken1", _ as String, _ as CreatePixPayment) >> {  throw new HttpStatusException(HttpStatus.FORBIDDEN, "DETALHE_PGTO_INVALIDO: Detalhe do pagamento inválido. JTI Reutilizada.")}
        def paymentRequest = TestRequestDataFactory.createPaymentRequest("123456789012", "1234", "12345678", EnumAccountPaymentsType.CACC, EnumLocalInstrument.MANU, "100.00", "BRL", "proxy", "qrcode", "61363003000184","remittanceInfo", null)
        def paymentReques2 = TestRequestDataFactory.createPaymentRequest("123456789012", "1234", "12345678", EnumAccountPaymentsType.CACC, EnumLocalInstrument.MANU, "100.00", "BRL", "proxy", "qrcode", "61363003000184","remittanceInfo", null )

        String jti = UUID.randomUUID().toString()
        String json = mapper.writeValueAsString(paymentRequest)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("audience")
                .issuer("real_issuer")
                .issueTime(new Date())
                .jwtID(jti)
                .build()
        json = TestJwtSigner.sign(json, otherClaims)

        String json2 = mapper.writeValueAsString(paymentReques2)
        JWTClaimsSet anotherClaims = new JWTClaimsSet.Builder()
                .audience("audience")
                .issuer("real_issuer")
                .issueTime(new Date())
                .jwtID(jti)
                .build()
        json2 = TestJwtSigner.sign(json2, anotherClaims)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v1/pix/payments', HttpMethod.POST.toString()).body(json)
        AuthHelper.authorize(scopes: "payments consent:urn:raidiambank:1234", org_id: 'real_issuer', software_id: 'ssid', builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")

        AwsProxyRequestBuilder builder2 = new AwsProxyRequestBuilder('/open-banking/payments/v1/pix/payments', HttpMethod.POST.toString()).body(json2)
        AuthHelper.authorize(scopes: "payments consent:urn:raidiambank:1234", org_id: 'real_issuer', software_id: 'ssid', builder2)
        builder2.header("x-idempotency-key", "idempotencyToken1")
        builder2.header("Content-Type", "application/jwt")
        builder2.header("Accept", "application/jwt")


        when:
        handler.proxy(builder.build(), lambdaContext)
        def response2 = handler.proxy(builder2.build(), lambdaContext)

        then:
        response2.statusCode == HttpStatus.FORBIDDEN.code
        response2.getMultiValueHeaders().get("Content-Type").contains('application/json')

        response2.multiValueHeaders['Content-Type'].contains('application/json')
        response2.body

        then:
        noExceptionThrown()
    }

    void "the OP gets a full payment consent response"() {
        given:
        mockService.getConsentFull(_ as String) >> consentFull

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v1/consents/consentid', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "op:payments", org_id: "issuer", builder)
        builder.header("Accept", "application/jwt")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null

        def jwt = SignedJWT.parse(response.body)
        def claims = jwt.getJWTClaimsSet()
        String body = "{\"data\":" + claims.getClaim("data").toString() + "}"
        ResponsePaymentConsentFull actual = mapper.readValue(body, ResponsePaymentConsentFull)
        actual.data.clientId == "client1"
    }


    def "we can get a payment consent as a signed JWT"() {

        given:
        mockService.getConsentFull(_ as String) >> consentFull

        when:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v1/consents/consentid', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "op:payments", org_id: "issuer", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Accept", "application/jwt")

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

    void "get payment consent response"() {
        given:
        mockService.getConsent(_ as String, _ as String) >> responseConsent

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v1/consents/consentid', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "payments", org_id: "issuer", builder)
        builder.header("Accept", "application/jwt")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:

        def jwt = SignedJWT.parse(response.body)
        def claims = jwt.getJWTClaimsSet()
        String body = "{\"data\":" + claims.getClaim("data").toString() + "}"

        mapper.readValue(body, ResponsePaymentConsent)
        response.statusCode == HttpStatus.OK.code

    }

    def "We can update a consent with signed JWT" () {
        given:
        def paymentConsentRequest = TestRequestDataFactory.testPaymentConsent()

        paymentConsentRequest.getData().getPayment().setCurrency("ZZZ")

        mockService.createConsent(_ as String, _ as String, _ as String, paymentConsentRequest) >> {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid currency")
        }

        String entity = mapper.writeValueAsString(paymentConsentRequest)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("mockbank")
                .issuer("issuer")
                .issueTime(new Date())
                .jwtID("arandomjti")
                .build()
        entity = TestJwtSigner.sign(entity, otherClaims)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v1/consents', HttpMethod.POST.toString()).body(entity)
        AuthHelper.authorize(scopes: "payments", org_id: "issuer", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.UNPROCESSABLE_ENTITY.getCode()
        response.multiValueHeaders['Content-Type'].contains('application/jwt')
        def body = response.body

        when:
        System.out.println(body)
        def jwt = SignedJWT.parse(body)
        System.out.println(jwt)

        then:
        noExceptionThrown()

        when:
        def claims = jwt.getJWTClaimsSet()


        then:
        claims.getAudience().contains('issuer')
        claims.getClaims().containsKey("errors")
        claims.getClaims().get("errors") instanceof JSONArray

    }

    def "Cannot create a payment consent if the currency is invalid"() {
        given:
        def paymentConsentRequest = TestRequestDataFactory.testPaymentConsent()

        paymentConsentRequest.getData().getPayment().setCurrency("ZZZ")

        mockService.createConsent(_ as String, _ as String, _ as String, paymentConsentRequest) >> {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid currency")
        }

        String entity = mapper.writeValueAsString(paymentConsentRequest)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("mockbank")
                .issuer("issuer")
                .issueTime(new Date())
                .jwtID("arandomjti")
                .build()
        entity = TestJwtSigner.sign(entity, otherClaims)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v1/consents', HttpMethod.POST.toString()).body(entity)
        AuthHelper.authorize(scopes: "payments", org_id: "issuer", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.UNPROCESSABLE_ENTITY.getCode()
        response.multiValueHeaders['Content-Type'].contains('application/jwt')
        def body = response.body

        when:
        System.out.println(body)
        def jwt = SignedJWT.parse(body)
        System.out.println(jwt)

        then:
        noExceptionThrown()

        when:
        def claims = jwt.getJWTClaimsSet()


        then:
        claims.getAudience().contains('issuer')
        claims.getClaims().containsKey("errors")
        claims.getClaims().get("errors") instanceof JSONArray

    }

    def "Cannot create a payment consent if the date is invalid"() {
        given:
        def paymentConsentRequest = TestRequestDataFactory.testPaymentConsent()

        paymentConsentRequest.getData().getPayment().setDate(LocalDate.of(2019,12,28))

        mockService.createConsent(_ as String, _ as String, _ as String, paymentConsentRequest) >> {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Not a valid date.")
        }

        String entity = mapper.writeValueAsString(paymentConsentRequest)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("mockbank")
                .issuer("issuer")
                .issueTime(new Date())
                .jwtID("arandomjti")
                .build()
        entity = TestJwtSigner.sign(entity, otherClaims)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v1/consents', HttpMethod.POST.toString()).body(entity)
        AuthHelper.authorize(scopes: "payments", org_id: "issuer", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")

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
        claims.getClaims().get("errors") instanceof JSONArray

    }

    def "we can PATCH consent"(){
        given:

        def paymentPatchRequest = TestRequestDataFactory.testPatchPaymentConsent()

        mockService.patchConsent(_ as String, _ as PatchPaymentsConsent) >> responseConsent

        String entity = mapper.writeValueAsString(paymentPatchRequest)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("mockbank")
                .issuer("issuer")
                .issueTime(new Date())
                .jwtID("arandomjti")
                .build()
        entity = TestJwtSigner.sign(entity, otherClaims)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v1/consents/id', HttpMethod.PATCH.toString()).body(entity)
        AuthHelper.authorize(scopes: "payments", org_id: "issuer", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")

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
        def responseData = mapper.readValue(claims.getClaim("data").toString(), ResponsePaymentConsentData.class)
        responseData
        responseData.getPayment()
    }

    def "We cannot patch a consent with JSON" () {
        given:

        def paymentPatchRequest = TestRequestDataFactory.testPatchPaymentConsent()
        mockService.patchConsent(_ as String, _ as PatchPaymentsConsent) >> responseConsent


        String json = mapper.writeValueAsString(paymentPatchRequest)
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v1/consents/id', HttpMethod.PATCH.toString()).body(json)
        AuthHelper.authorize(scopes: "payments", org_id: "issuer", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/json")
        builder.header("Accept", "application/jwt")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.FORBIDDEN.code
    }

    def "PATCH consent returns error"(){
        given:

        def paymentPatchRequest = TestRequestDataFactory.testPatchPaymentConsent()

        mockService.patchConsent(_ as String, _ as PatchPaymentsConsent) >> {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "patch 422")
        }

        String entity = mapper.writeValueAsString(paymentPatchRequest)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("mockbank")
                .issuer("issuer")
                .issueTime(new Date())
                .jwtID("arandomjti")
                .build()
        entity = TestJwtSigner.sign(entity, otherClaims)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/payments/v1/consents/id', HttpMethod.PATCH.toString()).body(entity)
        AuthHelper.authorize(scopes: "payments", org_id: "issuer", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.UNPROCESSABLE_ENTITY.code
        response.multiValueHeaders['Content-Type'].contains('application/jwt')
        def body = response.body

        when:
        def jwt = SignedJWT.parse(body)

        then:
        noExceptionThrown()

        when:
        def claims = jwt.getJWTClaimsSet()

        then:
        claims.getAudience().contains('issuer')
        claims.getClaims().containsKey("errors")
        claims.getClaims().get("errors") instanceof JSONArray
    }

    def "we always get a jwt response whatever the accept or content type"() {
        given:
        mockService.getConsent(_ as String, _ as String) >> responseConsent
        mockService.getConsentFull(_ as String) >> responseConsentFull
        mockService.getPayment( _ as String) >> responsePixPayment
        def request = buildGet(path, acceptHeader, contentType, scopes)

        when:
        def response = handler.proxy(request, lambdaContext)
        log.info("Response - {}", response.getBody())

        then:
        noExceptionThrown()
        response.statusCode == HttpStatus.OK.code
        response.multiValueHeaders['Content-Type'] == [responsetype]


        when:
        def headers = response.getMultiValueHeaders().get("Content-Type")

        then:
        noExceptionThrown()
        headers
        headers.size() == 1
        headers.contains(responsetype)

        when:
        SignedJWT jwt
        if(responsetype == "application/jwt") {
            jwt = SignedJWT.parse(response.body)
        }

        then:
        if(responsetype == "application/jwt") {
            jwt != null
            jwt.getJWTClaimsSet().getAudience().contains('issuer')
        }

        where:
        acceptHeader       | contentType         | path                                                | scopes        | responsetype
        "application/json" | "application/json"  | '/open-banking/payments/v1/consents/consentid'      | "op:payments" | 'application/json'
        null               | null                | '/open-banking/payments/v1/consents/consentid'      | "payments"    | 'application/jwt'
        "application/json" | null                | '/open-banking/payments/v1/consents/consentid'      | "payments"    | 'application/jwt'
        "application/jwt"  | null                | '/open-banking/payments/v1/consents/consentid'      | "payments"    | 'application/jwt'
        "text/plain"       | null                | '/open-banking/payments/v1/consents/consentid'      | "payments"    | 'application/jwt'
        "banana"           | null                | '/open-banking/payments/v1/consents/consentid'      | "payments"    | 'application/jwt'
        null               | "application/json"  | '/open-banking/payments/v1/consents/consentid'      | "payments"    | 'application/jwt'
        "application/json" | "application/json"  | '/open-banking/payments/v1/consents/consentid'      | "payments"    | 'application/jwt'
        null               | "application/jwt"   | '/open-banking/payments/v1/consents/consentid'      | "payments"    | 'application/jwt'
        "application/jwt"  | "application/jwt"   | '/open-banking/payments/v1/consents/consentid'      | "payments"    | 'application/jwt'
        "banana"           | "banana"            | '/open-banking/payments/v1/consents/consentid'      | "payments"    | 'application/jwt'
        null               | null                | '/open-banking/payments/v1/pix/payments/paymentid'  | "payments"    | 'application/jwt'
        "application/json" | null                | '/open-banking/payments/v1/pix/payments/consentid'  | "payments"    | 'application/jwt'
        "application/jwt"  | null                | '/open-banking/payments/v1/pix/payments/consentid'  | "payments"    | 'application/jwt'
        "text/plain"       | null                | '/open-banking/payments/v1/pix/payments/consentid'  | "payments"    | 'application/jwt'
        "banana"           | null                | '/open-banking/payments/v1/pix/payments/consentid'  | "payments"    | 'application/jwt'
        null               | "application/json"  | '/open-banking/payments/v1/pix/payments/consentid'  | "payments"    | 'application/jwt'
        "application/json" | "application/json"  | '/open-banking/payments/v1/pix/payments/consentid'  | "payments"    | 'application/jwt'
        null               | "application/jwt"   | '/open-banking/payments/v1/pix/payments/consentid'  | "payments"    | 'application/jwt'
        "application/jwt"  | "application/jwt"   | '/open-banking/payments/v1/pix/payments/consentid'  | "payments"    | 'application/jwt'
        "banana"           | "banana"            | '/open-banking/payments/v1/pix/payments/consentid'  | "payments"    | 'application/jwt'
    }

    static AwsProxyRequest buildGet(String path, String acceptHeader, String contentType, String scopes) {
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder(path, HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: scopes, org_id: "issuer", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")

        if(acceptHeader != null) {
            builder.header("Accept", acceptHeader)
        }

        if(contentType != null) {
            builder.header("Content-Type", contentType)
        }

        return builder.build()
    }
}
