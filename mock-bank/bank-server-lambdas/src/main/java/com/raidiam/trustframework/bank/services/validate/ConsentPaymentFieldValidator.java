package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

import java.time.LocalDate;
import java.util.Date;

public class ConsentPaymentFieldValidator implements PaymentConsentValidator{

    private static final String DETAIL_LOCAL_INSTRUMENT_PROXY = "DATA_PGTO_INVALIDA: O campo proxy não preenche os requisitos de preenchimento. proxy não deve estar presente para o localInstrument ";
    private static final String DETAIL_LOCAL_INSTRUMENT_PROXY_REQUIRED = "DATA_PGTO_INVALIDA: O campo proxy não preenche os requisitos de preenchimento. proxy deve estar presente para o localInstrument ";
    private static final String DETAIL_INSTRUMENT_QR = "DATA_PGTO_INVALIDA: O campo qrCode não preenche os requisitos de preenchimento. qrCode não deve estar presente para o localInstrument ";
    private static final String DETAIL_INSTRUMENT_QR_REQUIRED = "DATA_PGTO_INVALIDA: O campo qrCode não preenche os requisitos de preenchimento. qrCode deve estar presente para o localInstrument ";
    private static final String NAO_INFORMADO = "NAO_INFORMADO";
    private static final String INVALID_SCHEDULE = "INVALID_SCHEDULE";
    private static final int DAYS_ALLOWED_IN_FUTURE = 365;



