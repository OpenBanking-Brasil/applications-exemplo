package com.raidiam.trustframework.bank.services.validate.payment.consent.rejection.creditordebtor;

import com.raidiam.trustframework.bank.exceptions.ConsentRejectionException;
import com.raidiam.trustframework.bank.services.validate.payment.consent.rejection.PaymentConsentRejectionValidator;
import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsent;
import com.raidiam.trustframework.mockbank.models.generated.CreditorAccount;
import com.raidiam.trustframework.mockbank.models.generated.DebtorAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentConsentCreditorDebtorRejectionValidator implements PaymentConsentRejectionValidator {
    private static final Logger LOG = LoggerFactory.getLogger(PaymentConsentCreditorDebtorRejectionValidator.class);

    private final PaymentConsentCreditorDebtorRejectionValidatorExceptions exceptions;

    public PaymentConsentCreditorDebtorRejectionValidator(PaymentConsentCreditorDebtorRejectionValidatorExceptions exceptions) {
        this.exceptions = exceptions;
    }

    @Override
    public void validate(CreatePaymentConsent request) throws ConsentRejectionException {
        DebtorAccount debtorAccount = request.getData().getDebtorAccount();
        CreditorAccount creditorAccount = request.getData().getPayment().getDetails().getCreditorAccount();

        LOG.info("Started Debtor Account Consent Validation");

        if (debtorAccount == null) {
            LOG.info("Debtor Account is null, skipping validation");
            return;
        }

        if (debtorAccount.getNumber().equals(creditorAccount.getNumber()) &&
                debtorAccount.getAccountType() == creditorAccount.getAccountType() &&
                debtorAccount.getIspb().equals(creditorAccount.getIspb())) {
            throw exceptions.getSameCreditorDebtorException();
        }
    }
}
