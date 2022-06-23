package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.ContractBalloonPaymentsEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContractBalloonPaymentsRepository extends PageableRepository<ContractBalloonPaymentsEntity, UUID> {

    List<ContractBalloonPaymentsEntity> findAll();
}
