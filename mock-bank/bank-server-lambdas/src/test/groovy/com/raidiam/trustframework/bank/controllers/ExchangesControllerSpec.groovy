package com.raidiam.trustframework.bank.controllers

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.raidiam.trustframework.bank.AuthHelper
import com.raidiam.trustframework.bank.TestJwtSigner
import com.raidiam.trustframework.bank.services.ExchangesService
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.context.annotation.Value
import io.micronaut.data.model.Pageable
import io.micronaut.function.aws.proxy.MicronautLambdaContainerHandler
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpStatus
import io.micronaut.runtime.Micronaut
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

@MicronautTest(transactional = false, environments = ["db"])
class ExchangesControllerSpec extends Specification {
    private static Context lambdaContext = new MockLambdaContext()

    def mapper = new ObjectMapper()

    ExchangesService service = Mock(ExchangesService)

    MicronautLambdaContainerHandler handler

    ResponseExchangesProductList responseExchangesProductList

    ResponseExchangesOperationDetails responseExchangesOperationDetails

    ResponseExchangesEvents responseExchangesEvents


    @Value("\${mockbank.mockbankUrl}")
    String appBaseUrl

    def setup() {
        mapper.findAndRegisterModules()
        handler = new MicronautLambdaContainerHandler(Micronaut.build().singletons(service, TestJwtSigner.JWT_SIGNER))

        responseExchangesProductList = new ResponseExchangesProductList().data(List.of(new ExchangesProductList()
                .brandName("Test")
                .companyCnpj("0000000010101")))
                .meta(new Meta().totalPages(1))

        responseExchangesOperationDetails = new ResponseExchangesOperationDetails().data(new OperationDetails()
                .authorizedInstitutionCnpjNumber("0000000010101")
                .authorizedInstitutionName("Test")
                .intermediaryInstitutionCnpjNumber("0000000010102")
                .intermediaryInstitutionName("Test2")
                .operationNumber("121212121"))
        responseExchangesEvents = new ResponseExchangesEvents().data(List.of(new Events()
                .eventSequenceNumber("Test")
                .eventType(EnumExchangesEventType._1)
                .deliveryForeignCurrency(EnumExchangesDeliveryForeignCurrency.CARTA_CREDITO_A_VISTA),
                new Events()
                        .eventSequenceNumber("Test2")
                        .eventType(EnumExchangesEventType._1)
                        .deliveryForeignCurrency(EnumExchangesDeliveryForeignCurrency.CONTA_DEPOSITO)
                ))
                .meta(new Meta().totalPages(1))
    }

    def cleanup() {
        handler.close()
    }

    def "we cannot call exchange endpoints without x-fapi-interaction-id"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/exchanges/v1/${endpoint}", method.toString())
        AuthHelper.authorizeAuthorizationCodeGrant(scopes: "exchanges", builder)
        builder.header("Content-Type", "application/json")
        builder.header("Accept", "application/json")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code
        response.body.contains("No x-fapi-interaction-id in the request")

        where:
        endpoint                        | method
        "operations"                    | HttpMethod.GET
        "operations/testeid"            | HttpMethod.GET
        "operations/testeid/events"     | HttpMethod.GET
    }

    def "we can get all exchanges operations"() {
        given:
        service.getOperations(_ as Pageable, "urn:raidiambank:C1DD33123") >> responseExchangesProductList

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/exchanges/v1/operations", HttpMethod.GET.toString())
        AuthHelper.authorizeAuthorizationCodeGrant(scopes: "exchanges consent:urn:raidiambank:C1DD33123", builder)
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    def "we can get an exchanges operation by operation id"() {
        given:
        service.getOperationsByOperationId(_ as String, "urn:raidiambank:C1DD33123") >> responseExchangesOperationDetails

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/exchanges/v1/operations/testID", HttpMethod.GET.toString())
        AuthHelper.authorizeAuthorizationCodeGrant(scopes: "exchanges consent:urn:raidiambank:C1DD33123", builder)
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    def "we can get an exchanges operation events by operation id"() {
        given:
        service.getEventsByOperationId(_ as String, "urn:raidiambank:C1DD33123", _ as Pageable) >> responseExchangesEvents

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/exchanges/v1/operations/testID/events", HttpMethod.GET.toString())
        AuthHelper.authorizeAuthorizationCodeGrant(scopes: "exchanges consent:urn:raidiambank:C1DD33123", builder)
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }
}