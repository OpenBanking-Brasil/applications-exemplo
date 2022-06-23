package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.BusinessIdentificationsCompanyCnpjEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface BusinessIdentificationsCompanyCnpjRepository extends PageableRepository<BusinessIdentificationsCompanyCnpjEntity, Integer> {

}
