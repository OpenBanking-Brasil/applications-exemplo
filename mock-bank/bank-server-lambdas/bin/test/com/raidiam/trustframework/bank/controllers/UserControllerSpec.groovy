package com.raidiam.trustframework.bank.controllers

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.serverless.proxy.model.AwsProxyRequest
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.raidiam.trustframework.bank.AuthHelper
import com.raidiam.trustframework.bank.TestEntityDataFactory
import com.raidiam.trustframework.bank.domain.AccountEntity
import com.raidiam.trustframework.bank.domain.ContractEntity
import com.raidiam.trustframework.bank.domain.CreditCardAccountsEntity
import com.raidiam.trustframework.bank.enums.AccountOrContractType
import com.raidiam.trustframework.bank.services.UserService
import com.raidiam.trustframework.mockbank.models.generated.ResponseAccountList
import com.raidiam.trustframework.mockbank.models.generated.ResponseCreditCardAccountsList
import io.micronaut.function.aws.proxy.MicronautLambdaContainerHandler
import io.micronaut.http.HttpMethod
import io.micronaut.runtime.Micronaut
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Specification

@MicronautTest
class UserControllerSpec extends Specification {

    private static Context lambdaContext = new MockLambdaContext()

    UserService userService = Mock(UserService)

    @Shared
    ResponseCreditCardAccountsList responseCreditCardAccountsList

    @Shared
    ResponseAccountList responseAccountList

    @Shared
    ResponseAccountList responseLoanList
    @Shared
    ResponseAccountList responseFinancingList
    @Shared
    ResponseAccountList responseInvoiceFinancingList
    @Shared
    ResponseAccountList responseUnarrangedOverDraftList

    @Shared
    AccountEntity testAccount
    @Shared
    CreditCardAccountsEntity testCCAccount
    @Shared
    ContractEntity testLoan
    @Shared
    ContractEntity testFinancing
    @Shared
    ContractEntity testInvoiceFinancing
    @Shared
    ContractEntity testUnarrangedOverDraft

    MicronautLambdaContainerHandler handler
    def mapper = new ObjectMapper()

    def setupSpec () {
        testCCAccount = TestEntityDataFactory.anCreditCardAccounts(UUID.randomUUID())
        testCCAccount.setCreditCardAccountId(UUID.randomUUID())
        responseCreditCardAccountsList = new ResponseCreditCardAccountsList().data(List.of(testCCAccount.getCreditCardAccountsData()))

        testAccount = TestEntityDataFactory.anAccount(UUID.randomUUID())
        testAccount.setAccountId(UUID.randomUUID())
        responseAccountList = new ResponseAccountList().data(List.of(testAccount.getAccountData()))

        testLoan = TestEntityDataFactory.aContractEntity(UUID.randomUUID(), AccountOrContractType.LOAN, "Test Loan", "Test Loan Subtype")
        testLoan.setContractId(UUID.randomUUID())
        responseLoanList = new ResponseAccountList().data(List.of(testLoan.createSparseAccountData()))
        testFinancing = TestEntityDataFactory.aContractEntity(UUID.randomUUID(), AccountOrContractType.FINANCING, "Test Financing", "Test Financing Subtype")
        testFinancing.setContractId(UUID.randomUUID())
        responseFinancingList = new ResponseAccountList().data(List.of(testFinancing.createSparseAccountData()))
        testInvoiceFinancing = TestEntityDataFactory.aContractEntity(UUID.randomUUID(), AccountOrContractType.INVOICE_FINANCING, "Test Invoice Financing", "Test Invoice Financing Subtype")
        testInvoiceFinancing.setContractId(UUID.randomUUID())
        responseInvoiceFinancingList = new ResponseAccountList().data(List.of(testInvoiceFinancing.createSparseAccountData()))
        testUnarrangedOverDraft = TestEntityDataFactory.aContractEntity(UUID.randomUUID(), AccountOrContractType.UNARRANGED_ACCOUNT_OVERDRAFT, "Test Overdraft", "Test Overdraft Subtype")
        testUnarrangedOverDraft.setContractId(UUID.randomUUID())
        responseUnarrangedOverDraftList = new ResponseAccountList().data(List.of(testUnarrangedOverDraft.createSparseAccountData()))
    }

    def setup () {
        mapper.findAndRegisterModules()
        handler = new MicronautLambdaContainerHandler(Micronaut.build().singletons(userService))
    }

    def cleanup() {
        handler.close()
    }

