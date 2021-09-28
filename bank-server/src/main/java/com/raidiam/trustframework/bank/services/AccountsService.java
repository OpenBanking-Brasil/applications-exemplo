package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.domain.AccountEntity;
import com.raidiam.trustframework.bank.domain.PrivateAccountEntity;
import com.raidiam.trustframework.bank.models.generated.Level1Account;
import com.raidiam.trustframework.bank.models.generated.Level2Account;
import com.raidiam.trustframework.bank.repository.AccountRepository;
import com.raidiam.trustframework.bank.repository.PrivateAccountRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AccountsService extends BaseBankService{

    AccountsService(AccountRepository accountRepository, PrivateAccountRepository privateAccountRepository) {
        this.accountRepository = accountRepository;
        this.privateAccountRepository = privateAccountRepository;
    }

    public Page<Level1Account> getAccounts (Pageable pageable) {
        return accountRepository.findAll(Optional.ofNullable(pageable).orElse(Pageable.unpaged())).map(AccountEntity::getDTO);
    }

    @Transactional
    public Level1Account createAccount (Level1Account body) {
        AccountEntity account = AccountEntity.fromRequest(body);
        accountRepository.save(account);
        for (Level2Account l2Account: body.getAccount()) {
            PrivateAccountEntity privateEntity = PrivateAccountEntity.fromRequest(l2Account, account);
            privateAccountRepository.save(privateEntity);
        }
        return account.getDTOWithPrivateFields();
    }

    // needs to switch on who is calling in order to decide whether to return l2 details
    public Page<Level1Account> getAccount (String acctId) {
        return accountRepository.findByAccountId(UUID.fromString(acctId))
                .map(AccountEntity::getDTOWithPrivateFields)
                .map(List::of)
                .map(list -> Page.of(list, Pageable.unpaged(), 1))
                .orElse(null);
    }
}
