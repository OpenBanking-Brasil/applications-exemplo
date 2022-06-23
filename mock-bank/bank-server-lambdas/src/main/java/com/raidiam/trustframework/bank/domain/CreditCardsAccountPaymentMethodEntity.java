package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.CreditCardsAccountPaymentMethod;
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
@Table(name = "credit_cards_account_payment_method")
public class CreditCardsAccountPaymentMethodEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Generated(GenerationTime.INSERT)
    @Type(type = "pg-uuid")
    @Column(name = "payment_method_id", unique = true, nullable = false, updatable = false, insertable = false, columnDefinition = "uuid NOT NULL DEFAULT uuid_generate_v4()")
    private UUID paymentMethodId;

    @NotNull
    @Column(name = "identification_number", nullable = false)
    private String identificationNumber;

    @NotNull
    @Column(name = "is_multiple_credit_card", nullable = false)
    private boolean isMultipleCreditCard;

    @NotNull
    @Type(type = "pg-uuid")
    @Column(name = "credit_card_account_id", nullable = false)
    private UUID creditCardAccountId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_card_account_id", referencedColumnName = "credit_card_account_id", insertable = false, nullable = false, updatable = false)
    private CreditCardAccountsEntity account;

    public CreditCardsAccountPaymentMethod getDTO() {
        return new CreditCardsAccountPaymentMethod()
                .identificationNumber(this.identificationNumber)
                .isMultipleCreditCard(this.isMultipleCreditCard);
    }
}
