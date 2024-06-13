package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "over_parcel_charges")
public class ContractChargeOverParcelEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "over_parcel_charges_id", unique = true, nullable = false, updatable = false, insertable = false, columnDefinition = "uuid NOT NULL DEFAULT uuid_generate_v4()")
    private UUID overParcelChargesId;

    @NotNull
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "releases_id")
    @NotAudited
    private ContractReleasesEntity releases;

    @NotNull
    @Column(name = "charge_type", nullable = false)
    private String chargeType;

    @NotNull
    @Column(name = "charge_additional_info", nullable = false)
    private String chargeAdditionalInfo;

    @NotNull
    @Column(name = "charge_amount", nullable = false)
    private double chargeAmount;

    public LoansChargeOverParcel getLoansDTO() {
        LoansChargeOverParcel loansChargeOverParcel = new LoansChargeOverParcel();

        loansChargeOverParcel.setChargeType(EnumContractFinanceChargeType.valueOf(this.chargeType));
        loansChargeOverParcel.setChargeAdditionalInfo(this.chargeAdditionalInfo);
        loansChargeOverParcel.setChargeAmount(this.chargeAmount);

        return loansChargeOverParcel;
    }

    public LoansChargeOverParcelV2 getLoansDTOv2() {
        return new LoansChargeOverParcelV2()
                .chargeType(EnumContractFinanceChargeTypeV2.valueOf(this.chargeType))
                .chargeAdditionalInfo(this.chargeAdditionalInfo)
                .chargeAmount(BankLambdaUtils.formatAmountV2(this.chargeAmount));
    }

    public FinancingsChargeOverParcel getFinancingsDTO() {
        FinancingsChargeOverParcel financingsChargeOverParcel = new FinancingsChargeOverParcel();

        financingsChargeOverParcel.setChargeType(FinancingsFinanceChargeType.valueOf(this.chargeType));
        financingsChargeOverParcel.setChargeAdditionalInfo(this.chargeAdditionalInfo);
        financingsChargeOverParcel.setChargeAmount(this.chargeAmount);

        return financingsChargeOverParcel;
    }

    public FinancingsChargeOverParcelV2 getFinancingsDTOv2() {
        return new FinancingsChargeOverParcelV2()
                .chargeType(EnumContractFinanceChargeTypeV2.valueOf(this.chargeType))
                .chargeAdditionalInfo(this.chargeAdditionalInfo)
                .chargeAmount(BankLambdaUtils.formatAmountV2(this.chargeAmount));
    }

    public UnarrangedAccountOverdraftChargeOverParcel getUnarrangedAccountOverdraftDTO() {
        UnarrangedAccountOverdraftChargeOverParcel unarrangedAccountOverdraftChargeOverParcel = new UnarrangedAccountOverdraftChargeOverParcel();

        unarrangedAccountOverdraftChargeOverParcel.setChargeType(ChargeType.valueOf(this.chargeType));
        unarrangedAccountOverdraftChargeOverParcel.setChargeAdditionalInfo(this.chargeAdditionalInfo);
        unarrangedAccountOverdraftChargeOverParcel.setChargeAmount(this.chargeAmount);

        return unarrangedAccountOverdraftChargeOverParcel;
    }

    public UnarrangedAccountOverdraftChargeOverParcelV2 getUnarrangedAccountOverdraftDTOv2() {
        return new UnarrangedAccountOverdraftChargeOverParcelV2()
        .chargeType(ChargeTypeV2.valueOf(this.chargeType))
        .chargeAdditionalInfo(this.chargeAdditionalInfo)
        .chargeAmount(BankLambdaUtils.formatAmountV2(this.chargeAmount));
    }

    public InvoiceFinancingsChargeOverParcel getInvoiceFinancingsDTO() {
        InvoiceFinancingsChargeOverParcel invoiceFinancingsChargeOverParcel = new InvoiceFinancingsChargeOverParcel();

        invoiceFinancingsChargeOverParcel.setChargeType(InvoiceFinancingsChargeOverParcel.ChargeTypeEnum.valueOf(this.chargeType));
        invoiceFinancingsChargeOverParcel.setChargeAdditionalInfo(this.chargeAdditionalInfo);
        invoiceFinancingsChargeOverParcel.setChargeAmount(this.chargeAmount);

        return invoiceFinancingsChargeOverParcel;
    }

    public InvoiceFinancingsChargeOverParcelV2 getInvoiceFinancingsDTOv2() {
        return new InvoiceFinancingsChargeOverParcelV2()
        .chargeType(InvoiceFinancingsChargeOverParcelV2.ChargeTypeEnum.valueOf(this.chargeType))
        .chargeAdditionalInfo(this.chargeAdditionalInfo)
        .chargeAmount(BankLambdaUtils.formatAmountV2(this.chargeAmount));
    }

    public static ContractChargeOverParcelEntity from(ContractReleasesEntity releases, ContractOverParcelCharges overParcelCharges) {
        var overParcel = new ContractChargeOverParcelEntity();
        overParcel.setChargeType(overParcelCharges.getChargeType().toString());
        overParcel.setChargeAdditionalInfo(overParcelCharges.getChargeAdditionalInfo());
        overParcel.setChargeAmount(overParcelCharges.getChargeAmount());
        overParcel.setReleases(releases);

        return overParcel;
    }

    public ContractOverParcelCharges getContractOverParcelCharges() {
        return new ContractOverParcelCharges()
                .chargeType(ChargeType.fromValue(this.chargeType))
                .chargeAdditionalInfo(this.chargeAdditionalInfo)
                .chargeAmount(this.chargeAmount);
    }
}
