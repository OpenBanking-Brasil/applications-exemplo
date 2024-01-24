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
@Table(name = "funds_transactions")
public class FundsTransactionsEntity extends BaseEntity {

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

    @Column(name = "transaction_conversion_date")
    private LocalDate transactionConversionDate;

    @Column(name = "transaction_quota_quantity")
    private Double transactionQuotaQuantity;

    @Column(name = "transaction_quota_price")
    private Double transactionQuotaPrice;

    @Column(name = "transaction_quota_price_currency")
    private String transactionQuotaPriceCurrency;

    @Column(name = "transaction_value")
    private Double transactionValue;

    @Column(name = "transaction_value_currency")
    private String transactionValueCurrency;

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

    @Column(name = "transaction_exit_fee_amount")
    private Double transactionExitFeeAmount;

    @Column(name = "transaction_exit_fee_currency")
    private String transactionExitFeeCurrency;

    @Column(name = "investment_id")
    private UUID investmentId;

    @NotNull
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investment_id", referencedColumnName = "investment_id", nullable = false, updatable = false, insertable = false)
    private FundsEntity investment;

    public ResponseFundsTransactionsData getResponseFundsTransactionsData() {
        return new ResponseFundsTransactionsData()
                .type(EnumIncomeMovementType.fromValue(type))
                .transactionType(EnumFundsTransactionsTransactionType.fromValue(transactionType))
                .transactionTypeAdditionalInfo(transactionTypeAdditionalInfo)
                .transactionConversionDate(transactionConversionDate)
                .transactionQuotaPrice(new ResponseFundsTransactionsDataTransactionQuotaPrice()
                        .amount(BankLambdaUtils.doubleToString(transactionQuotaPrice))
                        .currency(transactionQuotaPriceCurrency))
                .transactionQuotaQuantity(BankLambdaUtils.doubleToString(transactionQuotaQuantity))
                .transactionValue(new ResponseFundsTransactionsDataTransactionValue()
                        .amount(BankLambdaUtils.doubleToString(transactionValue))
                        .currency(transactionValueCurrency))
                .transactionGrossValue(new ResponseFundsTransactionsDataTransactionGrossValue()
                        .amount(BankLambdaUtils.doubleToString(transactionGrossValue))
                        .currency(transactionGrossValueCurrency))
                .incomeTax(new ResponseFundsTransactionsDataIncomeTax()
                        .amount(BankLambdaUtils.doubleToString(incomeTaxValue))
                        .currency(incomeTaxCurrency))
                .financialTransactionTax(new ResponseFundsTransactionsDataFinancialTransactionTax()
                        .amount(BankLambdaUtils.doubleToString(financialTransactionTaxValue))
                        .currency(financialTransactionTaxCurrency))
                .transactionNetValue(new ResponseFundsTransactionsDataTransactionNetValue()
                        .amount(BankLambdaUtils.doubleToString(transactionNetValue))
                        .currency(getTransactionNetCurrency()))
                .transactionExitFee(new ResponseFundsTransactionsDataTransactionExitFee()
                        .amount(BankLambdaUtils.doubleToString(transactionExitFeeAmount))
                        .currency(getTransactionExitFeeCurrency()))
                .transactionId(transactionId.toString());
    }
}