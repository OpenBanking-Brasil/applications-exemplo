package com.raidiam.trustframework.bank.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raidiam.trustframework.mockbank.models.generated.EnrollmentFidoOptionsInput;
import com.raidiam.trustframework.mockbank.models.generated.EnrollmentFidoOptionsInputData;
import io.micronaut.core.type.Argument;

import javax.inject.Singleton;

@Singleton
public class EnrollmentFidoRegistrationOptionsInputJwtBinder extends AbstractJwtBinder<EnrollmentFidoOptionsInput> {

    public EnrollmentFidoRegistrationOptionsInputJwtBinder(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public Argument<EnrollmentFidoOptionsInput> argumentType() {
        return Argument.of(EnrollmentFidoOptionsInput.class);
    }

    @Override
    protected EnrollmentFidoOptionsInput doBinding(String body) throws JsonProcessingException {
        EnrollmentFidoOptionsInputData data = objectMapper.readValue(body, EnrollmentFidoOptionsInputData.class);
        return new EnrollmentFidoOptionsInput()
                .data(data);
    }

}
