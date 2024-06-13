package com.raidiam.trustframework.bank.services.validate.consentsextends;

import com.raidiam.trustframework.bank.domain.ConsentEntity;
import com.raidiam.trustframework.mockbank.models.generated.BusinessEntity;
import com.raidiam.trustframework.mockbank.models.generated.BusinessEntityDocument;
import com.raidiam.trustframework.mockbank.models.generated.CreateConsentExtendsV3;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsentExtendsBusinessEntityValidatorV3 implements ConsentsExtendsValidatorV3 {

    private static final Logger LOG = LoggerFactory.getLogger(ConsentExtendsBusinessEntityValidatorV3.class);

    @Override
    public void validate(CreateConsentExtendsV3 req, ConsentEntity consentEntity) {
        BusinessEntity businessEntity = req.getData().getBusinessEntity();
        if (businessEntity != null) {
            validateBusinessEntity(consentEntity, businessEntity);
        }
    }

    private void validateBusinessEntity(ConsentEntity consentEntity, BusinessEntity businessEntity) {
        BusinessEntityDocument document = businessEntity.getDocument();
        String requestRel = document.getRel();
        String requestIdentification = document.getIdentification();
        String consentRel = consentEntity.getBusinessDocumentRel();
        String consentIdentification = consentEntity.getBusinessDocumentIdentification();
        LOG.info("Verifying BusinessEntity rel - {} and identification - {} from request against rel - {} and identification - {} from consent",
                requestRel, requestIdentification, consentRel, consentIdentification);

        if (consentRel != null && consentIdentification != null && (!requestRel.equals(consentRel) || !requestIdentification.equals(consentIdentification))) {
            throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "BusinessEntity values must match those stored in the associated consent");
        }
    }
}