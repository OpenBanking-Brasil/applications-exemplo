package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.JtiEntity;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;
import io.micronaut.http.annotation.QueryValue;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface JtiRepository extends PageableRepository<JtiEntity, Integer>  {

    Optional<JtiEntity> getByJti(String jti);

    @Query("DELETE from JtiEntity j where j.createdDate < :watershed")
    void deleteOld(@QueryValue("watershed") OffsetDateTime watershed);
}
