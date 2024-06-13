package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "variable_incomes_broker_notes")
public class VariableIncomesBrokerNotesEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "broker_note_id", unique = true, nullable = false, updatable = false, insertable = false)
    private UUID brokerNoteId;

    @Column(name = "broker_note_number")
    private String brokerNoteNumber;

    @Column(name = "gross_value_amount")
    private Double grossValueAmount;

    @Column(name = "gross_value_currency")
    private String grossValueCurrency;

    @Column(name = "brokerage_fee_amount")
    private Double brokerageFeeAmount;

    @Column(name = "brokerage_fee_currency")
    private String brokerageFeeCurrency;

    @Column(name = "clearing_settlement_fee_amount")
    private Double clearingSettlementFeeAmount;

    @Column(name = "clearing_settlement_fee_currency")
    private String clearingSettlementFeeCurrency;

    @Column(name = "clearing_registration_fee_amount")
    private Double clearingRegistrationFeeAmount;

    @Column(name = "clearing_registration_fee_currency")
    private String clearingRegistrationFeeCurrency;

    @Column(name = "stock_exchange_asset_trade_notice_fee_amount")
    private Double stockExchangeAssetTradeNoticeFeeAmount;

    @Column(name = "stock_exchange_asset_trade_notice_fee_currency")
    private String stockExchangeAssetTradeNoticeFeeCurrency;

    @Column(name = "stock_exchange_fee_amount")
    private Double stockExchangeFeeAmount;

    @Column(name = "stock_exchange_fee_currency")
    private String stockExchangeFeeCurrency;

    @Column(name = "clearing_custody_fee_amount")
    private Double clearingCustodyFeeAmount;

    @Column(name = "clearing_custody_fee_currency")
    private String clearingCustodyFeeCurrency;

    @Column(name = "taxes_amount")
    private Double taxesAmount;

    @Column(name = "taxes_currency")
    private String taxesCurrency;

    @Column(name = "income_tax_amount")
    private Double incomeTaxAmount;

    @Column(name = "income_tax_currency")
    private String incomeTaxCurrency;

    @Column(name = "net_value_amount")
    private Double netValueAmount;

    @Column(name = "net_value_currency")
    private String netValueCurrency;

    public ResponseVariableIncomesBrokerData getResponseVariableIncomesBrokerData() {
        return new ResponseVariableIncomesBrokerData()
                .brokerNoteNumber(brokerNoteNumber)
                .grossValue(new ResponseVariableIncomesBrokerDataGrossValue()
                        .amount(BankLambdaUtils.doubleToString(grossValueAmount)).currency(grossValueCurrency))
                .brokerageFee(new ResponseVariableIncomesBrokerDataBrokerageFee()
                        .amount(BankLambdaUtils.doubleToString(brokerageFeeAmount)).currency(brokerageFeeCurrency))
                .clearingCustodyFee(new ResponseVariableIncomesBrokerDataClearingCustodyFee()
                        .amount(BankLambdaUtils.doubleToString(clearingCustodyFeeAmount)).currency(clearingCustodyFeeCurrency))
                .clearingRegistrationFee(new ResponseVariableIncomesBrokerDataClearingSettlementFee()
                        .amount(BankLambdaUtils.doubleToString(clearingRegistrationFeeAmount)).currency(clearingRegistrationFeeCurrency))
                .clearingSettlementFee(new ResponseVariableIncomesBrokerDataClearingSettlementFee()
                        .amount(BankLambdaUtils.doubleToString(clearingSettlementFeeAmount)).currency(clearingSettlementFeeCurrency))
                .stockExchangeFee(new ResponseVariableIncomesBrokerDataStockExchangeAssetTradeNoticeFee()
                        .amount(BankLambdaUtils.doubleToString(stockExchangeFeeAmount)).currency(stockExchangeFeeCurrency))
                .stockExchangeAssetTradeNoticeFee(new ResponseVariableIncomesBrokerDataStockExchangeAssetTradeNoticeFee()
                        .amount(BankLambdaUtils.doubleToString(stockExchangeAssetTradeNoticeFeeAmount)).currency(stockExchangeAssetTradeNoticeFeeCurrency))
                .taxes(new ResponseVariableIncomesBrokerDataTaxes()
                        .amount(BankLambdaUtils.doubleToString(taxesAmount)).currency(taxesCurrency))
                .incomeTax(new ResponseVariableIncomesBrokerDataIncomeTax()
                        .amount(BankLambdaUtils.doubleToString(incomeTaxAmount)).currency(incomeTaxCurrency))
                .netValue(new ResponseVariableIncomesBrokerDataNetValue()
                        .amount(BankLambdaUtils.doubleToString(netValueAmount)).currency(netValueCurrency));
    }
}