package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.domain.*;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
@Transactional
public class CustomerService extends BaseBankService {

    private static final Logger LOG = LoggerFactory.getLogger(CustomerService.class);

    public ResponseBusinessCustomersIdentificationV2 getBusinessIdentificationsV2(String consentId) {
        LOG.info("Getting Business Customers Identification response for consent id {}", consentId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, EnumConsentPermissions.CUSTOMERS_BUSINESS_IDENTIFICATIONS_READ);

        var businessIdentifications = businessIdentificationsRepository.findByAccountHolderAccountHolderId(consentEntity.getAccountHolder().getAccountHolderId());
        if(businessIdentifications.isEmpty()) {
            var businessPart = businessPartyRepository.findByDocumentNumber(consentEntity.getAccountHolder().getDocumentIdentification());
            businessPart.ifPresent(businessPartyEntity -> businessIdentifications.add(businessPartyEntity.getBusinessIdentifications()));
        }
        var data = businessIdentifications.stream().map(BusinessIdentificationsEntity::getDtoV2).collect(Collectors.toList());
        return new ResponseBusinessCustomersIdentificationV2().data(data);
    }

    public ResponseBusinessCustomersFinancialRelationV2 getBusinessFinancialRelationsV2(String consentId) {
        LOG.info("Getting Business Customers Financial Relation response for consent id {}", consentId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, EnumConsentPermissions.CUSTOMERS_BUSINESS_ADITTIONALINFO_READ);



        var businessFinancialRelations = businessFinancialRelationsRepository.findByAccountHolderAccountHolderId(consentEntity.getAccountHolder().getAccountHolderId()).or(() -> {
                    var businessPart = businessPartyRepository.findByDocumentNumber(consentEntity.getAccountHolder().getDocumentIdentification());
                    if(businessPart.isPresent()) {
                        return businessFinancialRelationsRepository.findByAccountHolderAccountHolderId(businessPart.get().getBusinessIdentifications().getAccountHolderId());
                    }
                    return Optional.empty();
                });


        var data = businessFinancialRelations.stream().findFirst().map(BusinessFinancialRelationsEntity::getDtoV2).orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Business FinancialRelations Not Found"));

        // filter returned accounts by consent
        var consentAccountNumbers = consentEntity.getAccounts().stream().map(AccountEntity::getNumber).collect(Collectors.toList());
        var filteredAccounts = data.getAccounts().stream().filter(account -> consentAccountNumbers.contains(account.getNumber())).collect(Collectors.toList());
        data.setAccounts(filteredAccounts);

        return new ResponseBusinessCustomersFinancialRelationV2().data(data);
    }

    public ResponseBusinessCustomersQualificationV2 getBusinessQualificationsV2(String consentId) {
        return new ResponseBusinessCustomersQualificationV2().data(getBusinessQualificationsEntity(consentId).getDtoV2());
    }

    private BusinessQualificationsEntity getBusinessQualificationsEntity(String consentId) {
        LOG.info("Getting Business Customers Qualification response for consent id {}", consentId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, EnumConsentPermissions.CUSTOMERS_BUSINESS_ADITTIONALINFO_READ);

        return businessQualificationsRepository.findByAccountHolderAccountHolderId(consentEntity.getAccountHolder().getAccountHolderId())
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Business Qualifications Not Found"));
    }

    public ResponsePersonalCustomersIdentification getPersonalIdentifications(String consentId) {
        LOG.info("Getting Personal Customers Identification response for consent id {}", consentId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, EnumConsentPermissions.CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ);

        var personalIdentifications = personalIdentificationsRepository.findByAccountHolderAccountHolderId(consentEntity.getAccountHolder().getAccountHolderId());
        var data = personalIdentifications.stream().map(PersonalIdentificationsEntity::getDTO).collect(Collectors.toList());
        return new ResponsePersonalCustomersIdentification().data(data);
    }

    public ResponsePersonalCustomersIdentificationV2 getPersonalIdentificationsV2(String consentId) {
        LOG.info("Getting Personal Customers Identification response for consent id {}", consentId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, EnumConsentPermissions.CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ);

        var personalIdentifications = personalIdentificationsRepository.findByAccountHolderAccountHolderId(consentEntity.getAccountHolder().getAccountHolderId());
        var data = personalIdentifications.stream().map(PersonalIdentificationsEntity::getDtoV2).collect(Collectors.toList());
        return new ResponsePersonalCustomersIdentificationV2().data(data);
    }

