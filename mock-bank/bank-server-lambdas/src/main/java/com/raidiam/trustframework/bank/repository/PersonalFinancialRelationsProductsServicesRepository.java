package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.PersonalFinancialRelationsProductsServicesTypeEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface PersonalFinancialRelationsProductsServicesRepository extends PageableRepository<PersonalFinancialRelationsProductsServicesTypeEntity, Integer> {
}
