package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "contracted_finance_charges")
public class ContractedFinanceChargesEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "contracted_finance_charges_id", unique = true, nullable = false, updatable = false, insertable = false, columnDefinition = "uuid NOT NULL DEFAULT uuid_generate_v4()")
    private UUID contractedFinanceChargesId;

    @Type(type = "pg-uuid")
    @NotNull
    @Column(name = "contract_id", updatable = false)
    private UUID contractId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", referencedColumnName = "contract_id", insertable = false, nullable = false, updatable = false)
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

    public FinancingsFinanceCharge getFinancingsDTO() {
        return new FinancingsFinanceCharge()
                .chargeType(FinancingsFinanceCharge.ChargeTypeEnum.valueOf(this.chargeType))
                .chargeAdditionalInfo(this.chargeAdditionalInfo)
                .chargeRate(BigDecimal.valueOf(this.chargeRate));
    }

    public UnarrangedAccountOverdraftFinanceCharge getUnarrangedAccountsOverdraftDTO() {
        return new UnarrangedAccountOverdraftFinanceCharge()
                .chargeType(ChargeType.valueOf(this.chargeType))
                .chargeAdditionalInfo(this.chargeAdditionalInfo)
                .chargeRate(BigDecimal.valueOf(this.chargeRate));
    }

    public InvoiceFinancingsFinanceCharge getInvoiceFinancingsDTO() {
        return new InvoiceFinancingsFinanceCharge()
                .chargeType(EnumContractFinanceChargeType1.valueOf(this.chargeType))
                .chargeAdditionalInfo(this.chargeAdditionalInfo)
                .chargeRate(BigDecimal.valueOf(this.chargeRate));
    }

}
