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
@Table(name = "variable_incomes_transactions")
public class VariableIncomesTransactionsEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "transaction_id", unique = true, nullable = false, updatable = false, insertable = false)
    private UUID transactionId;

    @Column(name = "broker_note_id")
    private UUID brokerNoteId;

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

    @Column(name = "transaction_unit_price_currency")
    private String transactionUnitPriceCurrency;

    @Column(name = "transaction_value")
    private Double transactionValue;

    @Column(name = "transaction_value_currency")
    private String transactionValueCurrency;

    @Column(name = "price_factor")
    private Double priceFactor;

    @Column(name = "investment_id")
    private UUID investmentId;

    @NotNull
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investment_id", referencedColumnName = "investment_id", nullable = false, updatable = false, insertable = false)
    private VariableIncomesEntity investment;

    public ResponseVariableIncomesTransactionsData getResponseVariableIncomesTransactionsData() {
        return new ResponseVariableIncomesTransactionsData()
                .type(EnumIncomeMovementType.fromValue(type))
                .transactionType(EnumVariableIncomesTransactionsTransactionType.fromValue(transactionType))
                .typeAdditionalInfo(transactionTypeAdditionalInfo)
                .transactionDate(transactionDate)
                .transactionUnitPrice(new ResponseVariableIncomesTransactionsDataTransactionUnitPrice()
                        .amount(BankLambdaUtils.doubleToString(transactionUnitPrice))
                        .currency(transactionUnitPriceCurrency))
                .transactionQuantity(BankLambdaUtils.doubleToString(transactionQuantity))
                .transactionValue(new ResponseVariableIncomesTransactionsDataTransactionValue()
                        .amount(BankLambdaUtils.doubleToString(transactionValue))
                        .currency(transactionValueCurrency))
                .transactionId(transactionId.toString())
                .brokerNoteId(brokerNoteId.toString())
                .priceFactor(BankLambdaUtils.doubleToString(priceFactor));
    }
}