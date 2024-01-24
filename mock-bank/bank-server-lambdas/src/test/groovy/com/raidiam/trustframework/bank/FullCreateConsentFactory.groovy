package com.raidiam.trustframework.bank

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.Payload
import com.nimbusds.jose.PlainHeader
import com.nimbusds.jose.PlainObject
import com.raidiam.trustframework.bank.domain.AccountHolderEntity
import com.raidiam.trustframework.bank.enums.ContractTypeEnum
import com.raidiam.trustframework.mockbank.models.generated.BusinessEntity
import com.raidiam.trustframework.mockbank.models.generated.BusinessEntityDocument
import com.raidiam.trustframework.mockbank.models.generated.CreateConsentV2
import com.raidiam.trustframework.mockbank.models.generated.CreateConsentV2Data
import com.raidiam.trustframework.mockbank.models.generated.EnumConsentPermissions
import com.raidiam.trustframework.mockbank.models.generated.LoggedUser
import com.raidiam.trustframework.mockbank.models.generated.LoggedUserDocument
import com.raidiam.trustframework.mockbank.models.generated.ResponseConsentFullV2
import com.raidiam.trustframework.mockbank.models.generated.ResponseConsentV2
import com.raidiam.trustframework.mockbank.models.generated.UpdateConsent
import com.raidiam.trustframework.mockbank.models.generated.UpdateConsentData
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.annotation.Client
import io.micronaut.rxjava2.http.client.RxHttpClient
import org.apache.commons.lang3.RandomStringUtils
import spock.lang.Shared

import javax.inject.Inject
import java.time.OffsetDateTime
import java.util.stream.Collectors

import static com.raidiam.trustframework.mockbank.models.generated.EnumConsentPermissions.*
import static com.raidiam.trustframework.bank.enums.ContractTypeEnum.*

class FullCreateConsentFactory extends CleanupLocalStackSpecification {
    @Inject
    @Client('/')
    RxHttpClient client
    @Shared
    static mapper = new ObjectMapper()
    @Shared
    String clientId = "client1"
    @Shared
    String subject = "ralph.bragg@gmail.com"
    @Shared
    String url = "/open-banking/consents/v2/consents"

    def setupSpec() {
        mapper.findAndRegisterModules()
    }

