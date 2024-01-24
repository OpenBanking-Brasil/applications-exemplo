package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.domain.*
import com.raidiam.trustframework.mockbank.models.generated.CreateConsentData
import com.raidiam.trustframework.mockbank.models.generated.EnumPartiePersonType
import com.raidiam.trustframework.mockbank.models.generated.UpdateConsentData
import io.micronaut.data.model.Pageable
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Stepwise

import javax.inject.Inject
import java.time.LocalDate

import static com.raidiam.trustframework.bank.TestEntityDataFactory.*

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class AccountServiceSpec extends CleanupSpecification {

    @Inject
    AccountsService accountsService
    @Shared
    ConsentEntity testConsent
    @Shared
    AccountEntity testAccount
    @Shared
    AccountTransactionsEntity testAccountTransactions
    @Shared
    AccountHolderEntity testAccountHolder

    def setup() {
        if (runSetup) {
            testAccountHolder = accountHolderRepository.save(anAccountHolder("10117409073", "CPF"))
            testAccount = accountRepository.save(anAccount(testAccountHolder.getAccountHolderId()))
            testConsent = consentRepository.save(aConsent(testAccountHolder.getAccountHolderId()))
            consentPermissionsRepository.save(aConsentPermission(CreateConsentData.PermissionsEnum.ACCOUNTS_READ, testConsent.getConsentId()))
            consentPermissionsRepository.save(aConsentPermission(CreateConsentData.PermissionsEnum.ACCOUNTS_BALANCES_READ, testConsent.getConsentId()))
            consentPermissionsRepository.save(aConsentPermission(CreateConsentData.PermissionsEnum.ACCOUNTS_TRANSACTIONS_READ, testConsent.getConsentId()))
            consentPermissionsRepository.save(aConsentPermission(CreateConsentData.PermissionsEnum.ACCOUNTS_OVERDRAFT_LIMITS_READ, testConsent.getConsentId()))
            consentAccountRepository.save(new ConsentAccountEntity(testConsent, testAccount))
            testAccountTransactions = accountTransactionsRepository.save(aTransaction(testAccount.getAccountId()))

            runSetup = false
        }
    }

    def "We can get all accounts provided in Consent"() {
        when:
        def accountIdentifications = accountsService.getAccounts(Pageable.from(0), testConsent.getConsentId(), null)

        then:
        !accountIdentifications.getData().empty
        //provided only 1 Account in Consent
        accountIdentifications.getData().size() == 1
        accountIdentifications.getData().get(0).getAccountId() == testAccount.getAccountId().toString()
    }

    def "We can get an account"() {
        when:
        def accountIdentification = accountsService.getAccount(testConsent.getConsentId(), testAccount.getAccountId().toString())

        then:
        accountIdentification.getData() !== null
        accountIdentification.getData().getCompeCode() == testAccount.getCompeCode()
        accountIdentification.getData().getBranchCode() == testAccount.getBranchCode()
        accountIdentification.getData().getNumber() == testAccount.getNumber()
        accountIdentification.getData().getCheckDigit() == testAccount.getCheckDigit()
        accountIdentification.getData().getType().toString() == testAccount.getAccountType()
        accountIdentification.getData().getSubtype().toString() == testAccount.getAccountSubType()
        accountIdentification.getData().getCurrency() == testAccount.getCurrency()
    }

    def "We can get Account Balances"() {
        when:
        def response = accountsService.getAccountBalances(testConsent.getConsentId(), testAccount.getAccountId().toString())

        then:
        response.getData() !== null
        response.getData().getAvailableAmount() == testAccount.getAvailableAmount()
        response.getData().getAvailableAmountCurrency() == testAccount.getAvailableAmountCurrency()
        response.getData().getBlockedAmount() == testAccount.getBlockedAmount()
        response.getData().getBlockedAmountCurrency() == testAccount.getBlockedAmountCurrency()
        response.getData().getAutomaticallyInvestedAmount() == testAccount.getAutomaticallyInvestedAmount()
        response.getData().getAutomaticallyInvestedAmountCurrency() == testAccount.getAutomaticallyInvestedAmountCurrency()
    }

    def "We can get OverdraftLimits"() {
        when:
        def response = accountsService.getAccountOverdraftLimits(testConsent.getConsentId(), testAccount.getAccountId().toString())

        then:
        response.getData().getOverdraftContractedLimit() == testAccount.getOverdraftContractedLimit()
        response.getData().getOverdraftContractedLimitCurrency() == testAccount.getOverdraftContractedLimitCurrency()
        response.getData().getOverdraftUsedLimit() == testAccount.getOverdraftUsedLimit()
        response.getData().getUnarrangedOverdraftAmount() == testAccount.getUnarrangedOverdraftAmount()
        response.getData().getUnarrangedOverdraftAmountCurrency() == testAccount.getUnarrangedOverdraftAmountCurrency()
    }

    def "We can get Account Transactions"() {
        when:
        def response = accountsService.getAccountTransactions(Pageable.from(0), testConsent.getConsentId(),
                LocalDate.now(), LocalDate.now(), null, testAccount.getAccountId().toString())

        then:
        !response.getData().empty

        when:
        def accountTransactionData = response.getData().stream()
                .filter(t -> t.getTransactionId() == testAccountTransactions.getTransactionId())
                .findFirst().get()

        then:
        accountTransactionData.getTransactionId() == testAccountTransactions.getTransactionId()
        accountTransactionData.getCompletedAuthorisedPaymentType().toString() == testAccountTransactions.getCompletedAuthorisedPaymentType()
        accountTransactionData.getCreditDebitType().toString() == testAccountTransactions.getCreditDebitType()
        accountTransactionData.getTransactionName() == testAccountTransactions.getTransactionName()
        accountTransactionData.getType().toString() == testAccountTransactions.getType()
        accountTransactionData.getAmount() == testAccountTransactions.getAmount()
        accountTransactionData.getTransactionCurrency() == testAccountTransactions.getTransactionCurrency()
        accountTransactionData.getTransactionDate() == testAccountTransactions.getTransactionDate().toString()
        accountTransactionData.getPartieCnpjCpf() == testAccountTransactions.getPartieCnpjCpf()
        accountTransactionData.getPartiePersonType().toString() == EnumPartiePersonType.valueOf(testAccountTransactions.getPartiePersonType()).toString()
        accountTransactionData.getPartieCompeCode() == testAccountTransactions.getPartieCompeCode()
        accountTransactionData.getPartieBranchCode() == testAccountTransactions.getPartieBranchCode()
        accountTransactionData.getPartieNumber() == testAccountTransactions.getPartieNumber()
        accountTransactionData.getPartieCheckDigit() == testAccountTransactions.getPartieCheckDigit()
    }

    def "we can get pages"() {
        given:
        var pageSize = 2
        consentAccountRepository.save(new ConsentAccountEntity(testConsent, accountRepository.save(anAccount(testAccountHolder.getAccountHolderId()))))
        consentAccountRepository.save(new ConsentAccountEntity(testConsent, accountRepository.save(anAccount(testAccountHolder.getAccountHolderId()))))

        when:
        //get first page
        def page1 = accountsService.getAccounts(Pageable.from(0, pageSize), testConsent.getConsentId(), null)

        then:
        !page1.getData().empty
        page1.getData().size() == page1.getMeta().getTotalRecords()
        page1.getMeta().getTotalPages() == pageSize

        when:
        //get second page
        def page2 = accountsService.getAccounts(Pageable.from(1, pageSize), testConsent.getConsentId(), null)

        then:
        !page2.getData().empty
        page2.getData().size() == page2.getMeta().getTotalRecords()
        page2.getMeta().getTotalPages() == pageSize

        and:
        //account from page2 is not contain in page1
        def accFromPage2 = page2.getData().first()
        !page1.getData().contains(accFromPage2)
    }

    def "we cannot get a response when the consent not have permissions"() {
        setup:
        def errorMessage = "You do not have the correct permission"
        def testAccountHolder2 = accountHolderRepository.save(anAccountHolder())
        def testAccount2 = accountRepository.save(anAccount(testAccountHolder2.getAccountHolderId()))
        def testConsent2 = consentRepository.save(aConsent(testAccountHolder2.getAccountHolderId()))
        consentAccountRepository.save(new ConsentAccountEntity(testConsent2, testAccount2))

        when:
        accountsService.getAccounts(Pageable.unpaged(), testConsent2.getConsentId(), null)

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.FORBIDDEN
        e1.getMessage() == errorMessage

        when:
        accountsService.getAccount(testConsent2.getConsentId(), testAccount2.getAccountId().toString())

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.FORBIDDEN
        e2.getMessage() == errorMessage

        when:
        accountsService.getAccountBalances(testConsent2.getConsentId(), testAccount2.getAccountId().toString())

        then:
        HttpStatusException e3 = thrown()
        e3.status == HttpStatus.FORBIDDEN
        e3.getMessage() == errorMessage

        when:
        accountsService.getAccountOverdraftLimits(testConsent2.getConsentId(), testAccount2.getAccountId().toString())

        then:
        HttpStatusException e4 = thrown()
        e4.status == HttpStatus.FORBIDDEN
        e4.getMessage() == errorMessage

        when:
        accountsService.getAccountTransactions(Pageable.unpaged(), testConsent2.getConsentId(),
                LocalDate.now(), LocalDate.now(),null, testAccount2.getAccountId().toString())

        then:
        HttpStatusException e5 = thrown()
        e5.status == HttpStatus.FORBIDDEN
        e5.getMessage() == errorMessage
    }

    def "we cannot get a response when the consent owner is not the account owner"() {
        setup:
        def errorMessage = "Forbidden, consent owner does not match account owner!"
        def testAccountHolder2 = accountHolderRepository.save(anAccountHolder())
        def testAccount2 = accountRepository.save(anAccount(testAccountHolder2.getAccountHolderId()))
        consentAccountRepository.save(new ConsentAccountEntity(testConsent, testAccount2))

        when:
        accountsService.getAccounts(Pageable.unpaged(), testConsent.getConsentId().toString(), null)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.FORBIDDEN
        e.getMessage() == errorMessage

        when:
        accountsService.getAccount(testConsent.getConsentId(), testAccount2.getAccountId().toString())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.FORBIDDEN
        e1.getMessage() == errorMessage

        when:
        accountsService.getAccountBalances(testConsent.getConsentId(), testAccount2.getAccountId().toString())

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.FORBIDDEN
        e2.getMessage() == errorMessage

        when:
        accountsService.getAccountOverdraftLimits(testConsent.getConsentId(), testAccount2.getAccountId().toString())

        then:
        HttpStatusException e3 = thrown()
        e3.status == HttpStatus.FORBIDDEN
        e3.getMessage() == errorMessage

        when:
        accountsService.getAccountTransactions(Pageable.unpaged(), testConsent.getConsentId(),
                LocalDate.now(), LocalDate.now(), null, testAccount2.getAccountId().toString())

        then:
        HttpStatusException e4 = thrown()
        e4.status == HttpStatus.FORBIDDEN
        e4.getMessage() == errorMessage
    }

    def "we cannot get response when consent does not cover account"() {
        setup:
        def errorMessage = "Bad request, consent does not cover this account!"
        def testAccountHolder2 = accountHolderRepository.save(anAccountHolder())
        def testAccount2 = accountRepository.save(anAccount(testAccountHolder2.getAccountHolderId()))

        when:
        accountsService.getAccount(testConsent.getConsentId(), testAccount2.getAccountId().toString())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.BAD_REQUEST
        e1.getMessage() == errorMessage

        when:
        accountsService.getAccountBalances(testConsent.getConsentId(), testAccount2.getAccountId().toString())

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.BAD_REQUEST
        e2.getMessage() == errorMessage

        when:
        accountsService.getAccountOverdraftLimits(testConsent.getConsentId(), testAccount2.getAccountId().toString())

        then:
        HttpStatusException e3 = thrown()
        e3.status == HttpStatus.BAD_REQUEST
        e3.getMessage() == errorMessage

        when:
        accountsService.getAccountTransactions(Pageable.unpaged(), testConsent.getConsentId(),
                LocalDate.now(), LocalDate.now(), null, testAccount2.getAccountId().toString())

        then:
        HttpStatusException e4 = thrown()
        e4.status == HttpStatus.BAD_REQUEST
        e4.getMessage() == errorMessage
    }

    def "we cannot get response without authorised status"() {
        setup:
        def errorMessage = "Bad request, consent not Authorised!"
        testConsent.setStatus(UpdateConsentData.StatusEnum.AWAITING_AUTHORISATION.name())
        consentRepository.update(testConsent)

        when:
        accountsService.getAccounts(Pageable.unpaged(), testConsent.getConsentId(), null)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.BAD_REQUEST
        e.getMessage() == errorMessage

        when:
        accountsService.getAccount(testConsent.getConsentId(), testAccount.getAccountId().toString())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.BAD_REQUEST
        e1.getMessage() == errorMessage

        when:
        accountsService.getAccountBalances(testConsent.getConsentId(), testAccount.getAccountId().toString())

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.BAD_REQUEST
        e2.getMessage() == errorMessage

        when:
        accountsService.getAccountOverdraftLimits(testConsent.getConsentId(), testAccount.getAccountId().toString())

        then:
        HttpStatusException e3 = thrown()
        e3.status == HttpStatus.BAD_REQUEST
        e3.getMessage() == errorMessage

        when:
        accountsService.getAccountTransactions(Pageable.unpaged(), testConsent.getConsentId(),
                LocalDate.now(), LocalDate.now(), null, testAccount.getAccountId().toString())

        then:
        HttpStatusException e4 = thrown()
        e4.status == HttpStatus.BAD_REQUEST
        e4.getMessage() == errorMessage
    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }
}
