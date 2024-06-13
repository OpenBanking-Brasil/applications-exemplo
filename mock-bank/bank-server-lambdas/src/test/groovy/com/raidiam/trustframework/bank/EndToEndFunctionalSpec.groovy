package com.raidiam.trustframework.bank

import com.nimbusds.jwt.SignedJWT
import com.raidiam.trustframework.bank.domain.AccountHolderEntity
import com.raidiam.trustframework.bank.enums.ContractTypeEnum
import com.raidiam.trustframework.bank.utils.BankLambdaUtils
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.context.annotation.Value
import io.micronaut.data.model.Pageable
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.apache.http.client.utils.URIBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Stepwise

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccountHolder
import static com.raidiam.trustframework.bank.controllers.ConsentFactory.*
import static com.raidiam.trustframework.mockbank.models.generated.EnumConsentPermissions.*

@Stepwise
@MicronautTest(transactional = false, environments = ["db-with-preload"])
@Testcontainers
class EndToEndFunctionalSpec extends FullCreateConsentFactory {
    @Shared
    Logger log = LoggerFactory.getLogger(EndToEndFunctionalSpec.class)

    @Shared
    AccountHolderEntity testAccountHolder
    @Shared
    String adminUrl

    @Value("\${mockbank.max-page-size}")
    int maxPageSize

    def setup() {
        if (runSetup) {
            testAccountHolder = accountHolderRepository.save(anAccountHolder())
            adminUrl = "/admin/customers/" + testAccountHolder.getAccountHolderId().toString()
            runSetup = false
        }
    }

    def cleanupSpec() {

    }

    void "we can post a consent request for our new user, then delete it"() {
        given:
        CreateConsent createConsent = createConsent(testAccountHolder.getDocumentIdentification(), testAccountHolder.getDocumentRel(), null)
        String jsonRequest = mapper.writeValueAsString(createConsent)
        def url = "/open-banking/consents/v2/consents"

        when:
        ResponseConsent returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST(url, jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsent)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 3

        when:
        ResponseConsentFull fullConsent = client.toBlocking()
                .retrieve(HttpRequest.GET("${url}/${returnedConsent.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${createToken("op:consent")}"),
                        ResponseConsentFull)

        then:
        fullConsent.getData().getSub() != null

        when:
        def delResponse = client.toBlocking().exchange(
                HttpRequest.DELETE("${url}/${returnedConsent.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${createToken("op:consent")}"))

        then:
        noExceptionThrown()
        delResponse.getStatus() == HttpStatus.NO_CONTENT

        when:
        ResponseConsentFull consentRejected = client.toBlocking()
                .retrieve(HttpRequest.GET("${url}/${returnedConsent.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${createToken("op:consent")}"),
                        ResponseConsentFull)

        then:
        consentRejected.getData() != null
        consentRejected.getData().getStatus() == EnumConsentStatus.REJECTED
    }

