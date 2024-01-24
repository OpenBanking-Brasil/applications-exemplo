package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.mockbank.models.generated.CreateRecurringConsentV1;

public interface RecurringPaymentConsentValidatorV1 {

    void validate(CreateRecurringConsentV1 request);

}
