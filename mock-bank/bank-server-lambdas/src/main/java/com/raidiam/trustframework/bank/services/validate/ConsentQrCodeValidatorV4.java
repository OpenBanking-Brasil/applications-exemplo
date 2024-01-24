package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsentV4;
import com.raidiam.trustframework.mockbank.models.generated.EnumLocalInstrument;

public class ConsentQrCodeValidatorV4 extends QrCodeValidationAbstract implements PaymentConsentValidatorV4 {

    @Override
    public void validate(CreatePaymentConsentV4 request) {
        EnumLocalInstrument localInstrument = request.getData().getPayment().getDetails().getLocalInstrument();
        String qrCode = request.getData().getPayment().getDetails().getQrCode();
        String proxy = request.getData().getPayment().getDetails().getProxy();

        validateData(localInstrument, qrCode, proxy);
    }
}
