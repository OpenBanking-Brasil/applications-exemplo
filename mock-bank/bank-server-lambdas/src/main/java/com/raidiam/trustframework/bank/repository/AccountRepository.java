package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.AccountEntity;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends PageableRepository<AccountEntity, Integer> {

    Optional<AccountEntity> findByAccountId(@NotNull UUID accountId);
    List<AccountEntity> findByAccountHolderId(@NotNull UUID accountId);

    @Join(value="accountHolder", type = Join.Type.FETCH)
    List<AccountEntity> findByAccountHolderUserId(@NotNull String userId);
}
