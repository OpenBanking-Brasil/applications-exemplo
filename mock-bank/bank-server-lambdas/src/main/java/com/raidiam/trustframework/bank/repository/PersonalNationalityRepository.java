package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.PersonalNationalityEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonalNationalityRepository extends PageableRepository<PersonalNationalityEntity, UUID> {
    Optional<PersonalNationalityEntity> findByPersonalNationalityId(@NotNull UUID personalNationalityId);
}
