package com.raidiam.trustframework.bank.services.validate.payment.consent.rejection.creditordebtor;

import com.raidiam.trustframework.bank.exceptions.ConsentRejectionException;

public interface PaymentConsentCreditorDebtorRejectionValidatorExceptions {
    ConsentRejectionException getSameCreditorDebtorException();
}
