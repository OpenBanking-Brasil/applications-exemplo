package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.CreditCardAccountsLimitsEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.UUID;

@Repository
public interface CreditCardAccountsLimitsRepository extends PageableRepository<CreditCardAccountsLimitsEntity, UUID> {
}
