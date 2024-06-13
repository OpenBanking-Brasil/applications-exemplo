package com.raidiam.trustframework.bank.services;

import com.google.common.base.Strings;
import com.nimbusds.jose.jwk.RSAKey;
import com.raidiam.trustframework.bank.domain.*;
import com.raidiam.trustframework.bank.exceptions.TrustframeworkException;
import com.raidiam.trustframework.bank.services.validate.EnrollmentAccountTypeValidator;
import com.raidiam.trustframework.bank.services.validate.EnrollmentPermissionsValidator;
import com.raidiam.trustframework.bank.services.validate.EnrollmentValidator;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.bank.utils.FidoUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import com.webauthn4j.data.attestation.AttestationObject;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.attestation.authenticator.COSEKey;
import com.webauthn4j.data.attestation.statement.COSEKeyType;
import com.webauthn4j.data.client.CollectedClientData;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.raidiam.trustframework.bank.utils.FidoUtils.*;

@Singleton
@Transactional
public class EnrollmentService extends BaseBankService {
    private static final Logger LOG = LoggerFactory.getLogger(EnrollmentService.class);
    private static final String RP_INVALIDA_MESSAGE = "RP_INVALIDA: RP Invalida";

    @Value("${mockbank.mockbankUrl}")
    private String appBaseUrl;

    private final List<EnrollmentValidator> validators = List.of(
            new EnrollmentPermissionsValidator(),
            new EnrollmentAccountTypeValidator());

