package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.domain.BankFixedIncomesBalanceEntity
import com.raidiam.trustframework.bank.domain.BankFixedIncomesEntity
import com.raidiam.trustframework.bank.domain.ConsentEntity
import com.raidiam.trustframework.mockbank.models.generated.EnumConsentPermissions
import com.raidiam.trustframework.mockbank.models.generated.EnumConsentStatus
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
class BankFixedIncomesServiceSpec extends CleanupSpecification {

    @Shared
    BankFixedIncomesEntity testBankFixedIncomes
    @Shared
    ConsentEntity testConsent
    @Shared
    BankFixedIncomesBalanceEntity testBankFixedIncomesBalance

    @Inject
    InvestmentService investmentService

    def setup() {
        if (runSetup) {
            testBankFixedIncomes = bankFixedIncomesRepository.save(aBankFixedIncomesEntity())
            testConsent = consentRepository.save(aConsent(anAccount().getAccountId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.BANK_FIXED_INCOMES_READ, testConsent.getConsentId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.RESOURCES_READ, testConsent.getConsentId()))

            testBankFixedIncomesBalance = bankFixedIncomesBalancesRepository.save(aBankFixedIncomesBalanceEntity(testBankFixedIncomes))
            bankFixedIncomesTransactionsRepository.save(aBankFixedIncomesTransactionsEntity(testBankFixedIncomes))
            bankFixedIncomesTransactionsRepository.save(aBankFixedIncomesTransactionsEntity(testBankFixedIncomes))
            bankFixedIncomesTransactionsRepository.save(aBankFixedIncomesTransactionsEntity(testBankFixedIncomes))

            runSetup = false
        }
    }

    def "We can get all bank fixed incomes"() {
        when:
        def bankFixedIncomesList = investmentService.getBankFixedIncomesList(Pageable.from(0), testConsent.getConsentId())

        then:
        !bankFixedIncomesList.getData().isEmpty()
        bankFixedIncomesList.getData().size() == 1
        bankFixedIncomesList.getData().get(0).getCompanyCnpj() == testBankFixedIncomes.getCompanyCnpj()
    }

    def "We can get bank fixed incomes by investment Id"() {
        when:
        def response = investmentService
                .getBankFixedIncomesById(testConsent.getConsentId(), testBankFixedIncomes.getInvestmentId())

        then:
        def bankFixedIncomesResponse = response.getData()
        bankFixedIncomesResponse.getIssuerInstitutionCnpjNumber() == testBankFixedIncomes.getCompanyCnpj()
        bankFixedIncomesResponse.getRemuneration().getPreFixedRate() ==~ /\d{1}\.\d{6}/
        bankFixedIncomesResponse.getRemuneration().getPostFixedIndexerPercentage() ==~ /\d{1}\.\d{6}/
    }

    def "We can get bank fixed incomes Balances"() {
        when:
        def response = investmentService
                .getBankFixedIncomesBalance(testConsent.getConsentId(), testBankFixedIncomes.getInvestmentId())

        then:
        def bankFixedIncomesBalanceResponse = response.getData()
        bankFixedIncomesBalanceResponse.getQuantity() != null
        bankFixedIncomesBalanceResponse.getQuantity() ==~ /\d{1,15}\.\d{2,8}/
        bankFixedIncomesBalanceResponse.getPreFixedRate() ==~ /\d{1}\.\d{6}/
        bankFixedIncomesBalanceResponse.getPostFixedIndexerPercentage() ==~ /\d{1}\.\d{6}/
    }

    def "We can get bank fixed incomes transactions"() {
        when:
        def response = investmentService
                .getBankFixedIncomesTransactions(testConsent.getConsentId(), testBankFixedIncomes.getInvestmentId(),
                        LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), Pageable.unpaged())

        then:
        response.getData().size() == 3
        response.getData().get(0).getRemunerationTransactionRate() ==~ /\d{1}\.\d{6}/
        response.getData().get(0).getIndexerPercentage() ==~ /\d{1}\.\d{6}/
    }

    def "we cannot get a response when the consent not have permissions"() {
        setup:
        def errorMessage = "You do not have the correct permission"
        def testConsent2 = consentRepository.save(aConsent(anAccountHolder().getAccountHolderId()))

        when:
        investmentService.getBankFixedIncomesList(Pageable.unpaged(), testConsent2.getConsentId())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.FORBIDDEN
        e1.getMessage() == errorMessage

        when:
        investmentService.getBankFixedIncomesBalance(testConsent2.getConsentId(), UUID.randomUUID())

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.FORBIDDEN
        e2.getMessage() == errorMessage

        when:
        investmentService.getBankFixedIncomesTransactions(testConsent2.getConsentId(), UUID.randomUUID(),
                LocalDate.now(), LocalDate.now(), Pageable.unpaged())

        then:
        HttpStatusException e3 = thrown()
        e3.status == HttpStatus.FORBIDDEN
        e3.getMessage() == errorMessage
    }

    def "we cannot get response without authorised status"() {
        setup:
        def errorMessage = "Bad request, consent not Authorised!"
        testConsent.setStatus(EnumConsentStatus.AWAITING_AUTHORISATION.name())
        consentRepository.update(testConsent)

        when:
        investmentService.getBankFixedIncomesList(Pageable.unpaged(), testConsent.getConsentId())

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.UNAUTHORIZED
        e.getMessage() == errorMessage

        when:
        investmentService.getBankFixedIncomesById(testConsent.getConsentId(), testBankFixedIncomes.getInvestmentId())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.UNAUTHORIZED
        e1.getMessage() == errorMessage

        when:
        investmentService.getBankFixedIncomesTransactions(testConsent.getConsentId(), testBankFixedIncomes.getInvestmentId(),
                LocalDate.now(), LocalDate.now(), Pageable.unpaged())

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.UNAUTHORIZED
        e2.getMessage() == errorMessage
    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }
}
