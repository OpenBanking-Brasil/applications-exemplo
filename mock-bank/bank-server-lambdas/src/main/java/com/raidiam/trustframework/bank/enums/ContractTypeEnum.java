package com.raidiam.trustframework.bank.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ContractTypeEnum {
    LOAN("loans"),
    FINANCING("financings"),
    INVOICE_FINANCING("invoice-financings"),
    UNARRANGED_ACCOUNT_OVERDRAFT("unarranged-accounts-overdraft");

    private String value;

    private ContractTypeEnum(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return String.valueOf(this.value);
    }

    @JsonCreator
    public static ContractTypeEnum fromValue(String text) {
        ContractTypeEnum[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            ContractTypeEnum b = var1[var3];
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }

        return null;
    }
}
