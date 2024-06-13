package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.domain.*
import com.raidiam.trustframework.mockbank.models.generated.BusinessAccountType
import com.raidiam.trustframework.mockbank.models.generated.EnumAccountTypeCustomersV2
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
class CustomerServiceSpec extends CleanupSpecification {

    @Inject
    CustomerService customerService

    @Shared
    AccountHolderEntity accountHolder

    @Shared
    AccountEntity account

    @Shared
    ConsentEntity consent

    @Shared
    BusinessIdentificationsEntity testBusinessIdentifications
    @Shared
    BusinessIdentificationsCompanyCnpjEntity testBusinessIdentificationsCompanyCnpj
    @Shared
    BusinessOtherDocumentEntity testBusinessOtherDocument
    @Shared
    BusinessPartyEntity testBusinessParty
    @Shared
    BusinessPostalAddressEntity testBusinessPostalAddress
    @Shared
    BusinessPhoneEntity testBusinessPhone
    @Shared
    BusinessEmailEntity testBusinessEmail
    @Shared
    BusinessFinancialRelationsEntity testBusinessFinancialRelations
    @Shared
    BusinessQualificationsEntity testBusinessQualifications
    @Shared
    BusinessQualificationsEconomicActivitiesEntity testBusinessQualificationsEconomicActivities

    @Shared
    PersonalIdentificationsEntity testPersonalIdentifications
    @Shared
    PersonalCompanyCnpjEntity testPersonalCompanyCnpj
    @Shared
    PersonalOtherDocumentEntity testPersonalOtherDocument
    @Shared
    PersonalPostalAddressEntity testPersonalPostalAddress
    @Shared
    PersonalPhoneEntity testPersonalPhone
    @Shared
    PersonalEmailEntity testPersonalEmail
    @Shared
    PersonalFinancialRelationsEntity testPersonalFinancialRelations
    @Shared
    PersonalQualificationsEntity testPersonalQualifications
    @Shared
    PersonalNationalityEntity testPersonalNationality
    @Shared
    PersonalNationalityDocumentEntity testPersonalNationalityDocument
    @Shared
    PersonalFiliationEntity testPersonalFiliation

