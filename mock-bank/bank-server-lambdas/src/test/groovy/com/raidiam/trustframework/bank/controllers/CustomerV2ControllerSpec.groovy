package com.raidiam.trustframework.bank.controllers

import com.raidiam.trustframework.bank.FullCreateConsentFactory
import com.raidiam.trustframework.bank.TestRequestDataFactory
import com.raidiam.trustframework.bank.domain.AccountHolderEntity
import com.raidiam.trustframework.bank.utils.BankLambdaUtils
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.http.HttpRequest
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.apache.http.client.utils.URIBuilder
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Stepwise

import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccountHolder

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
@Testcontainers
class CustomerV2ControllerSpec extends FullCreateConsentFactory {
    @Shared
    ResponsePersonalIdentification postPersonalIdentification
    @Shared
    PersonalFinancialRelations postPersonalFinancialRelations
    @Shared
    PersonalQualifications postPersonalQualifications
    @Shared
    ResponseBusinessIdentification postBusinessIdentification
    @Shared
    BusinessFinancialRelations postBusinessFinancialRelations
    @Shared
    BusinessQualifications postBusinessQualifications
    @Shared
    AccountHolderEntity accountHolder
    @Shared
    AccountHolderEntity accountHolder2
    @Shared
    AccountHolderEntity accountHolder3
    @Shared
    String personalToken
    @Shared
    String businessToken

    def setup() {
        if (runSetup) {
            //ADD Account Holder via db
            accountHolder = accountHolderRepository.save(anAccountHolder())
            accountHolder2 = accountHolderRepository.save(anAccountHolder())
            accountHolder3 = accountHolderRepository.save(anAccountHolder())

            //ADD Personal Identification via V2 Controller
            def adminToken = createToken("op:admin")
            CreatePersonalIdentification newPersonalIdentification = TestRequestDataFactory.createPersonalIdentifications()
            postPersonalIdentification = client.toBlocking()
                    .retrieve(HttpRequest.POST("/admin/customers/${accountHolder.getAccountHolderId().toString()}/personal/identifications",
                            mapper.writeValueAsString(newPersonalIdentification))
                            .header("Authorization", "Bearer ${adminToken}"), ResponsePersonalIdentification)

            //ADD Personal Financial Relations via V2 Controller
            PersonalFinancialRelations newPersonalFinancialRelations = TestRequestDataFactory.createPersonalFinancialRelations()
            postPersonalFinancialRelations = client.toBlocking()
                    .retrieve(HttpRequest.POST("/admin/customers/${accountHolder.getAccountHolderId().toString()}/personal/financial-relations",
                            mapper.writeValueAsString(newPersonalFinancialRelations))
                            .header("Authorization", "Bearer ${adminToken}"), PersonalFinancialRelations)


            //ADD Personal Qualifications via V2 Controller
            PersonalQualifications newPersonalQualifications = TestRequestDataFactory.createPersonalQualifications()
            postPersonalQualifications = client.toBlocking()
                    .retrieve(HttpRequest.POST("/admin/customers/${accountHolder.getAccountHolderId().toString()}/personal/qualifications",
                            mapper.writeValueAsString(newPersonalQualifications))
                            .header("Authorization", "Bearer ${adminToken}"), PersonalQualifications)

            //ADD Business Identification via V2 Controller
            CreateBusinessIdentification newBusinessIdentification = TestRequestDataFactory.createBusinessIdentifications(accountHolder2.getDocumentIdentification(), accountHolder3.getDocumentIdentification())
            postBusinessIdentification = client.toBlocking()
                    .retrieve(HttpRequest.POST("/admin/customers/${accountHolder.getAccountHolderId().toString()}/business/identifications",
                            mapper.writeValueAsString(newBusinessIdentification))
                            .header("Authorization", "Bearer ${adminToken}"), ResponseBusinessIdentification)

            //ADD Business Financial Relations via V2 Controller
            postBusinessFinancialRelations = client.toBlocking()
                    .retrieve(HttpRequest.POST("/admin/customers/${accountHolder.getAccountHolderId().toString()}/business/financial-relations",
                            mapper.writeValueAsString(TestRequestDataFactory.createBusinessFinancialRelations(accountHolder2.getDocumentIdentification(), accountHolder3.getDocumentIdentification())))
                            .header("Authorization", "Bearer ${adminToken}"), BusinessFinancialRelations)

            //ADD Business Qualifications via V2 Controller
            BusinessQualifications newBusinessQualifications = TestRequestDataFactory.createBusinessQualifications()
            postBusinessQualifications = client.toBlocking()
                    .retrieve(HttpRequest.POST("/admin/customers/${accountHolder.getAccountHolderId().toString()}/business/qualifications",
                            mapper.writeValueAsString(newBusinessQualifications))
                            .header("Authorization", "Bearer ${adminToken}"), BusinessQualifications)

            //ADD Personal Consent via V2 Controller
            personalToken = createConsentWithCustomerPermissions(accountHolder, false)

            //ADD Business Consent via V2 Controller
            businessToken = createConsentWithCustomerPermissions(accountHolder, true)

            runSetup = false
        }
    }

