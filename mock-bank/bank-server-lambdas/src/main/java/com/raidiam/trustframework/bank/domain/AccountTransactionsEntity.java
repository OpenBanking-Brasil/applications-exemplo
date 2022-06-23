package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Audited
@Entity
@Table(name = "account_transactions")
public class AccountTransactionsEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "account_transaction_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer accountTransactionId;

    @Column(name = "account_id")
    @Type(type = "pg-uuid")
    private UUID accountId;

    @Column(name = "transaction_id")
    private String transactionId;

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
    @Column(name = "transaction_date")
    private LocalDate transactionDate;

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
    private Instant compareTransactionDate () {
        return Optional.ofNullable(transactionDate).map(BankLambdaUtils::localDateToDate).map(Date::toInstant).orElse(null);
    }

    public static AccountTransactionsEntity from (AccountTransactionsData transactionsData, UUID accountId) {
        var entity = new AccountTransactionsEntity();
        entity.accountId = accountId;
        entity.transactionId = transactionsData.getTransactionId();
        entity.completedAuthorisedPaymentType = transactionsData.getCompletedAuthorisedPaymentType().name();
        entity.creditDebitType = transactionsData.getCreditDebitType().name();
        entity.transactionName = transactionsData.getTransactionName();
        entity.type = transactionsData.getType().name();
        entity.amount = transactionsData.getAmount();
        entity.transactionCurrency = transactionsData.getTransactionCurrency();
        entity.transactionDate = LocalDate.parse(transactionsData.getTransactionDate());
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
                .transactionId(this.transactionId)
                .completedAuthorisedPaymentType(EnumCompletedAuthorisedPaymentIndicator.valueOf(this.completedAuthorisedPaymentType))
                .creditDebitType(EnumCreditDebitIndicator.valueOf(this.creditDebitType))
                .transactionName(this.transactionName)
                .type(EnumTransactionTypes.valueOf(this.type))
                .amount(this.amount)
                .transactionCurrency(this.transactionCurrency)
                .transactionDate(this.transactionDate.toString())
                .partieCnpjCpf(this.partieCnpjCpf)
                .partiePersonType(EnumPartiePersonType.valueOf(this.getPartiePersonType()))
                .partieCompeCode(this.partieCompeCode)
                .partieBranchCode(this.partieBranchCode)
                .partieNumber(this.partieNumber)
                .partieCheckDigit(this.partieCheckDigit);
    }
}
