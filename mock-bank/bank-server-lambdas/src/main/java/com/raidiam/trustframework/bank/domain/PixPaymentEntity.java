package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Entity
//@Audited //- If you switch this on, hibernate decides that the payment_consent_id in the aud table needs to be an integer :/
@Table(name = "pix_payments")
public class PixPaymentEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @EqualsAndHashCode.Exclude
    @Column(name = "transaction_identification")
    private String transactionIdentification;

    @EqualsAndHashCode.Exclude
    @Column(name = "end_to_end_id")
    private String endToEndId;

    @EqualsAndHashCode.Exclude
    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @EqualsAndHashCode.Exclude
    @Column(name = "cancellation_from")
    private String cancellationFrom;

    @EqualsAndHashCode.Exclude
    @Column(name = "date")
    private LocalDate date;

    @EqualsAndHashCode.Exclude
    @Column(name = "document_identification")
    private String documentIdentification;

    @EqualsAndHashCode.Exclude
    @Column(name = "document_rel")
    private String documentRel;

    @EqualsAndHashCode.Exclude
    @Column(name = "ibge_town_code")
    private String ibgeTownCode;

    @EqualsAndHashCode.Exclude
    @Column(name = "authorisation_flow")
    @Convert(converter = AuthorizationFlowConverter.class)
    private EnumAuthorisationFlow authorisationFlow;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reference_id", referencedColumnName = "payment_reference_id", insertable = false, updatable = false)
    private AutomaticPaymentRiskSignalsEntity automaticPaymentRiskSignalsEntity;



    public static PixPaymentEntity from(Object createPixPaymentData, PaymentConsentEntity paymentConsentEntity, String status, String idempotencyKey) {
        var pixPaymentEntity = new PixPaymentEntity();
        UUID uuid = UUID.randomUUID();
        pixPaymentEntity.setPaymentId(uuid.toString());
        pixPaymentEntity.setPaymentConsentEntity(paymentConsentEntity);
        pixPaymentEntity.setCreationDateTime(Date.from(Instant.now()));
        pixPaymentEntity.setStatusUpdateDateTime(Date.from(Instant.now()));
        pixPaymentEntity.setIdempotencyKey(idempotencyKey);
        pixPaymentEntity.setPaymentId(uuid.toString());
        pixPaymentEntity.setPaymentConsentEntity(paymentConsentEntity);
        pixPaymentEntity.setStatus(status);
        pixPaymentEntity.setCreationDateTime(Date.from(Instant.now()));
        pixPaymentEntity.setStatusUpdateDateTime(Date.from(Instant.now()));

        pixPaymentEntity.setIdempotencyKey(idempotencyKey);

        if(createPixPaymentData instanceof CreatePixPayment) {
            return fromV1(((CreatePixPayment) createPixPaymentData).getData(), pixPaymentEntity);
        } else if(createPixPaymentData instanceof CreatePixPaymentV2) {
            return fromV2(((CreatePixPaymentV2) createPixPaymentData).getData(), pixPaymentEntity);
        } else if(createPixPaymentData instanceof CreatePixPaymentV3){
            return fromV3(((CreatePixPaymentV3) createPixPaymentData).getData(), pixPaymentEntity);
        } else if(createPixPaymentData instanceof CreatePixPaymentDataV4) {
            return fromV4(((CreatePixPaymentDataV4) createPixPaymentData), pixPaymentEntity);
        }  else {
            return fromRecurringPaymentsV1(((CreateRecurringPixPaymentV1Data) createPixPaymentData), pixPaymentEntity);
        }
    }
    private static PixPaymentEntity fromV1(CreatePixPaymentData createPixPaymentData, PixPaymentEntity pixPaymentEntity) {
        pixPaymentEntity.setEndToEndId(createPixPaymentData.getEndToEndId());
        pixPaymentEntity.setLocalInstrument(createPixPaymentData.getLocalInstrument().toString());
        pixPaymentEntity.setRemittanceInformation(createPixPaymentData.getRemittanceInformation());
        pixPaymentEntity.setQrCode(createPixPaymentData.getQrCode());
        pixPaymentEntity.setProxy(createPixPaymentData.getProxy());
        pixPaymentEntity.setPixPaymentPaymentEntity(PixPaymentPaymentEntity.from(createPixPaymentData.getPayment()));
        pixPaymentEntity.setCreditorAccountEntity(CreditorAccountEntity.from(createPixPaymentData.getCreditorAccount()));
        pixPaymentEntity.setTransactionIdentification(createPixPaymentData.getTransactionIdentification());
        return pixPaymentEntity;
    }

    private static PixPaymentEntity fromV2(CreatePixPaymentDataV2 createPixPaymentDataV2, PixPaymentEntity pixPaymentEntity) {
        pixPaymentEntity.setEndToEndId(createPixPaymentDataV2.getEndToEndId());
        pixPaymentEntity.setLocalInstrument(createPixPaymentDataV2.getLocalInstrument().toString());
        pixPaymentEntity.setRemittanceInformation(createPixPaymentDataV2.getRemittanceInformation());
        pixPaymentEntity.setQrCode(createPixPaymentDataV2.getQrCode());
        pixPaymentEntity.setProxy(createPixPaymentDataV2.getProxy());

        pixPaymentEntity.setPixPaymentPaymentEntity(PixPaymentPaymentEntity.from(createPixPaymentDataV2.getPayment()));
        pixPaymentEntity.setCreditorAccountEntity(CreditorAccountEntity.from(createPixPaymentDataV2.getCreditorAccount()));
        pixPaymentEntity.setTransactionIdentification(createPixPaymentDataV2.getTransactionIdentification());

        return pixPaymentEntity;
    }
    private static PixPaymentEntity fromV3(CreatePixPaymentDataV3 createPixPaymentDataV3, PixPaymentEntity pixPaymentEntity) {
        pixPaymentEntity.setEndToEndId(createPixPaymentDataV3.getEndToEndId());
        pixPaymentEntity.setLocalInstrument(createPixPaymentDataV3.getLocalInstrument().toString());
        pixPaymentEntity.setRemittanceInformation(createPixPaymentDataV3.getRemittanceInformation());
        pixPaymentEntity.setQrCode(createPixPaymentDataV3.getQrCode());
        pixPaymentEntity.setProxy(createPixPaymentDataV3.getProxy());
        pixPaymentEntity.setPixPaymentPaymentEntity(PixPaymentPaymentEntity.from(createPixPaymentDataV3.getPayment()));
        pixPaymentEntity.setCreditorAccountEntity(CreditorAccountEntity.from(createPixPaymentDataV3.getCreditorAccount()));
        pixPaymentEntity.setAuthorisationFlow(createPixPaymentDataV3.getAuthorisationFlow());
        pixPaymentEntity.setTransactionIdentification(createPixPaymentDataV3.getTransactionIdentification());

        return pixPaymentEntity;
    }

    private static PixPaymentEntity fromV4(CreatePixPaymentDataV4 createPixPaymentDataV4, PixPaymentEntity pixPaymentEntity) {
        pixPaymentEntity.setEndToEndId(createPixPaymentDataV4.getEndToEndId());
        pixPaymentEntity.setLocalInstrument(createPixPaymentDataV4.getLocalInstrument().toString());
        pixPaymentEntity.setRemittanceInformation(createPixPaymentDataV4.getRemittanceInformation());
        pixPaymentEntity.setQrCode(createPixPaymentDataV4.getQrCode());
        pixPaymentEntity.setProxy(createPixPaymentDataV4.getProxy());
        pixPaymentEntity.setPixPaymentPaymentEntity(PixPaymentPaymentEntity.from(createPixPaymentDataV4.getPayment()));
        pixPaymentEntity.setCreditorAccountEntity(CreditorAccountEntity.from(createPixPaymentDataV4.getCreditorAccount()));
        pixPaymentEntity.setAuthorisationFlow(Optional.ofNullable(createPixPaymentDataV4.getAuthorisationFlow()).orElse(EnumAuthorisationFlow.HYBRID_FLOW));
        pixPaymentEntity.setTransactionIdentification(createPixPaymentDataV4.getTransactionIdentification());

        return pixPaymentEntity;
    }

    private static PixPaymentEntity fromRecurringPaymentsV1(CreateRecurringPixPaymentV1Data data, PixPaymentEntity pixPaymentEntity) {
        pixPaymentEntity.setEndToEndId(data.getEndToEndId());
        pixPaymentEntity.setLocalInstrument(data.getLocalInstrument().toString());
        pixPaymentEntity.setRemittanceInformation(data.getRemittanceInformation());
        pixPaymentEntity.setProxy(data.getProxy());
        pixPaymentEntity.setPixPaymentPaymentEntity(PixPaymentPaymentEntity.from(data.getPayment()));
        pixPaymentEntity.setCreditorAccountEntity(CreditorAccountEntity.from(data.getCreditorAccount()));
        pixPaymentEntity.setAuthorisationFlow(Optional.ofNullable(data.getAuthorisationFlow()).orElse(EnumAuthorisationFlow.HYBRID_FLOW));
        pixPaymentEntity.setTransactionIdentification(data.getTransactionIdentification());
        pixPaymentEntity.setDate(data.getDate());
        pixPaymentEntity.setDocumentRel(data.getDocument().getRel());
        pixPaymentEntity.setDocumentIdentification(data.getDocument().getIdentification());
        pixPaymentEntity.setIbgeTownCode(data.getIbgeTownCode());



        if (data.getRiskSignals() != null) {
            pixPaymentEntity.setAutomaticPaymentRiskSignalsEntity(AutomaticPaymentRiskSignalsEntity.from(pixPaymentEntity.getPaymentId(), data.getRiskSignals()));
        }

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
                .cnpjInitiator("50685362000141")
                .transactionIdentification(transactionIdentification)
                .endToEndId(endToEndId);

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

    public ResponsePixPaymentV2 getDTOV2() {
        var payment = new ResponsePixPaymentV2();
        var paymentData = new ResponsePixPaymentDataV2()
                .paymentId(paymentId)
                .consentId(paymentConsentEntity.getPaymentConsentId())
                .creationDateTime(BankLambdaUtils.dateToOffsetDate(creationDateTime))
                .statusUpdateDateTime(BankLambdaUtils.dateToOffsetDate(statusUpdateDateTime))
                .proxy(proxy)
                .status(EnumPaymentStatusTypeV2.fromValue(status))
                .localInstrument(EnumLocalInstrument.fromValue(localInstrument))
                .remittanceInformation(remittanceInformation)
                .cnpjInitiator("50685362000151")
                .transactionIdentification(transactionIdentification)
                .endToEndId(endToEndId)
                .debtorAccount(paymentConsentEntity.getAccountEntity().getDebtorAccount());

        if(rejectionReason != null && !rejectionReason.isEmpty()) {
            paymentData.setRejectionReason(new RejectionReasonV2().code(RejectionReasonV2.CodeEnum.fromValue(rejectionReason)).detail(rejectionReason));
        }

        if(EnumPaymentStatusTypeV2.CANC.equals(paymentData.getStatus())) {
            paymentData.setCancellation(new Cancellation().reason(Cancellation.ReasonEnum.fromValue(cancellationReason))
                    .cancelledFrom(Cancellation.CancelledFromEnum.fromValue(cancellationFrom))
                    .cancelledAt(OffsetDateTime.now())
                    .cancelledBy(paymentConsentEntity.getAccountHolder().getLoggedUser()));
        }

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

    public ResponsePixPaymentV3 getDTOV3() {
        var payment = new ResponsePixPaymentV3();
        var paymentData = new ResponsePixPaymentDataV3()
                .paymentId(paymentId)
                .consentId(paymentConsentEntity.getPaymentConsentId())
                .creationDateTime(BankLambdaUtils.dateToOffsetDate(creationDateTime))
                .statusUpdateDateTime(BankLambdaUtils.dateToOffsetDate(statusUpdateDateTime))
                .proxy(proxy)
                .status(EnumPaymentStatusTypeV2.fromValue(status))
                .localInstrument(EnumLocalInstrument.fromValue(localInstrument))
                .remittanceInformation(remittanceInformation)
                .cnpjInitiator("50685362000131")
                .transactionIdentification(transactionIdentification)
                .endToEndId(endToEndId)
                .debtorAccount(paymentConsentEntity.getAccountEntity().getDebtorAccount())
                .authorisationFlow(authorisationFlow);

        if(rejectionReason != null && !rejectionReason.isEmpty()) {
            paymentData.setRejectionReason(new RejectionReasonV2().code(RejectionReasonV2.CodeEnum.fromValue(rejectionReason)).detail(rejectionReason));
        }

        if(EnumPaymentStatusTypeV2.CANC.equals(paymentData.getStatus())) {
            paymentData.setCancellation(new Cancellation().reason(Cancellation.ReasonEnum.fromValue(cancellationReason))
                    .cancelledFrom(Cancellation.CancelledFromEnum.fromValue(cancellationFrom))
                    .cancelledAt(OffsetDateTime.now())
                    .cancelledBy(paymentConsentEntity.getAccountHolder().getLoggedUser()));
        }

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

    public ResponsePixPaymentDataV4 getDTOV4() {
        var paymentData = new ResponsePixPaymentDataV4()
                .paymentId(paymentId)
                .consentId(paymentConsentEntity.getPaymentConsentId())
                .creationDateTime(BankLambdaUtils.dateToOffsetDate(creationDateTime))
                .statusUpdateDateTime(BankLambdaUtils.dateToOffsetDate(statusUpdateDateTime))
                .proxy(proxy)
                .status(EnumPaymentStatusTypeV2.fromValue(status))
                .localInstrument(EnumLocalInstrument.fromValue(localInstrument))
                .remittanceInformation(remittanceInformation)
                .cnpjInitiator("50685362000131")
                .transactionIdentification(transactionIdentification)
                .endToEndId(endToEndId)
                .debtorAccount(paymentConsentEntity.getAccountEntity().getDebtorAccount())
                .authorisationFlow(authorisationFlow);
        
        if(rejectionReason != null && !rejectionReason.isEmpty()) {
            paymentData.setRejectionReason(new RejectionReasonV2().code(RejectionReasonV2.CodeEnum.fromValue(rejectionReason)).detail(rejectionReason));
        }

        if(EnumPaymentStatusTypeV2.CANC.equals(paymentData.getStatus())) {
            paymentData.setCancellation(new PixPaymentCancellationV4().reason(EnumPaymentCancellationReasonTypeV4.fromValue(cancellationReason))
                    .cancelledFrom(EnumPaymentCancellationFromTypeV4.fromValue(cancellationFrom))
                    .cancelledAt(BankLambdaUtils.getOffsetDateTimeInBrasil().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")))
                    .cancelledBy(new PixPaymentCancellationV4CancelledBy().document(paymentConsentEntity.getAccountHolder().getLoggedUser().getDocument())));
        }

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

        return paymentData;
    }

    public ResponseRecurringPixPaymentsData postRecurringPixPaymentV1DTO() {
        ResponseRecurringPixPaymentsData data = getRecurringPixPaymentV1DTO();
        data.proxy(proxy)
        .localInstrument(EnumLocalInstrument.fromValue(localInstrument));

        return data;
    }

    public ResponseRecurringPixPaymentsData getRecurringPixPaymentsListV1DTO() {
        var rppData = new ResponseRecurringPixPaymentsData()
                .recurringPaymentId(paymentId)
                .recurringConsentId(paymentConsentEntity.getPaymentConsentId())
                .creationDateTime(BankLambdaUtils.dateToOffsetDateTimeInBrasil(creationDateTime))
                .statusUpdateDateTime(BankLambdaUtils.dateToOffsetDateTimeInBrasil(statusUpdateDateTime))
                .date(date)
                .status(EnumPaymentStatusTypeV2.fromValue(status))
                .remittanceInformation(remittanceInformation)
                .transactionIdentification(transactionIdentification)
                .endToEndId(endToEndId)
                .payment(new PaymentPix()
                        .amount(this.pixPaymentPaymentEntity.getAmount())
                        .currency(this.pixPaymentPaymentEntity.getCurrency()));


        if (documentIdentification != null && documentRel != null) {
            rppData.document(new Document().rel(documentRel).identification(documentIdentification));
        }

        return rppData;
    }

    public ResponseRecurringPixPaymentsData getRecurringPixPaymentV1DTO() {
        var rppData = getRecurringPixPaymentsListV1DTO()
                .cnpjInitiator("50685362000131")
                .authorisationFlow(authorisationFlow)
                .ibgeTownCode(ibgeTownCode);

        if(creditorAccountEntity != null) {
            var creditorAccount = new CreditorAccount()
                    .ispb(creditorAccountEntity.getIspb())
                    .issuer(creditorAccountEntity.getIssuer())
                    .number(creditorAccountEntity.getNumber())
                    .accountType(EnumAccountPaymentsType.fromValue(creditorAccountEntity.getAccountType()));
            rppData.creditorAccount(creditorAccount);
        }
        return rppData;
    }

    public ResponsePatchPixConsentV4Data getPatchDTOV4() {
        return new ResponsePatchPixConsentV4Data()
                .paymentId(paymentId)
                .statusUpdateDateTime(BankLambdaUtils.dateToOffsetDate(statusUpdateDateTime));
    }

    @Converter
    static class AuthorizationFlowConverter implements AttributeConverter<EnumAuthorisationFlow, String> {

        @Override
        public String convertToDatabaseColumn(EnumAuthorisationFlow attribute) {
            return Optional.ofNullable(attribute)
                    .map(EnumAuthorisationFlow::toString)
                    .orElse(EnumAuthorisationFlow.HYBRID_FLOW.name());
        }

        @Override
        public EnumAuthorisationFlow convertToEntityAttribute(String dbData) {
            return Optional.ofNullable(dbData)
                    .map(EnumAuthorisationFlow::fromValue)
                    .orElse(EnumAuthorisationFlow.HYBRID_FLOW);
        }
    }
}
