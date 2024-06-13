package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.TestEntityDataFactory
import com.raidiam.trustframework.bank.domain.AccountEntity
import com.raidiam.trustframework.bank.domain.AccountHolderEntity
import com.raidiam.trustframework.bank.domain.ContractEntity
import com.raidiam.trustframework.bank.domain.CreditCardAccountsEntity
import com.raidiam.trustframework.bank.domain.ExchangesOperationEntity
import com.raidiam.trustframework.bank.enums.ResourceType
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Stepwise

import javax.inject.Inject

import static com.raidiam.trustframework.bank.TestEntityDataFactory.*

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class UserServiceSpec extends CleanupSpecification {

    @Inject
    UserService userService

    @Inject
    TestEntityDataFactory testEntityDataFactory

    @Shared
    AccountHolderEntity testAccountHolder

    @Shared
    AccountEntity testAccount

    @Shared
    CreditCardAccountsEntity testCreditCard

    @Shared
    ContractEntity testLoan

    @Shared
    ContractEntity testFinancing

    @Shared
    ContractEntity testInvoiceFinancing

    @Shared
    ContractEntity testUnarrangedOverdraft

    @Shared
    ExchangesOperationEntity testExchangesOperation

    def setup() {
        if (runSetup) {
            testAccountHolder = accountHolderRepository.save(anAccountHolder("10117409073", "CPF"))
            testAccount = accountRepository.save(anAccount(testAccountHolder))
            testCreditCard = creditCardAccountsRepository.save(anCreditCardAccounts(testAccount.getAccountHolderId()))
            testLoan = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(),  ResourceType.LOAN, "EMPRESTIMOS",  "CONTA_GARANTIDA")
            testFinancing = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(),  ResourceType.FINANCING, "FINANCIAMENTOS",  "CONTA_GARANTIDA")
            testInvoiceFinancing = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(),  ResourceType.INVOICE_FINANCING, "FINANCIAMENTOS DE FATURAS",  "CONTA_GARANTIDA")
            testUnarrangedOverdraft = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(),  ResourceType.UNARRANGED_ACCOUNT_OVERDRAFT, "DESCONTO NÃO ACORDADO",  "CONTA_GARANTIDA")
            testExchangesOperation = testEntityDataFactory.createAndSaveExchangeOperation(testAccountHolder.getAccountHolderId())
            runSetup = false
        }
    }

    def "we can get user accounts" () {
        when:
        def accounts = userService.getAccounts(testAccountHolder.getUserId())
        then:
        accounts != null
        accounts.getData() != null
        accounts.getData().size() == 1
        accounts.getData().first().getAccountId() == testAccount.getAccountId().toString()
    }

    def "we can get credit card accounts" () {
        when:
        def accounts = userService.getCreditCardAccounts(testAccountHolder.getUserId())
        then:
        accounts != null
        accounts.getData() != null
        accounts.getData().size() == 1
        accounts.getData().first().getCreditCardAccountId() == testCreditCard.getCreditCardAccountId().toString()
    }

    def "we can get loans" () {
        when:
        def accounts = userService.getContractList(testAccountHolder.getUserId(), ResourceType.LOAN)
        then:
        accounts != null
        accounts.getData() != null
        accounts.getData().size() == 1
        accounts.getData().first().getAccountId() == testLoan.getContractId().toString()
    }

    def "we can get financings" () {
        when:
        def accounts = userService.getContractList(testAccountHolder.getUserId(), ResourceType.FINANCING)
        then:
        accounts != null
        accounts.getData() != null
        accounts.getData().size() == 1
        accounts.getData().first().getAccountId() == testFinancing.getContractId().toString()
    }

    def "we can get invoice financings" () {
        when:
        def accounts = userService.getContractList(testAccountHolder.getUserId(), ResourceType.INVOICE_FINANCING)
        then:
        accounts != null
        accounts.getData() != null
        accounts.getData().size() == 1
        accounts.getData().first().getAccountId() == testInvoiceFinancing.getContractId().toString()
    }

    def "we can get unarranged overdrafts" () {
        when:
        def accounts = userService.getContractList(testAccountHolder.getUserId(), ResourceType.UNARRANGED_ACCOUNT_OVERDRAFT)
        then:
        accounts != null
        accounts.getData() != null
        accounts.getData().size() == 1
        accounts.getData().first().getAccountId() == testUnarrangedOverdraft.getContractId().toString()
    }

    def "we can get exchanges operations" () {
        when:
        def exchanges = userService.getExchangesList(testAccountHolder.getUserId())
        then:
        exchanges != null
        exchanges.getData() != null
        exchanges.getData().size() == 1
        exchanges.getData().first().getAccountId() == testExchangesOperation.getOperationId().toString()
    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }
}
