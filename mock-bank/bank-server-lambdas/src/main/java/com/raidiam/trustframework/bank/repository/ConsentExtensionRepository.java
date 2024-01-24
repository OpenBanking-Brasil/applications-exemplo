package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.ConsentExtensionEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.List;

@Repository
public interface ConsentExtensionRepository extends PageableRepository<ConsentExtensionEntity, Integer> {

    List<ConsentExtensionEntity> findByConsentId(String consentId);

}
