package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.exceptions.TrustframeworkException;
import com.raidiam.trustframework.mockbank.models.generated.CreateConsent;

import java.time.OffsetDateTime;

public class ExpirationDateValidator implements ConsentValidator {
    @Override
    public void validate(CreateConsent request) {
        OffsetDateTime expirationDateTime = request.getData().getExpirationDateTime();
        if(expirationDateTime == null) {
            throw new TrustframeworkException("Expiration time is mandatory");
        }
        OffsetDateTime earliestPossible = OffsetDateTime.now();
        if(!expirationDateTime.isAfter(earliestPossible)) {
            throw new TrustframeworkException("Expiration time must be in the future");
        }
        OffsetDateTime latestPossible = OffsetDateTime.now().plusYears(1L);
        if(expirationDateTime.isAfter(latestPossible)) {
            throw new TrustframeworkException("Expiration time must be within a year");
        }
    }
}
