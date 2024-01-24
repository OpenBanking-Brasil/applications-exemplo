package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.BankFixedIncomesBalanceEntity;
import com.raidiam.trustframework.bank.domain.BankFixedIncomesEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankFixedIncomesBalancesRepository extends PageableRepository<BankFixedIncomesBalanceEntity, UUID> {
    Optional<BankFixedIncomesBalanceEntity> findByInvestment(@NotNull BankFixedIncomesEntity bankFixedIncomes);
}
