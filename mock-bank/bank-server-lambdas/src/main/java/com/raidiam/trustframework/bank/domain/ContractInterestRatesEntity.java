package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@Audited
@Table(name = "interest_rates")
public class ContractInterestRatesEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "interest_rates_id", unique = true, nullable = false, updatable = false, insertable = false, columnDefinition = "uuid NOT NULL DEFAULT uuid_generate_v4()")
    private UUID interestRates;

    @Type(type = "pg-uuid")
    @NotNull
    @Column(name = "contract_id", updatable = false)
    private UUID contractId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", referencedColumnName = "contract_id", insertable = false, nullable = false, updatable = false)
    private ContractEntity contract;

    @Column(name = "tax_type", nullable = false)
    private String taxType;

    @Column(name = "interest_rate_type", nullable = false)
    private String interestRateType;

    @Column(name = "tax_periodicity", nullable = false)
    private String taxPeriodicity;

    @Column(name = "calculation", nullable = false)
    private String calculation;

    @Column(name = "referential_rate_indexer_type", nullable = false)
    private String referentialRateIndexerType;

    @Column(name = "referential_rate_indexer_sub_type")
    private String referentialRateIndexerSubType;

    @Column(name = "referential_rate_indexer_additional_info")
    private String referentialRateIndexerAdditionalInfo;

    @Column(name = "pre_fixed_rate", nullable = false)
    private Double preFixedRate;

    @Column(name = "post_fixed_rate", nullable = false)
    private Double postFixedRate;

    @Column(name = "additional_info", nullable = false)
    private String additionalInfo;

    public LoansContractInterestRate getLoansDTO() {
        return new LoansContractInterestRate()
                .taxType(EnumContractTaxType.fromValue(this.taxType))
                .interestRateType(EnumContractInterestRateType.fromValue(this.interestRateType))
                .taxPeriodicity(EnumContractTaxPeriodicity.fromValue(this.taxPeriodicity))
                .calculation(EnumContractCalculation.fromValue(this.calculation))
                .referentialRateIndexerType(EnumContractReferentialRateIndexerType.fromValue(this.referentialRateIndexerType))
                .referentialRateIndexerSubType(EnumContractReferentialRateIndexerSubType.fromValue(this.referentialRateIndexerSubType))
                .referentialRateIndexerAdditionalInfo(this.referentialRateIndexerAdditionalInfo)
                .preFixedRate(BigDecimal.valueOf(this.preFixedRate))
                .postFixedRate(BigDecimal.valueOf(this.postFixedRate))
                .additionalInfo(this.additionalInfo);
    }

    public FinancingsContractInterestRate getFinancingsDTO() {
        return new FinancingsContractInterestRate()
                .taxType(FinancingsContractInterestRate.TaxTypeEnum.fromValue(this.taxType))
                .interestRateType(FinancingsContractInterestRate.InterestRateTypeEnum.fromValue(this.interestRateType))
                .taxPeriodicity(FinancingsContractInterestRate.TaxPeriodicityEnum.fromValue(this.taxPeriodicity))
                .calculation(FinancingsContractInterestRate.CalculationEnum.fromValue(this.calculation))
                .referentialRateIndexerType(FinancingsContractInterestRate.ReferentialRateIndexerTypeEnum.fromValue(this.referentialRateIndexerType))
                .referentialRateIndexerSubType(FinancingsContractInterestRate.ReferentialRateIndexerSubTypeEnum.fromValue(this.referentialRateIndexerSubType))
                .referentialRateIndexerAdditionalInfo(this.referentialRateIndexerAdditionalInfo)
                .preFixedRate(BigDecimal.valueOf(this.preFixedRate))
                .postFixedRate(BigDecimal.valueOf(this.postFixedRate))
                .additionalInfo(this.additionalInfo);
    }

    public UnarrangedAccountOverdraftContractInterestRate getUnarrangedAccountsOverdraftDTO() {
        return new UnarrangedAccountOverdraftContractInterestRate()
                .taxType(EnumContractTaxType.fromValue(this.taxType))
                .interestRateType(EnumContractInterestRateType.fromValue(this.interestRateType))
                .taxPeriodicity(EnumContractTaxPeriodicity.fromValue(this.taxPeriodicity))
                .calculation(UnarrangedAccountOverdraftContractInterestRate.CalculationEnum.fromValue(this.calculation))
                .referentialRateIndexerType(EnumContractReferentialRateIndexerType.fromValue(this.referentialRateIndexerType))
                .referentialRateIndexerSubType(EnumContractReferentialRateIndexerSubType.fromValue(this.referentialRateIndexerSubType))
                .referentialRateIndexerAdditionalInfo(this.referentialRateIndexerAdditionalInfo)
                .preFixedRate(BigDecimal.valueOf(this.preFixedRate))
                .postFixedRate(BigDecimal.valueOf(this.postFixedRate))
                .additionalInfo(this.additionalInfo);
    }

    public InvoiceFinancingsContractInterestRate getInvoiceFinancingsDTO() {
        return new InvoiceFinancingsContractInterestRate()
                .taxType(InvoiceFinancingsContractInterestRate.TaxTypeEnum.fromValue(this.taxType))
                .interestRateType(EnumContractInterestRateType1.fromValue(this.interestRateType))
                .taxPeriodicity(EnumContractTaxPeriodicity1.fromValue(this.taxPeriodicity))
                .calculation(EnumContractCalculation1.fromValue(this.calculation))
                .referentialRateIndexerType(EnumContractReferentialRateIndexerType1.fromValue(this.referentialRateIndexerType))
                .referentialRateIndexerSubType(EnumContractReferentialRateIndexerSubType1.fromValue(this.referentialRateIndexerSubType))
                .referentialRateIndexerAdditionalInfo(this.referentialRateIndexerAdditionalInfo)
                .preFixedRate(this.preFixedRate)
                .postFixedRate(this.preFixedRate)
                .additionalInfo(this.additionalInfo);
    }
}
