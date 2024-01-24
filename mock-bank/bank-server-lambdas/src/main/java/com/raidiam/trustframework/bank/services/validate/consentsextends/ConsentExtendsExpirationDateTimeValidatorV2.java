package com.raidiam.trustframework.bank.services.validate.consentsextends;

import com.raidiam.trustframework.bank.domain.ConsentEntity;
import com.raidiam.trustframework.mockbank.models.generated.CreateConsentExtends;
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

public class ConsentExtendsExpirationDateTimeValidatorV2 implements ConsentsExtendsValidatorV2{

    private static final Logger LOG = LoggerFactory.getLogger(ConsentExtendsExpirationDateTimeValidatorV2.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Override
    public void validate(CreateConsentExtends req, ConsentEntity consentEntity) {
        validateExpirationDateTime(consentEntity, req.getData().getExpirationDateTime());
    }

    private void validateExpirationDateTime(ConsentEntity consentEntity, OffsetDateTime expirationDateTime) {
        LocalDateTime currentDate = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
        LocalDateTime expirationTimeStamp = LocalDateTime.ofInstant(expirationDateTime.toInstant(), ZoneOffset.UTC);

        // 2300-01-01T00:00:00Z
        LocalDateTime infiniteTimeStamp = LocalDateTime.of(2300, 1, 1, 0, 0, 0);
        LocalDateTime consentExpirationTimeStamp = LocalDateTime.ofInstant(consentEntity.getExpirationDateTime().toInstant(), ZoneOffset.UTC);

        LOG.info("Validating expirationDateTime - {}, currentDate - {}, infiniteTimeStamp - {}, consentExpirationTimeStamp - {}",
                expirationTimeStamp.format(FORMATTER),
                currentDate.format(FORMATTER),
                infiniteTimeStamp.format(FORMATTER),
                consentExpirationTimeStamp.format(FORMATTER));


        if (!expirationTimeStamp.isEqual(infiniteTimeStamp) && (currentDate.isAfter(expirationTimeStamp) || expirationTimeStamp.isAfter(currentDate.plusYears(1)))) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    String.format("%s: new expirationDateTime cannot be in the past or more than one year", EnumConsentExtendsErrorCode.DATA_EXPIRACAO_INVALIDA));
        }

        if (expirationTimeStamp.isBefore(consentExpirationTimeStamp) || expirationTimeStamp.equals(consentExpirationTimeStamp)) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    String.format("%s: new expirationDateTime cannot be equal to or less than the expirationDateTime of the consnet",
                            EnumConsentExtendsErrorCode.DATA_EXPIRACAO_INVALIDA));
        }

    }
}