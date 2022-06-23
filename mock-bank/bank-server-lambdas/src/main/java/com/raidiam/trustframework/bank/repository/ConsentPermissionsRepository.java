package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.ConsentPermissionEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;

@Repository
public interface ConsentPermissionsRepository extends PageableRepository<ConsentPermissionEntity, Integer> {
    void deleteByConsentId(@NotNull String uuid);
}
