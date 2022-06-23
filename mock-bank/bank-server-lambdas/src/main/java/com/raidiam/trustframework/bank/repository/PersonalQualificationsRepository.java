package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.PersonalQualificationsEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PersonalQualificationsRepository extends PageableRepository<PersonalQualificationsEntity, Integer> {
    List<PersonalQualificationsEntity> findByAccountHolderAccountHolderId(UUID accountHolderId);
}