    def setup () {
        if(runSetup) {

            accountHolder = accountHolderRepository.save(anAccountHolder())
            account = accountRepository.save(anAccount(accountHolder))
            consent = consentRepository.save(aConsent(accountHolder.getAccountHolderId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ, consent.getConsentId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.CUSTOMERS_PERSONAL_ADITTIONALINFO_READ, consent.getConsentId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.CUSTOMERS_BUSINESS_IDENTIFICATIONS_READ, consent.getConsentId()))
            consentPermissionsRepository.save(aConsentPermission(EnumConsentPermissions.CUSTOMERS_BUSINESS_ADITTIONALINFO_READ, consent.getConsentId()))

            testBusinessIdentifications = businessIdentificationsRepository.save(aBusinessIdentificationEntity(accountHolder.getAccountHolderId()))
            testBusinessIdentificationsCompanyCnpj = businessIdentificationsCompanyCnpjRepository.save(aBusinessIdentificationCompanyCnpjEntity(testBusinessIdentifications))
            testBusinessOtherDocument = businessOtherDocumentRepository.save(aBusinessOtherDocument(testBusinessIdentifications))
            testBusinessParty = businessPartyRepository.save(aBusinessParty(testBusinessIdentifications))
            testBusinessPostalAddress = businessPostalAddressRepository.save(aBusinessPostalAddress(testBusinessIdentifications))
            testBusinessPhone = businessPhoneRepository.save(aBusinessPhone(testBusinessIdentifications))
            testBusinessEmail = businessEmailRepository.save(aBusinessEmail(testBusinessIdentifications))

            def businessFinancialRelations = aBusinessFinancialRelations(accountHolder.getAccountHolderId())
            businessFinancialRelations.setProductServicesType(Set.of(aBusinessFinancialRelationsProductServicesType(businessFinancialRelations)))
            businessFinancialRelations.setProcurators(Set.of(aBusinessFinancialRelationsProcurator(businessFinancialRelations)))
            testBusinessFinancialRelations = businessFinancialRelationsRepository.save(businessFinancialRelations)

            testBusinessQualifications = businessQualificationsRepository.save(aBusinessQualifications(accountHolder.getAccountHolderId()))
            testBusinessQualificationsEconomicActivities = businessQualificationsEconomicActivitiesRepository.save(aBusinessQualificationsEconomicActivities(testBusinessQualifications))

            testPersonalIdentifications = personalIdentificationsRepository.save(aPersonalIdentificationEntity(accountHolder.getAccountHolderId()))
            testPersonalCompanyCnpj = personalCompanyCnpjRepository.save(aPersonalCompanyCnpj(testPersonalIdentifications))
            testPersonalOtherDocument = personalOtherDocumentRepository.save(aPersonalOtherDocument(testPersonalIdentifications))
            testPersonalNationality = personalNationalityRepository.save(aPersonalNationality(testPersonalIdentifications))
            testPersonalNationalityDocument = personalNationalityDocumentRepository.save(aPersonalNationalityDocument(testPersonalNationality))
            testPersonalFiliation = personalFiliationRepository.save(aPersonalFiliation(testPersonalIdentifications))
            testPersonalPostalAddress = personalPostalAddressRepository.save(aPersonalPostalAddress(testPersonalIdentifications))
            testPersonalPhone = personalPhoneRepository.save(aPersonalPhone(testPersonalIdentifications))
            testPersonalEmail = personalEmailRepository.save(aPersonalEmail(testPersonalIdentifications))

            def personalFinancialRelations = aPersonalFinancialRelations(accountHolder.getAccountHolderId())
            personalFinancialRelations.setProductServicesType(Set.of(aPersonalFinancialRelationsProductServicesType(personalFinancialRelations)))
            personalFinancialRelations.setProcurators(Set.of(aPersonalFinancialRelationsProcurator(personalFinancialRelations)))
            testPersonalFinancialRelations = personalFinancialRelationsRepository.save(personalFinancialRelations)

            testPersonalQualifications = personalQualificationsRepository.save(aPersonalQualifications(accountHolder.getAccountHolderId()))

            runSetup = false
        }
    }

    def "we can get business identifications" () {
        when:
        def response = customerService.getBusinessIdentificationsV2(consent.getConsentId())

        then:
        response.getData()
        response.getData().size() == 1
        response.getData().first()

        when:
        def responseData = response.getData().first()

        then:
        responseData.getBrandName() == testBusinessIdentifications.getBrandName()
        responseData.getOtherDocuments().size() == 1
        responseData.getOtherDocuments().first()
        responseData.getOtherDocuments().first().type == aBusinessOtherDocument(testBusinessIdentifications).getType()
        responseData.getParties().size() == 1
        responseData.getContacts().getPostalAddresses().size() == 1
        responseData.getContacts().getEmails().size() == 1
        responseData.getContacts().getPhones().size() == 1
    }

    def "we can get business financial-relations" () {
        when:
        def response = customerService.getBusinessFinancialRelationsV2(consent.getConsentId())

        then:
        response.getData() != null

        when:
        def responseData = response.getData()

        then:
        responseData.getProductsServicesType().size() == 1
        responseData.getProcurators().size() == 1
        responseData.getAccounts().size() == 0

        when:
        consentAccountRepository.save(new ConsentAccountEntity(consent, account))
        def response2 = customerService.getBusinessFinancialRelationsV2(consent.getConsentId())

        then:
        response2.getData() != null

        when:
        def responseData2 = response2.getData()

        then:
        responseData2.getProductsServicesType().size() == 1
        responseData2.getProcurators().size() == 1
        responseData2.getAccounts().size() == 1
        responseData2.getAccounts().get(0).type == EnumAccountTypeCustomersV2.fromValue(account.accountType)
    }

