package com.raidiam.trustframework.bank.utils

import com.raidiam.trustframework.bank.domain.ConsentEntity
import com.raidiam.trustframework.bank.domain.ConsentPermissionEntity
import com.raidiam.trustframework.mockbank.models.generated.Links
import io.micronaut.context.annotation.Value
import io.micronaut.core.convert.value.MutableConvertibleValues
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpParameters
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.cookie.Cookies
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javax.inject.Inject
import java.time.LocalDate
import java.util.stream.Collectors

@MicronautTest
class BankLambdaUtilsSpec extends Specification {

    @Inject
    BankLambdaUtils bankLambdaUtils
    @Shared
    private final String CERT = "-----BEGIN CERTIFICATE-----\n" +
            "MIIHADCCBeigAwIBAgIUHzap6tr22LPCl0gAz6jkUZbszwQwDQYJKoZIhvcNAQEL\n" +
            "BQAwcTELMAkGA1UEBhMCQlIxHDAaBgNVBAoTE09wZW4gQmFua2luZyBCcmFzaWwx\n" +
            "FTATBgNVBAsTDE9wZW4gQmFua2luZzEtMCsGA1UEAxMkT3BlbiBCYW5raW5nIFNB\n" +
            "TkRCT1ggSXNzdWluZyBDQSAtIEcxMB4XDTIyMDgxMjEyNDgwMFoXDTIzMDkxMTEy\n" +
            "NDgwMFowggEcMQswCQYDVQQGEwJCUjELMAkGA1UECBMCU1AxDzANBgNVBAcTBkxP\n" +
            "TkRPTjEcMBoGA1UEChMTT3BlbiBCYW5raW5nIEJyYXNpbDEtMCsGA1UECxMkNzRl\n" +
            "OTI5ZDktMzNiNi00ZDg1LThiYTctYzE0NmM4NjdhODE3MR8wHQYDVQQDExZtb2Nr\n" +
            "LXRwcC0xLnJhaWRpYW0uY29tMRcwFQYDVQQFEw40MzE0MjY2NjAwMDE5NzEdMBsG\n" +
            "A1UEDxMUUHJpdmF0ZSBPcmdhbml6YXRpb24xEzARBgsrBgEEAYI3PAIBAxMCVUsx\n" +
            "NDAyBgoJkiaJk/IsZAEBEyQxMDEyMDM0MC0zMzE4LTRiYWYtOTllMi0wYjU2NzI5\n" +
            "YzRhYjIwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC5ILYWgl9nlspD\n" +
            "4+vfoZEPHg9STbCy3YgAYqan4tlIWOYqpgAkcuuma9zfk6f9SD3OCfmYyp4pXpT0\n" +
            "wdgwjxu9MTgixsuHPHYLMENO7/OGIHbmFXC2tONPId2OVkC9zdBxPTTtQ8tUQM3Y\n" +
            "rNV6pEWMukOIBYG9RcPklRl0FB+O0gTdkorg9RTkiBRIdDCiEn1h9Tzq+SF4mwpD\n" +
            "Mic85+VpCzot0nGnSx1xb0Wp7WWBPJeDip1pgPm1BL03NBPbyvsAkwklLXU0zZKz\n" +
            "KfW+vGgkGIvKDHREhr+aZPvTzeQ1oukc4S5yLBfgPXESIa9qyIO9GRozzH8IXNCx\n" +
            "4agzkTeNAgMBAAGjggLhMIIC3TAMBgNVHRMBAf8EAjAAMB0GA1UdDgQWBBQm8XFu\n" +
            "PrFYnrEgpmR+Z4hnce2ZWjAfBgNVHSMEGDAWgBSGf1itF/WCtk60BbP7sM4RQ99M\n" +
            "vjBMBggrBgEFBQcBAQRAMD4wPAYIKwYBBQUHMAGGMGh0dHA6Ly9vY3NwLnNhbmRi\n" +
            "b3gucGtpLm9wZW5iYW5raW5nYnJhc2lsLm9yZy5icjBLBgNVHR8ERDBCMECgPqA8\n" +
            "hjpodHRwOi8vY3JsLnNhbmRib3gucGtpLm9wZW5iYW5raW5nYnJhc2lsLm9yZy5i\n" +
            "ci9pc3N1ZXIuY3JsMCEGA1UdEQQaMBiCFm1vY2stdHBwLTEucmFpZGlhbS5jb20w\n" +
            "DgYDVR0PAQH/BAQDAgWgMBMGA1UdJQQMMAoGCCsGAQUFBwMCMIIBqAYDVR0gBIIB\n" +
            "nzCCAZswggGXBgorBgEEAYO6L2QBMIIBhzCCATYGCCsGAQUFBwICMIIBKAyCASRU\n" +
            "aGlzIENlcnRpZmljYXRlIGlzIHNvbGVseSBmb3IgdXNlIHdpdGggUmFpZGlhbSBT\n" +
            "ZXJ2aWNlcyBMaW1pdGVkIGFuZCBvdGhlciBwYXJ0aWNpcGF0aW5nIG9yZ2FuaXNh\n" +
            "dGlvbnMgdXNpbmcgUmFpZGlhbSBTZXJ2aWNlcyBMaW1pdGVkcyBUcnVzdCBGcmFt\n" +
            "ZXdvcmsgU2VydmljZXMuIEl0cyByZWNlaXB0LCBwb3NzZXNzaW9uIG9yIHVzZSBj\n" +
            "b25zdGl0dXRlcyBhY2NlcHRhbmNlIG9mIHRoZSBSYWlkaWFtIFNlcnZpY2VzIEx0\n" +
            "ZCBDZXJ0aWNpY2F0ZSBQb2xpY3kgYW5kIHJlbGF0ZWQgZG9jdW1lbnRzIHRoZXJl\n" +
            "aW4uMEsGCCsGAQUFBwIBFj9odHRwOi8vcmVwb3NpdG9yeS5zYW5kYm94LnBraS5v\n" +
            "cGVuYmFua2luZ2JyYXNpbC5vcmcuYnIvcG9saWNpZXMwDQYJKoZIhvcNAQELBQAD\n" +
            "ggEBAGaESJ0UBfEB0mI8Fh98D6261BWUBdR9vdcD4IX53EubFvOCWE75skpYYyMz\n" +
            "s0dsoU6q/ivHVudhWUWaXCK9UNDgFHb8hE/YaDOoOJLRYllGq0qEyo8u0tJa0XmW\n" +
            "BfXMwNajEvlu3RdKWQ09x+KwEDjCIiJE7hK0cXReJuE6cDc5EPVjQ/fM7TBMQza0\n" +
            "hkZqJgA7555HCi8+k7bovGiV7i9sElvcuWKl2In3AWJke85K0zJaWRXsmkwFTE7i\n" +
            "nkob4yXb4SyGmCnlFEGUZMhqScAsiIrltE5cgLScRZrwymq+1rvYbqgUVOKLVzov\n" +
            "Js11ZIb3W0cRkjSGD9nXP3lfU4s=\n" +
            "-----END CERTIFICATE-----";

