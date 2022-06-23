package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.*;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "accounts")
public class AccountEntity extends BaseEntity implements HasStatusInterface {

    @Id
    @GeneratedValue
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Generated(GenerationTime.INSERT)
    @Type(type = "pg-uuid")
    @Column(name = "account_id", nullable = false, updatable = false, insertable = false, columnDefinition = "uuid NOT NULL DEFAULT uuid_generate_v4()")
    private UUID accountId;

    @Column(name = "currency")
    private String currency;

    @Column(name = "account_type")
    private String accountType;

    @Column(name = "account_sub_type")
    private String accountSubType;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "company_cnpj")
    private String companyCnpj;

    @Column(name = "compe_code")
    private String compeCode;

    @Column(name = "branch_code")
    private String branchCode;

    @Column(name = "number")
    private String number;

    @Column(name = "check_digit")
    private String checkDigit;

    @Column(name = "available_amount")
    private Double availableAmount;

    @Column(name = "available_amount_currency")
    private String availableAmountCurrency;

    @Column(name = "blocked_amount")
    private Double blockedAmount;

    @Column(name = "blocked_amount_currency")
    private String blockedAmountCurrency;

    @Column(name = "automatically_invested_amount")
    private Double automaticallyInvestedAmount;

    @Column(name = "automatically_invested_amount_currency")
    private String automaticallyInvestedAmountCurrency;

    @Column(name = "overdraft_contracted_limit")
    private Double overdraftContractedLimit;

    @Column(name = "overdraft_contracted_limit_currency")
    private String overdraftContractedLimitCurrency;

    @Column(name = "overdraft_used_limit")
    private Double overdraftUsedLimit;

    @Column(name = "overdraft_used_limit_currency")
    private String overdraftUsedLimitCurrency;

    @Column(name = "unarranged_overdraft_amount")
    private Double unarrangedOverdraftAmount;

    @Column(name = "unarranged_overdraft_amount_currency")
    private String unarrangedOverdraftAmountCurrency;

    @Column(name = "debtor_ispb")
    private String debtorIspb;

    @Column(name = "debtor_issuer")
    private String debtorIssuer;

    @Column(name = "debtor_type")
    private String debtorType;

    @Column(name = "status")
    private String status;

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
    private Set<AccountTransactionsEntity> transactions;

    public AccountData getAccountData() {
        return new AccountData()
                .brandName(this.getBrandName())
                .companyCnpj(this.getCompanyCnpj())
                .type(EnumAccountType.fromValue(this.getAccountType()))
                .compeCode(this.getCompeCode())
                .branchCode(this.getBranchCode())
                .number(this.getNumber())
                .checkDigit(this.getCheckDigit())
                .accountId(this.getAccountId().toString());
    }

    public AccountIdentificationData getAccountIdentificationData() {
        return new AccountIdentificationData()
                .compeCode(this.getCompeCode())
                .branchCode(this.getBranchCode())
                .number(this.getNumber())
                .checkDigit(this.getCheckDigit())
                .type(EnumAccountType.fromValue(this.getAccountType()))
                .subtype(EnumAccountSubType.fromValue(this.getAccountSubType()))
                .currency(this.getCurrency());
    }

    public AccountBalancesData getAccountBalances() {
        return new AccountBalancesData()
                .availableAmount(this.getAvailableAmount())
                .availableAmountCurrency(this.getAvailableAmountCurrency())
                .blockedAmount(this.getBlockedAmount())
                .blockedAmountCurrency(this.getBlockedAmountCurrency())
                .automaticallyInvestedAmount(this.getAutomaticallyInvestedAmount())
                .automaticallyInvestedAmountCurrency(this.getAutomaticallyInvestedAmountCurrency());
    }

    public AccountOverdraftLimitsData getOverDraftLimits() {
        return new AccountOverdraftLimitsData()
                .overdraftContractedLimit(this.overdraftContractedLimit)
                .overdraftContractedLimitCurrency(this.overdraftContractedLimitCurrency)
                .overdraftUsedLimit(this.overdraftUsedLimit)
                .overdraftUsedLimitCurrency(this.overdraftUsedLimitCurrency)
                .unarrangedOverdraftAmount(this.unarrangedOverdraftAmount)
                .unarrangedOverdraftAmountCurrency(this.unarrangedOverdraftAmountCurrency);
    }

    public BusinessAccount getBusinessFinancialRelationsAccount() {
        return new BusinessAccount()
                .compeCode(this.getCompeCode())
                .branchCode(this.getBranchCode())
                .number(this.getNumber())
                .checkDigit(this.getCheckDigit());
    }

    public PersonalAccount getPersonalFinancialRelationsAccount() {
        return new PersonalAccount()
                .compeCode(this.getCompeCode())
                .branchCode(this.getBranchCode())
                .number(this.getNumber())
                .checkDigit(this.getCheckDigit())
                .type(BusinessAccountType.valueOf(this.getAccountType()))
                .subtype(PersonalAccount.SubtypeEnum.valueOf(this.getAccountSubType()));
    }

    public DebtorAccount getDebtorAccount() {
        DebtorAccount debtor = new DebtorAccount();
        debtor.setIspb(this.debtorIspb);
        debtor.setIssuer(this.debtorIssuer);
        debtor.setNumber(this.number);
        debtor.setAccountType(EnumAccountPaymentsType.valueOf(this.debtorType));
        return debtor;
    }

    public AccountEntity setDebtorAccount(DebtorAccount debtorAccount) {
        return Optional.ofNullable(debtorAccount)
                .map(a -> {
                    if (a.getAccountType() != null) {
                        this.debtorIspb = debtorAccount.getIspb();
                        this.debtorIssuer = debtorAccount.getIssuer();
                        this.debtorType = debtorAccount.getAccountType().name();
                        return this;
                    }
                    return null;
                }).orElse(null);
    }
}
