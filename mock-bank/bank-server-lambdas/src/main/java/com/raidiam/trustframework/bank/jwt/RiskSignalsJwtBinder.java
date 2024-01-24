package com.raidiam.trustframework.bank.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raidiam.trustframework.mockbank.models.generated.RiskSignals;
import com.raidiam.trustframework.mockbank.models.generated.RiskSignalsData;
import io.micronaut.core.type.Argument;

import javax.inject.Singleton;

@Singleton
public class RiskSignalsJwtBinder extends AbstractJwtBinder<RiskSignals> {

    public RiskSignalsJwtBinder(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public Argument<RiskSignals> argumentType() {
        return Argument.of(RiskSignals.class);
    }

    @Override
    protected RiskSignals doBinding(String body) throws JsonProcessingException {
        RiskSignalsData data = objectMapper.readValue(body, RiskSignalsData.class);
        return new RiskSignals()
                .data(data);
    }

}
