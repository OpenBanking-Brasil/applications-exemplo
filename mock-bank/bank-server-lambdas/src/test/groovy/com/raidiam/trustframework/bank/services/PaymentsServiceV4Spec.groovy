package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.EndToEndIdHelper
import com.raidiam.trustframework.bank.TestRequestDataFactory
import com.raidiam.trustframework.bank.domain.AccountEntity
import com.raidiam.trustframework.bank.domain.AccountHolderEntity
import com.raidiam.trustframework.bank.domain.WebhookEntity
import com.raidiam.trustframework.bank.enums.ErrorCodesEnumV2
import com.raidiam.trustframework.bank.services.message.PaymentErrorMessageV2
import com.raidiam.trustframework.bank.utils.BankLambdaUtils
import com.raidiam.trustframework.bank.utils.QrCodeUtils
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import lombok.Getter
import org.mockserver.integration.ClientAndServer
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils
import spock.lang.Shared
import spock.lang.Stepwise
import spock.lang.Unroll

import javax.inject.Inject
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccount
import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccountHolder
import static org.mockserver.integration.ClientAndServer.startClientAndServer
import static org.mockserver.model.HttpRequest.request

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class PaymentsServiceV4Spec extends CleanupSpecification {

    @Inject
    PaymentsService paymentsService
    @Inject
    PaymentConsentService paymentConsentService

    @Getter
    private ClientAndServer mockserver;

    @Shared
    AccountHolderEntity accountHolder
    @Shared
    AccountEntity account

    def setup() {
        if (runSetup) {
            accountHolder = accountHolderRepository.save(anAccountHolder("12345678901", "CPF"))
            account = accountRepository.save(anAccount(accountHolder))
            runSetup = false
        }
    }

    def "we can create a payment consent"() {

        CreatePaymentConsentV4 paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())

        when:
        var createdConsent = paymentConsentService.createConsentV4(
                UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        var responseConsent = paymentConsentRepository.findByPaymentConsentId(createdConsent.data.getConsentId())
                .orElse(null)

        then:
        noExceptionThrown()
        responseConsent != null
        def creditorEntities = responseConsent.getCreditorEntities()
        creditorEntities.size() == 1
        def creditorEntity = creditorEntities.get(0)
        creditorEntity.getCpfCnpj() == paymentConsentRequest.getData().getCreditor().getCpfCnpj()
        creditorEntity.getName() == paymentConsentRequest.getData().getCreditor().getName()
        creditorEntity.getPersonType().toString() == paymentConsentRequest.getData().getCreditor().getPersonType()
        responseConsent.getStatus() == ResponsePaymentConsentData.StatusEnum.AWAITING_AUTHORISATION.toString()
        responseConsent.getPaymentConsentId() != null
        var formattedDate = new SimpleDateFormat("yyyy-MM-dd")
        var paymentDate = responseConsent.getPaymentConsentPaymentEntity().getPaymentDate()
        var date = formattedDate.format(paymentDate)
        date.toString() == LocalDate.now(BankLambdaUtils.getBrasilZoneId()).toString()


        and:
        var details = responseConsent.getPaymentConsentPaymentEntity().paymentConsentDetails
        details.proxy == 'proxy'
        details.localInstrument == EnumLocalInstrument.INIC.toString()
        details.creditorIssuer == 'mockbank'
        details.creditorAccountType == EnumAccountPaymentsType.CACC.toString()
        details.creditorIspb == 'ispb'
        details.creditorAccountNumber == '1234567890'
    }

    def "we cannt create a payment consent when exceeding the maximum quantity"() {

        CreatePaymentConsentV4 paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())

        when: "daily quantity exceed 60"
        paymentConsentRequest.data.payment.date(null)
        paymentConsentRequest.data.payment.schedule(TestRequestDataFactory.scheduleDailyV4(70))

        def createdConsent = paymentConsentService.createConsentV4(
                UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        then:
        def e = thrown(HttpStatusException)
        createdConsent == null
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == String.format("%s: %s", ErrorCodesEnumV2.PARAMETRO_INVALIDO.name(), "Quantidade acima do limite máximo")

        when: "weekly quantity exceed 60"
        paymentConsentRequest.data.payment.date(null)
        paymentConsentRequest.data.payment.schedule(TestRequestDataFactory.scheduleWeeklyV4(70))

        createdConsent = paymentConsentService.createConsentV4(
                UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        then:
        def e2 = thrown(HttpStatusException)
        createdConsent == null
        e2.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e2.getMessage() == String.format("%s: %s", ErrorCodesEnumV2.PARAMETRO_INVALIDO.name(), "Quantidade acima do limite máximo")

        when: "custom dates quantity exceed 60"
        paymentConsentRequest.data.payment.date(null)
        def listOfDate = new ArrayList<LocalDate>()
        for (i in 0..<70) {
            listOfDate.add(LocalDate.now(BankLambdaUtils.getBrasilZoneId()).plusDays(i))
        }
        paymentConsentRequest.data.payment.schedule(new AllOfPaymentConsentV4PaymentSchedule()
                .custom(new ScheduleCustomCustom()
                        .dates(listOfDate)
                        .additionalInformation("This is not a cool custom list for cool dudes")
                ))

        createdConsent = paymentConsentService.createConsentV4(
                UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        then:
        def e3 = thrown(HttpStatusException)
        createdConsent == null
        e3.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e3.getMessage() == String.format("%s: %s", ErrorCodesEnumV2.PARAMETRO_INVALIDO.name(), "Quantidade acima do limite máximo")

    }

    def "we can create a scheduled payment consent"() {

        CreatePaymentConsentV4 paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.data.payment.setDate(date)
        def schedule = paymentSchedule as AllOfPaymentConsentV4PaymentSchedule
        paymentConsentRequest.data.payment.setSchedule(schedule)

        when:
        var createdConsent = paymentConsentService.createConsentV4(
                UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        then:
        noExceptionThrown()
        createdConsent != null
        createdConsent.getData() != null
        createdConsent.getData().getPayment() != null
        var responseSchedule = createdConsent.getData().getPayment().getSchedule()
        responseSchedule != null

        responseSchedule.getSingle() == schedule.getSingle()
        responseSchedule.getDaily() == schedule.getDaily()
        responseSchedule.getWeekly() == schedule.getWeekly()
        responseSchedule.getMonthly() == schedule.getMonthly()
        responseSchedule.getCustom() == schedule.getCustom()

        where:
        paymentSchedule                            | date
        TestRequestDataFactory.scheduleSingleV4()  | null
        TestRequestDataFactory.scheduleDailyV4()   | null
        TestRequestDataFactory.scheduleWeeklyV4()  | null
        TestRequestDataFactory.scheduleMonthlyV4() | null
        TestRequestDataFactory.scheduleCustomV4()  | null
    }

    def "we can't create a scheduled payment consent with the same custom dates"() {

        CreatePaymentConsentV4 paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.data.payment.setDate(null)
        paymentConsentRequest.data.payment.setSchedule(TestRequestDataFactory.scheduleCustomV4(0))

        when:
        var createdConsent = paymentConsentService.createConsentV4(
                UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        then:
        def e = thrown(HttpStatusException)
        createdConsent == null
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == String.format("%s: %s", ErrorCodesEnumV2.PARAMETRO_INVALIDO.name(), "All custom dates must be unique")
    }


    @Unroll
    def "we cant create a scheduled payment consent with scheduled duration of more than 2 years"() {

        CreatePaymentConsentV4 paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.data.payment.setDate(null)
        def schedule = paymentSchedule as AllOfPaymentConsentV4PaymentSchedule
        paymentConsentRequest.data.payment.setSchedule(schedule)

        when:
        var createdConsent = paymentConsentService.createConsentV4(
                UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        then:
        def e = thrown(HttpStatusException)
        createdConsent == null
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == String.format("%s: %s", ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name(), "Data de consentimento tem que ser no máximo até 2 anos no futuro")


        where:
        paymentSchedule << [
                TestRequestDataFactory.scheduleDailyV4(60, 800),
                TestRequestDataFactory.scheduleWeeklyV4(60, 600, ScheduleWeeklyWeekly.DayOfWeekEnum.SEGUNDA_FEIRA),
                TestRequestDataFactory.scheduleMonthlyV4(20, 150, 1),
                TestRequestDataFactory.scheduleCustomV4(800)
        ]
    }

    def "we get 422 when create a payment consent with account type SLRY"() {
        given:
        def clientId = UUID.randomUUID().toString()
        CreatePaymentConsentV4 paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel(), "20422.00")
        paymentConsentRequest.data.debtorAccount.accountType(EnumAccountPaymentsType.SLRY)

        when:
        paymentConsentService.createConsentV4(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)


        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY
    }

    def "we throw 422 for invalid schedule and date at consent creation"() {

        given:
        CreatePaymentConsentV4 paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel(), "100.00")
        paymentConsentRequest.data.payment.schedule(new AllOfPaymentConsentV4PaymentSchedule().single(new ScheduleSingleSingle().date(scheduleDate)) as AllOfPaymentConsentV4PaymentSchedule)
        paymentConsentRequest.data.payment.date(date)
        paymentConsentRequest.data.debtorAccount.setAccountType(EnumAccountPaymentsType.CACC)
        when:
        paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY
        ex.message.startsWith(errorMessage)

        where:
        scheduleDate                                                   | date                                             | errorMessage
        null                                                           | null                                             | ErrorCodesEnumV2.PARAMETRO_NAO_INFORMADO.name()
        LocalDate.now(BankLambdaUtils.getBrasilZoneId()).plusDays(1)   | LocalDate.now(BankLambdaUtils.getBrasilZoneId()) | ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name()
        LocalDate.now(BankLambdaUtils.getBrasilZoneId()).plusDays(380) | null                                             | ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name()
        LocalDate.now(BankLambdaUtils.getBrasilZoneId()).minusDays(1)  | null                                             | ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name()
        LocalDate.now(BankLambdaUtils.getBrasilZoneId())               | LocalDate.now(BankLambdaUtils.getBrasilZoneId()) | ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name()
    }

    def "we throw 422 for invalid enums at consent creation"() {

        given:
        CreatePaymentConsentV4 paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel(), "100.00")
        paymentConsentRequest.data.payment.setType(paymentType)
        paymentConsentRequest.data.creditor.setPersonType(personType.toString())
        when:
        paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY
        ex.message == errorMessage.toString()

        where:
        personType                     | paymentType         | errorMessage
        null                           | EnumPaymentType.PIX | ErrorCodesEnumV2.PARAMETRO_INVALIDO.name() + ": " + ErrorCodesEnumV2.PARAMETRO_INVALIDO.title
        EnumCreditorPersonType.NATURAL | null                | ErrorCodesEnumV2.PARAMETRO_INVALIDO.name() + ": " + ErrorCodesEnumV2.PARAMETRO_INVALIDO.title
    }

    def "we throw 422 for invalid routine schedule info"() {
        given:
        CreatePaymentConsentV4 paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel(), "100.00")
        paymentConsentRequest.data.payment.schedule(new AllOfPaymentConsentV4PaymentSchedule().weekly(
                new ScheduleWeeklyWeekly().startDate(scheduleDate).dayOfWeek(dayOfWeek).quantity(quantity)))
        paymentConsentRequest.data.debtorAccount.setAccountType(EnumAccountPaymentsType.CACC)
        paymentConsentRequest.data.payment.date(null)

        when:
        paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY
        ex.message.startsWith(errorMessage)

        where:
        scheduleDate                                                  | quantity | dayOfWeek                                  | errorMessage
        LocalDate.now(BankLambdaUtils.getBrasilZoneId())              | 600      | ScheduleWeeklyWeekly.DayOfWeekEnum.DOMINGO | ErrorCodesEnumV2.PARAMETRO_INVALIDO.name()
        LocalDate.now(BankLambdaUtils.getBrasilZoneId()).minusDays(1) | 1        | ScheduleWeeklyWeekly.DayOfWeekEnum.DOMINGO | ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name()
        LocalDate.now(BankLambdaUtils.getBrasilZoneId())              | -1       | ScheduleWeeklyWeekly.DayOfWeekEnum.DOMINGO | ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name()
    }

    def "we throw 422 for invalid custom schedule info"() {
        given:
        CreatePaymentConsentV4 paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel(), "100.00")
        paymentConsentRequest.data.payment.schedule(new AllOfPaymentConsentV4PaymentSchedule().custom(
                new ScheduleCustomCustom().dates(customDates).additionalInformation(additionalInformation)))
        paymentConsentRequest.data.debtorAccount.setAccountType(EnumAccountPaymentsType.CACC)
        paymentConsentRequest.data.payment.date(null)

        when:
        paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY
        ex.message.startsWith(errorMessage)

        where:
        customDates                                                                                                        | additionalInformation | errorMessage
        [LocalDate.now(BankLambdaUtils.getBrasilZoneId()), LocalDate.now(BankLambdaUtils.getBrasilZoneId()).plusDays(800)] | "hello world"         | ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name()
        [LocalDate.now(BankLambdaUtils.getBrasilZoneId()).minusDays(1), LocalDate.now(BankLambdaUtils.getBrasilZoneId())]  | "hello world"         | ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name()
        [LocalDate.now(BankLambdaUtils.getBrasilZoneId()), LocalDate.now(BankLambdaUtils.getBrasilZoneId()).plusDays(1)]   | null                  | ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name()
        [LocalDate.now(BankLambdaUtils.getBrasilZoneId()), LocalDate.now(BankLambdaUtils.getBrasilZoneId()).plusDays(1)]   | ""                    | ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name()
        [LocalDate.now(BankLambdaUtils.getBrasilZoneId())]                                                                 | "hello world"         | ErrorCodesEnumV2.PARAMETRO_INVALIDO.name()
    }

    @Unroll
    def "422 DETALHE_PAGAMENTO_INVALIDO is thrown if we try to create a consent with a QR Code and an invalid local instrument"() {
        given:
        def paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.getData().getPayment().getDetails().setQrCode(QrCodeUtils.createQrCode(paymentConsentRequest).toString())

        when:
        paymentConsentRequest.getData().getPayment().getDetails().setLocalInstrument(EnumLocalInstrument.MANU)
        paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        then:
        def e = thrown(HttpStatusException)
        def message = "DETALHE_PAGAMENTO_INVALIDO: Parâmetro não informado."
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == message

        when:
        paymentConsentRequest.getData().getPayment().getDetails().setLocalInstrument(EnumLocalInstrument.DICT)
        paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        then:
        def e2 = thrown(HttpStatusException)
        def message2 = "DETALHE_PAGAMENTO_INVALIDO: Parâmetro não informado."
        e2.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e2.getMessage() == message
    }

    @Unroll
    def "422 DETALHE_PGTO_INVALIDO is thrown if we cannot parse qrCode"() {
        given:
        def paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.getData().getPayment().getDetails().setQrCode("test")
        paymentConsentRequest.getData().getPayment().getDetails().setLocalInstrument(localInstrument)

        when:
        paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), jti, paymentConsentRequest)
        then:
        def e = thrown(HttpStatusException)
        def message = "DETALHE_PAGAMENTO_INVALIDO: Could not decode QrCode - test"
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == message

        where:
        localInstrument << [EnumLocalInstrument.QRES, EnumLocalInstrument.QRDN]
        jti << [UUID.randomUUID().toString(), UUID.randomUUID().toString()]
    }

    @Unroll
    def "422 PARAMETRO_NAO_INFORMADO is thrown if qrCode is not present"() {
        given:
        def paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.getData().getPayment().getDetails().setQrCode(null)
        paymentConsentRequest.getData().getPayment().getDetails().setLocalInstrument(localInstrument)

        when:
        paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), jti, paymentConsentRequest)
        then:
        def e = thrown(HttpStatusException)
        def message = "PARAMETRO_NAO_INFORMADO: QrCode is not present"
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == message

        where:
        localInstrument << [EnumLocalInstrument.QRES, EnumLocalInstrument.QRDN]
        jti << [UUID.randomUUID().toString(), UUID.randomUUID().toString()]
    }

    @Unroll
    def "422 DETALHE_PGTO_INVALIDO is thrown if the qrCode has incorrect fields"() {
        given: "The amount is different to the payment consent"
        CreatePaymentConsentV4 paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        String qrCode = QrCodeUtils.createQrCode(paymentConsentRequest.getData().getCreditor().getName(), paymentConsentRequest.getData().getPayment().getDetails().getProxy(),
                paymentConsentRequest.getData().getPayment().getAmount(), paymentConsentRequest.getData().getPayment().getCurrency())
        paymentConsentRequest.getData().getPayment().getDetails().setLocalInstrument(EnumLocalInstrument.QRDN)
        paymentConsentRequest.getData().getPayment().getDetails().setQrCode(qrCode)

        when: "The amount is different to the payment consent"
        paymentConsentRequest.getData().getPayment().setAmount("12345")
        paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        then:
        def e = thrown(HttpStatusException)
        def message = "DETALHE_PAGAMENTO_INVALIDO: Amount defined in QrCode - 100.00 differs from the amount specified in the Consent - 12345.00"
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == message

        when: "The currency is different to the payment consent"
        paymentConsentRequest.getData().getPayment().setCurrency("USD")
        paymentConsentRequest.getData().getPayment().setAmount("100.00")
        paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        then:
        def e2 = thrown(HttpStatusException)
        def message2 = "DETALHE_PAGAMENTO_INVALIDO: Currency code defined in QrCode - 986 differs from the currency code specified in the Consent - 840"
        e2.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e2.getMessage() == message2

        when: "The proxy is different to the payment consent"
        paymentConsentRequest.getData().getPayment().getDetails().setProxy("proxy99")
        paymentConsentRequest.getData().getPayment().setCurrency("BRL")
        paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        then:
        def e3 = thrown(HttpStatusException)
        def message3 = "DETALHE_PAGAMENTO_INVALIDO: Proxy defined in QrCode - proxy differs from the proxy specified in the Consent - proxy99"
        e3.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e3.getMessage() == message3

        when: "The creditor name is different to the payment consent"
        paymentConsentRequest.getData().getCreditor().setName("Billy Loanman")
        paymentConsentRequest.getData().getPayment().getDetails().setProxy("proxy")
        paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        then:
        def e4 = thrown(HttpStatusException)
        def message4 = "DETALHE_PAGAMENTO_INVALIDO: Creditor name defined in QrCode - Bob Creditor differs from the Creditor name specified in the Consent - Billy Loanman"
        e4.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e4.getMessage() == message4
    }

    def "we cannot create a payment consent with invalid currency"() {
        given:
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(BankLambdaUtils.getBrasilZoneId()), "LLL", "100")

        when:
        paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
    }

    def "we can create a pix payment with no schedule"() {
        given:
        def clientId = UUID.randomUUID().toString()

        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(), LocalDate.now(BankLambdaUtils.getBrasilZoneId()), "BRL", "100")

        def responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        def updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)
        def createPixPaymentRequest = new CreatePixPaymentV4().data(List.of(TestRequestDataFactory.createPixPaymentDataV4(responseConsent.getData().getConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(LocalDate.now(BankLambdaUtils.getBrasilZoneId())), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", "")))

        when:
        paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), createPixPaymentRequest, clientId)

        then:
        noExceptionThrown()
    }

    def "we can create a schedule payment if the EndToEndId dates are same as consent's scheduling"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(BankLambdaUtils.getBrasilZoneId()), "BRL", "100")
        paymentConsentRequest.data.payment.date(null)

        when: "we have 7 schedule daily dates"
        paymentConsentRequest.data.payment.schedule(TestRequestDataFactory.scheduleDailyV4())

        def responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        def updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)
        def listOfPaymentsData = new ArrayList<CreatePixPaymentDataV4>()
        for (i in 0..<7) {
            listOfPaymentsData.add(TestRequestDataFactory.createPixPaymentDataV4(responseConsent.getData().getConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(LocalDate.now(BankLambdaUtils.getBrasilZoneId()).plusDays(i)), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", ""))
        }
        ResponsePixPaymentV4 response = paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), new CreatePixPaymentV4().data(listOfPaymentsData), clientId)

        then:
        noExceptionThrown()
        response.getData().size() == 7

        when: "we have 7 schedule monthly dates"
        paymentConsentRequest.data.payment.schedule(TestRequestDataFactory.scheduleMonthlyV4())

        responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)
        def startDate = LocalDate.now(BankLambdaUtils.getBrasilZoneId())
        def scheduledDate = 31
        listOfPaymentsData = new ArrayList<CreatePixPaymentDataV4>()
        for (i in 0..<7) {
            def correctedDate = startDate.plusMonths(i).lengthOfMonth() < scheduledDate ? startDate.plusMonths(i+1).withDayOfMonth(1) : startDate.plusMonths(i).withDayOfMonth(scheduledDate)
            listOfPaymentsData.add(TestRequestDataFactory.createPixPaymentDataV4(responseConsent.getData().getConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(correctedDate), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", ""))
        }
        response = paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), new CreatePixPaymentV4().data(listOfPaymentsData), clientId)

        then:
        noExceptionThrown()
        response.getData().size() == 7

        when: "we have 5 schedule weekly dates"
        paymentConsentRequest.data.payment.schedule(TestRequestDataFactory.scheduleWeeklyV4())

        responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)
        def weeklySchedule = paymentConsentRequest.getData().getPayment().getSchedule().getWeekly()
        startDate = weeklySchedule.getStartDate()
        startDate = startDate.isBefore(LocalDate.now(BankLambdaUtils.getBrasilZoneId())) ? LocalDate.now(BankLambdaUtils.getBrasilZoneId()) : startDate
        DayOfWeek startDateDayOfWeek = startDate.getDayOfWeek()
        def scheduleDayOfWeek = BankLambdaUtils.getPaymentScheduleWeeklyOrdinal(weeklySchedule.getDayOfWeek().toString())
        if (startDate.getDayOfWeek().getValue() > scheduleDayOfWeek) {
            startDate = startDate.plusDays(7 - Math.abs(startDateDayOfWeek.getValue() - scheduleDayOfWeek))
        } else {
            startDate = startDate.plusDays(scheduleDayOfWeek - startDateDayOfWeek.getValue())
        }

        listOfPaymentsData = new ArrayList<CreatePixPaymentDataV4>()
        for (i in 0..<5) {
            listOfPaymentsData.add(TestRequestDataFactory.createPixPaymentDataV4(responseConsent.getData().getConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(startDate.plusWeeks(i)), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", ""))
        }
        response = paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), new CreatePixPaymentV4().data(listOfPaymentsData), clientId)

        then:
        noExceptionThrown()
        response.getData().size() == 5

        when: "we have a single schedule date"
        paymentConsentRequest.data.payment.schedule(TestRequestDataFactory.scheduleSingleV4())

        responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)
        listOfPaymentsData = new ArrayList<CreatePixPaymentDataV4>()
        listOfPaymentsData.add(TestRequestDataFactory.createPixPaymentDataV4(responseConsent.getData().getConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(LocalDate.now(BankLambdaUtils.getBrasilZoneId()).plusDays(1)), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", ""))
        response = paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), new CreatePixPaymentV4().data(listOfPaymentsData), clientId)

        then:
        noExceptionThrown()
        response.getData().size() == 1
    }

    def "idempotency requires functionally identical payloads"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def idempotencyKey = UUID.randomUUID().toString()
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(), LocalDate.now(BankLambdaUtils.getBrasilZoneId()), "BRL", "100")

        def responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        def updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)
        def createPixPaymentRequest = new CreatePixPaymentV4().data(List.of(TestRequestDataFactory.createPixPaymentDataV4(responseConsent.getData().getConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(LocalDate.now(BankLambdaUtils.getBrasilZoneId())), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", "")))
        paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), idempotencyKey, UUID.randomUUID().toString(), createPixPaymentRequest, clientId)

        when:
        paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), idempotencyKey, UUID.randomUUID().toString(), createPixPaymentRequest, clientId)

        then:
        noExceptionThrown()
    }

    def "idempotency fails when have different payloads"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def idempotencyKey = UUID.randomUUID().toString()
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(), LocalDate.now(BankLambdaUtils.getBrasilZoneId()), "BRL", "100")

        def responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        def updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)
        def createPixPaymentRequest = new CreatePixPaymentV4().data(List.of(TestRequestDataFactory.createPixPaymentDataV4(responseConsent.getData().getConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(LocalDate.now(BankLambdaUtils.getBrasilZoneId())), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", "")))
        paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), idempotencyKey, UUID.randomUUID().toString(), createPixPaymentRequest, clientId)

        when:
        createPixPaymentRequest = new CreatePixPaymentV4().data(List.of(TestRequestDataFactory.createPixPaymentDataV4(responseConsent.getData().getConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(LocalDate.now(BankLambdaUtils.getBrasilZoneId())), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "200", "GBP", "proxy", "", "cnpj", "")))
        paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), idempotencyKey, UUID.randomUUID().toString(), createPixPaymentRequest, clientId)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage().startsWith(ErrorCodesEnumV2.ERRO_IDEMPOTENCIA.name())

        when:
        createPixPaymentRequest = new CreatePixPaymentV4().data(List.of(TestRequestDataFactory.createPixPaymentDataV4(responseConsent.getData().getConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(LocalDate.now(BankLambdaUtils.getBrasilZoneId())), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", ""),
                TestRequestDataFactory.createPixPaymentDataV4(responseConsent.getData().getConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(LocalDate.now(BankLambdaUtils.getBrasilZoneId())), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", "")))
        paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), idempotencyKey, UUID.randomUUID().toString(), createPixPaymentRequest, clientId)

        then:
        HttpStatusException e2 = thrown()
        e2.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e2.getMessage().startsWith(ErrorCodesEnumV2.ERRO_IDEMPOTENCIA.name())

        when: "The consent has been set to CONSUMED when a valid payment with a new idempotency ID is used"
        createPixPaymentRequest = new CreatePixPaymentV4().data(List.of(TestRequestDataFactory.createPixPaymentDataV4(responseConsent.getData().getConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(LocalDate.now(BankLambdaUtils.getBrasilZoneId())), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", "")))
        paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), idempotencyKey, UUID.randomUUID().toString(), createPixPaymentRequest, clientId)
        idempotencyKey = UUID.randomUUID().toString()
        paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), idempotencyKey, UUID.randomUUID().toString(), createPixPaymentRequest, clientId)

        then:
        HttpStatusException e3 = thrown()
        e3.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e3.getMessage().startsWith(ErrorCodesEnumV2.CONSENTIMENTO_INVALIDO.name())
    }

    def "we cannot create a schedule payment if the EndToEndId dates are not the same as consent's scheduling"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4WithScheduled("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                "BRL", "100")

        ResponseCreatePaymentConsentV4 responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        CreatePixPaymentV4 paymentRequest = TestRequestDataFactory.createPaymentRequestV4WithScheduled(responseConsent.getData().getConsentId(), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", "")
        paymentRequest.getData().get(1).endToEndId(EndToEndIdHelper.generateRandomEndToEndIdPlusTenDay())
        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        ResponsePixPaymentV4 response = paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage().startsWith(ErrorCodesEnumV2.PAGAMENTO_DIVERGENTE_CONSENTIMENTO.name())

    }

    def "we cannot create a payment if the info doesn't mach with consent"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4WithScheduled("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(), "GBP", "100")
        def responseConsent = paymentConsentService.createConsentV4(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        def updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        def paymentRequest = TestRequestDataFactory.createPaymentRequestV4WithScheduled(responseConsent.getData().getConsentId(), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", "")

        when: "the currency differ from consent"
        paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage().startsWith(ErrorCodesEnumV2.PAGAMENTO_DIVERGENTE_CONSENTIMENTO.name()) && e.getMessage().contains("currency")

        when: "the proxy differ from consent"
        paymentRequest = TestRequestDataFactory.createPaymentRequestV4WithScheduled(responseConsent.getData().getConsentId(), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "GBP", "proxy2", "", "cnpj", "")
        paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException e2 = thrown()
        e2.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e2.getMessage().startsWith(ErrorCodesEnumV2.PAGAMENTO_DIVERGENTE_CONSENTIMENTO.name()) && e2.getMessage().contains("proxy")

        when: "the consent quantity is different from the payment quantity"
        paymentRequest = TestRequestDataFactory.createPaymentRequestV4(responseConsent.getData().getConsentId(), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "GBP", "proxy2", "", "cnpj", "")
        paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException e3 = thrown()
        e3.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e3.getMessage() == ErrorCodesEnumV2.PAGAMENTO_DIVERGENTE_CONSENTIMENTO.name()

        when: "the consent id differ from consent"
        paymentRequest = TestRequestDataFactory.createPaymentRequestV4WithScheduled("consentId", "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "GBP", "proxy", "", "cnpj", "", "", EnumAuthorisationFlow.FIDO_FLOW)
        paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException e4 = thrown()
        e4.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e4.getMessage().startsWith(ErrorCodesEnumV2.PAGAMENTO_DIVERGENTE_CONSENTIMENTO.name()) && e4.getMessage().contains("consentId")
    }

    def "We can't create a payment id the endToEndId is not set"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4WithScheduled("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(), "GBP", "100")
        def responseConsent = paymentConsentService.createConsentV4(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        def paymentRequest = TestRequestDataFactory.createPaymentRequestV4WithScheduled(responseConsent.getData().getConsentId(), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "GBP", "proxy", "", "cnpj", "", "", EnumAuthorisationFlow.HYBRID_FLOW, null)
        paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == new PaymentErrorMessageV2().getParameterNotInformed("endToEndId is missing or invalid")

        when:
        def getConsentResponse = paymentConsentService.getConsentV4(responseConsent.getData().getConsentId(), clientId)

        then:
        noExceptionThrown()
        getConsentResponse.getData().getStatus() == EnumAuthorisationStatusType.CONSUMED
    }

    def "We can't create a payment id the endToEndId time is not set to 1500"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4WithScheduled("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(), "GBP", "100")
        def responseConsent = paymentConsentService.createConsentV4(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        def paymentRequest = TestRequestDataFactory.createPaymentRequestV4WithScheduled(responseConsent.getData().getConsentId(), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "GBP", "proxy", "", "cnpj", "", "", EnumAuthorisationFlow.HYBRID_FLOW, "E90400888202101281100" +  RandomStringUtils.randomAlphanumeric(11))
        paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == new PaymentErrorMessageV2().getMessageInvalidParameter("endToEndId timestamp must be the correct format and match 15:00")
    }

    @Unroll
    def "We can't create a payment with a transactionIdentification value and an unsupported localInstrument"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(), LocalDate.now(BankLambdaUtils.getBrasilZoneId()), "BRL", "100")

        when:
        def responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        then:
        noExceptionThrown()

        when:
        def createPixPaymentRequest = new CreatePixPaymentV4().data(List.of(TestRequestDataFactory.createPixPaymentDataV4(responseConsent.getData().getConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(LocalDate.now(BankLambdaUtils.getBrasilZoneId())), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", "", "validTransactionIdentfication")))
        paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), createPixPaymentRequest, clientId)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "DETALHE_PAGAMENTO_INVALIDO: Parâmetro transactionIdentification não obedece às regras de negócio"

        where:
        localInstrument << [
            EnumLocalInstrument.MANU,
            EnumLocalInstrument.DICT,
            EnumLocalInstrument.QRES
        ]
    }

    def "we can create a payment consent and force a Reject Reason"() {
        given:
        def paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.data.payment.amount(amount)
        paymentConsentRequest.data.payment.details.creditorAccount.number("9876543")
        paymentConsentRequest.data.debtorAccount.setAccountType(EnumAccountPaymentsType.CACC)
        def responseCreate = paymentConsentService.createConsentV4(clientId, idemPotencyKey, jti, paymentConsentRequest)

        when:
        def response = paymentConsentService.getConsentV4(responseCreate.getData().getConsentId(), clientId)

        then:
        response.getData().getStatus() == EnumAuthorisationStatusType.REJECTED
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

    def "A single payment with RCVD will be moved to the ACSC status on GET if the date is today or earlier"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(), LocalDate.now(BankLambdaUtils.getBrasilZoneId()), "BRL", "100.00")

        def createdConsent = paymentConsentService.createConsentV4(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        def consentEntity = paymentConsentRepository.findByPaymentConsentId(createdConsent.data.getConsentId()).orElse(null)
        assert consentEntity != null
        consentEntity.setStatus(EnumConsentStatus.AUTHORISED.toString())
        consentEntity = paymentConsentRepository.update(consentEntity)

        def paymentData = TestRequestDataFactory.createPixPaymentDataV4(consentEntity.getPaymentConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(LocalDate.now(BankLambdaUtils.getBrasilZoneId())), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100.00", "BRL", "proxy", "", "cnpj", "")
        def paymentRequest = new CreatePixPaymentV4().data(List.of(paymentData))

        def createPaymentResponse = paymentsService.createPaymentV4(consentEntity.getPaymentConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentRequest, clientId)
        def paymentId = createPaymentResponse.getData().get(0).getPaymentId()
        when:
        ResponsePixPaymentReadV4 foundPayment = paymentsService.getPaymentV4(paymentId, clientId)

        then:
        noExceptionThrown()
        foundPayment != null
        foundPayment.getData() != null
        foundPayment.getData().getPaymentId() != null
        foundPayment.getData().getStatus() == EnumPaymentStatusTypeV2.ACSC
    }

    def "We can create a payment and move the status to PDNG after retrieval"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(), LocalDate.now(BankLambdaUtils.getBrasilZoneId()), "BRL", "12345.00")

        def createdConsent = paymentConsentService.createConsentV4(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        def consentEntity = paymentConsentRepository.findByPaymentConsentId(createdConsent.data.getConsentId()).orElse(null)
        assert consentEntity != null
        consentEntity.setStatus(EnumConsentStatus.AUTHORISED.toString())
        consentEntity = paymentConsentRepository.update(consentEntity)

        def paymentData = TestRequestDataFactory.createPixPaymentDataV4(consentEntity.getPaymentConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(LocalDate.now(BankLambdaUtils.getBrasilZoneId())), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "12345.00", "BRL", "proxy", "", "cnpj", "")
        def paymentRequest = new CreatePixPaymentV4().data(List.of(paymentData))

        def createPaymentResponse = paymentsService.createPaymentV4(consentEntity.getPaymentConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentRequest, clientId)
        def paymentId = createPaymentResponse.getData().get(0).getPaymentId()
        when:
        ResponsePixPaymentReadV4 foundPayment = paymentsService.getPaymentV4(paymentId, clientId)

        then:
        noExceptionThrown()
        foundPayment != null
        foundPayment.getData() != null
        foundPayment.getData().getPaymentId() != null
        foundPayment.getData().getStatus() == EnumPaymentStatusTypeV2.PDNG
    }

    def "we can create a payment consent and force the get call to return PDNG and ACSC "() {
        given:
        def paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.data.payment.amount("12345.67")
        paymentConsentRequest.data.payment.details.creditorAccount.number("9876543")
        paymentConsentRequest.data.debtorAccount.setAccountType(EnumAccountPaymentsType.CACC)
        String clientId = UUID.randomUUID().toString()
        def responseCreate = paymentConsentService.createConsentV4(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        when:
        def responseConsent = paymentConsentService.getConsentV4(responseCreate.getData().getConsentId(), clientId)
        def updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        def updateResponseConsent = paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        then:
        noExceptionThrown()
        updateResponseConsent.getData().getStatus() == ResponsePaymentConsentData.StatusEnum.AUTHORISED

        when:
        def paymentRequest = TestRequestDataFactory.createPaymentRequestV4(responseConsent.getData().getConsentId(), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "12345.67", "BRL", "proxy", "", "cnpj", "")
        def createPayment = paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        noExceptionThrown()
        createPayment.getData().get(0).getStatus() == EnumPaymentStatusTypeV2.RCVD

        when:
        String paymentId = createPayment.getData().get(0).getPaymentId()
        ResponsePixPaymentReadV4 foundPayment = paymentsService.getPaymentV4(paymentId, clientId)

        then:
        noExceptionThrown()
        foundPayment.getData().getStatus() == EnumPaymentStatusTypeV2.PDNG

        when:
        foundPayment = paymentsService.getPaymentV4(paymentId, clientId)

        then:
        noExceptionThrown()
        foundPayment.getData().getStatus() == EnumPaymentStatusTypeV2.ACSC
    }

    def "We can simulate the multiple consents flow using PARTIALLY_ACCEPTED to create a V4 payment"() {
        given:
        def paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.data.debtorAccount.setAccountType(EnumAccountPaymentsType.CACC)
        def clientId = UUID.randomUUID().toString()
        def responseConsent = paymentConsentService.createConsentV4(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        when:
        responseConsent.getData().getStatus() == EnumAuthorisationStatusType.AWAITING_AUTHORISATION
        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.PARTIALLY_ACCEPTED, false)
        def updateResponseConsent = paymentConsentService.updateConsentV4(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        then:
        noExceptionThrown()
        updateResponseConsent.getData().getStatus() == EnumAuthorisationStatusType.PARTIALLY_ACCEPTED

        when:
        def getConsent = paymentConsentService.getConsentV4(responseConsent.getData().getConsentId(), clientId)

        then:
        noExceptionThrown()
        assert getConsent != null
        getConsent.data.status == EnumAuthorisationStatusType.PARTIALLY_ACCEPTED

        when:
        def paymentRequest = TestRequestDataFactory.createPaymentRequestV4(responseConsent.getData().getConsentId(), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100.00", "BRL", "proxy", "", "cnpj", "")
        paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        def e2 = thrown(HttpStatusException)
        e2.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e2.getMessage() == "CONSENTIMENTO_PENDENTE_AUTORIZACAO"
        paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().getConsentId()).get().status == EnumAuthorisationStatusType.PARTIALLY_ACCEPTED.toString()

        when:
        def repositoryConsent = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().getConsentId()).orElse(null)
        assert repositoryConsent != null
        repositoryConsent.setCreationDateTime(Date.from(BankLambdaUtils.getInstantInBrasil().minusSeconds(180)))
        paymentConsentRepository.update(repositoryConsent)
        def responsePaymentConsent = paymentConsentService.getConsentV4(responseConsent.getData().getConsentId(), clientId)
        def responsePixPayment = paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentRequest, clientId)

        then:
        noExceptionThrown()
        assert responsePaymentConsent != null
        responsePixPayment.data != null
        responsePaymentConsent.data.status == EnumAuthorisationStatusType.AUTHORISED

        when: "We can also do it straight from a POST payment"
        repositoryConsent.setStatus(EnumAuthorisationStatusType.PARTIALLY_ACCEPTED.toString())
        paymentConsentRepository.update(repositoryConsent)
        repositoryConsent = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().getConsentId()).orElse(null)

        then:
        noExceptionThrown()
        repositoryConsent.getStatus() == EnumAuthorisationStatusType.PARTIALLY_ACCEPTED.toString()

        when:
        def responsePixPayment2 = paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentRequest, clientId)
        repositoryConsent = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().getConsentId()).orElse(null)

        then:
        responsePixPayment2.data != null
        repositoryConsent.status == EnumAuthorisationStatusType.CONSUMED.toString()
    }

    def "we can get reject reason when payment consent is rejected"() {
        given:
        def paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.data.debtorAccount.setAccountType(EnumAccountPaymentsType.CACC)
        def responseConsent = paymentConsentService.createConsentV4(clientId, idemPotencyKey, jti, paymentConsentRequest)
        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.REJECTED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)

        when:
        def response = paymentConsentService.getConsentV4(responseConsent.getData().getConsentId(), clientId)

        then:
        response.getData().getStatus() == EnumAuthorisationStatusType.REJECTED
        response.getData().getRejectionReason().getCode() == EnumConsentRejectionReasonType.REJEITADO_USUARIO

        where:
        clientId                     | jti                          | idemPotencyKey
        UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString()

    }

    def "A payment consent is created but rejected if the account lacks the funds to cover the transaction"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def idempotencyKey = UUID.randomUUID().toString()
        def jti = UUID.randomUUID().toString()

        CreatePaymentConsentV4 paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel(), "200.00")
        paymentConsentRequest.data.debtorAccount.setAccountType(EnumAccountPaymentsType.CACC)

        when:
        def responseCreate = paymentConsentService.createConsentV4(clientId, idempotencyKey, jti, paymentConsentRequest)

        then:
        responseCreate.getData().getStatus() == EnumAuthorisationStatusType.REJECTED

        when:
        ResponsePaymentConsentV4 response = paymentConsentService.getConsentV4(responseCreate.getData().getConsentId(), clientId)

        then:
        response.getData().getStatus() == EnumAuthorisationStatusType.REJECTED
        response.getData().getRejectionReason().getCode() == EnumConsentRejectionReasonType.SALDO_INSUFICIENTE

        when:
        paymentConsentRequest.data.setDebtorAccount(null)
        responseCreate = paymentConsentService.createConsentV4(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        then:
        noExceptionThrown()
        responseCreate.getData().getStatus() == EnumAuthorisationStatusType.AWAITING_AUTHORISATION
    }

    def "we can get a payment v4"() {
        given:
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(), LocalDate.now(BankLambdaUtils.getBrasilZoneId()), "BRL", "100")

        def clientId = UUID.randomUUID().toString()

        def createdConsent = paymentConsentService.createConsentV4(
                clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        def consentEntity = paymentConsentRepository.findByPaymentConsentId(createdConsent.data.getConsentId()).orElse(null)
        assert consentEntity != null
        consentEntity.setStatus(EnumConsentStatus.AUTHORISED.toString())
        consentEntity = paymentConsentRepository.update(consentEntity)

        def paymentData = TestRequestDataFactory.createPixPaymentDataV4(consentEntity.getPaymentConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(LocalDate.now(BankLambdaUtils.getBrasilZoneId())), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", "")
        paymentData.setAuthorisationFlow(authorisationFlow)
        def paymentRequest = new CreatePixPaymentV4().data(List.of(paymentData))

        def createPaymentResponse = paymentsService.createPaymentV4(consentEntity.getPaymentConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentRequest, clientId)
        def paymentId = createPaymentResponse.getData().get(0).getPaymentId()
        when:
        ResponsePixPaymentReadV4 foundPayment = paymentsService.getPaymentV4(paymentId, clientId)

        then:
        noExceptionThrown()
        foundPayment != null
        foundPayment.getData() != null
        foundPayment.getData().getPaymentId() != null
        foundPayment.getData().getPaymentId() == paymentId

        where:
        authorisationFlow << [null, EnumAuthorisationFlow.HYBRID_FLOW, EnumAuthorisationFlow.CIBA_FLOW, EnumAuthorisationFlow.FIDO_FLOW]
    }

    def "we can reject a payment v4 after initiation and return the required fields"() {
        given:
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(), LocalDate.now(BankLambdaUtils.getBrasilZoneId()), "BRL", "20201.00")

        def clientId = UUID.randomUUID().toString()

        def createdConsent = paymentConsentService.createConsentV4(
                clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        def consentEntity = paymentConsentRepository.findByPaymentConsentId(createdConsent.data.getConsentId()).orElse(null)
        assert consentEntity != null
        consentEntity.setStatus(EnumConsentStatus.AUTHORISED.toString())
        consentEntity = paymentConsentRepository.update(consentEntity)

        def paymentData = TestRequestDataFactory.createPixPaymentDataV4(consentEntity.getPaymentConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(LocalDate.now(BankLambdaUtils.getBrasilZoneId())), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "20201.00", "BRL", "proxy", "", "cnpj", "")
        paymentData.setAuthorisationFlow(EnumAuthorisationFlow.HYBRID_FLOW)
        def paymentRequest = new CreatePixPaymentV4().data(List.of(paymentData))

        def createPaymentResponse = paymentsService.createPaymentV4(consentEntity.getPaymentConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentRequest, clientId)
        def paymentId = createPaymentResponse.getData().get(0).getPaymentId()
        when:
        ResponsePixPaymentReadV4 foundPayment = paymentsService.getPaymentV4(paymentId, clientId)

        then:
        noExceptionThrown()
        foundPayment != null
        foundPayment.getData() != null
        foundPayment.getData().getPaymentId() != null
        foundPayment.getData().getPaymentId() == paymentId
        foundPayment.getData().getStatus() == EnumPaymentStatusTypeV2.RJCT
        foundPayment.getData().getRejectionReason().getCode() == RejectionReasonV2.CodeEnum.DETALHE_PAGAMENTO_INVALIDO
        foundPayment.getData().getRejectionReason().getDetail() == "DETALHE_PAGAMENTO_INVALIDO"
    }

    def "we can cancel a payment"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(BankLambdaUtils.getBrasilZoneId()), "BRL", "100")
        paymentConsentRequest.data.payment.date(null)

        paymentConsentRequest.data.payment.schedule(TestRequestDataFactory.scheduleDailyV4())

        def responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        def updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)

        def consentId = responseConsent.getData().getConsentId()

        paymentConsentService.updateConsent(consentId, clientId, updatePaymentConsent)
        def listOfPaymentsData = new ArrayList<CreatePixPaymentDataV4>()
        for (i in 0..<7) {
            listOfPaymentsData.add(TestRequestDataFactory.createPixPaymentDataV4(consentId, EndToEndIdHelper.generateEndToEndIdWithDate(LocalDate.now(BankLambdaUtils.getBrasilZoneId()).plusDays(i)), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", ""))
        }
        def createPaymentResponse = paymentsService.createPaymentV4(consentId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), new CreatePixPaymentV4().data(listOfPaymentsData), clientId)

        def createdPaymentIds = createPaymentResponse.getData().stream().map { p ->
            {
                paymentsService.getPaymentV4(p.getPaymentId(), clientId)
                return p.getPaymentId()
            }
        }.collect(Collectors.toList())

        def paymentConsentPatchRequest = TestRequestDataFactory.createPatchPixConsentV4Request(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())

        when:
        def response = paymentsService.patchPaymentByConsentIdV4(consentId, paymentConsentPatchRequest)

        then:
        noExceptionThrown()
        def data = response.getData()
        data.size() == 6
        createdPaymentIds.size() == 7
        createdPaymentIds.stream().skip(1).forEach { createdPaymentId ->
            def payments = data.stream()
                    .filter { (it.getPaymentId() == createdPaymentId) }
                    .findAll()
            assert payments.size() == 1
            assert payments[0] != null

            def updatedPayment = paymentsService.getPaymentV4(createdPaymentId, clientId)
            updatedPayment != null
            updatedPayment.data.status == EnumPaymentStatusTypeV2.CANC
            updatedPayment.data.cancellation != null
            updatedPayment.data.cancellation.getCancelledAt() == BankLambdaUtils.getOffsetDateTimeInBrasil().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
        }
    }

    def "we cant cancel a payment if we have not created it"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(BankLambdaUtils.getBrasilZoneId()), "BRL", "100")
        paymentConsentRequest.data.payment.date(null)

        paymentConsentRequest.data.payment.schedule(TestRequestDataFactory.scheduleDailyV4())

        def responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        def updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)

        def consentId = responseConsent.getData().getConsentId()

        paymentConsentService.updateConsent(consentId, clientId, updatePaymentConsent)
        def listOfPaymentsData = new ArrayList<CreatePixPaymentDataV4>()
        for (i in 0..<7) {
            listOfPaymentsData.add(TestRequestDataFactory.createPixPaymentDataV4(consentId, EndToEndIdHelper.generateEndToEndIdWithDate(LocalDate.now(BankLambdaUtils.getBrasilZoneId()).plusDays(i)), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", ""))
        }

        paymentsService.createPaymentV4(consentId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), new CreatePixPaymentV4().data(listOfPaymentsData), clientId)

        def paymentConsentPatchRequest = TestRequestDataFactory.createPatchPixConsentV4Request("test", "TST")

        when:
        def response = paymentsService.patchPaymentByConsentIdV4(consentId, paymentConsentPatchRequest)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNAUTHORIZED
        e.getMessage() == "unauthorized"
        response == null

    }

    def "we cant cancel a payment if consent does not exist"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(BankLambdaUtils.getBrasilZoneId()), "BRL", "100")
        paymentConsentRequest.data.payment.date(null)

        paymentConsentRequest.data.payment.schedule(TestRequestDataFactory.scheduleDailyV4())

        def responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        def updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)

        def consentId = responseConsent.getData().getConsentId()

        paymentConsentService.updateConsent(consentId, clientId, updatePaymentConsent)
        def listOfPaymentsData = new ArrayList<CreatePixPaymentDataV4>()
        for (i in 0..<7) {
            listOfPaymentsData.add(TestRequestDataFactory.createPixPaymentDataV4(consentId, EndToEndIdHelper.generateEndToEndIdWithDate(LocalDate.now(BankLambdaUtils.getBrasilZoneId()).plusDays(i)), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", ""))
        }

        paymentsService.createPaymentV4(consentId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), new CreatePixPaymentV4().data(listOfPaymentsData), clientId)

        def paymentConsentPatchRequest = TestRequestDataFactory.createPatchPixConsentV4Request(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())

        when:
        def response = paymentsService.patchPaymentByConsentIdV4("test", paymentConsentPatchRequest)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "Consent does not exist"
        response == null

    }

    def "we can cancel a scheduled payment by paymentId"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(BankLambdaUtils.getBrasilZoneId()), "BRL", "100")
        paymentConsentRequest.data.payment.date(null)

        paymentConsentRequest.data.payment.schedule(TestRequestDataFactory.scheduleDailyV4())

        def responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        def updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)

        def consentId = responseConsent.getData().getConsentId()

        paymentConsentService.updateConsent(consentId, clientId, updatePaymentConsent)
        def listOfPaymentsData = new ArrayList<CreatePixPaymentDataV4>()
        for (i in 0..<7) {
            listOfPaymentsData.add(TestRequestDataFactory.createPixPaymentDataV4(consentId, EndToEndIdHelper.generateEndToEndIdWithDate(LocalDate.now(BankLambdaUtils.getBrasilZoneId()).plusDays(i)), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", ""))
        }
        def createPaymentResponse = paymentsService.createPaymentV4(consentId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), new CreatePixPaymentV4().data(listOfPaymentsData), clientId)
        def createdPaymentId = createPaymentResponse.getData().stream().reduce((first, second) -> second).get().getPaymentId()
        paymentsService.getPaymentV4(createdPaymentId, clientId)

        def paymentConsentPatchRequest = TestRequestDataFactory.createPatchPixConsentV4Request(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())

        when:
        def response = paymentsService.patchPaymentV4(createdPaymentId, paymentConsentPatchRequest)

        then:
        noExceptionThrown()
        def data = response.getData()

        def updatedPayment = pixPaymentRepository.findByPaymentId(data.getPaymentId()).orElse(null)
        assert updatedPayment != null
        assert updatedPayment.getStatus() == EnumPaymentStatusTypeV2.CANC.toString()
        assert updatedPayment.getCancellationFrom() == EnumPaymentCancellationFromTypeV4.INICIADORA.toString()
        assert updatedPayment.getCancellationReason() == EnumPaymentCancellationReasonTypeV4.AGENDAMENTO.toString()
    }

    def "we can cancel a PDNG payment by paymentId"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(BankLambdaUtils.getBrasilZoneId()), "BRL", "100")
        paymentConsentRequest.data.payment.date(null)

        paymentConsentRequest.data.payment.schedule(TestRequestDataFactory.scheduleDailyV4())

        def responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        def updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)

        def consentId = responseConsent.getData().getConsentId()

        paymentConsentService.updateConsent(consentId, clientId, updatePaymentConsent)
        def listOfPaymentsData = new ArrayList<CreatePixPaymentDataV4>()
        for (i in 0..<7) {
            listOfPaymentsData.add(TestRequestDataFactory.createPixPaymentDataV4(consentId, EndToEndIdHelper.generateEndToEndIdWithDate(LocalDate.now(BankLambdaUtils.getBrasilZoneId()).plusDays(i)), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", ""))
        }
        def createPaymentResponse = paymentsService.createPaymentV4(consentId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), new CreatePixPaymentV4().data(listOfPaymentsData), clientId)
        def createdPaymentId = createPaymentResponse.getData().stream().reduce((first, second) -> second).get().getPaymentId()
        def paymentCreated = pixPaymentRepository.findByPaymentId(createdPaymentId).get()
        paymentCreated.setStatus(EnumPaymentStatusTypeV2.PDNG.name())
        pixPaymentRepository.update(paymentCreated)

        def paymentConsentPatchRequest = TestRequestDataFactory.createPatchPixConsentV4Request(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())

        when:
        def response = paymentsService.patchPaymentV4(createdPaymentId, paymentConsentPatchRequest)

        then:
        noExceptionThrown()
        def data = response.getData()

        def updatedPayment = pixPaymentRepository.findByPaymentId(data.getPaymentId()).orElse(null)
        assert updatedPayment != null
        assert updatedPayment.getStatus() == EnumPaymentStatusTypeV2.CANC.toString()
        assert updatedPayment.getCancellationFrom() == EnumPaymentCancellationFromTypeV4.INICIADORA.toString()
        assert updatedPayment.getCancellationReason() == EnumPaymentCancellationReasonTypeV4.PENDENCIA.toString()
    }

    def "we can't cancel a payment by paymentId if the stauts is not SCHD or PDNG"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(BankLambdaUtils.getBrasilZoneId()), "BRL", "100")
        paymentConsentRequest.data.payment.date(null)

        paymentConsentRequest.data.payment.schedule(TestRequestDataFactory.scheduleDailyV4())

        def responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        def updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)

        def consentId = responseConsent.getData().getConsentId()

        paymentConsentService.updateConsent(consentId, clientId, updatePaymentConsent)
        def listOfPaymentsData = new ArrayList<CreatePixPaymentDataV4>()
        for (i in 0..<7) {
            listOfPaymentsData.add(TestRequestDataFactory.createPixPaymentDataV4(consentId, EndToEndIdHelper.generateEndToEndIdWithDate(LocalDate.now(BankLambdaUtils.getBrasilZoneId()).plusDays(i)), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", ""))
        }
        def createPaymentResponse = paymentsService.createPaymentV4(consentId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), new CreatePixPaymentV4().data(listOfPaymentsData), clientId)

        def createdPaymentId = createPaymentResponse.getData().stream().findFirst().get().getPaymentId()

        def paymentConsentPatchRequest = TestRequestDataFactory.createPatchPixConsentV4Request(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())

        when:
        def response = paymentsService.patchPaymentV4(createdPaymentId, paymentConsentPatchRequest)

        then:
        def e = thrown(HttpStatusException)
        assert e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        assert e.getMessage() == String.format("%s: payment not on allowed status SCHD or PDNG",
                ErrorCodesEnumV2.PAGAMENTO_NAO_PERMITE_CANCELAMENTO.name())
    }

    def "we can't cancel a payment by paymentId if if cancellation document rel is not CPF"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(BankLambdaUtils.getBrasilZoneId()), "BRL", "100")
        paymentConsentRequest.data.payment.date(null)

        paymentConsentRequest.data.payment.schedule(TestRequestDataFactory.scheduleDailyV4())

        def responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        def updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)

        def consentId = responseConsent.getData().getConsentId()

        paymentConsentService.updateConsent(consentId, clientId, updatePaymentConsent)
        def listOfPaymentsData = new ArrayList<CreatePixPaymentDataV4>()
        for (i in 0..<7) {
            listOfPaymentsData.add(TestRequestDataFactory.createPixPaymentDataV4(consentId, EndToEndIdHelper.generateEndToEndIdWithDate(LocalDate.now(BankLambdaUtils.getBrasilZoneId()).plusDays(i)), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", ""))
        }
        def createPaymentResponse = paymentsService.createPaymentV4(consentId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), new CreatePixPaymentV4().data(listOfPaymentsData), clientId)

        def createdPaymentId = createPaymentResponse.getData().stream().findFirst().get().getPaymentId()

        def paymentConsentPatchRequest = TestRequestDataFactory.createPatchPixConsentV4Request(accountHolder.getDocumentIdentification(), "XXX")

        when:
        def response = paymentsService.patchPaymentV4(createdPaymentId, paymentConsentPatchRequest)

        then:
        def e = thrown(HttpStatusException)
        assert e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        assert e.getMessage() == String.format("%s: cancelledBy.document.rel has incorrect format",
                ErrorCodesEnumV2.PAGAMENTO_NAO_PERMITE_CANCELAMENTO.name())
        response == null
    }

    def "we have the expirationDateTime updated properly"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(BankLambdaUtils.getBrasilZoneId()), "BRL", "100")
        paymentConsentRequest.data.payment.date(null)

        paymentConsentRequest.data.payment.schedule(TestRequestDataFactory.scheduleDailyV4())

        when:
        def responseConsent = paymentConsentService.createConsentV4(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        then:
        noExceptionThrown()
        responseConsent.getData().getExpirationDateTime() == responseConsent.getData().getCreationDateTime().plusMinutes(5)

        when:
        def updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)
        def foundPaymentConsent = paymentConsentService.getConsentV4(responseConsent.getData().getConsentId(), clientId)

        then:
        noExceptionThrown()
        foundPaymentConsent.getData().getExpirationDateTime() == foundPaymentConsent.getData().getStatusUpdateDateTime().plusMinutes(60)
    }

    def "We can send status changes for a payment consent and pix payment over a notification webhook URI"() {
        given:
        mockserver = startClientAndServer()

        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(), null, "BRL", "20201.00")

        int scheduleCount = 5
        paymentConsentRequest.data.payment.schedule(TestRequestDataFactory.scheduleDailyV4(scheduleCount))

        def clientId = UUID.randomUUID().toString()

        when:
        def createdConsent = paymentConsentService.createConsentV4(
                clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        webhookRepository.save(new WebhookEntity().createWebhook(clientId, new CreateWebhook().data(new CreateWebhookData().webhookUri("https://web.conformance.directory.openbankingbrasil.org.br/test-mtls/a/obbsb"))))
        def repositoryWebhook = webhookRepository.findByClientId(clientId)

        then:
        mockserver.hasStarted()
        repositoryWebhook != null
        repositoryWebhook.get() != null
        repositoryWebhook.get().clientId == clientId
        repositoryWebhook.get().webhookUri == "https://web.conformance.directory.openbankingbrasil.org.br/test-mtls/a/obbsb"

        when: "We can update the consent and trigger a webhook request"
        def updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        def consentId = createdConsent.getData().getConsentId()

        def webhookConsentListener = mockserver.when(
                request().withMethod("POST")
                        .withPath(String.format("/open-banking/webhook/v1/payments/v4/consents/%s", consentId))
        ).collect().collect()

        def updatedConsent = paymentConsentService.updateConsent(consentId, clientId, updatePaymentConsent)

        then:
        noExceptionThrown()
        updatedConsent != null
        updatedConsent.data != null
        webhookConsentListener != null
        webhookConsentListener.size() > 0

        when:
        mockserver.reset();
        def webhookPaymentListener = mockserver.when(
                request().withMethod("POST")
                        .withPath(String.format("/open-banking/webhook/v1/payments/v4/pix/payments/%s", consentId))
        ).collect().collect()

        List<CreatePixPaymentDataV4> listOfPaymentsData = new ArrayList<>();
        for (int i = 0; i < scheduleCount; i++) {
            listOfPaymentsData.add(TestRequestDataFactory.createPixPaymentDataV4(consentId, EndToEndIdHelper.generateEndToEndIdWithDate(LocalDate.now().plusDays(i)), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "20201.00", "BRL", "proxy", "", "cnpj", "").authorisationFlow(EnumAuthorisationFlow.HYBRID_FLOW))
        }

        def createPaymentResponse = paymentsService.createPaymentV4(consentId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), new CreatePixPaymentV4().data(listOfPaymentsData), clientId)
        def getPayment = paymentsService.getPaymentV4(createPaymentResponse.data.get(0).getPaymentId(), clientId)

        then:
        noExceptionThrown()
        getPayment != null
        getPayment.data.status.toString() == "RJCT"
        webhookPaymentListener != null
        webhookPaymentListener.size() > 0
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
