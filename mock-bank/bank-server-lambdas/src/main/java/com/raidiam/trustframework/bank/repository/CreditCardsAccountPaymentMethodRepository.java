package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.CreditCardsAccountPaymentMethodEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.UUID;

@Repository
public interface CreditCardsAccountPaymentMethodRepository extends PageableRepository<CreditCardsAccountPaymentMethodEntity, UUID> {
}
