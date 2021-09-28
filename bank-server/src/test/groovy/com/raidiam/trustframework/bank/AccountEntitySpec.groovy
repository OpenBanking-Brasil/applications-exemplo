package com.raidiam.trustframework.bank

import com.raidiam.trustframework.bank.domain.AccountEntity
import com.raidiam.trustframework.bank.models.generated.Level1Account
import spock.lang.Specification

import static com.raidiam.trustframework.bank.TestDataFactory.anAccount

class AccountEntitySpec extends Specification {

    def "Grabbing from a DTO works"() {

        given:
        Level1Account accountReq = anAccount()

        when:
        AccountEntity account = AccountEntity.fromRequest(accountReq)

        then:
        account.status == accountReq.status.toString()
        account.currency == accountReq.getCurrency()
        account.accountType == accountReq.getAccountType().toString()
        account.accountSubType == accountReq.getAccountSubType().toString()
        account.description == accountReq.getDescription()
        account.nickname == accountReq.getNickname()
        account.switchStatus == accountReq.getSwitchStatus().toString()
        account.servicerSchemeName == accountReq.getServicer().getSchemeName()
        account.servicerIdentification == accountReq.getServicer().getIdentification()

    }

    def "Turning to a DTO works"() {

        given:
        def request = anAccount()
        def account = AccountEntity.fromRequest(request)
        account.accountId = UUID.randomUUID()

        when:
        def dto = account.getDTOWithPrivateFields()

        then:
        account.status == dto.status.toString()
        account.currency == dto.getCurrency()
        account.accountType == dto.getAccountType().toString()
        account.accountSubType == dto.getAccountSubType().toString()
        account.description == dto.getDescription()
        account.nickname == dto.getNickname()
        account.switchStatus == dto.getSwitchStatus().toString()
        account.servicerSchemeName == dto.getServicer().getSchemeName()
        account.servicerIdentification == dto.getServicer().getIdentification()

    }

    def "The equals method gets a workout"() {

        when:
        def time = new Date()
        AccountEntity entity = new AccountEntity()
        entity.setStatusUpdateDateTime(time)
        entity.setOpeningDate(time)
        entity.setMaturityDate(time)
        AccountEntity entity2 = new AccountEntity()
        entity2.setStatusUpdateDateTime(time)
        entity2.setOpeningDate(time)
        entity2.setMaturityDate(time)

        then:
        entity == entity2
        entity.hashCode() == entity2.hashCode()
        entity.openingDate.compareTo(entity2.openingDate) == 0
        entity.statusUpdateDateTime.compareTo(entity2.statusUpdateDateTime) == 0
        entity.maturityDate.compareTo(entity2.maturityDate) == 0

    }

}
