package com.raidiam.trustframework.bank

import com.raidiam.trustframework.bank.services.ConsentService
import com.raidiam.trustframework.bank.services.CustomerService
import com.raidiam.trustframework.bank.services.LoansService
import com.raidiam.trustframework.bank.services.PaymentsService
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
import java.util.stream.Collectors

import static com.raidiam.trustframework.mockbank.models.generated.CreateConsentData.PermissionsEnum.*

@Stepwise
@MicronautTest(transactional = false, environments = ["db-with-preload"])
class TestPrepopulationSpec extends CleanupSpecification {

    private static final Logger LOG = LoggerFactory.getLogger(TestPrepopulationSpec)

    @Inject
    ConsentService consentService

    @Inject
    PaymentsService paymentsService

    @Inject
    CustomerService customerService

    @Inject
    LoansService loansService

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
        LOG.info("Found personal company cnpj {}, for personal identifications id {} ", cnpj.getCompanyCnpj(), cnpj.getPersonalIdentificationsId())
    }

    def "We can get an accountholder and some transactions"() {

        when:
        def accountHolder = accountHolderRepository.findByDocumentIdentificationAndDocumentRel("10117409073", "CPF")

        then:
        accountHolder.size() == 1
        !accountHolder.get(0).getAccounts().empty
        !accountHolder.get(0).getAccounts().first().getTransactions().empty
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
                RESOURCES_READ, CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ, RESOURCES_READ).stream().map(CreateConsentData.PermissionsEnum::toString).map(CreateConsentData.PermissionsEnum::fromValue).collect(Collectors.toList())

        def loggedUser = new LoggedUser().document(new LoggedUserDocument().identification('76109277673').rel('CPF'))
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
        updatedConsent.getData().getStatus() == ResponseConsentFullData.StatusEnum.AUTHORISED

        when:
        def identifications = customerService.getPersonalIdentifications(result.getData().getConsentId())

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
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest(
                '78516956784', 'CPF', '78516956784', 'Bob Creditor', EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC,
                '12345678', '1774', '94088392', 'CPF', '76109277673', 'PIX',
                LocalDate.now().plusDays(2), 'BRL', '123.00')

        when:
        def result = paymentsService.createConsent('HkZsqIGRm8TL8yNzt1gLw', '1234', 'jti', paymentConsentRequest)

        then:
        noExceptionThrown()
        result != null

        when:
        def dto = paymentsService.getConsentFull(result.getData().getConsentId())

        then:
        noExceptionThrown()
        dto.getData().getSub() != null
        dto.getData().getSub() == 'ralph.bragg@gmail.com'
    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }
}

