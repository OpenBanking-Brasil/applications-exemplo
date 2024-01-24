package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.domain.PaymentConsentEntity;
import com.raidiam.trustframework.mockbank.models.generated.CreateRecurringPixPaymentV1Data;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

public interface AutomaticPixPaymentValidator {

    void validate(CreateRecurringPixPaymentV1Data request, PaymentConsentEntity paymentConsentEntity);

    default void assertSame(Object inbound, Object current, String consentId, String message) {
        if (!inbound.equals(current)) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, message);
        }
    }
}
