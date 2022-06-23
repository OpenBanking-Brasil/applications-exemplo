package com.raidiam.trustframework.bank.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raidiam.trustframework.mockbank.models.generated.PatchPaymentsConsent;
import com.raidiam.trustframework.mockbank.models.generated.PatchPaymentsConsentData;
import io.micronaut.core.type.Argument;

import javax.inject.Singleton;

@Singleton
public class PaymentPatchJwtBinder extends AbstractJwtBinder<PatchPaymentsConsent> {

    public PaymentPatchJwtBinder(ObjectMapper objectMapper){
        super(objectMapper);
    }

    @Override
    protected PatchPaymentsConsent doBinding(String body) throws JsonProcessingException {
        PatchPaymentsConsentData data = objectMapper.readValue(body, PatchPaymentsConsentData.class);
        return new PatchPaymentsConsent()
                .data(data);
    }

    @Override
    public Argument<PatchPaymentsConsent> argumentType() {
        return Argument.of(PatchPaymentsConsent.class);
    }
}
