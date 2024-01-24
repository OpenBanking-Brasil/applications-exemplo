package com.raidiam.trustframework.bank.services.validate;

import com.emv.qrcode.decoder.mpm.DecoderMpm;
import com.emv.qrcode.model.mpm.MerchantPresentedMode;
import com.raidiam.trustframework.mockbank.models.generated.EnumLocalInstrument;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

public abstract class QrCodeValidationAbstract {
    protected void validateData(EnumLocalInstrument localInstrument, String qrCode, String proxy) {
        if(localInstrument.equals(EnumLocalInstrument.QRDN) || localInstrument.equals(EnumLocalInstrument.QRES)) {
            try {
                DecoderMpm.decode(qrCode, MerchantPresentedMode.class);
            } catch (RuntimeException e) {
                String message = "PARAMETRO_NAO_INFORMADO: Parâmetro não informado.";
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, message);
            }
        } else if(localInstrument.equals(EnumLocalInstrument.MANU)
                && (proxy != null
                || qrCode != null ) ||
                (localInstrument.equals(EnumLocalInstrument.DICT) &&
                        qrCode != null)) {
            String message = "DETALHE_PAGAMENTO_INVALIDO: Parâmetro não informado.";
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, message);
        }
    }
}
