package com.raidiam.trustframework.bank.controllers

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.raidiam.trustframework.bank.AuthHelper
import com.raidiam.trustframework.bank.domain.AccountEntity
import com.raidiam.trustframework.bank.services.AccountsService
import com.raidiam.trustframework.bank.utils.BankLambdaUtils
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.context.annotation.Value
import io.micronaut.data.model.Pageable
import io.micronaut.function.aws.proxy.MicronautLambdaContainerHandler
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.runtime.Micronaut
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject
import java.time.OffsetDateTime

import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccount

@MicronautTest
class AccountControllerSpec extends Specification {

    private static Context lambdaContext = new MockLambdaContext()
    def mapper = new ObjectMapper()

    AccountsService mockService = Mock(AccountsService)

    ResponseAccountList accountPage
    AccountEntity account

    MicronautLambdaContainerHandler handler

    @Value("\${mockbank.mockbankUrl}")
    String appBaseUrl

    @Inject
    BankLambdaUtils bankLambdaUtils

    @MockBean(BankLambdaUtils)
    BankLambdaUtils bankLambdaUtils() {
        Mock(BankLambdaUtils)
    }

    def setup() {
        handler = new MicronautLambdaContainerHandler(Micronaut.build().singletons(mockService, bankLambdaUtils))

        mapper.findAndRegisterModules()

        account = anAccount()
        account.setAccountId(UUID.randomUUID())

        accountPage = new ResponseAccountList().data(List.of(account.getAccountData())).meta(new Meta().totalPages(1))

        bankLambdaUtils.getConsentIdFromRequest(_ as HttpRequest<?>) >> "12345"
        bankLambdaUtils.getAttributeFromRequest(_ as HttpRequest<?>, _ as String) >> Optional.of("acc_type")
    }

    def cleanup() {
        handler.close()
    }

    void "we can get all accounts v2"() {
        given:

        mockService.getAccounts(_ as Pageable, _ as String, _ as String) >> accountPage

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/accounts/v2/accounts", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "accounts", builder)
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body

    }

    void "we can get an account V2"() {
        given:
        mockService.getAccount(_ as String, _ as String) >> new ResponseAccountIdentification().data(account.getAccountIdentificationData())

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/accounts/v2/accounts/abc123", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "accounts", builder)
        builder.header("x-fapi-interaction-id", UUID.randomUUID().toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body

    }

    void "We need the correct scope to proceed"() {

        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/accounts/v2/accounts/abc123", HttpMethod.GET.toString())
        AuthHelper.authorize(builder, scopes: "org:nobody")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.FORBIDDEN.code
    }

    void "We need a known scope to proceed"() {

        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/accounts/v2/accounts/abc123", HttpMethod.GET.toString())
        AuthHelper.authorize(builder, scopes: "org:jeff")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.FORBIDDEN.code
    }

    void "We need a scope at all to proceed"() {

        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/accounts/v2/accounts/abc123", HttpMethod.GET.toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.UNAUTHORIZED.code
    }
}
