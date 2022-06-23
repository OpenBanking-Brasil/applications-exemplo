package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.CreditCardAccountsEntity;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditCardAccountsRepository extends PageableRepository<CreditCardAccountsEntity, UUID> {

    Optional<CreditCardAccountsEntity> findByCreditCardAccountId(@NotNull UUID accountId);

    List<CreditCardAccountsEntity> findByAccountHolderId(@NotNull UUID accountHolderId);

    @Join(value="accountHolder", type = Join.Type.FETCH)
    List<CreditCardAccountsEntity> findByAccountHolderUserId(@NotNull String userId);
}
