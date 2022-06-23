
package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.*;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "releases")
public class ContractReleasesEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "releases_id", unique = true, nullable = false, updatable = false, insertable = false)
    private UUID releasesId;

    @Generated(GenerationTime.INSERT)
    @Type(type = "pg-uuid")
    @Column(name = "payments_id", unique = true, nullable = false, updatable = false, columnDefinition = "uuid DEFAULT uuid_generate_v4()")
    private UUID paymentsId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", referencedColumnName = "contract_id", insertable = false, nullable = false, updatable = false)
    private ContractEntity contract;

    @Column(name = "contract_id", nullable = false)
    private UUID contractId;

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
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "releases")
    private Set<ContractFeeOverParcelEntity> fees;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "releases")
    private Set<ContractChargeOverParcelEntity> charges;

    public LoansReleases getLoansDTO() {
        return new LoansReleases()
                .paymentId(this.getPaymentsId().toString())
                .isOverParcelPayment(this.isOverParcelPayment)
                .instalmentId(this.instalmentId)
                .paidDate(this.paidDate)
                .currency(this.currency)
                .paidAmount(this.paidAmount)
                .overParcel(new LoansReleasesOverParcel()
                        .charges(this.getCharges().stream().map(ContractChargeOverParcelEntity::getLoansDTO).collect(Collectors.toList()))
                        .fees(this.getFees().stream().map(ContractFeeOverParcelEntity::getLoansDTO).collect(Collectors.toList())));
    }

    public FinancingsReleases getFinancingsDTO() {
        return new FinancingsReleases()
                .paymentId(String.valueOf(this.getPaymentsId()))
                .isOverParcelPayment(this.isOverParcelPayment)
                .instalmentId(this.instalmentId)
                .paidDate(this.paidDate)
                .currency(this.currency)
                .paidAmount(this.paidAmount)
                .overParcel(new FinancingsOverParcel()
                        .charges(this.getCharges().stream().map(ContractChargeOverParcelEntity::getFinancingsDTO).collect(Collectors.toList()))
                        .fees(this.getFees().stream().map(ContractFeeOverParcelEntity::getFinancingsDTO).collect(Collectors.toList())));
    }

    public UnarrangedAccountOverdraftReleases getUnarrangedAccountsOverdraftDTO() {
        return new UnarrangedAccountOverdraftReleases()
                .paymentId(this.getPaymentsId().toString())
                .isOverParcelPayment(this.isOverParcelPayment)
                .instalmentId(this.instalmentId)
                .paidDate(this.paidDate)
                .currency(this.currency)
                .paidAmount(this.paidAmount)
                .overParcel(new UnarrangedAccountOverdraftReleasesOverParcel()
                        .charges(this.getCharges().stream().map(ContractChargeOverParcelEntity::getUnarrangedAccountOverdraftDTO).collect(Collectors.toList()))
                        .fees(this.getFees().stream().map(ContractFeeOverParcelEntity::getUnarrangedAccountsOverdraftDTO).collect(Collectors.toList())));
    }

    public InvoiceFinancingsReleases getInvoiceFinancingsDTO() {
        return new InvoiceFinancingsReleases()
                .paymentId(String.valueOf(this.getPaymentsId()))
                .isOverParcelPayment(this.isOverParcelPayment)
                .instalmentId(this.instalmentId)
                .paidDate(this.paidDate)
                .currency(this.currency)
                .paidAmount(this.paidAmount)
                .overParcel(new InvoiceFinancingsReleasesOverParcel()
                        .charges(this.getCharges().stream().map(ContractChargeOverParcelEntity::getInvoiceFinancingsDTO).collect(Collectors.toList()))
                        .fees(this.getFees().stream().map(ContractFeeOverParcelEntity::getInvoiceFinancingsDTO).collect(Collectors.toList())));
    }

    public boolean isOverParcelPayment() {
        return this.isOverParcelPayment;
    }
}
