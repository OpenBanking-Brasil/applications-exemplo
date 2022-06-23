package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.PersonalNationalityDocumentEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface PersonalNationalityDocumentRepository extends PageableRepository<PersonalNationalityDocumentEntity, Integer> {
}
