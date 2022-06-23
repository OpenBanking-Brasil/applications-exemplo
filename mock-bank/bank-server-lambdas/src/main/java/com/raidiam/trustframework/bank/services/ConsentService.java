package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.domain.*;
import com.raidiam.trustframework.bank.enums.AccountOrContractType;
import com.raidiam.trustframework.bank.exceptions.TrustframeworkException;
import com.raidiam.trustframework.bank.services.validate.*;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConsentService extends BaseBankService {

    private final List<ConsentValidator> validators = List.of(
            new ExpirationDateValidator(),
            new ExpirationDateTransactionToValidator(),
            new ExpirationDateTransationFromValidator(),
            new TransactionToAndFromValidator(),
            new PermissionsGroupingValidator(),
            new ConsentUserDocumentPresentValidator(),
            new PermissionsGroupingBusinessOrPersonalValidator());

    public Page<ResponseConsent> getConsents (Pageable pageable) {
        return consentRepository.findAll(Optional.ofNullable(pageable).orElse(Pageable.unpaged())).map(ConsentEntity::toResponseConsent);
    }

    @Transactional
    public ResponseConsent createConsent (String clientId, @NotNull CreateConsent body) {
        validateRequest(body);

        var userDocument = body.getData().getLoggedUser().getDocument();
        var accountHolder = accountHolderRepository.findByDocumentIdentificationAndDocumentRel(userDocument.getIdentification(), userDocument.getRel()).stream().findAny();
        if (accountHolder.isEmpty()) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, String.format("User with documentation Id %s and rel %s not found", userDocument.getIdentification(), userDocument.getRel()));
        }

        ConsentEntity consentEntity = ConsentEntity.fromRequest(body);
        consentEntity.setAccountHolderId(accountHolder.get().getAccountHolderId());
        consentEntity.setClientId(clientId);
        ConsentEntity consent = consentRepository.save(consentEntity);
        for (CreateConsentData.PermissionsEnum permission: body.getData().getPermissions()) {
            ConsentPermissionEntity permissionEntity = ConsentPermissionEntity.fromRequest(permission, consent);
            permissionsRepository.save(permissionEntity);
        }
        ConsentEntity foundConsent = consentRepository.findByConsentId(consent.getConsentId()).orElseThrow(() -> new TrustframeworkException("Could not find recently saved ConsentEntity"));

        var transactionFromDateTime =consentEntity.getTransactionFromDateTime();
        var transactionToDateTime = consentEntity.getTransactionToDateTime();
        if(transactionFromDateTime != null || transactionToDateTime != null){
            throw new HttpStatusException(HttpStatus.BAD_REQUEST,
                    ("Invalid fields transactionFromDateTime and transactionToDateTime"));
        }

        return foundConsent.toResponseConsent();
    }

    public ResponseConsent getConsent(String consentId, String clientId) {
        ConsentEntity entity = consentRepository.findByConsentId(consentId)
                .orElseThrow(() -> consentNotFound(consentId));
        if(!entity.getClientId().equals(clientId)) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Requested a consent created with a different oauth client");
        }
        return entity.toResponseConsent();
    }

    public ResponseConsentFull getConsentFull(String consentId) {
        return consentRepository.findByConsentId(consentId)
                .map(ConsentEntity::getDTOInternal)
                .orElseThrow(() -> consentNotFound(consentId));
    }

    @Transactional
    public ResponseConsentFull updateConsent (@NotNull String consentId, UpdateConsent request) {
        if(request.getData().getStatus() == null) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Request data missing a status value");
        }
        ConsentEntity consent = BankLambdaUtils.getConsent(consentId, consentRepository);

        consent.setStatus(request.getData().getStatus().toString());

        addAccountsToConsent(consent, request);

        addCreditCardAccountsToConsent(consent, request);

        var requestLoanAccounts = nullProof(request.getData().getLinkedLoanAccountIds());
        addContractsToConsent(requestLoanAccounts, "Loan Contract", consent, AccountOrContractType.LOAN);

        var requestFinancingAccounts = nullProof(request.getData().getLinkedFinancingAccountIds());
        addContractsToConsent(requestFinancingAccounts, "Financing Contract", consent, AccountOrContractType.FINANCING);

        var requestInvoiceFinancingAccounts = nullProof(request.getData().getLinkedInvoiceFinancingAccountIds());
        addContractsToConsent(requestInvoiceFinancingAccounts, "Invoice Financing Contract", consent, AccountOrContractType.INVOICE_FINANCING);

        var requestOverDraftAccounts = nullProof(request.getData().getLinkedUnarrangedOverdraftAccountIds());
        addContractsToConsent(requestOverDraftAccounts, "Unarranged Overdraft Contract", consent, AccountOrContractType.UNARRANGED_ACCOUNT_OVERDRAFT);

        consent.setStatusUpdateDateTime(new Date());
        consentRepository.update(consent);
        // the response to the update command above does not populate the linked accounts and contracts, so something
        // we are doing here needs improvement. For now a new lookup on the consent works for us.
        return consentRepository.findById(consent.getReferenceId()).map(ConsentEntity::getDTOInternal)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not look up consent that has just been updated"));
    }

    private void addAccountsToConsent(ConsentEntity consent, UpdateConsent request) {
        var requestAccounts = nullProof(request.getData().getLinkedAccountIds());
        addObjectToConsent(consent, requestAccounts, "Account",
                accountRepository::findByAccountId,
                AccountEntity::getAccountHolder,
                b -> consentAccountRepository.save(new ConsentAccountEntity(consent,b)));
    }

    private void addCreditCardAccountsToConsent(ConsentEntity consent, UpdateConsent request) {
        var requestAccounts = nullProof(request.getData().getLinkedCreditCardAccountIds());
        addObjectToConsent(consent, requestAccounts, "Credit Card Account",
                creditCardAccountsRepository::findByCreditCardAccountId,
                CreditCardAccountsEntity::getAccountHolder,
                b -> consentCreditCardAccountsRepository.save(new ConsentCreditCardAccountsEntity(consent,b)));
    }

    private void addContractsToConsent(List<String> contractAccounts, String logType, ConsentEntity consent, AccountOrContractType contractType) {
        addObjectToConsent(consent, contractAccounts, logType,
                a -> contractsRepository.findByContractIdAndContractType(a, contractType.toString()),
                ContractEntity::getAccountHolder,
                b->consentContractRepository.save(new ConsentContractEntity(consent, b)));
    }

    private <T> void addObjectToConsent(ConsentEntity consent, List<String> requestAccounts, String logType,
                                        Function<UUID, Optional<T>> entityFinder,
                                        Function<T, AccountHolderEntity> getAccountHolder,
                                        Consumer<T> consentBinder) {
        for (var accountId: requestAccounts) {
            var entity = entityFinder.apply(UUID.fromString(accountId))
                    .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, String.format("%s could not be found, cannot update consent", logType)));
            if(!getAccountHolder.apply(entity).equals(consent.getAccountHolder())) {
                throw new HttpStatusException(HttpStatus.FORBIDDEN, String.format("%s does not belong to this accountHolder", logType));
            }
            consentBinder.accept(entity);
        }
    }

    private <T> List<T> nullProof(List<T> in) {
        return Optional.ofNullable(in)
                .orElse(Collections.emptyList());
    }

    public void deleteConsent (String consentId, String clientId) {
        ConsentEntity entity = consentRepository.findByConsentId(consentId)
                .orElseThrow(() -> consentNotFound(consentId));
        if(!entity.getClientId().equals(clientId)) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Requested a consent created with a different oauth client");
        }
        consentRepository.deleteByConsentId(consentId);
    }

    private void validateRequest(CreateConsent body) {
        try {
            validators.forEach(v -> v.validate(body));
        } catch(TrustframeworkException tfe) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, tfe.getMessage());
        }
    }

    private HttpStatusException consentNotFound(String consentId) {
        return new HttpStatusException(HttpStatus.NOT_FOUND, "No consent with ID " + consentId + " found");
    }
}
