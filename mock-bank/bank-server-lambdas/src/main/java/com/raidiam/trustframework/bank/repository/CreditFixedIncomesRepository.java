package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.CreditFixedIncomesEntity;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditFixedIncomesRepository extends PageableRepository<CreditFixedIncomesEntity, UUID> {

    Optional<CreditFixedIncomesEntity> findByInvestmentId(UUID investmentId);

    @Join(value="accountHolder", type = Join.Type.FETCH)
    List<CreditFixedIncomesEntity> findByAccountHolderId(@NotNull UUID accountHolderId);

}
