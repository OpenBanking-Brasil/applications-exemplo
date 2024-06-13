package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.CreditCardAccountsLimitsData;
import com.raidiam.trustframework.mockbank.models.generated.CreditCardAccountsLimitsDataV2;
import com.raidiam.trustframework.mockbank.models.generated.CreditCardsAvailableAmountV2;
import com.raidiam.trustframework.mockbank.models.generated.CreditCardsLimitAmountV2;
import com.raidiam.trustframework.mockbank.models.generated.CreditCardsUsedAmountV2;
import com.raidiam.trustframework.mockbank.models.generated.EnumCreditCardAccountsConsolidationType;
import com.raidiam.trustframework.mockbank.models.generated.EnumCreditCardAccountsConsolidationTypeV2;
import com.raidiam.trustframework.mockbank.models.generated.EnumCreditCardAccountsLineLimitType;
import com.raidiam.trustframework.mockbank.models.generated.EnumCreditCardAccountsLineLimitTypeV2;
import com.raidiam.trustframework.mockbank.models.generated.EnumCreditCardAccountsLineName;
import lombok.*;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "credit_card_accounts_limits")
public class CreditCardAccountsLimitsEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Generated(GenerationTime.INSERT)
    @Type(type = "pg-uuid")
    @Column(name = "limit_id", unique = true, nullable = false, updatable = false, insertable = false, columnDefinition = "uuid NOT NULL DEFAULT uuid_generate_v4()")
    private UUID limitsId;

    @NotNull
    @Column(name = "credit_line_limit_type", nullable = false)
    private String creditLineLimitType;

    @NotNull
    @Column(name = "consolidation_type", nullable = false)
    private String consolidationType;

    @NotNull
    @Column(name = "identification_number", nullable = false)
    private String identificationNumber;

    @NotNull
    @Column(name = "line_name", nullable = false)
    private String lineName;

    @NotNull
    @Column(name = "line_name_additional_info", nullable = false)
    private String lineNameAdditionalInfo;

    @NotNull
    @Column(name = "is_limit_flexible", nullable = false)
    private boolean isLimitFlexible;

    @NotNull
    @Column(name = "limit_amount_currency", nullable = false)
    private String limitAmountCurrency;

    @NotNull
    @Column(name = "limit_amount", nullable = false)
    private Double limitAmount;

    @NotNull
    @Column(name = "used_amount_currency", nullable = false)
    private String usedAmountCurrency;

    @NotNull
    @Column(name = "used_amount", nullable = false)
    private Double usedAmount;

    @NotNull
    @Column(name = "available_amount_currency", nullable = false)
    private String availableAmountCurrency;

    @NotNull
    @Column(name = "available_amount", nullable = false)
    private Double availableAmount;

    @NotNull
    @Type(type = "pg-uuid")
    @Column(name = "credit_card_account_id", nullable = false)
    private UUID creditCardAccountId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_card_account_id", referencedColumnName = "credit_card_account_id", insertable = false, nullable = false, updatable = false)
    private CreditCardAccountsEntity account;

    public CreditCardAccountsLimitsData getDTO() {
        return new CreditCardAccountsLimitsData()
                .creditLineLimitType(EnumCreditCardAccountsLineLimitType.valueOf(this.creditLineLimitType))
                .consolidationType(EnumCreditCardAccountsConsolidationType.valueOf(this.consolidationType))
                .identificationNumber(this.identificationNumber)
                .lineName(EnumCreditCardAccountsLineName.valueOf(this.lineName))
                .lineNameAdditionalInfo(this.lineNameAdditionalInfo)
                .isLimitFlexible(this.isLimitFlexible)
                .limitAmountCurrency(this.limitAmountCurrency)
                .limitAmount(this.limitAmount)
                .usedAmountCurrency(this.usedAmountCurrency)
                .usedAmount(this.usedAmount)
                .availableAmountCurrency(this.availableAmountCurrency)
                .availableAmount(this.availableAmount);
    }

    public CreditCardAccountsLimitsDataV2 getDtoV2() {
        return new CreditCardAccountsLimitsDataV2()
                .creditLineLimitType(EnumCreditCardAccountsLineLimitTypeV2.valueOf(this.creditLineLimitType))
                .consolidationType(EnumCreditCardAccountsConsolidationTypeV2.valueOf(this.consolidationType))
                .identificationNumber(this.identificationNumber)
                .lineName(CreditCardAccountsLimitsDataV2.LineNameEnum.valueOf(this.lineName))
                .lineNameAdditionalInfo(this.lineNameAdditionalInfo)
                .isLimitFlexible(this.isLimitFlexible)
                .limitAmount(new CreditCardsLimitAmountV2()
                        .amount(BankLambdaUtils.formatAmountV2(this.limitAmount))
                        .currency(this.limitAmountCurrency))
                .usedAmount(new CreditCardsUsedAmountV2()
                        .amount(BankLambdaUtils.formatAmountV2(this.usedAmount))
                        .currency(this.usedAmountCurrency))
                .availableAmount(new CreditCardsAvailableAmountV2()
                        .amount(BankLambdaUtils.formatAmountV2(this.availableAmount))
                        .currency(this.availableAmountCurrency));
    }

    public static CreditCardAccountsLimitsEntity from(UUID creditCardAccountId, CreditCardAccountsLimitsData accountLimit) {
        var accountLimitEntity = new CreditCardAccountsLimitsEntity();
        accountLimitEntity.setCreditCardAccountId(creditCardAccountId);
        accountLimitEntity.setCreditLineLimitType(accountLimit.getCreditLineLimitType().name());
        accountLimitEntity.setConsolidationType(accountLimit.getConsolidationType().name());
        accountLimitEntity.setIdentificationNumber(accountLimit.getIdentificationNumber());
        accountLimitEntity.setLineName(accountLimit.getLineName().name());
        accountLimitEntity.setLineNameAdditionalInfo(accountLimit.getLineNameAdditionalInfo());
        accountLimitEntity.setLimitFlexible(accountLimit.isIsLimitFlexible());
        accountLimitEntity.setLimitAmountCurrency(accountLimit.getLimitAmountCurrency());
        accountLimitEntity.setLimitAmount(accountLimit.getLimitAmount());
        accountLimitEntity.setUsedAmountCurrency(accountLimit.getUsedAmountCurrency());
        accountLimitEntity.setUsedAmount(accountLimit.getUsedAmount());
        accountLimitEntity.setAvailableAmountCurrency(accountLimit.getAvailableAmountCurrency());
        accountLimitEntity.setAvailableAmount(accountLimit.getAvailableAmount());
        return accountLimitEntity;
    }
}
