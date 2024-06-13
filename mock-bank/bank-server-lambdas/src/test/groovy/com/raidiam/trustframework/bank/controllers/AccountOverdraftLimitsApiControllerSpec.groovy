package com.raidiam.trustframework.bank.controllers

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.raidiam.trustframework.bank.AuthHelper
import com.raidiam.trustframework.bank.services.AccountsService
import com.raidiam.trustframework.bank.utils.BankLambdaUtils
import com.raidiam.trustframework.mockbank.models.generated.ResponseAccountOverdraftLimitsV2
import io.micronaut.function.aws.proxy.MicronautLambdaContainerHandler
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.runtime.Micronaut
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccount

@MicronautTest
class AccountOverdraftLimitsApiControllerSpec extends Specification {

    def mapper = new ObjectMapper()
    MicronautLambdaContainerHandler handler
    AccountsService mockService = Mock(AccountsService)
    ResponseAccountOverdraftLimitsV2 response
    private static Context lambdaContext = new MockLambdaContext()

    @Shared
    def accountId = UUID.randomUUID()


    @Inject
    BankLambdaUtils bankLambdaUtils

    @MockBean(BankLambdaUtils)
    BankLambdaUtils bankLambdaUtils() {
        Mock(BankLambdaUtils)
    }

    def setup () {
        mapper.findAndRegisterModules()
        handler = new MicronautLambdaContainerHandler(Micronaut.build()
                .singletons(mockService, bankLambdaUtils))

        given: "An Account OverdraftLimits"
        def account = anAccount()
        account.setAccountId(accountId)

        response = new ResponseAccountOverdraftLimitsV2().data(account.getOverDraftLimitsV2())

        bankLambdaUtils.getConsentIdFromRequest(_ as HttpRequest<?>) >> "12345"
    }

    def cleanup() {
        handler.close()
    }

    void "we can get account OverdraftLimits"() {
        given:
        mockService.getAccountOverdraftLimitsV2(_ as String, _ as String) >> response
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/accounts/v2/accounts/${accountId}/overdraft-limits", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "accounts", builder)
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body

        and:
        response.getMultiValueHeaders().containsKey("x-fapi-interaction-id")
    }

    void "we can not get account OverdraftLimits without ACCOUNTS_OVERDRAFT_LIMITS_READ role"() {
        given:
        mockService.getAccountBalances(_ as String, _ as String) >> response
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/accounts/v2/accounts/${accountId}/overdraft-limits", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "bananas", builder)
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.FORBIDDEN.code
    }
}
