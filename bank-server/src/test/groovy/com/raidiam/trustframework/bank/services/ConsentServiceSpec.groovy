package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.controllers.ConsentFactory
import com.raidiam.trustframework.bank.exceptions.TrustframeworkException
import com.raidiam.trustframework.bank.repository.ConsentAccountIdRepository
import com.raidiam.trustframework.bank.repository.ConsentRepository
import com.raidiam.trustframework.bank.utils.BankLambdaUtils
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.data.model.Page
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject
import java.time.OffsetDateTime

@MicronautTest(transactional = false, environments = ["db"])
class ConsentServiceSpec extends Specification {
    @Shared
    Logger log = LoggerFactory.getLogger(ConsentServiceSpec.class)

    @Inject
    @Shared
    ConsentService service

    @Inject
    @Shared
    ConsentRepository consentRepository

    @Inject
    @Shared
    ConsentAccountIdRepository consentAccountIdRepository

    @Inject
    @Shared
    ConsentRepository consentPermissionsRepository

    def setupSpec() {

    }

    def "We can request a consent" () {
        given:
        CreateConsent consent = ConsentFactory.createConsent()

        when:
        ResponseConsent response = service.createConsent(consent, "client1")

        then:
        response.getData().getStatus() == ResponseConsentData.StatusEnum.AWAITING_AUTHORISATION
    }

    def "We have to request an entire set of permissions"() {

        given:
        CreateConsent consent = ConsentFactory.createConsent()
        consent.data.setPermissions([CreateConsentData.PermissionsEnum.ACCOUNTS_READ, CreateConsentData.PermissionsEnum.ACCOUNTS_BALANCES_READ])

        when:
        service.createConsent(consent, "client1")

        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.BAD_REQUEST
        ex.message == 'You must request all the permissions from a given set'

    }

    def "We can update a consent" () {
        given:
        CreateConsent consent = ConsentFactory.createConsent()
        List<String> linkedAccountIds = ['123456', '1395733']
        List<String> linkedCCAccountIds = ['9798444', '39838593']
        List<String> linkedLoanAccountIds = ['46434382', '8468484']
        List<String> linkedFinancingAccountIds = ['789797947', '959874841']

        UpdateConsentData updateData = new UpdateConsentData()
        updateData.setStatus(UpdateConsentData.StatusEnum.AUTHORISED)
        updateData.setLinkedAccountIds(linkedAccountIds)
        updateData.setLinkedCreditCardAccountIds(linkedCCAccountIds)
        updateData.setLinkedLoanAccountIds(linkedLoanAccountIds)
        updateData.setLinkedFinancingAccountIds(linkedFinancingAccountIds)
        UpdateConsent updateRequest = new UpdateConsent()
        updateRequest.setData(updateData)

        when:
        ResponseConsent response = service.createConsent(consent, "client1")

        then:
        response.getData().getStatus() == ResponseConsentData.StatusEnum.AWAITING_AUTHORISATION

        when:
        ResponseConsentFull updateResponse = service.updateConsent(response.getData().getConsentId(), updateRequest)

        then:
        updateResponse.getData().getStatus() == ResponseConsentFullData.StatusEnum.AUTHORISED
        updateResponse.getData().getLinkedAccountIds().containsAll(linkedAccountIds)
        updateResponse.getData().getLinkedCreditCardAccountIds().containsAll(linkedCCAccountIds)
        updateResponse.getData().getLinkedLoanAccountIds().containsAll(linkedLoanAccountIds)
        updateResponse.getData().getLinkedFinancingAccountIds().containsAll(linkedFinancingAccountIds)

        when:
        ResponseConsentFull fetched = service.getConsentFull(response.getData().consentId)

        then:
        fetched.getData().getStatus() == ResponseConsentFullData.StatusEnum.AUTHORISED
        fetched.getData().getLinkedAccountIds().containsAll(linkedAccountIds)
        fetched.getData().getLinkedCreditCardAccountIds().containsAll(linkedCCAccountIds)
        fetched.getData().getLinkedLoanAccountIds().containsAll(linkedLoanAccountIds)
        fetched.getData().getLinkedFinancingAccountIds().containsAll(linkedFinancingAccountIds)
    }

