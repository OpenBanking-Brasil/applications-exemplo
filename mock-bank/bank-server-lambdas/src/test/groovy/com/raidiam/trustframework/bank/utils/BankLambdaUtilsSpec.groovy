package com.raidiam.trustframework.bank.utils

import com.raidiam.trustframework.bank.domain.ConsentEntity
import com.raidiam.trustframework.bank.domain.ConsentPermissionEntity
import com.raidiam.trustframework.mockbank.models.generated.Links
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification
import spock.lang.Unroll

import javax.inject.Inject
import java.time.LocalDate
import java.util.stream.Collectors

@MicronautTest
class BankLambdaUtilsSpec extends Specification {

    @Inject
    BankLambdaUtils bankLambdaUtils

    static ConsentPermissionEntity buildPermEntity(String perm) {
        def entity = new ConsentPermissionEntity()
        entity.setPermission(perm)
        return entity
    }

    def "we get the consent permissions" () {
        given:
        def permissionEntities = permissions.stream().map(a -> buildPermEntity(a)).collect(Collectors.toSet())
        ConsentEntity consent = new ConsentEntity()
        consent.setPermissions(permissionEntities)

        when:
        def result = BankLambdaUtils.getConsentPermissions(consent)

        then:
        noExceptionThrown()
        result.size() == resultSize

        where:
        permissions                                     | resultSize
        List.of("", "a")                                | 0
        List.of("ACCOUNTS_READ", "a")                   | 1
        List.of("ACCOUNTS_READ", "RESOURCES_READ")      | 2
        List.of("ACCOUNTS_READ", "RESOURCES_READ", "a") | 2

    }

    @Unroll
    def "we generate the pagination links correctly" () {
        given:
        Links links = new Links()

        when:
        BankLambdaUtils.decorateResponse({ a -> links = a}, total, "test", page)

        then:
        prev == (links.prev != null)
        next == (links.next != null)

        where:
        total | page | prev  | next
        1     | 0    | false | false
        2     | 0    | false | true
        2     | 1    | true  | false
        3     | 1    | true  | true
    }

    @Unroll
    def "we get dates out of http request params properly" () {
        given:
        def request = HttpRequest.GET("https://www.example.com/examplepage?${param}=${value}")

        when:
        def retrieved = bankLambdaUtils.getDateFromRequest(request, param)

        then:
        retrieved.get() == LocalDate.parse(value)

        where:
        param         | value
        "fromDueDate" | "2021-01-01"
        "toDueDate"   | "2022-06-05"
    }

    @Unroll
    def "we get the right sort of exception for bad param formatting" () {
        given:
        def request = HttpRequest.GET("https://www.example.com/examplepage?${param}=${value}")

        when:
        def retrieved = bankLambdaUtils.getDateFromRequest(request, param)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.BAD_REQUEST

        where:
        param         | value
        "fromDueDate" | "111"
        "toDueDate"   | "22-06-05"
        "toDueDate"   | "22-06-2021"
        "toDueDate"   | "banana"
        "toDueDate"   | ""
        "toDueDate"   | null
    }

    @Unroll
    def "we can get payeeMcc" () {
        given:
        def request = HttpRequest.GET("https://www.example.com/examplepage${param}")

        when:
        def retrieved = bankLambdaUtils.getPayeeMCCFromRequest(request)

        then:
        if(value == null) {
            retrieved.isEmpty()
        } else {
            retrieved.get() == value
        }

        where:
        param          | value
        "?payeeMCC=1"  | 1
        "?payeeMCC=2"  | 2
        "?payeeMcc=2"  | null
        null           | null
    }
}
