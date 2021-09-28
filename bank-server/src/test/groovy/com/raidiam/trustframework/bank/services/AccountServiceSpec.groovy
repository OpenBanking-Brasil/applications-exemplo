package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.models.generated.AccountSubType
import com.raidiam.trustframework.bank.models.generated.AccountType
import com.raidiam.trustframework.bank.models.generated.Level1Account
import com.raidiam.trustframework.bank.models.generated.Level1AccountStatus
import com.raidiam.trustframework.bank.models.generated.Level2Account
import com.raidiam.trustframework.bank.models.generated.Level2Accounts
import com.raidiam.trustframework.bank.models.generated.Servicer
import com.raidiam.trustframework.bank.repository.AccountRepository
import com.raidiam.trustframework.bank.repository.PrivateAccountRepository
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable;
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared;
import spock.lang.Specification

import javax.inject.Inject
import java.time.OffsetDateTime

import static com.raidiam.trustframework.bank.TestDataFactory.anAccount;

@MicronautTest(transactional = false, environments = ["db"])
class AccountServiceSpec extends Specification {

    @Inject
    @Shared
    AccountRepository accountRepository

    @Inject
    @Shared
    PrivateAccountRepository privateAccountRepository

    @Inject
    @Shared
    AccountsService accountsService

    def "We can create an account"() {

        given: "An account"
        def accountReq = anAccount()

        accountReq = accountsService.createAccount(accountReq)

        when:
        Page page = accountsService.getAccount(accountReq.accountId)

        then:
        page.totalSize == 1L

        when:
        accountReq = page.content.iterator().next()

        then:
        accountReq

    }

    def "We can get all accounts"() {

        given: "An account"
        accountRepository.deleteAll()
        def accountReq = anAccount()

        accountReq = accountsService.createAccount(accountReq)

        when:
        Page page = accountsService.getAccounts(Pageable.from(0))

        then:
        page.totalSize == 1L

        when:
        accountReq = page.content.iterator().next()

        then:
        accountReq

    }

}
