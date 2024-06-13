package com.raidiam.trustframework.bank.services.admin;

import com.raidiam.trustframework.bank.CleanupSpecification;
import com.raidiam.trustframework.bank.TestRequestDataFactory;
import com.raidiam.trustframework.bank.services.WebhookAdminService;
import io.micronaut.test.extensions.spock.annotation.MicronautTest;
import spock.lang.Stepwise;

import javax.inject.Inject

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class WebhookAdminServiceSpec extends CleanupSpecification {
    @Inject
    WebhookAdminService webhookAdminService

    def "We can create and retrieve a webhook entity"() {
        when: "We can post a webhook entity"
        def clientId = UUID.randomUUID().toString()
        def createdWebhook = webhookAdminService.createWebhookUri(clientId, TestRequestDataFactory.createWebhook())

        then:
        createdWebhook != null
        createdWebhook.data != null
        createdWebhook.data.clientId == clientId
        createdWebhook.data.webhookUri == "https://web.conformance.directory.openbankingbrasil.org.br/test-mtls/a/obbsb"

        when: "We can get a webhook entity"
        def getWebhook = webhookAdminService.getWebhookByClientId(clientId)

        then:
        getWebhook != null
        getWebhook.data != null
        getWebhook.data.clientId == clientId

        getWebhook.data.webhookUri == "https://web.conformance.directory.openbankingbrasil.org.br/test-mtls/a/obbsb"
    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }
}
