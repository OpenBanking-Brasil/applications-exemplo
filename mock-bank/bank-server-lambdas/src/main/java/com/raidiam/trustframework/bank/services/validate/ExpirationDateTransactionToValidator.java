package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.exceptions.TrustframeworkException;
import com.raidiam.trustframework.mockbank.models.generated.CreateConsent;

import java.time.OffsetDateTime;

public class ExpirationDateTransactionToValidator implements ConsentValidator {
    @Override
    public void validate(CreateConsent request) {
        OffsetDateTime transactionToTime = request.getData().getTransactionToDateTime();
        if(transactionToTime == null) {
            return;
        }
        OffsetDateTime expirationDateTime = request.getData().getExpirationDateTime();
        if(expirationDateTime.isEqual(transactionToTime)) {
            return;
        }
        if(!expirationDateTime.isAfter(transactionToTime)) {
            throw new TrustframeworkException("Expiration time should be on or after transactionTo time");
        }

    }
}
