package com.raidiam.trustframework.bank.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ErrorCodesEnumV2 {
    VALOR_INVALIDO("Valor inválido."),
    DETALHE_PAGAMENTO_INVALIDO("Detalhe do pagamento inválido."),
    FORMA_PAGAMENTO_INVALIDA("Forma do pagamento inválido."),
    PAGAMENTO_DIVERGENTE_CONSENTIMENTO("Divergência entre pagamento e consentimento"),
    CONSENTIMENTO_INVALIDO("Consentimento inválido."),
    VALOR_INCOMPATIVEL("Valor da transação incompatível."),
    NAO_INFORMADO("Não informado."),
    ERRO_IDEMPOTENCIA("Erro idempotência."),
    PARAMETRO_INVALIDO("Parametro Inválido."),
    DATA_PAGAMENTO_INVALIDA ("Data de pagamento inválida."),
    PARAMETRO_NAO_INFORMADO ("Parametro não informado."),
    PAGAMENTO_NAO_PERMITE_CANCELAMENTO ("Pagamento não permite Cancelamento.");

    private String title;

    private ErrorCodesEnumV2(String title) {
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
