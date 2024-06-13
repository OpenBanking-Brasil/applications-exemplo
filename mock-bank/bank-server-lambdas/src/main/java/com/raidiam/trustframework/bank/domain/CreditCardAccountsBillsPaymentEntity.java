package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.CreditCardAccountsBillsPayment;
import com.raidiam.trustframework.mockbank.models.generated.CreditCardAccountsBillsPaymentV2;
import com.raidiam.trustframework.mockbank.models.generated.EnumCreditCardAccountsBillingValueType;
import com.raidiam.trustframework.mockbank.models.generated.EnumCreditCardAccountsBillingValueTypeV2;
import com.raidiam.trustframework.mockbank.models.generated.EnumCreditCardAccountsPaymentMode;
import com.raidiam.trustframework.mockbank.models.generated.EnumCreditCardAccountsPaymentModeV2;
import lombok.*;
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
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bill_id")
    private CreditCardAccountsBillsEntity bill;

    public CreditCardAccountsBillsPayment getDTO() {
        return new CreditCardAccountsBillsPayment()
                .valueType(EnumCreditCardAccountsBillingValueType.valueOf(this.valueType))
                .paymentDate(this.paymentDate)
                .paymentMode(EnumCreditCardAccountsPaymentMode.valueOf(this.paymentMode))
                .amount(this.amount)
                .currency(this.currency);
    }

    public CreditCardAccountsBillsPaymentV2 getDTOV2() {
        return new CreditCardAccountsBillsPaymentV2()
                .valueType(EnumCreditCardAccountsBillingValueTypeV2.valueOf(this.valueType))
                .paymentDate(this.paymentDate)
                .paymentMode(EnumCreditCardAccountsPaymentModeV2.valueOf(this.paymentMode))
                .amount(BankLambdaUtils.formatAmountV2(this.amount))
                .currency(this.currency);
    }


    public static CreditCardAccountsBillsPaymentEntity from(CreditCardAccountsBillsEntity bill, CreditCardAccountsBillsPayment billPayment) {
        var billPaymentEntity = new CreditCardAccountsBillsPaymentEntity();
        billPaymentEntity.setBill(bill);
        billPaymentEntity.setValueType(billPayment.getValueType().name());
        billPaymentEntity.setPaymentDate(billPayment.getPaymentDate());
        billPaymentEntity.setPaymentMode(billPayment.getPaymentMode().name());
        billPaymentEntity.setAmount(billPayment.getAmount());
        billPaymentEntity.setCurrency(billPayment.getCurrency());
        return billPaymentEntity;
    }
}
