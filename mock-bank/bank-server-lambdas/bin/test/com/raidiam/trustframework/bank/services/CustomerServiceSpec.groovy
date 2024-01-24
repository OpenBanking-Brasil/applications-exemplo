package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.domain.*
import com.raidiam.trustframework.mockbank.models.generated.CreateConsentData
import com.raidiam.trustframework.mockbank.models.generated.UpdateConsentData
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
    BusinessFinancialRelationsProductsServicesTypeEntity testBusinessFinancialRelationsProductsServicesType
    @Shared
    BusinessFinancialRelationsProcuratorEntity testBusinessFinancialRelationsProcuratorEntity
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
    PersonalFinancialRelationsProductsServicesTypeEntity testPersonalFinancialRelationsProductsServicesType
    @Shared
    PersonalFinancialRelationsProcuratorEntity testPersonalFinancialRelationsProcuratorEntity
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
            account = accountRepository.save(anAccount(accountHolder.getAccountHolderId()))
            consent = consentRepository.save(aConsent(accountHolder.getAccountHolderId()))
            consentPermissionsRepository.save(aConsentPermission(CreateConsentData.PermissionsEnum.CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ, consent.getConsentId()))
            consentPermissionsRepository.save(aConsentPermission(CreateConsentData.PermissionsEnum.CUSTOMERS_PERSONAL_ADITTIONALINFO_READ, consent.getConsentId()))
            consentPermissionsRepository.save(aConsentPermission(CreateConsentData.PermissionsEnum.CUSTOMERS_BUSINESS_IDENTIFICATIONS_READ, consent.getConsentId()))
            consentPermissionsRepository.save(aConsentPermission(CreateConsentData.PermissionsEnum.CUSTOMERS_BUSINESS_ADITTIONALINFO_READ, consent.getConsentId()))


            testBusinessIdentifications = businessIdentificationsRepository.save(aBusinessIdentificationEntity(accountHolder.getAccountHolderId()))
            testBusinessIdentificationsCompanyCnpj = businessIdentificationsCompanyCnpjRepository.save(aBusinessIdentificationCompanyCnpjEntity(testBusinessIdentifications.getBusinessIdentificationsId()))
            testBusinessOtherDocument = businessOtherDocumentRepository.save(aBusinessOtherDocument(testBusinessIdentifications.getBusinessIdentificationsId()))
            testBusinessParty = businessPartyRepository.save(aBusinessParty(testBusinessIdentifications.getBusinessIdentificationsId()))
            testBusinessPostalAddress = businessPostalAddressRepository.save(aBusinessPostalAddress(testBusinessIdentifications.getBusinessIdentificationsId()))
            testBusinessPhone = businessPhoneRepository.save(aBusinessPhone(testBusinessIdentifications.getBusinessIdentificationsId()))
            testBusinessEmail = businessEmailRepository.save(aBusinessEmail(testBusinessIdentifications.getBusinessIdentificationsId()))

            testBusinessFinancialRelations = businessFinancialRelationsRepository.save(aBusinessFinancialRelations(accountHolder.getAccountHolderId()))
            testBusinessFinancialRelationsProductsServicesType = businessFinancialRelationsProductsServicesRepository.save(aBusinessFinancialRelationsProductServicesType(testBusinessFinancialRelations.getBusinessFinancialRelationsId()))
            testBusinessFinancialRelationsProcuratorEntity = businessFinancialRelationsProcuratorRepository.save(aBusinessFinancialRelationsProcurator(testBusinessFinancialRelations.getBusinessFinancialRelationsId()))

            testBusinessQualifications = businessQualificationsRepository.save(aBusinessQualifications(accountHolder.getAccountHolderId()))
            testBusinessQualificationsEconomicActivities = businessQualificationsEconomicActivitiesRepository.save(aBusinessQualificationsEconomicActivities(testBusinessQualifications.getBusinessQualificationsId()))

            testPersonalIdentifications = personalIdentificationsRepository.save(aPersonalIdentificationEntity(accountHolder.getAccountHolderId()))
            testPersonalCompanyCnpj = personalCompanyCnpjRepository.save(aPersonalCompanyCnpj(testPersonalIdentifications.getPersonalIdentificationsId()))
            testPersonalOtherDocument = personalOtherDocumentRepository.save(aPersonalOtherDocument(testPersonalIdentifications.getPersonalIdentificationsId()))
            testPersonalNationality = personalNationalityRepository.save(aPersonalNationality(testPersonalIdentifications.getPersonalIdentificationsId()))
            testPersonalNationalityDocument = personalNationalityDocumentRepository.save(aPersonalNationalityDocument(testPersonalNationality.getPersonalNationalityId()))
            testPersonalFiliation = personalFiliationRepository.save(aPersonalFiliation(testPersonalIdentifications.getPersonalIdentificationsId()))
            testPersonalPostalAddress = personalPostalAddressRepository.save(aPersonalPostalAddress(testPersonalIdentifications.getPersonalIdentificationsId()))
            testPersonalPhone = personalPhoneRepository.save(aPersonalPhone(testPersonalIdentifications.getPersonalIdentificationsId()))
            testPersonalEmail = personalEmailRepository.save(aPersonalEmail(testPersonalIdentifications.getPersonalIdentificationsId()))

            testPersonalFinancialRelations = personalFinancialRelationsRepository.save(aPersonalFinancialRelations(accountHolder.getAccountHolderId()))
            testPersonalFinancialRelationsProductsServicesType = personalFinancialRelationsProductsServicesRepository.save(aPersonalFinancialRelationsProductServicesType(testPersonalFinancialRelations.getPersonalFinancialRelationsId()))
            testPersonalFinancialRelationsProcuratorEntity = personalFinancialRelationsProcuratorRepository.save(aPersonalFinancialRelationsProcurator(testPersonalFinancialRelations.getPersonalFinancialRelationsId()))

            testPersonalQualifications = personalQualificationsRepository.save(aPersonalQualifications(accountHolder.getAccountHolderId()))


            runSetup = false
        }
    }

    def "we can get business identifications" () {
        when:
        def response = customerService.getBusinessIdentifications(consent.getConsentId())

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
        responseData.getOtherDocuments().first().type == aBusinessOtherDocument(UUID.randomUUID()).getType()
        responseData.getParties().size() == 1
        responseData.getContacts().getPostalAddresses().size() == 1
        responseData.getContacts().getEmails().size() == 1
        responseData.getContacts().getPhones().size() == 1
    }

    def "we can get business financial-relations" () {
        when:
        def response = customerService.getBusinessFinancialRelations(consent.getConsentId())

        then:
        response.getData()

        when:
        def responseData = response.getData()

        then:
        responseData.getProductsServicesType().size() == 1
        responseData.getProcurators().size() == 1
        responseData.getAccounts().size() == 1
    }

    def "we can get business qualifications" () {
        when:
        def response = customerService.getBusinessQualifications(consent.getConsentId())

        then:
        response.getData()

        when:
        def responseData = response.getData()

        then:
        responseData.getEconomicActivities().size() == 1
    }

    def "we can get personal identifications" () {
        when:
        def response = customerService.getPersonalIdentifications(consent.getConsentId())

        then:
        response.getData()
        response.getData().size() == 1
        response.getData().first()

        when:
        def responseData = response.getData().first()

        then:
        responseData.getBrandName() == testPersonalIdentifications.getBrandName()
        responseData.getCompanyCnpj().size() == 1
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
        response.getData()

        when:
        def responseData = response.getData()

        then:
        responseData.getProductsServicesType().size() == 1
        responseData.getProcurators().size() == 1
        responseData.getAccounts().size() == 1
    }

    def "we can get personal qualifications" () {
        when:
        def response = customerService.getPersonalQualifications(consent.getConsentId())

        then:
        response.getData()

        when:
        def responseData = response.getData()

        then:
        responseData.getInformedPatrimony().getAmount() == aPersonalQualifications(UUID.randomUUID()).getInformedPatrimonyAmount()
    }

    def "we cannot get response without authorised status"() {
        setup:
        def errorMessage = "Bad request, consent not Authorised!"
        consent.setStatus(UpdateConsentData.StatusEnum.AWAITING_AUTHORISATION.name())
        consentRepository.update(consent)

        when:
        customerService.getPersonalIdentifications( consent.getConsentId())

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.BAD_REQUEST
        e.getMessage() == errorMessage

        when:
        customerService.getPersonalQualifications(consent.getConsentId())

        then:
        HttpStatusException e1 = thrown()
        e1.status == HttpStatus.BAD_REQUEST
        e1.getMessage() == errorMessage

        when:
        customerService.getPersonalFinancialRelations(consent.getConsentId())

        then:
        HttpStatusException e2 = thrown()
        e2.status == HttpStatus.BAD_REQUEST
        e2.getMessage() == errorMessage

        when:
        customerService.getBusinessQualifications(consent.getConsentId())

        then:
        HttpStatusException e3 = thrown()
        e3.status == HttpStatus.BAD_REQUEST
        e3.getMessage() == errorMessage

        when:
        customerService.getBusinessIdentifications(consent.getConsentId())

        then:
        HttpStatusException e4 = thrown()
        e4.status == HttpStatus.BAD_REQUEST
        e4.getMessage() == errorMessage

        when:
        customerService.getBusinessFinancialRelations(consent.getConsentId())

        then:
        HttpStatusException e5 = thrown()
        e5.status == HttpStatus.BAD_REQUEST
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
