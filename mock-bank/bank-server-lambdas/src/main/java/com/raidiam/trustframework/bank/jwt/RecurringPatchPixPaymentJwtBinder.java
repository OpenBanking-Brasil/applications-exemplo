package com.raidiam.trustframework.bank.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raidiam.trustframework.mockbank.models.generated.RecurringPatchPixPayment;
import com.raidiam.trustframework.mockbank.models.generated.RecurringPatchPixPaymentData;
import io.micronaut.core.type.Argument;

import javax.inject.Singleton;

@Singleton
public class RecurringPatchPixPaymentJwtBinder extends AbstractJwtBinder<RecurringPatchPixPayment> {

    public RecurringPatchPixPaymentJwtBinder(ObjectMapper objectMapper) {
       super(objectMapper);
    }

    @Override
    public Argument<RecurringPatchPixPayment> argumentType() {
        return Argument.of(RecurringPatchPixPayment.class);
    }

    protected RecurringPatchPixPayment doBinding(String body) throws JsonProcessingException {
        RecurringPatchPixPaymentData data = objectMapper.readValue(body, RecurringPatchPixPaymentData.class);
        return new RecurringPatchPixPayment()
                .data(data);
    }

}