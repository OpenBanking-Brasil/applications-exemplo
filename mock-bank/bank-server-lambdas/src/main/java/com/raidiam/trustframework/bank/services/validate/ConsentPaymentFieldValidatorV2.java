package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.enums.ErrorCodesEnumV2;
import com.raidiam.trustframework.bank.services.message.PaymentErrorMessage;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;

public class ConsentPaymentFieldValidatorV2 implements PaymentConsentValidator{

    private static final int DAYS_ALLOWED_IN_FUTURE = 365;

    private PaymentErrorMessage errorMessage;
    public ConsentPaymentFieldValidatorV2(PaymentErrorMessage errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public void validate(CreatePaymentConsent request) {
        CreatePaymentConsentData data = request.getData();
        PaymentConsent payment = data.getPayment();

        // Message: Invalid payment type
        var containTypeEnum = Arrays.stream(EnumPaymentType.values()).filter(p -> p.name().equals(payment.getType())).findAny();
        if(containTypeEnum.isEmpty()){
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    errorMessage.getMessageInvalidParameter()
            );
        }
        LocalDate consentDate = payment.getDate();
        Schedule consentSchedule = payment.getSchedule();
        //Message: If Date and Schedule is sent then we return 422 NAO_INFORMADO
        if (consentDate != null && consentSchedule != null){
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name());
        }

        if (consentDate == null && consentSchedule == null){
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    ErrorCodesEnumV2.PARAMETRO_NAO_INFORMADO.name());
        }
        // Message: Invalid payment date in context, date in the past.
        //          For one-time payments, the current date must be informed.
        if (consentDate != null){
            validatePaymentDate(consentDate);
        }

        if (consentSchedule != null){
            validatePaymentSchedule(consentSchedule);
        }

        var containCreditorPersonTypeEnum = Arrays.stream(EnumCreditorPersonType.values()).filter(p -> p.toString().equals(data.getCreditor().getPersonType())).findAny();
        if(containCreditorPersonTypeEnum.isEmpty()){
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    errorMessage.getMessageInvalidParameter()
            );
        }
    }

    public void validatePaymentDate(LocalDate consentDate){
        LocalDate currentDate = new Date().toInstant().atZone(ZoneId.of("America/Sao_Paulo")).toLocalDate();
        if(consentDate.isBefore(currentDate)){
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, errorMessage.getMessageInvalidDate("Data de pagamento " +
                    "inválida no contexto, data no passado. Para pagamentos únicos deve ser informada a data atual, " +
                    "do dia corrente.")
            );
        }
    }

    private void validatePaymentSchedule(Schedule consentSchedule) {
        LocalDate scheduleDate = consentSchedule.getSingle().getDate();
        LocalDate currentDate = new Date().toInstant().atZone(ZoneId.of("America/Sao_Paulo")).toLocalDate();
        LocalDate futureDate = currentDate.plusDays(DAYS_ALLOWED_IN_FUTURE);
        if (scheduleDate.isBefore(currentDate)
                || scheduleDate.equals(currentDate)
                || scheduleDate.isAfter(futureDate)
        ){
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name());
        }
    }
}
