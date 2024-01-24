package com.raidiam.trustframework.bank.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.nimbusds.jwt.JWTClaimsSet
import com.raidiam.trustframework.bank.TestJwtSigner
import com.raidiam.trustframework.bank.TestRequestDataFactory
import io.micronaut.core.bind.ArgumentBinder
import io.micronaut.function.aws.proxy.MicronautAwsProxyRequest
import io.micronaut.http.MediaType
import io.micronaut.http.exceptions.HttpStatusException
import spock.lang.Specification
import com.raidiam.trustframework.mockbank.models.generated.CreatePixPaymentV4

class PixPaymentV4JwtBinderSpec extends Specification {

    def "If the body has already been decoded we don't bind"() {

        given:
        def payment = TestRequestDataFactory.testPixPaymentV4()
        MicronautAwsProxyRequest request = Mock(MicronautAwsProxyRequest)
        request.isBodyDecoded() >> true
        request.getBody(CreatePixPaymentV4) >> Optional.of(payment)
        request.getContentType() >> MediaType.APPLICATION_JSON_TYPE
        PixPaymentV4JwtBinder binder = new PixPaymentV4JwtBinder(new ObjectMapper())

        when:
        def result = binder.bind(null, request)

        then:
        result.satisfied
        result.get() == payment

    }

    def "If the body has not already been decoded we bind"() {

        given:
        def payment = TestRequestDataFactory.testPixPaymentV4()
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
        PixPaymentV4JwtBinder binder = new PixPaymentV4JwtBinder(mapper)

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
        PixPaymentV4JwtBinder binder = new PixPaymentV4JwtBinder(mapper)

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
        PixPaymentV4JwtBinder binder = new PixPaymentV4JwtBinder(new ObjectMapper())

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
        PixPaymentV4JwtBinder binder = new PixPaymentV4JwtBinder(new ObjectMapper())

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
        PixPaymentV4JwtBinder binder = new PixPaymentV4JwtBinder(new ObjectMapper())

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
        PixPaymentV4JwtBinder binder = new PixPaymentV4JwtBinder(new ObjectMapper())

        when:
        def bind = binder.bind(null, request)

        then:
        bind == ArgumentBinder.BindingResult.UNSATISFIED

    }

