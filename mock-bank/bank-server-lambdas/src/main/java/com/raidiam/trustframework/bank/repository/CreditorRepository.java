package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.CreditorEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface CreditorRepository extends PageableRepository<CreditorEntity, Integer> {
}
