package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;


@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "consent_permissions")
public class ConsentPermissionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @NotNull
    @Column(name = "permission")
    private String permission;

    @NotNull
    @Column(name = "consent_id", nullable = false, updatable = false)
    private String consentId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consent_id", referencedColumnName = "consent_id", insertable = false, nullable = false, updatable = false)
    @NotAudited
    private ConsentEntity consent;

    public static ConsentPermissionEntity fromRequest(EnumConsentPermissions req, ConsentEntity consent) {
        ConsentPermissionEntity entity = new ConsentPermissionEntity();
        entity.setPermission(req.toString());
        entity.setConsentId(consent.getConsentId());
        entity.setConsent(consent);
        return entity;
    }

    public EnumConsentPermissions getDTO() {
        return EnumConsentPermissions.fromValue(permission);
    }
}
