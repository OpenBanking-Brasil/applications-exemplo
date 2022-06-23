package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.PersonalCompanyCnpjEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface PersonalCompanyCnpjRepository extends PageableRepository<PersonalCompanyCnpjEntity, Integer> {
}
