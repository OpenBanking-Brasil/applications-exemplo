package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.PersonalFinancialRelationsEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PersonalFinancialRelationsRepository extends PageableRepository<PersonalFinancialRelationsEntity, Integer> {
    List<PersonalFinancialRelationsEntity> findByAccountHolderAccountHolderId(UUID accountHolderId);
}
