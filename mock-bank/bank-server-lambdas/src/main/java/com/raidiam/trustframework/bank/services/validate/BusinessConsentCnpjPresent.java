package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.exceptions.TrustframeworkException;
import com.raidiam.trustframework.mockbank.models.generated.CreateConsent;
import com.raidiam.trustframework.mockbank.models.generated.EnumConsentPermissions;

import java.util.List;

public class BusinessConsentCnpjPresent implements ConsentValidator {
    @Override
    public void validate(CreateConsent request) {
        if(isRequestForBusinessInfo(request) && (request.getData().getBusinessEntity() == null
                || request.getData().getBusinessEntity().getDocument().getIdentification().isEmpty()))
            throw new TrustframeworkException("Business identification must be informed");
    }

    private boolean isRequestForBusinessInfo(CreateConsent request) {
        return request.getData()
                .getPermissions().stream()
                .anyMatch(List.of(
                        EnumConsentPermissions.CUSTOMERS_BUSINESS_IDENTIFICATIONS_READ,
                        EnumConsentPermissions.CUSTOMERS_BUSINESS_ADITTIONALINFO_READ)::contains);
    }
}
