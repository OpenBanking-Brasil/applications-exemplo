package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.BusinessFinancialRelationsProductsServicesTypeEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface BusinessFinancialRelationsProductsServicesRepository extends PageableRepository<BusinessFinancialRelationsProductsServicesTypeEntity, Integer> {
}
