package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.EnumProductServiceType;
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
@Table(name = "personal_financial_relations_products_services_type")
public class PersonalFinancialRelationsProductsServicesTypeEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "type")
    private String type;

    @Column(name = "personal_financial_relations_id")
    @Type(type = "pg-uuid")
    private UUID personalFinancialRelationsId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_financial_relations_id", referencedColumnName = "personal_financial_relations_id", insertable = false, nullable = false, updatable = false)
    private PersonalFinancialRelationsEntity financialRelations;

    public EnumProductServiceType getDTO() {
        return EnumProductServiceType.fromValue(this.getType());
    }
}
