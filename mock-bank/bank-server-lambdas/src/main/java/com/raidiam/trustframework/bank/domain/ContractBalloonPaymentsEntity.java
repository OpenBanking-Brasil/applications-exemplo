
package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.FinancingsBalloonPayment;
import com.raidiam.trustframework.mockbank.models.generated.InvoiceFinancingsBalloonPayment;
import com.raidiam.trustframework.mockbank.models.generated.LoansBalloonPayment;
import com.raidiam.trustframework.mockbank.models.generated.UnarrangedAccountOverdraftBalloonPayment;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "balloon_payments")
public class ContractBalloonPaymentsEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "balloon_payments_id", unique = true, nullable = false, updatable = false, insertable = false, columnDefinition = "uuid NOT NULL DEFAULT uuid_generate_v4()")
    private UUID balloonPaymentsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "contract_id", referencedColumnName = "contract_id", insertable = false, nullable = false, updatable = false)
    private ContractEntity contract;

    @Type(type = "pg-uuid")
    @NotNull
    @Column(name = "contract_id", nullable = false, updatable = false)
    private UUID contractId;

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
}

