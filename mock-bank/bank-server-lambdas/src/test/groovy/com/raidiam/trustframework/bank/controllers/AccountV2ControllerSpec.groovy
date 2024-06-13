package com.raidiam.trustframework.bank.controllers

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.raidiam.trustframework.bank.AuthHelper
import com.raidiam.trustframework.bank.FullCreateConsentFactory
import com.raidiam.trustframework.bank.TestRequestDataFactory
import com.raidiam.trustframework.bank.domain.AccountHolderEntity
import com.raidiam.trustframework.bank.utils.BankLambdaUtils
import com.raidiam.trustframework.mockbank.models.generated.CreateAccount
import com.raidiam.trustframework.mockbank.models.generated.CreateAccountTransaction
import com.raidiam.trustframework.mockbank.models.generated.ResponseAccount
import com.raidiam.trustframework.mockbank.models.generated.ResponseAccountBalancesV2
import com.raidiam.trustframework.mockbank.models.generated.ResponseAccountData
import com.raidiam.trustframework.mockbank.models.generated.ResponseAccountOverdraftLimitsV2
import com.raidiam.trustframework.mockbank.models.generated.ResponseAccountTransaction
import com.raidiam.trustframework.mockbank.models.generated.ResponseAccountTransactionData
import com.raidiam.trustframework.mockbank.models.generated.ResponseAccountTransactionsV2
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.apache.http.client.utils.URIBuilder
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Stepwise
import java.time.LocalDate
import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccountHolder

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
@Testcontainers
class AccountV2ControllerSpec extends FullCreateConsentFactory {
    @Shared
    AccountHolderEntity accountHolder
    @Shared
    ResponseAccountData postAccountResponse
    @Shared
    ResponseAccountTransactionData postTransactionResponse
    @Shared
    String accountToken

    def setup() {
        if (runSetup) {
            //ADD Account Holder via db
            accountHolder = accountHolderRepository.save(anAccountHolder())

            //ADD Account via Controller
            CreateAccount newAccount = TestRequestDataFactory.createAccount()
            postAccountResponse = client.toBlocking()
                    .retrieve(HttpRequest.POST("/admin/customers/${accountHolder.getAccountHolderId().toString()}/accounts", mapper.writeValueAsString(newAccount))
                            .header("Authorization", "Bearer ${createToken("op:admin")}"), ResponseAccount).getData()

            //ADD Account Transaction via V2 Controller
            CreateAccountTransaction accountTransactionDto = TestRequestDataFactory.createAccountTransaction()
            String transactionUrl = "/admin/customers/${accountHolder.getAccountHolderId().toString()}/accounts/${postAccountResponse.getAccountId()}/transactions"
            postTransactionResponse = client.toBlocking()
                    .retrieve(HttpRequest.POST(transactionUrl, mapper.writeValueAsString(accountTransactionDto))
                            .header("Authorization", "Bearer ${createToken("op:admin")}"),
                            ResponseAccountTransaction).data

            //ADD Update Consent via Controller
            accountToken = createConsentWithAccountPermissions(accountHolder, postAccountResponse.getAccountId())

            runSetup = false
        }
    }

    def "we cannot call account endpoints without x-fapi-interaction-id"() {
        given:
        URI uri = new URIBuilder("/open-banking/accounts/v2/${endpoint}").build()

        when:
        client.toBlocking().retrieve(HttpRequest.GET(uri)
                .header("Authorization", "Bearer ${accountToken}"), ResponseAccountBalancesV2)

        then:
        HttpClientResponseException e = thrown()
        e.status == HttpStatus.BAD_REQUEST

        where:
        endpoint << ["accounts", "accounts/testid", "accounts/testid/balances", "accounts/testid/transactions", "accounts/testid/transactions-current", "accounts/testid/overdraft-limits"]
    }

    void "we can GET account balances v2"() {
        when:
        URI uri = new URIBuilder("/open-banking/accounts/v2/accounts/${postAccountResponse.getAccountId()}/balances").build()

        def response = client.toBlocking().retrieve(HttpRequest.GET(uri)
                .header("Authorization", "Bearer ${accountToken}")
                .header("x-fapi-interaction-id", UUID.randomUUID().toString()), ResponseAccountBalancesV2)

        then:
        response.getData() != null
        def balances = response.getData()
        balances.getAvailableAmount().getAmount() == BankLambdaUtils.formatAmountV2(postAccountResponse.getAvailableAmount())
        balances.getAvailableAmount().getCurrency() == postAccountResponse.getAvailableAmountCurrency()
        balances.getBlockedAmount().getAmount() == BankLambdaUtils.formatAmountV2(postAccountResponse.getBlockedAmount())
        balances.getBlockedAmount().getCurrency() == postAccountResponse.getBlockedAmountCurrency()
        balances.getAutomaticallyInvestedAmount().getAmount() == BankLambdaUtils.formatAmountV2(postAccountResponse.getAutomaticallyInvestedAmount())
        balances.getAutomaticallyInvestedAmount().getCurrency() == postAccountResponse.getAutomaticallyInvestedAmountCurrency()
    }

