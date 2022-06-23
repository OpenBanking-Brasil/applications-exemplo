package com.raidiam.trustframework.bank.controllers

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.raidiam.trustframework.bank.AuthHelper
import com.raidiam.trustframework.bank.services.AccountsService
import com.raidiam.trustframework.bank.services.ConsentService
import com.raidiam.trustframework.mockbank.models.generated.ResponseAccountBalances
import io.micronaut.function.aws.proxy.MicronautLambdaContainerHandler
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpStatus
import io.micronaut.runtime.Micronaut
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import static com.raidiam.trustframework.bank.TestRequestDataFactory.anAccountBalances

@MicronautTest
class AccountBalancesApiControllerSpec extends Specification {

    def mapper = new ObjectMapper()
    MicronautLambdaContainerHandler handler
    AccountsService mockService = Mock(AccountsService)
    ConsentService consentService = Mock(ConsentService)

    ResponseAccountBalances response
    private static Context lambdaContext = new MockLambdaContext()

    def setup () {
        mapper.findAndRegisterModules();
        handler = new MicronautLambdaContainerHandler(Micronaut.build().singletons(mockService, consentService))

        given: "An Account Balances"
        response = new ResponseAccountBalances().data(anAccountBalances())
    }

    def cleanup() {
        handler.close()
    }

    void "we can not get account Balances without ACCOUNTS_BALANCES_READ role"() {
        given:
        mockService.getAccountBalances(_ as String, _ as String) >> response
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/accounts/v1/accounts/account1/balances', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "bananas consent:urn:raidiambank:bf43d0e5-7bc2-4a5b-b6da-19d43fabd991", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.FORBIDDEN.code
    }

    void "we can get account Balances"() {
        given:
        mockService.getAccountBalances(_ as String, _ as String) >> response
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/accounts/v1/accounts/account1/balances', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "accounts consent:urn:raidiambank:bf43d0e5-7bc2-4a5b-b6da-19d43fabd991", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
    }

}

