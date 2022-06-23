package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.PersonalEmailEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface PersonalEmailRepository extends PageableRepository<PersonalEmailEntity, Integer> {
}
