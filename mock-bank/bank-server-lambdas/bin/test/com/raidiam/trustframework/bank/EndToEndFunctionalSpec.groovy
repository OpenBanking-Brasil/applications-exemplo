package com.raidiam.trustframework.bank

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.Payload
import com.nimbusds.jose.PlainHeader
import com.nimbusds.jose.PlainObject
import com.raidiam.trustframework.bank.domain.AccountHolderEntity
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpParameters
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.simple.SimpleHttpParameters
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.rxjava2.http.client.RxHttpClient
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
import spock.lang.Unroll

import javax.inject.Inject

import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccountHolder
import static com.raidiam.trustframework.bank.controllers.ConsentFactory.createConsent
import static com.raidiam.trustframework.mockbank.models.generated.CreateConsentData.PermissionsEnum.*

@Stepwise
@MicronautTest(transactional = false, environments = ["db-with-preload"])
@Testcontainers
class EndToEndFunctionalSpec extends CleanupLocalStackSpecification implements TestPropertyProvider {
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

    @Shared
    AccountHolderEntity testAccountHolder

    def setupSpec() {
        mapper.findAndRegisterModules()
    }

    def setup () {
        if(runSetup) {
            testAccountHolder = accountHolderRepository.save(anAccountHolder())
            runSetup = false
        }
    }

    def cleanupSpec () {

    }

    void "we can post a consent request for our new user" () {
        given:
        CreateConsent createConsent = createConsent(testAccountHolder.getDocumentIdentification(), testAccountHolder.getDocumentRel(), null)
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        PlainObject plainObject = new PlainObject(new PlainHeader(), new Payload([scope: "consents", client_id: "client1", subject: "sub1"]))
        def bearerToken = plainObject.serialize()
        ResponseConsent returnedConsent = client.toBlocking()
                    .retrieve(HttpRequest.POST('/open-banking/consents/v1/consents', jsonRequest)
                                         .header("Authorization", "Bearer ${bearerToken}"),
                              ResponseConsent)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 3

        when:
        def newToken = new PlainObject(new PlainHeader(), new Payload([scope: "op:consent", client_id: "client1", subject: "sub1"])).serialize()
        log.info("OP consent token - {}", newToken)
        ResponseConsentFull fullConsent = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/consents/v1/consents/${returnedConsent.getData().getConsentId()}")
                                     .header("Authorization", "Bearer ${newToken}"),
                          ResponseConsentFull)

