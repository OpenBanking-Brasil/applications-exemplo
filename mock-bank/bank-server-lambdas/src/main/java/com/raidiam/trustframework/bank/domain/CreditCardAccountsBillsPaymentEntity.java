package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.CreditCardAccountsBillsPayment;
import com.raidiam.trustframework.mockbank.models.generated.EnumCreditCardAccountsBillingValueType;
import com.raidiam.trustframework.mockbank.models.generated.EnumCreditCardAccountsPaymentMode;
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
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@Audited
@Table(name = "credit_card_accounts_bills_payment")
public class CreditCardAccountsBillsPaymentEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Generated(GenerationTime.INSERT)
    @Type(type = "pg-uuid")
    @Column(name = "payment_id", unique = true, nullable = false, updatable = false, insertable = false, columnDefinition = "uuid NOT NULL DEFAULT uuid_generate_v4()")
    private UUID paymentsId;

    @NotNull
    @Column(name = "value_type", nullable = false)
    private String valueType;

    @NotNull
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @NotNull
    @Column(name = "payment_mode", nullable = false)
    private String paymentMode;

    @NotNull
    @Column(name = "amount", nullable = false)
    private Double amount;

    @NotNull
    @Column(name = "currency", nullable = false)
    private String currency;

    @NotNull
    @Type(type = "pg-uuid")
    @Column(name = "bill_id", nullable = false)
    private UUID billId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", referencedColumnName = "bill_id", insertable = false, nullable = false, updatable = false)
    private CreditCardAccountsBillsEntity bill;

    public CreditCardAccountsBillsPayment getDTO() {
        return new CreditCardAccountsBillsPayment()
                .valueType(EnumCreditCardAccountsBillingValueType.valueOf(this.valueType))
                .paymentDate(this.paymentDate)
                .paymentMode(EnumCreditCardAccountsPaymentMode.valueOf(this.paymentMode))
                .amount(this.amount)
                .currency(this.currency);
    }
}
