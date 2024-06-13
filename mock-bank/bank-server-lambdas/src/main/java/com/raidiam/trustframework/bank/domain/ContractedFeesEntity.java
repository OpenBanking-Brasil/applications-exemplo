package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "contracted_fees")
public class ContractedFeesEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "contracted_fees_id", unique = true, nullable = false, updatable = false, insertable = false, columnDefinition = "uuid NOT NULL DEFAULT uuid_generate_v4()")
    private UUID contractedFeesId;

    @NotNull
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id")
    private ContractEntity contract;

    @Column(name = "fee_name", nullable = false)
    private String feeName;

    @Column(name = "fee_code", nullable = false)
    private String feeCode;

    @Column(name = "fee_charge_type", nullable = false)
    private String feeChargeType;

    @Column(name = "fee_charge", nullable = false)
    private String feeCharge;

    @Column(name = "fee_amount", nullable = false)
    private double feeAmount;

    @Column(name = "fee_rate", nullable = false)
    private double feeRate;

    public LoansContractedFee getLoansDTO() {
        return new LoansContractedFee()
                .feeName(this.feeName)
                .feeCode(this.feeCode)
                .feeChargeType(EnumContractFeeChargeType.valueOf(this.feeChargeType))
                .feeCharge(EnumContractFeeCharge.valueOf(this.feeCharge))
                .feeAmount(this.feeAmount)
                .feeRate(this.feeRate);
    }

    public LoansContractedFeeV2 getLoansDTOV2() {
        return new LoansContractedFeeV2()
                .feeName(this.feeName)
                .feeCode(this.feeCode)
                .feeChargeType(EnumContractFeeChargeTypeV2.valueOf(this.feeChargeType))
                .feeCharge(EnumContractFeeChargeV2.valueOf(this.feeCharge))
                .feeAmount(BankLambdaUtils.formatAmountV2(this.feeAmount))
                .feeRate(BankLambdaUtils.formatRateV2(this.feeRate));
    }

    public FinancingsContractFee getFinancingsDTO() {
        return new FinancingsContractFee()
                .feeName(this.feeName)
                .feeCode(this.feeCode)
                .feeChargeType(FinancingsContractFee.FeeChargeTypeEnum.valueOf(this.feeChargeType))
                .feeCharge(FinancingsContractFee.FeeChargeEnum.valueOf(this.feeCharge))
                .feeAmount(this.feeAmount)
                .feeRate(this.feeRate);
    }

    public FinancingsContractFeeV2 getFinancingsDTOV2() {
        return new FinancingsContractFeeV2()
                .feeName(this.feeName)
                .feeCode(this.feeCode)
                .feeChargeType(FinancingsContractFeeV2.FeeChargeTypeEnum.valueOf(this.feeChargeType))
                .feeCharge(FinancingsContractFeeV2.FeeChargeEnum.valueOf(this.feeCharge))
                .feeAmount(BankLambdaUtils.formatAmountV2(this.feeAmount))
                .feeRate(BankLambdaUtils.formatRateV2(this.feeRate));
    }

    public UnarrangedAccountOverdraftContractedFee getUnarrangedAccountOverdraftDTO() {
        return new UnarrangedAccountOverdraftContractedFee()
                .feeName(this.feeName)
                .feeCode(this.feeCode)
                .feeChargeType(EnumContractFeeChargeType.valueOf(this.feeChargeType))
                .feeCharge(EnumContractFeeCharge.valueOf(this.feeCharge))
                .feeAmount(this.feeAmount)
                .feeRate(this.feeRate);
    }

    public UnarrangedAccountOverdraftContractedFeeV2 getUnarrangedAccountOverdraftFeeDTOV2() {
        return new UnarrangedAccountOverdraftContractedFeeV2()
                .feeName(this.feeName)
                .feeCode(this.feeCode)
                .feeChargeType(EnumContractFeeChargeTypeV2.valueOf(this.feeChargeType))
                .feeCharge(EnumContractFeeChargeV2.valueOf(this.feeCharge))
                .feeAmount(BankLambdaUtils.formatAmountV2(this.feeAmount))
                .feeRate(BankLambdaUtils.formatRateV2(this.feeRate));
    }

    public InvoiceFinancingsContractedFee getInvoiceFinancingsDTO() {
        return new InvoiceFinancingsContractedFee()
                .feeName(this.feeName)
                .feeCode(this.feeCode)
                .feeChargeType(EnumContractFeeChargeType.valueOf(this.feeChargeType))
                .feeCharge(EnumContractFeeCharge.valueOf(this.feeCharge))
                .feeAmount(this.feeAmount)
                .feeRate(this.feeRate);
    }

    public InvoiceFinancingsContractedFeeV2 getInvoiceFinancingsDTOV2() {
        return new InvoiceFinancingsContractedFeeV2()
                .feeName(this.feeName)
                .feeCode(this.feeCode)
                .feeChargeType(EnumContractFeeChargeTypeV2.valueOf(this.feeChargeType))
                .feeCharge(EnumContractFeeChargeV2.valueOf(this.feeCharge))
                .feeAmount(BankLambdaUtils.formatAmountV2(this.feeAmount))
                .feeRate(BankLambdaUtils.formatRateV2(this.feeRate));
    }

    public static ContractedFeesEntity from(ContractEntity contract, ContractFees fees) {
        var feesEntity = new ContractedFeesEntity();
        feesEntity.setContract(contract);
        feesEntity.setFeeName(fees.getFeeName());
        feesEntity.setFeeCode(fees.getFeeCode());
        feesEntity.setFeeChargeType(fees.getFeeChargeType().name());
        feesEntity.setFeeCharge(fees.getFeeCharge().name());
        feesEntity.setFeeAmount(fees.getFeeAmount());
        feesEntity.setFeeRate(fees.getFeeRate());
        return feesEntity;
    }

    public ContractFees getContractFees() {
        return new ContractFees()
                .feeName(this.feeName)
                .feeCode(this.feeCode)
                .feeChargeType(EnumContractFeeChargeType.valueOf(this.feeChargeType))
                .feeCharge(EnumContractFeeCharge.valueOf(this.feeCharge))
                .feeAmount(this.feeAmount)
                .feeRate(this.feeRate);
    }
}
