package com.raidiam.trustframework.bank.jwt

import com.raidiam.trustframework.bank.TestJwtSigner
import com.raidiam.trustframework.bank.exceptions.TrustframeworkException
import spock.lang.Specification

class JwtSignerSpec extends Specification {

    def "unparseable jwt"() {

        when:
        TestJwtSigner.sign("notajwt", null)

        then:
        TrustframeworkException e = thrown()

    }

}
