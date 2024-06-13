package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import com.vladmihalcea.hibernate.type.array.DateArrayType;
import lombok.*;
import org.hibernate.annotations.TypeDef;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@TypeDef(name = "date-array", typeClass = DateArrayType.class)
@Table(name = "payment_consents")
public class PaymentConsentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "account_holder_id")
    private UUID accountHolderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "account_holder_id", referencedColumnName = "account_holder_id", nullable = false, updatable = false, insertable = false)
    private AccountHolderEntity accountHolder;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "paymentConsent")
    private List<CreditorEntity> creditorEntities = new ArrayList<>();

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
    @Column(name = "start_date_time")
    private Date startDateTime;

    @EqualsAndHashCode.Exclude
    @Column(name = "status")
    private String status;

    @EqualsAndHashCode.Exclude
    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Column(name = "business_document_identification")
    private String businessDocumentIdentification;

    @Column(name = "business_document_rel")
    private String businessDocumentRel;


    @EqualsAndHashCode.Exclude
    @Column(name = "reject_reason_code")
    private String rejectReasonCode;

    @EqualsAndHashCode.Exclude
    @Column(name = "reject_reason_detail")
    private String rejectReasonDetail;

    @EqualsAndHashCode.Exclude
    @Column(name = "revoke_reason_code")
    private String revokeReasonCode;

    @EqualsAndHashCode.Exclude
    @Column(name = "revoke_reason_detail")
    private String revokeReasonDetail;

    @EqualsAndHashCode.Exclude
    @Column(name = "rejected_by")
    @Convert(converter = RecurringConsentsRejectedRevokedByConverter.class)
    private EnumRecurringConsentRejectRevokedBy rejectedBy;

    @EqualsAndHashCode.Exclude
    @Column(name = "rejected_from")
    @Convert(converter = RecurringConsentsRejectedRevokedFromConverter.class)
    private EnumRecurringConsentRejectRevokedFrom rejectedFrom;

    @EqualsAndHashCode.Exclude
    @Column(name = "rejected_at")
    private Date rejectedAt;

    @EqualsAndHashCode.Exclude
    @Column(name = "revoked_by")
    @Convert(converter = RecurringConsentsRejectedRevokedByConverter.class)
    private EnumRecurringConsentRejectRevokedBy revokedBy;

    @EqualsAndHashCode.Exclude
    @Column(name = "revoked_from")
    @Convert(converter = RecurringConsentsRejectedRevokedFromConverter.class)
    private EnumRecurringConsentRejectRevokedFrom revokedFrom;

    @EqualsAndHashCode.Exclude
    @Column(name = "revoked_at")
    private Date revokedAt;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "automatic_recurring_configuration_reference_id", referencedColumnName = "reference_id")
    private AutomaticRecurringConfiguration automaticRecurringConfiguration;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "post_sweeping_recurring_configuration_reference_id", referencedColumnName = "reference_id")
    private PostSweepingRecurringConfiguration postSweepingRecurringConfiguration;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "vrp_recurring_configuration_reference_id", referencedColumnName = "reference_id")
    private VrpRecurringConfiguration vrpRecurringConfiguration;


    public static PaymentConsentEntity from(CreatePaymentConsent req, String clientId, String idempotencyKey, AccountEntity account, AccountHolderEntity accountHolder) {
        PaymentConsentEntity entity = new PaymentConsentEntity();
        String paymentConsentId = buildId();
        entity.setPaymentConsentId(paymentConsentId);
        entity.setClientId(clientId);

        entity.getCreditorEntities().add(CreditorEntity.from(req.getData().getCreditor(), entity));
        if (account != null) entity.setAccountId(account.getAccountId());
        entity.setPaymentConsentPaymentEntity(PaymentConsentPaymentEntity.from(req.getData().getPayment()));
        entity.setAccountHolderId(accountHolder.getAccountHolderId());
        entity.setAccountHolder(accountHolder);

        entity.setCreationDateTime(Date.from(Instant.now()));
        entity.setStatusUpdateDateTime(Date.from(Instant.now()));
        entity.setExpirationDateTime(Date.from(Instant.now().plus(Duration.ofMinutes(5))));
        entity.setStatus(EnumConsentStatus.AWAITING_AUTHORISATION.name());
        entity.setIdempotencyKey(idempotencyKey);

        Optional.ofNullable(req.getData().getBusinessEntity())
                .map(BusinessEntity::getDocument)
                .ifPresent(d -> {
                    entity.setBusinessDocumentIdentification(d.getIdentification());
                    entity.setBusinessDocumentRel(d.getRel());
                });

        return entity;
    }

    public static PaymentConsentEntity fromV4(CreatePaymentConsentV4 req, String clientId, String idempotencyKey, AccountEntity account, AccountHolderEntity accountHolder) {
        PaymentConsentEntity entity = new PaymentConsentEntity();

        String paymentConsentId = buildId();
        entity.setPaymentConsentId(paymentConsentId);
        entity.setClientId(clientId);

        entity.getCreditorEntities().add(CreditorEntity.from(req.getData().getCreditor(), entity));
        if (account != null) entity.setAccountId(account.getAccountId());
        entity.setPaymentConsentPaymentEntity(PaymentConsentPaymentEntity.fromV4(req.getData().getPayment()));
        entity.setAccountHolderId(accountHolder.getAccountHolderId());
        entity.setAccountHolder(accountHolder);

        entity.setCreationDateTime(Date.from(BankLambdaUtils.getInstantInBrasil()));
        entity.setStatusUpdateDateTime(Date.from(BankLambdaUtils.getInstantInBrasil()));
        entity.setExpirationDateTime(Date.from(BankLambdaUtils.getInstantInBrasil().plus(Duration.ofMinutes(5))));
        entity.setStatus(EnumConsentStatus.AWAITING_AUTHORISATION.name());
        entity.setIdempotencyKey(idempotencyKey);

        Optional.ofNullable(req.getData().getBusinessEntity())
                .map(BusinessEntity::getDocument)
                .ifPresent(d -> {
                    entity.setBusinessDocumentIdentification(d.getIdentification());
                    entity.setBusinessDocumentRel(d.getRel());
                });

        return entity;
    }

    public static PaymentConsentEntity fromRecurringV1(CreateRecurringConsentV1 req, String clientId, String idempotencyKey, AccountEntity account, AccountHolderEntity accountHolder) {
        PaymentConsentEntity entity = new PaymentConsentEntity();
        entity.setPaymentConsentId(buildId());
        entity.setClientId(clientId);
        entity.setIdempotencyKey(idempotencyKey);
        entity.setStatus(EnumConsentStatus.AWAITING_AUTHORISATION.name());


        // Logged User
        entity.setAccountHolderId(accountHolder.getAccountHolderId());
        entity.setAccountHolder(accountHolder);

        // Business Document
        Optional.ofNullable(req.getData().getBusinessEntity())
                .map(BusinessEntity::getDocument)
                .ifPresent(businessEntityDocument -> {
                    entity.setBusinessDocumentIdentification(businessEntityDocument.getIdentification());
                    entity.setBusinessDocumentRel(businessEntityDocument.getRel());
                });


        //Debtor account
        entity.setAccountEntity(account);
        Optional.ofNullable(account).ifPresent(a -> entity.setAccountId(a.getAccountId()));

        // Creation Date Time
        entity.setCreationDateTime(Date.from(Instant.now().atZone(BankLambdaUtils.getBrasilZoneId()).toInstant()));

        // Status Update Date Time
        entity.setStatusUpdateDateTime(Date.from(Instant.now().atZone(BankLambdaUtils.getBrasilZoneId()).toInstant()));

        // Expiration Date Time
        if (req.getData().getExpirationDateTime() != null) {
            entity.setExpirationDateTime(BankLambdaUtils.offsetDateToDate(req.getData().getExpirationDateTime()
                    .atZoneSameInstant(BankLambdaUtils.getBrasilZoneId()).toOffsetDateTime()));
        }

        // Start Date Time
        entity.setStartDateTime(BankLambdaUtils.offsetDateToDate(Optional.ofNullable(req.getData().getStartDateTime())
                .orElse(BankLambdaUtils.getOffsetDateTimeInBrasil())));

        // Creditors
        List<CreditorEntity> creditors = req.getData().getCreditors().stream()
                .map(creditor -> CreditorEntity.from(creditor, entity))
                .collect(Collectors.toList());
        entity.setCreditorEntities(creditors);


        //Automatic Configuration
        Optional.ofNullable(req.getData().getRecurringConfiguration().getAutomatic())
                .ifPresent(a -> entity.setAutomaticRecurringConfiguration(AutomaticRecurringConfiguration.from(a)));

        //Post Sweeping Configuration
        Optional.ofNullable(req.getData().getRecurringConfiguration().getSweeping())
                .ifPresent(s -> entity.setPostSweepingRecurringConfiguration(PostSweepingRecurringConfiguration.from(s)));

        //VRP Sweeping Configuration
        Optional.ofNullable(req.getData().getRecurringConfiguration().getVrp())
                .ifPresent(v -> entity.setVrpRecurringConfiguration(VrpRecurringConfiguration.from(v)));

        return entity;
    }

    public ResponseRecurringConsent getRecurringDTOV1() {
        var data = new ResponseRecurringConsentData()
                .recurringConsentId(paymentConsentId)
                .statusUpdateDateTime(BankLambdaUtils.dateToOffsetDateTimeInBrasil(statusUpdateDateTime))
                .startDateTime(BankLambdaUtils.dateToOffsetDateTimeInBrasil(startDateTime))
                .creationDateTime(BankLambdaUtils.dateToOffsetDateTimeInBrasil(creationDateTime))
                .expirationDateTime(BankLambdaUtils.dateToOffsetDateTimeInBrasil(expirationDateTime))
                .status(EnumAuthorisationStatusType.fromValue(status))
                .loggedUser(accountHolder.getLoggedUser())
                .recurringConfiguration((RecurringConfiguration) new RecurringConfiguration()
                        .vrp(Optional.ofNullable(vrpRecurringConfiguration).map(VrpRecurringConfiguration::getDTO).orElse(null))
                        .sweeping(Optional.ofNullable(postSweepingRecurringConfiguration).map(PostSweepingRecurringConfiguration::getDTO).orElse(null))
                        .automatic(Optional.ofNullable(automaticRecurringConfiguration).map(AutomaticRecurringConfiguration::getDTO).orElse(null)))
                .creditors(creditorEntities.stream().map(CreditorEntity::getIdentification).collect(Collectors.toCollection(Creditors::new)))
                .debtorAccount(Optional.ofNullable(accountEntity).map(AccountEntity::getRecurringDebtorAccount).orElse(null));

        if(businessDocumentIdentification != null && businessDocumentRel != null){
            data.businessEntity(new BusinessEntity()
                    .document(new BusinessEntityDocument()
                            .identification(businessDocumentIdentification)
                            .rel(businessDocumentRel)));
        }

        if (rejectedBy != null) {
            data.rejection(new Rejection()
                    .rejectedAt(BankLambdaUtils.dateToOffsetDateTimeInBrasil(rejectedAt))
                    .rejectedBy(rejectedBy)
                    .rejectedFrom(rejectedFrom)
                    .reason(new ConsentRejectionReason()
                            .code(EnumConsentRejectionReasonType.fromValue(rejectReasonCode))
                            .detail(rejectReasonDetail)));
        }

        if (revokedBy != null) {
            data.revocation(new ResponseRecurringConsentDataRevocation()
                    .revokedAt(BankLambdaUtils.dateToOffsetDateTimeInBrasil(revokedAt))
                    .revokedBy(revokedBy)
                    .revokedFrom(revokedFrom)
                    .reason(new RecurringConsentRevokedReasonV1()
                            .code(RecurringConsentRevokedReasonV1.CodeEnum.fromValue(revokeReasonCode))
                            .detail(revokeReasonDetail)));
        }

        return new ResponseRecurringConsent().data(data);
    }


    public ResponsePaymentConsent getDTO() {
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

    public ResponsePaymentConsentV2 getDTOV2() {
        var consent = new ResponsePaymentConsentV2();
        var consentData = new ResponsePaymentConsentDataV2()
                .consentId(paymentConsentId)
                .creationDateTime(BankLambdaUtils.dateToOffsetDateTimeInBrasil(creationDateTime))
                .expirationDateTime(BankLambdaUtils.dateToOffsetDateTimeInBrasil(expirationDateTime))
                .statusUpdateDateTime(BankLambdaUtils.dateToOffsetDateTimeInBrasil(statusUpdateDateTime))
                .status(ResponsePaymentConsentDataV2.StatusEnum.fromValue(status));

        enrichData((loggedUser, businessEntity, creditor, paymentConsent, debtorAccount) -> consentData.loggedUser(loggedUser)
                .businessEntity(businessEntity)
                .creditor(creditor)
                .payment(paymentConsent)
                .debtorAccount(debtorAccount));

        consent.data(consentData);
        return consent;
    }

    public ResponsePaymentConsentV3 getDTOV3() {
        var consent = new ResponsePaymentConsentV3();
        var consentData = new ResponsePaymentConsentDataV3()
                .consentId(paymentConsentId)
                .creationDateTime(BankLambdaUtils.dateToOffsetDateTimeInBrasil(creationDateTime))
                .expirationDateTime(BankLambdaUtils.dateToOffsetDateTimeInBrasil(expirationDateTime))
                .statusUpdateDateTime(BankLambdaUtils.dateToOffsetDateTimeInBrasil(statusUpdateDateTime))
                .status(ResponsePaymentConsentDataV3.StatusEnum.fromValue(status));
        if (rejectReasonCode != null) {
            consentData.rejectionReason(new PaymentConsentRejectionReason()
                    .code(EnumConsentRejectionReasonType.fromValue(rejectReasonCode))
                    .detail(rejectReasonDetail));
        }

        enrichData((loggedUser, businessEntity, creditor, paymentConsent, debtorAccount) -> consentData.loggedUser(loggedUser)
                .businessEntity(businessEntity)
                .creditor(creditor)
                .payment(paymentConsent)
                .debtorAccount(debtorAccount));

        consent.data(consentData);
        return consent;
    }

    public ResponsePaymentConsentV4 getDTOV4() {
        var consentData = new ResponsePaymentConsentV4Data()
                .consentId(paymentConsentId)
                .creationDateTime(BankLambdaUtils.dateToOffsetDateTimeInBrasil(creationDateTime))
                .expirationDateTime(BankLambdaUtils.dateToOffsetDateTimeInBrasil(expirationDateTime))
                .statusUpdateDateTime(BankLambdaUtils.dateToOffsetDateTimeInBrasil(statusUpdateDateTime))
                .status(EnumAuthorisationStatusType.valueOf(status));

        if (rejectReasonCode != null) {
            consentData.rejectionReason(new ConsentRejectionReason()
                    .code(EnumConsentRejectionReasonType.fromValue(rejectReasonCode))
                    .detail(rejectReasonDetail));
        }

        enrichCreateDataV4((loggedUser, businessEntity, creditor, paymentConsent, debtorAccount) -> consentData.loggedUser(loggedUser)
                .businessEntity(businessEntity)
                .creditor(creditor)
                .payment(paymentConsent)
                .debtorAccount(debtorAccount));


        return new ResponsePaymentConsentV4().data(consentData);
    }

    public ResponseCreatePaymentConsentV4 getCreateDTOV4() {
        var consent = new ResponseCreatePaymentConsentV4();
        var consentData = new ResponseCreatePaymentConsentV4Data()
                .consentId(paymentConsentId)
                .creationDateTime(BankLambdaUtils.dateToOffsetDateTimeInBrasil(creationDateTime))
                .expirationDateTime(BankLambdaUtils.dateToOffsetDateTimeInBrasil(expirationDateTime))
                .statusUpdateDateTime(BankLambdaUtils.dateToOffsetDateTimeInBrasil(statusUpdateDateTime))
                .status(EnumAuthorisationStatusType.valueOf(status));

        enrichCreateDataV4((loggedUser, businessEntity, creditor, paymentConsent, debtorAccount) -> consentData.loggedUser(loggedUser)
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
                .creationDateTime(BankLambdaUtils.dateToOffsetDateTimeInBrasil(creationDateTime))
                .expirationDateTime(BankLambdaUtils.dateToOffsetDateTimeInBrasil(expirationDateTime))
                .statusUpdateDateTime(BankLambdaUtils.dateToOffsetDateTimeInBrasil(statusUpdateDateTime))
                .status(EnumAuthorisationStatusType.fromValue(status));

        enrichData((loggedUser, businessEntity, creditor, paymentConsent, debtorAccount) -> consentData.loggedUser(loggedUser)
                .businessEntity(businessEntity)
                .creditor(creditor)
                .payment(paymentConsent)
                .debtorAccount(debtorAccount));

        consentData.setSub(accountHolder.getUserId());
        consent.data(consentData);
        return consent;
    }

    public boolean hasSameDebtorAndCreditor() {
        if (accountEntity != null && accountEntity.getDebtorAccount() != null
                && paymentConsentPaymentEntity != null
                && paymentConsentPaymentEntity.getPaymentConsentDetails() != null
                && paymentConsentPaymentEntity.getPaymentConsentDetails().getCreditorAccountNumber() != null) {
            return accountEntity.getDebtorAccount().getNumber().equals(paymentConsentPaymentEntity.getPaymentConsentDetails().getCreditorAccountNumber());

        }
        return false;
    }

    public boolean isTimeAuthorizationExpired() {
        LocalDateTime localDateTimeCreationPlusFive = LocalDateTime.ofInstant(creationDateTime.toInstant(), ZoneId.systemDefault()).plusMinutes(5L);
        return LocalDateTime.now().isAfter(localDateTimeCreationPlusFive);
    }

    private void enrichData(PaymentDataEnrichment enrichment) {
        LoggedUser loggedUser = null;
        BusinessEntity businessEntity = null;
        Identification creditor = null;
        PaymentConsent paymentConsent = null;
        DebtorAccount debtorAccount = null;
        Details details = null;


        if (accountHolder != null) {
            loggedUser = accountHolder.getLoggedUser();
        }
        if (businessDocumentIdentification != null) {
            var businessEntityDocument = new BusinessEntityDocument()
                    .identification(businessDocumentIdentification)
                    .rel(businessDocumentRel);
            businessEntity = new BusinessEntity().document(businessEntityDocument);
        }

        if (!creditorEntities.isEmpty()) {
            CreditorEntity creditorEntity = creditorEntities.get(0);
            creditor = new Identification()
                    .personType(creditorEntity.getPersonType().toString())
                    .cpfCnpj(creditorEntity.getCpfCnpj())
                    .name(creditorEntity.getName());
        }

        if (paymentConsentPaymentEntity != null) {
            if (paymentConsentPaymentEntity.getPaymentConsentDetails() != null) {
                var detailsEntity = paymentConsentPaymentEntity.getPaymentConsentDetails();
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
            if (paymentConsentPaymentEntity.getPaymentDate() != null) {
                paymentConsent = new PaymentConsent()
                        .type(paymentConsentPaymentEntity.getPaymentType())
                        .date(BankLambdaUtils.dateToLocalDate(paymentConsentPaymentEntity.getPaymentDate()))
                        .currency(paymentConsentPaymentEntity.getCurrency())
                        .amount(paymentConsentPaymentEntity.getAmount())
                        .details(details);

            }
            if (paymentConsentPaymentEntity.getSchedule() != null) {
                Schedule schedule = new Schedule();
                schedule.setSingle(new ScheduleSingleSingle().date(BankLambdaUtils.dateToLocalDate(paymentConsentPaymentEntity.getSchedule())));
                paymentConsent = new PaymentConsent()
                        .type(paymentConsentPaymentEntity.getPaymentType())
                        .schedule(schedule)
                        .currency(paymentConsentPaymentEntity.getCurrency())
                        .amount(paymentConsentPaymentEntity.getAmount())
                        .details(details);
            }
        }

        if (accountEntity != null) {
            debtorAccount = accountEntity.getDebtorAccount();
        }
        enrichment.enrich(loggedUser, businessEntity, creditor, paymentConsent, debtorAccount);

    }

    public boolean hasTotalAllowedLimit() {
        return this.getPostSweepingRecurringConfiguration() != null &&
                this.getPostSweepingRecurringConfiguration().getDTO() != null &&
                this.getPostSweepingRecurringConfiguration().getDTO().getTotalAllowedAmount() != null;
    }

    public boolean hasPeriodicLimit() {
        return this.getPostSweepingRecurringConfiguration() != null && this.getPostSweepingRecurringConfiguration().getPeriodicLimits() != null;
    }
    public boolean hasSweepingDailyPeriodicLimit() {
        return hasPeriodicLimit()
                && this.getPostSweepingRecurringConfiguration().getPeriodicLimits().getPeriodicLimitsDayTransactionLimit() != null;
    }

    public boolean hasSweepingWeeklyPeriodicLimit() {
        return hasPeriodicLimit()
                && this.getPostSweepingRecurringConfiguration().getPeriodicLimits().getPeriodicLimitsWeekTransactionLimit() != null;
    }

    public boolean hasSweepingMonthlyPeriodicLimit() {
        return hasPeriodicLimit()
                && this.getPostSweepingRecurringConfiguration().getPeriodicLimits().getPeriodicLimitsMonthTransactionLimit() != null;
    }

    private void enrichCreateDataV4(PaymentDataEnrichmentV4 enrichment) {
        LoggedUser loggedUser = null;
        BusinessEntity businessEntity = null;
        Identification creditor = null;
        PaymentConsentV4Payment paymentConsent = null;
        DebtorAccount debtorAccount = null;
        Details details = null;
        PaymentConsentDetailsEntity detailsEntity = paymentConsentPaymentEntity.getPaymentConsentDetails();

        if (accountHolder != null) {
            loggedUser = accountHolder.getLoggedUser();
        }
        if (businessDocumentIdentification != null) {
            var businessEntityDocument = new BusinessEntityDocument()
                    .identification(businessDocumentIdentification)
                    .rel(businessDocumentRel);
            businessEntity = new BusinessEntity().document(businessEntityDocument);
        }

        if (!creditorEntities.isEmpty()) {
            CreditorEntity creditorEntity = creditorEntities.get(0);
            creditor = new Identification()
                    .personType(creditorEntity.getPersonType().toString())
                    .cpfCnpj(creditorEntity.getCpfCnpj())
                    .name(creditorEntity.getName());
        }

        if (detailsEntity != null) {
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

        paymentConsent = new PaymentConsentV4Payment()
                .type(EnumPaymentType.fromValue(paymentConsentPaymentEntity.getPaymentType()))
                .currency(paymentConsentPaymentEntity.getCurrency())
                .amount(paymentConsentPaymentEntity.getAmount())
                .date(BankLambdaUtils.dateToLocalDate(paymentConsentPaymentEntity.getPaymentDate()))
                .details(details);

        if (paymentConsentPaymentEntity.getScheduleSingleDate() != null) {
            paymentConsent.schedule((AllOfPaymentConsentV4PaymentSchedule) new AllOfPaymentConsentV4PaymentSchedule().single(new ScheduleSingleSingle()
                    .date(paymentConsentPaymentEntity.getScheduleSingleDate())));
        }


        if (paymentConsentPaymentEntity.getScheduleDailyStartDate() != null) {
            paymentConsent.schedule(
                    new AllOfPaymentConsentV4PaymentSchedule().daily(
                            new ScheduleDailyDaily()
                                    .startDate(paymentConsentPaymentEntity.getScheduleDailyStartDate())
                                    .quantity(paymentConsentPaymentEntity.getScheduleDailyQuantity())
                    )
            );
        }

        if (paymentConsentPaymentEntity.getScheduleWeeklyStartDate() != null) {
            paymentConsent.schedule(
                    new AllOfPaymentConsentV4PaymentSchedule().weekly(
                            new ScheduleWeeklyWeekly()
                                    .startDate(paymentConsentPaymentEntity.getScheduleWeeklyStartDate())
                                    .quantity(paymentConsentPaymentEntity.getScheduleWeeklyQuantity())
                                    .dayOfWeek(ScheduleWeeklyWeekly.DayOfWeekEnum.fromValue(
                                            paymentConsentPaymentEntity.getScheduleWeeklyDayOfWeek()))
                    )
            );
        }

        if (paymentConsentPaymentEntity.getScheduleMonthlyStartDate() != null) {
            paymentConsent.schedule(
                    new AllOfPaymentConsentV4PaymentSchedule().monthly(
                            new ScheduleMonthlyMonthly()
                                    .startDate(paymentConsentPaymentEntity.getScheduleMonthlyStartDate())
                                    .quantity(paymentConsentPaymentEntity.getScheduleMonthlyQuantity())
                                    .dayOfMonth(paymentConsentPaymentEntity.getScheduleMonthlyDayOfMonth())
                    )
            );

        }


        if (paymentConsentPaymentEntity.getScheduleCustomDates().length > 0) {
            paymentConsent.schedule(
                    new AllOfPaymentConsentV4PaymentSchedule().custom(
                            new ScheduleCustomCustom()
                                    .dates(List.of(paymentConsentPaymentEntity.getScheduleCustomDates()))
                                    .additionalInformation(paymentConsentPaymentEntity.getScheduleCustomAdditionalInformation())
                    )
            );
        }

        if (accountEntity != null) {
            debtorAccount = accountEntity.getDebtorAccount();
        }
        enrichment.enrich(loggedUser, businessEntity, creditor, paymentConsent, debtorAccount);
    }

    private static String buildId() {
        return String.format("urn:raidiambank:%s", UUID.randomUUID());
    }

    private interface PaymentDataEnrichment {
        void enrich(LoggedUser loggedUser, BusinessEntity businessEntity, Identification creditor, PaymentConsent paymentConsent, DebtorAccount debtorAccount);
    }

    private interface PaymentDataEnrichmentV4 {
        void enrich(LoggedUser loggedUser, BusinessEntity businessEntity, Identification creditor, PaymentConsentV4Payment paymentConsent, DebtorAccount debtorAccount);
    }

    @Converter
    static class RecurringConsentsRejectedRevokedByConverter implements AttributeConverter<EnumRecurringConsentRejectRevokedBy, String> {

        @Override
        public String convertToDatabaseColumn(EnumRecurringConsentRejectRevokedBy attribute) {
            return Optional.ofNullable(attribute)
                    .map(EnumRecurringConsentRejectRevokedBy::toString)
                    .orElse(null);
        }

        @Override
        public EnumRecurringConsentRejectRevokedBy convertToEntityAttribute(String dbData) {
            return Optional.ofNullable(dbData)
                    .map(EnumRecurringConsentRejectRevokedBy::fromValue)
                    .orElse(null);
        }
    }

    @Converter
    static class RecurringConsentsRejectedRevokedFromConverter implements AttributeConverter<EnumRecurringConsentRejectRevokedFrom, String> {

        @Override
        public String convertToDatabaseColumn(EnumRecurringConsentRejectRevokedFrom attribute) {
            return Optional.ofNullable(attribute)
                    .map(EnumRecurringConsentRejectRevokedFrom::toString)
                    .orElse(null);
        }

        @Override
        public EnumRecurringConsentRejectRevokedFrom convertToEntityAttribute(String dbData) {
            return Optional.ofNullable(dbData)
                    .map(EnumRecurringConsentRejectRevokedFrom::fromValue)
                    .orElse(null);
        }
    }


}
