package com.raidiam.trustframework.bank.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsentV4;
import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsentV4Data;
import io.micronaut.core.type.Argument;

import javax.inject.Singleton;

@Singleton
public class PaymentConsentV4JwtBinder extends AbstractJwtBinder<CreatePaymentConsentV4> {

    public PaymentConsentV4JwtBinder(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public Argument<CreatePaymentConsentV4> argumentType() {
        return Argument.of(CreatePaymentConsentV4.class);
    }

    @Override
    protected CreatePaymentConsentV4 doBinding(String body) throws JsonProcessingException {
        CreatePaymentConsentV4Data data = objectMapper.readValue(body, CreatePaymentConsentV4Data.class);
        return new CreatePaymentConsentV4()
                .data(data);
    }

}
