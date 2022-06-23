package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.domain.AccountEntity;
import com.raidiam.trustframework.bank.domain.AccountTransactionsEntity;
import com.raidiam.trustframework.bank.domain.ConsentAccountEntity;
import com.raidiam.trustframework.bank.repository.AccountRepository;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.data.model.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Singleton
public class AccountsService extends BaseBankService {

    private static final Logger LOG = LoggerFactory.getLogger(AccountsService.class);

    AccountsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public ResponseAccountList getAccounts(Pageable pageable, String consentId, String accountType) {
        LOG.info("Getting accounts response for consent id {}", consentId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.ACCOUNTS_READ);

        var consentAccount = accountType != null ?
                consentAccountRepository.findByConsentIdAndAccountAccountTypeOrderByCreatedAtAsc(consentId, accountType, pageable)
                : consentAccountRepository.findByConsentIdOrderByCreatedAtAsc(consentId, pageable);
        BankLambdaUtils.checkConsentOwnerIsAccountOwner(consentAccount, consentEntity);
        var response = new ResponseAccountList().data(consentAccount.getContent()
                .stream()
                .map(ConsentAccountEntity::getAccount)
                .map(AccountEntity::getAccountData)
                .collect(Collectors.toList()));

        response.setMeta(BankLambdaUtils.getMeta(consentAccount, response.getData().size()));

        return response;
    }

    public ResponseAccountIdentification getAccount(String consentId, String acctId) {
        LOG.info("Getting account response for consent id {} and account id {}", consentId, acctId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var accountEntity = BankLambdaUtils.getAccount(acctId, accountRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentCoversAccount(consentEntity, accountEntity);
        BankLambdaUtils.checkConsentOwnerIsAccountOwner(consentEntity, accountEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.ACCOUNTS_READ);

        return new ResponseAccountIdentification().data(accountEntity.getAccountIdentificationData());
    }

    public ResponseAccountTransactions getAccountTransactions(Pageable pageable, String consentId,
                                                              LocalDate fromDate, LocalDate toDate, String creditDebitIndicator, String acctId) {
        LOG.info("Getting account transactions response for consent id {} and account id {}", consentId, acctId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var accountEntity = BankLambdaUtils.getAccount(acctId, accountRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentCoversAccount(consentEntity, accountEntity);
        BankLambdaUtils.checkConsentOwnerIsAccountOwner(consentEntity, accountEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.ACCOUNTS_TRANSACTIONS_READ);

        var transactions = creditDebitIndicator != null ? accountTransactionsRepository
                .findByAccountIdAndTransactionDateBetweenIsAndCreditDebitTypeOrderByCreatedAtAsc(accountEntity.getAccountId(), fromDate, toDate, creditDebitIndicator, pageable)
                : accountTransactionsRepository
                .findByAccountIdAndTransactionDateBetweenIsOrderByCreatedAtAsc(accountEntity.getAccountId(), fromDate, toDate, pageable);

        var response = new ResponseAccountTransactions().data(transactions.getContent()
                .stream()
                .map(AccountTransactionsEntity::getDto)
                .collect(Collectors.toList()));

        response.setMeta(BankLambdaUtils.getMeta(transactions, response.getData().size()));

        return response;
    }

    public ResponseAccountOverdraftLimits getAccountOverdraftLimits(String consentId, String acctId) {
        LOG.info("Getting account OverdraftLimits response for consent id {} and account id {}", consentId, acctId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var accountEntity = BankLambdaUtils.getAccount(acctId, accountRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentCoversAccount(consentEntity, accountEntity);
        BankLambdaUtils.checkConsentOwnerIsAccountOwner(consentEntity, accountEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.ACCOUNTS_OVERDRAFT_LIMITS_READ);

        return new ResponseAccountOverdraftLimits().data(accountEntity.getOverDraftLimits());
    }

    public ResponseAccountBalances getAccountBalances(String consentId, String acctId) {
        LOG.info("Getting account balances response for consent id {} and account id {}", consentId, acctId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var accountEntity = BankLambdaUtils.getAccount(acctId, accountRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentCoversAccount(consentEntity, accountEntity);
        BankLambdaUtils.checkConsentOwnerIsAccountOwner(consentEntity, accountEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.ACCOUNTS_BALANCES_READ);

        return new ResponseAccountBalances().data(accountEntity.getAccountBalances());
    }
}
