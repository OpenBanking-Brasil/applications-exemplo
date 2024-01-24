package com.raidiam.trustframework.bank.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raidiam.trustframework.mockbank.models.generated.PatchPaymentsDataV2;
import com.raidiam.trustframework.mockbank.models.generated.PatchPaymentsV2;
import io.micronaut.core.type.Argument;

import javax.inject.Singleton;

@Singleton
public class PixPaymentPatchJwtBinder extends AbstractJwtBinder<PatchPaymentsV2> {

    public PixPaymentPatchJwtBinder(ObjectMapper objectMapper){
        super(objectMapper);
    }

    @Override
    protected PatchPaymentsV2 doBinding(String body) throws JsonProcessingException {
        PatchPaymentsDataV2 data = objectMapper.readValue(body, PatchPaymentsDataV2.class);
        return new PatchPaymentsV2()
                .data(data);
    }

    @Override
    public Argument<PatchPaymentsV2> argumentType() {
        return Argument.of(PatchPaymentsV2.class);
    }
}
