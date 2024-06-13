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
class LoansServiceSpec extends CleanupSpecification {

    @Inject
    LoansService loansService

    @Shared
    ConsentEntity testConsent
    @Shared
    AccountHolderEntity testAccountHolder

    @Shared
    ContractEntity testContract

    @Shared
    ContractEntity testContractUnavailable

    @Inject
    TestEntityDataFactory testEntityDataFactory

    def setup() {
        if (runSetup) {
            testAccountHolder = accountHolderRepository.save(anAccountHolder())
            testConsent = consentRepository.save(aConsent(testAccountHolder.getAccountHolderId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.LOANS_READ, testConsent.getConsentId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.LOANS_WARRANTIES_READ, testConsent.getConsentId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.LOANS_SCHEDULED_INSTALMENTS_READ, testConsent.getConsentId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.LOANS_PAYMENTS_READ, testConsent.getConsentId()))

            testContract = testEntityDataFactory.createAndSaveFullContract(
                    testAccountHolder.getAccountHolderId(),
                    ResourceType.LOAN,
                    "EMPRESTIMOS",
                    "CONTA_GARANTIDA")

            def consentContract = new ConsentContractEntity(testConsent, testContract)
            consentContractRepository.save(consentContract)

            testContractUnavailable = testEntityDataFactory.createAndSaveFullContractUnavailable(
                    testAccountHolder.getAccountHolderId(),
                    ResourceType.LOAN,
                    "EMPRESTIMOS",
                    "CONTA_GARANTIDA")

            def consentContractUnavailable = new ConsentContractEntity(testConsent, testContractUnavailable)
            consentContractRepository.save(consentContractUnavailable)

            runSetup = false
        }
    }

    def "We can get a contract and check its values"() {
        when:
        def savedContractOptional =  contractsRepository.findByContractIdAndContractType(testContract.getContractId(), ResourceType.LOAN.name())

        then:
        savedContractOptional.isPresent()

        when:
        def savedContract = savedContractOptional.get()
        def savedInterestRates = contractInterestRatesRepository.findAll().get(0)
        def savedContractedFees = contractedFeesRepository.findAll().get(0)
        def savedFinanceCharges = contractedFinanceChargesRepository.findAll().get(0)
        def newContractPage = loansService.getLoanContractV2(testConsent.getConsentId().toString(), testContract.getContractId())
        def newContract = newContractPage.getData()

        then:
        // Check all values from loan
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
        newContract.getCnpjConsignee() == savedContract.getCompanyCnpj()
        newContract.getInterestRates() == List.of(savedInterestRates.getLoansDTOV2())
        newContract.getContractedFees() == List.of(savedContractedFees.getLoansDTOV2())
        newContract.getContractedFinanceCharges() == List.of(savedFinanceCharges.getLoansDTOV2())
    }


    def "We can get all contracts and check their values"() {
        when:
        def contracts = loansService.getLoansContractList(Pageable.unpaged(), testConsent.getConsentId())

        then:
        contracts.getData().size() == 1
        contracts.getData().get(0) == testContract.getLoanDTOList()
    }

    def "We can get the Warranty information for a contract"() {
        when:
        def warranties = testContract.getContractWarranties()

        then:
        warranties.size() == 1

        when:
        def contractWarrantyDTO = warranties.first().getLoansWarrantiesV2()
        def foundWarranties = loansService.getLoansWarrantiesV2(Pageable.unpaged(), testConsent.getConsentId().toString(), testContract.getContractId())

        then:
        foundWarranties.getData().size() == 1
        contractWarrantyDTO == foundWarranties.getData().get(0)
    }

    def "We can't get the Warranty information for v2 contract with unavailable status"() {
        when:
        def warranties = testContractUnavailable.getContractWarranties()

        then:
        warranties.size() == 1

        when:
        loansService.getLoansWarrantiesV2(Pageable.unpaged(), testConsent.getConsentId(), testContractUnavailable.getContractId())

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.FORBIDDEN
    }

    def "We can get the scheduled instalments for a contract"() {
        when:
        def instalments = loansService.getLoanScheduledInstalmentsV2(testConsent.getConsentId().toString(), testContract.getContractId())

        then:
        instalments.getData().getPaidInstalments().toInteger() == testContract.getPaidInstalments()
        instalments .getData().getTypeNumberOfInstalments().toString() == testContract.getTypeNumberOfInstalments()
    }

    def "We can't get the scheduled instalments for v2 contract with unavailable status"() {
        when:
        def instalments = loansService.getLoanScheduledInstalmentsV2(testConsent.getConsentId(), testContractUnavailable.getContractId())

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.FORBIDDEN
    }

    def "We can get the payments information for a contract"() {
        when:
        def paymentsResponse = loansService.getLoanPaymentsV2(testConsent.getConsentId().toString(), testContract.getContractId())

        then:
        Double.valueOf(paymentsResponse.getData().getContractOutstandingBalance()) == testContract.getContractOutstandingBalance()
        paymentsResponse.getData().getPaidInstalments().toInteger() == testContract.getPaidInstalments()
    }

    def "We can't get the payments information for v2 contract with unavailable status"() {
        when:
        loansService.getLoanPaymentsV2(testConsent.getConsentId(), testContractUnavailable.getContractId())

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.FORBIDDEN
    }

