package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.PersonalNationalityEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface PersonalNationalityRepository extends PageableRepository<PersonalNationalityEntity, Integer> {
}
