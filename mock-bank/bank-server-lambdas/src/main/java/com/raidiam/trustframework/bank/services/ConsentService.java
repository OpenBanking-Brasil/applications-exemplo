package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.domain.*;
import com.raidiam.trustframework.bank.enums.ResourceType;
import com.raidiam.trustframework.bank.exceptions.TrustframeworkException;
import com.raidiam.trustframework.bank.services.validate.*;
import com.raidiam.trustframework.bank.services.validate.consentsextends.*;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
@Transactional
public class ConsentService extends BaseBankService {

    private static final Logger LOG = LoggerFactory.getLogger(ConsentService.class);


    private final List<ConsentValidator> validators = List.of(
            new ExpirationDateValidator(),
            new ExpirationDateTransactionToValidator(),
            new ExpirationDateTransationFromValidator(),
            new TransactionToAndFromValidator(),
            new PermissionsGroupingValidator(),
            new ConsentUserDocumentPresentValidator(),
            new PermissionsGroupingBusinessOrPersonalValidator(),
            new BusinessConsentCnpjPresent());

    private final List<ConsentValidatorV2> validatorsV2 = List.of(
            new ExpirationDateValidator(),
            new PermissionsGroupingValidator(),
            new ConsentUserDocumentPresentValidator(),
            new PermissionsGroupingBusinessOrPersonalValidator(),
            new PermissionsGroupingPersonalAndBusinessEntityValidator());

    private final List<ConsentValidatorV3> validatorsV3 = List.of(
            new ExpirationDateValidator(),
            new PermissionsGroupingValidator(),
            new ConsentUserDocumentPresentValidator(),
            new PermissionsGroupingBusinessOrPersonalValidator(),
            new PermissionsGroupingPersonalAndBusinessEntityValidator());

    private final List<ConsentsExtendsValidatorV2> extendsValidatorsV2 = List.of(
            new ConsentExtendsLoggedUserValidator(),
            new ConsentExtendsExpirationDateTimeValidatorV2()
    );

    private final List<ConsentsExtendsValidatorV3> extendsValidatorsV3 = List.of(
            new ConsentExtendsLoggedUserValidator(),
            new ConsentExtendsExpirationDateTimeValidatorV3(),
            new ConsentExtendsBusinessEntityValidatorV3()
    );

    public Page<ResponseConsent> getConsents(Pageable pageable) {
        return consentRepository.findAll(Optional.ofNullable(pageable).orElse(Pageable.unpaged())).map(ConsentEntity::toResponseConsent);
    }