    static ConsentPermissionEntity buildPermEntity(String perm) {
        def entity = new ConsentPermissionEntity()
        entity.setPermission(perm)
        return entity
    }

    def "we get the consent permissions" () {
        given:
        def permissionEntities = permissions.stream().map(a -> buildPermEntity(a)).collect(Collectors.toList())
        ConsentEntity consent = new ConsentEntity()
        consent.setConsentPermissions(permissionEntities)

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
        BankLambdaUtils.decorateResponse({ a -> links = a}, 1,"test", page, total)

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

    @Unroll
    def "we can get the day of week from a schedule" () {

        when:
        def retrieved = BankLambdaUtils.getPaymentScheduleWeeklyOrdinal(dayOfWeek)

        then:
        assert(retrieved == result)

        where:
        dayOfWeek           | result
        "SEGUNDA_FEIRA"     | 1
        "TERCA_FEIRA"       | 2
        "QUARTA_FEIRA"      | 3
        "QUINTA_FEIRA"      | 4
        "SEXTA_FEIRA"       | 5
        "SABADO"            | 6
        "DOMINGO"           | 7
        null                | 0
        "TEST"                | 0

    }

    @Unroll
    def "we can get certificate cn" () {
        given:
        def request = HttpRequest.POST("https://www.example.com/examplepage", null)
        request.parameters.add("BANK-TLS-Certificate", param)
        when:
        def retrieved = bankLambdaUtils.getCertificateCNFromRequest(request)

        then:
        if(value == null) {
            retrieved.isEmpty()
        } else {
            retrieved.get() == value
        }

        where:
        param          | value
        CERT           | "mock-tpp-1.raidiam.com"
        null           | null
    }
}
