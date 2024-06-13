package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.CreditCardAccountsLimitsEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Repository
public interface CreditCardAccountsLimitsRepository extends PageableRepository<CreditCardAccountsLimitsEntity, UUID> {
    List<CreditCardAccountsLimitsEntity> findByCreditCardAccountId(@NotNull UUID creditCardAccountId);
}
