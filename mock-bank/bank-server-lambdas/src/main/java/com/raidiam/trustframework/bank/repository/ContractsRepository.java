package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.ContractEntity;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractsRepository extends PageableRepository<ContractEntity, UUID> {
    Optional<ContractEntity> findByContractIdAndContractType(@NotNull UUID contractId, @NotNull String contractType);
    List<ContractEntity> findByAccountHolderIdAndContractType(@NotNull UUID accountHolderId, @NotNull String contractType);

    @Join(value="accountHolder", type = Join.Type.FETCH)
    List<ContractEntity> findByAccountHolderUserIdAndContractType(@NotNull String userId, @NotNull String contractType);

}
