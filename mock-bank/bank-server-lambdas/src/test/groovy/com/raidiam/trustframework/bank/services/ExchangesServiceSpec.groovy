package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.domain.ConsentEntity
import com.raidiam.trustframework.bank.domain.ExchangesOperationEntity
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
class ExchangesServiceSpec extends CleanupSpecification {

    @Shared
    ExchangesOperationEntity testExchangeOperationEntity

    @Shared
    ConsentEntity testConsent

    @Inject
    ExchangesService service

    def setup() {
        if (runSetup) {
            testExchangeOperationEntity = exchangesOperationRepository.save(aExchangesOperationEntity())
            exchangesOperationEventRepository.save(aExchangesOperationEventEntity(testExchangeOperationEntity.getOperationId()))
            exchangesOperationEventRepository.save(aExchangesOperationEventEntity(testExchangeOperationEntity.getOperationId()))
            testConsent = consentRepository.save(aConsent(anAccount().getAccountId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.EXCHANGES_READ, testConsent.getConsentId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.RESOURCES_READ, testConsent.getConsentId()))

            runSetup = false
        }
    }

    def "We can get all exchanges operations"() {
        when:
        def exchangesOperationList = service.getOperations(Pageable.from(0), testConsent.getConsentId())

        then:
        !exchangesOperationList.getData().isEmpty()
        exchangesOperationList.getData().size() == 1
        exchangesOperationList.getData().get(0).getCompanyCnpj() == testExchangeOperationEntity.getCompanyCnpj()
    }

    def "We can get an exchanges operations"() {
        when:
        def exchangesOperation = service.getOperationsByOperationId(testExchangeOperationEntity.getOperationId().toString(), testConsent.getConsentId())

        then:
        exchangesOperation.getData() != null
        exchangesOperation.getData().getAuthorizedInstitutionCnpjNumber() == testExchangeOperationEntity.getCompanyCnpj()
    }

    def "We can get an exchanges operations events"() {
        when:
        def exchangesOperation = service.getEventsByOperationId(testExchangeOperationEntity.getOperationId().toString(), testConsent.getConsentId(), Pageable.from(0))

        then:
        exchangesOperation.getData() != null
        exchangesOperation.getData().size() == 2
        var exchangesOperationEvent = exchangesOperation.getData().get(0)
        exchangesOperationEvent != null

        and:
        exchangesOperationEvent.foreignPartie.foreignPartieCountryCode == "ZA"
        exchangesOperationEvent.foreignPartie.foreignPartieName == "José da Silva"
        exchangesOperationEvent.foreignPartie.relationshipCode == "50"
    }


    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }
}
