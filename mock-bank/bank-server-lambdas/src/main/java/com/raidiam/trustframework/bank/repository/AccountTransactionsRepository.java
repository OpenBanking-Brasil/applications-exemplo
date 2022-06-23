package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.AccountTransactionsEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface AccountTransactionsRepository extends PageableRepository<AccountTransactionsEntity, Integer> {

    Page<AccountTransactionsEntity> findByAccountIdOrderByCreatedAtAsc(@NotNull UUID accountId, Pageable pageable);

    Page<AccountTransactionsEntity> findByAccountIdAndTransactionDateBetweenIsOrderByCreatedAtAsc(@NotNull UUID accountId,
                                                                                                  @NotNull LocalDate startDate,
                                                                                                  @NotNull LocalDate endDate,
                                                                                                  Pageable pageable);

    Page<AccountTransactionsEntity> findByAccountIdAndTransactionDateBetweenIsAndCreditDebitTypeOrderByCreatedAtAsc(@NotNull UUID accountId,
                                                                                                                    @NotNull LocalDate startDate,
                                                                                                                    @NotNull LocalDate endDate,
                                                                                                                    @NotNull String creditDebitType,
                                                                                                                    Pageable pageable);
}