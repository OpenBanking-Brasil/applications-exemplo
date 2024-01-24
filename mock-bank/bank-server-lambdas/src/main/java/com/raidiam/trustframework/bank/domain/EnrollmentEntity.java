package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "enrollments")
public class EnrollmentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "enrollment_id")
    private String enrollmentId;

    @Column(name = "account_holder_id")
    private UUID accountHolderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "account_holder_id", referencedColumnName = "account_holder_id", nullable = false, updatable = false, insertable = false)
    private AccountHolderEntity accountHolder;

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

    @Column(name = "business_document_identification")
    private String businessDocumentIdentification;

    @Column(name = "business_document_rel")
    private String businessDocumentRel;

    @EqualsAndHashCode.Exclude
    @Column(name = "transaction_limit")
    private String transactionLimit;

    @EqualsAndHashCode.Exclude
    @Column(name = "daily_limit")
    private String dailyLimit;

    @EqualsAndHashCode.Exclude
    @Column(name = "cancelled_by_document_identification")
    private String cancelledByDocumentIdentification;

    @EqualsAndHashCode.Exclude
    @Column(name = "cancelled_by_document_rel")
    private String cancelledByDocumentRel;

    @EqualsAndHashCode.Exclude
    @Column(name = "cancelled_from")
    private String cancelledFrom;

    @EqualsAndHashCode.Exclude
    @Column(name = "reject_reason")
    private String rejectReason;

    @EqualsAndHashCode.Exclude
    @Column(name = "rejected_at")
    private Date rejectedAt;

    @EqualsAndHashCode.Exclude
    @Column(name = "revocation_reason")
    private String revocationReason;

    @EqualsAndHashCode.Exclude
    @Column(name = "additional_information")
    private String additionalInformation;


    private static String buildEnrollmentId() {
        return String.format("urn:raidiambank:%s",  UUID.randomUUID());
    }

    public static EnrollmentEntity from(CreateEnrollment req, String clientId, String idempotencyKey, AccountEntity account, AccountHolderEntity accountHolder) {
        EnrollmentEntity entity = new EnrollmentEntity();

        entity.setEnrollmentId(buildEnrollmentId());
        entity.setClientId(clientId);

        if (account != null) entity.setAccountId(account.getAccountId());
        entity.setAccountHolderId(accountHolder.getAccountHolderId());
        entity.setAccountHolder(accountHolder);
        entity.setCreationDateTime(Date.from(Instant.now()));
        entity.setStatusUpdateDateTime(Date.from(Instant.now()));
        entity.setExpirationDateTime(Date.from(Instant.now().plus(Duration.ofMinutes(5))));
        entity.setStatus(EnumEnrollmentStatus.AWAITING_RISK_SIGNALS.name());
        entity.setIdempotencyKey(idempotencyKey);

        Optional.ofNullable(req.getData().getBusinessEntity())
                .map(BusinessEntity::getDocument)
                .ifPresent(d -> {
                    entity.setBusinessDocumentIdentification(d.getIdentification());
                    entity.setBusinessDocumentRel(d.getRel());
                });

        return entity;
    }

    public ResponseCreateEnrollment getDTO () {
        var enrollment = new ResponseCreateEnrollment();
        var consentData = new ResponseCreateEnrollmentData()
                .enrollmentId(enrollmentId)
                .creationDateTime(BankLambdaUtils.dateToOffsetDate(creationDateTime))
                .expirationDateTime(BankLambdaUtils.dateToOffsetDate(expirationDateTime))
                .statusUpdateDateTime(BankLambdaUtils.dateToOffsetDate(statusUpdateDateTime))
                .status(EnumEnrollmentStatus.fromValue(status));

        enrichData((loggedUser, businessEntity, debtorAccount) -> consentData.loggedUser(loggedUser)
                .businessEntity(businessEntity)
                .debtorAccount(debtorAccount));

        enrollment.data(consentData);
        return enrollment;
    }

    public ResponseEnrollment getResponseDTO() {
        var enrollment = new ResponseEnrollment();
        var enrollmentData = new ResponseEnrollmentData()
                .enrollmentId(enrollmentId)
                .creationDateTime(BankLambdaUtils.dateToOffsetDate(creationDateTime))
                .expirationDateTime(BankLambdaUtils.dateToOffsetDate(expirationDateTime))
                .statusUpdateDateTime(BankLambdaUtils.dateToOffsetDate(statusUpdateDateTime))
                .status(EnumEnrollmentStatus.fromValue(status))
                .transactionLimit(transactionLimit)
                .dailyLimit(dailyLimit);

        if(rejectReason != null) {
            enrollmentData.cancellation(new EnrollmentCancellation()
                    .reason(new EnrollmentCancellationReason()
                            .rejectionReason(EnrollmentRejectionReason.fromValue(rejectReason))
                    )
                    .rejectedAt(BankLambdaUtils.dateToOffsetDate(rejectedAt)))
                    .setStatus(EnumEnrollmentStatus.REJECTED);
        } else if (revocationReason != null) {
            enrollmentData.cancellation(new EnrollmentCancellation()
                    .reason(new EnrollmentCancellationReason()
                            .revocationReason(EnrollmentRevocationReason.fromValue(revocationReason))
                    )
                    .rejectedAt(BankLambdaUtils.dateToOffsetDate(rejectedAt)))
                    .setStatus(EnumEnrollmentStatus.REVOKED);
        }

        enrichData((loggedUser, businessEntity, debtorAccount) -> enrollmentData.loggedUser(loggedUser)
                .businessEntity(businessEntity)
                .debtorAccount(debtorAccount));

        enrollment.data(enrollmentData);
        return enrollment;
    }

    public boolean isTimeAuthorizationExpired() {
        LocalDateTime localDateTimeCreationPlusFive = LocalDateTime.ofInstant(creationDateTime.toInstant(), ZoneId.systemDefault()).plusMinutes(5L);
        return LocalDateTime.now().isAfter(localDateTimeCreationPlusFive);
    }

    private void enrichData(EnrollmentDataEnrichment enrichment) {
        LoggedUser loggedUser = null;
        BusinessEntity businessEntity = null;
        DebtorAccount debtorAccount = null;

        if(accountHolder != null) {
            loggedUser = accountHolder.getLoggedUser();
        }
        if(businessDocumentIdentification != null) {
            var businessEntityDocument = new BusinessEntityDocument()
                    .identification(businessDocumentIdentification)
                    .rel(businessDocumentRel);
            businessEntity = new BusinessEntity().document(businessEntityDocument);
        }
        if(accountEntity != null) {
            debtorAccount = accountEntity.getDebtorAccount();
        }
        enrichment.enrich(loggedUser, businessEntity, debtorAccount);
    }

    public static EnrollmentEntity fromRequest(CreateEnrollment req) {
        EnrollmentEntity entity = new EnrollmentEntity();
        entity.setEnrollmentId(buildEnrollmentId());
        entity.setCreationDateTime(Date.from(Instant.now()));
        entity.setStatusUpdateDateTime(Date.from(Instant.now()));
        entity.setStatus(EnumEnrollmentStatus.AWAITING_RISK_SIGNALS.toString());

        Optional.ofNullable(req.getData().getBusinessEntity())
                .map(BusinessEntity::getDocument)
                .ifPresent(d -> {
                    entity.setBusinessDocumentIdentification(d.getIdentification());
                    entity.setBusinessDocumentRel(d.getRel());
                });
        return entity;
    }

    private interface EnrollmentDataEnrichment {
        void enrich(LoggedUser loggedUser, BusinessEntity businessEntity, DebtorAccount debtorAccount);
    }


}