    public ResponseCreateEnrollment createEnrollment(String clientId, String idempotencyKey, String jti, CreateEnrollment body) {
        validateRequest(body);

        BankLambdaUtils.checkAndSaveJti(jtiRepository, jti);

        var userDocument = body.getData().getLoggedUser().getDocument();
        var accountHolder = accountHolderRepository
                .findByDocumentIdentificationAndDocumentRel(userDocument.getIdentification(), userDocument.getRel())
                .stream()
                .findAny()
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, String.format("User with documentation Id %s and rel %s not found", userDocument.getIdentification(), userDocument.getRel())));

        var accountWithDebtor = checkAndSetDebtor(body.getData().getDebtorAccount(), accountHolder);

        return enrollmentRepository.findByIdempotencyKey(idempotencyKey)
                .orElseGet(() -> enrollmentRepository
                        .save(EnrollmentEntity.from(body, clientId, idempotencyKey, accountWithDebtor.orElse(null), accountHolder))).getDTO();
    }

    private void validateRequest(CreateEnrollment body) {
        try {
            validators.forEach(v -> v.validate(body));
        } catch (TrustframeworkException tfe) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, tfe.getMessage());
        }
    }

    private Optional<AccountEntity> checkAndSetDebtor(DebtorAccount debtor, AccountHolderEntity accountHolder) {
        if (debtor == null) return Optional.empty();
        AccountEntity account = accountRepository.findByNumberAndAccountHolderId(debtor.getNumber(), accountHolder.getAccountHolderId())
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Account with number %s not found", debtor.getNumber())));
        if (account.setDebtorAccount(debtor) != null) {
            return Optional.of(accountRepository.update(account));
        }
        return Optional.empty();
    }

    public ResponseEnrollment getEnrollment(String enrollmentId, String clientId, boolean isPaymentFullManage) {
        EnrollmentEntity enrollmentEntity = enrollmentRepository.findByEnrollmentId(enrollmentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "No enrollment with ID " + enrollmentId + " found"));

        if (!isPaymentFullManage && !enrollmentEntity.getClientId().equals(clientId)) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Requested a enrollment created with a different oauth client");
        }

        OffsetDateTime creationTime = BankLambdaUtils.dateToOffsetDate(enrollmentEntity.getCreationDateTime());
        OffsetDateTime statusUpdateTime = BankLambdaUtils.dateToOffsetDate(enrollmentEntity.getStatusUpdateDateTime());

        if (creationTime.isBefore(OffsetDateTime.now().minusMinutes(5)) && enrollmentEntity.getStatus().equals(EnumEnrollmentStatus.AWAITING_RISK_SIGNALS.name())) {
            enrollmentEntity.setRejectReason(EnrollmentRejectionReason.TEMPO_EXPIRADO_RISK_SIGNALS.toString());
            enrollmentEntity.setRejectedAt(Date.from(creationTime.plusMinutes(5).toInstant()));
            enrollmentRepository.update(enrollmentEntity);
        }
        if (statusUpdateTime.isBefore(OffsetDateTime.now().minusMinutes(15)) && enrollmentEntity.getStatus().equals(EnumEnrollmentStatus.AWAITING_ACCOUNT_HOLDER_VALIDATION.name())) {
            enrollmentEntity.setRejectReason(EnrollmentRejectionReason.TEMPO_EXPIRADO_ACCOUNT_HOLDER_VALIDATION.toString());
            enrollmentEntity.setRejectedAt(Date.from(statusUpdateTime.plusMinutes(15).toInstant()));
            enrollmentRepository.update(enrollmentEntity);
        }
        if (statusUpdateTime.isBefore(OffsetDateTime.now().minusMinutes(5)) && enrollmentEntity.getStatus().equals(EnumEnrollmentStatus.AWAITING_ENROLLMENT.name())) {
            enrollmentEntity.setRejectReason(EnrollmentRejectionReason.TEMPO_EXPIRADO_ENROLLMENT.toString());
            enrollmentEntity.setRejectedAt(Date.from(statusUpdateTime.plusMinutes(5).toInstant()));
            enrollmentRepository.update(enrollmentEntity);
        }

        ResponseEnrollment enrollmentResponse = enrollmentEntity.getResponseDTO();

        String responseEnrollmentId = enrollmentResponse.getData().getEnrollmentId();
        enrollmentResponse.setLinks(new Links().self(appBaseUrl + "/open-banking/enrollments/v1/enrollments/" + responseEnrollmentId));
        enrollmentResponse.setMeta(new MetaOnlyRequestDateTime().requestDateTime(OffsetDateTime.now()));
        return enrollmentResponse;
    }

    public void updateEnrollment(String enrollmentId, PatchEnrollment request) {
        final var cancellation = Optional.ofNullable(request.getData())
                .map(PatchEnrollmentData::getCancellation)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "Could not find cancellation"));

        final var reason = Optional.ofNullable(cancellation.getReason())
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "Could not find cancellation reason"));

        Optional<EnrollmentRejectionReason> rejectionReason = Optional.ofNullable(reason.getRejectionReason());
        Optional<EnrollmentRevocationReason> revocationReason = Optional.ofNullable(reason.getRevocationReason());

        if ((rejectionReason.isEmpty() && revocationReason.isEmpty()) ||
                (rejectionReason.isPresent() && revocationReason.isPresent())) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Either Rejection Reason or Revocation Reason must be provided");
        }

        final EnrollmentEntity enrollment = enrollmentRepository.findByEnrollmentId(enrollmentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND,
                        String.format("Could not find Enrollment with ID - %s", enrollmentId)));

        LOG.info("fetched enrollment  - {}", enrollment);


        final EnumEnrollmentStatus enrollmentStatus = EnumEnrollmentStatus.fromValue(enrollment.getStatus());

        if ((EnumEnrollmentStatus.AWAITING_RISK_SIGNALS.equals(enrollmentStatus) || EnumEnrollmentStatus.AWAITING_ENROLLMENT.equals(enrollmentStatus) || EnumEnrollmentStatus.AWAITING_ACCOUNT_HOLDER_VALIDATION.equals(enrollmentStatus))
                && revocationReason.isPresent()) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, String.format("%s: Enrollment cannot be revoked if the status is %s", EnrollmentCancelErrorCode.MOTIVO_REVOGACAO, enrollmentStatus));
        }

        if (EnumEnrollmentStatus.AUTHORISED.equals(enrollmentStatus) && rejectionReason.isPresent()) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, String.format("%s: Enrollment cannot be rejected if the status is %s", EnrollmentCancelErrorCode.MOTIVO_REJEICAO, enrollmentStatus));
        }


        rejectionReason.ifPresent(r -> {
            LOG.info("Rejection reason is present in the request");
            enrollment.setStatus(EnumEnrollmentStatus.REJECTED.toString());
            enrollment.setRejectReason(r.toString());
        });

        revocationReason.ifPresent(r -> {
            LOG.info("Revocation reason is present in the request");
            enrollment.setStatus(EnumEnrollmentStatus.REVOKED.toString());
            enrollment.setRevocationReason(r.toString());
        });

        enrollment.setCancelledFrom(EnumEnrollmentCancelledFrom.INICIADORA.toString());
        enrollment.setRejectedAt(new Date());
        Optional.ofNullable(cancellation.getAdditionalInformation())
                .ifPresent(enrollment::setAdditionalInformation);

        Optional.ofNullable(cancellation.getCancelledBy()).ifPresent(cancelledBy -> {
            LOG.info("Cancelled by is present in the request");

            String identification = Optional.ofNullable(cancelledBy.getDocument().getIdentification())
                    .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "document.identification must be provided if cancelledBy is present"));

            String rel = Optional.ofNullable(cancelledBy.getDocument().getRel())
                    .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "document.rel must be provided if cancelledBy is present"));

            enrollment.setCancelledByDocumentIdentification(identification);
            enrollment.setCancelledByDocumentRel(rel);
        });

        enrollmentRepository.update(enrollment);
        LOG.info("enrollment has been updated - {}", enrollment);

    }

    public void createRiskSignal(String enrollmentId, RiskSignals body) {
        var enrollmentEntity = getEnrollmentEntity(enrollmentId);
        validateEnrollmentStatus(EnumEnrollmentStatus.AWAITING_RISK_SIGNALS, enrollmentEntity.getStatus());
        enrollmentRiskSignalsRepository.save(EnrollmentRiskSignalsEntity.from(enrollmentId, body));

        enrollmentEntity.setStatus(EnumEnrollmentStatus.AWAITING_ACCOUNT_HOLDER_VALIDATION.name());
        enrollmentRepository.save(enrollmentEntity);
    }

    private void validateEnrollmentStatus(EnumEnrollmentStatus expected, String actual) {
        if (!actual.equals(expected.name())) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "STATUS_VINCULO_INVALIDO: Vinculo inválido");
        }
    }

    private EnrollmentEntity getEnrollmentEntity(String enrollmentId) {
        return enrollmentRepository.findByEnrollmentId(enrollmentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
    }

    public EnrollmentFidoRegistrationOptions createFidoRegistrationOptions(String enrollmentId, EnrollmentFidoOptionsInput body, Optional<String> certificateCn) {
        var enrollmentEntity = getEnrollmentEntity(enrollmentId);
        validateEnrollmentStatus(EnumEnrollmentStatus.AWAITING_ENROLLMENT, enrollmentEntity.getStatus());
        certificateCn.ifPresentOrElse(c -> {
            if (!body.getData().getRp().equals(c)) {
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, RP_INVALIDA_MESSAGE);
            }
        }, () -> {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, RP_INVALIDA_MESSAGE);
        });


        return enrollmentFidoRegistrationOptionsRepository.save(EnrollmentFidoRegistrationOptionsEntity.from(enrollmentEntity, body)).getDTO();
    }


    public void rejectEnrollment(String enrollmentId) {
        enrollmentRepository.findByEnrollmentId(enrollmentId)
                .ifPresentOrElse(
                        e -> {
                            e.setStatus(EnumEnrollmentStatus.REJECTED.toString());
                            enrollmentRepository.update(e);
                            LOG.info("enrollemt - {} has been rejected", enrollmentId);
                        },
                        () -> LOG.info("Could not find enrollment with ID - {}, skipping...", enrollmentId)
                );
    }

    public void createFidoRegistration(String enrollmentId, EnrollmentFidoRegistration request) {
        var enrollmentEntity = getEnrollmentEntity(enrollmentId);
        validateEnrollmentStatus(EnumEnrollmentStatus.AWAITING_ENROLLMENT, enrollmentEntity.getStatus());

        EnrollmentFidoRegistrationData data = request.getData();
        EnrollmentFidoRegistrationDataResponse response = data.getResponse();


        byte[] id = decode(data.getId());
        AttestationObject attestationObject = decodeAttestationObject(response.getAttestationObject());
        CollectedClientData clientData = decodeClientDataJson(response.getClientDataJSON());
        AttestedCredentialData attestedCredentialData = Optional.ofNullable(attestationObject.getAuthenticatorData().getAttestedCredentialData())
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "could not extract attestedCredentialData from attestation object"));

        if (!Arrays.equals(attestedCredentialData.getCredentialId(), id)) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "id must the be same as credentials ID");
        }

        var registrationOptions = enrollmentFidoRegistrationOptionsRepository.findByEnrollmentId(enrollmentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "could not find registration options associated with this enrollment ID"));

        String challenge = registrationOptions.getChallenge();

        if (!Arrays.equals(clientData.getChallenge().getValue(), challenge.getBytes())) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    String.format("%s: %s", EnrollmentsFidoRegistrationErrorCode.PUBLIC_KEY_INVALIDA, EnrollmentsFidoRegistrationErrorCode.PUBLIC_KEY_INVALIDA));
        }

        String host = clientData.getOrigin().getHost();
        if (Strings.isNullOrEmpty(host) || host.equals("INVALID_ORIGIN")) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    String.format("%s: %s", EnrollmentsFidoRegistrationErrorCode.ORIGEM_FIDO_INVALIDA, EnrollmentsFidoRegistrationErrorCode.ORIGEM_FIDO_INVALIDA));
        }

        COSEKey coseKey = attestedCredentialData.getCOSEKey();
        if (!(COSEKeyType.RSA.equals(coseKey.getKeyType()))) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    String.format("%s: unsupported public key", EnrollmentsFidoRegistrationErrorCode.PUBLIC_KEY_INVALIDA));
        }

        RSAPublicKey publicKey = Optional.ofNullable(coseKey.getPublicKey())
                .map(k -> (RSAPublicKey) k)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        String.format("%s: %s", EnrollmentsFidoRegistrationErrorCode.PUBLIC_KEY_INVALIDA, EnrollmentsFidoRegistrationErrorCode.PUBLIC_KEY_INVALIDA)));

        RSAKey jwk = new RSAKey.Builder(publicKey).keyID(new String(attestedCredentialData.getCredentialId())).build();

        if (fidoJwkRepository.findByKid(jwk.getKeyID()).isPresent()) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    String.format("%s: duplicated public key", EnrollmentsFidoRegistrationErrorCode.PUBLIC_KEY_INVALIDA));
        }

        fidoJwkRepository.save(new FidoJwkEntity(jwk, enrollmentEntity));
        LOG.info("Public Key saved");

        enrollmentEntity.setStatus(EnumEnrollmentStatus.AUTHORISED.toString());
        enrollmentRepository.update(enrollmentEntity);
        LOG.info("Enrollment status updated to AUTHORISED");
    }

    public ResponseEnrollment updateEnrollment(String enrollmentId, UpdateEnrollment body) {
        var enrollmentEntity = enrollmentRepository.findByEnrollmentId(enrollmentId).orElseThrow(() ->
                new HttpStatusException(HttpStatus.NOT_FOUND, "Requested enrollment not found"));
        var status = body.getData().getStatus().name();

        if (!status.equals(EnumEnrollmentStatus.AWAITING_ENROLLMENT.name()) && !status.equals(EnumEnrollmentStatus.REJECTED.name())) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Status not allowed");
        }

        if (!enrollmentEntity.getStatus().equals(EnumEnrollmentStatus.AWAITING_ACCOUNT_HOLDER_VALIDATION.name())) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Enrollment doesn't have the required status");
        }

        enrollmentEntity.setStatus(status);
        return enrollmentRepository.update(enrollmentEntity).getResponseDTO();
    }


    public void createFidoAuthorisation(String consentId, String enrollmentId, ConsentAuthorization request) {
        var consentEntity = getConsentEntity(consentId);
        validatePaymentConsentStatus(EnumConsentStatus.AWAITING_AUTHORISATION, consentEntity.getStatus());

        var enrollmentEntity = getEnrollmentEntity(enrollmentId);
        validateEnrollmentStatus(EnumEnrollmentStatus.AUTHORISED, enrollmentEntity.getStatus());

        var fidoAssertion = Optional.ofNullable(request.getData())
                .map(ConsentAuthorizationData::getFidoAssertion)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "Could not find fidoAssertion"));

        String keyId = Optional.ofNullable(fidoAssertion.getId())
                .map(FidoUtils::decode)
                .map(String::new)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "Could not find fidoAssertion key id"));

        RSAPublicKey publicKey = fidoJwkRepository.findByKidAndEnrollmentId(keyId, enrollmentId)
                .map(FidoJwkEntity::getRsaPublicKey)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, String.format("%s: could not find registered public key",
                        EnrollmentsConsentAuthorizationErrorCode.RISCO)));

        var fidoAssertionResponse = Optional.ofNullable(fidoAssertion.getResponse())
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "Could not find fidoAssertion response"));

        CollectedClientData clientDataJson = Optional.ofNullable(fidoAssertionResponse.getClientDataJSON())
                .map(FidoUtils::decodeClientDataJson)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "Could not find clientDataJson"));


        byte[] expectedChallenge = enrollmentFidoSignOptionsRepository.findByEnrollmentId(enrollmentId)
                .map(EnrollmentFidoSignOptionsEntity::getChallenge)
                .map(String::getBytes)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "Could not find Sign Options challenge"));


        byte[] challenge = clientDataJson.getChallenge().getValue();


        if (!Arrays.equals(expectedChallenge, challenge)) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, String.format("%s: invalid challenge",
                    EnrollmentsConsentAuthorizationErrorCode.RISCO));
        }

        validateSignature(publicKey, fidoAssertionResponse);

        consentEntity.setStatus(EnumConsentStatus.AUTHORISED.toString());
        consentEntity.setStatusUpdateDateTime(Date.from(Instant.now()));
        paymentConsentRepository.update(consentEntity);

        LOG.info("Updated payment consent entity {} to AUTHORISED", consentEntity.getPaymentConsentId());
    }

    private void validateSignature(RSAPublicKey publicKey, ConsentAuthorizationDataFidoAssertionResponse fidoAssertionResponse) {
        LOG.info("Validating FIDO Assertion Signature");

        byte[] signature = Optional.ofNullable(fidoAssertionResponse.getSignature())
                .map(FidoUtils::decode)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "Could not find signature"));

        byte[] decodedClientDataJsonBytes = Optional.ofNullable(fidoAssertionResponse.getClientDataJSON())
                .map(FidoUtils::decode)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "Could not find clientDataJson"));

        byte[] decodedAuthenticatorDataBytes = Optional.ofNullable(fidoAssertionResponse.getAuthenticatorData())
                .map(FidoUtils::decode)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "Could not find authenticator data"));


        boolean isSignatureValid = false;

        try {
            isSignatureValid = FidoUtils.validateSignature(publicKey, decodedClientDataJsonBytes, decodedAuthenticatorDataBytes, signature);
        } catch (GeneralSecurityException e) {
            LOG.warn("Could not verify signature due to " + e.getMessage(), e);
        }

        if (!isSignatureValid) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, String.format("%s: invalid signature",
                    EnrollmentsConsentAuthorizationErrorCode.RISCO));
        }
        LOG.info("Signature is valid");
    }

    public EnrollmentFidoSignOptions createFidoSignOptions(String enrollmentId, EnrollmentFidoSignOptionsInput body, Optional<String> certificateCn) {
        var enrollmentEntity = getEnrollmentEntity(enrollmentId);
        validateEnrollmentStatus(EnumEnrollmentStatus.AUTHORISED, enrollmentEntity.getStatus());
        var consentEntity = getConsentEntity(body.getData().getConsentId());
        validatePaymentConsentStatus(EnumConsentStatus.AWAITING_AUTHORISATION, consentEntity.getStatus());

        certificateCn.ifPresentOrElse(c -> {
            if (!body.getData().getRp().equals(c)) {
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, RP_INVALIDA_MESSAGE);
            }
        }, () -> {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, RP_INVALIDA_MESSAGE);
        });

        return enrollmentFidoSignOptionsRepository.save(EnrollmentFidoSignOptionsEntity.from(enrollmentEntity, body)).getDTO();
    }


    private PaymentConsentEntity getConsentEntity(String consentId) {
        return paymentConsentRepository.findByPaymentConsentId(consentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Consent not found"));

    }

    private void validatePaymentConsentStatus(EnumConsentStatus expected, String actual) {
        if (!actual.equals(expected.name())) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "STATUS_CONSENTIMENTO_INVALIDO: Consentimento inválido");
        }
    }
}
