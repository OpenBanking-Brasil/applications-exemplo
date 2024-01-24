package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.envers.Audited;

import javax.persistence.*;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Accessors(chain = true)
@Table(name = "periodic_limits")
public class PeriodicLimitsEntity extends BaseEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "periodic_limits_day_quantity_limit")
    private int periodicLimitsDayQuantityLimit;

    @Column(name = "periodic_limits_day_transaction_limit")

    private String periodicLimitsDayTransactionLimit;

    @Column(name = "periodic_limits_week_quantity_limit")
    private int periodicLimitsWeekQuantityLimit;

    @Column(name = "periodic_limits_week_transaction_limit")
    private String periodicLimitsWeekTransactionLimit;

    @Column(name = "periodic_limits_month_quantity_limit")
    private int periodicLimitsMonthQuantityLimit;

    @Column(name = "periodic_limits_month_transaction_limit")
    private String periodicLimitsMonthTransactionLimit;

    @Column(name = "periodic_limits_year_quantity_limit")
    private int periodicLimitsYearQuantityLimit;

    @Column(name = "periodic_limits_year_transaction_limit")
    private String periodicLimitsYearTransactionLimit;


    public static PeriodicLimitsEntity from(PeriodicLimits periodicLimits) {
        var periodicLimitEntity = new PeriodicLimitsEntity();
        if (periodicLimits.getDay() != null) {
            periodicLimitEntity.setPeriodicLimitsDayQuantityLimit(periodicLimits.getDay().getQuantityLimit());
            periodicLimitEntity.setPeriodicLimitsDayTransactionLimit(periodicLimits.getDay().getTransactionLimit());
        }

        if (periodicLimits.getWeek() != null) {
            periodicLimitEntity.setPeriodicLimitsWeekQuantityLimit(periodicLimits.getWeek().getQuantityLimit());
            periodicLimitEntity.setPeriodicLimitsWeekTransactionLimit(periodicLimits.getWeek().getTransactionLimit());
        }

        if (periodicLimits.getMonth() != null) {
            periodicLimitEntity.setPeriodicLimitsMonthQuantityLimit(periodicLimits.getMonth().getQuantityLimit());
            periodicLimitEntity.setPeriodicLimitsMonthTransactionLimit(periodicLimits.getMonth().getTransactionLimit());
        }

        if (periodicLimits.getYear() != null) {
            periodicLimitEntity.setPeriodicLimitsYearQuantityLimit(periodicLimits.getYear().getQuantityLimit());
            periodicLimitEntity.setPeriodicLimitsYearTransactionLimit(periodicLimits.getYear().getTransactionLimit());
        }
        return periodicLimitEntity;
    }

    public PeriodicLimits getDTO() {
       var periodicLimits =  new PeriodicLimits();
        if (periodicLimitsDayTransactionLimit != null) {
            periodicLimits.day(new Day()
                    .quantityLimit(periodicLimitsDayQuantityLimit)
                    .transactionLimit(periodicLimitsDayTransactionLimit));
        }

        if (periodicLimitsWeekTransactionLimit != null) {
            periodicLimits.week(new Week()
                    .quantityLimit(periodicLimitsWeekQuantityLimit)
                    .transactionLimit(periodicLimitsWeekTransactionLimit));
        }

        if (periodicLimitsMonthTransactionLimit != null) {
            periodicLimits.month(new Month()
                    .quantityLimit(periodicLimitsMonthQuantityLimit)
                    .transactionLimit(periodicLimitsMonthTransactionLimit));
        }

        if (periodicLimitsYearTransactionLimit != null) {
            periodicLimits.year(new Year()
                    .quantityLimit(periodicLimitsYearQuantityLimit)
                    .transactionLimit(periodicLimitsYearTransactionLimit));
        }

        return periodicLimits;
    }
}
