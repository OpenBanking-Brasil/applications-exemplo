package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.Nationality;
import lombok.*;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "personal_nationality")
public class PersonalNationalityEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Generated(GenerationTime.INSERT)
    @Type(type = "pg-uuid")
    @Column(name = "personal_nationality_id", unique = true, nullable = false, updatable = false, columnDefinition = "uuid DEFAULT uuid_generate_v4()")
    private UUID personalNationalityId;

    @Column(name = "other_nationalities_info")
    private String otherNationalitiesInfo;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "nationality")
    private Set<PersonalNationalityDocumentEntity> documents;

    @Column(name = "personal_identifications_id")
    @Type(type = "pg-uuid")
    private UUID personalIdentificationsId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_identifications_id", referencedColumnName = "personal_identifications_id", insertable = false, nullable = false, updatable = false)
    private PersonalIdentificationsEntity identification;

    public Nationality getDTO() {
        return new Nationality()
                .otherNationalitiesInfo(this.getOtherNationalitiesInfo())
                .documents(this.getDocuments().stream().map(PersonalNationalityDocumentEntity::getDTO).collect(Collectors.toList()));
    }
}
