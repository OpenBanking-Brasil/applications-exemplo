package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.VariableIncomesBrokerNotesEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VariableIncomesBrokerNotesRepository extends PageableRepository<VariableIncomesBrokerNotesEntity, UUID> {

    Optional<VariableIncomesBrokerNotesEntity> findByInvestmentIdAndBrokerNoteId(UUID investmentId, UUID brokerNoteId);

}
