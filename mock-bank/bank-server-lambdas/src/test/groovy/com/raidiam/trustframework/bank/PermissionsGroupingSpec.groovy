package com.raidiam.trustframework.bank


import com.raidiam.trustframework.bank.services.validate.ConsentValidator
import com.raidiam.trustframework.bank.services.validate.PermissionGroups
import com.raidiam.trustframework.bank.services.validate.PermissionsGroupingValidator
import com.raidiam.trustframework.mockbank.models.generated.CreateConsent
import com.raidiam.trustframework.mockbank.models.generated.CreateConsentData
import com.raidiam.trustframework.mockbank.models.generated.EnumConsentPermissions
import io.micronaut.http.exceptions.HttpStatusException
import spock.lang.Specification

class PermissionsGroupingSpec extends Specification {

    def "We can request a consent" () {
        given:
        CreateConsentData consentData = new CreateConsentData()
        consentData.setPermissions([EnumConsentPermissions.ACCOUNTS_BALANCES_READ])
        CreateConsent consent = new CreateConsent().data(consentData)

        and:
        ConsentValidator validator = new PermissionsGroupingValidator()

        when:
        validator.validate(consent)

        then:
        thrown(HttpStatusException)
    }

    def "no complete set of permissions fails"() {

        when:
        ConsentValidator validator = new PermissionsGroupingValidator()
        validator.validate(createConsent(permissions))

        then:
        noExceptionThrown()

        where:
        permissions << PermissionGroups.ALL_PERMISSION_GROUPS

    }

    def "no combinations of complete sets of permissions fails"() {

        when:
        ConsentValidator validator = new PermissionsGroupingValidator()
        validator.validate(createConsent(permissions))

        then:
        noExceptionThrown()

        where:
        permissions << combinedSets(PermissionGroups.ALL_PERMISSION_GROUPS)

    }

    def "all incomplete sets of permissions fails"() {

        when:
        ConsentValidator validator = new PermissionsGroupingValidator()
        validator.validate(createConsent(permissions))

        then:
        thrown(HttpStatusException)

        where:
        permissions << loseOneFromEach(PermissionGroups.ALL_PERMISSION_GROUPS)

    }

    CreateConsent createConsent(permissions) {
        new CreateConsent().data(new CreateConsentData().permissions(new ArrayList<EnumConsentPermissions>(permissions)))
    }

    Set<Set<EnumConsentPermissions>> loseOneFromEach(input) {
        Set<Set<EnumConsentPermissions>> output = new HashSet<>()
        input.each {
            def incomplete = new HashSet(it)
            def no = incomplete.iterator().next()
            incomplete.remove(no)
            output << incomplete
        }
        output
    }

    Set<Set<EnumConsentPermissions>> combinedSets(input) {
        Set<Set<EnumConsentPermissions>> output = new HashSet<>()
        input.size().times {x ->
            def iter = input.iterator()
            Set<EnumConsentPermissions> current = new HashSet<>()
            x.times { y ->
                current.addAll(iter.next())
            }
            output << current
        }
        output
    }

}
