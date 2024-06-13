package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.BusinessPartyEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Repository
public interface BusinessPartyRepository extends PageableRepository<BusinessPartyEntity, Integer> {
    Optional<BusinessPartyEntity>  findByDocumentNumber(@NotNull String documentNumber);
}
