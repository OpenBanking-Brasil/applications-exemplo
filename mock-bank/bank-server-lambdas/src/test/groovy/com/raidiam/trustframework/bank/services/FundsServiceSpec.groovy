package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.domain.ConsentEntity
import com.raidiam.trustframework.bank.domain.FundsBalanceEntity
import com.raidiam.trustframework.bank.domain.FundsEntity
import com.raidiam.trustframework.bank.repository.FundsBalancesRepository
import com.raidiam.trustframework.bank.repository.FundsRepository
import com.raidiam.trustframework.bank.repository.FundsTransactionsRepository
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
class FundsServiceSpec extends CleanupSpecification {

    @Shared
    FundsEntity testFunds
    @Shared
    ConsentEntity testConsent
    @Shared
    FundsBalanceEntity testFundsBalance

    @Inject
    InvestmentService investmentService

    def setup() {
        if (runSetup) {
            testFunds = fundsRepository.save(aFundsEntity())
            testConsent = consentRepository.save(aConsent(anAccount().getAccountId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.FUNDS_READ, testConsent.getConsentId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.RESOURCES_READ, testConsent.getConsentId()))

            testFundsBalance = fundsBalancesRepository.save(aFundsBalanceEntity(testFunds))
            fundsTransactionsRepository.save(aFundsTransactionsEntity(testFunds))
            fundsTransactionsRepository.save(aFundsTransactionsEntity(testFunds))
            fundsTransactionsRepository.save(aFundsTransactionsEntity(testFunds))

            runSetup = false
        }
    }

    def "We can get all funds"() {
        when:
        def fundsList = investmentService.getFundsList(Pageable.from(0), testConsent.getConsentId())

        then:
        !fundsList.getData().isEmpty()
        fundsList.getData().size() == 1
        fundsList.getData().get(0).getCompanyCnpj() == testFunds.getCompanyCnpj()
    }

    def "We can get funds by investment Id"() {
        when:
        def response = investmentService
                .getFundsById(testConsent.getConsentId(), testFunds.getInvestmentId())

        then:
        def fundsResponse = response.getData()
        fundsResponse.getCnpjNumber() == testFunds.getCompanyCnpj()
        //TODO
    }

    def "We can get funds Balances"() {
        when:
        def response = investmentService
                .getFundsBalance(testConsent.getConsentId(), testFunds.getInvestmentId())

        then:
        def FundsBalanceResponse = response.getData()
        FundsBalanceResponse.getQuotaQuantity() != null
    }

    def "We can get funds transactions"() {
        when:
        def response = investmentService
                .getFundsTransactions(testConsent.getConsentId(), testFunds.getInvestmentId(),
                        LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), Pageable.unpaged())

        then:
        response.getData().size() == 3
    }

    //TODO:
    void "Correct transactions self link is returned"() {

    }

    def "we cannot get a response when the consent not have permissions"() {
        setup:
        def errorMessage = "You do not have the correct permission"
        def testConsent2 = consentRepository.save(aConsent(anAccountHolder().getAccountHolderId()))

        when:
        investmentService.getFundsList(Pageable.unpaged(), testConsent2.getConsentId())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.FORBIDDEN
        e1.getMessage() == errorMessage

        when:
        investmentService.getFundsBalance(testConsent2.getConsentId(), UUID.randomUUID())

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.FORBIDDEN
        e2.getMessage() == errorMessage

        when:
        investmentService.getFundsTransactions(testConsent2.getConsentId(), UUID.randomUUID(),
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
        investmentService.getFundsList(Pageable.unpaged(), testConsent.getConsentId())

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.UNAUTHORIZED
        e.getMessage() == errorMessage

        when:
        investmentService.getFundsById(testConsent.getConsentId(), testFunds.getInvestmentId())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.UNAUTHORIZED
        e1.getMessage() == errorMessage

        when:
        investmentService.getFundsTransactions(testConsent.getConsentId(), testFunds.getInvestmentId(),
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
