package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.ContractFeeOverParcelEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContractOverParcelFeesRepository extends PageableRepository<ContractFeeOverParcelEntity, UUID> {

    List<ContractFeeOverParcelEntity> findAll();
}
