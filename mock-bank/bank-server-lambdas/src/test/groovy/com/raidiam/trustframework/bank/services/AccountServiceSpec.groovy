package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.mockbank.models.generated.EnumConsentPermissions
import com.raidiam.trustframework.mockbank.models.generated.EnumConsentStatus
import com.raidiam.trustframework.mockbank.models.generated.EnumPartiePersonType
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
@MicronautTest(transactional = true, environments = ["db"])
class AccountServiceSpec extends CleanupSpecification {

    @Inject
    AccountsService accountsService
    @Shared
    int consentReferenceId
    @Shared
    int testAccountReferenceId
    @Shared
    int testAccountTransactionId
    @Shared
    int testAccountHolderReferenceId

    def setup() {
        if (runSetup) {
            accountHolderRepository.save(anAccountHolder("10117409073", "CPF"))
            def testAccountHolder = accountHolderRepository.findByDocumentIdentificationAndDocumentRel("10117409073", "CPF").get(0)
            accountRepository.save(anAccount(testAccountHolder))
            def testAccount = accountRepository.findByAccountHolderId(testAccountHolder.getAccountHolderId()).get(0)
            def testConsent = consentRepository.save(aConsent(testAccountHolder.getAccountHolderId()))
            testConsent.getConsentPermissions().add(aConsentPermission(EnumConsentPermissions.ACCOUNTS_READ, testConsent.getConsentId()))
            testConsent.getConsentPermissions().add(aConsentPermission(EnumConsentPermissions.ACCOUNTS_BALANCES_READ, testConsent.getConsentId()))
            testConsent.getConsentPermissions().add(aConsentPermission(EnumConsentPermissions.ACCOUNTS_TRANSACTIONS_READ, testConsent.getConsentId()))
            testConsent.getConsentPermissions().add(aConsentPermission(EnumConsentPermissions.ACCOUNTS_OVERDRAFT_LIMITS_READ, testConsent.getConsentId()))
            testConsent.getAccounts().add(testAccount)
            consentRepository.update(testConsent)

            testAccount.getTransactions().add(aTransaction(testAccount.getAccountId()))
            testAccount = accountRepository.update(testAccount)

            consentReferenceId = testConsent.getReferenceId()
            testAccountReferenceId = testAccount.getReferenceId()
            testAccountHolderReferenceId = testAccountHolder.getReferenceId()
            testAccountTransactionId = testAccount.getTransactions().first().getAccountTransactionId()

            runSetup = false
        }
    }

    def "We can get all accounts provided in Consent"() {
        given:
        def testConsent = consentRepository.findById(consentReferenceId).get()
        def testAccount = accountRepository.findById(testAccountReferenceId).get()

        when:
        def accountIdentifications = accountsService.getAccounts(Pageable.from(0), testConsent.getConsentId(), null)

        then:
        !accountIdentifications.getData().empty
        //provided only 1 Account in Consent
        accountIdentifications.getData().size() == 1
        accountIdentifications.getData().get(0).getAccountId() == testAccount.getAccountId().toString()
    }

    def "We can get an account"() {
        given:
        def testConsent = consentRepository.findById(consentReferenceId).get()
        def testAccount = accountRepository.findById(testAccountReferenceId).get()

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
        given:
        def testConsent = consentRepository.findById(consentReferenceId).get()
        def testAccount = accountRepository.findById(testAccountReferenceId).get()

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
        given:
        def testConsent = consentRepository.findById(consentReferenceId).get()
        def testAccount = accountRepository.findById(testAccountReferenceId).get()

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
        given:
        def testConsent = consentRepository.findById(consentReferenceId).get()
        def testAccount = accountRepository.findById(testAccountReferenceId).get()
        def testAccountTransaction = accountTransactionsRepository.findById(testAccountTransactionId).get()

        when:
        def response = accountsService.getAccountTransactionsV2(Pageable.from(0), testConsent.getConsentId(),
                LocalDate.now(), LocalDate.now(), null, testAccount.getAccountId().toString())

        then:
        !response.getData().empty

        when:
        def accountTransactionData = response.getData().stream()
                .filter(t -> t.getTransactionId() == testAccountTransaction.getTransactionId().toString())
                .findFirst().get()

        then:
        accountTransactionData.getTransactionId() == testAccountTransaction.getTransactionId().toString()
        accountTransactionData.getCompletedAuthorisedPaymentType().toString() == testAccountTransaction.getCompletedAuthorisedPaymentType()
        accountTransactionData.getCreditDebitType().toString() == testAccountTransaction.getCreditDebitType()
        accountTransactionData.getTransactionName() == testAccountTransaction.getTransactionName()
        accountTransactionData.getType().toString() == testAccountTransaction.getType()
        accountTransactionData.getPartieCnpjCpf() == testAccountTransaction.getPartieCnpjCpf()
        accountTransactionData.getPartiePersonType().toString() == EnumPartiePersonType.valueOf(testAccountTransaction.getPartiePersonType()).toString()
        accountTransactionData.getPartieCompeCode() == testAccountTransaction.getPartieCompeCode()
        accountTransactionData.getPartieBranchCode() == testAccountTransaction.getPartieBranchCode()
        accountTransactionData.getPartieNumber() == testAccountTransaction.getPartieNumber()
        accountTransactionData.getPartieCheckDigit() == testAccountTransaction.getPartieCheckDigit()
    }

