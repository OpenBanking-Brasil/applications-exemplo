package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "credit_fixed_incomes_transactions")
public class CreditFixedIncomesTransactionsEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "transaction_id", unique = true, nullable = false, updatable = false, insertable = false)
    private UUID transactionId;

    @Column(name = "type")
    private String type;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "transaction_type_additional_info")
    private String transactionTypeAdditionalInfo;

    @Column(name = "transaction_date")
    private LocalDate transactionDate;

    @Column(name = "transaction_quantity")
    private Double transactionQuantity;

    @Column(name = "transaction_unit_price")
    private Double transactionUnitPrice;

    @Column(name = "transaction_unitPrice_currency")
    private String transactionUnitPriceCurrency;

    @Column(name = "transaction_gross_value")
    private Double transactionGrossValue;

    @Column(name = "transaction_gross_value_currency")
    private String transactionGrossValueCurrency;

    @Column(name = "income_tax_value")
    private Double incomeTaxValue;

    @Column(name = "income_tax_currency")
    private String incomeTaxCurrency;

    @Column(name = "financial_transaction_tax_value")
    private Double financialTransactionTaxValue;

    @Column(name = "financial_transaction_tax_currency")
    private String financialTransactionTaxCurrency;

    @Column(name = "transaction_net_value")
    private Double transactionNetValue;

    @Column(name = "transaction_net_currency")
    private String transactionNetCurrency;

    @Column(name = "remuneration_transaction_rate")
    private Double remunerationTransactionRate;

    @Column(name = "indexer_percentage")
    private Double indexerPercentage;

    @Column(name = "investment_id")
    private UUID investmentId;

    @NotNull
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investment_id", referencedColumnName = "investment_id", nullable = false, updatable = false, insertable = false)
    private CreditFixedIncomesEntity investment;

    public CreditFixedIncomesTransactions getCreditFixedIncomesTransactions() {
        return new CreditFixedIncomesTransactions()
                .type(Type.fromValue(type))
                .transactionType(TransactionType.fromValue(transactionType))
                .typeAdditionalInfo(transactionTypeAdditionalInfo)
                .transactionDate(transactionDate)
                .transactionUnitPrice(new CreditFixedIncomesTransactionsTransactionUnitPrice()
                        .amount(BankLambdaUtils.doubleToString(transactionUnitPrice))
                        .currency(transactionUnitPriceCurrency))
                .transactionQuantity(BankLambdaUtils.doubleToString(transactionQuantity))
                .transactionGrossValue(new TransactionGrossValue()
                        .amount(BankLambdaUtils.doubleToString(transactionGrossValue))
                        .currency(transactionGrossValueCurrency))
                .incomeTax(new CreditFixedIncomesTransactionsIncomeTax()
                        .amount(BankLambdaUtils.doubleToString(incomeTaxValue))
                        .currency(incomeTaxCurrency))
                .financialTransactionTax(new CreditFixedIncomesTransactionsFinancialTransactionTax()
                        .amount(BankLambdaUtils.doubleToString(financialTransactionTaxValue))
                        .currency(financialTransactionTaxCurrency))
                .transactionNetValue(new TransactionNetValue()
                        .amount(BankLambdaUtils.doubleToString(transactionNetValue))
                        .currency(getTransactionNetCurrency()))
                .remunerationTransactionRate(BankLambdaUtils.formatDoubleToLongString(remunerationTransactionRate))
                .indexerPercentage(BankLambdaUtils.formatDoubleToLongString(indexerPercentage))
                .transactionId(transactionId.toString());
    }
}