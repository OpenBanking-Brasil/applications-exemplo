package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.mockbank.models.generated.PatchPaymentsConsent;

public interface PaymentPatchValidator {

    void validate(PatchPaymentsConsent request);
}
