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
import java.time.ZoneOffset;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "credit_fixed_incomes_balance")
public class CreditFixedIncomesBalanceEntity extends BaseEntity{
    @Id
    @GeneratedValue
    @Column(name = "balance_id", unique = true, nullable = false, updatable = false, insertable = false)
    private UUID balanceId;

    @NotNull
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investment_id")
    private CreditFixedIncomesEntity investment;

    @Column(name = "reference_date_time")
    private LocalDate referenceDateTime;

    @Column(name = "updated_unit_price")
    private Double updatedUnitPrice;

    @Column(name = "updated_unit_price_currency")
    private String updatedUnitPriceCurrency;

    @Column(name = "gross_amount")
    private Double grossAmount;

    @Column(name = "gross_amount_currency")
    private String grossAmountCurrency;

    @Column(name = "net_amount")
    private Double netAmount;

    @Column(name = "net_amount_currency")
    private String netAmountCurrency;

    @Column(name = "income_tax_amount")
    private Double incomeTaxAmount;

    @Column(name = "income_tax_currency")
    private String incomeTaxCurrency;

    @Column(name = "financial_transaction_tax_amount")
    private Double financialTransactionTaxAmount;

    @Column(name = "financial_transaction_tax_currency")
    private String financialTransactionTaxCurrency;

    @Column(name = "blocked_balance")
    private Double blockedBalance;

    @Column(name = "blocked_balance_currency")
    private String blockedBalanceCurrency;

    @Column(name = "purchase_unit_price")
    private Double purchaseUnitPrice;

    @Column(name = "purchase_unit_price_currency")
    private String purchaseUnitPriceCurrency;

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "pre_fixed_rate")
    private Double preFixedRate;

    @Column(name = "post_fixed_indexer_percentage")
    private Double postFixedIndexerPercentage;
    public ResponseCreditFixedIncomesBalancesData getCreditFixedIncomesBalancesData() {
        return new ResponseCreditFixedIncomesBalancesData()
                .referenceDateTime(referenceDateTime.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime())
                .quantity(BankLambdaUtils.formatDoubleToLongString(quantity))
                .updatedUnitPrice(new UpdatedUnitPriceunit()
                        .amount(BankLambdaUtils.doubleToString(updatedUnitPrice)).currency(updatedUnitPriceCurrency))
                .grossAmount(new GrossAmount()
                        .amount(BankLambdaUtils.doubleToString(grossAmount)).currency(grossAmountCurrency))
                .netAmount(new NetAmount()
                        .amount(BankLambdaUtils.doubleToString(netAmount)).currency(netAmountCurrency))
                .incomeTax(new IncomeTax()
                        .amount(BankLambdaUtils.doubleToString(incomeTaxAmount)).currency(incomeTaxCurrency))
                .financialTransactionTax(new FinancialTransactionTax()
                        .amount(BankLambdaUtils.doubleToString(financialTransactionTaxAmount)).currency(financialTransactionTaxCurrency))
                .blockedBalance(new BlockedBalance()
                        .amount(BankLambdaUtils.doubleToString(blockedBalance)).currency(blockedBalanceCurrency))
                .purchaseUnitPrice(new PurchaseUnitPrice()
                        .amount(BankLambdaUtils.doubleToString(purchaseUnitPrice)).currency(purchaseUnitPriceCurrency))
                .preFixedRate(BankLambdaUtils.formatDoubleToLongString(preFixedRate))
                .postFixedIndexerPercentage(BankLambdaUtils.formatDoubleToLongString(postFixedIndexerPercentage));
    }

}