    void "we can GET account overdraft limits v2"() {
        when:
        URI uri = new URIBuilder("/open-banking/accounts/v2/accounts/${postAccountResponse.getAccountId()}/overdraft-limits").build()

        def response = client.toBlocking().retrieve(HttpRequest.GET(uri)
                .header("Authorization", "Bearer ${accountToken}")
                .header("x-fapi-interaction-id", UUID.randomUUID().toString()), ResponseAccountOverdraftLimitsV2)

        then:
        response.getData() != null
        def overdraftLimits = response.getData()
        overdraftLimits.getOverdraftContractedLimit().getAmount() == BankLambdaUtils.formatAmountV2(postAccountResponse.getOverdraftContractedLimit())
        overdraftLimits.getOverdraftContractedLimit().getCurrency() == postAccountResponse.getOverdraftContractedLimitCurrency()
        overdraftLimits.getOverdraftUsedLimit().getAmount() == BankLambdaUtils.formatAmountV2(postAccountResponse.getOverdraftUsedLimit())
        overdraftLimits.getOverdraftUsedLimit().getCurrency() == postAccountResponse.getOverdraftUsedLimitCurrency()
        overdraftLimits.getUnarrangedOverdraftAmount().getAmount() == BankLambdaUtils.formatAmountV2(postAccountResponse.getUnarrangedOverdraftAmount())
        overdraftLimits.getUnarrangedOverdraftAmount().getCurrency() == postAccountResponse.getUnarrangedOverdraftAmountCurrency()
    }

    void "we can GET account transactions v2"() {
        when:
        URI uri = new URIBuilder("/open-banking/accounts/v2/accounts/${postAccountResponse.getAccountId()}/transactions")
                .addParameter("fromBookingDate", LocalDate.now().toString())
                .addParameter("toBookingDate", LocalDate.now().plusDays(2).toString())
                .build()

        def response = client.toBlocking().retrieve(HttpRequest.GET(uri)
                .header("Authorization", "Bearer ${accountToken}")
                .header("x-fapi-interaction-id", UUID.randomUUID().toString()), ResponseAccountTransactionsV2)

        then:
        response.getData() != null
        response.getData().size() == 1

        def transaction = response.getData().first()
        transaction.getTransactionId() != null
        transaction.getCompletedAuthorisedPaymentType().name() == postTransactionResponse.getCompletedAuthorisedPaymentType().name()
        transaction.getCreditDebitType().name() == postTransactionResponse.getCreditDebitType().name()
        transaction.getTransactionName() == postTransactionResponse.getTransactionName()
        transaction.getType().name() == postTransactionResponse.getType().name()
        transaction.getTransactionAmount().getAmount() == BankLambdaUtils.formatAmountV2(postTransactionResponse.getAmount())
        transaction.getTransactionAmount().getCurrency() == postTransactionResponse.getTransactionCurrency()
        transaction.getPartieCnpjCpf() == postTransactionResponse.getPartieCnpjCpf()
        transaction.getPartiePersonType().name() == postTransactionResponse.getPartiePersonType().name()
        transaction.getPartieCompeCode() == postTransactionResponse.getPartieCompeCode()
        transaction.getPartieBranchCode() == postTransactionResponse.getPartieBranchCode()
        transaction.getPartieNumber() == postTransactionResponse.getPartieNumber()
        transaction.getPartieCheckDigit() == postTransactionResponse.getPartieCheckDigit()
    }

    void "we can GET account transactions current v2"() {
        when:
        URI uri = new URIBuilder("/open-banking/accounts/v2/accounts/${postAccountResponse.getAccountId()}/transactions-current")
                .addParameter("fromBookingDate", LocalDate.now().toString())
                .addParameter("toBookingDate", LocalDate.now().plusDays(2).toString())
                .build()

        def response = client.toBlocking().retrieve(HttpRequest.GET(uri)
                .header("Authorization", "Bearer ${accountToken}")
                .header("x-fapi-interaction-id", UUID.randomUUID().toString()), ResponseAccountTransactionsV2)

        then:
        response.getData() != null
        response.getData().size() == 1
    }

    void "we can GET throw account transactions current v2"() {
        when:
        URI uri = new URIBuilder("/open-banking/accounts/v2/accounts/${postAccountResponse.getAccountId()}/transactions-current")
                .addParameter("fromBookingDate", fromBookingDate.toString())
                .addParameter("toBookingDate", toBookingDate.toString())
                .build()

        client.toBlocking().retrieve(HttpRequest.GET(uri)
                .header("Authorization", "Bearer ${accountToken}")
                .header("x-fapi-interaction-id", UUID.randomUUID().toString()), ResponseAccountTransactionsV2)

        then:
        HttpClientResponseException e = thrown()
        e.status == status

        where:
        fromBookingDate              | toBookingDate                | status                          | msg
        LocalDate.now().minusDays(9) | LocalDate.now()              | HttpStatus.UNPROCESSABLE_ENTITY | "Date range must be no more than 7 days"
        LocalDate.now()              | LocalDate.now().minusDays(1) | HttpStatus.UNPROCESSABLE_ENTITY | "Wrong date period"
    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }
}
