package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.PersonalPostalAddressEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface PersonalPostalAddressRepository extends PageableRepository<PersonalPostalAddressEntity, Integer> {
}
