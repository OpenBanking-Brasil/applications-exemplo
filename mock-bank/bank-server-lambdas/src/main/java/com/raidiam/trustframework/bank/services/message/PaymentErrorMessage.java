package com.raidiam.trustframework.bank.services.message;

public interface PaymentErrorMessage {
    String getMessageConsentConsumed();
    String getMessageTransactionIdentifier();
    String getMessagePaymentDivergent(String detail);
    String getMessageInvalidConsent(String detail);
    String getMessageValueNotCompatible(String detail);
    String getMessageNotInformed();
    String getMessageVersionDiff();
    String getMessagePaymentDetailInvalid(String detail);
    String getMessageInvalidParameter();
    String getMessageInvalidParameter(String message);
    String getMessageInvalidCurrency();
    String getMessageInvalidDate(String detail);

    String getMessageInvalidValue(String detail);

    String getParameterNotInformed(String message);
}
