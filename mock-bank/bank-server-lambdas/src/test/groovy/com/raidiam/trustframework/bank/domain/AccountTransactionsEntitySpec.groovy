package com.raidiam.trustframework.bank.domain

import spock.lang.Ignore
import spock.lang.Specification

import static com.raidiam.trustframework.bank.TestEntityDataFactory.aTransaction

class AccountTransactionsEntitySpec extends Specification{


    def "we can create an accountTransaction from an incoming DTO" () {
        given:
        def transaction = aTransaction(UUID.randomUUID())
        def dto = transaction.getDto()
        when:
        def rebuiltTransaction = AccountTransactionsEntity.from(dto, transaction.getAccountId())
        then:
        rebuiltTransaction == transaction
    }
}
