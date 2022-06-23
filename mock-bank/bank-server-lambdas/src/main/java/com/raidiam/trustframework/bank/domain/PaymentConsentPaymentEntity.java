package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.PaymentConsent;
import lombok.*;
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

    @Column(name = "schedule")
    private Date schedule;

    @OneToOne(cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "payment_consent_details_id", referencedColumnName = "payment_consent_details_id", nullable = true)
    private PaymentConsentDetailsEntity paymentConsentDetails;

    public static PaymentConsentPaymentEntity from(PaymentConsent paymentConsent) {
        return Optional.ofNullable(paymentConsent)
                .map(p -> {
                    if ((p.getType() != null) && (p.getDate() != null)){
                        var entity = new PaymentConsentPaymentEntity();
                        entity.setPaymentType(p.getType());
                        entity.setPaymentDate(BankLambdaUtils.localDateToDate(p.getDate()));
                        entity.setCurrency(p.getCurrency());
                        entity.setAmount(p.getAmount());
                        entity.setPaymentConsentDetails(PaymentConsentDetailsEntity.from(paymentConsent.getDetails()));
                        return entity;
                    } else if ((p.getType() != null) && (p.getSchedule() != null)){
                        var entity = new PaymentConsentPaymentEntity();
                        var single = p.getSchedule().getSingle();
                        entity.setPaymentType(p.getType());
                        entity.setSchedule(BankLambdaUtils.localDateToDate(single.getDate()));
                        entity.setCurrency(p.getCurrency());
                        entity.setAmount(p.getAmount());
                        entity.setPaymentConsentDetails(PaymentConsentDetailsEntity.from(paymentConsent.getDetails()));
                        return entity;
                    }
                    return null;
                }).orElse(null);
    }
}
