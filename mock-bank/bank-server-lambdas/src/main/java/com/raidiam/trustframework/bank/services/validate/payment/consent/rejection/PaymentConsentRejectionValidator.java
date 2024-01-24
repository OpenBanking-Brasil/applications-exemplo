package com.raidiam.trustframework.bank.services.validate.payment.consent.rejection;

import com.raidiam.trustframework.bank.exceptions.ConsentRejectionException;
import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsent;

public interface PaymentConsentRejectionValidator {

    void validate(CreatePaymentConsent request) throws ConsentRejectionException;
}
