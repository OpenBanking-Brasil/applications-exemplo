package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.BusinessProcurator;
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
@Table(name = "business_financial_relations_procurators")
public class BusinessFinancialRelationsProcuratorEntity extends BaseEntity {

    @Id
    @GeneratedValue
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

    @Column(name = "business_financial_relations_id")
    @Type(type = "pg-uuid")
    private UUID businessFinancialRelationsId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_financial_relations_id", referencedColumnName = "business_financial_relations_id", insertable = false, nullable = false, updatable = false)
    private BusinessFinancialRelationsEntity financialRelations;

    public BusinessProcurator getDTO() {
        return new BusinessProcurator()
                .type(BusinessProcurator.TypeEnum.fromValue(this.getType()))
                .cnpjCpfNumber(this.getCnpjCpfNumber())
                .civilName(this.getCivilName())
                .socialName(this.getSocialName());
    }
}
