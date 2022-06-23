package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.BusinessOtherDocumentEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface BusinessOtherDocumentRepository extends PageableRepository<BusinessOtherDocumentEntity, Integer> {
}
