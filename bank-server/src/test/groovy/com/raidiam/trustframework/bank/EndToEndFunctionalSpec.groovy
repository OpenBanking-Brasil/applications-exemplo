package com.raidiam.trustframework.bank

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.Payload
import com.nimbusds.jose.PlainHeader
import com.nimbusds.jose.PlainObject
import com.raidiam.trustframework.bank.controllers.ConsentFactory
import com.raidiam.trustframework.bank.models.generated.*
import com.raidiam.trustframework.mockbank.models.generated.BusinessEntity
import com.raidiam.trustframework.mockbank.models.generated.BusinessEntityDocument
import com.raidiam.trustframework.mockbank.models.generated.CreateConsent
import com.raidiam.trustframework.mockbank.models.generated.LoggedUser
import com.raidiam.trustframework.mockbank.models.generated.LoggedUserDocument
import com.raidiam.trustframework.mockbank.models.generated.ResponseConsent
import com.raidiam.trustframework.mockbank.models.generated.ResponseConsentFull
import com.raidiam.trustframework.mockbank.models.generated.ResponseConsentFullData
import com.raidiam.trustframework.mockbank.models.generated.UpdateConsent
import com.raidiam.trustframework.mockbank.models.generated.UpdateConsentData
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import org.junit.ClassRule
import org.mockserver.junit.MockServerRule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.spock.Testcontainers
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Stepwise

import javax.inject.Inject
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
@Testcontainers
class EndToEndFunctionalSpec extends AbstractLocalStackSpec implements TestPropertyProvider {
    @Shared
    Logger log = LoggerFactory.getLogger(EndToEndFunctionalSpec.class)

    @ClassRule
    @Shared
    private MockServerRule mockserver = new MockServerRule(this)

    @Override
    Map<String, String> getProperties() {
        return [
                'somekey.somesubkey' : 'someothervalue',
        ]
    }

    @Shared
    @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)


    @Shared
    static mapper = new ObjectMapper()

    @Inject
    @Client('/')
    RxHttpClient client

    def setupSpec() {
        mapper.findAndRegisterModules()
    }

    def cleanupSpec () {

    }

    void "we can post a consent request" () {
        given:
        CreateConsent createConsent = ConsentFactory.createConsent()
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        PlainObject plainObject = new PlainObject(new PlainHeader(), new Payload([scope: "consents"]))
        def bearerToken = plainObject.serialize()
        ResponseConsent returnedConsent = client.toBlocking()
                    .retrieve(HttpRequest.POST('/consents/v1/consents', jsonRequest)
                    .header("Authorization", "Bearer ${bearerToken}")
                , ResponseConsent)


        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 3
    }

    void "we can update a consent request" () {
        given:
        CreateConsent createConsent = ConsentFactory.createConsent()
        String jsonRequest = mapper.writeValueAsString(createConsent)

        UpdateConsentData consentUpdateData = new UpdateConsentData()
        consentUpdateData.setStatus(UpdateConsentData.StatusEnum.AUTHORISED)
        UpdateConsent consentUpdate = new UpdateConsent()
        consentUpdate.setData(consentUpdateData)


        when:
        PlainObject plainObject = new PlainObject(new PlainHeader(), new Payload([scope: "op:consent"]))
        def bearerToken = plainObject.serialize()
        ResponseConsent returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST('/consents/v1/consents', jsonRequest)
                .header("Authorization", "Bearer ${bearerToken}")
                        , ResponseConsent)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 3

        when:
        String url = '/consents/v1/consents/' + returnedConsent.getData().getConsentId()
        String jsonUpdateRequest = mapper.writeValueAsString(consentUpdate)
        ResponseConsentFull returnedUpdateConsent = client.toBlocking().retrieve(HttpRequest.PUT(url, jsonUpdateRequest)
                .header("Authorization", "Bearer ${bearerToken}")
                , ResponseConsentFull)

        then:
        returnedUpdateConsent.data.status == ResponseConsentFullData.StatusEnum.AUTHORISED
    }
}
