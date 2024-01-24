package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.domain.PaymentConsentEntity;
import com.raidiam.trustframework.mockbank.models.generated.PatchRecurringConsentV1;

public interface RecurringPaymentConsentPatchValidatorV1 {

    void validate(PatchRecurringConsentV1 request, PaymentConsentEntity consent);

}
