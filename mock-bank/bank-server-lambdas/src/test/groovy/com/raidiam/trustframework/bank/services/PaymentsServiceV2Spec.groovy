package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.EndToEndIdHelper
import com.raidiam.trustframework.bank.TestRequestDataFactory
import com.raidiam.trustframework.bank.domain.AccountEntity
import com.raidiam.trustframework.bank.domain.AccountHolderEntity
import com.raidiam.trustframework.bank.domain.PaymentConsentEntity
import com.raidiam.trustframework.bank.enums.ErrorCodesEnumV2
import com.raidiam.trustframework.bank.repository.PaymentsSimulateResponseRepository
import com.raidiam.trustframework.bank.services.message.PaymentErrorMessageV2
import com.raidiam.trustframework.bank.utils.QrCodeUtils
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils
import spock.lang.Shared
import spock.lang.Stepwise
import spock.lang.Unroll

import javax.inject.Inject
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccount
import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccountHolder

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class PaymentsServiceV2Spec extends CleanupSpecification {
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

    def "we can create a payment consent and force a 422 response"() {
        given:
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.data.payment.amount(amount)

        when:
        paymentConsentService.createConsentV2(clientId, idemPotencyKey, jti, paymentConsentRequest)

        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY
        ex.message.endsWith(code)

        where:
        amount     | clientId                     | jti                          | idemPotencyKey               | code
        "10422.00" | UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString() | "Forced a 422 for payment consent request"
        "10422.01" | UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString() | ErrorCodesEnumV2.DETALHE_PAGAMENTO_INVALIDO.name()
        "10422.02" | UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString() | ErrorCodesEnumV2.FORMA_PAGAMENTO_INVALIDA.name()
    }

    def "unknown CNPJs can create a payment consent"() {
        given:
        def clientId = "client1"
        def idemPotencyKey = "idemkey1"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("MADEUP", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")

        when:
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjti2", paymentConsentRequest)

        then:
        noExceptionThrown()
        responseConsent != null
        responseConsent.getData() != null
        responseConsent.getData().getStatus() == ResponsePaymentConsentDataV2.StatusEnum.AWAITING_AUTHORISATION
        responseConsent.getData().getConsentId() != null
    }

    def "payment consent using date in past returns 422"() {
        given:
        var clientId = "client13"
        var idemPotencyKey = "idemkey17"
        def errorMessage = new PaymentErrorMessageV2()
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory
                .createPaymentConsentRequest("BID1", "REL1",
                        "66.001.455/0001-30", "Bob Creditor", EnumCreditorPersonType.NATURAL,
                        EnumAccountPaymentsType.SLRY, "ispb1", "issuer1",
                        "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(),
                        EnumPaymentType.PIX.toString(), LocalDate.parse("2019-11-12"), "BRL", "100")

        when:
        paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjtiwg;ijhwefo8w7ggf", paymentConsentRequest)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == errorMessage.getMessageInvalidDate("Data de pagamento inválida no contexto, data no passado. Para pagamentos únicos deve ser informada a data atual, do dia corrente.")
    }

    def "payment consent v2 fail tests - Invalid enums"() {
        given:
        var clientId = "client16"
        var idemPotencyKey = "idemkey20"
        def errorMessage = new PaymentErrorMessageV2()
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
        paymentConsentService.createConsentV2(clientId, idemPotencyKey, "somejti69", paymentConsentRequest)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == errorMessage.getMessageInvalidParameter()

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
        paymentConsentService.createConsentV2(clientId, idemPotencyKey, "somejti420", paymentConsentRequest)

        then:
        e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == errorMessage.getMessageInvalidParameter()

        when:
        id.setPersonType(EnumCreditorPersonType.JURIDICA.toString())
        paymentConsentRequest = new CreatePaymentConsent().data(new CreatePaymentConsentData()
                .businessEntity(be)
                .creditor(id)
                .debtorAccount(da)
                .loggedUser(lu)
                .payment(pc)
        )
        paymentConsentService.createConsentV1(clientId, idemPotencyKey, "somejti4", paymentConsentRequest)

        then:
        noExceptionThrown()

    }

    def "we can get a payment consent V2"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def idemPotencyKey = UUID.randomUUID().toString()
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjti7", paymentConsentRequest)

        when:
        ResponsePaymentConsentV2 retrievedConsent = paymentConsentService.getConsentV2(responseConsent.getData().getConsentId(), clientId)

        then:
        noExceptionThrown()
        retrievedConsent != null
        retrievedConsent.getData() != null
        retrievedConsent.getData().getConsentId() != null
        retrievedConsent.getData().getConsentId() == responseConsent.getData().getConsentId()
    }

    def "we can create a payment and force a 422"() {
        given:
        def clientId = "client4"
        def idemPotencyKey = "idemkey7"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel(), "20422.00")
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjti93", paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", null)
        paymentRequest.data.payment.amount("20422.00")

        when:
        paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY
        ex.message == "Forced a 422 for payment request"

        and:
        Optional<PaymentConsentEntity> consentEntityOpt = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().consentId)
        consentEntityOpt.isPresent()
        PaymentConsentEntity consentEntity = consentEntityOpt.get()
        consentEntity.status == "CONSUMED"
    }

    def "idempotency works for payment consents"() {
        given:
        def clientId = "client1"
        def idemPotencyKey2 = "idemkey2"
        def idemPotencyKey3 = "idemkey3"

        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        when:
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey2, "randomjti3", paymentConsentRequest)

        then:
        noExceptionThrown()
        responseConsent != null
        responseConsent.getData() != null
        responseConsent.getData().getStatus() == ResponsePaymentConsentDataV2.StatusEnum.AWAITING_AUTHORISATION
        responseConsent.getData().getConsentId() != null

        when:
        ResponsePaymentConsentV2 responseConsent2 = paymentConsentService.createConsentV2(clientId, idemPotencyKey2, "randomjti4", paymentConsentRequest)

        then:
        noExceptionThrown()
        responseConsent2 != null
        responseConsent2.getData() != null
        responseConsent2.getData().getStatus() == ResponsePaymentConsentDataV2.StatusEnum.AWAITING_AUTHORISATION
        responseConsent2.getData().getConsentId() != null
        responseConsent2.getData().getConsentId() == responseConsent.getData().getConsentId()

        when:
        ResponsePaymentConsentV2 responseConsent3 = paymentConsentService.createConsentV2(clientId, idemPotencyKey3, "randomjti5", paymentConsentRequest)

        then:
        noExceptionThrown()
        responseConsent3 != null
        responseConsent3.getData() != null
        responseConsent3.getData().getStatus() == ResponsePaymentConsentDataV2.StatusEnum.AWAITING_AUTHORISATION
        responseConsent3.getData().getConsentId() != null
        responseConsent3.getData().getConsentId() != responseConsent.getData().getConsentId()
    }

    def "we can create payment and allow transaction identifier on a pix payment that has INIC/QRDN/QRES local instrument"() {
        given:
        def transactionIdentification = "E00038166201907261559y6j6"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.getData().getPayment().getDetails().setLocalInstrument(localInstrument)
        def qrCode = QrCodeUtils.createQrCode(paymentConsentRequest).toString()
        paymentConsentRequest.getData().getPayment().getDetails().setQrCode(qrCode)
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, jti, paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC2", "CRISS2", "CRISPB2", EnumAccountPaymentsType.SLRY, localInstrument, "100.00", "BRL", "", qrCode, "cnpj", "", transactionIdentification)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        ResponsePixPaymentV2 paymentResponse = paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

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

        where:
        clientId << [UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString()]
        idemPotencyKey << [UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString()]
        jti << [UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString()]
        localInstrument << [EnumLocalInstrument.INIC, EnumLocalInstrument.QRDN, EnumLocalInstrument.QRES]
    }

    def "we can create a payment v2 and force it to be rejected"() {
        given:
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel(), amount)
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, jti, paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", null)
        paymentRequest.data.payment.amount(amount)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        ResponsePixPaymentV2 paymentResponse = paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        noExceptionThrown()
        paymentResponse != null
        paymentResponse.getData() != null
        paymentResponse.getData().getPaymentId() != null
        paymentResponse.getData().getStatus() == EnumPaymentStatusTypeV2.RCVD

        and:
        Optional<PaymentConsentEntity> consentEntityOpt = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().consentId)
        consentEntityOpt.isPresent()
        PaymentConsentEntity consentEntity = consentEntityOpt.get()
        consentEntity.status == "CONSUMED"

        when:
        paymentResponse = paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)
        then:
        noExceptionThrown()
        paymentResponse.getData().getStatus() == EnumPaymentStatusTypeV2.RCVD

        when:
        ResponsePixPaymentV2 paymentRetrieved = paymentsService.getPaymentV2(paymentResponse.getData().getPaymentId(), clientId)

        then:
        paymentRetrieved != null
        paymentRetrieved.getData().getStatus() == EnumPaymentStatusTypeV2.RJCT
        paymentRetrieved.getData().getRejectionReason().getCode() == code

        where:
        amount         | clientId   | jti          | idemPotencyKey | code
        "20201.00"     | "client03" | "arandomjti" | "gwgrgeberb3"  | RejectionReasonV2.CodeEnum.DETALHE_PAGAMENTO_INVALIDO
        "20201.20"     | "client04" | "brandomjti" | "gwgrgeberb4"  | RejectionReasonV2.CodeEnum.VALOR_INVALIDO
        "20201.30"     | "client05" | "crandomjti" | "gwgrgeberb5"  | RejectionReasonV2.CodeEnum.VALOR_ACIMA_LIMITE
        "999999999.99" | "client06" | "drandomjti" | "gwgrgeberb6"  | RejectionReasonV2.CodeEnum.SALDO_INSUFICIENTE
        "20201.50"     | "client07" | "frandomjti" | "gwgrgeberb7"  | RejectionReasonV2.CodeEnum.COBRANCA_INVALIDA

    }

    def "force a 422 error to disallow transaction identifier on a pix payment that has none INIC local instrument"() {
        given:
        def clientId = "client46"
        def idemPotencyKey = "idemkey23"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjti82", paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC3", "CRISS3", "CRISPB3", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", "E00038166201907261559y6j6")

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY
        ex.message.contains(ErrorCodesEnumV2.DETALHE_PAGAMENTO_INVALIDO.toString())

        and:
        Optional<PaymentConsentEntity> consentEntityOpt = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().consentId)
        consentEntityOpt.isPresent()
        PaymentConsentEntity consentEntity = consentEntityOpt.get()
        consentEntity.status == "CONSUMED"
    }

    def "we can create a payment and force a 422 with a specific error"() {
        given:
        def clientId = "client45"
        def idemPotencyKey = "gwgrgeberb"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel(), "20422.01")
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjti9g3", paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", null)
        paymentRequest.data.payment.amount("20422.01")

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY
        ex.message == ErrorCodesEnumV2.DETALHE_PAGAMENTO_INVALIDO.name()

        and:
        Optional<PaymentConsentEntity> consentEntityOpt = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().consentId)
        consentEntityOpt.isPresent()
        PaymentConsentEntity consentEntity = consentEntityOpt.get()
        consentEntity.status == "CONSUMED"
    }

    def "we can create a payment and force a 422 with a specific error msg"() {
        given:
        def clientId = "client46"
        def idemPotencyKey = "gwgrgeberb1"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel(), "20422.02")
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjti8g8", paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "")
        paymentRequest.data.payment.amount("20422.02")

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY
        ex.message == ErrorCodesEnumV2.VALOR_INVALIDO.toString()

        and:
        Optional<PaymentConsentEntity> consentEntityOpt = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().consentId)
        consentEntityOpt.isPresent()
        PaymentConsentEntity consentEntity = consentEntityOpt.get()
        consentEntity.status == "CONSUMED"
    }

    def "we can create a payment and force a 422 return a specific error"() {
        given:
        def clientId = "client45"
        def idemPotencyKey = "gwgrgeberb2"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel(), "20422.03")
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjti9g4", paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "")
        paymentRequest.data.payment.amount("20422.03")

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY
        ex.message == ErrorCodesEnumV2.PAGAMENTO_DIVERGENTE_CONSENTIMENTO.toString()

        and:
        Optional<PaymentConsentEntity> consentEntityOpt = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().consentId)
        consentEntityOpt.isPresent()
        PaymentConsentEntity consentEntity = consentEntityOpt.get()
        consentEntity.status == "CONSUMED"
    }

    def "we trhow 422 for invalid schedule and date at consent creation"() {
        given:
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel(), "100.00")
        paymentConsentRequest.data.payment.schedule(scheduleDate as Schedule)
        paymentConsentRequest.data.payment.date(date)

        when:
        paymentConsentService.createConsentV2(clientId, idemPotencyKey, jti, paymentConsentRequest)

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

    @Unroll
    def "we can create a payment and force it to be accepted"() {
        given:
        def clientId = "client04"
        def idemPotencyKey = UUID.randomUUID().toString()
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel(), amount)
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, UUID.randomUUID().toString(), paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", null)
        paymentRequest.data.payment.amount(amount)

        def withDebtor = responseConsent.getData().getDebtorAccount() == null

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        ResponsePixPaymentV2 paymentResponse = paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        noExceptionThrown()
        paymentResponse != null
        paymentResponse.getData() != null
        paymentResponse.getData().getPaymentId() != null
        paymentResponse.getData().getStatus() == EnumPaymentStatusTypeV2.RCVD

        and:
        Optional<PaymentConsentEntity> consentEntityOpt = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().consentId)
        consentEntityOpt.isPresent()
        PaymentConsentEntity consentEntity = consentEntityOpt.get()
        consentEntity.status == "CONSUMED"

        when:
        paymentResponse = paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)
        then:
        noExceptionThrown()
        paymentResponse.getData().getStatus() == EnumPaymentStatusTypeV2.RCVD

        when:
        ResponsePixPaymentV2 paymentRetrieved = paymentsService.getPaymentV2(paymentResponse.getData().getPaymentId(), clientId)

        then:
        paymentRetrieved != null
        paymentRetrieved.getData().getStatus() == EnumPaymentStatusTypeV2.ACSC

        where:
        amount << ["1333.00", "1333.08", "1333.50", "1333.99"]

    }

    def "we can create a payment and force it to be ACSC"() {
        given:
        def clientId = "client09"
        def idemPotencyKey = "idemkey64h4wgetrn"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel(), "1336.00")
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjtih8ff8", paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", null)
        paymentRequest.data.payment.amount("1336.00")

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        ResponsePixPaymentV2 paymentResponse = paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        noExceptionThrown()
        paymentResponse != null
        paymentResponse.getData() != null
        paymentResponse.getData().getPaymentId() != null
        paymentResponse.getData().getStatus() == EnumPaymentStatusTypeV2.RCVD

        and:
        Optional<PaymentConsentEntity> consentEntityOpt = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().consentId)
        consentEntityOpt.isPresent()
        PaymentConsentEntity consentEntity = consentEntityOpt.get()
        consentEntity.status == "CONSUMED"

        when:
        paymentResponse = paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)
        then:
        noExceptionThrown()
        paymentResponse.getData().getStatus() == EnumPaymentStatusTypeV2.RCVD

        when:
        ResponsePixPaymentV2 paymentRetrieved = paymentsService.getPaymentV2(paymentResponse.getData().getPaymentId(), clientId)

        then:
        paymentRetrieved != null
        paymentRetrieved.getData().getStatus() == EnumPaymentStatusTypeV2.ACSC

    }

    def "we can create a payment and force it to be ACPD"() {
        given:
        def clientId = "client09"
        def idemPotencyKey = "idemkey64h4wgetrns"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel(), "1334.00")
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjtih8ff89", paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", null)
        paymentRequest.data.payment.amount("1334.00")

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        ResponsePixPaymentV2 paymentResponse = paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        noExceptionThrown()
        paymentResponse != null
        paymentResponse.getData() != null
        paymentResponse.getData().getPaymentId() != null
        paymentResponse.getData().getStatus() == EnumPaymentStatusTypeV2.RCVD

        and:
        Optional<PaymentConsentEntity> consentEntityOpt = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().consentId)
        consentEntityOpt.isPresent()
        PaymentConsentEntity consentEntity = consentEntityOpt.get()
        consentEntity.status == "CONSUMED"

        when:
        paymentResponse = paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)
        then:
        noExceptionThrown()
        paymentResponse.getData().getStatus() == EnumPaymentStatusTypeV2.RCVD

        when:
        ResponsePixPaymentV2 paymentRetrieved = paymentsService.getPaymentV2(paymentResponse.getData().getPaymentId(), clientId)

        then:
        paymentRetrieved != null
        paymentRetrieved.getData().getStatus() == EnumPaymentStatusTypeV2.ACPD

    }

    def "we can create a payment and force it to be ACCP"() {
        given:
        def clientId = "client11"
        def idemPotencyKey = "idemkey64hfevr4"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel(), "1335.00")
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjtih88w4g", paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", null)
        paymentRequest.data.payment.amount("1335.00")

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        ResponsePixPaymentV2 paymentResponse = paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        noExceptionThrown()
        paymentResponse != null
        paymentResponse.getData() != null
        paymentResponse.getData().getPaymentId() != null
        paymentResponse.getData().getStatus() == EnumPaymentStatusTypeV2.RCVD

        and:
        Optional<PaymentConsentEntity> consentEntityOpt = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().consentId)
        consentEntityOpt.isPresent()
        PaymentConsentEntity consentEntity = consentEntityOpt.get()
        consentEntity.status == "CONSUMED"

        when:
        paymentResponse = paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)
        then:
        noExceptionThrown()
        paymentResponse.getData().getStatus() == EnumPaymentStatusTypeV2.RCVD

        when:
        ResponsePixPaymentV2 paymentRetrieved = paymentsService.getPaymentV2(paymentResponse.getData().getPaymentId(), clientId)

        then:
        paymentRetrieved != null
        paymentRetrieved.getData().getStatus() == EnumPaymentStatusTypeV2.ACCP

    }

    def "Unknown cnpj cannot create a payment"() {
        given:
        def clientId = "client3"
        def idemPotencyKey = "idemkey9"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")

        when:
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjti9", paymentConsentRequest)

        then:
        responseConsent != null
        responseConsent.data != null
        responseConsent.data.status == ResponsePaymentConsentDataV2.StatusEnum.AWAITING_AUTHORISATION

        when:
        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "36386527000143", "", null)

        paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.UNPROCESSABLE_ENTITY

        and:
        Optional<PaymentConsentEntity> consentEntityOpt = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().consentId)
        consentEntityOpt.isPresent()
        PaymentConsentEntity consentEntity = consentEntityOpt.get()
        consentEntity.status == "CONSUMED"
    }

    def "we cannot reuse a consent"() {
        given:
        def clientId = "client3"
        def idemPotencyKey = "idemkey711"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjti10", paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.CONSUMED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.UNPROCESSABLE_ENTITY
        e.message.contains('CONSENTIMENTO_INVALIDO')

    }

    def "A null or empty consent is rejected"() {
        given:
        def clientId = "client33"
        def idemPotencyKey = "idemkey311"

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "", null)

        when:
        paymentsService.createPaymentV2(null, idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException e = thrown()
        e.status == HttpStatus.FORBIDDEN
        e.message.contains('CONSENTIMENTO_INVALIDO')

        when:
        paymentsService.createPaymentV2("", idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException f = thrown()
        f.status == HttpStatus.FORBIDDEN
        f.message.contains('CONSENTIMENTO_INVALIDO')
    }

    def "idempotency works for payments"() {
        given:
        def clientId = "client4"
        def idemPotencyKey = "idemkey27"
        def idemPotencyKey2 = "idemkey28"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjti21", paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        ResponsePixPaymentV2 paymentResponse = paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        noExceptionThrown()
        paymentResponse != null
        paymentResponse.getData() != null
        paymentResponse.getData().getPaymentId() != null

        when:
        ResponsePixPaymentV2 paymentResponse2 = paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        noExceptionThrown()
        paymentResponse2 != null
        paymentResponse2.getData() != null
        paymentResponse2.getData().getPaymentId() != null
        paymentResponse2.getData().getPaymentId() == paymentResponse.getData().getPaymentId()

        when:
        // reset the consent, just to test this bit...
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)
        ResponsePixPaymentV2 paymentResponse3 = paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey2, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        noExceptionThrown()
        paymentResponse3 != null
        paymentResponse3.getData() != null
        paymentResponse3.getData().getPaymentId() != null
        paymentResponse3.getData().getPaymentId() != paymentResponse.getData().getPaymentId()
    }

    def "we can get a payment"() {
        given:
        def clientId = "client6"
        def idemPotencyKey = "idemkey10"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjtiwgpeiruhgowa4igyuh0w4iogrs", paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        ResponsePixPaymentV2 paymentResponse = paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        when:
        ResponsePixPaymentV2 foundPayment = paymentsService.getPaymentV2(paymentResponse.getData().getPaymentId(), clientId)

        then:
        noExceptionThrown()
        foundPayment != null
        foundPayment.getData() != null
        foundPayment.getData().getPaymentId() != null
        foundPayment.getData().getPaymentId() == paymentResponse.getData().getPaymentId()
    }

    def "we cannot create a payment if the consent amount doesn't match"() {
        given:
        def clientId = "client7"
        def idemPotencyKey = "idemkey11"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "1")
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjti24", paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
    }

    def "we can not re-use a JTI for a payments"() {
        given:
        def clientId = "client6"
        def idemPotencyKey = "idemkey40"
        def idemPotencyKey2 = "idemkey41"

        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjtiwgpeiruhgowa4igyuh0w4iogrs4", paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        String jti = UUID.randomUUID().toString()
        paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, jti, paymentRequest, clientId)

        //Create second payment
        CreatePaymentConsent paymentConsentRequest2 = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsentV2 responseConsent2 = paymentConsentService.createConsentV2(clientId, idemPotencyKey2, "randomjtiwgpeiruhgowa4igyuh0w4iogrs5", paymentConsentRequest2)

        CreatePixPaymentV2 paymentRequest2 = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "")

        when:
        paymentsService.createPaymentV2(responseConsent2.getData().getConsentId(), idemPotencyKey, jti, paymentRequest2, clientId)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.FORBIDDEN
    }

    def "we cannot create a payment if the consent currency doesn't match"() {
        given:
        def clientId = "client8"
        def idemPotencyKey = "idemkey12"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "GBP", "100")
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjti256", paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "")

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
    }

    def "an invalid endToEndId in a scheduled payment returns a 422"() {
        given:
        def clientId = "client99"
        def idemPotencyKey = "idemkey123"
        // create consent w/ schedule set on creation
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjti2567687674", paymentConsentRequest)

        responseConsent != null
        responseConsent.data != null
        responseConsent.data.status == ResponsePaymentConsentDataV2.StatusEnum.AWAITING_AUTHORISATION

        when:
        // payment with endtoendid using 1200 in HHmm
        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "", null, ("E90400888202101281200" + RandomStringUtils.randomAlphanumeric(11)))
        paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        // should return 422
        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY

        and:
        Optional<PaymentConsentEntity> consentEntityOpt = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().consentId)
        consentEntityOpt.isPresent()
        PaymentConsentEntity consentEntity = consentEntityOpt.get()
        consentEntity.status == "CONSUMED"
    }

    def "We can force a cancellation with a specific amount on a scheduled payment"() {
        given:
        def clientId = "client999"
        def idemPotencyKey = "idemkey1234"
        // create consent w/ schedule set on creation
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel(), "1400.00")
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjti2567687677", paymentConsentRequest)
        // payment with endtoendid using 1200 in HHmm
        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "1400.00", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        def payment = paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)
        // should return RCVD
        then:
        noExceptionThrown()
        payment.getData().getStatus() == EnumPaymentStatusTypeV2.RCVD

        when:
        def retrievedPayment = paymentsService.getPaymentV2(payment.getData().getPaymentId(), clientId)
        // should return SCHD
        then:
        noExceptionThrown()
        retrievedPayment.getData().getStatus() == EnumPaymentStatusTypeV2.SCHD

        when:
        sleep(180000)
        retrievedPayment = paymentsService.getPaymentV2(payment.getData().getPaymentId(), clientId)
        // should return CANC after scheduled date validation with 1400.00
        then:
        noExceptionThrown()
        retrievedPayment.getData().getStatus() == EnumPaymentStatusTypeV2.CANC
        retrievedPayment.getData().getCancellation().getReason() == Cancellation.ReasonEnum.AGENDAMENTO
        retrievedPayment.getData().getCancellation().getCancelledFrom() == Cancellation.CancelledFromEnum.DETENTORA
    }

    def "Creating a payment with 12345.67 as the amount behaves as expected"() {
        given:
        def clientId = "client1999"
        def idemPotencyKey = "idemkey01234"
        // create consent
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "12345.67")
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjti2567687687", paymentConsentRequest)
        // payment with endtoendid using 1200 in HHmm
        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "12345.67", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        def payment = paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)
        // should return RCVD
        then:
        noExceptionThrown()
        payment.getData().getStatus() == EnumPaymentStatusTypeV2.RCVD

        when:
        def retrievedPayment = paymentsService.getPaymentV2(payment.getData().getPaymentId(), clientId)
        // should return PDNG
        then:
        noExceptionThrown()
        retrievedPayment.getData().getStatus() == EnumPaymentStatusTypeV2.PDNG

        when:
        retrievedPayment = paymentsService.getPaymentV2(payment.getData().getPaymentId(), clientId)
        // should return ACSC
        then:
        noExceptionThrown()
        retrievedPayment != null
        retrievedPayment.getData() != null
        retrievedPayment.getData().getPaymentId() != null
        retrievedPayment.getData().getPaymentId() == payment.getData().getPaymentId()
        retrievedPayment.getData().getStatus() == EnumPaymentStatusTypeV2.RJCT
    }

    def "Calling PATCH endpoint on 12345.67 cancels a payment"() {
        given:
        def clientId = "client1234567"
        def idemPotencyKey = "idemkey1234567"
        // create consent
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "12345.67")
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjti1234567", paymentConsentRequest)
        // payment with endtoendid using 1200 in HHmm
        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "12345.67", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        def payment = paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)
        // should return RCVD
        then:
        noExceptionThrown()
        payment.getData().getStatus() == EnumPaymentStatusTypeV2.RCVD

        when:
        def retrievedPayment = paymentsService.getPaymentV2(payment.getData().getPaymentId(), clientId)
        // should return PDNG
        then:
        noExceptionThrown()
        retrievedPayment.getData().getStatus() == EnumPaymentStatusTypeV2.PDNG

        when:
        def paymentPatchRequest = TestRequestDataFactory.testPatchPayment()
        retrievedPayment = paymentsService.patchPaymentV2(payment.getData().getPaymentId(), paymentPatchRequest)
        // should be cancelled with the correct messages set
        then:
        noExceptionThrown()
        retrievedPayment != null
        retrievedPayment.getData() != null
        retrievedPayment.getData().getPaymentId() != null
        retrievedPayment.getData().getPaymentId() == payment.getData().getPaymentId()
        retrievedPayment.getData().getStatus() == EnumPaymentStatusTypeV2.CANC
        retrievedPayment.getData().getCancellation().getReason() == Cancellation.ReasonEnum.PENDENCIA
        retrievedPayment.getData().getCancellation().getCancelledFrom() == Cancellation.CancelledFromEnum.INICIADORA
    }

    def "we cannot create a payment if the consent doesn't exist"() {
        given:
        def clientId = "client9"
        def idemPotencyKey = "idemkey13"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjti24g", paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        paymentsService.createPaymentV2("Ceci n'est pas un payment consent id", idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.BAD_REQUEST
    }

    def "we cannot create a payment if the consent is awaiting authorisation"() {
        given:
        var clientId = "client10"
        var idemPotencyKey = "idemkey14"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjti2wrgpi9", paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "", null)

        when:
        paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.BAD_REQUEST
    }

    def "we cannot create a payment if the consent is rejected"() {
        given:
        var clientId = "client11"
        var idemPotencyKey = "idemkey15"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjti2wgpiwehgwe", paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.REJECTED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.BAD_REQUEST
    }

    def "we cannot create a payment if the consent is consumed"() {
        given:
        var clientId = "client12"
        var idemPotencyKey = "idemkey16"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjtigwpeguw97fy9iuf3", paymentConsentRequest)
        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "")

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.CONSUMED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage().contains("CONSENTIMENTO_INVALIDO")
    }

    def "we cannot create a payment if the consent is expired"() {
        given:
        var clientId = "client13"
        var idemPotencyKey = "idemkey17"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjtiwg;ijhwefo8w7ggf", paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        var paymentConsentEntityOptional = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().getConsentId())
        var paymentConsentEntity = paymentConsentEntityOptional.get()
        paymentConsentEntity.setExpirationDateTime(Date.from(Instant.now() - Duration.ofDays(10)))
        paymentConsentRepository.update(paymentConsentEntity)

        when:
        paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.BAD_REQUEST
    }

    def "we cannot update a payment consent with a debtor if the consent already has it"() {
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

        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjtiwrgerhg`", paymentConsentRequest)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        updatePaymentConsent.data.debtorAccount(new DebtorAccount().accountType(EnumAccountPaymentsType.SLRY).ispb("12341234").issuer("1234").number("1234567890"))

        when:
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.BAD_REQUEST
        e.message == 'Debtor account already set in initial consent'
    }

    def "we can update a payment consent with a debtor if the consent does not already have it"() {
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

        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjti24wgherbs", paymentConsentRequest)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        updatePaymentConsent.data.debtorAccount(new DebtorAccount().accountType(EnumAccountPaymentsType.SLRY).ispb("123412534").issuer("1234").number("1234567890"))

        when:
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        then:
        noExceptionThrown()
    }

    def "idempotency requires functionally identical payloads"() {
        given:
        def clientId = "client4"
        def idemPotencyKey = "idemkey49"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjti248", paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        ResponsePixPaymentV2 paymentResponse = paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        noExceptionThrown()
        paymentResponse != null
        paymentResponse.getData() != null
        paymentResponse.getData().getPaymentId() != null

        when:
        paymentRequest.getData().getPayment().setAmount("99999")
        paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY

    }

    def "Payment tests"() {
        given:
        def clientId = "client30"
        def idemPotencyKey = "idemkey30"
        def errorMessage = new PaymentErrorMessageV2()
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.getData().getPayment().getDetails().setProxy(null)
        paymentConsentRequest.getData().getPayment().getDetails().setLocalInstrument(EnumLocalInstrument.MANU)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.testPixPaymentV2()
        paymentRequest.getData().setProxy(null)
        paymentRequest.getData().setQrCode(null)

        def jti = "randomjti30"


        when:
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, jti, paymentConsentRequest)
        String consentId = responseConsent.getData().getConsentId()
        UpdatePaymentConsent update = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(consentId, clientId, update)
        ResponsePixPaymentV2 paymentResponse = paymentsService.createPaymentV2(consentId, idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        noExceptionThrown()

        when:
        paymentsService.createPaymentV2(consentId, idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        noExceptionThrown()

        when:
        paymentsService.getPayment(paymentResponse.getData().getPaymentId(), clientId)

        then:
        noExceptionThrown()

        when:
        def updatePixPayment = new UpdatePixPaymentV2().data(new UpdatePixPaymentDataV2().status(EnumPaymentStatusTypeV2.CANC))
        paymentsService.updatePaymentV2(paymentResponse.getData().getPaymentId(), updatePixPayment, clientId)

        then:
        noExceptionThrown()

        when:
        paymentRequest = TestRequestDataFactory.testPixPaymentV2()
        paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())

        clientId = "client32"
        idemPotencyKey = "idemkey32"
        jti = "randomjti32"

        responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, jti, paymentConsentRequest)
        consentId = responseConsent.getData().getConsentId()
        paymentConsentService.updateConsent(consentId, clientId, update)
        paymentsService.createPaymentV2(consentId, idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        paymentRequest.getData().setProxy(null)
        paymentsService.createPaymentV2(consentId, idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == errorMessage.getMessageVersionDiff()


        when:
        paymentRequest = TestRequestDataFactory.testPixPaymentV2()
        paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.getData().getPayment().getDetails().setLocalInstrument(EnumLocalInstrument.QRDN)
        paymentConsentRequest.getData().getPayment().getDetails().setProxy("proxy")
        paymentConsentRequest.getData().getPayment().getDetails().setQrCode(QrCodeUtils.createQrCode(paymentConsentRequest).toString())


        clientId = "client33"
        idemPotencyKey = "idemkey33"
        jti = "randomjti33"

        responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, jti, paymentConsentRequest)
        consentId = responseConsent.getData().getConsentId()
        paymentConsentService.updateConsent(consentId, clientId, update)
        paymentsService.createPaymentV2(consentId, idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        paymentRequest.getData().setQrCode(null)
        paymentsService.createPaymentV2(consentId, idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == errorMessage.getMessageVersionDiff()

        when:
        paymentsService.updatePayment("BAD PAYMENT ID!", TestRequestDataFactory.createPaymentUpdateRequest(EnumPaymentStatusType.RJCT), clientId)

        then:
        e = thrown()
        e.getStatus() == HttpStatus.NOT_FOUND
        e.getMessage() == "Requested pix payment not found"
    }

    def "we can patch a payment"() {
        given:
        def clientId = "client3"
        def idemPotencyKey = "idemkey62"

        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjti84240", paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        ResponsePaymentConsent updatedConsent = paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)
        System.println(updatedConsent)

        def payment = paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), "someidempotencykey", "somejti320", paymentRequest, clientId)
        paymentsService.getPaymentV2(payment.getData().getPaymentId(), clientId)

        when:
        def paymentPatchRequest = TestRequestDataFactory.testPatchPayment()
        ResponsePixPaymentV2 response = paymentsService.patchPaymentV2(payment.getData().getPaymentId(), paymentPatchRequest)

        then:
        noExceptionThrown()
        System.out.println(response)
    }

    def "we can't patch a payment with RCVD status"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def idemPotencyKey = UUID.randomUUID().toString()
        def jti = UUID.randomUUID().toString()

        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, jti, paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        ResponsePaymentConsent updatedConsent = paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)
        System.println(updatedConsent)

        def payment = paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentRequest, clientId)

        when:
        def paymentPatchRequest = TestRequestDataFactory.testPatchPayment()
        ResponsePixPaymentV2 response = paymentsService.patchPaymentV2(payment.getData().getPaymentId(), paymentPatchRequest)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == ErrorCodesEnumV2.PAGAMENTO_NAO_PERMITE_CANCELAMENTO.name()
    }

    @Unroll
    def "422 PARAMETRO_INVALIDO is thrown if we try to patch a payment with invalid rel"() {
        given:
        def clientId = "client3"
        def idemPotencyKey = "idemkey63"

        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjti85242", paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", null)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        ResponsePaymentConsent updatedConsent = paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)
        System.println(updatedConsent)

        def payment = paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), "someidempotencykey1", "somejti321", paymentRequest, clientId)

        when:
        def paymentPatchRequest = TestRequestDataFactory.testPatchPayment()
        paymentPatchRequest.data.cancellation.cancelledBy.document.rel("XXX")
        ResponsePixPaymentV2 response = paymentsService.patchPaymentV2(payment.getData().getPaymentId(), paymentPatchRequest)
        then:
        def e = thrown(HttpStatusException)
        def message = new PaymentErrorMessageV2().getMessageInvalidParameter()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == message

        and:
        Optional<PaymentConsentEntity> consentEntityOpt = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().consentId)
        consentEntityOpt.isPresent()
        PaymentConsentEntity consentEntity = consentEntityOpt.get()
        consentEntity.status == "CONSUMED"
    }

    def "test forced 429 end to end"() {
        given:
        def clientId = "client1"
        def idemPotencyKey = "idemkey1"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel(), "10429.00")
        CreatePaymentConsent paymentConsentRequest2 = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def jti = "randomjti1"
        CreatePixPaymentV2 pixPaymentRequest = TestRequestDataFactory.testPixPaymentV2()
        pixPaymentRequest.getData().setPayment(new PaymentPix().currency("BRL").amount("10429.00"))
        pixPaymentRequest.getData().setProxy(null)
        pixPaymentRequest.getData().setQrCode(null)

        when:
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, jti, paymentConsentRequest)
        String consentId = responseConsent.getData().getConsentId()
        UpdatePaymentConsent update = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(consentId, clientId, update)
        ResponsePixPaymentV2 paymentResponse = paymentsService.createPaymentV2(consentId, idemPotencyKey, UUID.randomUUID().toString(), pixPaymentRequest, clientId)

        then:
        def error = thrown(HttpStatusException.class)
        error.message == "Too many requests"
        error.status == HttpStatus.TOO_MANY_REQUESTS

        when:
        paymentConsentService.createConsentV2(clientId, "somethingidempotent", "jtiijtjtitij", paymentConsentRequest)

        then:
        def error2 = thrown(HttpStatusException.class)
        error2.message == "Too many requests"
        error2.status == HttpStatus.TOO_MANY_REQUESTS

        when:
        paymentConsentRequest.getData().getPayment().setAmount("")
        System.out.println(paymentConsentRequest)
        paymentConsentService.createConsentV2(clientId, "idemPotencyKey", "jti", paymentConsentRequest)

        then:
        def error3 = thrown(HttpStatusException.class)
        error3.message == "Too many requests"
        error3.status == HttpStatus.TOO_MANY_REQUESTS

        when:
        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.testPixPaymentV2()
        paymentsService.createPaymentV2(
                "someconsentID",
                idemPotencyKey + 123456789,
                jti + "haha",
                paymentRequest,
                clientId
        )

        then:
        thrown(HttpStatusException.class)

        when:

        paymentsService.getPayment("someid", clientId)

        then:
        thrown(HttpStatusException.class)

        when:
        paymentsService.updatePayment("someid", TestRequestDataFactory.createPaymentUpdateRequest(EnumPaymentStatusType.ACCC), clientId)

        then:
        thrown(HttpStatusException.class)

        when:

        paymentConsentService.getConsent("someid", clientId)

        then:
        thrown(HttpStatusException.class)

        when:
        def entity = paymentsSimulateResponseRepository.findByUserClientIdAndRequestEndTimeAfter(clientId, LocalDateTime.now())
        def time = entity.get(0).getRequestEndTime().minusMinutes(60)
        entity.get(0).setRequestEndTime(time)
        paymentsSimulateResponseRepository.update(entity.get(0))
        paymentConsentService.createConsentV2(clientId, "idemPotencyKeykeykeykey", "jtijitjitjit", paymentConsentRequest)
        then:
        noExceptionThrown()

        when:
        def response = paymentConsentService.createConsentV2("clientId", "idemPotencyKeyxzczx", "jtizxczxc", paymentConsentRequest2)

        then:
        noExceptionThrown()
        response != null

        when:
        paymentConsentService.createConsentV2("clientId", "idemPotencyKasdasdasdey", "jtasdasdasdasdi", paymentConsentRequest2)

        then:
        noExceptionThrown()
    }

    @Unroll
    def "422 DETALHE_PGTO_INVALIDO is thrown if we cannot parse qrCode"() {
        given:
        def clientId = "client123123"
        def idemPotencyKey = "idemkey151561561"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.getData().getPayment().getDetails().setQrCode("test")
        paymentConsentRequest.getData().getPayment().getDetails().setLocalInstrument(localInstrument)

        when:
        paymentConsentService.createConsentV2(clientId, idemPotencyKey, jti, paymentConsentRequest)
        then:
        def e = thrown(HttpStatusException)
        def message = "PARAMETRO_NAO_INFORMADO: Parâmetro não informado."
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == message

        where:
        localInstrument << [EnumLocalInstrument.QRES, EnumLocalInstrument.QRDN]
        jti << ["randomnjti12132", "randomjti545456"]
    }

    @Unroll
    def "422 PAGAMENTO_DIVERGENTE_CONSENTIMENTO is thrown when payments and consents payments qrCodes are different with QRES/QRDN"() {
        given:
        def clientId = "client1111"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.getData().getPayment().getDetails().setLocalInstrument(localInstrument)

        def qrCode = QrCodeUtils.createQrCode(paymentConsentRequest)
        def paymentQrCode = QrCodeUtils.createQrCode(paymentConsentRequest)
        paymentQrCode.setTransactionAmount("1.00")

        paymentConsentRequest.getData().getPayment().getDetails().setQrCode(qrCode.toString())

        def ResponsePaymentConsentV2 = paymentConsentService.createConsentV2(clientId, idemPotencyKey, jti, paymentConsentRequest)
        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(ResponsePaymentConsentV2.getData().getConsentId(), clientId, updatePaymentConsent)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, localInstrument,
                ResponsePaymentConsentV2.getData().getPayment().getAmount(), "BRL", "", paymentQrCode.toString(), "cnpj", "", null)

        when:
        paymentsService.createPaymentV2(ResponsePaymentConsentV2.getData().getConsentId(), idemPotencyKey, jti2, paymentRequest, clientId)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        def message = String.format("PAGAMENTO_DIVERGENTE_CONSENTIMENTO: Requested pix payment qrCode - %s differs from the qrCode in the associated Consent - %s",
                paymentQrCode, qrCode)
        e.getMessage() == message

        and:
        Optional<PaymentConsentEntity> consentEntityOpt = paymentConsentRepository.findByPaymentConsentId(ResponsePaymentConsentV2.getData().consentId)
        consentEntityOpt.isPresent()
        PaymentConsentEntity consentEntity = consentEntityOpt.get()
        consentEntity.status == "CONSUMED"

        where:
        localInstrument          | jti             | jti2           | idemPotencyKey
        EnumLocalInstrument.QRES | "randomnjti122" | "randomnjti54" | "idemkey105615056"
        EnumLocalInstrument.QRDN | "randomjti54556" | "randomjti5456" | "idemkey105610"
    }

    @Unroll
    def "422 PAGAMENTO_DIVERGENTE_CONSENTIMENTO is thrown when payments amount and consents amount are different"() {
        given:
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())

        def ResponsePaymentConsentV2 = paymentConsentService.createConsentV2(clientId, idemPotencyKey, jti, paymentConsentRequest)
        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(ResponsePaymentConsentV2.getData().getConsentId(), clientId, updatePaymentConsent)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT,
                "1.00", "BRL", "", null, "cnpj", "", null)

        when:
        paymentsService.createPaymentV2(ResponsePaymentConsentV2.getData().getConsentId(), idemPotencyKey, jti2, paymentRequest, clientId)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        def message = String.format("PAGAMENTO_DIVERGENTE_CONSENTIMENTO: O valor informado no consentimento não é o mesmo valor do informado no payload de pagamento. amount consentimento - %s, amount pagamento- %s",
                ResponsePaymentConsentV2.getData().getPayment().getAmount(), "1.00")
        e.getMessage() == message

        and:
        Optional<PaymentConsentEntity> consentEntityOpt = paymentConsentRepository.findByPaymentConsentId(ResponsePaymentConsentV2.getData().consentId)
        consentEntityOpt.isPresent()
        PaymentConsentEntity consentEntity = consentEntityOpt.get()
        consentEntity.status == "CONSUMED"

        where:
        clientId                     | jti                          | jti2                         | idemPotencyKey
        UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString()
    }


    def "end to end id is saved"() {
        given:
        def clientId = "client3030303"
        def idemPotencyKey = "idemkey1596411896"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjti8841561", paymentConsentRequest)
        def endToEndId = EndToEndIdHelper.generateRandomEndToEndId()
        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", null, endToEndId)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        ResponsePixPaymentV2 paymentResponse = paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)
        def paymentId = paymentResponse.getData().getPaymentId()

        then:
        noExceptionThrown()
        paymentResponse != null
        paymentResponse.getData() != null
        paymentResponse.getData().getEndToEndId() == endToEndId

        and:
        def payment = paymentsService.getPayment(paymentId, clientId)
        payment != null
        payment.getData() != null
        payment.getData().getEndToEndId() == endToEndId
    }

    def "can get payment with SCHD status"() {
        given:
        def clientId = "client3030303"
        def idemPotencyKey = "idemkey1596411896"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())

        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, idemPotencyKey, "randomjti8841562", paymentConsentRequest)
        def endToEndId = EndToEndIdHelper.generateRandomEndToEndId()
        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.DICT, "100.00", "BRL", "", "", "cnpj", "", null, endToEndId)

        when:
        ResponsePixPaymentV2 paymentResponse = paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), idemPotencyKey, UUID.randomUUID().toString(), paymentRequest, clientId)
        def paymentId = paymentResponse.getData().getPaymentId()

        then:
        noExceptionThrown()
        paymentResponse != null
        paymentResponse.getData() != null

        and:
        def payment = paymentsService.getPaymentV2(paymentId, clientId)
        payment != null
        payment.getData() != null
        payment.getData().getStatus() == EnumPaymentStatusTypeV2.SCHD
    }


    def "VALOR_INVALIDO is thrown if Amount defined in QrCode differs from the amount specified in the Consent"() {
        given:
        def clientId = "client11111111"
        def jti = "randomnjti1223232"
        def jti2 = "randomnjti543232"
        def idemPotencyKey = "idemkey1056150563232"
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.getData().getPayment().getDetails().setLocalInstrument(EnumLocalInstrument.QRES)

        def qrCode = QrCodeUtils.createQrCode(paymentConsentRequest)
        qrCode.setTransactionAmount("1.00")

        paymentConsentRequest.getData().getPayment().getDetails().setQrCode(qrCode.toString())

        def responsePaymentConsentV2 = paymentConsentService.createConsentV2(clientId, idemPotencyKey, jti, paymentConsentRequest)
        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responsePaymentConsentV2.getData().getConsentId(), clientId, updatePaymentConsent)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.SLRY, EnumLocalInstrument.QRES,
                responsePaymentConsentV2.getData().getPayment().getAmount(), "BRL", "", qrCode.toString(), "cnpj", "", null)

        when:
        paymentsService.createPaymentV2(responsePaymentConsentV2.getData().getConsentId(), idemPotencyKey, jti2, paymentRequest, clientId)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        def message = String.format("%s: Amount defined in QrCode - %s differs from the amount specified in the Consent - %s",
                ErrorCodesEnumV2.VALOR_INVALIDO.name(), qrCode.getTransactionAmount().getValue(), paymentConsentRequest.getData().getPayment().getAmount())
        e.getMessage() == message
    }


    def "we cannot create a payment if end to end ID is missing or incorrect"() {
        given:
        def clientId = UUID.randomUUID().toString()
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "GBP", "100")

        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV2(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        CreatePixPaymentV2 paymentRequest = TestRequestDataFactory.createPaymentRequestV2("CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "", "", "cnpj", "", null, endToEndId)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        paymentsService.createPaymentV2(responseConsent.getData().getConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentRequest, clientId)

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