    void "we can update and get a consent request for our new user v1"() {
        given:
        def url = "/open-banking/consents/v2/consents"

        CreateConsent createConsent = createConsent(testAccountHolder.getDocumentIdentification(), testAccountHolder.getDocumentRel(), null)
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED))

        when:
        def bearerToken = createToken("op:consent")
        ResponseConsent returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST(url, mapper.writeValueAsString(createConsent))
                        .header("Authorization", "Bearer ${bearerToken}"),
                        ResponseConsent)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 3

        when:
        ResponseConsentFull returnedUpdateConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT("${url}/${returnedConsent.getData().getConsentId()}", mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${bearerToken}"),
                        ResponseConsentFull)

        then:
        returnedUpdateConsent.data.status == EnumConsentStatus.AUTHORISED

        when:
        ResponseConsent getConsent = client.toBlocking()
                .retrieve(HttpRequest.GET("${url}/${returnedConsent.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${bearerToken}"),
                        ResponseConsent)

        then:
        getConsent.data != null
        getConsent.data.consentId == returnedUpdateConsent.getData().getConsentId()
        getConsent.data.creationDateTime == returnedUpdateConsent.getData().getCreationDateTime()
        getConsent.data.status.toString() == returnedUpdateConsent.getData().getStatus().toString()
        getConsent.data.statusUpdateDateTime == returnedUpdateConsent.getData().getStatusUpdateDateTime()
        getConsent.data.permissions.stream()
                .map(c -> c.toString()).collect(Collectors.toSet())
                .containsAll(returnedUpdateConsent.getData().getPermissions().stream()
                        .map(c -> c.toString()).collect(Collectors.toSet()))
        getConsent.data.expirationDateTime == returnedUpdateConsent.getData().getExpirationDateTime()
        getConsent.data.transactionFromDateTime == returnedUpdateConsent.getData().getTransactionFromDateTime()
        getConsent.data.transactionToDateTime == returnedUpdateConsent.getData().getTransactionToDateTime()
    }

    void "We can simulate the multiple consents flow with a PARTIALLY_ACCEPTED status and move it to AUTHORISED"() {
        given:
        def createPaymentConsent = createPaymentConsentV4('87517400444', 'CPF')
        def jwtPayload = AwsProxyHelper.signPayload(createPaymentConsent)

        when:
        def returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST("/open-banking/payments/v4/consents/", jwtPayload)
                        .header("Authorization", "Bearer ${createToken("payments")}")
                        .header("x-idempotency-key", UUID.randomUUID().toString())
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString())
                        .header("Content-Type", "application/jwt")
                        .header("Accept", "application/jwt"),
                        String)

        def claimsPostData = mapper.writeValueAsString(SignedJWT.parse(returnedConsent).getJWTClaimsSet().getClaim("data"))
        def responsePost = mapper.readValue("{\"data\":" + claimsPostData + "}", ResponseCreatePaymentConsentV4)
        UpdatePaymentConsent consentUpdate = new UpdatePaymentConsent().data(new UpdatePaymentConsentData().status(UpdatePaymentConsentData.StatusEnum.PARTIALLY_ACCEPTED))

        def fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT('/open-banking/payments/v4/consents/' + responsePost.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${createToken("op:payments")}"),
                        ResponsePaymentConsentV4)

        then:
        noExceptionThrown()
        fullConsent.data != null
        fullConsent.data.status == EnumAuthorisationStatusType.PARTIALLY_ACCEPTED

        when:
        def partiallyAcceptedConsent = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/payments/v4/consents/"+responsePost.getData().getConsentId())
                        .header("Authorization", "Bearer ${createToken("payments")}")
                        .header("x-idempotency-key", UUID.randomUUID().toString())
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString())
                        .header("Content-Type", "application/jwt")
                        .header("Accept", "application/jwt"),
                        String)
        def claimsPartiallyAcceptedData = mapper.writeValueAsString(SignedJWT.parse(partiallyAcceptedConsent).getJWTClaimsSet().getClaim("data"))
        def responsePartiallyAccepted = mapper.readValue("{\"data\":" + claimsPartiallyAcceptedData + "}", ResponsePaymentConsentV4)

        then:
        noExceptionThrown()
        responsePartiallyAccepted.data != null
        responsePartiallyAccepted.data.status == EnumAuthorisationStatusType.PARTIALLY_ACCEPTED

        when:
        def paymentConsent = paymentConsentRepository.findByPaymentConsentId(fullConsent.data.consentId).get()
        paymentConsent.setCreationDateTime(Date.from(Instant.now().minusSeconds(180)))
        paymentConsentRepository.update(paymentConsent)

        def getConsent = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/payments/v4/consents/"+responsePost.getData().getConsentId())
                        .header("Authorization", "Bearer ${createToken("payments")}")
                        .header("x-idempotency-key", UUID.randomUUID().toString())
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString())
                        .header("Content-Type", "application/jwt")
                        .header("Accept", "application/jwt"),
                        String)
        def claimsGetData = mapper.writeValueAsString(SignedJWT.parse(getConsent).getJWTClaimsSet().getClaim("data"))
        def responseGet = mapper.readValue("{\"data\":" + claimsGetData + "}", ResponsePaymentConsentV4)

        then:
        noExceptionThrown()
        responseGet.data != null
        responseGet.data.status == EnumAuthorisationStatusType.AUTHORISED

    }

    void "we can rejected a consent v2"() {
        given:
        String url = "/open-banking/consents/v2/consents"
        def bearerToken = createToken("op:consent")

        // auto rejected after 60 min
        when:
        CreateConsentV2 createConsent1 = createConsentV2(testAccountHolder.getDocumentIdentification(), testAccountHolder.getDocumentRel(), null)
        ResponseConsentV2 returnedConsent1 = client.toBlocking()
                .retrieve(HttpRequest.POST(url, mapper.writeValueAsString(createConsent1))
                        .header("Authorization", "Bearer ${bearerToken}"),
                        ResponseConsentV2)

        then:
        returnedConsent1.getData().getStatus() == EnumConsentStatus.AWAITING_AUTHORISATION

        when:
        var consentEntity1 = consentRepository.findByConsentId(returnedConsent1.getData().getConsentId()).get()
        consentEntity1.setStatusUpdateDateTime(Date.from(Instant.now() - Duration.ofHours(1)))
        consentRepository.update(consentEntity1)

        UpdateConsent consentUpdate1 = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED))
        client.toBlocking()
                .retrieve(HttpRequest.PUT("${url}/" + returnedConsent1.getData().getConsentId(),
                        mapper.writeValueAsString(consentUpdate1)).header("Authorization", "Bearer ${bearerToken}"),
                        ResponseConsentFullV2)

        then:
        HttpClientResponseException e1 = thrown()
        e1.status == HttpStatus.BAD_REQUEST

        when:
        def response2 = client.toBlocking()
                .retrieve(HttpRequest.GET("${url}/${returnedConsent1.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsentReadV2)

        then:
        def rejection = response2.getData().getRejection()
        rejection.getRejectedBy() == EnumRejectedByV2.ASPSP
        rejection.getReason().getCode() == EnumReasonCodeV2.CONSENT_EXPIRED

        // customer manually rejected
        when:
        CreateConsentV2 createConsent2 = createConsentV2(testAccountHolder.getDocumentIdentification(), testAccountHolder.getDocumentRel(), null)
        UpdateConsent consentUpdate2 = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED))
        ResponseConsentV2 returnedConsent2 = client.toBlocking()
                .retrieve(HttpRequest.POST(url, mapper.writeValueAsString(createConsent2))
                        .header("Authorization", "Bearer ${bearerToken}"),
                        ResponseConsentV2)

        then:
        returnedConsent2.getData().getStatus() == EnumConsentStatus.AWAITING_AUTHORISATION

        when:
        client.toBlocking().retrieve(HttpRequest.DELETE("${url}/" + returnedConsent2.getData().getConsentId(),
                mapper.writeValueAsString(consentUpdate2)).header("Authorization", "Bearer ${bearerToken}"),
                HttpStatus)
        def responseConsentReadV2 = client.toBlocking().retrieve(HttpRequest.GET("${url}/" + returnedConsent2.getData().getConsentId())
                .header("Authorization", "Bearer ${createToken("consents")}"),
                ResponseConsentReadV2)

        then:
        def rejection2 = responseConsentReadV2.getData().getRejection()
        rejection2.getRejectedBy() == EnumRejectedByV2.USER
        rejection2.getReason().getCode() == EnumReasonCodeV2.CUSTOMER_MANUALLY_REJECTED

        // auto rejected after expiration date
        when:
        CreateConsentV2 createConsent3 = createConsentV2(testAccountHolder.getDocumentIdentification(), testAccountHolder.getDocumentRel(), null)
        UpdateConsent consentUpdate3 = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED))
        ResponseConsentV2 returnedConsent3 = client.toBlocking()
                .retrieve(HttpRequest.POST(url, mapper.writeValueAsString(createConsent3))
                        .header("Authorization", "Bearer ${bearerToken}"),
                        ResponseConsentV2)

        then:
        returnedConsent3.getData().getStatus() == EnumConsentStatus.AWAITING_AUTHORISATION

        when:
        var consentEntity3 = consentRepository.findByConsentId(returnedConsent3.getData().getConsentId()).get()
        consentEntity3.setExpirationDateTime(Date.from(Instant.now() - Duration.ofDays(1)))
        consentRepository.update(consentEntity3)
        client.toBlocking()
                .retrieve(HttpRequest.PUT("${url}/" + returnedConsent3.getData().getConsentId(),
                        mapper.writeValueAsString(consentUpdate3)).header("Authorization", "Bearer ${bearerToken}"),
                        ResponseConsentFullV2)

        then:
        HttpClientResponseException e = thrown()
        e.status == HttpStatus.BAD_REQUEST

        when:
        def response3 = client.toBlocking().retrieve(HttpRequest.GET("${url}/" + returnedConsent3.getData().getConsentId())
                .header("Authorization", "Bearer ${createToken("consents")}"),
                ResponseConsentReadV2)

        then:
        def rejection3 = response3.getData().getRejection()
        rejection3.getRejectedBy() == EnumRejectedByV2.ASPSP
        rejection3.getReason().getCode() == EnumReasonCodeV2.CONSENT_MAX_DATE_REACHED

        // customer manually revoked
        when:
        CreateConsentV2 createConsent4 = createConsentV2(testAccountHolder.getDocumentIdentification(), testAccountHolder.getDocumentRel(), null)
        UpdateConsent consentUpdate4 = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED))
        ResponseConsentV2 returnedConsent4 = client.toBlocking()
                .retrieve(HttpRequest.POST(url, mapper.writeValueAsString(createConsent4))
                        .header("Authorization", "Bearer ${bearerToken}"),
                        ResponseConsentV2)

        then:
        returnedConsent4.getData().getStatus() == EnumConsentStatus.AWAITING_AUTHORISATION

        when:
        ResponseConsentFullV2 returnedUpdateConsent4 = client.toBlocking()
                .retrieve(HttpRequest.PUT("${url}/" + returnedConsent4.getData().getConsentId(),
                        mapper.writeValueAsString(consentUpdate4)).header("Authorization", "Bearer ${bearerToken}"),
                        ResponseConsentFullV2)

        then:
        returnedUpdateConsent4.data.status == EnumConsentStatus.AUTHORISED

        when:
        client.toBlocking().retrieve(HttpRequest.DELETE("${url}/" + returnedConsent4.getData().getConsentId(),
                mapper.writeValueAsString(consentUpdate2)).header("Authorization", "Bearer ${bearerToken}"),
                HttpStatus)
        def response4 = client.toBlocking().retrieve(HttpRequest.GET("${url}/" + returnedConsent4.getData().getConsentId())
                .header("Authorization", "Bearer ${createToken("consents")}"),
                ResponseConsentReadV2)

        then:
        def rejection4 = response4.getData().getRejection()
        rejection4.getRejectedBy() == EnumRejectedByV2.USER
        rejection4.getReason().getCode() == EnumReasonCodeV2.CUSTOMER_MANUALLY_REVOKED

        when: //user can cancel (AWAITING_AUTHORISATION) consent while updating consent and Rejection, Reason will be set
        CreateConsentV2 createConsent5 = createConsentV2(testAccountHolder.getDocumentIdentification(), testAccountHolder.getDocumentRel(), null)
        def returnedConsent5 = client.toBlocking()
                .retrieve(HttpRequest.POST(url, mapper.writeValueAsString(createConsent5))
                        .header("Authorization", "Bearer ${bearerToken}"),
                        ResponseConsentV2)

        UpdateConsent consentUpdate5 = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.REJECTED))
        client.toBlocking()
                .retrieve(HttpRequest.PUT("${url}/" + returnedConsent5.getData().getConsentId(),
                        mapper.writeValueAsString(consentUpdate5)).header("Authorization", "Bearer ${bearerToken}"),
                        ResponseConsentFullV2)

        ResponseConsentReadV2 rejectedConsent = client.toBlocking()
                .retrieve(HttpRequest.GET("${url}/${returnedConsent5.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsentReadV2)

        then:
        rejectedConsent.getData().getRejection().getRejectedBy() == EnumRejectedByV2.USER
        rejectedConsent.getData().getRejection().getReason().getCode() == EnumReasonCodeV2.CUSTOMER_MANUALLY_REJECTED

        when: //user can cancel (AUTHORISED) consent while updating consent and Rejection, Reason will be set
        CreateConsentV2 createConsent6 = createConsentV2(testAccountHolder.getDocumentIdentification(), testAccountHolder.getDocumentRel(), null)
        def returnedConsent6 = client.toBlocking()
                .retrieve(HttpRequest.POST(url, mapper.writeValueAsString(createConsent6))
                        .header("Authorization", "Bearer ${bearerToken}"),
                        ResponseConsentV2)

        UpdateConsent consentUpdate6 = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED))
        client.toBlocking()
                .retrieve(HttpRequest.PUT("${url}/" + returnedConsent6.getData().getConsentId(),
                        mapper.writeValueAsString(consentUpdate6)).header("Authorization", "Bearer ${bearerToken}"),
                        ResponseConsentFullV2)

        UpdateConsent reject = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.REJECTED))
        client.toBlocking()
                .retrieve(HttpRequest.PUT("${url}/" + returnedConsent6.getData().getConsentId(),
                        mapper.writeValueAsString(reject)).header("Authorization", "Bearer ${bearerToken}"),
                        ResponseConsentFullV2)

        ResponseConsentReadV2 rejectedAuthConsent = client.toBlocking()
                .retrieve(HttpRequest.GET("${url}/${returnedConsent6.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsentReadV2)

        then:
        rejectedAuthConsent.getData().getRejection().getRejectedBy() == EnumRejectedByV2.USER
        rejectedAuthConsent.getData().getRejection().getReason().getCode() == EnumReasonCodeV2.CUSTOMER_MANUALLY_REVOKED
    }

    void "we can't get a personal consent v2 with business entity"(){
        given:
        CreateConsent createConsentRequest = createConsent('76109277673', 'CPF', [CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ,
                                                                                  RESOURCES_READ,CUSTOMERS_PERSONAL_ADITTIONALINFO_READ,ACCOUNTS_READ,
                                                                                  ACCOUNTS_BALANCES_READ,ACCOUNTS_OVERDRAFT_LIMITS_READ,ACCOUNTS_TRANSACTIONS_READ,
                                                                                  CREDIT_CARDS_ACCOUNTS_LIMITS_READ,CREDIT_CARDS_ACCOUNTS_READ,CREDIT_CARDS_ACCOUNTS_BILLS_READ,
                                                                                  CREDIT_CARDS_ACCOUNTS_TRANSACTIONS_READ,CREDIT_CARDS_ACCOUNTS_BILLS_TRANSACTIONS_READ,
                                                                                  LOANS_READ,LOANS_WARRANTIES_READ,LOANS_SCHEDULED_INSTALMENTS_READ,LOANS_PAYMENTS_READ,
                                                                                  FINANCINGS_READ,FINANCINGS_WARRANTIES_READ,FINANCINGS_SCHEDULED_INSTALMENTS_READ,
                                                                                  FINANCINGS_PAYMENTS_READ,UNARRANGED_ACCOUNTS_OVERDRAFT_READ,UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ,
                                                                                  UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ,UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ,
                                                                                  INVOICE_FINANCINGS_READ,INVOICE_FINANCINGS_WARRANTIES_READ,INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ,
                                                                                  INVOICE_FINANCINGS_PAYMENTS_READ])

        when:
        ResponseConsent returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST('/open-banking/consents/v2/consents', mapper.writeValueAsString(createConsentRequest))
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsent)

        then:
        HttpClientResponseException e = thrown()
        e.getStatus() == HttpStatus.BAD_REQUEST
    }
    void "we can get personal and business identifications for a preloaded user"() {
        given:
        CreateConsentV2 createConsentRequest = createConsentV2('76109277673', 'CPF', [CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ, RESOURCES_READ])
        createConsentRequest.data.setBusinessEntity(null)
        when:
        ResponseConsentV2 returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST('/open-banking/consents/v2/consents', mapper.writeValueAsString(createConsentRequest))
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsentV2)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 2

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED).sub("ralph.bragg@gmail.com").clientId('1234'))
        def updateToken = createToken("op:consent")
        log.info("OP consent token - {}", updateToken)
        ResponseConsentFullV2 fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT('/open-banking/consents/v2/consents/' + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFullV2)

        then:
        fullConsent.getData().getSub() != null

        when:
        def customersToken = createToken("customers consent:${fullConsent.getData().getConsentId().toString()}")
        log.info("OP consent token - {}", updateToken)
        ResponsePersonalCustomersIdentificationV2 identifications = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/customers/v2/personal/identifications")
                        .header("Authorization", "Bearer ${customersToken}"),
                        ResponsePersonalCustomersIdentificationV2)

        then:
        identifications != null
        identifications.getData() != null
        identifications.getData().get(0) != null
        identifications.getData().get(0).getSocialName() != null

        when:
        CreateConsent businessConsentRequest = createConsent('76109277673', 'CPF', [CUSTOMERS_BUSINESS_IDENTIFICATIONS_READ, RESOURCES_READ])
        ResponseConsentV2 returnedBusinessConsent = client.toBlocking()
                .retrieve(HttpRequest.POST('/open-banking/consents/v2/consents', mapper.writeValueAsString(businessConsentRequest))
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsentV2)

        then:
        returnedBusinessConsent.getData().getCreationDateTime() != null
        returnedBusinessConsent.getData().getPermissions().size() == 2

        when:
        UpdateConsent businessConsentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED).sub("ralph.bragg@gmail.com").clientId('1234'))
        def businessUpdateToken = createToken("op:consent")
        log.info("OP consent token - {}", businessUpdateToken)
        ResponseConsentFullV2 businessConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT('/open-banking/consents/v2/consents/' + returnedBusinessConsent.getData().getConsentId(), mapper.writeValueAsString(businessConsentUpdate))
                        .header("Authorization", "Bearer ${businessUpdateToken}"),
                        ResponseConsentFullV2)

        then:
        businessConsent.getData().getSub() != null

        when:
        def identificationsToken = createToken("customers consent:${businessConsent.getData().getConsentId().toString()}")
        ResponseBusinessCustomersIdentificationV2 businessIdentifications = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/customers/v2/business/identifications")
                        .header("Authorization", "Bearer ${identificationsToken}"),
                        ResponseBusinessCustomersIdentificationV2)

        then:
        businessIdentifications != null
        businessIdentifications.getData() != null
        businessIdentifications.getData().get(0) != null
        businessIdentifications.getData().get(0).getBusinessId() != null
    }

    void "we can get account transactions for a preloaded user"() {
        given:
        CreateConsentV2 createConsent = createConsentV2('76109277673', 'CPF', [ACCOUNTS_READ, ACCOUNTS_TRANSACTIONS_READ, RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        ResponseConsentV2 returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST("/open-banking/consents/v2/consents", jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsentV2)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 3

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234').linkedAccountIds(List.of('291e5a29-49ed-401f-a583-193caa7aceee')))
        def updateToken = createToken("op:consent")
        log.info("OP consent token - {}", updateToken)

        ResponseConsentFullV2 fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT("/open-banking/consents/v2/consents/" + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFullV2)

        then:
        fullConsent.getData().getSub() != null

        when:
        def transactionToken = createToken("accounts consent:${fullConsent.getData().getConsentId().toString()}")
        log.info("OP consent token - {}", updateToken)

        def uri = "/open-banking/accounts/v2/accounts/291e5a29-49ed-401f-a583-193caa7aceee/transactions?fromBookingDate=2021-01-01&toBookingDate=2023-01-01"
        ResponseAccountTransactionsV2 transactions = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${transactionToken}")
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString()),
                        ResponseAccountTransactionsV2)

        then:
        transactions != null
        transactions.getData() != null
        transactions.getData().size() == 25
        transactions.getData().get(0) != null
        transactions.getMeta() != null
        transactions.getLinks() != null
        transactions.getLinks().getSelf().endsWith("?page-size=${maxPageSize}&page=1&fromBookingDate=2021-01-01&toBookingDate=2023-01-01")

        when:
        def delResponse = client.toBlocking().exchange(
                HttpRequest.DELETE("/open-banking/consents/v2/consents/${fullConsent.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${updateToken}"))

        then:
        noExceptionThrown()
        delResponse.getStatus() == HttpStatus.NO_CONTENT

        when:
        client.toBlocking().exchange(
                HttpRequest.DELETE("/open-banking/consents/v2/consents/${fullConsent.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${updateToken}"))

        then:
        HttpClientResponseException e = thrown()
        e.getStatus() == HttpStatus.BAD_REQUEST
    }

    void "we can get credit card account limits, transactions and bills for a preloaded user"() {
        given:
        CreateConsentV2 createConsent = createConsentV2('76109277673', 'CPF',
                [CREDIT_CARDS_ACCOUNTS_READ, CREDIT_CARDS_ACCOUNTS_LIMITS_READ,
                 RESOURCES_READ, CREDIT_CARDS_ACCOUNTS_TRANSACTIONS_READ,
                 CREDIT_CARDS_ACCOUNTS_BILLS_READ, CREDIT_CARDS_ACCOUNTS_BILLS_TRANSACTIONS_READ])

        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        ResponseConsentV2 returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST("/open-banking/consents/v2/consents", jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsentV2)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 6

        when:
        ResponseCreditCardAccountsList cardAccountsList = client.toBlocking()
                .retrieve(HttpRequest.GET("/user/ralph.bragg@gmail.com/credit-card-accounts")
                        .header("Authorization", "Bearer ${createToken("openid")}"),
                        ResponseCreditCardAccountsList)

        then:
        cardAccountsList != null
        cardAccountsList.getData() != null
        cardAccountsList.getData().size() != 0
        cardAccountsList.getData().get(0) != null
        cardAccountsList.getData().get(0).getCreditCardAccountId() != null

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(
                new UpdateConsentData()
                        .status(UpdateConsentData.StatusEnum.AUTHORISED)
                        .sub("ralph.bragg@gmail.com")
                        .clientId('1234')
                        .linkedCreditCardAccountIds(List.of(cardAccountsList.getData().get(0).getCreditCardAccountId().toString())))
        ResponseConsentFullV2 fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT("/open-banking/consents/v2/consents/" + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${createToken("op:consent")}"),
                        ResponseConsentFullV2)
        then:
        fullConsent.getData().getSub() != null

        when:
        def limitsToken = createToken("credit-cards-accounts consent:${fullConsent.getData().getConsentId().toString()}")
        log.info("OP consent token - {}", limitsToken)
        def uri = "/open-banking/credit-cards-accounts/v2/accounts/${cardAccountsList.getData().get(0).getCreditCardAccountId()}/limits"
        ResponseCreditCardAccountsLimitsV2 limits = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${limitsToken}")
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString()),
                        ResponseCreditCardAccountsLimitsV2)

        then:
        noExceptionThrown()
        limits != null
        limits.getData() != null
        limits.getLinks() != null
        limits.getLinks().getSelf().endsWith(uri)

        when:
        uri = "/open-banking/credit-cards-accounts/v2/accounts/${cardAccountsList.getData().get(0).getCreditCardAccountId()}/bills?fromDueDate=2022-01-01&toDueDate=2023-01-01"
        def request = HttpRequest.GET(uri)
                .header("Authorization", "Bearer ${limitsToken}")
                .header("x-fapi-interaction-id", UUID.randomUUID().toString())
        ResponseCreditCardAccountsBillsV2 bills = client.toBlocking()
                .retrieve(request, ResponseCreditCardAccountsBillsV2)

        then:
        noExceptionThrown()
        bills != null
        bills.getData() != null
        bills.getData().size() == 1
        bills.getLinks() != null

        when:
        uri = "/open-banking/credit-cards-accounts/v2/accounts/" +
                "${cardAccountsList.getData().get(0).getCreditCardAccountId()}/bills/" +
                "${bills.getData().get(0).getBillId()}/transactions?fromTransactionDate=2022-01-01&toTransactionDate=2023-01-01"

        def billTransRequest = HttpRequest.GET(uri)
                .header("Authorization", "Bearer ${limitsToken}")
                .header("x-fapi-interaction-id", UUID.randomUUID().toString())
        ResponseCreditCardAccountsTransactionsV2 billTransactions = client.toBlocking()
                .retrieve(billTransRequest, ResponseCreditCardAccountsTransactionsV2)

        then:
        noExceptionThrown()
        billTransactions != null
        billTransactions.getData() != null
        billTransactions.getData().size() == maxPageSize
        billTransactions.getLinks() != null

        when:
        uri = "/open-banking/credit-cards-accounts/v2/accounts/" +
                "${cardAccountsList.getData().get(0).getCreditCardAccountId()}" +
                "/transactions?fromTransactionDate=2021-01-01&toTransactionDate=2023-01-01&page-size=1000&page=1"

        def transRequest = HttpRequest.GET(uri)
                .header("Authorization", "Bearer ${limitsToken}")
                .header("x-fapi-interaction-id", UUID.randomUUID().toString())
        ResponseCreditCardAccountsTransactionsV2 transactions = client.toBlocking()
                .retrieve(transRequest, ResponseCreditCardAccountsTransactionsV2)

        then:
        noExceptionThrown()
        transactions != null
        transactions.getData() != null
        transactions.getData().size() == maxPageSize
        transactions.getLinks() != null
        transactions.getLinks().getSelf().endsWith("?page-size=${maxPageSize}&page=1&fromTransactionDate=2021-01-01&toTransactionDate=2023-01-01")

        when:
        uri = "/open-banking/credit-cards-accounts/v2/accounts/" +
                "${cardAccountsList.getData().get(0).getCreditCardAccountId()}" +
                "/transactions?fromTransactionDate=2021-01-01&toTransactionDate=2023-01-01&page-size=1000&page=2"

        transRequest = HttpRequest.GET(uri)
                .header("Authorization", "Bearer ${limitsToken}")
                .header("x-fapi-interaction-id", UUID.randomUUID().toString())
        transactions = client.toBlocking()
                .retrieve(transRequest, ResponseCreditCardAccountsTransactionsV2)

        then:
        noExceptionThrown()
        transactions != null
        transactions.getData() != null
        transactions.getData().size() == maxPageSize
        transactions.getLinks() != null
        transactions.getLinks().getSelf().endsWith("?page-size=${maxPageSize}&page=2&fromTransactionDate=2021-01-01&toTransactionDate=2023-01-01")
    }

    void "unarranged overdrafts have the correct links"() {
        given:
        CreateConsentV2 createConsent = createConsentV2('76109277673', 'CPF', [LOANS_READ, LOANS_WARRANTIES_READ,
                                                                           LOANS_SCHEDULED_INSTALMENTS_READ, LOANS_PAYMENTS_READ,
                                                                           FINANCINGS_READ, FINANCINGS_WARRANTIES_READ,
                                                                           FINANCINGS_SCHEDULED_INSTALMENTS_READ, FINANCINGS_PAYMENTS_READ,
                                                                           UNARRANGED_ACCOUNTS_OVERDRAFT_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ,
                                                                           UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ,
                                                                           INVOICE_FINANCINGS_READ, INVOICE_FINANCINGS_WARRANTIES_READ,
                                                                           INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ, INVOICE_FINANCINGS_PAYMENTS_READ,
                                                                           RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        ResponseConsentV2 returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST('/open-banking/consents/v2/consents', jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsentV2)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 17

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(
                new UpdateConsentData()
                        .status(UpdateConsentData.StatusEnum.AUTHORISED)
                        .sub("ralph.bragg@gmail.com")
                        .clientId('1234')
                        .linkedUnarrangedOverdraftAccountIds(List.of('e5fae2fe-603b-42c9-ae7d-70fbaad1809c')))

        ResponseConsentFullV2 fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT('/open-banking/consents/v2/consents/' + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${createToken("op:consent")}"),
                        ResponseConsentFullV2)
        then:
        fullConsent.getData().getSub() != null

        when:
        ResponseUnarrangedAccountOverdraftContractListV2 overdrafts = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/unarranged-accounts-overdraft/v2/contracts")
                        .header("Authorization", "Bearer ${createToken("unarranged-accounts-overdraft consent:${fullConsent.getData().getConsentId().toString()}")}"),
                        ResponseUnarrangedAccountOverdraftContractListV2)

        then:
        noExceptionThrown()
        overdrafts != null
        overdrafts.getData() != null
        overdrafts.getData().size() == 1
        overdrafts.getLinks().getNext() == null
    }

    void "we can get personal financial relations for a preloaded user"() {
        given:
        CreateConsentV2 createConsent = createConsentV2('76109277673', 'CPF', [CUSTOMERS_PERSONAL_ADITTIONALINFO_READ, RESOURCES_READ])
        createConsent.data.setBusinessEntity(null)
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        ResponseConsentV2 returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST('/open-banking/consents/v2/consents', jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsentV2)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 2

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234').linkedAccountIds(List.of('291e5a29-49ed-401f-a583-193caa7aceee')))

        ResponseConsentFullV2 fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT('/open-banking/consents/v2/consents/' + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${createToken("op:consent")}"),
                        ResponseConsentFullV2)

        then:
        fullConsent.getData().getSub() != null

        when:
        ResponsePersonalCustomersFinancialRelationV2 personalRelation = client.toBlocking()
                .retrieve(HttpRequest.GET('/open-banking/customers/v2/personal/financial-relations')
                        .header("Authorization", "Bearer ${createToken("customers consent:${fullConsent.getData().getConsentId().toString()}")}"),
                        ResponsePersonalCustomersFinancialRelationV2)

        then:
        personalRelation != null
        personalRelation.getData() != null
        personalRelation.getData().getAccounts() != null
        personalRelation.getData().getAccounts().size() != 0
        personalRelation.getData().getAccounts().get(0) != null
    }

    void "we can get business financial relations for a preloaded user"() {
        given:
        CreateConsentV2 createConsent = createConsentV2('76109277673', 'CPF', [CUSTOMERS_BUSINESS_ADITTIONALINFO_READ, RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        ResponseConsentV2 returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST('/open-banking/consents/v2/consents', jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsentV2)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 2

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234').linkedAccountIds(List.of('291e5a29-49ed-401f-a583-193caa7aceee')))

        ResponseConsentFull fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT('/open-banking/consents/v2/consents/' + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${createToken("op:consent")}"),
                        ResponseConsentFull)

        then:
        fullConsent.getData().getSub() != null

        when:
        ResponseBusinessCustomersFinancialRelationV2 businessRelation = client.toBlocking()
                .retrieve(HttpRequest.GET('/open-banking/customers/v2/business/financial-relations')
                        .header("Authorization", "Bearer ${createToken("customers consent:${fullConsent.getData().getConsentId().toString()}")}"),
                        ResponseBusinessCustomersFinancialRelationV2)

        then:
        businessRelation != null
        businessRelation.getData() != null
        businessRelation.getData().getAccounts() != null
        businessRelation.getData().getAccounts().size() != 0
        businessRelation.getData().getAccounts().get(0) != null
    }

    void "error formatting is correct"() {
        given:
        CreateConsent createConsentRequest = createConsent('76109277673', 'CPF', [CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ, CUSTOMERS_BUSINESS_IDENTIFICATIONS_READ, RESOURCES_READ])

        when:
        client.toBlocking()
                .retrieve(HttpRequest.POST('/open-banking/consents/v2/consents', mapper.writeValueAsString(createConsentRequest))
                        .header("Authorization", "Bearer ${createToken("consents")}"))

        then:
        HttpClientResponseException ex = thrown()

        when:
        def response = ex.getResponse()

        then:
        response != null
        response.getBody() != null

        when:
        def body = response.getBody()
        then:
        body.isPresent()

        when:
        def errorResponse = mapper.readValue(body.get().toString(), ResponseError)
        then:
        errorResponse.getErrors() != null
        errorResponse.getErrors().size() != 0
        errorResponse.getErrors().get(0) != null

        when:
        def error = errorResponse.getErrors().get(0)

        then:
        error.getCode() != null
        error.getDetail() != null
    }

    void "we can POST and GET an account, balances, limits, transactions"() {
        given:
        CreateConsentV2 createConsent = createConsentV2(testAccountHolder.getDocumentIdentification(),
                testAccountHolder.getDocumentRel(), [ACCOUNTS_READ, ACCOUNTS_BALANCES_READ,
                                                     RESOURCES_READ, ACCOUNTS_OVERDRAFT_LIMITS_READ,
                                                     ACCOUNTS_TRANSACTIONS_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        ResponseConsentV2 returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST("/open-banking/consents/v2/consents", jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsentV2)

        def consentId = returnedConsent.getData().getConsentId()

        CreateAccount newAccount = TestRequestDataFactory.createAccount()
        String requestBody = mapper.writeValueAsString(newAccount)

        when:// we can post account
        ResponseAccount postAccountResponse = client.toBlocking()
                .retrieve(HttpRequest.POST("${adminUrl}/accounts", requestBody)
                        .header("Authorization", "Bearer ${createToken("op:admin")}")
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString()),
                        ResponseAccount)

        then:
        postAccountResponse.getData().getAccountId() != null
        postAccountResponse.getData().getAccountType() == newAccount.getData().getAccountType()

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234').linkedAccountIds(List.of(postAccountResponse.getData().getAccountId())))

        client.toBlocking()
                .retrieve(HttpRequest.PUT("/open-banking/consents/v2/consents/" + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${createToken("op:consent")}"),
                        ResponseConsentFull)
        // we can get account
        def accountToken = createToken("accounts consent:" + consentId)
        ResponseAccountIdentificationV2 getAccountResponse = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/accounts/v2/accounts/" + postAccountResponse.getData().getAccountId())
                        .header("Authorization", "Bearer ${accountToken}")
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString()),
                        ResponseAccountIdentificationV2)

        then:
        getAccountResponse.getData().getType().toString() == newAccount.getData().getAccountType().toString()
        getAccountResponse.getData().getSubtype().toString() == newAccount.getData().getAccountSubType().toString()
        getAccountResponse.getData().getNumber() == newAccount.getData().getNumber()
        getAccountResponse.getData().getCurrency() == newAccount.getData().getCurrency()
        getAccountResponse.getData().getCheckDigit() == newAccount.getData().getCheckDigit()
        getAccountResponse.getData().getCompeCode() == newAccount.getData().getCompeCode()
        getAccountResponse.getData().getBranchCode() == newAccount.getData().getBranchCode()

        when:// we can get account limits
        String uri = "/open-banking/accounts/v2/accounts/" + postAccountResponse.getData().getAccountId() + "/overdraft-limits"
        ResponseAccountOverdraftLimitsV2 getLimitsResponse = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${accountToken}")
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString()),
                        ResponseAccountOverdraftLimitsV2)
        then:
        Double.valueOf(getLimitsResponse.getData().getOverdraftContractedLimit().getAmount()) == newAccount.getData().getOverdraftContractedLimit()
        getLimitsResponse.getData().getOverdraftContractedLimit().getCurrency() == newAccount.getData().getOverdraftContractedLimitCurrency()
        Double.valueOf(getLimitsResponse.getData().getOverdraftUsedLimit().getAmount()) == newAccount.getData().getOverdraftUsedLimit()
        getLimitsResponse.getData().getOverdraftUsedLimit().getCurrency() == newAccount.getData().getOverdraftUsedLimitCurrency()
        Double.valueOf(getLimitsResponse.getData().getUnarrangedOverdraftAmount().getAmount()) == newAccount.getData().getUnarrangedOverdraftAmount()
        getLimitsResponse.getData().getUnarrangedOverdraftAmount().getCurrency() == newAccount.getData().getUnarrangedOverdraftAmountCurrency()

        when:// we can get account balances
        String balancesUri = "/open-banking/accounts/v2/accounts/" + postAccountResponse.getData().getAccountId() + "/balances"
        ResponseAccountBalancesV2 getBalancesResponse = client.toBlocking()
                .retrieve(HttpRequest.GET(balancesUri)
                        .header("Authorization", "Bearer ${accountToken}")
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString()),
                        ResponseAccountBalancesV2)
        then:
        Double.valueOf(getBalancesResponse.getData().getAvailableAmount().getAmount()) == newAccount.getData().getAvailableAmount()
        getBalancesResponse.getData().getAvailableAmount().getCurrency() == newAccount.getData().getAvailableAmountCurrency()
        Double.valueOf(getBalancesResponse.getData().getAutomaticallyInvestedAmount().getAmount()) == newAccount.getData().getAutomaticallyInvestedAmount()
        getBalancesResponse.getData().getAutomaticallyInvestedAmount().getCurrency() == newAccount.getData().getAutomaticallyInvestedAmountCurrency()
        Double.valueOf(getBalancesResponse.getData().getBlockedAmount().getAmount()) == newAccount.getData().getBlockedAmount()
        getBalancesResponse.getData().getBlockedAmount().getCurrency() == newAccount.getData().getBlockedAmountCurrency()


        when:// we can post account Transaction
        CreateAccountTransaction accountTransactionDto = TestRequestDataFactory.createAccountTransaction()

        ResponseAccountTransaction postTransactionResponse = client.toBlocking()
                .retrieve(HttpRequest.POST("${adminUrl}/accounts/" + postAccountResponse.getData().getAccountId() + '/transactions', mapper.writeValueAsString(accountTransactionDto))
                        .header("Authorization", "Bearer ${createToken("op:admin")}")
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString()),
                        ResponseAccountTransaction)

        then:
        postTransactionResponse.getData().getTransactionId() != null
        postTransactionResponse.getData().getTransactionName() == accountTransactionDto.getData().getTransactionName()

        when:// we can get account Transaction
        ResponseAccountTransactionsV2 getTransactionResponse = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/accounts/v2/accounts/" + postAccountResponse.getData().getAccountId() + '/transactions' + "/?fromBookingDate=" + postTransactionResponse.getData().getTransactionDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .header("Authorization", "Bearer ${accountToken}")
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString()),
                        ResponseAccountTransactionsV2)
        then:
        AccountTransactionsDataV2 transactionsData = getTransactionResponse.getData().first()
        transactionsData.getTransactionName() == accountTransactionDto.getData().getTransactionName()
        Double.valueOf(transactionsData.getTransactionAmount().getAmount()) == accountTransactionDto.getData().getAmount()
        transactionsData.getCompletedAuthorisedPaymentType().toString() == accountTransactionDto.getData().getCompletedAuthorisedPaymentType().toString()
        transactionsData.getCreditDebitType().toString() == accountTransactionDto.getData().getCreditDebitType().toString()
        transactionsData.getPartieBranchCode() == accountTransactionDto.getData().getPartieBranchCode()
        transactionsData.getPartieCheckDigit() == accountTransactionDto.getData().getPartieCheckDigit()
        transactionsData.getPartieCnpjCpf() == accountTransactionDto.getData().getPartieCnpjCpf()
        transactionsData.getPartieNumber() == accountTransactionDto.getData().getPartieNumber()
        transactionsData.getPartieCompeCode() == accountTransactionDto.getData().getPartieCompeCode()
        transactionsData.getPartiePersonType().toString() == accountTransactionDto.getData().getPartiePersonType().toString()
        transactionsData.getTransactionAmount().getCurrency() == accountTransactionDto.getData().getTransactionCurrency()
        transactionsData.getType().toString() == accountTransactionDto.getData().getType().toString()
    }

    void "we can POST and GET Credit Card - Account, Limit, Bills and Transactions"() {
        given:
        CreateConsentV2 createConsent = createConsentV2(testAccountHolder.getDocumentIdentification(),
                testAccountHolder.getDocumentRel(), [CREDIT_CARDS_ACCOUNTS_READ,
                                                     CREDIT_CARDS_ACCOUNTS_LIMITS_READ,
                                                     CREDIT_CARDS_ACCOUNTS_TRANSACTIONS_READ,
                                                     CREDIT_CARDS_ACCOUNTS_BILLS_READ,
                                                     CREDIT_CARDS_ACCOUNTS_BILLS_TRANSACTIONS_READ,
                                                     RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        ResponseConsentV2 returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST("/open-banking/consents/v2/consents", jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsentV2)

        def consentId = returnedConsent.getData().getConsentId()

        CreateCreditCardAccount newAccount = TestRequestDataFactory.creditCardAccount()

        when:// we can post Credit Card Account
        ResponseCreditCardAccount postAccountResponse = client.toBlocking()
                .retrieve(HttpRequest.POST("${adminUrl}/credit-cards-accounts", mapper.writeValueAsString(newAccount))
                        .header("Authorization", "Bearer ${createToken("op:admin")}")
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString()),
                        ResponseCreditCardAccount)

        then:
        postAccountResponse.getData().getCreditCardAccountId() != null

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234').linkedCreditCardAccountIds(List.of(postAccountResponse.getData().getCreditCardAccountId().toString())))

        client.toBlocking()
                .retrieve(HttpRequest.PUT("/open-banking/consents/v2/consents/" + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${createToken("op:consent")}"),
                        ResponseConsentFullV2)
        def accountToken = createToken("credit-cards-accounts consent:" + consentId)

        // we can get Credit Card Account
        ResponseCreditCardAccountsIdentificationV2 getAccountResponse = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/credit-cards-accounts/v2/accounts/" + postAccountResponse.getData().getCreditCardAccountId())
                        .header("Authorization", "Bearer ${accountToken}")
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString()),
                        ResponseCreditCardAccountsIdentificationV2)

        then:
        getAccountResponse.getData().getName() == newAccount.getData().getName()
        getAccountResponse.getData().getProductType().toString() == newAccount.getData().getProductType().toString()
        getAccountResponse.getData().getProductAdditionalInfo() == newAccount.getData().getProductAdditionalInfo()
        getAccountResponse.getData().getCreditCardNetwork().toString() == newAccount.getData().getCreditCardNetwork().toString()
        getAccountResponse.getData().getNetworkAdditionalInfo() == newAccount.getData().getNetworkAdditionalInfo()
        getAccountResponse.getData().getPaymentMethod() != null
        getAccountResponse.getData().getPaymentMethod().size() == newAccount.getData().getPaymentMethod().size()

        when:// we can post Credit Card Account Limits
        CreateCreditCardAccountLimits newLimitDto = TestRequestDataFactory.creditCardAccountLimitDto()
        ResponseCreditCardAccountLimits postLimitResponse = client.toBlocking()
                .retrieve(HttpRequest.POST("${adminUrl}/credit-cards-accounts/" + postAccountResponse.getData().getCreditCardAccountId().toString() + "/limits", mapper.writeValueAsString(newLimitDto))
                        .header("Authorization", "Bearer ${createToken("op:admin")}")
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString()),
                        ResponseCreditCardAccountLimits)

        then:
        postLimitResponse.getData().first() == newLimitDto.getData().first()

        when:// we can get Credit Card Account Limits
        ResponseCreditCardAccountsLimitsV2 getLimitsResponse = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/credit-cards-accounts/v2/accounts/" + postAccountResponse.getData().getCreditCardAccountId().toString() + "/limits")
                        .header("Authorization", "Bearer ${accountToken}")
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString()),
                        ResponseCreditCardAccountsLimitsV2)

        then:
        def accountLimits = getLimitsResponse.getData().first()
        accountLimits.getCreditLineLimitType().toString() == newLimitDto.getData().first().getCreditLineLimitType().toString()
        accountLimits.getConsolidationType().toString() == newLimitDto.getData().first().getConsolidationType().toString()
        accountLimits.getIdentificationNumber() == newLimitDto.getData().first().getIdentificationNumber()
        accountLimits.getLineName().toString() == newLimitDto.getData().first().getLineName().toString()
        accountLimits.getLineNameAdditionalInfo() == newLimitDto.getData().first().getLineNameAdditionalInfo()
        accountLimits.isIsLimitFlexible() == newLimitDto.getData().first().isIsLimitFlexible()
        Double.valueOf(accountLimits.getLimitAmount().getAmount()) == newLimitDto.getData().first().getLimitAmount()
        accountLimits.getLimitAmount().getCurrency() == newLimitDto.getData().first().getLimitAmountCurrency()
        Double.valueOf(accountLimits.getUsedAmount().getAmount()) == newLimitDto.getData().first().getUsedAmount()
        accountLimits.getUsedAmount().getCurrency() == newLimitDto.getData().first().getUsedAmountCurrency()
        accountLimits.getAvailableAmount().getCurrency() == newLimitDto.getData().first().getAvailableAmountCurrency()
        Double.valueOf(accountLimits.getAvailableAmount().getAmount()) == newLimitDto.getData().first().getAvailableAmount()

        when://POST Bills
        CreateCreditCardAccountBill newBillDto = TestRequestDataFactory.creditCardBillDto()
        ResponseCreditCardAccountBill postBillsResponse = client.toBlocking()
                .retrieve(HttpRequest.POST("${adminUrl}/credit-cards-accounts/" + postAccountResponse.getData().getCreditCardAccountId() + "/bills", mapper.writeValueAsString(newBillDto))
                        .header("Authorization", "Bearer ${createToken("op:admin")}")
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString()),
                        ResponseCreditCardAccountBill)

        then:
        postBillsResponse.getData().getBillId() != null

        when://GET Bills
        ResponseCreditCardAccountsBillsV2 getBillsResponse = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/credit-cards-accounts/v2/accounts/" + postAccountResponse.getData().getCreditCardAccountId() + "/bills" + "/?fromDueDate=" + postBillsResponse.getData().getDueDate())
                        .header("Authorization", "Bearer ${accountToken}"),
                        ResponseCreditCardAccountsBillsV2)

        then:
        def bill = getBillsResponse.getData().first()
        bill.getBillId() != null
        bill.getDueDate() == newBillDto.getData().getDueDate()
        Double.valueOf(bill.getBillTotalAmount().getAmount()) == newBillDto.getData().getBillTotalAmount()
        bill.getBillTotalAmount().getCurrency() == newBillDto.getData().getBillTotalAmountCurrency()
        Double.valueOf(bill.getBillMinimumAmount().getAmount()) == newBillDto.getData().getBillMinimumAmount()
        bill.getBillMinimumAmount().getCurrency() == newBillDto.getData().getBillMinimumAmountCurrency()
        bill.isIsInstalment() == newBillDto.getData().isInstalment()
        bill.getFinanceCharges() != null
        bill.getPayments() != null

        when://POST Transactions
        CreateCreditCardAccountTransactionData newTransactionsDto = TestRequestDataFactory.cardAccountTransactionDto()
        ResponseCreditCardAccountTransactionList postTransactionsResponse = client.toBlocking()
                .retrieve(HttpRequest.POST("${adminUrl}/credit-cards-accounts/" + postAccountResponse.getData().getCreditCardAccountId() + "/bills/" + postBillsResponse.getData().getBillId() + "/transactions", mapper.writeValueAsString(new CreateCreditCardAccountTransactionList().data(List.of(newTransactionsDto))))
                        .header("Authorization", "Bearer ${createToken("op:admin")}"),
                        ResponseCreditCardAccountTransactionList)


        then:
        postTransactionsResponse.getData().first().getTransactionId() != null

        when://GET ALL Transactions
        String getAllUrl = "/open-banking/credit-cards-accounts/v2/accounts/" + postAccountResponse.getData().getCreditCardAccountId() + "/transactions"
        ResponseCreditCardAccountsTransactionsV2 getTransactionsResponse = client.toBlocking()
                .retrieve(HttpRequest.GET(getAllUrl + "/?fromTransactionDate=" + postTransactionsResponse.getData().first().getTransactionDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE)+ "&payeeMCC=" + postTransactionsResponse.getData().first().getPayeeMCC())
                        .header("Authorization", "Bearer ${accountToken}")
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString()),
                        ResponseCreditCardAccountsTransactionsV2)

        def transaction = getTransactionsResponse.getData().first()

        then:
        transaction.getTransactionId() != null
        transaction.identificationNumber == newTransactionsDto.identificationNumber
        transaction.transactionName == newTransactionsDto.transactionName
        transaction.creditDebitType.toString() == newTransactionsDto.creditDebitType.toString()
        transaction.transactionType.toString() == newTransactionsDto.transactionType.toString()
        transaction.transactionalAdditionalInfo == newTransactionsDto.transactionalAdditionalInfo
        transaction.paymentType.toString() == newTransactionsDto.paymentType.toString()
        transaction.feeType.toString() == newTransactionsDto.feeType.toString()
        transaction.feeTypeAdditionalInfo == newTransactionsDto.feeTypeAdditionalInfo
        transaction.otherCreditsType.toString() == newTransactionsDto.otherCreditsType.toString()
        transaction.otherCreditsAdditionalInfo == newTransactionsDto.otherCreditsAdditionalInfo
        Double.valueOf(transaction.brazilianAmount.amount) == newTransactionsDto.brazilianAmount
        transaction.chargeNumber == newTransactionsDto.chargeNumber
        Double.valueOf(transaction.amount.amount) == newTransactionsDto.amount
        transaction.amount.currency == newTransactionsDto.currency
        transaction.billPostDate == newTransactionsDto.billPostDate
        transaction.payeeMCC == newTransactionsDto.payeeMCC

        when://GET Transactions by Bill id
        ResponseCreditCardAccountsTransactionsV2 getTransactionsByBillResponse = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/credit-cards-accounts/v2/accounts/" + postAccountResponse.getData().getCreditCardAccountId() + "/bills/" + postBillsResponse.getData().getBillId() + "/transactions" + "/?fromTransactionDate=" + transaction.getTransactionDateTime().substring(0, 10))
                        .header("Authorization", "Bearer ${accountToken}")
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString()),
                        ResponseCreditCardAccountsTransactionsV2)

        def transactionByBillId = getTransactionsByBillResponse.getData().first()

        then:
        transactionByBillId.getBillId() == transaction.getBillId()

        when://GET Transactions with param
        var param = "page-size=1&page=1"
        ResponseCreditCardAccountsTransactionsV2 getTransactionsResponseWithParam = client.toBlocking()
                .retrieve(HttpRequest.GET(getAllUrl + "/?fromTransactionDate=" + postTransactionsResponse.getData().first().getTransactionDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE)
                        + "&payeeMCC=" + postTransactionsResponse.getData().first().getPayeeMCC() + "&" + param)
                        .header("Authorization", "Bearer ${accountToken}")
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString()),
                        ResponseCreditCardAccountsTransactionsV2)

        then:
        getTransactionsResponseWithParam.getLinks().getSelf().contains(param)

        when://GET Transactions without param
        def defaultParam = "page-size=25&page=1"
        ResponseCreditCardAccountsTransactionsV2 getTransactionsResponseWithOutParam = client.toBlocking()
                .retrieve(HttpRequest.GET(getAllUrl + "/?fromTransactionDate=" + postTransactionsResponse.getData().first().getTransactionDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE)
                        + "&payeeMCC=" + postTransactionsResponse.getData().first().getPayeeMCC())
                        .header("Authorization", "Bearer ${accountToken}")
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString()),
                        ResponseCreditCardAccountsTransactionsV2)

        then:
        getTransactionsResponseWithOutParam.getLinks().getSelf().contains(defaultParam)
    }

    void "we can POST and GET Contracts v2"() {
        given:
        CreateConsentV2 createConsent = createConsentV2(testAccountHolder.getDocumentIdentification(),
                testAccountHolder.getDocumentRel(), [LOANS_READ, LOANS_WARRANTIES_READ,
                                                     LOANS_SCHEDULED_INSTALMENTS_READ, LOANS_PAYMENTS_READ,
                                                     FINANCINGS_READ, FINANCINGS_WARRANTIES_READ,
                                                     FINANCINGS_SCHEDULED_INSTALMENTS_READ, FINANCINGS_PAYMENTS_READ,
                                                     UNARRANGED_ACCOUNTS_OVERDRAFT_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ,
                                                     UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ,
                                                     INVOICE_FINANCINGS_READ, INVOICE_FINANCINGS_WARRANTIES_READ,
                                                     INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ, INVOICE_FINANCINGS_PAYMENTS_READ,
                                                     RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        ResponseConsentV2 returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST('/open-banking/consents/v2/consents', jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsentV2)

        CreateContract newContractDto = TestRequestDataFactory.createContract(EnumContractType.LOAN)

        when:// we can post Contracts
        String url = "${adminUrl}/${ContractTypeEnum.LOAN.toString()}"
        ResponseContract postContractsResponse = client.toBlocking()
                .retrieve(HttpRequest.POST(url, mapper.writeValueAsString(newContractDto))
                        .header("Authorization", "Bearer ${createToken("op:admin")}"),
                        ResponseContract)


        then:
        postContractsResponse.getData().getContractId() != null

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234').linkedLoanAccountIds(List.of(postContractsResponse.getData().getContractId().toString())))

        ResponseConsentFullV2 fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT('/open-banking/consents/v2/consents/' + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${createToken("op:consent")}"),
                        ResponseConsentFullV2)
        def contractsToken = createToken("loans consent:${fullConsent.getData().getConsentId().toString()}")

        // we can GET Contracts by Contract Id
        String getByIdUrl = '/open-banking/loans/v2/contracts/' + postContractsResponse.getData().getContractId()
        ResponseLoansContractV2 getLoansContractResponse = client.toBlocking()
                .retrieve(HttpRequest.GET(getByIdUrl)
                        .header("Authorization", "Bearer ${contractsToken}"),
                        ResponseLoansContractV2)


        then:
        getLoansContractResponse.getData().getDueDate() == newContractDto.getData().getDueDate()
        getLoansContractResponse.getData().getCurrency() == newContractDto.getData().getCurrency()
        getLoansContractResponse.getData().getProductType().toString() == newContractDto.getData().getProductType()
        getLoansContractResponse.getData().getAmortizationScheduled().name() == newContractDto.getData().getAmortizationScheduled().name()
        getLoansContractResponse.getData().getAmortizationScheduledAdditionalInfo() == newContractDto.getData().getAmortizationScheduledAdditionalInfo()
        Double.valueOf(getLoansContractResponse.getData().getContractAmount()) == newContractDto.getData().getContractAmount()
        getLoansContractResponse.getData().getContractDate() == newContractDto.getData().getContractDate()
        getLoansContractResponse.getData().getContractNumber() == newContractDto.getData().getContractNumber()
        getLoansContractResponse.getData().getFirstInstalmentDueDate() == newContractDto.getData().getFirstInstalmentDueDate()
        getLoansContractResponse.getData().getInstalmentPeriodicityAdditionalInfo() == newContractDto.getData().getInstalmentPeriodicityAdditionalInfo()
        getLoansContractResponse.getData().getIpocCode() == newContractDto.getData().getIpocCode()
        getLoansContractResponse.getData().getProductName() == newContractDto.getData().getProductName()
        getLoansContractResponse.getData().getProductSubType().toString() == newContractDto.getData().getProductSubType()
        getLoansContractResponse.getData().getSettlementDate() == newContractDto.getData().getSettlementDate()
        getLoansContractResponse.getData().getContractedFees() != null
        LoansContractedFeeV2 contractedFee = getLoansContractResponse.getData().getContractedFees().first()
        Double.valueOf(contractedFee.getFeeAmount()) == newContractDto.getData().getContractedFees().first().getFeeAmount()
        contractedFee.getFeeCode() == newContractDto.getData().getContractedFees().first().getFeeCode()
        contractedFee.getFeeName() == newContractDto.getData().getContractedFees().first().getFeeName()
        Double.valueOf(contractedFee.getFeeRate()) == newContractDto.getData().getContractedFees().first().getFeeRate()
        getLoansContractResponse.getData().getContractedFinanceCharges() != null
        LoansFinanceChargeV2 financeCharges = getLoansContractResponse.getData().getContractedFinanceCharges().first()
        financeCharges.getChargeAdditionalInfo() == newContractDto.getData().getContractedFinanceCharges().first().getChargeAdditionalInfo()
        financeCharges.getChargeRate().toDouble() == newContractDto.getData().getContractedFinanceCharges().first().getChargeRate()
        financeCharges.getChargeType().name() == newContractDto.getData().getContractedFinanceCharges().first().getChargeType().name()

        getLoansContractResponse.getData().getInterestRates() != null
        LoansContractInterestRateV2 interestRates = getLoansContractResponse.getData().getInterestRates().first()
        interestRates.getTaxType().toString() == newContractDto.getData().getInterestRates().first().getTaxType().toString()
        interestRates.getTaxPeriodicity().toString() == newContractDto.getData().getInterestRates().first().getTaxPeriodicity().toString()
        interestRates.getReferentialRateIndexerSubType().toString() == newContractDto.getData().getInterestRates().first().getReferentialRateIndexerSubType().toString()
        interestRates.getReferentialRateIndexerAdditionalInfo() == newContractDto.getData().getInterestRates().first().getReferentialRateIndexerAdditionalInfo()
        interestRates.getPreFixedRate().toDouble() == newContractDto.getData().getInterestRates().first().getPreFixedRate()
        interestRates.getPostFixedRate().toDouble() == newContractDto.getData().getInterestRates().first().getPostFixedRate()
        interestRates.getInterestRateType().toString() == newContractDto.getData().getInterestRates().first().getInterestRateType().toString()
        interestRates.getCalculation().name() == newContractDto.getData().getInterestRates().first().getCalculation().name()
        interestRates.getAdditionalInfo() == newContractDto.getData().getInterestRates().first().getAdditionalInfo()
        interestRates.getReferentialRateIndexerType().toString() == newContractDto.getData().getInterestRates().first().getReferentialRateIndexerType().toString()

        when:// we can GET Contracts Payments
        String getPaymentsUrl = '/open-banking/loans/v2/contracts/' + postContractsResponse.getData().getContractId() + '/payments'
        ResponseLoansPaymentsV2 getPaymentsResponse = client.toBlocking()
                .retrieve(HttpRequest.GET(getPaymentsUrl)
                        .header("Authorization", "Bearer ${contractsToken}"),
                        ResponseLoansPaymentsV2)


        then:
        getPaymentsResponse.getData().getPaidInstalments().intValue() == newContractDto.getData().getPaidInstalments().intValue()
        getPaymentsResponse.getData().getContractOutstandingBalance().toDouble() == newContractDto.getData().getContractOutstandingBalance()

        getPaymentsResponse.getData().getReleases() != null
        def responseReleasesDto = getPaymentsResponse.getData().getReleases().first()
        def newReleasesDto = newContractDto.getData().getReleases().stream()
                .filter { (it.getInstalmentId() == responseReleasesDto.getInstalmentId()) }
                .findFirst().get()
        responseReleasesDto.getPaidAmount().toDouble() == newReleasesDto.getPaidAmount()
        responseReleasesDto.getPaidDate() == newReleasesDto.getPaidDate()
        responseReleasesDto.getCurrency() == newReleasesDto.getCurrency()
        def overParcelCharges = responseReleasesDto.getOverParcel().getCharges().first()
        overParcelCharges.getChargeType().name() == newReleasesDto.getOverParcelCharges().first().getChargeType().name()
        overParcelCharges.getChargeAdditionalInfo() == newReleasesDto.getOverParcelCharges().first().getChargeAdditionalInfo()
        overParcelCharges.getChargeAmount().toDouble() == newReleasesDto.getOverParcelCharges().first().getChargeAmount()
        def overParcelFees = responseReleasesDto.getOverParcel().getFees().first()
        overParcelFees.getFeeName() == newReleasesDto.getOverParcelFees().first().getFeeName()
        overParcelFees.getFeeCode() == newReleasesDto.getOverParcelFees().first().getFeeCode()
        overParcelFees.getFeeAmount().toDouble() == newReleasesDto.getOverParcelFees().first().getFeeAmount()

        when:// we can GET Contracts scheduled-instalments
        String getInstalmentsUrl = '/open-banking/loans/v2/contracts/' + postContractsResponse.getData().getContractId() + '/scheduled-instalments'
        ResponseLoansInstalmentsV2 getInstalmentsResponse = client.toBlocking()
                .retrieve(HttpRequest.GET(getInstalmentsUrl)
                        .header("Authorization", "Bearer ${contractsToken}"),
                        ResponseLoansInstalmentsV2)


        then:
        getInstalmentsResponse.getData().getTotalNumberOfInstalments().intValue() == newContractDto.getData().getTotalNumberOfInstalments().intValue()
        getInstalmentsResponse.getData().getTypeContractRemaining().name() == newContractDto.getData().getTypeContractRemaining().name()
        getInstalmentsResponse.getData().getTypeNumberOfInstalments().name() == newContractDto.getData().getTypeNumberOfInstalments().name()
        getInstalmentsResponse.getData().getContractRemainingNumber().intValue() == newContractDto.getData().getContractRemainingNumber().intValue()
        getInstalmentsResponse.getData().getPastDueInstalments().intValue() == newContractDto.getData().getPastDueInstalments().intValue()
        getInstalmentsResponse.getData().getDueInstalments().intValue() == newContractDto.getData().getDueInstalments().intValue()
        getInstalmentsResponse.getData().getPaidInstalments().intValue() == newContractDto.getData().getPaidInstalments().intValue()

        getInstalmentsResponse.getData().getBalloonPayments() != null
        def balloonPayment = getInstalmentsResponse.getData().getBalloonPayments().first()
        def contractBalloonPayment = newContractDto.getData().getBalloonPayments().stream()
                .filter { (BankLambdaUtils.formatAmountV2(it.getAmount().toDouble()) == BankLambdaUtils.formatAmountV2(balloonPayment.getAmount().getAmount().toDouble())) }
                .findFirst()
                .get()
        balloonPayment.getDueDate() == contractBalloonPayment.getDueDate()
        balloonPayment.getAmount().getCurrency() == contractBalloonPayment.getCurrency()

        when:// we can post warranties
        List<ContractWarrantiesData> newWarranties = List.of(TestRequestDataFactory.createWarranties())
        String warrantiesUrl = "${adminUrl}/loans/${postContractsResponse.getData().getContractId()}/warranties"
        ResponseContractWarranties postWarrantiesResponse = client.toBlocking()
                .retrieve(HttpRequest.POST(warrantiesUrl, mapper.writeValueAsString(new ContractWarranties().data(newWarranties)))
                        .header("Authorization", "Bearer ${createToken("op:admin")}"),
                        ResponseContractWarranties)

        then:
        !postWarrantiesResponse.getData().isEmpty()

        when:// we can GET Contracts Warranties
        String getWarrantiesUrl = '/open-banking/loans/v2/contracts/' + postContractsResponse.getData().getContractId() + '/warranties'
        ResponseLoansWarrantiesV2 getWarrantiesResponse = client.toBlocking()
                .retrieve(HttpRequest.GET(getWarrantiesUrl)
                        .header("Authorization", "Bearer ${contractsToken}"),
                        ResponseLoansWarrantiesV2)

        then:
        getWarrantiesResponse.getData().first().getCurrency() == newWarranties.first().getCurrency()
        getWarrantiesResponse.getData().first().getWarrantyAmount().toDouble() == newWarranties.first().getWarrantyAmount()
        getWarrantiesResponse.getData().first().getWarrantySubType().name() == newWarranties.first().getWarrantySubType()
        getWarrantiesResponse.getData().first().getWarrantyType().name() == newWarranties.first().getWarrantyType()
    }

    def "we can POST and GET personal identifications"() {
        given:
        CreatePersonalIdentification newPersonalIdentificationsDto = TestRequestDataFactory.createPersonalIdentifications()
        CreateConsentV2 createConsent = createConsentV2(testAccountHolder.getDocumentIdentification(),
                testAccountHolder.getDocumentRel(), [CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ, RESOURCES_READ])
        createConsent.data.businessEntity(null)
        String jsonRequest = mapper.writeValueAsString(createConsent)

        ResponseConsentV2 returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST('/open-banking/consents/v2/consents', jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsentV2)

        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234'))

        ResponseConsentFullV2 fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT('/open-banking/consents/v2/consents/' + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${createToken("op:consent")}"),
                        ResponseConsentFullV2)

        when:// we can post personal identifications
        def customersToken = createToken("customers consent:${fullConsent.getData().getConsentId().toString()}")
        String url = "${adminUrl}/personal/identifications"
        ResponsePersonalIdentification postResponse = client.toBlocking()
                .retrieve(HttpRequest.POST(url, mapper.writeValueAsString(newPersonalIdentificationsDto))
                        .header("Authorization", "Bearer ${createToken("op:admin")}"),
                        ResponsePersonalIdentification)

        then:
        postResponse.getData() != null


        when:// we can get personal identifications
        ResponsePersonalCustomersIdentificationV2 getResponse = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/customers/v2/personal/identifications")
                        .header("Authorization", "Bearer ${customersToken}"),
                        ResponsePersonalCustomersIdentificationV2)

        then:
        def personalIdentification = getResponse.getData().first()
        personalIdentification.getBrandName() == newPersonalIdentificationsDto.getData().getBrandName()
        personalIdentification.getBirthDate() == newPersonalIdentificationsDto.getData().getBirthDate()
        personalIdentification.getCivilName() == newPersonalIdentificationsDto.getData().getCivilName()
        personalIdentification.getUpdateDateTime() != null
        personalIdentification.getMaritalStatusAdditionalInfo() == newPersonalIdentificationsDto.getData().getMaritalStatusAdditionalInfo()
        personalIdentification.getMaritalStatusCode() == newPersonalIdentificationsDto.getData().getMaritalStatusCode()
        personalIdentification.getSex().toString() == newPersonalIdentificationsDto.getData().getSex().toString()

        def documents = personalIdentification.getDocuments()
        documents.getPassport().getCountry() == newPersonalIdentificationsDto.getData().getPassportCountry()
        documents.getPassport().getExpirationDate() == newPersonalIdentificationsDto.getData().getPassportExpirationDate()
        documents.getPassport().getIssueDate() == newPersonalIdentificationsDto.getData().getPassportIssueDate()
        documents.getPassport().getNumber() == newPersonalIdentificationsDto.getData().getPassportNumber()
        documents.getCpfNumber() == newPersonalIdentificationsDto.getData().getCpfNumber()
        !personalIdentification.getCompaniesCnpj().isEmpty()
        personalIdentification.getCompaniesCnpj().containsAll(newPersonalIdentificationsDto.getData().getCompanyCnpj())
        !personalIdentification.getOtherDocuments().isEmpty()
//        def otherDocuments = personalIdentification.getOtherDocuments().first()
//        otherDocuments.getAdditionalInfo() == newPersonalIdentificationsDto.getData().getOtherDocuments().first().getAdditionalInfo()
//        otherDocuments.getCheckDigit() == newPersonalIdentificationsDto.getData().getOtherDocuments().first().getCheckDigit()
//        otherDocuments.getType().toString() == newPersonalIdentificationsDto.getData().getOtherDocuments().first().getType().toString()
//        otherDocuments.getNumber() == newPersonalIdentificationsDto.getData().getOtherDocuments().first().getNumber()
//        otherDocuments.getExpirationDate() == newPersonalIdentificationsDto.getData().getOtherDocuments().first().getExpirationDate()
//        otherDocuments.getTypeAdditionalInfo() == newPersonalIdentificationsDto.getData().getOtherDocuments().first().getTypeAdditionalInfo()
        !personalIdentification.getNationality().isEmpty()
        !personalIdentification.getFiliation().isEmpty()
        personalIdentification.getContacts() != null
        !personalIdentification.getContacts().getPostalAddresses().isEmpty()
        !personalIdentification.getContacts().getEmails().isEmpty()
        !personalIdentification.getContacts().getPhones().isEmpty()
    }

    def "we can POST and GET personal financial-relations"() {
        given:
        PersonalFinancialRelations newPersonalFinancialRelations = TestRequestDataFactory
                .createPersonalFinancialRelations()
        CreateConsentV2 createConsent = createConsentV2(testAccountHolder.getDocumentIdentification(),
                testAccountHolder.getDocumentRel(), [CUSTOMERS_PERSONAL_ADITTIONALINFO_READ, RESOURCES_READ])
        createConsent.data.businessEntity(null)
        String jsonRequest = mapper.writeValueAsString(createConsent)

        ResponseConsentV2 returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST('/open-banking/consents/v2/consents', jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsentV2)

        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234'))

        ResponseConsentFullV2 fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT('/open-banking/consents/v2/consents/' + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${createToken("op:consent")}"),
                        ResponseConsentFullV2)

        when:// we can post personal financial-relations
        def customersToken = createToken("customers consent:${fullConsent.getData().getConsentId().toString()}")
        PersonalFinancialRelations postResponse = client.toBlocking()
                .retrieve(HttpRequest.POST("${adminUrl}/personal/financial-relations", mapper.writeValueAsString(newPersonalFinancialRelations))
                        .header("Authorization", "Bearer ${createToken("op:admin")}"),
                        PersonalFinancialRelations)

        then:
        postResponse != null
        postResponse.getData().getAccountHolderId() != null

        when:// we can get personal financial-relations
        ResponsePersonalCustomersFinancialRelationV2 getResponse = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/customers/v2/personal/financial-relations")
                        .header("Authorization", "Bearer ${customersToken}"),
                        ResponsePersonalCustomersFinancialRelationV2)

        then:
        getResponse.getData().getStartDate() != null
        !getResponse.getData().getProductsServicesType().isEmpty()
        !getResponse.getData().getProcurators().isEmpty()
        getResponse.getData().getProductsServicesTypeAdditionalInfo() == newPersonalFinancialRelations.getData().getProductsServicesTypeAdditionalInfo()
    }

    def "we can POST and GET personal qualifications"() {
        given:
        PersonalQualifications newPersonalQualifications = TestRequestDataFactory.createPersonalQualifications()
        CreateConsentV2 createConsent = createConsentV2(testAccountHolder.getDocumentIdentification(),
                testAccountHolder.getDocumentRel(), [CUSTOMERS_PERSONAL_ADITTIONALINFO_READ, RESOURCES_READ])
        createConsent.data.businessEntity(null)
        String jsonRequest = mapper.writeValueAsString(createConsent)

        ResponseConsentV2 returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST('/open-banking/consents/v2/consents', jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsentV2)

        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234'))

        ResponseConsentFullV2 fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT('/open-banking/consents/v2/consents/' + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${createToken("op:consent")}"),
                        ResponseConsentFullV2)

        when:// we can post personal qualifications
        def customersToken = createToken("customers consent:${fullConsent.getData().getConsentId().toString()}")
        PersonalQualifications postResponse = client.toBlocking()
                .retrieve(HttpRequest.POST("${adminUrl}/personal/qualifications", mapper.writeValueAsString(newPersonalQualifications))
                        .header("Authorization", "Bearer ${createToken("op:admin")}"),
                        PersonalQualifications)

        then:
        postResponse != null
        postResponse.getData().getAccountHolderId() != null

        when:// we can put personal qualifications
        ResponsePersonalCustomersQualificationV2 getResponse = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/customers/v2/personal/qualifications").header("Authorization", "Bearer ${customersToken}"),
                        ResponsePersonalCustomersQualificationV2)

        then:
        getResponse.getData().getCompanyCnpj() == newPersonalQualifications.getData().getCompanyCnpj()
        getResponse.getData().getOccupationCode().toString() == newPersonalQualifications.getData().getOccupationCode().toString()
        getResponse.getData().getOccupationDescription() == newPersonalQualifications.getData().getOccupationDescription()
        Double.valueOf(getResponse.getData().getInformedIncome().getAmount().getAmount()) == newPersonalQualifications.getData().getInformedIncomeAmount()
        getResponse.getData().getInformedIncome().getAmount().getCurrency() == newPersonalQualifications.getData().getInformedIncomeCurrency()
        getResponse.getData().getInformedIncome().getDate().toString() == newPersonalQualifications.getData().getInformedIncomeDate()
        getResponse.getData().getInformedIncome().getFrequency().toString() == newPersonalQualifications.getData().getInformedIncomeFrequency().toString()
        Double.valueOf(getResponse.getData().getInformedPatrimony().getAmount().getAmount()) == newPersonalQualifications.getData().getInformedPatrimonyAmount()
        getResponse.getData().getInformedPatrimony().getAmount().getCurrency() == newPersonalQualifications.getData().getInformedPatrimonyCurrency()
        getResponse.getData().getInformedPatrimony().getYear() == newPersonalQualifications.getData().getInformedPatrimonyYear()
    }

    def "we can POST and GET business identifications"() {
        given:
        CreateBusinessIdentification newBusinessIdentifications = TestRequestDataFactory.createBusinessIdentifications()
        CreateConsentV2 createConsent = createConsentV2(testAccountHolder.getDocumentIdentification(),
                testAccountHolder.getDocumentRel(), [CUSTOMERS_BUSINESS_IDENTIFICATIONS_READ, RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        ResponseConsentV2 returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST('/open-banking/consents/v2/consents', jsonRequest)
                        .header("Authorization", "Bearer ${createToken("op:consent")}"),
                        ResponseConsentV2)

        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234'))

        client.toBlocking()
                .retrieve(HttpRequest.PUT('/open-banking/consents/v2/consents/' + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${createToken("op:consent")}"),
                        ResponseConsentFullV2)

        when:// we can post business identifications
        ResponseBusinessIdentification postResponse = client.toBlocking()
                .retrieve(HttpRequest.POST("${adminUrl}/business/identifications", mapper.writeValueAsString(newBusinessIdentifications))
                        .header("Authorization", "Bearer ${createToken("op:admin")}"),
                        ResponseBusinessIdentification)

        then:
        postResponse.getData() != null
        postResponse.getData().getBusinessIdentificationsId() != null

        when:// we can get business identifications
        def getCustomersToken = createToken("customers consent:" + returnedConsent.getData().getConsentId())
        ResponseBusinessCustomersIdentificationV2 getResponse = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/customers/v2/business/identifications").header("Authorization", "Bearer ${getCustomersToken}"),
                        ResponseBusinessCustomersIdentificationV2)

        then:
        def identificationData = getResponse.getData().first()
        identificationData.getBrandName() == newBusinessIdentifications.getData().getBrandName()
        identificationData.getCompanyName() == newBusinessIdentifications.getData().getCompanyName()
        identificationData.getTradeName() == newBusinessIdentifications.getData().getTradeName()
        identificationData.getIncorporationDate() == newBusinessIdentifications.getData().getIncorporationDate()
        identificationData.getCnpjNumber() == newBusinessIdentifications.getData().getCnpjNumber()
        !identificationData.getCompaniesCnpj().isEmpty()
        identificationData.getCompaniesCnpj().containsAll(newBusinessIdentifications.getData().getCompanyCnpjNumber())
        !identificationData.getOtherDocuments().isEmpty()
        !identificationData.getParties().isEmpty()
        identificationData.getContacts() != null
        !identificationData.getContacts().getPostalAddresses().isEmpty()
        !identificationData.getContacts().getEmails().isEmpty()
        !identificationData.getContacts().getPhones().isEmpty()
    }

    def "we can POST and GET business financial-relations"() {
        given:
        BusinessFinancialRelations newBusinessFinancialRelations = TestRequestDataFactory.createBusinessFinancialRelations()
        CreateConsentV2 createConsent = createConsentV2(testAccountHolder.getDocumentIdentification(),
                testAccountHolder.getDocumentRel(), [CUSTOMERS_BUSINESS_ADITTIONALINFO_READ, RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        ResponseConsentV2 returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST('/open-banking/consents/v2/consents', jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsentV2)

        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234'))

        ResponseConsentFullV2 fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT('/open-banking/consents/v2/consents/' + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${createToken("op:consent")}"),
                        ResponseConsentFullV2)

        when:// we can post business financial-relations
        def customersToken = createToken("customers consent:${fullConsent.getData().getConsentId().toString()}")
        BusinessFinancialRelations postResponse = client.toBlocking()
                .retrieve(HttpRequest.POST("${adminUrl}/business/financial-relations", mapper.writeValueAsString(newBusinessFinancialRelations))
                        .header("Authorization", "Bearer ${createToken("op:admin")}"),
                        BusinessFinancialRelations)

        then:
        postResponse.getData() != null
        postResponse.getData().getAccountHolderId() != null

        when:// we can get business financial-relations
        ResponseBusinessCustomersFinancialRelationV2 getResponse = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/customers/v2/business/financial-relations")
                        .header("Authorization", "Bearer ${customersToken}"),
                        ResponseBusinessCustomersFinancialRelationV2)

        then:
        getResponse.getData().getStartDate() != null
        !getResponse.getData().getProductsServicesType().isEmpty()
        !getResponse.getData().getProcurators().isEmpty()
    }

    def "we can POST and GET business qualifications"() {
        given:
        BusinessQualifications newBusinessQualifications = TestRequestDataFactory.createBusinessQualifications()
        CreateConsentV2 createConsent = createConsentV2(testAccountHolder.getDocumentIdentification(),
                testAccountHolder.getDocumentRel(), [CUSTOMERS_BUSINESS_ADITTIONALINFO_READ, RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        ResponseConsent returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST('/open-banking/consents/v2/consents', jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsent)

        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234'))

        ResponseConsentFull fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT('/open-banking/consents/v2/consents/' + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${createToken("op:consent")}"),
                        ResponseConsentFull)

        when:// we can post business qualifications
        def customersToken = createToken("customers consent:${fullConsent.getData().getConsentId().toString()}")
        BusinessQualifications postResponse = client.toBlocking()
                .retrieve(HttpRequest.POST("${adminUrl}/business/qualifications", mapper.writeValueAsString(newBusinessQualifications))
                        .header("Authorization", "Bearer ${createToken("op:admin")}"),
                        BusinessQualifications)

        then:
        postResponse.getData() != null
        postResponse.getData().getAccountHolderId() != null

        when:// we can get business qualifications
        ResponseBusinessCustomersQualificationV2 getResponse = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/customers/v2/business/qualifications")
                        .header("Authorization", "Bearer ${customersToken}"),
                        ResponseBusinessCustomersQualificationV2)

        then:
        getResponse.getData().getUpdateDateTime() != null

        getResponse.getData().getInformedRevenue().getFrequency().toString() == newBusinessQualifications.getData().getInformedRevenueFrequency().toString()
        getResponse.getData().getInformedRevenue().getFrequencyAdditionalInfo() == newBusinessQualifications.getData().getInformedRevenueFrequencyAdditionalInfo()
        Double.valueOf(getResponse.getData().getInformedRevenue().getAmount().getAmount()) == newBusinessQualifications.getData().getInformedRevenueAmount()
        getResponse.getData().getInformedRevenue().getAmount().getCurrency() == newBusinessQualifications.getData().getInformedRevenueCurrency()
        getResponse.getData().getInformedRevenue().getYear().intValue() == newBusinessQualifications.getData().getInformedRevenueYear().intValue()
        Double.valueOf(getResponse.getData().getInformedPatrimony().getAmount().getAmount()) == newBusinessQualifications.getData().getInformedPatrimonyAmount()
        getResponse.getData().getInformedPatrimony().getAmount().getCurrency() == newBusinessQualifications.getData().getInformedPatrimonyCurrency()
        getResponse.getData().getInformedPatrimony().getDate().toString() == newBusinessQualifications.getData().getInformedPatrimonyDate()
        !getResponse.getData().getEconomicActivities().isEmpty()
    }

    void "we can get bank Fixed Incomes for a preloaded investment"() {
        given:
        CreateConsentV2 createConsent = createConsentV2('76109277673', 'CPF', [BANK_FIXED_INCOMES_READ,
                                                                           CREDIT_FIXED_INCOMES_READ, FUNDS_READ, VARIABLE_INCOMES_READ,
                                                                           TREASURE_TITLES_READ, RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        ResponseConsentV2 returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST("/open-banking/consents/v2/consents/", jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsentV2)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 6

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234').linkedAccountIds(List.of('291e5a29-49ed-401f-a583-193caa7aceee')))
        def updateToken = createToken("op:consent")
        log.info("OP consent token - {}", updateToken)

        ResponseConsentFullV2 fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT("/open-banking/consents/v2/consents/" + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFullV2)

        then:
        fullConsent.getData().getSub() != null

        when:
        def token = createToken("bank-fixed-incomes consent:${fullConsent.getData().getConsentId().toString()}")
        log.info("OP consent token - {}", updateToken)

        def uri = "/open-banking/bank-fixed-incomes/v1/investments/"
        ResponseBankFixedIncomesProductList investments = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${token}"),
                        ResponseBankFixedIncomesProductList)
        then:
        investments != null
        investments.getData() != null
        investments.getData().size() == 25
        investments.getData().get(0) != null
        investments.getData().get(0).getInvestmentId() != null
        investments.getMeta() != null
        investments.getMeta().getTotalRecords() == 61
        investments.getMeta().getTotalPages() == 3
        investments.getLinks() != null
        investments.getLinks().getSelf() == "https://mockbank.com/api${uri}?page-size=25&page=1"
        investments.getLinks().getFirst() == "https://mockbank.com/api${uri}?page-size=25&page=1"
        investments.getLinks().getNext() == "https://mockbank.com/api${uri}?page-size=25&page=2"
        investments.getLinks().getLast() == "https://mockbank.com/api${uri}?page-size=25&page=3"

        when:
        def delResponse = client.toBlocking().exchange(
                HttpRequest.DELETE("/open-banking/consents/v2/consents/${fullConsent.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${updateToken}"))

        then:
        noExceptionThrown()
        delResponse.getStatus() == HttpStatus.NO_CONTENT
    }

    void "we can get bank Fixed Incomes Balances for a preloaded investment"() {
        given:
        CreateConsent createConsent = createConsent('76109277673', 'CPF', [BANK_FIXED_INCOMES_READ,
                                                                           CREDIT_FIXED_INCOMES_READ, FUNDS_READ, VARIABLE_INCOMES_READ,
                                                                           TREASURE_TITLES_READ, RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        ResponseConsent returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST("/open-banking/consents/v2/consents/", jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsent)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 6

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234').linkedAccountIds(List.of('291e5a29-49ed-401f-a583-193caa7aceee')))
        def updateToken = createToken("op:consent")
        log.info("OP consent token - {}", updateToken)

        ResponseConsentFull fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT("/open-banking/consents/v2/consents/" + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFull)

        then:
        fullConsent.getData().getSub() != null

        when:
        def token = createToken("bank-fixed-incomes consent:${fullConsent.getData().getConsentId().toString()}")
        log.info("OP consent token - {}", updateToken)

        def uri = "/open-banking/bank-fixed-incomes/v1/investments/e1561120-ed09-42a8-a94e-f19c62e0826f/balances"
        ResponseBankFixedIncomesBalances balances = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${token}"),
                        ResponseBankFixedIncomesBalances)
        then:
        balances != null
        balances.getData() != null

        when:
        def delResponse = client.toBlocking().exchange(
                HttpRequest.DELETE("/open-banking/consents/v2/consents/${fullConsent.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${updateToken}"))

        then:
        noExceptionThrown()
        delResponse.getStatus() == HttpStatus.NO_CONTENT
    }

    void "we can get bank Fixed Incomes current transactions for a preloaded investment"() {
        given:
        CreateConsent createConsent = createConsent('76109277673', 'CPF', [BANK_FIXED_INCOMES_READ,
                                                                           CREDIT_FIXED_INCOMES_READ, FUNDS_READ, VARIABLE_INCOMES_READ,
                                                                           TREASURE_TITLES_READ, RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        ResponseConsent returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST("/open-banking/consents/v2/consents/", jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsent)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 6

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234').linkedAccountIds(List.of('291e5a29-49ed-401f-a583-193caa7aceee')))
        def updateToken = createToken("op:consent")
        log.info("OP consent token - {}", updateToken)

        ResponseConsentFull fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT("/open-banking/consents/v2/consents/" + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFull)

        then:
        fullConsent.getData().getSub() != null

        when:
        def token = createToken("bank-fixed-incomes consent:${fullConsent.getData().getConsentId().toString()}")
        log.info("OP consent token - {}", updateToken)


        def baseUri = "/open-banking/bank-fixed-incomes/v1/investments/e1561120-ed09-42a8-a94e-f19c62e0826f/transactions-current"
        def dateParams = "fromTransactionDate=${LocalDate.now().minusDays(3).toString()}&toTransactionDate=${LocalDate.now().toString()}"
        def uri = "${baseUri}?${dateParams}"
        ResponseBankFixedIncomesTransactions balances = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${token}"),
                        ResponseBankFixedIncomesTransactions)
        then:
        balances != null
        balances.getData() != null
        balances.getData().size() > 2
        balances.getLinks().getSelf() == "https://mockbank.com/api${baseUri}?page-size=25&page=1&${dateParams}"

        when:
        def delResponse = client.toBlocking().exchange(
                HttpRequest.DELETE("/open-banking/consents/v2/consents/${fullConsent.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${updateToken}"))

        then:
        noExceptionThrown()
        delResponse.getStatus() == HttpStatus.NO_CONTENT
    }


    void "we can get bank Fixed Incomes transactions for a preloaded investment"() {
        given:
        CreateConsent createConsent = createConsent('76109277673', 'CPF', [BANK_FIXED_INCOMES_READ,
                                                                           CREDIT_FIXED_INCOMES_READ, FUNDS_READ, VARIABLE_INCOMES_READ,
                                                                           TREASURE_TITLES_READ, RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        ResponseConsent returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST("/open-banking/consents/v2/consents/", jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsent)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 6

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234').linkedAccountIds(List.of('291e5a29-49ed-401f-a583-193caa7aceee')))
        def updateToken = createToken("op:consent")
        log.info("OP consent token - {}", updateToken)

        ResponseConsentFull fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT("/open-banking/consents/v2/consents/" + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFull)

        then:
        fullConsent.getData().getSub() != null

        when:
        def token = createToken("bank-fixed-incomes consent:${fullConsent.getData().getConsentId().toString()}")
        log.info("OP consent token - {}", updateToken)


        def baseUri = "/open-banking/bank-fixed-incomes/v1/investments/e1561120-ed09-42a8-a94e-f19c62e0826f/transactions"
        def dateParams = "fromTransactionDate=${LocalDate.now().minusYears(1).toString()}&toTransactionDate=${LocalDate.now().toString()}"
        def uri = baseUri + "?" + dateParams
        ResponseBankFixedIncomesTransactions balances = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${token}"),
                        ResponseBankFixedIncomesTransactions)
        then:
        balances != null
        balances.getData() != null
        balances.getData().size() > 2
        balances.getLinks().getSelf() == "https://mockbank.com/api${baseUri}?page-size=25&page=1&${dateParams}"

        when:
        def delResponse = client.toBlocking().exchange(
                HttpRequest.DELETE("/open-banking/consents/v2/consents/${fullConsent.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${updateToken}"))

        then:
        noExceptionThrown()
        delResponse.getStatus() == HttpStatus.NO_CONTENT
    }

    void "we can get credit Fixed Incomes for a preloaded investment"() {
        given:
        CreateConsent createConsent = createConsent('76109277673', 'CPF', [BANK_FIXED_INCOMES_READ,
                                                                           CREDIT_FIXED_INCOMES_READ, FUNDS_READ, VARIABLE_INCOMES_READ,
                                                                           TREASURE_TITLES_READ, RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        ResponseConsent returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST("/open-banking/consents/v2/consents/", jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsent)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 6

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234').linkedAccountIds(List.of('291e5a29-49ed-401f-a583-193caa7aceee')))
        def updateToken = createToken("op:consent")
        log.info("OP consent token - {}", updateToken)

        ResponseConsentFull fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT("/open-banking/consents/v2/consents/" + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFull)

        then:
        fullConsent.getData().getSub() != null

        when:
        def token = createToken("credit-fixed-incomes consent:${fullConsent.getData().getConsentId().toString()}")
        log.info("OP consent token - {}", updateToken)

        def uri = "/open-banking/credit-fixed-incomes/v1/investments/"
        ResponseCreditFixedIncomesProductList investments = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${token}"),
                        ResponseCreditFixedIncomesProductList)
        then:
        investments != null
        investments.getData() != null
        investments.getData().size() == 25
        investments.getData().get(0) != null
        investments.getData().get(0).getInvestmentId() != null
        investments.getMeta() != null
        investments.getMeta().getTotalRecords() == 61
        investments.getMeta().getTotalPages() == 3
        investments.getLinks() != null
        investments.getLinks().getSelf() == "https://mockbank.com/api${uri}?page-size=25&page=1"
        investments.getLinks().getFirst() == "https://mockbank.com/api${uri}?page-size=25&page=1"
        investments.getLinks().getNext() == "https://mockbank.com/api${uri}?page-size=25&page=2"
        investments.getLinks().getLast() == "https://mockbank.com/api${uri}?page-size=25&page=3"


        when:
        def delResponse = client.toBlocking().exchange(
                HttpRequest.DELETE("/open-banking/consents/v2/consents/${fullConsent.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${updateToken}"))

        then:
        noExceptionThrown()
        delResponse.getStatus() == HttpStatus.NO_CONTENT
    }

    void "we can get credit Fixed Incomes Balances for a preloaded investment"() {
        given:
        CreateConsent createConsent = createConsent('76109277673', 'CPF', [BANK_FIXED_INCOMES_READ,
                                                                           CREDIT_FIXED_INCOMES_READ, FUNDS_READ, VARIABLE_INCOMES_READ,
                                                                           TREASURE_TITLES_READ, RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        ResponseConsent returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST("/open-banking/consents/v2/consents/", jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsent)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 6

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234').linkedAccountIds(List.of('291e5a29-49ed-401f-a583-193caa7aceee')))
        def updateToken = createToken("op:consent")
        log.info("OP consent token - {}", updateToken)

        ResponseConsentFull fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT("/open-banking/consents/v2/consents/" + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFull)

        then:
        fullConsent.getData().getSub() != null

        when:
        def token = createToken("credit-fixed-incomes consent:${fullConsent.getData().getConsentId().toString()}")
        log.info("OP consent token - {}", updateToken)

        def uri = "/open-banking/credit-fixed-incomes/v1/investments/22276676-8264-452c-bf4d-cd3bf17b057f/balances"
        ResponseCreditFixedIncomesBalances balances = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${token}"),
                        ResponseCreditFixedIncomesBalances)
        then:
        balances != null
        balances.getData() != null

        when:
        def delResponse = client.toBlocking().exchange(
                HttpRequest.DELETE("/open-banking/consents/v2/consents/${fullConsent.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${updateToken}"))

        then:
        noExceptionThrown()
        delResponse.getStatus() == HttpStatus.NO_CONTENT
    }

    void "we can get credit Fixed Incomes current transactions for a preloaded investment"() {
        given:
        CreateConsent createConsent = createConsent('76109277673', 'CPF', [BANK_FIXED_INCOMES_READ,
                                                                           CREDIT_FIXED_INCOMES_READ, FUNDS_READ, VARIABLE_INCOMES_READ,
                                                                           TREASURE_TITLES_READ, RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        ResponseConsent returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST("/open-banking/consents/v2/consents/", jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsent)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 6

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234').linkedAccountIds(List.of('291e5a29-49ed-401f-a583-193caa7aceee')))
        def updateToken = createToken("op:consent")
        log.info("OP consent token - {}", updateToken)

        ResponseConsentFull fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT("/open-banking/consents/v2/consents/" + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFull)

        then:
        fullConsent.getData().getSub() != null

        when:
        def token = createToken("credit-fixed-incomes consent:${fullConsent.getData().getConsentId().toString()}")
        log.info("OP consent token - {}", updateToken)


        def baseUri = "/open-banking/credit-fixed-incomes/v1/investments/22276676-8264-452c-bf4d-cd3bf17b057f/transactions-current"
        def dateParams = "fromTransactionDate=${LocalDate.now().minusDays(3).toString()}&toTransactionDate=${LocalDate.now().toString()}"
        def uri = baseUri + "?" + dateParams
        ResponseCreditFixedIncomesTransactions balances = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${token}"),
                        ResponseCreditFixedIncomesTransactions)
        then:
        balances != null
        balances.getData() != null
        balances.getData().size() > 2
        balances.getLinks().getSelf() == "https://mockbank.com/api${baseUri}?page-size=25&page=1&${dateParams}"

        when:
        def delResponse = client.toBlocking().exchange(
                HttpRequest.DELETE("/open-banking/consents/v2/consents/${fullConsent.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${updateToken}"))

        then:
        noExceptionThrown()
        delResponse.getStatus() == HttpStatus.NO_CONTENT
    }

    void "we can get funds for a preloaded investment"() {
        given:
        CreateConsent createConsent = createConsent('76109277673', 'CPF', [BANK_FIXED_INCOMES_READ,
                                                                           CREDIT_FIXED_INCOMES_READ, FUNDS_READ, VARIABLE_INCOMES_READ,
                                                                           TREASURE_TITLES_READ, RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        ResponseConsent returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST("/open-banking/consents/v2/consents/", jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsent)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 6

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234').linkedAccountIds(List.of('291e5a29-49ed-401f-a583-193caa7aceee')))
        def updateToken = createToken("op:consent")
        log.info("OP consent token - {}", updateToken)

        ResponseConsentFull fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT("/open-banking/consents/v2/consents/" + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFull)

        then:
        fullConsent.getData().getSub() != null

        when:
        def token = createToken("funds consent:${fullConsent.getData().getConsentId().toString()}")
        log.info("OP consent token - {}", updateToken)

        def uri = "/open-banking/funds/v1/investments/"
        ResponseFundsProductList investments = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${token}"),
                        ResponseFundsProductList)
        then:
        investments != null
        investments.getData() != null
        investments.getData().size() == 25
        investments.getData().get(0) != null
        investments.getData().get(0).getInvestmentId() != null
        investments.getMeta() != null
        investments.getMeta().getTotalRecords() == 61
        investments.getMeta().getTotalPages() == 3
        investments.getLinks() != null
        investments.getLinks().getSelf() == "https://mockbank.com/api${uri}?page-size=25&page=1"
        investments.getLinks().getFirst() == "https://mockbank.com/api${uri}?page-size=25&page=1"
        investments.getLinks().getNext() == "https://mockbank.com/api${uri}?page-size=25&page=2"
        investments.getLinks().getLast() == "https://mockbank.com/api${uri}?page-size=25&page=3"

        when:
        def delResponse = client.toBlocking().exchange(
                HttpRequest.DELETE("/open-banking/consents/v2/consents/${fullConsent.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${updateToken}"))

        then:
        noExceptionThrown()
        delResponse.getStatus() == HttpStatus.NO_CONTENT
    }

    void "we can get funds Balances for a preloaded investment"() {
        given:
        CreateConsent createConsent = createConsent('76109277673', 'CPF', [BANK_FIXED_INCOMES_READ,
                                                                           CREDIT_FIXED_INCOMES_READ, FUNDS_READ, VARIABLE_INCOMES_READ,
                                                                           TREASURE_TITLES_READ, RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        ResponseConsent returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST("/open-banking/consents/v2/consents/", jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsent)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 6

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234').linkedAccountIds(List.of('291e5a29-49ed-401f-a583-193caa7aceee')))
        def updateToken = createToken("op:consent")
        log.info("OP consent token - {}", updateToken)

        ResponseConsentFull fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT("/open-banking/consents/v2/consents/" + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFull)

        then:
        fullConsent.getData().getSub() != null

        when:
        def token = createToken("funds consent:${fullConsent.getData().getConsentId().toString()}")
        log.info("OP consent token - {}", updateToken)

        def uri = "/open-banking/funds/v1/investments/c0826748-22b6-432d-9b3f-a7e5a876e0bf/balances"
        ResponseFundsBalances balances = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${token}"),
                        ResponseFundsBalances)
        then:
        balances != null
        balances.getData() != null

        when:
        def delResponse = client.toBlocking().exchange(
                HttpRequest.DELETE("/open-banking/consents/v2/consents/${fullConsent.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${updateToken}"))

        then:
        noExceptionThrown()
        delResponse.getStatus() == HttpStatus.NO_CONTENT
    }

    void "we can get funds current transactions for a preloaded investment"() {
        given:
        CreateConsent createConsent = createConsent('76109277673', 'CPF', [BANK_FIXED_INCOMES_READ,
                                                                           CREDIT_FIXED_INCOMES_READ, FUNDS_READ, VARIABLE_INCOMES_READ,
                                                                           TREASURE_TITLES_READ, RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        ResponseConsent returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST("/open-banking/consents/v2/consents/", jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsent)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 6

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234').linkedAccountIds(List.of('291e5a29-49ed-401f-a583-193caa7aceee')))
        def updateToken = createToken("op:consent")
        log.info("OP consent token - {}", updateToken)

        ResponseConsentFull fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT("/open-banking/consents/v2/consents/" + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFull)

        then:
        fullConsent.getData().getSub() != null

        when:
        def token = createToken("funds consent:${fullConsent.getData().getConsentId().toString()}")
        log.info("OP consent token - {}", updateToken)


        def baseUri = "/open-banking/funds/v1/investments/c0826748-22b6-432d-9b3f-a7e5a876e0bf/transactions-current"
        def dateParams = "fromTransactionConversionDate=${LocalDate.now().minusDays(3).toString()}&toTransactionConversionDate=${LocalDate.now().toString()}"
        def uri = baseUri + "?" + dateParams
        ResponseFundsTransactions transactions = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${token}"),
                        ResponseFundsTransactions)
        then:
        transactions != null
        transactions.getData() != null
        transactions.getData().size() > 2
        transactions.getLinks().getSelf() == "https://mockbank.com/api${baseUri}?page-size=25&page=1&${dateParams}"

        when:
        def delResponse = client.toBlocking().exchange(
                HttpRequest.DELETE("/open-banking/consents/v2/consents/${fullConsent.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${updateToken}"))

        then:
        noExceptionThrown()
        delResponse.getStatus() == HttpStatus.NO_CONTENT
    }

    void "we can get treasure titles for a preloaded investment"() {
        given:
        CreateConsent createConsent = createConsent('76109277673', 'CPF', [BANK_FIXED_INCOMES_READ,
                                                                           CREDIT_FIXED_INCOMES_READ, FUNDS_READ, VARIABLE_INCOMES_READ,
                                                                           TREASURE_TITLES_READ, RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        ResponseConsent returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST("/open-banking/consents/v2/consents/", jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsent)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 6

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234').linkedAccountIds(List.of('291e5a29-49ed-401f-a583-193caa7aceee')))
        def updateToken = createToken("op:consent")
        log.info("OP consent token - {}", updateToken)

        ResponseConsentFull fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT("/open-banking/consents/v2/consents/" + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFull)

        then:
        fullConsent.getData().getSub() != null

        when:
        def token = createToken("treasure-titles consent:${fullConsent.getData().getConsentId().toString()}")
        log.info("OP consent token - {}", updateToken)

        def uri = "/open-banking/treasure-titles/v1/investments/"
        ResponseTreasureTitlesProductList investments = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${token}"),
                        ResponseTreasureTitlesProductList)
        then:
        investments != null
        investments.getData() != null
        investments.getData().size() == 25
        investments.getData().get(0) != null
        investments.getData().get(0).getInvestmentId() != null
        investments.getMeta() != null
        investments.getMeta().getTotalRecords() == 61
        investments.getMeta().getTotalPages() == 3
        investments.getLinks() != null
        investments.getLinks().getSelf() == "https://mockbank.com/api${uri}?page-size=25&page=1"
        investments.getLinks().getFirst() == "https://mockbank.com/api${uri}?page-size=25&page=1"
        investments.getLinks().getNext() == "https://mockbank.com/api${uri}?page-size=25&page=2"
        investments.getLinks().getLast() == "https://mockbank.com/api${uri}?page-size=25&page=3"

        when:
        def delResponse = client.toBlocking().exchange(
                HttpRequest.DELETE("/open-banking/consents/v2/consents/${fullConsent.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${updateToken}"))

        then:
        noExceptionThrown()
        delResponse.getStatus() == HttpStatus.NO_CONTENT
    }

    void "we can get treasure titles Balances for a preloaded investment"() {
        given:
        CreateConsent createConsent = createConsent('76109277673', 'CPF', [BANK_FIXED_INCOMES_READ,
                                                                           CREDIT_FIXED_INCOMES_READ, FUNDS_READ, VARIABLE_INCOMES_READ,
                                                                           TREASURE_TITLES_READ, RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        ResponseConsent returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST("/open-banking/consents/v2/consents/", jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsent)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 6

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234').linkedAccountIds(List.of('291e5a29-49ed-401f-a583-193caa7aceee')))
        def updateToken = createToken("op:consent")
        log.info("OP consent token - {}", updateToken)

        ResponseConsentFull fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT("/open-banking/consents/v2/consents/" + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFull)

        then:
        fullConsent.getData().getSub() != null

        when:
        def token = createToken("treasure-titles consent:${fullConsent.getData().getConsentId().toString()}")
        log.info("OP consent token - {}", updateToken)

        def uri = "/open-banking/treasure-titles/v1/investments/a5ae963d-1156-4911-ad66-59cd079afaab/balances"
        ResponseTreasureTitlesBalances balances = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${token}"),
                        ResponseTreasureTitlesBalances)
        then:
        balances != null
        balances.getData() != null

        when:
        def delResponse = client.toBlocking().exchange(
                HttpRequest.DELETE("/open-banking/consents/v2/consents/${fullConsent.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${updateToken}"))

        then:
        noExceptionThrown()
        delResponse.getStatus() == HttpStatus.NO_CONTENT
    }

    void "we can get treasure titles current transactions for a preloaded investment"() {
        given:
        CreateConsent createConsent = createConsent('76109277673', 'CPF', [BANK_FIXED_INCOMES_READ,
                                                                           CREDIT_FIXED_INCOMES_READ, FUNDS_READ, VARIABLE_INCOMES_READ,
                                                                           TREASURE_TITLES_READ, RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        ResponseConsent returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST("/open-banking/consents/v2/consents/", jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsent)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 6

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234').linkedAccountIds(List.of('291e5a29-49ed-401f-a583-193caa7aceee')))
        def updateToken = createToken("op:consent")
        log.info("OP consent token - {}", updateToken)

        ResponseConsentFull fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT("/open-banking/consents/v2/consents/" + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFull)

        then:
        fullConsent.getData().getSub() != null

        when:
        def token = createToken("treasure-titles consent:${fullConsent.getData().getConsentId().toString()}")
        log.info("OP consent token - {}", updateToken)


        def baseUri = "/open-banking/treasure-titles/v1/investments/a5ae963d-1156-4911-ad66-59cd079afaab/transactions-current"
        def dateParams = "fromTransactionDate=${LocalDate.now().minusDays(3).toString()}&toTransactionDate=${LocalDate.now().toString()}"
        def uri = baseUri + "?" + dateParams
        ResponseTreasureTitlesTransactions transactions = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${token}"),
                        ResponseTreasureTitlesTransactions)
        then:
        transactions != null
        transactions.getData() != null
        transactions.getData().size() > 2
        transactions.getLinks().getSelf() == "https://mockbank.com/api${baseUri}?page-size=25&page=1&${dateParams}"

        when:
        def delResponse = client.toBlocking().exchange(
                HttpRequest.DELETE("/open-banking/consents/v2/consents/${fullConsent.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${updateToken}"))

        then:
        noExceptionThrown()
        delResponse.getStatus() == HttpStatus.NO_CONTENT
    }

    void "we can get variable incomes for a preloaded investment"() {
        given:
        CreateConsentV2 createConsent = createConsentV2('76109277673', 'CPF', [BANK_FIXED_INCOMES_READ,
                                                                           CREDIT_FIXED_INCOMES_READ, FUNDS_READ, VARIABLE_INCOMES_READ,
                                                                           TREASURE_TITLES_READ, RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        ResponseConsentFullV2 returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST("/open-banking/consents/v2/consents/", jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsentFullV2)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 6

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234').linkedAccountIds(List.of('291e5a29-49ed-401f-a583-193caa7aceee')))
        def updateToken = createToken("op:consent")
        log.info("OP consent token - {}", updateToken)

        ResponseConsentFullV2 fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT("/open-banking/consents/v2/consents/" + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFullV2)

        then:
        fullConsent.getData().getSub() != null

        when:
        def token = createToken("variable-incomes consent:${fullConsent.getData().getConsentId().toString()}")
        log.info("OP consent token - {}", updateToken)

        def uri = "/open-banking/variable-incomes/v1/investments/"
        ResponseVariableIncomesProductList investments = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${token}"),
                        ResponseVariableIncomesProductList)
        then:
        investments != null
        investments.getData() != null
        investments.getData().size() == 25
        investments.getData().get(0) != null
        investments.getData().get(0).getInvestmentId() != null
        investments.getMeta() != null
        investments.getMeta().getTotalRecords() == 61
        investments.getMeta().getTotalPages() == 3
        investments.getLinks() != null
        investments.getLinks().getSelf() == "https://mockbank.com/api${uri}?page-size=25&page=1"
        investments.getLinks().getFirst() == "https://mockbank.com/api${uri}?page-size=25&page=1"
        investments.getLinks().getNext() == "https://mockbank.com/api${uri}?page-size=25&page=2"
        investments.getLinks().getLast() == "https://mockbank.com/api${uri}?page-size=25&page=3"

        when:
        def delResponse = client.toBlocking().exchange(
                HttpRequest.DELETE("/open-banking/consents/v2/consents/${fullConsent.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${updateToken}"))

        then:
        noExceptionThrown()
        delResponse.getStatus() == HttpStatus.NO_CONTENT
    }

    void "we can get variable incomes Balances for a preloaded investment"() {
        given:
        CreateConsentV2 createConsent = createConsentV2('76109277673', 'CPF', [BANK_FIXED_INCOMES_READ,
                                                                           CREDIT_FIXED_INCOMES_READ, FUNDS_READ, VARIABLE_INCOMES_READ,
                                                                           TREASURE_TITLES_READ, RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        ResponseConsentV2 returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST("/open-banking/consents/v2/consents/", jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsentV2)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 6

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234').linkedAccountIds(List.of('291e5a29-49ed-401f-a583-193caa7aceee')))
        def updateToken = createToken("op:consent")
        log.info("OP consent token - {}", updateToken)

        ResponseConsentFullV2 fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT("/open-banking/consents/v2/consents/" + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFullV2)

        then:
        fullConsent.getData().getSub() != null

        when:
        def token = createToken("variable-incomes consent:${fullConsent.getData().getConsentId().toString()}")
        log.info("OP consent token - {}", updateToken)

        def uri = "/open-banking/variable-incomes/v1/investments/e4e6bce7-2182-4502-a5c7-574af90a537e/balances"
        ResponseVariableIncomesBalance balances = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${token}"),
                        ResponseVariableIncomesBalance)
        then:
        balances != null
        balances.getData() != null

        when:
        def delResponse = client.toBlocking().exchange(
                HttpRequest.DELETE("/open-banking/consents/v2/consents/${fullConsent.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${updateToken}"))

        then:
        noExceptionThrown()
        delResponse.getStatus() == HttpStatus.NO_CONTENT
    }

    void "we can get variable incomes current transactions for a preloaded investment"() {
        given:
        CreateConsentV2 createConsent = createConsentV2('76109277673', 'CPF', [BANK_FIXED_INCOMES_READ,
                                                                           CREDIT_FIXED_INCOMES_READ, FUNDS_READ, VARIABLE_INCOMES_READ,
                                                                           TREASURE_TITLES_READ, RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        ResponseConsentV2 returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST("/open-banking/consents/v2/consents/", jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsentV2)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 6

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234').linkedAccountIds(List.of('291e5a29-49ed-401f-a583-193caa7aceee')))
        def updateToken = createToken("op:consent")
        log.info("OP consent token - {}", updateToken)

        ResponseConsentFullV2 fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT("/open-banking/consents/v2/consents/" + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFullV2)

        then:
        fullConsent.getData().getSub() != null

        when:
        def token = createToken("variable-incomes consent:${fullConsent.getData().getConsentId().toString()}")
        log.info("OP consent token - {}", updateToken)


        def baseUri = "/open-banking/variable-incomes/v1/investments/e4e6bce7-2182-4502-a5c7-574af90a537e/transactions-current"
        def dateParams = "fromTransactionDate=${LocalDate.now().minusDays(3).toString()}&toTransactionDate=${LocalDate.now().toString()}"
        def uri = baseUri + "?" + dateParams
        ResponseVariableIncomesTransactions transactions = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${token}"),
                        ResponseVariableIncomesTransactions)
        then:
        transactions != null
        transactions.getData() != null
        transactions.getData().size() > 2
        transactions.getLinks().getSelf() == "https://mockbank.com/api${baseUri}?page-size=25&page=1&${dateParams}"
        when:
        def delResponse = client.toBlocking().exchange(
                HttpRequest.DELETE("/open-banking/consents/v2/consents/${fullConsent.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${updateToken}"))

        then:
        noExceptionThrown()
        delResponse.getStatus() == HttpStatus.NO_CONTENT
    }

    void "we can get variable incomes broker notes for a preloaded investment"() {
        given:
        CreateConsentV2 createConsent = createConsentV2('76109277673', 'CPF', [BANK_FIXED_INCOMES_READ,
                                                                           CREDIT_FIXED_INCOMES_READ, FUNDS_READ, VARIABLE_INCOMES_READ,
                                                                           TREASURE_TITLES_READ, RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        ResponseConsentV2 returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST("/open-banking/consents/v2/consents/", jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsentV2)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 6

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234').linkedAccountIds(List.of('291e5a29-49ed-401f-a583-193caa7aceee')))
        def updateToken = createToken("op:consent")
        log.info("OP consent token - {}", updateToken)

        ResponseConsentFullV2 fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT("/open-banking/consents/v2/consents/" + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFullV2)

        then:
        fullConsent.getData().getSub() != null

        when:
        def token = createToken("variable-incomes consent:${fullConsent.getData().getConsentId().toString()}")
        log.info("OP consent token - {}", updateToken)

        def uri = "/open-banking/variable-incomes/v1/broker-notes/50fa1645-d45b-4c06-a8c2-1f402aeda850"
        ResponseVariableIncomesBroker brokerNotes = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${token}"),
                        ResponseVariableIncomesBroker)
        then:
        brokerNotes != null
        brokerNotes.getData() != null

        when:
        def delResponse = client.toBlocking().exchange(
                HttpRequest.DELETE("/open-banking/consents/v2/consents/${fullConsent.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${updateToken}"))

        then:
        noExceptionThrown()
        delResponse.getStatus() == HttpStatus.NO_CONTENT
    }

    void "user Gabriel Nune always get RESOURCE with PENDING_AUTHORIZATION"() {
        given:
        CreateConsentV2 createConsent = createConsentV2('87517400444', 'CPF', [ACCOUNTS_READ, ACCOUNTS_BALANCES_READ, RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        ResponseConsentV2 returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST("/open-banking/consents/v2/consents/", jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsentV2)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 3

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("gabriel.nunes@email.com").clientId('1234').linkedAccountIds(List.of('291e5a29-49ed-401f-a583-193caa7ac79d')))
        def updateToken = createToken("op:consent")
        log.info("OP consent token - {}", updateToken)

        ResponseConsentFullV2 fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT("/open-banking/consents/v2/consents/" + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFullV2)

        then:
        fullConsent.getData().getSub() != null

        when:
        def token = createToken("accounts resources consent:${fullConsent.getData().getConsentId().toString()}")

        def uri = "/open-banking/resources/v2/resources/"
        ResponseResourceList resource = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${token}"),
                        ResponseResourceList)
        then:
        resource != null
        resource.getData() != null
        resource.getData().first().getStatus() == ResponseResourceListData.StatusEnum.PENDING_AUTHORISATION

        when:
        uri = "/open-banking/accounts/v2/accounts/"
        client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${token}")
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString()),
                        ResponseResourceList)
        then:
        HttpClientResponseException e = thrown()
        e.status == HttpStatus.FORBIDDEN

        when:
        uri = "/open-banking/accounts/v2/accounts/291e5a29-49ed-401f-a583-193caa7ac79d"
        client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${token}")
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString()),
                        ResponseResourceList)
        then:
        HttpClientResponseException e2 = thrown()
        e2.status == HttpStatus.FORBIDDEN

        when:
        def delResponse = client.toBlocking().exchange(
                HttpRequest.DELETE("/open-banking/consents/v2/consents/${fullConsent.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${updateToken}"))

        then:
        noExceptionThrown()
        delResponse.getStatus() == HttpStatus.NO_CONTENT
    }

    void "user Janice gets RESOURCE with TEMPORARILY_UNAVAILABLE"() {
        given:
        CreateConsentV2 createConsent = createConsentV2('96644087000', 'CPF', [LOANS_READ, LOANS_WARRANTIES_READ,
                                                                           LOANS_SCHEDULED_INSTALMENTS_READ, LOANS_PAYMENTS_READ,
                                                                           FINANCINGS_READ, FINANCINGS_WARRANTIES_READ,
                                                                           FINANCINGS_SCHEDULED_INSTALMENTS_READ, FINANCINGS_PAYMENTS_READ,
                                                                           UNARRANGED_ACCOUNTS_OVERDRAFT_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ,
                                                                           UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ,
                                                                           INVOICE_FINANCINGS_READ, INVOICE_FINANCINGS_WARRANTIES_READ,
                                                                           INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ, INVOICE_FINANCINGS_PAYMENTS_READ,
                                                                           RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        ResponseConsentV2 returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST("/open-banking/consents/v2/consents/", jsonRequest)
                        .header("Authorization", "Bearer ${createToken("consents")}"),
                        ResponseConsentV2)

        then:
        returnedConsent.getData().getCreationDateTime() != null

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("janice.matos@email.com").clientId('1234').linkedLoanAccountIds(List.of('8cdf6902-f7d7-4d35-a8b5-db72250fd510', 'b7c92a74-a517-4f7c-8f6f-62ccbf032cc1')))
        def updateToken = createToken("op:consent")
        log.info("OP consent token - {}", updateToken)

        ResponseConsentFullV2 fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT("/open-banking/consents/v2/consents/" + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFullV2)

        then:
        fullConsent.getData().getSub() != null

        when:
        def token = createToken("loans resources consent:${fullConsent.getData().getConsentId().toString()}")

        def uri = "/open-banking/resources/v2/resources/"
        ResponseResourceList resource = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${token}"),
                        ResponseResourceList)
        then:
        resource != null
        resource.getData() != null
        resource.getData().stream().filter { it.getStatus() == ResponseResourceListData.StatusEnum.TEMPORARILY_UNAVAILABLE }.count() >  0

        when:
        uri = "/open-banking/loans/v2/contracts/"
        ResponseLoansContractListV2 loans = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${token}"),
                        ResponseLoansContractListV2)
        then:
        loans.getData().size() == 1
        loans.getData().first().getContractId() != "8cdf6902-f7d7-4d35-a8b5-db72250fd510"

        when:
        uri = "/open-banking/loans/v2/contracts/8cdf6902-f7d7-4d35-a8b5-db72250fd510"
        client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${token}"),
                        ResponseLoansContractListV2)
        then:
        HttpClientResponseException e2 = thrown()
        e2.status == HttpStatus.FORBIDDEN

        when:
        uri = "/open-banking/loans/v2/contracts/b7c92a74-a517-4f7c-8f6f-62ccbf032cc1"
        client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${token}"),
                        ResponseResourceList)
        then:
        noExceptionThrown()

        when:
        def delResponse = client.toBlocking().exchange(
                HttpRequest.DELETE("/open-banking/consents/v2/consents/${fullConsent.getData().getConsentId()}")
                        .header("Authorization", "Bearer ${updateToken}"))

        then:
        noExceptionThrown()
        delResponse.getStatus() == HttpStatus.NO_CONTENT
    }

    //TODO: in progress
    def "User Ralph Bragg can use data from all API's V2 (Accounts, CreditCard, Customers, Contracts, Resources)"() {
        given:
        AccountHolderEntity ralphUser = accountHolderRepository.findByDocumentIdentificationAndDocumentRel('76109277673', 'CPF').first()

        when:// get Account
        def accounts = accountRepository.findByAccountHolderId(ralphUser.getAccountHolderId())

        then:
        accounts.size() == 2
        def account = accounts.first()
        account.getAccountHolder().getAccountHolderName() == "Ralph Bragg"

        when:// get Account Transactions
        def transactions = accountTransactionsRepository.findByAccountIdOrderByCreatedAtAsc(account.getAccountId(), Pageable.from(0, 100))

        then:
        transactions.totalSize == 112

        when:/// get personal identifications
        def personalIdentifications = personalIdentificationsRepository.findByAccountHolderAccountHolderId(ralphUser.getAccountHolderId()).first()
        String customerToken = createConsentWithCustomerPermissions(ralphUser, false)
        URI uri = new URIBuilder('/open-banking/customers/v2/personal/identifications').build()

        def response = client.toBlocking().retrieve(HttpRequest.GET(uri).header("Authorization", "Bearer ${customerToken}"), ResponsePersonalCustomersIdentificationV2)

        then:
        response.getData() != null
        def personal = response.getData().first()
        personal.getPersonalId() == personalIdentifications.getPersonalIdentificationsId().toString()
        personal.getBrandName() == personalIdentifications.getBrandName()
        personal.getCivilName() == personalIdentifications.getCivilName()
        personal.getSocialName() == personalIdentifications.getSocialName()
        personal.getBirthDate() == personalIdentifications.getBirthDate()
        personal.getMaritalStatusCode().name() == personalIdentifications.getMaritalStatusCode()
        personal.getMaritalStatusAdditionalInfo() == personalIdentifications.getMaritalStatusAdditionalInfo()
        personal.getSex().name() == personalIdentifications.getSex()
        personal.getDocuments().getCpfNumber() == personalIdentifications.getCpfNumber()
        personal.getDocuments().getPassport().getNumber() == personalIdentifications.getPassportNumber()
        personal.getDocuments().getPassport().getCountry() == personalIdentifications.getPassportCountry()
        personal.getDocuments().getPassport().getExpirationDate() == personalIdentifications.getPassportExpirationDate()
        personal.getDocuments().getPassport().getIssueDate() == personalIdentifications.getPassportIssueDate()
        personal.isHasBrazilianNationality() == personalIdentifications.hasBrazilianNationality

        !personal.getOtherDocuments().isEmpty()
        !personal.getNationality().isEmpty()
        !personal.getNationality().first().getDocuments().isEmpty()
        !personal.getFiliation().isEmpty()
        !personal.getCompaniesCnpj().isEmpty()
        !personal.getContacts().getPostalAddresses().isEmpty()
        !personal.getContacts().getPhones().isEmpty()
        !personal.getContacts().getEmails().isEmpty()

        when:
        def personalFinancialRelations = personalFinancialRelationsRepository.findByAccountHolderAccountHolderId(ralphUser.getAccountHolderId()).get()
        uri = new URIBuilder('/open-banking/customers/v2/personal/financial-relations').build()

        response = client.toBlocking().retrieve(HttpRequest.GET(uri).header("Authorization", "Bearer ${customerToken}"), ResponsePersonalCustomersFinancialRelationV2)

        then:
        response.getData() != null
        def financialRelations = response.getData()
        financialRelations.getStartDate().toLocalDate() == personalFinancialRelations.getStartDate()
        !financialRelations.getProductsServicesType().isEmpty()
        financialRelations.getProductsServicesTypeAdditionalInfo() == personalFinancialRelations.getProductsServicesTypeAdditionalInfo()
        !financialRelations.getProcurators().isEmpty()

        when:
        def personalQualifications = personalQualificationsRepository.findByAccountHolderAccountHolderId(ralphUser.getAccountHolderId()).get()

        uri = new URIBuilder('/open-banking/customers/v2/personal/qualifications').build()
        response = client.toBlocking().retrieve(HttpRequest.GET(uri).header("Authorization", "Bearer ${customerToken}"), ResponsePersonalCustomersQualificationV2)

        then:
        response.getData() != null
        def qualification = response.getData()
        qualification.getCompanyCnpj() == personalQualifications.getCompanyCnpj()
        qualification.getOccupationCode().name() == personalQualifications.getOccupationCode()
        qualification.getOccupationDescription() == personalQualifications.getOccupationDescription()
        qualification.getInformedIncome().getFrequency().name() == personalQualifications.getInformedIncomeFrequency()
        qualification.getInformedIncome().getAmount().getAmount() == BankLambdaUtils.formatAmountV2(personalQualifications.getInformedIncomeAmount())
        qualification.getInformedIncome().getAmount().getCurrency() == personalQualifications.getInformedIncomeCurrency()
        qualification.getInformedIncome().getDate() == personalQualifications.getInformedIncomeDate()
        qualification.getInformedPatrimony().getAmount().getAmount() == BankLambdaUtils.formatAmountV2(personalQualifications.getInformedPatrimonyAmount())
        qualification.getInformedPatrimony().getAmount().getCurrency() == personalQualifications.getInformedPatrimonyCurrency()
        qualification.getInformedPatrimony().getYear() == personalQualifications.getInformedPatrimonyYear()

        when:
        customerToken = createConsentWithCustomerPermissions(ralphUser, true)
        def businessIdentifications = businessIdentificationsRepository.findByAccountHolderAccountHolderId(ralphUser.getAccountHolderId()).first()

        uri = new URIBuilder('/open-banking/customers/v2/business/identifications').build()
        response = client.toBlocking().retrieve(HttpRequest.GET(uri).header("Authorization", "Bearer ${customerToken}"), ResponseBusinessCustomersIdentificationV2)

        then:
        response.getData() != null
        def business = response.getData().first()
        business.getBusinessId() == businessIdentifications.getBusinessIdentificationsId().toString()
        business.getBrandName() == businessIdentifications.getBrandName()
        business.getCompanyName() == businessIdentifications.getCompanyName()
        business.getTradeName() == businessIdentifications.getTradeName()
        business.getIncorporationDate().toLocalDate() == businessIdentifications.getIncorporationDate()
        business.getCnpjNumber() == businessIdentifications.getCnpjNumber()
        !business.getCompaniesCnpj().isEmpty()
        !business.getOtherDocuments().isEmpty()
        !business.getParties().isEmpty()
        !business.getContacts().getPostalAddresses().isEmpty()
        !business.getContacts().getPhones().isEmpty()
        !business.getContacts().getEmails().isEmpty()

        when:
        def businessFinancialRelationsEntity = businessFinancialRelationsRepository.findByAccountHolderAccountHolderId(ralphUser.getAccountHolderId()).get()

        uri = new URIBuilder('/open-banking/customers/v2/business/financial-relations').build()
        response = client.toBlocking().retrieve(HttpRequest.GET(uri).header("Authorization", "Bearer ${customerToken}"), ResponseBusinessCustomersFinancialRelationV2)

        then:
        response.getData() != null
        def businessFinancialRelations = response.getData()
        businessFinancialRelations.getStartDate().toLocalDate() == businessFinancialRelationsEntity.getStartDate()
        !businessFinancialRelations.getProductsServicesType().isEmpty()
        !businessFinancialRelations.getProcurators().isEmpty()

        when:
        def businessQualificationsEntity = businessQualificationsRepository.findByAccountHolderAccountHolderId(ralphUser.getAccountHolderId()).get()
        uri = new URIBuilder('/open-banking/customers/v2/business/qualifications').build()

        response = client.toBlocking().retrieve(HttpRequest.GET(uri).header("Authorization", "Bearer ${customerToken}"), ResponseBusinessCustomersQualificationV2)

        then:
        response.getData() != null
        def businessQualification = response.getData()
        !businessQualification.getEconomicActivities().isEmpty()
        businessQualification.getInformedRevenue().getFrequency().name() == businessQualificationsEntity.getInformedRevenueFrequency()
        businessQualification.getInformedRevenue().getFrequencyAdditionalInfo() == businessQualificationsEntity.getInformedRevenueFrequencyAdditionalInformation()
        businessQualification.getInformedRevenue().getAmount().getAmount() == BankLambdaUtils.formatAmountV2(businessQualificationsEntity.getInformedRevenueAmount())
        businessQualification.getInformedRevenue().getAmount().getCurrency() == businessQualificationsEntity.getInformedRevenueCurrency()
        businessQualification.getInformedRevenue().getYear() == businessQualificationsEntity.getInformedRevenueYear()
        businessQualification.getInformedPatrimony().getAmount().getAmount() == BankLambdaUtils.formatAmountV2(businessQualificationsEntity.getInformedPatrimonyAmount())
        businessQualification.getInformedPatrimony().getAmount().getCurrency() == businessQualificationsEntity.getInformedPatrimonyCurrency()
        businessQualification.getInformedPatrimony().getDate() == businessQualificationsEntity.getInformedPatrimonyDate()

        when:// get Credit Card Account
        def creditCardAccount = creditCardAccountsRepository.findByAccountHolderUserId(ralphUser.getUserId()).first()
        uri = new URIBuilder("/open-banking/credit-cards-accounts/v2/accounts").build()

        def creditCardToken = createConsentWithCreditCardAccountPermissions(ralphUser, creditCardAccount.getCreditCardAccountId().toString())
        def responseCreditCard = client.toBlocking()
                .retrieve(HttpRequest.GET("${uri}/${creditCardAccount.getCreditCardAccountId().toString()}")
                        .header("Authorization", "Bearer ${creditCardToken}")
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString()), ResponseCreditCardAccountsIdentification).getData()

        then:
        creditCardAccount.getAccountHolder().getAccountHolderName() == "Ralph Bragg"
        responseCreditCard.getName() == creditCardAccount.getName()
        responseCreditCard.getProductType().name() == creditCardAccount.getProductType()
        responseCreditCard.getProductAdditionalInfo() == creditCardAccount.getProductAdditionalInfo()
        responseCreditCard.getCreditCardNetwork().name() == creditCardAccount.getCreditCardNetwork()
        !responseCreditCard.getPaymentMethod().isEmpty()

        when:// get Credit Card Transactions
        def creditCardTransactions = creditCardAccountsTransactionRepository.findByCreditCardAccountIdAndTransactionDateTimeBetweenOrderByCreatedAtAsc(
                creditCardAccount.getCreditCardAccountId(), OffsetDateTime.parse("2022-01-01T00:01:00+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME), OffsetDateTime.parse("2022-08-01T23:59:59+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME), Pageable.from(0, 100))

        uri = new URIBuilder("/open-banking/credit-cards-accounts/v2/accounts/${creditCardAccount.getCreditCardAccountId().toString()}/transactions")
                .addParameter("fromTransactionDate", "2022-01-01")
                .addParameter("toTransactionDate", "2022-08-01")
                .build()
        def responseCreditCardTransaction = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${creditCardToken}")
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString()), ResponseCreditCardAccountsTransactionsV2)

        then:
        creditCardTransactions.totalSize == 62
        responseCreditCardTransaction.getData().size() == 25

        when:// get Contract

        //Loan
        def loanContract = contractsRepository.findByAccountHolderUserIdAndContractType(ralphUser.getUserId(),
                EnumContractType.LOAN.name()).first()
        def loanToken = createConsentWithContractPermissions(ralphUser, loanContract.getContractId().toString(),
                ContractTypeEnum.LOAN)
        def responseLoan = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/loans/v2/contracts/${loanContract.getContractId().toString()}")
                        .header("Authorization", "Bearer ${loanToken}"), ResponseLoansContractV2)

        //Financing
        def financingContract = contractsRepository.findByAccountHolderUserIdAndContractType(ralphUser.getUserId(),
                EnumContractType.FINANCING.name()).first()
        def financingToken = createConsentWithContractPermissions(ralphUser, financingContract.getContractId().toString(),
                ContractTypeEnum.FINANCING)
        def responseFinancing = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/financings/v2/contracts/${financingContract.getContractId().toString()}")
                        .header("Authorization", "Bearer ${financingToken}"), ResponseFinancingsContractV2)

        //Invoice Financing
        def invoiceFinancingContract = contractsRepository.findByAccountHolderUserIdAndContractType(ralphUser.getUserId(),
                EnumContractType.INVOICE_FINANCING.name()).first()
        def invoiceFinancingToken = createConsentWithContractPermissions(ralphUser, invoiceFinancingContract.getContractId().toString(),
                ContractTypeEnum.INVOICE_FINANCING)
        def responseInvoiceFinancing = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/invoice-financings/v2/contracts/${invoiceFinancingContract.getContractId().toString()}")
                        .header("Authorization", "Bearer ${invoiceFinancingToken}"), ResponseInvoiceFinancingsContractV2)

        //Overdraft
        def overdraftContract = contractsRepository.findByAccountHolderUserIdAndContractType(ralphUser.getUserId(),
                EnumContractType.UNARRANGED_ACCOUNT_OVERDRAFT.name()).first()
        def overdraftToken = createConsentWithContractPermissions(ralphUser, overdraftContract.getContractId().toString(),
                ContractTypeEnum.UNARRANGED_ACCOUNT_OVERDRAFT)
        def responseOverdraft = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/unarranged-accounts-overdraft/v2/contracts/${overdraftContract.getContractId().toString()}")
                        .header("Authorization", "Bearer ${overdraftToken}"), ResponseUnarrangedAccountOverdraftContractV2)

        then:
        responseLoan.getData() != null
        responseFinancing.getData() != null
        responseInvoiceFinancing.getData() != null
        responseOverdraft.getData() != null

        when:// get Resources (page 1)
        def resourcesToken = createConsentWithResourcesPermissions(ralphUser)

        def responseResources = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/resources/v2/resources")
                        .header("Authorization", "Bearer ${resourcesToken}"), ResponseResourceList)

        def responseResourcesData = responseResources.getData()

        then:
        !responseResourcesData.isEmpty()
        responseResourcesData.size() == 25
        responseResources.getMeta() != null
        responseResources.getMeta().getTotalPages() >= 3
        responseResources.getMeta().getTotalRecords() > 50
        def lastPage = Math.ceil(responseResources.getMeta().getTotalRecords() / maxPageSize).intValue()
        responseResources.getLinks().getSelf().endsWith("?page-size=25&page=1")
        responseResources.getLinks().getNext().endsWith("?page-size=25&page=2")
        responseResources.getLinks().getLast().endsWith("?page-size=25&page=${lastPage}")
        responseResources.getLinks().getFirst().endsWith("?page-size=25&page=1")


        when:// get Resources (page 2)
        responseResources = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/resources/v2/resources?page=2")
                        .header("Authorization", "Bearer ${resourcesToken}"), ResponseResourceList)

        responseResourcesData = responseResources.getData()

        then:
        !responseResourcesData.isEmpty()
        responseResourcesData.size() == 25
        responseResources.getMeta() != null
        responseResources.getMeta().getTotalPages() >= 3
        responseResources.getMeta().getTotalRecords() > 50
        responseResources.getLinks().getSelf().endsWith("?page-size=25&page=2")
        responseResources.getLinks().getPrev().endsWith("?page-size=25&page=1")
        responseResources.getLinks().getNext().endsWith("?page-size=25&page=3")
        responseResources.getLinks().getLast().endsWith("?page-size=25&page=${lastPage}")
        responseResources.getLinks().getFirst().endsWith("?page-size=25&page=1")

        when://get an empty list when only customers permissions are granted
        def resourcesBusinessToken = createPermissionsOnlyForCustomersBusiness(ralphUser)
        def responseEmptyBusinessResources = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/resources/v2/resources")
                        .header("Authorization", "Bearer ${resourcesBusinessToken}"), ResponseResourceList)

        def resourcesPersonalToken = createPermissionsOnlyForCustomersPersonal(ralphUser)
        def responseEmptyPersonalResources = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/resources/v2/resources")
                        .header("Authorization", "Bearer ${resourcesPersonalToken}"), ResponseResourceList)

        then:
        responseEmptyBusinessResources.getData().isEmpty()
        responseEmptyPersonalResources.getData().isEmpty()
    }

    def "User Ralph Bragg can create a v4 payment consent and get it"() {
        given:
        def createPaymentConsent = createPaymentConsentV4('96644087000', 'CPF')
        def jwtPayload = AwsProxyHelper.signPayload(createPaymentConsent)

        when:
        def returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST("/open-banking/payments/v4/consents/", jwtPayload)
                        .header("Authorization", "Bearer ${createToken("payments")}")
                        .header("x-idempotency-key", UUID.randomUUID().toString())
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString())
                        .header("Content-Type", "application/jwt")
                        .header("Accept", "application/jwt"),
                        String)

        then:
        def claimsPostData = mapper.writeValueAsString(SignedJWT.parse(returnedConsent).getJWTClaimsSet().getClaim("data"))
        def responsePost = mapper.readValue("{\"data\":" + claimsPostData + "}", ResponseCreatePaymentConsentV4)

        responsePost.getData().getStatus() == EnumAuthorisationStatusType.AWAITING_AUTHORISATION
        responsePost.getData().getExpirationDateTime() == responsePost.getData().getCreationDateTime().plusMinutes(5)

        when:
        def getConsent = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/payments/v4/consents/"+responsePost.getData().getConsentId())
                        .header("Authorization", "Bearer ${createToken("payments")}")
                        .header("x-idempotency-key", UUID.randomUUID().toString())
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString())
                        .header("Content-Type", "application/jwt")
                        .header("Accept", "application/jwt"),
                        String)

        then:
        def claimsGetData = mapper.writeValueAsString(SignedJWT.parse(getConsent).getJWTClaimsSet().getClaim("data"))
        def responseGet = mapper.readValue("{\"data\":" + claimsGetData + "}", ResponseCreatePaymentConsentV4)
        responseGet.getData().getStatus() == EnumAuthorisationStatusType.AWAITING_AUTHORISATION
        responseGet.getData().getExpirationDateTime() == responseGet.getData().getCreationDateTime().plusMinutes(5)

    }

    void "we can POST and GET notification webhook URIs"() {
        given:
        CreateWebhook createdWebhook = TestRequestDataFactory.createWebhook()
        String jsonRequest = mapper.writeValueAsString(createdWebhook)

        when:
        ResponseWebhook returnedWebhook = client.toBlocking()
                .retrieve(HttpRequest.POST("/admin/webhooks", jsonRequest)
                        .header("Authorization", "Bearer ${createToken("op:admin")}"),
                        ResponseWebhook)

        then:
        noExceptionThrown()
        returnedWebhook != null
        returnedWebhook.data != null
        returnedWebhook.data.webhookUri == createdWebhook.data.webhookUri
        returnedWebhook.data.clientId == clientId

        when:
        ResponseWebhook getWebhook = client.toBlocking()
                .retrieve(HttpRequest.GET("/admin/webhooks/" + clientId)
                        .header("Authorization", "Bearer ${createToken("op:admin")}")
                        .header("x-fapi-interaction-id", UUID.randomUUID().toString()),
                        ResponseWebhook)

        then:
        noExceptionThrown()
        getWebhook != null
        getWebhook.data != null
        getWebhook.data.webhookUri == createdWebhook.data.webhookUri
        getWebhook.data.clientId == clientId
    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }
}
