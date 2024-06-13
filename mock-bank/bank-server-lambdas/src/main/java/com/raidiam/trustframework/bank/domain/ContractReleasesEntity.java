
package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.*;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "releases")
public class ContractReleasesEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Generated(GenerationTime.INSERT)
    @Type(type = "pg-uuid")
    @Column(name = "releases_id", unique = true, nullable = false, updatable = false, insertable = false, columnDefinition = "uuid DEFAULT uuid_generate_v4()")
    private UUID releasesId;

    @Generated(GenerationTime.INSERT)
    @Type(type = "pg-uuid")
    @Column(name = "payments_id", unique = true, nullable = false, updatable = false, insertable = false, columnDefinition = "uuid DEFAULT uuid_generate_v4()")
    private UUID paymentsId;

    @NotNull
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id")
    private ContractEntity contract;

    @NotNull
    @Column(name = "is_over_parcel_payment", nullable = false)
    private boolean isOverParcelPayment;

    @NotNull
    @Column(name = "instalment_id", nullable = false)
    private String instalmentId;

    @NotNull
    @Column(name = "paid_date", nullable = false)
    private LocalDate paidDate;

    @NotNull
    @Column(name = "currency", nullable = false)
    private String currency;

    @NotNull
    @Column(name = "paid_amount", nullable = false)
    private double paidAmount;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "releases")
    private Set<ContractFeeOverParcelEntity> fees = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "releases")
    private Set<ContractChargeOverParcelEntity> charges = new HashSet<>();

    public LoansReleases getLoansDTO() {
        return new LoansReleases()
                .paymentId(String.valueOf(this.paymentsId))
                .isOverParcelPayment(this.isOverParcelPayment)
                .instalmentId(this.instalmentId)
                .paidDate(this.paidDate)
                .currency(this.currency)
                .paidAmount(this.paidAmount)
                .overParcel(new LoansReleasesOverParcel()
                        .charges(this.getCharges().stream().map(ContractChargeOverParcelEntity::getLoansDTO).collect(Collectors.toList()))
                        .fees(this.getFees().stream().map(ContractFeeOverParcelEntity::getLoansDTO).collect(Collectors.toList())));
    }

    public LoansReleasesV2 getLoansDTOV2() {
        return new LoansReleasesV2()
                .paymentId(String.valueOf(this.paymentsId))
                .isOverParcelPayment(this.isOverParcelPayment)
                .instalmentId(this.instalmentId)
                .paidDate(this.paidDate)
                .currency(this.currency)
                .paidAmount(BankLambdaUtils.formatAmountV2(this.paidAmount))
                .overParcel(new LoansReleasesV2OverParcel()
                        .charges(this.getCharges().stream().map(ContractChargeOverParcelEntity::getLoansDTOv2).collect(Collectors.toList()))
                        .fees(this.getFees().stream().map(ContractFeeOverParcelEntity::getLoansDTOv2).collect(Collectors.toList())));
    }

    public FinancingsReleases getFinancingsDTO() {
        return new FinancingsReleases()
                .paymentId(String.valueOf(this.paymentsId))
                .isOverParcelPayment(this.isOverParcelPayment)
                .instalmentId(this.instalmentId)
                .paidDate(this.paidDate)
                .currency(this.currency)
                .paidAmount(this.paidAmount)
                .overParcel(new FinancingsOverParcel()
                        .charges(this.getCharges().stream().map(ContractChargeOverParcelEntity::getFinancingsDTO).collect(Collectors.toList()))
                        .fees(this.getFees().stream().map(ContractFeeOverParcelEntity::getFinancingsDTO).collect(Collectors.toList())));
    }

    public FinancingsReleasesV2 getFinancingsDTOv2() {
        return new FinancingsReleasesV2()
                .paymentId(String.valueOf(this.paymentsId))
                .isOverParcelPayment(this.isOverParcelPayment)
                .instalmentId(this.instalmentId)
                .paidDate(this.paidDate)
                .currency(this.currency)
                .paidAmount(BankLambdaUtils.formatAmountV2(this.paidAmount))
                .overParcel(new FinancingsOverParcelV2()
                        .charges(this.getCharges().stream().map(ContractChargeOverParcelEntity::getFinancingsDTOv2).collect(Collectors.toList()))
                        .fees(this.getFees().stream().map(ContractFeeOverParcelEntity::getFinancingsDTOv2).collect(Collectors.toList())));
    }

    public UnarrangedAccountOverdraftReleases getUnarrangedAccountsOverdraftDTO() {
        return new UnarrangedAccountOverdraftReleases()
                .paymentId(String.valueOf(this.paymentsId))
                .isOverParcelPayment(this.isOverParcelPayment)
                .instalmentId(this.instalmentId)
                .paidDate(this.paidDate)
                .currency(this.currency)
                .paidAmount(this.paidAmount)
                .overParcel(new UnarrangedAccountOverdraftReleasesOverParcel()
                        .charges(this.getCharges().stream().map(ContractChargeOverParcelEntity::getUnarrangedAccountOverdraftDTO).collect(Collectors.toList()))
                        .fees(this.getFees().stream().map(ContractFeeOverParcelEntity::getUnarrangedAccountsOverdraftDTO).collect(Collectors.toList())));
    }

    public UnarrangedAccountOverdraftReleasesV2 getUnarrangedAccountsOverdraftDTOv2() {
        return new UnarrangedAccountOverdraftReleasesV2()
                .paymentId(String.valueOf(this.paymentsId))
                .isOverParcelPayment(this.isOverParcelPayment)
                .instalmentId(this.instalmentId)
                .paidDate(this.paidDate)
                .currency(this.currency)
                .paidAmount(BankLambdaUtils.formatAmountV2(this.paidAmount))
                .overParcel(new UnarrangedAccountOverdraftReleasesV2OverParcel()
                        .charges(this.getCharges().stream().map(ContractChargeOverParcelEntity::getUnarrangedAccountOverdraftDTOv2).collect(Collectors.toList()))
                        .fees(this.getFees().stream().map(ContractFeeOverParcelEntity::getUnarrangedAccountsOverdraftDTOv2).collect(Collectors.toList())));
    }

    public InvoiceFinancingsReleases getInvoiceFinancingsDTO() {
        return new InvoiceFinancingsReleases()
                .paymentId(String.valueOf(this.paymentsId))
                .isOverParcelPayment(this.isOverParcelPayment)
                .instalmentId(this.instalmentId)
                .paidDate(this.paidDate)
                .currency(this.currency)
                .paidAmount(this.paidAmount)
                .overParcel(new InvoiceFinancingsReleasesOverParcel()
                        .charges(this.getCharges().stream().map(ContractChargeOverParcelEntity::getInvoiceFinancingsDTO).collect(Collectors.toList()))
                        .fees(this.getFees().stream().map(ContractFeeOverParcelEntity::getInvoiceFinancingsDTO).collect(Collectors.toList())));
    }

    public InvoiceFinancingsReleasesV2 getInvoiceFinancingsDTOv2() {
        return new InvoiceFinancingsReleasesV2()
                .paymentId(String.valueOf(this.paymentsId))
                .isOverParcelPayment(this.isOverParcelPayment)
                .instalmentId(this.instalmentId)
                .paidDate(this.paidDate)
                .currency(this.currency)
                .paidAmount(BankLambdaUtils.formatAmountV2(this.paidAmount))
                .overParcel(new InvoiceFinancingsReleasesV2OverParcel()
                        .charges(this.getCharges().stream().map(ContractChargeOverParcelEntity::getInvoiceFinancingsDTOv2).collect(Collectors.toList()))
                        .fees(this.getFees().stream().map(ContractFeeOverParcelEntity::getInvoiceFinancingsDTOv2).collect(Collectors.toList())));
    }

    public static ContractReleasesEntity from(ContractEntity contract, ContractReleases releases) {
        var releaseEntity = new ContractReleasesEntity();
        releaseEntity.setContract(contract);
        releaseEntity.setPaymentsId(releases.getPaymentsId());
        releaseEntity.setOverParcelPayment(releases.isIsOverParcelPayment());
        releaseEntity.setInstalmentId(releases.getInstalmentId());
        releaseEntity.setPaidDate(releases.getPaidDate());
        releaseEntity.setCurrency(releases.getCurrency());
        releaseEntity.setPaidAmount(releases.getPaidAmount());

        var overParcelFeesEntity = releases.getOverParcelFees().stream()
                .map(o -> ContractFeeOverParcelEntity.from(releaseEntity, o))
                .collect(Collectors.toSet());
        releaseEntity.setFees(overParcelFeesEntity);

        var overParcelChargesEntity = releases.getOverParcelCharges().stream()
                .map(o -> ContractChargeOverParcelEntity.from(releaseEntity, o))
                .collect(Collectors.toSet());
        releaseEntity.setCharges(overParcelChargesEntity);

        return releaseEntity;
    }

    public ContractReleases getContractReleases() {
        return new ContractReleases()
                .paymentsId(this.paymentsId)
                .isOverParcelPayment(this.isOverParcelPayment)
                .instalmentId(this.instalmentId)
                .paidDate(this.paidDate)
                .currency(this.currency)
                .paidAmount(this.paidAmount)
                .overParcelFees(this.fees.stream().map(ContractFeeOverParcelEntity::getContractOverParcelFees).collect(Collectors.toList()))
                .overParcelCharges(this.charges.stream().map(ContractChargeOverParcelEntity::getContractOverParcelCharges).collect(Collectors.toList()));
    }
}
