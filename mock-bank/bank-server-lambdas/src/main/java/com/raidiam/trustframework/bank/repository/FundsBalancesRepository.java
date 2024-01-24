package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.FundsBalanceEntity;
import com.raidiam.trustframework.bank.domain.FundsEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FundsBalancesRepository extends PageableRepository<FundsBalanceEntity, UUID> {
    Optional<FundsBalanceEntity> findByInvestment(@NotNull FundsEntity fundsEntity);
}
