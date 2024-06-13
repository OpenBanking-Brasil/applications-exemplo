package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.PersonalIdentificationsEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonalIdentificationsRepository extends PageableRepository<PersonalIdentificationsEntity, UUID> {
    List<PersonalIdentificationsEntity> findByAccountHolderAccountHolderId(UUID accountHolderId);
    Optional<PersonalIdentificationsEntity> findByPersonalIdentificationsId(UUID personalIdentificationsId);
}
