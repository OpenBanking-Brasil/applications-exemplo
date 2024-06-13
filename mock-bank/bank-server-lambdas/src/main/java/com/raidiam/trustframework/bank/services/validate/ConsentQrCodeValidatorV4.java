package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsentV4;

public class ConsentQrCodeValidatorV4 extends QrCodeValidationAbstract implements PaymentConsentValidatorV4 {

    public ConsentQrCodeValidatorV4(ConsentQrCodeValidationErrors validationErrors) {
        super(validationErrors);
    }

    @Override
    public void validate(CreatePaymentConsentV4 request) {
        validateQrCodeData(request);
    }
}
