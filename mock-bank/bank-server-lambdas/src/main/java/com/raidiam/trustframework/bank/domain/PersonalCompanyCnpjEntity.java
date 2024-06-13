package com.raidiam.trustframework.bank.domain;

import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "personal_identifications_company_cnpj")
public class PersonalCompanyCnpjEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "company_cnpj")
    private String companyCnpj;

    @NotNull
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_identifications_id", referencedColumnName = "personal_identifications_id", nullable = false, updatable = false)
    private PersonalIdentificationsEntity identification;

    public String getDTO() {
        return this.companyCnpj;
    }

    public static PersonalCompanyCnpjEntity from(PersonalIdentificationsEntity identification, String personalCompanyCnpj) {
        var personalCompanyCnpjEntity = new PersonalCompanyCnpjEntity();
        personalCompanyCnpjEntity.setIdentification(identification);
        personalCompanyCnpjEntity.setCompanyCnpj(personalCompanyCnpj);
        return personalCompanyCnpjEntity;
    }
}
