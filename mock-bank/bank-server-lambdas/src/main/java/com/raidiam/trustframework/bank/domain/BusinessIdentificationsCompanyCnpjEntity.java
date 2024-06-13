package com.raidiam.trustframework.bank.domain;

import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "business_identifications_company_cnpj")
public class BusinessIdentificationsCompanyCnpjEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "companyCnpj")
    private String companyCnpj;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "business_identifications_id", referencedColumnName = "business_identifications_id", nullable = false, updatable = false)
    private BusinessIdentificationsEntity businessIdentifications;

    public static BusinessIdentificationsCompanyCnpjEntity from(BusinessIdentificationsEntity business, String cnpj) {
        var cnpjEntity = new BusinessIdentificationsCompanyCnpjEntity();
        cnpjEntity.setCompanyCnpj(cnpj);
        cnpjEntity.setBusinessIdentifications(business);
        return cnpjEntity;
    }

    public String getDto() {
        return this.companyCnpj;
    }
}
