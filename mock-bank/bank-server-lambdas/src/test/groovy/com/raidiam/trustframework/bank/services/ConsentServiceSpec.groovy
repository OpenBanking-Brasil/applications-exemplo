package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.TestEntityDataFactory
import com.raidiam.trustframework.bank.controllers.ConsentFactory
import com.raidiam.trustframework.bank.domain.AccountEntity
import com.raidiam.trustframework.bank.domain.AccountHolderEntity
import com.raidiam.trustframework.bank.enums.AccountOrContractType
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Stepwise

import javax.inject.Inject
import java.time.OffsetDateTime

import static com.raidiam.trustframework.bank.TestEntityDataFactory.*

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class ConsentServiceSpec extends CleanupSpecification {
    @Inject
    ConsentService service

    @Shared
    AccountEntity testAccount
    @Shared
    AccountHolderEntity testAccountHolder

    @Inject
    TestEntityDataFactory testEntityDataFactory

    def setup() {
        if (runSetup) {
            testAccountHolder = accountHolderRepository.save(anAccountHolder())
            def account = anAccount()
            account.setAccountHolderId(testAccountHolder.getAccountHolderId())
            testAccount = accountRepository.save(account)
            runSetup = false
        }
    }

    def "We can request a consent" () {
        given:
        CreateConsent consent = ConsentFactory.createConsent(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)

        when:
        ResponseConsent response = service.createConsent('client1', consent)

        then:
        response.getData().getStatus() == ResponseConsentData.StatusEnum.AWAITING_AUTHORISATION
    }

    def "We have to request an entire set of permissions"() {

        given:
        CreateConsent consent = ConsentFactory.createConsent(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        consent.data.setPermissions([CreateConsentData.PermissionsEnum.ACCOUNTS_READ, CreateConsentData.PermissionsEnum.ACCOUNTS_BALANCES_READ])

        when:
        service.createConsent('client1', consent)

        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.BAD_REQUEST
        ex.message == 'You must request all the permissions from a given set'

    }

    def "We can update a consent" () {
        given:
        CreateConsent consent = ConsentFactory.createConsent(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)

        def account = accountRepository.save(anAccount(testAccountHolder.getAccountHolderId()))
        def account2 = accountRepository.save(anAccount(testAccountHolder.getAccountHolderId()))
        def creditCard = creditCardAccountsRepository.save(anCreditCardAccounts(testAccountHolder.getAccountHolderId()))
        def creditCard2 = creditCardAccountsRepository.save(anCreditCardAccounts(testAccountHolder.getAccountHolderId()))
        def loan = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(), AccountOrContractType.LOAN, "a", "b")
        def loan2 = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(), AccountOrContractType.LOAN, "a", "b")
        def financing = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(), AccountOrContractType.FINANCING, "a", "b")
        def financing2  = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(), AccountOrContractType.FINANCING, "a", "b")
        def invoiceFinancing  = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(), AccountOrContractType.INVOICE_FINANCING, "a", "b")
        def invoiceFinancing2  = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(), AccountOrContractType.INVOICE_FINANCING, "a", "b")
        def overdraft  = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(), AccountOrContractType.UNARRANGED_ACCOUNT_OVERDRAFT, "a", "b")
        def overdraft2  = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(), AccountOrContractType.UNARRANGED_ACCOUNT_OVERDRAFT, "a", "b")

        List<String> linkedAccountIds = [account.getAccountId().toString(), account2.getAccountId().toString()]
        List<String> linkedCreditCardAccountIds = [creditCard.getCreditCardAccountId().toString(), creditCard2.getCreditCardAccountId().toString()]
        List<String> linkedLoanAccountIds = [loan.getContractId().toString(), loan2.getContractId().toString()]
        List<String> linkedFinancingAccountIds = [financing.getContractId().toString(), financing2.getContractId().toString()]
        List<String> linkedInvoiceFinancingAccountIds = [invoiceFinancing.getContractId().toString(), invoiceFinancing2.getContractId().toString()]
        List<String> linkedOverdraftAccountIds = [overdraft.getContractId().toString(), overdraft2.getContractId().toString()]

        UpdateConsentData updateData = new UpdateConsentData()
        updateData.setStatus(UpdateConsentData.StatusEnum.AUTHORISED)
        updateData.setLinkedAccountIds(linkedAccountIds)
        updateData.setLinkedCreditCardAccountIds(linkedCreditCardAccountIds)
        updateData.setLinkedLoanAccountIds(linkedLoanAccountIds)
        updateData.setLinkedFinancingAccountIds(linkedFinancingAccountIds)
        updateData.setLinkedInvoiceFinancingAccountIds(linkedInvoiceFinancingAccountIds)
        updateData.setLinkedUnarrangedOverdraftAccountIds(linkedOverdraftAccountIds)

        UpdateConsent updateRequest = new UpdateConsent()
        updateRequest.setData(updateData)

        when:
        ResponseConsent response = service.createConsent('client1', consent)

        then:
        response.getData().getStatus() == ResponseConsentData.StatusEnum.AWAITING_AUTHORISATION

        when:
        ResponseConsentFull updateResponse = service.updateConsent(response.getData().getConsentId(), updateRequest)

        then:
        updateResponse.getData().getStatus() == ResponseConsentFullData.StatusEnum.AUTHORISED
        updateResponse.getData().getLinkedAccountIds().containsAll(linkedAccountIds)
        updateResponse.getData().getLinkedCreditCardAccountIds().containsAll(linkedCreditCardAccountIds)
        updateResponse.getData().getLinkedLoanAccountIds().containsAll(linkedLoanAccountIds)
        updateResponse.getData().getLinkedFinancingAccountIds().containsAll(linkedFinancingAccountIds)
        updateResponse.getData().getLinkedInvoiceFinancingAccountIds().containsAll(linkedInvoiceFinancingAccountIds)
        updateResponse.getData().getLinkedUnarrangedOverdraftAccountIds().containsAll(linkedOverdraftAccountIds)
        updateResponse.getData().getSub() == testAccountHolder.getUserId()

        when:
        ResponseConsentFull fetched = service.getConsentFull(response.getData().consentId)

        then:
        fetched.getData().getStatus() == ResponseConsentFullData.StatusEnum.AUTHORISED
        fetched.getData().getLinkedAccountIds().containsAll(linkedAccountIds)
        fetched.getData().getLinkedCreditCardAccountIds().containsAll(linkedCreditCardAccountIds)
        fetched.getData().getLinkedLoanAccountIds().containsAll(linkedLoanAccountIds)
        fetched.getData().getLinkedFinancingAccountIds().containsAll(linkedFinancingAccountIds)
        fetched.getData().getLinkedInvoiceFinancingAccountIds().containsAll(linkedInvoiceFinancingAccountIds)
        fetched.getData().getLinkedUnarrangedOverdraftAccountIds().containsAll(linkedOverdraftAccountIds)
        fetched.getData().getSub() == testAccountHolder.getUserId()
    }

    def "We can look up a consent"() {

        given:
        def consent = ConsentFactory.createConsent(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        ResponseConsent response = service.createConsent('client1', consent)

        when:
        def foundResponse = service.getConsent(response.data.consentId, 'client1')

        then:
        foundResponse.getData().getStatus() == ResponseConsentData.StatusEnum.AWAITING_AUTHORISATION

    }

    def "We can look up a consent as an OP and it has a sub"() {

        given:
        def consent = ConsentFactory.createConsent(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        ResponseConsent response = service.createConsent('client1', consent)

        when:
        def foundResponse = service.getConsentFull(response.data.consentId)

        then:
        foundResponse.getData().getStatus() == ResponseConsentFullData.StatusEnum.AWAITING_AUTHORISATION
        foundResponse.getData().getSub() != null
        foundResponse.getData().getSub() == testAccountHolder.getUserId()
    }

    def "We cannot look up another client's consent"() {

        given:
        def consent = ConsentFactory.createConsent(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        ResponseConsent response = service.createConsent('client1', consent)

        when:
        service.getConsent(response.data.consentId, 'client2')

        then:
        def ex = thrown(HttpStatusException)
        ex.status == HttpStatus.FORBIDDEN

    }

    def "We cannot delete another client's consent"() {

        given:
        def consent = ConsentFactory.createConsent(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        ResponseConsent response = service.createConsent('client1', consent)

        when:
        service.deleteConsent(response.data.consentId, 'client2')

        then:
        def ex = thrown(HttpStatusException)
        ex.status == HttpStatus.FORBIDDEN

    }

    def "We can look up all consents"() {

        given:
        consentAccountRepository.deleteAll()
        consentCreditCardAccountsRepository.deleteAll()
        consentContractRepository.deleteAll()
        consentRepository.deleteAll()
        def consent = ConsentFactory.createConsent(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        ResponseConsent response = service.createConsent('client1', consent)
        Pageable pageable = Pageable.from(0)

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
        def consent = ConsentFactory.createConsent(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)

        when:
        ResponseConsent response = service.createConsent('client1', consent)
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
        def consent = ConsentFactory.createConsent(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)

        when:
        ResponseConsent response = service.createConsent('client1', consent)

        then:
        noExceptionThrown()
        response != null

        when:
        // wait for one second, so that on update the timestamp changes
        Thread.sleep(1000)
        UpdateConsent update = new UpdateConsent()
        update.data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AWAITING_AUTHORISATION))

        service.updateConsent(response.getData().getConsentId(), update)
        def updateResponse = service.getConsent(response.getData().getConsentId(), 'client1')

        then:
        noExceptionThrown()
        updateResponse != null

        when:
        def onCreateUpdateTime = response.getData().getStatusUpdateDateTime()
        def updatedTime = updateResponse.getData().getStatusUpdateDateTime()

        then:
        onCreateUpdateTime != null
        updatedTime != null
        onCreateUpdateTime != updatedTime
    }

    def "We can delete a consent"() {

        given:
        def consent = ConsentFactory.createConsent(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        ResponseConsent response = service.createConsent('client1', consent)
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
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.FORBIDDEN

    }

    def "expiration date must not be before transaction To time"() {

        given:
        def consent = ConsentFactory.createConsent(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        consent.data.expirationDateTime(OffsetDateTime.now().plusHours(1L))
        consent.data.transactionToDateTime(OffsetDateTime.now().plusDays(1))

        when:
        service.createConsent('client1', consent)

        then:
        HttpStatusException it = thrown()
        it.status == HttpStatus.BAD_REQUEST
        it.message == "Expiration time should be on or after transactionTo time"

    }

    def "expiration date is mandatory"() {

        given:
        def consent = ConsentFactory.createConsent(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        consent.data.expirationDateTime(null)

        when:
        service.createConsent('client1', consent)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.BAD_REQUEST
        e.message == 'Expiration time is mandatory'

    }

    def "expiration date must be within a year"() {

        given:
        def consent = ConsentFactory.createConsent(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        consent.data.expirationDateTime(OffsetDateTime.now().plusDays(367))

        when:
        service.createConsent('client1', consent)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.BAD_REQUEST
        e.message == 'Expiration time must be within a year'

    }

    def "expiration date must be in the future"() {

        given:
        def consent = ConsentFactory.createConsent(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        consent.data.expirationDateTime(OffsetDateTime.now())

        when:
        service.createConsent('client1', consent)

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

    def "If transaction from/to date fields are set then a 400 response is returned"() {
        given:
        CreateConsent consent = ConsentFactory.createConsent()
        consent.data.transactionFromDateTime(OffsetDateTime.now())
        consent.data.transactionToDateTime(OffsetDateTime.now())

        when:
        ResponseConsent response = service.createConsent('client1', consent)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.BAD_REQUEST
    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }
}
