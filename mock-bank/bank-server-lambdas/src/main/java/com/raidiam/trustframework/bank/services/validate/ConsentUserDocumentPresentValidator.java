package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.exceptions.TrustframeworkException;
import com.raidiam.trustframework.mockbank.models.generated.*;

import java.util.Optional;

public class ConsentUserDocumentPresentValidator implements ConsentValidator, ConsentValidatorV2, ConsentValidatorV3 {
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

    @Override
    public void validate(CreateConsentV2 request) {
        var validOptional = Optional.of(request)
                .map(CreateConsentV2::getData)
                .map(CreateConsentV2Data::getLoggedUser)
                .map(LoggedUser::getDocument)
                .filter(a -> a.getIdentification() != null)
                .filter(a -> a.getRel() != null);
        if(validOptional.isEmpty()){
            throw new TrustframeworkException("Consent Requests must have a logged user with document identification and rel");
        }
    }

    @Override
    public void validate(CreateConsentV3 request) {
        var validOptional = Optional.of(request)
                .map(CreateConsentV3::getData)
                .map(CreateConsentV3Data::getLoggedUser)
                .map(LoggedUser::getDocument)
                .filter(a -> a.getIdentification() != null)
                .filter(a -> a.getRel() != null);
        if(validOptional.isEmpty()){
            throw new TrustframeworkException("Consent Requests must have a logged user with document identification and rel");
        }
    }
}
