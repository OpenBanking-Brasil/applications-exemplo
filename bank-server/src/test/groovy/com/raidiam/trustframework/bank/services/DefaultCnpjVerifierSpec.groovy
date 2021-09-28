package com.raidiam.trustframework.bank.services


import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest
class DefaultCnpjVerifierSpec extends Specification {

    private static final String KNOWN_CNPJ = '01042487000138'
    private static final String UNKNOWN_CNPJ = '01042487000139'

    @Inject
    private CnpjVerifier apiService

    def "Service gives true for known CNPJ"() {

        expect:
        apiService.isKnownCnpj(KNOWN_CNPJ)

    }

    def "Service gives false for unknown CNPJ"() {

        expect:
        apiService.isKnownCnpj(UNKNOWN_CNPJ)

    }

    @MockBean(RolesApiClient)
    RolesApiClient rolesApiClient() {
        def roles = [
                DirectoryRole.builder()
                        .status("Active")
                        .registrationNumber("01042487000138")
                        .build(),
                DirectoryRole.builder()
                        .status("Active")
                        .registrationNumber("01042487000139")
                        .build()
        ]
        new RolesApiClient() {
            @Override
            List<DirectoryRole> roles() {
                roles
            }
        }
    }

}
