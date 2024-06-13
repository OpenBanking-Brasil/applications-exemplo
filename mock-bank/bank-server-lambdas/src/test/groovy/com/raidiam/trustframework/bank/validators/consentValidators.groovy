package com.raidiam.trustframework.bank.validators

import com.raidiam.trustframework.bank.services.validate.ConsentCreditorFieldValidator
import com.raidiam.trustframework.bank.services.validate.ConsentPaymentFieldValidator
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import spock.lang.Specification

import java.time.LocalDate

class consentValidators extends Specification {
    ConsentCreditorFieldValidator creditorFieldValidator = new ConsentCreditorFieldValidator()
    ConsentPaymentFieldValidator consentPaymentFieldValidator = new ConsentPaymentFieldValidator()

    def "We can validate all person types"(){
        given:
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
                .number("12341234")

        LoggedUser lu = new LoggedUser()
                .document(
                        new Document()
                                .rel("CPF")
                                .identification("12345678901")
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

        creditorFieldValidator.validate(paymentConsentRequest)

        then:
        noExceptionThrown()

        when:
        id.setPersonType(EnumCreditorPersonType.JURIDICA.toString())
        paymentConsentRequest = new CreatePaymentConsent()
                .data(new CreatePaymentConsentData()
                        .businessEntity(be)
                        .creditor(id)
                        .debtorAccount(da)
                        .loggedUser(lu)
                        .payment(pc)
                )

        creditorFieldValidator.validate(paymentConsentRequest)

        then:
        noExceptionThrown()

        when:
        id.setPersonType("BADDDD")
        paymentConsentRequest = new CreatePaymentConsent()
                .data(new CreatePaymentConsentData()
                        .businessEntity(be)
                        .creditor(id)
                        .debtorAccount(da)
                        .loggedUser(lu)
                        .payment(pc)
                )

        creditorFieldValidator.validate(paymentConsentRequest)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "DETAIL_PGTO_INVALID: O campo creditorConta - personType não preenche os requisitos de preenchimento."

        when:
        id.setPersonType(EnumCreditorPersonType.NATURAL.toString())
        pc.setType(EnumPaymentType.PIX.toString())
        pc.getDetails().setProxy(null)
        pc.getDetails().setQrCode("qrcode")
        pc.getDetails().setLocalInstrument(EnumLocalInstrument.INIC)

        paymentConsentRequest = new CreatePaymentConsent()
                .data(new CreatePaymentConsentData()
                        .businessEntity(be)
                        .creditor(id)
                        .debtorAccount(da)
                        .loggedUser(lu)
                        .payment(pc)
                )
        consentPaymentFieldValidator.validate(paymentConsentRequest)

        then:
        e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "DATA_PGTO_INVALIDA: O campo qrCode não preenche os requisitos de preenchimento. qrCode não deve estar presente para o localInstrument do tipo INIC."

        when:
        id.setPersonType(EnumCreditorPersonType.NATURAL.toString())
        pc.getDetails().setProxy(null)
        pc.getDetails().setQrCode(null)
        pc.getDetails().setLocalInstrument(EnumLocalInstrument.INIC)

        paymentConsentRequest = new CreatePaymentConsent()
                .data(new CreatePaymentConsentData()
                        .businessEntity(be)
                        .creditor(id)
                        .debtorAccount(da)
                        .loggedUser(lu)
                        .payment(pc)
                )
        consentPaymentFieldValidator.validate(paymentConsentRequest)

        then:
        e = thrown()
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "DATA_PGTO_INVALIDA: O campo proxy não preenche os requisitos de preenchimento. proxy deve estar presente para o localInstrument do tipo INIC."

        when:
        id.setPersonType(EnumCreditorPersonType.NATURAL.toString())
        pc.getDetails().setProxy("proxy")
        pc.getDetails().setLocalInstrument(EnumLocalInstrument.INIC)

        paymentConsentRequest = new CreatePaymentConsent()
                .data(new CreatePaymentConsentData()
                        .businessEntity(be)
                        .creditor(id)
                        .debtorAccount(da)
                        .loggedUser(lu)
                        .payment(pc)
                )
        consentPaymentFieldValidator.validate(paymentConsentRequest)

        then:
        noExceptionThrown()
    }

}
