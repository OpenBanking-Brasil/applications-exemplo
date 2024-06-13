package com.raidiam.trustframework.bank.services.validate.payment.consent.rejection;

import com.raidiam.trustframework.bank.exceptions.ConsentRejectionException;
import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsentV4;

public interface PaymentConsentRejectionValidatorV4 {

    void validate(CreatePaymentConsentV4 request) throws ConsentRejectionException;
}
