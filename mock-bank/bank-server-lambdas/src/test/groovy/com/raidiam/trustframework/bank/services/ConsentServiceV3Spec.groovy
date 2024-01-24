package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.TestEntityDataFactory
import com.raidiam.trustframework.bank.controllers.ConsentFactory
import com.raidiam.trustframework.bank.domain.AccountHolderEntity
import com.raidiam.trustframework.bank.enums.AccountOrContractType
import com.raidiam.trustframework.bank.utils.BankLambdaUtils
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Stepwise
import spock.lang.Unroll

import javax.inject.Inject
import java.time.OffsetDateTime
import java.time.ZoneOffset

import static com.raidiam.trustframework.bank.TestEntityDataFactory.*
import static com.raidiam.trustframework.mockbank.models.generated.EnumConsentPermissions.*

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class ConsentServiceV3Spec extends CleanupSpecification {
    @Shared
    AccountHolderEntity testAccountHolder
    @Shared
    UpdateConsent updateRequest
    @Inject
    ConsentService service
    @Inject
    TestEntityDataFactory testEntityDataFactory

    def setup() {
        if (runSetup) {
            testAccountHolder = accountHolderRepository.save(anAccountHolder("1234567899", "CPF"))
            def account = accountRepository.save(anAccount(testAccountHolder.getAccountHolderId()))
            def account2 = accountRepository.save(anAccount(testAccountHolder.getAccountHolderId()))
            def creditCard = creditCardAccountsRepository.save(anCreditCardAccounts(testAccountHolder.getAccountHolderId()))
            def creditCard2 = creditCardAccountsRepository.save(anCreditCardAccounts(testAccountHolder.getAccountHolderId()))
            def loan = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(),
                    AccountOrContractType.LOAN,
                    EnumContractProductTypeLoans.EMPRESTIMOS.toString(), EnumContractProductSubTypeLoans.CAPITAL_GIRO_TETO_ROTATIVO.toString())
            def loan2 = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(),
                    AccountOrContractType.LOAN,
                    EnumContractProductTypeLoans.EMPRESTIMOS.toString(), EnumContractProductSubTypeLoans.CREDITO_PESSOAL_COM_CONSIGNACAO.toString())
            def financing = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(),
                    AccountOrContractType.FINANCING,
                    EnumProductType.FINANCIAMENTOS_IMOBILIARIOS.toString(), EnumProductSubType.CUSTEIO.toString())
            def financing2 = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(),
                    AccountOrContractType.FINANCING,
                    EnumProductType.FINANCIAMENTOS.toString(), EnumProductSubType.INVESTIMENTO.toString())
            def invoiceFinancing = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(),
                    AccountOrContractType.INVOICE_FINANCING,
                    EnumContractProductTypeInvoiceFinancings.DIREITOS_CREDITORIOS_DESCONTADOS.toString(), EnumContractProductSubTypeInvoiceFinancings.DESCONTO_CHEQUES.toString())
            def invoiceFinancing2 = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(),
                    AccountOrContractType.INVOICE_FINANCING,
                    EnumContractProductTypeInvoiceFinancings.DIREITOS_CREDITORIOS_DESCONTADOS.toString(), EnumContractProductSubTypeInvoiceFinancings.ANTECIPACAO_FATURA_CARTAO_CREDITO.toString())
            def overdraft = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(),
                    AccountOrContractType.UNARRANGED_ACCOUNT_OVERDRAFT,
                    ProductType.ADIANTAMENTO_A_DEPOSITANTES.toString(), ProductSubType.ADIANTAMENTO_A_DEPOSITANTES.toString())
            def overdraft2 = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(),
                    AccountOrContractType.UNARRANGED_ACCOUNT_OVERDRAFT,
                    ProductType.ADIANTAMENTO_A_DEPOSITANTES.toString(), ProductSubType.ADIANTAMENTO_A_DEPOSITANTES.toString())

            List<String> linkedAccountIds = [account.getAccountId().toString(), account2.getAccountId().toString()]
            List<String> linkedCreditCardAccountIds = [creditCard.getCreditCardAccountId().toString(), creditCard2.getCreditCardAccountId().toString()]
            List<String> linkedLoanAccountIds = [loan.getContractId().toString(), loan2.getContractId().toString()]
            List<String> linkedFinancingAccountIds = [financing.getContractId().toString(), financing2.getContractId().toString()]
            List<String> linkedInvoiceFinancingAccountIds = [invoiceFinancing.getContractId().toString(), invoiceFinancing2.getContractId().toString()]
            List<String> linkedOverdraftAccountIds = [overdraft.getContractId().toString(), overdraft2.getContractId().toString()]

            UpdateConsentData updateData = new UpdateConsentData()
            updateData.setStatus(UpdateConsentData.StatusEnum.AUTHORISED)
            updateData.setLinkedAccountIds(linkedAccountIds)
            updateData.setLinkedCreditCardAccountIds(linkedCreditCardAccountIds)
            updateData.setLinkedLoanAccountIds(linkedLoanAccountIds)
            updateData.setLinkedFinancingAccountIds(linkedFinancingAccountIds)
            updateData.setLinkedInvoiceFinancingAccountIds(linkedInvoiceFinancingAccountIds)
            updateData.setLinkedUnarrangedOverdraftAccountIds(linkedOverdraftAccountIds)

            updateRequest = new UpdateConsent()
            updateRequest.setData(updateData)
            runSetup = false
        }
    }


    def "we can create consent with different permission groups"() {
        given:
        def consentRequest = ConsentFactory.createConsentV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel, permissions)

        if (permissions.contains(CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ) || permissions.contains(CUSTOMERS_PERSONAL_ADITTIONALINFO_READ)) {
            consentRequest.getData().setBusinessEntity(null)
        }

        when:
        def response = service.createConsentV3(UUID.randomUUID().toString(), consentRequest)
        then:
        noExceptionThrown()
        def data = response.getData()
        data.getStatus() == EnumConsentStatus.AWAITING_AUTHORISATION
        data.getStatusUpdateDateTime() != null
        data.getExpirationDateTime() != null
        data.getCreationDateTime() != null
        data.getConsentId() != null
        data.getPermissions() == permissions

        where:
        permissions << [
                [CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ, RESOURCES_READ],
                [CUSTOMERS_PERSONAL_ADITTIONALINFO_READ, RESOURCES_READ],
                [CUSTOMERS_BUSINESS_IDENTIFICATIONS_READ, RESOURCES_READ],
                [CUSTOMERS_BUSINESS_ADITTIONALINFO_READ, RESOURCES_READ],
                [ACCOUNTS_READ, ACCOUNTS_BALANCES_READ, RESOURCES_READ],
                [ACCOUNTS_READ, ACCOUNTS_OVERDRAFT_LIMITS_READ, RESOURCES_READ],
                [ACCOUNTS_READ, ACCOUNTS_TRANSACTIONS_READ, RESOURCES_READ],
                [CREDIT_CARDS_ACCOUNTS_READ, CREDIT_CARDS_ACCOUNTS_LIMITS_READ, RESOURCES_READ],
                [CREDIT_CARDS_ACCOUNTS_READ, CREDIT_CARDS_ACCOUNTS_TRANSACTIONS_READ, RESOURCES_READ],
                [CREDIT_CARDS_ACCOUNTS_READ, CREDIT_CARDS_ACCOUNTS_BILLS_READ, CREDIT_CARDS_ACCOUNTS_BILLS_TRANSACTIONS_READ, RESOURCES_READ],
                [LOANS_READ, LOANS_WARRANTIES_READ,
                 LOANS_SCHEDULED_INSTALMENTS_READ, LOANS_PAYMENTS_READ,
                 FINANCINGS_READ, FINANCINGS_WARRANTIES_READ,
                 FINANCINGS_SCHEDULED_INSTALMENTS_READ, FINANCINGS_PAYMENTS_READ,
                 UNARRANGED_ACCOUNTS_OVERDRAFT_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ,
                 UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ,
                 INVOICE_FINANCINGS_READ, INVOICE_FINANCINGS_WARRANTIES_READ,
                 INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ, INVOICE_FINANCINGS_PAYMENTS_READ,
                 RESOURCES_READ],
                [BANK_FIXED_INCOMES_READ, CREDIT_FIXED_INCOMES_READ, FUNDS_READ, VARIABLE_INCOMES_READ,
                 TREASURE_TITLES_READ, RESOURCES_READ],
                [EXCHANGES_READ, RESOURCES_READ]

        ]
    }


    def "we cant create consent with with business and personal permissions at the same time"() {
        given:
        def consentRequest = ConsentFactory.createConsentV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel, permissions)
        when:
        def response = service.createConsentV3(UUID.randomUUID().toString(), consentRequest)
        then:
        def e = thrown(HttpStatusException)
        response == null
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "PERMISSAO_PF_PJ_EM_CONJUNTO: You must not request Business and Personal permissions in the same request"


        where:
        permissions << [
                [CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ, RESOURCES_READ, CUSTOMERS_BUSINESS_IDENTIFICATIONS_READ],
                [CUSTOMERS_PERSONAL_ADITTIONALINFO_READ, RESOURCES_READ, CUSTOMERS_BUSINESS_ADITTIONALINFO_READ],
        ]
    }

    def "we cant create consent with incomplete permission groups"() {
        given:
        def consentRequest = ConsentFactory.createConsentV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel, permissions)
        when:
        def response = service.createConsentV3(UUID.randomUUID().toString(), consentRequest)
        then:
        def e = thrown(HttpStatusException)
        response == null
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "COMBINACAO_PERMISSOES_INCORRETA: You must request all the permissions from a given set"


        where:
        permissions << [
                [CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ],
                [CUSTOMERS_PERSONAL_ADITTIONALINFO_READ],
                [ACCOUNTS_READ, ACCOUNTS_BALANCES_READ],
                [ACCOUNTS_READ, ACCOUNTS_OVERDRAFT_LIMITS_READ],
                [ACCOUNTS_READ, ACCOUNTS_TRANSACTIONS_READ],
                [CREDIT_CARDS_ACCOUNTS_READ, CREDIT_CARDS_ACCOUNTS_LIMITS_READ],
                [CREDIT_CARDS_ACCOUNTS_READ, CREDIT_CARDS_ACCOUNTS_TRANSACTIONS_READ],
                [CREDIT_CARDS_ACCOUNTS_READ, CREDIT_CARDS_ACCOUNTS_BILLS_READ, CREDIT_CARDS_ACCOUNTS_BILLS_TRANSACTIONS_READ],
                [LOANS_READ,
                 LOANS_SCHEDULED_INSTALMENTS_READ, LOANS_PAYMENTS_READ,
                 FINANCINGS_READ, FINANCINGS_WARRANTIES_READ,
                 FINANCINGS_SCHEDULED_INSTALMENTS_READ, FINANCINGS_PAYMENTS_READ,
                 UNARRANGED_ACCOUNTS_OVERDRAFT_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ,
                 UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ,
                 INVOICE_FINANCINGS_READ, INVOICE_FINANCINGS_WARRANTIES_READ,
                 INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ, INVOICE_FINANCINGS_PAYMENTS_READ,
                 RESOURCES_READ],
                [BANK_FIXED_INCOMES_READ, CREDIT_FIXED_INCOMES_READ, FUNDS_READ,
                 TREASURE_TITLES_READ],
                [EXCHANGES_READ]
        ]
    }

    def "we cant create consent with expiration date as current date + 2 years"() {
        given:
        def consentRequest = ConsentFactory.createConsentV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        consentRequest.getData().setExpirationDateTime(OffsetDateTime.now().plusYears(2))
        when:
        def response = service.createConsentV3(UUID.randomUUID().toString(), consentRequest)
        then:
        def e = thrown(HttpStatusException)
        response == null
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "Expiration time must be within a year"

    }

    def "we cant create consent with expiration date in the past"() {
        given:
        def consentRequest = ConsentFactory.createConsentV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        consentRequest.getData().setExpirationDateTime(OffsetDateTime.now().minusDays(2))
        when:
        def response = service.createConsentV3(UUID.randomUUID().toString(), consentRequest)
        then:
        def e = thrown(HttpStatusException)
        response == null
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "Expiration time must be in the future"

    }

    def "we can get consent"() {
        given:
        def consentRequest = ConsentFactory.createConsentV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        def clientId = UUID.randomUUID().toString()
        def createConsentResponse = service.createConsentV3(clientId, consentRequest)
        def consentId = createConsentResponse.getData().getConsentId()
        when:
        def getConsentResponse = service.getConsentV3(consentId, clientId)

        then:
        noExceptionThrown()
        def data = getConsentResponse.getData()
        data.consentId == createConsentResponse.data.consentId
        data.creationDateTime == createConsentResponse.data.creationDateTime
        data.status == createConsentResponse.data.status
        data.statusUpdateDateTime == createConsentResponse.data.statusUpdateDateTime
        data.permissions == createConsentResponse.data.permissions
        data.expirationDateTime == createConsentResponse.data.expirationDateTime
        data.rejection == null

    }


    def "we can get consent with MAX_DATE_REACHED rejection"() {
        given:
        def consentRequest = ConsentFactory.createConsentV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        consentRequest.getData().setExpirationDateTime(OffsetDateTime.now().plusSeconds(1))
        def clientId = UUID.randomUUID().toString()
        def createConsentResponse = service.createConsentV3(clientId, consentRequest)
        def consentId = createConsentResponse.getData().getConsentId()
        when:
        sleep(1000)
        def getConsentResponse = service.getConsentV3(consentId, clientId)

        then:
        noExceptionThrown()
        def data = getConsentResponse.getData()
        data.getStatus() == EnumConsentStatus.REJECTED
        data.getRejection().getRejectedBy() == EnumRejectedByV2.ASPSP
        data.getRejection().getReason().getCode() == EnumReasonCodeV2.CONSENT_MAX_DATE_REACHED

    }

    def "we cant get consent belonging to another client"() {
        given:
        def consentRequest = ConsentFactory.createConsentV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        def clientId = UUID.randomUUID().toString()
        def createConsentResponse = service.createConsentV3(clientId, consentRequest)
        def consentId = createConsentResponse.getData().getConsentId()
        when:
        def getConsentResponse = service.getConsentV3(consentId, UUID.randomUUID().toString())

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.FORBIDDEN
        e.getMessage() == "Requested a consent created with a different oauth client"
        getConsentResponse == null

    }

    def "we can delete authorised consent"() {
        given:
        def consentRequest = ConsentFactory.createConsentV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        def clientId = UUID.randomUUID().toString()
        def createConsentResponse = service.createConsentV3(clientId, consentRequest)
        def consentId = createConsentResponse.getData().getConsentId()
        consentRepository.findByConsentId(consentId).ifPresent {
            it.setStatus(EnumConsentStatus.AUTHORISED.toString())
            consentRepository.update(it)
        }

        when:
        service.deleteConsentV3(consentId, clientId)
        def getConsentResponse = service.getConsentV3(consentId, clientId)

        then:
        noExceptionThrown()
        def data = getConsentResponse.getData()
        data.consentId == createConsentResponse.data.consentId
        data.creationDateTime == createConsentResponse.data.creationDateTime
        data.status == EnumConsentStatus.REJECTED
        data.statusUpdateDateTime == createConsentResponse.data.statusUpdateDateTime
        data.permissions == createConsentResponse.data.permissions
        data.expirationDateTime == createConsentResponse.data.expirationDateTime
        data.rejection.reason.code == EnumReasonCodeV2.CUSTOMER_MANUALLY_REVOKED
        data.rejection.rejectedBy == EnumRejectedByV2.USER
    }


    def "we can create consent extension"() {
        given:
        def customerIpAddress = "0.0.0.0"
        def customerUserAgent = "User_agent"
        def consentReq = ConsentFactory.createConsentV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        def consent = service.createConsentV3(UUID.randomUUID().toString(), consentReq)
        def consentId = consent.getData().getConsentId()
        def updatedConsent = consentRepository.findByConsentId(consentId).orElse(null)
        assert updatedConsent != null
        updatedConsent.setStatus(EnumConsentStatus.AUTHORISED.toString())
        consentRepository.update(updatedConsent)
        def consentStatusUpdateDateTime = updatedConsent.getStatusUpdateDateTime()
        def consentExpirationDateTime = updatedConsent.getExpirationDateTime()


        def consentExtendsReq = ConsentFactory.createConsentExtendsV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel)

        when:
        def consentExtendsResponse = service.createConsentExtensionV3(consentId, consentExtendsReq, customerIpAddress, customerUserAgent)
        def extensions = consentExtensionRepository.findByConsentId(consentId)
        updatedConsent = consentRepository.findByConsentId(consentId).orElse(null)

        then:
        noExceptionThrown()
        consentExtendsResponse != null

        def expirationDateTime = consentExtendsReq.getData().getExpirationDateTime()
        consentExtendsResponse.getData().getExpirationDateTime() == expirationDateTime

        extensions != null
        extensions.size() == 1
        def extension = extensions.get(0)
        extension.getExpirationDateTime() == BankLambdaUtils.offsetDateToDate(expirationDateTime)
        extension.getConsentId() == consentId
        extension.getConsent() == updatedConsent
        extension.getRequestDateTime() != null
        extension.getRequestDateTime() != updatedConsent.getStatusUpdateDateTime()
        extension.getXCustomerUserAgent() == customerUserAgent
        extension.getXFapiCustomerIpAddress() == customerIpAddress
        extension.getPreviousExpirationDateTime() == consentExpirationDateTime

        def loggedUserDocument = consentExtendsReq.getData().getLoggedUser().getDocument()
        extension.getLoggedDocumentIdentification() == loggedUserDocument.getIdentification()
        extension.getLoggedDocumentRel() == loggedUserDocument.getRel()

        updatedConsent != null
        updatedConsent.getExpirationDateTime() == BankLambdaUtils.offsetDateToDate(expirationDateTime)
        updatedConsent.getStatusUpdateDateTime() == consentStatusUpdateDateTime

        when:
        consentExtendsReq = ConsentFactory.createConsentExtends(testAccountHolder.documentIdentification, testAccountHolder.documentRel)
        consentExtendsReq.getData().setExpirationDateTime(consentExtendsReq.getData().getExpirationDateTime().plusMonths(1))
        expirationDateTime = consentExtendsReq.getData().getExpirationDateTime()
        service.createConsentExtension(consentId, consentExtendsReq)
        extensions = consentExtensionRepository.findByConsentId(consentId)
        updatedConsent = consentRepository.findByConsentId(consentId).orElse(null)


        then:
        noExceptionThrown()
        extensions.size() == 2
        updatedConsent != null
        updatedConsent.getExpirationDateTime() == BankLambdaUtils.offsetDateToDate(expirationDateTime)
        updatedConsent.getStatusUpdateDateTime() == consentStatusUpdateDateTime

        when:
        consentExtendsReq = ConsentFactory.createConsentExtends(testAccountHolder.documentIdentification, testAccountHolder.documentRel)
        consentExtendsReq.getData().setExpirationDateTime(OffsetDateTime.of(2300, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
        expirationDateTime = consentExtendsReq.getData().getExpirationDateTime()
        service.createConsentExtension(consentId, consentExtendsReq)
        extensions = consentExtensionRepository.findByConsentId(consentId)
        updatedConsent = consentRepository.findByConsentId(consentId).orElse(null)


        then:
        noExceptionThrown()
        extensions.size() == 3

        updatedConsent != null
        updatedConsent.getExpirationDateTime() == BankLambdaUtils.offsetDateToDate(expirationDateTime)
        updatedConsent.getStatusUpdateDateTime() == consentStatusUpdateDateTime

    }

    def "we cant create consent extension if expiration time is in the past or more than one year"() {
        def customerIpAddress = "0.0.0.0"
        def customerUserAgent = "User_agent"
        def consentReq = ConsentFactory.createConsentV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        def consent = service.createConsentV3(UUID.randomUUID().toString(), consentReq)
        def consentId = consent.getData().getConsentId()
        def updatedConsent = consentRepository.findByConsentId(consentId).orElse(null)
        assert updatedConsent != null
        updatedConsent.setStatus(EnumConsentStatus.AUTHORISED.toString())
        consentRepository.update(updatedConsent)
        def consentExtendsReq = ConsentFactory.createConsentExtendsV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel)

        when:
        def overOneYearDate = OffsetDateTime.now().plusYears(5)
        consentExtendsReq.getData().setExpirationDateTime(overOneYearDate)
        service.createConsentExtensionV3(consentId, consentExtendsReq, customerIpAddress, customerUserAgent)

        then:
        def e = thrown(HttpStatusException)
        e.status == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == String.format("%s: new expirationDateTime cannot be in the past or more than one year", EnumConsentExtendsErrorCode.DATA_EXPIRACAO_INVALIDA)

        when:
        def oldDate = OffsetDateTime.now().minusYears(1)
        consentExtendsReq.getData().setExpirationDateTime(oldDate)
        service.createConsentExtensionV3(consentId, consentExtendsReq, customerIpAddress, customerUserAgent)

        then:
        def e2 = thrown(HttpStatusException)
        e2.status == HttpStatus.UNPROCESSABLE_ENTITY
        e2.getMessage() == String.format("%s: new expirationDateTime cannot be in the past or more than one year", EnumConsentExtendsErrorCode.DATA_EXPIRACAO_INVALIDA)
    }

    def "we can create consent extension if expiration time is less than or equal to the expiration time of the consent"() {
        given:
        def customerIpAddress = "0.0.0.0"
        def customerUserAgent = "User_agent"
        def consentReq = ConsentFactory.createConsentV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        def consent = service.createConsentV3(UUID.randomUUID().toString(), consentReq)
        def consentId = consent.getData().getConsentId()
        def updatedConsent = consentRepository.findByConsentId(consentId).orElse(null)
        assert updatedConsent != null
        updatedConsent.setStatus(EnumConsentStatus.AUTHORISED.toString())
        consentRepository.update(updatedConsent)
        def consentExtendsReq = ConsentFactory.createConsentExtendsV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel)
        service.createConsentExtensionV3(consentId, consentExtendsReq, customerIpAddress, customerUserAgent)

        when:
        def extensionResponse = service.createConsentExtensionV3(consentId, consentExtendsReq, customerIpAddress, customerUserAgent)
        def extensions = consentExtensionRepository.findByConsentId(consentId)

        then:
        noExceptionThrown()
        extensionResponse != null
        extensions.size() == 2

        when:
        def oldDate = BankLambdaUtils.dateToOffsetDate(updatedConsent.getExpirationDateTime()).minusDays(1)
        consentExtendsReq.getData().setExpirationDateTime(oldDate)
        extensionResponse = service.createConsentExtensionV3(consentId, consentExtendsReq, customerIpAddress, customerUserAgent)
        extensions = consentExtensionRepository.findByConsentId(consentId)

        then:
        noExceptionThrown()
        extensionResponse != null
        extensions.size() == 3
    }

    def "we cant create consent extension if consent status is not AUTHORISED"() {
        def customerIpAddress = "0.0.0.0"
        def customerUserAgent = "User_agent"
        def consentReq = ConsentFactory.createConsentV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        def consent = service.createConsentV3(UUID.randomUUID().toString(), consentReq)
        def consentId = consent.getData().getConsentId()
        def updatedConsent = consentRepository.findByConsentId(consentId).orElse(null)
        assert updatedConsent != null
        updatedConsent.setStatus(consentStatus.toString())
        consentRepository.update(updatedConsent)
        def consentExtendsReq = ConsentFactory.createConsentExtendsV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel)
        when:
        service.createConsentExtensionV3(consentId, consentExtendsReq, customerIpAddress, customerUserAgent)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == String.format("%s: only AUTHORISED consents can be extended", EnumConsentExtendsErrorCode.ESTADO_CONSENTIMENTO_INVALIDO)

        where:
        consentStatus << [EnumConsentStatus.AWAITING_AUTHORISATION, EnumConsentStatus.REJECTED]
    }

    def "we can trigger create consent extension mock responses"() {
        def customerIpAddress = "0.0.0.0"
        def customerUserAgent = "User_agent"
        def consentReq = ConsentFactory.createConsentV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        def consent = service.createConsentV3(UUID.randomUUID().toString(), consentReq)
        def consentId = consent.getData().getConsentId()
        def consentExtendsReq = ConsentFactory.createConsentExtendsV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel)
        consentExtendsReq.getData().getLoggedUser().getDocument().setIdentification(identification)
        when:
        service.createConsentExtensionV3(consentId, consentExtendsReq, customerIpAddress, customerUserAgent)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == message

        where:
        identification | message
        "00000000000"  | String.format("%s: %s", EnumConsentExtendsErrorCode.ESTADO_CONSENTIMENTO_INVALIDO, EnumConsentExtendsErrorCode.ESTADO_CONSENTIMENTO_INVALIDO)
        "00000000001"  | String.format("%s: %s", EnumConsentExtendsErrorCode.REFRESH_TOKEN_JWT, EnumConsentExtendsErrorCode.REFRESH_TOKEN_JWT)
        "00000000002"  | String.format("%s: %s", EnumConsentExtendsErrorCode.DEPENDE_MULTIPLA_ALCADA, EnumConsentExtendsErrorCode.DEPENDE_MULTIPLA_ALCADA)
    }

    @Unroll
    def "we cant create consent extension if user identification is different"() {
        given:
        def customerIpAddress = "0.0.0.0"
        def customerUserAgent = "User_agent"
        def consentReq = ConsentFactory.createConsentV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        def consent = service.createConsentV3(UUID.randomUUID().toString(), consentReq)
        def consentId = consent.getData().getConsentId()
        def updatedConsent = consentRepository.findByConsentId(consentId).orElse(null)
        assert updatedConsent != null
        updatedConsent.setStatus(EnumConsentStatus.AUTHORISED.toString())
        consentRepository.update(updatedConsent)
        def consentExtendsReq = ConsentFactory.createConsentExtendsV3(consentExtendtIdentification, consentExtendRel)

        when:
        def consentExtendsResponse = service.createConsentExtensionV3(consentId, consentExtendsReq, customerIpAddress, customerUserAgent)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNAUTHORIZED
        e.getMessage() == "unauthorized"
        consentExtendsResponse == null

        where:
        consentExtendtIdentification | consentExtendRel
        "1234567899"                 | "TEST"
        "11111111111"                | "CPF"
        "11111111111"                | "TEST"
    }

    def "we can create consent extension v3 without expirationDateTime in the request"() {
        given:
        def customerIpAddress = "0.0.0.0"
        def customerUserAgent = "User_agent"
        def consentReq = ConsentFactory.createConsentV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        def consent = service.createConsentV3(UUID.randomUUID().toString(), consentReq)
        def consentId = consent.getData().getConsentId()
        def updatedConsent = consentRepository.findByConsentId(consentId).orElse(null)
        updatedConsent.setStatus(EnumConsentStatus.AUTHORISED.toString())
        consentRepository.update(updatedConsent)

        def consentExtendsReq = ConsentFactory.createConsentExtendsV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel)
        consentExtendsReq.getData().setExpirationDateTime(null) // Remove expiration time

        when:
        def extensionResponse = service.createConsentExtensionV3(consentId, consentExtendsReq, customerIpAddress, customerUserAgent)
        def extensions = consentExtensionRepository.findByConsentId(consentId)

        then:
        noExceptionThrown()
        extensionResponse != null
        extensions.size() == 1
        extensions[0].getExpirationDateTime() == null

    }


    def "we can get consent extensions v3"() {
        given:
        def customerIpAddress = "0.0.0.0"
        def customerUserAgent = "User_agent"
        def consentReq = ConsentFactory.createConsentV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        def consent = service.createConsentV3(UUID.randomUUID().toString(), consentReq)
        def consentId = consent.getData().getConsentId()
        def updatedConsent = consentRepository.findByConsentId(consentId).orElse(null)
        assert updatedConsent != null
        updatedConsent.setStatus(EnumConsentStatus.AUTHORISED.toString())
        consentRepository.update(updatedConsent)
        def consentExtendsReq = ConsentFactory.createConsentExtendsV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel)
        service.createConsentExtensionV3(consentId, consentExtendsReq, customerIpAddress, customerUserAgent)
        def firstDate = consentExtendsReq.getData().getExpirationDateTime()
        def secondDate = firstDate.plusMonths(2)
        consentExtendsReq.getData().setExpirationDateTime(secondDate)
        service.createConsentExtensionV3(consentId, consentExtendsReq, customerIpAddress, customerUserAgent)
        def thirdDate = firstDate.plusDays(1)
        consentExtendsReq.getData().setExpirationDateTime(thirdDate)
        service.createConsentExtensionV3(consentId, consentExtendsReq, customerIpAddress, customerUserAgent)

        def forthDate = null
        consentExtendsReq.getData().setExpirationDateTime(forthDate)
        service.createConsentExtensionV3(consentId, consentExtendsReq, customerIpAddress, customerUserAgent)

        def orderControl = [
                forthDate,
                thirdDate,
                secondDate,
                firstDate
        ]

        when:
        def getConsentExtensionsResponse = service.getConsentExtensionsV3(consentId)
        updatedConsent = consentRepository.findByConsentId(consentId).orElse(null)

        then:
        noExceptionThrown()
        def data = getConsentExtensionsResponse.getData()
        data.size() == 4

        updatedConsent.getExpirationDateTime() == BankLambdaUtils.offsetDateToDate(data.get(0).getExpirationDateTime())

        data.size().times {
            def ext = data.get(it)
            assert ext.getExpirationDateTime() == orderControl[it]
            assert ext.getRequestDateTime() != null
            assert ext.getPreviousExpirationDateTime() == (it == data.size() - 1 ? consent.getData().getExpirationDateTime() : orderControl[it + 1])
            assert ext.getXFapiCustomerIpAddress() == customerIpAddress
            assert ext.getXCustomerUserAgent() == customerUserAgent
            def document = ext.getLoggedUser().getDocument()
            assert document.getRel() == consentExtendsReq.getData().getLoggedUser().getDocument().getRel()
            assert document.getIdentification() == consentExtendsReq.getData().getLoggedUser().getDocument().getIdentification()
        }
    }

    def "an empty array is returned if no consent extensions exist"() {
        given:
        def consentReq = ConsentFactory.createConsentV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        def consent = service.createConsentV3(UUID.randomUUID().toString(), consentReq)
        def consentId = consent.getData().getConsentId()
        def updatedConsent = consentRepository.findByConsentId(consentId).orElse(null)
        assert updatedConsent != null
        updatedConsent.setStatus(EnumConsentStatus.AUTHORISED.toString())
        consentRepository.update(updatedConsent)

        when:
        def getConsentExtensionsResponse = service.getConsentExtensionsV3(consentId)

        then:
        getConsentExtensionsResponse.getData() != null
        getConsentExtensionsResponse.getData().size() == 0
    }

    def "We cannt delete a consent already rejected"() {
        given:
        CreateConsentV3 consent = ConsentFactory.createConsentV3(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)

        when:
        ResponseConsentV3 response = service.createConsentV3('client1', consent)

        then:
        response.getData().getStatus() == EnumConsentStatus.AWAITING_AUTHORISATION

        when:
        var consentEntity = consentRepository.findByConsentId(response.getData().getConsentId()).get()
        consentEntity.setStatus(EnumConsentStatus.REJECTED.name())
        consentRepository.update(consentEntity)
        service.deleteConsentV3(consentEntity.getConsentId(), 'client1')

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "CONSENTIMENTO_EM_STATUS_REJEITADO: Consent request already in a rejected state"
    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }

}
