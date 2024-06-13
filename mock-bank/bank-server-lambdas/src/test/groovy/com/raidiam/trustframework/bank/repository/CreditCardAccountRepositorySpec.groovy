package com.raidiam.trustframework.bank.repository

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.domain.ConsentCreditCardAccountsEntity
import com.raidiam.trustframework.mockbank.models.generated.EnumCreditCardTransactionType
import io.micronaut.data.model.Pageable
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Stepwise

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.IntStream

import static com.raidiam.trustframework.bank.TestEntityDataFactory.*

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class CreditCardAccountRepositorySpec extends CleanupSpecification {

    private static final Logger LOG = LoggerFactory.getLogger(CreditCardAccountRepositorySpec.class);

    def "We can get an creditCardAccount"() {

        given:
        def accountHolder = accountHolderRepository.save(anAccountHolder())
        def account = creditCardAccountsRepository.save(anCreditCardAccounts(accountHolder.getAccountHolderId()))
        def accountPayment = creditCardsAccountPaymentMethodRepository.save(anCreditCardsAccountPaymentMethod(account))
        def accountLimits = creditCardAccountsLimitsRepository.save(anCreditCardAccountsLimits(account.creditCardAccountId))
        def accountBill = creditCardAccountsBillsRepository.save(anCreditCardAccountsBill(account))
        def billsFinanceCharge = creditCardAccountsBillsFinanceChargeRepository.save(anCreditCardAccountsBillsFinanceCharge(accountBill))
        def billPayments = creditCardAccountsBillsPaymentRepository.save(anCreditCardAccountsBillsPayment(accountBill))
        def accountTransaction = creditCardAccountsTransactionRepository.save(anCreditCardAccountsTransaction(accountBill.billId, account.creditCardAccountId))

        when:
        def creditCardAccountOpt = creditCardAccountsRepository.findByCreditCardAccountId(account.getCreditCardAccountId())

        then:
        noExceptionThrown()

        when:
        def foundAccount = creditCardAccountOpt.get()
        def foundAccountPayment = foundAccount.getPaymentMethods().first()
        def foundAccountLimits = foundAccount.getLimits().first()
        def foundAccoundBills = foundAccount.getBills().first()
        def foundBillsFinanceCharge = foundAccoundBills.getFinanceCharges().first()
        def foundBillPayments = foundAccoundBills.getPayments().first()
        def foundAccountTransaction = foundAccoundBills.getTransactions().first()

        then:
        noExceptionThrown()
        account == foundAccount
        accountPayment == foundAccountPayment
        accountLimits == foundAccountLimits
        accountBill == foundAccoundBills
        billsFinanceCharge == foundBillsFinanceCharge
        billPayments == foundBillPayments
        accountTransaction == foundAccountTransaction

    }

    def "We can get pageable credit card accounts"() {
        given:
        def accountHolder = accountHolderRepository.save(anAccountHolder())
        def account1 = creditCardAccountsRepository.save(anCreditCardAccounts(accountHolder.getAccountHolderId()))
        def account2 = creditCardAccountsRepository.save(anCreditCardAccounts(accountHolder.getAccountHolderId()))
        def account3 = creditCardAccountsRepository.save(anCreditCardAccounts(accountHolder.getAccountHolderId()))
        def consent = consentRepository.save(aConsent(accountHolder.getAccountHolderId()))
        consentCreditCardAccountsRepository.save(new ConsentCreditCardAccountsEntity(consent, account1))
        consentCreditCardAccountsRepository.save(new ConsentCreditCardAccountsEntity(consent, account2))
        consentCreditCardAccountsRepository.save(new ConsentCreditCardAccountsEntity(consent, account3))

        when:
        def page1 = consentCreditCardAccountsRepository.findByConsentIdOrderByCreatedAtAsc(consent.getConsentId(), Pageable.from(0, 2))
        def page2 = consentCreditCardAccountsRepository.findByConsentIdOrderByCreatedAtAsc(consent.getConsentId(), Pageable.from(1, 2))

        then:
        // we can see the total number of accounts on each page
        page1.totalSize == 3
        page2.totalSize == 3

        // first page has 2 accounts
        page1.size() == 2
        page1.first().creditCardAccount == account1
        page1.last().creditCardAccount == account2

        // second page has 1 account
        page2.size() == 1
        page2.first().creditCardAccount == account3

        // page 1 has no accounts from page 2
        !page1.collect().contains(page2.first())

        when:
        def pageAll = consentCreditCardAccountsRepository.findByConsentIdOrderByCreatedAtAsc(consent.getConsentId(), Pageable.from(0))

        then:
        // page have all accounts
        pageAll.size() == 3

        // only one page
        pageAll.getTotalPages() == 1

        // sorted by createAt (first CC is always older than second CC and second older then third etc)
        pageAll.collect().get(0).createdAt.before(pageAll.collect().get(1).createdAt)
        pageAll.collect().get(1).createdAt.before(pageAll.collect().get(2).createdAt)
    }

    def "We can get pageable credit card account transactions by transaction period"() {
        given:
        def accountHolder = accountHolderRepository.save(anAccountHolder())
        def account = creditCardAccountsRepository.save(anCreditCardAccounts(accountHolder.getAccountHolderId()))

        def date = OffsetDateTime.parse("2022-05-01T00:01:00+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        // add 5 bills each with 1 transaction (with range 2022-05-01 - 2022-05-05)
        IntStream.range(0, 5).forEach(i -> {
            def bill = creditCardAccountsBillsRepository.save(anCreditCardAccountsBill(account))
            creditCardAccountsTransactionRepository.save(anCreditCardAccountsTransaction(bill.billId, account.getCreditCardAccountId(),
                    date.plusDays(i)))
        })

        //required range (2022-05-02 - 2022-05-04)
        when:
        def page1 = creditCardAccountsTransactionRepository.findByCreditCardAccountIdAndTransactionDateTimeBetweenOrderByCreatedAtAsc(account.creditCardAccountId,
                OffsetDateTime.parse("2022-05-02T00:01:00+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME), OffsetDateTime.parse("2022-05-04T23:59:59+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME), Pageable.from(0, 2))
        def page2 = creditCardAccountsTransactionRepository.findByCreditCardAccountIdAndTransactionDateTimeBetweenOrderByCreatedAtAsc(account.creditCardAccountId,
                OffsetDateTime.parse("2022-05-02T00:01:00+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME), OffsetDateTime.parse("2022-05-04T23:59:59+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME), Pageable.from(1, 2))

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
        def pageAll = creditCardAccountsTransactionRepository.findByCreditCardAccountIdAndTransactionDateTimeBetweenOrderByCreatedAtAsc(
                account.creditCardAccountId, date, date.plusDays(4), Pageable.from(0))

        then:
        // page has all transactions
        pageAll.size() == 5

        // only one page
        pageAll.getTotalPages() == 1

        // sorted by createAt (first CC is always older than second CC and second older then third etc)
        pageAll.collect().get(0).createdAt.before(pageAll.collect().get(1).createdAt)
        pageAll.collect().get(1).createdAt.before(pageAll.collect().get(2).createdAt)
        pageAll.collect().get(2).createdAt.before(pageAll.collect().get(3).createdAt)
        pageAll.collect().get(3).createdAt.before(pageAll.collect().get(4).createdAt)

        when:
        // find transactions with transactionType parameter
        def pageWithParameter1 = creditCardAccountsTransactionRepository.findByCreditCardAccountIdAndTransactionDateTimeBetweenAndTransactionTypeOrderByCreatedAtAsc(
                account.creditCardAccountId, date, date.plusDays(4), EnumCreditCardTransactionType.OPERACOES_CREDITO_CONTRATADAS_CARTAO.name(), Pageable.from(0))

        then:
        pageWithParameter1.size() == 5
        !pageWithParameter1.collect().empty

        when:
        // find transactions with wrong transactionType parameter
        def pageWithWrongParameter1 = creditCardAccountsTransactionRepository.findByCreditCardAccountIdAndTransactionDateTimeBetweenAndTransactionTypeOrderByCreatedAtAsc(
                account.creditCardAccountId, date, date.plusDays(4), EnumCreditCardTransactionType.PAGAMENTO.name(), Pageable.from(0))

        then:
        pageWithWrongParameter1.size() == 0
        pageWithWrongParameter1.collect().empty

        when:
        // find transactions with payeeMCC parameter
        def pageWithParameter2 = creditCardAccountsTransactionRepository.findByCreditCardAccountIdAndTransactionDateTimeBetweenAndPayeeMCCOrderByCreatedAtAsc(
                account.creditCardAccountId, date, date.plusDays(4), new BigDecimal(5912), Pageable.from(0))

        then:
        pageWithParameter2.size() == 5
        !pageWithParameter2.collect().empty

        when:
        // find transactions with wrong payeeMCC parameter
        def pageWithWrongParameter2 = creditCardAccountsTransactionRepository.findByCreditCardAccountIdAndTransactionDateTimeBetweenAndPayeeMCCOrderByCreatedAtAsc(
                account.creditCardAccountId, date, date.plusDays(4), new BigDecimal(2195), Pageable.from(0))

        then:
        pageWithWrongParameter2.size() == 0
        pageWithWrongParameter2.collect().empty
    }

    def "We can get pageable credit card account transactions by transaction period and billId"() {
        given:
        def accountHolder = accountHolderRepository.save(anAccountHolder())
        def account = creditCardAccountsRepository.save(anCreditCardAccounts(accountHolder.getAccountHolderId()))
        def bill1 = creditCardAccountsBillsRepository.save(anCreditCardAccountsBill(account))

        def date = OffsetDateTime.parse("2022-05-01T00:01:00+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        // add 5 transaction in bill1 (with range 2022-05-01 - 2022-05-05)
        IntStream.range(0, 5).forEach(i -> {
            creditCardAccountsTransactionRepository.save(anCreditCardAccountsTransaction(bill1.billId, account.getCreditCardAccountId(),
                    date.plusDays(i)))
        })
        // add 1 transaction in bill2 (And this other bill does not affect the response)
        def someBill = creditCardAccountsBillsRepository.save(anCreditCardAccountsBill(account))
        creditCardAccountsTransactionRepository.save(anCreditCardAccountsTransaction(someBill.billId, account.getCreditCardAccountId(),
                date.plusDays(2)))

        //required range (2022-05-02 - 2022-05-04)
        when:
        def page1 = creditCardAccountsTransactionRepository.findByCreditCardAccountIdAndBillIdAndTransactionDateTimeBetweenOrderByCreatedAtAsc(
                account.creditCardAccountId, bill1.getBillId(),
                OffsetDateTime.parse("2022-05-02T00:01:00+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME), OffsetDateTime.parse("2022-05-04T23:59:59+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME), Pageable.from(0, 2))
        def page2 = creditCardAccountsTransactionRepository.findByCreditCardAccountIdAndBillIdAndTransactionDateTimeBetweenOrderByCreatedAtAsc(
                account.creditCardAccountId, bill1.getBillId(),
                OffsetDateTime.parse("2022-05-02T00:01:00+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME), OffsetDateTime.parse("2022-05-04T23:59:59+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME), Pageable.from(1, 2))

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

        //get all transactions
        when:
        def pageAll = creditCardAccountsTransactionRepository.findByCreditCardAccountIdAndBillIdAndTransactionDateTimeBetweenOrderByCreatedAtAsc(
                account.creditCardAccountId, bill1.getBillId(), date, OffsetDateTime.parse("2022-05-05T23:59:59+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME), Pageable.from(0))

        then:
        // page has all transactions
        pageAll.size() == 5

        // only one page
        pageAll.getTotalPages() == 1

        // sorted by createAt(first CC is always older than second CC and second older then third etc)
        pageAll.collect().get(0).createdAt.before(pageAll.collect().get(1).createdAt)
        pageAll.collect().get(1).createdAt.before(pageAll.collect().get(2).createdAt)
        pageAll.collect().get(2).createdAt.before(pageAll.collect().get(3).createdAt)
        pageAll.collect().get(3).createdAt.before(pageAll.collect().get(4).createdAt)

        when:
        // find transactions with transactionType parameter
        def pageWithParameter1 = creditCardAccountsTransactionRepository.findByCreditCardAccountIdAndBillIdAndTransactionDateTimeBetweenAndTransactionTypeOrderByCreatedAtAsc(
                account.creditCardAccountId, bill1.getBillId(), date, OffsetDateTime.parse("2022-05-05T23:59:59+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                EnumCreditCardTransactionType.OPERACOES_CREDITO_CONTRATADAS_CARTAO.name(), Pageable.from(0))

        then:
        pageWithParameter1.size() == 5
        !pageWithParameter1.collect().empty

        when:
        // find transactions with wrong transactionType parameter
        def pageWithWrongParameter1 = creditCardAccountsTransactionRepository.findByCreditCardAccountIdAndBillIdAndTransactionDateTimeBetweenAndTransactionTypeOrderByCreatedAtAsc(
                account.creditCardAccountId, bill1.getBillId(), date, OffsetDateTime.parse("2022-05-05T23:59:59+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                EnumCreditCardTransactionType.PAGAMENTO.name(), Pageable.from(0))

        then:
        pageWithWrongParameter1.size() == 0
        pageWithWrongParameter1.collect().empty

        when:
        // find transactions with payeeMCC parameter
        def pageWithParameter2 = creditCardAccountsTransactionRepository.findByCreditCardAccountIdAndBillIdAndTransactionDateTimeBetweenAndPayeeMCCOrderByCreatedAtAsc(
                account.creditCardAccountId, bill1.getBillId(), date, OffsetDateTime.parse("2022-05-05T23:59:59+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                new BigDecimal(5912), Pageable.from(0))

        then:
        pageWithParameter2.size() == 5
        !pageWithParameter2.collect().empty

        when:
        // find transactions with wrong payeeMCC parameter
        def pageWithWrongParameter2 = creditCardAccountsTransactionRepository.findByCreditCardAccountIdAndBillIdAndTransactionDateTimeBetweenAndPayeeMCCOrderByCreatedAtAsc(
                account.creditCardAccountId, bill1.getBillId(), date, OffsetDateTime.parse("2022-05-05T23:59:59+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                new BigDecimal(2195), Pageable.from(0))

        then:
        pageWithWrongParameter2.size() == 0
        pageWithWrongParameter2.collect().empty
    }

    def "We can get pageable bill by dueDate period"() {
        given:
        def accountHolder = accountHolderRepository.save(anAccountHolder())
        def account = creditCardAccountsRepository.save(anCreditCardAccounts(accountHolder.getAccountHolderId()))

        def date = LocalDate.parse("2022-05-01")
        // add 5 bills (with range 2022-05-01 - 2022-05-05)
        IntStream.range(0, 5).forEach(i -> {
            creditCardAccountsBillsRepository.save(anCreditCardAccountsBill(account, date.plusDays(i)))
	    // if we don't sleep then the comparisons later on fail, if the bills are created in the same millisecond
            sleep(100)
        })

        //required range (2022-05-02 - 2022-05-04)
        when:
        def page1 = creditCardAccountsBillsRepository.findByAccountAndDueDateBetweenOrderByCreatedAtAsc(
                account, LocalDate.parse("2022-05-02"), LocalDate.parse("2022-05-04"), Pageable.from(0, 2))
        def page2 = creditCardAccountsBillsRepository.findByAccountAndDueDateBetweenOrderByCreatedAtAsc(
                account, LocalDate.parse("2022-05-02"), LocalDate.parse("2022-05-04"), Pageable.from(1, 2))

        then:
        // we can see the total number of bills on each page
        page1.totalSize == 3
        page2.totalSize == 3

        // first page has 2 bills
        page1.size() == 2

        // second page has 1 bill
        page2.size() == 1

        // page 1 has no bills from page 2
        !page1.collect().contains(page2.first())

        when:
        def pageAll = creditCardAccountsBillsRepository.findByAccountAndDueDateBetweenOrderByCreatedAtAsc(
                account, date, LocalDate.parse("2022-05-05"), Pageable.from(0))

        then:
        // page has all bills
        pageAll.size() == 5

        // only one page
        pageAll.getTotalPages() == 1

        // sorted by createAt (first bill is always older than second bill and second older then third etc)
        pageAll.collect().get(0).createdAt.before(pageAll.collect().get(1).createdAt)
        pageAll.collect().get(1).createdAt.before(pageAll.collect().get(2).createdAt)
        pageAll.collect().get(2).createdAt.before(pageAll.collect().get(3).createdAt)
        pageAll.collect().get(3).createdAt.before(pageAll.collect().get(4).createdAt)
    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }
}
