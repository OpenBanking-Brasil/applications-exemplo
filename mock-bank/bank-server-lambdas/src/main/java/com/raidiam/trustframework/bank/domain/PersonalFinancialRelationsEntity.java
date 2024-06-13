package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.PersonalFinancialRelationData;
import com.raidiam.trustframework.mockbank.models.generated.PersonalFinancialRelationDataV2;
import com.raidiam.trustframework.mockbank.models.generated.PersonalFinancialRelations;
import com.raidiam.trustframework.mockbank.models.generated.PersonalFinancialRelationsData;
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
@Table(name = "personal_financial_relations")
public class PersonalFinancialRelationsEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Type(type = "pg-uuid")
    @Column(name = "personal_financial_relations_id", unique = true, nullable = false, updatable = false, columnDefinition = "uuid DEFAULT uuid_generate_v4()")
    private UUID personalFinancialRelationsId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "products_services_type_additional_info")
    private String productsServicesTypeAdditionalInfo;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "financialRelations")
    private Set<PersonalFinancialRelationsProductsServicesTypeEntity> productServicesType = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "financialRelations")
    private Set<PersonalFinancialRelationsProcuratorEntity> procurators = new HashSet<>();

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

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "financialRelations")
    private Set<PersonalFinancialRelationsPortabilitiesReceivedEntity> portabilitiesReceived = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "financialRelations")
    private Set<PersonalFinancialRelationsPaychecksBankLinkEntity> paychecksBankLink = new HashSet<>();

    public PersonalFinancialRelationData getDTO() {
        return new PersonalFinancialRelationData()
                .updateDateTime(BankLambdaUtils.dateToOffsetDate(this.getUpdatedAt()))
                .startDate(BankLambdaUtils.localDateToOffsetDate(this.getStartDate()))
                .productsServicesTypeAdditionalInfo(this.getProductsServicesTypeAdditionalInfo())
                .productsServicesType(this.getProductServicesType().stream().map(PersonalFinancialRelationsProductsServicesTypeEntity::getDTO).collect(Collectors.toList()))
                .procurators(this.getProcurators().stream().map(PersonalFinancialRelationsProcuratorEntity::getDTO).collect(Collectors.toList()))
                .accounts(this.getAccounts().stream().map(AccountEntity::getPersonalFinancialRelationsAccount).collect(Collectors.toList()));
    }

    public PersonalFinancialRelationDataV2 getDtoV2() {
        return new PersonalFinancialRelationDataV2()
                .updateDateTime(BankLambdaUtils.dateToOffsetDate(this.getUpdatedAt()))
                .startDate(BankLambdaUtils.localDateToOffsetDate(this.startDate))
                .productsServicesTypeAdditionalInfo(this.productsServicesTypeAdditionalInfo)
                .productsServicesType(this.productServicesType.stream().map(PersonalFinancialRelationsProductsServicesTypeEntity::getDTOV2).collect(Collectors.toList()))
                .procurators(this.procurators.stream().map(PersonalFinancialRelationsProcuratorEntity::getDtoV2).collect(Collectors.toList()))
                .accounts(this.accounts.stream().map(AccountEntity::getPersonalFinancialRelationsAccountV2).collect(Collectors.toList()))
                .portabilitiesReceived(this.portabilitiesReceived.stream().map(PersonalFinancialRelationsPortabilitiesReceivedEntity::getDTO).collect(Collectors.toList()))
                .paychecksBankLink(this.paychecksBankLink.stream().map(PersonalFinancialRelationsPaychecksBankLinkEntity::getDTO).collect(Collectors.toList()));
    }

    public static PersonalFinancialRelationsEntity from(PersonalFinancialRelationsData personalFinancialRelations, UUID accountHolderId) {
        var financialRelations = new PersonalFinancialRelationsEntity();
        financialRelations.setAccountHolderId(accountHolderId);
        financialRelations.setStartDate(personalFinancialRelations.getStartDate().toLocalDate());
        financialRelations.setProductsServicesTypeAdditionalInfo(personalFinancialRelations.getProductsServicesTypeAdditionalInfo());

        var productServicesTypeList = personalFinancialRelations.getProductsServicesType().stream()
                .map(p -> PersonalFinancialRelationsProductsServicesTypeEntity.from(financialRelations, p))
                .collect(Collectors.toSet());
        financialRelations.setProductServicesType(productServicesTypeList);

        var procuratorsList = personalFinancialRelations.getProcurators().stream()
                .map(p -> PersonalFinancialRelationsProcuratorEntity.from(financialRelations, p))
                .collect(Collectors.toSet());
        financialRelations.setProcurators(procuratorsList);

        return financialRelations;
    }

    public PersonalFinancialRelations getAdminPersonalFinancialRelations() {
        return new PersonalFinancialRelations().data(new PersonalFinancialRelationsData()
                .accountHolderId(this.accountHolderId)
                .startDate(BankLambdaUtils.localDateToOffsetDate(this.startDate))
                .productsServicesTypeAdditionalInfo(this.productsServicesTypeAdditionalInfo)
                .productsServicesType(this.productServicesType != null ? this.productServicesType.stream().map(PersonalFinancialRelationsProductsServicesTypeEntity::getDTO).collect(Collectors.toList()) : null)
                .procurators(this.procurators != null ? this.procurators.stream().map(PersonalFinancialRelationsProcuratorEntity::getDTO).collect(Collectors.toList()) : null));
    }

    public PersonalFinancialRelationsEntity update(PersonalFinancialRelationsData personalFinancialRelations) {
        this.startDate = personalFinancialRelations.getStartDate().toLocalDate();
        this.productsServicesTypeAdditionalInfo = personalFinancialRelations.getProductsServicesTypeAdditionalInfo();

        var productServicesTypeList = personalFinancialRelations.getProductsServicesType().stream()
                .map(p -> PersonalFinancialRelationsProductsServicesTypeEntity.from(this, p))
                .collect(Collectors.toSet());
        this.productServicesType.clear();
        this.productServicesType.addAll(productServicesTypeList);

        var procuratorsList = personalFinancialRelations.getProcurators().stream()
                .map(p -> PersonalFinancialRelationsProcuratorEntity.from(this, p))
                .collect(Collectors.toSet());
        this.procurators.clear();
        this.procurators.addAll(procuratorsList);

        return this;
    }
}