    public ResponsePersonalCustomersFinancialRelation getPersonalFinancialRelations(String consentId) {
        LOG.info("Getting Personal Customers Financial Relation response for consent id {}", consentId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, EnumConsentPermissions.CUSTOMERS_PERSONAL_ADITTIONALINFO_READ);

        var personalFinancialRelations = personalFinancialRelationsRepository.findByAccountHolderAccountHolderId(consentEntity.getAccountHolder().getAccountHolderId());
        var data = personalFinancialRelations.stream().findFirst().map(PersonalFinancialRelationsEntity::getDTO).orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Personal FinancialRelations Not Found"));

        // filter returned accounts by consent
        var consentAccountNumbers = consentEntity.getAccounts().stream().map(AccountEntity::getNumber).collect(Collectors.toList());
        var filteredAccounts = data.getAccounts().stream().filter(account -> consentAccountNumbers.contains(account.getNumber())).collect(Collectors.toList());
        data.setAccounts(filteredAccounts);

        return new ResponsePersonalCustomersFinancialRelation().data(data);
    }

    public ResponsePersonalCustomersFinancialRelationV2 getPersonalFinancialRelationsV2(String consentId) {
        LOG.info("Getting Personal Customers Financial Relation response for consent id {}", consentId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, EnumConsentPermissions.CUSTOMERS_PERSONAL_ADITTIONALINFO_READ);

        var personalFinancialRelations = personalFinancialRelationsRepository.findByAccountHolderAccountHolderId(consentEntity.getAccountHolder().getAccountHolderId());
        var data = personalFinancialRelations.stream().findFirst().map(PersonalFinancialRelationsEntity::getDtoV2).orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Personal FinancialRelations Not Found"));

        // filter returned accounts by consent
        var consentAccountNumbers = consentEntity.getAccounts().stream().map(AccountEntity::getNumber).collect(Collectors.toList());
        var filteredAccounts = data.getAccounts().stream().filter(account -> consentAccountNumbers.contains(account.getNumber())).collect(Collectors.toList());
        data.setAccounts(filteredAccounts);

        return new ResponsePersonalCustomersFinancialRelationV2().data(data);
    }

    public ResponsePersonalCustomersQualificationV2 getPersonalQualificationsV2(String consentId) {
        return new ResponsePersonalCustomersQualificationV2().data(getPersonalQualificationsEntity(consentId).getDtoV2());
    }

    private PersonalQualificationsEntity getPersonalQualificationsEntity(String consentId) {
        LOG.info("Getting Personal Customers Qualification response for consent id {}", consentId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, EnumConsentPermissions.CUSTOMERS_PERSONAL_ADITTIONALINFO_READ);

        return personalQualificationsRepository.findByAccountHolderAccountHolderId(consentEntity.getAccountHolder().getAccountHolderId())
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Personal Qualifications Not Found"));
    }

    public ResponseAccountHolderList getAccountHolders() {
        LOG.info("Getting Account Holders");
        return new ResponseAccountHolderList().data(StreamSupport.stream(accountHolderRepository.findAll().spliterator(), false)
                .map(AccountHolderEntity::getAdminAccountHolderDto)
                .collect(Collectors.toList()));
    }

    public ResponseAccountHolder addAccountHolder(CreateAccountHolderData accountHolder) {
        LOG.info("Add Account Holder");
        return accountHolderRepository.save(AccountHolderEntity.from(accountHolder)).getAccountHolderResponse();
    }

    public ResponseAccountHolder getAccountHolder(String accountHolderId) {
        LOG.info("Getting Account Holder id {}", accountHolderId);
        var accountHolderEntity = BankLambdaUtils.getAccountHolder(accountHolderId, accountHolderRepository);
        return accountHolderEntity.getAccountHolderResponse();
    }

    public ResponseAccountHolder updateAccountHolder(String accountHolderId, CreateAccountHolderData accountHolder) {
        LOG.info("Update Account Holder id {}", accountHolderId);
        var accountHolderEntity = BankLambdaUtils.getAccountHolder(accountHolderId, accountHolderRepository);
        return accountHolderRepository.update(accountHolderEntity.update(accountHolder)).getAccountHolderResponse();
    }

    public void deleteAccountHolder(String accountHolderId) {
        LOG.info("Delete Account Holder id {}", accountHolderId);
        var accountHolderEntity = BankLambdaUtils.getAccountHolder(accountHolderId, accountHolderRepository);
        accountHolderRepository.delete(accountHolderEntity);
    }

