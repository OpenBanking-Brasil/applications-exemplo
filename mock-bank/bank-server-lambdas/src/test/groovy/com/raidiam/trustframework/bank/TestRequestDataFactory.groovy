package com.raidiam.trustframework.bank

import com.raidiam.trustframework.bank.enums.ContractStatusEnum
import com.raidiam.trustframework.bank.utils.BankLambdaUtils
import com.raidiam.trustframework.mockbank.models.generated.*
import org.apache.commons.lang3.RandomStringUtils

import java.time.LocalDate
import java.time.OffsetDateTime

import static com.raidiam.trustframework.mockbank.models.generated.ScheduleWeeklyWeekly.DayOfWeekEnum

class TestRequestDataFactory {

    private static final Random random = new Random()

    static AccountBalancesDataV2 anAccountBalances() {

        AccountBalancesDataV2 accountReq = new AccountBalancesDataV2()
        accountReq.setAvailableAmount(new AccountBalancesDataAvailableAmountV2().currency("BRL").amount("3"))
        accountReq.setBlockedAmount(new AccountBalancesDataBlockedAmountV2().currency("BRL").amount("4"))
        accountReq.setAutomaticallyInvestedAmount(new AccountBalancesDataAutomaticallyInvestedAmountV2().currency("BRL").amount("5"))
        accountReq
    }

