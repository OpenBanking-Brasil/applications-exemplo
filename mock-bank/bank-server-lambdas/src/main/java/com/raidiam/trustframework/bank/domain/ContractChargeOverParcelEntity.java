package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "over_parcel_charges")
public class ContractChargeOverParcelEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "over_parcel_charges_id", unique = true, nullable = false, updatable = false, insertable = false, columnDefinition = "uuid NOT NULL DEFAULT uuid_generate_v4()")
    private UUID overParcelChargesId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "releases_id", referencedColumnName = "releases_id", insertable = false, nullable = false, updatable = false)
    @NotAudited
    private ContractReleasesEntity releases;

    @Column(name = "releases_id", nullable = false)
    @NotNull
    @Type(type = "pg-uuid")
    private UUID releasesId;

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

    public FinancingsChargeOverParcel getFinancingsDTO() {
        FinancingsChargeOverParcel financingsChargeOverParcel = new FinancingsChargeOverParcel();

        financingsChargeOverParcel.setChargeType(null);
        financingsChargeOverParcel.setChargeAdditionalInfo(this.chargeAdditionalInfo);
        financingsChargeOverParcel.setChargeAmount(this.chargeAmount);


        return financingsChargeOverParcel;
    }

    public UnarrangedAccountOverdraftChargeOverParcel getUnarrangedAccountOverdraftDTO() {
        UnarrangedAccountOverdraftChargeOverParcel unarrangedAccountOverdraftChargeOverParcel = new UnarrangedAccountOverdraftChargeOverParcel();

        unarrangedAccountOverdraftChargeOverParcel.setChargeType(ChargeType.valueOf(this.chargeType));
        unarrangedAccountOverdraftChargeOverParcel.setChargeAdditionalInfo(this.chargeAdditionalInfo);
        unarrangedAccountOverdraftChargeOverParcel.setChargeAmount(this.chargeAmount);


        return unarrangedAccountOverdraftChargeOverParcel;
    }

    public InvoiceFinancingsChargeOverParcel getInvoiceFinancingsDTO() {
        InvoiceFinancingsChargeOverParcel invoiceFinancingsChargeOverParcel = new InvoiceFinancingsChargeOverParcel();

        invoiceFinancingsChargeOverParcel.setChargeType(InvoiceFinancingsChargeOverParcel.ChargeTypeEnum.valueOf(this.chargeType));
        invoiceFinancingsChargeOverParcel.setChargeAdditionalInfo(this.chargeAdditionalInfo);
        invoiceFinancingsChargeOverParcel.setChargeAmount(this.chargeAmount);


        return invoiceFinancingsChargeOverParcel;
    }
}
