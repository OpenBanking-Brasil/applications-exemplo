package com.raidiam.trustframework.bank.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;


@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "consent_account_ids")
public class ConsentAccountIdEntity {

    @Id
    @GeneratedValue
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @NotNull
    @Column(name = "account_id")
    private String accountId;

    @NotNull
    @Column(name = "consent_id", nullable = false, updatable = false)
    private String consentId;

    @NotNull
    @Column(name = "account_type")
    @Enumerated(EnumType.STRING)
    private LinkedAccountType accountType;

    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consent_id", referencedColumnName = "consent_id", insertable = false, nullable = false, updatable = false)
    @NotAudited
    private ConsentEntity consent;

    public String getDTO() {
        return accountId;
    }

    public Integer getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Integer referenceId) {
        this.referenceId = referenceId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getConsentId() {
        return consentId;
    }

    public void setConsentId(String consentId) {
        this.consentId = consentId;
    }

    public ConsentEntity getConsent() {
        return consent;
    }

    public void setConsent(ConsentEntity consent) {
        this.consent = consent;
    }

    public void setAccountType(LinkedAccountType accountType) {
        this.accountType = accountType;
    }

    public LinkedAccountType getAccountType() {
        return accountType;
    }

    public static ConsentAccountIdEntity fromRequest(String req, LinkedAccountType accountType, ConsentEntity consent) {
        ConsentAccountIdEntity entity = new ConsentAccountIdEntity();
        entity.setAccountId(req);
        entity.setAccountType(accountType);
        entity.setConsentId(consent.getConsentId());
        entity.setConsent(consent);
        return entity;
    }

}
