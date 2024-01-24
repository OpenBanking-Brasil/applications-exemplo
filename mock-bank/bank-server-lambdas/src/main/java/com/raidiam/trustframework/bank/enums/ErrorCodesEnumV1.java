package com.raidiam.trustframework.bank.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ErrorCodesEnumV1 {
    COBRANCA_INVALIDA("Cobrança inválida."),
    DETALHE_PGTO_INVALIDO("Detalhe do pagamento inválido."),
    DATA_PGTO_INVALIDA("Data de pagamento inválida."),
    PAGAMENTO_DIVERGENTE_DO_CONSENTIMENTO("Pagamento divergente do consentimento"),
    CONSENTIMENTO_INVALIDO("Consentimento inválido."),
    VALOR_INCOMPATIVEL("Valor da transação incompatível."),
    NAO_INFORMADO("Não informado."),
    FORMA_PGTO_INVALIDA("Forma de pagamento inválida."),
    PARAMETRO_INVALIDO("Parametro Inválido."),
    VALOR_INVALIDO("O valor enviado não é válido para o QR Code informado."),
    PARAMETRO_NAO_INFORMADO("Parametro não informado.");

    private final String title;

    ErrorCodesEnumV1(String title) {
        this.title = title;
    }
    @JsonValue
    @Override
    public String toString() {
        return this.name();
    }

    public String getTitle(){
        return title;
    }
}
