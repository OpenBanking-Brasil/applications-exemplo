package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "funds")
public class FundsEntity extends BaseEntity implements HasStatusInterface {
    @Id
    @GeneratedValue
    @Column(name = "investment_id", unique = true, nullable = false, updatable = false, insertable = false)
    private UUID investmentId;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "company_cnpj")
    private String companyCnpj;

    @Column(name = "anbima_category")
    private String anbimaCategory;

    @Column(name = "anbima_class")
    private String anbimaClass;

    @Column(name = "anbima_subclass")
    private String anbimaSubclass;

    @Column(name = "isin_code")
    private String isinCode;

    @Column(name = "name")
    private String name;

    @Column(name = "account_holder_id")
    private UUID accountHolderId;

    @Column(name = "status")
    private String status;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_holder_id", referencedColumnName = "account_holder_id", insertable = false, nullable = false, updatable = false)
    private AccountHolderEntity accountHolder;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "investment")
    private Set<FundsTransactionsEntity> transactionsEntities = new HashSet<>();

    public ResponseFundsProductListData getResponseFundsProductListData() {
        return new ResponseFundsProductListData()
                .investmentId(investmentId.toString())
                .brandName(brandName)
                .companyCnpj(companyCnpj)
                .anbimaCategory(EnumFundsAnbimaCategory.fromValue(anbimaCategory))
                .anbimaClass(anbimaClass)
                .anbimaSubclass(anbimaSubclass);
    }

    public ResponseFundsProductIdentificationData getResponseFundsProductIdentificationData() {
        return new ResponseFundsProductIdentificationData()
                .cnpjNumber(companyCnpj)
                .isinCode(isinCode)
                .name(name)
                .anbimaCategory(EnumFundsAnbimaCategory.fromValue(anbimaCategory))
                .anbimaClass(anbimaClass)
                .anbimaSubclass(anbimaSubclass);

    }

}
