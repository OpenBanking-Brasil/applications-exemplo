package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "exchanges_operation_event")
public class ExchangesOperationEventEntity extends BaseEntity {
    @Id
    @GeneratedValue
    @Column(name = "event_id", unique = true, nullable = false, updatable = false, insertable = false)
    private UUID eventId;

    @Column(name = "operation_id")
    private UUID operationId;

    @Column(name = "event_sequence_number")
    private String eventSequenceNumber;

    @Column(name = "event_type")
    @Convert(converter = ExchangesEventType.class)
    private EnumExchangesEventType eventType;

    @Column(name = "event_date")
    private OffsetDateTime eventDate;

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

    @Column(name = "relationship_code")
    private String relationshipCode;

    @Column(name = "foreign_partie_name")
    private String foreignPartieName;

    @Column(name = "foreign_partie_country_code")
    private String foreignPartieCountryCode;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id", referencedColumnName = "operation_id", insertable = false, nullable = false, updatable = false)
    private ExchangesOperationEntity exchangesOperationEntity;

    public Events getV1Data() {
        var events = new Events()
                .eventSequenceNumber(eventSequenceNumber)
                .eventType(eventType)
                .eventDate(eventDate)
                .dueDate(dueDate)
                .localCurrencyAdvancePercentage(String.format("%.6f", localCurrencyAdvancePercentage))
                .deliveryForeignCurrency(deliveryForeignCurrency)
                .operationCategoryCode(operationCategoryCode);

        if (localCurrencyOperationTaxAmount != null && localCurrencyOperationTaxCurrency != null) {
            events.localCurrencyOperationTax(new EventsLocalCurrencyOperationTax()
                    .currency(localCurrencyOperationTaxCurrency)
                    .amount(localCurrencyOperationTaxAmount.toString()));
        }

        if (localCurrencyOperationValueAmount != null && localCurrencyOperationValueCurrency != null) {
            events.localCurrencyOperationValue(new OperationDetailsLocalCurrencyOperationValue()
                    .currency(localCurrencyOperationValueCurrency)
                    .amount(localCurrencyOperationValueAmount.toString()));
        }

        if (foreignOperationValueAmount != null && foreignOperationValueCurrency != null) {
            events.foreignOperationValue(new OperationDetailsForeignOperationValue()
                    .currency(foreignOperationValueCurrency)
                    .amount(foreignOperationValueAmount.toString()));
        }

        if (vetAmountAmount != null && vetAmountCurrency != null) {
            events.vetAmount(new OperationDetailsVetAmount()
                    .currency(vetAmountCurrency)
                    .amount(vetAmountAmount.toString()));
        }

        if (foreignPartieCountryCode != null && foreignPartieName != null && relationshipCode != null) {
            events.foreignPartie(new EventsForeignPartie()
                    .foreignPartieCountryCode(foreignPartieCountryCode)
                    .foreignPartieName(foreignPartieName)
                    .relationshipCode(relationshipCode)
            );
        }

        return events;
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

    @Converter
    static class ExchangesEventType implements AttributeConverter<EnumExchangesEventType, String> {

        @Override
        public String convertToDatabaseColumn(EnumExchangesEventType attribute) {
            return Optional.ofNullable(attribute)
                    .map(EnumExchangesEventType::toString)
                    .orElse(null);
        }

        @Override
        public EnumExchangesEventType convertToEntityAttribute(String dbData) {
            return Optional.ofNullable(dbData)
                    .map(EnumExchangesEventType::fromValue)
                    .orElse(null);
        }
    }
}