    def "We can look up a consent"() {

        given:
        def consent = ConsentFactory.createConsent()
        ResponseConsent response = service.createConsent(consent, "client1")

        when:
        def foundResponse = service.getConsent(response.data.consentId, 'client1')

        then:
        foundResponse.getData().getStatus() == ResponseConsentData.StatusEnum.AWAITING_AUTHORISATION

    }

    def "We can look up a consent as an OP"() {

        given:
        def consent = ConsentFactory.createConsent()
        ResponseConsent response = service.createConsent(consent, "client1")

        when:
        def foundResponse = service.getConsentFull(response.data.consentId)

        then:
        foundResponse.getData().getStatus() == ResponseConsentFullData.StatusEnum.AWAITING_AUTHORISATION

    }

    def "We cannot look up another client's consent"() {

        given:
        def consent = ConsentFactory.createConsent()
        ResponseConsent response = service.createConsent(consent, "client1")

        when:
        def foundResponse = service.getConsent(response.data.consentId, 'client2')

        then:
        def ex = thrown(HttpStatusException)
        ex.status == HttpStatus.FORBIDDEN

    }

    def "We cannot delete another client's consent"() {

        given:
        def consent = ConsentFactory.createConsent()
        ResponseConsent response = service.createConsent(consent, "client1")
        def id = response.data.consentId

        when:
        service.deleteConsent(response.data.consentId, 'client2')

        then:
        def ex = thrown(HttpStatusException)
        ex.status == HttpStatus.FORBIDDEN

    }

    def "We can look up all consents"() {

        given:
        service.consentRepository.deleteAll()
        def consent = ConsentFactory.createConsent()
        ResponseConsent response = service.createConsent(consent, "client1")
        io.micronaut.data.model.Pageable pageable = io.micronaut.data.model.Pageable.from(0)

        when:
        Page<ResponseConsent> page = service.getConsents(pageable)

        then:
        page.totalPages == 1
        page.totalSize == 1L
        ResponseConsent found = page.iterator().next()
        found == response

    }

    def "Consent status must not be null"() {

        given:
        def consent = ConsentFactory.createConsent()

        when:
        ResponseConsent response = service.createConsent(consent, 'client1')
        UpdateConsent internal = new UpdateConsent()
            .data(new UpdateConsentData())

        then:
        response.getData().getStatus() == ResponseConsentData.StatusEnum.AWAITING_AUTHORISATION

        when:
        service.updateConsent(response.getData().getConsentId(), internal)

        then:
        HttpStatusException it = thrown()
        it.status == HttpStatus.BAD_REQUEST
        it.message == "Request data missing a status value"

    }

    def "Consent update time gets updated"() {

        given:
        def consent = ConsentFactory.createConsent()
        OffsetDateTime time = OffsetDateTime.now().minusDays(1L)
        def date = BankLambdaUtils.offsetDateToDate time
        time = BankLambdaUtils.dateToOffsetDate date

        when:
        ResponseConsent response = service.createConsent(consent, "client1")
        UpdateConsent update = new UpdateConsent()
        update.data(new UpdateConsentData()
                .status(UpdateConsentData.StatusEnum.AWAITING_AUTHORISATION))

        and:
        service.updateConsent(response.getData().getConsentId(), update)
        response = service.getConsent(response.getData().getConsentId(), 'client1')

        then:
        def updatedTime = response.getData().getStatusUpdateDateTime()
//        updatedTime.compareTo(time) == 0

    }

    def "We can delete a consent"() {

        given:
        def consent = ConsentFactory.createConsent()
        ResponseConsent response = service.createConsent(consent, "client1")
        def id = response.data.consentId

        when:
        service.deleteConsent(id, 'client1')
        service.getConsent(id, 'client1')

        then:
        def ex = thrown(HttpStatusException)
        ex.status == HttpStatus.NOT_FOUND

    }