    def "we can get pages"() {
        given:
        var pageSize = 2
        def testAccountHolder = accountHolderRepository.findById(testAccountHolderReferenceId).get()
        def testConsent = consentRepository.findById(consentReferenceId).get()
        testConsent.getAccounts().add(accountRepository.save(anAccount(testAccountHolder)))
        testConsent.getAccounts().add(accountRepository.save(anAccount(testAccountHolder)))
        testConsent = consentRepository.update(testConsent)


        when:
        //get first page
        def page1 = accountsService.getAccounts(Pageable.from(0, pageSize), testConsent.getConsentId(), null)
        def page1Size = page1.getData().size()
        then:
        !page1.getData().empty
        page1.getMeta().getTotalPages() == pageSize

        when:
        //get second page
        def page2 = accountsService.getAccounts(Pageable.from(1, pageSize), testConsent.getConsentId(), null)
        def page2Size = page2.getData().size()

        then:
        !page2.getData().empty
        page2.getMeta().getTotalPages() == pageSize

        and:
        page1.getMeta().getTotalRecords() == page1Size + page2Size
        page2.getMeta().getTotalRecords() == page1Size + page2Size
        //account from page2 is not contain in page1
        def accFromPage2 = page2.getData().first()
        !page1.getData().contains(accFromPage2)
    }

    def "we cannot get a response when the consent not have permissions"() {
        setup:
        def errorMessage = "You do not have the correct permission"
        def testAccountHolder2 = accountHolderRepository.save(anAccountHolder())
        def placeholderAcct = anAccount(testAccountHolder2)
        placeholderAcct.accountHolder = testAccountHolder2
        def testAccount2 = accountRepository.save(placeholderAcct)
        def placeholderConsent = aConsent(testAccountHolder2.getAccountHolderId())
        placeholderConsent.setAccountHolder(testAccountHolder2)
        def testConsent2 = consentRepository.save(placeholderConsent)
        testAccount2 = accountRepository.findById(testAccount2.getReferenceId()).get()
        testConsent2.getAccounts().add(testAccount2)
        testConsent2 = consentRepository.update(testConsent2)


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
        accountsService.getAccountTransactionsV2(Pageable.unpaged(), testConsent2.getConsentId(),
                LocalDate.now(), LocalDate.now(),null, testAccount2.getAccountId().toString())

        then:
        HttpStatusException e5 = thrown()
        e5.status == HttpStatus.FORBIDDEN
        e5.getMessage() == errorMessage
    }

    def "we cannot get a response when the consent owner is not the account owner"() {
        setup:
        def testConsent = consentRepository.findById(consentReferenceId).get()
        def errorMessage = "Forbidden, consent owner does not match account owner!"
        def testAccountHolder2 = accountHolderRepository.save(anAccountHolder())
        testAccountHolder2 = accountHolderRepository.findById(testAccountHolder2.getReferenceId()).get()
        def placeholder = anAccount(testAccountHolder2)
        placeholder.setAccountHolder(testAccountHolder2)
        def testAccount2 = accountRepository.save(placeholder)
        testConsent.getAccounts().add(testAccount2)
        testConsent = consentRepository.save(testConsent)

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
        accountsService.getAccountTransactionsV2(Pageable.unpaged(), testConsent.getConsentId(),
                LocalDate.now(), LocalDate.now(), null, testAccount2.getAccountId().toString())

        then:
        HttpStatusException e4 = thrown()
        e4.status == HttpStatus.FORBIDDEN
        e4.getMessage() == errorMessage
    }

    def "we cannot get response when consent does not cover account"() {
        setup:
        def testConsent = consentRepository.findById(consentReferenceId).get()
        def errorMessage = "Bad request, consent does not cover this account!"
        def testAccountHolder2 = accountHolderRepository.save(anAccountHolder())
        testAccountHolder2 = accountHolderRepository.findById(testAccountHolder2.getReferenceId()).get()
        def placeholder = anAccount(testAccountHolder2)
        def testAccount2 = accountRepository.save(placeholder)
        testAccount2 = accountRepository.findById(testAccount2.getReferenceId()).get()

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
        accountsService.getAccountTransactionsV2(Pageable.unpaged(), testConsent.getConsentId(),
                LocalDate.now(), LocalDate.now(), null, testAccount2.getAccountId().toString())

        then:
        HttpStatusException e4 = thrown()
        e4.status == HttpStatus.BAD_REQUEST
        e4.getMessage() == errorMessage
    }

    def "we cannot get response without authorised status"() {
        setup:
        def testConsent = consentRepository.findById(consentReferenceId).get()
        def errorMessage = "Bad request, consent not Authorised!"
        testConsent.setStatus(EnumConsentStatus.AWAITING_AUTHORISATION.name())
        consentRepository.update(testConsent)
        def testAccount = accountRepository.findById(testAccountReferenceId).get()

        when:
        accountsService.getAccounts(Pageable.unpaged(), testConsent.getConsentId(), null)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.UNAUTHORIZED
        e.getMessage() == errorMessage

        when:
        accountsService.getAccount(testConsent.getConsentId(), testAccount.getAccountId().toString())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.UNAUTHORIZED
        e1.getMessage() == errorMessage

        when:
        accountsService.getAccountBalances(testConsent.getConsentId(), testAccount.getAccountId().toString())

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.UNAUTHORIZED
        e2.getMessage() == errorMessage

        when:
        accountsService.getAccountOverdraftLimits(testConsent.getConsentId(), testAccount.getAccountId().toString())

        then:
        HttpStatusException e3 = thrown()
        e3.status == HttpStatus.UNAUTHORIZED
        e3.getMessage() == errorMessage

        when:
        accountsService.getAccountTransactionsV2(Pageable.unpaged(), testConsent.getConsentId(),
                LocalDate.now(), LocalDate.now(), null, testAccount.getAccountId().toString())

        then:
        HttpStatusException e4 = thrown()
        e4.status == HttpStatus.UNAUTHORIZED
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
