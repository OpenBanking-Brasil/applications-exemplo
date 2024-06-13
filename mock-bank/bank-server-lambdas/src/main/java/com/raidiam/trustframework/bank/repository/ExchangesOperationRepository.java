package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.ExchangesOperationEntity;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExchangesOperationRepository extends PageableRepository<ExchangesOperationEntity, UUID> {
    Optional<ExchangesOperationEntity> findByOperationId(@NotNull UUID operationId);
    @Join(value="accountHolder", type = Join.Type.FETCH)
    List<ExchangesOperationEntity> findByAccountHolderUserId(@NotNull String accountHolderId);
}
