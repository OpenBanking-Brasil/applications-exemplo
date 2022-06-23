package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.exceptions.TrustframeworkException;
import com.raidiam.trustframework.mockbank.models.generated.CreateConsent;

import java.time.OffsetDateTime;

public class TransactionToAndFromValidator implements ConsentValidator {
    @Override
    public void validate(CreateConsent request) {
        OffsetDateTime transactionToDate = request.getData().getTransactionToDateTime();
        if(transactionToDate == null) {
            return;
        }
        OffsetDateTime transactionFromDate = request.getData().getTransactionFromDateTime();
        if(transactionFromDate == null) {
            return;
        }
        if(!transactionToDate.isAfter(transactionFromDate)) {
            throw new TrustframeworkException("Transaction to date should be after transaction from date");
        }

    }
}