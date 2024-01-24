package com.raidiam.trustframework.bank.controllers

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jwt.SignedJWT
import com.raidiam.trustframework.bank.AwsProxyHelper
import com.raidiam.trustframework.bank.TestJwtSigner
import com.raidiam.trustframework.bank.TestRequestDataFactory
import com.raidiam.trustframework.bank.jwt.DefaultJwksFetcher
import com.raidiam.trustframework.bank.jwt.JwksFetcher
import com.raidiam.trustframework.bank.services.EnrollmentService
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
import spock.lang.Unroll

import java.time.OffsetDateTime

@MicronautTest
class EnrollmentControllerSpec extends Specification {
    @Shared
    Logger log = LoggerFactory.getLogger(EnrollmentControllerSpec.class)

    private static Context lambdaContext = new MockLambdaContext()
    def mapper = new ObjectMapper()

    MicronautLambdaContainerHandler handler

    EnrollmentService mockEnrollmentService = Mock(EnrollmentService)

    @Replaces(DefaultJwksFetcher.class)
    static class MockJwksFetcher implements JwksFetcher {

        @Override
        JWKSet findForOrg(String orgId) {
            return TestJwtSigner.JWKS
        }
    }

    @Shared
    ResponseCreateEnrollment responseEnrollment
    @Shared
    ResponseEnrollment responseGetEnrollment
    @Shared
    EnrollmentFidoRegistrationOptions responseEnrollmentFidoRegistrationOptions
    @Shared
    ResponseEnrollment responsePutEnrollment
    @Shared
    EnrollmentFidoSignOptions responseEnrollmentFidoSignOptions

    def setup() {
        mapper.findAndRegisterModules()
        mapper.registerModule(new JavaTimeModule())

        handler = new MicronautLambdaContainerHandler(Micronaut.build().singletons(mockEnrollmentService, new MockJwksFetcher(), TestJwtSigner.JWT_SIGNER))

        responseEnrollment = TestRequestDataFactory.createEnrollmentResponse("enrollment1", OffsetDateTime.now(), OffsetDateTime.now(), OffsetDateTime.now(),
                EnumEnrollmentStatus.AWAITING_RISK_SIGNALS, "LUID1", "LUREL1", "BEID1", "BEREL1", "ISPB1", "ISSUER1", "AC1", EnumAccountPaymentsType.CACC)

        responseGetEnrollment = TestRequestDataFactory.getEnrollmentResponse("enrollment1", OffsetDateTime.now(), OffsetDateTime.now(), OffsetDateTime.now(),
                EnumEnrollmentStatus.AWAITING_RISK_SIGNALS, "LUID1", "LUREL1", "BEID1", "BEREL1", "ISPB1", "ISSUER1", "AC1", "100000.12", "100000.12", EnumAccountPaymentsType.CACC)

        responseEnrollmentFidoRegistrationOptions = TestRequestDataFactory.createEnrollmentFidoRegistrationOptionResponse("enrollment1")

        responsePutEnrollment = TestRequestDataFactory.getEnrollmentResponse("enrollment1", OffsetDateTime.now(), OffsetDateTime.now(), OffsetDateTime.now(),
                EnumEnrollmentStatus.AWAITING_RISK_SIGNALS, "LUID1", "LUREL1", "BEID1", "BEREL1", "ISPB1", "ISSUER1", "AC1", "100000.12", "100000.12", EnumAccountPaymentsType.CACC)

        responseEnrollmentFidoSignOptions = TestRequestDataFactory.createEnrollmentFidoSignOptionResponse()
    }

    def cleanup() {
        handler.close()
    }

    def "we get 400 calling enrollments without x-fapi-interaction-id"() {
        given:
        def enrollmentRequest = EnrollmentFactory.createEnrollment()

        mockEnrollmentService.createEnrollment(_ as String, _ as String, _ as String, _ as CreateEnrollment) >> responseEnrollment
        def entity = AwsProxyHelper.signPayload(enrollmentRequest)
        AwsProxyRequestBuilder builder = AwsProxyHelper.buildJwtCall('/open-banking/enrollments/v1/enrollments', HttpMethod.POST, entity, false)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code
        response.getMultiValueHeaders().get("x-fapi-interaction-id") != null
    }

