package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.VariableIncomesBalanceEntity;
import com.raidiam.trustframework.bank.domain.VariableIncomesEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VariableIncomesBalancesRepository extends PageableRepository<VariableIncomesBalanceEntity, UUID> {
    Optional<VariableIncomesBalanceEntity> findByInvestment(@NotNull VariableIncomesEntity variableIncomesEntity);
}
