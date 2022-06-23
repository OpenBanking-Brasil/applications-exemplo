package com.raidiam.trustframework.bank
import com.raidiam.trustframework.mockbank.models.generated.*

import java.time.LocalDate
import java.time.OffsetDateTime

class TestRequestDataFactory {

    static AccountBalancesData anAccountBalances() {

        AccountBalancesData accountReq = new AccountBalancesData()
        accountReq.setAvailableAmount(3)
        accountReq.setAvailableAmountCurrency("AvailableAmountCurrency")
        accountReq.setBlockedAmount(4)
        accountReq.setBlockedAmountCurrency("BlockedAmountCurrency")
        accountReq.setAutomaticallyInvestedAmount(5)
        accountReq.setAutomaticallyInvestedAmountCurrency("AutomaticallyInvestedAmountCurrency")
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
                .loggedUser(new LoggedUser().document(new LoggedUserDocument().rel(loggedUserRel).identification(loggedUserIdentification)))
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

    static createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum status, boolean withDebtor) {
        return new UpdatePaymentConsent().data(new UpdatePaymentConsentData().status(status)
                .debtorAccount(withDebtor ? new DebtorAccount().accountType(EnumAccountPaymentsType.SLRY).ispb("12341234").issuer("1234").number("1234567890") : null))
    }

    static createPaymentRequest (String creditorAcNumber,
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
        CreatePixPaymentData createPixPaymentData = new CreatePixPaymentData()
                .creditorAccount(new CreditorAccount().number(creditorAcNumber).issuer(creditorIssuer).ispb(creditorIspb).accountType(creditorAcType))
                .localInstrument(localInstrument)
                .payment(new PaymentPix().amount(amount).currency(currency))
                .proxy(proxy)
                .qrCode(qrcode)
                .cnpjInitiator(cnpjInitiator)
                .remittanceInformation(remittanceInformation)
                .transactionIdentification(transactionIdentification)
        return new CreatePixPayment().data(createPixPaymentData)
    }

    static createPaymentUpdateRequest(EnumPaymentStatusType status) {
        UpdatePixPaymentData updatePixPaymentData = new UpdatePixPaymentData().status(status)
        return new UpdatePixPayment().data(updatePixPaymentData)
    }

