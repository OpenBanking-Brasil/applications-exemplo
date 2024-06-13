package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.*;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "credit_card_accounts")
public class CreditCardAccountsEntity extends BaseEntity implements HasStatusInterface {
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
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "account")
    private Set<CreditCardsAccountPaymentMethodEntity> paymentMethods = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "account")
    private Set<CreditCardAccountsLimitsEntity> limits = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "account")
    private Set<CreditCardAccountsBillsEntity> bills = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "account")
    private Set<CreditCardAccountsTransactionEntity> transactions = new HashSet<>();

    public CreditCardAccountsData getCreditCardAccountsData() {
        return new CreditCardAccountsData()
                .creditCardAccountId(this.creditCardAccountId.toString())
                .brandName(this.brandName)
                .companyCnpj(this.companyCnpj)
                .name(this.name)
                .productType(EnumCreditCardAccountsProductType.valueOf(this.productType))
                .productAdditionalInfo(this.productAdditionalInfo)
                .creditCardNetwork(EnumCreditCardAccountNetwork.valueOf(this.creditCardNetwork))
                .networkAdditionalInfo(this.networkAdditionalInfo);
    }

    public CreditCardsAccountsIdentificationData getCreditCardsAccountsIdentificationData() {
        return new CreditCardsAccountsIdentificationData()
                .name(this.name)
                .productType(EnumCreditCardAccountsProductType.valueOf(this.productType))
                .productAdditionalInfo(this.productAdditionalInfo)
                .creditCardNetwork(EnumCreditCardAccountNetwork.valueOf(this.creditCardNetwork))
                .networkAdditionalInfo(this.networkAdditionalInfo)
                .paymentMethod(this.paymentMethods.stream().map(CreditCardsAccountPaymentMethodEntity::getDTO).collect(Collectors.toList()));
    }

    public List<CreditCardAccountsLimitsData> getCreditCardAccountsLimitsData() {
        return limits.stream().map(CreditCardAccountsLimitsEntity::getDTO).collect(Collectors.toList());
    }

    public List<CreditCardAccountsLimitsDataV2> getCreditCardAccountsLimitsDataV2() {
        return limits.stream().map(CreditCardAccountsLimitsEntity::getDtoV2).collect(Collectors.toList());
    }

    public List<CreditCardAccountsBillsData> getCreditCardAccountsBillsData() {
        return this.bills.stream().map(CreditCardAccountsBillsEntity::getDTO).collect(Collectors.toList());
    }

    public List<CreditCardAccountsTransaction> getCreditCardAccountsTransaction() {
        return this.transactions.stream().map(CreditCardAccountsTransactionEntity::getDTO).collect(Collectors.toList());
    }

    public static CreditCardAccountsEntity from(CreateCreditCardAccountData creditCard, UUID accountHolderId) {
        var account =  new CreditCardAccountsEntity();
        account.setAccountHolderId(accountHolderId);
        account.setBrandName(creditCard.getBrandName());
        account.setCompanyCnpj(creditCard.getCompanyCnpj());
        account.setName(creditCard.getName());
        account.setProductType(creditCard.getProductType().name());
        account.setProductAdditionalInfo(creditCard.getProductAdditionalInfo());
        account.setCreditCardNetwork(creditCard.getCreditCardNetwork().name());
        account.setNetworkAdditionalInfo(creditCard.getNetworkAdditionalInfo());
        account.setStatus(creditCard.getStatus());

        var newPaymentMethods = creditCard.getPaymentMethod().stream()
                .map(p -> CreditCardsAccountPaymentMethodEntity.from(account, p))
                .collect(Collectors.toSet());
        account.setPaymentMethods(newPaymentMethods);

        return account;
    }

    public CreditCardAccountsEntity update(EditedCreditCardAccountData creditCard) {
        this.brandName = creditCard.getBrandName();
        this.companyCnpj = creditCard.getCompanyCnpj();
        this.name = creditCard.getName();
        this.productType = creditCard.getProductType().name();
        this.productAdditionalInfo = creditCard.getProductAdditionalInfo();
        this.creditCardNetwork = creditCard.getCreditCardNetwork().name();
        this.networkAdditionalInfo = creditCard.getNetworkAdditionalInfo();
        this.status = creditCard.getStatus();
        this.paymentMethods.clear();

        var updatePaymentMethods = creditCard.getPaymentMethod().stream()
                .map(p -> CreditCardsAccountPaymentMethodEntity.from(this, p))
                .collect(Collectors.toSet());
        this.paymentMethods.addAll(updatePaymentMethods);

        return this;
    }

    public ResponseCreditCardAccount getAdminCreditCardAccountDto() {
        ResponseCreditCardAccountData data = new ResponseCreditCardAccountData()
                .creditCardAccountId(this.creditCardAccountId)
                .brandName(this.brandName)
                .companyCnpj(this.companyCnpj)
                .name(this.name)
                .productType(EnumCreditCardAccountsProductType.fromValue(this.productType))
                .productAdditionalInfo(this.productAdditionalInfo)
                .creditCardNetwork(EnumCreditCardAccountNetwork.fromValue(this.creditCardNetwork))
                .networkAdditionalInfo(this.networkAdditionalInfo)
                .status(this.status)
                .paymentMethod(this.paymentMethods.stream().map(CreditCardsAccountPaymentMethodEntity::getDTO).collect(Collectors.toList()));
    return new ResponseCreditCardAccount().data(data);
    }
}
