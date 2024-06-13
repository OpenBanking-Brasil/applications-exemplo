package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.ContractEntity;
import com.raidiam.trustframework.bank.domain.ContractWarrantyEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Repository
public interface ContractWarrantiesRepository extends PageableRepository<ContractWarrantyEntity, UUID> {
    Page<ContractWarrantyEntity> findByContractOrderByCreatedAtAsc(@NotNull ContractEntity contract, Pageable pageable);
}
