package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.EndToEndIdHelper
import com.raidiam.trustframework.bank.TestRequestDataFactory
import com.raidiam.trustframework.bank.domain.AccountEntity
import com.raidiam.trustframework.bank.domain.AccountHolderEntity
import com.raidiam.trustframework.bank.enums.ErrorCodesEnumV2
import com.raidiam.trustframework.bank.utils.BankLambdaUtils
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Stepwise
import spock.lang.Unroll

import javax.inject.Inject
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.stream.Collectors

import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccount
import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccountHolder

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class PaymentsServiceV4Spec extends CleanupSpecification {

    @Inject
    PaymentsService paymentsService
    @Inject
    PaymentConsentService paymentConsentService

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
        date.toString() == LocalDate.now().toString()


        and:
        var details = responseConsent.getPaymentConsentPaymentEntity().paymentConsentDetails
        details.proxy == 'proxy'
        details.localInstrument == EnumLocalInstrument.INIC.toString()
        details.creditorIssuer == 'mockbank'
        details.creditorAccountType == EnumAccountPaymentsType.CACC.toString()
        details.creditorIspb == 'ispb'
        details.creditorAccountNumber == '1234567890'
    }

    def "we can create a scheduled payment consent"() {

        CreatePaymentConsentV4 paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        paymentConsentRequest.data.payment.setDate(date)
        def schedule = paymentSchedule as AllOfCreatePaymentConsentV4DataPaymentSchedule
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
        paymentConsentRequest.data.payment.schedule(new AllOfCreatePaymentConsentV4DataPaymentSchedule().single(new ScheduleSingleSingle().date(scheduleDate)) as AllOfCreatePaymentConsentV4DataPaymentSchedule)
        paymentConsentRequest.data.payment.date(date)
        paymentConsentRequest.data.debtorAccount.setAccountType(EnumAccountPaymentsType.CACC)
        when:
        paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        then:
        HttpStatusException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY
        ex.message.startsWith(errorMessage)

        where:
        scheduleDate                  | date            | errorMessage
        null                          | null            | ErrorCodesEnumV2.PARAMETRO_NAO_INFORMADO.name()
        LocalDate.now().plusDays(1)   | LocalDate.now() | ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name()
        LocalDate.now().plusDays(380) | null            | ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name()
        LocalDate.now().minusDays(1)  | null            | ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name()
        LocalDate.now()               | LocalDate.now() | ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name()
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
        paymentConsentRequest.data.payment.schedule(new AllOfCreatePaymentConsentV4DataPaymentSchedule().weekly(
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
        scheduleDate                 | quantity | dayOfWeek                                  | errorMessage
        LocalDate.now()              | 600      | ScheduleWeeklyWeekly.DayOfWeekEnum.DOMINGO | ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name()
        LocalDate.now().minusDays(1) | 1        | ScheduleWeeklyWeekly.DayOfWeekEnum.DOMINGO | ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name()
        LocalDate.now()              | -1       | ScheduleWeeklyWeekly.DayOfWeekEnum.DOMINGO | ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name()
    }

    def "we throw 422 for invalid custom schedule info"() {
        given:
        CreatePaymentConsentV4 paymentConsentRequest = TestRequestDataFactory.testPaymentConsentV4(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel(), "100.00")
        paymentConsentRequest.data.payment.schedule(new AllOfCreatePaymentConsentV4DataPaymentSchedule().custom(
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
        customDates                                      | additionalInformation | errorMessage
        [LocalDate.now(), LocalDate.now().plusDays(800)] | "hello world"         | ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name()
        [LocalDate.now().minusDays(1), LocalDate.now()]  | "hello world"         | ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name()
        [LocalDate.now(), LocalDate.now().plusDays(1)]   | null                  | ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name()
        [LocalDate.now(), LocalDate.now().plusDays(1)]   | ""                    | ErrorCodesEnumV2.DATA_PAGAMENTO_INVALIDA.name()
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
        def message = "PARAMETRO_NAO_INFORMADO: Parâmetro não informado."
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == message

        where:
        localInstrument << [EnumLocalInstrument.QRES, EnumLocalInstrument.QRDN]
        jti << [UUID.randomUUID().toString(), UUID.randomUUID().toString()]
    }

    def "we cannot create a payment consent with invalid currency"() {
        given:
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.SLRY, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "LLL", "100")

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
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(), LocalDate.now(), "BRL", "100")

        def responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        def updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)
        def createPixPaymentRequest = new CreatePixPaymentV4().data(List.of(TestRequestDataFactory.createPixPaymentDataV4(responseConsent.getData().getConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(LocalDateTime.now()), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", "")))

        when:
        paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), createPixPaymentRequest, clientId)

        then:
        noExceptionThrown()
    }

    def "We can extract the weekly dates from a v4 schedule"() {
        given:
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        paymentConsentRequest.data.payment.date(null)
        paymentConsentRequest.data.payment.schedule(new AllOfCreatePaymentConsentV4DataPaymentSchedule()
                .weekly(new ScheduleWeeklyWeekly()
                        .startDate(LocalDate.now())
                        .quantity(1).dayOfWeek(startDayOfWeek)))

        def responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        def paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(responseConsent.getData().getConsentId()).get()
        paymentConsentEntity.getPaymentConsentPaymentEntity().setScheduleWeeklyDayOfWeek(startDayOfWeek.name())
        paymentConsentEntity.getPaymentConsentPaymentEntity().setScheduleWeeklyStartDate(LocalDate.now()
                .withDayOfMonth(startDay)
                .withMonth(startMonth)
                .withYear(startYear))

        def expectedDate = LocalDate.now()
                .withDayOfMonth(startDay)
                .withMonth(startMonth)
                .withYear(startYear)

        expectedDate = expectedDate.isBefore(LocalDate.now()) ? LocalDate.now() : expectedDate
        def scheduleDayOfWeek = BankLambdaUtils.getPaymentScheduleWeeklyOrdinal(startDayOfWeek.toString())

        if (expectedDate.getDayOfWeek().getValue() > scheduleDayOfWeek) {
            expectedDate = expectedDate.plusDays(7 - Math.abs(expectedDate.getDayOfWeek().getValue() - scheduleDayOfWeek))
        } else {
            expectedDate = expectedDate.plusDays(scheduleDayOfWeek - expectedDate.getDayOfWeek().getValue())
        }

        when:
        def extractedDates = paymentsService.generateScheduleDates(paymentConsentEntity)

        then:
        assert (extractedDates.first().dayOfMonth == expectedDate.dayOfMonth)
        assert (extractedDates.first().month == expectedDate.month)
        assert (extractedDates.first().year == expectedDate.year)

        where:
        startDay | startMonth | startYear | startDayOfWeek
        21       | 11         | 2023      | ScheduleWeeklyWeekly.DayOfWeekEnum.SEGUNDA_FEIRA
        28       | 11         | 2023      | ScheduleWeeklyWeekly.DayOfWeekEnum.SEGUNDA_FEIRA
        28       | 11         | 2023      | ScheduleWeeklyWeekly.DayOfWeekEnum.TERCA_FEIRA
        28       | 11         | 2023      | ScheduleWeeklyWeekly.DayOfWeekEnum.SEXTA_FEIRA
        28       | 11         | 2023      | ScheduleWeeklyWeekly.DayOfWeekEnum.DOMINGO
        01       | 12         | 2023      | ScheduleWeeklyWeekly.DayOfWeekEnum.QUINTA_FEIRA
    }

    def "we can create a schedule payment if the EndToEndId dates are same as consent's scheduling"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        paymentConsentRequest.data.payment.date(null)

        when: "we have 7 schedule daily dates"
        paymentConsentRequest.data.payment.schedule(TestRequestDataFactory.scheduleDailyV4())

        def responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        def updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)
        def listOfPaymentsData = new ArrayList<CreatePixPaymentDataV4>()
        for (i in 0..<7) {
            listOfPaymentsData.add(TestRequestDataFactory.createPixPaymentDataV4(responseConsent.getData().getConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(LocalDateTime.now().plusDays(i)), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", ""))
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
        def startDate = LocalDateTime.now()
        startDate = startDate.withDayOfMonth(paymentConsentRequest.data.payment.schedule.getMonthly().getDayOfMonth())
        if (paymentConsentRequest.data.payment.schedule.getMonthly().getDayOfMonth() < LocalDate.now().getDayOfMonth()) {
            startDate = startDate.plusMonths(1L)
        }
        listOfPaymentsData = new ArrayList<CreatePixPaymentDataV4>()
        for (i in 0..<7) {
            listOfPaymentsData.add(TestRequestDataFactory.createPixPaymentDataV4(responseConsent.getData().getConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(startDate.plusMonths(i)), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", ""))
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
        startDate = weeklySchedule.getStartDate().atStartOfDay()
        startDate = startDate.isBefore(LocalDateTime.now()) ? LocalDateTime.now() : startDate
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
        listOfPaymentsData.add(TestRequestDataFactory.createPixPaymentDataV4(responseConsent.getData().getConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(LocalDateTime.now().plusDays(1)), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", ""))
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
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(), LocalDate.now(), "BRL", "100")

        def responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        def updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)
        def createPixPaymentRequest = new CreatePixPaymentV4().data(List.of(TestRequestDataFactory.createPixPaymentDataV4(responseConsent.getData().getConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(LocalDateTime.now()), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", "")))
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
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(), LocalDate.now(), "BRL", "100")

        def responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        def updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        paymentConsentService.updateConsent(responseConsent.getData().getConsentId(), clientId, updatePaymentConsent)
        def createPixPaymentRequest = new CreatePixPaymentV4().data(List.of(TestRequestDataFactory.createPixPaymentDataV4(responseConsent.getData().getConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(LocalDateTime.now()), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", "")))
        paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), idempotencyKey, UUID.randomUUID().toString(), createPixPaymentRequest, clientId)

        when:
        createPixPaymentRequest = new CreatePixPaymentV4().data(List.of(TestRequestDataFactory.createPixPaymentDataV4(responseConsent.getData().getConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(LocalDateTime.now()), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "200", "GBP", "proxy", "", "cnpj", "")))
        paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), idempotencyKey, UUID.randomUUID().toString(), createPixPaymentRequest, clientId)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage().startsWith(ErrorCodesEnumV2.ERRO_IDEMPOTENCIA.name())

        when:
        createPixPaymentRequest = new CreatePixPaymentV4().data(List.of(TestRequestDataFactory.createPixPaymentDataV4(responseConsent.getData().getConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(LocalDateTime.now()), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", ""),
                TestRequestDataFactory.createPixPaymentDataV4(responseConsent.getData().getConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(LocalDateTime.now()), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", "")))
        paymentsService.createPaymentV4(responseConsent.getData().getConsentId(), idempotencyKey, UUID.randomUUID().toString(), createPixPaymentRequest, clientId)

        then:
        HttpStatusException e2 = thrown()
        e2.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e2.getMessage().startsWith(ErrorCodesEnumV2.ERRO_IDEMPOTENCIA.name())
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


    def "we can get a payment v4"() {
        given:
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(), LocalDate.now(), "BRL", "100")

        def clientId = UUID.randomUUID().toString()

        def createdConsent = paymentConsentService.createConsentV4(
                clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)

        def consentEntity = paymentConsentRepository.findByPaymentConsentId(createdConsent.data.getConsentId()).orElse(null)
        assert consentEntity != null
        consentEntity.setStatus(EnumConsentStatus.AUTHORISED.toString())
        consentEntity = paymentConsentRepository.update(consentEntity)

        def paymentData = TestRequestDataFactory.createPixPaymentDataV4(consentEntity.getPaymentConsentId(), EndToEndIdHelper.generateEndToEndIdWithDate(LocalDateTime.now()), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", "")
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


    def "we can cancel a payment"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        paymentConsentRequest.data.payment.date(null)

        paymentConsentRequest.data.payment.schedule(TestRequestDataFactory.scheduleDailyV4())

        def responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        def updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)

        def consentId = responseConsent.getData().getConsentId()

        paymentConsentService.updateConsent(consentId, clientId, updatePaymentConsent)
        def listOfPaymentsData = new ArrayList<CreatePixPaymentDataV4>()
        for (i in 0..<7) {
            listOfPaymentsData.add(TestRequestDataFactory.createPixPaymentDataV4(consentId, EndToEndIdHelper.generateEndToEndIdWithDate(LocalDateTime.now().plusDays(i)), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", ""))
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

            def updatedPayment = pixPaymentRepository.findByPaymentId(createdPaymentId).orElse(null)
            assert updatedPayment != null
            assert updatedPayment.getStatus() == EnumPaymentStatusTypeV2.CANC.toString()
        }
    }

    def "we cant cancel a payment if we have not created it"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        paymentConsentRequest.data.payment.date(null)

        paymentConsentRequest.data.payment.schedule(TestRequestDataFactory.scheduleDailyV4())

        def responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        def updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)

        def consentId = responseConsent.getData().getConsentId()

        paymentConsentService.updateConsent(consentId, clientId, updatePaymentConsent)
        def listOfPaymentsData = new ArrayList<CreatePixPaymentDataV4>()
        for (i in 0..<7) {
            listOfPaymentsData.add(TestRequestDataFactory.createPixPaymentDataV4(consentId, EndToEndIdHelper.generateEndToEndIdWithDate(LocalDateTime.now().plusDays(i)), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", ""))
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
                LocalDate.now(), "BRL", "100")
        paymentConsentRequest.data.payment.date(null)

        paymentConsentRequest.data.payment.schedule(TestRequestDataFactory.scheduleDailyV4())

        def responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        def updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)

        def consentId = responseConsent.getData().getConsentId()

        paymentConsentService.updateConsent(consentId, clientId, updatePaymentConsent)
        def listOfPaymentsData = new ArrayList<CreatePixPaymentDataV4>()
        for (i in 0..<7) {
            listOfPaymentsData.add(TestRequestDataFactory.createPixPaymentDataV4(consentId, EndToEndIdHelper.generateEndToEndIdWithDate(LocalDateTime.now().plusDays(i)), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", ""))
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

    def "we can cancel a payment by paymentId"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        paymentConsentRequest.data.payment.date(null)

        paymentConsentRequest.data.payment.schedule(TestRequestDataFactory.scheduleDailyV4())

        def responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        def updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)

        def consentId = responseConsent.getData().getConsentId()

        paymentConsentService.updateConsent(consentId, clientId, updatePaymentConsent)
        def listOfPaymentsData = new ArrayList<CreatePixPaymentDataV4>()
        for (i in 0..<7) {
            listOfPaymentsData.add(TestRequestDataFactory.createPixPaymentDataV4(consentId, EndToEndIdHelper.generateEndToEndIdWithDate(LocalDateTime.now().plusDays(i)), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", ""))
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
    }

    def "we can't cancel a payment by paymentId if the stauts is not SCHD or PDNG"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        paymentConsentRequest.data.payment.date(null)

        paymentConsentRequest.data.payment.schedule(TestRequestDataFactory.scheduleDailyV4())

        def responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        def updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)

        def consentId = responseConsent.getData().getConsentId()

        paymentConsentService.updateConsent(consentId, clientId, updatePaymentConsent)
        def listOfPaymentsData = new ArrayList<CreatePixPaymentDataV4>()
        for (i in 0..<7) {
            listOfPaymentsData.add(TestRequestDataFactory.createPixPaymentDataV4(consentId, EndToEndIdHelper.generateEndToEndIdWithDate(LocalDateTime.now().plusDays(i)), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", ""))
        }
        def createPaymentResponse = paymentsService.createPaymentV4(consentId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), new CreatePixPaymentV4().data(listOfPaymentsData), clientId)

        def createdPaymentId = createPaymentResponse.getData().stream().findFirst().get().getPaymentId()

        def paymentConsentPatchRequest = TestRequestDataFactory.createPatchPixConsentV4Request(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())

        when:
        def response = paymentsService.patchPaymentV4(createdPaymentId, paymentConsentPatchRequest)

        then:
        def e = thrown(HttpStatusException)
        assert e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        assert e.getMessage() ==String.format("%s: payment not on allowed status SCHD or PDNG",
                ErrorCodesEnumV2.PAGAMENTO_NAO_PERMITE_CANCELAMENTO.name())
    }

    def "we can't cancel a payment by paymentId if if cancellation document rel is not CPF"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequestV4("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        paymentConsentRequest.data.payment.date(null)

        paymentConsentRequest.data.payment.schedule(TestRequestDataFactory.scheduleDailyV4())

        def responseConsent = paymentConsentService.createConsentV4(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        def updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)

        def consentId = responseConsent.getData().getConsentId()

        paymentConsentService.updateConsent(consentId, clientId, updatePaymentConsent)
        def listOfPaymentsData = new ArrayList<CreatePixPaymentDataV4>()
        for (i in 0..<7) {
            listOfPaymentsData.add(TestRequestDataFactory.createPixPaymentDataV4(consentId, EndToEndIdHelper.generateEndToEndIdWithDate(LocalDateTime.now().plusDays(i)), "CRAC1", "CRISS1", "CRISPB1", EnumAccountPaymentsType.CACC, EnumLocalInstrument.DICT, "100", "BRL", "proxy", "", "cnpj", ""))
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