    def "we can get business qualifications" () {
        when:
        def response = customerService.getBusinessQualificationsV2(consent.getConsentId())

        then:
        response.getData()

        when:
        def responseData = response.getData()

        then:
        responseData.getEconomicActivities().size() == 1
    }

    def "we can get personal identifications" () {
        when:
        def response = customerService.getPersonalIdentificationsV2(consent.getConsentId())

        then:
        response.getData()
        response.getData().size() == 1
        response.getData().first()

        when:
        def responseData = response.getData().first()

        then:
        responseData.getBrandName() == testPersonalIdentifications.getBrandName()
        responseData.getCompaniesCnpj().size() == 1
        responseData.getOtherDocuments().size() == 1
        responseData.getNationality().size() == 1
        responseData.getNationality().first().getDocuments().size() == 1
        responseData.getFiliation().size() == 1
        responseData.getContacts().getPostalAddresses().size() == 1
        responseData.getContacts().getEmails().size() == 1
        responseData.getContacts().getPhones().size() == 1
    }

    def "we can get personal financial-relations" () {
        when:
        def response = customerService.getPersonalFinancialRelations(consent.getConsentId())

        then:
        response.getData() != null

        when:
        def responseData = response.getData()

        then:
        responseData.getProductsServicesType().size() == 1
        responseData.getProcurators().size() == 1
        responseData.getAccounts().size() == 1

        when:
        def consentAccountPage = consentAccountRepository.findByConsentConsentIdOrderByCreatedAtAsc(consent.getConsentId(), Pageable.UNPAGED)
        def consentAccount = consentAccountPage.getContent().get(0)
        consentAccountRepository.delete(consentAccount)

        def response2 = customerService.getPersonalFinancialRelationsV2(consent.getConsentId())

        then:
        response2.getData() != null

        when:
        def responseData2 = response2.getData()

        then:
        responseData2.getProductsServicesType().size() == 1
        responseData2.getProcurators().size() == 1
        responseData2.getAccounts().size() == 0
    }

    def "we can get personal qualifications" () {
        when:
        def response = customerService.getPersonalQualificationsV2(consent.getConsentId())

        then:
        response.getData()

        when:
        def responseData = response.getData()

        then:
        Double.parseDouble(responseData.getInformedPatrimony().getAmount().getAmount()) == aPersonalQualifications(UUID.randomUUID()).getInformedPatrimonyAmount()
    }

    def "we cannot get response without authorised status"() {
        setup:
        def errorMessage = "Bad request, consent not Authorised!"
        consent.setStatus(EnumConsentStatus.AWAITING_AUTHORISATION.name())
        consentRepository.update(consent)

        when:
        customerService.getPersonalIdentificationsV2( consent.getConsentId())

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.UNAUTHORIZED
        e.getMessage() == errorMessage

        when:
        customerService.getPersonalQualificationsV2(consent.getConsentId())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.UNAUTHORIZED
        e1.getMessage() == errorMessage

        when:
        customerService.getPersonalFinancialRelationsV2(consent.getConsentId())

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.UNAUTHORIZED
        e2.getMessage() == errorMessage

        when:
        customerService.getBusinessQualificationsV2(consent.getConsentId())

        then:
        HttpStatusException e3 = thrown()
        e3.status == HttpStatus.UNAUTHORIZED
        e3.getMessage() == errorMessage

        when:
        customerService.getBusinessIdentificationsV2(consent.getConsentId())

        then:
        HttpStatusException e4 = thrown()
        e4.status == HttpStatus.UNAUTHORIZED
        e4.getMessage() == errorMessage

        when:
        customerService.getBusinessFinancialRelationsV2(consent.getConsentId())

        then:
        HttpStatusException e5 = thrown()
        e5.status == HttpStatus.UNAUTHORIZED
        e5.getMessage() == errorMessage
    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }
}
