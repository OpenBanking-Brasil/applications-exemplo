package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.CreditCardAccountsBillsFinanceCharge;
import com.raidiam.trustframework.mockbank.models.generated.CreditCardAccountsBillsFinanceChargeV2;
import com.raidiam.trustframework.mockbank.models.generated.EnumCreditCardAccountsFinanceChargeType;
import com.raidiam.trustframework.mockbank.models.generated.EnumCreditCardAccountsFinanceChargeTypeV2;
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
@Table(name = "credit_card_accounts_bills_finance_charge")
public class CreditCardAccountsBillsFinanceChargeEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Generated(GenerationTime.INSERT)
    @Type(type = "pg-uuid")
    @Column(name = "finance_charge_id", unique = true, nullable = false, updatable = false, insertable = false, columnDefinition = "uuid NOT NULL DEFAULT uuid_generate_v4()")
    private UUID financeChargesId;

    @NotNull
    @Column(name = "type", nullable = false)
    private String type;

    @NotNull
    @Column(name = "currency", nullable = false)
    private String currency;

    @NotNull
    @Column(name = "additional_info", nullable = false)
    private String additionalInfo;

    @NotNull
    @Column(name = "amount", nullable = false)
    private Double amount;

    @NotNull
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bill_id")
    private CreditCardAccountsBillsEntity bill;

    public CreditCardAccountsBillsFinanceCharge getDTO() {
        return new CreditCardAccountsBillsFinanceCharge()
                .type(EnumCreditCardAccountsFinanceChargeType.valueOf(this.type))
                .additionalInfo(this.additionalInfo)
                .amount(this.amount)
                .currency(this.currency);
    }

    public CreditCardAccountsBillsFinanceChargeV2 getDTOV2() {
        return new CreditCardAccountsBillsFinanceChargeV2()
                .type(EnumCreditCardAccountsFinanceChargeTypeV2.valueOf(this.type))
                .additionalInfo(this.additionalInfo)
                .amount(BankLambdaUtils.formatAmountV2(this.amount))
                .currency(this.currency);
    }

    public static CreditCardAccountsBillsFinanceChargeEntity from(CreditCardAccountsBillsEntity bill, CreditCardAccountsBillsFinanceCharge financeCharge) {
        var financeChargeEntity = new CreditCardAccountsBillsFinanceChargeEntity();
        financeChargeEntity.setBill(bill);
        financeChargeEntity.setType(financeCharge.getType().name());
        financeChargeEntity.setAdditionalInfo(financeCharge.getAdditionalInfo());
        financeChargeEntity.setAmount(financeCharge.getAmount());
        financeChargeEntity.setCurrency(financeCharge.getCurrency());
        return financeChargeEntity;
    }
}
