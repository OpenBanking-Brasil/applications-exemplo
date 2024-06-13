package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.BusinessInformedPatrimony;
import com.raidiam.trustframework.mockbank.models.generated.BusinessInformedPatrimonyV2;
import com.raidiam.trustframework.mockbank.models.generated.BusinessQualificationData;
import com.raidiam.trustframework.mockbank.models.generated.BusinessInformedRevenue;
import com.raidiam.trustframework.mockbank.models.generated.BusinessQualificationDataV2;
import com.raidiam.trustframework.mockbank.models.generated.BusinessQualifications;
import com.raidiam.trustframework.mockbank.models.generated.BusinessQualificationsData;
import com.raidiam.trustframework.mockbank.models.generated.EnumInformedRevenueFrequency;
import com.raidiam.trustframework.mockbank.models.generated.EnumInformedRevenueFrequencyV2;
import com.raidiam.trustframework.mockbank.models.generated.InformedPatrimonyAmountV2;
import com.raidiam.trustframework.mockbank.models.generated.InformedRevenueAmountV2;
import com.raidiam.trustframework.mockbank.models.generated.InformedRevenueV2;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "business_qualifications")
public class BusinessQualificationsEntity extends BaseEntity {

    @Id
    @GeneratedValue
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
    private LocalDate informedPatrimonyDate;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "qualification")
    private Set<BusinessQualificationsEconomicActivitiesEntity> economicActivities = new HashSet<>();

    public BusinessQualificationData getDTO() {
        return new BusinessQualificationData()
                .updateDateTime(BankLambdaUtils.dateToOffsetDate(this.getUpdatedAt()))
                .economicActivities(this.getEconomicActivities().stream().map(BusinessQualificationsEconomicActivitiesEntity::getDTO).collect(Collectors.toList()))
                .informedRevenue(new BusinessInformedRevenue()
                        .frequency(EnumInformedRevenueFrequency.fromValue(this.getInformedRevenueFrequency()))
                        .frequencyAdditionalInfo(this.getInformedRevenueFrequencyAdditionalInformation())
                        .amount(this.getInformedRevenueAmount())
                        .currency(this.getInformedRevenueCurrency())
                        .year(BigDecimal.valueOf(this.getInformedRevenueYear())))
                .informedPatrimony(new BusinessInformedPatrimony()
                        .amount(this.getInformedPatrimonyAmount())
                        .currency(this.getInformedPatrimonyCurrency())
                        .date(this.getInformedPatrimonyDate()));
    }

    public BusinessQualificationDataV2 getDtoV2() {
        return new BusinessQualificationDataV2()
                .updateDateTime(BankLambdaUtils.dateToOffsetDate(this.getUpdatedAt()))
                .economicActivities(this.getEconomicActivities().stream().map(BusinessQualificationsEconomicActivitiesEntity::getDTOV2).collect(Collectors.toList()))
                .informedRevenue(new InformedRevenueV2()
                        .frequency(EnumInformedRevenueFrequencyV2.fromValue(this.getInformedRevenueFrequency()))
                        .frequencyAdditionalInfo(this.getInformedRevenueFrequencyAdditionalInformation())
                        .amount(new InformedRevenueAmountV2()
                                .amount(BankLambdaUtils.formatAmountV2(this.getInformedRevenueAmount()))
                                .currency(this.getInformedRevenueCurrency()))
                        .year(BigDecimal.valueOf(this.getInformedRevenueYear())))
                .informedPatrimony(new BusinessInformedPatrimonyV2()
                        .amount(new InformedPatrimonyAmountV2()
                                .amount(BankLambdaUtils.formatAmountV2(this.getInformedPatrimonyAmount()))
                                .currency(this.getInformedPatrimonyCurrency()))
                        .date(this.getInformedPatrimonyDate()));
    }

    public static BusinessQualificationsEntity from(BusinessQualificationsData qualificationsDto, UUID accountHolderId) {
        var qualifications = new BusinessQualificationsEntity();
        qualifications.setAccountHolderId(accountHolderId);
        qualifications.setInformedRevenueFrequency(qualificationsDto.getInformedRevenueFrequency().toString());
        qualifications.setInformedRevenueFrequencyAdditionalInformation(qualificationsDto.getInformedRevenueFrequencyAdditionalInfo());
        qualifications.setInformedRevenueAmount(qualificationsDto.getInformedRevenueAmount());
        qualifications.setInformedRevenueCurrency(qualificationsDto.getInformedRevenueCurrency());
        qualifications.setInformedRevenueYear(qualificationsDto.getInformedRevenueYear().intValue());
        qualifications.setInformedPatrimonyAmount(qualificationsDto.getInformedPatrimonyAmount());
        qualifications.setInformedPatrimonyCurrency(qualificationsDto.getInformedPatrimonyCurrency());
        qualifications.setInformedPatrimonyDate(LocalDate.parse(qualificationsDto.getInformedPatrimonyDate()));

        var economicActivitiesList = qualificationsDto.getEconomicActivities().stream()
                .map(e -> BusinessQualificationsEconomicActivitiesEntity.from(qualifications, e))
                .collect(Collectors.toSet());
        qualifications.setEconomicActivities(economicActivitiesList);

        return qualifications;
    }

    public BusinessQualifications getAdminBusinessQualifications() {
        return new BusinessQualifications().data(new BusinessQualificationsData()
                .accountHolderId(this.accountHolderId)
                .informedRevenueFrequency(EnumInformedRevenueFrequency.fromValue(this.informedRevenueFrequency))
                .informedRevenueFrequencyAdditionalInfo(this.informedRevenueFrequencyAdditionalInformation)
                .informedRevenueAmount(this.informedRevenueAmount)
                .informedRevenueCurrency(this.informedRevenueCurrency)
                .informedRevenueYear(BigDecimal.valueOf(this.informedRevenueYear))
                .informedPatrimonyAmount(this.informedPatrimonyAmount)
                .informedPatrimonyCurrency(this.informedPatrimonyCurrency)
                .informedPatrimonyDate(this.informedPatrimonyDate.toString())
                .economicActivities(this.economicActivities != null ? this.economicActivities.stream().map(BusinessQualificationsEconomicActivitiesEntity::getDTO).collect(Collectors.toList()) : null));
    }

    public BusinessQualificationsEntity update(BusinessQualificationsData qualificationsDto) {
        this.informedRevenueFrequency = qualificationsDto.getInformedRevenueFrequency().toString();
        this.informedRevenueFrequencyAdditionalInformation = qualificationsDto.getInformedRevenueFrequencyAdditionalInfo();
        this.informedRevenueAmount = qualificationsDto.getInformedRevenueAmount();
        this.informedRevenueCurrency = qualificationsDto.getInformedRevenueCurrency();
        this.informedRevenueYear = qualificationsDto.getInformedRevenueYear().intValue();
        this.informedPatrimonyAmount = qualificationsDto.getInformedPatrimonyAmount();
        this.informedPatrimonyCurrency = qualificationsDto.getInformedPatrimonyCurrency();
        this.informedPatrimonyDate = LocalDate.parse(qualificationsDto.getInformedPatrimonyDate());

        var economicActivitiesList = qualificationsDto.getEconomicActivities().stream()
                .map(e -> BusinessQualificationsEconomicActivitiesEntity.from(this, e))
                .collect(Collectors.toSet());
        this.economicActivities.clear();
        this.economicActivities.addAll(economicActivitiesList);

        return this;
    }
}
