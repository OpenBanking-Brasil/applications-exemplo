package com.raidiam.trustframework.bank.services.validate.consentsextends;

import com.raidiam.trustframework.bank.domain.ConsentEntity;
import com.raidiam.trustframework.mockbank.models.generated.CreateConsentExtendsV3;
import com.raidiam.trustframework.mockbank.models.generated.EnumConsentExtendsErrorCode;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ConsentExtendsExpirationDateTimeValidatorV3 implements ConsentsExtendsValidatorV3 {

    private static final Logger LOG = LoggerFactory.getLogger(ConsentExtendsExpirationDateTimeValidatorV3.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Override
    public void validate(CreateConsentExtendsV3 req, ConsentEntity consentEntity) {
        Optional.ofNullable(req.getData().getExpirationDateTime()).ifPresentOrElse(
                this::validateExpirationDateTime,
                () -> LOG.info("Expiration date time is missing, skipping validation")
        );
    }

    private void validateExpirationDateTime(OffsetDateTime expirationDateTime) {
        LocalDateTime currentDate = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
        LocalDateTime expirationTimeStamp = LocalDateTime.ofInstant(expirationDateTime.toInstant(), ZoneOffset.UTC);
        LOG.info("Validating expirationDateTime - {}, currentDate - {}",
                expirationTimeStamp.format(FORMATTER),
                currentDate.format(FORMATTER));

        if (expirationTimeStamp.isAfter(currentDate.plusYears(1)) || currentDate.isAfter(expirationTimeStamp) ) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    String.format("%s: new expirationDateTime cannot be in the past or more than one year", EnumConsentExtendsErrorCode.DATA_EXPIRACAO_INVALIDA));
        }
    }
}