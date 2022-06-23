package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.BusinessFinancialRelationsEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BusinessFinancialRelationsRepository extends PageableRepository<BusinessFinancialRelationsEntity, Integer> {
    List<BusinessFinancialRelationsEntity> findByAccountHolderAccountHolderId(UUID accountHolderId);
}