    def "we can create an enrollment as a signed JWT"() {
        given:
        def enrollmentRequest = EnrollmentFactory.createEnrollment()
        mockEnrollmentService.createEnrollment(_ as String, _ as String, _ as String, _ as CreateEnrollment) >> responseEnrollment

        def entity = AwsProxyHelper.signPayload(enrollmentRequest)
        AwsProxyRequestBuilder builder = AwsProxyHelper.buildJwtCall('/open-banking/enrollments/v1/enrollments', HttpMethod.POST, entity)

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
        def responseData = mapper.readValue(claimsData, ResponseCreateEnrollmentData.class)
        responseData
        responseData.getEnrollmentId()
    }

    def "We cannot create a enrollment if permission is not PAYMENT_INITIATE"() {
        given:
        def enrollmentRequest = EnrollmentFactory.createEnrollment()
        enrollmentRequest.data.setPermissions(null)

        mockEnrollmentService.createEnrollment(_ as String, _ as String, _ as String, _ as CreateEnrollment) >> { throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "The permission is not allowed") }

        def entity = AwsProxyHelper.signPayload(enrollmentRequest)
        AwsProxyRequestBuilder builder = AwsProxyHelper.buildJwtCall('/open-banking/enrollments/v1/enrollments', HttpMethod.POST, entity)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.UNPROCESSABLE_ENTITY.code
        println response.body

