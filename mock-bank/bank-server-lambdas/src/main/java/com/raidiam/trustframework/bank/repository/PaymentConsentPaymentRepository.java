package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.PaymentConsentPaymentEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface PaymentConsentPaymentRepository extends PageableRepository<PaymentConsentPaymentEntity, Integer> {
}
