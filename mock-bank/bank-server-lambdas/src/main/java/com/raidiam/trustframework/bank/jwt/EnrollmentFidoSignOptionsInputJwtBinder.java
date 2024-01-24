package com.raidiam.trustframework.bank.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raidiam.trustframework.mockbank.models.generated.EnrollmentFidoSignOptionsInput;
import com.raidiam.trustframework.mockbank.models.generated.EnrollmentFidoSignOptionsInputData;
import io.micronaut.core.type.Argument;

import javax.inject.Singleton;

@Singleton
public class EnrollmentFidoSignOptionsInputJwtBinder extends AbstractJwtBinder<EnrollmentFidoSignOptionsInput> {

    public EnrollmentFidoSignOptionsInputJwtBinder(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public Argument<EnrollmentFidoSignOptionsInput> argumentType() {
        return Argument.of(EnrollmentFidoSignOptionsInput.class);
    }

    @Override
    protected EnrollmentFidoSignOptionsInput doBinding(String body) throws JsonProcessingException {
        EnrollmentFidoSignOptionsInputData data = objectMapper.readValue(body, EnrollmentFidoSignOptionsInputData.class);
        return new EnrollmentFidoSignOptionsInput()
                .data(data);
    }

}
