package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.ResponseFundsBalancesDataBlockedAmount;
import com.raidiam.trustframework.mockbank.models.generated.ResponseFundsBalancesDataGrossAmount;
import com.raidiam.trustframework.mockbank.models.generated.ResponseVariableIncomesBalanceData;
import com.raidiam.trustframework.mockbank.models.generated.ResponseVariableIncomesBalanceDataClosingPrice;
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
@Table(name = "variable_incomes_balance")
public class VariableIncomesBalanceEntity extends BaseEntity{
    @Id
    @GeneratedValue
    @Column(name = "balance_id", unique = true, nullable = false, updatable = false, insertable = false)
    private UUID balanceId;

    @NotNull
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investment_id")
    private VariableIncomesEntity investment;

    @Column(name = "reference_date")
    private LocalDate referenceDate;

    @Column(name = "price_factor")
    private Double priceFactor;

    @Column(name = "gross_amount")
    private Double grossAmount;

    @Column(name = "gross_amount_currency")
    private String grossAmountCurrency;

    @Column(name = "blocked_balance")
    private Double blockedBalance;

    @Column(name = "blocked_balance_currency")
    private String blockedBalanceCurrency;

    @Column(name = "closing_price")
    private Double closingPrice;

    @Column(name = "closing_price_currency")
    private String closingPriceCurrency;

    @Column(name = "quantity")
    private Double quantity;

    public ResponseVariableIncomesBalanceData getResponseVariableIncomesBalanceData() {
        return new ResponseVariableIncomesBalanceData()
                .referenceDate(referenceDate)
                .quantity(BankLambdaUtils.formatDoubleToLongString(quantity))
                .grossAmount(new ResponseFundsBalancesDataGrossAmount()
                        .amount(BankLambdaUtils.doubleToString(grossAmount)).currency(grossAmountCurrency))
                .priceFactor(BankLambdaUtils.doubleToString(priceFactor))
                .blockedBalance(new ResponseFundsBalancesDataBlockedAmount()
                        .amount(BankLambdaUtils.doubleToString(blockedBalance)).currency(blockedBalanceCurrency))
                .closingPrice(new ResponseVariableIncomesBalanceDataClosingPrice()
                        .amount(BankLambdaUtils.doubleToString(closingPrice)).currency(closingPriceCurrency));
    }

}
