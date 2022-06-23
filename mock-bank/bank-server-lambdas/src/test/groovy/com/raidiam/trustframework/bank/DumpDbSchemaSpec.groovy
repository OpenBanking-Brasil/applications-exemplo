package com.raidiam.trustframework.bank

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Ignore
import spock.lang.Stepwise

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class DumpDbSchemaSpec extends CleanupSpecification {

    private static final Logger LOG = LoggerFactory.getLogger(DumpDbSchemaSpec)

    // un-ignore to run. This test inits the DB then waits, nothing more
    @Ignore
    def "We can pause for a few minutes while someone dumps the schema"() {
        given:
        LOG.info("Sleeping....")
        Thread.sleep(1000000)
        //
        // now in the toplevel directory run -
        // docker exec -it $(docker ps |grep postgres | awk '{print $1}')  /usr/bin/pg_dump -U test -d bank > ./src/test/resources/import.sql
        //
        LOG.info("Waking....")
        when:
        1 + 1
        then:
        noExceptionThrown()
    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }
}

