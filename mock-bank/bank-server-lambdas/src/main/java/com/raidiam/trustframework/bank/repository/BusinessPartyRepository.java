package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.BusinessPartyEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface BusinessPartyRepository extends PageableRepository<BusinessPartyEntity, Integer> {
}
