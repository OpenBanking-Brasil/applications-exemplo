package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.TestEntityDataFactory
import com.raidiam.trustframework.bank.TestRequestDataFactory
import com.raidiam.trustframework.bank.domain.*
import com.raidiam.trustframework.bank.enums.AccountOrContractType
import com.raidiam.trustframework.mockbank.models.generated.*
import com.raidiam.trustframework.mockbank.models.generated.CreateConsentData.PermissionsEnum
import io.micronaut.data.model.Pageable
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Stepwise
import spock.lang.Unroll

import javax.inject.Inject
import java.time.Duration
import java.time.OffsetDateTime

import static com.raidiam.trustframework.bank.TestEntityDataFactory.*

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class ResourcesServiceSpec extends CleanupSpecification {
    @Inject
    ResourcesService resourcesService

    @Inject
    ConsentService consentService

    @Inject
    AccountsService accountsService
    @Inject
    TestEntityDataFactory testEntityDataFactory

    @Shared
    ConsentEntity testConsent
    @Shared
    ConsentPermissionEntity permission
    @Shared
    AccountHolderEntity testAccountHolder
    @Shared
    ConsentAccountEntity consentAccount
    @Shared
    ConsentContractEntity consentContract
    @Shared
    ConsentCreditCardAccountsEntity consentCreditCard
    @Shared
    AccountEntity testAccount
    @Shared
    ContractEntity testContract
    @Shared
    CreditCardAccountsEntity testCreditCard

    def setup() {
        if (runSetup) {
            testAccountHolder = accountHolderRepository.save(anAccountHolder())
            testConsent = consentRepository.save(aConsent(testAccountHolder.getAccountHolderId()))
            permission = consentPermissionsRepository.save(aConsentPermission(PermissionsEnum.RESOURCES_READ, testConsent.getConsentId()))

            testAccount = accountRepository.save(anAccount(testAccountHolder.getAccountHolderId()))
            consentAccount = new ConsentAccountEntity(testConsent, testAccount)
            consentAccountRepository.save(consentAccount)

            testContract = testEntityDataFactory.createAndSaveFullContract(
                    testAccountHolder.getAccountHolderId(),
                    AccountOrContractType.FINANCING,
                    EnumProductType.FINANCIAMENTOS.toString(),
                    EnumProductSubType.CUSTEIO.toString())
            consentContract = new ConsentContractEntity(testConsent, testContract)
            consentContractRepository.save(consentContract)

            testCreditCard = creditCardAccountsRepository.save(anCreditCardAccounts(testAccountHolder.accountHolderId))
            consentCreditCard = new ConsentCreditCardAccountsEntity(testConsent, testCreditCard)
            consentCreditCardAccountsRepository.save(consentCreditCard)

            runSetup = false
        }
    }

    def "we can get pages"() {
        given:
        def permission1 = consentPermissionsRepository.save(aConsentPermission(PermissionsEnum.ACCOUNTS_READ, testConsent.getConsentId()))
        def permission2 = consentPermissionsRepository.save(aConsentPermission(PermissionsEnum.FINANCINGS_READ, testConsent.getConsentId()))
        def permission3 = consentPermissionsRepository.save(aConsentPermission(PermissionsEnum.CREDIT_CARDS_ACCOUNTS_READ, testConsent.getConsentId()))

        when:
        def page1 = resourcesService.getResourceList(Pageable.from(0, 2), testConsent.getConsentId())
        def page2 = resourcesService.getResourceList(Pageable.from(1, 2), testConsent.getConsentId())

        then:
        // we can see the number of pages on each page
        page1.getMeta().totalPages == 2
        page2.getMeta().totalPages == 2

        // first page has 2 resources
        page1.getMeta().totalRecords == 2
        page1.getData().first().resourceId == testAccount.getAccountId().toString()
        page1.getData().last().resourceId == testContract.getContractId().toString()

        // second page has 1 resource
        page2.getMeta().totalRecords == 1
        page2.getData().first().resourceId == testCreditCard.getCreditCardAccountId().toString()

        // page 1 has no resources from page 2
        !page1.getData().contains(page2.getData().first())

        consentPermissionsRepository.delete(permission1)
        consentPermissionsRepository.delete(permission2)
        consentPermissionsRepository.delete(permission3)
    }

    def "we can not get resources without permissions"() {
        when:
        resourcesService.getResourceList(Pageable.unpaged(), testConsent.consentId)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.NOT_FOUND
        e.getMessage() == "Resource not found, no appropriate permissions attached to consent"
    }

    def "we can get resource only account with permission"() {
        setup:
        def permission = consentPermissionsRepository.save(aConsentPermission(PermissionsEnum.ACCOUNTS_READ, testConsent.getConsentId()))

        when:
        def response = resourcesService.getResourceList(Pageable.unpaged(), testConsent.consentId)

        then:
        response.data.size() == 3
        def acc = response.getData().stream()
                .filter(r -> r.getStatus() == ResponseResourceListData.StatusEnum.AVAILABLE).findFirst()
        acc.isPresent()
        acc.get().getType() == ResponseResourceListData.TypeEnum.ACCOUNT
        consentPermissionsRepository.delete(permission)
    }

    def "we can get resource only contract with permission"() {
        setup:
        def permission = consentPermissionsRepository.save(aConsentPermission(PermissionsEnum.FINANCINGS_READ, testConsent.getConsentId()))

        when:
        def response = resourcesService.getResourceList(Pageable.unpaged(), testConsent.consentId)

        then:
        response.data.size() == 3
        def acc = response.getData().stream()
                .filter(r -> r.getStatus() == ResponseResourceListData.StatusEnum.AVAILABLE).findFirst()
        acc.isPresent()
        acc.get().getType() == ResponseResourceListData.TypeEnum.FINANCING
        consentPermissionsRepository.delete(permission)
    }

    def "we can get resource only credit card with permission"() {
        setup:
        def permission = consentPermissionsRepository.save(aConsentPermission(PermissionsEnum.CREDIT_CARDS_ACCOUNTS_READ, testConsent.getConsentId()))

        when:
        def response = resourcesService.getResourceList(Pageable.unpaged(), testConsent.consentId)

        then:
        response.data.size() == 3
        def acc = response.getData().stream()
                .filter(r -> r.getStatus() == ResponseResourceListData.StatusEnum.AVAILABLE).findFirst()
        acc.isPresent()
        acc.get().getType() == ResponseResourceListData.TypeEnum.CREDIT_CARD_ACCOUNT
        consentPermissionsRepository.delete(permission)
    }

    def "we can not get resource without consent accounts"() {
        setup:
        def permission = consentPermissionsRepository.save(aConsentPermission(PermissionsEnum.ACCOUNTS_READ, testConsent.getConsentId()))
        consentAccountRepository.delete(consentAccount)

        when:
        def response = resourcesService.getResourceList(Pageable.unpaged(), testConsent.consentId)

        then:
        def acc = response.getData().stream()
                .filter(r -> r.getType() == ResponseResourceListData.TypeEnum.ACCOUNT).findFirst()
        acc.empty
        consentPermissionsRepository.delete(permission)
    }

    def "we can get resource without contracts"() {
        setup:
        def permission = consentPermissionsRepository.save(aConsentPermission(PermissionsEnum.FINANCINGS_READ, testConsent.getConsentId()))
        consentContractRepository.delete(consentContract)

        when:
        def response = resourcesService.getResourceList(Pageable.unpaged(), testConsent.consentId)

        then:
        def acc = response.getData().stream()
                .filter(r -> r.getType() == ResponseResourceListData.TypeEnum.FINANCING).findFirst()
        acc.empty
        consentPermissionsRepository.delete(permission)
    }

    def "we can get resource without credit cards"() {
        setup:
        def permission = consentPermissionsRepository.save(aConsentPermission(PermissionsEnum.CREDIT_CARDS_ACCOUNTS_READ, testConsent.getConsentId()))
        consentCreditCardAccountsRepository.delete(consentCreditCard)

        when:
        def response = resourcesService.getResourceList(Pageable.unpaged(), testConsent.consentId)

        then:
        def acc = response.getData().stream()
                .filter(r -> r.getType() == ResponseResourceListData.TypeEnum.CREDIT_CARD_ACCOUNT).findFirst()
        acc.empty
        consentPermissionsRepository.delete(permission)
    }

    def "we get an 403 on consent not found"() {
        given:
        def consentId = "notfoundconsent"

        when:
        resourcesService.getResourceList(Pageable.unpaged(), consentId)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.FORBIDDEN
        e.getLocalizedMessage() == "Consent Id " + consentId + " not found"
    }

    @Unroll
    def "everything works as expected"() {
        given:

        def account = anAccount(testAccountHolder.getAccountHolderId())
        account.status = status
        account = accountRepository.save(account)

        def clientId = "client12345"

        def expireIn10Days = OffsetDateTime.now() + Duration.ofDays(10)
        def fromLastWeek = OffsetDateTime.now() - Duration.ofDays(7)
        def toToday = OffsetDateTime.now()

        def consentBody = TestRequestDataFactory.createConsentRequest(
                "BID1",
                "REL1",
                testAccountHolder.documentIdentification,
                testAccountHolder.documentRel,
                expireIn10Days,
                List.of(PermissionsEnum.RESOURCES_READ,
                        PermissionsEnum.ACCOUNTS_READ,
                        PermissionsEnum.ACCOUNTS_BALANCES_READ))

        ResponseConsent responseConsent = consentService.createConsent(clientId, consentBody)

        def consentUpdate = TestRequestDataFactory.createConsentUpdate(
                consentStatus,
                "sub1",
                List.of(account.getAccountId().toString()),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                "client12345")

        ResponseConsentFull updatedConsent = consentService.updateConsent(responseConsent.data.consentId, consentUpdate)

        when:
        def response = resourcesService.getResourceList(Pageable.unpaged(), updatedConsent.data.consentId)

        then:
        noExceptionThrown()
        response.data.size() == 1
        response.data.get(0).resourceId == (resourceIdPresent ? account.accountId.toString() : null)
        response.data.get(0).status == resourceStatus

        where:
        status        | consentStatus                                       | resourceIdPresent | resourceStatus
        "AVAILABLE"   | UpdateConsentData.StatusEnum.AUTHORISED             | true              | ResponseResourceListData.StatusEnum.AVAILABLE
        "UNAVAILABLE" | UpdateConsentData.StatusEnum.AUTHORISED             | false             | ResponseResourceListData.StatusEnum.UNAVAILABLE
    }

    def "we get an 403 on wrong permissions"() {
        when:
        consentPermissionsRepository.delete(permission)
        resourcesService.getResourceList(Pageable.unpaged(), testConsent.consentId)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.FORBIDDEN
        e.getLocalizedMessage() == "You do not have the correct permission"
    }

    def "we get an 400 on not autorized"() {
        when:
        testConsent.setStatus("AWAITING_AUTHORISATION")
        consentRepository.update(testConsent)
        resourcesService.getResourceList(Pageable.unpaged(), testConsent.consentId)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.BAD_REQUEST
        e.getLocalizedMessage() == "Bad request, consent not Authorised!"
    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }

}
