package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.PersonalFiliationEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface PersonalFiliationRepository extends PageableRepository<PersonalFiliationEntity, Integer> {
}
