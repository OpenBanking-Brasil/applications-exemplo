package com.raidiam.trustframework.bank.services


import com.raidiam.trustframework.bank.domain.PaymentConsentEntity
import com.raidiam.trustframework.bank.domain.PaymentConsentPaymentEntity
import com.raidiam.trustframework.bank.utils.BankLambdaUtils
import com.raidiam.trustframework.mockbank.models.generated.ScheduleWeeklyWeekly
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import javax.inject.Inject
import java.time.LocalDate
import java.time.temporal.ChronoField

@MicronautTest
class ScheduledDatesServiceSpec extends Specification {

    @Inject
    ScheduledDatesService scheduledDatesService


    @Unroll
    def "We can extract the single scheduled date"() {
        given:
        def paymentConsentEntity = new PaymentConsentEntity()
        def paymentConsentPaymentEntity = new PaymentConsentPaymentEntity()
        paymentConsentPaymentEntity.setScheduleSingleDate(date)

        paymentConsentEntity.setPaymentConsentPaymentEntity(paymentConsentPaymentEntity)

        when:
        def extractedDates = scheduledDatesService.generateScheduleDates(paymentConsentEntity)

        then:
        extractedDates.size() == 1
        extractedDates.first() == date

        where:
        date << [
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                LocalDate.now().minusYears(1)
        ]

    }


    @Unroll
    def "We can extract the daily scheduled dates"() {
        given:
        def paymentConsentEntity = new PaymentConsentEntity()
        def paymentConsentPaymentEntity = new PaymentConsentPaymentEntity()
        paymentConsentPaymentEntity.setScheduleDailyQuantity(2)
        paymentConsentPaymentEntity.setScheduleDailyStartDate(startDate)

        paymentConsentEntity.setPaymentConsentPaymentEntity(paymentConsentPaymentEntity)

        when:
        def extractedDates = scheduledDatesService.generateScheduleDates(paymentConsentEntity)

        then:
        extractedDates.size() == 2


        def minDate = extractedDates.stream().min(LocalDate::compareTo).orElse(null)
        def maxDate = extractedDates.stream().max(LocalDate::compareTo).orElse(null)

        minDate != null
        maxDate != null

        assert (minDate.dayOfMonth == expectedDate.dayOfMonth)
        assert (minDate.month == expectedDate.month)
        assert (minDate.year == expectedDate.year)

        assert (maxDate.dayOfMonth == expectedDate2.dayOfMonth)
        assert (maxDate.month == expectedDate2.month)
        assert (maxDate.year == expectedDate2.year)

        where:
        startDate                                                 | expectedDate                                           | expectedDate2
        LocalDate.now()                                           | LocalDate.now()                                        | LocalDate.now().plusDays(1)
        LocalDate.now().minusYears(1)                             | LocalDate.now()                                        | LocalDate.now().plusDays(1)
        LocalDate.now().plusYears(1)                              | LocalDate.now().plusYears(1)                           | LocalDate.now().plusYears(1).plusDays(1)
        LocalDate.now().minusYears(1).minusMonths(1)              | LocalDate.now()                                        | LocalDate.now().plusDays(1)
        LocalDate.now().plusYears(1).plusMonths(1)                | LocalDate.now().plusYears(1).plusMonths(1)             | LocalDate.now().plusYears(1).plusMonths(1).plusDays(1)
        LocalDate.now().minusYears(1).minusMonths(1).minusDays(1) | LocalDate.now()                                        | LocalDate.now().plusDays(1)
        LocalDate.now().plusYears(1).plusMonths(1).plusDays(1)    | LocalDate.now().plusYears(1).plusMonths(1).plusDays(1) | LocalDate.now().plusYears(1).plusMonths(1).plusDays(2)
    }


