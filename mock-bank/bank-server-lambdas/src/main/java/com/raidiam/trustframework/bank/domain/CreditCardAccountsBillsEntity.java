package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.CreateCreditCardAccountBillData;
import com.raidiam.trustframework.mockbank.models.generated.CreditCardAccountsBillMinimumAmountV2;
import com.raidiam.trustframework.mockbank.models.generated.CreditCardAccountsBillsData;
import com.raidiam.trustframework.mockbank.models.generated.CreditCardAccountsBillsDataV2;
import com.raidiam.trustframework.mockbank.models.generated.CreditCardAccountsTransaction;
import com.raidiam.trustframework.mockbank.models.generated.CreditCardsBillTotalAmountV2;
import com.raidiam.trustframework.mockbank.models.generated.ResponseCreditCardAccountBill;
import com.raidiam.trustframework.mockbank.models.generated.ResponseCreditCardAccountBillData;
import lombok.*;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
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
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "credit_card_account_id")
    private CreditCardAccountsEntity account;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "bill")
    private Set<CreditCardAccountsBillsFinanceChargeEntity> financeCharges = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "bill")
    private Set<CreditCardAccountsBillsPaymentEntity> payments = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "bill")
    private Set<CreditCardAccountsTransactionEntity> transactions = new HashSet<>();

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

    public CreditCardAccountsBillsDataV2 getDtoV2() {
        return new CreditCardAccountsBillsDataV2()
                .billId(this.billId.toString())
                .dueDate(this.dueDate)
                .billTotalAmount(new CreditCardsBillTotalAmountV2()
                        .amount(BankLambdaUtils.formatAmountV2(this.billTotalAmount))
                        .currency(this.billTotalAmountCurrency))
                .billMinimumAmount(new CreditCardAccountsBillMinimumAmountV2()
                        .amount(BankLambdaUtils.formatAmountV2(this.billMinimumAmount))
                        .currency(this.billMinimumAmountCurrency))
                .isInstalment(this.isInstalment)
                .financeCharges(this.financeCharges.stream().map(CreditCardAccountsBillsFinanceChargeEntity::getDTOV2).collect(Collectors.toList()))
                .payments(this.payments.stream().map(CreditCardAccountsBillsPaymentEntity::getDTOV2).collect(Collectors.toList()));
    }

    public List<CreditCardAccountsTransaction> getCreditCardAccountsTransaction() {
        return this.transactions.stream().map(CreditCardAccountsTransactionEntity::getDTO).collect(Collectors.toList());
    }

    public static CreditCardAccountsBillsEntity from(CreditCardAccountsEntity account, CreateCreditCardAccountBillData bill) {
        var billEntity = new CreditCardAccountsBillsEntity();
        billEntity.setAccount(account);
        billEntity.setDueDate(bill.getDueDate());
        billEntity.setBillTotalAmount(bill.getBillTotalAmount());
        billEntity.setBillTotalAmountCurrency(bill.getBillTotalAmountCurrency());
        billEntity.setBillMinimumAmount(bill.getBillMinimumAmount());
        billEntity.setBillMinimumAmountCurrency(bill.getBillMinimumAmountCurrency());
        billEntity.setInstalment(bill.isInstalment());

        var financeCharges = bill.getFinanceCharges().stream()
                .map(f -> CreditCardAccountsBillsFinanceChargeEntity.from(billEntity, f))
                .collect(Collectors.toSet());
        billEntity.setFinanceCharges(financeCharges);

        var payments = bill.getPayments().stream()
                .map(p -> CreditCardAccountsBillsPaymentEntity.from(billEntity, p))
                .collect(Collectors.toSet());
        billEntity.setPayments(payments);

        return billEntity;
    }

    public CreditCardAccountsBillsEntity update(CreateCreditCardAccountBillData bill) {
        this.dueDate = bill.getDueDate();
        this.billTotalAmount = bill.getBillTotalAmount();
        this.billTotalAmountCurrency = bill.getBillTotalAmountCurrency();
        this.billMinimumAmount = bill.getBillMinimumAmount();
        this.billMinimumAmountCurrency = bill.getBillMinimumAmountCurrency();
        this.isInstalment = bill.isInstalment();

        var updateFinanceCharges = bill.getFinanceCharges().stream()
                .map(f -> CreditCardAccountsBillsFinanceChargeEntity.from(this, f))
                .collect(Collectors.toSet());
        this.financeCharges.clear();
        this.financeCharges.addAll(updateFinanceCharges);

        var updatePayments = bill.getPayments().stream()
                .map(p -> CreditCardAccountsBillsPaymentEntity.from(this, p))
                .collect(Collectors.toSet());
        this.payments.clear();
        this.payments.addAll(updatePayments);

        return this;
    }

    public ResponseCreditCardAccountBill getAdminCreditCardBillDto() {
        return new ResponseCreditCardAccountBill().data(new ResponseCreditCardAccountBillData()
                .billId(this.billId)
                .dueDate(this.dueDate)
                .billTotalAmount(this.billTotalAmount)
                .billTotalAmountCurrency(this.billTotalAmountCurrency)
                .billMinimumAmount(this.billMinimumAmount)
                .billMinimumAmountCurrency(this.billMinimumAmountCurrency)
                .instalment(this.isInstalment)
                .financeCharges(this.financeCharges.stream().map(CreditCardAccountsBillsFinanceChargeEntity::getDTO).collect(Collectors.toList()))
                .payments(this.payments.stream().map(CreditCardAccountsBillsPaymentEntity::getDTO).collect(Collectors.toList())));
    }
}
