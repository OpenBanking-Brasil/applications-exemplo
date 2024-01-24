package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "exchanges_operation")
public class ExchangesOperationEntity extends BaseEntity implements HasStatusInterface {
    @Id
    @GeneratedValue
    @Column(name = "operation_id", unique = true, nullable = false, updatable = false, insertable = false)
    private UUID operationId;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "company_cnpj")
    private String companyCnpj;

    @Column(name = "intermediary_institution_cnpj_number")
    private String intermediaryInstitutionCnpjNumber;

    @Column(name = "intermediary_institution_name")
    private String intermediaryInstitutionName;

    @Column(name = "operation_number")
    private String operationNumber;

    @Column(name = "operation_type")
    @Convert(converter = ExchangesOperationType.class)
    private EnumExchangesOperationType operationType;

    @Column(name = "operation_date")
    private OffsetDateTime operationDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "local_currency_operation_tax_amount")
    private Double localCurrencyOperationTaxAmount;

    @Column(name = "local_currency_operation_tax_currency")
    private String localCurrencyOperationTaxCurrency;

    @Column(name = "local_currency_operation_value_amount")
    private Double localCurrencyOperationValueAmount;

    @Column(name = "local_currency_operation_value_currency")
    private String localCurrencyOperationValueCurrency;

    @Column(name = "foreign_operation_value_amount")
    private Double foreignOperationValueAmount;

    @Column(name = "foreign_operation_value_currency")
    private String foreignOperationValueCurrency;

    @Column(name = "operation_outstanding_balance_amount")
    private Double operationOutstandingBalanceAmount;

    @Column(name = "operation_outstanding_balance_currency")
    private String operationOutstandingBalanceCurrency;

    @Column(name = "vet_amount_amount")
    private Double vetAmountAmount;

    @Column(name = "vet_amount_currency")
    private String vetAmountCurrency;

    @Column(name = "local_currency_advance_percentage")
    private Double localCurrencyAdvancePercentage;

    @Column(name = "delivery_foreign_currency")
    @Convert(converter = ExchangesDeliveryForeignCurrency.class)
    private EnumExchangesDeliveryForeignCurrency deliveryForeignCurrency;

    @Column(name = "operationCategoryCode")
    private String operationCategoryCode;

    @Column(name = "account_holder_id")
    private UUID accountHolderId;

    @Column(name = "status")
    private String status;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_holder_id", referencedColumnName = "account_holder_id", insertable = false, nullable = false, updatable = false)
    private AccountHolderEntity accountHolder;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id", referencedColumnName = "operation_id", insertable = false, nullable = false, updatable = false)
    private List<ExchangesOperationEventEntity> events;

    public ExchangesProductList getV1Data() {
        return new ExchangesProductList()
                .brandName(brandName)
                .companyCnpj(companyCnpj)
                .operationId(operationId.toString());
    }

    public OperationDetails getV1DetailsData() {
        var operation = new OperationDetails()
                .authorizedInstitutionCnpjNumber(companyCnpj)
                .authorizedInstitutionName(brandName)
                .intermediaryInstitutionCnpjNumber(intermediaryInstitutionCnpjNumber)
                .intemediaryInstitutionName(intermediaryInstitutionName)
                .operationNumber(operationNumber)
                .operationType(operationType)
                .operationDate(operationDate)
                .dueDate(dueDate)
                .localCurrencyAdvancePercentage(String.format("%.6f", localCurrencyAdvancePercentage))
                .deliveryForeignCurrency(deliveryForeignCurrency)
                .operationCategoryCode(operationCategoryCode);

        if (localCurrencyOperationTaxAmount != null && localCurrencyOperationTaxCurrency != null) {
            operation.localCurrencyOperationTax(new OperationDetailsLocalCurrencyOperationTax()
                    .currency(localCurrencyOperationTaxCurrency)
                    .amount(localCurrencyOperationTaxAmount.toString()));
        }

        if (localCurrencyOperationValueAmount != null && localCurrencyOperationValueCurrency != null) {
            operation.localCurrencyOperationValue(new OperationDetailsLocalCurrencyOperationValue()
                    .currency(localCurrencyOperationValueCurrency)
                    .amount(localCurrencyOperationValueAmount.toString()));
        }

        if (foreignOperationValueAmount != null && foreignOperationValueCurrency != null) {
            operation.foreignOperationValue(new OperationDetailsForeignOperationValue()
                    .currency(foreignOperationValueCurrency)
                    .amount(foreignOperationValueAmount.toString()));
        }

        if (vetAmountAmount != null && vetAmountCurrency != null) {
            operation.vetAmount(new OperationDetailsVetAmount()
                    .currency(vetAmountCurrency)
                    .amount(vetAmountAmount.toString()));
        }

        return operation;
    }

    public AccountData createSparseAccountData() {
        return new AccountData().accountId(this.operationId.toString()).number(this.brandName);
    }

    @Converter
    static class ExchangesOperationType implements AttributeConverter<EnumExchangesOperationType, String> {

        @Override
        public String convertToDatabaseColumn(EnumExchangesOperationType attribute) {
            return Optional.ofNullable(attribute)
                    .map(EnumExchangesOperationType::toString)
                    .orElse(null);
        }

        @Override
        public EnumExchangesOperationType convertToEntityAttribute(String dbData) {
            return Optional.ofNullable(dbData)
                    .map(EnumExchangesOperationType::fromValue)
                    .orElse(null);
        }
    }

    @Converter
    static class ExchangesDeliveryForeignCurrency implements AttributeConverter<EnumExchangesDeliveryForeignCurrency, String> {

        @Override
        public String convertToDatabaseColumn(EnumExchangesDeliveryForeignCurrency attribute) {
            return Optional.ofNullable(attribute)
                    .map(EnumExchangesDeliveryForeignCurrency::toString)
                    .orElse(null);
        }

        @Override
        public EnumExchangesDeliveryForeignCurrency convertToEntityAttribute(String dbData) {
            return Optional.ofNullable(dbData)
                    .map(EnumExchangesDeliveryForeignCurrency::fromValue)
                    .orElse(null);
        }
    }
}
