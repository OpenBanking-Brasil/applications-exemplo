package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.domain.PaymentConsentEntity;

public interface PixPaymentValidator {

    void validate(PaymentConsentEntity request);


}
