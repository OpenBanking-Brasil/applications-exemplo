package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.domain.PaymentConsentEntity;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;

import javax.inject.Singleton;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.*;

@Singleton
public class ScheduledDatesService {


    public Set<LocalDate> generateScheduleDates(PaymentConsentEntity paymentConsentEntity) {
        var paymentConsentPaymentEntity = paymentConsentEntity.getPaymentConsentPaymentEntity();
        List<LocalDate> dates = new ArrayList<>();

        if (paymentConsentPaymentEntity.getScheduleSingleDate() != null) {
            dates.add(paymentConsentPaymentEntity.getScheduleSingleDate());

        } else if (paymentConsentPaymentEntity.getScheduleDailyStartDate() != null) {
            LocalDate startDate = getEffectiveStartDate(paymentConsentPaymentEntity.getScheduleDailyStartDate());
            int quantity = paymentConsentPaymentEntity.getScheduleDailyQuantity();
            for (int i = 0; i < quantity; i++) {
                dates.add(startDate.plusDays(i));
            }

        } else if (paymentConsentPaymentEntity.getScheduleWeeklyStartDate() != null) {
            long scheduleDayOfWeek = BankLambdaUtils.getPaymentScheduleWeeklyOrdinal(paymentConsentPaymentEntity.getScheduleWeeklyDayOfWeek());
            LocalDate startDate = getEffectiveWeeklyStartDate(paymentConsentPaymentEntity.getScheduleWeeklyStartDate(), scheduleDayOfWeek);
            int quantity = paymentConsentPaymentEntity.getScheduleWeeklyQuantity();
            for (int i = 0; i < quantity; i++) {
                dates.add(startDate.plusWeeks(i));
            }

        } else if (paymentConsentPaymentEntity.getScheduleMonthlyStartDate() != null) {
            int scheduledDate = paymentConsentPaymentEntity.getScheduleMonthlyDayOfMonth();
            LocalDate startDate = getEffectiveMonthlyStartDate(paymentConsentPaymentEntity.getScheduleMonthlyStartDate(), scheduledDate);
            int quantity = paymentConsentPaymentEntity.getScheduleMonthlyQuantity();
            for (int i = 0; i < quantity; i++) {
                if (i > 0) {
                    if (dates.get(i-1).getDayOfMonth() == 1) {
                        if (startDate.minusMonths(1).lengthOfMonth() <= scheduledDate) {
                            startDate = dates.get(i-1).withDayOfMonth(scheduledDate);
                            dates.add(startDate);
                        } else if (startDate.lengthOfMonth() <= scheduledDate) {
                            startDate = startDate.withDayOfMonth(scheduledDate);
                            dates.add(startDate);
                        }
                    } else if (startDate.plusMonths(1).getDayOfMonth() == startDate.plusMonths(1).lengthOfMonth() && startDate.plusMonths(1).lengthOfMonth() < scheduledDate) {
                        startDate = startDate.withDayOfMonth(1).plusMonths(2L);
                        dates.add(startDate);
                    } else {
                        startDate = startDate.plusMonths(1);
                        dates.add(startDate);
                    }
                } else {
                    dates.add(startDate);
                }
            }

        } else if (paymentConsentPaymentEntity.getScheduleCustomDates() != null) {
            LocalDate[] customDates = paymentConsentPaymentEntity.getScheduleCustomDates();
            dates.addAll(Arrays.asList(customDates));
        }

        return new TreeSet<>(dates);
    }

    public LocalDate getEffectiveStartDate(LocalDate startDate) {
        return startDate.isBefore(LocalDate.now()) ? LocalDate.now() : startDate;
    }

    public LocalDate getEffectiveWeeklyStartDate(LocalDate startDate, long scheduleDayOfWeek) {
        startDate = getEffectiveStartDate(startDate);
        if (startDate.getDayOfWeek().getValue() > scheduleDayOfWeek) {
            startDate = startDate.plusDays(7 - Math.abs(startDate.getDayOfWeek().getValue() - scheduleDayOfWeek));
        } else {
            startDate = startDate.plusDays(scheduleDayOfWeek - startDate.getDayOfWeek().getValue());
        }
        return startDate;
    }

    public LocalDate getEffectiveMonthlyStartDate(LocalDate startDate, int scheduledDayOfMonth) {
        startDate = getEffectiveStartDate(startDate);
        if (startDate.getDayOfMonth() > scheduledDayOfMonth) {
            startDate = startDate.plusMonths(1L);
        }
        try {
            return startDate.withDayOfMonth(scheduledDayOfMonth);
        } catch (DateTimeException e) {
            return startDate.plusMonths(1L).withDayOfMonth(1);
        }
    }

}
