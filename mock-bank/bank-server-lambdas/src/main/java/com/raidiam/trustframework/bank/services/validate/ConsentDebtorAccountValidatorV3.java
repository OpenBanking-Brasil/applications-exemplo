package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.services.message.PaymentErrorMessage;
import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsent;
import com.raidiam.trustframework.mockbank.models.generated.EnumAccountPaymentsType;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConsentDebtorAccountValidatorV3 implements PaymentConsentValidator {

    private static final Logger LOG = LoggerFactory.getLogger(ConsentDebtorAccountValidatorV3.class);

    private PaymentErrorMessage errorMessage;
    public ConsentDebtorAccountValidatorV3(PaymentErrorMessage errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public void validate(CreatePaymentConsent request) {
        LOG.info("Started Debtor Account Consent Validation");
        if(request.getData().getDebtorAccount() != null && request.getData().getDebtorAccount().getAccountType().equals(EnumAccountPaymentsType.SLRY)) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, errorMessage.getMessageInvalidParameter());
        }
    }
}
