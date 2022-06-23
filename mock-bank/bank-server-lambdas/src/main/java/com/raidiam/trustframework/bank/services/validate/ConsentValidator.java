package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.mockbank.models.generated.CreateConsent;

public interface ConsentValidator {

    void validate(CreateConsent request);

}
