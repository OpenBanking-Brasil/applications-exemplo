package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.PersonalFinancialRelationsProcuratorEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface PersonalFinancialRelationsProcuratorRepository extends PageableRepository<PersonalFinancialRelationsProcuratorEntity, Integer> {
}
