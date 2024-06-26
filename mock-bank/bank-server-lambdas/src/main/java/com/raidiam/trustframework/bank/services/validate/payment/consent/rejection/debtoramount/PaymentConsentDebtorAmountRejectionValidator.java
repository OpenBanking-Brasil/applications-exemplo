package com.raidiam.trustframework.bank.services.validate.payment.consent.rejection.debtoramount;

import com.raidiam.trustframework.bank.exceptions.ConsentRejectionException;
import com.raidiam.trustframework.bank.repository.AccountHolderRepository;
import com.raidiam.trustframework.bank.repository.AccountRepository;
import com.raidiam.trustframework.bank.services.validate.payment.consent.rejection.PaymentConsentRejectionValidator;
import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsent;
import com.raidiam.trustframework.mockbank.models.generated.DebtorAccount;
import com.raidiam.trustframework.mockbank.models.generated.Document;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class PaymentConsentDebtorAmountRejectionValidator implements PaymentConsentRejectionValidator {
    private static final Logger LOG = LoggerFactory.getLogger(PaymentConsentDebtorAmountRejectionValidator.class);


    private final PaymentConsentDebtorAmountRejectionValidatorExceptions exceptions;
    private final AccountHolderRepository accountHolderRepository;
    private final AccountRepository accountRepository;


    @Override
    public void validate(CreatePaymentConsent request) throws ConsentRejectionException {
        LOG.info("Started Debtor Amount Validation");
        Document document = request.getData().getLoggedUser().getDocument();
        DebtorAccount debtorAccount = request.getData().getDebtorAccount();
        double paymentAmount = Double.parseDouble(request.getData().getPayment().getAmount());

        validateDebtorAmount(document, debtorAccount, paymentAmount);
    }

    public void validateDebtorAmount(Document document, DebtorAccount debtorAccount,double paymentAmount) {
        if (debtorAccount == null) {
            LOG.info("Debtor Account is null, skipping validation");
            return;
        }

        var accountHolder = accountHolderRepository
                .findByDocumentIdentificationAndDocumentRel(document.getIdentification(), document.getRel())
                .stream()
                .findAny()
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, String.format("User with documentation Id %s and rel %s not found",
                        document.getIdentification(), document.getRel())));

        var account = accountRepository.findByNumberAndAccountHolderId(debtorAccount.getNumber(), accountHolder.getAccountHolderId())
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Account with number %s not found", debtorAccount.getNumber())));


        double availableAmount = account.getAvailableAmount();

        LOG.info("Payment amount {} | available amount {}", paymentAmount, availableAmount);

        if (paymentAmount > availableAmount) {
            throw exceptions.getInsufficientAmountException();
        }
    }
}
