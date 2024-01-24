package com.raidiam.trustframework.bank.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raidiam.trustframework.mockbank.models.generated.CreateRecurringPixPaymentV1;
import com.raidiam.trustframework.mockbank.models.generated.CreateRecurringPixPaymentV1Data;
import io.micronaut.core.type.Argument;

import javax.inject.Singleton;

@Singleton
public class RecurringPixPaymentV1JwtBinder extends AbstractJwtBinder<CreateRecurringPixPaymentV1> {

    public RecurringPixPaymentV1JwtBinder(ObjectMapper objectMapper) {
       super(objectMapper);
    }

    @Override
    public Argument<CreateRecurringPixPaymentV1> argumentType() {
        return Argument.of(CreateRecurringPixPaymentV1.class);
    }

    protected CreateRecurringPixPaymentV1 doBinding(String body) throws JsonProcessingException {
        CreateRecurringPixPaymentV1Data data = objectMapper.readValue(body, CreateRecurringPixPaymentV1Data.class);
        return new CreateRecurringPixPaymentV1()
                .data(data);
    }

}