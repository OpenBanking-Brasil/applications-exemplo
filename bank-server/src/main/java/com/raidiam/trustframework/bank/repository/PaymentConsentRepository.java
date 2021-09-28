package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.PaymentConsentEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.Optional;

@Repository
public interface PaymentConsentRepository extends PageableRepository<PaymentConsentEntity, String> {
    Optional<PaymentConsentEntity> findByPaymentConsentId(String Id);
    Optional<PaymentConsentEntity> findByIdempotencyKey(String idempotencyKey);
}
