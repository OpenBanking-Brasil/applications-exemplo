package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.TestRequestDataFactory
import com.raidiam.trustframework.bank.domain.AccountEntity
import com.raidiam.trustframework.bank.domain.AccountHolderEntity
import com.raidiam.trustframework.bank.domain.PaymentConsentEntity
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Stepwise

import javax.inject.Inject
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccount
import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccountHolder

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class PaymentsServiceSpec extends CleanupSpecification {
    @Inject
    PaymentsService paymentsService

    @Shared
    AccountHolderEntity accountHolder
    @Shared
    AccountEntity account

    def setup () {
        if(runSetup) {
            accountHolder = accountHolderRepository.save(anAccountHolder())
            account = accountRepository.save(anAccount(accountHolder.getAccountHolderId()))
            runSetup = false
        }
    }

    def "we can create a payment consent" () {
        given:
        def clientId = "client1"
        def idemPotencyKey = "idemkey1"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def jti = "randomjti1"

        when:
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, jti, paymentConsentRequest)

        then:
        noExceptionThrown()
        def details = responseConsent.getData().getPayment().getDetails()
        responseConsent != null
        responseConsent.getData() != null
        responseConsent.getData().getStatus() == ResponsePaymentConsentData.StatusEnum.AWAITING_AUTHORISATION
        responseConsent.getData().getConsentId() != null


        and:
        details.proxy == 'proxy'
        details.localInstrument == EnumLocalInstrument.DICT
        details.creditorAccount.issuer == 'mockbank'
        details.creditorAccount.accountType == EnumAccountPaymentsType.CACC
        details.creditorAccount.ispb == 'ispb'
        details.creditorAccount.number == '1234567890'
    }

    def "we can create a payment consent and force a 422 response" () {
        given:
        def clientId = "client1"
        def idemPotencyKey = "idemkey1"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.data.payment.amount("10422.00")
        def jti = "randomjti1"

        when:
        paymentsService.createConsent(clientId, idemPotencyKey, jti, paymentConsentRequest)

        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY
        ex.message == "Forced a 422 for payment consent request"
    }

    def "we can create a payment consent and force a 422 response with a specific error message" () {
        given:
        def clientId = "client1rw4g"
        def idemPotencyKey = "idemkey1gwrebe"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.data.payment.amount("10422.01")
        def jti = "randomjti1gwrebera"

        when:
        paymentsService.createConsent(clientId, idemPotencyKey, jti, paymentConsentRequest)

        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY
        ex.message == "DETALHE_PGTO_INVALIDO"

    }

    def "we can create a payment consent and force a 422 payment response" () {
        given:
        def clientId = "client1rw4g"
        def idemPotencyKey = "idemkey1gwrebe"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.data.payment.amount("100.00")
        CreatePixPayment paymentRequest = TestRequestDataFactory.testPixPayment()
        paymentRequest.data.payment.currency("ZAR")
        def jti = "randomjti1gwrebera"

        when:
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, jti, paymentConsentRequest)
        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, true)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(), updatePaymentConsent)

        paymentsService.createPayment(
                responseConsent.getData().getConsentId(),
                idemPotencyKey,
                jti + "haha",
                paymentRequest
        )


        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY
        ex.message.contains("PAGAMENTO_DIVERGENTE_DO_CONSENTIMENTO")

    }

    def "unknown CNPJs can create a payment consent" () {
        given:
        def clientId = "client1"
        def idemPotencyKey = "idemkey1"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("MADEUP", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")

        when:
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti2", paymentConsentRequest)

        then:
        noExceptionThrown()
        responseConsent != null
        responseConsent.getData() != null
        responseConsent.getData().getStatus() == ResponsePaymentConsentData.StatusEnum.AWAITING_AUTHORISATION
        responseConsent.getData().getConsentId() != null
    }

    def "idempotency works for payment consents" () {
        given:
        def clientId = "client1"
        def idemPotencyKey2 = "idemkey2"
        def idemPotencyKey3 = "idemkey3"

        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        when:
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey2, "randomjti3", paymentConsentRequest)

        then:
        noExceptionThrown()
        responseConsent != null
        responseConsent.getData() != null
        responseConsent.getData().getStatus() == ResponsePaymentConsentData.StatusEnum.AWAITING_AUTHORISATION
        responseConsent.getData().getConsentId() != null

        when:
        ResponsePaymentConsent responseConsent2 = paymentsService.createConsent(clientId, idemPotencyKey2, "randomjti4", paymentConsentRequest)

        then:
        noExceptionThrown()
        responseConsent2 != null
        responseConsent2.getData() != null
        responseConsent2.getData().getStatus() == ResponsePaymentConsentData.StatusEnum.AWAITING_AUTHORISATION
        responseConsent2.getData().getConsentId() != null
        responseConsent2.getData().getConsentId() == responseConsent.getData().getConsentId()

        when:
        ResponsePaymentConsent responseConsent3 = paymentsService.createConsent(clientId, idemPotencyKey3, "randomjti5", paymentConsentRequest)

        then:
        noExceptionThrown()
        responseConsent3 != null
        responseConsent3.getData() != null
        responseConsent3.getData().getStatus() == ResponsePaymentConsentData.StatusEnum.AWAITING_AUTHORISATION
        responseConsent3.getData().getConsentId() != null
        responseConsent3.getData().getConsentId() != responseConsent.getData().getConsentId()
    }

    def "we can update a payment consent" () {
        given:
        def clientId = "client2"
        def idemPotencyKey = "idemkey4"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti6", paymentConsentRequest)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, true)
        when:
        ResponsePaymentConsent updatedConsent = paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        then:
        noExceptionThrown()
        updatedConsent != null
        updatedConsent.getData() != null
        updatedConsent.getData().getStatus() == ResponsePaymentConsentData.StatusEnum.AUTHORISED
        updatedConsent.getData().getConsentId() != null
        updatedConsent.getData().getConsentId() == responseConsent.getData().getConsentId()
    }

    def "we can get a payment consent" () {
        given:
        def clientId = "client2"
        def idemPotencyKey = "idemkey5"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti7", paymentConsentRequest)

        when:
        ResponsePaymentConsent retrievedConsent = paymentsService.getConsent(responseConsent.getData().getConsentId(), clientId)

        then:
        noExceptionThrown()
        retrievedConsent != null
        retrievedConsent.getData() != null
        retrievedConsent.getData().getConsentId() != null
        retrievedConsent.getData().getConsentId() == responseConsent.getData().getConsentId()
    }

    def "we can create a payment" () {
        given:
        def clientId = "client3"
        def idemPotencyKey = "idemkey6"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti8", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, true)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        ResponsePixPayment paymentResponse = paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        noExceptionThrown()
        paymentResponse != null
        paymentResponse.getData() != null
        paymentResponse.getData().getPaymentId() != null
        //paymentResponse.getData().getPaymentId() != "tid1234"

        and:
        Optional<PaymentConsentEntity> consentEntityOpt = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().consentId)
        consentEntityOpt.isPresent()
        PaymentConsentEntity consentEntity = consentEntityOpt.get()
        consentEntity.status == "CONSUMED"
    }

    def "we can create a payment and force a 422" () {
        given:
        def clientId = "client4"
        def idemPotencyKey = "idemkey7"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti93", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", null)
        paymentRequest.data.payment.amount("20422.00")

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, true)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY
        ex.message == "Forced a 422 for payment request"

    }

    def "we can create payment and allow transaction identifier on a pix payment that has INIC local instrument" () {
        given:
        def clientId = "client45"
        def idemPotencyKey = "idemkey22"
        def transactionIdentification = "E00038166201907261559y6j6"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti81", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC2", "CRISS2", "CRISPB2", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.INIC, "100.00", "BRL", "", "", "cnpj", "", transactionIdentification)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, true)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        ResponsePixPayment paymentResponse = paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        noExceptionThrown()
        paymentResponse != null
        paymentResponse.getData() != null
        paymentResponse.getData().getPaymentId() != null

        and:
        Optional<PaymentConsentEntity> consentEntityOpt = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().consentId)
        consentEntityOpt.isPresent()
        PaymentConsentEntity consentEntity = consentEntityOpt.get()
        consentEntity.status == "CONSUMED"

        and:
        paymentResponse.getData().transactionIdentification == transactionIdentification
    }

    def "force a 422 error to disallow transaction identifier on a pix payment that has none INIC local instrument" () {
        given:
        def clientId = "client46"
        def idemPotencyKey = "idemkey23"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti82", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC3", "CRISS3", "CRISPB3", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", "E00038166201907261559y6j6")

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, true)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY
        ex.message.contains('NAO_INFORMADO')

    }

    def "we can create a payment and force a 422 with a specific error" () {
        given:
        def clientId = "client45"
        def idemPotencyKey = "gwgrgeberb"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti9g3", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", null)
        paymentRequest.data.payment.amount("20422.01")

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, true)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY
        ex.message == "DETALHE_PGTO_INVALIDO"

    }

    def "we can create a payment and force a 422 with a specific error msg" () {
        given:
        def clientId = "client46"
        def idemPotencyKey = "gwgrgeberb1"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti8g8", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "")
        paymentRequest.data.payment.amount("20422.02")

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, true)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY
        ex.message == "COBRANCA_INVALIDA"

    }

    def "we can create a payment and force a 422 return a specific error" () {
        given:
        def clientId = "client45"
        def idemPotencyKey = "gwgrgeberb2"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti9g4", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "")
        paymentRequest.data.payment.amount("20422.03")

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, true)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY
        ex.message == "PAGAMENTO_DIVERGENTE_DO_CONSENTIMENTO"

    }

    def "we can create a payment and force it to be rejected" () {
        given:
        def clientId = "client03"
        def idemPotencyKey = "idemkey64g4"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.data.payment.amount("20201.00")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjtih58", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", null)
        paymentRequest.data.payment.amount("20201.00")

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, true)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        ResponsePixPayment paymentResponse = paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        noExceptionThrown()
        paymentResponse != null
        paymentResponse.getData() != null
        paymentResponse.getData().getPaymentId() != null
        paymentResponse.getData().getStatus() == EnumPaymentStatusType.PDNG

        and:
        Optional<PaymentConsentEntity> consentEntityOpt = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().consentId)
        consentEntityOpt.isPresent()
        PaymentConsentEntity consentEntity = consentEntityOpt.get()
        consentEntity.status == "CONSUMED"

        when:
        paymentResponse = paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)
        then:
        noExceptionThrown()
        paymentResponse.getData().getStatus() == EnumPaymentStatusType.RJCT

    }

    def "we can create a payment and force it to be accepted" () {
        given:
        def clientId = "client04"
        def idemPotencyKey = "idemkey64h4"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.data.payment.amount("1333.00")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjtih88", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", null)
        paymentRequest.data.payment.amount("1333.00")

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, true)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        ResponsePixPayment paymentResponse = paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        noExceptionThrown()
        paymentResponse != null
        paymentResponse.getData() != null
        paymentResponse.getData().getPaymentId() != null
        paymentResponse.getData().getStatus() == EnumPaymentStatusType.PDNG

        and:
        Optional<PaymentConsentEntity> consentEntityOpt = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().consentId)
        consentEntityOpt.isPresent()
        PaymentConsentEntity consentEntity = consentEntityOpt.get()
        consentEntity.status == "CONSUMED"

        when:
        paymentResponse = paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)
        then:
        noExceptionThrown()
        paymentResponse.getData().getStatus() == EnumPaymentStatusType.ACCC

    }

    def "we can create a payment and force it to be ACSC" () {
        given:
        def clientId = "client09"
        def idemPotencyKey = "idemkey64h4wgetrn"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.data.payment.amount("1334.00")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjtih8ff8", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", null)
        paymentRequest.data.payment.amount("1334.00")

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, true)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        ResponsePixPayment paymentResponse = paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        noExceptionThrown()
        paymentResponse != null
        paymentResponse.getData() != null
        paymentResponse.getData().getPaymentId() != null
        paymentResponse.getData().getStatus() == EnumPaymentStatusType.PDNG

        and:
        Optional<PaymentConsentEntity> consentEntityOpt = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().consentId)
        consentEntityOpt.isPresent()
        PaymentConsentEntity consentEntity = consentEntityOpt.get()
        consentEntity.status == "CONSUMED"

        when:
        paymentResponse = paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)
        then:
        noExceptionThrown()
        paymentResponse.getData().getStatus() == EnumPaymentStatusType.ACSC

    }

    def "we can create a payment and force it to be ACSP" () {
        given:
        def clientId = "client11"
        def idemPotencyKey = "idemkey64hfevr4"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.data.payment.amount("1335.00")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjtih88w4g", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", null)
        paymentRequest.data.payment.amount("1335.00")

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, true)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        ResponsePixPayment paymentResponse = paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        noExceptionThrown()
        paymentResponse != null
        paymentResponse.getData() != null
        paymentResponse.getData().getPaymentId() != null
        paymentResponse.getData().getStatus() == EnumPaymentStatusType.PDNG

        and:
        Optional<PaymentConsentEntity> consentEntityOpt = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().consentId)
        consentEntityOpt.isPresent()
        PaymentConsentEntity consentEntity = consentEntityOpt.get()
        consentEntity.status == "CONSUMED"

        when:
        paymentResponse = paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)
        then:
        noExceptionThrown()
        paymentResponse.getData().getStatus() == EnumPaymentStatusType.ACSP

    }

    def "Unknown cnpj cannot create a payment" () {
        given:
        def clientId = "client3"
        def idemPotencyKey = "idemkey9"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti9", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "36386527000143", "", null)

        when:
        paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.UNPROCESSABLE_ENTITY

        and:
        Optional<PaymentConsentEntity> consentEntityOpt = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().consentId)
        consentEntityOpt.isPresent()
        PaymentConsentEntity consentEntity = consentEntityOpt.get()
        consentEntity.status == "CONSUMED"
    }

    def "we cannot reuse a consent" () {
        given:
        def clientId = "client3"
        def idemPotencyKey = "idemkey7"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti10", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj","", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.CONSUMED, false)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.UNPROCESSABLE_ENTITY
        e.message.contains('CONSENTIMENTO_INVALIDO')

    }

    def "idempotency works for payments" () {
        given:
        def clientId = "client4"
        def idemPotencyKey = "idemkey27"
        def idemPotencyKey2 = "idemkey28"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti21", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        ResponsePixPayment paymentResponse = paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        noExceptionThrown()
        paymentResponse != null
        paymentResponse.getData() != null
        paymentResponse.getData().getPaymentId() != null

        when:
        ResponsePixPayment paymentResponse2 = paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        noExceptionThrown()
        paymentResponse2 != null
        paymentResponse2.getData() != null
        paymentResponse2.getData().getPaymentId() != null
        paymentResponse2.getData().getPaymentId() == paymentResponse.getData().getPaymentId()

        when:
        // reset the consent, just to test this bit...
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)
        ResponsePixPayment paymentResponse3 = paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey2, UUID.randomUUID().toString(), paymentRequest)

        then:
        noExceptionThrown()
        paymentResponse3 != null
        paymentResponse3.getData() != null
        paymentResponse3.getData().getPaymentId() != null
        paymentResponse3.getData().getPaymentId() != paymentResponse.getData().getPaymentId()
    }

    def "we can update a payment" () {
        given:
        def clientId = "client5"
        def idemPotencyKey = "idemkey9"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjtiwgwrighwrogj", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj","", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        ResponsePixPayment paymentResponse = paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        UpdatePixPayment updatePixPayment = TestRequestDataFactory.createPaymentUpdateRequest(EnumPaymentStatusType.ACCC)

        when:
        ResponsePixPayment updatedPayment = paymentsService.updatePayment(paymentResponse.getData().getPaymentId(), updatePixPayment)

        then:
        noExceptionThrown()
        updatedPayment != null
        updatedPayment.getData() != null
        updatedPayment.getData().getPaymentId() != null
        updatedPayment.getData().getPaymentId() == paymentResponse.getData().getPaymentId()
    }

    def "we can get a payment" () {
        given:
        def clientId = "client6"
        def idemPotencyKey = "idemkey10"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjtiwgpeiruhgowa4igyuh0w4iogrs", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj","", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        ResponsePixPayment paymentResponse = paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        when:
        ResponsePixPayment foundPayment = paymentsService.getPayment(paymentResponse.getData().getPaymentId())

        then:
        noExceptionThrown()
        foundPayment != null
        foundPayment.getData() != null
        foundPayment.getData().getPaymentId() != null
        foundPayment.getData().getPaymentId() == paymentResponse.getData().getPaymentId()
    }

    def "we cannot create a payment if the consent amount doesn't match" () {
        given:
        def clientId = "client7"
        def idemPotencyKey = "idemkey11"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "1")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti24", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
    }

    def "we can not re-use a JTI for a payments" () {
        given:
        def clientId = "client6"
        def idemPotencyKey = "idemkey40"
        def idemPotencyKey2 = "idemkey41"

        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjtiwgpeiruhgowa4igyuh0w4iogrs4", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj","", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        String jti = UUID.randomUUID().toString()
        paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, jti, paymentRequest)

        //Create second payment
        CreatePaymentConsent paymentConsentRequest2 = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsent responseConsent2 = paymentsService.createConsent(clientId, idemPotencyKey2, "randomjtiwgpeiruhgowa4igyuh0w4iogrs5", paymentConsentRequest2)

        CreatePixPayment paymentRequest2 = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj","")

        when:
        paymentsService.createPayment(responseConsent2.getData().getConsentId(), idemPotencyKey, jti, paymentRequest2)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.FORBIDDEN
    }

    def "we cannot create a payment if the consent currency doesn't match" () {
        given:
        def clientId = "client8"
        def idemPotencyKey = "idemkey12"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "GBP", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti256", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "")

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
    }


    def "we cannot create a payment if the consent doesn't exist" () {
        given:
        def clientId = "client9"
        def idemPotencyKey = "idemkey13"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti24g", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, true)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        paymentsService.createPayment("Ceci n'est pas un payment consent id", idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.BAD_REQUEST
    }

    def "we cannot create a payment if the consent is awaiting authorisation" () {
        given:
        var clientId = "client10"
        var idemPotencyKey = "idemkey14"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti2wrgpi9", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "", null)

        when:
        paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.BAD_REQUEST
    }

    def "we cannot create a payment if the consent is rejected" () {
        given:
        var clientId = "client11"
        var idemPotencyKey = "idemkey15"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti2wgpiwehgwe", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj","", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.REJECTED, false)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.BAD_REQUEST
    }

    def "we cannot create a payment if the consent is consumed" () {
        given:
        var clientId = "client12"
        var idemPotencyKey = "idemkey16"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjtigwpeguw97fy9iuf3", paymentConsentRequest)
        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "")

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.CONSUMED, false)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage().contains("CONSENTIMENTO_INVALIDO")
    }

    def "we cannot create a payment if the consent is expired" () {
        given:
        var clientId = "client13"
        var idemPotencyKey = "idemkey17"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjtiwg;ijhwefo8w7ggf", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        var paymentConsentEntityOptional = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().getConsentId())
        var paymentConsentEntity = paymentConsentEntityOptional.get()
        paymentConsentEntity.setExpirationDateTime(Date.from(Instant.now() - Duration.ofDays(10)))
        paymentConsentRepository.update(paymentConsentEntity)

        when:
        paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.BAD_REQUEST
    }

    def "we cannot update a payment consent with a debtor if the consent already has it" () {
        given:
        var clientId = "client13"
        var idemPotencyKey = "idemkey17"
        CreatePaymentConsent paymentConsentRequest = new CreatePaymentConsent().data(new CreatePaymentConsentData()
                .businessEntity(
                        new BusinessEntity().document(
                                new BusinessEntityDocument().identification("12345678901234")
                                        .rel("CNPJ")))
                .creditor(new Identification().cpfCnpj("12345678901").name("Bob Creditor").personType(EnumCreditorPersonType.NATURAL.toString()))
                .debtorAccount(new DebtorAccount().accountType(EnumAccountPaymentsType.SLRY).ispb("12341234").issuer("1234").number("1234567890"))
                .loggedUser(new LoggedUser().document(new LoggedUserDocument().rel(accountHolder.documentRel).identification(accountHolder.documentIdentification)))
                .payment(new PaymentConsent().type(EnumPaymentType.PIX.toString()).date(LocalDate.now()).currency("BRL").amount("100.00").details(
                    new Details()
                        .localInstrument(EnumLocalInstrument.DICT)
                        .proxy("proxy")
                        .creditorAccount(new CreditorAccount()
                                .number("123")
                                .ispb("ispb")
                                .accountType(EnumAccountPaymentsType.CACC)
                                .issuer("issuer")

                        ))
                )
        )

        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjtiwrgerhg`", paymentConsentRequest)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        updatePaymentConsent.data.debtorAccount(new DebtorAccount().accountType(EnumAccountPaymentsType.SLRY).ispb("12341234").issuer("1234").number("1234567890"))

        when:
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.BAD_REQUEST
        e.message == 'Debtor account already set in initial consent'
    }

    def "we can update a payment consent with a debtor if the consent does not already have it" () {
        given:
        def clientId = "client13"
        def idemPotencyKey = "idemkey21"
        CreatePaymentConsent paymentConsentRequest = new CreatePaymentConsent().data(new CreatePaymentConsentData()
                .businessEntity(
                        new BusinessEntity().document(
                                new BusinessEntityDocument().identification("12345678901235")
                                        .rel("CNPJ")))
                .creditor(new Identification().cpfCnpj("12345678904").name("Bob Creditor").personType(EnumCreditorPersonType.NATURAL.toString()))
                .loggedUser(new LoggedUser().document(new LoggedUserDocument().rel(accountHolder.documentRel).identification(accountHolder.documentIdentification)))
                .payment(new PaymentConsent().type(EnumPaymentType.PIX.toString()).date(LocalDate.now()).currency("BRL")
                        .amount("100.00")
                        .details(new Details()
                                .localInstrument(EnumLocalInstrument.DICT)
                                .proxy("proxy")
                                .creditorAccount(new CreditorAccount()
                                        .ispb("ispb")
                                        .issuer("mockbank")
                                        .number("1234567890")
                                        .accountType(EnumAccountPaymentsType.CACC)
                                ))
                ))

        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti24wgherbs", paymentConsentRequest)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        updatePaymentConsent.data.debtorAccount(new DebtorAccount().accountType(EnumAccountPaymentsType.SLRY).ispb("123412534").issuer("1234").number("1234567890"))

        when:
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        then:
        noExceptionThrown()
    }

    def "we can not create a payment consent if wrong account number said" () {
        given:
        def clientId = "client13"
        def idemPotencyKey = "idemkey18"
        CreatePaymentConsent paymentConsentRequest = new CreatePaymentConsent().data(new CreatePaymentConsentData()
                .businessEntity(
                        new BusinessEntity().document(
                                new BusinessEntityDocument().identification("12345678901235")
                                        .rel("CNPJ")))
                .creditor(new Identification().cpfCnpj("12345678904").name("Bob Creditor").personType(EnumCreditorPersonType.NATURAL.toString()))
                .loggedUser(new LoggedUser().document(new LoggedUserDocument().rel(accountHolder.documentRel).identification(accountHolder.documentIdentification)))
                .payment(new PaymentConsent().type(EnumPaymentType.PIX.toString()).date(LocalDate.now()).currency("BRL")
                        .amount("100.00")
                        .details(new Details()
                                .localInstrument(EnumLocalInstrument.DICT)
                                .proxy("proxy")
                                .creditorAccount(new CreditorAccount()
                                        .ispb("ispb")
                                        .issuer("mockbank")
                                        .number("1234567890")
                                        .accountType(EnumAccountPaymentsType.CACC)
                                ))
                ))

        when:
        paymentConsentRequest.getData()
                .debtorAccount(new DebtorAccount().accountType(EnumAccountPaymentsType.SLRY).ispb("12341234").issuer("1234").number("123456"))
        paymentsService.createConsent(clientId, idemPotencyKey, "randomjti24wgherbs1", paymentConsentRequest)


        then:
        HttpStatusException e1 = thrown()
        e1.getStatus() == HttpStatus.BAD_REQUEST
        e1.getMessage() == "User with number 123456 not found"

    }

    def "we cannot create a payment consent re-using JTI" () {
        given:
        def clientId = "client1"
        def idemPotencyKey = "idemkey1"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def jti = "anotherjti"

        when:
        paymentsService.createConsent(clientId, idemPotencyKey, jti, paymentConsentRequest)

        then:
        noExceptionThrown()

        when:
        paymentsService.createConsent(clientId, idemPotencyKey, jti, paymentConsentRequest)

        then:
        thrown HttpStatusException
    }

    def "we cannot create a payment consent with an invalid currency"() {
        given:
        var clientId = "client13"
        var idemPotencyKey = "idemkey17"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory
                .createPaymentConsentRequest("BID1", "REL1",
                        "66.001.455/0001-30", "Bob Creditor", EnumCreditorPersonType.NATURAL,
                        EnumAccountPaymentsType.SLRY, "ispb1", "issuer1",
                        "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(),
                        EnumPaymentType.PIX.toString(), LocalDate.now(), "ZZZ", "100")

        when:
        paymentsService.createConsent(clientId, idemPotencyKey, "randomjtiwg;ijhwefo8w7ggf", paymentConsentRequest)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "DETALHE_PGTO_INVALIDO: O campo currency não atende os requisitos de preenchimento. O valor dado não é uma moeda válida."

    }

    def "idempotency requires functionally identical payloads" () {
        given:
        def clientId = "client4"
        def idemPotencyKey = "idemkey49"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti248", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        ResponsePixPayment paymentResponse = paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        noExceptionThrown()
        paymentResponse != null
        paymentResponse.getData() != null
        paymentResponse.getData().getPaymentId() != null

        when:
        paymentRequest.getData().getPayment().setAmount("99999")
        paymentsService.createPayment(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY

    }
    def "payment consent using date in past returns 422"() {
        given:
        var clientId = "client13"
        var idemPotencyKey = "idemkey17"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory
                .createPaymentConsentRequest("BID1", "REL1",
                        "66.001.455/0001-30", "Bob Creditor", EnumCreditorPersonType.NATURAL,
                        EnumAccountPaymentsType.SLRY, "ispb1", "issuer1",
                        "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(),
                        EnumPaymentType.PIX.toString(), LocalDate.parse("2019-11-12"), "BRL", "100")

        when:
        paymentsService.createConsent(clientId, idemPotencyKey, "randomjtiwg;ijhwefo8w7ggf", paymentConsentRequest)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "DATA_PGTO_INVALIDA: Data de pagamento inválida no contexto, data no passado. Para pagamentos únicos deve ser informada a data atual, do dia corrente."

    }

    def "payment consent fail tests - date in past"() {
        given:
        var clientId = "client15"
        var idemPotencyKey = "idemkey19"

        PaymentConsent pc = new PaymentConsent()
                .type(EnumPaymentType.PIX.toString())
                .date(new Date().toInstant().atZone(ZoneId.of("America/Sao_Paulo")).toLocalDate().minusDays(1))
                .currency("BRL")
                .amount("100.00")
                .details(
                        new Details()
                                .localInstrument(EnumLocalInstrument.DICT)
                                .proxy("proxy")
                                .creditorAccount(new CreditorAccount()
                                        .number("123")
                                        .ispb("ispb")
                                        .accountType(EnumAccountPaymentsType.CACC)
                                        .issuer("issuer")

                                )
                )
        BusinessEntity be = new BusinessEntity().document(
                new BusinessEntityDocument()
                        .identification("12345678901234")
                        .rel("CNPJ"))

        Identification id = new Identification()
                .cpfCnpj("12345678901")
                .name("Bob Creditor")
                .personType(EnumCreditorPersonType.NATURAL.toString())

        DebtorAccount da = new DebtorAccount()
                .accountType(EnumAccountPaymentsType.SLRY)
                .ispb("12341234")
                .issuer("1234")
                .number("1234567890")

        LoggedUser lu = new LoggedUser()
                .document(
                        new LoggedUserDocument()
                                .rel(accountHolder.documentRel)
                                .identification(accountHolder.documentIdentification)
                )

        CreatePaymentConsent paymentConsentRequest = new CreatePaymentConsent().data(new CreatePaymentConsentData()
                .businessEntity(be)
                .creditor(id)
                .debtorAccount(da)
                .loggedUser(lu)
                .payment(pc)
        )

        when:
        paymentsService.createConsent(clientId, idemPotencyKey, "somejti", paymentConsentRequest)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "DATA_PGTO_INVALIDA: Data de pagamento " +
                "inválida no contexto, data no passado. Para pagamentos únicos deve ser informada a data atual, " +
                "do dia corrente."
    }
    def "payment consent fail tests - MANU validation"() {
        given:
        var clientId = "client16"
        var idemPotencyKey = "idemkey20"

        PaymentConsent pc = new PaymentConsent()
            .type(EnumPaymentType.PIX.toString())
            .date(LocalDate.now())
            .currency("BRL")
            .amount("100.00")
            .details(
                new Details()
                    .localInstrument(EnumLocalInstrument.MANU)
                    .qrCode("qrcode")
                    .proxy("proxy")
                    .creditorAccount(new CreditorAccount()
                        .number("1234567890")
                        .ispb("ispb")
                        .accountType(EnumAccountPaymentsType.CACC)
                        .issuer("issuer")

                    )
            )
        BusinessEntity be = new BusinessEntity().document(
                new BusinessEntityDocument()
                        .identification(accountHolder.documentIdentification)
                        .rel(accountHolder.documentRel))

        Identification id = new Identification()
                .cpfCnpj("12345678901")
                .name("Bob Creditor")
                .personType(EnumCreditorPersonType.NATURAL.toString())

        DebtorAccount da = new DebtorAccount()
                .accountType(EnumAccountPaymentsType.SLRY)
                .ispb("12341234")
                .issuer("1234")
                .number("1234567890")

        LoggedUser lu = new LoggedUser()
                .document(
                        new LoggedUserDocument()
                                .rel(accountHolder.documentRel)
                                .identification(accountHolder.documentIdentification)
                )

        when:
        CreatePaymentConsent paymentConsentRequest = new CreatePaymentConsent().data(new CreatePaymentConsentData()
                .businessEntity(be)
                .creditor(id)
                .debtorAccount(da)
                .loggedUser(lu)
                .payment(pc)
        )
        paymentsService.createConsent(clientId, idemPotencyKey, "somejti", paymentConsentRequest)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "DATA_PGTO_INVALIDA: O campo qrCode não preenche os requisitos de preenchimento. qrCode não deve estar presente para o localInstrument do tipo MANU."

        when:
        pc.getDetails().setQrCode(null)
        paymentConsentRequest = new CreatePaymentConsent().data(new CreatePaymentConsentData()
                .businessEntity(be)
                .creditor(id)
                .debtorAccount(da)
                .loggedUser(lu)
                .payment(pc)
        )
        paymentsService.createConsent(clientId, idemPotencyKey, "somejti", paymentConsentRequest)

        then:
        e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "DATA_PGTO_INVALIDA: O campo proxy não preenche os requisitos de preenchimento. proxy não deve estar presente para o localInstrument do tipo MANU."

        when:
        pc.getDetails().setQrCode(null)
        pc.getDetails().setProxy(null)
        paymentConsentRequest = new CreatePaymentConsent().data(new CreatePaymentConsentData()
                .businessEntity(be)
                .creditor(id)
                .debtorAccount(da)
                .loggedUser(lu)
                .payment(pc)
        )
        paymentsService.createConsent(clientId, idemPotencyKey, "somejti", paymentConsentRequest)

        then:
        noExceptionThrown()

    }

    def "payment consent fail tests - DICT validation"() {
        given:
        var clientId = "client16"
        var idemPotencyKey = "idemkey20"

        PaymentConsent pc = new PaymentConsent()
                .type(EnumPaymentType.PIX.toString())
                .date(LocalDate.now())
                .currency("BRL")
                .amount("100.00")
                .details(
                        new Details()
                                .localInstrument(EnumLocalInstrument.DICT)
                                .proxy(null)
                                .qrCode("qrcode")
                                .creditorAccount(new CreditorAccount()
                                        .number("123")
                                        .ispb("ispb")
                                        .accountType(EnumAccountPaymentsType.CACC)
                                        .issuer("issuer")

                                )
                )
        BusinessEntity be = new BusinessEntity().document(
                new BusinessEntityDocument()
                        .identification("12345678901234")
                        .rel("CNPJ"))

        Identification id = new Identification()
                .cpfCnpj("12345678901")
                .name("Bob Creditor")
                .personType(EnumCreditorPersonType.NATURAL.toString())

        DebtorAccount da = new DebtorAccount()
                .accountType(EnumAccountPaymentsType.SLRY)
                .ispb("12341234")
                .issuer("1234")
                .number("1234567890")

        LoggedUser lu = new LoggedUser()
                .document(
                        new LoggedUserDocument()
                                .rel(accountHolder.documentRel)
                                .identification(accountHolder.documentIdentification)
                )

        when:
        CreatePaymentConsent paymentConsentRequest = new CreatePaymentConsent().data(new CreatePaymentConsentData()
                .businessEntity(be)
                .creditor(id)
                .debtorAccount(da)
                .loggedUser(lu)
                .payment(pc)
        )
        paymentsService.createConsent(clientId, idemPotencyKey, "somejti", paymentConsentRequest)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "DATA_PGTO_INVALIDA: O campo qrCode não preenche os requisitos de preenchimento. qrCode não deve estar presente para o localInstrument do tipo DICT."

        when:
        pc.getDetails().setQrCode(null)
        paymentConsentRequest = new CreatePaymentConsent().data(new CreatePaymentConsentData()
                .businessEntity(be)
                .creditor(id)
                .debtorAccount(da)
                .loggedUser(lu)
                .payment(pc)
        )
        paymentsService.createConsent(clientId, idemPotencyKey, "somejti", paymentConsentRequest)

        then:
        e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "DATA_PGTO_INVALIDA: O campo proxy não preenche os requisitos de preenchimento. proxy deve estar presente para o localInstrument do tipo DICT."
    }

    def "payment consent fail tests - QRDN"() {
        given:
        var clientId = "client16"
        var idemPotencyKey = "idemkey20"

        PaymentConsent pc = new PaymentConsent()
            .type(EnumPaymentType.PIX.toString())
            .date(LocalDate.now())
            .currency("BRL")
            .amount("100.00")
            .details(
                new Details()
                    .localInstrument(EnumLocalInstrument.QRDN)
                    .proxy(null)
                    .qrCode(null)
                    .creditorAccount(new CreditorAccount()
                        .number("123")
                        .ispb("ispb")
                        .accountType(EnumAccountPaymentsType.CACC)
                        .issuer("issuer")

                    )
            )
        BusinessEntity be = new BusinessEntity()
                .document(new BusinessEntityDocument()
                        .identification("12345678901234")
                        .rel("CNPJ"))

        Identification id = new Identification()
                .cpfCnpj("12345678901")
                .name("Bob Creditor")
                .personType(EnumCreditorPersonType.NATURAL.toString())

        DebtorAccount da = new DebtorAccount()
                .accountType(EnumAccountPaymentsType.SLRY)
                .ispb("12341234")
                .issuer("1234")
                .number("1234567890")

        LoggedUser lu = new LoggedUser()
                .document(
                        new LoggedUserDocument()
                                .rel(accountHolder.documentRel)
                                .identification(accountHolder.documentIdentification)
                )

        when:
        CreatePaymentConsent paymentConsentRequest = new CreatePaymentConsent()
                .data(new CreatePaymentConsentData()
                        .businessEntity(be)
                        .creditor(id)
                        .debtorAccount(da)
                        .loggedUser(lu)
                        .payment(pc)
                )
        paymentsService.createConsent(clientId, idemPotencyKey, "somejti", paymentConsentRequest)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "DATA_PGTO_INVALIDA: O campo proxy não preenche os requisitos de preenchimento. proxy deve estar presente para o localInstrument do tipo QRDN."

        when:
        pc.getDetails().setProxy("proxy")
        paymentConsentRequest = new CreatePaymentConsent().data(new CreatePaymentConsentData()
                .businessEntity(be)
                .creditor(id)
                .debtorAccount(da)
                .loggedUser(lu)
                .payment(pc)
        )
        paymentsService.createConsent(clientId, idemPotencyKey, "somejti", paymentConsentRequest)

        then:
        e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "DATA_PGTO_INVALIDA: O campo qrCode não preenche os requisitos de preenchimento. qrCode deve estar presente para o localInstrument do tipo QRDN."

        when:
        pc.getDetails().setQrCode("qr")
        paymentConsentRequest = new CreatePaymentConsent().data(new CreatePaymentConsentData()
                .businessEntity(be)
                .creditor(id)
                .debtorAccount(da)
                .loggedUser(lu)
                .payment(pc)
        )
        paymentsService.createConsent(clientId, idemPotencyKey, "somejti3", paymentConsentRequest)

        then:
        noExceptionThrown()
    }
    def "payment consent fail tests - QRES"() {
        given:
        var clientId = "client16"
        var idemPotencyKey = "idemkey20"

        PaymentConsent pc = new PaymentConsent()
                .type(EnumPaymentType.PIX.toString())
                .date(LocalDate.now())
                .currency("BRL")
                .amount("100.00")
                .details(
                        new Details()
                                .localInstrument(EnumLocalInstrument.QRES)
                                .proxy(null)
                                .qrCode(null)
                                .creditorAccount(new CreditorAccount()
                                        .number("123")
                                        .ispb("ispb")
                                        .accountType(EnumAccountPaymentsType.CACC)
                                        .issuer("issuer")

                                )
                )
        BusinessEntity be = new BusinessEntity()
                .document(new BusinessEntityDocument()
                        .identification("12345678901234")
                        .rel("CNPJ"))

        Identification id = new Identification()
                .cpfCnpj("12345678901")
                .name("Bob Creditor")
                .personType(EnumCreditorPersonType.NATURAL.toString())

        DebtorAccount da = new DebtorAccount()
                .accountType(EnumAccountPaymentsType.SLRY)
                .ispb("12341234")
                .issuer("1234")
                .number("1234567890")

        LoggedUser lu = new LoggedUser()
                .document(
                        new LoggedUserDocument()
                                .rel(accountHolder.documentRel)
                                .identification(accountHolder.documentIdentification)
                )

        when:
        CreatePaymentConsent paymentConsentRequest = new CreatePaymentConsent()
                .data(new CreatePaymentConsentData()
                        .businessEntity(be)
                        .creditor(id)
                        .debtorAccount(da)
                        .loggedUser(lu)
                        .payment(pc)
                )
        paymentsService.createConsent(clientId, idemPotencyKey, "somejti", paymentConsentRequest)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "DATA_PGTO_INVALIDA: O campo proxy não preenche os requisitos de preenchimento. proxy deve estar presente para o localInstrument do tipo QRES."

        when:
        pc.getDetails().setProxy("proxy")
        paymentConsentRequest = new CreatePaymentConsent().data(new CreatePaymentConsentData()
                .businessEntity(be)
                .creditor(id)
                .debtorAccount(da)
                .loggedUser(lu)
                .payment(pc)
        )
        paymentsService.createConsent(clientId, idemPotencyKey, "somejti", paymentConsentRequest)

        then:
        e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "DATA_PGTO_INVALIDA: O campo qrCode não preenche os requisitos de preenchimento. qrCode deve estar presente para o localInstrument do tipo QRES."

        when:
        pc.getDetails().setQrCode("qr")
        paymentConsentRequest = new CreatePaymentConsent().data(new CreatePaymentConsentData()
                .businessEntity(be)
                .creditor(id)
                .debtorAccount(da)
                .loggedUser(lu)
                .payment(pc)
        )
        paymentsService.createConsent(clientId, idemPotencyKey, "somejti2", paymentConsentRequest)

        then:
        noExceptionThrown()

    }
    def "payment consent fail tests - Invalid enums"() {
        given:
        var clientId = "client16"
        var idemPotencyKey = "idemkey20"
        PaymentConsent pc = new PaymentConsent()
                .type("BAD")
                .date(LocalDate.now())
                .currency("BRL")
                .amount("100.00")
                .details(
                        new Details()
                                .localInstrument(EnumLocalInstrument.MANU)
                                .creditorAccount(new CreditorAccount()
                                        .number("123")
                                        .ispb("ispb")
                                        .accountType(EnumAccountPaymentsType.CACC)
                                        .issuer("issuer")

                                )
                )
        BusinessEntity be = new BusinessEntity()
                .document(new BusinessEntityDocument()
                        .identification("12345678901234")
                        .rel("CNPJ"))

        Identification id = new Identification()
                .cpfCnpj("12345678901")
                .name("Bob Creditor")
                .personType(EnumCreditorPersonType.NATURAL.toString())

        DebtorAccount da = new DebtorAccount()
                .accountType(EnumAccountPaymentsType.SLRY)
                .ispb("12341234")
                .issuer("1234")
                .number("1234567890")

        LoggedUser lu = new LoggedUser()
                .document(
                        new LoggedUserDocument()
                                .rel(accountHolder.documentRel)
                                .identification(accountHolder.documentIdentification)
                )

        when:
        CreatePaymentConsent paymentConsentRequest = new CreatePaymentConsent()
                .data(new CreatePaymentConsentData()
                        .businessEntity(be)
                        .creditor(id)
                        .debtorAccount(da)
                        .loggedUser(lu)
                        .payment(pc)
                )
        paymentsService.createConsent(clientId, idemPotencyKey, "somejti69", paymentConsentRequest)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "FORMA_PGTO_INVALIDA: Meio de pagamento inválido."

        when:
        pc.setType(EnumPaymentType.PIX.toString())
        id.setPersonType("bad!")
        paymentConsentRequest = new CreatePaymentConsent().data(new CreatePaymentConsentData()
                .businessEntity(be)
                .creditor(id)
                .debtorAccount(da)
                .loggedUser(lu)
                .payment(pc)
        )
        paymentsService.createConsent(clientId, idemPotencyKey, "somejti420", paymentConsentRequest)

        then:
        e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "DETALHE_PGTO_INVALIDO: O campo creditorAccount - accountType não atende os requisitos de preenchimento."

        when:
        id.setPersonType(EnumCreditorPersonType.JURIDICA.toString())
        paymentConsentRequest = new CreatePaymentConsent().data(new CreatePaymentConsentData()
                .businessEntity(be)
                .creditor(id)
                .debtorAccount(da)
                .loggedUser(lu)
                .payment(pc)
        )
        paymentsService.createConsent(clientId, idemPotencyKey, "somejti4", paymentConsentRequest)

        then:
        noExceptionThrown()

    }
    def "Reponse payment full contains the right stuff"(){
        given:
        def clientId = "client20"
        def idemPotencyKey = "idemkey20"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def jti = "randomjti20"

        when:
        paymentsService.getConsentFull("definitely not an existing consent ID")

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.NOT_FOUND
        e.getMessage().contains("No payment consent with ID")

        when:
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, jti, paymentConsentRequest)
        String consentId = responseConsent.getData().getConsentId()
        def consentFull = paymentsService.getConsentFull(consentId)

        then:
        noExceptionThrown()
        consentFull != null
        consentFull.getData() != null
        consentFull.getData().getSub() != null
        consentFull.getData().getSub() == accountHolder.getUserId()

        when:
        paymentsService.getConsent(consentId, "client19")

        then:
        e = thrown()
        e.getStatus() == HttpStatus.FORBIDDEN
    }

    def "Payment tests"() {
        given:
        def clientId = "client30"
        def idemPotencyKey = "idemkey30"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.getData().getPayment().getDetails().setProxy(null)
        paymentConsentRequest.getData().getPayment().getDetails().setLocalInstrument(EnumLocalInstrument.MANU)

        CreatePixPayment paymentRequest = TestRequestDataFactory.testPixPayment()
        paymentRequest.getData().setProxy(null)
        paymentRequest.getData().setQrCode(null)

        def jti = "randomjti30"


        when:
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, jti, paymentConsentRequest)
        String consentId = responseConsent.getData().getConsentId()
        UpdatePaymentConsent update = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, true)
        paymentsService.updateConsent(consentId, update)
        ResponsePixPayment paymentResponse =  paymentsService.createPayment(consentId, idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        noExceptionThrown()

        when:
        paymentsService.createPayment(consentId, idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        noExceptionThrown()

        when:
        paymentsService.getPayment(paymentResponse.getData().getPaymentId())

        then:
        noExceptionThrown()

        when:
        paymentRequest = TestRequestDataFactory.testPixPayment()
        paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())

        clientId = "client32"
        idemPotencyKey = "idemkey32"
        jti = "randomjti32"

        responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, jti, paymentConsentRequest)
        consentId = responseConsent.getData().getConsentId()
        paymentsService.updateConsent(consentId, update)
        paymentsService.createPayment(consentId, idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        paymentRequest.getData().setProxy(null)
        paymentsService.createPayment(consentId, idemPotencyKey, UUID.randomUUID().toString(), paymentRequest)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "New version of resource not the same"



        when:
        paymentRequest = TestRequestDataFactory.testPixPayment()
        paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.getData().getPayment().getDetails().setLocalInstrument(EnumLocalInstrument.QRDN)
        paymentConsentRequest.getData().getPayment().getDetails().setProxy("proxy")
        paymentConsentRequest.getData().getPayment().getDetails().setQrCode("qrCode")

        clientId = "client33"
        idemPotencyKey = "idemkey33"
        jti = "randomjti33"

        responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, jti, paymentConsentRequest)
        consentId = responseConsent.getData().getConsentId()
        paymentsService.updateConsent(consentId, update)
        paymentsService.createPayment(consentId, idemPotencyKey,UUID.randomUUID().toString(), paymentRequest)

        paymentRequest.getData().setQrCode(null)
        paymentsService.createPayment(consentId, idemPotencyKey,UUID.randomUUID().toString(), paymentRequest)

        then:
        e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "New version of resource not the same"

        when:
        paymentsService.updatePayment("BAD PAYMENT ID!", TestRequestDataFactory.createPaymentUpdateRequest(EnumPaymentStatusType.RJCT))

        then:
        e = thrown()
        e.getStatus() == HttpStatus.NOT_FOUND
        e.getMessage() == "Requested pix payment not found"
    }

    def "we can patch a consent"(){
        given:
        def clientId = "client3"
        def idemPotencyKey = "idemkey62"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti84240", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, true)
        ResponsePaymentConsent updatedConsent = paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)
        System.println(updatedConsent)

        paymentsService.createPayment(responseConsent.getData().getConsentId(),"someidempotencykey","somejti320", paymentRequest)

        when:
        PatchPaymentsConsent patchPaymentsConsent = TestRequestDataFactory.testPatchPaymentConsent()
        ResponsePaymentConsent response = paymentsService.patchConsent(responseConsent.getData().getConsentId(), patchPaymentsConsent)

        then:
        noExceptionThrown()
        responseConsent.getData().getConsentId() != null
        System.out.println(response)
    }

    def "we can patch a consent and revokes with SASC status"(){
        given:
        def clientId = "client3"
        def idemPotencyKey = "idemkey6dsfasdf"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti898", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "")

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, true)
        ResponsePaymentConsent updatedConsent = paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)
        System.println(updatedConsent)

        ResponsePixPayment payment = paymentsService.createPayment(responseConsent.getData().getConsentId(),"someidempotencykey69","somejti65", paymentRequest)
        paymentsService.updatePayment(payment.getData().getPaymentId(),TestRequestDataFactory.createPaymentUpdateRequest(EnumPaymentStatusType.SASC))
        when:
        PatchPaymentsConsent patchPaymentsConsent = TestRequestDataFactory.testPatchPaymentConsent()
        ResponsePaymentConsent response = paymentsService.patchConsent(responseConsent.getData().getConsentId(), patchPaymentsConsent)

        then:
        noExceptionThrown()
        responseConsent.getData().getConsentId() != null
        System.out.println(response)
    }

    def "we can patch a consent and revokes with SASP status"(){
        given:
        def clientId = "client3"
        def idemPotencyKey = "idemkey6sdafg"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti86546546546", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, true)
        ResponsePaymentConsent updatedConsent = paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)
        System.println(updatedConsent)

        ResponsePixPayment payment = paymentsService.createPayment(responseConsent.getData().getConsentId(),"someidempotencykey68","somejti65465465456", paymentRequest)
        paymentsService.updatePayment(payment.getData().getPaymentId(),TestRequestDataFactory.createPaymentUpdateRequest(EnumPaymentStatusType.SASP))
        when:
        PatchPaymentsConsent patchPaymentsConsent = TestRequestDataFactory.testPatchPaymentConsent()
        ResponsePaymentConsent response = paymentsService.patchConsent(responseConsent.getData().getConsentId(), patchPaymentsConsent)

        then:
        noExceptionThrown()
        responseConsent.getData().getConsentId() != null
        System.out.println(response)
    }

    def "we can patch a consent and revokes with PNDG status"(){
        given:
        def clientId = "client3"
        def idemPotencyKey = "idemkey69"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti86546", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, true)
        ResponsePaymentConsent updatedConsent = paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)
        System.println(updatedConsent)

        ResponsePixPayment payment = paymentsService.createPayment(responseConsent.getData().getConsentId(),"someidempotencykey789789","somejti789789", paymentRequest)
        ResponsePixPayment test = paymentsService.updatePayment(payment.getData().getPaymentId(),TestRequestDataFactory.createPaymentUpdateRequest(EnumPaymentStatusType.PDNG))
        System.out.println(test)
        when:
        PatchPaymentsConsent patchPaymentsConsent = TestRequestDataFactory.testPatchPaymentConsent()
        ResponsePaymentConsent response = paymentsService.patchConsent(responseConsent.getData().getConsentId(), patchPaymentsConsent)

        then:
        noExceptionThrown()
        response.getData().getStatus() == ResponsePaymentConsentData.StatusEnum.REVOKED
        System.out.println(response)
    }

    def "when we PATCH a consent no logged user 422"(){
        given:
        EnumAuthorisationPatchStatusType.valueOf("BAD") >> "BAD"

        def clientId = "client3"
        def idemPotencyKey = "idemkey63"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti8424099", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "",null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, true)
        ResponsePaymentConsent updatedConsent = paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)
        System.println(updatedConsent)

        paymentsService.createPayment(responseConsent.getData().getConsentId(),"someidempotencykey","somejti320999", paymentRequest)

        when:
        PatchPaymentsConsent patchPaymentsConsent = TestRequestDataFactory.testPatchPaymentConsentNoLoggedUser()
        paymentsService.patchConsent(responseConsent.getData().getConsentId(), patchPaymentsConsent)

        then:
        def error = thrown(HttpStatusException.class)
        error.message == "INFORMACAO_USUARIO_REQUERIDA"
    }

    def "when we PATCH a consent tpp/aspsp no additional reason 422"(){
        given:
        EnumAuthorisationPatchStatusType.valueOf("BAD") >> "BAD"

        def clientId = "client3"
        def idemPotencyKey = "idemkey64"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "iamajti", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, true)
        ResponsePaymentConsent updatedConsent = paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)
        System.println(updatedConsent)

        paymentsService.createPayment(responseConsent.getData().getConsentId(),"someidempotencykey","areallylegitjtu", paymentRequest)

        when:
        PatchPaymentsConsent patchPaymentsConsent = TestRequestDataFactory.testPatchPaymentConsentNoAdditionalReason()
        paymentsService.patchConsent(responseConsent.getData().getConsentId(), patchPaymentsConsent)

        then:
        def error = thrown(HttpStatusException.class)
        error.message == "INFORMACAO_ADICIONAL_REVOGACAO_REQUERIDA"
    }

    def "when we PATCH a consent not tpp/aspsp and revocation reason is fraud/account closure 422"(){
        given:
        EnumAuthorisationPatchStatusType.valueOf("BAD") >> "BAD"

        def clientId = "client3"
        def idemPotencyKey = "idemkey65"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "jtijtijti", paymentConsentRequest)

        CreatePixPayment paymentRequest = TestRequestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "",null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, true)
        ResponsePaymentConsent updatedConsent = paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)
        System.println(updatedConsent)

        paymentsService.createPayment(responseConsent.getData().getConsentId(),"someidempotencykey","notajtuipromise", paymentRequest)
        when:
        PatchPaymentsConsent patchPaymentsConsent = TestRequestDataFactory.testPatchPaymentConsentRevocationReasonNotAllowed()
        paymentsService.patchConsent(responseConsent.getData().getConsentId(), patchPaymentsConsent)

        then:
        def error = thrown(HttpStatusException.class)
        error.message == "MOTIVO_REVOGACAO_NAO_PERMITIDO"
    }

    def "we can patch a consent and no schedule we 422"(){
        given:
        def clientId = "client3"
        def idemPotencyKey = "idemkeyest"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsentPatchNoSchedule(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "somerandomjti12", paymentConsentRequest)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.CONSUMED, true)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)
        when:
        PatchPaymentsConsent patchPaymentsConsent = TestRequestDataFactory.testPatchPaymentConsent()
        paymentsService.patchConsent(responseConsent.getData().getConsentId(), patchPaymentsConsent)

        then:
        def error = thrown(HttpStatusException.class)
        error.message == "OPERACAO_NAO_SUPORTADA_TIPO_CONSENTIMENTO"
    }

    def "we can patch a consent and its consumed so you guessed it we 422"(){
        given:
        def clientId = "client3"
        def idemPotencyKey = "idemkey66"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsentPatchNoSchedule(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti8424078978", paymentConsentRequest)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, true)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)
        when:
        PatchPaymentsConsent patchPaymentsConsent = TestRequestDataFactory.testPatchPaymentConsent()
        paymentsService.patchConsent(responseConsent.getData().getConsentId(), patchPaymentsConsent)

        then:
        def error = thrown(HttpStatusException.class)
        error.message == "OPERACAO_NAO_PERMITIDA_STATUS"
    }

    def "we cannot create a payment consent for an unknown user" () {
        given:
        def clientId = "client3"
        def idemPotencyKey = "idemkey71"

        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsentPatchNoSchedule('12345', 'CPF')

        when:
        paymentsService.createConsent(clientId, idemPotencyKey, "randomjti8424078979", paymentConsentRequest)

        then:
        thrown HttpStatusException.class
    }

    def "payment consent responses contain a logged user" () {
        given:
        def clientId = "client3"
        def idemPotencyKey = "idemkey82"

        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsentPatchNoSchedule(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())

        when:
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(clientId, idemPotencyKey, "randomjti8424078981", paymentConsentRequest)

        then:
        noExceptionThrown()
        responseConsent.getData().getLoggedUser().getDocument().getIdentification() == accountHolder.getDocumentIdentification()
        responseConsent.getData().getLoggedUser().getDocument().getRel() == accountHolder.getDocumentRel()
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
