package com.raidiam.trustframework.bank.services.validate.consentsextends;

import com.raidiam.trustframework.bank.domain.ConsentEntity;
import com.raidiam.trustframework.mockbank.models.generated.CreateConsentExtendsV3;

public interface ConsentsExtendsValidatorV3 {
    void validate(CreateConsentExtendsV3 req, ConsentEntity consentEntity);
}
