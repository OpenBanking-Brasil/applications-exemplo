package com.raidiam.trustframework.bank.repository

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.TestEntityDataFactory
import com.raidiam.trustframework.bank.domain.ConsentAccountEntity
import com.raidiam.trustframework.mockbank.models.generated.EnumAccountType
import com.raidiam.trustframework.mockbank.models.generated.EnumCreditDebitIndicator
import io.micronaut.data.model.Pageable
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Stepwise

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.IntStream

import static com.raidiam.trustframework.bank.TestEntityDataFactory.aConsent

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class AccountRepositorySpec extends CleanupSpecification {

    def "We can get an account"() {

        given:
        def accountHolder = accountHolderRepository.save(TestEntityDataFactory.anAccountHolder())
        def account = accountRepository.save(TestEntityDataFactory.anAccount(accountHolder))
        def accountTransaction = accountTransactionsRepository.save(TestEntityDataFactory.aTransaction(account.getAccountId()))

        when:
        def accountRetrieved = accountRepository.findByAccountHolderId(accountHolder.getAccountHolderId())
        def accountHoldersRetrieved = accountHolderRepository.findByDocumentIdentificationAndDocumentRel(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())

        then:
        noExceptionThrown()
        accountRetrieved.size() == 1

        when:
        def foundAccount = accountRetrieved.get(0)
        def transactions = foundAccount.getTransactions()

        then:
        noExceptionThrown()
        transactions.size() == 1
        transactions.first().getTransactionId() == accountTransaction.getTransactionId()
    }

    def "We can get pageable accounts"() {
        given:
        def accountHolder = accountHolderRepository.save(TestEntityDataFactory.anAccountHolder())
        def account1 = accountRepository.save(TestEntityDataFactory.anAccount(accountHolder))
        def account2 = accountRepository.save(TestEntityDataFactory.anAccount(accountHolder))
        def account3 = accountRepository.save(TestEntityDataFactory.anAccount(accountHolder))
        def consent = consentRepository.save(aConsent(accountHolder.getAccountHolderId()))
        consentAccountRepository.save(new ConsentAccountEntity(consent, account1))
        consentAccountRepository.save(new ConsentAccountEntity(consent, account2))
        consentAccountRepository.save(new ConsentAccountEntity(consent, account3))

        when:
        def page1 = consentAccountRepository.findByConsentConsentIdOrderByCreatedAtAsc(consent.getConsentId(), Pageable.from(0, 2))
        def page2 = consentAccountRepository.findByConsentConsentIdOrderByCreatedAtAsc(consent.getConsentId(), Pageable.from(1, 2))

        then:
        // we can see the total number of accounts on each page
        page1.totalSize == 3
        page2.totalSize == 3

        // first page has 2 accounts
        page1.size() == 2
        page1.first().account == account1
        page1.last().account == account2

        // second page has 1 account
        page2.size() == 1
        page2.first().account == account3

        // page 1 has no accounts from page 2
        !page1.collect().contains(page2.first())

        when:
        def pageAll = consentAccountRepository.findByConsentConsentIdOrderByCreatedAtAsc(consent.getConsentId(), Pageable.from(0))

        then:
        // page has all accounts
        pageAll.size() == 3

        // only one page
        pageAll.getTotalPages() == 1

        // sorted by createAt (second account is always older than first account, and third older then second)
        pageAll.collect().get(0).createdAt.before(pageAll.collect().get(1).createdAt)
        pageAll.collect().get(1).createdAt.before(pageAll.collect().get(2).createdAt)

        when:
        // find accounts with accountType parameter
        def pageWithParameter = consentAccountRepository.findByConsentConsentIdAndAccountAccountTypeOrderByCreatedAtAsc(consent.getConsentId(),
                EnumAccountType.DEPOSITO_A_VISTA.toString(), Pageable.from(0))

        then:
        pageWithParameter.size() == 3
        !pageWithParameter.collect().empty

        when:
        // find accounts with wrong accountType parameter
        def pageWithWrongParameter = consentAccountRepository.findByConsentConsentIdAndAccountAccountTypeOrderByCreatedAtAsc(consent.getConsentId(),
                EnumAccountType.PAGAMENTO_PRE_PAGA.toString(), Pageable.from(0))

        then:
        pageWithWrongParameter.size() == 0
        pageWithWrongParameter.collect().empty
    }

    def "We can get pageable account transactions by transaction period"() {
        given:
        def accountHolder = accountHolderRepository.save(TestEntityDataFactory.anAccountHolder())
        def account = accountRepository.save(TestEntityDataFactory.anAccount(accountHolder))

        def date = OffsetDateTime.parse("2022-05-01T00:01:00+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        // add 5 transactions (with range 2022-05-01 - 2022-05-05)
        IntStream.range(0, 5).forEach(i -> {
            accountTransactionsRepository.save(TestEntityDataFactory.aTransaction(account.accountId, date.plusDays(i)))
        })

        //required range (2022-05-02 - 2022-05-04)
        when:
        def page1 = accountTransactionsRepository.findByAccountIdAndTransactionDateTimeBetweenOrderByCreatedAtAsc(
                account.accountId, OffsetDateTime.parse("2022-05-02T00:01:00+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME), OffsetDateTime.parse("2022-05-04T23:59:59+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME), Pageable.from(0, 2))
        def page2 = accountTransactionsRepository.findByAccountIdAndTransactionDateTimeBetweenOrderByCreatedAtAsc(
                account.accountId, OffsetDateTime.parse("2022-05-02T00:01:00+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME), OffsetDateTime.parse("2022-05-04T23:59:59+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME), Pageable.from(1, 2))

        then:
        // we can see the total number of transactions on each page
        page1.totalSize == 3
        page2.totalSize == 3

        // first page has 2 transactions
        page1.size() == 2

        // second page has 1 transaction
        page2.size() == 1

        // page 1 has no transactions from page 2
        !page1.collect().contains(page2.first())

        when:
        def pageAll = accountTransactionsRepository.findByAccountIdAndTransactionDateTimeBetweenOrderByCreatedAtAsc(
                account.accountId, date, OffsetDateTime.parse("2022-05-05T23:59:59+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME), Pageable.from(0))

        then:
        // page has all transactions
        pageAll.size() == 5

        // only one page
        pageAll.getTotalPages() == 1

        // sorted by createAt (first account is always older than second account and second older then third etc)
        pageAll.collect().get(0).createdAt.before(pageAll.collect().get(1).createdAt)
        pageAll.collect().get(1).createdAt.before(pageAll.collect().get(2).createdAt)
        pageAll.collect().get(2).createdAt.before(pageAll.collect().get(3).createdAt)
        pageAll.collect().get(3).createdAt.before(pageAll.collect().get(4).createdAt)

        when:
        // find transactions with creditDebitType parameter
        def pageWithParameter = accountTransactionsRepository.findByAccountIdAndTransactionDateTimeBetweenAndCreditDebitTypeOrderByCreatedAtAsc(
                account.accountId, date, OffsetDateTime.parse("2022-05-05T23:59:59+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME), EnumCreditDebitIndicator.DEBITO.toString(), Pageable.from(0))

        then:
        pageWithParameter.size() == 5
        !pageWithParameter.collect().empty

        when:
        // find transactions with wrong creditDebitType parameter
        def pageWithWrongParameter = accountTransactionsRepository.findByAccountIdAndTransactionDateTimeBetweenAndCreditDebitTypeOrderByCreatedAtAsc(
                account.accountId, date, OffsetDateTime.parse("2022-05-05T23:59:59+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME), EnumCreditDebitIndicator.CREDITO.toString(), Pageable.from(0))

        then:
        pageWithWrongParameter.size() == 0
        pageWithWrongParameter.collect().empty
    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }
}

