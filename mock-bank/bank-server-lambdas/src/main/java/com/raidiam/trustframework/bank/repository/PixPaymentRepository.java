package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.PaymentConsentEntity;
import com.raidiam.trustframework.bank.domain.PixPaymentEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PixPaymentRepository extends PageableRepository<PixPaymentEntity, Integer> {
    Optional<PixPaymentEntity> findByPaymentId(String paymentId);

    List<PixPaymentEntity> findByIdempotencyKey(String idempotencyKey);
    List<PixPaymentEntity> findByPaymentConsentEntity(PaymentConsentEntity paymentConsentEntity);
    List<PixPaymentEntity> findByPaymentConsentEntityAndDateBetween(PaymentConsentEntity paymentConsentEntity, LocalDate startDate, LocalDate endDate);
}
