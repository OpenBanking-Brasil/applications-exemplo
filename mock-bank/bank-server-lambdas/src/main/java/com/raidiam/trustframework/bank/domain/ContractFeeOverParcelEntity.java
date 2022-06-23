package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.FinancingsFeeOverParcel;
import com.raidiam.trustframework.mockbank.models.generated.InvoiceFinancingsFeeOverParcel;
import com.raidiam.trustframework.mockbank.models.generated.LoansFeeOverParcel;
import com.raidiam.trustframework.mockbank.models.generated.UnarrangedAccountOverdraftFeeOverParcel;
import lombok.*;
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
@Table(name = "over_parcel_fees")
public class ContractFeeOverParcelEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "over_parcel_fees_id", unique = true, nullable = false, updatable = false, insertable = false, columnDefinition = "uuid NOT NULL DEFAULT uuid_generate_v4()")
    private UUID overParcelFeesId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "releases_id", referencedColumnName = "releases_id", insertable = false, nullable = false, updatable = false)
    @NotAudited
    private ContractReleasesEntity releases;

    @Column(name = "releases_id", nullable = false)
    private UUID releasesId;

    @NotNull
    @Column(name = "fee_name", nullable = false)
    private String feeName;

    @NotNull
    @Column(name = "fee_code", nullable = false)
    private String feeCode;

    @NotNull
    @Column(name = "fee_amount", nullable = false)
    private double feeAmount;

    public LoansFeeOverParcel getLoansDTO() {
        LoansFeeOverParcel loansFeeOverParcel = new LoansFeeOverParcel();

        loansFeeOverParcel.feeName(this.feeName);
        loansFeeOverParcel.feeCode(this.feeCode);
        loansFeeOverParcel.feeAmount(this.feeAmount);


        return loansFeeOverParcel;
    }

    public FinancingsFeeOverParcel getFinancingsDTO() {
        FinancingsFeeOverParcel financingsFeeOverParcel = new FinancingsFeeOverParcel();

        financingsFeeOverParcel.feeName(this.feeName);
        financingsFeeOverParcel.feeCode(this.feeCode);
        financingsFeeOverParcel.feeAmount(this.feeAmount);


        return financingsFeeOverParcel;
    }

    public UnarrangedAccountOverdraftFeeOverParcel getUnarrangedAccountsOverdraftDTO() {
        UnarrangedAccountOverdraftFeeOverParcel unarrangedAccountOverdraftFeeOverParcel = new UnarrangedAccountOverdraftFeeOverParcel();

        unarrangedAccountOverdraftFeeOverParcel.feeName(this.feeName);
        unarrangedAccountOverdraftFeeOverParcel.feeCode(this.feeCode);
        unarrangedAccountOverdraftFeeOverParcel.feeAmount(this.feeAmount);


        return unarrangedAccountOverdraftFeeOverParcel;
    }

    public InvoiceFinancingsFeeOverParcel getInvoiceFinancingsDTO() {
        InvoiceFinancingsFeeOverParcel invoiceFinancingsFeeOverParcel = new InvoiceFinancingsFeeOverParcel();

        invoiceFinancingsFeeOverParcel.feeName(this.feeName);
        invoiceFinancingsFeeOverParcel.feeCode(this.feeCode);
        invoiceFinancingsFeeOverParcel.feeAmount(this.feeAmount);


        return invoiceFinancingsFeeOverParcel;
    }
}
