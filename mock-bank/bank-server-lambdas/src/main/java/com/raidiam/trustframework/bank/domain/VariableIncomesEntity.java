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
@Table(name = "variable_incomes")
public class VariableIncomesEntity extends BaseEntity implements HasStatusInterface {
    @Id
    @GeneratedValue
    @Column(name = "investment_id", unique = true, nullable = false, updatable = false, insertable = false)
    private UUID investmentId;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "company_cnpj")
    private String companyCnpj;

    @Column(name = "ticker")
    private String ticker;

    @Column(name = "isin_code")
    private String isinCode;

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
    private Set<VariableIncomesTransactionsEntity> transactionsEntities = new HashSet<>();

    public ResponseVariableIncomesProductListData getResponseVariableIncomesProductListData() {
        return new ResponseVariableIncomesProductListData()
                .investmentId(investmentId.toString())
                .brandName(brandName)
                .companyCnpj(companyCnpj);
    }

    public ResponseVariableIncomesProductIdentificationData getResponseVariableIncomesProductIdentificationData() {
        return new ResponseVariableIncomesProductIdentificationData()
                .issuerInstitutionCnpjNumber(companyCnpj)
                .isinCode(isinCode)
                .ticker(ticker);
    }

}
