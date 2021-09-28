package com.raidiam.trustframework.bank.domain;

public enum LinkedAccountType {

    BANK_ACCOUNT,
    CREDIT_CARD,
    LOAN,
    FINANCING
    ;

    public boolean isOfType(ConsentAccountIdEntity entity) {
        return entity.getAccountType() == this;
    }

}
