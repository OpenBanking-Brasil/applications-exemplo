package com.raidiam.trustframework.bank.services.validate.consentsextends;

import com.raidiam.trustframework.bank.domain.ConsentEntity;
import com.raidiam.trustframework.mockbank.models.generated.CreateConsentExtends;
import com.raidiam.trustframework.mockbank.models.generated.CreateConsentExtendsV3;
import com.raidiam.trustframework.mockbank.models.generated.Document;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsentExtendsLoggedUserValidator implements ConsentsExtendsValidatorV2, ConsentsExtendsValidatorV3 {

    private static final Logger LOG = LoggerFactory.getLogger(ConsentExtendsLoggedUserValidator.class);

    @Override
    public void validate(CreateConsentExtends req, ConsentEntity consentEntity) {
        Document document = req.getData().getLoggedUser().getDocument();
        validateLoggedDocument(consentEntity, document);
    }

    @Override
    public void validate(CreateConsentExtendsV3 req, ConsentEntity consentEntity) {
        Document document = req.getData().getLoggedUser().getDocument();
        validateLoggedDocument(consentEntity, document);
    }

    private void validateLoggedDocument(ConsentEntity consentEntity, Document document) {
        String requestRel = document.getRel();
        String requestIdentification = document.getIdentification();
        String consentIdentification = consentEntity.getAccountHolder().getDocumentIdentification();
        String consentRel = consentEntity.getAccountHolder().getDocumentRel();

        LOG.info("verifying request rel - {} and identification - {} against consent rel - {} and identification - {}",
                requestRel, requestIdentification, consentRel, consentIdentification);

        if (!requestIdentification.equals(consentIdentification) || !requestRel.equals(consentRel)) {
            throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "unauthorized");
        }
    }
}