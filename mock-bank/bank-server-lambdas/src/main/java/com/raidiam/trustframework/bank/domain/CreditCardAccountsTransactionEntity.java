package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.*;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@Audited
@Table(name = "credit_card_accounts_transaction")
public class CreditCardAccountsTransactionEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Generated(GenerationTime.INSERT)
    @Type(type = "pg-uuid")
    @Column(name = "transaction_id", unique = true, nullable = false, updatable = false, insertable = false, columnDefinition = "uuid NOT NULL DEFAULT uuid_generate_v4()")
    private UUID transactionId;

    @NotNull
    @Column(name = "identification_number", nullable = false)
    private String identificationNumber;

    @NotNull
    @Column(name = "line_name", nullable = false)
    private String lineName;

    @NotNull
    @Column(name = "transaction_name", nullable = false)
    private String transactionName;

    @NotNull
    @Type(type = "pg-uuid")
    @Column(name = "bill_id", nullable = false)
    private UUID billId;

    @NotNull
    @Column(name = "credit_debit_type", nullable = false)
    private String creditDebitType;

    @NotNull
    @Column(name = "transaction_type", nullable = false)
    private String transactionType;

    @NotNull
    @Column(name = "transactional_additional_info", nullable = false)
    private String transactionalAdditionalInfo;

    @NotNull
    @Column(name = "payment_type", nullable = false)
    private String paymentType;

    @NotNull
    @Column(name = "fee_type", nullable = false)
    private String feeType;

    @NotNull
    @Column(name = "fee_type_additional_info", nullable = false)
    private String feeTypeAdditionalInfo;

    @NotNull
    @Column(name = "other_credits_type", nullable = false)
    private String otherCreditsType;

    @NotNull
    @Column(name = "other_credits_additional_info", nullable = false)
    private String otherCreditsAdditionalInfo;

    @NotNull
    @Column(name = "charge_identificator", nullable = false)
    private String chargeIdentificator;

    @NotNull
    @Column(name = "charge_number", nullable = false)
    private BigDecimal chargeNumber;

    @NotNull
    @Column(name = "brazilian_amount", nullable = false)
    private Double brazilianAmount;

    @NotNull
    @Column(name = "amount", nullable = false)
    private Double amount;

    @NotNull
    @Column(name = "currency", nullable = false)
    private String currency;

    @NotNull
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @NotNull
    @Column(name = "bill_post_date", nullable = false)
    private LocalDate billPostDate;

    @NotNull
    @Column(name = "payee_mcc", nullable = false)
    private BigDecimal payeeMCC;

    @NotNull
    @Type(type = "pg-uuid")
    @Column(name = "credit_card_account_id", nullable = false)
    private UUID creditCardAccountId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", referencedColumnName = "bill_id", insertable = false, nullable = false, updatable = false)
    private CreditCardAccountsBillsEntity bill;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_card_account_id", referencedColumnName = "credit_card_account_id", insertable = false, nullable = false, updatable = false)
    private CreditCardAccountsEntity account;

    public CreditCardAccountsTransaction getDTO() {
        return new CreditCardAccountsTransaction()
                .transactionId(this.transactionId.toString())
                .identificationNumber(this.identificationNumber)
                .lineName(EnumCreditCardAccountsLineName.valueOf(this.lineName))
                .transactionName(this.transactionName)
                .billId(this.billId.toString())
                .creditDebitType(EnumCreditDebitIndicator1.valueOf(this.creditDebitType))
                .transactionType(EnumCreditCardTransactionType.valueOf(this.transactionType))
                .transactionalAdditionalInfo(this.transactionalAdditionalInfo)
                .paymentType(EnumCreditCardAccountsPaymentType.valueOf(this.paymentType))
                .feeType(EnumCreditCardAccountFee.valueOf(this.feeType))
                .feeTypeAdditionalInfo(this.feeTypeAdditionalInfo)
                .otherCreditsType(EnumCreditCardAccountsOtherCreditType.valueOf(this.otherCreditsType))
                .otherCreditsAdditionalInfo(this.otherCreditsAdditionalInfo)
                .chargeIdentificator(this.chargeIdentificator)
                .chargeNumber(this.chargeNumber)
                .brazilianAmount(this.brazilianAmount)
                .amount(this.amount)
                .currency(this.currency)
                .transactionDate(this.transactionDate)
                .billPostDate(this.billPostDate)
                .payeeMCC(this.payeeMCC);
    }
}
