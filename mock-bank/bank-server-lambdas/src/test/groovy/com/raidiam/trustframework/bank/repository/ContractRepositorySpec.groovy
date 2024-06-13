package com.raidiam.trustframework.bank.repository

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.TestEntityDataFactory
import com.raidiam.trustframework.bank.domain.ConsentContractEntity
import com.raidiam.trustframework.bank.enums.ResourceType
import com.raidiam.trustframework.mockbank.models.generated.EnumProductSubType
import com.raidiam.trustframework.mockbank.models.generated.EnumProductType
import io.micronaut.data.model.Pageable
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Stepwise

import javax.inject.Inject

import static com.raidiam.trustframework.bank.TestEntityDataFactory.*

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class ContractRepositorySpec extends CleanupSpecification {

    @Inject
    TestEntityDataFactory testEntityDataFactory

    def "We can get pageable contracts"() {
        given:
        def CONTRACT_TYPE = ResourceType.FINANCING
        def accountHolder = accountHolderRepository.save(anAccountHolder())
        def consent = consentRepository.save(aConsent(accountHolder.getAccountHolderId()))
        def contract1 = contractsRepository.save(aContractEntity(accountHolder.getAccountHolderId(), CONTRACT_TYPE,
                EnumProductType.FINANCIAMENTOS.toString(), EnumProductSubType.CUSTEIO.toString(), "AVAILABLE"))
        def contract2 = contractsRepository.save(aContractEntity(accountHolder.getAccountHolderId(), CONTRACT_TYPE,
                EnumProductType.FINANCIAMENTOS.toString(), EnumProductSubType.CUSTEIO.toString(), "AVAILABLE"))
        def contract3 = contractsRepository.save(aContractEntity(accountHolder.getAccountHolderId(), CONTRACT_TYPE,
                EnumProductType.FINANCIAMENTOS.toString(), EnumProductSubType.CUSTEIO.toString(), "AVAILABLE"))

        consentContractRepository.save(new ConsentContractEntity(consent, contract1))
        consentContractRepository.save(new ConsentContractEntity(consent, contract2))
        consentContractRepository.save(new ConsentContractEntity(consent, contract3))

        when:
        def page1 = consentContractRepository.findByConsentIdAndContractContractTypeOrderByCreatedAtAsc(consent.consentId, CONTRACT_TYPE.name(), Pageable.from(0, 2))
        def page2 = consentContractRepository.findByConsentIdAndContractContractTypeOrderByCreatedAtAsc(consent.consentId, CONTRACT_TYPE.name(), Pageable.from(1, 2))


        then:
        // we can see the total number of contracts on each page
        page1.totalSize == 3
        page2.totalSize == 3

        // first page has 2 contracts
        page1.size() == 2
        page1.first().contract == contract1
        page1.last().contract == contract2

        // second page has 1 contract
        page2.size() == 1
        page2.first().contract == contract3

        // page 1 has no contracts from page 2
        !page1.collect().contains(page2.first())

        when:
        def pageAll = consentContractRepository.findByConsentIdAndContractContractTypeOrderByCreatedAtAsc(consent.consentId, CONTRACT_TYPE.name(), Pageable.from(0))

        then:
        // page has all contracts
        pageAll.size() == 3

        //and only one page
        pageAll.getTotalPages() == 1

        // sorted by createAt (first contract is always older than second contract and second older then third etc)
        pageAll.collect().get(0).createdAt.before(pageAll.collect().get(1).createdAt)
        pageAll.collect().get(1).createdAt.before(pageAll.collect().get(2).createdAt)
    }

    def "We can get pageable warranties"() {
        given:
        def CONTRACT_TYPE = ResourceType.FINANCING
        def accountHolder = accountHolderRepository.save(anAccountHolder())
        def consent = consentRepository.save(aConsent(accountHolder.getAccountHolderId()))
        def contract = contractsRepository.save(aContractEntity(accountHolder.getAccountHolderId(), CONTRACT_TYPE,
                EnumProductType.FINANCIAMENTOS.toString(), EnumProductSubType.CUSTEIO.toString(), "AVAILABLE"))
        consentContractRepository.save(new ConsentContractEntity(consent, contract))
        def warranty1 = aWarranty(contract)
        contractWarrantiesRepository.save(warranty1)
        def warranty2 = aWarranty(contract)
        contractWarrantiesRepository.save(warranty2)
        def warranty3 = aWarranty(contract)
        contractWarrantiesRepository.save(warranty3)

        when:
        def page1 = contractWarrantiesRepository.findByContractOrderByCreatedAtAsc(contract, Pageable.from(0, 2))
        def page2 = contractWarrantiesRepository.findByContractOrderByCreatedAtAsc(contract, Pageable.from(1, 2))

        then:
        // total number of warranties 3
        page1.totalSize == 3
        page2.totalSize == 3

        // first page has 2 warranties
        page1.size() == 2
        page1.first() == warranty1
        page1.last() == warranty2

        // second page has 1 warranty
        page2.size() == 1
        page2.first() == warranty3

        // page 1 has no warranties from page 2
        !page1.collect().contains(page2.first())

        when:
        def pageAll = contractWarrantiesRepository.findByContractOrderByCreatedAtAsc(contract, Pageable.from(0))

        then:
        // page have all warranties
        pageAll.size() == 3

        // only one page
        pageAll.getTotalPages() == 1

        // sorted by createAt (first contract is always older than second contract and second older then third)
        pageAll.collect().get(0).createdAt.before(pageAll.collect().get(1).createdAt)
        pageAll.collect().get(1).createdAt.before(pageAll.collect().get(2).createdAt)
    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }
}
