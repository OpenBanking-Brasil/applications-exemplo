package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "balloon_payments")
public class ContractBalloonPaymentsEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "balloon_payments_id", unique = true, nullable = false, updatable = false, insertable = false, columnDefinition = "uuid NOT NULL DEFAULT uuid_generate_v4()")
    private UUID balloonPaymentsId;

    @NotNull
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id")
    private ContractEntity contract;

    @NotNull
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @NotNull
    @Column(name = "currency", nullable = false)
    private String currency;

    @NotNull
    @Column(name = "amount", nullable = false)
    private double amount;

    public LoansBalloonPayment getLoansDTO() {
        return new LoansBalloonPayment()
                .dueDate(this.dueDate)
                .currency(this.currency)
                .amount(this.amount);
    }

    public FinancingsBalloonPayment getFinancingsDTO() {
        return new FinancingsBalloonPayment()
                .dueDate(this.dueDate)
                .currency(this.currency)
                .amount(this.amount);
    }

    public UnarrangedAccountOverdraftBalloonPayment getUnarrangedAccountsOverdraftDTO() {
        return new UnarrangedAccountOverdraftBalloonPayment()
                .dueDate(this.dueDate)
                .currency(this.currency)
                .amount(this.amount);
    }

    public InvoiceFinancingsBalloonPayment getInvoiceFinancingsDTO() {
        return new InvoiceFinancingsBalloonPayment()
                .dueDate(this.dueDate)
                .currency(this.currency)
                .amount(this.amount);
    }

    public UnarrangedAccountOverdraftBalloonPaymentV2 getOverdraftBalloonPaymentV2() {
        return new UnarrangedAccountOverdraftBalloonPaymentV2()
                .dueDate(this.dueDate)
                .amount(new UnarrangedAccountOverdraftBalloonPaymentAmountV2()
                        .amount(BankLambdaUtils.formatAmountV2(this.amount))
                        .currency(this.currency));
    }

    public FinancingsBalloonPaymentV2 getFinancingsBalloonPaymentV2() {
        return new FinancingsBalloonPaymentV2()
                .dueDate(this.dueDate)
                .amount(new FinancingsBalloonPaymentAmountV2()
                        .amount(BankLambdaUtils.formatAmountV2(this.amount))
                        .currency(this.currency));
    }

    public InvoiceFinancingsBalloonPaymentV2 getInvoiceFinancingsBalloonPaymentV2() {
        return new InvoiceFinancingsBalloonPaymentV2()
                .dueDate(this.dueDate)
                .amount(new InvoiceFinancingsBallonPaymentAmountV2()
                        .amount(BankLambdaUtils.formatAmountV2(this.amount))
                        .currency(this.currency));
    }

    public LoansBalloonPaymentV2 getLoansBalloonPaymentV2() {
        return new LoansBalloonPaymentV2()
                .dueDate(this.dueDate)
                .amount(new LoansBalloonPaymentAmountV2()
                        .amount(BankLambdaUtils.formatAmountV2(this.amount))
                        .currency(this.currency));
    }

    public static ContractBalloonPaymentsEntity from(ContractEntity contract, ContractBalloonPayment balloonPayment) {
        var balloonPaymentEntity = new ContractBalloonPaymentsEntity();
        balloonPaymentEntity.setContract(contract);
        balloonPaymentEntity.setDueDate(balloonPayment.getDueDate());
        balloonPaymentEntity.setCurrency(balloonPayment.getCurrency());
        balloonPaymentEntity.setAmount(balloonPayment.getAmount());
        return balloonPaymentEntity;
    }

    public ContractBalloonPayment getContractBalloonPayment() {
        return new ContractBalloonPayment()
                .dueDate(this.dueDate)
                .currency(this.currency)
                .amount(this.amount);
    }
}

