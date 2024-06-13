package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "accounts")
public class AccountEntity extends BaseEntity implements HasStatusInterface {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "account")
    private Set<AccountTransactionsEntity> transactions = new HashSet<>();

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

    public AccountBalancesDataV2 getAccountBalancesV2() {
        return new AccountBalancesDataV2()
                .availableAmount(new AccountBalancesDataAvailableAmountV2()
                        .amount(BankLambdaUtils.formatAmountV2(this.availableAmount))
                        .currency(this.availableAmountCurrency))
                .blockedAmount(new AccountBalancesDataBlockedAmountV2()
                        .amount(BankLambdaUtils.formatAmountV2(this.blockedAmount))
                        .currency(this.blockedAmountCurrency))
                .automaticallyInvestedAmount(new AccountBalancesDataAutomaticallyInvestedAmountV2()
                        .amount(BankLambdaUtils.formatAmountV2(this.automaticallyInvestedAmount))
                        .currency(this.automaticallyInvestedAmountCurrency))
                .updateDateTime(this.getUpdatedAt().toString());
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

    public AccountOverdraftLimitsDataV2 getOverDraftLimitsV2() {
        return new AccountOverdraftLimitsDataV2()
                .overdraftContractedLimit(new AccountOverdraftLimitsDataOverdraftContractedLimitV2()
                        .amount(BankLambdaUtils.formatAmountV2(this.overdraftContractedLimit))
                        .currency(this.overdraftContractedLimitCurrency))
                .overdraftUsedLimit(new AccountOverdraftLimitsDataOverdraftUsedLimitV2()
                        .amount(BankLambdaUtils.formatAmountV2(this.overdraftUsedLimit))
                        .currency(this.overdraftUsedLimitCurrency))
                .unarrangedOverdraftAmount(new AccountOverdraftLimitsDataUnarrangedOverdraftAmountV2()
                        .amount(BankLambdaUtils.formatAmountV2(this.unarrangedOverdraftAmount))
                        .currency(this.unarrangedOverdraftAmountCurrency));
    }

    public BusinessAccount getBusinessFinancialRelationsAccount() {
        return new BusinessAccount()
                .compeCode(this.getCompeCode())
                .branchCode(this.getBranchCode())
                .number(this.getNumber())
                .checkDigit(this.getCheckDigit())
                .type(BusinessAccountType.valueOf(this.getAccountType()));
    }

    public BusinessAccountV2 getBusinessFinancialRelationsAccountV2() {
        return new BusinessAccountV2()
                .compeCode(this.getCompeCode())
                .branchCode(this.getBranchCode())
                .number(this.getNumber())
                .checkDigit(this.getCheckDigit())
                .type(EnumAccountTypeCustomersV2.fromValue(this.getAccountType()));
    }

    public PersonalAccount getPersonalFinancialRelationsAccount() {
        return new PersonalAccount()
                .compeCode(this.getCompeCode())
                .branchCode(this.getBranchCode())
                .number(this.getNumber())
                .checkDigit(this.getCheckDigit())
                .type(BusinessAccountType.fromValue(this.getAccountType()))
                .subtype(PersonalAccount.SubtypeEnum.fromValue(this.getAccountSubType()));
    }

    public PersonalAccountV2 getPersonalFinancialRelationsAccountV2() {
        return new PersonalAccountV2()
                .compeCode(this.getCompeCode())
                .branchCode(this.getBranchCode())
                .number(this.getNumber())
                .checkDigit(this.getCheckDigit())
                .type(EnumAccountTypeCustomersV2.fromValue(this.getAccountType()))
                .subtype(PersonalAccountV2.SubtypeEnum.fromValue(this.getAccountSubType()));
    }

    public RecurringDebtorAccount getRecurringDebtorAccount() {
        return new RecurringDebtorAccount()
                .accountType(EnumAccountTypeConsents.fromValue(debtorType))
                .issuer(debtorIssuer)
                .ispb(debtorIspb)
                .ibgeTownCode("5300108")
                .number(number);
    }

    public DebtorAccount getDebtorAccount() {
        DebtorAccount debtor = new DebtorAccount();
        debtor.setIspb(this.debtorIspb);
        debtor.setIssuer(this.debtorIssuer);
        debtor.setNumber(this.number);
        debtor.setAccountType(EnumAccountPaymentsType.fromValue(this.debtorType));
        return debtor;
    }

    public AccountEntity setDebtorAccount(DebtorAccount debtorAccount) {
        return Optional.ofNullable(debtorAccount)
                .map(a -> {
                    if (a.getAccountType() != null) {
                        this.debtorIspb = debtorAccount.getIspb();
                        this.debtorIssuer = debtorAccount.getIssuer();
                        this.debtorType = debtorAccount.getAccountType().toString();
                        return this;
                    }
                    return null;
                }).orElse(null);
    }

    public static AccountEntity from(CreateAccountData account, UUID accountHolderId) {
        var accountEntity = new AccountEntity();
        accountEntity.setAccountHolderId(accountHolderId);
        accountEntity.setAccountType(account.getAccountType().toString());
        accountEntity.setAccountSubType(account.getAccountSubType().toString());
        accountEntity.setCurrency(account.getCurrency());
        accountEntity.setStatus(account.getStatus());
        accountEntity.setBrandName(account.getBrandName());
        accountEntity.setBranchCode(account.getBranchCode());
        accountEntity.setCompanyCnpj(account.getCompanyCnpj());
        accountEntity.setCompeCode(account.getCompeCode());
        accountEntity.setNumber(account.getNumber());
        accountEntity.setCheckDigit(account.getCheckDigit());
        accountEntity.setAvailableAmount(account.getAvailableAmount());
        accountEntity.setAvailableAmountCurrency(account.getAvailableAmountCurrency());
        accountEntity.setBlockedAmount(account.getBlockedAmount());
        accountEntity.setBlockedAmountCurrency(account.getBlockedAmountCurrency());
        accountEntity.setAutomaticallyInvestedAmount(account.getAutomaticallyInvestedAmount());
        accountEntity.setAutomaticallyInvestedAmountCurrency(account.getAutomaticallyInvestedAmountCurrency());
        accountEntity.setOverdraftContractedLimit(account.getOverdraftContractedLimit());
        accountEntity.setOverdraftContractedLimitCurrency(account.getOverdraftContractedLimitCurrency());
        accountEntity.setOverdraftUsedLimit(account.getOverdraftUsedLimit());
        accountEntity.setOverdraftUsedLimitCurrency(account.getOverdraftUsedLimitCurrency());
        accountEntity.setUnarrangedOverdraftAmount(account.getUnarrangedOverdraftAmount());
        accountEntity.setUnarrangedOverdraftAmountCurrency(account.getUnarrangedOverdraftAmountCurrency());

        return accountEntity;
    }

    public AccountEntity update(EditedAccountData account) {
        this.accountType = account.getAccountType().toString();
        this.accountSubType = account.getAccountSubType().toString();
        this.currency = account.getCurrency();
        this.status = account.getStatus();
        this.brandName = account.getBrandName();
        this.branchCode = account.getBranchCode();
        this.companyCnpj = account.getCompanyCnpj();
        this.compeCode = account.getCompeCode();
        this.number = account.getNumber();
        this.checkDigit = account.getCheckDigit();
        this.availableAmount = account.getAvailableAmount();
        this.availableAmountCurrency = account.getAvailableAmountCurrency();
        this.blockedAmount = account.getBlockedAmount();
        this.blockedAmountCurrency = account.getBlockedAmountCurrency();
        this.automaticallyInvestedAmount = account.getAutomaticallyInvestedAmount();
        this.automaticallyInvestedAmountCurrency = account.getAutomaticallyInvestedAmountCurrency();
        this.overdraftContractedLimit = account.getOverdraftContractedLimit();
        this.overdraftContractedLimitCurrency = account.getOverdraftContractedLimitCurrency();
        this.overdraftUsedLimit = account.getOverdraftUsedLimit();
        this.overdraftUsedLimitCurrency = account.getOverdraftUsedLimitCurrency();
        this.unarrangedOverdraftAmount = account.getUnarrangedOverdraftAmount();
        this.unarrangedOverdraftAmountCurrency = account.getUnarrangedOverdraftAmountCurrency();
        return this;
    }

    public ResponseAccount getAdminAccountDto() {
        return new ResponseAccount().data(new ResponseAccountData()
                .accountId(this.accountId.toString())
                .accountType(EnumAccountType.fromValue(this.accountType))
                .accountSubType(EnumAccountSubType.fromValue(this.accountSubType))
                .currency(this.currency)
                .status(this.status)
                .brandName(this.brandName)
                .branchCode(this.branchCode)
                .companyCnpj(this.companyCnpj)
                .compeCode(this.compeCode)
                .number(this.number)
                .checkDigit(this.checkDigit)
                .availableAmount(this.availableAmount)
                .availableAmountCurrency(this.availableAmountCurrency)
                .blockedAmount(this.blockedAmount)
                .blockedAmountCurrency(this.blockedAmountCurrency)
                .automaticallyInvestedAmount(this.automaticallyInvestedAmount)
                .automaticallyInvestedAmountCurrency(this.automaticallyInvestedAmountCurrency)
                .overdraftContractedLimit(this.overdraftContractedLimit)
                .overdraftContractedLimitCurrency(this.overdraftContractedLimitCurrency)
                .overdraftUsedLimit(this.overdraftUsedLimit)
                .overdraftUsedLimitCurrency(this.overdraftUsedLimitCurrency)
                .unarrangedOverdraftAmount(this.unarrangedOverdraftAmount)
                .unarrangedOverdraftAmountCurrency(this.unarrangedOverdraftAmountCurrency));
    }
}
