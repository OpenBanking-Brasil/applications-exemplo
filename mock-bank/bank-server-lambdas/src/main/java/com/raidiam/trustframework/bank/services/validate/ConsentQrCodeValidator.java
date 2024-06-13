package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.domain.CreditorEntity;
import com.raidiam.trustframework.bank.domain.PaymentConsentEntity;
import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsent;
import com.raidiam.trustframework.mockbank.models.generated.EnumLocalInstrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsentQrCodeValidator extends QrCodeValidationAbstract implements PaymentConsentValidator, PixPaymentValidator {

    private static final Logger LOG = LoggerFactory.getLogger(ConsentQrCodeValidator.class);

    public ConsentQrCodeValidator(ConsentQrCodeValidationErrors validationErrors) {
        super(validationErrors);
    }

    @Override
    public void validate(CreatePaymentConsent request) {
        validateQrCodeData(request);
    }
    @Override
    public void validate(PaymentConsentEntity data) {
        EnumLocalInstrument localInstrument = EnumLocalInstrument.valueOf(data.getPaymentConsentPaymentEntity().getPaymentConsentDetails().getLocalInstrument());
        //Validate amount only apply for QRES
        if (localInstrument == EnumLocalInstrument.QRES || localInstrument == EnumLocalInstrument.QRDN) {
            qrCodeValidationUtils.isMissing(data.getPaymentConsentPaymentEntity().getPaymentConsentDetails().getQrCode());
            merchantPresentedMode = qrCodeValidationUtils.decodeQrCode(data.getPaymentConsentPaymentEntity().getPaymentConsentDetails().getQrCode());

            String proxy = data.getPaymentConsentPaymentEntity().getPaymentConsentDetails().getProxy();
            String amount = data.getPaymentConsentPaymentEntity().getAmount();
            String currency = data.getPaymentConsentPaymentEntity().getCurrency();
            String creditorName = data.getCreditorEntities().stream()
                    .findFirst()
                    .map(CreditorEntity::getName)
                    .orElse(null);

            validateData(proxy, amount, currency, creditorName);
        } else {
            LOG.info("Consent QrCode Validation skipped since it is only applicable for either QRES or QRDN local instruments");
        }
    }
}
