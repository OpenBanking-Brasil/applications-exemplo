package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsent;

public class ConsentQrCodeValidatorV2 extends QrCodeValidationAbstract implements PaymentConsentValidator {
    public ConsentQrCodeValidatorV2(ConsentQrCodeValidationErrors validationErrors) {
        super(validationErrors);
    }

    @Override
    public void validate(CreatePaymentConsent request) {
        validateQrCodeData(request);
    }
}
