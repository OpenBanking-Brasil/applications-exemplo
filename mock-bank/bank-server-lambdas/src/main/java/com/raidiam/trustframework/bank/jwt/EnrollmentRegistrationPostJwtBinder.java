package com.raidiam.trustframework.bank.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raidiam.trustframework.mockbank.models.generated.EnrollmentFidoRegistration;
import com.raidiam.trustframework.mockbank.models.generated.EnrollmentFidoRegistrationData;
import io.micronaut.core.type.Argument;

import javax.inject.Singleton;

@Singleton
public class EnrollmentRegistrationPostJwtBinder extends AbstractJwtBinder<EnrollmentFidoRegistration> {

    public EnrollmentRegistrationPostJwtBinder(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public Argument<EnrollmentFidoRegistration> argumentType() {
        return Argument.of(EnrollmentFidoRegistration.class);
    }

    @Override
    protected EnrollmentFidoRegistration doBinding(String body) throws JsonProcessingException {
        EnrollmentFidoRegistrationData data = objectMapper.readValue(body, EnrollmentFidoRegistrationData.class);
        return new EnrollmentFidoRegistration()
                .data(data);
    }

}
