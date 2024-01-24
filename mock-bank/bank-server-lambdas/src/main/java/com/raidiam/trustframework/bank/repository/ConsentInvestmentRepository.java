package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.ConsentInvestmentEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Repository
public interface ConsentInvestmentRepository extends PageableRepository<ConsentInvestmentEntity, Integer> {

    Optional<ConsentInvestmentEntity> findByConsentId(@NotNull String consentId);

}
