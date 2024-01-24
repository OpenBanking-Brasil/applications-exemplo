package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.TreasureTitlesBalanceEntity;
import com.raidiam.trustframework.bank.domain.TreasureTitlesEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TreasureTitlesBalancesRepository extends PageableRepository<TreasureTitlesBalanceEntity, UUID> {
    Optional<TreasureTitlesBalanceEntity> findByInvestment(@NotNull TreasureTitlesEntity treasureTitlesEntity);
}
