package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.AccountEntity;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.annotation.EntityGraph;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends PageableRepository<AccountEntity, Integer> {

    Optional<AccountEntity> findByAccountId(@NotNull UUID accountId);

    @EntityGraph(attributePaths = {"transactions", "accountHolder"})
    List<AccountEntity> findByAccountHolderId(@NotNull UUID accountHolderId);

    Optional<AccountEntity> findByNumberAndAccountHolderId(@NotNull String accountNumber, @NotNull UUID accountHolderId);
    Optional<AccountEntity> findByNumber(@NotNull String accountNumber);

    @Join(value="accountHolder", type = Join.Type.FETCH)
    List<AccountEntity> findByAccountHolderUserId(@NotNull String userId);
}