    def "we can bind the pix payment v4"() {

        given:
        MicronautAwsProxyRequest request = Mock(MicronautAwsProxyRequest)
        request.isBodyDecoded() >> false
        request.getBody(String) >> Optional.of("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiI3NGU5MjlkOS0zM2I2LTRkODUtOGJhNy1jMTQ2Yzg2N2E4MTciLCJhdWQiOiJodHRwczovL21hdGxzLWFwaS5tb2NrYmFuay5wb2MucmFpZGlhbS5pby9vcGVuLWJhbmtpbmcvcGF5bWVudHMvdjQvcGl4L3BheW1lbnRzIiwiZGF0YSI6W3sicHJveHkiOiJjbGllbnRlLWEwMDAwMUBwaXguYmNiLmdvdi5iciIsImxvY2FsSW5zdHJ1bWVudCI6IkRJQ1QiLCJyZW1pdHRhbmNlSW5mb3JtYXRpb24iOiJDb25mb3JtYW5jZSBTdWl0ZSBUZXN0IiwiY3JlZGl0b3JBY2NvdW50Ijp7Im51bWJlciI6IjEyMzQ1Njc4IiwiYWNjb3VudFR5cGUiOiJDQUNDIiwiaXNwYiI6Ijk5OTk5MDA0IiwiaXNzdWVyIjoiMDAwMSJ9LCJjbnBqSW5pdGlhdG9yIjoiMDAwMDAwMDAwMDAxOTEiLCJwYXltZW50Ijp7ImFtb3VudCI6IjEzMzMuMDAiLCJjdXJyZW5jeSI6IkJSTCJ9LCJlbmRUb0VuZElkIjoiRTAwMDAwMDAwMjAyMzExMjMxNTAwckVzM1pBS2l0bGUifSx7InByb3h5IjoiY2xpZW50ZS1hMDAwMDFAcGl4LmJjYi5nb3YuYnIiLCJsb2NhbEluc3RydW1lbnQiOiJESUNUIiwicmVtaXR0YW5jZUluZm9ybWF0aW9uIjoiQ29uZm9ybWFuY2UgU3VpdGUgVGVzdCIsImNyZWRpdG9yQWNjb3VudCI6eyJudW1iZXIiOiIxMjM0NTY3OCIsImFjY291bnRUeXBlIjoiQ0FDQyIsImlzcGIiOiI5OTk5OTAwNCIsImlzc3VlciI6IjAwMDEifSwiY25wakluaXRpYXRvciI6IjAwMDAwMDAwMDAwMTkxIiwicGF5bWVudCI6eyJhbW91bnQiOiIxMzMzLjAwIiwiY3VycmVuY3kiOiJCUkwifSwiZW5kVG9FbmRJZCI6IkUwMDAwMDAwMDIwMjMxMTI5MTUwME5lV2xoR2hCU1diIn0seyJwcm94eSI6ImNsaWVudGUtYTAwMDAxQHBpeC5iY2IuZ292LmJyIiwibG9jYWxJbnN0cnVtZW50IjoiRElDVCIsInJlbWl0dGFuY2VJbmZvcm1hdGlvbiI6IkNvbmZvcm1hbmNlIFN1aXRlIFRlc3QiLCJjcmVkaXRvckFjY291bnQiOnsibnVtYmVyIjoiMTIzNDU2NzgiLCJhY2NvdW50VHlwZSI6IkNBQ0MiLCJpc3BiIjoiOTk5OTkwMDQiLCJpc3N1ZXIiOiIwMDAxIn0sImNucGpJbml0aWF0b3IiOiIwMDAwMDAwMDAwMDE5MSIsInBheW1lbnQiOnsiYW1vdW50IjoiMTMzMy4wMCIsImN1cnJlbmN5IjoiQlJMIn0sImVuZFRvRW5kSWQiOiJFMDAwMDAwMDAyMDIzMTIyNDE1MDBXUkVkVjFITVJKVSJ9LHsicHJveHkiOiJjbGllbnRlLWEwMDAwMUBwaXguYmNiLmdvdi5iciIsImxvY2FsSW5zdHJ1bWVudCI6IkRJQ1QiLCJyZW1pdHRhbmNlSW5mb3JtYXRpb24iOiJDb25mb3JtYW5jZSBTdWl0ZSBUZXN0IiwiY3JlZGl0b3JBY2NvdW50Ijp7Im51bWJlciI6IjEyMzQ1Njc4IiwiYWNjb3VudFR5cGUiOiJDQUNDIiwiaXNwYiI6Ijk5OTk5MDA0IiwiaXNzdWVyIjoiMDAwMSJ9LCJjbnBqSW5pdGlhdG9yIjoiMDAwMDAwMDAwMDAxOTEiLCJwYXltZW50Ijp7ImFtb3VudCI6IjEzMzMuMDAiLCJjdXJyZW5jeSI6IkJSTCJ9LCJlbmRUb0VuZElkIjoiRTAwMDAwMDAwMjAyNDAzMzAxNTAwbTVGTUFsblJMS1EifSx7InByb3h5IjoiY2xpZW50ZS1hMDAwMDFAcGl4LmJjYi5nb3YuYnIiLCJsb2NhbEluc3RydW1lbnQiOiJESUNUIiwicmVtaXR0YW5jZUluZm9ybWF0aW9uIjoiQ29uZm9ybWFuY2UgU3VpdGUgVGVzdCIsImNyZWRpdG9yQWNjb3VudCI6eyJudW1iZXIiOiIxMjM0NTY3OCIsImFjY291bnRUeXBlIjoiQ0FDQyIsImlzcGIiOiI5OTk5OTAwNCIsImlzc3VlciI6IjAwMDEifSwiY25wakluaXRpYXRvciI6IjAwMDAwMDAwMDAwMTkxIiwicGF5bWVudCI6eyJhbW91bnQiOiIxMzMzLjAwIiwiY3VycmVuY3kiOiJCUkwifSwiZW5kVG9FbmRJZCI6IkUwMDAwMDAwMDIwMjUwNDA1MTUwMGthelMyTW00SUtCIn1dLCJpYXQiOjE3MDA2NzI5NDQsImp0aSI6IjdiYWY4MDYzLTRkYTgtNGEwZC04MzRhLTNjYjVhNDU2Njg1ZCJ9.WuAqc-2XBK_rryBHN0pOFxwqBuZAgLGTfC0GFyePLXk")
        request.getContentType() >> Optional.ofNullable(new MediaType("application/json+jwt"))
        PixPaymentV4JwtBinder binder = new PixPaymentV4JwtBinder(new ObjectMapper())

        when:
        def bind = binder.bind(null, request)

        then:
        bind.get().data.size() == 5

    }

}