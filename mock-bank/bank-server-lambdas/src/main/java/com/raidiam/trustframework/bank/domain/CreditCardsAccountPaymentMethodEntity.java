package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.CreditCardsAccountPaymentMethod;
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
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "credit_card_account_id")
    private CreditCardAccountsEntity account;

    public CreditCardsAccountPaymentMethod getDTO() {
        return new CreditCardsAccountPaymentMethod()
                .identificationNumber(this.identificationNumber)
                .isMultipleCreditCard(this.isMultipleCreditCard);
    }

    public static CreditCardsAccountPaymentMethodEntity from(CreditCardAccountsEntity account, CreditCardsAccountPaymentMethod paymentMethod) {
        var paymentMethodEntity = new CreditCardsAccountPaymentMethodEntity();
        paymentMethodEntity.setAccount(account);
        paymentMethodEntity.setIdentificationNumber(paymentMethod.getIdentificationNumber());
        paymentMethodEntity.setMultipleCreditCard(paymentMethod.isIsMultipleCreditCard());

        return paymentMethodEntity;
    }
}
