package com.raidiam.trustframework.bank.utils;

import com.emv.qrcode.core.model.mpm.TagLengthString;
import com.emv.qrcode.decoder.mpm.DecoderMpm;
import com.emv.qrcode.model.mpm.MerchantAccountInformationReservedAdditional;
import com.emv.qrcode.model.mpm.MerchantPresentedMode;
import com.emv.qrcode.model.mpm.constants.MerchantPresentedModeCodes;
import com.raidiam.trustframework.bank.services.validate.ConsentQrCodeValidationErrors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Currency;

public class QrCodeValidationUtils  {

    private static final Logger LOG = LoggerFactory.getLogger(QrCodeValidationUtils.class);
    private static final String PIX_KEY = "01";
    private final ConsentQrCodeValidationErrors validationErrors;

    public QrCodeValidationUtils(ConsentQrCodeValidationErrors validationErrors) {
        this.validationErrors = validationErrors;
    }

    public void isMissing(String qrCode) {
        LOG.info("Check if QrCode is empty");
        if (qrCode == null || qrCode.isEmpty()) {
            throw validationErrors.getQrCodeIsMissingError();
        }
    }

    public MerchantPresentedMode decodeQrCode(String qrCode) {
        LOG.info("Started QrCode Consent Validation");

        try {
            return DecoderMpm.decode(qrCode, MerchantPresentedMode.class);
        } catch (RuntimeException e) {
            throw validationErrors.getCouldNotDecodeQrCodeError(qrCode);
        }
    }
    public void validateCreditorName(String creditorName, MerchantPresentedMode merchantPresentedMode) {
        String qrCodeCreditorName = extractTagValueOrThrow(merchantPresentedMode.getMerchantName(), "CreditorName");
        LOG.info("Validating creditor name - {} with qrCodeCreditorName - {}", creditorName, qrCodeCreditorName);

        if (!qrCodeCreditorName.equalsIgnoreCase(creditorName)) {
            throw validationErrors.getCreditorNameDiffError(qrCodeCreditorName, creditorName);
        }
    }

    public void validateProxy(String proxy, MerchantPresentedMode merchantPresentedMode) {
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

    public void validateCurrency(String currency, MerchantPresentedMode merchantPresentedMode) {
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

    public void validateAmount(String amount, MerchantPresentedMode merchantPresentedMode) {
        amount = String.format("%.2f", Float.parseFloat(amount));
        String qrCodeTransactionAmount = String.format("%.2f", Float.parseFloat(extractTagValueOrThrow(merchantPresentedMode.getTransactionAmount(), "amount")));
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
