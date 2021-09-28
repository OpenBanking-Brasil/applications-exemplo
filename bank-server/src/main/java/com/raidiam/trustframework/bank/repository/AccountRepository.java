package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.AccountEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends PageableRepository<AccountEntity, Integer> {

    Optional<AccountEntity> findByAccountId(UUID accountId);

}
