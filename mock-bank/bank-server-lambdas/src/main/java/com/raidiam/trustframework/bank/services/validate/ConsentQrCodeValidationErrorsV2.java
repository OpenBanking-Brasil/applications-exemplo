package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.services.message.PaymentErrorMessage;
import com.raidiam.trustframework.bank.services.message.PaymentErrorMessageV2;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

public class ConsentQrCodeValidationErrorsV2 extends ConsentQrCodeValidationErrorsV1 {
    private final PaymentErrorMessageV2 errorMessage = new PaymentErrorMessageV2();

    public ConsentQrCodeValidationErrorsV2(PaymentErrorMessage errorMessage) {
        super(errorMessage);
    }

    @Override
    public HttpStatusException getAmountIsDifferentError(String qrCodeTransactionAmount, String amount) {
        String message = errorMessage.getMessageInvalidValue(String.format("Amount defined in QrCode - %s differs from the amount specified in the Consent - %s",
                qrCodeTransactionAmount, amount));
        return new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}
