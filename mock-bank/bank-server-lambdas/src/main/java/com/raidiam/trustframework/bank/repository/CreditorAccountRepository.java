package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.CreditorAccountEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface CreditorAccountRepository extends PageableRepository<CreditorAccountEntity, Integer> {
}
