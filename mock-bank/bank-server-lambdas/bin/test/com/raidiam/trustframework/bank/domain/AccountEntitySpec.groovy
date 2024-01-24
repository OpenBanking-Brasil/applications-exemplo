package com.raidiam.trustframework.bank.domain

import com.raidiam.trustframework.bank.TestEntityDataFactory
import spock.lang.Specification

class AccountEntitySpec extends Specification {

    def "Turning to a DTO works"() {

        when:
        def account = TestEntityDataFactory.anAccount()
        account.accountId = UUID.randomUUID()
        def a_code = account.getCompeCode()
        def b_code = account.getAccountData().getCompeCode()

        then:
        a_code == b_code

        account.currency == account.getAccountIdentificationData().getCurrency()
    }

    def "The equals method gets a workout"() {

        when:
        AccountEntity entity = new AccountEntity()
        entity.setStatus("AVAILABLE")
        entity.setCurrency("GBP")
        AccountEntity entity2 = new AccountEntity()
        entity2.setStatus("AVAILABLE")
        entity2.setCurrency("GBP")

        then:
        entity == entity2
        entity.hashCode() == entity2.hashCode()

        when:
        entity2.setCurrency("USD")

        then:
        entity != entity2
    }

}
