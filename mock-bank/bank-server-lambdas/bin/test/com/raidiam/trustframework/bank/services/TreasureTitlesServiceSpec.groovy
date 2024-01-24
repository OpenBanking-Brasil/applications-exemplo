package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.domain.ConsentEntity
import com.raidiam.trustframework.bank.domain.TreasureTitlesBalanceEntity
import com.raidiam.trustframework.bank.domain.TreasureTitlesEntity
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
class TreasureTitlesServiceSpec extends CleanupSpecification {

    @Shared
    TreasureTitlesEntity testTreasureTitles
    @Shared
    ConsentEntity testConsent
    @Shared
    TreasureTitlesBalanceEntity testTreasureTitlesBalance

    @Inject
    InvestmentService investmentService

    def setup() {
        if (runSetup) {
            testTreasureTitles = treasureTitlesRepository.save(aTreasureTitlesEntity())
            testConsent = consentRepository.save(aConsent(anAccount().getAccountId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.TREASURE_TITLES_READ, testConsent.getConsentId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.RESOURCES_READ, testConsent.getConsentId()))

            testTreasureTitlesBalance = treasureTitlesBalancesRepository.save(aTreasureTitlesBalanceEntity(testTreasureTitles))
            treasureTitlesTransactionsRepository.save(aTreasureTitlesTransactionsEntity(testTreasureTitles))
            treasureTitlesTransactionsRepository.save(aTreasureTitlesTransactionsEntity(testTreasureTitles))
            treasureTitlesTransactionsRepository.save(aTreasureTitlesTransactionsEntity(testTreasureTitles))

            runSetup = false
        }
    }

    def "We can get all treasure titles"() {
        when:
        def treasureTitlesList = investmentService.getTreasureTitlesList(Pageable.from(0), testConsent.getConsentId())

        then:
        !treasureTitlesList.getData().isEmpty()
        treasureTitlesList.getData().size() == 1
        treasureTitlesList.getData().get(0).getCompanyCnpj() == testTreasureTitles.getCompanyCnpj()
    }

    def "We can get treasure titles by investment Id"() {
        when:
        def response = investmentService
                .getTreasureTitlesById(testConsent.getConsentId(), testTreasureTitles.getInvestmentId())

        then:
        def treasureTitlesResponse = response.getData()
        treasureTitlesResponse.getIsinCode() == testTreasureTitles.getIsinCode()
        treasureTitlesResponse.getRemuneration().getPreFixedRate() ==~ /\d{1}\.\d{6}/
        treasureTitlesResponse.getRemuneration().getPostFixedIndexerPercentage() ==~ /\d{1}\.\d{6}/
        //TODO
    }

    def "We can get treasure titles Balances"() {
        when:
        def response = investmentService
                .getTreasureTitlesBalance(testConsent.getConsentId(), testTreasureTitles.getInvestmentId())

        then:
        def treasureTitlesBalanceResponse = response.getData()
        treasureTitlesBalanceResponse.getQuantity() != null
        treasureTitlesBalanceResponse.getQuantity() ==~ /\d{1,15}\.\d{2,8}/
    }

    def "We can get treasure titles transactions"() {
        when:
        def response = investmentService
                .getTreasureTitlesTransactions(testConsent.getConsentId(), testTreasureTitles.getInvestmentId(),
                        LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), Pageable.unpaged())

        then:
        response.getData().size() == 3
        response.getData().get(0).getRemunerationTransactionRate() ==~ /\d{1}\.\d{6}/
    }

    //TODO:
    void "Correct transactions self link is returned"() {

    }

    def "we cannot get a response when the consent not have permissions"() {
        setup:
        def errorMessage = "You do not have the correct permission"
        def testConsent2 = consentRepository.save(aConsent(anAccountHolder().getAccountHolderId()))

        when:
        investmentService.getTreasureTitlesList(Pageable.unpaged(), testConsent2.getConsentId())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.FORBIDDEN
        e1.getMessage() == errorMessage

        when:
        investmentService.getTreasureTitlesBalance(testConsent2.getConsentId(), UUID.randomUUID())

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.FORBIDDEN
        e2.getMessage() == errorMessage

        when:
        investmentService.getTreasureTitlesTransactions(testConsent2.getConsentId(), UUID.randomUUID(),
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
        investmentService.getTreasureTitlesList(Pageable.unpaged(), testConsent.getConsentId())

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.UNAUTHORIZED
        e.getMessage() == errorMessage

        when:
        investmentService.getTreasureTitlesById(testConsent.getConsentId(), testTreasureTitles.getInvestmentId())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.UNAUTHORIZED
        e1.getMessage() == errorMessage

        when:
        investmentService.getTreasureTitlesTransactions(testConsent.getConsentId(), testTreasureTitles.getInvestmentId(),
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
