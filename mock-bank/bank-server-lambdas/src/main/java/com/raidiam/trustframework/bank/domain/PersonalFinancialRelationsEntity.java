package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.PersonalFinancialRelationData;
import lombok.*;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "personal_financial_relations")
public class PersonalFinancialRelationsEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Generated(GenerationTime.INSERT)
    @Type(type = "pg-uuid")
    @Column(name = "personal_financial_relations_id", unique = true, nullable = false, updatable = false, columnDefinition = "uuid DEFAULT uuid_generate_v4()")
    private UUID personalFinancialRelationsId;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "products_services_type_additional_info")
    private String productsServicesTypeAdditionalInfo;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "financialRelations")
    private Set<PersonalFinancialRelationsProductsServicesTypeEntity> productServicesType;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "financialRelations")
    private Set<PersonalFinancialRelationsProcuratorEntity> procurators;

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
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "account_holder_id", referencedColumnName = "account_holder_id", insertable = false, updatable = false)
    private Set<AccountEntity> accounts;

    public PersonalFinancialRelationData getDTO() {
        return new PersonalFinancialRelationData()
                .updateDateTime(BankLambdaUtils.dateToOffsetDate(this.getUpdatedAt()))
                .startDate(BankLambdaUtils.dateToOffsetDate(this.getStartDate()))
                .productsServicesTypeAdditionalInfo(this.getProductsServicesTypeAdditionalInfo())
                .productsServicesType(this.getProductServicesType().stream().map(PersonalFinancialRelationsProductsServicesTypeEntity::getDTO).collect(Collectors.toList()))
                .procurators(this.getProcurators().stream().map(PersonalFinancialRelationsProcuratorEntity::getDTO).collect(Collectors.toList()))
                .accounts(this.getAccounts().stream().map(AccountEntity::getPersonalFinancialRelationsAccount).collect(Collectors.toList()));
    }
}
