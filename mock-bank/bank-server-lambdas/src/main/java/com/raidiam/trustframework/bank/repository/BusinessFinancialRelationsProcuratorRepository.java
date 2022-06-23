package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.BusinessFinancialRelationsProcuratorEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface BusinessFinancialRelationsProcuratorRepository extends PageableRepository<BusinessFinancialRelationsProcuratorEntity, Integer> {
}
