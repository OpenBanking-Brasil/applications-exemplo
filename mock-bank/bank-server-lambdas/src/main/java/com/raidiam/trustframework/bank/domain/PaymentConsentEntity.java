package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.*;
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
public class PaymentConsentEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @OneToOne(cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "business_entity_document_id", referencedColumnName = "business_entity_document_id", nullable = true)
    private BusinessEntityDocumentEntity businessEntityDocumentEntity;

    @Column(name = "account_holder_id")
    private UUID accountHolderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "account_holder_id", referencedColumnName = "account_holder_id", nullable = false, updatable = false, insertable = false)
    private AccountHolderEntity accountHolder;

    @OneToOne(cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "creditor_id", referencedColumnName = "creditor_id")
    private CreditorEntity creditorEntity;

    @OneToOne(cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "payment_id", referencedColumnName = "payment_id")
    private PaymentConsentPaymentEntity paymentConsentPaymentEntity;

    @Column(name = "account_id")
    private UUID accountId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "account_id", insertable = false, nullable = false, updatable = false)
    private AccountEntity accountEntity;

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

    public static PaymentConsentEntity from (CreatePaymentConsent req, String clientId, String idempotencyKey, AccountEntity account, AccountHolderEntity accountHolder) {
        PaymentConsentEntity entity = new PaymentConsentEntity();
        // TODO this is probably wrong, what is it supposed to be? Is it needed?
        UUID uuid = UUID.randomUUID();
        String paymentConsentId = String.format("urn:raidiambank:%s", uuid);
        entity.setPaymentConsentId(paymentConsentId);
        entity.setClientId(clientId);

        entity.setCreditorEntity(CreditorEntity.from(req.getData().getCreditor()));
        if (account != null) entity.setAccountId(account.getAccountId());
        entity.setPaymentConsentPaymentEntity(PaymentConsentPaymentEntity.from(req.getData().getPayment()));
        entity.setBusinessEntityDocumentEntity(BusinessEntityDocumentEntity.from(req.getData().getBusinessEntity()));
        entity.setAccountHolderId(accountHolder.getAccountHolderId());
        entity.setAccountHolder(accountHolder);

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

        consentData.setSub(accountHolder.getUserId());
        consent.data(consentData);
        return consent;
    }

    private void enrichData(PaymentDataEnrichment enrichment) {
        LoggedUser loggedUser = null;
        BusinessEntity businessEntity = null;
        Identification creditor = null;
        PaymentConsent paymentConsent = null;
        DebtorAccount debtorAccount = null;
        Details details = null;
        PaymentConsentDetailsEntity detailsEntity = paymentConsentPaymentEntity.getPaymentConsentDetails();

        if(accountHolder != null) {
            loggedUser = accountHolder.getLoggedUser();
        }
        if(businessEntityDocumentEntity != null) {
            var businessEntityDocument = new BusinessEntityDocument()
                    .identification(businessEntityDocumentEntity.getIdentification())
                    .rel(businessEntityDocumentEntity.getRel());
            businessEntity = new BusinessEntity().document(businessEntityDocument);
        }
        if(creditorEntity != null) {
            creditor = new Identification()
                    .personType(creditorEntity.getPersonType())
                    .cpfCnpj(creditorEntity.getCpfCnpj())
                    .name(creditorEntity.getName());
        }

        if(detailsEntity != null) {
            details = new Details()
                    .proxy(detailsEntity.getProxy())
                    .localInstrument(EnumLocalInstrument.fromValue(detailsEntity.getLocalInstrument()))
                    .qrCode(detailsEntity.getQrCode())
                    .creditorAccount(new CreditorAccount()
                            .accountType(EnumAccountPaymentsType.fromValue(detailsEntity.getCreditorAccountType()))
                            .ispb(detailsEntity.getCreditorIspb())
                            .number(detailsEntity.getCreditorAccountNumber())
                            .issuer(detailsEntity.getCreditorIssuer())
                    );

        }
        if(paymentConsentPaymentEntity.getPaymentDate() != null) {
            paymentConsent = new PaymentConsent()
                    .type(paymentConsentPaymentEntity.getPaymentType())
                    .date(BankLambdaUtils.dateToLocalDate(paymentConsentPaymentEntity.getPaymentDate()))
                    .currency(paymentConsentPaymentEntity.getCurrency())
                    .amount(paymentConsentPaymentEntity.getAmount())
                    .details(details);

        }
        if (paymentConsentPaymentEntity.getSchedule() != null){
            paymentConsent = new PaymentConsent()
                    .type(paymentConsentPaymentEntity.getPaymentType())
                    .schedule(new Schedule().single(new Single().date(BankLambdaUtils.dateToLocalDate(paymentConsentPaymentEntity.getSchedule()))))
                    .currency(paymentConsentPaymentEntity.getCurrency())
                    .amount(paymentConsentPaymentEntity.getAmount())
                    .details(details);
        }

        if(accountEntity != null) {
            debtorAccount = accountEntity.getDebtorAccount();
        }
        enrichment.enrich(loggedUser, businessEntity, creditor, paymentConsent, debtorAccount);

    }

    private interface PaymentDataEnrichment {
        void enrich(LoggedUser loggedUser, BusinessEntity businessEntity, Identification creditor, PaymentConsent paymentConsent, DebtorAccount debtorAccount);
    }


}
