package com.raidiam.trustframework.bank.services.admin

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.TestRequestDataFactory
import com.raidiam.trustframework.bank.domain.AccountHolderEntity
import com.raidiam.trustframework.bank.services.ContractAdminService
import com.raidiam.trustframework.bank.utils.BankLambdaUtils
import com.raidiam.trustframework.mockbank.models.generated.ContractWarrantiesData
import com.raidiam.trustframework.mockbank.models.generated.EnumContractType
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Stepwise

import javax.inject.Inject

import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccountHolder

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class ContractAdminServiceSpec extends CleanupSpecification {

    @Inject
    ContractAdminService contractAdminService
    @Shared
    AccountHolderEntity testAccountHolder

    def setup() {
        if (runSetup) {
            testAccountHolder = accountHolderRepository.save(anAccountHolder("00000000000", "CPF"))
            runSetup = false
        }
    }

    def "we can get add update delete Contract"() {
        given:
        def contractDto = TestRequestDataFactory.createContract(EnumContractType.LOAN).getData()

        when://Add Contract
        def contractFromDB = contractAdminService.addContract(contractDto.getContractType().toString(), contractDto, testAccountHolder.getAccountHolderId().toString())

        then:
        contractFromDB.getContractId() != null
        contractFromDB.getInterestRates().size() == 1
        contractFromDB.getContractedFees().size() == 1
        contractFromDB.getContractedFinanceCharges().size() == 1
        contractFromDB.getBalloonPayments().size() == 2
        contractFromDB.getReleases().size() == 2
        contractFromDB.getReleases().get(0).getOverParcelFees().size() == 1
        contractFromDB.getReleases().get(0).getOverParcelCharges().size() == 1

        when: //Update Contract
        var forUpdate = TestRequestDataFactory.createEditedContract()
        def updatedContract = contractAdminService.updateContract(contractFromDB.getContractType().toString(), contractFromDB.getContractId().toString(), forUpdate)

        then:
        updatedContract.getContractId() != null
        updatedContract.getDueDate() == forUpdate.getDueDate()
        updatedContract.getCurrency() == forUpdate.getCurrency()
        updatedContract.getProductType() == forUpdate.getProductType()
        updatedContract.getStatus() == forUpdate.getStatus()
        updatedContract.getCompanyCnpj() == forUpdate.getCompanyCnpj()
        updatedContract.getAmortizationScheduled() == forUpdate.getAmortizationScheduled()
        updatedContract.getAmortizationScheduledAdditionalInfo() == forUpdate.getAmortizationScheduledAdditionalInfo()
        updatedContract.getCet() == forUpdate.getCet()
        updatedContract.getContractAmount() == forUpdate.getContractAmount()
        updatedContract.getContractDate() == forUpdate.getContractDate()
        updatedContract.getContractNumber() == forUpdate.getContractNumber()
        updatedContract.getContractOutstandingBalance() == forUpdate.getContractOutstandingBalance()
        updatedContract.getContractRemainingNumber() == forUpdate.getContractRemainingNumber()
        updatedContract.getContractType() == contractFromDB.getContractType()
        updatedContract.getDisbursementDate() == forUpdate.getDisbursementDate()
        updatedContract.getDueInstalments() == forUpdate.getDueInstalments()
        updatedContract.getFirstInstalmentDueDate() == forUpdate.getFirstInstalmentDueDate()
        updatedContract.getInstalmentPeriodicity() == forUpdate.getInstalmentPeriodicity()
        updatedContract.getInstalmentPeriodicityAdditionalInfo() == forUpdate.getInstalmentPeriodicityAdditionalInfo()
        updatedContract.getIpocCode() == forUpdate.getIpocCode()
        updatedContract.getPaidInstalments() == forUpdate.getPaidInstalments()
        updatedContract.getPastDueInstalments() == forUpdate.getPastDueInstalments()
        updatedContract.getProductName() == forUpdate.getProductName()
        updatedContract.getProductSubType() == forUpdate.getProductSubType()
        updatedContract.getSettlementDate() == forUpdate.getSettlementDate()
        updatedContract.getTotalNumberOfInstalments() == forUpdate.getTotalNumberOfInstalments()
        updatedContract.getTypeContractRemaining() == forUpdate.getTypeContractRemaining()
        updatedContract.getTypeNumberOfInstalments() == forUpdate.getTypeNumberOfInstalments()
        updatedContract.getBalloonPayments().containsAll(forUpdate.getBalloonPayments())
        updatedContract.getContractedFees().containsAll(forUpdate.getContractedFees())
        updatedContract.getContractedFinanceCharges().containsAll(forUpdate.getContractedFinanceCharges())
        updatedContract.getInterestRates().containsAll(forUpdate.getInterestRates().first())

        def responseReleasesDto = updatedContract.getReleases().first()
        def newReleasesDto = forUpdate.getReleases().first()

        responseReleasesDto.getPaymentsId() != null
        responseReleasesDto.getInstalmentId() == newReleasesDto.getInstalmentId()
        responseReleasesDto.getPaidAmount() == newReleasesDto.getPaidAmount()
        responseReleasesDto.getPaidDate() == newReleasesDto.getPaidDate()
        responseReleasesDto.getCurrency() == newReleasesDto.getCurrency()
        responseReleasesDto.getOverParcelCharges().containsAll(newReleasesDto.getOverParcelCharges())
        responseReleasesDto.getOverParcelFees().containsAll(newReleasesDto.getOverParcelFees())


        when: //Get Contract
        def getContract = contractAdminService.getContract(updatedContract.getContractType().toString(), updatedContract.getContractId().toString())

        then:
        getContract != null
        getContract.getContractId() == updatedContract.getContractId()
        getContract.getContractType() == updatedContract.getContractType()


        when://Delete Contract
        contractAdminService.deleteContract(updatedContract.getContractType().toString(), updatedContract.getContractId().toString())
        contractAdminService.getContract(updatedContract.getContractType().toString(), updatedContract.getContractId().toString())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.NOT_FOUND
        e1.getMessage() == "Could not find contract"
    }

    def "we can add update delete Warranties"() {
        given:
        def warranties = List.of(TestRequestDataFactory.createWarranties())
        def contractDto = TestRequestDataFactory.createContract(EnumContractType.LOAN).getData()
        def contractFromDB = contractAdminService.addContract(contractDto.getContractType().toString(), contractDto, testAccountHolder.getAccountHolderId().toString())

        when://Add Warranties
        def warrantiesList = contractAdminService.addWarranties(contractDto.getContractType().toString(), contractFromDB.getContractId().toString(), warranties)

        then:
        warrantiesList.size() == 1

        when://Update Warranties
        var forUpdate = List.of(TestRequestDataFactory.createWarranties())
        def updatedWarrantiesList = contractAdminService.updateWarranties(contractDto.getContractType().toString(), contractFromDB.getContractId().toString(), forUpdate)

        then:
        updatedWarrantiesList != null
        ContractWarrantiesData warrantiesDto = updatedWarrantiesList.first() as ContractWarrantiesData
        warrantiesDto.getCurrency() == forUpdate.first().getCurrency()
        warrantiesDto.getWarrantyAmount() == forUpdate.first().getWarrantyAmount()
        warrantiesDto.getWarrantySubType() == forUpdate.first().getWarrantySubType()
        warrantiesDto.getWarrantyType() == forUpdate.first().getWarrantyType()

        when: //Delete Warranties
        contractAdminService.deleteWarranties(contractDto.getContractType().toString(), contractFromDB.getContractId().toString())

        then:
        BankLambdaUtils.getContract(contractFromDB.getContractId(), contractsRepository, contractDto.getContractType().toString()).getContractWarranties().empty
    }


    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }
}
