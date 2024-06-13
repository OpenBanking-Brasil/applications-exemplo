package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.EnumFiliationType;
import com.raidiam.trustframework.mockbank.models.generated.EnumFiliationTypeV2;
import com.raidiam.trustframework.mockbank.models.generated.PersonalIdentificationDataFiliation;
import com.raidiam.trustframework.mockbank.models.generated.PersonalIdentificationDataV2Filiation;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "personal_filiation")
public class PersonalFiliationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "type")
    private String type;

    @Column(name = "civil_name")
    private String civilName;

    @Column(name = "social_name")
    private String socialName;

    @NotNull
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_identifications_id", referencedColumnName = "personal_identifications_id", nullable = false, updatable = false)
    private PersonalIdentificationsEntity identification;

    public PersonalIdentificationDataFiliation getDTO() {
        return new PersonalIdentificationDataFiliation()
                .type(EnumFiliationType.fromValue(this.getType()))
                .civilName(this.getCivilName())
                .socialName(this.getSocialName());
    }

    public PersonalIdentificationDataV2Filiation getDTOV2() {
        return new PersonalIdentificationDataV2Filiation()
                .type(EnumFiliationTypeV2.fromValue(this.getType()))
                .civilName(this.getCivilName())
                .socialName(this.getSocialName());
    }

    public static PersonalFiliationEntity from(PersonalIdentificationsEntity identification, PersonalIdentificationDataFiliation personalFiliation) {
        var personalFiliationEntity = new PersonalFiliationEntity();
        personalFiliationEntity.setIdentification(identification);
        personalFiliationEntity.setType(personalFiliation.getType().name());
        personalFiliationEntity.setCivilName(personalFiliation.getCivilName());
        personalFiliationEntity.setSocialName(personalFiliation.getSocialName());
        return personalFiliationEntity;
    }
}
