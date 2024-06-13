package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.PaymentConsentEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.annotation.EntityGraph;
import io.micronaut.data.repository.PageableRepository;

import java.util.Optional;

@Repository
public interface PaymentConsentRepository extends PageableRepository<PaymentConsentEntity, Integer> {
    @EntityGraph(attributePaths = {"creditorEntities", "paymentConsentPaymentEntity",
            "paymentConsentPaymentEntity.paymentConsentDetails", "accountHolder", "accountEntity",
            "vrpRecurringConfiguration", "vrpRecurringConfiguration.periodicLimits",
            "automaticRecurringConfiguration", "postSweepingRecurringConfiguration",
            "postSweepingRecurringConfiguration.periodicLimits"
    })
    Optional<PaymentConsentEntity> findByPaymentConsentId(String Id);

    @EntityGraph(attributePaths = {"creditorEntities", "paymentConsentPaymentEntity",
            "paymentConsentPaymentEntity.paymentConsentDetails", "accountHolder", "accountEntity",
            "vrpRecurringConfiguration", "vrpRecurringConfiguration.periodicLimits",
            "automaticRecurringConfiguration", "postSweepingRecurringConfiguration",
            "postSweepingRecurringConfiguration.periodicLimits"
    })
    Optional<PaymentConsentEntity> findByIdempotencyKey(String idempotencyKey);
}
