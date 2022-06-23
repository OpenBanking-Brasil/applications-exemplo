package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.domain.*;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.stream.Collectors;

@Singleton
public class CustomerService extends BaseBankService {

    private static final Logger LOG = LoggerFactory.getLogger(CustomerService.class);

    public ResponseBusinessCustomersIdentification getBusinessIdentifications (String consentId) {
        LOG.info("Getting Business Customers Identification response for consent id {}", consentId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.CUSTOMERS_BUSINESS_IDENTIFICATIONS_READ);

        var businessIdentifications = businessIdentificationsRepository.findByAccountHolderAccountHolderId(consentEntity.getAccountHolder().getAccountHolderId());
        var data = businessIdentifications.stream().map(BusinessIdentificationsEntity::getDTO).collect(Collectors.toList());
        return new ResponseBusinessCustomersIdentification().data(data);
    }

    public ResponseBusinessCustomersFinancialRelation getBusinessFinancialRelations (String consentId) {
        LOG.info("Getting Business Customers Financial Relation response for consent id {}", consentId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.CUSTOMERS_BUSINESS_ADITTIONALINFO_READ);

        var businessFinancialRelations = businessFinancialRelationsRepository.findByAccountHolderAccountHolderId(consentEntity.getAccountHolder().getAccountHolderId());
        var data = businessFinancialRelations.stream().findFirst().map(BusinessFinancialRelationsEntity::getDTO).orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "FinancialRelations Not Found"));
        return new ResponseBusinessCustomersFinancialRelation().data(data);
    }

    public ResponseBusinessCustomersQualification getBusinessQualifications(String consentId) {
        LOG.info("Getting Business Customers Qualification response for consent id {}", consentId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.CUSTOMERS_BUSINESS_ADITTIONALINFO_READ);

        var businessQualifications = businessQualificationsRepository.findByAccountHolderAccountHolderId(consentEntity.getAccountHolder().getAccountHolderId());
        var data = businessQualifications.stream().findFirst().map(BusinessQualificationsEntity::getDTO).orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Qualifications Not Found"));
        return new ResponseBusinessCustomersQualification().data(data);
    }

    public ResponsePersonalCustomersIdentification getPersonalIdentifications(String consentId) {
        LOG.info("Getting Personal Customers Identification response for consent id {}", consentId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ);

        var personalIdentifications = personalIdentificationsRepository.findByAccountHolderAccountHolderId(consentEntity.getAccountHolder().getAccountHolderId());
        var data = personalIdentifications.stream().map(PersonalIdentificationsEntity::getDTO).collect(Collectors.toList());
        return new ResponsePersonalCustomersIdentification().data(data);
    }

    public ResponsePersonalCustomersFinancialRelation getPersonalFinancialRelations(String consentId) {
        LOG.info("Getting Personal Customers Financial Relation response for consent id {}", consentId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.CUSTOMERS_PERSONAL_ADITTIONALINFO_READ);

        var personalFinancialRelations = personalFinancialRelationsRepository.findByAccountHolderAccountHolderId(consentEntity.getAccountHolder().getAccountHolderId());
        var data = personalFinancialRelations.stream().findFirst().map(PersonalFinancialRelationsEntity::getDTO).orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "FinancialRelations Not Found"));
        return new ResponsePersonalCustomersFinancialRelation().data(data);
    }

    public ResponsePersonalCustomersQualification getPersonalQualifications(String consentId) {
        LOG.info("Getting Personal Customers Qualification response for consent id {}", consentId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.CUSTOMERS_PERSONAL_ADITTIONALINFO_READ);

        var personalQualifications = personalQualificationsRepository.findByAccountHolderAccountHolderId(consentEntity.getAccountHolder().getAccountHolderId());
        var data = personalQualifications.stream().findFirst().map(PersonalQualificationsEntity::getDTO).orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Qualifications Not Found"));
        return new ResponsePersonalCustomersQualification().data(data);
    }

}
