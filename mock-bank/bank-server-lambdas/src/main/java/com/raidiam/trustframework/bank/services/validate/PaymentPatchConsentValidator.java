package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class PaymentPatchConsentValidator implements PaymentPatchValidator{
    Logger log = LoggerFactory.getLogger(PaymentPatchConsentValidator.class);

    @Override
    public void validate(PatchPaymentsConsent request) {
        PatchPaymentsConsentData data = request.getData();
        EnumAuthorisationPatchStatusType status = data.getStatus();

        log.info("Starting Patch payment request validation");
        if (Arrays.stream(EnumAuthorisationPatchStatusType.values()).noneMatch(n -> n.equals(status))){
            log.info("Patch payment request has an invalid status: {}", status);
            throw new HttpStatusException(HttpStatus.BAD_REQUEST,
                    "Status sent is invalid"
            );
        }

        Revocation revocation = data.getRevocation();

        EnumRevokedBy revokedBy = revocation.getRevokedBy();
        if (Arrays.stream(EnumRevokedBy.values()).noneMatch(n -> n.equals(revokedBy))){
            log.info("Patch payment request has an invalid revokedBy: {}", revokedBy);
            throw new HttpStatusException(HttpStatus.BAD_REQUEST,
                    "revokedBy sent is invalid"
            );
        }
        log.info("Checking if logged user is sent");
        LoggedUser loggedUser = revocation.getLoggedUser();
        if (loggedUser == null && revokedBy.equals(EnumRevokedBy.USER)){
            log.info("Patch payment request is revoked by a user and should therefore send a logged user: {}", revokedBy);
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "INFORMACAO_USUARIO_REQUERIDA"
            );
        }

        Reason reason = revocation.getReason();
            validateReason(reason, revokedBy);
    }


    public void validateReason(Reason reason, EnumRevokedBy revokedBy){
        EnumRevocationReason revocationReason = reason.getCode();
        if (Arrays.stream(EnumRevocationReason.values()).noneMatch(n -> n.equals(revocationReason))){
            log.info("Patch payment request has an invalid revocation reason: {}", revocationReason);
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "revocation reason sent is invalid"
            );
        }

        if (revocationReason.equals(EnumRevocationReason.FRAUD) || revocationReason.equals(EnumRevocationReason.ACCOUNT_CLOSURE)){
            checkRevocationReasonAllowed(revokedBy);
        }

        String reasonAdditionalInformation = reason.getAdditionalInformation();
        if (checkAdditionalInformationRequired(revocationReason,revokedBy) && reasonAdditionalInformation == null){
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "INFORMACAO_ADICIONAL_REVOGACAO_REQUERIDA"
            );
        }
    }

    public boolean checkAdditionalInformationRequired(EnumRevocationReason revocationReason, EnumRevokedBy revokedBy){
        EnumRevokedBy[] revokedByList = new EnumRevokedBy[]{EnumRevokedBy.TPP, EnumRevokedBy.ASPSP};
        return Arrays.asList(revokedByList).contains(revokedBy) && revocationReason.equals(EnumRevocationReason.OTHER);
    }

    public void checkRevocationReasonAllowed(EnumRevokedBy revokedBy){
        EnumRevokedBy[] revokedByList = new EnumRevokedBy[]{EnumRevokedBy.TPP, EnumRevokedBy.ASPSP};
        if (!Arrays.asList(revokedByList).contains(revokedBy)) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "MOTIVO_REVOGACAO_NAO_PERMITIDO"
            );
        }
    }
}