        then:
        fullConsent.getData().getSub() != null
    }

    void "we can update a consent request for our new user" () {
        given:
        CreateConsent createConsent = createConsent(testAccountHolder.getDocumentIdentification(), testAccountHolder.getDocumentRel(), null)

        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED))

        when:
        PlainObject plainObject = new PlainObject(new PlainHeader(), new Payload([scope: "op:consent"]))
        def bearerToken = plainObject.serialize()
        ResponseConsent returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST('/open-banking/consents/v1/consents', mapper.writeValueAsString(createConsent))
                                     .header("Authorization", "Bearer ${bearerToken}"),
                          ResponseConsent)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 3

        when:
        String url = '/open-banking/consents/v1/consents/' + returnedConsent.getData().getConsentId()
        ResponseConsentFull returnedUpdateConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT(url, mapper.writeValueAsString(consentUpdate))
                                     .header("Authorization", "Bearer ${bearerToken}"),
                          ResponseConsentFull)

        then:
        returnedUpdateConsent.data.status == ResponseConsentFullData.StatusEnum.AUTHORISED
    }

    void "we can get personal identifications for a preloaded user" () {
        given:
        CreateConsent createConsent = createConsent('76109277673', 'CPF', [CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ, RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)

        when:
        PlainObject plainObject = new PlainObject(new PlainHeader(), new Payload([scope: "consents", client_id: "client1", subject: "ralph.bragg@gmail.com"]))
        def consentToken = plainObject.serialize()
        ResponseConsent returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST('/open-banking/consents/v1/consents', jsonRequest)
                                     .header("Authorization", "Bearer ${consentToken}"),
                          ResponseConsent)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 2

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED).sub("ralph.bragg@gmail.com").clientId('1234'))
        def updateToken = new PlainObject(new PlainHeader(), new Payload([scope: "op:consent", client_id: "client1", subject: "ralph.bragg@gmail.com"])).serialize()
        log.info("OP consent token - {}", updateToken)
        ResponseConsentFull fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT('/open-banking/consents/v1/consents/' + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFull)

        then:
        fullConsent.getData().getSub() != null

        when:
        def customersToken = new PlainObject(new PlainHeader(), new Payload([scope: "customers consent:${fullConsent.getData().getConsentId().toString()}".toString(), client_id: "client1", subject: "ralph.bragg@gmail.com"])).serialize()
        log.info("OP consent token - {}", updateToken)
        ResponsePersonalCustomersIdentification identifications = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/customers/v1/personal/identifications")
                                     .header("Authorization", "Bearer ${customersToken}"),
                          ResponsePersonalCustomersIdentification)

        then:
        identifications != null
        identifications.getData() != null
        identifications.getData().get(0) != null
        identifications.getData().get(0).getSocialName() != null
    }

    void "we can get account transactions for a preloaded user" () {
        given:
        CreateConsent createConsent = createConsent('76109277673', 'CPF', [ACCOUNTS_READ, ACCOUNTS_TRANSACTIONS_READ, RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)
        PlainObject plainObject = new PlainObject(new PlainHeader(), new Payload([scope: "consents", client_id: "client1", subject: "ralph.bragg@gmail.com"]))
        def consentToken = plainObject.serialize()

        when:
        ResponseConsent returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST('/open-banking/consents/v1/consents', jsonRequest)
                        .header("Authorization", "Bearer ${consentToken}"),
                        ResponseConsent)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 3

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").clientId('1234').linkedAccountIds(List.of('291e5a29-49ed-401f-a583-193caa7aceee')))
        def updateToken = new PlainObject(new PlainHeader(), new Payload([scope: "op:consent", client_id: "client1", subject: "ralph.bragg@gmail.com"])).serialize()
        log.info("OP consent token - {}", updateToken)

        ResponseConsentFull fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT('/open-banking/consents/v1/consents/' + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFull)

        then:
        fullConsent.getData().getSub() != null

        when:
        def transactionToken = new PlainObject(new PlainHeader(), new Payload([scope: "accounts consent:${fullConsent.getData().getConsentId().toString()}".toString(), client_id: "client1", subject: "ralph.bragg@gmail.com"])).serialize()
        log.info("OP consent token - {}", updateToken)
        ResponseAccountTransactions transactions = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/accounts/v1/accounts/291e5a29-49ed-401f-a583-193caa7aceee/transactions?fromBookingDate=2021-01-01&toBookingDate=2022-06-01")
                        .header("Authorization", "Bearer ${transactionToken}"),
                        ResponseAccountTransactions)

        then:
        transactions != null
        transactions.getData() != null
        transactions.getData().size() != 0
        transactions.getData().get(0) != null
        transactions.getData().get(0).getAmount() != 0.0
    }

    void "we can get credit card account limits and bills for a preloaded user" () {
        given:
        CreateConsent createConsent = createConsent('76109277673', 'CPF', [CREDIT_CARDS_ACCOUNTS_READ, CREDIT_CARDS_ACCOUNTS_LIMITS_READ, RESOURCES_READ, CREDIT_CARDS_ACCOUNTS_BILLS_READ,
                                                                           CREDIT_CARDS_ACCOUNTS_BILLS_TRANSACTIONS_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)
        PlainObject plainObject = new PlainObject(new PlainHeader(), new Payload([scope: "consents", client_id: "client1", subject: "ralph.bragg@gmail.com"]))
        def consentToken = plainObject.serialize()

        when:
        ResponseConsent returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST('/open-banking/consents/v1/consents', jsonRequest)
                        .header("Authorization", "Bearer ${consentToken}"),
                        ResponseConsent)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 5

        when:
        def cardsToken = new PlainObject(new PlainHeader(), new Payload([scope: "openid", client_id: "client1", subject: "ralph.bragg@gmail.com"])).serialize()
        ResponseCreditCardAccountsList cardAccountsList = client.toBlocking()
                .retrieve(HttpRequest.GET("/user/ralph.bragg@gmail.com/credit-card-accounts")
                        .header("Authorization", "Bearer ${cardsToken}"),
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
        def updateToken = new PlainObject(new PlainHeader(), new Payload([scope: "op:consent", client_id: "client1", subject: "ralph.bragg@gmail.com"])).serialize()
        log.info("OP consent token - {}", updateToken)
        ResponseConsentFull fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT('/open-banking/consents/v1/consents/' + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFull)
        then:
        fullConsent.getData().getSub() != null

        when:
        def limitsToken = new PlainObject(new PlainHeader(), new Payload([scope: "credit-cards-accounts consent:${fullConsent.getData().getConsentId().toString()}".toString(), client_id: "client1", subject: "ralph.bragg@gmail.com"])).serialize()
        log.info("OP consent token - {}", updateToken)
        ResponseCreditCardAccountsLimits limits = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/credit-cards-accounts/v1/accounts/${cardAccountsList.getData().get(0).getCreditCardAccountId()}/limits")
                        .header("Authorization", "Bearer ${limitsToken}"),
                        ResponseCreditCardAccountsLimits)

        then:
        noExceptionThrown()
        limits != null
        limits.getData() != null

        when:
        log.info("OP consent token - {}", updateToken)
        def request = HttpRequest.GET("/open-banking/credit-cards-accounts/v1/accounts/${cardAccountsList.getData().get(0).getCreditCardAccountId()}/bills?fromDueDate=2021-01-01&toDueDate=2022-06-15")
                .header("Authorization", "Bearer ${limitsToken}")

        then:
        request.getParameters() != null
        request.getParameters().get("fromDueDate") != null

        when:
        ResponseCreditCardAccountsBills bills = client.toBlocking()
                .retrieve(request,
                        ResponseCreditCardAccountsBills)

        then:
        noExceptionThrown()
        bills != null
        bills.getData() != null
        bills.getData().size() == 1
    }

    void "unarranged overdrafts have the correct links" () {
        given:
        CreateConsent createConsent = createConsent('76109277673', 'CPF', [LOANS_READ, LOANS_WARRANTIES_READ,
                                                                           LOANS_SCHEDULED_INSTALMENTS_READ, LOANS_PAYMENTS_READ,
                                                                           FINANCINGS_READ, FINANCINGS_WARRANTIES_READ,
                                                                           FINANCINGS_SCHEDULED_INSTALMENTS_READ, FINANCINGS_PAYMENTS_READ,
                                                                           UNARRANGED_ACCOUNTS_OVERDRAFT_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ,
                                                                           UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ,
                                                                           INVOICE_FINANCINGS_READ, INVOICE_FINANCINGS_WARRANTIES_READ,
                                                                           INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ, INVOICE_FINANCINGS_PAYMENTS_READ,
                                                                           RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)
        PlainObject plainObject = new PlainObject(new PlainHeader(), new Payload([scope: "consents", client_id: "client1", subject: "ralph.bragg@gmail.com"]))
        def consentToken = plainObject.serialize()

        when:
        ResponseConsent returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST('/open-banking/consents/v1/consents', jsonRequest)
                        .header("Authorization", "Bearer ${consentToken}"),
                        ResponseConsent)

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
        def updateToken = new PlainObject(new PlainHeader(), new Payload([scope: "op:consent", client_id: "client1", subject: "ralph.bragg@gmail.com"])).serialize()
        log.info("OP consent token - {}", updateToken)
        ResponseConsentFull fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT('/open-banking/consents/v1/consents/' + returnedConsent.getData().getConsentId(), mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFull)
        then:
        fullConsent.getData().getSub() != null

        when:
        def limitsToken = new PlainObject(new PlainHeader(), new Payload([scope: "unarranged-accounts-overdraft consent:${fullConsent.getData().getConsentId().toString()}".toString(), client_id: "client1", subject: "ralph.bragg@gmail.com"])).serialize()
        log.info("OP consent token - {}", updateToken)
        ResponseUnarrangedAccountOverdraftContractList overdrafts = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/unarranged-accounts-overdraft/v1/contracts")
                        .header("Authorization", "Bearer ${limitsToken}"),
                        ResponseUnarrangedAccountOverdraftContractList)

        then:
        noExceptionThrown()
        overdrafts != null
        overdrafts.getData() != null
        overdrafts.getData().size() == 1
        overdrafts.getLinks().getNext() == null
    }

    @Unroll
    void "We can do pages correctly"() {
        // set up the necessary consent
        given:
        CreateConsent createConsent = createConsent('97812797457', 'CPF', [LOANS_READ, LOANS_WARRANTIES_READ,
                                                                           LOANS_SCHEDULED_INSTALMENTS_READ, LOANS_PAYMENTS_READ,
                                                                           FINANCINGS_READ, FINANCINGS_WARRANTIES_READ,
                                                                           FINANCINGS_SCHEDULED_INSTALMENTS_READ, FINANCINGS_PAYMENTS_READ,
                                                                           UNARRANGED_ACCOUNTS_OVERDRAFT_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ,
                                                                           UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ,
                                                                           INVOICE_FINANCINGS_READ, INVOICE_FINANCINGS_WARRANTIES_READ,
                                                                           INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ, INVOICE_FINANCINGS_PAYMENTS_READ,
                                                                           RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)
        PlainObject plainObject = new PlainObject(new PlainHeader(), new Payload([scope: "consents", client_id: "client1", subject: "loan.guy@test.com"]))
        def consentToken = plainObject.serialize()

        when:
        ResponseConsent returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST('/open-banking/consents/v1/consents', jsonRequest)
                        .header("Authorization", "Bearer ${consentToken}"),
                        ResponseConsent)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 17

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(
                new UpdateConsentData()
                        .status(UpdateConsentData.StatusEnum.AUTHORISED)
                        .sub("loan.guy@test.com")
                        .clientId('1234')
                        .linkedLoanAccountIds(List.of('470072b3-0cdf-47f8-a073-b7ead47cc718', '4973a578-0148-4ebb-9593-920eac3bdb98', '35fc1140-50de-493a-bbe8-b0db153b8727')))
        def updateToken = new PlainObject(new PlainHeader(),
                new Payload([scope  : "op:consent", client_id: "client1",
                             subject: "loan.guy@test.com"])).serialize()
        log.info("OP consent token - {}", updateToken)
        ResponseConsentFull fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT('/open-banking/consents/v1/consents/' + returnedConsent.getData().getConsentId(),
                        mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFull)
        then:
        fullConsent.getData().getSub() != null

        // now grab a page
        when:
        def loanToken = new PlainObject(new PlainHeader(),
                new Payload([scope    : "loans consent:${fullConsent.getData().getConsentId().toString()}".toString(),
                             client_id: "client1", subject: "loan.guy@test.com"])).serialize()
        log.info("OP consent token - {}", updateToken)
        def uri = "/open-banking/loans/v1/contracts?page=${page}&page-size=${pageSize}"
        if (page == 0) {
            if (pageSize != 0) {
                uri = "/open-banking/loans/v1/contracts?page-size=${pageSize}"
            } else {
                uri = "/open-banking/loans/v1/contracts"
            }
        } else {
            if (pageSize == 0) {
                uri = "/open-banking/loans/v1/contracts?page=${page}"
            }
        }
        ResponseLoansContractList loans = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${loanToken}"),
                        ResponseLoansContractList)

        then:
        noExceptionThrown()
        loans != null
        loans.getData() != null
        loans.getData().size() == results
        prev == (loans.getLinks().getPrev() != null)
        next == (loans.getLinks().getNext() != null)

        where:
        page | pageSize | results | prev  | next
        0    | 1        | 1       | false | true // skip the param entirely
        1    | 0        | 3       | false | false // skip the param entirely
        0    | 0        | 3       | false | false // skip the param entirely
        2    | 0        | 0       | true  | false // skip the param entirely
        1    | 1        | 1       | false | true
        2    | 1        | 1       | true  | true
        3    | 1        | 1       | true  | false
        4    | 1        | 0       | true  | false
        1    | 2        | 2       | false | true
        2    | 2        | 1       | true  | false
        3    | 2        | 0       | true  | false
        1    | 3        | 3       | false | false
        2    | 3        | 0       | true  | false
        1    | 4        | 3       | false | false
        2    | 4        | 0       | true  | false
    }

    def "interest rates are formatted correctly" () {
        // set up the necessary consent
        given:
        CreateConsent createConsent = createConsent('97812797457', 'CPF', [LOANS_READ, LOANS_WARRANTIES_READ,
                                                                           LOANS_SCHEDULED_INSTALMENTS_READ, LOANS_PAYMENTS_READ,
                                                                           FINANCINGS_READ, FINANCINGS_WARRANTIES_READ,
                                                                           FINANCINGS_SCHEDULED_INSTALMENTS_READ, FINANCINGS_PAYMENTS_READ,
                                                                           UNARRANGED_ACCOUNTS_OVERDRAFT_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ,
                                                                           UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ,
                                                                           INVOICE_FINANCINGS_READ, INVOICE_FINANCINGS_WARRANTIES_READ,
                                                                           INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ, INVOICE_FINANCINGS_PAYMENTS_READ,
                                                                           RESOURCES_READ])
        String jsonRequest = mapper.writeValueAsString(createConsent)
        PlainObject plainObject = new PlainObject(new PlainHeader(), new Payload([scope: "consents", client_id: "client1", subject: "loan.guy@test.com"]))
        def consentToken = plainObject.serialize()

        when:
        ResponseConsent returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST('/open-banking/consents/v1/consents', jsonRequest)
                        .header("Authorization", "Bearer ${consentToken}"),
                        ResponseConsent)

        then:
        returnedConsent.getData().getCreationDateTime() != null
        returnedConsent.getData().getPermissions().size() == 17

        when:
        UpdateConsent consentUpdate = new UpdateConsent().data(
                new UpdateConsentData()
                        .status(UpdateConsentData.StatusEnum.AUTHORISED)
                        .sub("loan.guy@test.com")
                        .clientId('1234')
                        .linkedLoanAccountIds(List.of('470072b3-0cdf-47f8-a073-b7ead47cc718'))
                        .linkedFinancingAccountIds(List.of('8c49d82d-d5d3-4a2a-b790-92e08d6df77a')))
        def updateToken = new PlainObject(new PlainHeader(),
                new Payload([scope  : "op:consent", client_id: "client1",
                             subject: "loan.guy@test.com"])).serialize()
        log.info("OP consent token - {}", updateToken)
        ResponseConsentFull fullConsent = client.toBlocking()
                .retrieve(HttpRequest.PUT('/open-banking/consents/v1/consents/' + returnedConsent.getData().getConsentId(),
                        mapper.writeValueAsString(consentUpdate))
                        .header("Authorization", "Bearer ${updateToken}"),
                        ResponseConsentFull)
        then:
        fullConsent.getData().getSub() != null

        // now grab a page
        when:
        def financingToken = new PlainObject(new PlainHeader(),
                new Payload([scope    : "loans financings consent:${fullConsent.getData().getConsentId().toString()}".toString(),
                             client_id: "client1", subject: "loan.guy@test.com"])).serialize()
        log.info("OP consent token - {}", updateToken)
        def uri = "/open-banking/loans/v1/contracts/470072b3-0cdf-47f8-a073-b7ead47cc718"
        String response = client.toBlocking()
                .retrieve(HttpRequest.GET(uri)
                        .header("Authorization", "Bearer ${financingToken}"),
                        String)
        ResponseLoansContract loan = mapper.readValue(response, ResponseLoansContract)

        then:
        noExceptionThrown()
        loan != null
        loan.getData() != null
        loan.getData().getInterestRates() != null
        loan.getData().getInterestRates().size() != 0
        loan.getData().getInterestRates().get(0) != null
        loan.getData().getInterestRates().get(0).getPreFixedRate().toString() == "0.015"
        loan.getData().getInterestRates().get(0).getPostFixedRate().toString() == "0.0253"

        when:
        def finangingUri = "/open-banking/financings/v1/contracts/8c49d82d-d5d3-4a2a-b790-92e08d6df77a"
        String financingResponse = client.toBlocking()
                .retrieve(HttpRequest.GET(finangingUri)
                        .header("Authorization", "Bearer ${financingToken}"),
                        String)
        ResponseFinancingsContract financing = mapper.readValue(financingResponse, ResponseFinancingsContract)

        then:
        noExceptionThrown()
        financing != null
        financing.getData() != null
        financing.getData().getInterestRates() != null
        financing.getData().getInterestRates().size() != 0
        financing.getData().getInterestRates().get(0) != null
        financing.getData().getInterestRates().get(0).getPreFixedRate().toString() == "0.015"
        financing.getData().getInterestRates().get(0).getPostFixedRate().toString() == "0.0253"
    }


    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }
}
