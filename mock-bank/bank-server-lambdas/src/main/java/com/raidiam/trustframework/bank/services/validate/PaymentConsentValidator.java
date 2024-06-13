package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsent;

public interface PaymentConsentValidator {

    void validate(CreatePaymentConsent request);


}
