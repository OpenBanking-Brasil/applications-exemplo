package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.TreasureTitlesEntity;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TreasureTitlesRepository extends PageableRepository<TreasureTitlesEntity, UUID> {

    Optional<TreasureTitlesEntity> findByInvestmentId(UUID investmentId);
    @Join(value="accountHolder", type = Join.Type.FETCH)
    List<TreasureTitlesEntity> findByAccountHolderId(@NotNull UUID accountHolderId);
}
