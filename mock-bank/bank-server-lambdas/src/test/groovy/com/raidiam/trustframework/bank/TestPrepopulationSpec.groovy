package com.raidiam.trustframework.bank

import com.raidiam.trustframework.bank.services.*
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.data.model.Pageable
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Stepwise

import javax.inject.Inject
import java.time.Duration
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

import static com.raidiam.trustframework.mockbank.models.generated.EnumConsentPermissions.*

@Stepwise
@MicronautTest(transactional = false, environments = ["db-with-preload"])
class TestPrepopulationSpec extends CleanupSpecification {

    private static final Logger LOG = LoggerFactory.getLogger(TestPrepopulationSpec)

    @Inject
    ConsentService consentService

    @Inject
    PaymentsService paymentsService

    @Inject
    PaymentConsentService paymentConsentService

    @Inject
    CustomerService customerService

    @Inject
    LoansService loansService

    @Inject
    AccountsService accountsService

    def "we can get the pre-propulated stuff" () {

        when:
        def accountHolders = accountHolderRepository.findAll()

        then:
        noExceptionThrown()
        accountHolders != null
        accountHolders.size() != 0

        when:
        def a = accountHolders.first()

        then:
        noExceptionThrown()
        a != null
        LOG.info("Found account_holder {} with documentation {}, rel {}, created_at {}, updated_at {}, created_by {}, updated_by {}",
                a.getAccountHolderId(), a.getDocumentIdentification(), a.getDocumentRel(), a.getCreatedAt(), a.getUpdatedAt(), a.getCreatedBy(), a.getUpdatedBy())

        when:
        def personalFr = personalFinancialRelationsRepository.findAll()

        then:
        noExceptionThrown()
        personalFr.size() != 0

        when:
        def p = personalFr.first()

        then:
        noExceptionThrown()
        LOG.info("Found personal financial relation {} for accountHolder {} with start date {}, additional info {}, created_at {}, updated_at {}, created_by {}, updated_by {}",
                p.getPersonalFinancialRelationsId(), p.getAccountHolderId(), p.getStartDate(), p.getProductsServicesTypeAdditionalInfo(),
                p.getCreatedAt(), p.getUpdatedAt(), p.getCreatedBy(), p.getUpdatedBy())

        when:
        def companyCnpjs = personalCompanyCnpjRepository.findAll()

        then:
        noExceptionThrown()
        companyCnpjs.size() != 0

        when:
        def cnpj = companyCnpjs.first()

        then:
        noExceptionThrown()
        LOG.info("Found personal company cnpj {}, for personal identifications id {} ", cnpj.getCompanyCnpj(), cnpj.getIdentification().getPersonalIdentificationsId())
    }

    def "We can get an accountholder and some transactions"() {

        when:
        def accountHolder = accountHolderRepository.findByDocumentIdentificationAndDocumentRel("10117409073", "CPF")

        then:
        accountHolder.size() == 1

        when:
        def accounts = accountRepository.findByAccountHolderId(accountHolder.get(0).getAccountHolderId())

        then:
        !accounts.empty
        !accounts.first().getTransactions().empty
    }

    def "Check Ralph is real" () {
        when:
        def result = accountHolderRepository.findByDocumentIdentificationAndDocumentRel('76109277673', 'CPF')

        if (result.isEmpty() || result.first().getAccountHolderName() != "Ralph Bragg") {
            LOG.error("CECI N'EST PAS UN RALPH")
        }

        then:
        !result.isEmpty()
        result.first().getAccountHolderName() == "Ralph Bragg"
    }

    def "we can create and retrieve a consent for an injected user" () {
        given:
        def perms = List.of(
                LOANS_READ, LOANS_WARRANTIES_READ,
                LOANS_SCHEDULED_INSTALMENTS_READ, LOANS_PAYMENTS_READ,
                FINANCINGS_READ, FINANCINGS_WARRANTIES_READ,
                FINANCINGS_SCHEDULED_INSTALMENTS_READ, FINANCINGS_PAYMENTS_READ,
                UNARRANGED_ACCOUNTS_OVERDRAFT_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ,
                UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ,
                INVOICE_FINANCINGS_READ, INVOICE_FINANCINGS_WARRANTIES_READ,
                INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ, INVOICE_FINANCINGS_PAYMENTS_READ,
                RESOURCES_READ, CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ, RESOURCES_READ).stream().map(EnumConsentPermissions::toString).map(EnumConsentPermissions::fromValue).collect(Collectors.toList())

        def loggedUser = new LoggedUser().document(new Document().identification('76109277673').rel('CPF'))
        def data = new CreateConsentData()
                .loggedUser(loggedUser)
                .permissions(perms)
                .expirationDateTime(OffsetDateTime.now() + Duration.ofDays(2))

        def consentRequest = new CreateConsent().data(data)

        when:
        def result = consentService.createConsent('HkZsqIGRm8TL8yNzt1gLw', consentRequest)

        then:
        noExceptionThrown()
        result != null

        when:
        def dto = consentService.getConsentFull(result.getData().getConsentId())

        then:
        noExceptionThrown()
        dto.getData().getSub() != null
        dto.getData().getSub() == 'ralph.bragg@gmail.com'

        when:
        def consentUpdateRequest = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("ralph.bragg@gmail.com").linkedLoanAccountIds(List.of("dadd421d-184e-4689-a085-409d1bca4193")))
        def updatedConsent = consentService.updateConsent(result.getData().getConsentId(), consentUpdateRequest)

