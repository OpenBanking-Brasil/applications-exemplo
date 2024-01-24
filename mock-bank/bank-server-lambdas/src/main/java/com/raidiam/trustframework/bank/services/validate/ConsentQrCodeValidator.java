package com.raidiam.trustframework.bank.services.validate;

import com.emv.qrcode.core.model.mpm.TagLengthString;
import com.emv.qrcode.decoder.mpm.DecoderMpm;
import com.emv.qrcode.model.mpm.MerchantAccountInformationReservedAdditional;
import com.emv.qrcode.model.mpm.MerchantPresentedMode;
import com.emv.qrcode.model.mpm.constants.MerchantPresentedModeCodes;
import com.raidiam.trustframework.bank.domain.CreditorEntity;
import com.raidiam.trustframework.bank.domain.PaymentConsentEntity;
import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsent;
import com.raidiam.trustframework.mockbank.models.generated.EnumLocalInstrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Currency;

public class ConsentQrCodeValidator implements PaymentConsentValidator, PixPaymentValidator {

    private static final Logger LOG = LoggerFactory.getLogger(ConsentQrCodeValidator.class);
    private static final String PIX_KEY = "01";


    private final ConsentQrCodeValidationErrors validationErrors;

    public ConsentQrCodeValidator(ConsentQrCodeValidationErrors validationErrors) {
        this.validationErrors = validationErrors;
    }
    @Override
    public void validate(CreatePaymentConsent request) {

        EnumLocalInstrument localInstrument = request.getData().getPayment().getDetails().getLocalInstrument();
        if (localInstrument == EnumLocalInstrument.QRES || localInstrument == EnumLocalInstrument.QRDN) {
            MerchantPresentedMode merchantPresentedMode = decodeQrCode(request.getData().getPayment().getDetails().getQrCode());

            validateAmount(request.getData().getPayment().getAmount(), merchantPresentedMode);
            validateCurrency(request.getData().getPayment().getCurrency(), merchantPresentedMode);
            validateProxy(request.getData().getPayment().getDetails().getProxy(), merchantPresentedMode);
            validateCreditorName(request.getData().getCreditor().getName(), merchantPresentedMode);

            LOG.info("QrCode Consent Validation is Finished");

        } else {
            LOG.info("Consent QrCode Validation skipped since it is only applicable for either QRES or QRDN local instruments");
        }
    }
    @Override
    public void validate(PaymentConsentEntity data) {

        EnumLocalInstrument localInstrument = EnumLocalInstrument.valueOf(data.getPaymentConsentPaymentEntity().getPaymentConsentDetails().getLocalInstrument());
        if (localInstrument == EnumLocalInstrument.QRES || localInstrument == EnumLocalInstrument.QRDN) {
            MerchantPresentedMode merchantPresentedMode = decodeQrCode(data.getPaymentConsentPaymentEntity().getPaymentConsentDetails().getQrCode());
            //Validate amount only apply for QRES
            if(localInstrument == EnumLocalInstrument.QRES) {
                validateAmount(data.getPaymentConsentPaymentEntity().getAmount(), merchantPresentedMode);
            }
            validateCurrency(data.getPaymentConsentPaymentEntity().getCurrency(), merchantPresentedMode);
            validateProxy(data.getPaymentConsentPaymentEntity().getPaymentConsentDetails().getProxy(), merchantPresentedMode);
            String creditorName = data.getCreditorEntities().stream()
                    .findFirst()
                    .map(CreditorEntity::getName)
                    .orElse(null);
            validateCreditorName(creditorName, merchantPresentedMode);

            LOG.info("QrCode Consent Validation is Finished");

        } else {
            LOG.info("Consent QrCode Validation skipped since it is only applicable for either QRES or QRDN local instruments");
        }
    }

    private MerchantPresentedMode decodeQrCode(String qrCode) {
        LOG.info("Started QrCode Consent Validation");

        try {
            return DecoderMpm.decode(qrCode, MerchantPresentedMode.class);
        } catch (RuntimeException e) {
            throw validationErrors.getCouldNotDecodeQrCodeError(qrCode);
        }
    }
    private void validateCreditorName(String creditorName, MerchantPresentedMode merchantPresentedMode) {
        String qrCodeCreditorName = extractTagValueOrThrow(merchantPresentedMode.getMerchantName(), "CreditorName");
        LOG.info("Validating creditor name - {} with qrCodeCreditorName - {}", creditorName, qrCodeCreditorName);

        if (!qrCodeCreditorName.equalsIgnoreCase(creditorName)) {
            throw validationErrors.getCreditorNameDiffError(qrCodeCreditorName, creditorName);
        }
    }

    private void validateProxy(String proxy, MerchantPresentedMode merchantPresentedMode) {
        var merchantAccountInformation = merchantPresentedMode.getMerchantAccountInformation();

        if (merchantAccountInformation == null) {
            throw validationErrors.getMerchantAccountInformationIsMissingError();
        }

        var merchantAccountInformationTemplate = merchantAccountInformation
                .get(MerchantPresentedModeCodes.ID_MERCHANT_ACCOUNT_INFORMATION_RESERVED_ADDITIONAL_RANGE_START);

        if (merchantAccountInformationTemplate == null) {
            throw validationErrors.getProxyIsMissingError();
        }

        var info = (MerchantAccountInformationReservedAdditional) merchantAccountInformationTemplate.getValue();

        String qrCodeProxy = extractTagValueOrThrow(info.getPaymentNetworkSpecific().get(PIX_KEY), "proxy");
        LOG.info("Validating proxy - {} with qrCode proxy - {}", proxy, qrCodeProxy);

        if (!proxy.equalsIgnoreCase(qrCodeProxy)) {
            throw validationErrors.getProxyIsDifferentError(qrCodeProxy, proxy);
        }
    }

    private void validateCurrency(String currency, MerchantPresentedMode merchantPresentedMode) {
        try {
            String currencyCode = Currency.getInstance(currency).getNumericCodeAsString();
            String qrCodeCurrencyCode = extractTagValueOrThrow(merchantPresentedMode.getTransactionCurrency(), "currency");
            LOG.info("Validating currency code - {} with qrCode currency code - {}", currencyCode, qrCodeCurrencyCode);
            if (!currencyCode.equals(qrCodeCurrencyCode)) {
                throw validationErrors.getCurrencyCodeIsDifferentError(qrCodeCurrencyCode, currencyCode);
            }

        } catch (IllegalArgumentException e) {
            throw validationErrors.getCurrencyIsInvalidError(currency);
        }
    }

    private void validateAmount(String amount, MerchantPresentedMode merchantPresentedMode) {
        String qrCodeTransactionAmount = extractTagValueOrThrow(merchantPresentedMode.getTransactionAmount(), "amount");
        LOG.info("Validating amount - {} with qrCode amount - {}", amount, qrCodeTransactionAmount);

        if (!amount.equals(qrCodeTransactionAmount)) {
            throw validationErrors.getAmountIsDifferentError(qrCodeTransactionAmount, amount);
        }
    }

    private String extractTagValueOrThrow(TagLengthString tag, String tagName) {
        if (tag != null) {
            return tag.getValue();
        }
        throw validationErrors.getRequiredQrCodeTagIsMissingError(tagName);
    }
}
