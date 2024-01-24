package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.mockbank.models.generated.CreateEnrollment;

public interface EnrollmentValidator {

    void validate(CreateEnrollment request);

}
