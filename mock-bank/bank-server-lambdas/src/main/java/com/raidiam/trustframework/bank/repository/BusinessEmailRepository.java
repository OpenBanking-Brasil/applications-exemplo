package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.BusinessEmailEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface BusinessEmailRepository extends PageableRepository<BusinessEmailEntity, Integer> {
}
