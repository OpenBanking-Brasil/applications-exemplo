package com.raidiam.trustframework.bank.controllers

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.raidiam.trustframework.bank.AuthHelper
import com.raidiam.trustframework.bank.TestDataFactory
import com.raidiam.trustframework.bank.TestJwtSigner
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
import spock.lang.Shared
import spock.lang.Specification

import java.time.LocalDate
import java.time.OffsetDateTime

@MicronautTest
class PaymentControllerSpec extends Specification {

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
    ResponsePixPayment responsePixPayment

    def setup () {
        mapper.findAndRegisterModules();
        handler = new MicronautLambdaContainerHandler(Micronaut.build().singletons(mockService, new MockJwksFetcher(), TestJwtSigner.JWT_SIGNER))

        responseConsent = TestDataFactory.createPaymentConsentResponse("consent1", OffsetDateTime.now(), OffsetDateTime.now(), OffsetDateTime.now(),
                ResponsePaymentConsentData.StatusEnum.AUTHORISED, "LUID1", "LUREL1", "BEID1", "BEREL1",
                Identification.PersonTypeEnum.NATURAL, "BORKBORKBORK", "Kate Human", "BRL", "1", LocalDate.now(), PaymentConsent.TypeEnum.PIX,
                "ISPB1", "ISSUER1", "AC1", EnumAccountPaymentsType.CACC)


        responsePixPayment = TestDataFactory.createPixPaymentResponse("paymentId", "end2endId", "consentId",
                OffsetDateTime.now(), OffsetDateTime.now(), "proxy", ResponsePixPaymentData.StatusEnum.ACCC, null,
                EnumLocalInstrument.QRES, "1", "BRL", "blah", "ispb", "issuer",
                "acno", EnumAccountPaymentsType.CACC)

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

    def "we can create a payment consent" () {
        given:
        def paymentConsentRequest = TestDataFactory.createPaymentConsentRequest("12345678901234", "RELA", "12345678901",
                "Bob Creditor", Identification.PersonTypeEnum.NATURAL, EnumAccountPaymentsType.SLRY, "12341234",
                "1234", "12345678", "ABC", "12345678901", PaymentConsent.TypeEnum.PIX,
                LocalDate.now(), "BRL", "100.00")

        mockService.createConsent(_, _, _) >> responseConsent

        String entity = mapper.writeValueAsString(paymentConsentRequest)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/payments/v1/consents', HttpMethod.POST.toString()).body(entity)
        AuthHelper.authorize(scopes: "payments", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.CREATED.code
        def body = response.body

        when:
        mapper.readValue(body, ResponsePaymentConsent)

        then:
        noExceptionThrown()
    }

    def "we can create a payment consent as a signed JWT" () {
        given:
        def paymentConsentRequest = TestDataFactory.createPaymentConsentRequest("12345678901234", "RELA", "12345678901",
                "Bob Creditor", Identification.PersonTypeEnum.NATURAL,EnumAccountPaymentsType.SLRY, "12341234",
                "1234","12345678", "ABC", "12345678901", PaymentConsent.TypeEnum.PIX,
                LocalDate.now(), "BRL", "100.00")

        mockService.createConsent(_, _, _) >> responseConsent

        String entity = mapper.writeValueAsString(paymentConsentRequest)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
            .audience("mockbank")
            .issuer("issuer")
            .issueTime(new Date())
            .jwtID("arandomjti")
            .build()
        entity = TestJwtSigner.sign(entity, otherClaims)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/payments/v1/consents', HttpMethod.POST.toString()).body(entity)
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

        and:
        jwt.getJWTClaimsSet().getAudience().contains('issuer')
    }

    def "we see a 4xx if bad signer and JWT" () {
        given:
        def paymentConsentRequest = TestDataFactory.createPaymentConsentRequest("12345678901234", "RELA", "12345678901",
                "Bob Creditor", Identification.PersonTypeEnum.NATURAL,EnumAccountPaymentsType.SLRY, "12341234",
                "1234","12345678", "ABC", "12345678901", PaymentConsent.TypeEnum.PIX,
                LocalDate.now(), "BRL", "100.00")

        mockService.createConsent(_, _, _) >> responseConsent

        String entity = mapper.writeValueAsString(paymentConsentRequest)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("mockbank")
                .issuer("issuer")
                .issueTime(new Date())
                .jwtID("arandomjti")
                .build()
        entity = TestJwtSigner.signBadly(entity, otherClaims)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/payments/v1/consents', HttpMethod.POST.toString()).body(entity)
        AuthHelper.authorize(scopes: "payments", org_id: 'issuer', builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.UNAUTHORIZED.code
        response.multiValueHeaders['Content-Type'] == ['application/jwt']
        def body = response.body

        when:
        SignedJWT.parse(body)

        then:
        noExceptionThrown()

    }

    def "we see a 4xx if missing claim" () {
        given:
        def paymentConsentRequest = TestDataFactory.createPaymentConsentRequest("12345678901234", "RELA", "12345678901",
                "Bob Creditor", Identification.PersonTypeEnum.NATURAL,EnumAccountPaymentsType.SLRY, "12341234",
                "1234","12345678", "ABC", "12345678901", PaymentConsent.TypeEnum.PIX,
                LocalDate.now(), "BRL", "100.00")

        mockService.createConsent(_, _, _) >> responseConsent

        String entity = mapper.writeValueAsString(paymentConsentRequest)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("mockbank")
                .issuer("issuer")
                .jwtID("arandomjti")
                .build()
        entity = TestJwtSigner.signBadly(entity, otherClaims)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/payments/v1/consents', HttpMethod.POST.toString()).body(entity)
        AuthHelper.authorize(scopes: "payments", org_id: 'issuer', builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.UNAUTHORIZED.code

    }

    def "we see a 4xx if body not jwt" () {
        given:
        def paymentConsentRequest = TestDataFactory.createPaymentConsentRequest("12345678901234", "RELA", "12345678901",
                "Bob Creditor", Identification.PersonTypeEnum.NATURAL,EnumAccountPaymentsType.SLRY, "12341234",
                "1234","12345678", "ABC", "12345678901", PaymentConsent.TypeEnum.PIX,
                LocalDate.now(), "BRL", "100.00")

        mockService.createConsent(_, _, _) >> responseConsent

        String entity = mapper.writeValueAsString(paymentConsentRequest)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/payments/v1/consents', HttpMethod.POST.toString()).body(entity)
        AuthHelper.authorize(scopes: "payments", org_id: 'org', builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code

    }

    def "we can create a payment" () {
        given:
        def paymentRequest = TestDataFactory.createPaymentRequest("123456789012", "1234", "12345678", EnumAccountPaymentsType.CACC, EnumLocalInstrument.MANU, "100.00", "BRL", "proxy", "qrcode",  "86325173000103","remittanceInfo")

        String json = mapper.writeValueAsString(paymentRequest)
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/payments/v1/pix/payments', HttpMethod.POST.toString()).body(json)
        AuthHelper.authorize(scopes: "payments consent:1234", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        1 * mockService.createPayment(_, "1234", _) >> responsePixPayment
        response.statusCode == HttpStatus.CREATED.code
        response.body
    }

    def "we can create a payment as a JWT" () {
        given:
        def paymentRequest = TestDataFactory.createPaymentRequest("123456789012", "1234", "12345678", EnumAccountPaymentsType.CACC, EnumLocalInstrument.MANU, "100.00", "BRL", "proxy", "qrcode", "61363003000184","remittanceInfo")

        String json = mapper.writeValueAsString(paymentRequest)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("mockbank")
                .issuer("issuer")
                .issueTime(new Date())
                .jwtID("arandomjti")
                .build()
        json = TestJwtSigner.sign(json, otherClaims)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/payments/v1/pix/payments', HttpMethod.POST.toString()).body(json)
        AuthHelper.authorize(scopes: "payments consent:1234", org_id: 'issuer', builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        1 * mockService.createPayment(_, "1234", _) >> responsePixPayment
        response.statusCode == HttpStatus.CREATED.code
        response.body
    }

    def "we cannot create a payment if the inbound issuer claim is not the org id" () {
        given:
        def paymentRequest = TestDataFactory.createPaymentRequest("123456789012", "1234", "12345678", EnumAccountPaymentsType.CACC, EnumLocalInstrument.MANU, "100.00", "BRL", "proxy", "qrcode", "61363003000184","remittanceInfo")

        String json = mapper.writeValueAsString(paymentRequest)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("audience")
                .issuer("bad_issuer")
                .issueTime(new Date())
                .jwtID("arandomjti")
                .build()
        json = TestJwtSigner.sign(json, otherClaims)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/payments/v1/pix/payments', HttpMethod.POST.toString()).body(json)
        AuthHelper.authorize(scopes: "payments consent:1234", org_id: 'real_issuer', software_id: 'ssid', builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        0 * mockService.createPayment(_, "1234", _) >> responsePixPayment
        response.statusCode == HttpStatus.BAD_REQUEST.code
    }

    def "we cannot create a payment if the initiating cnpj is bogus" () {
        given:
        mockService.createPayment(_ as CreatePixPayment, "1234", "idempotencyToken1") >> {  throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Unregistered CNPJ")}
        def paymentRequest = TestDataFactory.createPaymentRequest("123456789012", "1234", "12345678", EnumAccountPaymentsType.CACC, EnumLocalInstrument.MANU, "100.00", "BRL", "proxy", "qrcode", "61363003000184","remittanceInfo")

        String json = mapper.writeValueAsString(paymentRequest)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("audience")
                .issuer("real_issuer")
                .issueTime(new Date())
                .jwtID("arandomjti")
                .build()
        json = TestJwtSigner.sign(json, otherClaims)

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/payments/v1/pix/payments', HttpMethod.POST.toString()).body(json)
        AuthHelper.authorize(scopes: "payments consent:1234", org_id: 'real_issuer', software_id: 'ssid', builder)
        builder.header("x-idempotency-key", "idempotencyToken1")
        builder.header("Content-Type", "application/jwt")
        builder.header("Accept", "application/jwt")


        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.UNAUTHORIZED.code
        def err = mapper.readValue(response.body, Map)
        err.message == 'Unregistered CNPJ'
    }

    void "the OP gets a full payment consent response"() {
        given:
        mockService.getConsentFull(_, _) >> consentFull

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/payments/v1/consents/consentid', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "op:payments", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
        ResponsePaymentConsentFull actual = mapper.readValue(response.body, ResponsePaymentConsentFull)
        actual.data.clientId == "client1"
    }


    def "we can get a payment consent as a signed JWT"() {

        given:
        mockService.getConsentFull(_, _) >> consentFull

        when:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/payments/v1/consents/consentid', HttpMethod.GET.toString())
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
        mockService.getConsent(_, _) >> responseConsent

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/payments/v1/consents/consentid', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "payments", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        mapper.readValue(response.body, ResponsePaymentConsent)
        response.statusCode == HttpStatus.OK.code

    }

    def "If a consent already contains a debtor account we cannot re-provide it" () {
        given:

        def paymentConsentRequest = new CreatePaymentConsent().data(new CreatePaymentConsentData()
                .businessEntity(
                        new BusinessEntity().document(
                                new BusinessEntityDocument().identification("12345678901234")
                                        .rel("CNPJ")))
                .creditor(new Identification().cpfCnpj("12345678901").name("Bob Creditor").personType(Identification.PersonTypeEnum.NATURAL))
                .debtorAccount(new DebtorAccount().accountType(EnumAccountPaymentsType.SLRY).ispb("12341234").issuer("1234").number("12341234"))
                .loggedUser(new LoggedUser().document(new LoggedUserDocument().rel("CPF").identification("12345678901")))
                .payment(new PaymentConsent().type(PaymentConsent.TypeEnum.PIX).date(LocalDate.now()).currency("BRL").amount("100.00")))

        mockService.createConsent(_, _, _) >> responseConsent

        String json = mapper.writeValueAsString(paymentConsentRequest)
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/payments/v1/consents', HttpMethod.POST.toString()).body(json)
        AuthHelper.authorize(scopes: "payments", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")

        and:
        handler.proxy(builder.build(), lambdaContext)
        mockService.updateConsent(_, _) >> { -> throw new RuntimeException() }
        def updateConsent = new UpdatePaymentConsent().data(new UpdatePaymentConsentData()
                .status(UpdatePaymentConsentData.StatusEnum.AUTHORISED)
                .debtorAccount(new DebtorAccount().accountType(EnumAccountPaymentsType.SLRY).ispb("12341234").issuer("1234").number("12341234"))
        )

        when:
        json = mapper.writeValueAsString(paymentConsentRequest)
        builder = new AwsProxyRequestBuilder('/payments/v1/consents/id', HttpMethod.PUT.toString()).body(json)
        AuthHelper.authorize(scopes: "op:payments", builder)
        builder.header("x-idempotency-key", "idempotencyToken1")

        then:
        def response = handler.proxy(builder.build(), lambdaContext)
        response.statusCode == HttpStatus.BAD_REQUEST.code

    }

}
