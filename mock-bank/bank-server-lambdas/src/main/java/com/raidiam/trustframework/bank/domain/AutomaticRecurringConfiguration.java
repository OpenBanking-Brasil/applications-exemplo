package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Accessors(chain = true)
@Table(name = "automatic_recurring_configuration")
public class AutomaticRecurringConfiguration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "contract_id")
    private String contractId;

    @Column(name = "amount")
    private String amount;

    @Column(name = "transaction_limit")
    private String transactionLimit;

    @Column(name = "period")
    @Convert(converter = PeriodConverter.class)
    private AutomaticAutomatic.PeriodEnum period;

    @Column(name = "day_of_month")
    private int dayOfMonth;

    @Column(name = "day_of_week")
    @Convert(converter = DayOfWeekConverter.class)
    private AutomaticAutomatic.DayOfWeekEnum dayOfWeek;

    @Column(name = "month")
    @Convert(converter = MonthConverter.class)
    private AutomaticAutomatic.MonthEnum month;

    @Column(name = "contract_debtor_name")
    private String contractDebtorName;

    @Column(name = "contract_debtor_identification")
    private String contractDebtorIdentification;

    @Column(name = "contract_debtor_rel")
    private String contractDebtorRel;

    @Column(name = "immediate_payment_type")
    @Convert(converter = PaymentTypeConverter.class)
    private EnumPaymentType immediatePaymentType;

    @Column(name = "immediate_payment_date")
    private LocalDate immediatePaymentDate;

    @Column(name = "immediate_payment_currency")
    private String immediatePaymentCurrency;

    @Column(name = "immediate_payment_amount")
    private String immediatePaymentAmount;

    @Column(name = "immediate_payment_creditor_ispb")
    private String immediatePaymentCreditorIspb;

    @Column(name = "immediate_payment_creditor_issuer")
    private String immediatePaymentCreditorIssuer;

    @Column(name = "immediate_payment_creditor_number")
    private String immediatePaymentCreditorNumber;

    @Column(name = "immediate_payment_creditor_accountType")
    @Convert(converter = AccountTypeConsentsConverter.class)
    private EnumAccountTypeConsents immediatePaymentCreditorAccountType;

    public static AutomaticRecurringConfiguration from(AutomaticAutomatic automaticRecurringConfiguration) {
        ContractDebtor contractDebtor = automaticRecurringConfiguration.getContractDebtor();
        ImmediatePayment immediatePayment = automaticRecurringConfiguration.getImmediatePayment();
        return new AutomaticRecurringConfiguration()
                .setContractId(automaticRecurringConfiguration.getContractId())
                .setAmount(automaticRecurringConfiguration.getAmount())
                .setTransactionLimit(automaticRecurringConfiguration.getTransactionLimit())
                .setPeriod(automaticRecurringConfiguration.getPeriod())
                .setDayOfMonth(automaticRecurringConfiguration.getDayOfMonth())
                .setMonth(automaticRecurringConfiguration.getMonth())
                .setContractDebtorName(contractDebtor.getName())
                .setContractDebtorIdentification(contractDebtor.getDocument().getIdentification())
                .setContractDebtorRel(contractDebtor.getDocument().getRel())
                .setImmediatePaymentType(immediatePayment.getType())
                .setImmediatePaymentDate(immediatePayment.getDate())
                .setImmediatePaymentCurrency(immediatePayment.getCurrency())
                .setImmediatePaymentCreditorIspb(immediatePayment.getCreditorAccount().getIspb())
                .setImmediatePaymentCreditorIssuer(immediatePayment.getCreditorAccount().getIssuer())
                .setImmediatePaymentCreditorNumber(immediatePayment.getCreditorAccount().getNumber())
                .setImmediatePaymentCreditorAccountType(immediatePayment.getCreditorAccount().getAccountType());
    }

    public AutomaticAutomatic getDTO() {
        return new AutomaticAutomatic()
                .contractId(contractId)
                .amount(amount)
                .transactionLimit(transactionLimit)
                .period(period)
                .dayOfMonth(dayOfMonth)
                .month(month)
                .contractDebtor(new ContractDebtor()
                        .name(contractDebtorName)
                        .document(new ContractDebtorDocument()
                                .identification(contractDebtorIdentification)
                                .rel(contractDebtorRel)))
                .immediatePayment(new ImmediatePayment()
                        .type(immediatePaymentType)
                        .date(immediatePaymentDate)
                        .currency(immediatePaymentCurrency)
                        .creditorAccount(new PostCreditorAccount()
                                .ispb(immediatePaymentCreditorIspb)
                                .issuer(immediatePaymentCreditorIssuer)
                                .number(immediatePaymentCreditorNumber)
                                .accountType(immediatePaymentCreditorAccountType)));
    }


    @Converter
    static class PeriodConverter implements AttributeConverter<AutomaticAutomatic.PeriodEnum, String> {

        @Override
        public String convertToDatabaseColumn(AutomaticAutomatic.PeriodEnum attribute) {
            return Optional.ofNullable(attribute)
                    .map(AutomaticAutomatic.PeriodEnum::toString)
                    .orElse(null);
        }

        @Override
        public AutomaticAutomatic.PeriodEnum convertToEntityAttribute(String dbData) {
            return Optional.ofNullable(dbData)
                    .map(AutomaticAutomatic.PeriodEnum::fromValue)
                    .orElse(null);
        }
    }

    @Converter
    static class DayOfWeekConverter implements AttributeConverter<AutomaticAutomatic.DayOfWeekEnum, String> {

        @Override
        public String convertToDatabaseColumn(AutomaticAutomatic.DayOfWeekEnum attribute) {
            return Optional.ofNullable(attribute)
                    .map(AutomaticAutomatic.DayOfWeekEnum::toString)
                    .orElse(null);
        }

        @Override
        public AutomaticAutomatic.DayOfWeekEnum convertToEntityAttribute(String dbData) {
            return Optional.ofNullable(dbData)
                    .map(AutomaticAutomatic.DayOfWeekEnum::fromValue)
                    .orElse(null);
        }
    }

    @Converter
    static class MonthConverter implements AttributeConverter<AutomaticAutomatic.MonthEnum, String> {

        @Override
        public String convertToDatabaseColumn(AutomaticAutomatic.MonthEnum attribute) {
            return Optional.ofNullable(attribute)
                    .map(AutomaticAutomatic.MonthEnum::toString)
                    .orElse(null);
        }

        @Override
        public AutomaticAutomatic.MonthEnum convertToEntityAttribute(String dbData) {
            return Optional.ofNullable(dbData)
                    .map(AutomaticAutomatic.MonthEnum::fromValue)
                    .orElse(null);
        }
    }

    @Converter
    static class PaymentTypeConverter implements AttributeConverter<EnumPaymentType, String> {

        @Override
        public String convertToDatabaseColumn(EnumPaymentType attribute) {
            return Optional.ofNullable(attribute)
                    .map(EnumPaymentType::toString)
                    .orElse(null);
        }

        @Override
        public EnumPaymentType convertToEntityAttribute(String dbData) {
            return Optional.ofNullable(dbData)
                    .map(EnumPaymentType::fromValue)
                    .orElse(null);
        }
    }

    @Converter
    static class AccountTypeConsentsConverter implements AttributeConverter<EnumAccountTypeConsents, String> {

        @Override
        public String convertToDatabaseColumn(EnumAccountTypeConsents attribute) {
            return Optional.ofNullable(attribute)
                    .map(EnumAccountTypeConsents::toString)
                    .orElse(null);
        }

        @Override
        public EnumAccountTypeConsents convertToEntityAttribute(String dbData) {
            return Optional.ofNullable(dbData)
                    .map(EnumAccountTypeConsents::fromValue)
                    .orElse(null);
        }
    }

}
