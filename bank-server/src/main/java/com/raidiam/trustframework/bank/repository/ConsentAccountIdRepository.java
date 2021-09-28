package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.ConsentAccountIdEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface ConsentAccountIdRepository extends PageableRepository<ConsentAccountIdEntity, String> {
}
