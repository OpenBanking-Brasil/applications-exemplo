package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.PaymentConsent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Date;
import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "payment_consent_payments")
public class PaymentConsentPaymentEntity {
    @Id
    @GeneratedValue
    @Column(name = "payment_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer id;

    @Column(name = "payment_type")
    private String paymentType;

    @Column(name = "payment_date")
    private Date paymentDate;

    @Column(name = "currency")
    private String currency;

    @Column(name = "amount")
    private String amount;

    public static PaymentConsentPaymentEntity from(PaymentConsent paymentConsent) {
        return Optional.ofNullable(paymentConsent)
                .map(p -> {
                    if ((p.getType() != null) && (p.getDate() != null)){
                        var entity = new PaymentConsentPaymentEntity();
                        entity.setPaymentType(p.getType().toString());
                        entity.setPaymentDate(BankLambdaUtils.localDateToDate(p.getDate()));
                        entity.setCurrency(p.getCurrency());
                        entity.setAmount(p.getAmount());
                        return entity;
                    }
                    return null;
                }).orElse(null);
    }
}
