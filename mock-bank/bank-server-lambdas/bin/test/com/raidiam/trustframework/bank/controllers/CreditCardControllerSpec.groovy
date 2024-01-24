package com.raidiam.trustframework.bank.controllers

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.raidiam.trustframework.bank.AuthHelper
import com.raidiam.trustframework.bank.TestJwtSigner
import com.raidiam.trustframework.bank.services.CreditCardAccountsService
import com.raidiam.trustframework.bank.utils.BankLambdaUtils
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.data.model.Pageable
import io.micronaut.function.aws.proxy.MicronautLambdaContainerHandler
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.runtime.Micronaut
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject
import java.time.LocalDate

@MicronautTest(transactional = false)
class CreditCardControllerSpec extends Specification {

    private static Context lambdaContext = new MockLambdaContext()

    def mapper = new ObjectMapper()

    CreditCardAccountsService cardAccountsService = Mock(CreditCardAccountsService)

    MicronautLambdaContainerHandler handler

    ResponseCreditCardAccountsList responseCreditCardAccountsList
    ResponseCreditCardAccountsIdentification responseCreditCardAccountsIdentification
    ResponseCreditCardAccountsTransactions responseCreditCardAccountsTransactions
    ResponseCreditCardAccountsLimits responseCreditCardAccountsLimits
    ResponseCreditCardAccountsBills responseCreditCardAccountsBills

    @Inject
    BankLambdaUtils bankLambdaUtils

    @MockBean(BankLambdaUtils)
    BankLambdaUtils bankLambdaUtils() {
        Mock(BankLambdaUtils)
    }

    def setup() {
        mapper.findAndRegisterModules()
        handler = new MicronautLambdaContainerHandler(Micronaut.build().singletons(cardAccountsService, bankLambdaUtils, TestJwtSigner.JWT_SIGNER))

        responseCreditCardAccountsList = new ResponseCreditCardAccountsList().addDataItem(new CreditCardAccountsData().brandName("test")).meta(new Meta().totalPages(1))
        responseCreditCardAccountsIdentification = new ResponseCreditCardAccountsIdentification().data(new CreditCardsAccountsIdentificationData().name("test"))
        responseCreditCardAccountsTransactions = new ResponseCreditCardAccountsTransactions().addDataItem(new CreditCardAccountsTransaction().billId("test")).meta(new Meta().totalPages(1))
        responseCreditCardAccountsLimits = new ResponseCreditCardAccountsLimits().addDataItem(new CreditCardAccountsLimitsData().identificationNumber("test"))
        responseCreditCardAccountsBills = new ResponseCreditCardAccountsBills().addDataItem(new CreditCardAccountsBillsData().billId("test")).meta(new Meta().totalPages(1))

        bankLambdaUtils.getConsentIdFromRequest(_ as HttpRequest<?>) >> "12345"
        bankLambdaUtils.getDateFromRequest(_ as HttpRequest<?>, "fromDueDate") >> Optional.of(LocalDate.now())
        bankLambdaUtils.getDateFromRequest(_ as HttpRequest<?>, "toDueDate") >> Optional.of(LocalDate.now())
        bankLambdaUtils.getDateFromRequest(_ as HttpRequest<?>, "fromTransactionDate") >> Optional.of(LocalDate.now())
        bankLambdaUtils.getDateFromRequest(_ as HttpRequest<?>, "toTransactionDate") >> Optional.of(LocalDate.now())
        bankLambdaUtils.getPayeeMCCFromRequest(_ as HttpRequest<?>) >> Optional.of(new BigDecimal(123.1))
        bankLambdaUtils.getAttributeFromRequest(_ as HttpRequest<?>, "transactionType") >> Optional.of("transactionType")
    }

    def cleanup() {
        handler.close()
    }

    void "we can get all credit card accounts"() {
        given:

        cardAccountsService.getCreditCardAccounts(_ as Pageable, _ as String) >> responseCreditCardAccountsList

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/credit-cards-accounts/v1/accounts', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "credit-cards-accounts", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get credit card account"() {
        given:
        cardAccountsService.getCreditCardAccount(_ as String, _ as String) >> responseCreditCardAccountsIdentification

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/credit-cards-accounts/v1/accounts/${UUID.randomUUID()}", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "credit-cards-accounts", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can't get a credit card account which doesn't exist"() {
        given:
        cardAccountsService.getCreditCardAccount(_ as String, _ as String) >> { throw new HttpStatusException(HttpStatus.BAD_REQUEST, "") }

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/credit-cards-accounts/v1/accounts/${UUID.randomUUID()}", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "credit-cards-accounts", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code
    }

    void "We can get a bills"() {
        given:
        cardAccountsService.getCreditCardAccountsBills(_ as Pageable, _ as String, _ as LocalDate, _ as LocalDate, _ as String) >> responseCreditCardAccountsBills

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/credit-cards-accounts/v1/accounts/${UUID.randomUUID()}/bills", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "credit-cards-accounts", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get transactions"() {
        given:
        cardAccountsService.getCreditCardAccountTransactions(_ as Pageable, _ as String,
                _ as LocalDate, _ as LocalDate,
                _ as BigDecimal, _ as String, _ as String) >> responseCreditCardAccountsTransactions

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/credit-cards-accounts/v1/accounts/${UUID.randomUUID()}/transactions", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "credit-cards-accounts", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get limits"() {
        given:
        cardAccountsService.getCreditCardAccountLimits(_ as String, _ as String) >> responseCreditCardAccountsLimits

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/credit-cards-accounts/v1/accounts/${UUID.randomUUID()}/limits", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "credit-cards-accounts", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get account bills transactions"() {
        given:
        cardAccountsService.getCreditCardAccountBillsTransactions(_ as Pageable, _ as String,
                _ as LocalDate, _ as LocalDate,
                _ as BigDecimal, _ as String, _ as String, _ as String) >> responseCreditCardAccountsTransactions

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/credit-cards-accounts/v1/accounts/${UUID.randomUUID()}/${UUID.randomUUID()}/transactions", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "credit-cards-accounts", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }
}