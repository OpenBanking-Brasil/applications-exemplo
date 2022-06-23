package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.ConsentCreditCardAccountsEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;

@Repository
public interface ConsentCreditCardAccountsRepository extends PageableRepository<ConsentCreditCardAccountsEntity, Integer> {

    Page<ConsentCreditCardAccountsEntity> findByConsentIdOrderByCreatedAtAsc(@NotNull String consentId, Pageable pageable);
}
