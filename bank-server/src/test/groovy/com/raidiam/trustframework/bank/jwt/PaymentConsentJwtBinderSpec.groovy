package com.raidiam.trustframework.bank.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.nimbusds.jwt.JWTClaimsSet
import com.raidiam.trustframework.bank.TestDataFactory
import com.raidiam.trustframework.bank.TestJwtSigner
import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsent
import io.micronaut.function.aws.proxy.MicronautAwsProxyRequest
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.exceptions.HttpStatusException
import spock.lang.Specification

class PaymentConsentJwtBinderSpec extends Specification{

    def "If the body has already been decoded we don't bind"() {

        given:
        def consent = TestDataFactory.testPaymentConsent()
        MicronautAwsProxyRequest request = Mock(MicronautAwsProxyRequest)
        request.isBodyDecoded() >> true
        request.getBody(CreatePaymentConsent) >> Optional.of(consent)
        request.getContentType() >> MediaType.APPLICATION_JSON_TYPE
        PaymentConsentJwtBinder binder = new PaymentConsentJwtBinder(new ObjectMapper())

        when:
        def result = binder.bind(null, request)

        then:
        result.satisfied
        result.get() == consent

    }

    def "If the body has not already been decoded we bind"() {

        given:
        def consent = TestDataFactory.testPaymentConsent()
        def mapper = JsonMapper.builder()
                .findAndAddModules()
                .build()
        def body = mapper.writeValueAsString(consent)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("audience")
                .issuer("issuer")
                .issueTime(new Date())
                .jwtID("arandomjti")
                .build()
        body = TestJwtSigner.sign(body, otherClaims)
        MicronautAwsProxyRequest request = Mock(MicronautAwsProxyRequest)
        request.isBodyDecoded() >> false
        request.getBody(String) >> Optional.of(body)
        request.getContentType() >> Optional.of(new MediaType("application/jwt"))
        PaymentConsentJwtBinder binder = new PaymentConsentJwtBinder(mapper)

        when:
        def result = binder.bind(null, request)

        then:
        result.satisfied
        result.get() !== consent

    }

    def "If the body is plain JSON but the header says JWT we die"() {

        given:
        def consent = TestDataFactory.testPaymentConsent()
        def mapper = JsonMapper.builder()
                .findAndAddModules()
                .build()
        def body = mapper.writeValueAsString(consent)
        MicronautAwsProxyRequest request = Mock(MicronautAwsProxyRequest)
        request.isBodyDecoded() >> false
        request.getBody(String) >> Optional.of(body)
        request.getContentType() >> Optional.of(new MediaType("application/jwt"))
        PaymentConsentJwtBinder binder = new PaymentConsentJwtBinder(mapper)

        when:
        binder.bind(null, request)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.BAD_REQUEST

    }

    def "If the body is garbled text but the header says JWT we die"() {

        given:
        MicronautAwsProxyRequest request = Mock(MicronautAwsProxyRequest)
        request.isBodyDecoded() >> false
        request.getBody(String) >> Optional.of("thisisnotajwtnorjson")
        request.getContentType() >> Optional.of(new MediaType("application/jwt"))
        PaymentConsentJwtBinder binder = new PaymentConsentJwtBinder(new ObjectMapper())

        when:
        binder.bind(null, request)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.BAD_REQUEST

    }

    def "If the body is a JWT but not a payment consent then we die"() {

        given:
        MicronautAwsProxyRequest request = Mock(MicronautAwsProxyRequest)
        request.isBodyDecoded() >> false
        request.getBody(String) >> Optional.of("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzb21lYm9keSIsInN1YiI6IjEyMzQ1Njc4OTAiLCJpYXQiOjE1MTYyMzkwMjIsImF1ZCI6IkRpZCB5b3UgcmVhbGx5IHRha2UgdGhlIHRpbWUgdG8gcGFyc2UgdGhpcz8ifQ.ylighsjDXCb5fGiAXlWtIvV-WFWmMtNC5daEjVYwAng")
        request.getContentType() >> Optional.of(new MediaType("application/jwt"))
        PaymentConsentJwtBinder binder = new PaymentConsentJwtBinder(new ObjectMapper())

        when:
        binder.bind(null, request)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.BAD_REQUEST

    }

    def "If not a micronaut request then we're fine"() {

        given:
        def consent = TestDataFactory.testPaymentConsent()
        def mapper = JsonMapper.builder()
                .findAndAddModules()
                .build()
        def body = mapper.writeValueAsString(consent)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("audience")
                .issuer("issuer")
                .issueTime(new Date())
                .jwtID("arandomjti")
                .build()
        body = TestJwtSigner.sign(body, otherClaims)
        HttpRequest<?> request = Mock(HttpRequest<?>)
        request.getBody(String) >> Optional.of(body)
        request.getContentType() >> Optional.of(new MediaType("application/jwt"))
        PaymentConsentJwtBinder binder = new PaymentConsentJwtBinder(mapper)

        when:
        def result = binder.bind(null, request)

        then:
        result.satisfied
        result.get() !== consent

    }

}
