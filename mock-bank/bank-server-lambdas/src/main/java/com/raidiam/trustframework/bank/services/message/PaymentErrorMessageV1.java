package com.raidiam.trustframework.bank.services.message;

import com.raidiam.trustframework.bank.enums.ErrorCodesEnumV1;

public class PaymentErrorMessageV1 implements PaymentErrorMessage {

    @Override
    public String getMessageConsentConsumed() {
        return String.format("%s: Consentimento inválido - consentimento encontrado, mas que expirou", ErrorCodesEnumV1.CONSENTIMENTO_INVALIDO.name());
    }

    @Override
    public String getMessageTransactionIdentifier() {
        return String.format("%s: %s", ErrorCodesEnumV1.NAO_INFORMADO.name(), "Não reportado/identificado pela instituição detentora de conta. CNPJ não registrado.");
    }

    @Override
    public String getMessagePaymentDivergent(String detail) {
        return String.format("%s: %s", ErrorCodesEnumV1.PAGAMENTO_DIVERGENTE_DO_CONSENTIMENTO.name(), detail);
    }

    @Override
    public String getMessageInvalidConsent(String detail) {
        return String.format("%s: %s", ErrorCodesEnumV1.CONSENTIMENTO_INVALIDO.name(), detail);
    }

    @Override
    public String getMessageValueNotCompatible(String detail) {
        return String.format("%s: %s", ErrorCodesEnumV1.VALOR_INCOMPATIVEL.name(), detail);
    }

    @Override
    public String getMessageNotInformed() {
        return String.format("%s: %s", ErrorCodesEnumV1.NAO_INFORMADO.name(), "Não reportado/identificado pela instituição detentora de conta. CNPJ não registrado.");
    }

    @Override
    public String getMessageVersionDiff() {
        return String.format("%s: %s", ErrorCodesEnumV1.NAO_INFORMADO.name(), "Received request with the same x-idempotency-key and the JWT claim data with content different from the original");
    }

    @Override
    public String getMessagePaymentDetailInvalid(String detail) {
        return String.format("%s: %s", ErrorCodesEnumV1.DETALHE_PGTO_INVALIDO.name(), detail);
    }

    @Override
    public String getMessageInvalidParameter() {
        return String.format("%s: %s", ErrorCodesEnumV1.PARAMETRO_INVALIDO.name(), "Parametro Inválido.");
    }

    @Override
    public String getMessageInvalidParameter(String message) {
        return String.format("%s: %s", ErrorCodesEnumV1.PARAMETRO_INVALIDO.name(), message);
    }

    @Override
    public String getMessageInvalidCurrency() {
        return String.format("%s: O campo currency não atende os requisitos de preenchimento. O valor dado não é uma moeda válida.", ErrorCodesEnumV1.DETALHE_PGTO_INVALIDO.name());
    }
    @Override
    public String getMessageInvalidDate(String detail) {
        return String.format("%s: %s", ErrorCodesEnumV1.DATA_PGTO_INVALIDA.name(), detail);
    }

    @Override
    public String getMessageInvalidValue(String detail) {
        return String.format("%s: %s", ErrorCodesEnumV1.VALOR_INVALIDO.name(), detail);
    }

    @Override
    public String getParameterNotInformed(String message) {
        return String.format("%s: %s", ErrorCodesEnumV1.PARAMETRO_NAO_INFORMADO.name(), message);
    }
}
