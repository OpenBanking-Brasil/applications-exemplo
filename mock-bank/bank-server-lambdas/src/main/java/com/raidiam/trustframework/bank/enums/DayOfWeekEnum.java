package com.raidiam.trustframework.bank.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DayOfWeekEnum {
    SEGUNDA_FEIRA("SEGUNDA_FEIRA"),
    TERCA_FEIRA("TERCA_FEIRA"),
    QUARTA_FEIRA("QUARTA_FEIRA"),
    QUINTA_FEIRA("QUINTA_FEIRA"),
    SEXTA_FEIRA("SEXTA_FEIRA"),
    SABADO("SABADO"),
    DOMINGO("DOMINGO");

    private String value;

    private DayOfWeekEnum(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return String.valueOf(this.value);
    }

    @JsonCreator
    public static DayOfWeekEnum fromValue(String text) {
        DayOfWeekEnum[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            DayOfWeekEnum b = var1[var3];
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }

        return null;
    }
}
