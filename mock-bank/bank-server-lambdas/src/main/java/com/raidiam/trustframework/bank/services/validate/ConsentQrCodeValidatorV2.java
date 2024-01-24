package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsent;
import com.raidiam.trustframework.mockbank.models.generated.EnumLocalInstrument;

public class ConsentQrCodeValidatorV2 extends QrCodeValidationAbstract implements PaymentConsentValidator {
    @Override
    public void validate(CreatePaymentConsent request) {
        EnumLocalInstrument localInstrument = request.getData().getPayment().getDetails().getLocalInstrument();
        String qrCode = request.getData().getPayment().getDetails().getQrCode();
        String proxy = request.getData().getPayment().getDetails().getProxy();

        validateData(localInstrument, qrCode, proxy);
    }
}