    public ResponsePersonalIdentification addPersonalIdentifications(CreatePersonalIdentificationData personalIdentifications, String accountHolderId) {
        LOG.info("Add Personal Identification for Account Holder id {}", accountHolderId);
        BankLambdaUtils.checkExistAccountHolder(accountHolderId, accountHolderRepository);
        return personalIdentificationsRepository.save(PersonalIdentificationsEntity.from(personalIdentifications, UUID.fromString(accountHolderId))).getResponseAdminIdentifications();
    }

    public ResponsePersonalIdentification updatePersonalIdentifications(String personalIdentificationsId, EditedPersonalIdentificationData personalIdentifications) {
        LOG.info("Update Personal Identification id {}", personalIdentificationsId);
        var personalIdentificationsEntity = BankLambdaUtils.getPersonalIdentifications(personalIdentificationsId, personalIdentificationsRepository);
        return personalIdentificationsRepository.update(personalIdentificationsEntity.update(personalIdentifications)).getResponseAdminIdentifications();
    }

    public void deletePersonalIdentifications(String personalIdentificationsId) {
        LOG.info("Delete Personal Identification id {}", personalIdentificationsId);
        var personalIdentificationsEntity = BankLambdaUtils.getPersonalIdentifications(personalIdentificationsId, personalIdentificationsRepository);
        personalIdentificationsRepository.delete(personalIdentificationsEntity);
    }

    public PersonalFinancialRelations addPersonalFinancialRelations(PersonalFinancialRelationsData personalFinancialRelations, String accountHolderId) {
        LOG.info("Add Personal Financial Relations for Account Holder id {}", accountHolderId);
        BankLambdaUtils.checkExistAccountHolder(accountHolderId, accountHolderRepository);
        BankLambdaUtils.checkExistPersonalFinancialRelations(accountHolderId, personalFinancialRelationsRepository);
        return personalFinancialRelationsRepository.save(PersonalFinancialRelationsEntity.from(personalFinancialRelations, UUID.fromString(accountHolderId))).getAdminPersonalFinancialRelations();
    }

    public PersonalFinancialRelations updatePersonalFinancialRelations(PersonalFinancialRelationsData personalFinancialRelations, String accountHolderId) {
        LOG.info("Update Personal Financial Relations for Account Holder id {}", accountHolderId);
        var personalFinancialRelationsEntity = BankLambdaUtils.getPersonalFinancialRelations(accountHolderId, personalFinancialRelationsRepository);
        return personalFinancialRelationsRepository.update(personalFinancialRelationsEntity.update(personalFinancialRelations)).getAdminPersonalFinancialRelations();
    }

    public void deletePersonalFinancialRelations(String accountHolderId) {
        LOG.info("Delete Personal Financial Relations by Account Holder id {}", accountHolderId);
        var personalFinancialRelationsEntity = BankLambdaUtils.getPersonalFinancialRelations(accountHolderId, personalFinancialRelationsRepository);
        personalFinancialRelationsRepository.delete(personalFinancialRelationsEntity);
    }

    public PersonalQualifications addPersonalQualifications(PersonalQualificationsData personalQualifications, String accountHolderId) {
        LOG.info("Add Personal Qualifications for Account Holder id {}", accountHolderId);
        BankLambdaUtils.checkExistAccountHolder(accountHolderId, accountHolderRepository);
        BankLambdaUtils.checkExistPersonalQualifications(accountHolderId, personalQualificationsRepository);
        return personalQualificationsRepository.save(PersonalQualificationsEntity.from(personalQualifications, UUID.fromString(accountHolderId))).getAdminPersonalQualifications();
    }

    public PersonalQualifications updatePersonalQualifications(PersonalQualificationsData personalQualifications, String accountHolderId) {
        LOG.info("Update Personal Qualifications for Account Holder id {}", accountHolderId);
        var personalQualificationsEntity = BankLambdaUtils.getPersonalQualifications(accountHolderId, personalQualificationsRepository);
        return personalQualificationsRepository.update(personalQualificationsEntity.update(personalQualifications)).getAdminPersonalQualifications();
    }

    public void deletePersonalQualifications(String accountHolderId) {
        LOG.info("Delete Personal Qualifications by Account Holder id {}", accountHolderId);
        var personalQualificationsEntity = BankLambdaUtils.getPersonalQualifications(accountHolderId, personalQualificationsRepository);
        personalQualificationsRepository.delete(personalQualificationsEntity);
    }