        then:
        updatedConsent != null
        updatedConsent.getData().getStatus() == EnumConsentStatus.AUTHORISED

        when:
        def identifications = customerService.getPersonalIdentificationsV2(result.getData().getConsentId())

        then:
        identifications != null
        identifications.getData().get(0) != null
        identifications.getData().get(0).getSocialName() != null


        when:
        def loans = loansService.getLoansContractList(Pageable.unpaged(), updatedConsent.getData().getConsentId())

        then:
        loans.getData().size() != 0
    }

    def "we can create and retrieve a payment consent for an injected user" () {
        given:
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4(
                '78516956784', 'CPF', '78516956784', 'Bob Creditor', EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC,
                '12345678', '1774', '94088392', 'CPF', '76109277673', 'PIX',
                LocalDate.now().plusDays(2), 'BRL', '123.00')
        def clientId = 'HkZsqIGRm8TL8yNzt1gLw'

        when:
        def result = paymentConsentService.createConsentV4(clientId, '1234', 'jti', paymentConsentRequest)

        then:
        noExceptionThrown()
        result != null

        when:
        def dto = paymentConsentService.getConsentFull(result.getData().getConsentId(), clientId)

        then:
        noExceptionThrown()
        dto.getData().getSub() != null
        dto.getData().getSub() == 'ralph.bragg@gmail.com'
    }


    def "Check Lilian bussiness identification" () {
        given:
        def expireIn10Days = OffsetDateTime.now() + Duration.ofDays(10)
        def consentRequest = TestRequestDataFactory.createConsentRequest(
                "43053510000130",
                "CNPJ",
                "37964623168",
                "CPF",
                expireIn10Days,
                List.of(RESOURCES_READ,
                        CUSTOMERS_BUSINESS_IDENTIFICATIONS_READ,
                        CUSTOMERS_BUSINESS_ADITTIONALINFO_READ))

        when:
        def consentResult = consentService.createConsent('HkZsqIGRm8TL8yNzt1gLw', consentRequest)

        then:
        noExceptionThrown()
        consentResult != null

        when:
        def consentUpdateRequest = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("lilian.psicologia@email.com"))
        def updatedConsent = consentService.updateConsent(consentResult.getData().getConsentId(), consentUpdateRequest)

        then:
        updatedConsent != null
        updatedConsent.getData().getStatus() == EnumConsentStatus.AUTHORISED

        when:
        def result = customerService.getBusinessIdentificationsV2(consentResult.getData().getConsentId())

        if (result.getData().isEmpty() || result.getData().first().getCnpjNumber() != "43053510000130") {
            LOG.error("This is not a Lilian Psicologia Familiar")
        }

        then:
        !result.getData().isEmpty()
        result.getData().first().getCnpjNumber() == "43053510000130"
    }

    def "Check Lilian has transactions" () {
        given:
        def expireIn10Days = OffsetDateTime.now() + Duration.ofDays(10)
        def consentRequest = TestRequestDataFactory.createConsentRequest(
                "43053510000130",
                "CNPJ",
                "37964623168",
                "CPF",
                expireIn10Days,
                List.of(RESOURCES_READ,
                        ACCOUNTS_READ,
                        ACCOUNTS_TRANSACTIONS_READ))

        when:
        def consentResult = consentService.createConsent('HkZsqIGRm8TL8yNzt1gLw', consentRequest)

        then:
        noExceptionThrown()
        consentResult != null

        when:
        def consentUpdateRequest = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("lilian.psicologia@email.com").linkedAccountIds(List.of('d448bbb0-9d53-306f-816a-d59c12d73630')))
        def updatedConsent = consentService.updateConsent(consentResult.getData().getConsentId(), consentUpdateRequest)

        then:
        updatedConsent != null
        updatedConsent.getData().getStatus() == EnumConsentStatus.AUTHORISED

        when:
        def fromDate = LocalDate.of(2022, 9, 1)
        def toDate = LocalDate.of(2023, 9, 1)
        def result = accountsService.getAccountTransactionsV2(Pageable.from(0),
                consentResult.getData().getConsentId(),
                fromDate, toDate,
                null, 'd448bbb0-9d53-306f-816a-d59c12d73630')

        then:
        !result.getData().isEmpty()
        result.getData().first().getTransactionId() == "e24796a5-e55a-3cd3-b755-bbaa4d5352b0"
    }

    def "Check Lilian has loans" () {
        given:
        def expireIn10Days = OffsetDateTime.now() + Duration.ofDays(10)
        def consentRequest = TestRequestDataFactory.createConsentRequest(
                "43053510000130",
                "CNPJ",
                "37964623168",
                "CPF",
                expireIn10Days,
                List.of(LOANS_READ, LOANS_WARRANTIES_READ,
                        LOANS_SCHEDULED_INSTALMENTS_READ, LOANS_PAYMENTS_READ,
                        FINANCINGS_READ, FINANCINGS_WARRANTIES_READ,
                        FINANCINGS_SCHEDULED_INSTALMENTS_READ, FINANCINGS_PAYMENTS_READ,
                        UNARRANGED_ACCOUNTS_OVERDRAFT_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ,
                        UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ,
                        INVOICE_FINANCINGS_READ, INVOICE_FINANCINGS_WARRANTIES_READ,
                        INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ, INVOICE_FINANCINGS_PAYMENTS_READ,
                        RESOURCES_READ))

        when:
        def consentResult = consentService.createConsent('HkZsqIGRm8TL8yNzt1gLw', consentRequest)

        then:
        noExceptionThrown()
        consentResult != null

        when:
        def consentUpdateRequest = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("lilian.psicologia@email.com").linkedLoanAccountIds(List.of('b8a6cccb-9e4e-4f21-9c7d-b83d440f363f')))
        def updatedConsent = consentService.updateConsent(consentResult.getData().getConsentId(), consentUpdateRequest)

        then:
        updatedConsent != null
        updatedConsent.getData().getStatus() == EnumConsentStatus.AUTHORISED

        when:
        def result = loansService.getLoanContractV2(consentResult.getData().getConsentId(), UUID.fromString("b8a6cccb-9e4e-4f21-9c7d-b83d440f363f"))

        then:
        result.getData().getContractNumber() == "51561588037"
        result.getData().getIpocCode() == "90400888021328032674854848"
        result.getData().getInterestRates().size() > 0
        result.getData().getContractedFees().size() > 0
        result.getData().getContractedFinanceCharges().size() > 0

    }

    def "Check Lilian has payments" () {
        given:
        def expireIn10Days = OffsetDateTime.now() + Duration.ofDays(10)
        def consentRequest = TestRequestDataFactory.createConsentRequest(
                "43053510000130",
                "CNPJ",
                "37964623168",
                "CPF",
                expireIn10Days,
                List.of(LOANS_READ, LOANS_WARRANTIES_READ,
                        LOANS_SCHEDULED_INSTALMENTS_READ, LOANS_PAYMENTS_READ,
                        FINANCINGS_READ, FINANCINGS_WARRANTIES_READ,
                        FINANCINGS_SCHEDULED_INSTALMENTS_READ, FINANCINGS_PAYMENTS_READ,
                        UNARRANGED_ACCOUNTS_OVERDRAFT_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ,
                        UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ, UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ,
                        INVOICE_FINANCINGS_READ, INVOICE_FINANCINGS_WARRANTIES_READ,
                        INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ, INVOICE_FINANCINGS_PAYMENTS_READ,
                        RESOURCES_READ))

        when:
        def consentResult = consentService.createConsent('HkZsqIGRm8TL8yNzt1gLw', consentRequest)

        then:
        noExceptionThrown()
        consentResult != null

        when:
        def consentUpdateRequest = new UpdateConsent().data(new UpdateConsentData().status(UpdateConsentData.StatusEnum.AUTHORISED)
                .sub("lilian.psicologia@email.com").linkedLoanAccountIds(List.of('b8a6cccb-9e4e-4f21-9c7d-b83d440f363f')))
        def updatedConsent = consentService.updateConsent(consentResult.getData().getConsentId(), consentUpdateRequest)

        then:
        updatedConsent != null
        updatedConsent.getData().getStatus() == EnumConsentStatus.AUTHORISED

        when:
        def result = loansService.getLoanPaymentsV2(consentResult.getData().getConsentId(), UUID.fromString("b8a6cccb-9e4e-4f21-9c7d-b83d440f363f"))

        then:
        result.getData().getReleases().size() > 0

    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }
}

