package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.services.message.PaymentErrorMessage;
import com.raidiam.trustframework.mockbank.models.generated.BusinessEntity;
import com.raidiam.trustframework.mockbank.models.generated.CreateRecurringConsentV1;
import com.raidiam.trustframework.mockbank.models.generated.CreateRecurringConsentV1Data;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class RecurringPaymentConsentFieldsValidatorV1 implements RecurringPaymentConsentValidatorV1 {
    private static final Logger LOG = LoggerFactory.getLogger(RecurringPaymentConsentFieldsValidatorV1.class);
    private final PaymentErrorMessage errorMessage;


    @Override
    public void validate(CreateRecurringConsentV1 request) {
        LOG.info("Validating recurring consent fields");
        var data = request.getData();


        var recurringConfiguration = Optional.ofNullable(data.getRecurringConfiguration())
                .orElseThrow(() -> new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        errorMessage.getParameterNotInformed("Exactly one of sweeping, vrp or automatic recurring configurations must be specified")
                ));

        long nonNullRecurringConfigurationCount = Stream.of(recurringConfiguration.getSweeping(), recurringConfiguration.getVrp(), recurringConfiguration.getAutomatic())
                .filter(Objects::nonNull)
                .count();

        if (nonNullRecurringConfigurationCount == 0 || nonNullRecurringConfigurationCount > 1) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    errorMessage.getParameterNotInformed("Exactly one of sweeping, vrp or automatic recurring configurations must be specified")
            );
        }

        // This should be changed when we start supporting more configurations
        if (recurringConfiguration.getVrp() != null || recurringConfiguration.getAutomatic() != null) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    errorMessage.getParameterNotInformed("Only sweeping recurring configuration is supported by mockbank")
            );
        }

        if (recurringConfiguration.getSweeping() != null) {
            validateSweepingConfiguration(data);
        }


        OffsetDateTime expirationDateTime = data.getExpirationDateTime();
        if (expirationDateTime.isBefore(OffsetDateTime.now())) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    errorMessage.getMessageInvalidParameter("expirationDateTime cannot be in the past")
            );
        }

        OffsetDateTime startDateTime = data.getStartDateTime();
        if (expirationDateTime.isBefore(startDateTime)) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    errorMessage.getMessageInvalidParameter("startDateTime cannot be after expirationDateTime")
            );
        }
        LOG.info("recurring consent fields validation complete");

    }


    private void validateSweepingConfiguration(CreateRecurringConsentV1Data data) {
        LOG.info("Sweeping recurring configuration is not null, validating");
        BusinessEntity businessEntity = data.getBusinessEntity();
        if (businessEntity != null) {
            LOG.info("Business entity is present, validating creditors CNPJ against business entity CNPJ prefix");
            String businessIdentificationPrefix = businessEntity.getDocument().getIdentification()
                    .substring(0, 8);

            long numberOfCreditorsWithDifferentCnpjPrefix = data.getCreditors().stream()
                    .filter(c -> !c.getCpfCnpj().startsWith(businessIdentificationPrefix))
                    .count();
            LOG.info("Found {} creditors with different CNPJ", numberOfCreditorsWithDifferentCnpjPrefix);
            if (numberOfCreditorsWithDifferentCnpjPrefix > 0) {
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        errorMessage.getMessageInvalidParameter("All Creditors have to have the same CNPJ prefix as the business entity")
                );
            }
        } else {
            LOG.info("Business entity is absent, validating creditors CPF logged user CPF");
            String loggedUserIdentification = data.getLoggedUser().getDocument().getIdentification();
            long numberOfCreditorsWithDifferentCpf = data.getCreditors().stream()
                    .filter(c -> !c.getCpfCnpj().equals(loggedUserIdentification))
                    .count();
            LOG.info("Found {} creditors with different CPF", numberOfCreditorsWithDifferentCpf);
            if (numberOfCreditorsWithDifferentCpf > 0) {
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        errorMessage.getMessageInvalidParameter("All Creditors have to have the same CPF as the logged user")
                );
            }
        }

    }
}
