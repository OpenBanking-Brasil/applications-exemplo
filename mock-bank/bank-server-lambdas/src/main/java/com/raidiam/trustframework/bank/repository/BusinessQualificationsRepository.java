package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.BusinessQualificationsEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BusinessQualificationsRepository extends PageableRepository<BusinessQualificationsEntity, Integer> {
    List<BusinessQualificationsEntity> findByAccountHolderAccountHolderId(UUID accountHolderId);
}
