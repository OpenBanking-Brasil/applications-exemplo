package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.EnumProcuratorsTypePersonal;
import com.raidiam.trustframework.mockbank.models.generated.EnumProcuratorsTypePersonalV2;
import com.raidiam.trustframework.mockbank.models.generated.PersonalProcurator;
import com.raidiam.trustframework.mockbank.models.generated.PersonalProcuratorV2;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "personal_financial_relations_procurators")
public class PersonalFinancialRelationsProcuratorEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "type")
    private String type;

    @Column(name = "cpf_number")
    private String cpfNumber;

    @Column(name = "civil_name")
    private String civilName;

    @Column(name = "social_name")
    private String socialName;

    @NotNull
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_financial_relations_id", referencedColumnName = "personal_financial_relations_id", nullable = false, updatable = false)
    private PersonalFinancialRelationsEntity financialRelations;

    public PersonalProcurator getDTO() {
        return new PersonalProcurator()
                .type(EnumProcuratorsTypePersonal.valueOf(this.getType()))
                .cpfNumber(this.getCpfNumber())
                .civilName(this.getCivilName())
                .socialName(this.getSocialName());
    }

    public PersonalProcuratorV2 getDtoV2() {
        return new PersonalProcuratorV2()
                .type(EnumProcuratorsTypePersonalV2.valueOf(this.getType()))
                .cpfNumber(this.getCpfNumber())
                .civilName(this.getCivilName())
                .socialName(this.getSocialName());
    }

    public static PersonalFinancialRelationsProcuratorEntity from(PersonalFinancialRelationsEntity financialRelations, PersonalProcurator procurators) {
        var procuratorsEntity = new PersonalFinancialRelationsProcuratorEntity();
        procuratorsEntity.setFinancialRelations(financialRelations);
        procuratorsEntity.setType(procurators.getType().name());
        procuratorsEntity.setCpfNumber(procurators.getCpfNumber());
        procuratorsEntity.setCivilName(procurators.getCivilName());
        procuratorsEntity.setSocialName(procurators.getSocialName());
        return procuratorsEntity;
    }
}
