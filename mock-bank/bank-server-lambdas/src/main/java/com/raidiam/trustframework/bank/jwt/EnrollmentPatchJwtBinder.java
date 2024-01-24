package com.raidiam.trustframework.bank.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raidiam.trustframework.mockbank.models.generated.PatchEnrollment;
import com.raidiam.trustframework.mockbank.models.generated.PatchEnrollmentData;
import io.micronaut.core.type.Argument;

import javax.inject.Singleton;

@Singleton
public class EnrollmentPatchJwtBinder extends AbstractJwtBinder<PatchEnrollment> {

    public EnrollmentPatchJwtBinder(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public Argument<PatchEnrollment> argumentType() {
        return Argument.of(PatchEnrollment.class);
    }

    @Override
    protected PatchEnrollment doBinding(String body) throws JsonProcessingException {
        PatchEnrollmentData data = objectMapper.readValue(body, PatchEnrollmentData.class);
        return new PatchEnrollment()
                .data(data);
    }

}
