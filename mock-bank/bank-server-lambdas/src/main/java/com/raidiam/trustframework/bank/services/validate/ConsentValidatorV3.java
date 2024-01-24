package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.mockbank.models.generated.CreateConsentV3;

public interface ConsentValidatorV3 {

    void validate(CreateConsentV3 request);

}
