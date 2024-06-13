package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.BusinessProcurator;
import com.raidiam.trustframework.mockbank.models.generated.BusinessProcuratorV2;
import lombok.*;
import org.hibernate.envers.Audited;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "business_financial_relations_procurators")
public class BusinessFinancialRelationsProcuratorEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "type")
    private String type;

    @Column(name = "cnpj_cpf_number")
    private String cnpjCpfNumber;

    @Column(name = "civil_name")
    private String civilName;

    @Column(name = "social_name")
    private String socialName;

    @NotNull
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_financial_relations_id", referencedColumnName = "business_financial_relations_id", nullable = false, updatable = false)
    private BusinessFinancialRelationsEntity financialRelations;

    public BusinessProcurator getDTO() {
        return new BusinessProcurator()
                .type(BusinessProcurator.TypeEnum.fromValue(this.getType()))
                .cnpjCpfNumber(this.getCnpjCpfNumber())
                .civilName(this.getCivilName())
                .socialName(this.getSocialName());
    }

    public BusinessProcuratorV2 getDtoV2() {
        return new BusinessProcuratorV2()
                .type(BusinessProcuratorV2.TypeEnum.fromValue(this.getType()))
                .cnpjCpfNumber(this.getCnpjCpfNumber())
                .civilName(this.getCivilName())
                .socialName(this.getSocialName());
    }

    public static BusinessFinancialRelationsProcuratorEntity from(BusinessFinancialRelationsEntity financialRelations, BusinessProcurator procurator) {
        var procuratorEntity = new BusinessFinancialRelationsProcuratorEntity();
        procuratorEntity.setFinancialRelations(financialRelations);
        procuratorEntity.setType(procurator.getType().name());
        procuratorEntity.setCnpjCpfNumber(procurator.getCnpjCpfNumber());
        procuratorEntity.setCivilName(procurator.getCivilName());
        procuratorEntity.setSocialName(procurator.getSocialName());
        return procuratorEntity;
    }
}