    @Unroll
    def "We can extract the weekly scheduled dates"() {
        given:
        def paymentConsentEntity = new PaymentConsentEntity()
        def paymentConsentPaymentEntity = new PaymentConsentPaymentEntity()
        paymentConsentPaymentEntity.setScheduleWeeklyQuantity(2)
        paymentConsentPaymentEntity.setScheduleWeeklyDayOfWeek(startDayOfWeek.name())
        paymentConsentPaymentEntity.setScheduleWeeklyStartDate(LocalDate.now()
                .withDayOfMonth(startDay)
                .withMonth(startMonth)
                .withYear(startYear))

        paymentConsentEntity.setPaymentConsentPaymentEntity(paymentConsentPaymentEntity)

        def expectedDate = LocalDate.now()
                .withDayOfMonth(startDay)
                .withMonth(startMonth)
                .withYear(startYear)

        expectedDate = expectedDate.isBefore(LocalDate.now()) ? LocalDate.now() : expectedDate
        def scheduleDayOfWeek = BankLambdaUtils.getPaymentScheduleWeeklyOrdinal(startDayOfWeek.toString())

        if (expectedDate.getDayOfWeek().getValue() > scheduleDayOfWeek) {
            expectedDate = expectedDate.plusDays(7 - Math.abs(expectedDate.getDayOfWeek().getValue() - scheduleDayOfWeek))
        } else {
            expectedDate = expectedDate.plusDays(scheduleDayOfWeek - expectedDate.getDayOfWeek().getValue())
        }

        when:
        def extractedDates = scheduledDatesService.generateScheduleDates(paymentConsentEntity)

        then:
        extractedDates.size() == 2

        def minDate = extractedDates.stream().min(LocalDate::compareTo).orElse(null)
        def maxDate = extractedDates.stream().max(LocalDate::compareTo).orElse(null)

        minDate != null
        maxDate != null

        assert (minDate.dayOfMonth == expectedDate.dayOfMonth)
        assert (minDate.month == expectedDate.month)
        assert (minDate.year == expectedDate.year)
        assert (minDate.dayOfWeek.get(ChronoField.DAY_OF_WEEK) == scheduleDayOfWeek)

        assert (maxDate.dayOfMonth == expectedDate.plusDays(7).dayOfMonth)
        assert (maxDate.month == expectedDate.plusDays(7).month)
        assert (maxDate.year == expectedDate.plusDays(7).year)
        assert (maxDate.dayOfWeek.get(ChronoField.DAY_OF_WEEK) == scheduleDayOfWeek)


        where:
        startDay | startMonth | startYear | startDayOfWeek
        21       | 11         | 2023      | ScheduleWeeklyWeekly.DayOfWeekEnum.SEGUNDA_FEIRA
        28       | 11         | 2023      | ScheduleWeeklyWeekly.DayOfWeekEnum.SEGUNDA_FEIRA
        28       | 11         | 2023      | ScheduleWeeklyWeekly.DayOfWeekEnum.TERCA_FEIRA
        28       | 11         | 2023      | ScheduleWeeklyWeekly.DayOfWeekEnum.SEXTA_FEIRA
        28       | 11         | 2023      | ScheduleWeeklyWeekly.DayOfWeekEnum.DOMINGO
        01       | 12         | 2023      | ScheduleWeeklyWeekly.DayOfWeekEnum.QUINTA_FEIRA
    }

