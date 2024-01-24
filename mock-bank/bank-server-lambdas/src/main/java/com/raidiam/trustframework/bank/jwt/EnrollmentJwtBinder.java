package com.raidiam.trustframework.bank.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raidiam.trustframework.mockbank.models.generated.CreateEnrollment;
import com.raidiam.trustframework.mockbank.models.generated.CreateEnrollmentData;
import io.micronaut.core.type.Argument;

import javax.inject.Singleton;

@Singleton
public class EnrollmentJwtBinder extends AbstractJwtBinder<CreateEnrollment> {

    public EnrollmentJwtBinder(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public Argument<CreateEnrollment> argumentType() {
        return Argument.of(CreateEnrollment.class);
    }

    @Override
    protected CreateEnrollment doBinding(String body) throws JsonProcessingException {
        CreateEnrollmentData data = objectMapper.readValue(body, CreateEnrollmentData.class);
        return new CreateEnrollment()
                .data(data);
    }

}
