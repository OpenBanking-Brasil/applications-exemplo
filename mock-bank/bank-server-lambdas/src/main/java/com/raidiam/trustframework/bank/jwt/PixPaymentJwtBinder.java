package com.raidiam.trustframework.bank.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raidiam.trustframework.mockbank.models.generated.CreatePixPayment;
import com.raidiam.trustframework.mockbank.models.generated.CreatePixPaymentData;
import io.micronaut.core.type.Argument;

import javax.inject.Singleton;

@Singleton
public class PixPaymentJwtBinder extends AbstractJwtBinder<CreatePixPayment> {

    public PixPaymentJwtBinder(ObjectMapper objectMapper) {
       super(objectMapper);
    }

    @Override
    public Argument<CreatePixPayment> argumentType() {
        return Argument.of(CreatePixPayment.class);
    }

    protected CreatePixPayment doBinding(String body) throws JsonProcessingException {
        CreatePixPaymentData data = objectMapper.readValue(body, CreatePixPaymentData.class);
        CreatePixPayment cpp = new CreatePixPayment()
                .data(data);
        return cpp;
    }

}
