package com.raidiam.trustframework.bank.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.Payload
import com.nimbusds.jose.PlainHeader
import com.nimbusds.jose.PlainObject
import com.raidiam.trustframework.bank.CleanupLocalStackSpecification
import com.raidiam.trustframework.bank.TestRequestDataFactory
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.rxjava2.http.client.RxHttpClient
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import org.junit.ClassRule
import org.mockserver.junit.MockServerRule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.spock.Testcontainers
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Stepwise

import javax.inject.Inject

import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccountHolder

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
@Testcontainers
class AdminApiControllersSpec extends CleanupLocalStackSpecification implements TestPropertyProvider {
    @Shared
    Logger log = LoggerFactory.getLogger(AdminApiControllersSpec.class)

    @ClassRule
    @Shared
    private MockServerRule mockserver = new MockServerRule(this)

    @Override
    Map<String, String> getProperties() {
        return [
                'somekey.somesubkey': 'someothervalue',
        ]
    }

    @Shared
    @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)

    @Shared
    static mapper = new ObjectMapper()

    @Inject
    @Client('/')
    RxHttpClient client

    @Shared
    String accountHolderId
    @Shared
    String postedAccountId
    @Shared
    String postedTransactionId
    @Shared
    String adminUrl
    @Shared
    def adminToken

    def setupSpec() {
        mapper.findAndRegisterModules()
    }

    def setup() {
        if (runSetup) {
            accountHolderId = accountHolderRepository.save(anAccountHolder()).getAccountHolderId().toString()
            adminUrl = "/admin/customers/" + accountHolderId
            adminToken = new PlainObject(new PlainHeader(), new Payload([scope: "op:admin", client_id: "client1", subject: "ralph.bragg@gmail.com"])).serialize()
            runSetup = false
        }
    }

    def cleanupSpec() {

    }

    void "we can post put an account"() {
        given:
        CreateAccount newAccount = TestRequestDataFactory.createAccount()
        String requestBody = mapper.writeValueAsString(newAccount)

        when:// we can post account
        ResponseAccountData postResponse = client.toBlocking()
                .retrieve(HttpRequest.POST("${adminUrl}/accounts", requestBody)
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponseAccount).getData()

        postedAccountId = postResponse.getAccountId()

        then:
        postResponse.getAccountId() != null
        postResponse.getAccountType() == newAccount.getData().getAccountType()
        postResponse.getAccountSubType() == newAccount.getData().getAccountSubType()
        postResponse.getNumber() == newAccount.getData().getNumber()
        postResponse.getBrandName() == newAccount.getData().getBrandName()
        postResponse.getBranchCode() == newAccount.getData().getBranchCode()
        postResponse.getCompeCode() == newAccount.getData().getCompeCode()
        postResponse.getCompanyCnpj() == newAccount.getData().getCompanyCnpj()
        postResponse.getCheckDigit() == newAccount.getData().getCheckDigit()
        postResponse.getCurrency() == newAccount.getData().getCurrency()
        postResponse.getStatus() == newAccount.getData().getStatus()
        //Limits
        postResponse.getOverdraftContractedLimit() == newAccount.getData().getOverdraftContractedLimit()
        postResponse.getOverdraftContractedLimitCurrency() == newAccount.getData().getOverdraftContractedLimitCurrency()
        postResponse.getOverdraftUsedLimit() == newAccount.getData().getOverdraftUsedLimit()
        postResponse.getOverdraftUsedLimitCurrency() == newAccount.getData().getOverdraftUsedLimitCurrency()
        postResponse.getUnarrangedOverdraftAmount() == newAccount.getData().getUnarrangedOverdraftAmount()
        postResponse.getUnarrangedOverdraftAmountCurrency() == newAccount.getData().getUnarrangedOverdraftAmountCurrency()

        when:
        // we can update account
        String newBrandName = "newBrandName"
        def updated = TestRequestDataFactory.editedAccountDto()
        updated.setBrandName(newBrandName)

        String url = "${adminUrl}/accounts/${postedAccountId}"
        ResponseAccountData putResponse = client.toBlocking()
                .retrieve(HttpRequest.PUT(url, mapper.writeValueAsString(new EditedAccount().data(updated)))
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponseAccount).getData()

        then:
        putResponse.getBrandName() == newBrandName
    }

    void "we can post put account transactions"() {
        when:
        CreateAccountTransaction accountTransactionDto = TestRequestDataFactory.createAccountTransaction()
        String requestBody = mapper.writeValueAsString(accountTransactionDto)

        String postUrl = "${adminUrl}/accounts/${postedAccountId}/transactions"
        ResponseAccountTransactionData postResponse = client.toBlocking()
                .retrieve(HttpRequest.POST(postUrl, requestBody)
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponseAccountTransaction).getData()
        postedTransactionId = postResponse.getTransactionId()

        then:
        postResponse.getTransactionId() != null
        postResponse.getTransactionName() == accountTransactionDto.getData().getTransactionName()
        postResponse.getAmount() == accountTransactionDto.getData().getAmount()
        postResponse.getCompletedAuthorisedPaymentType() == accountTransactionDto.getData().getCompletedAuthorisedPaymentType()
        postResponse.getCreditDebitType() == accountTransactionDto.getData().getCreditDebitType()
        postResponse.getPartieBranchCode() == accountTransactionDto.getData().getPartieBranchCode()
        postResponse.getPartieCheckDigit() == accountTransactionDto.getData().getPartieCheckDigit()
        postResponse.getPartieCnpjCpf() == accountTransactionDto.getData().getPartieCnpjCpf()
        postResponse.getPartieNumber() == accountTransactionDto.getData().getPartieNumber()
        postResponse.getPartieCompeCode() == accountTransactionDto.getData().getPartieCompeCode()
        postResponse.getPartiePersonType() == accountTransactionDto.getData().getPartiePersonType()
        postResponse.getTransactionCurrency() == accountTransactionDto.getData().getTransactionCurrency()
        postResponse.getType() == accountTransactionDto.getData().getType()

        when:
        // we can update account Transaction
        Double newAmount = 99999.999
        accountTransactionDto.getData().setAmount(newAmount)
        String putUrl = "${adminUrl}/accounts/${postedAccountId}/transactions/${postedTransactionId}"
        ResponseAccountTransactionData putResponse = client.toBlocking()
                .retrieve(HttpRequest.PUT(putUrl, mapper.writeValueAsString(accountTransactionDto))
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponseAccountTransaction).getData()

        then:
        putResponse.getAmount() == newAmount
    }

    def "we can delete Account Transaction"() {
        when:
        String url = "${adminUrl}/accounts/${postedAccountId}/transactions/${postedTransactionId}"
        HttpStatus deleteResponse = client.toBlocking()
                .retrieve(HttpRequest.DELETE(url)
                        .header("Authorization", "Bearer ${adminToken}"),
                        HttpStatus)
        then:
        deleteResponse.getCode() == HttpStatus.NO_CONTENT.code
    }

    def "we can delete account"() {
        when:
        HttpStatus deleteResponse = client.toBlocking()
                .retrieve(HttpRequest.DELETE("${adminUrl}/accounts/${postedAccountId}")
                        .header("Authorization", "Bearer ${adminToken}"),
                        HttpStatus)

        then:
        deleteResponse.getCode() == HttpStatus.NO_CONTENT.code
    }

    void "we can post put a Credit Card Account"() {
        given:
        CreateCreditCardAccount newAccount = TestRequestDataFactory.creditCardAccount()
        String requestBody = mapper.writeValueAsString(newAccount)

        when:// we can post Credit Card Account
        ResponseCreditCardAccount postResponse = client.toBlocking()
                .retrieve(HttpRequest.POST("${adminUrl}/credit-cards-accounts", requestBody)
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponseCreditCardAccount)

        postedAccountId = postResponse.getData().getCreditCardAccountId().toString()

        then:
        def account = postResponse.getData()
        account.getCreditCardAccountId() != null
        account.getBrandName() == newAccount.getData().getBrandName()
        account.getCompanyCnpj() == newAccount.getData().getCompanyCnpj()
        account.getName() == newAccount.getData().getName()
        account.getProductType() == newAccount.getData().getProductType()
        account.getProductAdditionalInfo() == newAccount.getData().getProductAdditionalInfo()
        account.getCreditCardNetwork() == newAccount.getData().getCreditCardNetwork()
        account.getNetworkAdditionalInfo() == newAccount.getData().getNetworkAdditionalInfo()
        account.getStatus() == newAccount.getData().getStatus()
        account.getPaymentMethod() != null
        account.getPaymentMethod().size() == newAccount.getData().getPaymentMethod().size()
        account.getPaymentMethod().containsAll(newAccount.getData().getPaymentMethod())

        when:
        // we can update Credit Card Account
        String newBrandName = "newBrandName"
        def updated = TestRequestDataFactory.editCardAccountDto()
        updated.getData().setBrandName(newBrandName)
        String url = "${adminUrl}/credit-cards-accounts/${postedAccountId}"
        ResponseCreditCardAccountData putResponse = client.toBlocking()
                .retrieve(HttpRequest.PUT(url, mapper.writeValueAsString(updated))
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponseCreditCardAccount).getData()

        then:
        putResponse.getBrandName() == newBrandName
    }

    void "we can post put delete a Credit Card Account Limit"() {
        given:
        CreateCreditCardAccountLimits newLimitDto = TestRequestDataFactory.creditCardAccountLimitDto()
        String requestBody = mapper.writeValueAsString(newLimitDto)

        when:
        String postUrl = "${adminUrl}/credit-cards-accounts/${postedAccountId}/limits"
        ResponseCreditCardAccountLimits postResponse = client.toBlocking()
                .retrieve(HttpRequest.POST(postUrl, requestBody)
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponseCreditCardAccountLimits)

        then:
        postResponse.getData() != null

        when:
        // we can update Credit Card limits
        CreateCreditCardAccountLimits editedLimitDto = TestRequestDataFactory.creditCardAccountLimitDto()
        String url = "${adminUrl}/credit-cards-accounts/${postedAccountId}/limits/"
        ResponseCreditCardAccountLimits putResponse = client.toBlocking()
                .retrieve(HttpRequest.PUT(url, mapper.writeValueAsString(editedLimitDto))
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponseCreditCardAccountLimits)

        then:
        putResponse.getData() != null

        when:
        HttpStatus deleteResponse = client.toBlocking()
                .retrieve(HttpRequest.DELETE(url)
                        .header("Authorization", "Bearer ${adminToken}"),
                        HttpStatus)

        then:
        deleteResponse.getCode() == HttpStatus.NO_CONTENT.code
    }

    void "we can post put delete Credit Card Account Bills and Transactions"() {
        given:
        CreateCreditCardAccountBill newBillDto = TestRequestDataFactory.creditCardBillDto()
        String requestBody = mapper.writeValueAsString(newBillDto)
        String billId

        when://POST Bills
        String postUrl = "${adminUrl}/credit-cards-accounts/${postedAccountId}/bills"
        ResponseCreditCardAccountBill postResponse = client.toBlocking()
                .retrieve(HttpRequest.POST(postUrl, requestBody)
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponseCreditCardAccountBill)

        billId = postResponse.getData().getBillId().toString()

        then:
        def bill = postResponse.getData()
        bill.getBillId() != null
        bill.getDueDate() == newBillDto.getData().getDueDate()
        bill.getBillTotalAmount() == newBillDto.getData().getBillTotalAmount()
        bill.getBillTotalAmountCurrency() == newBillDto.getData().getBillTotalAmountCurrency()
        bill.getBillMinimumAmount() == newBillDto.getData().getBillMinimumAmount()
        bill.getBillMinimumAmountCurrency() == newBillDto.getData().getBillMinimumAmountCurrency()
        bill.isInstalment() == newBillDto.getData().isInstalment()
        bill.getFinanceCharges() != null
        bill.getFinanceCharges().containsAll(newBillDto.getData().getFinanceCharges())
        bill.getPayments() != null
        bill.getPayments().containsAll(newBillDto.getData().getPayments())

        when://PUT Bills
        CreateCreditCardAccountBill editedBillDto = TestRequestDataFactory.creditCardBillDto()
        String putBillUrl = "${adminUrl}/credit-cards-accounts/${postedAccountId}/bills/${bill.getBillId()}"
        ResponseCreditCardAccountBill putResponse = client.toBlocking()
                .retrieve(HttpRequest.PUT(putBillUrl, mapper.writeValueAsString(editedBillDto))
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponseCreditCardAccountBill)

        then:
        putResponse.getData().getBillMinimumAmount() != bill.getBillMinimumAmount()

        when://POST Transactions
        CreateCreditCardAccountTransactionData newTransactions = TestRequestDataFactory.cardAccountTransactionDto()
        String postTransactionsUrl = "${adminUrl}/credit-cards-accounts/${postedAccountId}/bills/${billId}/transactions"
        ResponseCreditCardAccountTransactionList responseTransactionList = client.toBlocking()
                .retrieve(HttpRequest.POST(postTransactionsUrl, mapper.writeValueAsString(
                        new CreateCreditCardAccountTransactionList().data(List.of(newTransactions))))
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponseCreditCardAccountTransactionList)

        then:
        def transaction = responseTransactionList.getData().first()
        transaction.getTransactionId() != null
        transaction.identificationNumber == newTransactions.identificationNumber
        transaction.lineName == newTransactions.lineName
        transaction.transactionName == newTransactions.transactionName
        transaction.creditDebitType == newTransactions.creditDebitType
        transaction.transactionType == newTransactions.transactionType
        transaction.transactionalAdditionalInfo == newTransactions.transactionalAdditionalInfo
        transaction.paymentType == newTransactions.paymentType
        transaction.feeType == newTransactions.feeType
        transaction.feeTypeAdditionalInfo == newTransactions.feeTypeAdditionalInfo
        transaction.otherCreditsType == newTransactions.otherCreditsType
        transaction.otherCreditsAdditionalInfo == newTransactions.otherCreditsAdditionalInfo
        transaction.chargeIdentificator == newTransactions.chargeIdentificator
        transaction.brazilianAmount == newTransactions.brazilianAmount
        transaction.chargeNumber == newTransactions.chargeNumber
        transaction.amount == newTransactions.amount
        transaction.currency == newTransactions.currency
        transaction.billPostDate == newTransactions.billPostDate
        transaction.payeeMCC == newTransactions.payeeMCC


        when://PUT Transactions
        def editedTransactions = TestRequestDataFactory.cardAccountTransactionDto()
        String putTransactionsUrl = "${adminUrl}/credit-cards-accounts/${postedAccountId}/bills/${bill.getBillId()}/transactions/${transaction.getTransactionId()}"
        ResponseCreditCardAccountTransaction responseTransaction = client.toBlocking()
                .retrieve(HttpRequest.PUT(putTransactionsUrl, mapper.writeValueAsString(
                        new EditedCreditCardAccountTransaction().data(editedTransactions)))
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponseCreditCardAccountTransaction)

        then:
        responseTransaction.getData() != null
        when://DELETE Transactions
        HttpStatus deleteTransactionsResponse = client.toBlocking()
                .retrieve(HttpRequest.DELETE(putTransactionsUrl)
                        .header("Authorization", "Bearer ${adminToken}"),
                        HttpStatus)

        then:
        deleteTransactionsResponse.getCode() == HttpStatus.NO_CONTENT.code

        when://DELETE Bill
        HttpStatus deleteBillResponse = client.toBlocking()
                .retrieve(HttpRequest.DELETE(putBillUrl)
                        .header("Authorization", "Bearer ${adminToken}"),
                        HttpStatus)

        then:
        deleteBillResponse.getCode() == HttpStatus.NO_CONTENT.code
    }

    def "we can delete Credit Card Account"() {
        when:
        HttpStatus deleteResponse = client.toBlocking()
                .retrieve(HttpRequest.DELETE("${adminUrl}/credit-cards-accounts/${postedAccountId}")
                        .header("Authorization", "Bearer ${adminToken}"),
                        HttpStatus)

        then:
        deleteResponse.getCode() == HttpStatus.NO_CONTENT.code
    }

    void "we can post put Contracts"() {
        given:
        CreateContract newContractDto = TestRequestDataFactory.createContract(EnumContractType.LOAN)
        String requestBody = mapper.writeValueAsString(newContractDto)

        when:// we can post Contracts
        ResponseContract postResponse = client.toBlocking()
                .retrieve(HttpRequest.POST("${adminUrl}/loans", requestBody)
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponseContract)

        postedAccountId = postResponse.getData().getContractId().toString()

        then:
        def contract = postResponse.getData()
        contract.getContractId() != null
        contract.getDueDate() == newContractDto.getData().getDueDate()
        contract.getCurrency() == newContractDto.getData().getCurrency()
        contract.getProductType() == newContractDto.getData().getProductType()
        contract.getStatus() == newContractDto.getData().getStatus()
        contract.getCompanyCnpj() == newContractDto.getData().getCompanyCnpj()
        contract.getAmortizationScheduled() == newContractDto.getData().getAmortizationScheduled()
        contract.getAmortizationScheduledAdditionalInfo() == newContractDto.getData().getAmortizationScheduledAdditionalInfo()
        contract.getCet() == newContractDto.getData().getCet()
        contract.getContractAmount() == newContractDto.getData().getContractAmount()
        contract.getContractDate() == newContractDto.getData().getContractDate()
        contract.getContractNumber() == newContractDto.getData().getContractNumber()
        contract.getContractOutstandingBalance() == newContractDto.getData().getContractOutstandingBalance()
        contract.getContractRemainingNumber() == newContractDto.getData().getContractRemainingNumber()
        contract.getContractType() == newContractDto.getData().getContractType()
        contract.getDisbursementDate() == newContractDto.getData().getDisbursementDate()
        contract.getDueInstalments() == newContractDto.getData().getDueInstalments()
        contract.getFirstInstalmentDueDate() == newContractDto.getData().getFirstInstalmentDueDate()
        contract.getInstalmentPeriodicity() == newContractDto.getData().getInstalmentPeriodicity()
        contract.getInstalmentPeriodicityAdditionalInfo() == newContractDto.getData().getInstalmentPeriodicityAdditionalInfo()
        contract.getIpocCode() == newContractDto.getData().getIpocCode()
        contract.getPaidInstalments() == newContractDto.getData().getPaidInstalments()
        contract.getPastDueInstalments() == newContractDto.getData().getPastDueInstalments()
        contract.getProductName() == newContractDto.getData().getProductName()
        contract.getProductSubType() == newContractDto.getData().getProductSubType()
        contract.getSettlementDate() == newContractDto.getData().getSettlementDate()
        contract.getTotalNumberOfInstalments() == newContractDto.getData().getTotalNumberOfInstalments()
        contract.getTypeContractRemaining() == newContractDto.getData().getTypeContractRemaining()
        contract.getTypeNumberOfInstalments() == newContractDto.getData().getTypeNumberOfInstalments()
        contract.getBalloonPayments() != null
        contract.getBalloonPayments().containsAll(newContractDto.getData().getBalloonPayments())
        contract.getContractedFees() != null
        contract.getContractedFees().containsAll(newContractDto.getData().getContractedFees())
        contract.getContractedFinanceCharges() != null
        contract.getContractedFinanceCharges().containsAll(newContractDto.getData().getContractedFinanceCharges())
        contract.getInterestRates() != null
        contract.getInterestRates().containsAll(newContractDto.getData().getInterestRates())
        contract.getReleases() != null

        def responseReleasesDto = contract.getReleases().first()
        def newReleasesDto = newContractDto.getData().getReleases().stream()
                .filter(c -> c.getInstalmentId().equals(responseReleasesDto.getInstalmentId()))
                .findFirst()
                .get()
        responseReleasesDto.getPaymentsId() != null
        responseReleasesDto.getPaidAmount() == newReleasesDto.getPaidAmount()
        responseReleasesDto.getPaidDate() == newReleasesDto.getPaidDate()
        responseReleasesDto.getCurrency() == newReleasesDto.getCurrency()
        responseReleasesDto.getOverParcelCharges().containsAll(newReleasesDto.getOverParcelCharges())
        responseReleasesDto.getOverParcelFees().containsAll(newReleasesDto.getOverParcelFees())

        when:
        // we can update Contracts
        String putUrl = "${adminUrl}/loans/" + postedAccountId

        EditedContractData editedContract = TestRequestDataFactory.createEditedContract()
        ResponseContract putResponse = client.toBlocking()
                .retrieve(HttpRequest.PUT(putUrl, mapper.writeValueAsString(new EditedContract().data(editedContract)))
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponseContract)

        then:
        putResponse.getData() != null
    }

    void "we can post put Warranties"() {
        given:
        ContractWarrantiesData newWarranties = TestRequestDataFactory.createWarranties()
        when:// we can post warranties
        String url = "${adminUrl}/loans/${postedAccountId}/warranties"
        ResponseContractWarranties postResponse = client.toBlocking()
                .retrieve(HttpRequest.POST(url, mapper.writeValueAsString(new ContractWarranties().data(List.of(newWarranties))))
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponseContractWarranties)

        then:
        postResponse.getData() != null

        when:// we can put warranties
        ContractWarrantiesData editedWarranties = TestRequestDataFactory.createWarranties()
        ResponseContractWarranties putResponse = client.toBlocking()
                .retrieve(HttpRequest.PUT(url, mapper.writeValueAsString(new ContractWarranties().data(List.of(editedWarranties))))
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponseContractWarranties)

        then:
        putResponse.getData() != null
        !putResponse.getData().isEmpty()
    }

    def "we can get and delete Contracts and delete Warranties"() {
        when:// we can get Contracts
        String getContractFullUrl = "${adminUrl}/loans/${postedAccountId}"
        ResponseContract getResponse = client.toBlocking()
                .retrieve(HttpRequest.GET(getContractFullUrl)
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponseContract)


        then:
        getResponse != null
        getResponse.getData() != null

        when:
        String deleteWarrantiesUrl = "${adminUrl}/loans/${postedAccountId}/warranties"

        HttpStatus deleteWarrantiesResponse = client.toBlocking()
                .retrieve(HttpRequest.DELETE(deleteWarrantiesUrl)
                        .header("Authorization", "Bearer ${adminToken}"),
                        HttpStatus)

        then:
        deleteWarrantiesResponse.getCode() == HttpStatus.NO_CONTENT.code

        when:
        String deleteContractUrl = "${adminUrl}/loans/${postedAccountId}"
        HttpStatus deleteContractResponse = client.toBlocking()
                .retrieve(HttpRequest.DELETE(deleteContractUrl)
                        .header("Authorization", "Bearer ${adminToken}"),
                        HttpStatus)

        then:
        deleteContractResponse.getCode() == HttpStatus.NO_CONTENT.code
    }

    def "we can get post put delete account-holder"() {
        given:
        def newAccountHolderDto = new CreateAccountHolderData()
                .accountHolderName("accountHolderName1")
                .documentIdentification("3333333333")
                .documentRel("RRR")

        when:// we can post accountHolder
        String url = "/admin/customers"
        client.toBlocking()
                .retrieve(HttpRequest.POST(url, mapper.writeValueAsString(new CreateAccountHolder().data(newAccountHolderDto)))
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponseAccountHolder)

        // post another one
        newAccountHolderDto.setAccountHolderName("AccountHolderName2")
        newAccountHolderDto.setDocumentIdentification("4444444444")
        ResponseAccountHolderData postResponse = client.toBlocking()
                .retrieve(HttpRequest.POST(url, mapper.writeValueAsString(new CreateAccountHolder().data(newAccountHolderDto)))
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponseAccountHolder).getData()
        def accountHolderId = postResponse.getAccountHolderId().toString()

        then:
        postResponse != null
        postResponse.getAccountHolderId() != null
        postResponse.getAccountHolderName() == newAccountHolderDto.getAccountHolderName()
        postResponse.getDocumentIdentification() == newAccountHolderDto.getDocumentIdentification()
        postResponse.getDocumentRel() == newAccountHolderDto.getDocumentRel()

        when:// we can get All accountHolder
        ResponseAccountHolderList getAllResponse = client.toBlocking()
                .retrieve(HttpRequest.GET(url)
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponseAccountHolderList)

        then:
        getAllResponse != null
        !getAllResponse.getData().isEmpty()

        when:// we can get accountHolder by ID
        ResponseAccountHolderData getResponse = client.toBlocking()
                .retrieve(HttpRequest.GET(url + "/${accountHolderId}")
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponseAccountHolder).getData()

        then:
        getResponse != null
        getResponse.getAccountHolderId().toString() == accountHolderId
        getResponse.getDocumentIdentification() != null
        getResponse.getDocumentRel() != null
        getResponse.getAccountHolderName() != null

        when:// we can put accountHolder by ID
        newAccountHolderDto.setAccountHolderName("put AccountHolderName3")
        newAccountHolderDto.setDocumentIdentification("5555555555")
        newAccountHolderDto.setDocumentRel("CCC")

        ResponseAccountHolderData putResponse = client.toBlocking()
                .retrieve(HttpRequest.PUT(url + "/${accountHolderId}", new CreateAccountHolder().data(newAccountHolderDto))
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponseAccountHolder).getData()

        then:
        putResponse != null
        putResponse.getAccountHolderName() == newAccountHolderDto.getAccountHolderName()
        putResponse.getDocumentRel() == newAccountHolderDto.getDocumentRel()
        putResponse.getDocumentIdentification() == newAccountHolderDto.getDocumentIdentification()


        when:// we can delete accountHolder by ID
        HttpStatus deleteResponse = client.toBlocking()
                .retrieve(HttpRequest.DELETE(url + "/${accountHolderId}")
                        .header("Authorization", "Bearer ${adminToken}"),
                        HttpStatus)

        then:
        deleteResponse.getCode() == HttpStatus.NO_CONTENT.code
    }

    def "we can post put delete personal identifications"() {
        given:
        CreatePersonalIdentification newPersonalIdentifications = TestRequestDataFactory.createPersonalIdentifications()
        String requestBody = mapper.writeValueAsString(newPersonalIdentifications)
        def url = "${adminUrl}/personal/identifications"

        when:// we can post personal identifications
        ResponsePersonalIdentificationData postResponse = client.toBlocking()
                .retrieve(HttpRequest.POST(url, requestBody)
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponsePersonalIdentification).getData()


        then:
        postResponse != null
        postResponse.getBrandName() == newPersonalIdentifications.getData().getBrandName()
        postResponse.getBirthDate() == newPersonalIdentifications.getData().getBirthDate()
        postResponse.getCivilName() == newPersonalIdentifications.getData().getCivilName()
        postResponse.getCpfNumber() == newPersonalIdentifications.getData().getCpfNumber()
        postResponse.getMaritalStatusAdditionalInfo() == newPersonalIdentifications.getData().getMaritalStatusAdditionalInfo()
        postResponse.getMaritalStatusCode() == newPersonalIdentifications.getData().getMaritalStatusCode()
        postResponse.getPassportCountry() == newPersonalIdentifications.getData().getPassportCountry()
        postResponse.getPassportExpirationDate() == newPersonalIdentifications.getData().getPassportExpirationDate()
        postResponse.getPassportIssueDate() == newPersonalIdentifications.getData().getPassportIssueDate()
        postResponse.getPassportNumber() == newPersonalIdentifications.getData().getPassportNumber()
        postResponse.getSex() == newPersonalIdentifications.getData().getSex()
        !postResponse.getCompanyCnpj().isEmpty()
        postResponse.getCompanyCnpj().containsAll(newPersonalIdentifications.getData().getCompanyCnpj())
        !postResponse.getOtherDocuments().isEmpty()
        postResponse.getOtherDocuments().containsAll(newPersonalIdentifications.getData().getOtherDocuments())
        !postResponse.getNationality().isEmpty()
        postResponse.getNationality().containsAll(newPersonalIdentifications.getData().getNationality())
        !postResponse.getNationality().first().getDocuments().isEmpty()
        postResponse.getNationality().first().getDocuments() == newPersonalIdentifications.getData().getNationality().first().getDocuments()
        !postResponse.getFiliation().isEmpty()
        postResponse.getFiliation().containsAll(newPersonalIdentifications.getData().getFiliation())
        postResponse.getContacts() != null
        !postResponse.getContacts().getPostalAddresses().isEmpty()
        postResponse.getContacts().getPostalAddresses().containsAll(newPersonalIdentifications.getData().getContacts().getPostalAddresses())
        !postResponse.getContacts().getEmails().isEmpty()
        postResponse.getContacts().getEmails().containsAll(newPersonalIdentifications.getData().getContacts().getEmails())
        !postResponse.getContacts().getPhones().isEmpty()
        postResponse.getContacts().getPhones().containsAll(newPersonalIdentifications.getData().getContacts().getPhones())


        when:// we can put personal identifications
        newPersonalIdentifications
        def putBody = TestRequestDataFactory
                .editPersonalIdentifications()
        putBody.getData().setBrandName("new Brand Name")
        ResponsePersonalIdentificationData putResponse = client.toBlocking()
                .retrieve(HttpRequest.PUT("${url}/${postResponse.getPersonalId()}", putBody)
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponsePersonalIdentification).getData()

        then:
        putResponse != null
        putResponse.getBrandName() == putBody.getData().getBrandName()

        when:// we can delete personal identifications
        HttpStatus deleteResponse = client.toBlocking()
                .retrieve(HttpRequest.DELETE("${url}/${postResponse.getPersonalId()}")
                        .header("Authorization", "Bearer ${adminToken}"),
                        HttpStatus)

        then:
        deleteResponse.getCode() == HttpStatus.NO_CONTENT.code
    }

    def "we can post put delete personal financial-relations"() {
        given:
        PersonalFinancialRelations newPersonalFinancialRelations = TestRequestDataFactory.createPersonalFinancialRelations()
        String requestBody = mapper.writeValueAsString(newPersonalFinancialRelations)

        when:// we can post personal financial-relations
        String url = "${adminUrl}/personal/financial-relations"
        PersonalFinancialRelationsData postResponse = client.toBlocking()
                .retrieve(HttpRequest.POST(url, requestBody)
                        .header("Authorization", "Bearer ${adminToken}"),
                        PersonalFinancialRelations).getData()

        then:
        postResponse.getAccountHolderId() != null
        postResponse.getAccountHolderId().toString() == accountHolderId
        postResponse.getStartDate() != null
        !postResponse.getProductsServicesType().isEmpty()
        postResponse.getProductsServicesType().containsAll(newPersonalFinancialRelations.getData().getProductsServicesType())
        !postResponse.getProcurators().isEmpty()
        postResponse.getProcurators().containsAll(newPersonalFinancialRelations.getData().getProcurators())
        postResponse.getProductsServicesTypeAdditionalInfo() == newPersonalFinancialRelations.getData().getProductsServicesTypeAdditionalInfo()


        when:// we can put personal financial-relations
        newPersonalFinancialRelations.getData().setProductsServicesType(List.of(EnumProductServiceType.CONTA_DEPOSITO_A_VISTA, EnumProductServiceType.INVESTIMENTO))
        newPersonalFinancialRelations.getData().setProductsServicesTypeAdditionalInfo("new Additional Info")
        PersonalFinancialRelationsData putResponse = client.toBlocking()
                .retrieve(HttpRequest.PUT(url, mapper.writeValueAsString(newPersonalFinancialRelations))
                        .header("Authorization", "Bearer ${adminToken}"),
                        PersonalFinancialRelations).getData()

        then:
        putResponse.getProductsServicesTypeAdditionalInfo() == newPersonalFinancialRelations.getData().getProductsServicesTypeAdditionalInfo()
        putResponse.getProductsServicesType() == newPersonalFinancialRelations.getData().getProductsServicesType()

        when:// we can delete personal financial-relations
        HttpStatus deleteResponse = client.toBlocking()
                .retrieve(HttpRequest.DELETE(url)
                        .header("Authorization", "Bearer ${adminToken}"),
                        HttpStatus)

        then:
        deleteResponse.getCode() == HttpStatus.NO_CONTENT.code
    }

    def "we can post put delete personal qualifications"() {
        given:
        PersonalQualifications newPersonalQualifications = TestRequestDataFactory.createPersonalQualifications()
        String requestBody = mapper.writeValueAsString(newPersonalQualifications)

        when:// we can post personal qualifications
        String url = "${adminUrl}/personal/qualifications"
        PersonalQualificationsData postResponse = client.toBlocking()
                .retrieve(HttpRequest.POST(url, requestBody)
                        .header("Authorization", "Bearer ${adminToken}"),
                        PersonalQualifications).getData()

        then:
        postResponse != null
        postResponse.getAccountHolderId() != null
        postResponse.getCompanyCnpj() == newPersonalQualifications.getData().getCompanyCnpj()
        postResponse.getOccupationCode() == newPersonalQualifications.getData().getOccupationCode()
        postResponse.getOccupationDescription() == newPersonalQualifications.getData().getOccupationDescription()
        postResponse.getInformedIncomeAmount() == newPersonalQualifications.getData().getInformedIncomeAmount()
        postResponse.getInformedIncomeCurrency() == newPersonalQualifications.getData().getInformedIncomeCurrency()
        postResponse.getInformedIncomeDate() == newPersonalQualifications.getData().getInformedIncomeDate()
        postResponse.getInformedIncomeFrequency() == newPersonalQualifications.getData().getInformedIncomeFrequency()
        postResponse.getInformedPatrimonyAmount() == newPersonalQualifications.getData().getInformedPatrimonyAmount()
        postResponse.getInformedPatrimonyCurrency() == newPersonalQualifications.getData().getInformedPatrimonyCurrency()
        postResponse.getInformedPatrimonyYear() == newPersonalQualifications.getData().getInformedPatrimonyYear()

        when:// we can put personal qualifications
        newPersonalQualifications.getData().setOccupationDescription("new Occupation Description")
        PersonalQualificationsData putResponse = client.toBlocking()
                .retrieve(HttpRequest.PUT(url, mapper.writeValueAsString(newPersonalQualifications))
                        .header("Authorization", "Bearer ${adminToken}"),
                        PersonalQualifications).getData()

        then:
        putResponse != null
        putResponse.getOccupationDescription() == newPersonalQualifications.getData().getOccupationDescription()

        when:// we can delete personal qualifications
        HttpStatus deleteResponse = client.toBlocking()
                .retrieve(HttpRequest.DELETE(url)
                        .header("Authorization", "Bearer ${adminToken}"),
                        HttpStatus)

        then:
        deleteResponse.getCode() == HttpStatus.NO_CONTENT.code
    }

    def "we can post put delete business identifications"() {
        given:
        CreateBusinessIdentification newBusinessIdentifications = TestRequestDataFactory.createBusinessIdentifications()
        String requestBody = mapper.writeValueAsString(newBusinessIdentifications)
        String url = "${adminUrl}/business/identifications"

        when:// we can post business identifications
        ResponseBusinessIdentificationData postResponse = client.toBlocking()
                .retrieve(HttpRequest.POST(url, requestBody)
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponseBusinessIdentification).getData()

        then:
        postResponse != null
        postResponse.getBusinessIdentificationsId() != null
        postResponse.getBrandName() == newBusinessIdentifications.getData().getBrandName()
        postResponse.getCompanyName() == newBusinessIdentifications.getData().getCompanyName()
        postResponse.getTradeName() == newBusinessIdentifications.getData().getTradeName()
        postResponse.getIncorporationDate() == newBusinessIdentifications.getData().getIncorporationDate()
        postResponse.getCnpjNumber() == newBusinessIdentifications.getData().getCnpjNumber()
        !postResponse.getCompanyCnpjNumber().isEmpty()
        postResponse.getCompanyCnpjNumber().containsAll(newBusinessIdentifications.getData().getCompanyCnpjNumber())
        !postResponse.getOtherDocuments().isEmpty()
        postResponse.getOtherDocuments().containsAll(newBusinessIdentifications.getData().getOtherDocuments())
        !postResponse.getParties().isEmpty()
        postResponse.getParties().containsAll(newBusinessIdentifications.getData().getParties())
        postResponse.getContacts() != null
        !postResponse.getContacts().getPostalAddresses().isEmpty()
        postResponse.getContacts().getPostalAddresses().containsAll(newBusinessIdentifications.getData().getContacts().getPostalAddresses())
        !postResponse.getContacts().getEmails().isEmpty()
        postResponse.getContacts().getEmails().containsAll(newBusinessIdentifications.getData().getContacts().getEmails())
        !postResponse.getContacts().getPhones().isEmpty()
        postResponse.getContacts().getPhones().containsAll(newBusinessIdentifications.getData().getContacts().getPhones())

        when:// we can put business identifications
        def putBody = TestRequestDataFactory.editBusinessIdentifications()
        putBody.setBrandName("new Brand Name")
        ResponseBusinessIdentificationData putResponse = client.toBlocking()
                .retrieve(HttpRequest.PUT(url + "/${postResponse.getBusinessIdentificationsId().toString()}",
                        new EditedBusinessIdentification().data(putBody))
                        .header("Authorization", "Bearer ${adminToken}"),
                        ResponseBusinessIdentification).getData()

        then:
        putResponse != null
        putResponse.getBrandName() == putBody.getBrandName()

        when:// we can delete business identifications
        HttpStatus deleteResponse = client.toBlocking()
                .retrieve(HttpRequest.DELETE(url + "/${postResponse.getBusinessIdentificationsId().toString()}")
                        .header("Authorization", "Bearer ${adminToken}"),
                        HttpStatus)

        then:
        deleteResponse.getCode() == HttpStatus.NO_CONTENT.code
    }

    def "we can post put delete business financial-relations"() {
        given:
        BusinessFinancialRelations newBusinessFinancialRelations = TestRequestDataFactory.createBusinessFinancialRelations()

        when:// we can post business financial-relations
        String url = "${adminUrl}/business/financial-relations"
        BusinessFinancialRelations postResponse = client.toBlocking()
                .retrieve(HttpRequest.POST(url, mapper.writeValueAsString(newBusinessFinancialRelations))
                        .header("Authorization", "Bearer ${adminToken}"),
                        BusinessFinancialRelations)

        then:
        postResponse.getData() != null
        postResponse.getData().getAccountHolderId() != null
        postResponse.getData().getStartDate() != null
        !postResponse.getData().getProductsServicesType().isEmpty()
        postResponse.getData().getProductsServicesType().containsAll(newBusinessFinancialRelations.getData().getProductsServicesType())
        !postResponse.getData().getProcurators().isEmpty()
        postResponse.getData().getProcurators().containsAll(newBusinessFinancialRelations.getData().getProcurators())

        when:// we can put business financial-relations
        newBusinessFinancialRelations.getData().setProductsServicesType(List.of(EnumProductServiceType.INVESTIMENTO))
        BusinessFinancialRelations putResponse = client.toBlocking()
                .retrieve(HttpRequest.PUT(url, mapper.writeValueAsString(newBusinessFinancialRelations))
                        .header("Authorization", "Bearer ${adminToken}"),
                        BusinessFinancialRelations)

        then:
        putResponse.getData() != null
        putResponse.getData().getProductsServicesType() == newBusinessFinancialRelations.getData().getProductsServicesType()

        when:// we can delete business financial-relations
        HttpStatus deleteResponse = client.toBlocking()
                .retrieve(HttpRequest.DELETE(url)
                        .header("Authorization", "Bearer ${adminToken}"),
                        HttpStatus)

        then:
        deleteResponse.getCode() == HttpStatus.NO_CONTENT.code
    }

    def "we can post put delete business qualifications"() {
        given:
        BusinessQualifications newBusinessQualifications = TestRequestDataFactory.createBusinessQualifications()

        when:// we can post business qualifications
        String url = "${adminUrl}/business/qualifications"
        BusinessQualifications postResponse = client.toBlocking()
                .retrieve(HttpRequest.POST(url, mapper.writeValueAsString(newBusinessQualifications))
                        .header("Authorization", "Bearer ${adminToken}"),
                        BusinessQualifications)

        then:
        postResponse.getData() != null
        postResponse.getData().getAccountHolderId() != null
        postResponse.getData().getInformedRevenueFrequency() == newBusinessQualifications.getData().getInformedRevenueFrequency()
        postResponse.getData().getInformedRevenueFrequencyAdditionalInfo() == newBusinessQualifications.getData().getInformedRevenueFrequencyAdditionalInfo()
        postResponse.getData().getInformedRevenueAmount() == newBusinessQualifications.getData().getInformedRevenueAmount()
        postResponse.getData().getInformedRevenueCurrency() == newBusinessQualifications.getData().getInformedRevenueCurrency()
        postResponse.getData().getInformedRevenueYear() == newBusinessQualifications.getData().getInformedRevenueYear()
        postResponse.getData().getInformedPatrimonyAmount() == newBusinessQualifications.getData().getInformedPatrimonyAmount()
        postResponse.getData().getInformedPatrimonyCurrency() == newBusinessQualifications.getData().getInformedPatrimonyCurrency()
        postResponse.getData().getInformedPatrimonyDate() == newBusinessQualifications.getData().getInformedPatrimonyDate()
        !postResponse.getData().getEconomicActivities().isEmpty()
        postResponse.getData().getEconomicActivities().containsAll(newBusinessQualifications.getData().getEconomicActivities())


        when:// we can put business qualifications
        newBusinessQualifications.getData().setInformedRevenueAmount(22222)
        BusinessQualifications putResponse = client.toBlocking()
                .retrieve(HttpRequest.PUT(url, mapper.writeValueAsString(newBusinessQualifications))
                        .header("Authorization", "Bearer ${adminToken}"),
                        BusinessQualifications)

        then:
        putResponse.getData() != null
        putResponse.getData().getInformedRevenueAmount() == newBusinessQualifications.getData().getInformedRevenueAmount()

        when:// we can delete business qualifications
        HttpStatus deleteResponse = client.toBlocking()
                .retrieve(HttpRequest.DELETE(url)
                        .header("Authorization", "Bearer ${adminToken}"),
                        HttpStatus)

        then:
        deleteResponse.getCode() == HttpStatus.NO_CONTENT.code
    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }
}
