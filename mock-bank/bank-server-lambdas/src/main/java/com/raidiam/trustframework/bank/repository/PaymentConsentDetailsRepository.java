package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.PaymentConsentDetailsEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface PaymentConsentDetailsRepository extends PageableRepository<PaymentConsentDetailsEntity, Integer> {
}
