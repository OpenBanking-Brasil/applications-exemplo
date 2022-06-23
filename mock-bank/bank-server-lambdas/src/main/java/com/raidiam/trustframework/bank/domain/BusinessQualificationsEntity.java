package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.BusinessInformedPatrimony;
import com.raidiam.trustframework.mockbank.models.generated.BusinessQualificationData;
import com.raidiam.trustframework.mockbank.models.generated.BusinessQualificationDataInformedRevenue;
import com.raidiam.trustframework.mockbank.models.generated.EnumInformedRevenueFrequency;
import lombok.*;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "business_qualifications")
public class BusinessQualificationsEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Generated(GenerationTime.INSERT)
    @Type(type = "pg-uuid")
    @Column(name = "business_qualifications_id", nullable = false, unique = true, updatable = false, insertable = false, columnDefinition = "uuid DEFAULT uuid_generate_v4()")
    private UUID businessQualificationsId;

    @Column(name = "account_holder_id")
    @Type(type = "pg-uuid")
    private UUID accountHolderId;

    @OneToOne(fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "account_holder_id", referencedColumnName = "account_holder_id", insertable = false, updatable = false)
    private AccountHolderEntity accountHolder;

    @Column(name = "informed_revenue_frequency")
    private String informedRevenueFrequency;

    @Column(name = "informed_revenue_frequency_additional_information")
    private String informedRevenueFrequencyAdditionalInformation;

    @Column(name = "informed_revenue_amount")
    private Double informedRevenueAmount;

    @Column(name = "informed_revenue_currency")
    private String informedRevenueCurrency;

    @Column(name = "informed_revenue_year")
    private Integer informedRevenueYear;

    @Column(name = "informed_patrimony_amount")
    private Double informedPatrimonyAmount;

    @Column(name = "informed_patrimony_currency")
    private String informedPatrimonyCurrency;

    @Column(name = "informed_patrimony_date")
    private Date informedPatrimonyDate;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "qualification")
    private Set<BusinessQualificationsEconomicActivitiesEntity> economicActivities;

    public BusinessQualificationData getDTO() {
        return new BusinessQualificationData()
                .updateDateTime(BankLambdaUtils.dateToOffsetDate(this.getUpdatedAt()))
                .economicActivities(this.getEconomicActivities().stream().map(BusinessQualificationsEconomicActivitiesEntity::getDTO).collect(Collectors.toList()))
                .informedRevenue(new BusinessQualificationDataInformedRevenue()
                        .frequency(EnumInformedRevenueFrequency.fromValue(this.getInformedRevenueFrequency()))
                        .frequencyAdditionalInfo(this.getInformedRevenueFrequencyAdditionalInformation())
                        .amount(this.getInformedRevenueAmount())
                        .currency(this.getInformedRevenueCurrency())
                        .year(BigDecimal.valueOf(this.getInformedRevenueYear())))
                .informedPatrimony(new BusinessInformedPatrimony()
                        .amount(this.getInformedPatrimonyAmount())
                        .currency(this.getInformedPatrimonyCurrency())
                        .date(BankLambdaUtils.dateToLocalDate(this.getInformedPatrimonyDate())));
        }
}
