package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsent;
import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsentData;
import com.raidiam.trustframework.mockbank.models.generated.EnumCreditorPersonType;
import com.raidiam.trustframework.mockbank.models.generated.Identification;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

public class ConsentCreditorFieldValidator implements PaymentConsentValidator {
    @Override
    public void validate(CreatePaymentConsent request) {
        CreatePaymentConsentData data = request.getData();
        Identification creditor = data.getCreditor();

        if(!checkPersonType(creditor.getPersonType())){
            // Message: The field creditorAccount - personType does not fulfill the filling in requirements.
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "DETAIL_PGTO_INVALID: O campo creditorConta " +
                    "- personType não preenche os requisitos de preenchimento."
            );
        }
    }

    private boolean checkPersonType(String type){
        for(EnumCreditorPersonType value : EnumCreditorPersonType.values()){
            if(value.toString().equals(type)){
                return true;
            }
        }
        return false;
    }
}
