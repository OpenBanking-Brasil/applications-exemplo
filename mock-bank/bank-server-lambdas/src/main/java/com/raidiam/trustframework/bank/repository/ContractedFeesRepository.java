package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.ContractedFeesEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Repository
public interface ContractedFeesRepository extends PageableRepository<ContractedFeesEntity, UUID> {

    List<ContractedFeesEntity> findByContractId(@NotNull UUID contractId);

    List<ContractedFeesEntity> findAll();
}
