package com.raidiam.trustframework.bank.services.validate.payment.consent.rejection.creditordebtor;

import com.raidiam.trustframework.bank.exceptions.ConsentRejectionException;
import com.raidiam.trustframework.mockbank.models.generated.EnumConsentRejectionReasonType;

public class PaymentConsentCreditorDebtorRejectionValidatorExceptionsV3 implements PaymentConsentCreditorDebtorRejectionValidatorExceptions {
    @Override
    public ConsentRejectionException getSameCreditorDebtorException() {
        return new ConsentRejectionException(
                EnumConsentRejectionReasonType.CONTAS_ORIGEM_DESTINO_IGUAIS.name(),
                "A conta selecionada é igual à conta destino e não permite realizar esse pagamento."
        );
    }
}
