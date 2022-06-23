package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.ContractWarrantyEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Repository
public interface ContractWarrantiesRepository extends PageableRepository<ContractWarrantyEntity, UUID> {

    List<ContractWarrantyEntity> findByContractIdOrderByCreatedAtAsc(@NotNull UUID contractId);

    Page<ContractWarrantyEntity> findByContractIdOrderByCreatedAtAsc(@NotNull UUID contractId, Pageable pageable);
}
