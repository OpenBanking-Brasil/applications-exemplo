package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "payment_consents")
public class PaymentConsentEntity extends BaseEntity{

    @Id
    @GeneratedValue
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @OneToOne(cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @JoinColumn(name = "business_entity_document_id", referencedColumnName = "business_entity_document_id", nullable = true)
    private BusinessEntityDocumentEntity businessEntityDocumentEntity;

    @OneToOne(cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @JoinColumn(name = "logged_in_user_entity_document_id", referencedColumnName = "logged_in_user_entity_document_id")
    private LoggedInUserEntity loggedInUserEntityDocument;

    @OneToOne(cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @JoinColumn(name = "creditor_id", referencedColumnName = "creditor_id")
    private CreditorEntity creditorEntity;

    @OneToOne(cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @JoinColumn(name = "payment_id", referencedColumnName = "payment_id")
    private PaymentConsentPaymentEntity paymentConsentPaymentEntity;

    @OneToOne(cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @JoinColumn(name = "debtor_account_id", referencedColumnName = "debtor_account_id")
    private DebtorAccountEntity debtorAccountEntity;

    @EqualsAndHashCode.Exclude
    @Column(name = "client_id", nullable = false, updatable = false, insertable = true)
    private String clientId;

    @EqualsAndHashCode.Exclude
    @Column(name = "payment_consent_id", nullable = false, updatable = false, insertable = true)
    private String paymentConsentId;

    @EqualsAndHashCode.Exclude
    @Column(name = "creation_date_time")
    private Date creationDateTime;

    @EqualsAndHashCode.Exclude
    @Column(name = "expiration_date_time")
    private Date expirationDateTime;

    @EqualsAndHashCode.Exclude
    @Column(name = "status_update_date_time")
    private Date statusUpdateDateTime;

    @EqualsAndHashCode.Exclude
    @Column(name = "status")
    private String status;

    @EqualsAndHashCode.Exclude
    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    public static PaymentConsentEntity from (CreatePaymentConsent req, String clientId, String idempotencyKey) {
        PaymentConsentEntity entity = new PaymentConsentEntity();
        // TODO this is probably wrong, what is it supposed to be? Is it needed?
        UUID uuid = UUID.randomUUID();
        String paymentConsentId = String.format("urn:raidiambank:%s", uuid);
        entity.setPaymentConsentId(paymentConsentId);
        entity.setClientId(clientId);

        entity.setCreditorEntity(CreditorEntity.from(req.getData().getCreditor()));
        entity.setDebtorAccountEntity(DebtorAccountEntity.from(req.getData().getDebtorAccount()));
        entity.setPaymentConsentPaymentEntity(PaymentConsentPaymentEntity.from(req.getData().getPayment()));
        entity.setBusinessEntityDocumentEntity(BusinessEntityDocumentEntity.from(req.getData().getBusinessEntity()));
        entity.setLoggedInUserEntityDocument(LoggedInUserEntity.from(req.getData().getLoggedUser()));

        entity.setCreationDateTime(Date.from(Instant.now()));
        entity.setStatusUpdateDateTime(Date.from(Instant.now()));
        entity.setExpirationDateTime(Date.from(Instant.now().plus(Duration.ofMinutes(5))));
        entity.setStatus(ResponsePaymentConsentData.StatusEnum.AWAITING_AUTHORISATION.toString());
        entity.setIdempotencyKey(idempotencyKey);
        return entity;
    }

    public ResponsePaymentConsent getDTO () {
        var consent = new ResponsePaymentConsent();
        var consentData = new ResponsePaymentConsentData()
                .consentId(paymentConsentId)
                .creationDateTime(BankLambdaUtils.dateToOffsetDate(creationDateTime))
                .expirationDateTime(BankLambdaUtils.dateToOffsetDate(expirationDateTime))
                .statusUpdateDateTime(BankLambdaUtils.dateToOffsetDate(statusUpdateDateTime))
                .status(ResponsePaymentConsentData.StatusEnum.fromValue(status));

        enrichData((loggedUser, businessEntity, creditor, paymentConsent, debtorAccount) -> consentData.loggedUser(loggedUser)
                .businessEntity(businessEntity)
                .creditor(creditor)
                .payment(paymentConsent)
                .debtorAccount(debtorAccount));

        consent.data(consentData);
        return consent;
    }

    public ResponsePaymentConsentFull getFullDTO() {
        var consent = new ResponsePaymentConsentFull();
        var consentData = new ResponsePaymentConsentFullData()
                .consentId(paymentConsentId)
                .clientId(clientId)
                .creationDateTime(BankLambdaUtils.dateToOffsetDate(creationDateTime))
                .expirationDateTime(BankLambdaUtils.dateToOffsetDate(expirationDateTime))
                .statusUpdateDateTime(BankLambdaUtils.dateToOffsetDate(statusUpdateDateTime))
                .status(ResponsePaymentConsentFullData.StatusEnum.fromValue(status));

        enrichData((loggedUser, businessEntity, creditor, paymentConsent, debtorAccount) -> consentData.loggedUser(loggedUser)
                .businessEntity(businessEntity)
                .creditor(creditor)
                .payment(paymentConsent)
                .debtorAccount(debtorAccount));

        consent.data(consentData);
        return consent;
    }

    private void enrichData(PaymentDataEnrichment enrichment) {
        LoggedUser loggedUser = null;
        BusinessEntity businessEntity = null;
        Identification creditor = null;
        PaymentConsent paymentConsent = null;
        DebtorAccount debtorAccount = null;
        if(loggedInUserEntityDocument != null) {
            var loggedUserDocument = new LoggedUserDocument()
                    .identification(loggedInUserEntityDocument.getIdentification())
                    .rel(loggedInUserEntityDocument.getRel());
            loggedUser = new LoggedUser().document(loggedUserDocument);
        }
        if(businessEntityDocumentEntity != null) {
            var businessEntityDocument = new BusinessEntityDocument()
                    .identification(businessEntityDocumentEntity.getIdentification())
                    .rel(businessEntityDocumentEntity.getRel());
            businessEntity = new BusinessEntity().document(businessEntityDocument);
        }
        if(creditorEntity != null) {
            creditor = new Identification()
                    .personType(Identification.PersonTypeEnum.fromValue(creditorEntity.getPersonType()))
                    .cpfCnpj(creditorEntity.getCpfCnpj())
                    .name(creditorEntity.getName());
        }
        if(paymentConsentPaymentEntity != null) {
            paymentConsent = new PaymentConsent()
                    .type(PaymentConsent.TypeEnum.fromValue(paymentConsentPaymentEntity.getPaymentType()))
                    .date(BankLambdaUtils.dateToLocalDate(paymentConsentPaymentEntity.getPaymentDate()))
                    .currency(paymentConsentPaymentEntity.getCurrency())
                    .amount(paymentConsentPaymentEntity.getAmount());
        }
        if(debtorAccountEntity != null) {
             debtorAccount = new DebtorAccount()
                    .ispb(debtorAccountEntity.getIspb())
                    .issuer(debtorAccountEntity.getIssuer())
                    .number(debtorAccountEntity.getNumber())
                    .accountType(EnumAccountPaymentsType.fromValue(debtorAccountEntity.getAccountType()));
        }
        enrichment.enrich(loggedUser, businessEntity, creditor, paymentConsent, debtorAccount);

    }

    private interface PaymentDataEnrichment {
       void enrich(LoggedUser loggedUser, BusinessEntity businessEntity, Identification creditor, PaymentConsent paymentConsent, DebtorAccount debtorAccount);
    }


}
