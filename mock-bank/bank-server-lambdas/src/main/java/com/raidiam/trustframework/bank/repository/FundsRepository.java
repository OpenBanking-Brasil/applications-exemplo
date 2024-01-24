package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.FundsEntity;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FundsRepository extends PageableRepository<FundsEntity, UUID> {

    Optional<FundsEntity> findByInvestmentId(UUID investmentId);
    @Join(value="accountHolder", type = Join.Type.FETCH)
    List<FundsEntity> findByAccountHolderId(@NotNull UUID accountHolderId);

}
