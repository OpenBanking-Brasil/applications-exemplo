package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.TestEntityDataFactory
import com.raidiam.trustframework.bank.TestRequestDataFactory
import com.raidiam.trustframework.bank.domain.*
import com.raidiam.trustframework.bank.enums.ResourceType
import com.raidiam.trustframework.mockbank.models.generated.*
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
    ConsentEntity testConsentInvestment
    @Shared
    ConsentPermissionEntity permission
    @Shared
    AccountHolderEntity testAccountHolder
    @Shared
    ConsentAccountEntity consentAccount
    @Shared
    ConsentContractEntity consentContract
    @Shared
    ConsentContractEntity consentContractUnavailable
    @Shared
    ConsentCreditCardAccountsEntity consentCreditCard
    @Shared
    AccountEntity testAccount
    @Shared
    ContractEntity testContract
    @Shared
    ContractEntity testUnavailableContract
    @Shared
    CreditCardAccountsEntity testCreditCard
    @Shared
    ConsentInvestmentEntity consentInvestment
    @Shared
    BankFixedIncomesEntity testBankFixedIncome
    @Shared
    CreditFixedIncomesEntity testCreditFixedIncome
    @Shared
    VariableIncomesEntity testVariableIncome
    @Shared
    TreasureTitlesEntity testTreasureTitle
    @Shared
    FundsEntity testFund
    @Shared
    ConsentExchangeOperationEntity consentExchangeOperation
    @Shared
    ExchangesOperationEntity testExchangesOperation
    @Shared
    ConsentExchangeOperationEntity unavailableConsentExchangeOperation
    @Shared
    ExchangesOperationEntity testUnavailableExchangesOperation

    def setup() {
        if (runSetup) {
            testAccountHolder = accountHolderRepository.save(anAccountHolder())
            testConsent = consentRepository.save(aConsent(testAccountHolder.getAccountHolderId()))
            testConsentInvestment = consentRepository.save(aConsent(testAccountHolder.getAccountHolderId()))
            permission = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.RESOURCES_READ, testConsent.getConsentId()))

            testAccount = accountRepository.save(anAccount(testAccountHolder))
            consentAccount = new ConsentAccountEntity(testConsent, testAccount)
            consentAccountRepository.save(consentAccount)

            testContract = testEntityDataFactory.createAndSaveFullContract(
                    testAccountHolder.getAccountHolderId(),
                    ResourceType.FINANCING,
                    EnumProductType.FINANCIAMENTOS.toString(),
                    EnumProductSubType.CUSTEIO.toString())
            consentContract = new ConsentContractEntity(testConsent, testContract)
            consentContractRepository.save(consentContract)

            testUnavailableContract = testEntityDataFactory.createAndSaveFullContractUnavailable(
                    testAccountHolder.getAccountHolderId(),
                    ResourceType.FINANCING,
                    EnumProductType.FINANCIAMENTOS.toString(),
                    EnumProductSubType.CUSTEIO.toString())
            consentContractUnavailable = new ConsentContractEntity(testConsent, testUnavailableContract)
            consentContractRepository.save(consentContractUnavailable)

            testCreditCard = creditCardAccountsRepository.save(anCreditCardAccounts(testAccountHolder.accountHolderId))
            consentCreditCard = new ConsentCreditCardAccountsEntity(testConsent, testCreditCard)
            consentCreditCardAccountsRepository.save(consentCreditCard)

            testExchangesOperation = testEntityDataFactory.createAndSaveExchangeOperation(
                    testAccountHolder.getAccountHolderId())
            consentExchangeOperation = new ConsentExchangeOperationEntity(testConsent, testExchangesOperation)
            consentExchangeOperationRepository.save(consentExchangeOperation)

            testUnavailableExchangesOperation = testEntityDataFactory.createAndSaveUnavailableExchangeOperation(
                    testAccountHolder.getAccountHolderId())
            unavailableConsentExchangeOperation = new ConsentExchangeOperationEntity(testConsent, testUnavailableExchangesOperation)
            consentExchangeOperationRepository.save(unavailableConsentExchangeOperation)

            testBankFixedIncome = testEntityDataFactory.createAndSaveBankFixedIncome(
                    testAccountHolder.getAccountHolderId())
            consentInvestment = new ConsentInvestmentEntity(testConsentInvestment)
            consentInvestment.setBankFixedIncomeId(testBankFixedIncome.getInvestmentId())

            testCreditFixedIncome = testEntityDataFactory.createAndSaveCreditFixedIncome(
                    testAccountHolder.getAccountHolderId())
            consentInvestment.setCreditFixedIncomeId(testCreditFixedIncome.getInvestmentId())

            testVariableIncome = testEntityDataFactory.createAndSaveVariableIncome(
                    testAccountHolder.getAccountHolderId())
            consentInvestment.setVariableIncomeId(testVariableIncome.getInvestmentId())

            testTreasureTitle = testEntityDataFactory.createAndSaveTreasureTitle(
                    testAccountHolder.getAccountHolderId())
            consentInvestment.setTreasureTitleId(testTreasureTitle.getInvestmentId())

            testFund = testEntityDataFactory.createAndSaveFund(
                    testAccountHolder.getAccountHolderId())
            consentInvestment.setFundId(testFund.getInvestmentId())
            consentInvestmentRepository.save(consentInvestment)
            runSetup = false
        }
    }

    def "we can get pages"() {
        given:
        def permission1 = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.ACCOUNTS_READ, testConsent.getConsentId()))
        def permission2 = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.FINANCINGS_READ, testConsent.getConsentId()))
        def permission3 = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.CREDIT_CARDS_ACCOUNTS_READ, testConsent.getConsentId()))

        when:
        def page1 = resourcesService.getResourceList(Pageable.from(0, 2), testConsent.getConsentId())
        def page2 = resourcesService.getResourceList(Pageable.from(1, 2), testConsent.getConsentId())

        then:
        // we can see the number of pages on each page
        page1.getMeta().totalPages == 2
        page2.getMeta().totalPages == 2

        // first page has 2 resources
        page1.getData().size() == 2

        // second page has 1 resource
        page2.getData().size() == 2

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
        def permission = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.ACCOUNTS_READ, testConsent.getConsentId()))

        when:
        def response = resourcesService.getResourceList(Pageable.unpaged(), testConsent.consentId)

        then:
        response.data.size() == 1
        def acc = response.getData().stream()
                .filter(r -> r.getStatus() == ResponseResourceListData.StatusEnum.AVAILABLE).findFirst()
        acc.isPresent()
        acc.get().getType() == ResponseResourceListData.TypeEnum.ACCOUNT
        consentPermissionsRepository.delete(permission)
    }

    def "we can get resource only contract with permission"() {
        setup:
        def permission = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.FINANCINGS_READ, testConsent.getConsentId()))

        when:
        def response = resourcesService.getResourceList(Pageable.unpaged(), testConsent.consentId)

        then:
        response.data.size() == 2
        def acc = response.getData().stream()
                .filter(r -> r.getStatus() == ResponseResourceListData.StatusEnum.AVAILABLE).findFirst()
        acc.isPresent()
        acc.get().getType() == ResponseResourceListData.TypeEnum.FINANCING

        def unavailable = response.getData().stream()
                .filter(r -> r.getStatus() == ResponseResourceListData.StatusEnum.UNAVAILABLE).findFirst()
        unavailable.isPresent()
        unavailable.get().getType() == ResponseResourceListData.TypeEnum.FINANCING
        consentPermissionsRepository.delete(permission)
    }

    def "we can get an exchanges resource with permission"() {
        setup:
        def permission = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.EXCHANGES_READ, testConsent.getConsentId()))

        when:
        def response = resourcesService.getResourceList(Pageable.unpaged(), testConsent.consentId)

        then:
        response.data.size() == 2
        def acc = response.getData().stream()
                .filter(r -> r.getStatus() == ResponseResourceListData.StatusEnum.AVAILABLE).findFirst()
        acc.isPresent()
        acc.get().getType() == ResponseResourceListData.TypeEnum.EXCHANGE

        def unavailable = response.getData().stream()
                .filter(r -> r.getStatus() == ResponseResourceListData.StatusEnum.UNAVAILABLE).findFirst()
        unavailable.isPresent()
        unavailable.get().getType() == ResponseResourceListData.TypeEnum.EXCHANGE

        consentPermissionsRepository.delete(permission)
    }

    def "we can get resource only credit card with permission"() {
        setup:
        def permission = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.CREDIT_CARDS_ACCOUNTS_READ, testConsent.getConsentId()))

        when:
        def response = resourcesService.getResourceList(Pageable.unpaged(), testConsent.consentId)

        then:
        response.data.size() == 1
        def acc = response.getData().stream()
                .filter(r -> r.getStatus() == ResponseResourceListData.StatusEnum.AVAILABLE).findFirst()
        acc.isPresent()
        acc.get().getType() == ResponseResourceListData.TypeEnum.CREDIT_CARD_ACCOUNT
        consentPermissionsRepository.delete(permission)
    }

    def "Accounts permissions should only return the ACCOUNT resource"() {
        setup:
        def permission = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.ACCOUNTS_READ, testConsent.getConsentId()))
        def permission2 = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.ACCOUNTS_BALANCES_READ, testConsent.getConsentId()))
        def permission3 = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.ACCOUNTS_OVERDRAFT_LIMITS_READ, testConsent.getConsentId()))
        def permission4 = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.ACCOUNTS_TRANSACTIONS_READ, testConsent.getConsentId()))

        when:
        def response = resourcesService.getResourceList(Pageable.unpaged(), testConsent.consentId)

        then:
        response.data.size() == 1
        def acc = response.getData().stream()
                .filter(r -> r.getStatus() == ResponseResourceListData.StatusEnum.AVAILABLE).findFirst()
        acc.isPresent()
        acc.get().getType() == ResponseResourceListData.TypeEnum.ACCOUNT
        consentPermissionsRepository.delete(permission)
        consentPermissionsRepository.delete(permission2)
        consentPermissionsRepository.delete(permission3)
        consentPermissionsRepository.delete(permission4)
    }

    def "we can not get resource without consent accounts"() {
        setup:
        def permission = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.ACCOUNTS_READ, testConsent.getConsentId()))
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
        def permission = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.FINANCINGS_READ, testConsent.getConsentId()))
        consentContractRepository.delete(consentContract)
        consentContractRepository.delete(consentContractUnavailable)

        when:
        def response = resourcesService.getResourceList(Pageable.unpaged(), testConsent.consentId)

        then:
        def acc = response.getData().stream()
                .filter(r -> r.getType() == ResponseResourceListData.TypeEnum.FINANCING).findFirst()
        acc.empty
        consentPermissionsRepository.delete(permission)
    }

    def "we can get investment resources"() {
        setup:
        def p1 = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.BANK_FIXED_INCOMES_READ, testConsentInvestment.getConsentId()))
        def p2 = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.CREDIT_FIXED_INCOMES_READ, testConsentInvestment.getConsentId()))
        def p3 = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.VARIABLE_INCOMES_READ, testConsentInvestment.getConsentId()))
        def p4 = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.TREASURE_TITLES_READ, testConsentInvestment.getConsentId()))
        def p5 = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.FUNDS_READ, testConsentInvestment.getConsentId()))
        def p6 = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.RESOURCES_READ, testConsentInvestment.getConsentId()))

        when:
        def response = resourcesService.getResourceList(Pageable.unpaged(), testConsentInvestment.consentId)

        then:
        def bfa = response.getData().stream()
                .filter(r -> r.getType() == ResponseResourceListData.TypeEnum.BANK_FIXED_INCOME).findFirst()
        !bfa.empty
        def cfa = response.getData().stream()
                .filter(r -> r.getType() == ResponseResourceListData.TypeEnum.CREDIT_FIXED_INCOME).findFirst()
        !cfa.empty
        def va = response.getData().stream()
                .filter(r -> r.getType() == ResponseResourceListData.TypeEnum.VARIABLE_INCOME).findFirst()
        !va.empty
        consentPermissionsRepository.delete(p1)
        consentPermissionsRepository.delete(p2)
        consentPermissionsRepository.delete(p3)
        consentPermissionsRepository.delete(p4)
        consentPermissionsRepository.delete(p5)
        consentPermissionsRepository.delete(p6)
    }

    def "we can get resource without credit cards"() {
        setup:
        def permission = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.CREDIT_CARDS_ACCOUNTS_READ, testConsent.getConsentId()))
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
        e.status == HttpStatus.NOT_FOUND
        e.getLocalizedMessage() == "Consent Id " + consentId + " not found"
    }

    @Unroll
    def "everything works as expected"() {
        given:

        def account = anAccount(testAccountHolder)
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
                List.of(EnumConsentPermissions.RESOURCES_READ,
                        EnumConsentPermissions.ACCOUNTS_READ,
                        EnumConsentPermissions.ACCOUNTS_BALANCES_READ))

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
        response.data.size() == size
        if (size > 0) {
            def acc = response.getData().stream()
                    .filter(r -> r.getType() == ResponseResourceListData.TypeEnum.ACCOUNT).findFirst().get()
            acc.resourceId == account.accountId.toString()
            acc.status == resourceStatus
        }

        where:
        status        | consentStatus                                       | resourceStatus                                    | size
        "AVAILABLE"   | UpdateConsentData.StatusEnum.AUTHORISED             | ResponseResourceListData.StatusEnum.AVAILABLE     | 1
        "UNAVAILABLE" | UpdateConsentData.StatusEnum.AUTHORISED             | ResponseResourceListData.StatusEnum.UNAVAILABLE   | 1
    }

    @Unroll
    def "Calling resources with just the permission for the resources API throws an error"() {
        given:
        def p = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.RESOURCES_READ, testConsentInvestment.getConsentId()))

        when:
        def response = resourcesService.getResourceList(Pageable.unpaged(), testConsent.getConsentId())

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.NOT_FOUND
        e.getLocalizedMessage() == "Resource not found, no appropriate permissions attached to consent"

        consentPermissionsRepository.delete(p)
    }

    def "we get an empty list when only customers permissions are granted"() {
        when:
        def p1 = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ, testConsent.getConsentId()))
        def p2 = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.CUSTOMERS_PERSONAL_ADITTIONALINFO_READ, testConsent.getConsentId()))
        ResponseResourceList response1 = resourcesService.getResourceList(Pageable.unpaged(), testConsent.consentId)

        then:
        response1.getData().isEmpty()
        consentPermissionsRepository.delete(p1)
        consentPermissionsRepository.delete(p2)

        when:
        def b1 = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.CUSTOMERS_BUSINESS_IDENTIFICATIONS_READ, testConsent.getConsentId()))
        def b2 = consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.CUSTOMERS_BUSINESS_ADITTIONALINFO_READ, testConsent.getConsentId()))
        ResponseResourceList response2 = resourcesService.getResourceList(Pageable.unpaged(), testConsent.consentId)

        then:
        response2.getData().isEmpty()
        consentPermissionsRepository.delete(b1)
        consentPermissionsRepository.delete(b2)
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

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }

}