    @Unroll
    def "We can extract the monthly scheduled dates"() {
        given:
        def paymentConsentEntity = new PaymentConsentEntity()
        def paymentConsentPaymentEntity = new PaymentConsentPaymentEntity()
        paymentConsentPaymentEntity.setScheduleMonthlyQuantity(scheduleQuantity)
        paymentConsentPaymentEntity.setScheduleMonthlyDayOfMonth(scheduledDayOfMonth)
        paymentConsentPaymentEntity.setScheduleMonthlyStartDate(startDate)

        paymentConsentEntity.setPaymentConsentPaymentEntity(paymentConsentPaymentEntity)

        when:
        def extractedDates = scheduledDatesService.generateScheduleDates(paymentConsentEntity)


        var correctedExpectedDate = scheduledDayOfMonth > expectedDate.lengthOfMonth() ? expectedDate.plusMonths(1).withDayOfMonth(1) : expectedDate
        var correctedExpectedDate2 = scheduledDayOfMonth > expectedDate2.lengthOfMonth() ? expectedDate2.plusMonths(1).withDayOfMonth(1) : expectedDate2
        correctedExpectedDate2 = correctedExpectedDate.isBefore(LocalDate.now()) ? correctedExpectedDate2.plusMonths(1) : correctedExpectedDate2
        correctedExpectedDate = correctedExpectedDate.isBefore(LocalDate.now()) ? correctedExpectedDate.plusMonths(1) : correctedExpectedDate

        then:
        extractedDates.size() == scheduleQuantity
        def minDate = extractedDates.stream().min(LocalDate::compareTo).orElse(null)
        def maxDate = extractedDates.stream().max(LocalDate::compareTo).orElse(null)

        minDate != null
        maxDate != null

        assert (minDate.dayOfMonth == correctedExpectedDate.dayOfMonth)
        assert (minDate.month == correctedExpectedDate.month)
        assert (minDate.year == correctedExpectedDate.year)

        assert (maxDate.dayOfMonth == correctedExpectedDate2.dayOfMonth)
        assert (maxDate.month == correctedExpectedDate2.month)
        assert (maxDate.year == correctedExpectedDate2.year)

        where:
        startDate                                                       | scheduleQuantity  | scheduledDayOfMonth              | expectedDate                                                       | expectedDate2
        LocalDate.now()                                                 | 2                 | LocalDate.now().dayOfMonth       | LocalDate.now()                                                    | LocalDate.now().plusMonths(1)
        LocalDate.now().minusDays(1)                                    | 2                 | LocalDate.now().dayOfMonth       | LocalDate.now()                                                    | LocalDate.now().plusMonths(1)
        LocalDate.now().plusDays(1)                                     | 2                 | LocalDate.now().dayOfMonth       | LocalDate.now().plusMonths(1)                                      | LocalDate.now().plusMonths(2)
        LocalDate.now().plusMonths(1)                                   | 2                 | LocalDate.now().dayOfMonth       | LocalDate.now().plusMonths(1)                                      | LocalDate.now().plusMonths(2)
        LocalDate.now().plusMonths(1)                                   | 3                 | LocalDate.now().dayOfMonth       | LocalDate.now().plusMonths(1)                                      | LocalDate.now().plusMonths(3)
        LocalDate.now().minusMonths(1)                                  | 2                 | LocalDate.now().dayOfMonth       | LocalDate.now()                                                    | LocalDate.now().plusMonths(1)
        LocalDate.now().plusMonths(1).plusDays(1)                       | 2                 | LocalDate.now().dayOfMonth       | LocalDate.now().plusMonths(2)                                      | LocalDate.now().plusMonths(3)
        LocalDate.now().minusMonths(1).minusDays(1)                     | 2                 | LocalDate.now().dayOfMonth       | LocalDate.now()                                                    | LocalDate.now().plusMonths(1)
        LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()) | 2                 | LocalDate.now().lengthOfMonth()  | LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())    | LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).plusMonths(1)
        LocalDate.now()                                                 | 2                 | 16                               | LocalDate.now().withDayOfMonth(16)                                 | LocalDate.now().plusMonths(1).withDayOfMonth(16)
        LocalDate.of(LocalDate.now().getYear()+1, 1, 31)                | 2                 | 31                               | LocalDate.of(LocalDate.now().getYear()+1, 1, 31)                   | LocalDate.of(LocalDate.now().getYear()+1, 3, 1)
        LocalDate.of(LocalDate.now().getYear()+1, 2, 28)                | 2                 | 31                               | LocalDate.of(LocalDate.now().getYear()+1, 3, 1)                    | LocalDate.of(LocalDate.now().getYear()+1, 3, 31)
        LocalDate.of(LocalDate.now().getYear()+1, 2, 28)                | 3                 | 31                               | LocalDate.of(LocalDate.now().getYear()+1, 3, 1)                    | LocalDate.of(LocalDate.now().getYear()+1, 5, 1)
        LocalDate.of(LocalDate.now().getYear()+1, 1, 24)                | 3                 | 30                               | LocalDate.of(LocalDate.now().getYear()+1, 1, 30)                   | LocalDate.of(LocalDate.now().getYear()+1, 3, 30)
        LocalDate.of(LocalDate.now().getYear()+1, 3, 24)                | 3                 | 31                               | LocalDate.of(LocalDate.now().getYear()+1, 3, 31)                   | LocalDate.of(LocalDate.now().getYear()+1, 5, 31)
    }

    @Unroll
    def "We can extract custom scheduled dates"() {
        given:
        def paymentConsentEntity = new PaymentConsentEntity()
        def paymentConsentPaymentEntity = new PaymentConsentPaymentEntity()
        paymentConsentPaymentEntity.setScheduleCustomDates(dates)

        paymentConsentEntity.setPaymentConsentPaymentEntity(paymentConsentPaymentEntity)

        when:
        def extractedDates = scheduledDatesService.generateScheduleDates(paymentConsentEntity)

        then:
        extractedDates.size() == dates.size()
        extractedDates.containsAll(dates)

        where:
        dates << [
                new LocalDate[]{LocalDate.now(), LocalDate.now().plusYears(1)},
                new LocalDate[]{LocalDate.now(), LocalDate.now().minusYears(1)}
        ]

    }


}
