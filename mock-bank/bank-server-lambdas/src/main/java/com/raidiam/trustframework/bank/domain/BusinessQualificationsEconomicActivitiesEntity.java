package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.EconomicActivity;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "business_qualifications_economic_activities")
public class BusinessQualificationsEconomicActivitiesEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "code")
    private Integer code;

    @Column(name = "is_main")
    private boolean isMain;

    @Column(name = "business_qualifications_id")
    @Type(type = "pg-uuid")
    private UUID businessQualificationsId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_qualifications_id", referencedColumnName = "business_qualifications_id", insertable = false, nullable = false, updatable = false)
    private BusinessQualificationsEntity qualification;

    public EconomicActivity getDTO() {
        return new EconomicActivity()
                .code(BigDecimal.valueOf(this.getCode()))
                .isMain(this.isMain());
    }
}
