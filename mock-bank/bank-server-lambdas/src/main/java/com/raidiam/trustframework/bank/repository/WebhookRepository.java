package com.raidiam.trustframework.bank.repository;


import com.raidiam.trustframework.bank.domain.WebhookEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Repository
public interface WebhookRepository  extends PageableRepository<WebhookEntity, Integer> {
    Optional<WebhookEntity> findByClientId(@NotNull String clientId);
}
