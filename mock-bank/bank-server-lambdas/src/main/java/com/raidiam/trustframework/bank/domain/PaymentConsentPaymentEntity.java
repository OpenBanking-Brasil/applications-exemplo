package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.PaymentConsent;
import com.raidiam.trustframework.mockbank.models.generated.PaymentConsentV4Payment;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "payment_consent_payments")
public class PaymentConsentPaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer id;

    @Column(name = "payment_type")
    private String paymentType;

    @Column(name = "payment_date")
    private Date paymentDate;

    @Column(name = "currency")
    private String currency;

    @Column(name = "amount")
    private String amount;

    @Column(name = "schedule")
    private Date schedule;

    @EqualsAndHashCode.Exclude
    @Column(name = "schedule_single_date")
    private LocalDate scheduleSingleDate;

    @EqualsAndHashCode.Exclude
    @Column(name = "schedule_daily_start_date")
    private LocalDate scheduleDailyStartDate;

    @EqualsAndHashCode.Exclude
    @Column(name = "schedule_daily_quantity")
    private Integer scheduleDailyQuantity;

    @EqualsAndHashCode.Exclude
    @Column(name = "schedule_weekly_day_of_week")
    private String scheduleWeeklyDayOfWeek;

    @EqualsAndHashCode.Exclude
    @Column(name = "schedule_weekly_start_date")
    private LocalDate scheduleWeeklyStartDate;

    @EqualsAndHashCode.Exclude
    @Column(name = "schedule_weekly_quantity")
    private Integer scheduleWeeklyQuantity;

    @EqualsAndHashCode.Exclude
    @Column(name = "schedule_monthly_start_date")
    private LocalDate scheduleMonthlyStartDate;

    @EqualsAndHashCode.Exclude
    @Column(name = "schedule_monthly_day_of_month")
    private Integer scheduleMonthlyDayOfMonth;

    @EqualsAndHashCode.Exclude
    @Column(name = "schedule_monthly_quantity")
    private Integer scheduleMonthlyQuantity;

    @EqualsAndHashCode.Exclude
    @Column(name = "schedule_custom_dates")
    private LocalDate[] scheduleCustomDates = new LocalDate[]{};

    @EqualsAndHashCode.Exclude
    @Column(name = "schedule_custom_additional_information")
    private String scheduleCustomAdditionalInformation;

    @OneToOne(cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "payment_consent_details_id", referencedColumnName = "payment_consent_details_id", nullable = true)
    private PaymentConsentDetailsEntity paymentConsentDetails;

    public static PaymentConsentPaymentEntity from(PaymentConsent paymentConsent) {
        return Optional.ofNullable(paymentConsent)
                .map(p -> {
                    if ((p.getType() != null) && (p.getDate() != null)){
                        var entity = new PaymentConsentPaymentEntity();
                        entity.setPaymentType(p.getType());
                        entity.setPaymentDate(BankLambdaUtils.localDateToDate(p.getDate()));
                        entity.setCurrency(p.getCurrency());
                        entity.setAmount(p.getAmount());
                        entity.setPaymentConsentDetails(PaymentConsentDetailsEntity.from(paymentConsent.getDetails()));
                        return entity;
                    } else if ((p.getType() != null) && (p.getSchedule() != null)){
                        var entity = new PaymentConsentPaymentEntity();
                        var single = p.getSchedule().getSingle();
                        entity.setPaymentType(p.getType());
                        entity.setSchedule(BankLambdaUtils.localDateToDate(single.getDate()));
                        entity.setCurrency(p.getCurrency());
                        entity.setAmount(p.getAmount());
                        entity.setPaymentConsentDetails(PaymentConsentDetailsEntity.from(paymentConsent.getDetails()));
                        return entity;
                    }
                    return null;
                }).orElse(null);
    }

    public static PaymentConsentPaymentEntity fromV4(PaymentConsentV4Payment paymentConsent) {
        return Optional.ofNullable(paymentConsent)
                .map(p -> {
                    if ((p.getType() != null) && (p.getSchedule() != null)){
                        var entity = new PaymentConsentPaymentEntity();
                        entity.setPaymentType(p.getType().toString());
                        entity.setCurrency(p.getCurrency());
                        entity.setAmount(p.getAmount());
                        setSchedule(p, entity);
                        entity.setPaymentConsentDetails(PaymentConsentDetailsEntity.from(paymentConsent.getDetails()));
                        return entity;
                    } else if ((p.getType() != null) && (p.getDate() != null)){
                        var entity = new PaymentConsentPaymentEntity();
                        entity.setPaymentType(p.getType().toString());
                        entity.setPaymentDate(BankLambdaUtils.localDateToDate(p.getDate()));
                        entity.setCurrency(p.getCurrency());
                        entity.setAmount(p.getAmount());
                        entity.setPaymentConsentDetails(PaymentConsentDetailsEntity.from(paymentConsent.getDetails()));
                        return entity;
                    }
                    return null;
                }).orElse(null);
    }

    private static void setSchedule(PaymentConsentV4Payment data, PaymentConsentPaymentEntity entity) {
        if (data.getSchedule() != null) {
            if (data.getSchedule().getSingle() != null) {
                entity.setScheduleSingleDate(data.getSchedule().getSingle().getDate());
            }

            if (data.getSchedule().getDaily() != null) {
                entity.setScheduleDailyStartDate(data.getSchedule().getDaily().getStartDate());
                entity.setScheduleDailyQuantity(data.getSchedule().getDaily().getQuantity());
            }

            if (data.getSchedule().getWeekly() != null) {
                entity.setScheduleWeeklyStartDate(data.getSchedule().getWeekly().getStartDate());
                entity.setScheduleWeeklyDayOfWeek(data.getSchedule().getWeekly().getDayOfWeek().toString());
                entity.setScheduleWeeklyQuantity(data.getSchedule().getWeekly().getQuantity());
            }

            if (data.getSchedule().getMonthly() != null) {
                entity.setScheduleMonthlyStartDate(data.getSchedule().getMonthly().getStartDate());
                entity.setScheduleMonthlyDayOfMonth(data.getSchedule().getMonthly().getDayOfMonth());
                entity.setScheduleMonthlyQuantity(data.getSchedule().getMonthly().getQuantity());
            }

            if (data.getSchedule().getCustom() != null && !data.getSchedule().getCustom().getDates().isEmpty()) {
                entity.setScheduleCustomDates(data.getSchedule().getCustom().getDates().toArray(LocalDate[]::new));
                entity.setScheduleCustomAdditionalInformation(data.getSchedule().getCustom().getAdditionalInformation());
            }
        }
    }
}