    static createPaymentConsentRequest(String businessIdentityDocumentIdentification,
                                       String businessIdentityDocumentREL,
                                       String cpfCnpj,
                                       String name,
                                       EnumCreditorPersonType personType,
                                       EnumAccountPaymentsType accountType,
                                       String debtorIspb,
                                       String debtorIssuer,
                                       String debtorAccountNumber,
                                       String loggedUserRel,
                                       String loggedUserIdentification,
                                       String paymentConsentType,
                                       LocalDate paymentConsentDate,
                                       String currency,
                                       String amount) {
        CreatePaymentConsentData paymentConsentRequestData = new CreatePaymentConsentData()
                .businessEntity(
                        new BusinessEntity().document(
                                new BusinessEntityDocument().identification(businessIdentityDocumentIdentification)
                                        .rel(businessIdentityDocumentREL)))
                .creditor(new Identification().cpfCnpj(cpfCnpj).name(name).personType(personType.toString()))
                .debtorAccount(new DebtorAccount().accountType(accountType).ispb(debtorIspb).issuer(debtorIssuer).number(debtorAccountNumber))
                .loggedUser(new LoggedUser().document(new Document().rel(loggedUserRel).identification(loggedUserIdentification)))
                .payment(new PaymentConsent().type(paymentConsentType).date(paymentConsentDate).currency(currency).amount(amount).details(
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
        return new CreatePaymentConsent().data(paymentConsentRequestData)
    }

    static createPatchRecurringPaymentConsentRequestV1DataChange() {
        return new PatchRecurringConsentV1()
                .data(new PatchRecurringConsentV1Data()
                        .startDateTime(OffsetDateTime.now()))
    }

    static createPatchRecurringPaymentConsentRequestV1Rejected() {
        return new PatchRecurringConsentV1()
                .data(new PatchRecurringConsentV1Data()
                        .status(PatchRecurringConsentV1Data.StatusEnum.REJECTED)
                        .rejection(new RecurringConsentRejectionV1()
                                .rejectedBy(EnumRecurringConsentRejectRevokedBy.DETENTORA)
                                .rejectedFrom(EnumRecurringConsentRejectRevokedFrom.DETENTORA)
                                .reason(new ConsentRejectionReason()
                                        .code(EnumConsentRejectionReasonType.CONTA_NAO_PERMITE_PAGAMENTO)
                                        .detail("Detail"))))
    }

    static createPatchRecurringPaymentConsentRequestV1Revoked() {
        return new PatchRecurringConsentV1()
                .data(new PatchRecurringConsentV1Data()
                        .status(PatchRecurringConsentV1Data.StatusEnum.REVOKED)
                        .revocation(new RecurringConsentRevocationV1()
                                .revokedBy(EnumRecurringConsentRejectRevokedBy.DETENTORA)
                                .revokedFrom(EnumRecurringConsentRejectRevokedFrom.DETENTORA)
                                .reason(new RecurringConsentRevokedReasonV1()
                                        .code(RecurringConsentRevokedReasonV1.CodeEnum.NAO_INFORMADO)
                                        .detail("Detail"))))
    }

    static createPatchRecurringPixPaymentRequestV1Cancelled(String rel, String identification) {
        return new RecurringPatchPixPayment()
                .data(new RecurringPatchPixPaymentData()
                        .status(EnumPaymentV4CancellationStatusType.CANC)
                        .cancellation(new PixPaymentCancellation()
                                .cancelledBy(new CancelledBy()
                                        .document(new Document()
                                                .rel(rel)
                                                .identification(identification)))))
    }

    static createRecurringPaymentConsentRequestV1(String loggedUserRel, String loggedUserIdentification) {

        def creditors = new Creditors()
        creditors.addAll(List.of(
                new Identification()
                        .cpfCnpj("12345678901")
                        .name("name")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
        ))

        return new CreateRecurringConsentV1()
                .data(new CreateRecurringConsentV1Data()
                        .loggedUser(new LoggedUser()
                                .document(new Document()
                                        .rel(loggedUserRel)
                                        .identification(loggedUserIdentification)))
                        .businessEntity(new BusinessEntity()
                                .document(new BusinessEntityDocument()
                                        .identification("12345678901234")
                                        .rel("CNPJ")))
                        .creditors(creditors)
                        .startDateTime(OffsetDateTime.now())
                        .expirationDateTime(OffsetDateTime.now().plusMinutes(5)))

    }

    static createRecurringPaymentConsentRequestV1WithSweeping(String loggedUserRel, String loggedUserIdentification) {
        def req = createRecurringPaymentConsentRequestV1(loggedUserRel, loggedUserIdentification)
        req.getData().setRecurringConfiguration(new AllOfCreateRecurringConsentV1DataRecurringConfiguration()
                .sweeping(new SweepingSweeping()
                        .totalAllowedAmount("100.00")
                        .transactionLimit("100.00")
                        .periodicLimits(new PeriodicLimits()
                                .day(new Day()
                                        .quantityLimit(2)
                                        .transactionLimit("100.00"))
                                .week(new Week()
                                        .quantityLimit(2)
                                        .transactionLimit("100.00"))
                                .month(new Month()
                                        .quantityLimit(2)
                                        .transactionLimit("100.00"))
                                .year(new Year()
                                        .quantityLimit(2)
                                        .transactionLimit("100.00")))))
        return req
    }

    static createRecurringPaymentConsentRequestV1WithoutSweeping(String loggedUserRel, String loggedUserIdentification) {
        def req = createRecurringPaymentConsentRequestV1(loggedUserRel, loggedUserIdentification)
        req.getData().setRecurringConfiguration(new AllOfCreateRecurringConsentV1DataRecurringConfiguration()
                .sweeping(new SweepingSweeping()))
        return req
    }

    static createRecurringPixPayment(String recurringConsentId) {
        return new CreateRecurringPixPaymentV1()
                .data(new CreateRecurringPixPaymentV1Data()
                .authorisationFlow(EnumAuthorisationFlow.HYBRID_FLOW)
                .date(LocalDate.now())
                .recurringConsentId(recurringConsentId)
                .cnpjInitiator(RandomStringUtils.randomNumeric(14))
                .endToEndId(EndToEndIdHelper.generateRandomEndToEndId())
                .localInstrument(EnumLocalInstrument.DICT)
                .creditorAccount(new CreditorAccount().number("1234567890").issuer("1774").ispb("12345678").accountType(EnumAccountPaymentsType.CACC))
                .payment(new PaymentPix().amount("50.00").currency("BRL"))
                .proxy("proxy")
                .ibgeTownCode("5300108")
                .remittanceInformation("remittanceInformation")
                .transactionIdentification("transactionIdentification")
                .document(new Document().rel("CPF").identification("1234567890")))
    }


    static createRecurringPaymentConsentRequestV1WithVrp(String loggedUserRel, String loggedUserIdentification) {
        def req = createRecurringPaymentConsentRequestV1(loggedUserRel, loggedUserIdentification)
        req.getData().setRecurringConfiguration(new AllOfCreateRecurringConsentV1DataRecurringConfiguration()
                .vrp(new VrpVrp()
                        .transactionLimit("100.00")
                        .globalLimits(new VrpVrpGlobalLimits()
                                .transactionLimit("100.00")
                                .quantityLimit(10))
                        .periodicLimits(new PeriodicLimits()
                                .day(new Day()
                                        .quantityLimit(10)
                                        .transactionLimit("100.00"))
                                .week(new Week()
                                        .quantityLimit(10)
                                        .transactionLimit("100.00"))
                                .month(new Month()
                                        .quantityLimit(10)
                                        .transactionLimit("100.00"))
                                .year(new Year()
                                        .quantityLimit(10)
                                        .transactionLimit("100.00")))))
        return req
    }


    static createRecurringPaymentConsentRequestV1WithAutomatic(String loggedUserRel, String loggedUserIdentification) {
        def req = createRecurringPaymentConsentRequestV1(loggedUserRel, loggedUserIdentification)
        req.getData().setRecurringConfiguration(new AllOfCreateRecurringConsentV1DataRecurringConfiguration()
                .automatic(new AutomaticAutomatic()
                        .contractId("id")
                        .amount("100.00")
                        .transactionLimit("100.00")
                        .period(AutomaticAutomatic.PeriodEnum.ANUAL)
                        .dayOfMonth(10)
                        .dayOfWeek(AutomaticAutomatic.DayOfWeekEnum.QUARTA_FEIRA)
                        .month(AutomaticAutomatic.MonthEnum.ABRIL)
                        .contractDebtor(new ContractDebtor()
                                .name("name")
                                .document(new ContractDebtorDocument()
                                        .identification("12345678901234")
                                        .rel("CNPJ")))
                        .immediatePayment(new ImmediatePayment()
                                .type(EnumPaymentType.PIX)
                                .date(LocalDate.now())
                                .currency("GBP")
                                .amount("100.00")
                                .creditorAccount(new PostCreditorAccount()
                                        .ispb("ispb")
                                        .issuer("issuer")
                                        .number("number")
                                        .accountType(EnumAccountTypeConsents.CACC)))) as AllOfCreateRecurringConsentV1DataRecurringConfiguration)
        return req
    }

    static createPaymentConsentRequestV4(String businessIdentityDocumentIdentification,
                                         String businessIdentityDocumentREL,
                                         String cpfCnpj,
                                         String name,
                                         EnumCreditorPersonType personType,
                                         EnumAccountPaymentsType accountType,
                                         String debtorIspb,
                                         String debtorIssuer,
                                         String debtorAccountNumber,
                                         String loggedUserRel,
                                         String loggedUserIdentification,
                                         String paymentConsentType,
                                         LocalDate paymentConsentDate,
                                         String currency,
                                         String amount) {
        CreatePaymentConsentV4Data paymentConsentRequestData = new CreatePaymentConsentV4Data()
                .businessEntity(
                        new BusinessEntity().document(
                                new BusinessEntityDocument().identification(businessIdentityDocumentIdentification)
                                        .rel(businessIdentityDocumentREL)))
                .creditor(new Identification().cpfCnpj(cpfCnpj).name(name).personType(personType.toString()))
                .debtorAccount(new DebtorAccount().accountType(accountType).ispb(debtorIspb).issuer(debtorIssuer).number(debtorAccountNumber))
                .loggedUser(new LoggedUser().document(new Document().rel(loggedUserRel).identification(loggedUserIdentification)))
                .payment(new PaymentConsentV4Payment().type(EnumPaymentType.fromValue(paymentConsentType)).date(paymentConsentDate).currency(currency).amount(amount).details(
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
        return new CreatePaymentConsentV4().data(paymentConsentRequestData)
    }

    static createPaymentConsentRequestV4WithScheduled(String businessIdentityDocumentIdentification,
                                                      String businessIdentityDocumentREL,
                                                      String cpfCnpj,
                                                      String name,
                                                      EnumCreditorPersonType personType,
                                                      EnumAccountPaymentsType accountType,
                                                      String debtorIspb,
                                                      String debtorIssuer,
                                                      String debtorAccountNumber,
                                                      String loggedUserRel,
                                                      String loggedUserIdentification,
                                                      String paymentConsentType,
                                                      String currency,
                                                      String amount) {
        CreatePaymentConsentV4Data paymentConsentRequestData = new CreatePaymentConsentV4Data()
                .businessEntity(
                        new BusinessEntity().document(
                                new BusinessEntityDocument().identification(businessIdentityDocumentIdentification)
                                        .rel(businessIdentityDocumentREL)))
                .creditor(new Identification().cpfCnpj(cpfCnpj).name(name).personType(personType.toString()))
                .debtorAccount(new DebtorAccount().accountType(accountType).ispb(debtorIspb).issuer(debtorIssuer).number(debtorAccountNumber))
                .loggedUser(new LoggedUser().document(new Document().rel(loggedUserRel).identification(loggedUserIdentification)))
                .payment(new PaymentConsentV4Payment().type(EnumPaymentType.fromValue(paymentConsentType)).currency(currency).amount(amount).details(
                        new Details()
                                .localInstrument(EnumLocalInstrument.DICT)
                                .proxy("proxy")
                                .creditorAccount(new CreditorAccount()
                                        .number("123")
                                        .ispb("ispb")
                                        .accountType(EnumAccountPaymentsType.CACC)
                                        .issuer("issuer")
                                ))
                        .schedule(new AllOfPaymentConsentV4PaymentSchedule()
                                .daily(new ScheduleDailyDaily()
                                        .startDate(LocalDate.now()).quantity(2)))
                )

        return new CreatePaymentConsentV4().data(paymentConsentRequestData)
    }

    static createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum status, boolean withDebtor) {
        return new UpdatePaymentConsent().data(new UpdatePaymentConsentData().status(status)
                .debtorAccount(withDebtor ? new DebtorAccount().accountType(EnumAccountPaymentsType.SLRY).ispb("12341234").issuer("1234").number("1234567890") : null))
    }

    static createPaymentRequest(String creditorAcNumber,
                                String creditorIssuer,
                                String creditorIspb,
                                EnumAccountPaymentsType creditorAcType,
                                EnumLocalInstrument localInstrument,
                                String amount,
                                String currency,
                                String proxy,
                                String qrcode,
                                String cnpjInitiator,
                                String remittanceInformation,
                                String transactionIdentification = null,
                                String endToEndId = EndToEndIdHelper.generateRandomEndToEndId()) {

        CreatePixPaymentData createPixPaymentData = new CreatePixPaymentData()
                .creditorAccount(new CreditorAccount().number(creditorAcNumber).issuer(creditorIssuer).ispb(creditorIspb).accountType(creditorAcType))
                .localInstrument(localInstrument)
                .payment(new PaymentPix().amount(amount).currency(currency))
                .proxy(proxy)
                .qrCode(qrcode)
                .cnpjInitiator(cnpjInitiator)
                .remittanceInformation(remittanceInformation)
                .transactionIdentification(transactionIdentification)
                .endToEndId(endToEndId)
        return new CreatePixPayment().data(createPixPaymentData)
    }

    static createPaymentRequestV2(String creditorAcNumber,
                                  String creditorIssuer,
                                  String creditorIspb,
                                  EnumAccountPaymentsType creditorAcType,
                                  EnumLocalInstrument localInstrument,
                                  String amount,
                                  String currency,
                                  String proxy,
                                  String qrcode,
                                  String cnpjInitiator,
                                  String remittanceInformation,
                                  String transactionIdentification = null,
                                  String endToEndId = EndToEndIdHelper.generateRandomEndToEndId()) {

        CreatePixPaymentDataV2 createPixPaymentData = new CreatePixPaymentDataV2()
                .creditorAccount(new CreditorAccount().number(creditorAcNumber).issuer(creditorIssuer).ispb(creditorIspb).accountType(creditorAcType))
                .localInstrument(localInstrument)
                .payment(new PaymentPix().amount(amount).currency(currency))
                .proxy(proxy)
                .qrCode(qrcode)
                .cnpjInitiator(cnpjInitiator)
                .remittanceInformation(remittanceInformation)
                .transactionIdentification(transactionIdentification)
                .endToEndId(endToEndId)
        return new CreatePixPaymentV2().data(createPixPaymentData)
    }

    static createPaymentRequestV3(String creditorAcNumber,
                                  String creditorIssuer,
                                  String creditorIspb,
                                  EnumAccountPaymentsType creditorAcType,
                                  EnumLocalInstrument localInstrument,
                                  String amount,
                                  String currency,
                                  String proxy,
                                  String qrcode,
                                  String cnpjInitiator,
                                  String remittanceInformation,
                                  String transactionIdentification = null,
                                  String endToEndId = EndToEndIdHelper.generateRandomEndToEndId()) {

        CreatePixPaymentDataV3 createPixPaymentData = new CreatePixPaymentDataV3()
                .creditorAccount(new CreditorAccount().number(creditorAcNumber).issuer(creditorIssuer).ispb(creditorIspb).accountType(creditorAcType))
                .localInstrument(localInstrument)
                .payment(new PaymentPix().amount(amount).currency(currency))
                .proxy(proxy)
                .qrCode(qrcode)
                .cnpjInitiator(cnpjInitiator)
                .remittanceInformation(remittanceInformation)
                .transactionIdentification(transactionIdentification)
                .endToEndId(endToEndId)
                .authorisationFlow(EnumAuthorisationFlow.HYBRID_FLOW)
        return new CreatePixPaymentV3().data(createPixPaymentData)
    }

    static createPaymentRequestV4(String consentId, String creditorAcNumber,
                                  String creditorIssuer,
                                  String creditorIspb,
                                  EnumAccountPaymentsType creditorAcType,
                                  EnumLocalInstrument localInstrument,
                                  String amount,
                                  String currency,
                                  String proxy,
                                  String qrcode,
                                  String cnpjInitiator,
                                  String remittanceInformation,
                                  String transactionIdentification = null,
                                  String endToEndId = EndToEndIdHelper.generateRandomEndToEndId()) {

        CreatePixPaymentDataV4 createPixPaymentData = new CreatePixPaymentDataV4()
                .consentId(consentId)
                .creditorAccount(new CreditorAccount().number(creditorAcNumber).issuer(creditorIssuer).ispb(creditorIspb).accountType(creditorAcType))
                .localInstrument(localInstrument)
                .payment(new PaymentPix().amount(amount).currency(currency))
                .proxy(proxy)
                .qrCode(qrcode)
                .cnpjInitiator(cnpjInitiator)
                .remittanceInformation(remittanceInformation)
                .transactionIdentification(transactionIdentification)
                .endToEndId(endToEndId)
                .authorisationFlow(EnumAuthorisationFlow.HYBRID_FLOW)
        return new CreatePixPaymentV4().data(List.of(createPixPaymentData))
    }

    static createPixPaymentDataV4(String consentId,
                                  String end2endId,
                                  String creditorAcNumber,
                                  String creditorIssuer,
                                  String creditorIspb,
                                  EnumAccountPaymentsType creditorAcType,
                                  EnumLocalInstrument localInstrument,
                                  String amount,
                                  String currency,
                                  String proxy,
                                  String qrcode,
                                  String cnpjInitiator,
                                  String remittanceInformation,
                                  String transactionIdentification = null) {
        return new CreatePixPaymentDataV4()
                .consentId(consentId)
                .creditorAccount(new CreditorAccount().number(creditorAcNumber).issuer(creditorIssuer).ispb(creditorIspb).accountType(creditorAcType))
                .localInstrument(localInstrument)
                .payment(new PaymentPix().amount(amount).currency(currency))
                .proxy(proxy)
                .qrCode(qrcode)
                .cnpjInitiator(cnpjInitiator)
                .remittanceInformation(remittanceInformation)
                .transactionIdentification(transactionIdentification)
                .endToEndId(end2endId)
                .authorisationFlow(EnumAuthorisationFlow.HYBRID_FLOW)
    }

    static createPaymentRequestV4WithScheduled(String consentId,
                                               String creditorAcNumber,
                                               String creditorIssuer,
                                               String creditorIspb,
                                               EnumAccountPaymentsType creditorAcType,
                                               EnumLocalInstrument localInstrument,
                                               String amount,
                                               String currency,
                                               String proxy,
                                               String qrcode,
                                               String cnpjInitiator,
                                               String remittanceInformation,
                                               String transactionIdentification = null,
                                               EnumAuthorisationFlow authorisationFlow = EnumAuthorisationFlow.HYBRID_FLOW,
                                               String endToEndId = EndToEndIdHelper.generateRandomEndToEndIdPlusOneDay()
    ) {

        CreatePixPaymentDataV4 createPixPaymentData1 = new CreatePixPaymentDataV4()
                .consentId(consentId)
                .creditorAccount(new CreditorAccount().number(creditorAcNumber).issuer(creditorIssuer).ispb(creditorIspb).accountType(creditorAcType))
                .localInstrument(localInstrument)
                .payment(new PaymentPix().amount(amount).currency(currency))
                .proxy(proxy)
                .qrCode(qrcode)
                .cnpjInitiator(cnpjInitiator)
                .remittanceInformation(remittanceInformation)
                .transactionIdentification(transactionIdentification)
                .endToEndId(EndToEndIdHelper.generateRandomEndToEndIdNow())
                .authorisationFlow(authorisationFlow)

        CreatePixPaymentDataV4 createPixPaymentData2 = new CreatePixPaymentDataV4()
                .consentId(consentId)
                .creditorAccount(new CreditorAccount().number(creditorAcNumber).issuer(creditorIssuer).ispb(creditorIspb).accountType(creditorAcType))
                .localInstrument(localInstrument)
                .payment(new PaymentPix().amount(amount).currency(currency))
                .proxy(proxy)
                .qrCode(qrcode)
                .cnpjInitiator(cnpjInitiator)
                .remittanceInformation(remittanceInformation)
                .transactionIdentification(transactionIdentification)
                .endToEndId(endToEndId)
                .authorisationFlow(authorisationFlow)


        return new CreatePixPaymentV4().data(List.of(createPixPaymentData1, createPixPaymentData2))
    }

    static createPaymentUpdateRequest(EnumPaymentStatusType status) {
        UpdatePixPaymentData updatePixPaymentData = new UpdatePixPaymentData().status(status)
        return new UpdatePixPayment().data(updatePixPaymentData)
    }

    static createPaymentConsentResponse(String consentId,
                                        OffsetDateTime creation,
                                        OffsetDateTime expiration,
                                        OffsetDateTime update,
                                        ResponsePaymentConsentData.StatusEnum status,
                                        String loggedUserId,
                                        String loggedUserRel,
                                        String businessId,
                                        String businessRel,
                                        EnumCreditorPersonType personType,
                                        String cpf,
                                        String name,
                                        String currency,
                                        String amount,
                                        LocalDate paymentDate,
                                        String paymentType,
                                        String ispb,
                                        String issuer,
                                        String number,
                                        EnumAccountPaymentsType accountType) {
        return new ResponsePaymentConsent().data(new ResponsePaymentConsentData()
                .consentId(consentId)
                .creationDateTime(creation)
                .expirationDateTime(expiration)
                .statusUpdateDateTime(update)
                .status(status)
                .loggedUser(new LoggedUser().document(new Document().identification(loggedUserId).rel(loggedUserRel)))
                .businessEntity(new BusinessEntity().document(new BusinessEntityDocument().identification(businessId).rel(businessRel)))
                .creditor(new Identification().personType(personType.toString()).cpfCnpj(cpf).name(name))
                .payment(new PaymentConsent().currency(currency).amount(amount).date(paymentDate).type(paymentType))
                .debtorAccount(new DebtorAccount().ispb(ispb).issuer(issuer).number(number).accountType(accountType)))
    }

    static getEnrollmentResponse(String enrollmentId,
                                 OffsetDateTime creation,
                                 OffsetDateTime expiration,
                                 OffsetDateTime update,
                                 EnumEnrollmentStatus status,
                                 String loggedUserId,
                                 String loggedUserRel,
                                 String businessId,
                                 String businessRel,
                                 String ispb,
                                 String issuer,
                                 String number,
                                 String dailyLimit,
                                 String transactionLimit,
                                 EnumAccountPaymentsType accountType) {
        return new ResponseEnrollment().data(new ResponseEnrollmentData()
                .enrollmentId(enrollmentId)
                .creationDateTime(creation)
                .expirationDateTime(expiration)
                .statusUpdateDateTime(update)
                .status(status)
                .loggedUser(new LoggedUser().document(new Document().identification(loggedUserId).rel(loggedUserRel)))
                .businessEntity(new BusinessEntity().document(new BusinessEntityDocument().identification(businessId).rel(businessRel)))
                .debtorAccount(new DebtorAccount().ispb(ispb).issuer(issuer).number(number).accountType(accountType))
                .dailyLimit(dailyLimit)
                .transactionLimit(transactionLimit)
        )
    }

    static createEnrollmentResponse(String enrollmentId,
                                    OffsetDateTime creation,
                                    OffsetDateTime expiration,
                                    OffsetDateTime update,
                                    EnumEnrollmentStatus status,
                                    String loggedUserId,
                                    String loggedUserRel,
                                    String businessId,
                                    String businessRel,
                                    String ispb,
                                    String issuer,
                                    String number,
                                    EnumAccountPaymentsType accountType) {
        return new ResponseCreateEnrollment().data(new ResponseCreateEnrollmentData()
                .enrollmentId(enrollmentId)
                .creationDateTime(creation)
                .expirationDateTime(expiration)
                .statusUpdateDateTime(update)
                .status(status)
                .loggedUser(new LoggedUser().document(new Document().identification(loggedUserId).rel(loggedUserRel)))
                .businessEntity(new BusinessEntity().document(new BusinessEntityDocument().identification(businessId).rel(businessRel)))
                .debtorAccount(new DebtorAccount().ispb(ispb).issuer(issuer).number(number).accountType(accountType)))
    }


    static createEnrollmentFidoRegistrationOptionResponse(String enrollmentId) {
        return new EnrollmentFidoRegistrationOptions().data(new EnrollmentFidoRegistrationOptionsData()
                .enrollmentId(enrollmentId)
                .challenge(RandomStringUtils.randomAlphanumeric(20).getBytes()))
    }

    static createEnrollmentFidoSignOptionResponse() {
        return new EnrollmentFidoSignOptions().data(new EnrollmentFidoSignOptionsData()
                .challenge(RandomStringUtils.randomAlphanumeric(20).getBytes()))
    }

    static createPaymentConsentResponseV2(String consentId,
                                          OffsetDateTime creation,
                                          OffsetDateTime expiration,
                                          OffsetDateTime update,
                                          ResponsePaymentConsentDataV2.StatusEnum status,
                                          String loggedUserId,
                                          String loggedUserRel,
                                          String businessId,
                                          String businessRel,
                                          EnumCreditorPersonType personType,
                                          String cpf,
                                          String name,
                                          String currency,
                                          String amount,
                                          LocalDate paymentDate,
                                          String paymentType,
                                          String ispb,
                                          String issuer,
                                          String number,
                                          EnumAccountPaymentsType accountType) {
        return new ResponsePaymentConsentV2().data(new ResponsePaymentConsentDataV2()
                .consentId(consentId)
                .creationDateTime(creation)
                .expirationDateTime(expiration)
                .statusUpdateDateTime(update)
                .status(status)
                .loggedUser(new LoggedUser().document(new Document().identification(loggedUserId).rel(loggedUserRel)))
                .businessEntity(new BusinessEntity().document(new BusinessEntityDocument().identification(businessId).rel(businessRel)))
                .creditor(new Identification().personType(personType.toString()).cpfCnpj(cpf).name(name))
                .payment(new PaymentConsent().currency(currency).amount(amount).date(paymentDate).type(paymentType))
                .debtorAccount(new DebtorAccount().ispb(ispb).issuer(issuer).number(number).accountType(accountType)))
    }

    static createPaymentConsentResponseV3(String consentId,
                                          OffsetDateTime creation,
                                          OffsetDateTime expiration,
                                          OffsetDateTime update,
                                          ResponsePaymentConsentDataV3.StatusEnum status,
                                          String loggedUserId,
                                          String loggedUserRel,
                                          String businessId,
                                          String businessRel,
                                          EnumCreditorPersonType personType,
                                          String cpf,
                                          String name,
                                          String currency,
                                          String amount,
                                          LocalDate paymentDate,
                                          String paymentType,
                                          String ispb,
                                          String issuer,
                                          String number,
                                          EnumAccountPaymentsType accountType) {
        return new ResponsePaymentConsentV3().data(new ResponsePaymentConsentDataV3()
                .consentId(consentId)
                .creationDateTime(creation)
                .expirationDateTime(expiration)
                .statusUpdateDateTime(update)
                .status(status)
                .loggedUser(new LoggedUser().document(new Document().identification(loggedUserId).rel(loggedUserRel)))
                .businessEntity(new BusinessEntity().document(new BusinessEntityDocument().identification(businessId).rel(businessRel)))
                .creditor(new Identification().personType(personType.toString()).cpfCnpj(cpf).name(name))
                .payment(new PaymentConsent().currency(currency).amount(amount).date(paymentDate).type(paymentType))
                .debtorAccount(new DebtorAccount().ispb(ispb).issuer(issuer).number(number).accountType(accountType)))
    }

    static createGetPaymentConsentResponseV4(String consentId,
                                             OffsetDateTime creation,
                                             OffsetDateTime expiration,
                                             OffsetDateTime update,
                                             EnumAuthorisationStatusType status,
                                             String loggedUserId,
                                             String loggedUserRel,
                                             String businessId,
                                             String businessRel,
                                             EnumCreditorPersonType personType,
                                             String cpf,
                                             String name,
                                             String currency,
                                             String amount,
                                             LocalDate paymentDate,
                                             String paymentType,
                                             String ispb,
                                             String issuer,
                                             String number,
                                             EnumAccountPaymentsType accountType) {
        return new ResponsePaymentConsentV4().data(new ResponsePaymentConsentV4Data()
                .consentId(consentId)
                .creationDateTime(creation)
                .expirationDateTime(expiration)
                .statusUpdateDateTime(update)
                .status(status)
                .loggedUser(new LoggedUser().document(new Document().identification(loggedUserId).rel(loggedUserRel)))
                .businessEntity(new BusinessEntity().document(new BusinessEntityDocument().identification(businessId).rel(businessRel)))
                .creditor(new Identification().personType(personType.toString()).cpfCnpj(cpf).name(name))
                .payment(new PaymentConsentV4Payment().currency(currency).amount(amount).date(paymentDate).type(EnumPaymentType.fromValue(paymentType)))
                .debtorAccount(new DebtorAccount().ispb(ispb).issuer(issuer).number(number).accountType(accountType)))
    }

    static createPaymentConsentResponseV4(String consentId,
                                          OffsetDateTime creation,
                                          OffsetDateTime expiration,
                                          OffsetDateTime update,
                                          EnumAuthorisationStatusType status,
                                          String loggedUserId,
                                          String loggedUserRel,
                                          String businessId,
                                          String businessRel,
                                          EnumCreditorPersonType personType,
                                          String cpf,
                                          String name,
                                          String currency,
                                          String amount,
                                          LocalDate paymentDate,
                                          EnumPaymentType paymentType,
                                          String ispb,
                                          String issuer,
                                          String number,
                                          EnumAccountPaymentsType accountType) {
        return new ResponseCreatePaymentConsentV4().data(new ResponseCreatePaymentConsentV4Data()
                .consentId(consentId)
                .creationDateTime(creation)
                .expirationDateTime(expiration)
                .statusUpdateDateTime(update)
                .status(status)
                .loggedUser(new LoggedUser().document(new Document().identification(loggedUserId).rel(loggedUserRel)))
                .businessEntity(new BusinessEntity().document(new BusinessEntityDocument().identification(businessId).rel(businessRel)))
                .creditor(new Identification().personType(personType.toString()).cpfCnpj(cpf).name(name))
                .payment(new PaymentConsentV4Payment()
                        .amount(amount)
                        .date(paymentDate)
                        .currency(currency)
                        .type(paymentType)
                )
                .debtorAccount(new DebtorAccount().ispb(ispb).issuer(issuer).number(number).accountType(accountType)))
    }

    static createPaymentConsentResponseFull(String consentId,
                                            OffsetDateTime creation,
                                            OffsetDateTime expiration,
                                            OffsetDateTime update,
                                            ResponsePaymentConsentData.StatusEnum status,
                                            String loggedUserId,
                                            String loggedUserRel,
                                            String businessId,
                                            String businessRel,
                                            EnumCreditorPersonType personType,
                                            String cpf,
                                            String name,
                                            String currency,
                                            String amount,
                                            LocalDate paymentDate,
                                            String paymentType,
                                            String ispb,
                                            String issuer,
                                            String number,
                                            EnumAccountPaymentsType accountType) {
        return new ResponsePaymentConsentFull().data(new ResponsePaymentConsentFullData()
                .consentId(consentId)
                .creationDateTime(creation)
                .expirationDateTime(expiration)
                .statusUpdateDateTime(update)
                .sub("dude@mail.place")
                .loggedUser(new LoggedUser().document(new Document().identification(loggedUserId).rel(loggedUserRel)))
                .businessEntity(new BusinessEntity().document(new BusinessEntityDocument().identification(businessId).rel(businessRel)))
                .creditor(new Identification().personType(personType.toString()).cpfCnpj(cpf).name(name))
                .payment(new PaymentConsent().currency(currency).amount(amount).date(paymentDate).type(paymentType))
                .debtorAccount(new DebtorAccount().ispb(ispb).issuer(issuer).number(number).accountType(accountType)))
    }


    static createPatchPixConsentV4Request() {
        createPatchPixConsentV4Request("test", "test")
    }

    static createPatchPixConsentV4Request(String identification, String rel) {
        new PatchPixPaymentV4()
                .data(new PatchPixPaymentV4Data()
                        .status(EnumPaymentV4CancellationStatusType.CANC)
                        .cancellation(new PixPaymentCancellation()
                                .cancelledBy(new CancelledBy()
                                        .document(new Document()
                                                .identification(identification)
                                                .rel(rel)))))
    }

    static createPatchPixConsentV4Response(String paymentId) {
        new ResponsePatchPixConsentV4()
                .data(List.of(new ResponsePatchPixConsentV4Data()
                        .paymentId(paymentId)
                        .statusUpdateDateTime(OffsetDateTime.now())))
    }

    static createPaymentConsentResponseSchedule(String consentId,
                                                OffsetDateTime creation,
                                                OffsetDateTime expiration,
                                                OffsetDateTime update,
                                                ResponsePaymentConsentData.StatusEnum status,
                                                String loggedUserId,
                                                String loggedUserRel,
                                                String businessId,
                                                String businessRel,
                                                EnumCreditorPersonType personType,
                                                String cpf,
                                                String name,
                                                String currency,
                                                String amount,
                                                LocalDate paymentDate,
                                                EnumPaymentType paymentType,
                                                String ispb,
                                                String issuer,
                                                String number,
                                                EnumAccountPaymentsType accountType) {

        return new ResponsePaymentConsent().data(new ResponsePaymentConsentData()
                .consentId(consentId)
                .creationDateTime(creation)
                .expirationDateTime(expiration)
                .statusUpdateDateTime(update)
                .status(status)
                .loggedUser(new LoggedUser().document(new Document().identification(loggedUserId).rel(loggedUserRel)))
                .businessEntity(new BusinessEntity().document(new BusinessEntityDocument().identification(businessId).rel(businessRel)))
                .creditor(new Identification().personType(personType.toString()).cpfCnpj(cpf).name(name))
                .payment(new PaymentConsent().currency(currency).amount(amount).schedule(new Schedule().single(new Single().date(paymentDate))).type(paymentType.toString()))
                .debtorAccount(new DebtorAccount().ispb(ispb).issuer(issuer).number(number).accountType(accountType)))
    }

    static createPixPaymentResponse(String paymentId,
                                    String endToEndId,
                                    String consentId,
                                    OffsetDateTime creation,
                                    OffsetDateTime statusUpdate,
                                    String proxy,
                                    EnumPaymentStatusType status,
                                    ResponsePixPaymentData.RejectionReasonEnum rejectionReason,
                                    EnumLocalInstrument localInstrument,
                                    String amount,
                                    String currency,
                                    String remittanceInformation,
                                    String ispb,
                                    String issuer,
                                    String number,
                                    EnumAccountPaymentsType accountType,
                                    String transactionIdentification = null) {
        return new ResponsePixPayment().data(new ResponsePixPaymentData()
                .paymentId(paymentId)
                .endToEndId(endToEndId)
                .consentId(consentId)
                .creationDateTime(creation)
                .statusUpdateDateTime(statusUpdate)
                .proxy(proxy)
                .status(status)
                .rejectionReason(rejectionReason)
                .localInstrument(localInstrument)
                .payment(new PaymentPix().amount(amount).currency(currency))
                .remittanceInformation(remittanceInformation)
                .creditorAccount(new CreditorAccount().ispb(ispb).issuer(issuer).number(number).accountType(accountType))
                .transactionIdentification(transactionIdentification))
    }

    static createPixPaymentResponseV2(String paymentId,
                                      String endToEndId,
                                      String consentId,
                                      OffsetDateTime creation,
                                      OffsetDateTime statusUpdate,
                                      String proxy,
                                      EnumPaymentStatusTypeV2 status,
                                      RejectionReasonV2 rejectionReason,
                                      EnumLocalInstrument localInstrument,
                                      String amount,
                                      String currency,
                                      String remittanceInformation,
                                      String ispb,
                                      String issuer,
                                      String number,
                                      EnumAccountPaymentsType accountType,
                                      String transactionIdentification = null) {
        return new ResponsePixPaymentV2().data(new ResponsePixPaymentDataV2()
                .paymentId(paymentId)
                .endToEndId(endToEndId)
                .consentId(consentId)
                .creationDateTime(creation)
                .statusUpdateDateTime(statusUpdate)
                .proxy(proxy)
                .status(status)
                .rejectionReason(rejectionReason)
                .localInstrument(localInstrument)
                .payment(new PaymentPix().amount(amount).currency(currency))
                .remittanceInformation(remittanceInformation)
                .creditorAccount(new CreditorAccount().ispb(ispb).issuer(issuer).number(number).accountType(accountType))
                .debtorAccount(new DebtorAccount().accountType(EnumAccountPaymentsType.SLRY).ispb("12341234").issuer("1234").number("1234567890"))
                .transactionIdentification(transactionIdentification))
    }

    static createPixPaymentResponseV3(String paymentId,
                                      String endToEndId,
                                      String consentId,
                                      OffsetDateTime creation,
                                      OffsetDateTime statusUpdate,
                                      String proxy,
                                      EnumPaymentStatusTypeV2 status,
                                      RejectionReasonV2 rejectionReason,
                                      EnumLocalInstrument localInstrument,
                                      String amount,
                                      String currency,
                                      String remittanceInformation,
                                      String ispb,
                                      String issuer,
                                      String number,
                                      EnumAccountPaymentsType accountType,
                                      String transactionIdentification = null) {
        return new ResponsePixPaymentV3().data(new ResponsePixPaymentDataV3()
                .paymentId(paymentId)
                .endToEndId(endToEndId)
                .consentId(consentId)
                .creationDateTime(creation)
                .statusUpdateDateTime(statusUpdate)
                .proxy(proxy)
                .status(status)
                .rejectionReason(rejectionReason)
                .localInstrument(localInstrument)
                .payment(new PaymentPix().amount(amount).currency(currency))
                .remittanceInformation(remittanceInformation)
                .creditorAccount(new CreditorAccount().ispb(ispb).issuer(issuer).number(number).accountType(accountType))
                .debtorAccount(new DebtorAccount().accountType(EnumAccountPaymentsType.SLRY).ispb("12341234").issuer("1234").number("1234567890"))
                .transactionIdentification(transactionIdentification)
                .authorisationFlow(EnumAuthorisationFlow.HYBRID_FLOW))

    }

    static createPixPaymentResponseV4(String paymentId,
                                      String endToEndId,
                                      String consentId,
                                      OffsetDateTime creation,
                                      OffsetDateTime statusUpdate,
                                      String proxy,
                                      EnumPaymentStatusTypeV2 status,
                                      RejectionReasonV2 rejectionReason,
                                      EnumLocalInstrument localInstrument,
                                      String amount,
                                      String currency,
                                      String remittanceInformation,
                                      String ispb,
                                      String issuer,
                                      String number,
                                      EnumAccountPaymentsType accountType,
                                      String transactionIdentification = null) {
        return new ResponsePixPaymentV4().data(List.of(createPixPaymentResponseDataV4(paymentId,
                endToEndId,
                consentId,
                creation,
                statusUpdate,
                proxy,
                status,
                rejectionReason,
                localInstrument,
                amount,
                currency,
                remittanceInformation,
                ispb,
                issuer,
                number,
                accountType,
                transactionIdentification)))

    }

    static createPixPaymentResponseReadV4(String paymentId,
                                          String endToEndId,
                                          String consentId,
                                          OffsetDateTime creation,
                                          OffsetDateTime statusUpdate,
                                          String proxy,
                                          EnumPaymentStatusTypeV2 status,
                                          RejectionReasonV2 rejectionReason,
                                          EnumLocalInstrument localInstrument,
                                          String amount,
                                          String currency,
                                          String remittanceInformation,
                                          String ispb,
                                          String issuer,
                                          String number,
                                          EnumAccountPaymentsType accountType,
                                          String transactionIdentification = null) {
        return new ResponsePixPaymentReadV4().data(createPixPaymentResponseDataV4(paymentId,
                endToEndId,
                consentId,
                creation,
                statusUpdate,
                proxy,
                status,
                rejectionReason,
                localInstrument,
                amount,
                currency,
                remittanceInformation,
                ispb,
                issuer,
                number,
                accountType,
                transactionIdentification))
    }

    static createPixPaymentResponseDataV4(String paymentId,
                                          String endToEndId,
                                          String consentId,
                                          OffsetDateTime creation,
                                          OffsetDateTime statusUpdate,
                                          String proxy,
                                          EnumPaymentStatusTypeV2 status,
                                          RejectionReasonV2 rejectionReason,
                                          EnumLocalInstrument localInstrument,
                                          String amount,
                                          String currency,
                                          String remittanceInformation,
                                          String ispb,
                                          String issuer,
                                          String number,
                                          EnumAccountPaymentsType accountType,
                                          String transactionIdentification = null) {
        new ResponsePixPaymentDataV4()
                .paymentId(paymentId)
                .endToEndId(endToEndId)
                .consentId(consentId)
                .creationDateTime(creation)
                .statusUpdateDateTime(statusUpdate)
                .proxy(proxy)
                .status(status)
                .rejectionReason(rejectionReason)
                .localInstrument(localInstrument)
                .payment(new PaymentPix().amount(amount).currency(currency))
                .remittanceInformation(remittanceInformation)
                .creditorAccount(new CreditorAccount().ispb(ispb).issuer(issuer).number(number).accountType(accountType))
                .debtorAccount(new DebtorAccount().accountType(EnumAccountPaymentsType.SLRY).ispb("12341234").issuer("1234").number("1234567890"))
                .transactionIdentification(transactionIdentification)
                .authorisationFlow(EnumAuthorisationFlow.HYBRID_FLOW)
    }

    static createPatchPixPaymentResponseV4(String paymentId,
                                           String endToEndId,
                                           String consentId,
                                           OffsetDateTime creation,
                                           OffsetDateTime statusUpdate,
                                           String proxy,
                                           EnumPaymentStatusTypeV2 status,
                                           RejectionReasonV2 rejectionReason,
                                           EnumLocalInstrument localInstrument,
                                           String amount,
                                           String currency,
                                           String remittanceInformation,
                                           String ispb,
                                           String issuer,
                                           String number,
                                           EnumAccountPaymentsType accountType,
                                           PixPaymentCancellationV4 cancellation,
                                           String transactionIdentification = null) {

        new ResponsePixPaymentDataV4().data(createPatchPixPaymentResponseDataV4(
                paymentId,
                endToEndId,
                consentId,
                creation,
                statusUpdate,
                proxy,
                status,
                rejectionReason,
                localInstrument,
                amount,
                currency,
                remittanceInformation,
                ispb,
                issuer,
                number,
                accountType,
                cancellation))
    }

    static createPatchPixPaymentResponseDataV4(String paymentId,
                                               String endToEndId,
                                               String consentId,
                                               OffsetDateTime creation,
                                               OffsetDateTime statusUpdate,
                                               String proxy,
                                               EnumPaymentStatusTypeV2 status,
                                               RejectionReasonV2 rejectionReason,
                                               EnumLocalInstrument localInstrument,
                                               String amount,
                                               String currency,
                                               String remittanceInformation,
                                               String ispb,
                                               String issuer,
                                               String number,
                                               EnumAccountPaymentsType accountType,
                                               PixPaymentCancellationV4 cancellation) {

        return new ResponsePixPaymentDataV4()
                    .paymentId(paymentId)
                    .endToEndId(endToEndId)
                    .consentId(consentId)
                    .creationDateTime(creation)
                    .statusUpdateDateTime(statusUpdate)
                    .proxy(proxy)
                    .status(status)
                    .rejectionReason(rejectionReason)
                    .localInstrument(localInstrument)
                    .payment(new PaymentPix().amount(amount).currency(currency))
                    .remittanceInformation(remittanceInformation)
                    .creditorAccount(new CreditorAccount().ispb(ispb).issuer(issuer).number(number).accountType(accountType))
                    .cancellation(cancellation)
                    .debtorAccount(new DebtorAccount().accountType(EnumAccountPaymentsType.SLRY).ispb("12341234").issuer("1234").number("1234567890"))
                    .authorisationFlow(EnumAuthorisationFlow.CIBA_FLOW)
    }

    static CreatePaymentConsent testPaymentConsent(String documentIdentification, String documentRel, String paymentAmount = "100.00") {
        new CreatePaymentConsent().data(new CreatePaymentConsentData()
                .businessEntity(
                        new BusinessEntity().document(
                                new BusinessEntityDocument().identification("12345678901235")
                                        .rel("CNPJ")))
                .creditor(new Identification().cpfCnpj("12345678904").name("Bob Creditor").personType(EnumCreditorPersonType.NATURAL.toString()))
                .loggedUser(new LoggedUser().document(new Document().rel(documentRel).identification(documentIdentification)))
                .payment(new PaymentConsent()
                        .type(EnumPaymentType.PIX.toString())
                        .schedule(new Schedule().single(new ScheduleSingleSingle().date(LocalDate.now().plusDays(1))) as Schedule)
                        .currency("BRL")
                        .amount(paymentAmount)
                        .details(new Details()
                                .localInstrument(EnumLocalInstrument.DICT)
                                .proxy("proxy")
                                .creditorAccount(new CreditorAccount()
                                        .ispb("ispb")
                                        .issuer("mockbank")
                                        .number("1234567890")
                                        .accountType(EnumAccountPaymentsType.CACC)
                                )))
                .debtorAccount(new DebtorAccount().accountType(EnumAccountPaymentsType.SLRY)
                        .ispb("12341234").issuer("1234").number("1234567890")))
    }

    static CreatePaymentConsent testPaymentConsent() {
        testPaymentConsent("12345678905", "CPF")
    }

    static CreatePaymentConsent testPaymentConsentPatchNoSchedule(String accountHolderIdentification, String accountHolderRel) {
        new CreatePaymentConsent().data(new CreatePaymentConsentData()
                .businessEntity(
                        new BusinessEntity().document(
                                new BusinessEntityDocument().identification("12345678901235")
                                        .rel("CNPJ")))
                .creditor(new Identification().cpfCnpj("12345678904").name("Bob Creditor").personType(EnumCreditorPersonType.NATURAL.toString()))
                .loggedUser(new LoggedUser().document(new Document().rel(accountHolderRel).identification(accountHolderIdentification)))
                .payment(new PaymentConsent()
                        .type(EnumPaymentType.PIX.toString())
                        .date(LocalDate.now())
                        .currency("BRL")
                        .amount("100.00")
                        .details(new Details()
                                .localInstrument(EnumLocalInstrument.DICT)
                                .proxy("proxy")
                                .creditorAccount(new CreditorAccount()
                                        .ispb("ispb")
                                        .issuer("mockbank")
                                        .number("1234567890")
                                        .accountType(EnumAccountPaymentsType.CACC)
                                ))))


    }

    static CreatePaymentConsent testPaymentConsentSchedule() {
        new CreatePaymentConsent().data(new CreatePaymentConsentData()
                .businessEntity(
                        new BusinessEntity().document(
                                new BusinessEntityDocument().identification("12345678901235")
                                        .rel("CNPJ")))
                .creditor(new Identification().cpfCnpj("12345678904").name("Bob Creditor").personType(EnumCreditorPersonType.NATURAL.toString()))
                .loggedUser(new LoggedUser().document(new Document().rel("CPF").identification("12345678905")))
                .payment(new PaymentConsent()
                        .type(EnumPaymentType.PIX.toString())
                        .schedule(new Schedule().single(new Single().date(LocalDate.now())))
                        .currency("BRL")
                        .amount("100.00")
                        .details(new Details()
                                .localInstrument(EnumLocalInstrument.DICT)
                                .proxy("proxy")
                                .creditorAccount(new CreditorAccount()
                                        .ispb("ispb")
                                        .issuer("mockbank")
                                        .number("1234567890")
                                        .accountType(EnumAccountPaymentsType.CACC)
                                ))))


    }

    static CreatePaymentConsentV4 testPaymentConsentV4() {
        testPaymentConsentV4("12345678905", "CPF")
    }

    static CreatePaymentConsentV4 testPaymentConsentV4(String documentIdentification, String documentRel, String paymentAmount = "100.00") {
        new CreatePaymentConsentV4().data(new CreatePaymentConsentV4Data()
                .businessEntity(
                        new BusinessEntity().document(
                                new BusinessEntityDocument().identification("12345678901235")
                                        .rel("CNPJ")))
                .creditor(new Identification().cpfCnpj("12345678904").name("Bob Creditor").personType(EnumCreditorPersonType.NATURAL.toString()))
                .loggedUser(new LoggedUser().document(new Document().rel(documentRel).identification(documentIdentification)))
                .payment(new PaymentConsentV4Payment()
                        .date(LocalDate.now())
                        .currency("BRL")
                        .amount(paymentAmount)
                        .details(new Details()
                                .localInstrument(EnumLocalInstrument.INIC)
                                .proxy("proxy")
                                .creditorAccount(new CreditorAccount()
                                        .ispb("ispb")
                                        .issuer("mockbank")
                                        .number("1234567890")
                                        .accountType(EnumAccountPaymentsType.CACC)
                                ))
                        .type(EnumPaymentType.PIX)
                        .currency("BRL")
                        .amount(paymentAmount))
                .debtorAccount(new DebtorAccount().accountType(EnumAccountPaymentsType.CACC)
                        .ispb("12341234").issuer("1234").number("1234567890")))

    }

    static AllOfPaymentConsentV4PaymentSchedule scheduleSingleV4(int daysToAdd = 1) {
        return new AllOfPaymentConsentV4PaymentSchedule()
                .single(new ScheduleSingleSingle().date(LocalDate.now().plusDays(daysToAdd)))
                as AllOfPaymentConsentV4PaymentSchedule
    }

    static AllOfPaymentConsentV4PaymentSchedule scheduleDailyV4(int  quantity = 7, int startDaysToAdd = 0) {
        return new AllOfPaymentConsentV4PaymentSchedule()
                .daily(new ScheduleDailyDaily()
                        .startDate(LocalDate.now().plusDays(startDaysToAdd))
                        .quantity(quantity)
                )
    }

    static AllOfPaymentConsentV4PaymentSchedule scheduleWeeklyV4(int quantity = 5, int startDaysToAdd = 0, DayOfWeekEnum dayOfWeek = DayOfWeekEnum.SEGUNDA_FEIRA) {
        return new AllOfPaymentConsentV4PaymentSchedule()
                .weekly(new ScheduleWeeklyWeekly()
                        .startDate(LocalDate.now().plusDays(startDaysToAdd))
                        .quantity(quantity)
                        .dayOfWeek(dayOfWeek)
                )
    }

    static AllOfPaymentConsentV4PaymentSchedule scheduleMonthlyV4(int quantity = 7, int startDaysToAdd = 0, int dayOfMonth = 31) {
        return new AllOfPaymentConsentV4PaymentSchedule()
                .monthly(new ScheduleMonthlyMonthly()
                        .startDate(LocalDate.now().plusDays(startDaysToAdd))
                        .quantity(quantity)
                        .dayOfMonth(dayOfMonth)
                )
    }

    static AllOfPaymentConsentV4PaymentSchedule scheduleCustomV4(int daysToAdd = 17) {
        return new AllOfPaymentConsentV4PaymentSchedule()
                .custom(new ScheduleCustomCustom()
                        .dates(List.of(LocalDate.now(), LocalDate.now().plusDays(5), LocalDate.now().plusDays(daysToAdd)))
                        .additionalInformation("This is a cool custom list for cool dudes")
                )
    }

    static CreatePixPayment testPixPayment() {
        CreatePixPaymentData createPixPaymentData = new CreatePixPaymentData()
                .creditorAccount(new CreditorAccount().number("123456789012").issuer("1234").ispb("12345678").accountType(EnumAccountPaymentsType.CACC))
                .localInstrument(EnumLocalInstrument.MANU)
                .payment(new PaymentPix().amount("100.00").currency("BRL"))
                .proxy("proxy")
                .qrCode("qrcode")
                .remittanceInformation("remittanceInfo")
                .endToEndId(EndToEndIdHelper.generateRandomEndToEndId())
        new CreatePixPayment().data(createPixPaymentData)
    }

    static CreatePixPaymentV2 testPixPaymentV2() {
        CreatePixPaymentDataV2 createPixPaymentData = new CreatePixPaymentDataV2()
                .creditorAccount(new CreditorAccount().number("123456789012").issuer("1234").ispb("12345678").accountType(EnumAccountPaymentsType.CACC))
                .localInstrument(EnumLocalInstrument.MANU)
                .payment(new PaymentPix().amount("100.00").currency("BRL"))
                .proxy("proxy")
                .qrCode("qrcode")
                .remittanceInformation("remittanceInfo")
                .endToEndId(EndToEndIdHelper.generateRandomEndToEndId())
        new CreatePixPaymentV2().data(createPixPaymentData)
    }

    static CreatePixPaymentV4 testPixPaymentV4() {
        CreatePixPaymentDataV4 createPixPaymentData = new CreatePixPaymentDataV4()
                .creditorAccount(new CreditorAccount().number("123456789012").issuer("1234").ispb("12345678").accountType(EnumAccountPaymentsType.CACC))
                .localInstrument(EnumLocalInstrument.MANU)
                .payment(new PaymentPix().amount("100.00").currency("BRL"))
                .proxy("proxy")
                .qrCode("qrcode")
                .remittanceInformation("remittanceInfo")
                .endToEndId(EndToEndIdHelper.generateRandomEndToEndId())
        new CreatePixPaymentV4().addDataItem(createPixPaymentData)
    }

    static ResponsePaymentConsent testPaymentConsentResponse() {
        new ResponsePaymentConsent()
                .data(new ResponsePaymentConsentData()
                        .expirationDateTime(OffsetDateTime.now())
                        .statusUpdateDateTime(OffsetDateTime.now())
                        .status(ResponsePaymentConsentData.StatusEnum.AUTHORISED)
                        .consentId("consent1")
                        .loggedUser(new LoggedUser().document(new Document().identification("LUID1").rel("CPF")))
                        .businessEntity(new BusinessEntity().document(new BusinessEntityDocument().identification("BID1").rel("CNPJ")))
                        .creditor(new Identification().personType(EnumCreditorPersonType.NATURAL.toString()).cpfCnpj("CPF").name("Kate Human"))
                        .payment(new PaymentConsent()
                                .type(EnumPaymentType.PIX.toString())
                                .currency("BRL")
                                .amount("100")
                                .date(LocalDate.now())
                                .details(new Details()
                                        .localInstrument(EnumLocalInstrument.DICT)
                                        .qrCode("qrcode")
                                        .proxy("proxy")
                                        .creditorAccount(new CreditorAccount()
                                                .number("123")
                                                .ispb("ispb")
                                                .accountType(EnumAccountPaymentsType.CACC)
                                                .issuer("issuer")
                                        )
                                )
                        )
                )
    }

    static UpdatePaymentConsent testPutPaymentConsent() {
        new UpdatePaymentConsent()
                .data(new UpdatePaymentConsentData()
                        .status(UpdatePaymentConsentData.StatusEnum.AUTHORISED)
                        .debtorAccount(new DebtorAccount().accountType(EnumAccountPaymentsType.SLRY)
                                .ispb("12341234").issuer("1234").number("1234567890")))
    }

//    static PatchPaymentsConsent testPatchPaymentConsent() {
//        new PatchPaymentsConsent()
//                .data(new PatchPaymentsConsentData()
//                        .status(EnumAuthorisationPatchStatusType.REVOKED)
//                        .revocation(new Revocation().loggedUser(new LoggedUser().document(new LoggedUserDocument().identification("12345678910").rel("CPF")))
//                                .revokedBy(EnumRevokedBy.USER)
//                                .reason(new Reason().code(EnumRevocationReason.OTHER).additionalInformation("Some additionalInformation"))
//                        )
//                )
//    }

    static PatchPaymentsV2 testPatchPayment() {
        new PatchPaymentsV2()
                .data(new PatchPaymentsDataV2()
                        .status(EnumPaymentStatusTypeV2.CANC)
                        .cancellation(new Cancellation().cancelledBy(new LoggedUser().document(new Document().identification("12345678910").rel("CPF")))
                        )
                )
    }

    static createConsentRequest(String businessIdentityDocumentIdentification,
                                String businessIdentityDocumentREL,
                                String loggedUserIdentification,
                                String loggedUserRel,
                                OffsetDateTime expirationDateTime,
                                List<EnumConsentPermissions> permissions) {
        CreateConsentData consentRequestData = new CreateConsentData()
                .businessEntity(
                        new BusinessEntity().document(
                                new BusinessEntityDocument().identification(businessIdentityDocumentIdentification)
                                        .rel(businessIdentityDocumentREL)))
                .loggedUser(new LoggedUser().document(new Document().rel(loggedUserRel).identification(loggedUserIdentification)))
                .permissions(permissions)
                .expirationDateTime(expirationDateTime)

        return new CreateConsent().data(consentRequestData)
    }

    static createConsentUpdate(UpdateConsentData.StatusEnum status,
                               String sub,
                               List<String> accountIds,
                               List<String> creditCardAccountIds,
                               List<String> loanAccountIds,
                               List<String> financingAccountIds,
                               String clientId) {
        UpdateConsentData updateConsentData = new UpdateConsentData()
                .status(status)
                .sub(sub)
                .linkedAccountIds(accountIds)
                .linkedCreditCardAccountIds(creditCardAccountIds)
                .linkedLoanAccountIds(loanAccountIds)
                .linkedFinancingAccountIds(financingAccountIds)
                .clientId(clientId)
        return new UpdateConsent().data(updateConsentData)
    }

    static CreateAccount createAccount() {
        CreateAccountData data = new CreateAccountData()
                .accountType(EnumAccountType.DEPOSITO_A_VISTA)
                .accountSubType(EnumAccountSubType.INDIVIDUAL)
                .currency(RandomStringUtils.randomAlphanumeric(10))
                .status("AVAILABLE")
                .brandName(RandomStringUtils.randomAlphanumeric(80))
                .branchCode(RandomStringUtils.randomNumeric(4))
                .number(RandomStringUtils.randomNumeric(20))
                .checkDigit(RandomStringUtils.randomAlphanumeric(1))
                .companyCnpj(RandomStringUtils.randomNumeric(14))
                .compeCode(RandomStringUtils.randomNumeric(3))
                .unarrangedOverdraftAmount(10)
                .unarrangedOverdraftAmountCurrency(RandomStringUtils.randomAlphabetic(3))
                .overdraftUsedLimit(random.nextInt(1000))
                .overdraftUsedLimitCurrency(RandomStringUtils.randomAlphabetic(3))
                .overdraftContractedLimit(random.nextInt(1000))
                .overdraftContractedLimitCurrency(RandomStringUtils.randomAlphabetic(3))
                .automaticallyInvestedAmount(random.nextInt(1000))
                .automaticallyInvestedAmountCurrency(RandomStringUtils.randomAlphabetic(3))
                .availableAmount(random.nextInt(1000))
                .availableAmountCurrency(RandomStringUtils.randomAlphabetic(3))
                .blockedAmount(random.nextInt(1000))
                .blockedAmountCurrency(RandomStringUtils.randomAlphabetic(3))
        new CreateAccount().data(data)
    }

    static EditedAccountData editedAccountDto() {
        new EditedAccountData()
                .accountType(EnumAccountType.DEPOSITO_A_VISTA)
                .accountSubType(EnumAccountSubType.INDIVIDUAL)
                .currency(RandomStringUtils.randomAlphanumeric(10))
                .status("AVAILABLE")
                .brandName(RandomStringUtils.randomAlphanumeric(80))
                .branchCode(RandomStringUtils.randomNumeric(4))
                .number(RandomStringUtils.randomNumeric(20))
                .checkDigit(RandomStringUtils.randomAlphanumeric(1))
                .companyCnpj(RandomStringUtils.randomNumeric(14))
                .compeCode(RandomStringUtils.randomNumeric(3))
                .unarrangedOverdraftAmount(10)
                .unarrangedOverdraftAmountCurrency(RandomStringUtils.randomAlphabetic(3))
                .overdraftUsedLimit(random.nextInt(1000))
                .overdraftUsedLimitCurrency(RandomStringUtils.randomAlphabetic(3))
                .overdraftContractedLimit(random.nextInt(1000))
                .overdraftContractedLimitCurrency(RandomStringUtils.randomAlphabetic(3))
                .automaticallyInvestedAmount(random.nextInt(1000))
                .automaticallyInvestedAmountCurrency(RandomStringUtils.randomAlphabetic(3))
                .availableAmount(random.nextInt(1000))
                .availableAmountCurrency(RandomStringUtils.randomAlphabetic(3))
                .blockedAmount(random.nextInt(1000))
                .blockedAmountCurrency(RandomStringUtils.randomAlphabetic(3))
    }

    static CreateAccountTransaction createAccountTransaction() {
        CreateAccountTransactionData data = new CreateAccountTransactionData()
                .completedAuthorisedPaymentType(EnumCompletedAuthorisedPaymentIndicator.values()[random.nextInt(EnumCompletedAuthorisedPaymentIndicator.values().size())])
                .creditDebitType(EnumCreditDebitIndicator.values()[random.nextInt(EnumCreditDebitIndicator.values().size())])
                .transactionName(RandomStringUtils.randomAlphanumeric(10))
                .type(EnumTransactionTypes.values()[random.nextInt(EnumTransactionTypes.values().size())])
                .amount(random.nextInt(1000))
                .transactionCurrency(RandomStringUtils.randomAlphanumeric(3))
                .transactionDateTime(OffsetDateTime.now())
                .partieCnpjCpf(RandomStringUtils.randomNumeric(11))
                .partiePersonType(EnumPartiePersonType.values()[random.nextInt(EnumPartiePersonType.values().size())])
                .partieCompeCode(RandomStringUtils.randomAlphanumeric(10))
                .partieBranchCode(RandomStringUtils.randomNumeric(4))
                .partieNumber(RandomStringUtils.randomNumeric(11))
                .partieCheckDigit(RandomStringUtils.randomAlphanumeric(1))
        new CreateAccountTransaction().data(data)
    }

    static CreateCreditCardAccount creditCardAccount() {
        new CreateCreditCardAccount().data(new CreateCreditCardAccountData()
                .brandName(RandomStringUtils.randomAlphanumeric(80))
                .companyCnpj(RandomStringUtils.randomNumeric(14))
                .name(RandomStringUtils.randomAlphanumeric(50))
                .productType(EnumCreditCardAccountsProductType.values()[random.nextInt(EnumCreditCardAccountsProductType.values().size())])
                .productAdditionalInfo(RandomStringUtils.randomAlphanumeric(50))
                .creditCardNetwork(EnumCreditCardAccountNetwork.values()[random.nextInt(EnumCreditCardAccountNetwork.values().size())])
                .networkAdditionalInfo(RandomStringUtils.randomAlphanumeric(50))
                .status("AVAILABLE")
                .paymentMethod(List.of(createCreditCardsAccountPaymentMethod())))
    }

    static EditedCreditCardAccount editCardAccountDto() {
        new EditedCreditCardAccount().data(new EditedCreditCardAccountData()
                .brandName(RandomStringUtils.randomAlphanumeric(80))
                .companyCnpj(RandomStringUtils.randomNumeric(14))
                .name(RandomStringUtils.randomAlphanumeric(50))
                .productType(EnumCreditCardAccountsProductType.values()[random.nextInt(EnumCreditCardAccountsProductType.values().size())])
                .productAdditionalInfo(RandomStringUtils.randomAlphanumeric(50))
                .creditCardNetwork(EnumCreditCardAccountNetwork.values()[random.nextInt(EnumCreditCardAccountNetwork.values().size())])
                .networkAdditionalInfo(RandomStringUtils.randomAlphanumeric(50))
                .status("AVAILABLE")
                .paymentMethod(List.of(createCreditCardsAccountPaymentMethod())))
    }

    static CreditCardsAccountPaymentMethod createCreditCardsAccountPaymentMethod() {
        new CreditCardsAccountPaymentMethod()
                .identificationNumber(RandomStringUtils.randomNumeric(10))
                .isMultipleCreditCard(random.nextBoolean())
    }

    static CreateCreditCardAccountLimits creditCardAccountLimitDto() {
        CreditCardAccountsLimitsData data = new CreditCardAccountsLimitsData()
                .creditLineLimitType(EnumCreditCardAccountsLineLimitType.values()[random.nextInt(EnumCreditCardAccountsLineLimitType.values().size())])
                .consolidationType(EnumCreditCardAccountsConsolidationType.values()[random.nextInt(EnumCreditCardAccountsConsolidationType.values().size())])
                .identificationNumber(RandomStringUtils.randomAlphanumeric(100))
                .lineName(EnumCreditCardAccountsLineName.values()[random.nextInt(EnumCreditCardAccountsLineName.values().size())])
                .lineNameAdditionalInfo(RandomStringUtils.randomAlphanumeric(10))
                .isLimitFlexible(random.nextBoolean())
                .limitAmountCurrency(RandomStringUtils.randomAlphanumeric(3))
                .limitAmount(random.nextInt(1000))
                .usedAmountCurrency(RandomStringUtils.randomAlphanumeric(3))
                .usedAmount(random.nextInt(1000))
                .availableAmountCurrency(RandomStringUtils.randomAlphanumeric(3))
                .availableAmount(random.nextInt(1000))
        new CreateCreditCardAccountLimits().data(List.of(data))
    }

    static CreateCreditCardAccountBill creditCardBillDto() {
        CreateCreditCardAccountBillData data = new CreateCreditCardAccountBillData()
                .dueDate(LocalDate.now())
                .billMinimumAmount(random.nextInt(1000))
                .billMinimumAmountCurrency(RandomStringUtils.randomAlphanumeric(3))
                .billTotalAmount(random.nextInt(1000))
                .billTotalAmountCurrency(RandomStringUtils.randomAlphanumeric(3))
                .instalment(random.nextBoolean())
                .financeCharges(List.of(createCreditCardAccountsBillsFinanceCharge()))
                .payments(List.of(createCreditCardAccountsBillsPayment()))
        new CreateCreditCardAccountBill().data(data)
    }

    static CreateCreditCardAccountLimits createCreditCardAccountsLimits() {
        CreditCardAccountsLimitsData accountLimits = new CreditCardAccountsLimitsData()
        accountLimits.setCreditLineLimitType(EnumCreditCardAccountsLineLimitType.TOTAL)
        accountLimits.setConsolidationType(EnumCreditCardAccountsConsolidationType.CONSOLIDADO)
        accountLimits.setIdentificationNumber("5320")
        accountLimits.setLineName(EnumCreditCardAccountsLineName.CREDITO_A_VISTA)
        accountLimits.setLineNameAdditionalInfo("NA")
        accountLimits.setIsLimitFlexible(false)
        accountLimits.setLimitAmountCurrency("BRL")
        accountLimits.setLimitAmount(3000.0000)
        accountLimits.setUsedAmountCurrency("BRL")
        accountLimits.setUsedAmount(343.0400)
        accountLimits.setAvailableAmountCurrency("BRL")
        accountLimits.setAvailableAmount(2656.9600)

        new CreateCreditCardAccountLimits().data(List.of(accountLimits))
    }

    static CreditCardAccountsBillsFinanceCharge createCreditCardAccountsBillsFinanceCharge() {
        new CreditCardAccountsBillsFinanceCharge()
                .amount(random.nextInt(1000))
                .type(EnumCreditCardAccountsFinanceChargeType.OUTROS)
                .currency(RandomStringUtils.randomAlphanumeric(3))
                .additionalInfo(RandomStringUtils.randomAlphanumeric(140))
    }

    static CreditCardAccountsBillsPayment createCreditCardAccountsBillsPayment() {
        new CreditCardAccountsBillsPayment()
                .valueType(EnumCreditCardAccountsBillingValueType.values()[random.nextInt(EnumCreditCardAccountsBillingValueType.values().size())])
                .paymentDate(LocalDate.now())
                .paymentMode(EnumCreditCardAccountsPaymentMode.values()[random.nextInt(EnumCreditCardAccountsPaymentMode.values().size())])
                .currency(RandomStringUtils.randomAlphanumeric(3))
                .amount(random.nextInt(1000))
    }

    static CreateCreditCardAccountTransactionData cardAccountTransactionDto() {
        new CreateCreditCardAccountTransactionData()
                .identificationNumber(RandomStringUtils.randomAlphanumeric(100))
                .lineName(EnumCreditCardAccountsLineName.values()[random.nextInt(EnumCreditCardAccountsLineName.values().size())])
                .transactionName(RandomStringUtils.randomAlphanumeric(100))
                .creditDebitType(EnumCreditDebitIndicator.values()[random.nextInt(EnumCreditDebitIndicator.values().size())])
                .transactionType(EnumCreditCardTransactionType.values()[random.nextInt(EnumCreditCardTransactionType.values().size())])
                .transactionalAdditionalInfo(RandomStringUtils.randomAlphanumeric(140))
                .paymentType(EnumCreditCardAccountsPaymentType.values()[random.nextInt(EnumCreditCardAccountsPaymentType.values().size())])
                .feeType(EnumCreditCardAccountFee.values()[random.nextInt(EnumCreditCardAccountFee.values().size())])
                .feeTypeAdditionalInfo(RandomStringUtils.randomAlphanumeric(140))
                .otherCreditsType(EnumCreditCardAccountsOtherCreditType.values()[random.nextInt(EnumCreditCardAccountsOtherCreditType.values().size())])
                .otherCreditsAdditionalInfo(RandomStringUtils.randomAlphanumeric(50))
                .chargeIdentificator(random.nextInt(10).toString())
                .chargeNumber(new BigDecimal(random.nextInt(1000)))
                .brazilianAmount(random.nextInt(1000))
                .amount(random.nextInt(1000))
                .currency(RandomStringUtils.randomAlphanumeric(3))
                .transactionDateTime(OffsetDateTime.now().minusDays(random.nextInt(3650)))
                .billPostDate(LocalDate.now().minusDays(random.nextInt(3650)))
                .payeeMCC(new BigDecimal(random.nextInt(1000)))
    }

    static EditedCreditCardAccountTransaction editAccountTransactionDto() {
        new EditedCreditCardAccountTransaction().data(new CreateCreditCardAccountTransactionData()
                .identificationNumber(RandomStringUtils.randomAlphanumeric(100))
                .lineName(EnumCreditCardAccountsLineName.values()[random.nextInt(EnumCreditCardAccountsLineName.values().size())])
                .transactionName(RandomStringUtils.randomAlphanumeric(100))
                .creditDebitType(EnumCreditDebitIndicator.values()[random.nextInt(EnumCreditDebitIndicator.values().size())])
                .transactionType(EnumCreditCardTransactionType.values()[random.nextInt(EnumCreditCardTransactionType.values().size())])
                .transactionalAdditionalInfo(RandomStringUtils.randomAlphanumeric(140))
                .paymentType(EnumCreditCardAccountsPaymentType.values()[random.nextInt(EnumCreditCardAccountsPaymentType.values().size())])
                .feeType(EnumCreditCardAccountFee.values()[random.nextInt(EnumCreditCardAccountFee.values().size())])
                .feeTypeAdditionalInfo(RandomStringUtils.randomAlphanumeric(140))
                .otherCreditsType(EnumCreditCardAccountsOtherCreditType.values()[random.nextInt(EnumCreditCardAccountsOtherCreditType.values().size())])
                .otherCreditsAdditionalInfo(RandomStringUtils.randomAlphanumeric(50))
                .chargeIdentificator(random.nextInt(10).toString())
                .chargeNumber(new BigDecimal(random.nextInt(1000)))
                .brazilianAmount(random.nextInt(1000))
                .amount(random.nextInt(1000))
                .currency(RandomStringUtils.randomAlphanumeric(3))
                .transactionDateTime(OffsetDateTime.now().minusDays(random.nextInt(3650)))
                .billPostDate(LocalDate.now().minusDays(random.nextInt(3650)))
                .payeeMCC(new BigDecimal(random.nextInt(1000))))
    }

    static CreateContract createContract(EnumContractType type) {
        createContract(type, ContractStatusEnum.AVAILABLE)
    }

    static CreateContract createContract(EnumContractType type, ContractStatusEnum status) {
        def contract = new CreateContractData()
                .contractNumber(RandomStringUtils.randomAlphanumeric(10))
                .ipocCode(RandomStringUtils.randomAlphanumeric(10))
                .productName(RandomStringUtils.randomAlphanumeric(10))
                .contractType(type)
                .contractDate(LocalDate.now().minusDays(random.nextInt(3650)))
                .disbursementDate(LocalDate.now().minusDays(random.nextInt(3650)))
                .settlementDate(LocalDate.now().minusDays(random.nextInt(3650)))
                .contractAmount(random.nextInt(1000))
                .currency(RandomStringUtils.randomAlphanumeric(3))
                .dueDate(LocalDate.now().minusDays(random.nextInt(3650)))
                .instalmentPeriodicity(EnumContractInstalmentPeriodicity.values()[random.nextInt(EnumContractInstalmentPeriodicity.values().size())])
                .instalmentPeriodicityAdditionalInfo(RandomStringUtils.randomAlphanumeric(10))
                .firstInstalmentDueDate(LocalDate.now().minusDays(random.nextInt(3650)))
                .cet(0.150001)
                .amortizationScheduled(EnumContracttAmortizationScheduled.values()[random.nextInt(EnumContracttAmortizationScheduled.values().size())])
                .amortizationScheduledAdditionalInfo(RandomStringUtils.randomAlphanumeric(10))
                .companyCnpj(RandomStringUtils.randomAlphanumeric(10))
                .status(status.toString())
                .paidInstalments(BigDecimal.valueOf(random.nextInt(1000)))
                .contractOutstandingBalance(random.nextInt(1000))
                .typeNumberOfInstalments(EnumTypeNumberOfInstalments.values()[random.nextInt(EnumTypeNumberOfInstalments.values().size())])
                .totalNumberOfInstalments(BigDecimal.valueOf(random.nextInt(1000)))
                .typeContractRemaining(EnumTypeContractRemaining.values()[random.nextInt(EnumTypeContractRemaining.values().size())])
                .contractRemainingNumber(BigDecimal.valueOf(random.nextInt(1000)))
                .dueInstalments(BigDecimal.valueOf(random.nextInt(1000)))
                .pastDueInstalments(BigDecimal.valueOf(random.nextInt(1000)))
                .interestRates(List.of(createAdminContractInterestRates()))
                .contractedFees(List.of(createAdminContractFees()))
                .contractedFinanceCharges(List.of(createAdminContractFinanceCharges()))
                .balloonPayments(List.of(createAdminContractBalloonPayment(), createAdminContractBalloonPayment()))
                .releases(List.of(createAdminContractReleases(), createAdminContractReleases()))

        switch (type) {
            case EnumContractType.LOAN: {
                contract.setProductType(EnumContractProductTypeLoans.values()[random.nextInt(EnumContractProductTypeLoans.values().size())].toString())
                contract.setProductSubType(EnumContractProductSubTypeLoans.values()[random.nextInt(EnumContractProductSubTypeLoans.values().size())].toString())
                break
            }
            case EnumContractType.FINANCING: {
                contract.setProductType(EnumProductType.values()[random.nextInt(EnumProductType.values().size())].toString())
                contract.setProductSubType(EnumProductSubType.values()[random.nextInt(EnumProductSubType.values().size())].toString())
                break
            }
            case EnumContractType.INVOICE_FINANCING: {
                contract.setProductType(EnumContractProductTypeInvoiceFinancings.values()[random.nextInt(EnumContractProductTypeInvoiceFinancings.values().size())].toString())
                contract.setProductSubType(EnumContractProductSubTypeInvoiceFinancings.values()[random.nextInt(EnumContractProductSubTypeInvoiceFinancings.values().size())].toString())
                break
            }
            case EnumContractType.UNARRANGED_ACCOUNT_OVERDRAFT: {
                contract.setProductType(ProductType.values()[random.nextInt(ProductType.values().size())].toString())
                contract.setProductSubType(ProductSubType.values()[random.nextInt(ProductSubType.values().size())].toString())
                break
            }
        }
        return new CreateContract().data(contract)
    }

    static EditedContractData createEditedContract() {
        new EditedContractData()
                .contractNumber(RandomStringUtils.randomAlphanumeric(10))
                .ipocCode(RandomStringUtils.randomAlphanumeric(10))
                .productName(RandomStringUtils.randomAlphanumeric(10))
                .contractDate(LocalDate.now().minusDays(random.nextInt(3650)))
                .disbursementDate(LocalDate.now().minusDays(random.nextInt(3650)))
                .settlementDate(LocalDate.now().minusDays(random.nextInt(3650)))
                .contractAmount(random.nextInt(1000))
                .currency(RandomStringUtils.randomAlphanumeric(3))
                .dueDate(LocalDate.now().minusDays(random.nextInt(3650)))
                .instalmentPeriodicity(EnumContractInstalmentPeriodicity.values()[random.nextInt(EnumContractInstalmentPeriodicity.values().size())])
                .instalmentPeriodicityAdditionalInfo(RandomStringUtils.randomAlphanumeric(10))
                .firstInstalmentDueDate(LocalDate.now().minusDays(random.nextInt(3650)))
                .cet(random.nextInt(1000))
                .amortizationScheduled(EnumContracttAmortizationScheduled.values()[random.nextInt(EnumContracttAmortizationScheduled.values().size())])
                .amortizationScheduledAdditionalInfo(RandomStringUtils.randomAlphanumeric(10))
                .companyCnpj(RandomStringUtils.randomAlphanumeric(10))
                .status("AVAILABLE")
                .paidInstalments(BigDecimal.valueOf(random.nextInt(1000)))
                .contractOutstandingBalance(random.nextInt(1000))
                .typeNumberOfInstalments(EnumTypeNumberOfInstalments.values()[random.nextInt(EnumTypeNumberOfInstalments.values().size())])
                .totalNumberOfInstalments(BigDecimal.valueOf(random.nextInt(1000)))
                .typeContractRemaining(EnumTypeContractRemaining.values()[random.nextInt(EnumTypeContractRemaining.values().size())])
                .contractRemainingNumber(BigDecimal.valueOf(random.nextInt(1000)))
                .dueInstalments(BigDecimal.valueOf(random.nextInt(1000)))
                .pastDueInstalments(BigDecimal.valueOf(random.nextInt(1000)))
                .interestRates(List.of(createAdminContractInterestRates()))
                .contractedFees(List.of(createAdminContractFees()))
                .contractedFinanceCharges(List.of(createAdminContractFinanceCharges()))
                .balloonPayments(List.of(createAdminContractBalloonPayment()))
                .releases(List.of(createAdminContractReleases()))
                .productType(EnumContractProductTypeLoans.values()[random.nextInt(EnumContractProductTypeLoans.values().size())].toString())
                .productSubType(EnumContractProductSubTypeLoans.values()[random.nextInt(EnumContractProductSubTypeLoans.values().size())].toString())

    }

    static ContractInterestRates createAdminContractInterestRates() {
        new ContractInterestRates()
                .taxType(EnumContractTaxType.values()[random.nextInt(EnumContractTaxType.values().size())])
                .interestRateType(EnumContractInterestRateType.values()[random.nextInt(EnumContractInterestRateType.values().size())])
                .taxPeriodicity(EnumContractTaxPeriodicity.values()[random.nextInt(EnumContractTaxPeriodicity.values().size())])
                .calculation(ContractInterestRates.CalculationEnum.values()[random.nextInt(ContractInterestRates.CalculationEnum.values().size())])
                .referentialRateIndexerType(EnumContractReferentialRateIndexerType.values()[random.nextInt(EnumContractReferentialRateIndexerType.values().size())])
                .referentialRateIndexerSubType(EnumContractReferentialRateIndexerSubType.values()[random.nextInt(EnumContractReferentialRateIndexerSubType.values().size())])
                .referentialRateIndexerAdditionalInfo(RandomStringUtils.randomAlphanumeric(10))
                .preFixedRate(random.nextInt(1000))
                .postFixedRate(random.nextInt(1000))
                .additionalInfo(RandomStringUtils.randomAlphanumeric(10))
    }

    static ContractFees createAdminContractFees() {
        new ContractFees()
                .feeName(RandomStringUtils.randomAlphanumeric(10))
                .feeCode(RandomStringUtils.randomAlphanumeric(10))
                .feeCharge(EnumContractFeeCharge.values()[random.nextInt(EnumContractFeeCharge.values().size())])
                .feeChargeType(EnumContractFeeChargeType.values()[random.nextInt(EnumContractFeeChargeType.values().size())])
                .feeAmount(random.nextInt(1000))
                .feeRate(random.nextInt(1000))
    }

    static ContractFinanceCharges createAdminContractFinanceCharges() {
        new ContractFinanceCharges()
                .chargeType(ChargeType.values()[random.nextInt(ChargeType.values().size())])
                .chargeAdditionalInfo(RandomStringUtils.randomAlphanumeric(10))
                .chargeRate(random.nextInt(1000))
    }

    static ContractBalloonPayment createAdminContractBalloonPayment() {
        new ContractBalloonPayment()
                .dueDate(LocalDate.now().minusDays(random.nextInt(3650)))
                .currency(RandomStringUtils.randomAlphanumeric(3))
                .amount(random.nextInt(1000))
    }

    static ContractReleases createAdminContractReleases() {
        new ContractReleases()
                .paymentsId(UUID.randomUUID())
                .isOverParcelPayment(random.nextBoolean())
                .instalmentId(RandomStringUtils.randomAlphanumeric(10))
                .paidDate(LocalDate.now().minusDays(random.nextInt(3650)))
                .currency(RandomStringUtils.randomAlphanumeric(3))
                .paidAmount(random.nextInt(1000))
                .overParcelFees(List.of(createAdminContractOverParcelFees()))
                .overParcelCharges(List.of(createAdminContractOverParcelCharges()))
    }

    static ContractOverParcelFees createAdminContractOverParcelFees() {
        new ContractOverParcelFees()
                .feeName(RandomStringUtils.randomAlphanumeric(10))
                .feeCode(RandomStringUtils.randomAlphanumeric(10))
                .feeAmount(random.nextInt(1000))
    }

    static ContractOverParcelCharges createAdminContractOverParcelCharges() {
        new ContractOverParcelCharges()
                .chargeType(ChargeType.values()[random.nextInt(ChargeType.values().size())])
                .chargeAdditionalInfo(RandomStringUtils.randomAlphanumeric(10))
                .chargeAmount(random.nextInt(1000))
    }

    static ContractWarrantiesData createWarranties() {
        new ContractWarrantiesData()
                .currency(RandomStringUtils.randomAlphanumeric(3))
                .warrantyAmount(11)
                .warrantySubType(EnumWarrantySubType.DEPOSITO_TITULOS_EMITIDOS_ENTIDADES_ART_23.toString())
                .warrantyType(EnumWarrantyType.OPERACOES_GARANTIDAS_PELO_GOVERNO.toString())
    }

    static CreateAccountHolderData createAccountHolder(String identification, String rel) {
        new CreateAccountHolderData()
                .documentIdentification(identification)
                .documentRel(rel)
                .accountHolderName(RandomStringUtils.randomAlphanumeric(10))
    }

    static CreatePersonalIdentification createPersonalIdentifications() {
        new CreatePersonalIdentification().data(new CreatePersonalIdentificationData()
                .brandName(RandomStringUtils.randomAlphanumeric(80))
                .civilName(RandomStringUtils.randomAlphanumeric(70))
                .socialName(RandomStringUtils.randomAlphanumeric(70))
                .birthDate(LocalDate.now().minusDays(random.nextInt(3650)))
                .maritalStatusCode(EnumMaritalStatusCode.values()[random.nextInt(EnumMaritalStatusCode.values().size())])
                .maritalStatusAdditionalInfo(RandomStringUtils.randomAlphanumeric(10))
                .sex(EnumSex.MASCULINO)
                .hasBrazilianNationality(random.nextBoolean())
                .cpfNumber(RandomStringUtils.randomAlphanumeric(11))
                .passportNumber(RandomStringUtils.randomAlphanumeric(20))
                .passportCountry(RandomStringUtils.randomAlphanumeric(3))
                .passportExpirationDate(LocalDate.now().plusDays(random.nextInt(3650)))
                .passportIssueDate(LocalDate.now().minusDays(random.nextInt(3650)))
                .companyCnpj(List.of(RandomStringUtils.randomNumeric(14), RandomStringUtils.randomNumeric(14)))
                .otherDocuments(List.of(createPersonalOtherDocuments()))
                .nationality(List.of(createPersonalNationality()))
                .filiation(List.of(createPersonalFiliation()))
                .contacts(createPersonalContacts()))

    }

    static EditedPersonalIdentification editPersonalIdentifications() {
        new EditedPersonalIdentification().data(new EditedPersonalIdentificationData()
                .brandName(RandomStringUtils.randomAlphanumeric(80))
                .civilName(RandomStringUtils.randomAlphanumeric(70))
                .socialName(RandomStringUtils.randomAlphanumeric(70))
                .birthDate(LocalDate.now().minusDays(random.nextInt(3650)))
                .maritalStatusCode(EnumMaritalStatusCode.values()[random.nextInt(EnumMaritalStatusCode.values().size())])
                .maritalStatusAdditionalInfo(RandomStringUtils.randomAlphanumeric(10))
                .sex(EnumSex.values()[random.nextInt(EnumSex.values().size())])
                .hasBrazilianNationality(random.nextBoolean())
                .cpfNumber(RandomStringUtils.randomAlphanumeric(11))
                .passportNumber(RandomStringUtils.randomAlphanumeric(20))
                .passportCountry(RandomStringUtils.randomAlphanumeric(3))
                .passportExpirationDate(LocalDate.now().plusDays(random.nextInt(3650)))
                .passportIssueDate(LocalDate.now().minusDays(random.nextInt(3650)))
                .companyCnpj(List.of(RandomStringUtils.randomNumeric(14), RandomStringUtils.randomNumeric(14)))
                .otherDocuments(List.of(createPersonalOtherDocuments()))
                .nationality(List.of(createPersonalNationality()))
                .filiation(List.of(createPersonalFiliation()))
                .contacts(createPersonalContacts()))

    }

    static PersonalOtherDocument createPersonalOtherDocuments() {
        new PersonalOtherDocument()
                .type(EnumPersonalOtherDocumentType.values()[random.nextInt(EnumPersonalOtherDocumentType.values().size())])
                .typeAdditionalInfo(RandomStringUtils.randomAlphanumeric(70))
                .number(RandomStringUtils.randomNumeric(11))
                .checkDigit(RandomStringUtils.randomAlphanumeric(2))
                .additionalInfo(RandomStringUtils.randomAlphanumeric(50))
                .expirationDate(LocalDate.now().plusDays(random.nextInt(3650)))
    }

    static Nationality createPersonalNationality() {
        new Nationality()
                .documents(List.of(createPersonalNationalityDocument()))
                .otherNationalitiesInfo(RandomStringUtils.randomAlphanumeric(40))
    }

    static NationalityOtherDocument createPersonalNationalityDocument() {
        new NationalityOtherDocument()
                .type(RandomStringUtils.randomAlphanumeric(10))
                .number(RandomStringUtils.randomNumeric(11))
                .country(RandomStringUtils.randomAlphanumeric(80))
                .typeAdditionalInfo(RandomStringUtils.randomAlphanumeric(70))
                .issueDate(LocalDate.now().minusDays(random.nextInt(3650)))
                .expirationDate(LocalDate.now().plusDays(random.nextInt(3650)))
    }

    static PersonalIdentificationDataFiliation createPersonalFiliation() {
        new PersonalIdentificationDataFiliation()
                .type(EnumFiliationType.values()[random.nextInt(EnumFiliationType.values().size())])
                .civilName(RandomStringUtils.randomAlphanumeric(70))
                .socialName(RandomStringUtils.randomAlphanumeric(70))
    }

    static PersonalContacts createPersonalContacts() {
        new PersonalContacts()
                .postalAddresses(List.of(createPersonalPostalAddresses(), createPersonalPostalAddresses()))
                .phones(List.of(createPhones(), createPhones()))
                .emails(List.of(createEmails(), createEmails()))
    }

    static PersonalPostalAddress createPersonalPostalAddresses() {
        new PersonalPostalAddress()
                .isMain(random.nextBoolean())
                .address(RandomStringUtils.randomAlphanumeric(150))
                .additionalInfo(RandomStringUtils.randomAlphanumeric(30))
                .districtName(RandomStringUtils.randomAlphanumeric(50))
                .townName(RandomStringUtils.randomAlphanumeric(50))
                .ibgeTownCode(RandomStringUtils.randomNumeric(7))
                .countrySubDivision(EnumCountrySubDivision.values()[random.nextInt(EnumCountrySubDivision.values().size())])
                .postCode(RandomStringUtils.randomAlphanumeric(8))
                .country(RandomStringUtils.randomAlphanumeric(80))
                .countryCode(RandomStringUtils.randomAlphanumeric(3))
                .geographicCoordinates(createGeographicCoordinates())
    }

    static CustomerPhone createPhones() {
        new CustomerPhone()
                .isMain(random.nextBoolean())
                .type(EnumCustomerPhoneType.values()[random.nextInt(EnumCustomerPhoneType.values().size())])
                .countryCallingCode(RandomStringUtils.randomAlphanumeric(4))
                .additionalInfo(RandomStringUtils.randomAlphanumeric(70))
                .areaCode(EnumAreaCode.values()[random.nextInt(EnumAreaCode.values().size())])
                .number(RandomStringUtils.randomNumeric(11))
                .phoneExtension(RandomStringUtils.randomAlphanumeric(5))
    }

    static CustomerEmail createEmails() {
        new CustomerEmail()
                .isMain(random.nextBoolean())
                .email(RandomStringUtils.randomAlphanumeric(320))
    }

    static GeographicCoordinates createGeographicCoordinates() {
        new GeographicCoordinates()
                .latitude(RandomStringUtils.randomNumeric(13))
                .longitude(RandomStringUtils.randomNumeric(13))
    }

    static PersonalFinancialRelations createPersonalFinancialRelations() {
        new PersonalFinancialRelations().data(new PersonalFinancialRelationsData()
                .startDate(OffsetDateTime.now().minusDays(random.nextInt(3650)))
                .productsServicesTypeAdditionalInfo(RandomStringUtils.randomAlphanumeric(100))
                .productsServicesType(List.of(EnumProductServiceType.CARTAO_CREDITO))
                .procurators(List.of(createPersonalProcurators(), createPersonalProcurators())))
    }

    static PersonalProcurator createPersonalProcurators() {
        new PersonalProcurator()
                .type(EnumProcuratorsTypePersonal.REPRESENTANTE_LEGAL)
                .cpfNumber(RandomStringUtils.randomNumeric(11))
                .civilName(RandomStringUtils.randomAlphanumeric(70))
                .socialName(RandomStringUtils.randomAlphanumeric(70))
    }

    static PersonalQualifications createPersonalQualifications() {
        new PersonalQualifications().data(new PersonalQualificationsData()
                .companyCnpj(RandomStringUtils.randomNumeric(14))
                .occupationCode(EnumOccupationMainCodeType.values()[random.nextInt(EnumOccupationMainCodeType.values().size())])
                .occupationDescription(RandomStringUtils.randomAlphanumeric(100))
                .informedIncomeFrequency(EnumInformedIncomeFrequency.OUTROS)
                .informedIncomeAmount(random.nextInt(1000))
                .informedIncomeCurrency(RandomStringUtils.randomAlphanumeric(3))
                .informedIncomeDate(LocalDate.now().minusDays(random.nextInt(3650)).toString())
                .informedPatrimonyAmount(random.nextInt(1000))
                .informedPatrimonyCurrency(RandomStringUtils.randomAlphanumeric(3))
                .informedPatrimonyYear(BigDecimal.valueOf(random.nextInt(2022))))

    }

    static CreateBusinessIdentification createBusinessIdentifications() {
        new CreateBusinessIdentification().data(new CreateBusinessIdentificationData()
                .brandName(RandomStringUtils.randomAlphanumeric(80))
                .companyName(RandomStringUtils.randomAlphanumeric(70))
                .tradeName(RandomStringUtils.randomAlphanumeric(70))
                .incorporationDate(BankLambdaUtils.localDateToOffsetDate(LocalDate.now().minusDays(random.nextInt(3650))))
                .cnpjNumber(RandomStringUtils.randomNumeric(14))
                .companyCnpjNumber(List.of(RandomStringUtils.randomNumeric(14), RandomStringUtils.randomNumeric(14)))
                .otherDocuments(List.of(createBusinessOtherDocuments(), createBusinessOtherDocuments()))
                .parties(List.of(createBusinessParties(), createBusinessParties()))
                .contacts(createBusinessContacts()))
    }

    static CreateBusinessIdentification createBusinessIdentifications(String... cpfs) {
        new CreateBusinessIdentification().data(new CreateBusinessIdentificationData()
                .brandName(RandomStringUtils.randomAlphanumeric(80))
                .companyName(RandomStringUtils.randomAlphanumeric(70))
                .tradeName(RandomStringUtils.randomAlphanumeric(70))
                .incorporationDate(BankLambdaUtils.localDateToOffsetDate(LocalDate.now().minusDays(random.nextInt(3650))))
                .cnpjNumber(RandomStringUtils.randomNumeric(14))
                .companyCnpjNumber(List.of(RandomStringUtils.randomNumeric(14), RandomStringUtils.randomNumeric(14)))
                .otherDocuments(List.of(createBusinessOtherDocuments(), createBusinessOtherDocuments()))
                .parties(cpfs.collect { createBusinessParties(it) })
                .contacts(createBusinessContacts()))
    }

    static EditedBusinessIdentificationData editBusinessIdentifications() {
        new EditedBusinessIdentificationData()
                .brandName(RandomStringUtils.randomAlphanumeric(80))
                .companyName(RandomStringUtils.randomAlphanumeric(70))
                .tradeName(RandomStringUtils.randomAlphanumeric(70))
                .incorporationDate(BankLambdaUtils.localDateToOffsetDate(LocalDate.now().minusDays(random.nextInt(3650))))
                .cnpjNumber(RandomStringUtils.randomNumeric(14))
                .companyCnpjNumber(List.of(RandomStringUtils.randomNumeric(14), RandomStringUtils.randomNumeric(14)))
                .otherDocuments(List.of(createBusinessOtherDocuments(), createBusinessOtherDocuments()))
                .parties(List.of(createBusinessParties(), createBusinessParties()))
                .contacts(createBusinessContacts())
    }

    static BusinessOtherDocument createBusinessOtherDocuments() {
        new BusinessOtherDocument()
                .type(RandomStringUtils.randomAlphanumeric(20))
                .number(RandomStringUtils.randomAlphanumeric(20))
                .country(RandomStringUtils.randomAlphanumeric(3))
                .expirationDate(LocalDate.now().plusDays(random.nextInt(3650)))
    }

    static PartiesParticipation createBusinessParties() {
        return createBusinessParties(RandomStringUtils.randomAlphanumeric(20))
    }

    static PartiesParticipation createBusinessParties(String cpf) {
        new PartiesParticipation()
                .personType(PartiesParticipation.PersonTypeEnum.values()[random.nextInt(PartiesParticipation.PersonTypeEnum.values().size())])
                .type(PartiesParticipation.TypeEnum.values()[random.nextInt(PartiesParticipation.TypeEnum.values().size())])
                .civilName(RandomStringUtils.randomAlphanumeric(70))
                .socialName(RandomStringUtils.randomAlphanumeric(70))
                .companyName(RandomStringUtils.randomAlphanumeric(70))
                .tradeName(RandomStringUtils.randomAlphanumeric(70))
                .startDate(BankLambdaUtils.localDateToOffsetDate(LocalDate.now().minusDays(random.nextInt(3650))))
                .shareholding("0.51")
                .documentType(EnumPartiesParticipationDocumentType.values()[random.nextInt(EnumPartiesParticipationDocumentType.values().size())])
                .documentNumber(cpf)
                .documentAdditionalInfo(RandomStringUtils.randomAlphanumeric(100))
                .documentCountry(RandomStringUtils.randomAlphanumeric(3))
                .documentExpirationDate(LocalDate.now().plusDays(random.nextInt(3650)))
                .documentIssueDate(LocalDate.now().minusDays(random.nextInt(3650)))
    }

    static BusinessContacts createBusinessContacts() {
        new BusinessContacts()
                .postalAddresses(List.of(createBusinessPostalAddresses(), createBusinessPostalAddresses()))
                .phones(List.of(createPhones(), createPhones()))
                .emails(List.of(createEmails(), createEmails()))
    }

    static BusinessPostalAddress createBusinessPostalAddresses() {
        new BusinessPostalAddress()
                .isMain(random.nextBoolean())
                .address(RandomStringUtils.randomAlphanumeric(150))
                .additionalInfo(RandomStringUtils.randomAlphanumeric(30))
                .districtName(RandomStringUtils.randomAlphanumeric(50))
                .townName(RandomStringUtils.randomAlphanumeric(50))
                .ibgeTownCode(RandomStringUtils.randomNumeric(7))
                .countrySubDivision(EnumCountrySubDivision.values()[random.nextInt(EnumCountrySubDivision.values().size())])
                .postCode(RandomStringUtils.randomAlphanumeric(8))
                .country(RandomStringUtils.randomAlphanumeric(80))
                .countryCode(RandomStringUtils.randomAlphanumeric(3))
                .geographicCoordinates(createGeographicCoordinates())
    }

    static BusinessFinancialRelations createBusinessFinancialRelations() {
        new BusinessFinancialRelations().data(new BusinessFinancialRelationsData()
                .startDate(BankLambdaUtils.localDateToOffsetDate(LocalDate.now().minusDays(random.nextInt(3650))))
                .productsServicesType(List.of(EnumProductServiceType.CONTA_DEPOSITO_A_VISTA))
                .procurators(List.of(createBusinessProcurators())))
    }

    static BusinessFinancialRelations createBusinessFinancialRelations(String... cpfs) {
        new BusinessFinancialRelations().data(new BusinessFinancialRelationsData()
                .startDate(BankLambdaUtils.localDateToOffsetDate(LocalDate.now().minusDays(random.nextInt(3650))))
                .productsServicesType(List.of(EnumProductServiceType.CONTA_DEPOSITO_A_VISTA))
                .procurators(cpfs.collect { createBusinessProcurators(it) }))
    }

    static BusinessProcurator createBusinessProcurators(String cpf) {
        new BusinessProcurator()
                .type(BusinessProcurator.TypeEnum.PROCURADOR)
                .cnpjCpfNumber(cpf)
                .civilName(RandomStringUtils.randomAlphanumeric(70))
                .socialName(RandomStringUtils.randomAlphanumeric(70))
    }

    static BusinessProcurator createBusinessProcurators() {
        new BusinessProcurator()
                .type(BusinessProcurator.TypeEnum.PROCURADOR)
                .cnpjCpfNumber(RandomStringUtils.randomNumeric(11))
                .civilName(RandomStringUtils.randomAlphanumeric(70))
                .socialName(RandomStringUtils.randomAlphanumeric(70))
    }

    static BusinessQualifications createBusinessQualifications() {
        new BusinessQualifications().data(new BusinessQualificationsData()
                .informedRevenueFrequency(EnumInformedRevenueFrequency.MENSAL)
                .informedRevenueFrequencyAdditionalInfo(RandomStringUtils.randomAlphanumeric(100))
                .informedRevenueAmount(random.nextInt(1000))
                .informedRevenueCurrency(RandomStringUtils.randomAlphanumeric(3))
                .informedRevenueYear(BigDecimal.valueOf(random.nextInt(2022)))
                .informedPatrimonyAmount(random.nextInt(1000))
                .informedPatrimonyCurrency(RandomStringUtils.randomAlphanumeric(3))
                .informedPatrimonyDate(LocalDate.now().minusDays(random.nextInt(3650)).toString())
                .economicActivities(List.of(createEconomicActivity(), createEconomicActivity())))

    }

    static RiskSignals createEnrollmentRiskSignal() {
        new RiskSignals().data(new RiskSignalsData()
                .deviceId(RandomStringUtils.randomAlphanumeric(3))
                .isRootedDevice(false))

    }

    static UpdateEnrollment createUpdateEnrollment(EnumEnrollmentStatus status) {
        new UpdateEnrollment().data(new UpdateEnrollmentData()
                .status(status))
    }

    static EnrollmentFidoOptionsInput createEnrollmentFidoOptionsInput() {
        new EnrollmentFidoOptionsInput().data(new EnrollmentFidoOptionsInputData()
                .platform(EnrollmentFidoOptionsInputData.PlatformEnum.ANDROID)
                .rp("mock-tpp-1.raidiam.com"))

    }


    static EnrollmentFidoSignOptionsInput createEnrollmentFidoSignOptionsInput(String consentId) {
        new EnrollmentFidoSignOptionsInput().data(new EnrollmentFidoSignOptionsInputData()
                .consentId(consentId)
                .platform(EnrollmentFidoSignOptionsInputData.PlatformEnum.ANDROID)
                .rp("mock-tpp-1.raidiam.com")
        )
    }

    static EnrollmentFidoSignOptionsInput createEnrollmentFidoSignOptionsInput() {
        createEnrollmentFidoSignOptionsInput("urn:bancoex:C1DD33123")
    }


    static EconomicActivity createEconomicActivity() {
        new EconomicActivity()
                .isMain(random.nextBoolean())
                .code(new BigDecimal(random.nextInt(1000)))
    }

    static CreateContract createEmptyContract(EnumContractType type) {
        def contract = new CreateContractData()
                .contractNumber(RandomStringUtils.randomAlphanumeric(10))
                .ipocCode(RandomStringUtils.randomAlphanumeric(10))
                .productName(RandomStringUtils.randomAlphanumeric(10))
                .contractType(type)
                .contractDate(LocalDate.now().minusDays(random.nextInt(3650)))
                .disbursementDate(LocalDate.now().minusDays(random.nextInt(3650)))
                .settlementDate(LocalDate.now().minusDays(random.nextInt(3650)))
                .contractAmount(random.nextInt(1000))
                .currency(RandomStringUtils.randomAlphanumeric(3))
                .dueDate(LocalDate.now().minusDays(random.nextInt(3650)))
                .instalmentPeriodicity(EnumContractInstalmentPeriodicity.values()[random.nextInt(EnumContractInstalmentPeriodicity.values().size())])
                .instalmentPeriodicityAdditionalInfo(RandomStringUtils.randomAlphanumeric(10))
                .firstInstalmentDueDate(LocalDate.now().minusDays(random.nextInt(3650)))
                .cet(0.150001)
                .amortizationScheduled(EnumContracttAmortizationScheduled.values()[random.nextInt(EnumContracttAmortizationScheduled.values().size())])
                .amortizationScheduledAdditionalInfo(RandomStringUtils.randomAlphanumeric(10))
                .companyCnpj(RandomStringUtils.randomAlphanumeric(10))
                .status("AVAILABLE")
                .paidInstalments(BigDecimal.valueOf(random.nextInt(1000)))
                .contractOutstandingBalance(random.nextInt(1000))
                .typeNumberOfInstalments(EnumTypeNumberOfInstalments.values()[random.nextInt(EnumTypeNumberOfInstalments.values().size())])
                .totalNumberOfInstalments(BigDecimal.valueOf(random.nextInt(1000)))
                .typeContractRemaining(EnumTypeContractRemaining.values()[random.nextInt(EnumTypeContractRemaining.values().size())])
                .contractRemainingNumber(BigDecimal.valueOf(random.nextInt(1000)))
                .dueInstalments(BigDecimal.valueOf(random.nextInt(1000)))
                .pastDueInstalments(BigDecimal.valueOf(random.nextInt(1000)))
                .interestRates(List.of())
                .contractedFees(List.of())
                .contractedFinanceCharges(List.of())
                .balloonPayments(List.of())
                .releases(List.of())

        switch (type) {
            case EnumContractType.LOAN: {
                contract.setProductType(EnumContractProductTypeLoans.values()[random.nextInt(EnumContractProductTypeLoans.values().size())].toString())
                contract.setProductSubType(EnumContractProductSubTypeLoans.values()[random.nextInt(EnumContractProductSubTypeLoans.values().size())].toString())
                break
            }
            case EnumContractType.FINANCING: {
                contract.setProductType(EnumProductType.values()[random.nextInt(EnumProductType.values().size())].toString())
                contract.setProductSubType(EnumProductSubType.values()[random.nextInt(EnumProductSubType.values().size())].toString())
                break
            }
            case EnumContractType.INVOICE_FINANCING: {
                contract.setProductType(EnumContractProductTypeInvoiceFinancings.values()[random.nextInt(EnumContractProductTypeInvoiceFinancings.values().size())].toString())
                contract.setProductSubType(EnumContractProductSubTypeInvoiceFinancings.values()[random.nextInt(EnumContractProductSubTypeInvoiceFinancings.values().size())].toString())
                break
            }
            case EnumContractType.UNARRANGED_ACCOUNT_OVERDRAFT: {
                contract.setProductType(ProductType.values()[random.nextInt(ProductType.values().size())].toString())
                contract.setProductSubType(ProductSubType.values()[random.nextInt(ProductSubType.values().size())].toString())
                break
            }
        }

        return new CreateContract().data(contract)
    }

    static CreateWebhook createWebhook() {
        return new CreateWebhook().data(new CreateWebhookData().webhookUri("https://web.conformance.directory.openbankingbrasil.org.br/test-mtls/a/obbsb"))
    }
}