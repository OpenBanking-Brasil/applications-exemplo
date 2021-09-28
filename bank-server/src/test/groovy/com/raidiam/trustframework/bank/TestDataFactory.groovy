package com.raidiam.trustframework.bank

import com.raidiam.trustframework.bank.models.generated.AccountSubType
import com.raidiam.trustframework.bank.models.generated.AccountType
import com.raidiam.trustframework.bank.models.generated.Level1Account
import com.raidiam.trustframework.bank.models.generated.Level1AccountStatus
import com.raidiam.trustframework.bank.models.generated.Level2Account
import com.raidiam.trustframework.bank.models.generated.Level2Accounts
import com.raidiam.trustframework.bank.models.generated.Servicer
import com.raidiam.trustframework.mockbank.models.generated.BusinessEntity
import com.raidiam.trustframework.mockbank.models.generated.BusinessEntityDocument
import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsent
import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsentData
import com.raidiam.trustframework.mockbank.models.generated.CreatePixPayment
import com.raidiam.trustframework.mockbank.models.generated.CreatePixPaymentData
import com.raidiam.trustframework.mockbank.models.generated.CreditorAccount
import com.raidiam.trustframework.mockbank.models.generated.DebtorAccount
import com.raidiam.trustframework.mockbank.models.generated.EnumAccountPaymentsType
import com.raidiam.trustframework.mockbank.models.generated.EnumLocalInstrument
import com.raidiam.trustframework.mockbank.models.generated.Identification
import com.raidiam.trustframework.mockbank.models.generated.LoggedUser
import com.raidiam.trustframework.mockbank.models.generated.LoggedUserDocument
import com.raidiam.trustframework.mockbank.models.generated.PaymentConsent
import com.raidiam.trustframework.mockbank.models.generated.PaymentPix
import com.raidiam.trustframework.mockbank.models.generated.ResponsePaymentConsent
import com.raidiam.trustframework.mockbank.models.generated.ResponsePaymentConsentData
import com.raidiam.trustframework.mockbank.models.generated.ResponsePixPayment
import com.raidiam.trustframework.mockbank.models.generated.ResponsePixPaymentData
import com.raidiam.trustframework.mockbank.models.generated.UpdatePaymentConsent
import com.raidiam.trustframework.mockbank.models.generated.UpdatePaymentConsentData
import com.raidiam.trustframework.mockbank.models.generated.UpdatePixPayment
import com.raidiam.trustframework.mockbank.models.generated.UpdatePixPaymentData

import java.time.LocalDate
import java.time.OffsetDateTime;

public class TestDataFactory {

    static Level1Account anAccount() {
        Level1Account accountReq = new Level1Account()
        accountReq.setAccountSubType(AccountSubType.CHARGECARD)
        accountReq.setAccountType(AccountType.BUSINESS)
        accountReq.setCurrency("GBP")
        accountReq.setDescription("TestL1Account")
        accountReq.setMaturityDate(OffsetDateTime.now())
        accountReq.setNickname("TestL1Account")
        accountReq.setOpeningDate(OffsetDateTime.now())
        accountReq.setStatus(Level1AccountStatus.ENABLED)
        accountReq.setStatusUpdateDateTime(OffsetDateTime.now())
        accountReq.setSwitchStatus(Level1Account.SwitchStatusEnum.SWITCHCOMPLETED)
        accountReq.setServicer(new Servicer().schemeName("scheme1"))

        Level2Account accountPriv = new Level2Account()
        accountPriv.identification("TestL2Account")
        accountPriv.name("TestL2Account")
        accountPriv.schemeName("TestL2Account")
        accountPriv.secondaryIdentification("TestL2Account")
        Level2Accounts accountsPriv = new Level2Accounts()
        accountsPriv.add(accountPriv)
        accountReq.setAccount(accountsPriv)
        accountReq
    }

