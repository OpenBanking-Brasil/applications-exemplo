package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.PixPaymentPaymentEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface PixPaymentPaymentRepository extends PageableRepository<PixPaymentPaymentEntity, Integer> {
}
