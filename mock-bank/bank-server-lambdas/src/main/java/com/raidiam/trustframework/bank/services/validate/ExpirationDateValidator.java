package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.exceptions.TrustframeworkException;
import com.raidiam.trustframework.mockbank.models.generated.CreateConsent;
import com.raidiam.trustframework.mockbank.models.generated.CreateConsentV2;
import com.raidiam.trustframework.mockbank.models.generated.CreateConsentV3;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

import java.time.OffsetDateTime;

public class ExpirationDateValidator implements ConsentValidator, ConsentValidatorV2, ConsentValidatorV3 {

    @Override
    public void validate(CreateConsent request) {
        OffsetDateTime expirationDateTime = request.getData().getExpirationDateTime();
        if (expirationDateTime == null) {
            throw new TrustframeworkException("Expiration time is mandatory");
        }
        validateExpirationDateTime(expirationDateTime, HttpStatus.BAD_REQUEST);
    }

    @Override
    public void validate(CreateConsentV2 request) {
        validateExpirationDateTime(request.getData().getExpirationDateTime(), HttpStatus.BAD_REQUEST);
    }

    @Override
    public void validate(CreateConsentV3 request) {
        OffsetDateTime expirationDateTime = request.getData().getExpirationDateTime();
        if (expirationDateTime != null) {
            validateExpirationDateTime(request.getData().getExpirationDateTime(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private void validateExpirationDateTime(OffsetDateTime expirationDateTime, HttpStatus status) {
        OffsetDateTime earliestPossible = OffsetDateTime.now();
        if (!expirationDateTime.isAfter(earliestPossible)) {
            throw new HttpStatusException(status, "Expiration time must be in the future");
        }

        OffsetDateTime latestPossible = OffsetDateTime.now().plusYears(1L);
        if (expirationDateTime.isAfter(latestPossible)) {
            throw new HttpStatusException(status, "Expiration time must be within a year");
        }

    }
}