    def "we can get pages"() {
        given:
        def contract1 = testEntityDataFactory.createAndSaveFullContract(
                testAccountHolder.getAccountHolderId(),
                ResourceType.LOAN,
                "EMPRESTIMOS",
                "CONTA_GARANTIDA")
        consentContractRepository.save(new ConsentContractEntity(testConsent, contract1))
        def contract2 = testEntityDataFactory.createAndSaveFullContract(
                testAccountHolder.getAccountHolderId(),
                ResourceType.LOAN,
                "EMPRESTIMOS",
                "CONTA_GARANTIDA")
        consentContractRepository.save(new ConsentContractEntity(testConsent, contract2))

        when:
        //get first page
        def pages1 = loansService.getLoansContractList(Pageable.from(0, 2), testConsent.getConsentId())

        then:
        !pages1.getData().empty
        pages1.getMeta().getTotalPages() == 2

        when:
        //get second page
        def pages2 = loansService.getLoansContractList(Pageable.from(1, 2), testConsent.getConsentId())

        then:
        !pages2.getData().empty
        pages2.getMeta().getTotalPages() == 2

        and:
        pages1.getMeta().getTotalRecords() == pages1.getData().size() + pages2.getData().size()
        pages2.getMeta().getTotalRecords() == pages1.getData().size() + pages2.getData().size()
        //contract from page2 is not contain in page1
        def accFromPage2 = pages2.getData().first()
        !pages1.getData().contains(accFromPage2)
    }

    def "we cannot get a response when the consent not have permissions"() {
        setup:
        def errorMessage = "You do not have the correct permission"
        def testAccountHolder2 = accountHolderRepository.save(anAccountHolder())
        def testContract2 = testEntityDataFactory.createAndSaveFullContract(
                testAccountHolder2.getAccountHolderId(),
                ResourceType.LOAN,
                "EMPRESTIMOS",
                "CONTA_GARANTIDA")
        def testConsent2 = consentRepository.save(aConsent(testAccountHolder2.getAccountHolderId()))
        consentContractRepository.save(new ConsentContractEntity(testConsent2, testContract2))

        when:
        loansService.getLoansContractList(Pageable.unpaged(), testConsent2.getConsentId())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.FORBIDDEN
        e1.getMessage() == errorMessage

        when:
        loansService.getLoanContractV2(testConsent2.getConsentId(), testContract2.getContractId())

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.FORBIDDEN
        e2.getMessage() == errorMessage

        when:
        loansService.getLoanScheduledInstalmentsV2(testConsent2.getConsentId(), testContract2.getContractId())

        then:
        HttpStatusException e3 = thrown()
        e3.status == HttpStatus.FORBIDDEN
        e3.getMessage() == errorMessage

        when:
        loansService.getLoansWarrantiesV2(Pageable.unpaged(), testConsent2.getConsentId(), testContract2.getContractId())

        then:
        HttpStatusException e4 = thrown()
        e4.status == HttpStatus.FORBIDDEN
        e4.getMessage() == errorMessage

        when:
        loansService.getLoanPaymentsV2(testConsent2.getConsentId(), testContract2.getContractId())

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
                ResourceType.LOAN,
                "EMPRESTIMOS",
                "CONTA_GARANTIDA")
        consentContractRepository.save(new ConsentContractEntity(testConsent, testContract2))

        when:
        loansService.getLoanContractV2(testConsent.getConsentId(), testContract2.getContractId())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.FORBIDDEN
        e1.getMessage() == errorMessage

        when:
        loansService.getLoanScheduledInstalmentsV2(testConsent.getConsentId(), testContract2.getContractId())

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.FORBIDDEN
        e2.getMessage() == errorMessage

        when:
        loansService.getLoansWarrantiesV2(Pageable.unpaged(), testConsent.getConsentId(), testContract2.getContractId())

        then:
        HttpStatusException e3 = thrown()
        e3.status == HttpStatus.FORBIDDEN
        e3.getMessage() == errorMessage

        when:
        loansService.getLoanPaymentsV2(testConsent.getConsentId(), testContract2.getContractId())

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
                ResourceType.LOAN,
                "EMPRESTIMOS",
                "CONTA_GARANTIDA")

        when:
        loansService.getLoanContractV2(testConsent.getConsentId(), testContract2.getContractId())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.BAD_REQUEST
        e1.getMessage() == errorMessage

        when:
        loansService.getLoanScheduledInstalmentsV2(testConsent.getConsentId(), testContract2.getContractId())

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.BAD_REQUEST
        e2.getMessage() == errorMessage

        when:
        loansService.getLoansWarrantiesV2(Pageable.unpaged(), testConsent.getConsentId(), testContract2.getContractId())

        then:
        HttpStatusException e3 = thrown()
        e3.status == HttpStatus.BAD_REQUEST
        e3.getMessage() == errorMessage

        when:
        loansService.getLoanPaymentsV2(testConsent.getConsentId(), testContract2.getContractId())

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
        loansService.getLoansContractList(Pageable.unpaged(), testConsent.getConsentId())

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.UNAUTHORIZED
        e.getMessage() == errorMessage

        when:
        loansService.getLoanContractV2(testConsent.getConsentId(), testContract.getContractId())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.UNAUTHORIZED
        e1.getMessage() == errorMessage

        when:
        loansService.getLoanPaymentsV2(testConsent.getConsentId(), testContract.getContractId())

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.UNAUTHORIZED
        e2.getMessage() == errorMessage

        when:
        loansService.getLoanScheduledInstalmentsV2( testConsent.getConsentId(), testContract.getContractId())

        then:
        HttpStatusException e3 = thrown()
        e3.status == HttpStatus.UNAUTHORIZED
        e3.getMessage() == errorMessage

        when:
        loansService.getLoansWarrantiesV2(Pageable.unpaged(),testConsent.getConsentId(), testContract.getContractId())

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

