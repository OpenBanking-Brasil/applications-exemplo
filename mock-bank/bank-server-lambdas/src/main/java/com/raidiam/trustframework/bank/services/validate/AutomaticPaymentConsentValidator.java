package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.domain.PaymentConsentEntity;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.CreateRecurringPixPaymentV1Data;
import com.raidiam.trustframework.mockbank.models.generated.EnumAuthorisationStatusType;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

import java.time.Instant;
import java.util.Date;

public class AutomaticPaymentConsentValidator implements AutomaticPixPaymentValidator {

    @Override
    public void validate(CreateRecurringPixPaymentV1Data data, PaymentConsentEntity paymentConsentEntity) {

        assertSame(paymentConsentEntity.getPaymentConsentId(), data.getRecurringConsentId(), paymentConsentEntity.getPaymentConsentId(),
                String.format("PAGAMENTO_DIVERGENTE_CONSENTIMENTO: O valor informado no consentimento não é o mesmo valor do informado no payload de pagamento. consentId consentimento - %s, consentId pagamento- %s",
                        paymentConsentEntity.getPaymentConsentId(), data.getRecurringConsentId()));

        if (paymentConsentEntity.getStatus().equals(EnumAuthorisationStatusType.REVOKED.name())) {
            throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "O consentimento informado encontra-se revogado");
        }

        if (paymentConsentEntity.getExpirationDateTime().before(Date.from(Instant.now()))) {
            throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "O consentimento informado encontra-se expirado");
        }

        if (data.getDate().isBefore(BankLambdaUtils.dateToLocalDate(paymentConsentEntity.getStartDateTime()))) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "PAGAMENTO_DIVERGENTE_CONSENTIMENTO: O pagamento encontra-se antes da data de inicio do consentimento");
        }
       
    }
}