    //POST and PUT Account consent
    String createConsentWithAccountPermissions(AccountHolderEntity accountHolder, String accountId) {
        def consentId = createConsentViaController(createConsentV2Request(accountHolder,
                [RESOURCES_READ, ACCOUNTS_READ, ACCOUNTS_BALANCES_READ,
                 ACCOUNTS_OVERDRAFT_LIMITS_READ, ACCOUNTS_TRANSACTIONS_READ]))

        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .clientId(clientId).sub(subject).linkedAccountIds(List.of(accountId)))
        client.toBlocking().retrieve(HttpRequest.PUT("${url}/${consentId}", mapper.writeValueAsString(consentUpdate))
                .header("Authorization", "Bearer ${createToken("op:consent")}"), ResponseConsentFullV2)
        return createToken("accounts consent:" + consentId)
    }

    //POST and PUT Credit Card Account consent
    String createConsentWithCreditCardAccountPermissions(AccountHolderEntity accountHolder, String creditCardAccountId) {
        def consentId = createConsentViaController(createConsentV2Request(accountHolder,
                [CREDIT_CARDS_ACCOUNTS_READ, CREDIT_CARDS_ACCOUNTS_BILLS_READ, RESOURCES_READ,
                 CREDIT_CARDS_ACCOUNTS_LIMITS_READ, CREDIT_CARDS_ACCOUNTS_TRANSACTIONS_READ,
                 CREDIT_CARDS_ACCOUNTS_BILLS_TRANSACTIONS_READ]))

        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .clientId(clientId).sub(subject).linkedCreditCardAccountIds(List.of(creditCardAccountId)))
        client.toBlocking().retrieve(HttpRequest.PUT("${url}/${consentId}", mapper.writeValueAsString(consentUpdate))
                .header("Authorization", "Bearer ${createToken("op:consent")}"), ResponseConsentFullV2)
        return createToken("credit-cards-accounts consent:" + consentId)
    }

    //POST and PUT Contracts consent
    String createConsentWithContractPermissions(AccountHolderEntity accountHolder, String contractId, ContractTypeEnum type) {
        def consentId = createConsentViaController(createConsentV2Request(accountHolder,
                [RESOURCES_READ,
                 FINANCINGS_READ, FINANCINGS_PAYMENTS_READ, FINANCINGS_SCHEDULED_INSTALMENTS_READ, FINANCINGS_WARRANTIES_READ,
                 INVOICE_FINANCINGS_READ, INVOICE_FINANCINGS_PAYMENTS_READ, INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ, INVOICE_FINANCINGS_WARRANTIES_READ,
                 LOANS_READ, LOANS_PAYMENTS_READ, LOANS_SCHEDULED_INSTALMENTS_READ, LOANS_WARRANTIES_READ,
                 UNARRANGED_ACCOUNTS_OVERDRAFT_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ]))

        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData()
                .status(UpdateConsentData.StatusEnum.AUTHORISED).clientId(clientId).sub(subject))
        switch (type) {
            case LOAN:
                consentUpdate.getData().linkedLoanAccountIds(List.of(contractId))
                break
            case FINANCING:
                consentUpdate.getData().linkedFinancingAccountIds(List.of(contractId))
                break
            case INVOICE_FINANCING:
                consentUpdate.getData().linkedInvoiceFinancingAccountIds(List.of(contractId))
                break
            case UNARRANGED_ACCOUNT_OVERDRAFT:
                consentUpdate.getData().linkedUnarrangedOverdraftAccountIds(List.of(contractId))
                break
        }
        client.toBlocking().retrieve(HttpRequest.PUT("${url}/${consentId}", mapper.writeValueAsString(consentUpdate))
                .header("Authorization", "Bearer ${createToken("op:consent")}"), ResponseConsentFullV2)
        return createToken(type.toString() + " consent:" + consentId)
    }

    //POST and PUT Customers consent
    String createConsentWithCustomerPermissions(AccountHolderEntity accountHolder, boolean isBusiness) {
        def consentId = createConsentViaController(isBusiness ? createConsentV2Request(accountHolder,
                    [RESOURCES_READ, CUSTOMERS_BUSINESS_IDENTIFICATIONS_READ, CUSTOMERS_BUSINESS_ADITTIONALINFO_READ])
                : createConsentV2RequestWithoutBusinessEntity(accountHolder,
                    [RESOURCES_READ, CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ, CUSTOMERS_PERSONAL_ADITTIONALINFO_READ]))

        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .clientId(clientId).sub(subject))
        client.toBlocking().retrieve(HttpRequest.PUT("${url}/${consentId}", mapper.writeValueAsString(consentUpdate))
                .header("Authorization", "Bearer ${createToken("op:consent")}"), ResponseConsentFullV2)
        return createToken("customers consent:" + consentId)
    }

    //POST and PUT Resources consent
    String createConsentWithResourcesPermissions(AccountHolderEntity accountHolder) {
        def consentId = createConsentViaController(createConsentV2Request(accountHolder, [RESOURCES_READ, ACCOUNTS_READ, ACCOUNTS_BALANCES_READ,
                                                                                          ACCOUNTS_OVERDRAFT_LIMITS_READ, ACCOUNTS_TRANSACTIONS_READ,
                                                                                          CREDIT_CARDS_ACCOUNTS_READ, CREDIT_CARDS_ACCOUNTS_BILLS_READ,
                                                                                          CREDIT_CARDS_ACCOUNTS_LIMITS_READ, CREDIT_CARDS_ACCOUNTS_TRANSACTIONS_READ,
                                                                                          CREDIT_CARDS_ACCOUNTS_BILLS_TRANSACTIONS_READ,
                                                                                          FINANCINGS_READ, FINANCINGS_PAYMENTS_READ, FINANCINGS_SCHEDULED_INSTALMENTS_READ, FINANCINGS_WARRANTIES_READ,
                                                                                          INVOICE_FINANCINGS_READ, INVOICE_FINANCINGS_PAYMENTS_READ, INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ, INVOICE_FINANCINGS_WARRANTIES_READ,
                                                                                          LOANS_READ, LOANS_PAYMENTS_READ, LOANS_SCHEDULED_INSTALMENTS_READ, LOANS_WARRANTIES_READ,
                                                                                          UNARRANGED_ACCOUNTS_OVERDRAFT_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ]))
        createResourcesBlock(consentId, accountHolder)
    }

    //POST and PUT exchanges consent
    String createConsentWithExchangesPermissions(AccountHolderEntity accountHolder, String creditCardAccountId) {
        def consentId = createConsentViaController(createConsentV2Request(accountHolder,
                [EXCHANGES_READ, RESOURCES_READ]))

        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .clientId(clientId).sub(subject).linkedCreditCardAccountIds(List.of(creditCardAccountId)))
        client.toBlocking().retrieve(HttpRequest.PUT("${url}/${consentId}", mapper.writeValueAsString(consentUpdate))
                .header("Authorization", "Bearer ${createToken("op:consent")}"), ResponseConsentFullV2)
        return createToken("exchanges consent:" + consentId)
    }

    String createPermissionsOnlyForCustomersBusiness(AccountHolderEntity accountHolder) {
        def consentId = createConsentViaController(createConsentV2Request(accountHolder, [RESOURCES_READ,
                                                                                          CUSTOMERS_BUSINESS_IDENTIFICATIONS_READ,
                                                                                          CUSTOMERS_BUSINESS_ADITTIONALINFO_READ]))
        createResourcesBlock(consentId, accountHolder)
    }

    String createPermissionsOnlyForCustomersPersonal(AccountHolderEntity accountHolder) {
        def consentId = createConsentViaController(createConsentV2RequestWithoutBusinessEntity(accountHolder, [RESOURCES_READ,
                                                                                          CUSTOMERS_PERSONAL_ADITTIONALINFO_READ,
                                                                                          CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ]))
        createResourcesBlock(consentId, accountHolder)
    }

    private String createResourcesBlock(String consentId, AccountHolderEntity accountHolder) {
        UpdateConsent consentUpdate = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED).clientId(clientId).sub(subject))

        def accounts = accountRepository.findByAccountHolderUserId(accountHolder.getUserId()).stream()
                .map(acc -> acc.getAccountId().toString()).collect(Collectors.toList())
        consentUpdate.getData().linkedAccountIds(accounts)

        def creditCards = creditCardAccountsRepository.findByAccountHolderUserId(accountHolder.getUserId()).stream()
                .map(cred -> cred.getCreditCardAccountId().toString()).collect(Collectors.toList())
        consentUpdate.getData().linkedCreditCardAccountIds(creditCards)

        def loans = contractsRepository.findByAccountHolderUserIdAndContractType(accountHolder.getUserId(), LOAN.name()).stream()
                .map(contr -> contr.getContractId().toString()).collect(Collectors.toList())
        consentUpdate.getData().linkedLoanAccountIds(loans)

        def financings = contractsRepository.findByAccountHolderUserIdAndContractType(accountHolder.getUserId(), FINANCING.name()).stream()
                .map(contr -> contr.getContractId().toString()).collect(Collectors.toList())
        consentUpdate.getData().linkedFinancingAccountIds(financings)

        def invoiceFinancings = contractsRepository.findByAccountHolderUserIdAndContractType(accountHolder.getUserId(), INVOICE_FINANCING.name()).stream()
                .map(contr -> contr.getContractId().toString()).collect(Collectors.toList())
        consentUpdate.getData().linkedInvoiceFinancingAccountIds(invoiceFinancings)

        def overdrafts = contractsRepository.findByAccountHolderUserIdAndContractType(accountHolder.getUserId(), UNARRANGED_ACCOUNT_OVERDRAFT.name()).stream()
                .map(contr -> contr.getContractId().toString()).collect(Collectors.toList())
        consentUpdate.getData().linkedUnarrangedOverdraftAccountIds(overdrafts)


        client.toBlocking().retrieve(HttpRequest.PUT("${url}/${consentId}", mapper.writeValueAsString(consentUpdate))
                .header("Authorization", "Bearer ${createToken("op:consent")}"), ResponseConsentFullV2)
        return createToken("resources consent:" + consentId)
    }


    private String createConsentViaController(CreateConsentV2 createConsent) {
        ResponseConsentV2 returnedConsent = client.toBlocking()
                .retrieve(HttpRequest.POST(url, mapper.writeValueAsString(createConsent))
                        .header("Authorization", "Bearer ${createToken("consents")}"), ResponseConsentV2)
        return returnedConsent.getData().getConsentId()
    }

    String createToken(String scope) {
        return new PlainObject(new PlainHeader(), new Payload([scope: scope, client_id: clientId, subject: subject])).serialize()
    }

    private CreateConsentV2 createConsentV2Request(AccountHolderEntity accountHolder, List<EnumConsentPermissions> permissions) {
        CreateConsentV2Data consentData = new CreateConsentV2Data()
                .expirationDateTime((OffsetDateTime.now().plusDays(3L)))
                .permissions(permissions)

        CreateConsentV2 consentRequest = new CreateConsentV2().data(consentData)
        consentRequest.data.businessEntity(new BusinessEntity()
                .document(new BusinessEntityDocument()
                        .identification(RandomStringUtils.random(14, false, true))
                        .rel("ASDF")))
        consentRequest.data.loggedUser(new LoggedUser()
                .document(new LoggedUserDocument()
                        .identification(accountHolder.getDocumentIdentification())
                        .rel(accountHolder.getDocumentRel())))
        consentRequest
    }

    private CreateConsentV2 createConsentV2RequestWithoutBusinessEntity(AccountHolderEntity accountHolder, List<EnumConsentPermissions> permissions) {
        CreateConsentV2Data consentData = new CreateConsentV2Data()
                .expirationDateTime((OffsetDateTime.now().plusDays(3L)))
                .permissions(permissions)

        CreateConsentV2 consentRequest = new CreateConsentV2().data(consentData)
        consentRequest.data.loggedUser(new LoggedUser()
                .document(new LoggedUserDocument()
                        .identification(accountHolder.getDocumentIdentification())
                        .rel(accountHolder.getDocumentRel())))
        consentRequest
    }
}