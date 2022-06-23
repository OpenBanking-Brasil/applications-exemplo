package com.raidiam.trustframework.bank.domain;

import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import java.util.UUID;

@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "consent_accounts")
public class ConsentAccountEntity extends BaseEntity  {

    @Id
    @GeneratedValue
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "consent_id")
    private String consentId;

    @Column(name = "account_id")
    private UUID accountId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consent_id", referencedColumnName = "consent_id", insertable = false, nullable = false, updatable = false)
    @NotAudited
    private ConsentEntity consent;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    // this is given "EAGER" fetch type, because there is no back-link from accounts to this table
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", referencedColumnName = "account_id", insertable = false, nullable = false, updatable = false)
    @NotAudited
    private AccountEntity account;

    public ConsentAccountEntity(ConsentEntity consent, AccountEntity account){
        this.consentId = consent.getConsentId();
        this.accountId = account.getAccountId();
    }
}
