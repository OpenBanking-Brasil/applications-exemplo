package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
//@Audited //- If you switch this on, hibernate decides that the payment_consent_id in the aud table needs to be an integer :/
@Table(name = "pix_payments")
public class PixPaymentEntity extends BaseEntity{
    @Id
    @GeneratedValue
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @EqualsAndHashCode.Exclude
    @Column(name = "payment_id", nullable = false, updatable = false, insertable = true)
    private String paymentId;

    @EqualsAndHashCode.Exclude
    @Column(name = "local_instrument", nullable = false, updatable = false, insertable = true)
    private String localInstrument;

    @OneToOne(cascade = CascadeType.MERGE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "payment_consent_id", referencedColumnName = "payment_consent_id", nullable = false)
    private PaymentConsentEntity paymentConsentEntity;

    @OneToOne(cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "pix_payment_id", referencedColumnName = "pix_payment_id", nullable = true)
    private PixPaymentPaymentEntity pixPaymentPaymentEntity;

    @OneToOne(cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "creditor_account_id", referencedColumnName = "creditor_account_id", nullable = true)
    private CreditorAccountEntity creditorAccountEntity;

    @EqualsAndHashCode.Exclude
    @Column(name = "remittance_information")
    private String remittanceInformation;

    @EqualsAndHashCode.Exclude
    @Column(name = "qr_code")
    private String qrCode;

    @EqualsAndHashCode.Exclude
    @Column(name = "proxy")
    private String proxy;

    @EqualsAndHashCode.Exclude
    @Column(name = "status")
    private String status;

    @EqualsAndHashCode.Exclude
    @Column(name = "creation_date_time")
    private Date creationDateTime;

    @EqualsAndHashCode.Exclude
    @Column(name = "status_update_date_time")
    private Date statusUpdateDateTime;

    @EqualsAndHashCode.Exclude
    @Column(name = "rejection_reason")
    private String rejectionReason;

    @EqualsAndHashCode.Exclude
    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @EqualsAndHashCode.Exclude
    @Column(name = "transaction_identification")
    private String transactionIdentification;

    public static PixPaymentEntity from (CreatePixPaymentData createPixPaymentData, PaymentConsentEntity paymentConsentEntity, String idempotencyKey) {
        var pixPaymentEntity = new PixPaymentEntity();
        UUID uuid = UUID.randomUUID();
        pixPaymentEntity.setPaymentId(uuid.toString());
        pixPaymentEntity.setPaymentConsentEntity(paymentConsentEntity);

        pixPaymentEntity.setLocalInstrument(createPixPaymentData.getLocalInstrument().toString());
        pixPaymentEntity.setRemittanceInformation(createPixPaymentData.getRemittanceInformation());
        pixPaymentEntity.setQrCode(createPixPaymentData.getQrCode());
        pixPaymentEntity.setProxy(createPixPaymentData.getProxy());

        pixPaymentEntity.setPixPaymentPaymentEntity(PixPaymentPaymentEntity.from(createPixPaymentData.getPayment()));
        pixPaymentEntity.setCreditorAccountEntity(CreditorAccountEntity.from(createPixPaymentData.getCreditorAccount()));

        pixPaymentEntity.setStatus(EnumPaymentStatusType.PDNG.toString());

        pixPaymentEntity.setCreationDateTime(Date.from(Instant.now()));
        pixPaymentEntity.setStatusUpdateDateTime(Date.from(Instant.now()));

        pixPaymentEntity.setIdempotencyKey(idempotencyKey);

        pixPaymentEntity.setTransactionIdentification(createPixPaymentData.getTransactionIdentification());

        return pixPaymentEntity;
    }

    public ResponsePixPayment getDTO() {
        var payment = new ResponsePixPayment();
        var paymentData = new ResponsePixPaymentData()
                .paymentId(paymentId)
                .consentId(paymentConsentEntity.getPaymentConsentId())
                .creationDateTime(BankLambdaUtils.dateToOffsetDate(creationDateTime))
                .statusUpdateDateTime(BankLambdaUtils.dateToOffsetDate(statusUpdateDateTime))
                .proxy(proxy)
                .rejectionReason(ResponsePixPaymentData.RejectionReasonEnum.fromValue(rejectionReason))
                .status(EnumPaymentStatusType.fromValue(status))
                .localInstrument(EnumLocalInstrument.fromValue(localInstrument))
                .remittanceInformation(remittanceInformation)
                .cnpjInitiator("50685362000135")
                .transactionIdentification(transactionIdentification);

        // TODO - it's not clear what this is
//                .endToEndId();

        if(pixPaymentPaymentEntity != null) {
            var paymentPix = new PaymentPix()
                    .amount(pixPaymentPaymentEntity.getAmount())
                    .currency(pixPaymentPaymentEntity.getCurrency());
            paymentData.payment(paymentPix);
        }

        if(creditorAccountEntity != null) {
            var creditorAccount = new CreditorAccount()
                    .ispb(creditorAccountEntity.getIspb())
                    .issuer(creditorAccountEntity.getIssuer())
                    .number(creditorAccountEntity.getNumber())
                    .accountType(EnumAccountPaymentsType.fromValue(creditorAccountEntity.getAccountType()));
            paymentData.creditorAccount(creditorAccount);
        }

        payment.data(paymentData);
        return payment;
    }
}
