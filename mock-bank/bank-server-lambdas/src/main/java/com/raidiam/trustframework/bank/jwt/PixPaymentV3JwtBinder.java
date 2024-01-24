package com.raidiam.trustframework.bank.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raidiam.trustframework.mockbank.models.generated.CreatePixPaymentDataV3;
import com.raidiam.trustframework.mockbank.models.generated.CreatePixPaymentV3;
import io.micronaut.core.type.Argument;

import javax.inject.Singleton;

@Singleton
public class PixPaymentV3JwtBinder extends AbstractJwtBinder<CreatePixPaymentV3> {

    public PixPaymentV3JwtBinder(ObjectMapper objectMapper) {
       super(objectMapper);
    }

    @Override
    public Argument<CreatePixPaymentV3> argumentType() {
        return Argument.of(CreatePixPaymentV3.class);
    }

    protected CreatePixPaymentV3 doBinding(String body) throws JsonProcessingException {
        CreatePixPaymentDataV3 data = objectMapper.readValue(body, CreatePixPaymentDataV3.class);
        return new CreatePixPaymentV3()
                .data(data);
    }

}
