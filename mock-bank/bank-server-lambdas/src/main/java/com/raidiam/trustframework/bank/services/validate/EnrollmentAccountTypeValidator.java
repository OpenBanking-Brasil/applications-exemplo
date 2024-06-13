package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.mockbank.models.generated.CreateEnrollment;
import com.raidiam.trustframework.mockbank.models.generated.EnumAccountPaymentsType;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

public class EnrollmentAccountTypeValidator implements EnrollmentValidator {

    @Override
    public void validate(CreateEnrollment request) {
        var requestedDebtorAccount = request.getData().getDebtorAccount();
        if (requestedDebtorAccount != null) {
            if (requestedDebtorAccount.getAccountType() == null) {
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "CONTA_INVALIDA: Debtor account account type is missing or empty");
            }
            if (requestedDebtorAccount.getIssuer() == null
                    && requestedDebtorAccount.getAccountType().equals(EnumAccountPaymentsType.CACC)) {
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "CONTA_INVALIDA: when issuer is missing cant have accountType as CACC");
            }
        }

    }


}
