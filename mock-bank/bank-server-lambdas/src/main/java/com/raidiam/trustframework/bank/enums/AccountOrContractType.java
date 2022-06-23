package com.raidiam.trustframework.bank.enums;

import com.raidiam.trustframework.bank.domain.ContractEntity;

public enum AccountOrContractType {

    ACCOUNT,
    CREDIT_CARD_ACCOUNT,
    LOAN,
    FINANCING,
    UNARRANGED_ACCOUNT_OVERDRAFT,
    INVOICE_FINANCING
    ;

    public boolean isOfType(ContractEntity entity) {
        return this.name().equals(entity.getContractType());
    }

    @Override
    public String toString() {
        return this.name();
    }
}
