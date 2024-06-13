
package com.raidiam.trustframework.bank.services.validate.payment.consent.rejection.debtoramount;

import com.raidiam.trustframework.bank.exceptions.ConsentRejectionException;
import com.raidiam.trustframework.bank.repository.AccountHolderRepository;
import com.raidiam.trustframework.bank.repository.AccountRepository;
import com.raidiam.trustframework.bank.services.validate.payment.consent.rejection.PaymentConsentRejectionValidatorV4;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentConsentDebtorAmountRejectionValidatorV4 implements PaymentConsentRejectionValidatorV4 {
    private static final Logger LOG = LoggerFactory.getLogger(PaymentConsentDebtorAmountRejectionValidatorV4.class);

    PaymentConsentDebtorAmountRejectionValidator paymentConsentDebtorAmountRejectionValidator;

    public PaymentConsentDebtorAmountRejectionValidatorV4(PaymentConsentDebtorAmountRejectionValidatorExceptionsV3 exceptions, AccountHolderRepository accountHolderRepository, AccountRepository accountRepository) {
        paymentConsentDebtorAmountRejectionValidator = new PaymentConsentDebtorAmountRejectionValidator(exceptions, accountHolderRepository, accountRepository);
    }

    @Override
    public void validate(CreatePaymentConsentV4 request) throws ConsentRejectionException {
        LOG.info("Started Debtor Amount Validation");
        Document document = request.getData().getLoggedUser().getDocument();
        DebtorAccount debtorAccount = request.getData().getDebtorAccount();
        double paymentAmount = Double.parseDouble(request.getData().getPayment().getAmount());

        paymentConsentDebtorAmountRejectionValidator.validateDebtorAmount(document, debtorAccount, paymentAmount);
    }
}