package com.raidiam.trustframework.bank.services.validate;

import com.google.common.base.Strings;
import com.raidiam.trustframework.bank.enums.DayOfWeekEnum;
import com.raidiam.trustframework.bank.services.ScheduledDatesService;
import com.raidiam.trustframework.bank.services.message.PaymentErrorMessage;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class ConsentPaymentFieldValidatorV4 implements PaymentConsentValidatorV4{

    private static final Logger LOG = LoggerFactory.getLogger(ConsentPaymentFieldValidatorV4.class);

    private final PaymentErrorMessage errorMessage;
    private final ScheduledDatesService scheduledDatesService;

    public ConsentPaymentFieldValidatorV4(PaymentErrorMessage errorMessage, ScheduledDatesService scheduledDatesService) {
        this.errorMessage = errorMessage;
        this.scheduledDatesService = scheduledDatesService;
    }

    @Override
    public void validate(CreatePaymentConsentV4 request) {
        LOG.info("Started Payment Consent Field validation");
        CreatePaymentConsentV4Data data = request.getData();
        PaymentConsentV4Payment payment = data.getPayment();

        // Message: Invalid payment type
        var containTypeEnum = Arrays.stream(EnumPaymentType.values()).filter(p -> p == payment.getType()).findAny();
        if(containTypeEnum.isEmpty()){
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    errorMessage.getMessageInvalidParameter()
            );
        }
        LocalDate consentDate = payment.getDate();
        var consentSchedule = payment.getSchedule();
        //Message: If Date and Schedule is sent then we return 422 NAO_INFORMADO
        if (consentDate != null && consentSchedule != null){
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    errorMessage.getMessageInvalidDate("Ambas data de consentimento e data de agendamento não podem estar preenchidas"));

        }

        if (consentDate == null && consentSchedule == null){
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    errorMessage.getParameterNotInformed("Ambas data de consentimento e data de agendamento não podem estar vazias"));
        }
        // Message: Invalid payment date in context, date in the past.
        //          For one-time payments, the current date must be informed.
        if (consentDate != null){
            validatePaymentDate(consentDate);
        }

        if (consentSchedule != null){
            validatePaymentScheduleV4(consentSchedule);
        }

        var containCreditorPersonTypeEnum = Arrays.stream(EnumCreditorPersonType.values()).filter(p -> p.toString().equals(data.getCreditor().getPersonType())).findAny();
        if (containCreditorPersonTypeEnum.isEmpty()) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    errorMessage.getMessageInvalidParameter()
            );
        }
        LOG.info("Finished Payment Consent Field validation");
    }

    public void validatePaymentDate(LocalDate consentDate){
        LocalDate currentDate = new Date().toInstant().atZone(BankLambdaUtils.getBrasilZoneId()).toLocalDate();
        if(consentDate.isBefore(currentDate)){
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, errorMessage.getMessageInvalidDate("Data de pagamento " +
                    "inválida no contexto, data no passado. Para pagamentos únicos deve ser informada a data atual, " +
                    "do dia corrente.")
            );
        }
    }

    private void validatePaymentScheduleV4(AllOfPaymentConsentV4PaymentSchedule schedule) {
        LocalDate currentDate = new Date().toInstant().atZone(BankLambdaUtils.getBrasilZoneId()).toLocalDate();
        if (schedule.getSingle() != null) {
            ScheduleSingleSingle single = schedule.getSingle();
            LocalDate singleDate = Optional.ofNullable(single.getDate()).orElseThrow(() -> new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, errorMessage.getParameterNotInformed("")));
            if (singleDate.isBefore(currentDate.plusDays(1)) || singleDate.isAfter(currentDate.plusDays(365))) {
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, errorMessage.getMessageInvalidDate("Data de agendamento inválida"));
            }
        }

        if (schedule.getDaily() != null) {
            ScheduleDailyDaily daily = schedule.getDaily();
            validateMaximumQuantity(daily.getQuantity());
            LocalDate startDate = daily.getStartDate();
            int quantity = daily.getQuantity();
            LocalDate consentDate = startDate.plusDays(quantity);
            checkScheduleParams(startDate, consentDate, currentDate, quantity);
        }

        if (schedule.getWeekly() != null) {
            ScheduleWeeklyWeekly weekly = schedule.getWeekly();
            validateMaximumQuantity(weekly.getQuantity());
            LocalDate startDate = weekly.getStartDate();
            int quantity = weekly.getQuantity();
            LocalDate effectiveStartDate = scheduledDatesService.getEffectiveWeeklyStartDate(startDate, BankLambdaUtils.getPaymentScheduleWeeklyOrdinal(weekly.getDayOfWeek().toString()));
            LocalDate consentDate = effectiveStartDate.plusWeeks(quantity);
            checkScheduleParams(startDate, consentDate, currentDate, quantity);
            if (!EnumUtils.isValidEnum(DayOfWeekEnum.class, weekly.getDayOfWeek().toString())) {
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, errorMessage.getMessageInvalidDate("Data de agendamento inválida - Dia da semana inválido"));
            }
        }

        if (schedule.getMonthly() != null) {
            ScheduleMonthlyMonthly monthly = schedule.getMonthly();
            LocalDate startDate = monthly.getStartDate();
            LocalDate effectiveStartDate = scheduledDatesService.getEffectiveMonthlyStartDate(startDate, monthly.getDayOfMonth());
            int quantity = monthly.getQuantity();
            LocalDate consentDate = effectiveStartDate.plusMonths(quantity);
            checkScheduleParams(startDate, consentDate, currentDate, quantity);
        }

        if (schedule.getCustom() != null) {
            ScheduleCustomCustom custom = schedule.getCustom();
            validateMaximumQuantity(custom.getDates().size());
            List<LocalDate> customDates = custom.getDates();
            for (LocalDate date : customDates) {
                if (date.isBefore(currentDate) || date.isAfter(currentDate.plusYears(2))) {
                    throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, errorMessage.getMessageInvalidDate("Data de consentimento tem que ser no máximo até 2 anos no futuro"));
                }
            }
            if (Strings.isNullOrEmpty(custom.getAdditionalInformation())) {
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, errorMessage.getMessageInvalidDate("Data de agendamento inválida - Informações adicionai não informadas"));
            }

            if (customDates.size() < 2) {
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, errorMessage.getMessageInvalidParameter("There must be more than one custom date scheduled in the request"));
            }

            if(customDates.size() != customDates.stream().distinct().count()){
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, errorMessage.getMessageInvalidParameter("All custom dates must be unique"));
            }
        }
    }

    private void validateMaximumQuantity(Integer quantity) {
        if(quantity > 60) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, errorMessage.getMessageInvalidParameter("Quantidade acima do limite máximo"));
        }
    }

    private void checkScheduleParams(LocalDate startDate, LocalDate consentDate, LocalDate currentDate, int quantity) {

        if (startDate.isBefore(currentDate)) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, errorMessage.getMessageInvalidDate("Data de agendamento inválida - Data no passado"));
        }
        if (quantity < 1) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, errorMessage.getMessageInvalidDate("Data de agendamento inválida - Quantidade menor que um"));
        }
        if (consentDate.isAfter(currentDate.plusYears(2))) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, errorMessage.getMessageInvalidDate("Data de consentimento tem que ser no máximo até 2 anos no futuro"));
        }
    }
}
