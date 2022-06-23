package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.ContractInterestRatesEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContractInterestRatesRepository extends PageableRepository<ContractInterestRatesEntity, UUID> {
    List<ContractInterestRatesEntity> findAll();
}
