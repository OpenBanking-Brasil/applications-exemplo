package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.*;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "personal_qualifications")
public class PersonalQualificationsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Generated(GenerationTime.INSERT)
    @Type(type = "pg-uuid")
    @Column(name = "personal_qualifications_id", unique = true, nullable = false, updatable = false, columnDefinition = "uuid DEFAULT uuid_generate_v4()")
    private UUID personalQualificationsId;

    @Column(name = "account_holder_id")
    @Type(type = "pg-uuid")
    private UUID accountHolderId;

    @OneToOne(fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "account_holder_id", referencedColumnName = "account_holder_id", updatable = false, insertable = false)
    private AccountHolderEntity accountHolder;

    @Column(name = "company_cnpj")
    private String companyCnpj;

    @Column(name = "occupation_code")
    private String occupationCode;

    @Column(name = "occupation_description")
    private String occupationDescription;

    @Column(name = "informed_income_frequency")
    private String informedIncomeFrequency;

    @Column(name = "informed_income_amount")
    private Double informedIncomeAmount;

    @Column(name = "informed_income_currency")
    private String informedIncomeCurrency;

    @Column(name = "informed_income_date")
    private LocalDate informedIncomeDate;

    @Column(name = "informed_patrimony_amount")
    private Double informedPatrimonyAmount;

    @Column(name = "informed_patrimony_currency")
    private String informedPatrimonyCurrency;

    @Column(name = "informed_patrimony_year")
    private Integer informedPatrimonyYear;

    public PersonalQualificationData getDTO() {
        return new PersonalQualificationData()
                .updateDateTime(BankLambdaUtils.dateToOffsetDate(this.getUpdatedAt()))
                .companyCnpj(this.getCompanyCnpj())
                .occupationCode(EnumOccupationMainCodeType.fromValue(this.getOccupationCode()))
                .occupationDescription(this.getOccupationDescription())
                .informedIncome(new PersonalInformedIncome()
                        .frequency(EnumInformedIncomeFrequency.fromValue(this.getInformedIncomeFrequency()))
                        .amount(this.getInformedIncomeAmount())
                        .currency(this.getInformedIncomeCurrency())
                        .date(this.getInformedIncomeDate()))
                .informedPatrimony(new PersonalInformedPatrimony()
                        .amount(this.getInformedPatrimonyAmount())
                        .currency(this.getInformedPatrimonyCurrency())
                        .year(BigDecimal.valueOf(this.getInformedPatrimonyYear())));
    }

    public PersonalQualificationDataV2 getDtoV2() {
        return new PersonalQualificationDataV2()
                .updateDateTime(BankLambdaUtils.dateToOffsetDate(this.getUpdatedAt()))
                .companyCnpj(this.getCompanyCnpj())
                .occupationCode(EnumOccupationMainCodeTypeV2.fromValue(this.getOccupationCode()))
                .occupationDescription(this.getOccupationDescription())
                .informedIncome(new InformedIncomeV2()
                        .frequency(EnumInformedIncomeFrequencyV2.fromValue(this.getInformedIncomeFrequency()))
                        .amount(new InformedIncomeAmountV2()
                                .amount(BankLambdaUtils.formatAmountV2(this.getInformedIncomeAmount()))
                                .currency(this.getInformedIncomeCurrency()))
                        .date(this.getInformedIncomeDate()))
                .informedPatrimony(new PersonalInformedPatrimonyV2()
                        .amount(new InformedPatrimonyAmountV2()
                                .amount(BankLambdaUtils.formatAmountV2(this.getInformedPatrimonyAmount()))
                                .currency(this.getInformedPatrimonyCurrency()))
                        .year(BigDecimal.valueOf(this.getInformedPatrimonyYear())));
    }

    public static PersonalQualificationsEntity from(PersonalQualificationsData qualificationsDto, UUID accountHolderId) {
        var qualificationsEntity = new PersonalQualificationsEntity();
        qualificationsEntity.setAccountHolderId(accountHolderId);
        qualificationsEntity.setCompanyCnpj(qualificationsDto.getCompanyCnpj());
        qualificationsEntity.setOccupationCode(qualificationsDto.getOccupationCode().toString());
        qualificationsEntity.setOccupationDescription(qualificationsDto.getOccupationDescription());
        qualificationsEntity.setInformedIncomeFrequency(qualificationsDto.getInformedIncomeFrequency().toString());
        qualificationsEntity.setInformedIncomeAmount(qualificationsDto.getInformedIncomeAmount());
        qualificationsEntity.setInformedIncomeCurrency(qualificationsDto.getInformedIncomeCurrency());
        qualificationsEntity.setInformedIncomeDate(LocalDate.parse(qualificationsDto.getInformedIncomeDate()));
        qualificationsEntity.setInformedPatrimonyAmount(qualificationsDto.getInformedPatrimonyAmount());
        qualificationsEntity.setInformedPatrimonyCurrency(qualificationsDto.getInformedPatrimonyCurrency());
        qualificationsEntity.setInformedPatrimonyYear(qualificationsDto.getInformedPatrimonyYear().intValue());
        return qualificationsEntity;
    }

    public PersonalQualifications getAdminPersonalQualifications() {
        return new PersonalQualifications().data(new PersonalQualificationsData()
                .accountHolderId(this.accountHolderId)
                .companyCnpj(this.companyCnpj)
                .occupationCode(EnumOccupationMainCodeType.fromValue(this.occupationCode))
                .occupationDescription(this.occupationDescription)
                .informedIncomeFrequency(EnumInformedIncomeFrequency.fromValue(this.informedIncomeFrequency))
                .informedIncomeAmount(this.informedIncomeAmount)
                .informedIncomeCurrency(this.informedIncomeCurrency)
                .informedIncomeDate(this.informedIncomeDate.toString())
                .informedPatrimonyAmount(this.informedPatrimonyAmount)
                .informedPatrimonyCurrency(this.informedPatrimonyCurrency)
                .informedPatrimonyYear(BigDecimal.valueOf(this.informedPatrimonyYear)));
    }

    public PersonalQualificationsEntity update(PersonalQualificationsData qualificationsDto) {
        this.companyCnpj = qualificationsDto.getCompanyCnpj();
        this.occupationCode = qualificationsDto.getOccupationCode().toString();
        this.occupationDescription = qualificationsDto.getOccupationDescription();
        this.informedIncomeFrequency = qualificationsDto.getInformedIncomeFrequency().toString();
        this.informedIncomeAmount = qualificationsDto.getInformedIncomeAmount();
        this.informedIncomeCurrency = qualificationsDto.getInformedIncomeCurrency();
        this.informedIncomeDate = LocalDate.parse(qualificationsDto.getInformedIncomeDate());
        this.informedPatrimonyAmount = qualificationsDto.getInformedPatrimonyAmount();
        this.informedPatrimonyCurrency = qualificationsDto.getInformedPatrimonyCurrency();
        this.informedPatrimonyYear = qualificationsDto.getInformedPatrimonyYear().intValue();
        return this;
    }
}