    @Override
    public void validate(CreatePaymentConsent request) {
        CreatePaymentConsentData data = request.getData();
        PaymentConsent payment = data.getPayment();

        // Message: Invalid payment type
        if(!typeEnumContains(payment.getType())){
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "FORMA_PGTO_INVALIDA: Meio de pagamento inválido."
            );
        }
        LocalDate consentDate = payment.getDate();
        Schedule consentSchedule = payment.getSchedule();
        //Message: If Date and Schedule is sent then we return 422 NAO_INFORMADO
        if (consentDate != null && consentSchedule != null){
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    NAO_INFORMADO);
        }
        // Message: Invalid payment date in context, date in the past.
        //          For one-time payments, the current date must be informed.
        if (consentDate != null){
            validatePaymentDate(consentDate);
        }

        if (consentSchedule != null){
            validatePaymentSchedule(consentSchedule);

        }

        switch(payment.getDetails().getLocalInstrument().toString()) {
            case "MANU":
                manuValidator(payment);
                break;
            case "DICT":
                dictValidator(payment);
                break;
            case "QRDN":
                qrdnValidator(payment);
                break;
            case "QRES":
                qresValidator(payment);
                break;
            case "INIC":
                inicValidator(payment);
                break;
            default:
                // Message: The field localInstrument does not fulfill the filling in requirements.
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "DETALHE_PGTO_INVALIDO: O campo " +
                        "localInstrument não atende os requisitos de preenchimento."
                );
        }

        if (!accountTypeEnumContains(data.getCreditor().getPersonType())) {
            // Message: The field creditorAccount - accountType does not fulfill the filling in requirements.
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "DETALHE_PGTO_INVALIDO: O campo " +
                    "creditorAccount - accountType não atende os requisitos de preenchimento."
            );
        }

    }

    private void inicValidator(PaymentConsent payment){
        String qrCode = payment.getDetails().getQrCode();
        String proxy = payment.getDetails().getProxy();

        if(qrCode != null){
            // Message: The field qrCode does not fulfill the filling in requirements.
            // qrCode should not be present for localInstrument of type INIC.
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, DETAIL_INSTRUMENT_QR + "do tipo INIC.");
        }

        if(proxy == null){
            // Message: The field proxy does not fulfill the filling in requirements.
            // proxy must be present for localInstrument of type INIC.
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, DETAIL_LOCAL_INSTRUMENT_PROXY_REQUIRED + "do tipo INIC.");
        }
    }
    private void manuValidator(PaymentConsent payment){
        String qrCode = payment.getDetails().getQrCode();
        String proxy = payment.getDetails().getProxy();

        if(qrCode != null){
            // Message: The field qrCode does not fulfill the filling in requirements.
            // qrCode should not be present for localInstrument of type MANU.
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, DETAIL_INSTRUMENT_QR + "do tipo MANU.");
        }
        if(proxy != null){
            // Message: The field proxy does not fulfill the filling in requirements.
            // proxy should not be present for localInstrument of type MANU
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, DETAIL_LOCAL_INSTRUMENT_PROXY + "do tipo MANU.");
        }
    }

    private void dictValidator(PaymentConsent payment){
        String qrCode = payment.getDetails().getQrCode();
        String proxy = payment.getDetails().getProxy();

        if(qrCode != null){
            // Message: The field qrCode does not fulfill the filling in requirements.
            // qrCode should not be present for localInstrument of type DICT.
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, DETAIL_INSTRUMENT_QR + "do tipo DICT.");
        }

        if(proxy == null){
            // Message: The field proxy does not fulfill the filling in requirements.
            // proxy must be present for localInstrument of type DICT.
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, DETAIL_LOCAL_INSTRUMENT_PROXY_REQUIRED + "do tipo DICT.");
        }
    }

    private void qrdnValidator(PaymentConsent payment){
        String qr = payment.getDetails().getQrCode();
        String proxy = payment.getDetails().getProxy();

        if(proxy == null){
            // Message: The field proxy does not fulfill the filling in requirements.
            // proxy must be present for localInstrument of type QRDN.
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, DETAIL_LOCAL_INSTRUMENT_PROXY_REQUIRED + "do tipo QRDN.");
        }

        if(qr == null){
            // Message: The field proxy does not fulfill the filling in requirements.
            // qrdCode must be present for localInstrument of type QRDN.
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, DETAIL_INSTRUMENT_QR_REQUIRED + "do tipo QRDN.");
        }
    }

    private void qresValidator(PaymentConsent payment){
        String qr = payment.getDetails().getQrCode();
        String proxy = payment.getDetails().getProxy();

        if(proxy == null){
            // Message: The field proxy does not fulfill the filling in requirements.
            // proxy must be present for localInstrument of type QRES.
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, DETAIL_LOCAL_INSTRUMENT_PROXY_REQUIRED + "do tipo QRES.");
        }

        if(qr == null){
            // Message: The field proxy does not fulfill the filling in requirements.
            // qrdCode must be present for localInstrument of type QRDN.
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, DETAIL_INSTRUMENT_QR_REQUIRED + "do tipo QRES.");
        }
    }

    private boolean typeEnumContains(String type){
        for( EnumPaymentType enumValue : EnumPaymentType.values()){
            if(enumValue.toString().equals(type)){
                return true;
            }
        }
        return false;
    }

    private boolean accountTypeEnumContains(String type){
        for(EnumCreditorPersonType enumValue : EnumCreditorPersonType.values()){
            if (enumValue.toString().equals(type)){
                return true;
            }
        }
        return false;
    }

    public void validatePaymentDate(LocalDate consentDate){
        Date dateOfToday = new Date();
        LocalDate currentDate = dateOfToday.toInstant().atZone(BankLambdaUtils.getBrasilZoneId()).toLocalDate();
        if(consentDate.isBefore(currentDate)){
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "DATA_PGTO_INVALIDA: Data de pagamento " +
                    "inválida no contexto, data no passado. Para pagamentos únicos deve ser informada a data atual, " +
                    "do dia corrente."
            );
        }
    }

    private void validatePaymentSchedule(Schedule consentSchedule) {
        LocalDate scheduleDate = consentSchedule.getSingle().getDate();
        Date dateOfToday = new Date();
        LocalDate currentDate = dateOfToday.toInstant().atZone(BankLambdaUtils.getBrasilZoneId()).toLocalDate();
        LocalDate futureDate = currentDate.plusDays(DAYS_ALLOWED_IN_FUTURE);
        if (scheduleDate.isBefore(currentDate)
                || scheduleDate.equals(currentDate)
                || scheduleDate.isAfter(futureDate)
        ){
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, INVALID_SCHEDULE);
        }
    }
}