    def "we can get credit card accounts" () {
        given:
        userService.getCreditCardAccounts(_ as String) >> responseCreditCardAccountsList

        AwsProxyRequest request = new AwsProxyRequestBuilder("/user/${testCCAccount.getAccountHolderId()}/credit-card-accounts", HttpMethod.GET.toString()).build()

        when:
        def response = handler.proxy(request, lambdaContext)

        then:
        noExceptionThrown()
        response != null
        response.getBody() != null

        when:
        def list = mapper.readValue(response.getBody(), ResponseCreditCardAccountsList)

        then:
        noExceptionThrown()
        list != null
        list.getData().size() == 1
        list.getData().first().getCreditCardAccountId() == testCCAccount.getCreditCardAccountId().toString()
    }

    def "we can get accounts without authorisation" () {
        given:
        userService.getAccounts(_ as String) >> responseAccountList

        AwsProxyRequest request = new AwsProxyRequestBuilder("/user/${testAccount.getAccountHolderId()}/accounts", HttpMethod.GET.toString()).build()

        when:
        def response = handler.proxy(request, lambdaContext)

        then:
        noExceptionThrown()
        response != null
        response.getBody() != null

        when:
        def list = mapper.readValue(response.getBody(), ResponseAccountList)

        then:
        noExceptionThrown()
        list != null
        list.getData().size() == 1
        list.getData().first().getAccountId() == testAccount.getAccountId().toString()
    }

    def "we can get accounts with authorisation" () {
        given:
        userService.getAccounts(_ as String) >> responseAccountList

        def builder = new AwsProxyRequestBuilder("/user/${testAccount.getAccountHolderId()}/accounts", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "openid user:account user:consent op:consent consent consents op:payments", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        noExceptionThrown()
        response != null
        response.getBody() != null

        when:
        def list = mapper.readValue(response.getBody(), ResponseAccountList)

        then:
        noExceptionThrown()
        list != null
        list.getData().size() == 1
        list.getData().first().getAccountId() == testAccount.getAccountId().toString()
    }

    def "we can get loans" () {
        given:
        userService.getContractList(_ as String, AccountOrContractType.LOAN) >> responseLoanList

        AwsProxyRequest request = new AwsProxyRequestBuilder("/user/${testLoan.getAccountHolderId()}/loans", HttpMethod.GET.toString()).build()

        when:
        def response = handler.proxy(request, lambdaContext)

        then:
        noExceptionThrown()
        response != null
        response.getBody() != null

        when:
        def list = mapper.readValue(response.getBody(), ResponseAccountList)

        then:
        noExceptionThrown()
        list != null
        list.getData().size() == 1
        list.getData().first().getAccountId() == testLoan.getContractId().toString()
    }

    def "we can get financings" () {
        given:
        userService.getContractList(_ as String, AccountOrContractType.FINANCING) >> responseFinancingList

        AwsProxyRequest request = new AwsProxyRequestBuilder("/user/${testFinancing.getAccountHolderId()}/financings", HttpMethod.GET.toString()).build()

        when:
        def response = handler.proxy(request, lambdaContext)

        then:
        noExceptionThrown()
        response != null
        response.getBody() != null

        when:
        def list = mapper.readValue(response.getBody(), ResponseAccountList)

        then:
        noExceptionThrown()
        list != null
        list.getData().size() == 1
        list.getData().first().getAccountId() == testFinancing.getContractId().toString()
    }

    def "we can get invoice financings" () {
        given:
        userService.getContractList(_ as String, AccountOrContractType.INVOICE_FINANCING) >> responseInvoiceFinancingList

        AwsProxyRequest request = new AwsProxyRequestBuilder("/user/${testInvoiceFinancing.getAccountHolderId()}/invoice-financings", HttpMethod.GET.toString()).build()

        when:
        def response = handler.proxy(request, lambdaContext)

        then:
        noExceptionThrown()
        response != null
        response.getBody() != null

        when:
        def list = mapper.readValue(response.getBody(), ResponseAccountList)

        then:
        noExceptionThrown()
        list != null
        list.getData().size() == 1
        list.getData().first().getAccountId() == testInvoiceFinancing.getContractId().toString()
    }

    def "we can get unarrangedAccountsOverdrafts" () {
        given:
        userService.getContractList(_ as String, AccountOrContractType.UNARRANGED_ACCOUNT_OVERDRAFT) >> responseUnarrangedOverDraftList

        AwsProxyRequest request = new AwsProxyRequestBuilder("/user/${testUnarrangedOverDraft.getAccountHolderId()}/unarranged-accounts-overdraft", HttpMethod.GET.toString()).build()

        when:
        def response = handler.proxy(request, lambdaContext)

        then:
        noExceptionThrown()
        response != null
        response.getBody() != null

        when:
        def list = mapper.readValue(response.getBody(), ResponseAccountList)

        then:
        noExceptionThrown()
        list != null
        list.getData().size() == 1
        list.getData().first().getAccountId() == testUnarrangedOverDraft.getContractId().toString()
    }
}
