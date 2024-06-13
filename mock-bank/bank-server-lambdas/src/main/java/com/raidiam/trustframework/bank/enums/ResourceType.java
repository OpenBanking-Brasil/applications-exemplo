package com.raidiam.trustframework.bank.enums;

import com.raidiam.trustframework.bank.domain.ContractEntity;

public enum ResourceType {

    ACCOUNT,
    CREDIT_CARD_ACCOUNT,
    LOAN,
    FINANCING,
    UNARRANGED_ACCOUNT_OVERDRAFT,
    INVOICE_FINANCING,
    EXCHANGE,
    BANK_FIXED_INCOME,
    CREDIT_FIXED_INCOME,
    VARIABLE_INCOME,
    TREASURE_TITLE,
    FUND;

    public boolean isOfType(ContractEntity entity) {
        return this.name().equals(entity.getContractType());
    }

    @Override
    public String toString() {
        return this.name();
    }
}
