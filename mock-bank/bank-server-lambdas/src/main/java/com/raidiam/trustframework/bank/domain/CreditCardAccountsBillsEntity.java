package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.CreditCardAccountsBillsData;
import com.raidiam.trustframework.mockbank.models.generated.CreditCardAccountsTransaction;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@Audited
@Table(name = "credit_card_accounts_bills")
public class CreditCardAccountsBillsEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Generated(GenerationTime.INSERT)
    @Type(type = "pg-uuid")
    @Column(name = "bill_id", unique = true, nullable = false, updatable = false, insertable = false, columnDefinition = "uuid NOT NULL DEFAULT uuid_generate_v4()")
    private UUID billId;

    @NotNull
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @NotNull
    @Column(name = "bill_total_amount", nullable = false)
    private Double billTotalAmount;

    @NotNull
    @Column(name = "bill_total_amount_currency", nullable = false)
    private String billTotalAmountCurrency;

    @NotNull
    @Column(name = "bill_minimum_amount", nullable = false)
    private Double billMinimumAmount;

    @NotNull
    @Column(name = "bill_minimum_amount_currency", nullable = false)
    private String billMinimumAmountCurrency;

    @NotNull
    @Column(name = "is_instalment", nullable = false)
    private boolean isInstalment;

    @NotNull
    @Type(type = "pg-uuid")
    @Column(name = "credit_card_account_id", nullable = false)
    private UUID creditCardAccountId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_card_account_id", referencedColumnName = "credit_card_account_id", insertable = false, nullable = false, updatable = false)
    private CreditCardAccountsEntity account;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "bill")
    private Set<CreditCardAccountsBillsFinanceChargeEntity> financeCharges;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "bill")
    private Set<CreditCardAccountsBillsPaymentEntity> payments;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "bill")
    private Set<CreditCardAccountsTransactionEntity> transactions;

    public CreditCardAccountsBillsData getDTO() {
        return new CreditCardAccountsBillsData()
                .billId(this.billId.toString())
                .dueDate(this.dueDate)
                .billTotalAmount(this.billTotalAmount)
                .billTotalAmountCurrency(this.billTotalAmountCurrency)
                .billMinimumAmount(this.billMinimumAmount)
                .billMinimumAmountCurrency(this.billMinimumAmountCurrency)
                .isInstalment(this.isInstalment)
                .financeCharges(this.financeCharges.stream().map(CreditCardAccountsBillsFinanceChargeEntity::getDTO).collect(Collectors.toList()))
                .payments(this.payments.stream().map(CreditCardAccountsBillsPaymentEntity::getDTO).collect(Collectors.toList()));
    }

    public List<CreditCardAccountsTransaction> getCreditCardAccountsTransaction() {
        return this.transactions.stream().map(CreditCardAccountsTransactionEntity::getDTO).collect(Collectors.toList());
    }
}
