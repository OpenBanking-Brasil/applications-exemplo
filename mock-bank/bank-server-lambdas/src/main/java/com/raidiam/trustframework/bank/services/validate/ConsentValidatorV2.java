package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.mockbank.models.generated.CreateConsentV2;

public interface ConsentValidatorV2 {

    void validate(CreateConsentV2 request);

}
