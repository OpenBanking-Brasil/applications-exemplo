package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.ConsentExchangeOperationEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface ConsentExchangeOperationRepository extends PageableRepository<ConsentExchangeOperationEntity, Integer> {

}
