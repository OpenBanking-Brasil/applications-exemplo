package com.raidiam.trustframework.bank.controllers

import com.raidiam.trustframework.bank.utils.BankLambdaUtils
import com.raidiam.trustframework.mockbank.models.generated.*
import org.apache.commons.lang3.RandomStringUtils

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

class ConsentFactory {

    static CreateConsent createConsent() {
        createConsent(RandomStringUtils.random(11, false, true), "ASD", null)
    }

    static CreateConsent createConsent(String loggedUserIdentification, String loggedUserRel, List<EnumConsentPermissions> permissions) {
        CreateConsentData consentData = new CreateConsentData()
        if (permissions != null) {
            consentData.setPermissions(permissions)
        } else {
            consentData.setPermissions([EnumConsentPermissions.RESOURCES_READ,
                                        EnumConsentPermissions.ACCOUNTS_READ,
                                        EnumConsentPermissions.ACCOUNTS_BALANCES_READ])
        }
        consentData.setExpirationDateTime((OffsetDateTime.now().plusDays(3L)))
        CreateConsent consentReq = new CreateConsent().data(consentData)
        consentReq.data.businessEntity(new BusinessEntity()
                .document(new BusinessEntityDocument()
                        .identification(RandomStringUtils.random(14, false, true))
                        .rel("ASDF")))
        consentReq.data.loggedUser(new LoggedUser()
                .document(new Document()
                        .identification(loggedUserIdentification)
                        .rel(loggedUserRel)
                ))
        consentReq
    }

    static CreateConsentV2 createConsentV2(String loggedUserIdentification, String loggedUserRel, List<EnumConsentPermissions> permissions) {
        CreateConsentV2Data consentData = new CreateConsentV2Data()
        if (permissions != null) {
            consentData.setPermissions(permissions)
        } else {
            consentData.setPermissions([EnumConsentPermissions.RESOURCES_READ,
                                        EnumConsentPermissions.ACCOUNTS_READ,
                                        EnumConsentPermissions.ACCOUNTS_BALANCES_READ])
        }
        consentData.setExpirationDateTime((OffsetDateTime.now().plusDays(3L)))
        CreateConsentV2 consentReq = new CreateConsentV2().data(consentData)
        consentReq.data.businessEntity(new BusinessEntity()
                .document(new BusinessEntityDocument()
                        .identification(RandomStringUtils.random(14, false, true))
                        .rel("ASDF")))
        consentReq.data.loggedUser(new LoggedUser()
                .document(new Document()
                        .identification(loggedUserIdentification)
                        .rel(loggedUserRel)
                ))
        consentReq
    }

    static CreatePaymentConsentV4 createPaymentConsentV4(String loggedUserIdentification, String loggedUserRel) {
        def pc = new PaymentConsentV4Payment()
                .type(EnumPaymentType.PIX)
                .date(new Date().toInstant().atZone(ZoneId.of("America/Sao_Paulo")).toLocalDate())
                .currency("BRL")
                .amount("100.00")
                .details(
                        new Details()
                                .localInstrument(EnumLocalInstrument.DICT)
                                .proxy("proxy")
                                .creditorAccount(new CreditorAccount()
                                        .number("94088392")
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

        LoggedUser lu = new LoggedUser()
                .document(
                        new Document()
                                .rel(loggedUserRel)
                                .identification(loggedUserIdentification)
                )

        return new CreatePaymentConsentV4().data(new CreatePaymentConsentV4Data()
                .businessEntity(be)
                .creditor(id)
                .loggedUser(lu)
                .payment(pc)
        )
    }

    static CreateConsentExtends createConsentExtends() {
        createConsentExtends("12345678999", "CPF")
    }

    static CreateConsentExtends createConsentExtends(String loggedUserIdentification, String loggedUserRel) {
        new CreateConsentExtends()
                .data(new CreateConsentExtendsData()
                        .expirationDateTime(BankLambdaUtils.localDateToOffsetDate(LocalDate.now().plusMonths(1)))
                        .businessEntity(new BusinessEntity()
                                .document(new BusinessEntityDocument()
                                        .identification("12345678901234")
                                        .rel("CNPJ")))
                        .loggedUser(new LoggedUser()
                                .document(new Document()
                                        .identification(loggedUserIdentification)
                                        .rel(loggedUserRel)))
                )
    }

    static CreateConsentExtendsV3 createConsentExtendsV3() {
        createConsentExtendsV3("12345678999", "CPF")
    }

    static CreateConsentExtendsV3 createConsentExtendsV3(String loggedUserIdentification, String loggedUserRel) {
        new CreateConsentExtendsV3()
                .data(new CreateConsentExtendsV3Data()
                        .expirationDateTime(BankLambdaUtils.localDateToOffsetDate(LocalDate.now().plusMonths(1)))
                        .businessEntity(new BusinessEntity()
                                .document(new BusinessEntityDocument()
                                        .identification("12345678901234")
                                        .rel("CNPJ")))
                        .loggedUser(new LoggedUser()
                                .document(new Document()
                                        .identification(loggedUserIdentification)
                                        .rel(loggedUserRel)))
                )
    }

    static CreateConsentExtendsV3 createConsentExtendsV31WeekExpiration() {
        new CreateConsentExtendsV3()
                .data(new CreateConsentExtendsV3Data()
                        .expirationDateTime(BankLambdaUtils.localDateToOffsetDate(LocalDate.now().plusWeeks(1)))
                        .businessEntity(new BusinessEntity()
                                .document(new BusinessEntityDocument()
                                        .identification("12345678901234")
                                        .rel("CNPJ")))
                        .loggedUser(new LoggedUser()
                                .document(new Document()
                                        .identification("12345678999")
                                        .rel("CPF")))
                )
    }

    static CreateConsentV3 createConsentV3(String loggedUserIdentification, String loggedUserRel, List<EnumConsentPermissions> permissions) {
        CreateConsentV3Data consentData = new CreateConsentV3Data()
        if (permissions != null) {
            consentData.setPermissions(permissions)
        } else {
            consentData.setPermissions([EnumConsentPermissions.RESOURCES_READ,
                                        EnumConsentPermissions.ACCOUNTS_READ,
                                        EnumConsentPermissions.ACCOUNTS_BALANCES_READ])
        }
        consentData.setExpirationDateTime((OffsetDateTime.now().plusDays(3L)))
        CreateConsentV3 consentReq = new CreateConsentV3().data(consentData)
        consentReq.data.businessEntity(new BusinessEntity()
                .document(new BusinessEntityDocument()
                        .identification("12345678901234")
                        .rel("CNPJ")))
        consentReq.data.loggedUser(new LoggedUser()
                .document(new Document()
                        .identification(loggedUserIdentification)
                        .rel(loggedUserRel)
                ))
        consentReq
    }

}
