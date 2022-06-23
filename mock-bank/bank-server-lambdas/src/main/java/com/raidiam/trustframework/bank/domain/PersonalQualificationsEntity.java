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
import java.util.Date;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "personal_qualifications")
public class PersonalQualificationsEntity extends BaseEntity {

    @Id
    @GeneratedValue
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
    private Date informedIncomeDate;

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
                .informedIncome(new PersonalQualificationDataInformedIncome()
                        .frequency(EnumInformedIncomeFrequency.fromValue(this.getInformedIncomeFrequency()))
                        .amount(this.getInformedIncomeAmount())
                        .currency(this.getInformedIncomeCurrency())
                        .date(BankLambdaUtils.dateToLocalDate(this.getInformedIncomeDate())))
                .informedPatrimony(new PersonalInformedPatrimony()
                        .amount(this.getInformedPatrimonyAmount())
                        .currency(this.getInformedIncomeCurrency())
                        .year(BigDecimal.valueOf(this.getInformedPatrimonyYear())));
    }
}
