package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.EnumFiliationType;
import com.raidiam.trustframework.mockbank.models.generated.PersonalIdentificationDataFiliation;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "personal_filiation")
public class PersonalFiliationEntity extends BaseEntity{

    @Id
    @GeneratedValue
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "type")
    private String type;

    @Column(name = "civil_name")
    private String civilName;

    @Column(name = "social_name")
    private String socialName;

    @Column(name = "personal_identifications_id")
    @Type(type = "pg-uuid")
    private UUID personalIdentificationsId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_identifications_id", referencedColumnName = "personal_identifications_id", insertable = false, nullable = false, updatable = false)
    private PersonalIdentificationsEntity identification;

    public PersonalIdentificationDataFiliation getDTO() {
        return new PersonalIdentificationDataFiliation()
                .type(EnumFiliationType.fromValue(this.getType()))
                .civilName(this.getCivilName())
                .socialName(this.getSocialName());
    }
}
