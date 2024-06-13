package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.ContractEntity;
import com.raidiam.trustframework.bank.domain.ContractedFinanceChargesEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Repository
public interface ContractedFinanceChargesRepository extends PageableRepository<ContractedFinanceChargesEntity, UUID> {

    List<ContractedFinanceChargesEntity> findByContract(@NotNull ContractEntity contract);

    List<ContractedFinanceChargesEntity> findAll();
}
