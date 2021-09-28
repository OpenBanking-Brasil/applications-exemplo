package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.TestDataFactory
import com.raidiam.trustframework.bank.domain.PaymentConsentEntity
import com.raidiam.trustframework.bank.repository.PaymentConsentRepository
import com.raidiam.trustframework.bank.repository.PixPaymentRepository
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

@MicronautTest(transactional = false, environments = ["db"])
class PaymentsServiceSpec extends Specification {
    @Inject
    PaymentsService paymentsService

    @Inject
    PaymentConsentRepository paymentConsentRepository

    @Inject
    PixPaymentRepository pixPaymentRepository

    def "we can create a payment consent" () {
        given:
        def clientId = "client1"
        def idemPotencyKey = "idemkey1"
        CreatePaymentConsent paymentConsentRequest = TestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                        "Bob Creditor", Identification.PersonTypeEnum.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                        "issuer1","AC001", "LUREL1", "LUID1", PaymentConsent.TypeEnum.PIX,
                        LocalDate.now(), "BRL", "100")

        when:
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(paymentConsentRequest, clientId, idemPotencyKey)

        then:
        noExceptionThrown()
        responseConsent != null
        responseConsent.getData() != null
        responseConsent.getData().getStatus() == ResponsePaymentConsentData.StatusEnum.AWAITING_AUTHORISATION
        responseConsent.getData().getConsentId() != null
    }

    def "unknown CNPJs can create a payment consent" () {
        given:
        def clientId = "client1"
        def idemPotencyKey = "idemkey1"
        CreatePaymentConsent paymentConsentRequest = TestDataFactory.createPaymentConsentRequest("MADEUP", "REL1", "66.001.455/0001-30",
                "Bob Creditor", Identification.PersonTypeEnum.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","AC001", "LUREL1", "LUID1", PaymentConsent.TypeEnum.PIX,
                LocalDate.now(), "BRL", "100")

        when:
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(paymentConsentRequest, clientId, idemPotencyKey)

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
        CreatePaymentConsent paymentConsentRequest = TestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", Identification.PersonTypeEnum.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","AC001", "LUREL1", "LUID1", PaymentConsent.TypeEnum.PIX,
                LocalDate.now(), "BRL", "100")
        when:
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(paymentConsentRequest, clientId, idemPotencyKey2)

        then:
        noExceptionThrown()
        responseConsent != null
        responseConsent.getData() != null
        responseConsent.getData().getStatus() == ResponsePaymentConsentData.StatusEnum.AWAITING_AUTHORISATION
        responseConsent.getData().getConsentId() != null

        when:
        ResponsePaymentConsent responseConsent2 = paymentsService.createConsent(paymentConsentRequest, clientId, idemPotencyKey2)

        then:
        noExceptionThrown()
        responseConsent2 != null
        responseConsent2.getData() != null
        responseConsent2.getData().getStatus() == ResponsePaymentConsentData.StatusEnum.AWAITING_AUTHORISATION
        responseConsent2.getData().getConsentId() != null
        responseConsent2.getData().getConsentId() == responseConsent.getData().getConsentId()

        when:
        ResponsePaymentConsent responseConsent3 = paymentsService.createConsent(paymentConsentRequest, clientId, idemPotencyKey3)

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
        CreatePaymentConsent paymentConsentRequest = TestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", Identification.PersonTypeEnum.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","AC001", "LUREL1", "LUID1", PaymentConsent.TypeEnum.PIX,
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(paymentConsentRequest, clientId, idemPotencyKey)

        UpdatePaymentConsent updatePaymentConsent = TestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED)

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
        CreatePaymentConsent paymentConsentRequest = TestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", Identification.PersonTypeEnum.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","AC001", "LUREL1", "LUID1", PaymentConsent.TypeEnum.PIX,
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(paymentConsentRequest, clientId, idemPotencyKey)

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
        CreatePaymentConsent paymentConsentRequest = TestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", Identification.PersonTypeEnum.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","AC001", "LUREL1", "LUID1", PaymentConsent.TypeEnum.PIX,
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(paymentConsentRequest, clientId, idemPotencyKey)

        CreatePixPayment paymentRequest = TestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "")

        UpdatePaymentConsent updatePaymentConsent = TestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        ResponsePixPayment paymentResponse = paymentsService.createPayment(paymentRequest,responseConsent.getData().getConsentId(), idemPotencyKey)

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

    }

    def "Unknown cnpj cannot create a payment" () {
        given:
        def clientId = "client3"
        def idemPotencyKey = "idemkey9"
        CreatePaymentConsent paymentConsentRequest = TestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", Identification.PersonTypeEnum.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","AC001", "LUREL1", "LUID1", PaymentConsent.TypeEnum.PIX,
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(paymentConsentRequest, clientId, idemPotencyKey)

        CreatePixPayment paymentRequest = TestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "36386527000143", "")

        when:
        ResponsePixPayment paymentResponse = paymentsService.createPayment(paymentRequest,responseConsent.getData().getConsentId(), idemPotencyKey)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.UNAUTHORIZED

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
        CreatePaymentConsent paymentConsentRequest = TestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", Identification.PersonTypeEnum.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","AC001", "LUREL1", "LUID1", PaymentConsent.TypeEnum.PIX,
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(paymentConsentRequest, clientId, idemPotencyKey)

        CreatePixPayment paymentRequest = TestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj","")

        UpdatePaymentConsent updatePaymentConsent = TestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.CONSUMED)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        paymentsService.createPayment(paymentRequest,responseConsent.getData().getConsentId(), idemPotencyKey)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.BAD_REQUEST
        e.message.contains('but the consent was already consumed')

    }

    def "idempotency works for payments" () {
        given:
        def clientId = "client4"
        def idemPotencyKey = "idemkey7"
        def idemPotencyKey2 = "idemkey8"
        CreatePaymentConsent paymentConsentRequest = TestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", Identification.PersonTypeEnum.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","AC001", "LUREL1", "LUID1", PaymentConsent.TypeEnum.PIX,
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(paymentConsentRequest, clientId, idemPotencyKey)

        CreatePixPayment paymentRequest = TestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "")

        UpdatePaymentConsent updatePaymentConsent = TestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        ResponsePixPayment paymentResponse = paymentsService.createPayment(paymentRequest,responseConsent.getData().getConsentId(), idemPotencyKey)

        then:
        noExceptionThrown()
        paymentResponse != null
        paymentResponse.getData() != null
        paymentResponse.getData().getPaymentId() != null

        when:
        ResponsePixPayment paymentResponse2 = paymentsService.createPayment(paymentRequest,responseConsent.getData().getConsentId(), idemPotencyKey)

        then:
        noExceptionThrown()
        paymentResponse2 != null
        paymentResponse2.getData() != null
        paymentResponse2.getData().getPaymentId() != null
        paymentResponse2.getData().getPaymentId() == paymentResponse.getData().getPaymentId()

        when:
        // reset the consent, just to test this bit...
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)
        ResponsePixPayment paymentResponse3 = paymentsService.createPayment(paymentRequest, responseConsent.getData().getConsentId(), idemPotencyKey2)

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
        CreatePaymentConsent paymentConsentRequest = TestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", Identification.PersonTypeEnum.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","AC001", "LUREL1", "LUID1", PaymentConsent.TypeEnum.PIX,
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(paymentConsentRequest, clientId, idemPotencyKey)

        CreatePixPayment paymentRequest = TestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj","")

        UpdatePaymentConsent updatePaymentConsent = TestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        ResponsePixPayment paymentResponse = paymentsService.createPayment(paymentRequest,responseConsent.getData().getConsentId(), idemPotencyKey)

        UpdatePixPayment updatePixPayment = TestDataFactory.createPaymentUpdateRequest(UpdatePixPaymentData.StatusEnum.ACCC)

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
        CreatePaymentConsent paymentConsentRequest = TestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", Identification.PersonTypeEnum.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","AC001", "LUREL1", "LUID1", PaymentConsent.TypeEnum.PIX,
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(paymentConsentRequest, clientId, idemPotencyKey)

        CreatePixPayment paymentRequest = TestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj","")

        UpdatePaymentConsent updatePaymentConsent = TestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        ResponsePixPayment paymentResponse = paymentsService.createPayment(paymentRequest,responseConsent.getData().getConsentId(), idemPotencyKey)

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
        CreatePaymentConsent paymentConsentRequest = TestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", Identification.PersonTypeEnum.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","AC001", "LUREL1", "LUID1", PaymentConsent.TypeEnum.PIX,
                LocalDate.now(), "BRL", "1")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(paymentConsentRequest, clientId, idemPotencyKey)

        CreatePixPayment paymentRequest = TestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "")

        UpdatePaymentConsent updatePaymentConsent = TestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        paymentsService.createPayment(paymentRequest,responseConsent.getData().getConsentId(), idemPotencyKey)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.BAD_REQUEST
    }

    def "we cannot create a payment if the consent currency doesn't match" () {
        given:
        var clientId = "client8"
        var idemPotencyKey = "idemkey12"
        CreatePaymentConsent paymentConsentRequest = TestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", Identification.PersonTypeEnum.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","AC001", "LUREL1", "LUID1", PaymentConsent.TypeEnum.PIX,
                LocalDate.now(), "GBP", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(paymentConsentRequest, clientId, idemPotencyKey)

        CreatePixPayment paymentRequest = TestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "")

        UpdatePaymentConsent updatePaymentConsent = TestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        paymentsService.createPayment(paymentRequest,responseConsent.getData().getConsentId(), idemPotencyKey)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.BAD_REQUEST
    }

    def "we cannot create a payment if the consent doesn't exist" () {
        given:
        var clientId = "client9"
        var idemPotencyKey = "idemkey13"
        CreatePaymentConsent paymentConsentRequest = TestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", Identification.PersonTypeEnum.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","AC001", "LUREL1", "LUID1", PaymentConsent.TypeEnum.PIX,
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(paymentConsentRequest, clientId, idemPotencyKey)

        CreatePixPayment paymentRequest = TestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "")

        UpdatePaymentConsent updatePaymentConsent = TestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        paymentsService.createPayment(paymentRequest,"Ceci n'est pas un payment consent id", idemPotencyKey)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.BAD_REQUEST
    }

    def "we cannot create a payment if the consent is awaiting authorisation" () {
        given:
        var clientId = "client10"
        var idemPotencyKey = "idemkey14"
        CreatePaymentConsent paymentConsentRequest = TestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", Identification.PersonTypeEnum.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","AC001", "LUREL1", "LUID1", PaymentConsent.TypeEnum.PIX,
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(paymentConsentRequest, clientId, idemPotencyKey)

        CreatePixPayment paymentRequest = TestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "")

        when:
        paymentsService.createPayment(paymentRequest,responseConsent.getData().getConsentId(), idemPotencyKey)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.BAD_REQUEST
    }

    def "we cannot create a payment if the consent is rejected" () {
        given:
        var clientId = "client11"
        var idemPotencyKey = "idemkey15"
        CreatePaymentConsent paymentConsentRequest = TestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", Identification.PersonTypeEnum.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","AC001", "LUREL1", "LUID1", PaymentConsent.TypeEnum.PIX,
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(paymentConsentRequest, clientId, idemPotencyKey)

        CreatePixPayment paymentRequest = TestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj","")

        UpdatePaymentConsent updatePaymentConsent = TestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.REJECTED)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        paymentsService.createPayment(paymentRequest,responseConsent.getData().getConsentId(), idemPotencyKey)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.BAD_REQUEST
    }

    def "we cannot create a payment if the consent is consumed" () {
        given:
        var clientId = "client12"
        var idemPotencyKey = "idemkey16"
        CreatePaymentConsent paymentConsentRequest = TestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", Identification.PersonTypeEnum.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","AC001", "LUREL1", "LUID1", PaymentConsent.TypeEnum.PIX,
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(paymentConsentRequest, clientId, idemPotencyKey)

        CreatePixPayment paymentRequest = TestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "")

        UpdatePaymentConsent updatePaymentConsent = TestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.CONSUMED)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        when:
        paymentsService.createPayment(paymentRequest,responseConsent.getData().getConsentId(), idemPotencyKey)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.BAD_REQUEST
    }

    def "we cannot create a payment if the consent is expired" () {
        given:
        var clientId = "client13"
        var idemPotencyKey = "idemkey17"
        CreatePaymentConsent paymentConsentRequest = TestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", Identification.PersonTypeEnum.NATURAL,EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1","AC001", "LUREL1", "LUID1", PaymentConsent.TypeEnum.PIX,
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsent responseConsent = paymentsService.createConsent(paymentConsentRequest, clientId, idemPotencyKey)

        CreatePixPayment paymentRequest = TestDataFactory.createPaymentRequest("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "")

        UpdatePaymentConsent updatePaymentConsent = TestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED)
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        var paymentConsentEntityOptional = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().getConsentId())
        var paymentConsentEntity = paymentConsentEntityOptional.get()
        paymentConsentEntity.setExpirationDateTime(Date.from(Instant.now() - Duration.ofDays(10)))
        paymentConsentRepository.update(paymentConsentEntity)

        when:
        paymentsService.createPayment(paymentRequest,responseConsent.getData().getConsentId(), idemPotencyKey)

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
                .creditor(new Identification().cpfCnpj("12345678901").name("Bob Creditor").personType(Identification.PersonTypeEnum.NATURAL))
                .debtorAccount(new DebtorAccount().accountType(EnumAccountPaymentsType.SLRY).ispb("12341234").issuer("1234").number("12341234"))
                .loggedUser(new LoggedUser().document(new LoggedUserDocument().rel("CPF").identification("12345678901")))
                .payment(new PaymentConsent().type(PaymentConsent.TypeEnum.PIX).date(LocalDate.now()).currency("BRL").amount("100.00")))

        ResponsePaymentConsent responseConsent = paymentsService.createConsent(paymentConsentRequest, clientId, idemPotencyKey)

        UpdatePaymentConsent updatePaymentConsent = TestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED)
        updatePaymentConsent.data.debtorAccount(new DebtorAccount().accountType(EnumAccountPaymentsType.SLRY).ispb("12341234").issuer("1234").number("12341234"))

        when:
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.BAD_REQUEST
        e.message == 'Debtor account already set in initial consent'
    }

    def "we can update a payment consent with a debtor if the consent does not already have it" () {
        given:
        var clientId = "client13"
        var idemPotencyKey = "idemkey18"
        CreatePaymentConsent paymentConsentRequest = new CreatePaymentConsent().data(new CreatePaymentConsentData()
                .businessEntity(
                        new BusinessEntity().document(
                                new BusinessEntityDocument().identification("12345678901235")
                                        .rel("CNPJ")))
                .creditor(new Identification().cpfCnpj("12345678904").name("Bob Creditor").personType(Identification.PersonTypeEnum.NATURAL))
                .loggedUser(new LoggedUser().document(new LoggedUserDocument().rel("CPF").identification("12345678905")))
                .payment(new PaymentConsent().type(PaymentConsent.TypeEnum.PIX).date(LocalDate.now()).currency("BRL").amount("100.00")))

        ResponsePaymentConsent responseConsent = paymentsService.createConsent(paymentConsentRequest, clientId, idemPotencyKey)

        UpdatePaymentConsent updatePaymentConsent = TestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED)
        updatePaymentConsent.data.debtorAccount(new DebtorAccount().accountType(EnumAccountPaymentsType.SLRY).ispb("123412534").issuer("1234").number("12341234"))

        when:
        paymentsService.updateConsent(responseConsent.getData().getConsentId(),updatePaymentConsent)

        then:
        noExceptionThrown()
    }

    @MockBean(CnpjVerifier.class)
    CnpjVerifier rolesApiService() {
        return new CnpjVerifier() {
            @Override
            boolean isKnownCnpj(String cnpj) {
                return !("36386527000143".equals(cnpj))
            }
        }
    }

}
