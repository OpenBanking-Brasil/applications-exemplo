package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "contracted_finance_charges")
public class ContractedFinanceChargesEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "contracted_finance_charges_id", unique = true, nullable = false, updatable = false, insertable = false, columnDefinition = "uuid NOT NULL DEFAULT uuid_generate_v4()")
    private UUID contractedFinanceChargesId;

    @NotNull
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id")
    private ContractEntity contract;

    @Column(name = "charge_type", nullable = false)
    private String chargeType;

    @Column(name = "charge_additional_info", nullable = false)
    private String chargeAdditionalInfo;

    @Column(name = "charge_rate")
    private Double chargeRate;

    public LoansFinanceCharge getLoansDTO() {
        return new LoansFinanceCharge()
                .chargeType(EnumContractFinanceChargeType.valueOf(this.chargeType))
                .chargeAdditionalInfo(this.chargeAdditionalInfo)
                .chargeRate(BigDecimal.valueOf(this.chargeRate));
    }

    public LoansFinanceChargeV2 getLoansDTOV2() {
        return new LoansFinanceChargeV2()
                .chargeType(EnumContractFinanceChargeTypeV2.valueOf(this.chargeType))
                .chargeAdditionalInfo(this.chargeAdditionalInfo)
                .chargeRate(BankLambdaUtils.formatRateV2(this.chargeRate));
    }

    public FinancingsFinanceCharge getFinancingsDTO() {
        return new FinancingsFinanceCharge()
                .chargeType(FinancingsFinanceCharge.ChargeTypeEnum.valueOf(this.chargeType))
                .chargeAdditionalInfo(this.chargeAdditionalInfo)
                .chargeRate(BigDecimal.valueOf(this.chargeRate));
    }

    public FinancingsFinanceChargeV2 getFinancingsDTOV2() {
        return new FinancingsFinanceChargeV2()
                .chargeType(EnumContractFinanceChargeTypeV2.valueOf(this.chargeType))
                .chargeAdditionalInfo(this.chargeAdditionalInfo)
                .chargeRate(BankLambdaUtils.formatRateV2(this.chargeRate));
    }

    public UnarrangedAccountOverdraftFinanceCharge getUnarrangedAccountsOverdraftDTO() {
        return new UnarrangedAccountOverdraftFinanceCharge()
                .chargeType(ChargeType.valueOf(this.chargeType))
                .chargeAdditionalInfo(this.chargeAdditionalInfo)
                .chargeRate(BigDecimal.valueOf(this.chargeRate));
    }

    public UnarrangedAccountOverdraftFinanceChargeV2 getUnarrangedAccountsOverdraftDTOV2() {
        return new UnarrangedAccountOverdraftFinanceChargeV2()
                .chargeType(ChargeTypeV2.valueOf(this.chargeType))
                .chargeAdditionalInfo(this.chargeAdditionalInfo)
                .chargeRate(BankLambdaUtils.formatRateV2(this.chargeRate));
    }

    public InvoiceFinancingsFinanceCharge getInvoiceFinancingsDTO() {
        return new InvoiceFinancingsFinanceCharge()
                .chargeType(EnumContractFinanceChargeType.valueOf(this.chargeType))
                .chargeAdditionalInfo(this.chargeAdditionalInfo)
                .chargeRate(BigDecimal.valueOf(this.chargeRate));
    }

    public InvoiceFinancingsFinanceChargeV2 getInvoiceFinancingsDTOV2() {
        return new InvoiceFinancingsFinanceChargeV2()
                .chargeType(EnumContractFinanceChargeTypeV2.valueOf(this.chargeType))
                .chargeAdditionalInfo(this.chargeAdditionalInfo)
                .chargeRate(BankLambdaUtils.formatRateV2(this.chargeRate));
    }

    public static ContractedFinanceChargesEntity from(ContractEntity contract, ContractFinanceCharges financeCharge) {
        var financeChargeEntity = new ContractedFinanceChargesEntity();
        financeChargeEntity.setContract(contract);
        financeChargeEntity.setChargeType(financeCharge.getChargeType().name());
        financeChargeEntity.setChargeAdditionalInfo(financeCharge.getChargeAdditionalInfo());
        financeChargeEntity.setChargeRate(financeCharge.getChargeRate());
        return financeChargeEntity;
    }

    public ContractFinanceCharges getContractFinanceCharge() {
        return new ContractFinanceCharges()
                .chargeType(ChargeType.valueOf(this.chargeType))
                .chargeAdditionalInfo(this.chargeAdditionalInfo)
                .chargeRate(this.chargeRate);
    }
}
