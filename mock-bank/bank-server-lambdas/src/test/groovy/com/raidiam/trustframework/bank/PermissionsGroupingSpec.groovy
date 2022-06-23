package com.raidiam.trustframework.bank


import com.raidiam.trustframework.bank.exceptions.TrustframeworkException
import com.raidiam.trustframework.bank.services.validate.ConsentValidator
import com.raidiam.trustframework.bank.services.validate.PermissionGroups
import com.raidiam.trustframework.bank.services.validate.PermissionsGroupingValidator
import com.raidiam.trustframework.mockbank.models.generated.CreateConsent
import com.raidiam.trustframework.mockbank.models.generated.CreateConsentData
import spock.lang.Specification

class PermissionsGroupingSpec extends Specification {

    def "We can request a consent" () {
        given:
        CreateConsentData consentData = new CreateConsentData()
        consentData.setPermissions([CreateConsentData.PermissionsEnum.ACCOUNTS_BALANCES_READ])
        CreateConsent consent = new CreateConsent().data(consentData)

        and:
        ConsentValidator validator = new PermissionsGroupingValidator()

        when:
        validator.validate(consent)

        then:
        thrown(TrustframeworkException)
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
        thrown(TrustframeworkException)

        where:
        permissions << loseOneFromEach(PermissionGroups.ALL_PERMISSION_GROUPS)

    }

    CreateConsent createConsent(permissions) {
        new CreateConsent().data(new CreateConsentData().permissions(new ArrayList<CreateConsentData.PermissionsEnum>(permissions)))
    }

    Set<Set<CreateConsentData.PermissionsEnum>> loseOneFromEach(input) {
        Set<Set<CreateConsentData.PermissionsEnum>> output = new HashSet<>()
        input.each {
            def incomplete = new HashSet(it)
            def no = incomplete.iterator().next()
            incomplete.remove(no)
            output << incomplete
        }
        output
    }

    Set<Set<CreateConsentData.PermissionsEnum>> combinedSets(input) {
        Set<Set<CreateConsentData.PermissionsEnum>> output = new HashSet<>()
        input.size().times {x ->
            def iter = input.iterator()
            Set<CreateConsentData.PermissionsEnum> current = new HashSet<>()
            x.times { y ->
                current.addAll(iter.next())
            }
            output << current
        }
        output
    }

}
