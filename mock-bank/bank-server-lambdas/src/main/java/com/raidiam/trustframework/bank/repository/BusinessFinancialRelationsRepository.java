package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.BusinessFinancialRelationsEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessFinancialRelationsRepository extends PageableRepository<BusinessFinancialRelationsEntity, UUID> {
    Optional<BusinessFinancialRelationsEntity> findByAccountHolderAccountHolderId(UUID accountHolderId);
}