    public ResponseBusinessIdentification addBusinessIdentifications(CreateBusinessIdentificationData businessIdentifications, String accountHolderId) {
        LOG.info("Add Business Identification for Account Holder id {}", accountHolderId);
        BankLambdaUtils.checkExistAccountHolder(accountHolderId, accountHolderRepository);
        return businessIdentificationsRepository.save(BusinessIdentificationsEntity.from(businessIdentifications, UUID.fromString(accountHolderId))).getResponseAdminBusinessIdentifications();
    }

    public ResponseBusinessIdentification updateBusinessIdentifications(String businessIdentificationsId, EditedBusinessIdentificationData businessIdentifications) {
        LOG.info("Update Business Identification id {}", businessIdentificationsId);
        var businessIdentificationsEntity = BankLambdaUtils.getBusinessIdentifications(businessIdentificationsId, businessIdentificationsRepository);
        return businessIdentificationsRepository.update(businessIdentificationsEntity.update(businessIdentifications)).getResponseAdminBusinessIdentifications();
    }

    public void deleteBusinessIdentifications(String businessIdentificationsId) {
        LOG.info("Delete Business Identification id {}", businessIdentificationsId);
        var businessIdentificationsEntity = BankLambdaUtils.getBusinessIdentifications(businessIdentificationsId, businessIdentificationsRepository);
        businessIdentificationsRepository.delete(businessIdentificationsEntity);
    }

    public BusinessFinancialRelations addBusinessFinancialRelations(BusinessFinancialRelationsData businessFinancialRelations, String accountHolderId) {
        LOG.info("Add Business Financial Relations for Account Holder id {}", accountHolderId);
        BankLambdaUtils.checkExistAccountHolder(accountHolderId, accountHolderRepository);
        BankLambdaUtils.checkExistBusinessFinancialRelations(accountHolderId, businessFinancialRelationsRepository);
        return businessFinancialRelationsRepository.save(BusinessFinancialRelationsEntity.from(businessFinancialRelations, UUID.fromString(accountHolderId))).getAdminBusinessFinancialRelations();
    }

    public BusinessFinancialRelations updateBusinessFinancialRelations(BusinessFinancialRelationsData businessFinancialRelations, String accountHolderId) {
        LOG.info("Update Business Financial Relations for Account Holder id {}", accountHolderId);
        BusinessFinancialRelationsEntity businessFinancialRelationsEntity = BankLambdaUtils.getBusinessFinancialRelations(accountHolderId, businessFinancialRelationsRepository);
        businessFinancialRelationsRepository.delete(businessFinancialRelationsEntity);
        return businessFinancialRelationsRepository.save(BusinessFinancialRelationsEntity.from(businessFinancialRelations, UUID.fromString(accountHolderId))).getAdminBusinessFinancialRelations();
    }

    public void deleteBusinessFinancialRelations(String accountHolderId) {
        LOG.info("Delete Business Financial Relations by Account Holder id {}", accountHolderId);
        var businessFinancialRelationsEntity = BankLambdaUtils.getBusinessFinancialRelations(accountHolderId, businessFinancialRelationsRepository);
        businessFinancialRelationsRepository.delete(businessFinancialRelationsEntity);
    }

    public BusinessQualifications addBusinessQualifications(BusinessQualificationsData businessQualifications, String accountHolderId) {
        LOG.info("Add Business Qualifications for Account Holder id {}", accountHolderId);
        BankLambdaUtils.checkExistAccountHolder(accountHolderId, accountHolderRepository);
        BankLambdaUtils.checkExistBusinessQualifications(accountHolderId, businessQualificationsRepository);
        return businessQualificationsRepository.save(BusinessQualificationsEntity.from(businessQualifications, UUID.fromString(accountHolderId))).getAdminBusinessQualifications();
    }

    public BusinessQualifications updateBusinessQualifications(BusinessQualificationsData businessQualifications, String accountHolderId) {
        LOG.info("Update Business Qualifications for Account Holder id {}", accountHolderId);
        var businessQualificationsEntity = BankLambdaUtils.getBusinessQualifications(accountHolderId, businessQualificationsRepository);
        return businessQualificationsRepository.update(businessQualificationsEntity.update(businessQualifications)).getAdminBusinessQualifications();
    }

    public void deleteBusinessQualifications(String accountHolderId) {
        LOG.info("Delete Business Qualifications by Account Holder id {}", accountHolderId);
        var businessQualificationsEntity = BankLambdaUtils.getBusinessQualifications(accountHolderId, businessQualificationsRepository);
        businessQualificationsRepository.delete(businessQualificationsEntity);
    }
}