    def cleanupSpec() {
    }

    void "we can GET personal identification v2"() {
        when:
        URI uri = new URIBuilder('/open-banking/customers/v2/personal/identifications').build()

        def response = client.toBlocking().retrieve(HttpRequest.GET(uri).header("Authorization", "Bearer ${personalToken}"), ResponsePersonalCustomersIdentificationV2)

        then:
        response.getData() != null
        def personal = response.getData().first()
        personal.getPersonalId() == postPersonalIdentification.getData().getPersonalId()
        personal.getBrandName() == postPersonalIdentification.getData().getBrandName()
        personal.getCivilName() == postPersonalIdentification.getData().getCivilName()
        personal.getSocialName() == postPersonalIdentification.getData().getSocialName()
        personal.getBirthDate() == postPersonalIdentification.getData().getBirthDate()
        personal.getMaritalStatusCode().name() == postPersonalIdentification.getData().getMaritalStatusCode().name()
        personal.getMaritalStatusAdditionalInfo() == postPersonalIdentification.getData().getMaritalStatusAdditionalInfo()
        personal.getSex().name() == postPersonalIdentification.getData().getSex().name()
        personal.getCompaniesCnpj().containsAll(postPersonalIdentification.getData().getCompanyCnpj())
        personal.getDocuments().getCpfNumber() == postPersonalIdentification.getData().getCpfNumber()
        personal.getDocuments().getPassport().getNumber() == postPersonalIdentification.getData().getPassportNumber()
        personal.getDocuments().getPassport().getCountry() == postPersonalIdentification.getData().getPassportCountry()
        personal.getDocuments().getPassport().getExpirationDate() == postPersonalIdentification.getData().getPassportExpirationDate()
        personal.getDocuments().getPassport().getIssueDate() == postPersonalIdentification.getData().getPassportIssueDate()
        personal.getOtherDocuments().size() == postPersonalIdentification.getData().getOtherDocuments().size()
        personal.isHasBrazilianNationality() == postPersonalIdentification.getData().hasBrazilianNationality
        personal.getNationality().size() == postPersonalIdentification.getData().getNationality().size()
        personal.getFiliation().size() == postPersonalIdentification.getData().getFiliation().size()
        personal.getContacts().getPostalAddresses().size() == postPersonalIdentification.getData().getContacts().getPostalAddresses().size()
        personal.getContacts().getPhones().size() == postPersonalIdentification.getData().getContacts().getPhones().size()
        personal.getContacts().getEmails().size() == postPersonalIdentification.getData().getContacts().getEmails().size()
    }

    void "we can GET personal financial relations v2"() {
        when:
        URI uri = new URIBuilder('/open-banking/customers/v2/personal/financial-relations').build()

        def response = client.toBlocking().retrieve(HttpRequest.GET(uri).header("Authorization", "Bearer ${personalToken}"), ResponsePersonalCustomersFinancialRelationV2)

        then:
        response.getData() != null
        def financialRelations = response.getData()
        financialRelations.getStartDate() == postPersonalFinancialRelations.getData().getStartDate()
        financialRelations.getProductsServicesType().first().toString() == postPersonalFinancialRelations.getData().getProductsServicesType().first().toString()
        financialRelations.getProductsServicesTypeAdditionalInfo() == postPersonalFinancialRelations.getData().getProductsServicesTypeAdditionalInfo()
        !financialRelations.getProcurators().isEmpty()
    }

