package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.PersonalOtherDocumentEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface PersonalOtherDocumentRepository extends PageableRepository<PersonalOtherDocumentEntity, Integer> {
}
