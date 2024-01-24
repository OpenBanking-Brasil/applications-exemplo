package com.raidiam.trustframework.bank.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raidiam.trustframework.mockbank.models.generated.CreatePixPaymentDataV2;
import com.raidiam.trustframework.mockbank.models.generated.CreatePixPaymentV2;
import io.micronaut.core.type.Argument;

import javax.inject.Singleton;

@Singleton
public class PixPaymentV2JwtBinder extends AbstractJwtBinder<CreatePixPaymentV2> {

    public PixPaymentV2JwtBinder(ObjectMapper objectMapper) {
       super(objectMapper);
    }

    @Override
    public Argument<CreatePixPaymentV2> argumentType() {
        return Argument.of(CreatePixPaymentV2.class);
    }

    protected CreatePixPaymentV2 doBinding(String body) throws JsonProcessingException {
        CreatePixPaymentDataV2 data = objectMapper.readValue(body, CreatePixPaymentDataV2.class);
        return new CreatePixPaymentV2()
                .data(data);
    }

}
