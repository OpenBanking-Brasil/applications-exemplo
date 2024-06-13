package com.raidiam.trustframework.bank.services.admin

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.TestRequestDataFactory
import com.raidiam.trustframework.bank.domain.AccountEntity
import com.raidiam.trustframework.bank.domain.AccountHolderEntity
import com.raidiam.trustframework.bank.domain.AccountTransactionsEntity
import com.raidiam.trustframework.bank.services.AccountsService
import com.raidiam.trustframework.mockbank.models.generated.CreateAccountData
import com.raidiam.trustframework.mockbank.models.generated.CreateAccountTransactionData
import com.raidiam.trustframework.mockbank.models.generated.ResponseAccountTransaction
import com.raidiam.trustframework.mockbank.models.generated.ResponseAccountTransactionData
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Stepwise

import javax.inject.Inject

import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccountHolder

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class AccountAdminServiceSpec extends CleanupSpecification {

    @Inject
    AccountsService accountsService
    @Shared
    CreateAccountData testAccount
    @Shared
    CreateAccountTransactionData testAccountTransactions
    @Shared
    AccountHolderEntity testAccountHolder

    def setup() {
        if (runSetup) {
            testAccountHolder = accountHolderRepository.save(anAccountHolder("10117409073", "CPF"))
            testAccount = TestRequestDataFactory.createAccount().getData()
            testAccountTransactions = TestRequestDataFactory.createAccountTransaction().getData()

            runSetup = false
        }
    }

    def "We can add update delete Account"() {
        when:
        def account = accountsService.addAccount(testAccount, testAccountHolder.getAccountHolderId().toString())

        then:
        account != null
        account.getData().getAccountId() != null
        account.getData().getAccountType() == testAccount.getAccountType()

        when:
        var forUpdate = TestRequestDataFactory.editedAccountDto()
        def updatedAccount = accountsService.updateAccount(account.getData().getAccountId(), forUpdate).getData()

        then:
        updatedAccount.getAccountId() != null
        updatedAccount.getAccountType() == forUpdate.getAccountType()
        updatedAccount.getAccountSubType() == forUpdate.getAccountSubType()
        updatedAccount.getNumber() == forUpdate.getNumber()
        updatedAccount.getBrandName() == forUpdate.getBrandName()
        updatedAccount.getBranchCode() == forUpdate.getBranchCode()
        updatedAccount.getCompeCode() == forUpdate.getCompeCode()
        updatedAccount.getCompanyCnpj() == forUpdate.getCompanyCnpj()
        updatedAccount.getCheckDigit() == forUpdate.getCheckDigit()
        updatedAccount.getCurrency() == forUpdate.getCurrency()
        updatedAccount.getStatus() == forUpdate.getStatus()
        //Limits
        updatedAccount.getOverdraftContractedLimit() == forUpdate.getOverdraftContractedLimit()
        updatedAccount.getOverdraftContractedLimitCurrency() == forUpdate.getOverdraftContractedLimitCurrency()
        updatedAccount.getOverdraftUsedLimit() == forUpdate.getOverdraftUsedLimit()
        updatedAccount.getOverdraftUsedLimitCurrency() == forUpdate.getOverdraftUsedLimitCurrency()
        updatedAccount.getUnarrangedOverdraftAmount() == forUpdate.getUnarrangedOverdraftAmount()
        updatedAccount.getUnarrangedOverdraftAmountCurrency() == forUpdate.getUnarrangedOverdraftAmountCurrency()

        when:
        accountsService.deleteAccount(updatedAccount.getAccountId())
        Optional<AccountEntity> empty = accountRepository.findByAccountId(UUID.fromString(updatedAccount.getAccountId()))

        then:
        empty.isEmpty()
    }

    def "We can add update delete Account Transaction"() {
        when:
        def account = accountsService.addAccount(testAccount, testAccountHolder.getAccountHolderId().toString()).getData()
        ResponseAccountTransaction transactionDto = accountsService.addTransaction(account.getAccountId().toString(), testAccountTransactions)

        then:
        transactionDto != null
        transactionDto.getData().getTransactionId() != null

        when:
        var forUpdate = TestRequestDataFactory.createAccountTransaction().getData()
        ResponseAccountTransactionData updatedTransactionDto = accountsService.updateAccountTransaction(account.getAccountId().toString(),
                transactionDto.getData().getTransactionId(), forUpdate).getData()

        then:
        updatedTransactionDto.getTransactionId() != null
        updatedTransactionDto.getTransactionName() == forUpdate.getTransactionName()
        updatedTransactionDto.getAmount() == forUpdate.getAmount()
        updatedTransactionDto.getCompletedAuthorisedPaymentType() == forUpdate.getCompletedAuthorisedPaymentType()
        updatedTransactionDto.getCreditDebitType() == forUpdate.getCreditDebitType()
        updatedTransactionDto.getPartieBranchCode() == forUpdate.getPartieBranchCode()
        updatedTransactionDto.getPartieCheckDigit() == forUpdate.getPartieCheckDigit()
        updatedTransactionDto.getPartieCnpjCpf() == forUpdate.getPartieCnpjCpf()
        updatedTransactionDto.getPartieNumber() == forUpdate.getPartieNumber()
        updatedTransactionDto.getPartieCompeCode() == forUpdate.getPartieCompeCode()
        updatedTransactionDto.getPartiePersonType() == forUpdate.getPartiePersonType()
        updatedTransactionDto.getTransactionCurrency() == forUpdate.getTransactionCurrency()
        updatedTransactionDto.getType() == forUpdate.getType()

        when:
        accountsService.deleteTransaction(account.getAccountId(), updatedTransactionDto.getTransactionId())
        Page<AccountTransactionsEntity> empty = accountTransactionsRepository
                .findByAccountIdOrderByCreatedAtAsc(UUID.fromString(account.getAccountId()),  Pageable.from(0))

        then:
        empty.isEmpty()
    }
}
