package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.EnrollmentFidoSignOptionsEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.Optional;

@Repository
public interface EnrollmentFidoSignOptionsRepository extends PageableRepository<EnrollmentFidoSignOptionsEntity, Integer> {


    Optional<EnrollmentFidoSignOptionsEntity> findByEnrollmentId(String enrollmentId);
}
