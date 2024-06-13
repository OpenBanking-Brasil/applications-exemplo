package com.raidiam.trustframework.bank.services;

import com.amazonaws.transform.MapEntry;
import com.raidiam.trustframework.bank.domain.AccountEntity;
import com.raidiam.trustframework.bank.domain.AccountTransactionsEntity;
import com.raidiam.trustframework.bank.repository.AccountRepository;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.context.annotation.Value;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
@Transactional
public class AccountsService extends BaseBankService {

    @Value("${mockbank.mockbankUrl}")
    protected String appBaseUrl;

    @Value("${mockbank.max-page-size}")
    protected int maxPageSize;

    private ResourcesService resourcesService;

    private static final Logger LOG = LoggerFactory.getLogger(AccountsService.class);

    AccountsService(AccountRepository accountRepository, ResourcesService resourcesService) {
        this.accountRepository = accountRepository;
        this.resourcesService = resourcesService;
    }

    public ResponseAccountList getAccounts(Pageable pageable, String consentId, String accountType) {
        LOG.info("Getting accounts response for consent id {}", consentId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, EnumConsentPermissions.ACCOUNTS_READ);

        var consentAccount = accountType != null ?
                consentAccountRepository.findByConsentConsentIdAndAccountAccountTypeOrderByCreatedAtAsc(consentId, accountType, pageable)
                : consentAccountRepository.findByConsentConsentIdOrderByCreatedAtAsc(consentId, pageable);
        BankLambdaUtils.checkConsentOwnerIsAccountOwner(consentAccount, consentEntity);

        var response = new ResponseAccountList().data(consentAccount.getContent()
                .stream()
                .map(consentAccountEntity -> {
                    resourcesService.checkStatusAvailable(consentAccountEntity.getAccount(), consentEntity);
                    return consentAccountEntity.getAccount();
                })
                .map(AccountEntity::getAccountData)
                .collect(Collectors.toList()));

        response.setMeta(BankLambdaUtils.getMeta(consentAccount));

        return response;
    }

