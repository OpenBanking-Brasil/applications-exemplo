package com.raidiam.trustframework.bank.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raidiam.trustframework.mockbank.models.generated.CreateRecurringConsentV1;
import com.raidiam.trustframework.mockbank.models.generated.CreateRecurringConsentV1Data;
import io.micronaut.core.type.Argument;

import javax.inject.Singleton;

@Singleton
public class RecurringPaymentConsentV1JwtBinder extends AbstractJwtBinder<CreateRecurringConsentV1> {

    public RecurringPaymentConsentV1JwtBinder(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public Argument<CreateRecurringConsentV1> argumentType() {
        return Argument.of(CreateRecurringConsentV1.class);
    }

    @Override
    protected CreateRecurringConsentV1 doBinding(String body) throws JsonProcessingException {
        CreateRecurringConsentV1Data data = objectMapper.readValue(body, CreateRecurringConsentV1Data.class);
        return new CreateRecurringConsentV1()
                .data(data);
    }

}
