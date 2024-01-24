package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.FidoJwkEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.Optional;

@Repository
public interface FidoJwkRepository extends PageableRepository<FidoJwkEntity, Integer> {

    Optional<FidoJwkEntity> findByKid(String kid);

    Optional<FidoJwkEntity> findByKidAndEnrollmentId(String kid, String enrollmentId);



}
