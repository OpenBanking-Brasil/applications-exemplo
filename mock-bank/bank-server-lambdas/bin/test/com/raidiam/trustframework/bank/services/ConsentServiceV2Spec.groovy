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
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

import static com.raidiam.trustframework.bank.TestEntityDataFactory.*

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class ConsentServiceV2Spec extends CleanupSpecification {
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

    def "We can update a consent"() {
        given:
        CreateConsent consent = ConsentFactory.createConsent(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)

        def account = accountRepository.save(anAccount(testAccountHolder))
        def account2 = accountRepository.save(anAccount(testAccountHolder))
        def creditCard = creditCardAccountsRepository.save(anCreditCardAccounts(testAccountHolder.getAccountHolderId()))
        def creditCard2 = creditCardAccountsRepository.save(anCreditCardAccounts(testAccountHolder.getAccountHolderId()))
        def loan = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(), AccountOrContractType.LOAN, "a", "b")
        def loan2 = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(), AccountOrContractType.LOAN, "a", "b")
        def financing = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(), AccountOrContractType.FINANCING, "a", "b")
        def financing2 = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(), AccountOrContractType.FINANCING, "a", "b")
        def invoiceFinancing = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(), AccountOrContractType.INVOICE_FINANCING, "a", "b")
        def invoiceFinancing2 = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(), AccountOrContractType.INVOICE_FINANCING, "a", "b")
        def overdraft = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(), AccountOrContractType.UNARRANGED_ACCOUNT_OVERDRAFT, "a", "b")
        def overdraft2 = testEntityDataFactory.createAndSaveFullContract(testAccountHolder.getAccountHolderId(), AccountOrContractType.UNARRANGED_ACCOUNT_OVERDRAFT, "a", "b")
        def bankFixedIncome = testEntityDataFactory.createAndSaveBankFixedIncome(testAccountHolder.getAccountHolderId())
        def creditFixedIncome = testEntityDataFactory.createAndSaveCreditFixedIncome(testAccountHolder.getAccountHolderId())
        def variableIncome = testEntityDataFactory.createAndSaveVariableIncome(testAccountHolder.getAccountHolderId())
        def TreasureTitle = testEntityDataFactory.createAndSaveTreasureTitle(testAccountHolder.getAccountHolderId())
        def fund = testEntityDataFactory.createAndSaveFund(testAccountHolder.getAccountHolderId())

        List<String> linkedAccountIds = [account.getAccountId().toString(), account2.getAccountId().toString()]
        List<String> linkedCreditCardAccountIds = [creditCard.getCreditCardAccountId().toString(), creditCard2.getCreditCardAccountId().toString()]
        List<String> linkedLoanAccountIds = [loan.getContractId().toString(), loan2.getContractId().toString()]
        List<String> linkedFinancingAccountIds = [financing.getContractId().toString(), financing2.getContractId().toString()]
        List<String> linkedInvoiceFinancingAccountIds = [invoiceFinancing.getContractId().toString(), invoiceFinancing2.getContractId().toString()]
        List<String> linkedOverdraftAccountIds = [overdraft.getContractId().toString(), overdraft2.getContractId().toString()]
        List<String> linkedBankFixedIncomesAccountIds = [bankFixedIncome.getInvestmentId().toString()]
        List<String> linkedCreditFixedIncomesAccountIds = [creditFixedIncome.getInvestmentId().toString()]
        List<String> linkedVariableIncomesAccountIds = [variableIncome.getInvestmentId().toString()]
        List<String> linkedTreasureTitlesAccountIds = [TreasureTitle.getInvestmentId().toString()]
        List<String> linkedFundsAccountIds = [fund.getInvestmentId().toString()]

        UpdateConsentData updateData = new UpdateConsentData()
        updateData.setStatus(UpdateConsentData.StatusEnum.AUTHORISED)
        updateData.setLinkedAccountIds(linkedAccountIds)
        updateData.setLinkedCreditCardAccountIds(linkedCreditCardAccountIds)
        updateData.setLinkedLoanAccountIds(linkedLoanAccountIds)
        updateData.setLinkedFinancingAccountIds(linkedFinancingAccountIds)
        updateData.setLinkedInvoiceFinancingAccountIds(linkedInvoiceFinancingAccountIds)
        updateData.setLinkedUnarrangedOverdraftAccountIds(linkedOverdraftAccountIds)
        updateData.setLinkedBankFixedIncomesAccountIds(linkedBankFixedIncomesAccountIds)
        updateData.setLinkedCreditFixedIncomesAccountIds(linkedCreditFixedIncomesAccountIds)
        updateData.setLinkedVariableIncomesAccountIds(linkedVariableIncomesAccountIds)
        updateData.setLinkedTreasureTitlesAccountIds(linkedTreasureTitlesAccountIds)
        updateData.setLinkedFundsAccountIds(linkedFundsAccountIds)

        UpdateConsent updateRequest = new UpdateConsent()
        updateRequest.setData(updateData)

        when:
        ResponseConsent response = service.createConsent('client1', consent)

        then:
        response.getData().getStatus() == EnumConsentStatus.AWAITING_AUTHORISATION

        when:
        ResponseConsentFull updateResponse = service.updateConsent(response.getData().getConsentId(), updateRequest)

        then:
        updateResponse.getData().getStatus() == EnumConsentStatus.AUTHORISED
        updateResponse.getData().getLinkedAccountIds().containsAll(linkedAccountIds)
        updateResponse.getData().getLinkedCreditCardAccountIds().containsAll(linkedCreditCardAccountIds)
        updateResponse.getData().getLinkedLoanAccountIds().containsAll(linkedLoanAccountIds)
        updateResponse.getData().getLinkedFinancingAccountIds().containsAll(linkedFinancingAccountIds)
        updateResponse.getData().getLinkedInvoiceFinancingAccountIds().containsAll(linkedInvoiceFinancingAccountIds)
        updateResponse.getData().getLinkedUnarrangedOverdraftAccountIds().containsAll(linkedOverdraftAccountIds)
        updateResponse.getData().getSub() == testAccountHolder.getUserId()

        when:
        ResponseConsentFull fetched = service.getConsentFull(response.getData().consentId)

        then:
        fetched.getData().getStatus() == EnumConsentStatus.AUTHORISED
        fetched.getData().getLinkedAccountIds().containsAll(linkedAccountIds)
        fetched.getData().getLinkedCreditCardAccountIds().containsAll(linkedCreditCardAccountIds)
        fetched.getData().getLinkedLoanAccountIds().containsAll(linkedLoanAccountIds)
        fetched.getData().getLinkedFinancingAccountIds().containsAll(linkedFinancingAccountIds)
        fetched.getData().getLinkedInvoiceFinancingAccountIds().containsAll(linkedInvoiceFinancingAccountIds)
        fetched.getData().getLinkedUnarrangedOverdraftAccountIds().containsAll(linkedOverdraftAccountIds)
        fetched.getData().getSub() == testAccountHolder.getUserId()
    }

    def "auto rejected after 60 min v2"() {
        given:
        CreateConsentV2 consent = ConsentFactory.createConsentV2(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)

        when:
        ResponseConsentV2 response = service.createConsentV2('client1', consent)

        then:
        response.getData().getStatus() == EnumConsentStatus.AWAITING_AUTHORISATION

        when:
        var consentEntity = consentRepository.findByConsentId(response.getData().getConsentId()).get()
        consentEntity.setStatusUpdateDateTime(Date.from(Instant.now() - Duration.ofHours(1)))
        consentRepository.update(consentEntity)
        service.updateConsentV2(response.getData().getConsentId(), updateRequest)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.BAD_REQUEST
        e.getMessage() == "Consent request already in a rejected state"

        when:
        def response2 = service.getConsentV2(response.getData().getConsentId(), "client1")

        then:
        def rejection = response2.getData().getRejection()
        rejection.getRejectedBy() == EnumRejectedByV2.ASPSP
        rejection.getReason().getCode() == EnumReasonCodeV2.CONSENT_EXPIRED
    }

    def "customer manually rejected v2"() {
        given:
        CreateConsentV2 consent = ConsentFactory.createConsentV2(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)

        when:
        ResponseConsentV2 response = service.createConsentV2('client2', consent)

        then:
        response.getData().getStatus() == EnumConsentStatus.AWAITING_AUTHORISATION

        when:
        service.deleteConsentV2(response.getData().getConsentId(), "client2")
        def response2 = service.getConsentV2(response.getData().getConsentId(), "client2")

        then:
        def rejection = response2.getData().getRejection()
        rejection.getRejectedBy() == EnumRejectedByV2.USER
        rejection.getReason().getCode() == EnumReasonCodeV2.CUSTOMER_MANUALLY_REJECTED
    }

    def "auto rejected after expiration date v2"() {
        given:
        CreateConsentV2 consent = ConsentFactory.createConsentV2(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)

        when:
        ResponseConsentV2 response = service.createConsentV2('client3', consent)

        then:
        response.getData().getStatus() == EnumConsentStatus.AWAITING_AUTHORISATION

        when:
        var consentEntity = consentRepository.findByConsentId(response.getData().getConsentId()).get()
        consentEntity.setExpirationDateTime(Date.from(Instant.now() - Duration.ofDays(1)))
        consentRepository.update(consentEntity)
        service.updateConsentV2(response.getData().getConsentId(), updateRequest)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.BAD_REQUEST
        e.getMessage() == "Consent request already in a rejected state"

        when:
        def response2 = service.getConsentV2(response.getData().getConsentId(), "client3")

        then:
        def rejection = response2.getData().getRejection()
        rejection.getRejectedBy() == EnumRejectedByV2.ASPSP
        rejection.getReason().getCode() == EnumReasonCodeV2.CONSENT_MAX_DATE_REACHED
    }

    def "customer manually revoked v2"() {
        given:
        CreateConsentV2 consent = ConsentFactory.createConsentV2(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)

        when:
        ResponseConsentV2 response = service.createConsentV2('client4', consent)

        then:
        response.getData().getStatus() == EnumConsentStatus.AWAITING_AUTHORISATION

        when:
        updateRequest.getData().setStatus(UpdateConsentData.StatusEnum.AUTHORISED)
        ResponseConsentFull updateResponse = service.updateConsent(response.getData().getConsentId(), updateRequest)

        then:
        updateResponse.getData().getStatus() == EnumConsentStatus.AUTHORISED

        when:
        service.deleteConsentV2(response.getData().getConsentId(), "client4")
        def response2 = service.getConsentV2(response.getData().getConsentId(), "client4")

        then:
        def rejection = response2.getData().getRejection()
        rejection.getRejectedBy() == EnumRejectedByV2.USER
        rejection.getReason().getCode() == EnumReasonCodeV2.CUSTOMER_MANUALLY_REVOKED
    }

    def "we can create consent extension"() {
        given:
        def consentReq = ConsentFactory.createConsentV2(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        def consent = service.createConsentV2('client5', consentReq)
        def consentId = consent.getData().getConsentId()
        def updatedConsent = consentRepository.findByConsentId(consentId).orElse(null)
        assert updatedConsent != null
        updatedConsent.setStatus(EnumConsentStatus.AUTHORISED.toString())
        consentRepository.update(updatedConsent)
        def consentStatusUpdateDateTime = updatedConsent.getStatusUpdateDateTime()


        def consentExtendsReq = ConsentFactory.createConsentExtends(testAccountHolder.documentIdentification, testAccountHolder.documentRel)

        when:
        def consentExtendsResponse = service.createConsentExtension(consentId, consentExtendsReq)
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
        def consentReq = ConsentFactory.createConsentV2(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        def consent = service.createConsentV2('client6', consentReq)
        def consentId = consent.getData().getConsentId()
        def updatedConsent = consentRepository.findByConsentId(consentId).orElse(null)
        assert updatedConsent != null
        updatedConsent.setStatus(EnumConsentStatus.AUTHORISED.toString())
        consentRepository.update(updatedConsent)
        def consentExtendsReq = ConsentFactory.createConsentExtends(testAccountHolder.documentIdentification, testAccountHolder.documentRel)

        when:
        def overOneYearDate = OffsetDateTime.now().plusYears(5)
        consentExtendsReq.getData().setExpirationDateTime(overOneYearDate)
        service.createConsentExtension(consentId, consentExtendsReq)

        then:
        def e = thrown(HttpStatusException)
        e.status == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == String.format("%s: new expirationDateTime cannot be in the past or more than one year", EnumConsentExtendsErrorCode.DATA_EXPIRACAO_INVALIDA)

        when:
        def oldDate = OffsetDateTime.now().minusYears(1)
        consentExtendsReq.getData().setExpirationDateTime(oldDate)
        service.createConsentExtension(consentId, consentExtendsReq)

        then:
        def e2 = thrown(HttpStatusException)
        e2.status == HttpStatus.UNPROCESSABLE_ENTITY
        e2.getMessage() == String.format("%s: new expirationDateTime cannot be in the past or more than one year", EnumConsentExtendsErrorCode.DATA_EXPIRACAO_INVALIDA)
    }

    def "we cant create consent extension if expiration time is less than or equal to the expiration time of the consent"() {
        def consentReq = ConsentFactory.createConsentV2(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        def consent = service.createConsentV2('client7', consentReq)
        def consentId = consent.getData().getConsentId()
        def updatedConsent = consentRepository.findByConsentId(consentId).orElse(null)
        assert updatedConsent != null
        updatedConsent.setStatus(EnumConsentStatus.AUTHORISED.toString())
        consentRepository.update(updatedConsent)
        def consentExtendsReq = ConsentFactory.createConsentExtends(testAccountHolder.documentIdentification, testAccountHolder.documentRel)
        service.createConsentExtension(consentId, consentExtendsReq)

        when:
        service.createConsentExtension(consentId, consentExtendsReq)

        then:
        def e = thrown(HttpStatusException)
        e.status == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == String.format("%s: new expirationDateTime cannot be equal to or less than the expirationDateTime of the consnet",
                EnumConsentExtendsErrorCode.DATA_EXPIRACAO_INVALIDA)

        when:
        def oldDate = BankLambdaUtils.dateToOffsetDate(updatedConsent.getExpirationDateTime()).minusDays(1)
        consentExtendsReq.getData().setExpirationDateTime(oldDate)
        service.createConsentExtension(consentId, consentExtendsReq)

        then:
        def e2 = thrown(HttpStatusException)
        e2.status == HttpStatus.UNPROCESSABLE_ENTITY
        e2.getMessage() == String.format("%s: new expirationDateTime cannot be equal to or less than the expirationDateTime of the consnet",
                EnumConsentExtendsErrorCode.DATA_EXPIRACAO_INVALIDA)
    }

    def "we cant create consent extension if consent status is not AUTHORISED"() {
        def consentReq = ConsentFactory.createConsentV2(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        def consent = service.createConsentV2('client8', consentReq)
        def consentId = consent.getData().getConsentId()
        def updatedConsent = consentRepository.findByConsentId(consentId).orElse(null)
        assert updatedConsent != null
        updatedConsent.setStatus(consentStatus.toString())
        consentRepository.update(updatedConsent)
        def consentExtendsReq = ConsentFactory.createConsentExtends(testAccountHolder.documentIdentification, testAccountHolder.documentRel)
        when:
        service.createConsentExtension(consentId, consentExtendsReq)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == String.format("%s: only AUTHORISED consents can be extended", EnumConsentExtendsErrorCode.ESTADO_CONSENTIMENTO_INVALIDO)

        where:
        consentStatus << [EnumConsentStatus.AWAITING_AUTHORISATION, EnumConsentStatus.REJECTED]
    }

    def "we can trigger create consent extension mock responses"() {
        def consentReq = ConsentFactory.createConsentV2(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        def consent = service.createConsentV2('client9', consentReq)
        def consentId = consent.getData().getConsentId()
        def consentExtendsReq = ConsentFactory.createConsentExtends(testAccountHolder.documentIdentification, testAccountHolder.documentRel)
        consentExtendsReq.getData().getLoggedUser().getDocument().setIdentification(identification)
        when:
        service.createConsentExtension(consentId, consentExtendsReq)

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

    def "we can get consent extensions"() {
        given:
        def consentReq = ConsentFactory.createConsentV2(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        def consent = service.createConsentV2('client10', consentReq)
        def consentId = consent.getData().getConsentId()
        def updatedConsent = consentRepository.findByConsentId(consentId).orElse(null)
        assert updatedConsent != null
        updatedConsent.setStatus(EnumConsentStatus.AUTHORISED.toString())
        consentRepository.update(updatedConsent)
        def consentExtendsReq = ConsentFactory.createConsentExtends(testAccountHolder.documentIdentification, testAccountHolder.documentRel)
        service.createConsentExtension(consentId, consentExtendsReq)
        def firstDate = consentExtendsReq.getData().getExpirationDateTime()
        def secondDate = firstDate.plusMonths(2)
        consentExtendsReq.getData().setExpirationDateTime(secondDate)
        service.createConsentExtension(consentId, consentExtendsReq)
        def infiniteDate = OffsetDateTime.of(2300, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        consentExtendsReq.getData().setExpirationDateTime(infiniteDate)
        service.createConsentExtension(consentId, consentExtendsReq)

        def orderControl = [
                infiniteDate,
                secondDate,
                firstDate
        ]

        when:
        def getConsentExtensionsResponse = service.getConsentExtensions(consentId)
        updatedConsent = consentRepository.findByConsentId(consentId).orElse(null)

        then:
        noExceptionThrown()
        def data = getConsentExtensionsResponse.getData()
        data.size() == 3

        updatedConsent.getExpirationDateTime() == BankLambdaUtils.offsetDateToDate(data.get(0).getExpirationDateTime())

        data.size().times {
            def ext = data.get(it)
            assert ext.getExpirationDateTime() == orderControl[it]
            assert ext.getRequestDateTime() != null
            def document = ext.getLoggedUser().getDocument()
            assert document.getRel() == consentExtendsReq.getData().getLoggedUser().getDocument().getRel()
            assert document.getIdentification() == consentExtendsReq.getData().getLoggedUser().getDocument().getIdentification()
        }
    }

    def "an empty array is returned if no consent extensions exist"() {
        given:
        def consentReq = ConsentFactory.createConsentV2(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        def consent = service.createConsentV2('client11', consentReq)
        def consentId = consent.getData().getConsentId()
        def updatedConsent = consentRepository.findByConsentId(consentId).orElse(null)
        assert updatedConsent != null
        updatedConsent.setStatus(EnumConsentStatus.AUTHORISED.toString())
        consentRepository.update(updatedConsent)

        when:
        def getConsentExtensionsResponse = service.getConsentExtensions(consentId)

        then:
        getConsentExtensionsResponse.getData() != null
        getConsentExtensionsResponse.getData().size() == 0
    }

    @Unroll
    def "we cant create consent extension if user identification is different"() {
        given:
        def consentReq = ConsentFactory.createConsentV2(testAccountHolder.documentIdentification, testAccountHolder.documentRel, null)
        def consent = service.createConsentV2("client12", consentReq)
        def consentId = consent.getData().getConsentId()
        def updatedConsent = consentRepository.findByConsentId(consentId).orElse(null)
        assert updatedConsent != null
        updatedConsent.setStatus(EnumConsentStatus.AUTHORISED.toString())
        consentRepository.update(updatedConsent)
        def consentExtendsReq = ConsentFactory.createConsentExtends(consentExtendtIdentification, consentExtendRel)

        when:
        def consentExtendsResponse = service.createConsentExtension(consentId, consentExtendsReq)

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

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }

}
