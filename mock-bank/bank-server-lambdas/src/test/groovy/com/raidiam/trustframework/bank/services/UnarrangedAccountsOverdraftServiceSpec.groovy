package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.TestEntityDataFactory
import com.raidiam.trustframework.bank.domain.AccountHolderEntity
import com.raidiam.trustframework.bank.domain.ConsentContractEntity
import com.raidiam.trustframework.bank.domain.ConsentEntity
import com.raidiam.trustframework.bank.domain.ContractEntity
import com.raidiam.trustframework.bank.enums.ResourceType
import com.raidiam.trustframework.mockbank.models.generated.EnumConsentPermissions
import com.raidiam.trustframework.mockbank.models.generated.EnumConsentStatus
import com.raidiam.trustframework.mockbank.models.generated.ProductSubType
import com.raidiam.trustframework.mockbank.models.generated.ProductType
import io.micronaut.data.model.Pageable
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Stepwise

import javax.inject.Inject

import static com.raidiam.trustframework.bank.TestEntityDataFactory.*
@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class UnarrangedAccountsOverdraftServiceSpec extends CleanupSpecification {

    @Inject
    UnarrangedAccountsOverdraftService unarrangedAccountsOverdraftService

    @Shared
    ConsentEntity testConsent
    @Shared
    AccountHolderEntity testAccountHolder

    @Shared
    ContractEntity testContract

    @Inject
    TestEntityDataFactory testEntityDataFactory

    def setup() {
        if (runSetup) {
            testAccountHolder = accountHolderRepository.save(anAccountHolder())
            testConsent = consentRepository.save(aConsent(testAccountHolder.getAccountHolderId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.UNARRANGED_ACCOUNTS_OVERDRAFT_READ, testConsent.getConsentId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ, testConsent.getConsentId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ, testConsent.getConsentId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ, testConsent.getConsentId()))

            testContract = testEntityDataFactory.createAndSaveFullContract(
                    testAccountHolder.getAccountHolderId(),
                    ResourceType.UNARRANGED_ACCOUNT_OVERDRAFT,
                    ProductType.ADIANTAMENTO_A_DEPOSITANTES.toString(),
                    ProductSubType.ADIANTAMENTO_A_DEPOSITANTES.toString())

            def consentContract = new ConsentContractEntity(testConsent, testContract)
            consentContractRepository.save(consentContract)

            runSetup = false
        }
    }

    def "We can get a contract and check its values"() {
        when:
        def savedContractOptional =  contractsRepository.findByContractIdAndContractType(testContract.getContractId(), ResourceType.UNARRANGED_ACCOUNT_OVERDRAFT.name())

        then:
        savedContractOptional.isPresent()

        when:
        def savedContract = savedContractOptional.get()
        def savedInterestRates = contractInterestRatesRepository.findAll().get(0)
        def savedContractedFees = contractedFeesRepository.findAll().get(0)
        def savedFinanceCharges = contractedFinanceChargesRepository.findAll().get(0)
        def newContractPage = unarrangedAccountsOverdraftService.getUnarrangedOverdraftContractV2(testConsent.getConsentId().toString(), testContract.getContractId())
        def newContract = newContractPage.getData()

        then:
        // Check all values from contract
        newContract.getContractNumber() == savedContract.getContractNumber()
        newContract.getIpocCode() == savedContract.getIpocCode()
        newContract.getProductName() == savedContract.getProductName()
        newContract.getProductType().toString() == savedContract.getProductType()
        newContract.getProductSubType().toString() == savedContract.getProductSubType()
        newContract.getContractDate() == savedContract.getContractDate()
        newContract.getSettlementDate() == savedContract.getSettlementDate()
        newContract.getCurrency() == savedContract.getCurrency()
        newContract.getDueDate() == savedContract.getDueDate()
        newContract.getInstalmentPeriodicity().toString() == savedContract.getInstalmentPeriodicity()
        newContract.getInstalmentPeriodicityAdditionalInfo().toString() == savedContract.getInstalmentPeriodicityAdditionalInfo()
        newContract.getFirstInstalmentDueDate() == savedContract.getFirstInstalmentDueDate()
        newContract.getAmortizationScheduled().toString() == savedContract.getAmortizationScheduled()
        newContract.getAmortizationScheduledAdditionalInfo().toString() == savedContract.getAmortizationScheduledAdditionalInfo()
        newContract.getInterestRates() == List.of(savedInterestRates.getUnarrangedAccountsOverdraftDTOV2())
        newContract.getContractedFees() == List.of(savedContractedFees.getUnarrangedAccountOverdraftFeeDTOV2())
        newContract.getContractedFinanceCharges() == List.of(savedFinanceCharges.getUnarrangedAccountsOverdraftDTOV2())
    }


    def "We can get all contracts and check their values"() {
        when:
        def contracts = unarrangedAccountsOverdraftService.getUnarrangedOverdraftContractList(Pageable.unpaged(), testConsent.getConsentId())

        then:
        contracts.getData().size() == 1
        contracts.getData().get(0) == testContract.getOverdraftAccountsDTOList()
    }

    def "We can get the Warranty information for a contract"() {
        when:
        def warranties = testContract.getContractWarranties()

        then:
        warranties.size() == 1

        when:
        def contractWarrantyDTO = warranties.first().getUnarrangedAccountOverdraftWarrantiesV2()
        def foundWarranties = unarrangedAccountsOverdraftService.getUnarrangedAccountsOverdraftWarrantiesV2(Pageable.unpaged(), testConsent.getConsentId().toString(), testContract.getContractId())

        then:
        foundWarranties.getData().size() == 1
        contractWarrantyDTO == foundWarranties.getData().get(0)
    }

    def "We can get the scheduled instalments for a contract"() {
        when:
        def instalments = unarrangedAccountsOverdraftService.getUnarrangedOverdraftScheduledInstalmentsV2(testConsent.getConsentId().toString(), testContract.getContractId())

        then:
        instalments.getData().getPaidInstalments().toInteger() == testContract.getPaidInstalments()
        instalments .getData().getTypeNumberOfInstalments().toString() == testContract.getTypeNumberOfInstalments()
    }

    def "We can get the payments information for a contract"() {
        when:
        def paymentsResponse = unarrangedAccountsOverdraftService.getUnarrangedOverdraftPaymentsV2(testConsent.getConsentId().toString(), testContract.getContractId())

        then:
        Double.valueOf(paymentsResponse.getData().getContractOutstandingBalance()) == testContract.getContractOutstandingBalance()
        paymentsResponse.getData().getPaidInstalments().toInteger() == testContract.getPaidInstalments()
    }

    def "we can get pages"() {
        given:
        var pageSize = 2
        def contract1 = testEntityDataFactory.createAndSaveFullContract(
                testAccountHolder.getAccountHolderId(),
                ResourceType.UNARRANGED_ACCOUNT_OVERDRAFT,
                ProductType.ADIANTAMENTO_A_DEPOSITANTES.toString(),
                ProductSubType.ADIANTAMENTO_A_DEPOSITANTES.toString())
        consentContractRepository.save(new ConsentContractEntity(testConsent, contract1))
        def contract2 = testEntityDataFactory.createAndSaveFullContract(
                testAccountHolder.getAccountHolderId(),
                ResourceType.UNARRANGED_ACCOUNT_OVERDRAFT,
                ProductType.ADIANTAMENTO_A_DEPOSITANTES.toString(),
                ProductSubType.ADIANTAMENTO_A_DEPOSITANTES.toString())
        consentContractRepository.save(new ConsentContractEntity(testConsent, contract2))

        when:
        //get first page
        def page1 = unarrangedAccountsOverdraftService
                .getUnarrangedOverdraftContractList(Pageable.from(0, pageSize), testConsent.getConsentId())

        then:
        !page1.getData().empty
        page1.getMeta().getTotalPages() == pageSize

        when:
        //get second page
        def page2 = unarrangedAccountsOverdraftService
                .getUnarrangedOverdraftContractList(Pageable.from(1, pageSize), testConsent.getConsentId())

        then:
        !page2.getData().empty
        page2.getMeta().getTotalPages() == pageSize

        and:
        page1.getMeta().getTotalRecords() == page1.getData().size() + page2.getData().size()
        page2.getMeta().getTotalRecords() == page1.getData().size() + page2.getData().size()
        //contract from page2 is not contain in page1
        def accFromPage2 = page2.getData().first()
        !page1.getData().contains(accFromPage2)
    }

    def "we cannot get a response when the consent not have permissions"() {
        setup:
        def errorMessage = "You do not have the correct permission"
        def testAccountHolder2 = accountHolderRepository.save(anAccountHolder())
        def testContract2 = testEntityDataFactory.createAndSaveFullContract(
                testAccountHolder2.getAccountHolderId(),
                ResourceType.UNARRANGED_ACCOUNT_OVERDRAFT,
                ProductType.ADIANTAMENTO_A_DEPOSITANTES.toString(),
                ProductSubType.ADIANTAMENTO_A_DEPOSITANTES.toString())
        def testConsent2 = consentRepository.save(aConsent(testAccountHolder2.getAccountHolderId()))
        consentContractRepository.save(new ConsentContractEntity(testConsent2, testContract2))

        when:
        unarrangedAccountsOverdraftService.getUnarrangedOverdraftContractList(Pageable.unpaged(), testConsent2.getConsentId())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.FORBIDDEN
        e1.getMessage() == errorMessage

        when:
        unarrangedAccountsOverdraftService.getUnarrangedOverdraftContractV2(testConsent2.getConsentId(), testContract2.getContractId())

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.FORBIDDEN
        e2.getMessage() == errorMessage

        when:
        unarrangedAccountsOverdraftService.getUnarrangedOverdraftScheduledInstalmentsV2(testConsent2.getConsentId(), testContract2.getContractId())

        then:
        HttpStatusException e3 = thrown()
        e3.status == HttpStatus.FORBIDDEN
        e3.getMessage() == errorMessage

        when:
        unarrangedAccountsOverdraftService.getUnarrangedAccountsOverdraftWarrantiesV2(Pageable.unpaged(), testConsent2.getConsentId(), testContract2.getContractId())

        then:
        HttpStatusException e4 = thrown()
        e4.status == HttpStatus.FORBIDDEN
        e4.getMessage() == errorMessage

        when:
        unarrangedAccountsOverdraftService.getUnarrangedOverdraftPaymentsV2(testConsent2.getConsentId(), testContract2.getContractId())

        then:
        HttpStatusException e5 = thrown()
        e5.status == HttpStatus.FORBIDDEN
        e5.getMessage() == errorMessage
    }

    def "we cannot get a response when the consent owner is not the contract owner"() {
        setup:
        def errorMessage = "Forbidden, consent owner does not match contract owner!"
        def testAccountHolder2 = accountHolderRepository.save(anAccountHolder())
        def testContract2 = testEntityDataFactory.createAndSaveFullContract(
                testAccountHolder2.getAccountHolderId(),
                ResourceType.UNARRANGED_ACCOUNT_OVERDRAFT,
                ProductType.ADIANTAMENTO_A_DEPOSITANTES.toString(),
                ProductSubType.ADIANTAMENTO_A_DEPOSITANTES.toString())
        consentContractRepository.save(new ConsentContractEntity(testConsent, testContract2))

        when:
        unarrangedAccountsOverdraftService.getUnarrangedOverdraftContractList(Pageable.unpaged(), testConsent.getConsentId())

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.FORBIDDEN
        e.getMessage() == errorMessage

        when:
        unarrangedAccountsOverdraftService.getUnarrangedOverdraftContractV2(testConsent.getConsentId(), testContract2.getContractId())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.FORBIDDEN
        e1.getMessage() == errorMessage

        when:
        unarrangedAccountsOverdraftService.getUnarrangedOverdraftScheduledInstalmentsV2(testConsent.getConsentId(), testContract2.getContractId())

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.FORBIDDEN
        e2.getMessage() == errorMessage

        when:
        unarrangedAccountsOverdraftService.getUnarrangedAccountsOverdraftWarrantiesV2(Pageable.unpaged(), testConsent.getConsentId(), testContract2.getContractId())

        then:
        HttpStatusException e3 = thrown()
        e3.status == HttpStatus.FORBIDDEN
        e3.getMessage() == errorMessage

        when:
        unarrangedAccountsOverdraftService.getUnarrangedOverdraftPaymentsV2(testConsent.getConsentId(), testContract2.getContractId())

        then:
        HttpStatusException e4 = thrown()
        e4.status == HttpStatus.FORBIDDEN
        e4.getMessage() == errorMessage
    }

    def "we cannot get response when contract does not cover account"() {
        setup:
        def errorMessage = "Bad request, consent does not cover this contract!"
        def testAccountHolder2 = accountHolderRepository.save(anAccountHolder())
        def testContract2 = testEntityDataFactory.createAndSaveFullContract(
                testAccountHolder2.getAccountHolderId(),
                ResourceType.UNARRANGED_ACCOUNT_OVERDRAFT,
                ProductType.ADIANTAMENTO_A_DEPOSITANTES.toString(),
                ProductSubType.ADIANTAMENTO_A_DEPOSITANTES.toString())

        when:
        unarrangedAccountsOverdraftService.getUnarrangedOverdraftContractV2(testConsent.getConsentId(), testContract2.getContractId())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.BAD_REQUEST
        e1.getMessage() == errorMessage

        when:
        unarrangedAccountsOverdraftService.getUnarrangedOverdraftScheduledInstalmentsV2(testConsent.getConsentId(), testContract2.getContractId())

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.BAD_REQUEST
        e2.getMessage() == errorMessage

        when:
        unarrangedAccountsOverdraftService.getUnarrangedAccountsOverdraftWarrantiesV2(Pageable.unpaged(), testConsent.getConsentId(), testContract2.getContractId())

        then:
        HttpStatusException e3 = thrown()
        e3.status == HttpStatus.BAD_REQUEST
        e3.getMessage() == errorMessage

        when:
        unarrangedAccountsOverdraftService.getUnarrangedOverdraftPaymentsV2(testConsent.getConsentId(), testContract2.getContractId())

        then:
        HttpStatusException e4 = thrown()
        e4.status == HttpStatus.BAD_REQUEST
        e4.getMessage() == errorMessage
    }

    def "we cannot get response without authorised status"() {
        setup:
        def errorMessage = "Bad request, consent not Authorised!"
        testConsent.setStatus(EnumConsentStatus.AWAITING_AUTHORISATION.name())
        consentRepository.update(testConsent)

        when:
        unarrangedAccountsOverdraftService.getUnarrangedOverdraftContractList(Pageable.unpaged(), testConsent.getConsentId())

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.UNAUTHORIZED
        e.getMessage() == errorMessage

        when:
        unarrangedAccountsOverdraftService.getUnarrangedOverdraftContractV2(testConsent.getConsentId(), testContract.getContractId())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.UNAUTHORIZED
        e1.getMessage() == errorMessage

        when:
        unarrangedAccountsOverdraftService.getUnarrangedOverdraftPaymentsV2(testConsent.getConsentId(), testContract.getContractId())

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.UNAUTHORIZED
        e2.getMessage() == errorMessage

        when:
        unarrangedAccountsOverdraftService.getUnarrangedOverdraftScheduledInstalmentsV2( testConsent.getConsentId(), testContract.getContractId())

        then:
        HttpStatusException e3 = thrown()
        e3.status == HttpStatus.UNAUTHORIZED
        e3.getMessage() == errorMessage

        when:
        unarrangedAccountsOverdraftService.getUnarrangedAccountsOverdraftWarrantiesV2(Pageable.unpaged(),testConsent.getConsentId(), testContract.getContractId())

        then:
        HttpStatusException e4 = thrown()
        e4.status == HttpStatus.UNAUTHORIZED
        e4.getMessage() == errorMessage
    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }
}

