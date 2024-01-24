package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.domain.ConsentEntity
import com.raidiam.trustframework.bank.domain.CreditFixedIncomesBalanceEntity
import com.raidiam.trustframework.bank.domain.CreditFixedIncomesEntity
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
class CreditFixedIncomesServiceSpec extends CleanupSpecification {

    @Shared
    CreditFixedIncomesEntity testCreditFixedIncomes
    @Shared
    ConsentEntity testConsent
    @Shared
    CreditFixedIncomesBalanceEntity testCreditFixedIncomesBalance

    @Inject
    InvestmentService investmentService

    def setup() {
        if (runSetup) {
            testCreditFixedIncomes = creditFixedIncomesRepository.save(aCreditFixedIncomesEntity())
            testConsent = consentRepository.save(aConsent(anAccount().getAccountId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.CREDIT_FIXED_INCOMES_READ, testConsent.getConsentId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.RESOURCES_READ, testConsent.getConsentId()))

            testCreditFixedIncomesBalance = creditFixedIncomesBalancesRepository.save(aCreditFixedIncomesBalanceEntity(testCreditFixedIncomes))
            creditFixedIncomesTransactionsRepository.save(aCreditFixedIncomesTransactionsEntity(testCreditFixedIncomes))
            creditFixedIncomesTransactionsRepository.save(aCreditFixedIncomesTransactionsEntity(testCreditFixedIncomes))
            creditFixedIncomesTransactionsRepository.save(aCreditFixedIncomesTransactionsEntity(testCreditFixedIncomes))

            runSetup = false
        }
    }

    def "We can get all credit fixed incomes"() {
        when:
        def creditFixedIncomesList = investmentService.getCreditFixedIncomesList(Pageable.from(0), testConsent.getConsentId())

        then:
        !creditFixedIncomesList.getData().isEmpty()
        creditFixedIncomesList.getData().size() == 1
        creditFixedIncomesList.getData().get(0).getCompanyCnpj() == testCreditFixedIncomes.getCompanyCnpj()
    }

    def "We can get credit fixed incomes by investment Id"() {
        when:
        def response = investmentService
                .getCreditFixedIncomesById(testConsent.getConsentId(), testCreditFixedIncomes.getInvestmentId())

        then:
        def creditFixedIncomesResponse = response.getData()
        creditFixedIncomesResponse.getIssuerInstitutionCnpjNumber() == testCreditFixedIncomes.getCompanyCnpj()
        creditFixedIncomesResponse.getRemuneration().getPreFixedRate() ==~ /\d{1}\.\d{6}/
        creditFixedIncomesResponse.getRemuneration().getPostFixedIndexerPercentage() ==~ /\d{1}\.\d{6}/
        //TODO
    }

    def "We can get credit fixed incomes Balances"() {
        when:
        def response = investmentService
                .getCreditFixedIncomesBalance(testConsent.getConsentId(), testCreditFixedIncomes.getInvestmentId())

        then:
        def creditFixedIncomesBalanceResponse = response.getData()
        creditFixedIncomesBalanceResponse.getQuantity() != null
        creditFixedIncomesBalanceResponse.getQuantity() ==~ /\d{1,15}\.\d{2,8}/
        creditFixedIncomesBalanceResponse.getPreFixedRate() ==~ /\d{1}\.\d{6}/
        creditFixedIncomesBalanceResponse.getPostFixedIndexerPercentage() ==~ /\d{1}\.\d{6}/
    }

    def "We can get credit fixed incomes transactions"() {
        when:
        def response = investmentService
                .getCreditFixedIncomesTransactions(testConsent.getConsentId(), testCreditFixedIncomes.getInvestmentId(),
                        LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), Pageable.unpaged())

        then:
        response.getData().size() == 3
        response.getData().get(0).getRemunerationTransactionRate() ==~ /\d{1}\.\d{6}/
        response.getData().get(0).getIndexerPercentage() ==~ /\d{1}\.\d{6}/
    }

    //TODO:
    void "Correct transactions self link is returned"() {

    }

    def "we cannot get a response when the consent not have permissions"() {
        setup:
        def errorMessage = "You do not have the correct permission"
        def testConsent2 = consentRepository.save(aConsent(anAccountHolder().getAccountHolderId()))

        when:
        investmentService.getCreditFixedIncomesList(Pageable.unpaged(), testConsent2.getConsentId())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.FORBIDDEN
        e1.getMessage() == errorMessage

        when:
        investmentService.getCreditFixedIncomesBalance(testConsent2.getConsentId(), UUID.randomUUID())

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.FORBIDDEN
        e2.getMessage() == errorMessage

        when:
        investmentService.getCreditFixedIncomesTransactions(testConsent2.getConsentId(), UUID.randomUUID(),
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
        investmentService.getCreditFixedIncomesList(Pageable.unpaged(), testConsent.getConsentId())

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.UNAUTHORIZED
        e.getMessage() == errorMessage

        when:
        investmentService.getCreditFixedIncomesById(testConsent.getConsentId(), testCreditFixedIncomes.getInvestmentId())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.UNAUTHORIZED
        e1.getMessage() == errorMessage

        when:
        investmentService.getCreditFixedIncomesTransactions(testConsent.getConsentId(), testCreditFixedIncomes.getInvestmentId(),
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
