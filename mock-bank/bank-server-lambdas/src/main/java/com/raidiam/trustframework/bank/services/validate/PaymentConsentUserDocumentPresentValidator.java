package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.exceptions.TrustframeworkException;
import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsent;
import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsentData;
import com.raidiam.trustframework.mockbank.models.generated.LoggedUser;

import java.util.Optional;

public class PaymentConsentUserDocumentPresentValidator implements PaymentConsentValidator {
    @Override
    public void validate(CreatePaymentConsent request) {
        var validOptional = Optional.of(request)
                .map(CreatePaymentConsent::getData)
                .map(CreatePaymentConsentData::getLoggedUser)
                .map(LoggedUser::getDocument)
                .filter(a -> a.getIdentification() != null)
                .filter(a -> a.getRel() != null);
        if(validOptional.isEmpty()) {
            throw new TrustframeworkException("Payment Consent Requests must have a logged user with document identification and rel");
        }
    }
}
