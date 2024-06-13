package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.ConsentExtensionEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;

import java.util.List;

@Repository
public interface ConsentExtensionRepository extends PageableRepository<ConsentExtensionEntity, Integer> {

    Page<ConsentExtensionEntity> findByConsentId(String consentId, Pageable pageable);
    List<ConsentExtensionEntity> findByConsentId(String consentId);

}