        then:
        noExceptionThrown()

    }

    def "we can get a enrollment as a signed JWT"() {

        given:
        mockEnrollmentService.getEnrollment(_ as String, _ as String, _ as Boolean) >> responseGetEnrollment

        when:
        AwsProxyRequestBuilder builder = AwsProxyHelper.buildJwtCall('/open-banking/enrollments/v1/enrollments/testId', HttpMethod.GET, null)

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


    def "we can update enrollment with a signed JWT"() {
        given:
        def patchRequest = EnrollmentFactory.patchEnrollment(true)

        def entity = AwsProxyHelper.signPayload(patchRequest)
        AwsProxyRequestBuilder builder = AwsProxyHelper.buildJwtCall('/open-banking/enrollments/v1/enrollments/testId', HttpMethod.PATCH, entity)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.NO_CONTENT.code
        response.body == null

        then:
        noExceptionThrown()
    }

    def "we can create enrollment risk signals with a signed JWT"() {
        given:
        def riskSignalRequest = TestRequestDataFactory.createEnrollmentRiskSignal()

        def entity = AwsProxyHelper.signPayload(riskSignalRequest)
        AwsProxyRequestBuilder builder = AwsProxyHelper.buildJwtCall('/open-banking/enrollments/v1/enrollments/testId/risk-signals', HttpMethod.POST, entity)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.NO_CONTENT.code
        response.body == null
        response.multiValueHeaders.get("x-fapi-interaction-id") != null

        then:
        noExceptionThrown()
    }

    def "we can create enrollment fido-registration-option with a signed JWT"() {
        given:
        def fidoRegistrationOptionRequest = TestRequestDataFactory.createEnrollmentFidoOptionsInput()
        mockEnrollmentService.createFidoRegistrationOptions(_ as String, _ as EnrollmentFidoOptionsInput, _ as Optional<String>) >> responseEnrollmentFidoRegistrationOptions

        def entity = AwsProxyHelper.signPayload(fidoRegistrationOptionRequest)
        AwsProxyRequestBuilder builder = AwsProxyHelper.buildJwtCall('/open-banking/enrollments/v1/enrollments/testId/fido-registration-options', HttpMethod.POST, entity)

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
        def responseData = mapper.readValue(claimsData, EnrollmentFidoRegistrationOptionsData.class)
        responseData
        responseData.getEnrollmentId()
    }

    def "we can create enrollment fido-sign-option with a signed JWT"() {
        given:
        def fidoSignOptionRequest = TestRequestDataFactory.createEnrollmentFidoSignOptionsInput()
        mockEnrollmentService.createFidoSignOptions(_ as String, _ as EnrollmentFidoSignOptionsInput, _ as Optional<String>) >> responseEnrollmentFidoSignOptions

        def entity = AwsProxyHelper.signPayload(fidoSignOptionRequest)
        AwsProxyRequestBuilder builder = AwsProxyHelper.buildJwtCall('/open-banking/enrollments/v1/enrollments/testId/fido-sign-options', HttpMethod.POST, entity)

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
        def responseData = mapper.readValue(claimsData, EnrollmentFidoSignOptionsData.class)
        responseData
        responseData.getChallenge()
    }

    def "we can create a fido registration with a signed JWT"() {
        given:
        def registrationRequest = EnrollmentFactory.enrollmentFidoRegistration()

        def entity = AwsProxyHelper.signPayload(registrationRequest)
        AwsProxyRequestBuilder builder = AwsProxyHelper.buildJwtCall('/open-banking/enrollments/v1/enrollments/testid/fido-registration', HttpMethod.POST, entity)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.NO_CONTENT.code
        response.multiValueHeaders['Content-Type'] == ['application/jwt']
        response.body == null

        then:
        noExceptionThrown()
    }


    def "we can create a fido authorisation with a signed JWT"() {
        given:
        def registrationRequest = EnrollmentFactory.enrollmentFidoAuthorisation()

        def entity = AwsProxyHelper.signPayload(registrationRequest)
        AwsProxyRequestBuilder builder = AwsProxyHelper.buildJwtCall('/open-banking/enrollments/v1/consents/testId/authorise', HttpMethod.POST, entity)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.NO_CONTENT.code
        response.multiValueHeaders['Content-Type'] == ['application/jwt']
        response.body == null

        then:
        noExceptionThrown()
    }

    @Unroll
    def "we cannot call enrollments endpoint using plain JSON"() {
        given:
        def entity = mapper.writeValueAsString(entityRequest)

        AwsProxyRequestBuilder builder = AwsProxyHelper.buildCall(path, httpMedhod, entity)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.FORBIDDEN.code

        where:
        entityRequest                                             | path                                                                        | httpMedhod
        EnrollmentFactory.createEnrollment()                      | "/open-banking/enrollments/v1/enrollments/"                                 | HttpMethod.POST
        EnrollmentFactory.patchEnrollment(true)                   | "/open-banking/enrollments/v1/enrollments/testid"                           | HttpMethod.PATCH
        TestRequestDataFactory.createEnrollmentRiskSignal()       | "/open-banking/enrollments/v1/enrollments/testid/risk-signals"              | HttpMethod.POST
        TestRequestDataFactory.createEnrollmentFidoOptionsInput() | "/open-banking/enrollments/v1/enrollments/testid/fido-registration-options" | HttpMethod.POST
        TestRequestDataFactory.createEnrollmentFidoOptionsInput() | "/open-banking/enrollments/v1/enrollments/testid/fido-registration"         | HttpMethod.POST
        TestRequestDataFactory.createEnrollmentFidoSignOptionsInput() | "/open-banking/enrollments/v1/enrollments/testid/fido-sign-options"     | HttpMethod.POST
        EnrollmentFactory.enrollmentFidoAuthorisation()           | "/open-banking/enrollments/v1/consents/testId/authorise"                    | HttpMethod.POST
    }

    def "we can update enrollment"() {
        given:
        mockEnrollmentService.updateEnrollment(_ as String, _ as UpdateEnrollment) >> responsePutEnrollment
        def enrollmentUpdate = TestRequestDataFactory.createUpdateEnrollment(EnumEnrollmentStatus.AUTHORISED)
        String entity = mapper.writeValueAsString(enrollmentUpdate)
        AwsProxyRequestBuilder builder = AwsProxyHelper.buildPaymentsManagerCall('/open-banking/enrollments/v1/enrollments/testId', HttpMethod.PUT, entity)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
        response.multiValueHeaders.get("x-fapi-interaction-id") != null

        then:
        noExceptionThrown()
    }
}
