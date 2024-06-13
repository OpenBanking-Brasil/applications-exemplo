package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.*;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Audited
@Entity
@Table(name = "account_transactions")
public class AccountTransactionsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_transaction_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer accountTransactionId;

    @Column(name = "account_id")
    @Type(type = "pg-uuid")
    private UUID accountId;

    @Generated(GenerationTime.INSERT)
    @Column(name = "transaction_id")
    @Type(type = "pg-uuid")
    private UUID transactionId;

    @Column(name = "completed_authorised_payment_type")
    private String completedAuthorisedPaymentType;

    @Column(name = "credit_debit_type")
    private String creditDebitType;

    @Column(name = "transaction_name")
    private String transactionName;

    @Column(name = "type")
    private String type;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "transaction_currency")
    private String transactionCurrency;

    @EqualsAndHashCode.Exclude

    @Column(name = "transaction_date_time")
    private OffsetDateTime transactionDateTime;

    @Column(name = "partie_cnpj_cpf")
    private String partieCnpjCpf;

    @Column(name = "partie_person_type")
    private String partiePersonType;

    @Column(name = "partie_compe_code")
    private String partieCompeCode;

    @Column(name = "partie_branch_code")
    private String partieBranchCode;

    @Column(name = "partie_number")
    private String partieNumber;

    @Column(name = "partie_check_digit")
    private String partieCheckDigit;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "account_id", insertable = false, nullable = false, updatable = false)
    private AccountEntity account;


    @EqualsAndHashCode.Include
    private Instant compareTransactionDate() {
        return Optional.ofNullable(transactionDateTime).map(BankLambdaUtils::offsetDateToDate).map(Date::toInstant).orElse(null);
    }

    public static AccountTransactionsEntity from(AccountTransactionsData transactionsData, UUID accountId) {
        var entity = new AccountTransactionsEntity();
        entity.accountId = accountId;
        entity.completedAuthorisedPaymentType = transactionsData.getCompletedAuthorisedPaymentType().name();
        entity.creditDebitType = transactionsData.getCreditDebitType().name();
        entity.transactionName = transactionsData.getTransactionName();
        entity.type = transactionsData.getType().name();
        entity.amount = transactionsData.getAmount();
        entity.transactionCurrency = transactionsData.getTransactionCurrency();
        entity.transactionDateTime = OffsetDateTime.parse(transactionsData.getTransactionDateTime());
        entity.partieCnpjCpf = transactionsData.getPartieCnpjCpf();
        entity.partiePersonType = transactionsData.getPartiePersonType().name();
        entity.partieCompeCode = transactionsData.getPartieCompeCode();
        entity.partieBranchCode = transactionsData.getPartieBranchCode();
        entity.partieNumber = transactionsData.getPartieNumber();
        entity.partieCheckDigit = transactionsData.getPartieCheckDigit();
        return entity;
    }

    public AccountTransactionsData getDto() {
        return new AccountTransactionsData()
                .transactionId(this.transactionId != null? this.getTransactionId().toString() : null)
                .completedAuthorisedPaymentType(EnumCompletedAuthorisedPaymentIndicator.valueOf(this.completedAuthorisedPaymentType))
                .creditDebitType(EnumCreditDebitIndicator.valueOf(this.creditDebitType))
                .transactionName(this.transactionName)
                .type(EnumTransactionTypes.valueOf(this.type))
                .amount(this.amount)
                .transactionCurrency(this.transactionCurrency)
                .transactionDateTime(this.transactionDateTime.toString())
                .partieCnpjCpf(this.partieCnpjCpf)
                .partiePersonType(EnumPartiePersonType.valueOf(this.getPartiePersonType()))
                .partieCompeCode(this.partieCompeCode)
                .partieBranchCode(this.partieBranchCode)
                .partieNumber(this.partieNumber)
                .partieCheckDigit(this.partieCheckDigit);
    }

    public AccountTransactionsDataV2 getDtoV2() {
        return new AccountTransactionsDataV2()
                .transactionId(this.transactionId != null? this.getTransactionId().toString() : null)
                .completedAuthorisedPaymentType(EnumCompletedAuthorisedPaymentIndicatorV2.valueOf(this.completedAuthorisedPaymentType))
                .creditDebitType(EnumCreditDebitIndicatorV2.valueOf(this.creditDebitType))
                .transactionName(this.transactionName)
                .type(EnumTransactionTypesV2.valueOf(this.type))
                .transactionAmount(new AccountTransactionsDataAmountV2()
                        .amount(BankLambdaUtils.formatAmountV2(this.amount))
                        .currency(this.transactionCurrency))
                .transactionDateTime(BankLambdaUtils.formatTransactionDateTime(this.transactionDateTime))
                .partieCnpjCpf(this.partieCnpjCpf)
                .partiePersonType(EnumPartiePersonTypeV2.valueOf(this.getPartiePersonType()))
                .partieCompeCode(this.partieCompeCode)
                .partieBranchCode(this.partieBranchCode)
                .partieNumber(this.partieNumber)
                .partieCheckDigit(this.partieCheckDigit);
    }

    public static AccountTransactionsEntity from(CreateAccountTransactionData transaction, UUID accountId) {
        var transactionEntity = new AccountTransactionsEntity();
        transactionEntity.setAccountId(accountId);
        transactionEntity.setCompletedAuthorisedPaymentType(transaction.getCompletedAuthorisedPaymentType().name());
        transactionEntity.setCreditDebitType(transaction.getCreditDebitType().name());
        transactionEntity.setType(transaction.getType().name());
        transactionEntity.setAmount(transaction.getAmount());
        transactionEntity.setTransactionCurrency(transaction.getTransactionCurrency());
        transactionEntity.setTransactionDateTime(transaction.getTransactionDateTime());
        transactionEntity.setTransactionName(transaction.getTransactionName());
        transactionEntity.setPartieCnpjCpf(transaction.getPartieCnpjCpf());
        transactionEntity.setPartiePersonType(transaction.getPartiePersonType().name());
        transactionEntity.setPartieCompeCode(transaction.getPartieCompeCode());
        transactionEntity.setPartieBranchCode(transaction.getPartieBranchCode());
        transactionEntity.setPartieNumber(transaction.getPartieNumber());
        transactionEntity.setPartieCheckDigit(transaction.getPartieCheckDigit());
        return transactionEntity;
    }

    public AccountTransactionsEntity update(CreateAccountTransactionData transactionsDto) {
        this.completedAuthorisedPaymentType = transactionsDto.getCompletedAuthorisedPaymentType().name();
        this.creditDebitType = transactionsDto.getCreditDebitType().name();
        this.transactionName = transactionsDto.getTransactionName();
        this.type = transactionsDto.getType().name();
        this.amount = transactionsDto.getAmount();
        this.transactionCurrency = transactionsDto.getTransactionCurrency();
        this.transactionDateTime = transactionsDto.getTransactionDateTime();
        this.partieCnpjCpf = transactionsDto.getPartieCnpjCpf();
        this.partiePersonType = transactionsDto.getPartiePersonType().name();
        this.partieCompeCode = transactionsDto.getPartieCompeCode();
        this.partieBranchCode = transactionsDto.getPartieBranchCode();
        this.partieNumber = transactionsDto.getPartieNumber();
        this.partieCheckDigit = transactionsDto.getPartieCheckDigit();
        return this;
    }

    public ResponseAccountTransaction getAdminAccountTransactionDto() {
        return new ResponseAccountTransaction().data(new ResponseAccountTransactionData()
                .transactionId(this.transactionId.toString())
                .completedAuthorisedPaymentType(EnumCompletedAuthorisedPaymentIndicator.valueOf(this.completedAuthorisedPaymentType))
                .creditDebitType(EnumCreditDebitIndicator.valueOf(this.creditDebitType))
                .type(EnumTransactionTypes.valueOf(this.type))
                .amount(this.amount)
                .transactionCurrency(this.transactionCurrency)
                .transactionDateTime(this.transactionDateTime)
                .transactionName(this.transactionName)
                .partieCnpjCpf(this.partieCnpjCpf)
                .partiePersonType(EnumPartiePersonType.valueOf(this.partiePersonType))
                .partieCompeCode(this.partieCompeCode)
                .partieBranchCode(this.partieBranchCode)
                .partieNumber(this.partieNumber)
                .partieCheckDigit(this.partieCheckDigit));
    }
}
