package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.exceptions.TrustframeworkException;
import com.raidiam.trustframework.mockbank.models.generated.CreateConsent;

import java.time.OffsetDateTime;

public class ExpirationDateTransationFromValidator implements ConsentValidator {
    @Override
    public void validate(CreateConsent request) {
        OffsetDateTime transactionFromTime = request.getData().getTransactionFromDateTime();
        if(transactionFromTime == null) {
            return;
        }
        OffsetDateTime expirationDateTime = request.getData().getExpirationDateTime();
        if(!expirationDateTime.isAfter(transactionFromTime)) {
            throw new TrustframeworkException("Expiration time should be after transaction from time");
        }

    }
}