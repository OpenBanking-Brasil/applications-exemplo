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
@Table(name = "business_financial_relations_products_services_type")
public class BusinessFinancialRelationsProductsServicesTypeEntity extends BaseEntity{

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
    @JoinColumn(name = "business_financial_relations_id", referencedColumnName = "business_financial_relations_id", nullable = false, updatable = false)
    private BusinessFinancialRelationsEntity financialRelations;

    public EnumProductServiceType getDTO() {
        return EnumProductServiceType.fromValue(this.getType());
    }

    public EnumProductServiceTypeV2 getDTOV2() {
        return EnumProductServiceTypeV2.fromValue(this.getType());
    }

    public static BusinessFinancialRelationsProductsServicesTypeEntity from(BusinessFinancialRelationsEntity financialRelations, EnumProductServiceType serviceType) {
        var serviceTypeEntity = new BusinessFinancialRelationsProductsServicesTypeEntity();
        serviceTypeEntity.setType(serviceType.name());
        serviceTypeEntity.setFinancialRelations(financialRelations);
        return serviceTypeEntity;
    }
}
