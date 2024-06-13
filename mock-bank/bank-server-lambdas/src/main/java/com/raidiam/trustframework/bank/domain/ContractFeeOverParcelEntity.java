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
@Table(name = "over_parcel_fees")
public class ContractFeeOverParcelEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "over_parcel_fees_id", unique = true, nullable = false, updatable = false, insertable = false, columnDefinition = "uuid NOT NULL DEFAULT uuid_generate_v4()")
    private UUID overParcelFeesId;

    @NotNull
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "releases_id")
    @NotAudited
    private ContractReleasesEntity releases;

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

    public LoansFeeOverParcelV2 getLoansDTOv2() {
        return new LoansFeeOverParcelV2()
                .feeName(this.feeName)
                .feeCode(this.feeCode)
                .feeAmount(BankLambdaUtils.formatAmountV2(this.feeAmount));
    }

    public FinancingsFeeOverParcel getFinancingsDTO() {
        FinancingsFeeOverParcel financingsFeeOverParcel = new FinancingsFeeOverParcel();

        financingsFeeOverParcel.feeName(this.feeName);
        financingsFeeOverParcel.feeCode(this.feeCode);
        financingsFeeOverParcel.feeAmount(this.feeAmount);


        return financingsFeeOverParcel;
    }

    public FinancingsFeeOverParcelV2 getFinancingsDTOv2() {
        return new FinancingsFeeOverParcelV2()
                .feeName(this.feeName)
                .feeCode(this.feeCode)
                .feeAmount(BankLambdaUtils.formatAmountV2(this.feeAmount));
    }

    public UnarrangedAccountOverdraftFeeOverParcel getUnarrangedAccountsOverdraftDTO() {
        UnarrangedAccountOverdraftFeeOverParcel unarrangedAccountOverdraftFeeOverParcel = new UnarrangedAccountOverdraftFeeOverParcel();

        unarrangedAccountOverdraftFeeOverParcel.feeName(this.feeName);
        unarrangedAccountOverdraftFeeOverParcel.feeCode(this.feeCode);
        unarrangedAccountOverdraftFeeOverParcel.feeAmount(this.feeAmount);


        return unarrangedAccountOverdraftFeeOverParcel;
    }

    public UnarrangedAccountOverdraftFeeOverParcelV2 getUnarrangedAccountsOverdraftDTOv2() {
        return new UnarrangedAccountOverdraftFeeOverParcelV2()
                .feeName(this.feeName)
                .feeCode(this.feeCode)
                .feeAmount(BankLambdaUtils.formatAmountV2(this.feeAmount));
    }

    public InvoiceFinancingsFeeOverParcel getInvoiceFinancingsDTO() {
        InvoiceFinancingsFeeOverParcel invoiceFinancingsFeeOverParcel = new InvoiceFinancingsFeeOverParcel();

        invoiceFinancingsFeeOverParcel.feeName(this.feeName);
        invoiceFinancingsFeeOverParcel.feeCode(this.feeCode);
        invoiceFinancingsFeeOverParcel.feeAmount(this.feeAmount);


        return invoiceFinancingsFeeOverParcel;
    }

    public InvoiceFinancingsFeeOverParcelV2 getInvoiceFinancingsDTOv2() {
        return new InvoiceFinancingsFeeOverParcelV2()
                .feeName(this.feeName)
                .feeCode(this.feeCode)
                .feeAmount(BankLambdaUtils.formatAmountV2(this.feeAmount));
    }

    public static ContractFeeOverParcelEntity from(ContractReleasesEntity releases, ContractOverParcelFees overParcelFees) {
        var feeOverParcel = new ContractFeeOverParcelEntity();
        feeOverParcel.setFeeName(overParcelFees.getFeeName());
        feeOverParcel.setFeeCode(overParcelFees.getFeeCode());
        feeOverParcel.setFeeAmount(overParcelFees.getFeeAmount());
        feeOverParcel.setReleases(releases);

        return feeOverParcel;
    }

    public ContractOverParcelFees getContractOverParcelFees() {
        return new ContractOverParcelFees()
                .feeName(this.feeName)
                .feeCode(this.feeCode)
                .feeAmount(this.feeAmount);
    }
}
