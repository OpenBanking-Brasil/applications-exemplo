package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.domain.ConsentEntity
import com.raidiam.trustframework.bank.domain.VariableIncomesBalanceEntity
import com.raidiam.trustframework.bank.domain.VariableIncomesBrokerNotesEntity
import com.raidiam.trustframework.bank.domain.VariableIncomesEntity
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
class VariableIncomesServiceSpec extends CleanupSpecification {

    @Shared
    VariableIncomesEntity testVariableIncomes
    @Shared
    ConsentEntity testConsent
    @Shared
    VariableIncomesBalanceEntity testVariableIncomesBalance
    @Shared
    VariableIncomesBrokerNotesEntity testVariableIncomesBrokerNotes

    @Inject
    InvestmentService investmentService

    def setup() {
        if (runSetup) {
            testVariableIncomes = variableIncomesRepository.save(aVariableIncomesEntity())
            testConsent = consentRepository.save(aConsent(anAccount().getAccountId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.VARIABLE_INCOMES_READ, testConsent.getConsentId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.RESOURCES_READ, testConsent.getConsentId()))

            testVariableIncomesBalance = variableIncomesBalancesRepository.save(aVariableIncomesBalanceEntity(testVariableIncomes))
            testVariableIncomesBrokerNotes = variableIncomesBrokerNotesRepository.save(aVariableIncomesBrokerNotesEntity())
            variableIncomesTransactionsRepository.save(aVariableIncomesTransactionsEntity(testVariableIncomes, testVariableIncomesBrokerNotes.brokerNoteId))
            variableIncomesTransactionsRepository.save(aVariableIncomesTransactionsEntity(testVariableIncomes, testVariableIncomesBrokerNotes.brokerNoteId))
            variableIncomesTransactionsRepository.save(aVariableIncomesTransactionsEntity(testVariableIncomes, testVariableIncomesBrokerNotes.brokerNoteId))

            runSetup = false
        }
    }

    def "We can get all variable incomes"() {
        when:
        def variableIncomesList = investmentService.getVariableIncomesList(Pageable.from(0), testConsent.getConsentId())

        then:
        !variableIncomesList.getData().isEmpty()
        variableIncomesList.getData().size() == 1
        variableIncomesList.getData().get(0).getCompanyCnpj() == testVariableIncomes.getCompanyCnpj()
    }

    def "We can get variable incomes by investment Id"() {
        when:
        def response = investmentService
                .getVariableIncomesById(testConsent.getConsentId(), testVariableIncomes.getInvestmentId())

        then:
        def variableIncomesResponse = response.getData()
        variableIncomesResponse.getIssuerInstitutionCnpjNumber() == testVariableIncomes.getCompanyCnpj()
        //TODO
    }

    def "We can get variable incomes Balances"() {
        when:
        def response = investmentService
                .getVariableIncomesBalance(testConsent.getConsentId(), testVariableIncomes.getInvestmentId())

        then:
        def variableIncomesBalanceResponse = response.getData().get(0)
        variableIncomesBalanceResponse.getQuantity() != null
        variableIncomesBalanceResponse.getQuantity() ==~ /\d{1,15}\.\d{2,8}/
    }

    def "We can get variable incomes transactions"() {
        when:
        def response = investmentService
                .getVariableIncomesTransactions(testConsent.getConsentId(), testVariableIncomes.getInvestmentId(),
                        LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), Pageable.unpaged())

        then:
        response.getData().size() == 3
    }

    def "We can get variable incomes broker notes"() {
        when:
        def response = investmentService
                .getVariableIncomesBroker(testConsent.getConsentId(), testVariableIncomesBrokerNotes.getBrokerNoteId())

        then:
        def variableIncomesBrokerNotes = response.getData()
        variableIncomesBrokerNotes.getBrokerNoteNumber() != null
    }

    //TODO:
    void "Correct transactions self link is returned"() {

    }

    def "we cannot get a response when the consent not have permissions"() {
        setup:
        def errorMessage = "You do not have the correct permission"
        def testConsent2 = consentRepository.save(aConsent(anAccountHolder().getAccountHolderId()))

        when:
        investmentService.getVariableIncomesList(Pageable.unpaged(), testConsent2.getConsentId())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.FORBIDDEN
        e1.getMessage() == errorMessage

        when:
        investmentService.getVariableIncomesBalance(testConsent2.getConsentId(), UUID.randomUUID())

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.FORBIDDEN
        e2.getMessage() == errorMessage

        when:
        investmentService.getVariableIncomesTransactions(testConsent2.getConsentId(), UUID.randomUUID(),
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
        investmentService.getVariableIncomesList(Pageable.unpaged(), testConsent.getConsentId())

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.UNAUTHORIZED
        e.getMessage() == errorMessage

        when:
        investmentService.getVariableIncomesById(testConsent.getConsentId(), testVariableIncomes.getInvestmentId())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.UNAUTHORIZED
        e1.getMessage() == errorMessage

        when:
        investmentService.getVariableIncomesTransactions(testConsent.getConsentId(), testVariableIncomes.getInvestmentId(),
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
