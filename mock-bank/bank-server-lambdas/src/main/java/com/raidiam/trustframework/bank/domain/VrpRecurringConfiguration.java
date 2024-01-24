package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.VrpVrp;
import com.raidiam.trustframework.mockbank.models.generated.VrpVrpGlobalLimits;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.envers.Audited;

import javax.persistence.*;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Accessors(chain = true)
@Table(name = "vrp_recurring_configuration")
public class VrpRecurringConfiguration extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "transaction_limit")
    private String transactionLimit;

    @Column(name = "global_quantity_limit")
    private int globalQuantityLimit;

    @Column(name = "global_transaction_limit")
    private String globalTransactionLimit;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "periodic_limits_reference_id", referencedColumnName = "reference_id")
    private PeriodicLimitsEntity periodicLimits;


    public static VrpRecurringConfiguration from(VrpVrp vrp) {
        return new VrpRecurringConfiguration()
                .setTransactionLimit(vrp.getTransactionLimit())
                .setGlobalQuantityLimit(vrp.getGlobalLimits().getQuantityLimit())
                .setGlobalTransactionLimit(vrp.getGlobalLimits().getTransactionLimit())
                .setPeriodicLimits(PeriodicLimitsEntity.from(vrp.getPeriodicLimits()));
    }

    public VrpVrp getDTO() {
        return new VrpVrp()
                .transactionLimit(transactionLimit)
                .globalLimits(new VrpVrpGlobalLimits()
                        .quantityLimit(globalQuantityLimit)
                        .transactionLimit(globalTransactionLimit))
                .periodicLimits(periodicLimits.getDTO());
    }


}
