package com.raidiam.trustframework.bank.services.validate;

import io.micronaut.http.exceptions.HttpStatusException;

public interface ConsentQrCodeValidationErrors {

    HttpStatusException getCouldNotDecodeQrCodeError(String qrCode);

    HttpStatusException getCreditorNameDiffError(String qrCodeCreditorName, String creditorName);

    HttpStatusException getMerchantAccountInformationIsMissingError();

    HttpStatusException getProxyIsMissingError();

    HttpStatusException getProxyIsDifferentError(String qrCodeProxy, String proxy);

    HttpStatusException getCurrencyCodeIsDifferentError(String qrCodeCurrencyCode, String currencyCode);

    HttpStatusException getCurrencyIsInvalidError(String currency);

    HttpStatusException getAmountIsDifferentError(String qrCodeTransactionAmount, String amount);

    HttpStatusException getRequiredQrCodeTagIsMissingError(String tagName);
}
