package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.ConsentEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Repository
public interface ConsentRepository extends PageableRepository<ConsentEntity, Integer> {
    Optional<ConsentEntity> findByConsentId(@NotNull String id);
}
