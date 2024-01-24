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
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "treasure_titles")
public class TreasureTitlesEntity extends BaseEntity implements HasStatusInterface {
    @Id
    @GeneratedValue
    @Column(name = "investment_id", unique = true, nullable = false, updatable = false, insertable = false)
    private UUID investmentId;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "company_cnpj")
    private String companyCnpj;

    @Column(name = "voucher_payment_indicator")
    private String voucherPaymentIndicator;

    @Column(name = "voucher_payment_periodicity")
    private String voucherPaymentPeriodicity;

    @Column(name = "voucher_payment_periodicity_additional_info")
    private String voucherPaymentPeriodicityAdditionalInfo;

    @Column(name = "isin_code")
    private String isinCode;

    @Column(name = "pre_fixed_rate")
    private double preFixedRate;

    @Column(name = "post_fixed_indexer_percentage")
    private double postFixedIndexerPercentage;

    @Column(name = "rate_periodicity")
    private String ratePeriodicity;

    @Column(name = "calculation")
    private String calculation;

    @Column(name = "indexer")
    private String indexer;

    @Column(name = "indexer_additional_info")
    private String indexerAdditionalInfo;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "account_holder_id")
    private UUID accountHolderId;

    @Column(name = "status")
    private String status;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_holder_id", referencedColumnName = "account_holder_id", insertable = false, nullable = false, updatable = false)
    private AccountHolderEntity accountHolder;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "investment")
    private Set<TreasureTitlesTransactionsEntity> transactionsEntities = new HashSet<>();

    public ResponseTreasureTitlesProductListData getResponseTreasureTitlesProductListData() {
        return new ResponseTreasureTitlesProductListData()
                .investmentId(investmentId.toString())
                .brandName(brandName)
                .companyCnpj(companyCnpj);
    }

    public ResponseTreasureTitlesProductIdentificationData getResponseTreasureTitlesProductIdentificationData() {
        return new ResponseTreasureTitlesProductIdentificationData()
                .productName(productName)
                .isinCode(isinCode)
                .remuneration(new TreasureTitleRemuneration()
                        .preFixedRate(BankLambdaUtils.formatDoubleToLongString(preFixedRate))
                        .postFixedIndexerPercentage(BankLambdaUtils.formatDoubleToLongString(postFixedIndexerPercentage))
                        .ratePeriodicity(EnumRatePeriodicity.fromValue(ratePeriodicity))
                        .calculation(EnumCalculation.fromValue(calculation))
                        .indexer(EnumBankFixedIncomeIndexer.fromValue(indexer))
                        .indexerAdditionalInfo(indexerAdditionalInfo))
                .dueDate(dueDate)
                .purchaseDate(purchaseDate)
                .voucherPaymentIndicator(ResponseTreasureTitlesProductIdentificationData.VoucherPaymentIndicatorEnum.fromValue(voucherPaymentIndicator))
                .voucherPaymentPeriodicity(ResponseTreasureTitlesProductIdentificationData.VoucherPaymentPeriodicityEnum.fromValue(voucherPaymentPeriodicity))
                .voucherPaymentPeriodicityAdditionalInfo(voucherPaymentPeriodicityAdditionalInfo);
    }

}
