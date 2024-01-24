package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.EnrollmentFidoRegistrationOptionsEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.Optional;

@Repository
public interface EnrollmentFidoRegistrationOptionsRepository extends PageableRepository<EnrollmentFidoRegistrationOptionsEntity, Integer> {

    Optional<EnrollmentFidoRegistrationOptionsEntity> findByEnrollmentId(String enrollmentId);

}
