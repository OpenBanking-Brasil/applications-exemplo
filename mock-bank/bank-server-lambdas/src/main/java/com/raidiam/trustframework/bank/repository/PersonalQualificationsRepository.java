package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.PersonalQualificationsEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonalQualificationsRepository extends PageableRepository<PersonalQualificationsEntity, Integer> {
    Optional<PersonalQualificationsEntity> findByAccountHolderAccountHolderId(UUID accountHolderId);
}
