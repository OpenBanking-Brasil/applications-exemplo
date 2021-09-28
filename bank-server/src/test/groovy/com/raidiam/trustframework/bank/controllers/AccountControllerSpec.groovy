package com.raidiam.trustframework.bank.controllers

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.raidiam.trustframework.bank.AuthHelper
import com.raidiam.trustframework.bank.models.generated.*
import com.raidiam.trustframework.bank.services.AccountsService
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.function.aws.proxy.MicronautLambdaContainerHandler
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpStatus
import io.micronaut.runtime.Micronaut
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import java.time.OffsetDateTime

@MicronautTest
class AccountControllerSpec extends Specification {

    private static Context lambdaContext = new MockLambdaContext()
    def mapper = new ObjectMapper()

    MicronautLambdaContainerHandler handler

    AccountsService mockService = Mock(AccountsService)

    Page<Level1Account> accountPage
    Level1Account accountReq

    def setup() {
        mapper.findAndRegisterModules();
        handler = new MicronautLambdaContainerHandler(Micronaut
                .build()
//                .properties(["micronaut.security.enabled": "true"])
                .singletons(mockService))

        accountReq = new Level1Account()

        Level2Account accountPriv = new Level2Account()
        accountPriv.identification("TestL2Account")
        accountPriv.name("TestL2Account")
        accountPriv.schemeName("TestL2Account")
        accountPriv.secondaryIdentification("TestL2Account")
        Level2Accounts accountsPriv = new Level2Accounts()
        accountsPriv.add(accountPriv)
        accountReq.setAccount(accountsPriv)


        Servicer servicer = new Servicer()
        servicer.setIdentification("ServicerId")
        servicer.setSchemeName("ServicerScheme")
        accountReq.setServicer(servicer)

        accountReq.setAccountSubType(AccountSubType.CHARGECARD)
        accountReq.setAccountType(AccountType.BUSINESS)
        accountReq.setCurrency("GBP")
        accountReq.setDescription("TestL1Account")
        accountReq.setMaturityDate(OffsetDateTime.now())
        accountReq.setNickname("TestL1Account")
        accountReq.setOpeningDate(OffsetDateTime.now())
        accountReq.setStatus(Level1AccountStatus.ENABLED)
        accountReq.setStatusUpdateDateTime(OffsetDateTime.now())
        accountReq.setSwitchStatus(Level1Account.SwitchStatusEnum.SWITCHCOMPLETED)

        accountPage = Page.of(List.of(accountReq), Pageable.unpaged(), 1)
    }

    def cleanup() {
        handler.close()
    }

    void "Admins can post an account"() {
        given:
        mockService.createAccount(_) >> accountReq

        String json = mapper.writeValueAsString(accountReq)
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/accounts/v1/accounts', HttpMethod.POST.toString()).body(json)
        AuthHelper.authorize(scopes: "org:admin", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.CREATED.code
        response.body
    }

    void "Users cannot post an account"() {
        given:
        mockService.createAccount(_) >> accountReq

        String json = mapper.writeValueAsString(accountReq)
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/accounts/v1/accounts', HttpMethod.POST.toString()).body(json)
        AuthHelper.authorize(scopes: "user.account", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.FORBIDDEN.code
    }

    void "we can get all accounts"() {
        given:

        mockService.getAccounts(_) >> accountPage

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/accounts/v1/accounts', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "user:account", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body
    }

    void "we can get an account"() {
        given:
        mockService.getAccount(_) >> accountPage

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/accounts/v1/accounts/abc123', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "user:account", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body
    }

    void "We need the correct scope to proceed"() {

        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/accounts/v1/accounts/abc123', HttpMethod.GET.toString())
        AuthHelper.authorize(builder, scopes: "org:nobody")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.FORBIDDEN.code

    }

    void "We need a known scope to proceed"() {

        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/accounts/v1/accounts/abc123', HttpMethod.GET.toString())
        AuthHelper.authorize(builder, scopes: "org:jeff")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.FORBIDDEN.code

    }

    void "We need a scope at all to proceed"() {

        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/accounts/v1/accounts/abc123', HttpMethod.GET.toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.UNAUTHORIZED.code

    }

}
