package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.EconomicActivity;
import com.raidiam.trustframework.mockbank.models.generated.EconomicActivityV2;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "business_qualifications_economic_activities")
public class BusinessQualificationsEconomicActivitiesEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "code")
    private Integer code;

    @Column(name = "is_main")
    private boolean isMain;

    @NotNull
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_qualifications_id", referencedColumnName = "business_qualifications_id", nullable = false, updatable = false)
    private BusinessQualificationsEntity qualification;

    public EconomicActivity getDTO() {
        return new EconomicActivity()
                .code(BigDecimal.valueOf(this.getCode()))
                .isMain(this.isMain());
    }

    public EconomicActivityV2 getDTOV2() {
        return new EconomicActivityV2()
                .code(String.valueOf(this.getCode()))
                .isMain(this.isMain());
    }

    public static BusinessQualificationsEconomicActivitiesEntity from(BusinessQualificationsEntity qualification, EconomicActivity economicActivity) {
        var economicActivityEntity = new BusinessQualificationsEconomicActivitiesEntity();
        economicActivityEntity.setQualification(qualification);
        economicActivityEntity.setCode(economicActivity.getCode().intValue());
        economicActivityEntity.setMain(economicActivity.isIsMain());
        return economicActivityEntity;
    }
}
