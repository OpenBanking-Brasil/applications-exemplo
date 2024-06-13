package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.ConsentContractEntity;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;

@Repository
public interface ConsentContractRepository extends PageableRepository<ConsentContractEntity, Integer> {

    Page<ConsentContractEntity> findByConsentIdOrderByCreatedAtAsc(@NotNull String consentId, @NotNull Pageable pageable);

    @Join(value = "contract", type = Join.Type.FETCH)
    Page<ConsentContractEntity> findByConsentIdAndContractContractTypeOrderByCreatedAtAsc(@NotNull String consentId, @NotNull String contractType,
                                                                                          Pageable pageable);
    @Join(value = "contract", type = Join.Type.FETCH)
    Page<ConsentContractEntity> findByConsentIdAndContractContractTypeAndContractStatusOrderByCreatedAtAsc(@NotNull String consentId, @NotNull String contractType,
                                                                                                   @NotNull String status, Pageable pageable);
}
