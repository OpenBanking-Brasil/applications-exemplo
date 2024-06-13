package com.raidiam.trustframework.bank.services.validate;

import com.emv.qrcode.model.mpm.MerchantPresentedMode;
import com.raidiam.trustframework.bank.utils.QrCodeValidationUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class QrCodeValidationAbstract {

    protected final QrCodeValidationUtils qrCodeValidationUtils;
    protected MerchantPresentedMode merchantPresentedMode;
    private static final Logger LOG = LoggerFactory.getLogger(QrCodeValidationAbstract.class);


    protected QrCodeValidationAbstract(ConsentQrCodeValidationErrors validationErrors) {
        this.qrCodeValidationUtils = new QrCodeValidationUtils(validationErrors);
    }

    protected void validateQrCodeData(CreatePaymentConsent request) {
        Details details = request.getData().getPayment().getDetails();
        PaymentConsent paymentConsent = request.getData().getPayment();
        String creditorName = request.getData().getCreditor().getName();
        getAndValidateQrCode(details, paymentConsent.getAmount(), paymentConsent.getCurrency(), creditorName);
    }

    protected void validateQrCodeData(CreatePaymentConsentV4 request) {
        Details details = request.getData().getPayment().getDetails();
        PaymentConsentV4Payment paymentConsent = request.getData().getPayment();
        String creditorName = request.getData().getCreditor().getName();
        getAndValidateQrCode(details, paymentConsent.getAmount(), paymentConsent.getCurrency(), creditorName);
    }

    private void getAndValidateQrCode(Details details, String amount, String currency, String creditorName) {
        LOG.info("Started QR Code Validation");
        EnumLocalInstrument localInstrument = details.getLocalInstrument();
        String qrCode = details.getQrCode();
        String proxy = details.getProxy();
        if (localInstrument == EnumLocalInstrument.QRES || localInstrument == EnumLocalInstrument.QRDN) {
            qrCodeValidationUtils.isMissing(qrCode);
            merchantPresentedMode = qrCodeValidationUtils.decodeQrCode(qrCode);
            validateData(proxy, amount, currency, creditorName);
        } else if (localInstrument.equals(EnumLocalInstrument.MANU)
                && (proxy != null
                || qrCode != null ) ||
                (localInstrument.equals(EnumLocalInstrument.DICT) &&
                        qrCode != null)) {
            String message = "DETALHE_PAGAMENTO_INVALIDO: Parâmetro não informado.";
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, message);
        } else {
            LOG.info("Consent QR Code Validation skipped since it is only applicable for either QRES or QRDN local instruments");
        }
    }

    protected void validateData(String proxy, String amount, String currency, String creditorName) {
            LOG.info("Validating QR code values against values stored in payment consent");
            qrCodeValidationUtils.validateProxy(proxy, merchantPresentedMode);
            qrCodeValidationUtils.validateAmount(amount, merchantPresentedMode);
            qrCodeValidationUtils.validateCurrency(currency, merchantPresentedMode);
            qrCodeValidationUtils.validateCreditorName(creditorName, merchantPresentedMode);

        LOG.info("QrCode Consent Validation is Finished");
    }
}
