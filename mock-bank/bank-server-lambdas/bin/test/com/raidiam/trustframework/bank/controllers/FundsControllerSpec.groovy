package com.raidiam.trustframework.bank.controllers

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.serverless.proxy.model.MultiValuedTreeMap
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
class FundsControllerSpec extends Specification{
    private static Context lambdaContext = new MockLambdaContext()

    def mapper = new ObjectMapper()

    InvestmentService investmentService = Mock(InvestmentService)

    MicronautLambdaContainerHandler handler


    ResponseFundsProductList responseFundsProductList
    ResponseFundsProductIdentification responseFundsProductIdentification
    ResponseFundsBalances responseFundsBalances
    ResponseFundsTransactions responseFundsTransactions

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

        responseFundsProductList = new ResponseFundsProductList().data(List.of(new ResponseFundsProductListData()
                .brandName("Test")
                .companyCnpj("0000000010101")
                .anbimaClass("test")
                .anbimaCategory(EnumFundsAnbimaCategory.CAMBIAL)
                .anbimaSubclass("test")))
                .meta(new Meta().totalPages(1))

        responseFundsProductIdentification = new ResponseFundsProductIdentification().data(new ResponseFundsProductIdentificationData()
                .isinCode("ABASBA")
                .anbimaSubclass("test")
                .anbimaCategory(EnumFundsAnbimaCategory.CAMBIAL)
                .cnpjNumber("0000000010101")
                .anbimaClass("test")
                .name("tst"))

        responseFundsBalances = new ResponseFundsBalances().data(new ResponseFundsBalancesData()
                .referenceDate(LocalDate.of(2023, 2, 1)))
        .meta(new Meta().totalPages(1))

        responseFundsTransactions = new ResponseFundsTransactions().data(List.of(new ResponseFundsTransactionsData()
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

    void "we can get all funds"() {
        given:

        investmentService.getFundsList(_ as Pageable, _ as String) >> responseFundsProductList

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/funds/v1/investments", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "funds", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "we can get funds by id"() {
        given:

        investmentService.getFundsById(_ as String, _ as UUID) >> responseFundsProductIdentification

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/funds/v1/investments/"+UUID.randomUUID(), HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "funds", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get balances"() {
        given:
        investmentService.getFundsBalance(_ as String, _ as UUID) >> responseFundsBalances

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/funds/v1/investments/${UUID.randomUUID()}/balances", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "funds", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get transactions"() {
        given:
        investmentService.getFundsTransactions(_ as String, _ as UUID, _ as LocalDate, _ as LocalDate, _ as Pageable) >> responseFundsTransactions

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/funds/v1/investments/c0826748-22b6-432d-9b3f-a7e5a876e0bf/transactions", HttpMethod.GET.toString())
        var queryParams = new MultiValuedTreeMap()
        queryParams.add("fromTransactionDate", "2022-08-14")
        queryParams.add("toTransactionDate", "2023-08-14")
        builder.multiValueQueryString(queryParams)
        AuthHelper.authorize(scopes: "funds", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
        response.body.contains("/transactions?")
    }

    void "We can get transactions-currents"() {
        given:
        investmentService.getFundsTransactions(_ as String, _ as UUID, _ as LocalDate, _ as LocalDate, _ as Pageable) >> responseFundsTransactions

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/funds/v1/investments/${UUID.randomUUID()}/transactions-current", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "funds", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

}
