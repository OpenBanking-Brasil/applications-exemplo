package com.raidiam.trustframework.bank

import com.raidiam.trustframework.bank.domain.AccountEntity
import spock.lang.Specification

import static TestEntityDataFactory.anAccount

class AccountEntitySpec extends Specification {

    def "Turning to a DTO works"() {

        when:
        def account = anAccount()
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
