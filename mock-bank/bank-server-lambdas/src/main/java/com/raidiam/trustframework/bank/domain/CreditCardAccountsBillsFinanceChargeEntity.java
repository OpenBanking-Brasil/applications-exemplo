package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.CreditCardAccountsBillsFinanceCharge;
import com.raidiam.trustframework.mockbank.models.generated.EnumCreditCardAccountsFinanceChargeType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
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
    @Type(type = "pg-uuid")
    @Column(name = "bill_id", nullable = false)
    private UUID billId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", referencedColumnName = "bill_id", insertable = false, nullable = false, updatable = false)
    private CreditCardAccountsBillsEntity bill;

    public CreditCardAccountsBillsFinanceCharge getDTO() {
        return new CreditCardAccountsBillsFinanceCharge()
                .type(EnumCreditCardAccountsFinanceChargeType.valueOf(this.type))
                .additionalInfo(this.additionalInfo)
                .amount(this.amount)
                .currency(this.currency);
    }
}
