package com.raidiam.trustframework.bank.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raidiam.trustframework.mockbank.models.generated.CreatePixPaymentDataV4;
import com.raidiam.trustframework.mockbank.models.generated.CreatePixPaymentV4;
import io.micronaut.core.type.Argument;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class PixPaymentV4JwtBinder extends AbstractJwtBinder<CreatePixPaymentV4> {

    public PixPaymentV4JwtBinder(ObjectMapper objectMapper) {
       super(objectMapper);
    }

    @Override
    public Argument<CreatePixPaymentV4> argumentType() {
        return Argument.of(CreatePixPaymentV4.class);
    }

    protected CreatePixPaymentV4 doBinding(String body) throws JsonProcessingException {
        CreatePixPaymentDataV4[] data = objectMapper.readValue(body, CreatePixPaymentDataV4[].class);
        return new CreatePixPaymentV4()
                .data(List.of(data));
    }

}