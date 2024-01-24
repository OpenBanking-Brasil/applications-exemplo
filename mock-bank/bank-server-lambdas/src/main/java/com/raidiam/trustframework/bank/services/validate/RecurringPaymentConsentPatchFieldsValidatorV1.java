package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.domain.PaymentConsentEntity;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class RecurringPaymentConsentPatchFieldsValidatorV1 implements RecurringPaymentConsentPatchValidatorV1 {
    private static final Logger LOG = LoggerFactory.getLogger(RecurringPaymentConsentPatchFieldsValidatorV1.class);

    @Override
    public void validate(PatchRecurringConsentV1 request, PaymentConsentEntity consent) {
        LOG.info("Validating patch recurring payment consent body");
        var data = request.getData();
        validateStatusChange(data, consent);
        validateDataChange(data, consent);

        LOG.info("Validation completed");
    }

    private void validateDataChange(PatchRecurringConsentV1Data data, PaymentConsentEntity consent) {
        LOG.info("Validating data change");

        PatchRecurringConsentV1DataAutomatic automatic = data.getAutomatic();
        List<PatchRecurringConsentV1DataCreditors> creditors = data.getCreditors();
        OffsetDateTime expirationDateTime = data.getExpirationDateTime();
        RiskSignalsConsents riskSignals = data.getRiskSignals();
        OffsetDateTime startDateTime = data.getStartDateTime();

        long dataChangeCount = Stream.of(automatic, creditors, expirationDateTime, riskSignals, startDateTime)
                .filter(Objects::nonNull)
                .count();

        if(dataChangeCount == 0){
            LOG.info("No data change was requested, skipping validation");
            return;
        }

        LOG.info("Found {} data change requests", dataChangeCount);

        if (consent.getAutomaticRecurringConfiguration() == null) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    String.format("%s: Data change is only permitted for Automatic Recurring Configuration",
                    ResponseErrorCreateRecurringConsentPatchV1Errors.CodeEnum.CAMPO_NAO_PERMITIDO));
        }

    }

    private void validateStatusChange(PatchRecurringConsentV1Data data, PaymentConsentEntity consent) {
        LOG.info("Validating status change");
        Optional.ofNullable(data.getStatus()).ifPresentOrElse(status -> {
                    LOG.info("Status is present");
                    switch (status) {
                        case REJECTED:
                            validateRejected(data, consent);
                            break;
                        case REVOKED:
                            validateRevoked(data, consent);
                            break;
                    }
                },
                () -> LOG.info("Status is absent, validation will be skipped"));
    }

    private void validateRejected(PatchRecurringConsentV1Data data, PaymentConsentEntity consent) {
        LOG.info("Status is REJECTED, validating REJECTED case");

        if (data.getRejection() == null) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "rejection object is mandatory when status is REJECTED");
        }

        if (data.getRevocation() != null) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "revocation object shall not be present when status is REJECTED, " +
                    "rejection and revocation objects are mutually exclusive");
        }

        if (consent.getStatus().equals(EnumAuthorisationStatusType.AUTHORISED.toString())) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, String.format("%s: Authorised consent cannot be rejected",
                    ResponseErrorCreateRecurringConsentPatchV1Errors.CodeEnum.CONSENTIMENTO_NAO_PERMITE_CANCELAMENTO));
        }
    }

    private void validateRevoked(PatchRecurringConsentV1Data data, PaymentConsentEntity consent) {
        LOG.info("Status is REVOKED, validating REVOKED case");
        if (data.getRevocation() == null) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "revocation object is mandatory when status is REVOKED");
        }

        if (data.getRejection() != null) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "rejection object shall not be present when status is REVOKED, " +
                    "rejection and revocation objects are mutually exclusive");
        }

        if (!consent.getStatus().equals(EnumAuthorisationStatusType.AUTHORISED.toString())) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, String.format("%s: Only Authorised consent can be revoked",
                    ResponseErrorCreateRecurringConsentPatchV1Errors.CodeEnum.CONSENTIMENTO_NAO_PERMITE_CANCELAMENTO));
        }
    }

}
