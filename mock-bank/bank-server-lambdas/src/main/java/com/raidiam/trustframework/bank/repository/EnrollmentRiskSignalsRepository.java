package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.EnrollmentRiskSignalsEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface EnrollmentRiskSignalsRepository extends PageableRepository<EnrollmentRiskSignalsEntity, Integer> {

}
