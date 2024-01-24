package com.raidiam.trustframework.bank.controllers

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder;
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper
import com.raidiam.trustframework.bank.AuthHelper
import com.raidiam.trustframework.bank.TestJwtSigner
import com.raidiam.trustframework.bank.services.InvestmentService;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.context.annotation.Value
import io.micronaut.data.model.Pageable
import io.micronaut.function.aws.proxy.MicronautLambdaContainerHandler
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.runtime.Micronaut;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification;

import javax.inject.Inject
import java.time.LocalDate;
@MicronautTest(transactional = false)
class BankFixedIncomesControllerSpec extends Specification{
    private static Context lambdaContext = new MockLambdaContext()

    def mapper = new ObjectMapper()

    InvestmentService investmentService = Mock(InvestmentService)

    MicronautLambdaContainerHandler handler


    ResponseBankFixedIncomesProductList responseBankFixedIncomesProductList
    ResponseBankFixedIncomesProductIdentification responseBankFixedIncomesProductIdentification
    ResponseBankFixedIncomesBalances responseBankFixedIncomesBalances
    ResponseBankFixedIncomesTransactions responseBankFixedIncomesTransactions

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

        responseBankFixedIncomesProductList = new ResponseBankFixedIncomesProductList().data(List.of(new ResponseBankFixedIncomesProductListData()
                .brandName("Test")
                .companyCnpj("0000000010101")))
                .meta(new Meta().totalPages(1))

        responseBankFixedIncomesProductIdentification = new ResponseBankFixedIncomesProductIdentification().data(new ResponseBankFixedIncomesProductIdentificationData()
                .issuerInstitutionCnpjNumber("0000000010101")
                .isinCode("ABASBA")
                .clearingCode("BASBAS"))

        responseBankFixedIncomesBalances = new ResponseBankFixedIncomesBalances().data(new ResponseBankFixedIncomesBalancesData()
                .quantity("10")
                .preFixedRate("0.300000")
                .postFixedIndexerPercentage("1.000000"))
        .meta(new Meta().totalPages(1))

        responseBankFixedIncomesTransactions = new ResponseBankFixedIncomesTransactions().data(List.of(new ResponseBankFixedIncomesTransactionsData()
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

    void "we can get all bank fixed incomes"() {
        given:

        investmentService.getBankFixedIncomesList(_ as Pageable, _ as String) >> responseBankFixedIncomesProductList

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/bank-fixed-incomes/v1/investments", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "bank-fixed-incomes", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "we can get bank fixed incomes by id"() {
        given:

        investmentService.getBankFixedIncomesById(_ as String, _ as UUID) >> responseBankFixedIncomesProductIdentification

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/bank-fixed-incomes/v1/investments/"+UUID.randomUUID(), HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "bank-fixed-incomes", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get balances"() {
        given:
        investmentService.getBankFixedIncomesBalance(_ as String, _ as UUID) >> responseBankFixedIncomesBalances

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/bank-fixed-incomes/v1/investments/${UUID.randomUUID()}/balances", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "bank-fixed-incomes", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get transactions"() {
        given:
        investmentService.getBankFixedIncomesTransactions(_ as String, _ as UUID, _ as LocalDate, _ as LocalDate, _ as Pageable) >> responseBankFixedIncomesTransactions

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/bank-fixed-incomes/v1/investments/${UUID.randomUUID()}/transactions", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "bank-fixed-incomes", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get transactions-currents"() {
        given:
        investmentService.getBankFixedIncomesTransactions(_ as String, _ as UUID, _ as LocalDate, _ as LocalDate, _ as Pageable) >> responseBankFixedIncomesTransactions

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/bank-fixed-incomes/v1/investments/${UUID.randomUUID()}/transactions-current", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "bank-fixed-incomes", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

}
