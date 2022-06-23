package com.raidiam.trustframework.bank

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject
import java.time.OffsetDateTime
import java.time.ZoneOffset

@MicronautTest
class DateFormatSpec extends Specification {

    @Inject
    private ObjectMapper objectMapper

    def "Dates are in the correct format in JSON responses"() {

        given:
        OffsetDateTime dateTime = OffsetDateTime.of(2021, 6, 9, 11, 12, 1, 0, ZoneOffset.UTC)

        when:
        def doc = [time: dateTime]
        def json = objectMapper.writeValueAsString(doc)

        then:
        println json
        json == '{"time":"2021-06-09T11:12:01Z"}'

    }

}
