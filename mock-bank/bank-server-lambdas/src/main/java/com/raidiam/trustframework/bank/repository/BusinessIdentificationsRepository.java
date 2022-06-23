package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.BusinessIdentificationsEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BusinessIdentificationsRepository extends PageableRepository<BusinessIdentificationsEntity, Integer> {
    List<BusinessIdentificationsEntity> findByAccountHolderAccountHolderId(UUID accountHolderId);
}
