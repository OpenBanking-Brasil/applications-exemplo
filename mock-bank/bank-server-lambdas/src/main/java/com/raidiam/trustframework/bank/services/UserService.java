package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.domain.AccountEntity;
import com.raidiam.trustframework.bank.domain.ContractEntity;
import com.raidiam.trustframework.bank.domain.CreditCardAccountsEntity;
import com.raidiam.trustframework.bank.domain.ExchangesOperationEntity;
import com.raidiam.trustframework.bank.enums.ResourceType;
import com.raidiam.trustframework.mockbank.models.generated.ResponseAccountList;
import com.raidiam.trustframework.mockbank.models.generated.ResponseCreditCardAccountsList;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.stream.Collectors;

@Singleton
@Transactional
public class UserService extends BaseBankService {

    // used by the /user API, so there is reduced checking.
    public ResponseCreditCardAccountsList getCreditCardAccounts(@NotNull String userId) {
        var consentCreditCardAccount = creditCardAccountsRepository.findByAccountHolderUserId(userId);
        return new ResponseCreditCardAccountsList().data(consentCreditCardAccount
                .stream()
                .map(CreditCardAccountsEntity::getCreditCardAccountsData)
                .collect(Collectors.toList()));
    }

    public ResponseAccountList getAccounts (@NotNull String accountHolderId) {
        var accounts = accountRepository.findByAccountHolderUserId(accountHolderId);
        return new ResponseAccountList().data(accounts
                .stream()
                .map(AccountEntity::getAccountData)
                .collect(Collectors.toList()));
    }

    // no protection or checks, this is used by the /user interface for the openID server only
    public ResponseAccountList getContractList(@NotNull String userId, ResourceType contractType) {
        var contractsList = contractsRepository.findByAccountHolderUserIdAndContractType(userId, contractType.toString());
        return new ResponseAccountList().data(contractsList.stream().map(ContractEntity::createSparseAccountData).collect(Collectors.toList()));
    }

    public ResponseAccountList getExchangesList(@NotNull String userId) {
        var exchangeList = exchangesOperationRepository.findByAccountHolderUserId(userId);
        return new ResponseAccountList().data(exchangeList.stream().map(ExchangesOperationEntity::createSparseAccountData).collect(Collectors.toList()));
    }
}