    static createPaymentConsentResponse (String consentId,
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
                .loggedUser(new LoggedUser().document(new LoggedUserDocument().identification(loggedUserId).rel(loggedUserRel)))
                .businessEntity(new BusinessEntity().document(new BusinessEntityDocument().identification(businessId).rel(businessRel)))
                .creditor(new Identification().personType(personType.toString()).cpfCnpj(cpf).name(name))
                .payment(new PaymentConsent().currency(currency).amount(amount).date(paymentDate).type(paymentType))
                .debtorAccount(new DebtorAccount().ispb(ispb).issuer(issuer).number(number).accountType(accountType)))
    }

    static createPaymentConsentResponseFull (String consentId,
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
                .loggedUser(new LoggedUser().document(new LoggedUserDocument().identification(loggedUserId).rel(loggedUserRel)))
                .businessEntity(new BusinessEntity().document(new BusinessEntityDocument().identification(businessId).rel(businessRel)))
                .creditor(new Identification().personType(personType.toString()).cpfCnpj(cpf).name(name))
                .payment(new PaymentConsent().currency(currency).amount(amount).date(paymentDate).type(paymentType))
                .debtorAccount(new DebtorAccount().ispb(ispb).issuer(issuer).number(number).accountType(accountType)))
    }

    static createPaymentConsentResponseSchedule (String consentId,
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
                .loggedUser(new LoggedUser().document(new LoggedUserDocument().identification(loggedUserId).rel(loggedUserRel)))
                .businessEntity(new BusinessEntity().document(new BusinessEntityDocument().identification(businessId).rel(businessRel)))
                .creditor(new Identification().personType(personType.toString()).cpfCnpj(cpf).name(name))
                .payment(new PaymentConsent().currency(currency).amount(amount).schedule(new Schedule().single(new Single().date(paymentDate))).type(paymentType.toString()))
                .debtorAccount(new DebtorAccount().ispb(ispb).issuer(issuer).number(number).accountType(accountType)))
    }

    static createPixPaymentResponse (String paymentId,
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

    static CreatePaymentConsent testPaymentConsent(String documentIdentification, String documentRel) {
         new CreatePaymentConsent().data(new CreatePaymentConsentData()
                .businessEntity(
                        new BusinessEntity().document(
                                new BusinessEntityDocument().identification("12345678901235")
                                        .rel("CNPJ")))
                .creditor(new Identification().cpfCnpj("12345678904").name("Bob Creditor").personType(EnumCreditorPersonType.NATURAL.toString()))
                .loggedUser(new LoggedUser().document(new LoggedUserDocument().rel(documentRel).identification(documentIdentification)))
                .payment(new PaymentConsent()
                        .type(EnumPaymentType.PIX.toString())
                        .schedule(new Schedule().single(new Single().date(LocalDate.now().plusDays(1))))
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
                .loggedUser(new LoggedUser().document(new LoggedUserDocument().rel(accountHolderRel).identification(accountHolderIdentification)))
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
                .loggedUser(new LoggedUser().document(new LoggedUserDocument().rel("CPF").identification("12345678905")))
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

    static CreatePixPayment testPixPayment() {
        CreatePixPaymentData createPixPaymentData = new CreatePixPaymentData()
                .creditorAccount(new CreditorAccount().number("123456789012").issuer("1234").ispb("12345678").accountType(EnumAccountPaymentsType.CACC))
                .localInstrument(EnumLocalInstrument.MANU)
                .payment(new PaymentPix().amount("100.00").currency("BRL"))
                .proxy("proxy")
                .qrCode("qrcode")
                .remittanceInformation("remittanceInfo")
        new CreatePixPayment().data(createPixPaymentData)
    }

    static ResponsePaymentConsent testPaymentConsentResponse() {
         new ResponsePaymentConsent()
            .data(new ResponsePaymentConsentData()
                .expirationDateTime(OffsetDateTime.now())
                .statusUpdateDateTime(OffsetDateTime.now())
                .status(ResponsePaymentConsentData.StatusEnum.AUTHORISED)
                .consentId("consent1")
                .loggedUser(new LoggedUser().document(new LoggedUserDocument().identification("LUID1").rel("CPF")))
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

    static PatchPaymentsConsent testPatchPaymentConsent(){
        new PatchPaymentsConsent()
            .data(new PatchPaymentsConsentData()
                .status(EnumAuthorisationPatchStatusType.REVOKED)
                .revocation(new Revocation().loggedUser(new LoggedUser().document(new LoggedUserDocument().identification("12345678910").rel("CPF")))
                    .revokedBy(EnumRevokedBy.USER)
                        .reason(new Reason().code(EnumRevocationReason.OTHER).additionalInformation("Some additionalInformation"))
                )
            )
    }

    static PatchPaymentsConsent testPatchPaymentConsentNoLoggedUser(){
        new PatchPaymentsConsent()
                .data(new PatchPaymentsConsentData()
                        .status(EnumAuthorisationPatchStatusType.REVOKED)
                        .revocation(new Revocation()
                                .revokedBy(EnumRevokedBy.USER)
                                .reason(new Reason().code(EnumRevocationReason.OTHER).additionalInformation("Some additionalInformation"))
                        )
                )
    }

    static PatchPaymentsConsent testPatchPaymentConsentNoAdditionalReason(){
        new PatchPaymentsConsent()
                .data(new PatchPaymentsConsentData()
                        .status(EnumAuthorisationPatchStatusType.REVOKED)
                        .revocation(new Revocation()
                                .revokedBy(EnumRevokedBy.TPP)
                                .reason(new Reason().code(EnumRevocationReason.OTHER))
                        )
                )
    }

    static PatchPaymentsConsent testPatchPaymentConsentRevocationReasonNotAllowed(){
        new PatchPaymentsConsent()
                .data(new PatchPaymentsConsentData()
                        .status(EnumAuthorisationPatchStatusType.REVOKED)
                        .revocation(new Revocation().loggedUser(new LoggedUser().document(new LoggedUserDocument().identification("12345678910").rel("CPF")))
                                .revokedBy(EnumRevokedBy.USER)
                                .reason(new Reason().code(EnumRevocationReason.FRAUD).additionalInformation("Some additionalInformation"))
                        )
                )
    }

    static createPatchPaymentRequest (EnumAuthorisationPatchStatusType revoked,
                                     String identification,
                                     String rel,
                                     EnumRevocationReason revokedReason,
                                     EnumRevokedBy revokedBy,
                                     String additionalInfo) {
        return new PatchPaymentsConsent()
                .data(new PatchPaymentsConsentData()
                        .status(revoked)
                        .revocation(new Revocation().loggedUser(new LoggedUser().document(new LoggedUserDocument().identification(identification).rel(rel)))
                                .revokedBy(revokedBy)
                                .reason(new Reason().code(revokedReason).additionalInformation(additionalInfo))
                        )
                )
    }

    static createConsentRequest(String businessIdentityDocumentIdentification,
                                String businessIdentityDocumentREL,
                                String loggedUserIdentification,
                                String loggedUserRel,
                                OffsetDateTime expirationDateTime,
                                List<CreateConsentData.PermissionsEnum> permissions) {
        CreateConsentData consentRequestData = new CreateConsentData()
                .businessEntity(
                        new BusinessEntity().document(
                                new BusinessEntityDocument().identification(businessIdentityDocumentIdentification)
                                        .rel(businessIdentityDocumentREL)))
                .loggedUser(new LoggedUser().document(new LoggedUserDocument().rel(loggedUserRel).identification(loggedUserIdentification)))
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
}
