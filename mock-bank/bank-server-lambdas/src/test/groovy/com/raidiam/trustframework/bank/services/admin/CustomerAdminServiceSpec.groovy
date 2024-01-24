package com.raidiam.trustframework.bank.services.admin

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.TestRequestDataFactory
import com.raidiam.trustframework.bank.services.CustomerService
import com.raidiam.trustframework.bank.utils.BankLambdaUtils
import com.raidiam.trustframework.mockbank.models.generated.CreateAccountHolderData
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Stepwise

import javax.inject.Inject

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class CustomerAdminServiceSpec extends CleanupSpecification {

    @Inject
    CustomerService customerService
    @Shared
    UUID accountHolderId

    def setup() {
        if (runSetup) {
            def accountHolder = customerService.addAccountHolder(TestRequestDataFactory.createAccountHolder("1234567890", "CPF"))
            accountHolderId = accountHolder.getData().getAccountHolderId()
            return false
        }
    }

    def "We can add update delete Customer"() {
        when://add Account Holder
        def newAccountHolder = customerService.addAccountHolder(TestRequestDataFactory.createAccountHolder("1234567890", "CPF")).getData()

        then:
        newAccountHolder.getAccountHolderId() != null

        when: //update Account Holder
        def updated = new CreateAccountHolderData()
        updated.setDocumentIdentification("12345678")
        updated.setAccountHolderName("Boris")
        updated.setDocumentRel("NEW")

        def updatedAccountHolder = customerService.updateAccountHolder(newAccountHolder.getAccountHolderId().toString(), updated).getData()

        then:
        updatedAccountHolder.getAccountHolderName() == updated.getAccountHolderName()
        updatedAccountHolder.getDocumentIdentification() == updated.getDocumentIdentification()
        updatedAccountHolder.getDocumentRel() == updated.getDocumentRel()

        when://delete Account Holder
        customerService.deleteAccountHolder(newAccountHolder.getAccountHolderId().toString())
        customerService.getAccountHolder(newAccountHolder.getAccountHolderId().toString())

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.NOT_FOUND
        e.getMessage() == "Could not find account holder"
    }

    def "We can add update delete Personal Identifications"() {
        when: //add Personal Identifications
        def newPersonalIdentificationsDto = customerService
                .addPersonalIdentifications(TestRequestDataFactory.createPersonalIdentifications().getData(), accountHolderId.toString()).getData()

        then:
        newPersonalIdentificationsDto != null

        when://update Personal Identifications
        def forUpdate = TestRequestDataFactory.editPersonalIdentifications()
        def updatedPersonalIdentification = customerService.updatePersonalIdentifications(newPersonalIdentificationsDto.getPersonalId(), forUpdate.getData()).getData()

        then:
        updatedPersonalIdentification != null
        updatedPersonalIdentification.getBrandName() == forUpdate.getData().getBrandName()
        updatedPersonalIdentification.getBirthDate() == forUpdate.getData().getBirthDate()
        updatedPersonalIdentification.getCivilName() == forUpdate.getData().getCivilName()
        updatedPersonalIdentification.getCpfNumber() == forUpdate.getData().getCpfNumber()
        updatedPersonalIdentification.getMaritalStatusAdditionalInfo() == forUpdate.getData().getMaritalStatusAdditionalInfo()
        updatedPersonalIdentification.getMaritalStatusCode() == forUpdate.getData().getMaritalStatusCode()
        updatedPersonalIdentification.getPassportCountry() == forUpdate.getData().getPassportCountry()
        updatedPersonalIdentification.getPassportExpirationDate() == forUpdate.getData().getPassportExpirationDate()
        updatedPersonalIdentification.getPassportIssueDate() == forUpdate.getData().getPassportIssueDate()
        updatedPersonalIdentification.getPassportNumber() == forUpdate.getData().getPassportNumber()
        updatedPersonalIdentification.getSex() == forUpdate.getData().getSex()
        !updatedPersonalIdentification.getCompanyCnpj().isEmpty()
        updatedPersonalIdentification.getCompanyCnpj().containsAll(forUpdate.getData().getCompanyCnpj())
        !updatedPersonalIdentification.getOtherDocuments().isEmpty()
        updatedPersonalIdentification.getOtherDocuments().containsAll(forUpdate.getData().getOtherDocuments())
        !updatedPersonalIdentification.getNationality().isEmpty()
        updatedPersonalIdentification.getNationality().first().getOtherNationalitiesInfo() == forUpdate.getData().getNationality().first().getOtherNationalitiesInfo()
        !updatedPersonalIdentification.getNationality().first().getDocuments().isEmpty()
        updatedPersonalIdentification.getNationality().first().getDocuments() == forUpdate.getData().getNationality().first().getDocuments()
        !updatedPersonalIdentification.getFiliation().isEmpty()
        updatedPersonalIdentification.getFiliation().containsAll(forUpdate.getData().getFiliation())
        updatedPersonalIdentification.getContacts() != null
        !updatedPersonalIdentification.getContacts().getPostalAddresses().isEmpty()
        updatedPersonalIdentification.getContacts().getPostalAddresses().containsAll(forUpdate.getData().getContacts().getPostalAddresses())
        !updatedPersonalIdentification.getContacts().getEmails().isEmpty()
        updatedPersonalIdentification.getContacts().getEmails().containsAll(forUpdate.getData().getContacts().getEmails())
        !updatedPersonalIdentification.getContacts().getPhones().isEmpty()
        updatedPersonalIdentification.getContacts().getPhones().containsAll(forUpdate.getData().getContacts().getPhones())

        when://delete Personal Identifications
        customerService.deletePersonalIdentifications(newPersonalIdentificationsDto.getPersonalId())

        then:
        noExceptionThrown()

        when:
        BankLambdaUtils.getPersonalIdentifications(newPersonalIdentificationsDto.getPersonalId(), personalIdentificationsRepository)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.NOT_FOUND
        e.getMessage() == "Could not find personal identifications"
    }

    def "We can add update delete Business Identifications"() {
        when://add Business Identifications
        def newBusinessIdentificationsDto = customerService.addBusinessIdentifications(TestRequestDataFactory.createBusinessIdentifications().getData(), accountHolderId.toString()).getData()

        then:
        newBusinessIdentificationsDto != null
        newBusinessIdentificationsDto.getBusinessIdentificationsId() != null

        when://update Business Identifications
        var forUpdate = TestRequestDataFactory.editBusinessIdentifications()
        def updatedBusinessIdentification = customerService.updateBusinessIdentifications(newBusinessIdentificationsDto.getBusinessIdentificationsId().toString(), forUpdate).getData()

        then:
        updatedBusinessIdentification != null
        updatedBusinessIdentification.getBusinessIdentificationsId() != null
        updatedBusinessIdentification.getBrandName() == forUpdate.getBrandName()
        updatedBusinessIdentification.getCompanyName() == forUpdate.getCompanyName()
        updatedBusinessIdentification.getTradeName() == forUpdate.getTradeName()
        updatedBusinessIdentification.getIncorporationDate() == forUpdate.getIncorporationDate()
        updatedBusinessIdentification.getCnpjNumber() == forUpdate.getCnpjNumber()
        !updatedBusinessIdentification.getCompanyCnpjNumber().isEmpty()
        updatedBusinessIdentification.getCompanyCnpjNumber().containsAll(forUpdate.getCompanyCnpjNumber())
        !updatedBusinessIdentification.getOtherDocuments().isEmpty()
        updatedBusinessIdentification.getOtherDocuments().containsAll(forUpdate.getOtherDocuments())
        !updatedBusinessIdentification.getParties().isEmpty()
        updatedBusinessIdentification.getParties().containsAll(forUpdate.getParties())
        updatedBusinessIdentification.getContacts() != null
        !updatedBusinessIdentification.getContacts().getPostalAddresses().isEmpty()
        updatedBusinessIdentification.getContacts().getPostalAddresses().containsAll(forUpdate.getContacts().getPostalAddresses())
        !updatedBusinessIdentification.getContacts().getEmails().isEmpty()
        updatedBusinessIdentification.getContacts().getEmails().containsAll(forUpdate.getContacts().getEmails())
        !updatedBusinessIdentification.getContacts().getPhones().isEmpty()
        updatedBusinessIdentification.getContacts().getPhones().containsAll(forUpdate.getContacts().getPhones())

        when://delete Business Identifications
        customerService.deleteBusinessIdentifications(newBusinessIdentificationsDto.getBusinessIdentificationsId().toString())

        then:
        noExceptionThrown()

        when:
        BankLambdaUtils.getBusinessIdentifications(newBusinessIdentificationsDto.getBusinessIdentificationsId().toString(), businessIdentificationsRepository)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.NOT_FOUND
        e.getMessage() == "Could not find business identifications"
    }

    def "We can add update delete Personal Qualification"() {
        when://add Personal Qualification
        def newPersonalQualificationsDto = customerService
                .addPersonalQualifications(TestRequestDataFactory.createPersonalQualifications().getData(), accountHolderId.toString())

        then:
        newPersonalQualificationsDto != null
        newPersonalQualificationsDto.getData().getAccountHolderId() != null
        newPersonalQualificationsDto.getData().getAccountHolderId() == accountHolderId

        when://update Personal Qualification
        var forUpdate = TestRequestDataFactory.createPersonalQualifications().getData()
        def updatedPersonalQualifications = customerService.updatePersonalQualifications(forUpdate, accountHolderId.toString()).getData()

        then:
        updatedPersonalQualifications != null
        updatedPersonalQualifications.getAccountHolderId() != null
        updatedPersonalQualifications.getCompanyCnpj() == forUpdate.getCompanyCnpj()
        updatedPersonalQualifications.getOccupationCode() == forUpdate.getOccupationCode()
        updatedPersonalQualifications.getOccupationDescription() == forUpdate.getOccupationDescription()
        updatedPersonalQualifications.getInformedIncomeAmount() == forUpdate.getInformedIncomeAmount()
        updatedPersonalQualifications.getInformedIncomeCurrency() == forUpdate.getInformedIncomeCurrency()
        updatedPersonalQualifications.getInformedIncomeDate() == forUpdate.getInformedIncomeDate()
        updatedPersonalQualifications.getInformedIncomeFrequency() == forUpdate.getInformedIncomeFrequency()
        updatedPersonalQualifications.getInformedPatrimonyAmount() == forUpdate.getInformedPatrimonyAmount()
        updatedPersonalQualifications.getInformedPatrimonyCurrency() == forUpdate.getInformedPatrimonyCurrency()
        updatedPersonalQualifications.getInformedPatrimonyYear() == forUpdate.getInformedPatrimonyYear()

        when://delete Personal Qualification
        customerService.deletePersonalQualifications(accountHolderId.toString())

        then:
        noExceptionThrown()

        when:
        BankLambdaUtils.getPersonalQualifications(accountHolderId.toString(), personalQualificationsRepository)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.NOT_FOUND
        e.getMessage() == "Could not find personal qualifications"
    }

    def "We can add update delete Business Qualification"() {
        when://add Business Qualification
        def newBusinessQualificationsDto = customerService.addBusinessQualifications(TestRequestDataFactory.createBusinessQualifications().getData(), accountHolderId.toString()).getData()

        then:
        newBusinessQualificationsDto != null
        newBusinessQualificationsDto.getAccountHolderId() != null
        newBusinessQualificationsDto.getAccountHolderId() == accountHolderId

        when://update Business Qualification
        var forUpdate = TestRequestDataFactory.createBusinessQualifications().getData()
        def updateBusinessQualifications = customerService.updateBusinessQualifications(forUpdate, accountHolderId.toString()).getData()

        then:
        updateBusinessQualifications != null
        updateBusinessQualifications.getAccountHolderId() != null
        updateBusinessQualifications.getInformedRevenueFrequency() == forUpdate.getInformedRevenueFrequency()
        updateBusinessQualifications.getInformedRevenueFrequencyAdditionalInfo() == forUpdate.getInformedRevenueFrequencyAdditionalInfo()
        updateBusinessQualifications.getInformedRevenueAmount() == forUpdate.getInformedRevenueAmount()
        updateBusinessQualifications.getInformedRevenueCurrency() == forUpdate.getInformedRevenueCurrency()
        updateBusinessQualifications.getInformedRevenueYear() == forUpdate.getInformedRevenueYear()
        updateBusinessQualifications.getInformedPatrimonyAmount() == forUpdate.getInformedPatrimonyAmount()
        updateBusinessQualifications.getInformedPatrimonyCurrency() == forUpdate.getInformedPatrimonyCurrency()
        updateBusinessQualifications.getInformedPatrimonyDate() == forUpdate.getInformedPatrimonyDate()
        !updateBusinessQualifications.getEconomicActivities().isEmpty()
        updateBusinessQualifications.getEconomicActivities().containsAll(forUpdate.getEconomicActivities())

        when://delete Business Qualification
        customerService.deleteBusinessQualifications(accountHolderId.toString())

        then:
        noExceptionThrown()

        when:
        BankLambdaUtils.getBusinessQualifications(accountHolderId.toString(), businessQualificationsRepository)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.NOT_FOUND
        e.getMessage() == "Could not find business qualifications"
    }

    def "We can add update delete Personal Financial Relations"() {
        when://add Personal Financial Relations
        def newPersonalFinancialRelationsDto = customerService
                .addPersonalFinancialRelations(TestRequestDataFactory.createPersonalFinancialRelations().getData(), accountHolderId.toString()).getData()

        then:
        newPersonalFinancialRelationsDto != null
        newPersonalFinancialRelationsDto.getAccountHolderId() != null
        newPersonalFinancialRelationsDto.getAccountHolderId() == accountHolderId

        when://update Personal Financial Relations
        var forUpdate = TestRequestDataFactory.createPersonalFinancialRelations().getData()
        def updatedPersonalFinancialRelations = customerService.updatePersonalFinancialRelations(forUpdate, accountHolderId.toString()).getData()

        then:
        updatedPersonalFinancialRelations != null
        updatedPersonalFinancialRelations.getAccountHolderId() != null
        updatedPersonalFinancialRelations.getStartDate() != null
        !updatedPersonalFinancialRelations.getProductsServicesType().isEmpty()
        updatedPersonalFinancialRelations.getProductsServicesType().containsAll(forUpdate.getProductsServicesType())
        !updatedPersonalFinancialRelations.getProcurators().isEmpty()
        updatedPersonalFinancialRelations.getProcurators().containsAll(forUpdate.getProcurators())
        updatedPersonalFinancialRelations.getProductsServicesTypeAdditionalInfo() == forUpdate.getProductsServicesTypeAdditionalInfo()

        when://delete Personal Financial Relations
        customerService.deletePersonalFinancialRelations(accountHolderId.toString())

        then:
        noExceptionThrown()

        when:
        BankLambdaUtils.getPersonalFinancialRelations(accountHolderId.toString(), personalFinancialRelationsRepository)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.NOT_FOUND
        e.getMessage() == "Could not find personal financial relations"
    }

    def "We can add update delete Business Financial Relations"() {
        when://add Business Financial Relations
        def newBusinessFinancialRelationsDto = customerService
                .addBusinessFinancialRelations(TestRequestDataFactory.createBusinessFinancialRelations().getData(), accountHolderId.toString()).getData()

        then:
        newBusinessFinancialRelationsDto != null
        newBusinessFinancialRelationsDto.getAccountHolderId() != null
        newBusinessFinancialRelationsDto.getAccountHolderId() == accountHolderId

        when://update Business Financial Relations
        var forUpdate = TestRequestDataFactory.createBusinessFinancialRelations().getData()
        def updatedBusinessFinancialRelations = customerService.updateBusinessFinancialRelations(forUpdate, accountHolderId.toString()).getData()

        then:
        updatedBusinessFinancialRelations != null
        updatedBusinessFinancialRelations.getAccountHolderId() != null
        updatedBusinessFinancialRelations.getStartDate() != null
        updatedBusinessFinancialRelations.getStartDate() == forUpdate.getStartDate()
        !updatedBusinessFinancialRelations.getProductsServicesType().isEmpty()
        updatedBusinessFinancialRelations.getProductsServicesType().containsAll(forUpdate.getProductsServicesType())
        !updatedBusinessFinancialRelations.getProcurators().isEmpty()
        updatedBusinessFinancialRelations.getProcurators().containsAll(forUpdate.getProcurators())

        when://delete Business Financial Relations
        customerService.deleteBusinessFinancialRelations(accountHolderId.toString())

        then:
        noExceptionThrown()

        when:
        BankLambdaUtils.getBusinessFinancialRelations(accountHolderId.toString(), businessFinancialRelationsRepository)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.NOT_FOUND
        e.getMessage() == "Could not find business financial relations"
    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }
}
