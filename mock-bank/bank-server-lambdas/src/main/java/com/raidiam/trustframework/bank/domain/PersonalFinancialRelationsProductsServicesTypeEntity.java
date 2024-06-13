package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.EnumProductServiceType;
import com.raidiam.trustframework.mockbank.models.generated.EnumProductServiceTypeV2;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "personal_financial_relations_products_services_type")
public class PersonalFinancialRelationsProductsServicesTypeEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "type")
    private String type;

    @NotNull
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_financial_relations_id", referencedColumnName = "personal_financial_relations_id", nullable = false, updatable = false)
    private PersonalFinancialRelationsEntity financialRelations;

    public EnumProductServiceType getDTO() {
        return EnumProductServiceType.fromValue(this.getType());
    }

    public EnumProductServiceTypeV2 getDTOV2() {
        return EnumProductServiceTypeV2.fromValue(this.getType());
    }

    public static PersonalFinancialRelationsProductsServicesTypeEntity from(PersonalFinancialRelationsEntity financialRelations, EnumProductServiceType serviceType) {
        var productsServicesTypeEntity = new PersonalFinancialRelationsProductsServicesTypeEntity();
        productsServicesTypeEntity.setFinancialRelations(financialRelations);
        productsServicesTypeEntity.setType(serviceType.name());
        return productsServicesTypeEntity;
    }
}
