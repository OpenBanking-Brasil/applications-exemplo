package com.raidiam.trustframework.bank.services.validate.payment.consent.rejection.debtoramount;

import com.raidiam.trustframework.bank.exceptions.ConsentRejectionException;
import com.raidiam.trustframework.mockbank.models.generated.RejectionReasonV2;

public class PaymentConsentDebtorAmountRejectionValidatorExceptionsV3 implements PaymentConsentDebtorAmountRejectionValidatorExceptions {
    @Override
    public ConsentRejectionException getInsufficientAmountException() {
        return new ConsentRejectionException(
                RejectionReasonV2.CodeEnum.SALDO_INSUFICIENTE.name(),
                "Debtor has insufficient available amount to process the payment"
        );
    }
}
