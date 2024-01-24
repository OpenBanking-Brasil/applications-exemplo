package com.raidiam.trustframework.bank.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.nimbusds.jwt.JWTClaimsSet
import com.raidiam.trustframework.bank.TestJwtSigner
import com.raidiam.trustframework.bank.TestRequestDataFactory
import com.raidiam.trustframework.mockbank.models.generated.CreatePixPayment
import com.raidiam.trustframework.mockbank.models.generated.CreatePixPaymentV2
import io.micronaut.core.bind.ArgumentBinder
import io.micronaut.function.aws.proxy.MicronautAwsProxyRequest
import io.micronaut.http.MediaType
import io.micronaut.http.exceptions.HttpStatusException
import spock.lang.Specification

class PixPaymentV2JwtBinderSpec extends Specification {

    def "If the body has already been decoded we don't bind"() {

        given:
        def payment = TestRequestDataFactory.testPixPaymentV2()
        MicronautAwsProxyRequest request = Mock(MicronautAwsProxyRequest)
        request.isBodyDecoded() >> true
        request.getBody(CreatePixPaymentV2) >> Optional.of(payment)
        request.getContentType() >> MediaType.APPLICATION_JSON_TYPE
        PixPaymentV2JwtBinder binder = new PixPaymentV2JwtBinder(new ObjectMapper())

        when:
        def result = binder.bind(null, request)

        then:
        result.satisfied
        result.get() == payment

    }

    def "If the body has not already been decoded we bind"() {

        given:
        def payment = TestRequestDataFactory.testPixPaymentV2()
        def mapper = JsonMapper.builder()
                .findAndAddModules()
                .build()
        def body = mapper.writeValueAsString(payment)
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
        request.getContentType() >> Optional.of(new MediaType("application/json+jwt"))
        PixPaymentV2JwtBinder binder = new PixPaymentV2JwtBinder(mapper)

        when:
        def result = binder.bind(null, request)

        then:
        result.satisfied
        result.get() !== payment

    }

    def "If the body is plain JSON but the header says JWT we die"() {

        given:
        def payment = TestRequestDataFactory.testPixPaymentV2()
        def mapper = JsonMapper.builder()
                .findAndAddModules()
                .build()
        def body = mapper.writeValueAsString(payment)
        MicronautAwsProxyRequest request = Mock(MicronautAwsProxyRequest)
        request.isBodyDecoded() >> false
        request.getBody(String) >> Optional.of(body)
        request.getContentType() >> Optional.of(new MediaType("application/json+jwt"))
        PixPaymentV2JwtBinder binder = new PixPaymentV2JwtBinder(mapper)

        when:
        binder.bind(null, request)

        then:
        HttpStatusException e = thrown()

    }

    def "If the body is garbled text but the header says JWT we die"() {

        given:
        MicronautAwsProxyRequest request = Mock(MicronautAwsProxyRequest)
        request.isBodyDecoded() >> false
        request.getBody(String) >> Optional.of("thisisnotajwtnorjson")
        request.getContentType() >> Optional.of(new MediaType("application/json+jwt"))
        PixPaymentV2JwtBinder binder = new PixPaymentV2JwtBinder(new ObjectMapper())

        when:
        binder.bind(null, request)

        then:
        HttpStatusException e = thrown()

    }

    def "If the body is a JWT but not a payment consent then we die"() {

        given:
        MicronautAwsProxyRequest request = Mock(MicronautAwsProxyRequest)
        request.isBodyDecoded() >> false
        request.getBody(String) >> Optional.of("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzb21lYm9keSIsInN1YiI6IjEyMzQ1Njc4OTAiLCJpYXQiOjE1MTYyMzkwMjIsImF1ZCI6IkRpZCB5b3UgcmVhbGx5IHRha2UgdGhlIHRpbWUgdG8gcGFyc2UgdGhpcz8ifQ.ylighsjDXCb5fGiAXlWtIvV-WFWmMtNC5daEjVYwAng")
        request.getContentType() >> Optional.of(new MediaType("application/json+jwt"))
        PixPaymentV2JwtBinder binder = new PixPaymentV2JwtBinder(new ObjectMapper())

        when:
        binder.bind(null, request)

        then:
        HttpStatusException e = thrown()

    }

    def "If no content type then we continue"() {

        given:
        MicronautAwsProxyRequest request = Mock(MicronautAwsProxyRequest)
        request.isBodyDecoded() >> false
        request.getBody(String) >> Optional.of("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzb21lYm9keSIsInN1YiI6IjEyMzQ1Njc4OTAiLCJpYXQiOjE1MTYyMzkwMjIsImF1ZCI6IkRpZCB5b3UgcmVhbGx5IHRha2UgdGhlIHRpbWUgdG8gcGFyc2UgdGhpcz8ifQ.ylighsjDXCb5fGiAXlWtIvV-WFWmMtNC5daEjVYwAng")
        request.getContentType() >> Optional.ofNullable(null)
        PixPaymentV2JwtBinder binder = new PixPaymentV2JwtBinder(new ObjectMapper())

        when:
        def bind = binder.bind(null, request)

        then:
        bind == ArgumentBinder.BindingResult.EMPTY

    }

    def "If no body then we continue"() {

        given:
        MicronautAwsProxyRequest request = Mock(MicronautAwsProxyRequest)
        request.isBodyDecoded() >> false
        request.getBody(String) >> Optional.ofNullable(null)
        request.getContentType() >> Optional.of(new MediaType("application/json+jwt"))
        PixPaymentV2JwtBinder binder = new PixPaymentV2JwtBinder(new ObjectMapper())

        when:
        def bind = binder.bind(null, request)

        then:
        bind == ArgumentBinder.BindingResult.UNSATISFIED

    }

}