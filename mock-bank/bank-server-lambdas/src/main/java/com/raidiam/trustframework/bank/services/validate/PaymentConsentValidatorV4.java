package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsentV4;

public interface PaymentConsentValidatorV4 {

    void validate(CreatePaymentConsentV4 request);

}
