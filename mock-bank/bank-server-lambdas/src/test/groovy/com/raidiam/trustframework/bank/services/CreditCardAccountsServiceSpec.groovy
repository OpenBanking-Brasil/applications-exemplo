package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.domain.*
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.data.model.Pageable
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Stepwise

import javax.inject.Inject
import java.time.LocalDate
import java.time.OffsetDateTime

import static com.raidiam.trustframework.bank.TestEntityDataFactory.*

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class CreditCardAccountsServiceSpec extends CleanupSpecification {

    @Shared
    AccountHolderEntity testAccountHolder
    @Shared
    AccountHolderEntity testAccountHolder2
    @Shared
    ConsentEntity testConsent
    @Shared
    ConsentEntity testConsentWrong
    @Shared
    CreditCardAccountsEntity testAccount
    @Shared
    CreditCardAccountsEntity wrongTestAccount
    @Shared
    CreditCardAccountsBillsEntity wrongAccountBill
    @Shared
    CreditCardAccountsLimitsEntity testAccountLimits
    @Shared
    CreditCardsAccountPaymentMethodEntity testAccountPayment
    @Shared
    CreditCardAccountsBillsEntity testAccountBill
    @Shared
    CreditCardAccountsBillsFinanceChargeEntity testBillsFinanceCharge
    @Shared
    CreditCardAccountsBillsPaymentEntity testBillPayments
    @Shared
    CreditCardAccountsTransactionEntity testAccountTransaction

    @Inject
    CreditCardAccountsService creditCardAccountsService

    def setup() {
        if (runSetup) {
            testAccountHolder = accountHolderRepository.save(anAccountHolder("96644087000", "CPF"))
            testConsent = consentRepository.save(aConsent(testAccountHolder.getAccountHolderId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.CREDIT_CARDS_ACCOUNTS_READ, testConsent.getConsentId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.CREDIT_CARDS_ACCOUNTS_BILLS_READ, testConsent.getConsentId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.CREDIT_CARDS_ACCOUNTS_BILLS_TRANSACTIONS_READ, testConsent.getConsentId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.CREDIT_CARDS_ACCOUNTS_LIMITS_READ, testConsent.getConsentId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.CREDIT_CARDS_ACCOUNTS_TRANSACTIONS_READ, testConsent.getConsentId()))
            testAccount = creditCardAccountsRepository.save(anCreditCardAccounts(testAccountHolder.getAccountHolderId()))
            consentCreditCardAccountsRepository.save(new ConsentCreditCardAccountsEntity(testConsent, testAccount))
            testAccountPayment = creditCardsAccountPaymentMethodRepository.save(anCreditCardsAccountPaymentMethod(testAccount))
            testAccountLimits = creditCardAccountsLimitsRepository.save(anCreditCardAccountsLimits(testAccount.creditCardAccountId))
            testAccountBill = creditCardAccountsBillsRepository.save(anCreditCardAccountsBill(testAccount))
            testBillsFinanceCharge = creditCardAccountsBillsFinanceChargeRepository.save(anCreditCardAccountsBillsFinanceCharge(testAccountBill))
            testBillPayments = creditCardAccountsBillsPaymentRepository.save(anCreditCardAccountsBillsPayment(testAccountBill))

            testAccountTransaction = creditCardAccountsTransactionRepository.save(anCreditCardAccountsTransaction(testAccountBill.billId,
                    testAccount.getCreditCardAccountId(), new BigDecimal(5912), EnumCreditCardTransactionType.PAGAMENTO.name()))
            creditCardAccountsTransactionRepository.save(anCreditCardAccountsTransaction(testAccountBill.billId,
                    testAccount.getCreditCardAccountId(), new BigDecimal(6000), EnumCreditCardTransactionType.PAGAMENTO.name()))
            creditCardAccountsTransactionRepository.save(anCreditCardAccountsTransaction(testAccountBill.billId,
                    testAccount.getCreditCardAccountId(), new BigDecimal(6000), EnumCreditCardTransactionType.OUTROS.name()))


            testAccountHolder2 = accountHolderRepository.save(anAccountHolder("10117409073", "CPF"))
            testConsentWrong = consentRepository.save(aConsent(testAccountHolder2.getAccountHolderId()))
            wrongTestAccount = creditCardAccountsRepository.save(anCreditCardAccounts(testAccountHolder2.getAccountHolderId()))
            wrongAccountBill = creditCardAccountsBillsRepository.save(anCreditCardAccountsBill(wrongTestAccount))
            consentCreditCardAccountsRepository.save(new ConsentCreditCardAccountsEntity(testConsentWrong, testAccount))
            runSetup = false
        }
    }

    def "We can get all credit card accounts provided in Consent"() {
        when:
        def accounts = creditCardAccountsService.getCreditCardAccounts(Pageable.from(0), testConsent.getConsentId())

        then:
        !accounts.getData().isEmpty()
        //provided only 1 Account in Consent
        accounts.getData().size() == 1
        accounts.getData().get(0).getCreditCardAccountId() == testAccount.getCreditCardAccountId().toString()
    }

    def "We can get credit card account"() {
        when:
        def response = creditCardAccountsService
                .getCreditCardAccount(testConsent.getConsentId(), testAccount.getCreditCardAccountId().toString())

        then:
        def accountIdentification = response.getData()
        accountIdentification.getName() == testAccount.getName()
        accountIdentification.getProductType().toString() == testAccount.getProductType()
        accountIdentification.getProductAdditionalInfo().toString() == testAccount.getProductAdditionalInfo()
        accountIdentification.getCreditCardNetwork().toString() == testAccount.getCreditCardNetwork()
        accountIdentification.getNetworkAdditionalInfo() == testAccount.getNetworkAdditionalInfo()
        def paymentMethod = accountIdentification.getPaymentMethod().first()
        paymentMethod.getIdentificationNumber() == testAccountPayment.getIdentificationNumber()
        paymentMethod.isMultipleCreditCard(testAccountPayment.isMultipleCreditCard())
    }

    def "We can get credit card account bills"() {
        when:
        def response = creditCardAccountsService
                .getCreditCardAccountsBills(Pageable.from(0), testConsent.getConsentId(),
                        LocalDate.now(), LocalDate.now(), testAccount.getCreditCardAccountId().toString())

        then:
        def accountBill = response.getData().first()
        accountBill.getBillId() == testAccountBill.getBillId().toString()
        accountBill.getDueDate() == testAccountBill.getDueDate()
        accountBill.getBillTotalAmount() == testAccountBill.getBillTotalAmount()
        accountBill.getBillTotalAmountCurrency() == testAccountBill.getBillTotalAmountCurrency()
        accountBill.getBillMinimumAmount() == testAccountBill.getBillMinimumAmount()
        accountBill.getBillMinimumAmountCurrency() == testAccountBill.getBillMinimumAmountCurrency()
        accountBill.isInstalment(testAccountBill.isInstalment())
        def financeCharges = accountBill.getFinanceCharges().first()
        financeCharges.getType() == EnumCreditCardAccountsFinanceChargeType.valueOf(testBillsFinanceCharge.getType())
        financeCharges.getAdditionalInfo() == testBillsFinanceCharge.getAdditionalInfo()
        financeCharges.getAmount() == testBillsFinanceCharge.getAmount()
        financeCharges.getCurrency() == testBillsFinanceCharge.getCurrency()
        def payments = accountBill.getPayments().first()
        payments.getValueType() == EnumCreditCardAccountsBillingValueType.valueOf(testBillPayments.getValueType())
        payments.getPaymentDate() == testBillPayments.getPaymentDate()
        payments.getPaymentMode() == EnumCreditCardAccountsPaymentMode.valueOf(testBillPayments.getPaymentMode())
        payments.getAmount() == testBillPayments.getAmount()
        payments.getCurrency() == testBillPayments.getCurrency()
    }

    def "We can get credit card account bills transactions"() {
        when://with payeeMCC and transactionType
        def response = creditCardAccountsService
                .getBillsTransactionsV2(Pageable.from(0), testConsent.getConsentId(),
                        LocalDate.now(), LocalDate.now(), new BigDecimal(5912), "PAGAMENTO",
                        testAccount.getCreditCardAccountId().toString(),
                        testAccountBill.getBillId().toString())

        then:
        def billTransaction = response.getData().first()
        billTransaction.getTransactionId() == testAccountTransaction.getTransactionId().toString()
        billTransaction.getIdentificationNumber() == testAccountTransaction.getIdentificationNumber()
        billTransaction.getTransactionName() == testAccountTransaction.getTransactionName()
        billTransaction.getBillId() == testAccountBill.getBillId().toString()
        billTransaction.getCreditDebitType() == EnumCreditDebitIndicatorV2.valueOf(testAccountTransaction.getCreditDebitType())
        billTransaction.getTransactionType() == EnumCreditCardTransactionTypeV2.valueOf(testAccountTransaction.getTransactionType())
        billTransaction.getTransactionalAdditionalInfo() == testAccountTransaction.getTransactionalAdditionalInfo()
        billTransaction.getPaymentType() == EnumCreditCardAccountsPaymentTypeV2.valueOf(testAccountTransaction.getPaymentType())
        billTransaction.getFeeType() == EnumCreditCardAccountFeeV2.valueOf(testAccountTransaction.getFeeType())
        billTransaction.getFeeTypeAdditionalInfo() == testAccountTransaction.getFeeTypeAdditionalInfo()
        billTransaction.getOtherCreditsType() == EnumCreditCardAccountsOtherCreditTypeV2.valueOf(testAccountTransaction.getOtherCreditsType())
        billTransaction.getOtherCreditsAdditionalInfo() == testAccountTransaction.getOtherCreditsAdditionalInfo()
        billTransaction.getChargeIdentificator().toString() == testAccountTransaction.getChargeIdentificator()
        billTransaction.getChargeNumber() == testAccountTransaction.getChargeNumber()
        billTransaction.getBillPostDate() == testAccountTransaction.getBillPostDate()
        billTransaction.getPayeeMCC() == testAccountTransaction.getPayeeMCC()

        when: //with out payeeMCC and transactionType
        def response2 = creditCardAccountsService
                .getBillsTransactionsV2(Pageable.from(0), testConsent.getConsentId(),
                        LocalDate.now(), LocalDate.now(), null, null,
                        testAccount.getCreditCardAccountId().toString(), testAccountBill.billId.toString())

        then:
        response2.getData().size() == 3

        when://with payeeMCC = 5912
        def response3 = creditCardAccountsService.getBillsTransactionsV2(Pageable.from(0), testConsent.getConsentId(),
                LocalDate.now(), LocalDate.now(), new BigDecimal(5912), null,
                testAccount.getCreditCardAccountId().toString(), testAccountBill.billId.toString())

        then:
        response3.getData().size() == 1
        response3.getData().get(0).payeeMCC == 5912

        when: //with transactionType = OUTROS
        def response4 = creditCardAccountsService.getBillsTransactionsV2(Pageable.from(0), testConsent.getConsentId(),
                LocalDate.now(), LocalDate.now(), null, "OUTROS",
                testAccount.getCreditCardAccountId().toString(), testAccountBill.billId.toString())
        then:
        response4.getData().size() == 1
        response4.getData().get(0).transactionType.name() == "OUTROS"
    }

    def "We can not get credit card account bills transactions with billsId from another account"() {
        when:
        creditCardAccountsService
                .getBillsTransactionsV2(Pageable.from(0), testConsent.getConsentId(),
                        LocalDate.now(), LocalDate.now(), null, null,
                        testAccount.getCreditCardAccountId().toString(),  wrongAccountBill.getBillId().toString())

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.BAD_REQUEST
        e.getMessage() == "Bad request, bill does not owned credit card account!"
    }

    def "We can get credit card account limits"() {
        when:
        def response = creditCardAccountsService
                .getCreditCardAccountLimits(testConsent.getConsentId(), testAccount.getCreditCardAccountId().toString())

        then:
        def accountLimits = response.getData().first()
        accountLimits.getCreditLineLimitType() == EnumCreditCardAccountsLineLimitType.valueOf(testAccountLimits.getCreditLineLimitType())
        accountLimits.getConsolidationType() == EnumCreditCardAccountsConsolidationType.valueOf(testAccountLimits.getConsolidationType())
        accountLimits.getIdentificationNumber() == testAccountLimits.getIdentificationNumber()
        accountLimits.getLineName() == EnumCreditCardAccountsLineName.valueOf(testAccountLimits.getLineName())
        accountLimits.getLineNameAdditionalInfo() == testAccountLimits.getLineNameAdditionalInfo()
        accountLimits.isLimitFlexible(testAccountLimits.isLimitFlexible())
        accountLimits.getLimitAmountCurrency() == testAccountLimits.getLimitAmountCurrency()
        accountLimits.getLimitAmount() == testAccountLimits.getLimitAmount()
        accountLimits.getUsedAmountCurrency() == testAccountLimits.getUsedAmountCurrency()
        accountLimits.getUsedAmount() == testAccountLimits.getUsedAmount()
        accountLimits.getAvailableAmountCurrency() == testAccountLimits.getAvailableAmountCurrency()
        accountLimits.getAvailableAmount() == testAccountLimits.getAvailableAmount()
    }

    def "We can get credit card account transactions"() {
        when: //with out payeeMCC and transactionType
        def response = creditCardAccountsService
                .getTransactionsV2(Pageable.from(0), testConsent.getConsentId(),
                        LocalDate.now(), LocalDate.now(), null, null, testAccount.getCreditCardAccountId().toString())

        then:
        response.getData().size() == 3

        when://with payeeMCC = 5912
        def response2 = creditCardAccountsService
                .getTransactionsV2(Pageable.from(0), testConsent.getConsentId(),
                        LocalDate.now(), LocalDate.now(), BigDecimal.valueOf(5912), null, testAccount.getCreditCardAccountId().toString())

        then:
        response2.getData().size() == 1
        response2.getData().get(0).payeeMCC == 5912

        when: //with transactionType = OUTROS
        def response3 = creditCardAccountsService
                .getTransactionsV2(Pageable.from(0), testConsent.getConsentId(),
                        LocalDate.now(), LocalDate.now(), null, "OUTROS", testAccount.getCreditCardAccountId().toString())

        then:
        response3.getData().size() == 1
        response3.getData().get(0).transactionType.name() == "OUTROS"

        when: //with payeeMCC and transactionType
        def response4 = creditCardAccountsService
                .getTransactionsV2(Pageable.from(0), testConsent.getConsentId(),
                        LocalDate.now(), LocalDate.now(), BigDecimal.valueOf(6000), "OUTROS", testAccount.getCreditCardAccountId().toString())

        then:
        response4.getData().size() == 1
        response4.getData().get(0).payeeMCC == 6000
        response4.getData().get(0).transactionType.name() == "OUTROS"
    }

    //TODO:
    void "Correct transactions self link is returned"() {

    }

    def "we can get pages"() {
        given:
        var pageSize = 2
        consentCreditCardAccountsRepository.save(new ConsentCreditCardAccountsEntity(testConsent, creditCardAccountsRepository.save(anCreditCardAccounts(testAccountHolder.getAccountHolderId()))))
        consentCreditCardAccountsRepository.save(new ConsentCreditCardAccountsEntity(testConsent, creditCardAccountsRepository.save(anCreditCardAccounts(testAccountHolder.getAccountHolderId()))))
        println consentCreditCardAccountsRepository.findAll().size()

        when:
        //get first page
        def page1 = creditCardAccountsService.getCreditCardAccounts(Pageable.from(0, pageSize), testConsent.getConsentId())
        def page1Size = page1.getData().size()

        then:
        !page1.getData().empty
        page1.getMeta().getTotalPages() == pageSize

        when:
        //get second page
        def page2 = creditCardAccountsService.getCreditCardAccounts(Pageable.from(1, pageSize), testConsent.getConsentId())
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
        def testAccount2 = creditCardAccountsRepository.save(anCreditCardAccounts(testAccountHolder2.getAccountHolderId()))
        def testConsent2 = consentRepository.save(aConsent(testAccountHolder2.getAccountHolderId()))
        consentCreditCardAccountsRepository.save(new ConsentCreditCardAccountsEntity(testConsent2, testAccount2))
        def testAccountBill2 = creditCardAccountsBillsRepository.save(anCreditCardAccountsBill(testAccount2))

        when:
        creditCardAccountsService.getCreditCardAccounts(Pageable.unpaged(), testConsent2.getConsentId())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.FORBIDDEN
        e1.getMessage() == errorMessage

        when:
        creditCardAccountsService.getCreditCardAccount(testConsent2.getConsentId(), testAccount2.getCreditCardAccountId().toString())

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.FORBIDDEN
        e2.getMessage() == errorMessage

        when:
        creditCardAccountsService.getTransactionsV2(Pageable.unpaged(), testConsent2.getConsentId(),
                LocalDate.now(), LocalDate.now(), null, null, testAccount2.getCreditCardAccountId().toString())

        then:
        HttpStatusException e3 = thrown()
        e3.status == HttpStatus.FORBIDDEN
        e3.getMessage() == errorMessage

        when:
        creditCardAccountsService.getCreditCardAccountLimits(testConsent2.getConsentId(), testAccount2.getCreditCardAccountId().toString())

        then:
        HttpStatusException e4 = thrown()
        e4.status == HttpStatus.FORBIDDEN
        e4.getMessage() == errorMessage

        when:
        creditCardAccountsService.getCreditCardAccountsBills(Pageable.unpaged(), testConsent2.getConsentId(),
                null, null, testAccount2.getCreditCardAccountId().toString())

        then:
        HttpStatusException e5 = thrown()
        e5.status == HttpStatus.FORBIDDEN
        e5.getMessage() == errorMessage

        when:
        creditCardAccountsService.getBillsTransactionsV2(Pageable.unpaged(), testConsent2.getConsentId(),
                LocalDate.now(), LocalDate.now(), null, null, testAccount2.getCreditCardAccountId().toString(),
                testAccountBill2.getBillId().toString())

        then:
        HttpStatusException e6 = thrown()
        e6.status == HttpStatus.FORBIDDEN
        e6.getMessage() == errorMessage
    }

    def "we cannot get a response when the consent owner is not the credit card account owner"() {
        setup:
        def errorMessage = "Forbidden, consent owner does not match credit card account owner!"
        def testAccountHolder2 = accountHolderRepository.save(anAccountHolder())
        def testAccount2 = creditCardAccountsRepository.save(anCreditCardAccounts(testAccountHolder2.getAccountHolderId()))
        consentCreditCardAccountsRepository.save(new ConsentCreditCardAccountsEntity(testConsent, testAccount2))
        def testAccountBill2 = creditCardAccountsBillsRepository.save(anCreditCardAccountsBill(testAccount2))

        when:
        creditCardAccountsService.getCreditCardAccounts(Pageable.unpaged(), testConsent.getConsentId().toString())

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.FORBIDDEN
        e.getMessage() == errorMessage

        when:
        creditCardAccountsService.getCreditCardAccount(testConsent.getConsentId(), testAccount2.getCreditCardAccountId().toString())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.FORBIDDEN
        e1.getMessage() == errorMessage

        when:
        creditCardAccountsService.getTransactionsV2(Pageable.unpaged(), testConsent.getConsentId(),
                LocalDate.now(), LocalDate.now(), null, null, testAccount2.getCreditCardAccountId().toString())

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.FORBIDDEN
        e2.getMessage() == errorMessage

        when:
        creditCardAccountsService.getCreditCardAccountLimits(testConsent.getConsentId(), testAccount2.getCreditCardAccountId().toString())

        then:
        HttpStatusException e3 = thrown()
        e3.status == HttpStatus.FORBIDDEN
        e3.getMessage() == errorMessage

        when:
        creditCardAccountsService.getCreditCardAccountsBills(Pageable.unpaged(), testConsent.getConsentId(),
                null, null, testAccount2.getCreditCardAccountId().toString())

        then:
        HttpStatusException e4 = thrown()
        e4.status == HttpStatus.FORBIDDEN
        e4.getMessage() == errorMessage

        when:
        creditCardAccountsService.getBillsTransactionsV2(Pageable.unpaged(), testConsent.getConsentId(),
                LocalDate.now(), LocalDate.now(), null, null,
                testAccount2.getCreditCardAccountId().toString(),
                testAccountBill2.getBillId().toString())

        then:
        HttpStatusException e5 = thrown()
        e5.status == HttpStatus.FORBIDDEN
        e5.getMessage() == errorMessage
    }

    def "we cannot get response when consent does not cover account"() {
        setup:
        def errorMessage = "Bad request, consent does not cover this credit card account!"
        def testAccountHolder2 = accountHolderRepository.save(anAccountHolder())
        def testAccount2 = creditCardAccountsRepository.save(anCreditCardAccounts(testAccountHolder2.getAccountHolderId()))
        def testAccountBill2 = creditCardAccountsBillsRepository.save(anCreditCardAccountsBill(testAccount2))

        when:
        creditCardAccountsService.getCreditCardAccount(testConsent.getConsentId(), testAccount2.getCreditCardAccountId().toString())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.BAD_REQUEST
        e1.getMessage() == errorMessage

        when:
        creditCardAccountsService.getTransactionsV2(Pageable.unpaged(), testConsent.getConsentId(),
                LocalDate.now(), LocalDate.now(), null, null, testAccount2.getCreditCardAccountId().toString())

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.BAD_REQUEST
        e2.getMessage() == errorMessage

        when:
        creditCardAccountsService.getCreditCardAccountLimits(testConsent.getConsentId(), testAccount2.getCreditCardAccountId().toString())

        then:
        HttpStatusException e3 = thrown()
        e3.status == HttpStatus.BAD_REQUEST
        e3.getMessage() == errorMessage

        when:
        creditCardAccountsService.getCreditCardAccountsBills(Pageable.unpaged(), testConsent.getConsentId(),
                 null, null, testAccount2.getCreditCardAccountId().toString())

        then:
        HttpStatusException e4 = thrown()
        e4.status == HttpStatus.BAD_REQUEST
        e4.getMessage() == errorMessage

        when:
        creditCardAccountsService.getTransactionsV2(Pageable.unpaged(), testConsent.getConsentId(),
               LocalDate.now(), LocalDate.now(), null, null,
                testAccount2.getCreditCardAccountId().toString())

        then:
        HttpStatusException e5 = thrown()
        e5.status == HttpStatus.BAD_REQUEST
        e5.getMessage() == errorMessage
    }

    def "we cannot get response without authorised status"() {
        setup:
        def errorMessage = "Bad request, consent not Authorised!"
        testConsent.setStatus(EnumConsentStatus.AWAITING_AUTHORISATION.name())
        consentRepository.update(testConsent)

        when:
        creditCardAccountsService.getCreditCardAccounts(Pageable.unpaged(), testConsent.getConsentId())

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.UNAUTHORIZED
        e.getMessage() == errorMessage

        when:
        creditCardAccountsService.getCreditCardAccount(testConsent.getConsentId(), testAccount.getCreditCardAccountId().toString())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.UNAUTHORIZED
        e1.getMessage() == errorMessage

        when:
        creditCardAccountsService.getTransactionsV2(Pageable.unpaged(),testConsent.getConsentId(),
                LocalDate.now(), LocalDate.now(), null, null, testAccount.getCreditCardAccountId().toString())

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.UNAUTHORIZED
        e2.getMessage() == errorMessage

        when:
        creditCardAccountsService.getBillsTransactionsV2(Pageable.unpaged(), testConsent.getConsentId(),
                LocalDate.now(), LocalDate.now(), null, null,
                testAccount.getCreditCardAccountId().toString(),testAccountBill.getBillId().toString())

        then:
        HttpStatusException e3 = thrown()
        e3.status == HttpStatus.UNAUTHORIZED
        e3.getMessage() == errorMessage

        when:
        creditCardAccountsService.getCreditCardAccountsBills(Pageable.unpaged(), testConsent.getConsentId(),
                null , null, testAccount.getCreditCardAccountId().toString())

        then:
        HttpStatusException e4 = thrown()
        e4.status == HttpStatus.UNAUTHORIZED
        e4.getMessage() == errorMessage

        when:
        creditCardAccountsService.getCreditCardAccountLimits(
                testConsent.getConsentId(), testAccount.getCreditCardAccountId().toString())

        then:
        HttpStatusException e5 = thrown()
        e5.status == HttpStatus.UNAUTHORIZED
        e5.getMessage() == errorMessage
    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }
}
