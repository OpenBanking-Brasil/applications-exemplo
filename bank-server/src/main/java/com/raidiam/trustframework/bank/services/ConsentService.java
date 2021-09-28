package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.domain.ConsentAccountIdEntity;
import com.raidiam.trustframework.bank.domain.ConsentEntity;
import com.raidiam.trustframework.bank.domain.ConsentPermissionEntity;
import com.raidiam.trustframework.bank.domain.LinkedAccountType;
import com.raidiam.trustframework.bank.exceptions.TrustframeworkException;
import com.raidiam.trustframework.bank.repository.ConsentAccountIdRepository;
import com.raidiam.trustframework.bank.repository.ConsentPermissionsRepository;
import com.raidiam.trustframework.bank.repository.ConsentRepository;
import com.raidiam.trustframework.bank.services.validate.*;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

public class ConsentService extends BaseBankService {

    private List<ConsentValidator> validators = List.of(
            new ExpirationDateValidator(),
            new ExpirationDateTransactionToValidator(),
            new ExpirationDateTransationFromValidator(),
            new TransactionToAndFromValidator(),
            new PermissionsGroupingValidator());

    ConsentService(ConsentRepository consentRepository, ConsentPermissionsRepository permissionsRepository, ConsentAccountIdRepository consentAccountIdRepository) {
        this.consentRepository = consentRepository;
        this.permissionsRepository = permissionsRepository;
        this.consentAccountIdRepository = consentAccountIdRepository;
    }

    public Page<ResponseConsent> getConsents (Pageable pageable) {
        return consentRepository.findAll(Optional.ofNullable(pageable).orElse(Pageable.unpaged())).map(ConsentEntity::toResponseConsent);
    }

    @Transactional
    public ResponseConsent createConsent (CreateConsent body, String clientId) {
        validateRequest(body);
        ConsentEntity consentEntity = ConsentEntity.fromRequest(body);
        consentEntity.setClientId(clientId);
        ConsentEntity consent = consentRepository.save(consentEntity);
        for (CreateConsentData.PermissionsEnum permission: body.getData().getPermissions()) {
            ConsentPermissionEntity permissionEntity = ConsentPermissionEntity.fromRequest(permission, consent);
            permissionsRepository.save(permissionEntity);
        }
        ConsentEntity foundConsent = consentRepository.findByConsentId(consent.getConsentId()).orElseThrow(() -> new TrustframeworkException("Could not find recently saved ConsentEntity"));
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
    public ResponseConsentFull updateConsent (String consentId, UpdateConsent request) {
        if(request.getData().getStatus() == null) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Request data missing a status value");
        }
        ConsentEntity consent = consentRepository.findByConsentId(consentId).orElseThrow(() -> new TrustframeworkException("Cannot find consent to update"));

        consent.setStatus(request.getData().getStatus().toString());

        Set<ConsentAccountIdEntity> linkedAccounts = new HashSet<>();

        linkedAccounts.addAll(nullProof(request.getData().getLinkedAccountIds())
                    .stream()
                    .map(a -> ConsentAccountIdEntity.fromRequest(a, LinkedAccountType.BANK_ACCOUNT, consent))
                    .collect(Collectors.toSet())
            );
        linkedAccounts.addAll(nullProof(request.getData().getLinkedCreditCardAccountIds())
                .stream()
                .map(a -> ConsentAccountIdEntity.fromRequest(a, LinkedAccountType.CREDIT_CARD, consent))
                .collect(Collectors.toSet())
        );
        linkedAccounts.addAll(nullProof(request.getData().getLinkedLoanAccountIds())
                .stream()
                .map(a -> ConsentAccountIdEntity.fromRequest(a, LinkedAccountType.LOAN, consent))
                .collect(Collectors.toSet())
        );
        linkedAccounts.addAll(nullProof(request.getData().getLinkedFinancingAccountIds())
                .stream()
                .map(a -> ConsentAccountIdEntity.fromRequest(a, LinkedAccountType.FINANCING, consent))
                .collect(Collectors.toSet())
        );

        consent.setAccountIds(linkedAccounts);
        consent.setStatusUpdateDateTime(new Date());

        return consentRepository.update(consent).getDTOInternal();
    }

    private <T> Collection<T> nullProof(List<T> in) {
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
            validators.stream().forEach(v -> v.validate(body));
        } catch(TrustframeworkException tfe) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, tfe.getMessage());
        }
    }

    private HttpStatusException consentNotFound(String consentId) {
        return new HttpStatusException(HttpStatus.NOT_FOUND, "No consent with ID " + consentId + " found");
    }
}
