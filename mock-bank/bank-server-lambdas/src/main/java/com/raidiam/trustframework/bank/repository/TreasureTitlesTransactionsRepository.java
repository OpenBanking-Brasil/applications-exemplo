package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.TreasureTitlesTransactionsEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface TreasureTitlesTransactionsRepository extends PageableRepository<TreasureTitlesTransactionsEntity, UUID> {
    Page<TreasureTitlesTransactionsEntity> findByInvestmentIdAndTransactionDateBetween(
            @NotNull UUID investmentId,
            @NotNull LocalDate from,
            @NotNull LocalDate to,
            Pageable pageable);
}
