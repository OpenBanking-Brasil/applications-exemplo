package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.BusinessQualificationsEconomicActivitiesEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface BusinessQualificationsEconomicActivitiesRepository extends PageableRepository<BusinessQualificationsEconomicActivitiesEntity, Integer> {
}
