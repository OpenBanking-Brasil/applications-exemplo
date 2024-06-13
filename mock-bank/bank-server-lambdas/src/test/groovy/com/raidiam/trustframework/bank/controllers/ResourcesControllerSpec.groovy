package com.raidiam.trustframework.bank.controllers

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.raidiam.trustframework.bank.AuthHelper
import com.raidiam.trustframework.bank.TestJwtSigner
import com.raidiam.trustframework.bank.services.ResourcesService
import com.raidiam.trustframework.mockbank.models.generated.Meta
import com.raidiam.trustframework.mockbank.models.generated.ResponseResourceList
import com.raidiam.trustframework.mockbank.models.generated.ResponseResourceListData
import io.micronaut.data.model.Pageable
import io.micronaut.function.aws.proxy.MicronautLambdaContainerHandler
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.runtime.Micronaut
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import java.time.OffsetDateTime

import static com.raidiam.trustframework.bank.TestRequestDataFactory.createPatchRecurringPaymentConsentRequestV1Rejected
import static com.raidiam.trustframework.bank.TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping
import static com.raidiam.trustframework.bank.TestRequestDataFactory.createRecurringPixPayment

@MicronautTest(transactional = false)
class ResourcesControllerSpec extends Specification {

    private static Context lambdaContext = new MockLambdaContext()

    def mapper = new ObjectMapper()

    ResourcesService resourcesService = Mock(ResourcesService)

    MicronautLambdaContainerHandler handler

    def setup() {
        mapper.findAndRegisterModules()
        handler = new MicronautLambdaContainerHandler(Micronaut.build().singletons(resourcesService, TestJwtSigner.JWT_SIGNER))
    }

    def cleanup() {
        handler.close()
    }

    def "we get a 403 if there is no consent id"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/resources/${version}/resources", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "resources", builder)
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        resourcesService.getResourceList(_ as Pageable, _ as String) >> { throw new HttpStatusException(HttpStatus.NOT_FOUND, "Consent not found") }

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.FORBIDDEN.code
        response.body

        where:
        version << ["v2", "v3"]
    }

    def "we get a 403 if the consent isn't found"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/resources/${version}/resources", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "resources consent:12345", builder)
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        resourcesService.getResourceList(_ as Pageable, _ as String) >> { throw new HttpStatusException(HttpStatus.FORBIDDEN, "Consent not found") }

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.FORBIDDEN.code
        response.body

        where:
        version << ["v2", "v3"]
    }

    void "we can get a resource response"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/resources/${version}/resources", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "resources consent:urn:raidiambank:1234", builder)
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        def resource = new ResponseResourceListData()
                .type(ResponseResourceListData.TypeEnum.ACCOUNT)
                .status(ResponseResourceListData.StatusEnum.AVAILABLE)
                .resourceId(UUID.randomUUID().toString())

        resourcesService.getResourceList(_ as Pageable, _ as String) >>
                new ResponseResourceList()
                        .data(List.of(resource))
                        .meta(new Meta()
                                .totalPages(0)
                                .totalRecords(0)
                                .requestDateTime(OffsetDateTime.now()))

        when:
        def response = handler.proxy(builder.build(), lambdaContext)


        then:
        response.statusCode == HttpStatus.OK.code
        response.body

        where:
        version << ["v2", "v3"]
    }

    def "we cannot call resource v3 endpoints without x-fapi-interaction-id"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/resources/v3/resources", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "resources consent:urn:raidiambank:1234", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code
        response.body.contains("No x-fapi-interaction-id in the request")
    }
}