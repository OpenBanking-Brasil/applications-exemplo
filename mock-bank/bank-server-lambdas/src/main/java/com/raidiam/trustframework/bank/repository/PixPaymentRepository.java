package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.PaymentConsentEntity;
import com.raidiam.trustframework.bank.domain.PixPaymentEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.Optional;

@Repository
public interface PixPaymentRepository extends PageableRepository<PixPaymentEntity, Integer> {
    Optional<PixPaymentEntity> findByPaymentId(String paymentId);
    Optional<PixPaymentEntity> findByIdempotencyKey(String idempotencyKey);
    Optional<PixPaymentEntity> findByPaymentConsentEntity(PaymentConsentEntity paymentConsentEntity);
}
