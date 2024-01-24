package com.raidiam.trustframework.bank.services.admin

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.TestRequestDataFactory
import com.raidiam.trustframework.bank.domain.AccountHolderEntity
import com.raidiam.trustframework.bank.domain.CreditCardAccountsEntity

import com.raidiam.trustframework.bank.services.CreditCardAccountsService
import com.raidiam.trustframework.bank.utils.BankLambdaUtils
import com.raidiam.trustframework.mockbank.models.generated.CreateCreditCardAccountBill
import com.raidiam.trustframework.mockbank.models.generated.CreateCreditCardAccountData
import com.raidiam.trustframework.mockbank.models.generated.CreateCreditCardAccountLimits
import com.raidiam.trustframework.mockbank.models.generated.CreateCreditCardAccountTransactionList
import com.raidiam.trustframework.mockbank.models.generated.ResponseCreditCardAccountBillData
import com.raidiam.trustframework.mockbank.models.generated.ResponseCreditCardAccountLimits
import com.raidiam.trustframework.mockbank.models.generated.ResponseCreditCardAccountTransactionData
import com.raidiam.trustframework.mockbank.models.generated.ResponseCreditCardAccountTransactionList
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Stepwise

import javax.inject.Inject

import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccountHolder

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class CreditCardAccountAdminServiceSpec extends CleanupSpecification {

    @Inject
    CreditCardAccountsService cardAccountsService
    @Shared
    CreateCreditCardAccountData testAccount
    @Shared
    CreateCreditCardAccountTransactionList testCreditCardAccountTransactions
    @Shared
    CreateCreditCardAccountLimits testCreditCardAccountLimit
    @Shared
    CreateCreditCardAccountBill testCreditCardBill
    @Shared
    AccountHolderEntity testAccountHolder

    def setup() {
        if (runSetup) {
            testAccountHolder = accountHolderRepository.save(anAccountHolder("10117409073", "CPF"))
            testAccount = TestRequestDataFactory.creditCardAccount().getData()
            testCreditCardAccountTransactions = new CreateCreditCardAccountTransactionList().data(List.of(TestRequestDataFactory.cardAccountTransactionDto()))
            testCreditCardAccountLimit = TestRequestDataFactory.creditCardAccountLimitDto()
            testCreditCardBill = TestRequestDataFactory.creditCardBillDto()
            runSetup = false
        }
    }

    def "We can add update delete Account"() {
        when:
        def account = cardAccountsService.addCreditCardAccount(testAccount, testAccountHolder.getAccountHolderId().toString())

        then:
        account != null
        account.getData().getCreditCardAccountId() != null
        !account.getData().getPaymentMethod().isEmpty()

        when:
        var forUpdate = TestRequestDataFactory.editCardAccountDto()
        def updateAccount = cardAccountsService.updateCreditCardAccount(account.getData().getCreditCardAccountId().toString(), forUpdate)

        then:
        updateAccount.getData().getCreditCardAccountId() != null
        updateAccount.getData().getBrandName() == forUpdate.getData().getBrandName()
        updateAccount.getData().getCompanyCnpj() == forUpdate.getData().getCompanyCnpj()
        updateAccount.getData().getName() == forUpdate.getData().getName()
        updateAccount.getData().getProductType() == forUpdate.getData().getProductType()
        updateAccount.getData().getProductAdditionalInfo() == forUpdate.getData().getProductAdditionalInfo()
        updateAccount.getData().getCreditCardNetwork() == forUpdate.getData().getCreditCardNetwork()
        updateAccount.getData().getNetworkAdditionalInfo() == forUpdate.getData().getNetworkAdditionalInfo()
        updateAccount.getData().getStatus() == forUpdate.getData().getStatus()
        updateAccount.getData().getPaymentMethod() != null
        updateAccount.getData().getPaymentMethod().size() == forUpdate.getData().getPaymentMethod().size()
        updateAccount.getData().getPaymentMethod().containsAll(forUpdate.getData().getPaymentMethod())


        when:
        cardAccountsService.deleteCreditCardAccount(account.getData().getCreditCardAccountId().toString())
        Optional<CreditCardAccountsEntity> empty = creditCardAccountsRepository.findByCreditCardAccountId(UUID.fromString(account.getData().getCreditCardAccountId().toString()))

        then:
        empty.isEmpty()
    }

    def "we can add update delete Credit Card Account Limit"() {
        when://we can add update limits
        def account = cardAccountsService.addCreditCardAccount(testAccount, testAccountHolder.getAccountHolderId().toString())
        ResponseCreditCardAccountLimits accountLimitDto = cardAccountsService.addCreditCardAccountLimit(account.getData().getCreditCardAccountId().toString(), testCreditCardAccountLimit)

        then:
        !accountLimitDto.getData().isEmpty()
        accountLimitDto.getData().first() == testCreditCardAccountLimit.getData().first()

        when://we can update limits
        var forUpdate = TestRequestDataFactory.creditCardAccountLimitDto()
        ResponseCreditCardAccountLimits updatedLimitDto = cardAccountsService.updateCreditCardAccountLimit(account.getData().getCreditCardAccountId().toString(),
                forUpdate)

        then:
        !updatedLimitDto.getData().isEmpty()
        updatedLimitDto.getData().containsAll(forUpdate.getData())

        when://we can delete limits
        cardAccountsService.deleteCreditCardAccountLimit(account.getData().getCreditCardAccountId().toString())

        then:
        noExceptionThrown()
        creditCardAccountsLimitsRepository
                .findByCreditCardAccountId(UUID.fromString(account.getData().getCreditCardAccountId().toString())).isEmpty()
    }

    def "we can add update delete Credit Card Account Bills and Transactions"() {
        when://add Bills
        def account = cardAccountsService.addCreditCardAccount(testAccount, testAccountHolder.getAccountHolderId().toString()).getData()
        ResponseCreditCardAccountBillData newCardBillDto = cardAccountsService.addCreditCardBill(account.getCreditCardAccountId().toString(), testCreditCardBill).getData()

        then:
        newCardBillDto != null
        newCardBillDto.getBillId() != null
        newCardBillDto.getPayments() != null
        newCardBillDto.getFinanceCharges() != null

        when://update Bills
        var forUpdate = TestRequestDataFactory.creditCardBillDto()
        ResponseCreditCardAccountBillData updatedBillDto = cardAccountsService.updateCreditCardBill(account.getCreditCardAccountId().toString(),
                newCardBillDto.getBillId().toString(), forUpdate).getData()

        then:
        updatedBillDto.getBillId() != null
        updatedBillDto.getDueDate() == forUpdate.getData().getDueDate()
        updatedBillDto.getBillTotalAmount() == forUpdate.getData().getBillTotalAmount()
        updatedBillDto.getBillTotalAmountCurrency() == forUpdate.getData().getBillTotalAmountCurrency()
        updatedBillDto.getBillMinimumAmount() == forUpdate.getData().getBillMinimumAmount()
        updatedBillDto.getBillMinimumAmountCurrency() == forUpdate.getData().getBillMinimumAmountCurrency()
        updatedBillDto.isInstalment() == forUpdate.getData().isInstalment()
        updatedBillDto.getFinanceCharges() != null
        updatedBillDto.getFinanceCharges().containsAll(forUpdate.getData().getFinanceCharges())
        updatedBillDto.getPayments() != null
        updatedBillDto.getPayments().containsAll(forUpdate.getData().getPayments())


        when: //add Transactions
        ResponseCreditCardAccountTransactionList newTransactionDto = cardAccountsService
                .addCreditCardTransaction(account.getCreditCardAccountId().toString(),
                        newCardBillDto.getBillId().toString(), testCreditCardAccountTransactions)

        then:
        newTransactionDto != null
        newTransactionDto.getData().size() == 1

        when://update Transactions
        var forUpdateTransactions = TestRequestDataFactory.editAccountTransactionDto()
        ResponseCreditCardAccountTransactionData updatedTransactionDto = cardAccountsService
                .updateCreditCardTransaction(account.getCreditCardAccountId().toString(),
                        newCardBillDto.getBillId().toString(), newTransactionDto.getData().first().getTransactionId().toString(), forUpdateTransactions).getData()

        then:
        updatedTransactionDto.transactionId != null
        updatedTransactionDto.identificationNumber == forUpdateTransactions.getData().identificationNumber
        updatedTransactionDto.lineName == forUpdateTransactions.getData().lineName
        updatedTransactionDto.transactionName == forUpdateTransactions.getData().transactionName
        updatedTransactionDto.creditDebitType == forUpdateTransactions.getData().creditDebitType
        updatedTransactionDto.transactionType == forUpdateTransactions.getData().transactionType
        updatedTransactionDto.transactionalAdditionalInfo == forUpdateTransactions.getData().transactionalAdditionalInfo
        updatedTransactionDto.paymentType == forUpdateTransactions.getData().paymentType
        updatedTransactionDto.feeType == forUpdateTransactions.getData().feeType
        updatedTransactionDto.feeTypeAdditionalInfo == forUpdateTransactions.getData().feeTypeAdditionalInfo
        updatedTransactionDto.otherCreditsType == forUpdateTransactions.getData().otherCreditsType
        updatedTransactionDto.otherCreditsAdditionalInfo == forUpdateTransactions.getData().otherCreditsAdditionalInfo
        updatedTransactionDto.chargeIdentificator == forUpdateTransactions.getData().chargeIdentificator
        updatedTransactionDto.brazilianAmount == forUpdateTransactions.getData().brazilianAmount
        updatedTransactionDto.chargeNumber == forUpdateTransactions.getData().chargeNumber
        updatedTransactionDto.amount == forUpdateTransactions.getData().amount
        updatedTransactionDto.currency == forUpdateTransactions.getData().currency
        updatedTransactionDto.transactionDate == forUpdateTransactions.getData().transactionDate
        updatedTransactionDto.billPostDate == forUpdateTransactions.getData().billPostDate
        updatedTransactionDto.payeeMCC == forUpdateTransactions.getData().payeeMCC

        when://DELETE Transactions, Bills
        cardAccountsService.deleteCreditCardTransaction(account.getCreditCardAccountId().toString(), newCardBillDto.getBillId().toString(), newTransactionDto.getData().first().getTransactionId().toString())
        cardAccountsService.deleteCreditCardBill(account.getCreditCardAccountId().toString(), newCardBillDto.getBillId().toString())

        then:
        noExceptionThrown()

        when:
        BankLambdaUtils.getCreditCardAccountsTransaction(newTransactionDto.getData().first().getTransactionId().toString(), creditCardAccountsTransactionRepository)

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.NOT_FOUND

        when:
        BankLambdaUtils.getCreditCardAccountBill(newCardBillDto.getBillId().toString(), creditCardAccountsBillsRepository)

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.NOT_FOUND
    }
}