    void "we can GET personal qualifications v2"() {
        when:
        URI uri = new URIBuilder('/open-banking/customers/v2/personal/qualifications').build()

        def response = client.toBlocking().retrieve(HttpRequest.GET(uri).header("Authorization", "Bearer ${personalToken}"), ResponsePersonalCustomersQualificationV2)

        then:
        response.getData() != null
        def qualification = response.getData()
        qualification.getCompanyCnpj() == postPersonalQualifications.getData().getCompanyCnpj()
        qualification.getOccupationCode().name() == postPersonalQualifications.getData().getOccupationCode().name()
        qualification.getOccupationDescription() == postPersonalQualifications.getData().getOccupationDescription()
        qualification.getInformedIncome().getFrequency().name() == postPersonalQualifications.getData().getInformedIncomeFrequency().name()
        qualification.getInformedIncome().getAmount().getAmount() == BankLambdaUtils.formatAmountV2(postPersonalQualifications.getData().getInformedIncomeAmount())
        qualification.getInformedIncome().getAmount().getCurrency() == postPersonalQualifications.getData().getInformedIncomeCurrency()
        qualification.getInformedIncome().getDate().toString() == postPersonalQualifications.getData().getInformedIncomeDate()
        qualification.getInformedPatrimony().getAmount().getAmount() == BankLambdaUtils.formatAmountV2(postPersonalQualifications.getData().getInformedPatrimonyAmount())
        qualification.getInformedPatrimony().getAmount().getCurrency() == postPersonalQualifications.getData().getInformedPatrimonyCurrency()
        qualification.getInformedPatrimony().getYear() == postPersonalQualifications.getData().getInformedPatrimonyYear()
    }

    void "we can GET business identification v2"() {
        when:
        URI uri = new URIBuilder('/open-banking/customers/v2/business/identifications').build()

        def response = client.toBlocking().retrieve(HttpRequest.GET(uri).header("Authorization", "Bearer ${businessToken}"), ResponseBusinessCustomersIdentificationV2)

        then:
        response.getData() != null
        def business = response.getData().first()
        business.getBusinessId() == postBusinessIdentification.getData().getBusinessIdentificationsId().toString()
        business.getBrandName() == postBusinessIdentification.getData().getBrandName()
        business.getCompanyName() == postBusinessIdentification.getData().getCompanyName()
        business.getTradeName() == postBusinessIdentification.getData().getTradeName()
        business.getIncorporationDate().toLocalDate() == postBusinessIdentification.getData().getIncorporationDate().toLocalDate()
        business.getCnpjNumber() == postBusinessIdentification.getData().getCnpjNumber()
        business.getCompaniesCnpj().containsAll(postBusinessIdentification.getData().getCompanyCnpjNumber())
        business.getOtherDocuments().size() == postBusinessIdentification.getData().getOtherDocuments().size()
        business.getParties().size() == postBusinessIdentification.getData().getParties().size()
        business.getContacts().getPostalAddresses().size() == postBusinessIdentification.getData().getContacts().getPostalAddresses().size()
        business.getContacts().getPhones().size() == postBusinessIdentification.getData().getContacts().getPhones().size()
        business.getContacts().getEmails().size() == postBusinessIdentification.getData().getContacts().getEmails().size()
    }

