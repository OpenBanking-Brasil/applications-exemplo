package com.raidiam.trustframework.bank.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsent;
import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsentData;
import io.micronaut.core.type.Argument;

import javax.inject.Singleton;

@Singleton
public class PaymentConsentJwtBinder extends AbstractJwtBinder<CreatePaymentConsent> {

    public PaymentConsentJwtBinder(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public Argument<CreatePaymentConsent> argumentType() {
        return Argument.of(CreatePaymentConsent.class);
    }

    @Override
    protected CreatePaymentConsent doBinding(String body) throws JsonProcessingException {
        CreatePaymentConsentData data = objectMapper.readValue(body, CreatePaymentConsentData.class);
        CreatePaymentConsent cps = new CreatePaymentConsent()
                .data(data);
        return cps;
    }

}
