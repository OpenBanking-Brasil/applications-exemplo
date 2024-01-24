package com.raidiam.trustframework.bank.controllers

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.raidiam.trustframework.bank.AuthHelper
import com.raidiam.trustframework.bank.TestJwtSigner
import com.raidiam.trustframework.bank.services.InvestmentService
import com.raidiam.trustframework.bank.utils.BankLambdaUtils
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.context.annotation.Value
import io.micronaut.data.model.Pageable
import io.micronaut.function.aws.proxy.MicronautLambdaContainerHandler
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.runtime.Micronaut
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject
import java.time.LocalDate

@MicronautTest(transactional = false)
class TreasureTitlesControllerSpec extends Specification{
    private static Context lambdaContext = new MockLambdaContext()

    def mapper = new ObjectMapper()

    InvestmentService investmentService = Mock(InvestmentService)

    MicronautLambdaContainerHandler handler


    ResponseTreasureTitlesProductList responseTreasureTitlesProductList
    ResponseTreasureTitlesProductIdentification responseTreasureTitlesProductIdentification
    ResponseTreasureTitlesBalances responseTreasureTitlesBalances
    ResponseTreasureTitlesTransactions responseTreasureTitlesTransactions

    @Inject
    BankLambdaUtils bankLambdaUtils

    @Value("\${mockbank.mockbankUrl}")
    String appBaseUrl

    @MockBean(BankLambdaUtils)
    BankLambdaUtils bankLambdaUtils() {
        Mock(BankLambdaUtils)
    }

    def setup() {
        mapper.findAndRegisterModules()
        handler = new MicronautLambdaContainerHandler(Micronaut.build().singletons(investmentService, bankLambdaUtils, TestJwtSigner.JWT_SIGNER))

        responseTreasureTitlesProductList = new ResponseTreasureTitlesProductList().data(List.of(new ResponseTreasureTitlesProductListData()
                .brandName("Test")
                .companyCnpj("0000000010101")))
                .meta(new Meta().totalPages(1))

        responseTreasureTitlesProductIdentification = new ResponseTreasureTitlesProductIdentification().data(new ResponseTreasureTitlesProductIdentificationData()
                .isinCode("ABASBA"))

        responseTreasureTitlesBalances = new ResponseTreasureTitlesBalances().data(new ResponseTreasureTitlesBalancesData()
                .quantity("10"))
        .meta(new Meta().totalPages(1))

        responseTreasureTitlesTransactions = new ResponseTreasureTitlesTransactions().data(List.of(new ResponseTreasureTitlesTransactionsData()
                .transactionId(UUID.randomUUID().toString())))
                .meta(new Meta().totalPages(1))

        bankLambdaUtils.getConsentIdFromRequest(_ as HttpRequest<?>) >> "12345"
        bankLambdaUtils.getDateFromRequest(_ as HttpRequest<?>, "fromDueDate") >> Optional.of(LocalDate.now())
        bankLambdaUtils.getDateFromRequest(_ as HttpRequest<?>, "toDueDate") >> Optional.of(LocalDate.now())
        bankLambdaUtils.getDateFromRequest(_ as HttpRequest<?>, "fromTransactionDate") >> Optional.of(LocalDate.now())
        bankLambdaUtils.getDateFromRequest(_ as HttpRequest<?>, "toTransactionDate") >> Optional.of(LocalDate.now())
    }

    def cleanup() {
        handler.close()
    }

    void "we can get all treasure titles"() {
        given:

        investmentService.getTreasureTitlesList(_ as Pageable, _ as String) >> responseTreasureTitlesProductList

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/treasure-titles/v1/investments", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "treasure-titles", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "we can get treasure titles by id"() {
        given:

        investmentService.getTreasureTitlesById(_ as String, _ as UUID) >> responseTreasureTitlesProductIdentification

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/treasure-titles/v1/investments/"+UUID.randomUUID(), HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "treasure-titles", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get balances"() {
        given:
        investmentService.getTreasureTitlesBalance(_ as String, _ as UUID) >> responseTreasureTitlesBalances

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/treasure-titles/v1/investments/${UUID.randomUUID()}/balances", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "treasure-titles", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get transactions"() {
        given:
        investmentService.getTreasureTitlesTransactions(_ as String, _ as UUID, _ as LocalDate, _ as LocalDate, _ as Pageable) >> responseTreasureTitlesTransactions

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/treasure-titles/v1/investments/${UUID.randomUUID()}/transactions", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "treasure-titles", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get transactions-currents"() {
        given:
        investmentService.getTreasureTitlesTransactions(_ as String, _ as UUID, _ as LocalDate, _ as LocalDate, _ as Pageable) >> responseTreasureTitlesTransactions

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/treasure-titles/v1/investments/${UUID.randomUUID()}/transactions-current", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "treasure-titles", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

}
