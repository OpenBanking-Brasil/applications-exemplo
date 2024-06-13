package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.PaymentPix;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "pix_payments_payments")
public class PixPaymentPaymentEntity extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pix_payment_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer pixPaymentId;

    @Column(name = "currency")
    private String currency;

    @Column(name = "amount")
    private String amount;

    public static PixPaymentPaymentEntity from(PaymentPix paymentConsent) {
        return Optional.ofNullable(paymentConsent)
                .map(p -> {
                    if ((p.getCurrency() != null) && (p.getAmount() != null)){
                        var entity = new PixPaymentPaymentEntity();
                        entity.setCurrency(p.getCurrency());
                        entity.setAmount(p.getAmount());
                        return entity;
                    }
                    return null;
                }).orElse(null);
    }
}