    def "Cannot update a non-existent consent"() {

        when:
        service.updateConsent(UUID.randomUUID().toString(), new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)))

        then:
        TrustframeworkException e = thrown()
        e.message == "Cannot find consent to update"

    }

    def "expiration date must not be before transaction To time"() {

        given:
        def consent = ConsentFactory.createConsent()
        consent.data.expirationDateTime(OffsetDateTime.now().plusHours(1L))
        consent.data.transactionToDateTime(OffsetDateTime.now().plusDays(1))

        when:
        service.createConsent(consent, "client1")

        then:
        HttpStatusException it = thrown()
        it.status == HttpStatus.BAD_REQUEST
        it.message == "Expiration time should be on or after transactionTo time"

    }

    def "expiration date can be the same as transaction To time"() {

        given:
        def consent = ConsentFactory.createConsent()
        def now = OffsetDateTime.now().plusDays(1L)
        consent.data.expirationDateTime(now)
        consent.data.transactionToDateTime(now)

        when:
        service.createConsent(consent, "client1")

        then:
        noExceptionThrown()

    }

    def "expiration date must be after transaction from time"() {

        given:
        def consent = ConsentFactory.createConsent()
        consent.data.expirationDateTime(OffsetDateTime.now().plusMinutes(13L))
        consent.data.transactionToDateTime(OffsetDateTime.now().plusMinutes(10L))
        consent.data.transactionFromDateTime(OffsetDateTime.now().plusMinutes(20L))

        when:
        service.createConsent(consent, "client1")

        then:
        HttpStatusException it = thrown()
        it.status == HttpStatus.BAD_REQUEST
        it.message == "Expiration time should be after transaction from time"

    }

    def "Transaction to date must be after transaction from date"() {

        given:
        def consent = ConsentFactory.createConsent()
        consent.data.expirationDateTime(OffsetDateTime.now().plusDays(2L))
        consent.data.transactionToDateTime(OffsetDateTime.now().minusMinutes(2L))
        consent.data.transactionFromDateTime(OffsetDateTime.now())

        when:
        service.createConsent(consent, "client1")

        then:
        HttpStatusException it = thrown()
        it.status == HttpStatus.BAD_REQUEST
        it.message == "Transaction to date should be after transaction from date"

    }

    def "Transaction to date is optional"() {

        given:
        def consent = ConsentFactory.createConsent()
        consent.data.expirationDateTime(OffsetDateTime.now().plusDays(2L))
        consent.data.transactionToDateTime(null)
        consent.data.transactionFromDateTime(OffsetDateTime.now())

        when:
        service.createConsent(consent, "client1")

        then:
        noExceptionThrown()

    }

    def "Transaction from date is optional"() {

        given:
        def consent = ConsentFactory.createConsent()
        consent.data.expirationDateTime(OffsetDateTime.now().plusDays(2L))
        consent.data.transactionToDateTime(OffsetDateTime.now().minusMinutes(2L))
        consent.data.transactionFromDateTime(null)

        when:
        service.createConsent(consent, "client1")

        then:
        noExceptionThrown()

    }

    def "expiration date is mandatory"() {

        given:
        def consent = ConsentFactory.createConsent()
        consent.data.expirationDateTime(null)

        when:
        service.createConsent(consent, "client1")

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.BAD_REQUEST
        e.message == 'Expiration time is mandatory'

    }

    def "expiration date must be within a year"() {

        given:
        def consent = ConsentFactory.createConsent()
        consent.data.expirationDateTime(OffsetDateTime.now().plusDays(367))

        when:
        service.createConsent(consent, "client1")

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.BAD_REQUEST
        e.message == 'Expiration time must be within a year'

    }

    def "expiration date must be in the future"() {

        given:
        def consent = ConsentFactory.createConsent()
        consent.data.expirationDateTime(OffsetDateTime.now())

        when:
        service.createConsent(consent, "client1")

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.BAD_REQUEST
        e.message == 'Expiration time must be in the future'

    }

    def "Fictional consents give a 404"() {

        when:
        service.getConsent("doesntexist", "client1")

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.NOT_FOUND

    }

    def "Fictional consents give a 404 even when asked for full"() {

        when:
        service.getConsentFull("doesntexist")

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.NOT_FOUND

    }

}
