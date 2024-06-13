package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.BusinessIdentificationsEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessIdentificationsRepository extends PageableRepository<BusinessIdentificationsEntity, UUID> {
    List<BusinessIdentificationsEntity> findByAccountHolderAccountHolderId(UUID accountHolderId);
    Optional<BusinessIdentificationsEntity> findByBusinessIdentificationsId(@NotNull UUID businessIdentificationsId);
}
