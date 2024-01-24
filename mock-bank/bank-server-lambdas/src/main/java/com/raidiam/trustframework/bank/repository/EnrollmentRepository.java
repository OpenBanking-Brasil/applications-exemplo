package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.EnrollmentEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.Optional;

@Repository
public interface EnrollmentRepository extends PageableRepository<EnrollmentEntity, Integer> {
    Optional<EnrollmentEntity> findByIdempotencyKey(String idempotencyKey);
    Optional<EnrollmentEntity> findByEnrollmentId(String id);
}
