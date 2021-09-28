package com.raidiam.trustframework.bank.utils;

import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsent;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.TypeConverter;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
class MapToLocalDateConverter implements TypeConverter<String, CreatePaymentConsent> {

    @Override
    public Optional<CreatePaymentConsent> convert(String object, Class<CreatePaymentConsent> targetType, ConversionContext context) {
        return Optional.empty();
    }
}