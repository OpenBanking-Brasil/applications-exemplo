package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.ExchangesOperationEventEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Repository
public interface ExchangesOperationEventRepository extends PageableRepository<ExchangesOperationEventEntity, UUID> {
    Page<ExchangesOperationEventEntity> findAllByOperationId(@NotNull UUID operationId, Pageable pageable);



}
