package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "interest_rates")
public class ContractInterestRatesEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "interest_rates_id", unique = true, nullable = false, updatable = false, insertable = false, columnDefinition = "uuid NOT NULL DEFAULT uuid_generate_v4()")
    private UUID interestRates;

    @NotNull
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id")
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

    public LoansContractInterestRateV2 getLoansDTOV2() {
        return new LoansContractInterestRateV2()
                .taxType(EnumContractTaxTypeV2.fromValue(this.taxType))
                .interestRateType(EnumContractInterestRateTypeV2.fromValue(this.interestRateType))
                .taxPeriodicity(EnumContractTaxPeriodicityV2.fromValue(this.taxPeriodicity))
                .calculation(EnumContractCalculationV2.fromValue(this.calculation))
                .referentialRateIndexerType(EnumContractReferentialRateIndexerTypeV2.fromValue(this.referentialRateIndexerType))
                .referentialRateIndexerSubType(EnumContractReferentialRateIndexerSubTypeV2.fromValue(this.referentialRateIndexerSubType))
                .referentialRateIndexerAdditionalInfo(this.referentialRateIndexerAdditionalInfo)
                .preFixedRate(BankLambdaUtils.formatRateV2(this.preFixedRate))
                .postFixedRate(BankLambdaUtils.formatRateV2(this.postFixedRate))
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

    public FinancingsContractInterestRateV2 getFinancingsDTOV2() {
        return new FinancingsContractInterestRateV2()
                .taxType(FinancingsContractInterestRateV2.TaxTypeEnum.fromValue(this.taxType))
                .interestRateType(FinancingsContractInterestRateV2.InterestRateTypeEnum.fromValue(this.interestRateType))
                .taxPeriodicity(FinancingsContractInterestRateV2.TaxPeriodicityEnum.fromValue(this.taxPeriodicity))
                .calculation(FinancingsContractInterestRateV2.CalculationEnum.fromValue(this.calculation))
                .referentialRateIndexerType(FinancingsContractInterestRateV2.ReferentialRateIndexerTypeEnum.fromValue(this.referentialRateIndexerType))
                .referentialRateIndexerSubType(FinancingsContractInterestRateV2.ReferentialRateIndexerSubTypeEnum.fromValue(this.referentialRateIndexerSubType))
                .referentialRateIndexerAdditionalInfo(this.referentialRateIndexerAdditionalInfo)
                .preFixedRate(BankLambdaUtils.formatRateV2(this.preFixedRate))
                .postFixedRate(BankLambdaUtils.formatRateV2(this.postFixedRate))
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

    public UnarrangedAccountOverdraftContractInterestRateV2 getUnarrangedAccountsOverdraftDTOV2() {
        return new UnarrangedAccountOverdraftContractInterestRateV2()
                .taxType(EnumContractTaxTypeV2.fromValue(this.taxType))
                .interestRateType(EnumContractInterestRateTypeV2.fromValue(this.interestRateType))
                .taxPeriodicity(EnumContractTaxPeriodicityV2.fromValue(this.taxPeriodicity))
                .calculation(UnarrangedAccountOverdraftContractInterestRateV2.CalculationEnum.fromValue(this.calculation))
                .referentialRateIndexerType(EnumContractReferentialRateIndexerTypeV2.fromValue(this.referentialRateIndexerType))
                .referentialRateIndexerSubType(EnumContractReferentialRateIndexerSubTypeV2.fromValue(this.referentialRateIndexerSubType))
                .referentialRateIndexerAdditionalInfo(this.referentialRateIndexerAdditionalInfo)
                .preFixedRate(BankLambdaUtils.formatRateV2(this.preFixedRate))
                .postFixedRate(BankLambdaUtils.formatRateV2(this.postFixedRate))
                .additionalInfo(this.additionalInfo);
    }

    public InvoiceFinancingsContractInterestRate getInvoiceFinancingsDTO() {
        return new InvoiceFinancingsContractInterestRate()
                .taxType(InvoiceFinancingsContractInterestRate.TaxTypeEnum.fromValue(this.taxType))
                .interestRateType(EnumContractInterestRateType.fromValue(this.interestRateType))
                .taxPeriodicity(EnumContractTaxPeriodicity.fromValue(this.taxPeriodicity))
                .calculation(EnumContractCalculation.fromValue(this.calculation))
                .referentialRateIndexerType(EnumContractReferentialRateIndexerType.fromValue(this.referentialRateIndexerType))
                .referentialRateIndexerSubType(EnumContractReferentialRateIndexerSubType.fromValue(this.referentialRateIndexerSubType))
                .referentialRateIndexerAdditionalInfo(this.referentialRateIndexerAdditionalInfo)
                .preFixedRate(this.preFixedRate)
                .postFixedRate(this.preFixedRate)
                .additionalInfo(this.additionalInfo);
    }

    public InvoiceFinancingsContractInterestRateV2 getInvoiceFinancingsDTOV2() {
        return new InvoiceFinancingsContractInterestRateV2()
                .taxType(InvoiceFinancingsContractInterestRateV2.TaxTypeEnum.fromValue(this.taxType))
                .interestRateType(EnumContractInterestRateTypeV2.fromValue(this.interestRateType))
                .taxPeriodicity(EnumContractTaxPeriodicityV2.fromValue(this.taxPeriodicity))
                .calculation(EnumContractCalculationV2.fromValue(this.calculation))
                .referentialRateIndexerType(EnumContractReferentialRateIndexerTypeV2.fromValue(this.referentialRateIndexerType))
                .referentialRateIndexerSubType(EnumContractReferentialRateIndexerSubTypeV2.fromValue(this.referentialRateIndexerSubType))
                .referentialRateIndexerAdditionalInfo(this.referentialRateIndexerAdditionalInfo)
                .preFixedRate(BankLambdaUtils.formatRateV2(this.preFixedRate))
                .postFixedRate(BankLambdaUtils.formatRateV2(this.preFixedRate))
                .additionalInfo(this.additionalInfo);
    }

    public static ContractInterestRatesEntity from(ContractEntity contract, ContractInterestRates interestRates) {
        var interestRatesEntity = new ContractInterestRatesEntity();
        interestRatesEntity.setContract(contract);
        interestRatesEntity.setTaxType(interestRates.getTaxType().name());
        interestRatesEntity.setInterestRateType(interestRates.getInterestRateType().name());
        interestRatesEntity.setTaxPeriodicity(interestRates.getTaxPeriodicity().name());
        interestRatesEntity.setCalculation(interestRates.getCalculation().toString());
        interestRatesEntity.setReferentialRateIndexerType(interestRates.getReferentialRateIndexerType().name());
        interestRatesEntity.setReferentialRateIndexerSubType(interestRates.getReferentialRateIndexerSubType().name());
        interestRatesEntity.setReferentialRateIndexerAdditionalInfo(interestRates.getReferentialRateIndexerAdditionalInfo());
        interestRatesEntity.setPreFixedRate(interestRates.getPreFixedRate());
        interestRatesEntity.setPostFixedRate(interestRates.getPostFixedRate());
        interestRatesEntity.setAdditionalInfo(interestRates.getAdditionalInfo());
        return interestRatesEntity;
    }

    public ContractInterestRates getContractInterestRates() {
        return new ContractInterestRates()
                .taxType(EnumContractTaxType.valueOf(this.taxType))
                .interestRateType(EnumContractInterestRateType.valueOf(this.interestRateType))
                .taxPeriodicity(EnumContractTaxPeriodicity.valueOf(this.taxPeriodicity))
                .calculation(ContractInterestRates.CalculationEnum.fromValue(this.calculation))
                .referentialRateIndexerType(EnumContractReferentialRateIndexerType.valueOf(this.referentialRateIndexerType))
                .referentialRateIndexerSubType(EnumContractReferentialRateIndexerSubType.valueOf(this.referentialRateIndexerSubType))
                .referentialRateIndexerAdditionalInfo(this.referentialRateIndexerAdditionalInfo)
                .preFixedRate(this.preFixedRate)
                .postFixedRate(this.postFixedRate)
                .additionalInfo(this.additionalInfo);
    }
}
