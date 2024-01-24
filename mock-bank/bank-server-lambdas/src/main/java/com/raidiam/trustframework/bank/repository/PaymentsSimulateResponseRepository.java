package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.PaymentsSimulateResponseEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentsSimulateResponseRepository extends PageableRepository<PaymentsSimulateResponseEntity, UUID> {
    List<PaymentsSimulateResponseEntity> findByUserClientIdAndRequestEndTimeAfter(@NotNull String clientId, @NotNull LocalDateTime now);
    List<PaymentsSimulateResponseEntity> findByRequestEndTimeBefore(@NotNull LocalDateTime now);
}
