package com.raidiam.trustframework.bank.services.validate.consentsextends;

import com.raidiam.trustframework.bank.domain.ConsentEntity;
import com.raidiam.trustframework.mockbank.models.generated.CreateConsentExtends;

public interface ConsentsExtendsValidatorV2 {
    void validate(CreateConsentExtends req, ConsentEntity consentEntity);
}
