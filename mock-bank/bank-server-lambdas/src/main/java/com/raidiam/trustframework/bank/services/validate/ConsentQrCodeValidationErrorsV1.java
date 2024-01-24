package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.services.message.PaymentErrorMessage;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

public class ConsentQrCodeValidationErrorsV1 implements ConsentQrCodeValidationErrors {
    private final PaymentErrorMessage errorMessage;

    public ConsentQrCodeValidationErrorsV1(PaymentErrorMessage errorMessage) {
        this.errorMessage = errorMessage;
    }


    @Override
    public HttpStatusException getCouldNotDecodeQrCodeError(String qrCode) {
        String message = errorMessage.getMessagePaymentDetailInvalid(String.format("Could not decode QrCode - %s", qrCode));
        return new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

    @Override
    public HttpStatusException getCreditorNameDiffError(String qrCodeCreditorName, String creditorName) {
        String message = errorMessage.getMessagePaymentDetailInvalid(String.format("Creditor name defined in QrCode - %s differs from the Creditor name specified in the Consent - %s",
                qrCodeCreditorName, creditorName));
        return new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

    @Override
    public HttpStatusException getMerchantAccountInformationIsMissingError() {
        return new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                errorMessage.getMessagePaymentDetailInvalid("Merchant Account Information is missing in the qrCode"));
    }

    @Override
    public HttpStatusException getProxyIsMissingError() {
        return new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, errorMessage.getMessagePaymentDetailInvalid("required QrCode value - proxy is not present"));
    }

    @Override
    public HttpStatusException getProxyIsDifferentError(String qrCodeProxy, String proxy) {
        String message = errorMessage.getMessagePaymentDetailInvalid(String.format("Proxy defined in QrCode - %s differs from the proxy specified in the Consent - %s",
                qrCodeProxy, proxy));
        return new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

    @Override
    public HttpStatusException getCurrencyCodeIsDifferentError(String qrCodeCurrencyCode, String currencyCode) {
        String message = errorMessage.getMessagePaymentDetailInvalid(String.format("Currency code defined in QrCode - %s differs from the currency code specified in the Consent - %s",
                qrCodeCurrencyCode, currencyCode));
        return new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

    @Override
    public HttpStatusException getCurrencyIsInvalidError(String currency) {
        String message = errorMessage.getMessagePaymentDetailInvalid(String.format("The currency provided in the Consent - %s is invalid", currency));
        return new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

    @Override
    public HttpStatusException getAmountIsDifferentError(String qrCodeTransactionAmount, String amount) {
        String message = errorMessage.getMessagePaymentDetailInvalid(String.format("Amount defined in QrCode - %s differs from the amount specified in the Consent - %s",
                qrCodeTransactionAmount, amount));
        return new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

    @Override
    public HttpStatusException getRequiredQrCodeTagIsMissingError(String tagName) {
        String message = errorMessage.getMessagePaymentDetailInvalid(String.format("required QrCode value - %s is not present", tagName));
        return new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}