    void "we can GET business identification v2 by any part cpf"() {
        when:
        URI uri = new URIBuilder('/open-banking/customers/v2/business/identifications').build()

        def token = createConsentWithCustomerPermissions(accountHolder3, true)
        def response = client.toBlocking().retrieve(HttpRequest.GET(uri).header("Authorization", "Bearer ${token}"), ResponseBusinessCustomersIdentificationV2)

        then:
        response.getData() != null
        def business = response.getData().first()
        business.getBusinessId() == postBusinessIdentification.getData().getBusinessIdentificationsId().toString()
        business.getBrandName() == postBusinessIdentification.getData().getBrandName()
        business.getCompanyName() == postBusinessIdentification.getData().getCompanyName()
        business.getTradeName() == postBusinessIdentification.getData().getTradeName()
        business.getIncorporationDate().toLocalDate() == postBusinessIdentification.getData().getIncorporationDate().toLocalDate()
        business.getCnpjNumber() == postBusinessIdentification.getData().getCnpjNumber()
        business.getCompaniesCnpj().containsAll(postBusinessIdentification.getData().getCompanyCnpjNumber())
        business.getOtherDocuments().size() == postBusinessIdentification.getData().getOtherDocuments().size()
        business.getParties().size() == postBusinessIdentification.getData().getParties().size()
        business.getContacts().getPostalAddresses().size() == postBusinessIdentification.getData().getContacts().getPostalAddresses().size()
        business.getContacts().getPhones().size() == postBusinessIdentification.getData().getContacts().getPhones().size()
        business.getContacts().getEmails().size() == postBusinessIdentification.getData().getContacts().getEmails().size()
    }

    void "we can GET business financial relations v2"() {
        when:
        URI uri = new URIBuilder('/open-banking/customers/v2/business/financial-relations').build()

        def response = client.toBlocking().retrieve(HttpRequest.GET(uri).header("Authorization", "Bearer ${businessToken}"), ResponseBusinessCustomersFinancialRelationV2)

        then:
        response.getData() != null
        def financialRelations = response.getData()
        financialRelations.getStartDate().toLocalDate() == postBusinessFinancialRelations.getData().getStartDate().toLocalDate()
        financialRelations.getProductsServicesType().first().toString() == postBusinessFinancialRelations.getData().getProductsServicesType().first().toString()
        !financialRelations.getProcurators().isEmpty()
    }

    void "we can GET business financial relations v2 by any part cpf"() {
        when:
        URI uri = new URIBuilder('/open-banking/customers/v2/business/financial-relations').build()

        def token = createConsentWithCustomerPermissions(accountHolder2, true)
        def response = client.toBlocking().retrieve(HttpRequest.GET(uri).header("Authorization", "Bearer ${token}"), ResponseBusinessCustomersFinancialRelationV2)

        then:
        response.getData() != null
        def financialRelations = response.getData()
        financialRelations.getStartDate().toLocalDate() == postBusinessFinancialRelations.getData().getStartDate().toLocalDate()
        financialRelations.getProductsServicesType().first().toString() == postBusinessFinancialRelations.getData().getProductsServicesType().first().toString()
        !financialRelations.getProcurators().isEmpty()
    }

    void "we can GET business qualifications v2"() {
        when:
        URI uri = new URIBuilder('/open-banking/customers/v2/business/qualifications').build()

        def response = client.toBlocking().retrieve(HttpRequest.GET(uri).header("Authorization", "Bearer ${businessToken}"), ResponseBusinessCustomersQualificationV2)

        then:
        response.getData() != null
        def qualification = response.getData()
        qualification.getEconomicActivities().size() == postBusinessQualifications.getData().getEconomicActivities().size()
        qualification.getInformedRevenue().getFrequency().name() == postBusinessQualifications.getData().getInformedRevenueFrequency().name()
        qualification.getInformedRevenue().getFrequencyAdditionalInfo() == postBusinessQualifications.getData().getInformedRevenueFrequencyAdditionalInfo()
        qualification.getInformedRevenue().getAmount().getAmount() == BankLambdaUtils.formatAmountV2(postBusinessQualifications.getData().getInformedRevenueAmount())
        qualification.getInformedRevenue().getAmount().getCurrency() == postBusinessQualifications.getData().getInformedRevenueCurrency()
        qualification.getInformedRevenue().getYear() == postBusinessQualifications.getData().getInformedRevenueYear()
        qualification.getInformedPatrimony().getAmount().getAmount() == BankLambdaUtils.formatAmountV2(postBusinessQualifications.getData().getInformedPatrimonyAmount())
        qualification.getInformedPatrimony().getAmount().getCurrency() == postBusinessQualifications.getData().getInformedPatrimonyCurrency()
        qualification.getInformedPatrimony().getDate().toString() == postBusinessQualifications.getData().getInformedPatrimonyDate()
    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }
}
