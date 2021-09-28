package com.raidiam.trustframework.bank.repository

import com.raidiam.trustframework.bank.domain.AccountEntity
import com.raidiam.trustframework.bank.models.generated.AccountSubType
import com.raidiam.trustframework.bank.models.generated.AccountType
import com.raidiam.trustframework.bank.models.generated.Level1Account
import com.raidiam.trustframework.bank.models.generated.Level1AccountStatus
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject
import java.time.Instant
import java.time.OffsetDateTime

@MicronautTest(transactional = false, environments = ["db"])
class AccountRepositorySpec extends Specification {
    @Inject
    @Shared
    AccountRepository accountRepository

    def "We can save an account"() {
        given:
        AccountEntity toSave = new AccountEntity()
        toSave.setAccountSubType(AccountSubType.CHARGECARD.toString())
        toSave.setAccountType(AccountType.BUSINESS.toString())
        toSave.setCurrency("Imperial Credits")
        toSave.setDescription("TestL1Account")
        toSave.setMaturityDate(Date.from(OffsetDateTime.now().toInstant()))
        toSave.setNickname("TestL1Account")
        toSave.setOpeningDate(Date.from(OffsetDateTime.now().toInstant()))
        toSave.setStatus(Level1AccountStatus.ENABLED.toString())
        toSave.setStatusUpdateDateTime(Date.from(Instant.now()))
        toSave.setSwitchStatus(Level1Account.SwitchStatusEnum.SWITCHCOMPLETED.toString())

        when:
        AccountEntity acct = accountRepository.save(toSave)
        Optional<AccountEntity> acctOpt = accountRepository.findById(acct.getReferenceId())

        then:
        acctOpt.isPresent()
        AccountEntity accountBack = acctOpt.get()
        acct == accountBack
        accountBack.getReferenceId() != null
        accountBack.getAccountId() != null
    }
}

