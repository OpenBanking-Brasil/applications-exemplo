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
@Table(name = "funds_balance")
public class FundsBalanceEntity extends BaseEntity{
    @Id
    @GeneratedValue
    @Column(name = "balance_id", unique = true, nullable = false, updatable = false, insertable = false)
    private UUID balanceId;

    @NotNull
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investment_id")
    private FundsEntity investment;

    @Column(name = "reference_date")
    private LocalDate referenceDate;

    @Column(name = "gross_amount")
    private Double grossAmount;

    @Column(name = "gross_amount_currency")
    private String grossAmountCurrency;

    @Column(name = "net_amount")
    private Double netAmount;

    @Column(name = "net_amount_currency")
    private String netAmountCurrency;

    @Column(name = "income_tax_provision_amount")
    private Double incomeTaxProvisionAmount;

    @Column(name = "income_tax_provision_currency")
    private String incomeTaxProvisionCurrency;

    @Column(name = "financial_transaction_tax_provision_amount")
    private Double financialTransactionTaxProvisionAmount;

    @Column(name = "financial_transaction_tax_provision_currency")
    private String financialTransactionTaxProvisionCurrency;

    @Column(name = "blocked_amount")
    private Double blockedAmount;

    @Column(name = "blocked_amount_currency")
    private String blockedAmountCurrency;

    @Column(name = "quota_gross_price_value_amount")
    private Double quotaGrossPriceValueAmount;

    @Column(name = "quota_gross_price_value_amount_currency")
    private String quotaGrossPriceValueCurrency;

    @Column(name = "quota_quantity")
    private Double quotaQuantity;

    public ResponseFundsBalancesData getResponseFundsBalancesData() {
        return new ResponseFundsBalancesData()
                .referenceDate(referenceDate)
                .grossAmount(new ResponseFundsBalancesDataGrossAmount()
                        .amount(BankLambdaUtils.doubleToString(grossAmount)).currency(grossAmountCurrency))
                .netAmount(new ResponseFundsBalancesDataNetAmount()
                        .amount(BankLambdaUtils.doubleToString(netAmount)).currency(netAmountCurrency))
                .incomeTaxProvision(new ResponseFundsBalancesDataIncomeTaxProvision()
                        .amount(BankLambdaUtils.doubleToString(incomeTaxProvisionAmount)).currency(incomeTaxProvisionCurrency))
                .financialTransactionTaxProvision(new ResponseFundsBalancesDataFinancialTransactionTaxProvision()
                        .amount(BankLambdaUtils.doubleToString(financialTransactionTaxProvisionAmount)).currency(financialTransactionTaxProvisionCurrency))
                .blockedAmount(new ResponseFundsBalancesDataBlockedAmount()
                        .amount(BankLambdaUtils.doubleToString(blockedAmount)).currency(blockedAmountCurrency))
                .quotaQuantity(BankLambdaUtils.doubleToString(quotaQuantity))
                .quotaGrossPriceValue(new ResponseFundsBalancesDataQuotaGrossPriceValue()
                        .amount(BankLambdaUtils.doubleToString(quotaGrossPriceValueAmount)).currency(quotaGrossPriceValueCurrency));
    }

}
