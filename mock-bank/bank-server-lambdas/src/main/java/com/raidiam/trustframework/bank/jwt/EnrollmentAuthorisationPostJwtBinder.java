package com.raidiam.trustframework.bank.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raidiam.trustframework.mockbank.models.generated.ConsentAuthorization;
import com.raidiam.trustframework.mockbank.models.generated.ConsentAuthorizationData;
import io.micronaut.core.type.Argument;

import javax.inject.Singleton;

@Singleton
public class EnrollmentAuthorisationPostJwtBinder extends AbstractJwtBinder<ConsentAuthorization> {

    public EnrollmentAuthorisationPostJwtBinder(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public Argument<ConsentAuthorization> argumentType() {
        return Argument.of(ConsentAuthorization.class);
    }

    @Override
    protected ConsentAuthorization doBinding(String body) throws JsonProcessingException {
        ConsentAuthorizationData data = objectMapper.readValue(body, ConsentAuthorizationData.class);
        return new ConsentAuthorization()
                .data(data);
    }

}
