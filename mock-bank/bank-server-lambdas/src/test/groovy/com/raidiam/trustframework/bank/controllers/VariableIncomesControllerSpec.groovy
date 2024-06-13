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
class VariableIncomesControllerSpec extends Specification{
    private static Context lambdaContext = new MockLambdaContext()

    def mapper = new ObjectMapper()

    InvestmentService investmentService = Mock(InvestmentService)

    MicronautLambdaContainerHandler handler


    ResponseVariableIncomesProductList responseVariableIncomesProductList
    ResponseVariableIncomesProductIdentification responseVariableIncomesProductIdentification
    ResponseVariableIncomesBalance responseVariableIncomesBalances
    ResponseVariableIncomesTransactions responseVariableIncomesTransactions
    ResponseVariableIncomesBroker responseVariableIncomesBrokerNotes

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

        responseVariableIncomesProductList = new ResponseVariableIncomesProductList().data(List.of(new ResponseVariableIncomesProductListData()
                .brandName("Test")
                .companyCnpj("0000000010101")))
                .meta(new Meta().totalPages(1))

        responseVariableIncomesProductIdentification = new ResponseVariableIncomesProductIdentification().data(new ResponseVariableIncomesProductIdentificationData()
                .issuerInstitutionCnpjNumber("0000000010101")
                .isinCode("ABASBA"))

        responseVariableIncomesBalances = new ResponseVariableIncomesBalance().data(
                List.of(new ResponseVariableIncomesBalanceData().quantity("10")))
        .meta(new Meta().totalPages(1))

        responseVariableIncomesTransactions = new ResponseVariableIncomesTransactions().data(List.of(new ResponseVariableIncomesTransactionsData()
                .transactionId(UUID.randomUUID().toString())))
                .meta(new Meta().totalPages(1))

        responseVariableIncomesBrokerNotes = new ResponseVariableIncomesBroker().data(new ResponseVariableIncomesBrokerData()
                .brokerNoteNumber("12321321"))
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

        investmentService.getVariableIncomesList(_ as Pageable, _ as String) >> responseVariableIncomesProductList

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/variable-incomes/v1/investments", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "variable-incomes", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "we can get credit fixed incomes by id"() {
        given:

        investmentService.getVariableIncomesById(_ as String, _ as UUID) >> responseVariableIncomesProductIdentification

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/variable-incomes/v1/investments/"+UUID.randomUUID(), HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "variable-incomes", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get balances"() {
        given:
        investmentService.getVariableIncomesBalance(_ as String, _ as UUID) >> responseVariableIncomesBalances

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/variable-incomes/v1/investments/${UUID.randomUUID()}/balances", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "variable-incomes", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get transactions"() {
        given:
        investmentService.getVariableIncomesTransactions(_ as String, _ as UUID, _ as LocalDate, _ as LocalDate, _ as Pageable) >> responseVariableIncomesTransactions

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/variable-incomes/v1/investments/${UUID.randomUUID()}/transactions", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "variable-incomes", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get transactions-currents"() {
        given:
        investmentService.getVariableIncomesTransactions(_ as String, _ as UUID, _ as LocalDate, _ as LocalDate, _ as Pageable) >> responseVariableIncomesTransactions

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/variable-incomes/v1/investments/${UUID.randomUUID()}/transactions-current", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "variable-incomes", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get broker notes"() {
        given:
        investmentService.getVariableIncomesBroker(_ as String, _ as UUID) >> responseVariableIncomesBrokerNotes

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/variable-incomes/v1/broker-notes/${UUID.randomUUID()}", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "variable-incomes", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }
}
