package com.raidiam.trustframework.bank.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raidiam.trustframework.mockbank.models.generated.PatchPixPaymentV4;
import com.raidiam.trustframework.mockbank.models.generated.PatchPixPaymentV4Data;
import io.micronaut.core.type.Argument;

import javax.inject.Singleton;

@Singleton
public class PixPaymentV4PatchJwtBinder extends AbstractJwtBinder<PatchPixPaymentV4> {

    public PixPaymentV4PatchJwtBinder(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public Argument<PatchPixPaymentV4> argumentType() {
        return Argument.of(PatchPixPaymentV4.class);
    }

    protected PatchPixPaymentV4 doBinding(String body) throws JsonProcessingException {
        PatchPixPaymentV4Data data = objectMapper.readValue(body, PatchPixPaymentV4Data.class);
        return new PatchPixPaymentV4()
                .data(data);
    }
}
