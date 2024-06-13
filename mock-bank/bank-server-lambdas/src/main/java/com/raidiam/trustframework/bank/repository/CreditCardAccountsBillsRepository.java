package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.CreditCardAccountsBillsEntity;
import com.raidiam.trustframework.bank.domain.CreditCardAccountsEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditCardAccountsBillsRepository extends PageableRepository<CreditCardAccountsBillsEntity, UUID> {

    Optional<CreditCardAccountsBillsEntity> findByBillId(@NotNull UUID billId);

    Page<CreditCardAccountsBillsEntity> findByAccountAndDueDateBetweenOrderByCreatedAtAsc(@NotNull CreditCardAccountsEntity account,
                                                                                                        @NotNull LocalDate startDate,
                                                                                                        @NotNull LocalDate endDate,
                                                                                                        Pageable pageable);
}
