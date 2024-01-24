package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.BankFixedIncomesTransactionsEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface BankFixedIncomesTransactionsRepository extends PageableRepository<BankFixedIncomesTransactionsEntity, UUID> {
    Page<BankFixedIncomesTransactionsEntity> findByInvestmentIdAndTransactionDateBetween(
            @NotNull UUID investmentId,
            @NotNull LocalDate from,
            @NotNull LocalDate to,
            Pageable pageable);
}
