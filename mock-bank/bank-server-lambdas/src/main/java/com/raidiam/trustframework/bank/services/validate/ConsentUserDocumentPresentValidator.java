package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.exceptions.TrustframeworkException;
import com.raidiam.trustframework.mockbank.models.generated.CreateConsent;
import com.raidiam.trustframework.mockbank.models.generated.CreateConsentData;
import com.raidiam.trustframework.mockbank.models.generated.LoggedUser;

import java.util.Optional;

public class ConsentUserDocumentPresentValidator implements ConsentValidator {
    @Override
    public void validate(CreateConsent request) {
        var validOptional = Optional.of(request)
                .map(CreateConsent::getData)
                .map(CreateConsentData::getLoggedUser)
                .map(LoggedUser::getDocument)
                .filter(a -> a.getIdentification() != null)
                .filter(a -> a.getRel() != null);
        if(validOptional.isEmpty()){
            throw new TrustframeworkException("Consent Requests must have a logged user with document identification and rel");
        }
    }
}
