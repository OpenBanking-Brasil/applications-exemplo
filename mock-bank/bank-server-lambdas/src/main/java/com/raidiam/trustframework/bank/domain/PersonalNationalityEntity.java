package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.Nationality;
import com.raidiam.trustframework.mockbank.models.generated.NationalityV2;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "personal_nationality")
public class PersonalNationalityEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Type(type = "pg-uuid")
    @Column(name = "personal_nationality_id", unique = true, nullable = false, updatable = false, columnDefinition = "uuid DEFAULT uuid_generate_v4()")
    private UUID personalNationalityId;

    @Column(name = "other_nationalities_info")
    private String otherNationalitiesInfo;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "nationality")
    private Set<PersonalNationalityDocumentEntity> documents = new HashSet<>();

    @NotNull
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_identifications_id", referencedColumnName = "personal_identifications_id", nullable = false, updatable = false)
    private PersonalIdentificationsEntity identification;

    public Nationality getDTO() {
        return new Nationality()
                .otherNationalitiesInfo(this.getOtherNationalitiesInfo())
                .documents(this.getDocuments().stream().map(PersonalNationalityDocumentEntity::getDTO).collect(Collectors.toList()));
    }

    public NationalityV2 getDTOV2() {
        return new NationalityV2()
                .otherNationalitiesInfo(this.getOtherNationalitiesInfo())
                .documents(this.getDocuments().stream().map(PersonalNationalityDocumentEntity::getDTOV2).collect(Collectors.toList()));
    }

    public static PersonalNationalityEntity from(PersonalIdentificationsEntity identification, Nationality nationality) {
        var nationalityEmntity = new PersonalNationalityEntity();
        nationalityEmntity.setIdentification(identification);
        nationalityEmntity.setOtherNationalitiesInfo(nationality.getOtherNationalitiesInfo());

        var documentsList = nationality.getDocuments().stream()
                .map(d -> PersonalNationalityDocumentEntity.from(nationalityEmntity, d))
                .collect(Collectors.toSet());
        nationalityEmntity.setDocuments(documentsList);

        return nationalityEmntity;
    }
}