    public ResponseConsent createConsent(String clientId, @NotNull CreateConsent body) {
        validateRequest(body);

        ConsentEntity consentEntity = createConsentEntity(clientId, ConsentEntity.fromRequest(body),
                body.getData().getPermissions(), body.getData().getLoggedUser());

        var transactionFromDateTime = consentEntity.getTransactionFromDateTime();
        var transactionToDateTime = consentEntity.getTransactionToDateTime();
        if (transactionFromDateTime != null || transactionToDateTime != null) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST,
                    ("Invalid fields transactionFromDateTime and transactionToDateTime"));
        }

        return consentEntity.toResponseConsent();
    }

    public ResponseConsentV2 createConsentV2(String clientId, @NotNull CreateConsentV2 body) {
        validateRequestV2(body);

        return createConsentEntity(clientId, ConsentEntity.fromRequest(body),
                body.getData().getPermissions(), body.getData().getLoggedUser()).toResponseConsentV2();
    }

    public ResponseConsentV3 createConsentV3(String clientId, @NotNull CreateConsentV3 body) {
        validateRequestV3(body);

        return createConsentEntity(clientId, ConsentEntity.fromRequest(body),
                body.getData().getPermissions(), body.getData().getLoggedUser()).toResponseConsentV3();
    }

    private ConsentEntity createConsentEntity(String clientId, ConsentEntity consentEntity, List<EnumConsentPermissions> permissions, LoggedUser loggedUser) {
        var accountHolder = BankLambdaUtils.getAccountHolderByUser(loggedUser, accountHolderRepository);

        consentEntity.setAccountHolderId(accountHolder.getAccountHolderId());
        consentEntity.setClientId(clientId);
        var permissionEntitySet = permissions.stream()
                .map(permission -> ConsentPermissionEntity.fromRequest(permission, consentEntity)).collect(Collectors.toList());
        consentEntity.setConsentPermissions(permissionEntitySet);
        ConsentEntity consent = consentRepository.save(consentEntity);
        return consentRepository.findByConsentId(consent.getConsentId()).orElseThrow(() -> new TrustframeworkException("Could not find recently saved ConsentEntity"));
    }

    public ResponseConsent getConsent(String consentId, String clientId) {
        ConsentEntity entity = BankLambdaUtils.getConsent(consentId, consentRepository);
        checkClientId(entity, clientId);
        return entity.toResponseConsent();
    }

    public ResponseConsentReadV2 getConsentV2(String consentId, String clientId) {
        ConsentEntity entity = BankLambdaUtils.getConsent(consentId, consentRepository);
        checkClientId(entity, clientId);
        return checkForExpired(entity).toResponseConsentReadV2();
    }

    public ResponseConsentReadV3 getConsentV3(String consentId, String clientId) {
        ConsentEntity entity = BankLambdaUtils.getConsent(consentId, consentRepository);
        checkClientId(entity, clientId);
        return checkForExpired(entity).toResponseConsentReadV3();
    }

    public ResponseConsentFull getConsentFull(String consentId) {
        ConsentEntity entity = BankLambdaUtils.getConsent(consentId, consentRepository);
        return entity.getDTOInternal();
    }

    public ResponseConsentFullV2 getConsentFullV2(String consentId) {
        ConsentEntity entity = BankLambdaUtils.getConsent(consentId, consentRepository);
        return checkForExpired(entity).getDTOInternalV2();
    }

    public ResponseConsentFull updateConsent(@NotNull String consentId, UpdateConsent request) {
        ConsentEntity consent = updateConsentEntity(consentId, request);
        return consent.getDTOInternal();
    }

    public ResponseConsentFullV2 updateConsentV2(@NotNull String consentId, UpdateConsent request) {
        ConsentEntity consent = updateConsentEntity(consentId, request);
        return consent.getDTOInternalV2();
    }

    public ResponseConsentV2 createConsentExtension(@NotNull String consentId, CreateConsentExtends request) {
        Date expirationDateTime = BankLambdaUtils.offsetDateToDate(request.getData().getExpirationDateTime());
        String identification = request.getData().getLoggedUser().getDocument().getIdentification();
        String rel = request.getData().getLoggedUser().getDocument().getRel();

        mockConsentExtendsResponses(identification);

        ConsentEntity consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);

        if (!consentEntity.getStatus().equals(EnumConsentStatus.AUTHORISED.toString())) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, String.format("%s: only AUTHORISED consents can be extended", EnumConsentExtendsErrorCode.ESTADO_CONSENTIMENTO_INVALIDO));
        }

        validateConsentExtendsV2(request, consentEntity);

        ConsentExtensionEntity consentExtensionEntity = new ConsentExtensionEntity(consentEntity, expirationDateTime, identification, rel);
        consentExtensionEntity = consentExtensionRepository.save(consentExtensionEntity);
        LOG.info("Saved consent extension {}", consentExtensionEntity);

        consentEntity.setExpirationDateTime(expirationDateTime);
        consentEntity = consentRepository.update(consentEntity);
        LOG.info("updated consent with new expiration time {}", consentEntity);

        return consentEntity.toResponseConsentV2();
    }

    public ResponseConsentV3 createConsentExtensionV3(@NotNull String consentId, CreateConsentExtendsV3 request, String customerIpAddress, String customerUserAgent) {
        Date expirationDateTime = BankLambdaUtils.offsetDateToDate(request.getData().getExpirationDateTime());
        String identification = request.getData().getLoggedUser().getDocument().getIdentification();
        String rel = request.getData().getLoggedUser().getDocument().getRel();

        mockConsentExtendsResponses(identification);
        
        ConsentEntity consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);

        if (!consentEntity.getStatus().equals(EnumConsentStatus.AUTHORISED.toString())) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, String.format("%s: only AUTHORISED consents can be extended", EnumConsentExtendsErrorCode.ESTADO_CONSENTIMENTO_INVALIDO));
        }

        validateConsentExtendsV3(request, consentEntity);

        ConsentExtensionEntity consentExtensionEntity = new ConsentExtensionEntity(consentEntity, expirationDateTime, identification, rel)
                .setPreviousExpirationDateTime(consentEntity.getExpirationDateTime())
                .setXCustomerUserAgent(customerUserAgent)
                .setXFapiCustomerIpAddress(customerIpAddress);

        consentExtensionEntity = consentExtensionRepository.save(consentExtensionEntity);
        LOG.info("Saved consent extension {}", consentExtensionEntity);

        consentEntity.setExpirationDateTime(expirationDateTime);
        consentEntity = consentRepository.update(consentEntity);
        LOG.info("updated consent with new expiration time {}", consentEntity);

        return consentEntity.toResponseConsentV3();
    }


    private void validateConsentExtendsV3(CreateConsentExtendsV3 req, ConsentEntity consentEntity) {
        extendsValidatorsV3.forEach(validator -> validator.validate(req, consentEntity));
    }

    private void validateConsentExtendsV2(CreateConsentExtends req, ConsentEntity consentEntity) {
        extendsValidatorsV2.forEach(validator -> validator.validate(req, consentEntity));
    }


    private void mockConsentExtendsResponses(String identification) {
        LOG.info("Checking mock responses");
        switch (identification) {
            case "00000000000":
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        String.format("%s: %s", EnumConsentExtendsErrorCode.ESTADO_CONSENTIMENTO_INVALIDO, EnumConsentExtendsErrorCode.ESTADO_CONSENTIMENTO_INVALIDO));
            case "00000000001":
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        String.format("%s: %s", EnumConsentExtendsErrorCode.REFRESH_TOKEN_JWT, EnumConsentExtendsErrorCode.REFRESH_TOKEN_JWT));
            case "00000000002":
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        String.format("%s: %s", EnumConsentExtendsErrorCode.DEPENDE_MULTIPLA_ALCADA, EnumConsentExtendsErrorCode.DEPENDE_MULTIPLA_ALCADA));
            default:
                LOG.info("loggedUser.document.identification did not match, skipping");
        }
    }

    public ResponseConsentReadExtends getConsentExtensions(String consentId) {
        List<ConsentExtensionEntity> consentExtensions = consentRepository.findByConsentId(consentId)
                .map(ConsentEntity::getConsentExtensions)
                .orElse(new ArrayList<>());


        List<ResponseConsentReadExtendsData> extensions = consentExtensions.stream()
                .sorted(Comparator.comparing(ConsentExtensionEntity::getRequestDateTime).reversed())
                .map(ConsentExtensionEntity::getDTO)
                .collect(Collectors.toList());

        return new ResponseConsentReadExtends()
                .data(extensions);
    }

    public ResponseConsentReadExtensionsV3 getConsentExtensionsV3(String consentId, Pageable pageable) {
        Page<ConsentExtensionEntity> consentExtensionEntityPage = consentExtensionRepository.findByConsentId(consentId, pageable);
        var response = new ResponseConsentReadExtensionsV3().data(consentExtensionEntityPage.getContent()
                    .stream()
                    .sorted(Comparator.comparing(ConsentExtensionEntity::getRequestDateTime).reversed())
                    .map(ConsentExtensionEntity::getDTOV3)
                    .collect(Collectors.toList()));

        response.setMeta(BankLambdaUtils.getMeta(consentExtensionEntityPage));
        return response;
    }


    private ConsentEntity updateConsentEntity(String consentId, UpdateConsent request) {
        if (request.getData().getStatus() == null) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Request data missing a status value");
        }
        ConsentEntity consent = BankLambdaUtils.getConsent(consentId, consentRepository);
        checkForExpired(consent);
        consentAlreadyRejected(consent.getStatus(), HttpStatus.BAD_REQUEST, null);

        if (request.getData().getStatus().equals(UpdateConsentData.StatusEnum.REJECTED)) {
            consent.setRejectionCode(consent.getStatus().equals(EnumConsentStatus.AWAITING_AUTHORISATION.name()) ?
                    EnumReasonCodeV2.CUSTOMER_MANUALLY_REJECTED.name() : EnumReasonCodeV2.CUSTOMER_MANUALLY_REVOKED.name());
            consent.setRejectedBy(EnumRejectedByV2.USER.name());
            consent.setStatus(UpdateConsentData.StatusEnum.REJECTED.name());
            consent.setStatusUpdateDateTime(new Date());
            return consentRepository.update(consent);
        }

        consent.setStatus(request.getData().getStatus().toString());

        addAccountsToConsent(consent, nullProof(request.getData().getLinkedAccountIds()));

        addCreditCardAccountsToConsent(consent, nullProof(request.getData().getLinkedCreditCardAccountIds()));

        addContractsToConsent(nullProof(request.getData().getLinkedLoanAccountIds()),
                "Loan Contract", consent, ResourceType.LOAN);

        addContractsToConsent(nullProof(request.getData().getLinkedFinancingAccountIds()),
                "Financing Contract", consent, ResourceType.FINANCING);

        addContractsToConsent(nullProof(request.getData().getLinkedInvoiceFinancingAccountIds()),
                "Invoice Financing Contract", consent, ResourceType.INVOICE_FINANCING);

        addContractsToConsent(nullProof(request.getData().getLinkedUnarrangedOverdraftAccountIds()),
                "Unarranged Overdraft Contract", consent, ResourceType.UNARRANGED_ACCOUNT_OVERDRAFT);

        addInvestmentToConsent(nullProof(request.getData().getLinkedBankFixedIncomesAccountIds()),
                "Bank Fixed Incomes", consent, ResourceType.BANK_FIXED_INCOME);

        addInvestmentToConsent(nullProof(request.getData().getLinkedCreditFixedIncomesAccountIds()),
                "Credit Fixed Incomes", consent, ResourceType.CREDIT_FIXED_INCOME);

        addInvestmentToConsent(nullProof(request.getData().getLinkedVariableIncomesAccountIds()),
                "Variable Incomes", consent, ResourceType.VARIABLE_INCOME);

        addInvestmentToConsent(nullProof(request.getData().getLinkedTreasureTitlesAccountIds()),
                "Treasure Titles", consent, ResourceType.TREASURE_TITLE);

        addInvestmentToConsent(nullProof(request.getData().getLinkedFundsAccountIds()),
                "Funds", consent, ResourceType.FUND);

        addExchangesOperationsToConsent(nullProof(request.getData().getLinkedExchangeOperationIds()), consent);

        consent.setStatusUpdateDateTime(new Date());
        consentRepository.update(consent);
        // the response to the update command above does not populate the linked accounts and contracts, so something
        // we are doing here needs improvement. For now a new lookup on the consent works for us.
        return consentRepository.findById(consent.getReferenceId())
                .orElseThrow(() -> new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not look up consent that has just been updated"));
    }

    private void addAccountsToConsent(ConsentEntity consent, List<String> requestAccounts) {
        addObjectToConsent(consent, requestAccounts, "Account",
                accountRepository::findByAccountId,
                AccountEntity::getAccountHolder,
                b -> consentAccountRepository.save(new ConsentAccountEntity(consent, b)));
    }

    private void addCreditCardAccountsToConsent(ConsentEntity consent, List<String> requestAccounts) {
        addObjectToConsent(consent, requestAccounts, "Credit Card Account",
                creditCardAccountsRepository::findByCreditCardAccountId,
                CreditCardAccountsEntity::getAccountHolder,
                b -> consentCreditCardAccountsRepository.save(new ConsentCreditCardAccountsEntity(consent, b)));
    }

    private void addContractsToConsent(List<String> contractAccounts, String logType, ConsentEntity consent, ResourceType contractType) {
        addObjectToConsent(consent, contractAccounts, logType,
                a -> contractsRepository.findByContractIdAndContractType(a, contractType.toString()),
                ContractEntity::getAccountHolder,
                b -> consentContractRepository.save(new ConsentContractEntity(consent, b)));
    }

    private void addInvestmentToConsent(List<String> investmentsAccounts, String logType, ConsentEntity consent, ResourceType investmentType) {
        var ci = consentInvestmentRepository.findByConsentId(consent.getConsentId());
        addObjectToConsent(consent, investmentsAccounts, logType,
                a -> findInvestmentEntity(a, investmentType),
                b -> getInvestmentAccountHolderEntity(b, investmentType),
                c -> consentInvestmentRepository.save(ci.orElseGet(() -> new ConsentInvestmentEntity(consent)).setInvestmentsId(c)));
    }

    private void addExchangesOperationsToConsent(List<String> requestAccounts, ConsentEntity consent) {
        addObjectToConsent(consent, requestAccounts, "Exchanges operations",
                exchangesOperationRepository::findByOperationId,
                ExchangesOperationEntity::getAccountHolder,
                a -> consentExchangeOperationRepository.save(new ConsentExchangeOperationEntity(consent, a)));
    }

    private Optional<? extends BaseEntity> findInvestmentEntity(UUID a, ResourceType investmentType) {
        switch (investmentType) {
            case BANK_FIXED_INCOME:
                return bankFixedIncomesRepository.findByInvestmentId(a);
            case CREDIT_FIXED_INCOME:
                return creditFixedIncomesRepository.findByInvestmentId(a);
            case VARIABLE_INCOME:
                return variableIncomesRepository.findByInvestmentId(a);
            case TREASURE_TITLE:
                return treasureTitlesRepository.findByInvestmentId(a);
            case FUND:
                return fundsRepository.findByInvestmentId(a);
            default:
                return Optional.empty();
        }
    }

    private AccountHolderEntity getInvestmentAccountHolderEntity(BaseEntity entity, ResourceType investmentType) {
        switch (investmentType) {
            case BANK_FIXED_INCOME:
                return ((BankFixedIncomesEntity) entity).getAccountHolder();
            case CREDIT_FIXED_INCOME:
                return ((CreditFixedIncomesEntity) entity).getAccountHolder();
            case VARIABLE_INCOME:
                return ((VariableIncomesEntity) entity).getAccountHolder();
            case TREASURE_TITLE:
                return ((TreasureTitlesEntity) entity).getAccountHolder();
            case FUND:
                return ((FundsEntity) entity).getAccountHolder();
            default:
                return null;
        }
    }

    private <T> void addObjectToConsent(ConsentEntity consent, List<String> requestAccounts, String logType,
                                        Function<UUID, Optional<T>> entityFinder,
                                        Function<T, AccountHolderEntity> getAccountHolder,
                                        Consumer<T> consentBinder) {
        for (var accountId : requestAccounts) {
            var entity = entityFinder.apply(UUID.fromString(accountId))
                    .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, String.format("%s could not be found, cannot update consent", logType)));
            if (!getAccountHolder.apply(entity).equals(consent.getAccountHolder())) {
                throw new HttpStatusException(HttpStatus.FORBIDDEN, String.format("%s does not belong to this accountHolder", logType));
            }
            consentBinder.accept(entity);
        }
    }

    private <T> List<T> nullProof(List<T> in) {
        return Optional.ofNullable(in)
                .orElse(Collections.emptyList());
    }


    public void deleteConsentV2(String consentId, String clientId) {
        ConsentEntity entity = BankLambdaUtils.getConsent(consentId, consentRepository);
        consentAlreadyRejected(entity.getStatus(), HttpStatus.BAD_REQUEST, null);
        deleteConsent(clientId, entity);
    }

    public void deleteConsentV3(String consentId, String clientId) {
        ConsentEntity entity = BankLambdaUtils.getConsent(consentId, consentRepository);
        consentAlreadyRejected(entity.getStatus(), HttpStatus.UNPROCESSABLE_ENTITY,
                EnumConsentsErrorCodesV3.CONSENTIMENTO_EM_STATUS_REJEITADO.toString());
        deleteConsent(clientId, entity);
    }
    private void deleteConsent(String clientId, ConsentEntity entity) {
        checkClientId(entity, clientId);
        checkForExpired(entity);

        entity.setRejectionCode(entity.getStatus().equals(EnumConsentStatus.AWAITING_AUTHORISATION.name()) ?
                EnumReasonCodeV2.CUSTOMER_MANUALLY_REJECTED.name() : EnumReasonCodeV2.CUSTOMER_MANUALLY_REVOKED.name());
        entity.setStatus(EnumConsentStatus.REJECTED.name());
        entity.setRejectedBy(EnumRejectedByV2.USER.name());
        consentRepository.update(entity);
    }

    private void validateRequest(CreateConsent body) {
        try {
            validators.forEach(v -> v.validate(body));
        } catch (TrustframeworkException tfe) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, tfe.getMessage());
        }
    }

    private void validateRequestV2(CreateConsentV2 body) {
        try {
            validatorsV2.forEach(v -> v.validate(body));
        } catch (TrustframeworkException tfe) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, tfe.getMessage());
        }
    }

    private void validateRequestV3(CreateConsentV3 body) {
        try {
            validatorsV3.forEach(v -> v.validate(body));
        } catch (TrustframeworkException tfe) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, tfe.getMessage());
        }
    }

    private ConsentEntity checkForExpired(ConsentEntity consent) {
        //Check Status
        if (consent.getStatus().equals(EnumConsentStatus.REJECTED.name())) return consent;

        //Check EXPIRED
        if (consent.getStatus().equals(EnumConsentStatus.AWAITING_AUTHORISATION.name())) {
            var dateCreate = BankLambdaUtils.dateToOffsetDate(consent.getStatusUpdateDateTime());
            if (dateCreate.isBefore(OffsetDateTime.now().minusHours(1))) {
                consent.setStatus(EnumConsentStatus.REJECTED.name());
                consent.setStatusUpdateDateTime(new Date());
                consent.setRejectedBy(EnumRejectedByV2.ASPSP.name());
                consent.setRejectionCode(EnumReasonCodeV2.CONSENT_EXPIRED.name());
                return consentRepository.update(consent);
            }
        }

        //Check MAX_DATED_REACHED
        var expirationDate = BankLambdaUtils.dateToOffsetDate(consent.getExpirationDateTime());
        if (expirationDate != null && expirationDate.isBefore(OffsetDateTime.now())) {
            consent.setStatus(EnumConsentStatus.REJECTED.name());
            consent.setStatusUpdateDateTime(new Date());
            consent.setRejectedBy(EnumRejectedByV2.ASPSP.name());
            consent.setRejectionCode(EnumReasonCodeV2.CONSENT_MAX_DATE_REACHED.name());
            return consentRepository.update(consent);
        }

        return consent;
    }

    private void consentAlreadyRejected(String status, HttpStatus returnStatus, String errorCode) {
        if (status.equals(EnumConsentStatus.REJECTED.name())) {
            errorCode = errorCode == null ? "" : errorCode + ": ";
            throw new HttpStatusException(returnStatus, String.format("%sConsent request already in a rejected state", errorCode));
        }
    }

    private void checkClientId(ConsentEntity entity, String clientId) {
        if (!entity.getClientId().equals(clientId)) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Requested a consent created with a different oauth client");
        }
    }
}
