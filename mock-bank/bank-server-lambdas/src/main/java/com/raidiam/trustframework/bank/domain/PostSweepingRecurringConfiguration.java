package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.SweepingSweeping;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Accessors(chain = true)
@Table(name = "post_sweeping_recurring_configuration")
public class PostSweepingRecurringConfiguration extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "amount")
    private String amount;

    @Column(name = "transaction_Limit")
    private String transactionLimit;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "periodic_limits_reference_id", referencedColumnName = "reference_id")
    private PeriodicLimitsEntity periodicLimits;


    public static PostSweepingRecurringConfiguration from(SweepingSweeping sweeping) {
        return new PostSweepingRecurringConfiguration()
                .setAmount(sweeping.getTotalAllowedAmount())
                .setTransactionLimit(sweeping.getTransactionLimit())
                .setPeriodicLimits(Optional.ofNullable(sweeping.getPeriodicLimits()).map(PeriodicLimitsEntity::from).orElse(null));
    }

    public SweepingSweeping getDTO() {
        return new SweepingSweeping()
                .totalAllowedAmount(amount)
                .transactionLimit(transactionLimit)
                .periodicLimits(Optional.ofNullable(periodicLimits).map(PeriodicLimitsEntity::getDTO).orElse(null));
    }


}
