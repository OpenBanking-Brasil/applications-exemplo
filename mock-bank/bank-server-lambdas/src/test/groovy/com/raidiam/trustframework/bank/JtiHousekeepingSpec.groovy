package com.raidiam.trustframework.bank

import com.raidiam.trustframework.bank.domain.JtiEntity
import com.raidiam.trustframework.bank.repository.JtiRepository
import com.raidiam.trustframework.bank.utils.JtiHousekeeping
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject
import java.time.OffsetDateTime

@MicronautTest(transactional = false, environments = ["db"])
class JtiHousekeepingSpec extends Specification {

    @Inject
    private JtiRepository jtiRepository

    @Inject
    private JtiHousekeeping housekeeping

    def "JTIs older than a week are cleared out"() {

        given:
        JtiEntity jti = new JtiEntity()
        jti.setJti(UUID.randomUUID().toString())
        jtiRepository.save(jti)

        and:
        jti = new JtiEntity()
        jti.setJti(UUID.randomUUID().toString())
        jti.setCreatedDate(OffsetDateTime.now().minusDays(8))
        jtiRepository.save(jti)

        when:
        housekeeping.onApplicationEvent(null)

        then:
        jtiRepository.count() == 1

    }

}