    public ResponseAccountIdentification getAccount(String consentId, String acctId) {
        LOG.info("Getting account response for consent id {} and account id {}", consentId, acctId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var accountEntity = BankLambdaUtils.getAccount(acctId, accountRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, EnumConsentPermissions.ACCOUNTS_READ);
        BankLambdaUtils.checkConsentCoversAccount(consentEntity, accountEntity);
        BankLambdaUtils.checkConsentOwnerIsAccountOwner(consentEntity, accountEntity);
        resourcesService.checkStatusAvailable(accountEntity, consentEntity);
        return new ResponseAccountIdentification().data(accountEntity.getAccountIdentificationData());
    }

    public ResponseAccountTransactionsV2 getAccountTransactionsV2(Pageable pageable, String consentId,
                                                                   LocalDate fromDate, LocalDate toDate,
                                                                   String creditDebitIndicator, String acctId) {
        LOG.info("Getting account transactions response for consent id {} and account id {} v2", consentId, acctId);

        var accountEntity = getAccountEntity(consentId, acctId, EnumConsentPermissions.ACCOUNTS_TRANSACTIONS_READ);
        var from = BankLambdaUtils.localDateToOffsetDate(fromDate).withHour(0).withMinute(0);
        var to = BankLambdaUtils.localDateToOffsetDate(toDate).withHour(23).withMinute(59);
        var transactions = creditDebitIndicator != null ? accountTransactionsRepository
                .findByAccountIdAndTransactionDateTimeBetweenAndCreditDebitTypeOrderByCreatedAtAsc(accountEntity.getAccountId(), from, to, creditDebitIndicator, pageable)
                : accountTransactionsRepository
                .findByAccountIdAndTransactionDateTimeBetweenOrderByCreatedAtAsc(accountEntity.getAccountId(), from, to, pageable);

        return new ResponseAccountTransactionsV2().data(transactions.getContent()
                .stream()
                .map(AccountTransactionsEntity::getDtoV2)
                .collect(Collectors.toList()))
                .meta(BankLambdaUtils.getMeta(transactions));
    }

    public ResponseAccountOverdraftLimits getAccountOverdraftLimits(String consentId, String acctId) {
        LOG.info("Getting account OverdraftLimits response for consent id {} and account id {} v1", consentId, acctId);
        var accountEntity = getAccountEntity(consentId, acctId, EnumConsentPermissions.ACCOUNTS_OVERDRAFT_LIMITS_READ);
        return new ResponseAccountOverdraftLimits().data(accountEntity.getOverDraftLimits());
    }

    public ResponseAccountOverdraftLimitsV2 getAccountOverdraftLimitsV2(String consentId, String acctId) {
        LOG.info("Getting account OverdraftLimits response for consent id {} and account id {} v2", consentId, acctId);
        var accountEntity = getAccountEntity(consentId, acctId, EnumConsentPermissions.ACCOUNTS_OVERDRAFT_LIMITS_READ);
        return new ResponseAccountOverdraftLimitsV2().data(accountEntity.getOverDraftLimitsV2());
    }

    private AccountEntity getAccountEntity(String consentId, String acctId, EnumConsentPermissions permission) {
        LOG.info("Getting account for consent id {} and account id {}", consentId, acctId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var accountEntity = BankLambdaUtils.getAccount(acctId, accountRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, permission);
        BankLambdaUtils.checkConsentCoversAccount(consentEntity, accountEntity);
        BankLambdaUtils.checkConsentOwnerIsAccountOwner(consentEntity, accountEntity);

        return accountEntity;
    }

    public ResponseAccountBalances getAccountBalances(String consentId, String acctId) {
        LOG.info("Getting account balances response for consent id {} and account id {} v1", consentId, acctId);
        var accountEntity = getAccountEntity(consentId, acctId, EnumConsentPermissions.ACCOUNTS_BALANCES_READ);
        return new ResponseAccountBalances().data(accountEntity.getAccountBalances());
    }

    public ResponseAccountBalancesV2 getAccountBalancesV2(String consentId, String acctId) {
        LOG.info("Getting account balances response for consent id {} and account id {} v2", consentId, acctId);
        var accountEntity = getAccountEntity(consentId, acctId, EnumConsentPermissions.ACCOUNTS_BALANCES_READ);
        return new ResponseAccountBalancesV2().data(accountEntity.getAccountBalancesV2());
    }

    public ResponseAccount addAccount(CreateAccountData account, String accountHolderId) {
        LOG.info("POST account for an accountHolder id {}", accountHolderId);
        BankLambdaUtils.checkExistAccountHolder(accountHolderId, accountHolderRepository);
        return accountRepository.save(AccountEntity.from(account, UUID.fromString(accountHolderId))).getAdminAccountDto();
    }

    public ResponseAccount updateAccount(String accountId, EditedAccountData account) {
        LOG.info("PUT account for account id {}", accountId);
        AccountEntity accountEntity = BankLambdaUtils.getAccount(accountId, accountRepository);
        return accountRepository.update(accountEntity.update(account)).getAdminAccountDto();
    }

    public void deleteAccount(String accountId) {
        LOG.info("DELETE account for account id {}", accountId);
        AccountEntity accountEntity = BankLambdaUtils.getAccount(accountId, accountRepository);
        accountRepository.delete(accountEntity);
    }

    public ResponseAccountTransaction addTransaction(String accountId, CreateAccountTransactionData transactionDto) {
        LOG.info("POST accountTransaction for account id {}", accountId);
        AccountTransactionsEntity transactionsEntity = AccountTransactionsEntity.from(transactionDto, UUID.fromString(accountId));
        transactionsEntity.setAccountId(UUID.fromString(accountId));
        return accountTransactionsRepository.save(transactionsEntity).getAdminAccountTransactionDto();
    }

    public ResponseAccountTransaction updateAccountTransaction(String accountId, String transactionId, CreateAccountTransactionData transactionDto) {
        LOG.info("PUT accountOverdraftLimit for account id {}", accountId);
        AccountEntity accountEntity = BankLambdaUtils.getAccount(accountId, accountRepository);
        AccountTransactionsEntity transactionsEntity = getAccountTransaction(transactionId, accountEntity);
        return accountTransactionsRepository.update(transactionsEntity.update(transactionDto)).getAdminAccountTransactionDto();
    }

    public void deleteTransaction(String accountId, String transactionId) {
        LOG.info("DELETE AccountTransaction ({}) for account id {}", transactionId, accountId);
        AccountEntity accountEntity = BankLambdaUtils.getAccount(accountId, accountRepository);
        accountTransactionsRepository.deleteById(getAccountTransaction(transactionId, accountEntity).getAccountTransactionId());
    }

    private AccountTransactionsEntity getAccountTransaction(String transactionId, AccountEntity accountEntity) {
        return accountEntity
                .getTransactions()
                .stream()
                .filter(transaction -> transaction.getTransactionId().toString().equals(transactionId))
                .findFirst()
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Could not find Account Transactions Entity with id: " + transactionId));
    }
}
