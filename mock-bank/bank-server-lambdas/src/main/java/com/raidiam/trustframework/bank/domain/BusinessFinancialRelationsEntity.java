package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.BusinessFinancialRelationData;
import com.raidiam.trustframework.mockbank.models.generated.BusinessFinancialRelationDataV2;
import com.raidiam.trustframework.mockbank.models.generated.BusinessFinancialRelations;
import com.raidiam.trustframework.mockbank.models.generated.BusinessFinancialRelationsData;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "business_financial_relations")
public class BusinessFinancialRelationsEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Type(type = "pg-uuid")
    @Column(name = "business_financial_relations_id", nullable = false, unique = true, updatable = false, insertable = false, columnDefinition = "uuid DEFAULT uuid_generate_v4()")
    private UUID businessFinancialRelationsId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "financialRelations")
    private Set<BusinessFinancialRelationsProductsServicesTypeEntity> productServicesType = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "financialRelations")
    private Set<BusinessFinancialRelationsProcuratorEntity> procurators = new HashSet<>();

    @Column(name = "account_holder_id")
    @Type(type = "pg-uuid")
    private UUID accountHolderId;

    @OneToOne(fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "account_holder_id", referencedColumnName = "account_holder_id", insertable = false, updatable = false)
    private AccountHolderEntity accountHolder;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "account_holder_id", referencedColumnName = "account_holder_id", insertable = false, updatable = false)
    private Set<AccountEntity> accounts;

    public BusinessFinancialRelationData getDTO() {
        return new BusinessFinancialRelationData()
                .updateDateTime(BankLambdaUtils.dateToOffsetDate(this.getUpdatedAt()))
                .startDate(BankLambdaUtils.localDateToOffsetDate(this.getStartDate()))
                .productsServicesType(this.getProductServicesType().stream().map(BusinessFinancialRelationsProductsServicesTypeEntity::getDTO).collect(Collectors.toList()))
                .procurators(this.getProcurators().stream().map(BusinessFinancialRelationsProcuratorEntity::getDTO).collect(Collectors.toList()))
                .accounts(this.getAccounts().stream().map(AccountEntity::getBusinessFinancialRelationsAccount).collect(Collectors.toList()));
    }

    public BusinessFinancialRelationDataV2 getDtoV2() {
        return new BusinessFinancialRelationDataV2()
                .updateDateTime(BankLambdaUtils.dateToOffsetDate(this.getUpdatedAt()))
                .startDate(BankLambdaUtils.localDateToOffsetDate(this.getStartDate()))
                .productsServicesType(this.getProductServicesType().stream().map(BusinessFinancialRelationsProductsServicesTypeEntity::getDTOV2).collect(Collectors.toList()))
                .procurators(this.getProcurators().stream().map(BusinessFinancialRelationsProcuratorEntity::getDtoV2).collect(Collectors.toList()))
                .accounts(this.getAccounts().stream().map(AccountEntity::getBusinessFinancialRelationsAccountV2).collect(Collectors.toList()));
    }

    public static BusinessFinancialRelationsEntity from(BusinessFinancialRelationsData financialRelationsDto, UUID accountHolderId) {
        var financialRelation = new BusinessFinancialRelationsEntity();
        financialRelation.setAccountHolderId(accountHolderId);
        financialRelation.setStartDate(financialRelationsDto.getStartDate().toLocalDate());

        var servicesTypeList = financialRelationsDto.getProductsServicesType().stream()
                .map(t -> BusinessFinancialRelationsProductsServicesTypeEntity.from(financialRelation, t))
                .collect(Collectors.toSet());
        financialRelation.setProductServicesType(servicesTypeList);

        var procuratorsList = financialRelationsDto.getProcurators().stream()
                .map(p -> BusinessFinancialRelationsProcuratorEntity.from(financialRelation, p))
                .collect(Collectors.toSet());
        financialRelation.setProcurators(procuratorsList);

        return financialRelation;
    }

    public BusinessFinancialRelations getAdminBusinessFinancialRelations() {
        return new BusinessFinancialRelations().data(new BusinessFinancialRelationsData()
                .accountHolderId(getAccountHolderId())
                .startDate(BankLambdaUtils.localDateToOffsetDate(this.startDate))
                .productsServicesType(this.productServicesType != null ? this.productServicesType.stream().map(BusinessFinancialRelationsProductsServicesTypeEntity::getDTO).collect(Collectors.toList()) : null)
                .procurators(this.procurators != null ? this.procurators.stream().map(BusinessFinancialRelationsProcuratorEntity::getDTO).collect(Collectors.toList()) : null));
    }
}
