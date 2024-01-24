package com.raidiam.trustframework.bank.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raidiam.trustframework.mockbank.models.generated.PatchRecurringConsentV1;
import com.raidiam.trustframework.mockbank.models.generated.PatchRecurringConsentV1Data;
import io.micronaut.core.type.Argument;

import javax.inject.Singleton;

@Singleton
public class RecurringPaymentConsentPatchV1JwtBinder extends AbstractJwtBinder<PatchRecurringConsentV1> {

    public RecurringPaymentConsentPatchV1JwtBinder(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public Argument<PatchRecurringConsentV1> argumentType() {
        return Argument.of(PatchRecurringConsentV1.class);
    }

    @Override
    protected PatchRecurringConsentV1 doBinding(String body) throws JsonProcessingException {
        PatchRecurringConsentV1Data data = objectMapper.readValue(body, PatchRecurringConsentV1Data.class);
        return new PatchRecurringConsentV1()
                .data(data);
    }

}
