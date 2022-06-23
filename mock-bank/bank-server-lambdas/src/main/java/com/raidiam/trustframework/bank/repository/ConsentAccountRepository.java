package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.ConsentAccountEntity;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;

@Repository
public interface ConsentAccountRepository extends PageableRepository<ConsentAccountEntity, Integer> {

    Page<ConsentAccountEntity> findByConsentIdOrderByCreatedAtAsc(@NotNull String consentId, Pageable pageable);

    @Join(value="account", type = Join.Type.FETCH)
    Page<ConsentAccountEntity> findByConsentIdAndAccountAccountTypeOrderByCreatedAtAsc(@NotNull String consentId, @NotNull String accountType,
                                                                                       Pageable pageable);
}