    static createPaymentConsentRequest(String businessIdentityDocumentIdentification,
                                       String businessIdentityDocumentREL,
                                       String cpfCnpj,
                                       String name,
                                       Identification.PersonTypeEnum personType,
                                       EnumAccountPaymentsType accountType,
                                       String debtorIspb,
                                       String debtorIssuer,
                                       String debtorAccountNumber,
                                       String loggedUserRel,
                                       String loggedUserIdentification,
                                       PaymentConsent.TypeEnum paymentConsentType,
                                       LocalDate paymentConsentDate,
                                       String currency,
                                       String amount) {
        CreatePaymentConsentData paymentConsentRequestData = new CreatePaymentConsentData()
                .businessEntity(
                        new BusinessEntity().document(
                                new BusinessEntityDocument().identification(businessIdentityDocumentIdentification)
                                        .rel(businessIdentityDocumentREL)))
                .creditor(new Identification().cpfCnpj(cpfCnpj).name(name).personType(personType))
                .debtorAccount(new DebtorAccount().accountType(accountType).ispb(debtorIspb).issuer(debtorIssuer).number(debtorAccountNumber))
                .loggedUser(new LoggedUser().document(new LoggedUserDocument().rel(loggedUserRel).identification(loggedUserIdentification)))
                .payment(new PaymentConsent().type(paymentConsentType).date(paymentConsentDate).currency(currency).amount(amount))
        return new CreatePaymentConsent().data(paymentConsentRequestData)
    }

    static createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum status) {
        return new UpdatePaymentConsent().data(new UpdatePaymentConsentData().status(status))
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
                                 String remittanceInformation) {
        CreatePixPaymentData createPixPaymentData = new CreatePixPaymentData()
                .creditorAccount(new CreditorAccount().number(creditorAcNumber).issuer(creditorIssuer).ispb(creditorIspb).accountType(creditorAcType))
                .localInstrument(localInstrument)
                .payment(new PaymentPix().amount(amount).currency(currency))
                .proxy(proxy)
                .qrCode(qrcode)
                .cnpjInitiator(cnpjInitiator)
                .remittanceInformation(remittanceInformation)
        return new CreatePixPayment().data(createPixPaymentData)
    }

    static createPaymentUpdateRequest(UpdatePixPaymentData.StatusEnum status) {
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
                                         Identification.PersonTypeEnum personType,
                                         String cpf,
                                         String name,
                                         String currency,
                                         String amount,
                                         LocalDate paymentDate,
                                         PaymentConsent.TypeEnum paymentType,
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
                .creditor(new Identification().personType(personType).cpfCnpj(cpf).name(name))
                .payment(new PaymentConsent().currency(currency).amount(amount).date(paymentDate).type(paymentType))
                .debtorAccount(new DebtorAccount().ispb(ispb).issuer(issuer).number(number).accountType(accountType)))
    }

    static createPixPaymentResponse (String paymentId,
                                     String endToEndId,
                                     String consentId,
                                     OffsetDateTime creation,
                                     OffsetDateTime statusUpdate,
                                     String proxy,
                                     ResponsePixPaymentData.StatusEnum status,
                                     ResponsePixPaymentData.RejectionReasonEnum rejectionReason,
                                     EnumLocalInstrument localInstrument,
                                     String amount,
                                     String currency,
                                     String remittanceInformation,
                                     String ispb,
                                     String issuer,
                                     String number,
                                     EnumAccountPaymentsType accountType) {
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
                .creditorAccount(new CreditorAccount().ispb(ispb).issuer(issuer).number(number).accountType(accountType)))
    }

    static CreatePaymentConsent testPaymentConsent() {
         new CreatePaymentConsent().data(new CreatePaymentConsentData()
                .businessEntity(
                        new BusinessEntity().document(
                                new BusinessEntityDocument().identification("12345678901235")
                                        .rel("CNPJ")))
                .creditor(new Identification().cpfCnpj("12345678904").name("Bob Creditor").personType(Identification.PersonTypeEnum.NATURAL))
                .loggedUser(new LoggedUser().document(new LoggedUserDocument().rel("CPF").identification("12345678905")))
                .payment(new PaymentConsent().type(PaymentConsent.TypeEnum.PIX).date(LocalDate.now()).currency("BRL").amount("100.00")))

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

}
