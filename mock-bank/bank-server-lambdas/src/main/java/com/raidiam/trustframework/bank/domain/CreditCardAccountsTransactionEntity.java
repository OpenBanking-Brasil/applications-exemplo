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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
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


    @EqualsAndHashCode.Exclude
    @Column(name = "transaction_date_time")
    private OffsetDateTime transactionDateTime;

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
                .creditDebitType(EnumCreditDebitIndicator.valueOf(this.creditDebitType))
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
                .billPostDate(this.billPostDate)
                .payeeMCC(this.payeeMCC);
    }

    public CreditCardAccountsTransactionV2 getDtoV2() {
        return new CreditCardAccountsTransactionV2()
                .transactionId(this.transactionId.toString())
                .identificationNumber(this.identificationNumber)
                .transactionName(this.transactionName)
                .billId(this.billId.toString())
                .creditDebitType(EnumCreditDebitIndicatorV2.valueOf(this.creditDebitType))
                .transactionType(EnumCreditCardTransactionTypeV2.valueOf(this.transactionType))
                .transactionalAdditionalInfo(this.transactionalAdditionalInfo)
                .paymentType(EnumCreditCardAccountsPaymentTypeV2.valueOf(this.paymentType))
                .feeType(EnumCreditCardAccountFeeV2.valueOf(this.feeType))
                .feeTypeAdditionalInfo(this.feeTypeAdditionalInfo)
                .otherCreditsType(EnumCreditCardAccountsOtherCreditTypeV2.valueOf(this.otherCreditsType))
                .otherCreditsAdditionalInfo(this.otherCreditsAdditionalInfo)
                .chargeIdentificator(new BigDecimal(this.chargeIdentificator))
                .chargeNumber(this.chargeNumber)
                .brazilianAmount(new CreditCardAccountsTransactionBrazilianAmountV2()
                        .amount(BankLambdaUtils.formatAmountV2(this.brazilianAmount))
                        .currency(this.currency))
                .amount(new CreditCardAccountsTransactionAmountV2()
                        .amount(BankLambdaUtils.formatAmountV2(this.amount))
                        .currency(this.currency))
                .transactionDateTime(BankLambdaUtils.formatTransactionDateTime(this.transactionDateTime))
                .billPostDate(this.billPostDate)
                .payeeMCC(this.payeeMCC);
    }

    public static CreditCardAccountsTransactionEntity from(UUID creditCardAccountId, UUID billId, CreateCreditCardAccountTransactionData transaction) {
        var transactionEntity = new CreditCardAccountsTransactionEntity();
        transactionEntity.setBillId(billId);
        transactionEntity.setCreditCardAccountId(creditCardAccountId);
        transactionEntity.setIdentificationNumber(transaction.getIdentificationNumber());
        transactionEntity.setLineName(transaction.getLineName().name());
        transactionEntity.setTransactionName(transaction.getTransactionName());
        transactionEntity.setCreditDebitType(transaction.getCreditDebitType().name());
        transactionEntity.setTransactionType(transaction.getTransactionType().name());
        transactionEntity.setTransactionalAdditionalInfo(transaction.getTransactionalAdditionalInfo());
        transactionEntity.setPaymentType(transaction.getPaymentType().name());
        transactionEntity.setFeeType(transaction.getFeeType().name());
        transactionEntity.setFeeTypeAdditionalInfo(transaction.getFeeTypeAdditionalInfo());
        transactionEntity.setOtherCreditsType(transaction.getOtherCreditsType().name());
        transactionEntity.setOtherCreditsAdditionalInfo(transaction.getOtherCreditsAdditionalInfo());
        transactionEntity.setChargeIdentificator(transaction.getChargeIdentificator());
        transactionEntity.setChargeNumber(transaction.getChargeNumber());
        transactionEntity.setBrazilianAmount(transaction.getBrazilianAmount());
        transactionEntity.setAmount(transaction.getAmount());
        transactionEntity.setCurrency(transaction.getCurrency());
        transactionEntity.setTransactionDateTime(transaction.getTransactionDateTime());
        transactionEntity.setBillPostDate(transaction.getBillPostDate());
        transactionEntity.setPayeeMCC(transaction.getPayeeMCC());
        return transactionEntity;
    }

    public CreditCardAccountsTransactionEntity update(CreateCreditCardAccountTransactionData transaction) {
        this.identificationNumber = transaction.getIdentificationNumber();
        this.lineName = transaction.getLineName().name();
        this.transactionName = transaction.getTransactionName();
        this.creditDebitType = transaction.getCreditDebitType().name();
        this.transactionType = transaction.getTransactionType().name();
        this.transactionalAdditionalInfo = transaction.getTransactionalAdditionalInfo();
        this.paymentType = transaction.getPaymentType().name();
        this.feeType = transaction.getFeeType().name();
        this.feeTypeAdditionalInfo = transaction.getFeeTypeAdditionalInfo();
        this.otherCreditsType = transaction.getOtherCreditsType().name();
        this.otherCreditsAdditionalInfo = transaction.getOtherCreditsAdditionalInfo();
        this.chargeIdentificator = transaction.getChargeIdentificator();
        this.chargeNumber = transaction.getChargeNumber();
        this.brazilianAmount = transaction.getBrazilianAmount();
        this.amount = transaction.getAmount();
        this.currency = transaction.getCurrency();
        this.transactionDateTime = transaction.getTransactionDateTime();
        this.billPostDate = transaction.getBillPostDate();
        this.payeeMCC = transaction.getPayeeMCC();
        return this;
    }

    public ResponseCreditCardAccountTransactionData getAdminCreditCardTransactionDto() {
        return new ResponseCreditCardAccountTransactionData()
                .transactionId(this.transactionId)
                .identificationNumber(this.identificationNumber)
                .lineName(EnumCreditCardAccountsLineName.valueOf(this.lineName))
                .transactionName(this.transactionName)
                .creditDebitType(EnumCreditDebitIndicator.valueOf(this.creditDebitType))
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
                .transactionDateTime(this.transactionDateTime)
                .billPostDate(this.billPostDate)
                .payeeMCC(this.payeeMCC);
    }


}
