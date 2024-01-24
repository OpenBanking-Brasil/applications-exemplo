package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.CreditFixedIncomesBalanceEntity;
import com.raidiam.trustframework.bank.domain.CreditFixedIncomesEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditFixedIncomesBalancesRepository extends PageableRepository<CreditFixedIncomesBalanceEntity, UUID> {
    Optional<CreditFixedIncomesBalanceEntity> findByInvestment(@NotNull CreditFixedIncomesEntity creditFixedIncomes);
}
