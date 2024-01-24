package com.raidiam.trustframework.bank.services.validate.payment.consent.rejection.debtoramount;

import com.raidiam.trustframework.bank.exceptions.ConsentRejectionException;

public interface PaymentConsentDebtorAmountRejectionValidatorExceptions {
    ConsentRejectionException getInsufficientAmountException();
}
