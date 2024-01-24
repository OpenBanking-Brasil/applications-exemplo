package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.TestRequestDataFactory
import com.raidiam.trustframework.bank.domain.AccountEntity
import com.raidiam.trustframework.bank.domain.AccountHolderEntity
import com.raidiam.trustframework.bank.domain.PaymentConsentEntity
import com.raidiam.trustframework.bank.enums.ErrorCodesEnumV2
import com.raidiam.trustframework.bank.repository.PaymentsSimulateResponseRepository
import com.raidiam.trustframework.bank.services.message.PaymentErrorMessageV2
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils
import spock.lang.Shared
import spock.lang.Stepwise

import javax.inject.Inject
import java.time.LocalDate

import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccount
import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccountHolder

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class PaymentsServiceV3Spec extends CleanupSpecification {
    @Inject
    PaymentsService paymentsService

    @Inject
    PaymentConsentService paymentConsentService

    @Inject
    PaymentsSimulateResponseRepository paymentsSimulateResponseRepository

    @Shared
    AccountHolderEntity accountHolder
    @Shared
    AccountEntity account

    def setup() {
        if (runSetup) {
            accountHolder = accountHolderRepository.save(anAccountHolder())
            account = accountRepository.save(anAccount(accountHolder))
            runSetup = false
        }
    }

    def "we can create a payment consent and force a Reject Reason"() {
        given:
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.data.payment.amount(amount)
        paymentConsentRequest.data.payment.details.creditorAccount.number("9876543")
        paymentConsentRequest.data.debtorAccount.setAccountType(EnumAccountPaymentsType.CACC)
        def responseCreate = paymentConsentService.createConsentV3(clientId, idemPotencyKey, jti, paymentConsentRequest)

        when:
        ResponsePaymentConsentV3 response = paymentConsentService.getConsentV3(responseCreate.getData().getConsentId(), clientId)

        then:
        response.getData().getStatus() == ResponsePaymentConsentDataV3.StatusEnum.REJECTED
        response.getData().getRejectionReason().getCode() == code

        where:
        amount   | clientId                     | jti                          | idemPotencyKey               | code
        "300.01" | UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString() | EnumConsentRejectionReasonType.VALOR_INVALIDO
        "300.02" | UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString() | EnumConsentRejectionReasonType.NAO_INFORMADO
        "300.03" | UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString() | EnumConsentRejectionReasonType.FALHA_INFRAESTRUTURA
        "300.04" | UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString() | EnumConsentRejectionReasonType.TEMPO_EXPIRADO_CONSUMO
        "300.05" | UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString() | EnumConsentRejectionReasonType.CONTA_NAO_PERMITE_PAGAMENTO
        "300.06" | UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString() | EnumConsentRejectionReasonType.SALDO_INSUFICIENTE
        "300.07" | UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString() | EnumConsentRejectionReasonType.VALOR_ACIMA_LIMITE
        "300.08" | UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString() | EnumConsentRejectionReasonType.QRCODE_INVALIDO
    }

    def "we can create a payment consent and get reject when have same debtor and creditor"() {
        given:
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.data.debtorAccount.setAccountType(EnumAccountPaymentsType.CACC)
        paymentConsentRequest.data.payment.details.creditorAccount.setIspb("12341234")

        when:
        def responseCreate = paymentConsentService.createConsentV3(clientId, idemPotencyKey, jti, paymentConsentRequest)

        then:
        responseCreate.getData().getStatus() == ResponsePaymentConsentDataV2.StatusEnum.REJECTED

        when:
        ResponsePaymentConsentV3 response = paymentConsentService.getConsentV3(responseCreate.getData().getConsentId(), clientId)

        then:
        response.getData().getStatus() == ResponsePaymentConsentDataV3.StatusEnum.REJECTED
        response.getData().getRejectionReason().getCode() == EnumConsentRejectionReasonType.CONTAS_ORIGEM_DESTINO_IGUAIS

        where:
        clientId                     | jti                          | idemPotencyKey
        UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString()
    }

    def "we can create a payment consent and get reject when debtor available amount is insufficient"() {
        given:
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel(), "200.00")
        paymentConsentRequest.data.debtorAccount.setAccountType(EnumAccountPaymentsType.CACC)

        when:
        def responseCreate = paymentConsentService.createConsentV3(clientId, idemPotencyKey, jti, paymentConsentRequest)

        then:
        responseCreate.getData().getStatus() == ResponsePaymentConsentDataV2.StatusEnum.REJECTED

        when:
        ResponsePaymentConsentV3 response = paymentConsentService.getConsentV3(responseCreate.getData().getConsentId(), clientId)


        then:
        response.getData().getStatus() == ResponsePaymentConsentDataV3.StatusEnum.REJECTED
        response.getData().getRejectionReason().getCode() == EnumConsentRejectionReasonType.SALDO_INSUFICIENTE

        when:
        paymentConsentRequest.data.setDebtorAccount(null)
        responseCreate = paymentConsentService.createConsentV3(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        then:
        noExceptionThrown()
        responseCreate.getData().getStatus() == ResponsePaymentConsentDataV2.StatusEnum.AWAITING_AUTHORISATION

        where:
        clientId                     | jti                          | idemPotencyKey
        UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString()
    }

    def "we can get reject reason when payment consent is rejected"() {
        given:
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.data.debtorAccount.setAccountType(EnumAccountPaymentsType.CACC)
        def responseConsent = paymentConsentService.createConsentV3(clientId, idemPotencyKey, jti, paymentConsentRequest)
        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.REJECTED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        ResponsePaymentConsentV3 response = paymentConsentService.getConsentV3(responseConsent.getData().getConsentId(), clientId)

        then:
        response.getData().getStatus() == ResponsePaymentConsentDataV3.StatusEnum.REJECTED
        response.getData().getRejectionReason().getCode() == EnumConsentRejectionReasonType.REJEITADO_USUARIO

        where:
        clientId                     | jti                          | idemPotencyKey
        UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString()

    }

    def "we can get a payment v3"() {
        given:
        def clientId = "client6"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")

        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV3(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        CreatePixPaymentV3 paymentRequest = TestRequestDataFactory.createPaymentRequestV3("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "", null)
        paymentRequest.data.authorisationFlow(authorisationFlow)
        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        ResponsePixPaymentV3 paymentResponse = paymentsService.createPaymentV3(responseConsent.getData().getConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentRequest, clientId)

        when:
        ResponsePixPaymentV3 foundPayment = paymentsService.getPaymentV3(paymentResponse.getData().getPaymentId(), clientId)

        then:
        noExceptionThrown()
        foundPayment != null
        foundPayment.getData() != null
        foundPayment.getData().getPaymentId() != null
        foundPayment.getData().getPaymentId() == paymentResponse.getData().getPaymentId()

        where:
        authorisationFlow << [null, EnumAuthorisationFlow.HYBRID_FLOW, EnumAuthorisationFlow.CIBA_FLOW, EnumAuthorisationFlow.FIDO_FLOW]
    }

    def "an invalid endToEndId in a scheduled payment returns a 422"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def idemPotencyKey = "idemkey123"
        // create consent w/ schedule set on creation
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.data.debtorAccount.accountType(EnumAccountPaymentsType.CACC)
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV3(clientId, idemPotencyKey, "randomjti2567687674", paymentConsentRequest)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        // payment with endtoendid using 1200 in HHmm
        CreatePixPaymentV3 paymentRequest = TestRequestDataFactory.createPaymentRequestV3("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", null, ("E90400888202101281200" + RandomStringUtils.randomAlphanumeric(11)))
        paymentsService.createPaymentV3(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        // should return 422
        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.message == new PaymentErrorMessageV2().getMessageInvalidParameter()


        and:
        Optional<PaymentConsentEntity> consentEntityOpt = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().consentId)
        consentEntityOpt.isPresent()
        PaymentConsentEntity consentEntity = consentEntityOpt.get()
        consentEntity.status == "CONSUMED"
    }

    def "we get 422 when create a payment consent with account type SLRY"() {
        given:
        def clientId = UUID.randomUUID().toString()
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel(), "20422.00")

        when:
        paymentConsentService.createConsentV3(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY
    }

    def "we throw 422 for invalid schedule and date at consent creation"() {

        given:
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel(), "100.00")
        paymentConsentRequest.data.payment.schedule(scheduleDate as Schedule)
        paymentConsentRequest.data.payment.date(date)
        paymentConsentRequest.data.debtorAccount.setAccountType(EnumAccountPaymentsType.CACC)
        when:
        paymentConsentService.createConsentV3(clientId, idemPotencyKey, jti, paymentConsentRequest)

        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY
        ex.message == errorMessage

        where:
        clientId                     | idemPotencyKey               | jti                          | scheduleDate                                                                          | date            | errorMessage
        UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString() | null                                                                                  | null            | ErrorCodesEnumV2.PARAMETRO_NAO_INFORMADO.name()
        UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString() | new Schedule().single(new ScheduleSingleSingle().date(LocalDate.now().plusDays(1)))   | LocalDate.now() | ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name()
        UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString() | new Schedule().single(new ScheduleSingleSingle().date(LocalDate.now().plusDays(380))) | null            | ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name()
        UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString() | new Schedule().single(new ScheduleSingleSingle().date(LocalDate.now()))               | null            | ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name()
        UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString() | new Schedule().single(new ScheduleSingleSingle().date(LocalDate.now().minusDays(1)))  | null            | ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name()
    }

    def "we cannot create a payment if the consent currency doesn't match"() {
        given:
        def clientId = UUID.randomUUID().toString()
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "GBP", "100")
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV3(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        CreatePixPaymentV3 paymentRequest = TestRequestDataFactory.createPaymentRequestV3("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "")

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        paymentsService.createPaymentV3(responseConsent.getData().getConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
    }

    def "idempotency requires functionally identical payloads"() {
        given:
        def clientId = "client4"
        def idemPotencyKey = "idemkey49"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV3(clientId, idemPotencyKey, "randomjti248", paymentConsentRequest)

        CreatePixPaymentV3 paymentRequest = TestRequestDataFactory.createPaymentRequestV3("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        ResponsePixPaymentV3 paymentResponse = paymentsService.createPaymentV3(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        noExceptionThrown()
        paymentResponse != null
        paymentResponse.getData() != null
        paymentResponse.getData().getPaymentId() != null

        when:
        paymentRequest.getData().getPayment().setAmount("99999")
        paymentsService.createPaymentV3(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY

        def consent = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().getConsentId()).orElse(null)
        consent != null
        consent.getStatus() == ResponsePaymentConsentData.StatusEnum.CONSUMED.toString()

    }


    def "we cannot create a payment if end to end ID is missing or incorrect"() {
        given:
        def clientId = UUID.randomUUID().toString()
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "GBP", "100")

        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV3(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        CreatePixPaymentV3 paymentRequest = TestRequestDataFactory.createPaymentRequestV3("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "", null, endToEndId)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        paymentsService.createPaymentV3(responseConsent.getData().getConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "PARAMETRO_NAO_INFORMADO: endToEndId is missing or invalid"

        where:
        endToEndId << [
                null,
                "b" * 32,
                "1234568789"
        ]
    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }

    @MockBean(CnpjVerifier.class)
    CnpjVerifier rolesApiService() {
        return new CnpjVerifier() {
            @Override
            boolean isKnownCnpj(String cnpj) {
                return !("36386527000143" == cnpj)
            }
        }
    }
}
