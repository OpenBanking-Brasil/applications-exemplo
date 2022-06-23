package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.BusinessPhoneEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface BusinessPhoneRepository extends PageableRepository<BusinessPhoneEntity, Integer> {
}
