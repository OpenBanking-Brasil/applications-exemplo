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
class CreditFixedIncomesControllerSpec extends Specification{
    private static Context lambdaContext = new MockLambdaContext()

    def mapper = new ObjectMapper()

    InvestmentService investmentService = Mock(InvestmentService)

    MicronautLambdaContainerHandler handler


    ResponseCreditFixedIncomesProductList responseCreditFixedIncomesProductList
    ResponseCreditFixedIncomesProductIdentification responseCreditFixedIncomesProductIdentification
    ResponseCreditFixedIncomesBalances responseCreditFixedIncomesBalances
    ResponseCreditFixedIncomesTransactions responseCreditFixedIncomesTransactions

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

        responseCreditFixedIncomesProductList = new ResponseCreditFixedIncomesProductList().data(List.of(new CreditFixedList()
                .brandName("Test")
                .companyCnpj("0000000010101")))
                .meta(new Meta().totalPages(1))

        responseCreditFixedIncomesProductIdentification = new ResponseCreditFixedIncomesProductIdentification().data(new CreditFixedIdentification()
                .issuerInstitutionCnpjNumber("0000000010101")
                .isinCode("ABASBA")
                .clearingCode("BASBAS"))

        responseCreditFixedIncomesBalances = new ResponseCreditFixedIncomesBalances().data(new ResponseCreditFixedIncomesBalancesData()
                .quantity("10")
                .preFixedRate("0.300000")
                .postFixedIndexerPercentage("1.000000"))
        .meta(new Meta().totalPages(1))

        responseCreditFixedIncomesTransactions = new ResponseCreditFixedIncomesTransactions().data(List.of(new CreditFixedIncomesTransactions()
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

    void "we can get all credit fixed incomes"() {
        given:

        investmentService.getCreditFixedIncomesList(_ as Pageable, _ as String) >> responseCreditFixedIncomesProductList

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/credit-fixed-incomes/v1/investments", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "credit-fixed-incomes", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "we can get credit fixed incomes by id"() {
        given:

        investmentService.getCreditFixedIncomesById(_ as String, _ as UUID) >> responseCreditFixedIncomesProductIdentification

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/credit-fixed-incomes/v1/investments/"+UUID.randomUUID(), HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "credit-fixed-incomes", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get balances"() {
        given:
        investmentService.getCreditFixedIncomesBalance(_ as String, _ as UUID) >> responseCreditFixedIncomesBalances

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/credit-fixed-incomes/v1/investments/${UUID.randomUUID()}/balances", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "credit-fixed-incomes", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get transactions"() {
        given:
        investmentService.getCreditFixedIncomesTransactions(_ as String, _ as UUID, _ as LocalDate, _ as LocalDate, _ as Pageable) >> responseCreditFixedIncomesTransactions

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/credit-fixed-incomes/v1/investments/${UUID.randomUUID()}/transactions", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "credit-fixed-incomes", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get transactions-currents"() {
        given:
        investmentService.getCreditFixedIncomesTransactions(_ as String, _ as UUID, _ as LocalDate, _ as LocalDate, _ as Pageable) >> responseCreditFixedIncomesTransactions

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/credit-fixed-incomes/v1/investments/${UUID.randomUUID()}/transactions-current", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "credit-fixed-incomes", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

}
