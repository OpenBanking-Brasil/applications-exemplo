package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.domain.PaymentConsentEntity;
import com.raidiam.trustframework.mockbank.models.generated.CreateRecurringPixPaymentV1Data;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

public class AutomaticPaymentLimitTransactionValidator implements AutomaticPixPaymentValidator {

    @Override
    public void validate(CreateRecurringPixPaymentV1Data data, PaymentConsentEntity paymentConsentEntity) {
        if (paymentConsentEntity.getPostSweepingRecurringConfiguration() != null && paymentConsentEntity.getPostSweepingRecurringConfiguration().getTransactionLimit() != null) {
            var sweepingTransactionLimit = Double.parseDouble(paymentConsentEntity.getPostSweepingRecurringConfiguration().getTransactionLimit());
            var paymentAmount = Double.parseDouble(data.getPayment().getAmount());
            if(paymentAmount > sweepingTransactionLimit) {
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "LIMITE_PERIODO_VALOR_EXCEDIDO: O limite do pagamento excede o limite definido no consentimento");
            }
        }

    }

}
