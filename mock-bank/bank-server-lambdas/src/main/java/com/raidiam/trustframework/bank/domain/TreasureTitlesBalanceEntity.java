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
@Table(name = "treasure_titles_balance")
public class TreasureTitlesBalanceEntity extends BaseEntity{
    @Id
    @GeneratedValue
    @Column(name = "balance_id", unique = true, nullable = false, updatable = false, insertable = false)
    private UUID balanceId;

    @NotNull
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investment_id")
    private TreasureTitlesEntity investment;

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

    public ResponseTreasureTitlesBalancesData getResponseTreasureTitlesBalancesData() {
        return new ResponseTreasureTitlesBalancesData()
                .referenceDateTime(referenceDateTime.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime())
                .quantity(BankLambdaUtils.formatDoubleToLongString(quantity))
                .updatedUnitPrice(new ResponseTreasureTitlesBalancesDataUpdatedUnitPrice()
                        .amount(BankLambdaUtils.doubleToString(updatedUnitPrice)).currency(updatedUnitPriceCurrency))
                .grossAmount(new ResponseBankFixedIncomesBalancesDataGrossAmount()
                        .amount(BankLambdaUtils.doubleToString(grossAmount)).currency(grossAmountCurrency))
                .netAmount(new ResponseBankFixedIncomesBalancesDataNetAmount()
                        .amount(BankLambdaUtils.doubleToString(netAmount)).currency(netAmountCurrency))
                .incomeTax(new ResponseBankFixedIncomesBalancesDataIncomeTax()
                        .amount(BankLambdaUtils.doubleToString(incomeTaxAmount)).currency(incomeTaxCurrency))
                .financialTransactionTax(new ResponseBankFixedIncomesBalancesDataFinancialTransactionTax()
                        .amount(BankLambdaUtils.doubleToString(financialTransactionTaxAmount)).currency(financialTransactionTaxCurrency))
                .blockedBalance(new ResponseBankFixedIncomesBalancesDataBlockedBalance()
                        .amount(BankLambdaUtils.doubleToString(blockedBalance)).currency(blockedBalanceCurrency))
                .purchaseUnitPrice(new ResponseTreasureTitlesBalancesDataPurchaseUnitPrice()
                        .amount(BankLambdaUtils.doubleToString(purchaseUnitPrice)).currency(purchaseUnitPriceCurrency));
    }

}
