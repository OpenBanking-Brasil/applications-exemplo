package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.PersonalFinancialRelationsEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonalFinancialRelationsRepository extends PageableRepository<PersonalFinancialRelationsEntity, UUID> {
    Optional<PersonalFinancialRelationsEntity> findByAccountHolderAccountHolderId(UUID accountHolderId);
}
