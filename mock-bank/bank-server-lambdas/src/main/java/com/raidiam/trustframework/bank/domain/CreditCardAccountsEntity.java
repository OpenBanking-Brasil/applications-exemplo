package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@Audited
@Table(name = "credit_card_accounts")
public class CreditCardAccountsEntity extends BaseEntity implements HasStatusInterface{
    @Id
    @GeneratedValue
    @Generated(GenerationTime.INSERT)
    @Type(type = "pg-uuid")
    @Column(name = "credit_card_account_id", unique = true, nullable = false, updatable = false, insertable = false, columnDefinition = "uuid NOT NULL DEFAULT uuid_generate_v4()")
    private UUID creditCardAccountId;

    @NotNull
    @Column(name = "brand_name", nullable = false)
    private String brandName;

    @NotNull
    @Column(name = "company_cnpj", nullable = false)
    private String companyCnpj;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "product_type", nullable = false)
    private String productType;

    @NotNull
    @Column(name = "product_additional_info")
    private String productAdditionalInfo;

    @NotNull
    @Column(name = "credit_card_network", nullable = false)
    private String creditCardNetwork;

    @NotNull
    @Column(name = "network_additional_info")
    private String networkAdditionalInfo;

    @NotNull
    @Column(name = "status")
    private String status;

    @NotNull
    @Type(type = "pg-uuid")
    @Column(name = "account_holder_id")
    private UUID accountHolderId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_holder_id", referencedColumnName = "account_holder_id", insertable = false, nullable = false, updatable = false)
    private AccountHolderEntity accountHolder;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "account")
    private Set<CreditCardsAccountPaymentMethodEntity> paymentMethods;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "account")
    private Set<CreditCardAccountsLimitsEntity> limits;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "account")
    private Set<CreditCardAccountsBillsEntity> bills;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "account")
    private Set<CreditCardAccountsTransactionEntity> transactions;

    public CreditCardAccountsData getCreditCardAccountsData() {
        return new CreditCardAccountsData()
                .creditCardAccountId(this.getCreditCardAccountId().toString())
                .brandName(this.getBrandName())
                .companyCnpj(this.getCompanyCnpj())
                .name(this.getName())
                .productType(EnumCreditCardAccountsProductType.valueOf(this.getProductType()))
                .productAdditionalInfo(this.getProductAdditionalInfo())
                .creditCardNetwork(EnumCreditCardAccountNetwork.valueOf(this.getCreditCardNetwork()))
                .networkAdditionalInfo(this.getNetworkAdditionalInfo());
    }

    public CreditCardsAccountsIdentificationData getCreditCardsAccountsIdentificationData() {
        return new CreditCardsAccountsIdentificationData()
                .name(this.getName())
                .productType(EnumCreditCardAccountsProductType.valueOf(this.getProductType()))
                .productAdditionalInfo(this.getProductAdditionalInfo())
                .creditCardNetwork(EnumCreditCardAccountNetwork.valueOf(this.getCreditCardNetwork()))
                .networkAdditionalInfo(this.getNetworkAdditionalInfo())
                .paymentMethod(this.paymentMethods.stream().map(CreditCardsAccountPaymentMethodEntity::getDTO).collect(Collectors.toList()));
    }

    public List<CreditCardAccountsLimitsData> getCreditCardAccountsLimitsData() {
        return limits.stream().map(CreditCardAccountsLimitsEntity::getDTO).collect(Collectors.toList());
    }

    public List<CreditCardAccountsBillsData> getCreditCardAccountsBillsData() {
        return this.bills.stream().map(CreditCardAccountsBillsEntity::getDTO).collect(Collectors.toList());
    }

    public List<CreditCardAccountsTransaction> getCreditCardAccountsTransaction() {
        return this.transactions.stream().map(CreditCardAccountsTransactionEntity::getDTO).collect(Collectors.toList());
    }
}
