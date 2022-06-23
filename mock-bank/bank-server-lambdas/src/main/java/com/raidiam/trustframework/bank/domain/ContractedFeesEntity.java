package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@Audited
@Table(name = "contracted_fees")
public class ContractedFeesEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "contracted_fees_id", unique = true, nullable = false, updatable = false, insertable = false, columnDefinition = "uuid NOT NULL DEFAULT uuid_generate_v4()")
    private UUID contractedFeesId;

    @Type(type = "pg-uuid")
    @Column(name = "contract_id", updatable = false)
    private UUID contractId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", referencedColumnName = "contract_id", insertable = false, nullable = false, updatable = false)
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

    public FinancingsContractFee getFinancingsDTO() {
        return new FinancingsContractFee()
                .feeName(this.feeName)
                .feeCode(this.feeCode)
                .feeChargeType(FinancingsContractFee.FeeChargeTypeEnum.valueOf(this.feeChargeType))
                .feeCharge(FinancingsContractFee.FeeChargeEnum.valueOf(this.feeCharge))
                .feeAmount(this.feeAmount)
                .feeRate(this.feeRate);
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

    public InvoiceFinancingsContractedFee getInvoiceFinancingsDTO() {
        return new InvoiceFinancingsContractedFee()
                .feeName(this.feeName)
                .feeCode(this.feeCode)
                .feeChargeType(EnumContractFeeChargeType1.valueOf(this.feeChargeType))
                .feeCharge(EnumContractFeeCharge1.valueOf(this.feeCharge))
                .feeAmount(this.feeAmount)
                .feeRate(this.feeRate);
    }

}
