package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "credit_fixed_incomes")
public class CreditFixedIncomesEntity extends BaseEntity implements HasStatusInterface {
    @Id
    @GeneratedValue
    @Column(name = "investment_id", unique = true, nullable = false, updatable = false, insertable = false)
    private UUID investmentId;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "company_cnpj")
    private String companyCnpj;

    @Column(name = "investment_type")
    private String investmentType;

    @Column(name = "isin_code")
    private String isinCode;

    @Column(name = "pre_fixed_rate")
    private double preFixedRate;

    @Column(name = "post_fixed_indexer_percentage")
    private double postFixedIndexerPercentage;

    @Column(name = "rate_type")
    private String rateType;

    @Column(name = "rate_periodicity")
    private String ratePeriodicity;

    @Column(name = "calculation")
    private String calculation;

    @Column(name = "indexer")
    private String indexer;

    @Column(name = "indexer_additional_info")
    private String indexerAdditionalInfo;

    @Column(name = "issue_unit_price_amount")
    private double issueUnitPriceAmount;

    @Column(name = "issue_unit_price_currency")
    private String issueUnitPriceCurrency;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "clearing_code")
    private String clearingCode;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "grace_period_date")
    private LocalDate gracePeriodDate;

    @Column(name = "account_holder_id")
    private UUID accountHolderId;

    @Column(name = "status")
    private String status;

    @Column(name = "tax_exempt_product")
    @Convert(converter = TaxExemptConverter.class)
    private EnumTaxExemptProduct taxExemptProduct;

    @Column(name = "voucher_payment_indicator")
    @Convert(converter = VoucherPaymentIndicatorConverter.class)
    private VoucherPaymentIndicator voucherPaymentIndicator;

    @Column(name = "voucher_payment_periodicity")
    @Convert(converter = VoucherPaymentPeriodicityConverter.class)
    private VoucherPaymentPeriodicity voucherPaymentPeriodicity;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_holder_id", referencedColumnName = "account_holder_id", insertable = false, nullable = false, updatable = false)
    private AccountHolderEntity accountHolder;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "investment")
    private Set<CreditFixedIncomesTransactionsEntity> transactionsEntities = new HashSet<>();

    public CreditFixedList getCreditFixedList() {
        return new CreditFixedList()
                .investmentId(investmentId.toString())
                .brandName(brandName)
                .companyCnpj(companyCnpj)
                .investmentType(EnumInvestmentType.fromValue(investmentType));
    }

    public CreditFixedIdentification getCreditFixedIncomesProductIdentification() {
        return new CreditFixedIdentification()
                .issuerInstitutionCnpjNumber(companyCnpj)
                .investmentType(EnumInvestmentType.fromValue(investmentType))
                .isinCode(isinCode)
                .taxExemptProduct(taxExemptProduct)
                .voucherPaymentIndicator(voucherPaymentIndicator)
                .voucherPaymentPeriodicity(voucherPaymentPeriodicity)
                .remuneration(new Remuneration()
                        .preFixedRate(BankLambdaUtils.formatDoubleToLongString(preFixedRate))
                        .postFixedIndexerPercentage(BankLambdaUtils.formatDoubleToLongString(postFixedIndexerPercentage))
                        .rateType(EnumRateType.fromValue(rateType))
                        .ratePeriodicity(EnumRatePeriodicity.fromValue(ratePeriodicity))
                        .calculation(EnumCalculation.fromValue(calculation))
                        .indexer(EnumBankFixedIncomeIndexer.fromValue(indexer))
                        .indexerAdditionalInfo(indexerAdditionalInfo))
                .issueUnitPrice(new IssueUnitPrice()
                        .amount(Double.toString(issueUnitPriceAmount)).currency(issueUnitPriceCurrency))
                .dueDate(dueDate)
                .issueDate(issueDate)
                .clearingCode(clearingCode)
                .purchaseDate(purchaseDate)
                .gracePeriodDate(gracePeriodDate);
    }


    @Converter
    static class TaxExemptConverter implements AttributeConverter<EnumTaxExemptProduct, String> {

        @Override
        public String convertToDatabaseColumn(EnumTaxExemptProduct attribute) {
            return Optional.ofNullable(attribute)
                    .map(EnumTaxExemptProduct::toString)
                    .orElse(null);
        }

        @Override
        public EnumTaxExemptProduct convertToEntityAttribute(String dbData) {
            return Optional.ofNullable(dbData)
                    .map(EnumTaxExemptProduct::fromValue)
                    .orElse(null);
        }
    }

    @Converter
    static class VoucherPaymentIndicatorConverter implements AttributeConverter<VoucherPaymentIndicator, String> {

        @Override
        public String convertToDatabaseColumn(VoucherPaymentIndicator attribute) {
            return Optional.ofNullable(attribute)
                    .map(VoucherPaymentIndicator::toString)
                    .orElse(null);
        }

        @Override
        public VoucherPaymentIndicator convertToEntityAttribute(String dbData) {
            return Optional.ofNullable(dbData)
                    .map(VoucherPaymentIndicator::fromValue)
                    .orElse(null);
        }
    }

    @Converter
    static class VoucherPaymentPeriodicityConverter implements AttributeConverter<VoucherPaymentPeriodicity, String> {

        @Override
        public String convertToDatabaseColumn(VoucherPaymentPeriodicity attribute) {
            return Optional.ofNullable(attribute)
                    .map(VoucherPaymentPeriodicity::toString)
                    .orElse(null);
        }

        @Override
        public VoucherPaymentPeriodicity convertToEntityAttribute(String dbData) {
            return Optional.ofNullable(dbData)
                    .map(VoucherPaymentPeriodicity::fromValue)
                    .orElse(null);
        }
    }

}
