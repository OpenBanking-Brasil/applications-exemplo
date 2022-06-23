package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.PersonalPhoneEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface PersonalPhoneRepository extends PageableRepository<PersonalPhoneEntity, Integer> {
}
