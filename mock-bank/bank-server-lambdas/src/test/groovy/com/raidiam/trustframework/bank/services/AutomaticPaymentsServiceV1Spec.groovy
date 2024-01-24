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
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime

import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccount
import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccountHolder

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class AutomaticPaymentsServiceV1Spec extends CleanupSpecification {

    @Inject
    PaymentsService paymentsService
    @Inject
    PaymentConsentService paymentConsentService

    @Shared
    AccountHolderEntity accountHolder
    @Shared
    AccountEntity account


    @MockBean(CnpjVerifier.class)
    CnpjVerifier rolesApiService() {
        return new CnpjVerifier() {
            @Override
            boolean isKnownCnpj(String cnpj) {
                return !("36386527000143" == cnpj)
            }
        }
    }


    def setup() {
        if (runSetup) {
            accountHolder = accountHolderRepository.save(anAccountHolder())
            account = accountRepository.save(anAccount(accountHolder))
            runSetup = false
        }
    }


    def "we can persist recurring payment consent sweeping"() {
        given:
        def req = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())
        def entity = PaymentConsentEntity.fromRecurringV1(req, "id", "key", account, accountHolder)

        when:
        entity = paymentConsentRepository.save(entity)
        def consentId = entity.getPaymentConsentId()

        then:
        noExceptionThrown()

        when:
        def dto = paymentConsentRepository.findByPaymentConsentId(consentId)
                .map { it.getRecurringDTOV1() }.orElse(null)

        then:
        noExceptionThrown()
        dto != null
        def data = dto.getData()
        data.getRecurringConsentId() == consentId
        def configuration = data.getRecurringConfiguration()
        configuration != null
        configuration.getSweeping().getTotalAllowedAmount() == req.getData().getRecurringConfiguration().getSweeping().getTotalAllowedAmount()
        configuration.getSweeping().getTransactionLimit() == req.getData().getRecurringConfiguration().getSweeping().getTransactionLimit()
        configuration.getSweeping().getPeriodicLimits() == req.getData().getRecurringConfiguration().getSweeping().getPeriodicLimits()
    }

    def "we can persist recurring payment consent vrp"() {
        given:
        def req = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithVrp(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())
        def entity = PaymentConsentEntity.fromRecurringV1(req, "id", "key", account, accountHolder)

        when:
        entity = paymentConsentRepository.save(entity)
        def consentId = entity.getPaymentConsentId()

        then:
        noExceptionThrown()

        when:
        def dto = paymentConsentRepository.findByPaymentConsentId(consentId)
                .map { it.getRecurringDTOV1() }.orElse(null)

        then:
        noExceptionThrown()
        dto != null
        def data = dto.getData()
        data.getRecurringConsentId() == consentId
        def configuration = data.getRecurringConfiguration()
        configuration != null
        configuration.getVrp().getTransactionLimit() == req.getData().getRecurringConfiguration().getVrp().getTransactionLimit()
        configuration.getVrp().getGlobalLimits().getTransactionLimit() == req.getData().getRecurringConfiguration().getVrp().getGlobalLimits().getTransactionLimit()
        configuration.getVrp().getGlobalLimits().getQuantityLimit() == req.getData().getRecurringConfiguration().getVrp().getGlobalLimits().getQuantityLimit()
        configuration.getVrp().getPeriodicLimits() == req.getData().getRecurringConfiguration().getVrp().getPeriodicLimits()
    }

    def "we can create a sweeping consent with business entity"() {
        given:
        def req = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())
        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                // CNPJ is the same
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.JURIDICA.toString())
                        .cpfCnpj("12345678901234"),
                // CNPJ prefix the same
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.JURIDICA.toString())
                        .cpfCnpj("12345678000000"),
        ))
        req.data.setCreditors(creditors)
        when:
        def response = paymentConsentService.createRecurringConsentV1(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        then:
        noExceptionThrown()
        response.data.recurringConsentId != null
        response.data.statusUpdateDateTime != null
        response.data.status == EnumAuthorisationStatusType.AWAITING_AUTHORISATION
        response.data.loggedUser.document.identification == req.data.loggedUser.document.identification
        response.data.loggedUser.document.rel == req.data.loggedUser.document.rel
        response.data.businessEntity.document.identification == req.data.businessEntity.document.identification
        response.data.businessEntity.document.rel == req.data.businessEntity.document.rel
        response.data.startDateTime != null
        response.data.expirationDateTime != null
        response.data.creationDateTime != null
        response.data.creditors.size() == req.data.creditors.size()
        response.data.creditors[0] == req.data.creditors[0]
        response.data.recurringConfiguration.vrp == null
        response.data.recurringConfiguration.automatic == null
        response.data.recurringConfiguration.sweeping == req.data.recurringConfiguration.sweeping
    }

    def "we can create a sweeping consent without business entity"() {
        given:
        def req = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())
        req.getData().setBusinessEntity(null)
        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
        ))
        req.data.setCreditors(creditors)
        when:
        def response = paymentConsentService.createRecurringConsentV1(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        then:
        noExceptionThrown()
        response.data.recurringConsentId != null
        response.data.statusUpdateDateTime != null
        response.data.status == EnumAuthorisationStatusType.AWAITING_AUTHORISATION
        response.data.loggedUser.document.identification == req.data.loggedUser.document.identification
        response.data.loggedUser.document.rel == req.data.loggedUser.document.rel
        response.data.businessEntity == null
        response.data.startDateTime != null
        response.data.expirationDateTime != null
        response.data.creationDateTime != null
        response.data.creditors.size() == req.data.creditors.size()
        response.data.creditors[0] == req.data.creditors[0]
        response.data.recurringConfiguration.vrp == null
        response.data.recurringConfiguration.automatic == null
        response.data.recurringConfiguration.sweeping == req.data.recurringConfiguration.sweeping
    }

    def "we cant create a consent without recurringConfiguration"() {
        given:
        def req = TestRequestDataFactory.createRecurringPaymentConsentRequestV1(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())
        when:
        def response = paymentConsentService.createRecurringConsentV1(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "PARAMETRO_NAO_INFORMADO: Exactly one of sweeping, vrp or automatic recurring configurations must be specified"
        response == null
    }

    // This test needs to be changed once we start supporting more configurations
    def "we cant create a consent if recurringConfiguration is vrp"() {
        given:
        def req = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithVrp(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())
        when:
        def response = paymentConsentService.createRecurringConsentV1(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "PARAMETRO_NAO_INFORMADO: Only sweeping recurring configuration is supported by mockbank"
        response == null
    }

    // This test needs to be changed once we start supporting more configurations
    def "we cant create a consent if recurringConfiguration is automatic"() {
        given:
        def req = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithAutomatic(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())
        req.getData().getRecurringConfiguration().getAutomatic().getImmediatePayment().setDate(LocalDate.now().minusDays(1))
        when:
        def response = paymentConsentService.createRecurringConsentV1(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "PARAMETRO_NAO_INFORMADO: Only sweeping recurring configuration is supported by mockbank"
        response == null
    }

    def "we cant create a consent if expiration date is in the past"() {
        given:
        def req = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())
        req.getData().setExpirationDateTime(OffsetDateTime.now().minusDays(360))
        when:
        def response = paymentConsentService.createRecurringConsentV1(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "PARAMETRO_INVALIDO: expirationDateTime cannot be in the past"
        response == null
    }

    def "we cant create a sweeping consent if creditors CPF is different than logged user CPF"() {
        given:
        def req = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())
        req.getData().setBusinessEntity(null)
        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj("00000000000"),
        ))
        req.data.setCreditors(creditors)
        when:
        def response = paymentConsentService.createRecurringConsentV1(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "PARAMETRO_INVALIDO: All Creditors have to have the same CPF as the logged user"
        response == null
    }

    def "we cant create a sweeping consent if creditors CNPJ is different than business entity CNPJ prefix"() {
        given:
        def req = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())
        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                // CNPJ is the same
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj("12345678901234"),
                // CNPJ prefix the same
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj("12345678000000"),
                // CNPJ is different
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj("00000000000000"),
        ))
        req.data.setCreditors(creditors)
        when:
        def response = paymentConsentService.createRecurringConsentV1(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "PARAMETRO_INVALIDO: All Creditors have to have the same CNPJ prefix as the business entity"
        response == null
    }

    def "we can retrieve recurring payment consent"() {
        given:
        def req = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())
        req.getData().setBusinessEntity(null)
        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
        ))
        req.data.setCreditors(creditors)
        def clientId = UUID.randomUUID().toString()

        when:
        def responseCreate = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        then:
        noExceptionThrown()

        when:
        def response = paymentConsentService.getRecurringConsentsV1(responseCreate.getData().getRecurringConsentId(), clientId)


        then:
        noExceptionThrown()
        response != null
        def data = response.getData()
        data.getRecurringConsentId() == responseCreate.getData().getRecurringConsentId()
        def configuration = data.getRecurringConfiguration()
        configuration != null
        configuration.getSweeping().getTotalAllowedAmount() == req.getData().getRecurringConfiguration().getSweeping().getTotalAllowedAmount()
        configuration.getSweeping().getTransactionLimit() == req.getData().getRecurringConfiguration().getSweeping().getTransactionLimit()
        configuration.getSweeping().getPeriodicLimits() == req.getData().getRecurringConfiguration().getSweeping().getPeriodicLimits()
    }

    def "we can reject consent"() {
        given:
        def req = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())
        req.getData().setBusinessEntity(null)
        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
        ))
        req.data.setCreditors(creditors)

        def clientId = UUID.randomUUID().toString()
        def consentResponse = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)
        def consentId = consentResponse.getData().getRecurringConsentId()

        def patchConsentRequest = TestRequestDataFactory.createPatchRecurringPaymentConsentRequestV1Rejected()
        when:
        def patchResponse = paymentConsentService.patchRecurringConsentV1(consentId, clientId, patchConsentRequest)
        then:
        noExceptionThrown()
        def data = patchResponse.getData()
        data.getStatus() == EnumAuthorisationStatusType.REJECTED

        def statusUpdateDateTime = data.getStatusUpdateDateTime()
        statusUpdateDateTime != null
        statusUpdateDateTime.isAfter(consentResponse.getData().getStatusUpdateDateTime())

        data.getRevocation() == null
        def rejection = data.getRejection()
        rejection != null
        rejection.getRejectedAt() == statusUpdateDateTime
        rejection.getRejectedFrom() == patchConsentRequest.getData().getRejection().getRejectedFrom()
        rejection.getRejectedBy() == patchConsentRequest.getData().getRejection().getRejectedBy()
        rejection.getReason() == patchConsentRequest.getData().getRejection().getReason()
    }

    def "we can revoke consent"() {
        given:
        def req = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())
        req.getData().setBusinessEntity(null)
        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
        ))
        req.data.setCreditors(creditors)

        def clientId = UUID.randomUUID().toString()
        def consentResponse = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)
        def consentId = consentResponse.getData().getRecurringConsentId()

        paymentConsentRepository.findByPaymentConsentId(consentId).ifPresent {
            it.setStatus(EnumAuthorisationStatusType.AUTHORISED.toString())
            paymentConsentRepository.update(it)
        }


        def patchConsentRequest = TestRequestDataFactory.createPatchRecurringPaymentConsentRequestV1Revoked()
        when:
        def patchResponse = paymentConsentService.patchRecurringConsentV1(consentId, clientId, patchConsentRequest)
        then:
        noExceptionThrown()
        def data = patchResponse.getData()
        data.getStatus() == EnumAuthorisationStatusType.REVOKED

        def statusUpdateDateTime = data.getStatusUpdateDateTime()
        statusUpdateDateTime != null
        statusUpdateDateTime.isAfter(consentResponse.getData().getStatusUpdateDateTime())

        data.getRejection() == null
        def revocation = data.getRevocation()
        revocation != null
        revocation.getRevokedAt() != null
        revocation.getRevokedAt() == statusUpdateDateTime
        revocation.getRevokedFrom() == patchConsentRequest.getData().getRevocation().getRevokedFrom()
        revocation.getRevokedBy() == patchConsentRequest.getData().getRevocation().getRevokedBy()
        revocation.getReason() == patchConsentRequest.getData().getRevocation().getReason()
    }


    def "we cant patch consent if both rejection and revocation objects are present"() {
        given:
        def req = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())
        req.getData().setBusinessEntity(null)
        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
        ))
        req.data.setCreditors(creditors)

        def clientId = UUID.randomUUID().toString()
        def consentResponse = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)
        def consentId = consentResponse.getData().getRecurringConsentId()

        def patchConsentRequest = TestRequestDataFactory.createPatchRecurringPaymentConsentRequestV1Rejected()
        patchConsentRequest.getData().setRevocation(TestRequestDataFactory.createPatchRecurringPaymentConsentRequestV1Revoked().getData().getRevocation())
        patchConsentRequest.getData().setStatus(status)
        when:
        def patchResponse = paymentConsentService.patchRecurringConsentV1(consentId, clientId, patchConsentRequest)
        then:
        def e = thrown(HttpStatusException)
        patchResponse == null
        e.getStatus() == HttpStatus.BAD_REQUEST
        e.getMessage() == message

        where:
        status                                          | message
        PatchRecurringConsentV1Data.StatusEnum.REJECTED | "revocation object shall not be present when status is REJECTED, rejection and revocation objects are mutually exclusive"
        PatchRecurringConsentV1Data.StatusEnum.REVOKED  | "rejection object shall not be present when status is REVOKED, rejection and revocation objects are mutually exclusive"
    }

    def "we cant patch consent without rejection and revocation objects if status is specified"() {
        given:
        def req = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())
        req.getData().setBusinessEntity(null)
        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
        ))
        req.data.setCreditors(creditors)

        def clientId = UUID.randomUUID().toString()
        def consentResponse = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)
        def consentId = consentResponse.getData().getRecurringConsentId()

        def patchConsentRequest = TestRequestDataFactory.createPatchRecurringPaymentConsentRequestV1Rejected()
        patchConsentRequest.getData().setRejection(null)
        patchConsentRequest.getData().setStatus(status)
        when:
        def patchResponse = paymentConsentService.patchRecurringConsentV1(consentId, clientId, patchConsentRequest)
        then:
        def e = thrown(HttpStatusException)
        patchResponse == null
        e.getStatus() == HttpStatus.BAD_REQUEST
        e.getMessage() == message

        where:
        status                                          | message
        PatchRecurringConsentV1Data.StatusEnum.REJECTED | "rejection object is mandatory when status is REJECTED"
        PatchRecurringConsentV1Data.StatusEnum.REVOKED  | "revocation object is mandatory when status is REVOKED"
    }

    def "we cant reject a consent if status is Authorised"() {
        given:
        def req = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())
        req.getData().setBusinessEntity(null)
        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
        ))
        req.data.setCreditors(creditors)

        def clientId = UUID.randomUUID().toString()
        def consentResponse = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)
        def consentId = consentResponse.getData().getRecurringConsentId()

        paymentConsentRepository.findByPaymentConsentId(consentId).ifPresent {
            it.setStatus(EnumAuthorisationStatusType.AUTHORISED.toString())
            paymentConsentRepository.update(it)
        }

        def patchConsentRequest = TestRequestDataFactory.createPatchRecurringPaymentConsentRequestV1Rejected()

        when:
        def patchResponse = paymentConsentService.patchRecurringConsentV1(consentId, clientId, patchConsentRequest)
        then:
        def e = thrown(HttpStatusException)
        patchResponse == null
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == String.format("%s: Authorised consent cannot be rejected",
                ResponseErrorCreateRecurringConsentPatchV1Errors.CodeEnum.CONSENTIMENTO_NAO_PERMITE_CANCELAMENTO)

    }

    def "we cant revoke a consent if status is not Authorised"() {
        given:
        def req = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())
        req.getData().setBusinessEntity(null)
        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
        ))
        req.data.setCreditors(creditors)

        def clientId = UUID.randomUUID().toString()
        def consentResponse = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)
        def consentId = consentResponse.getData().getRecurringConsentId()



        def patchConsentRequest = TestRequestDataFactory.createPatchRecurringPaymentConsentRequestV1Revoked()

        when:
        def patchResponse = paymentConsentService.patchRecurringConsentV1(consentId, clientId, patchConsentRequest)
        then:
        def e = thrown(HttpStatusException)
        patchResponse == null
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == String.format("%s: Only Authorised consent can be revoked",
                ResponseErrorCreateRecurringConsentPatchV1Errors.CodeEnum.CONSENTIMENTO_NAO_PERMITE_CANCELAMENTO)

    }

    def "we cant change consent data if recurring configuration is not Automatic"() {
        given:
        def req = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())
        req.getData().setBusinessEntity(null)
        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
        ))
        req.data.setCreditors(creditors)

        def clientId = UUID.randomUUID().toString()
        def consentResponse = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)
        def consentId = consentResponse.getData().getRecurringConsentId()

        def patchConsentRequest = TestRequestDataFactory.createPatchRecurringPaymentConsentRequestV1DataChange()

        when:
        def patchResponse = paymentConsentService.patchRecurringConsentV1(consentId, clientId, patchConsentRequest)
        then:
        def e = thrown(HttpStatusException)
        patchResponse == null
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == String.format("%s: Data change is only permitted for Automatic Recurring Configuration",
                ResponseErrorCreateRecurringConsentPatchV1Errors.CodeEnum.CAMPO_NAO_PERMITIDO)

    }

    def "we cant create a recurring pix payment if the consentId doesn't match"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def reqConsent = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())
        def consentResponse = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), reqConsent)
        def req = TestRequestDataFactory.createRecurringPixPayment("urn:raidiambank:C1DD33123")

        when:
        def response = paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage().contains("PAGAMENTO_DIVERGENTE_CONSENTIMENTO: O valor informado no consentimento não é o mesmo valor do informado no payload de pagamento")
        response == null
    }

    def "we cant create a recurring pix payment if the consent is revoked"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def reqConsent = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())
        def consentResponse = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), reqConsent)
        def paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentResponse.getData().getRecurringConsentId()).get()
        paymentConsentEntity.setStatus(EnumAuthorisationStatusType.REVOKED.name())
        paymentConsentRepository.update(paymentConsentEntity)
        def req = TestRequestDataFactory.createRecurringPixPayment(consentResponse.getData().getRecurringConsentId())

        when:
        def response = paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNAUTHORIZED
        e.getMessage().contains("O consentimento informado encontra-se revogado")
        response == null
    }

    def "we cant create a recurring pix payment if the consent is expired"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def reqConsent = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())
        def consentResponse = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), reqConsent)
        def paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentResponse.getData().getRecurringConsentId()).get()
        //minus 2 days
        paymentConsentEntity.setExpirationDateTime(Date.from(Instant.now().minusMillis(172800000)))
        paymentConsentRepository.update(paymentConsentEntity)
        def req = TestRequestDataFactory.createRecurringPixPayment(consentResponse.getData().getRecurringConsentId())

        when:
        def response = paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNAUTHORIZED
        e.getMessage().contains("O consentimento informado encontra-se expirado")
        response == null
    }

    def "we cant create a recurring consent if the date is before the consent start date"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def reqConsent = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())
        reqConsent.data.setStartDateTime(OffsetDateTime.now().plusDays(2))

        when:
        def consentResponse = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), reqConsent)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage().contains("PARAMETRO_INVALIDO: startDateTime cannot be after expirationDateTime")
        consentResponse == null
    }


    def "we cant create a recurring pix payment if the daily sweeping transaction limit exceed"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def reqConsent = TestRequestDataFactory.createRecurringPaymentConsentRequestV1(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())
        reqConsent.getData().setRecurringConfiguration(new AllOfCreateRecurringConsentV1DataRecurringConfiguration()
                .sweeping(new SweepingSweeping()
                        .totalAllowedAmount("100.00")
                        .transactionLimit("100.00")
                        .periodicLimits(new PeriodicLimits()
                                .day(new Day()
                                        .quantityLimit(2)
                                        .transactionLimit("100.00"))
                                .week(new Week()
                                        .quantityLimit(5)
                                        .transactionLimit("100.00"))
                                .month(new Month()
                                        .quantityLimit(5)
                                        .transactionLimit("100.00"))
                                .year(new Year()
                                        .quantityLimit(5)
                                        .transactionLimit("100.00")))))
        reqConsent.getData().setBusinessEntity(null)
        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
        ))
        reqConsent.data.setCreditors(creditors)
        def consentResponse = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), reqConsent)
        def paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentResponse.getData().getRecurringConsentId()).get()
        paymentConsentEntity.setStatus(EnumAuthorisationStatusType.AUTHORISED.name())
        paymentConsentRepository.update(paymentConsentEntity)

        def req = TestRequestDataFactory.createRecurringPixPayment(consentResponse.getData().getRecurringConsentId())
        req.data.payment.amount("50")
        paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        when:
        req.data.payment.amount("60")
        paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage().contains("LIMITE_PERIODO_VALOR_EXCEDIDO: O limite diário estabelicido pelo consentimento foi excedido")

        when:
        req.data.payment.amount("10")
        paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)
        paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        then:
        def e2 = thrown(HttpStatusException)
        e2.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e2.getMessage().contains("LIMITE_PERIODO_QUANTIDADE_EXCEDIDO: O limite diário estabelicido pelo consentimento foi excedido")

    }

    def "we cant create a recurring pix payment if the weekly sweeping transaction limit exceed"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def reqConsent = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())

        reqConsent.getData().setBusinessEntity(null)
        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
        ))
        reqConsent.data.setCreditors(creditors)
        def consentResponse = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), reqConsent)
        def paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentResponse.getData().getRecurringConsentId()).get()
        paymentConsentEntity.setStatus(EnumAuthorisationStatusType.AUTHORISED.name())
        paymentConsentRepository.update(paymentConsentEntity)

        def req = TestRequestDataFactory.createRecurringPixPayment(consentResponse.getData().getRecurringConsentId())
        req.data.payment.amount("30")
        paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        when:
        req.data.payment.amount("80")
        req.data.date(LocalDate.now().plusDays(1))
        paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage().contains("LIMITE_PERIODO_VALOR_EXCEDIDO: O limite semanal estabelicido pelo consentimento foi excedido")

        when:
        req.data.payment.amount("10")
        req.data.date(LocalDate.now().plusDays(1))
        paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)
        paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        then:
        def e2 = thrown(HttpStatusException)
        e2.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e2.getMessage().contains("LIMITE_PERIODO_QUANTIDADE_EXCEDIDO: O limite semanal estabelicido pelo consentimento foi excedido")
    }

    def "we cant create a recurring pix payment if the montly sweeping transaction limit exceed"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def reqConsent = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())

        reqConsent.getData().setBusinessEntity(null)
        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
        ))
        reqConsent.data.setCreditors(creditors)
        def consentResponse = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), reqConsent)
        def paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentResponse.getData().getRecurringConsentId()).get()
        paymentConsentEntity.setStatus(EnumAuthorisationStatusType.AUTHORISED.name())
        paymentConsentRepository.update(paymentConsentEntity)

        def req = TestRequestDataFactory.createRecurringPixPayment(consentResponse.getData().getRecurringConsentId())
        req.data.payment.amount("30")
        req.data.date(LocalDate.of(20023, 12, 1))
        paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        when:
        req.data.payment.amount("80")
        req.data.date(LocalDate.of(20023, 12, 15))
        paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage().contains("LIMITE_PERIODO_VALOR_EXCEDIDO: O limite mensal estabelicido pelo consentimento foi excedido")

        when:
        req.data.payment.amount("10")
        req.data.date(LocalDate.of(20023, 12, 15))
        paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)
        req.data.date(LocalDate.of(20023, 12, 16))
        paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        then:
        def e2 = thrown(HttpStatusException)
        e2.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e2.getMessage().contains("LIMITE_PERIODO_QUANTIDADE_EXCEDIDO: O limite mensal estabelicido pelo consentimento foi excedido")
    }

    def "we cant create a recurring pix payment if the sweeping transaction limit exceed"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def reqConsent = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())

        reqConsent.getData().setBusinessEntity(null)
        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
        ))
        reqConsent.data.setCreditors(creditors)
        def consentResponse = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), reqConsent)
        def paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentResponse.getData().getRecurringConsentId()).get()
        paymentConsentEntity.setStatus(EnumAuthorisationStatusType.AUTHORISED.name())
        paymentConsentRepository.update(paymentConsentEntity)

        def req = TestRequestDataFactory.createRecurringPixPayment(consentResponse.getData().getRecurringConsentId())
        req.data.payment.amount("200")

        when:
        paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage().contains("LIMITE_PERIODO_VALOR_EXCEDIDO: O limite do pagamento excede o limite definido no consentimento")
    }

    def "we can create a recurring pix payment"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def reqConsent = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())

        reqConsent.getData().setBusinessEntity(null)
        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
        ))
        reqConsent.data.setCreditors(creditors)
        def consentResponse = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), reqConsent)
        def paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentResponse.getData().getRecurringConsentId()).get()
        paymentConsentEntity.setStatus(EnumAuthorisationStatusType.AUTHORISED.name())
        paymentConsentRepository.update(paymentConsentEntity)

        def req = TestRequestDataFactory.createRecurringPixPayment(consentResponse.getData().getRecurringConsentId())
        req.data.payment.amount("30")

        when:
        def response = paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        then:
        noExceptionThrown()
        response.data.recurringPaymentId != null
    }

    def "we can Force a 422 for with PAGAMENTO_DIVERGENTE_CONSENTIMENTO meaning the creditor validation"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def reqConsent = TestRequestDataFactory.createRecurringPaymentConsentRequestV1(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())
        reqConsent.data.recurringConfiguration(new AllOfCreateRecurringConsentV1DataRecurringConfiguration().sweeping(new SweepingSweeping()
                .totalAllowedAmount("10000.00")
                .transactionLimit("10000.00")
                .periodicLimits(new PeriodicLimits()
                        .day(new Day()
                                .quantityLimit(2)
                                .transactionLimit("10000.00"))
                        )))

        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.JURIDICA.toString())
                        .cpfCnpj("12345678901234")))
        reqConsent.data.setCreditors(creditors)
        def consentResponse = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), reqConsent)
        def paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentResponse.getData().getRecurringConsentId()).get()
        paymentConsentEntity.setStatus(EnumAuthorisationStatusType.AUTHORISED.name())
        paymentConsentRepository.update(paymentConsentEntity)

        def req = TestRequestDataFactory.createRecurringPixPayment(consentResponse.getData().getRecurringConsentId())
        req.data.payment.amount("1200.01")

        when:
        paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        then:
        def e2 = thrown(HttpStatusException)
        e2.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e2.getMessage().contains("O CNPJ do loggedUser (Bussiness Entity) não corresponde ao creditor do consentimento")
    }

    def "we can get a 422 with the creditor number doesn't match the one from the logged user (validated by cpf)"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def reqConsent = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())

        reqConsent.getData().setBusinessEntity(null)
        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
        ))
        reqConsent.data.setCreditors(creditors)
        def consentResponse = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), reqConsent)
        def paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentResponse.getData().getRecurringConsentId()).get()
        paymentConsentEntity.setStatus(EnumAuthorisationStatusType.AUTHORISED.name())
        paymentConsentRepository.update(paymentConsentEntity)

        def req = TestRequestDataFactory.createRecurringPixPayment(consentResponse.getData().getRecurringConsentId())
        req.data.creditorAccount(new CreditorAccount().number("1234567").issuer("1774").ispb("12345678").accountType(EnumAccountPaymentsType.CACC))

        when:
        paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage().contains("PAGAMENTO_DIVERGENTE_CONSENTIMENTO: O CPF do loggedUser não corresponde ao creditor do consentimento")
    }

    def "we cannt retrieve a recurring pix payment doesn't exist"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def reqConsent = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())

        reqConsent.getData().setBusinessEntity(null)
        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
        ))
        reqConsent.data.setCreditors(creditors)
        def consentResponse = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), reqConsent)
        def paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentResponse.getData().getRecurringConsentId()).get()
        paymentConsentEntity.setStatus(EnumAuthorisationStatusType.AUTHORISED.name())
        paymentConsentRepository.update(paymentConsentEntity)

        def req = TestRequestDataFactory.createRecurringPixPayment(consentResponse.getData().getRecurringConsentId())
        paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        when:
        paymentsService.getRecurringPixPaymentV1("inventedId", clientId)

        then:
        def e2 = thrown(HttpStatusException)
        e2.getStatus() == HttpStatus.NOT_FOUND
        e2.getMessage().contains("Payment not found")
    }

    def "we can retrieve a recurring pix payment"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def reqConsent = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())

        reqConsent.getData().setBusinessEntity(null)
        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
        ))
        reqConsent.data.setCreditors(creditors)
        def consentResponse = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), reqConsent)
        def paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentResponse.getData().getRecurringConsentId()).get()
        paymentConsentEntity.setStatus(EnumAuthorisationStatusType.AUTHORISED.name())
        paymentConsentRepository.update(paymentConsentEntity)

        def req = TestRequestDataFactory.createRecurringPixPayment(consentResponse.getData().getRecurringConsentId())
        def created = paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        when:
        def response = paymentsService.getRecurringPixPaymentV1(created.getData().getRecurringPaymentId(), clientId)

        then:
        noExceptionThrown()
        response.data.recurringPaymentId != null
    }

    def "we can cancel a recurring pix payment"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def reqConsent = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())

        reqConsent.getData().setBusinessEntity(null)
        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
        ))
        reqConsent.data.setCreditors(creditors)
        def consentResponse = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), reqConsent)
        def paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentResponse.getData().getRecurringConsentId()).get()
        paymentConsentEntity.setStatus(EnumAuthorisationStatusType.AUTHORISED.name())
        paymentConsentRepository.update(paymentConsentEntity)

        def req = TestRequestDataFactory.createRecurringPixPayment(consentResponse.getData().getRecurringConsentId())
        def respPixPayment = paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)
        paymentsService.getRecurringPixPaymentV1(respPixPayment.data.getRecurringPaymentId(), clientId)
        def patchReq = TestRequestDataFactory.createPatchRecurringPixPaymentRequestV1Cancelled(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())

        when:
        def response = paymentsService.patchRecurringPixPaymentV1(respPixPayment.data.getRecurringConsentId(), respPixPayment.data.getRecurringPaymentId(), patchReq)

        then:
        noExceptionThrown()
        response.data.status == EnumPaymentStatusTypeV2.CANC
    }

    def "we cannt cancel a recurring pix payment with status SCHD or PNDG"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def reqConsent = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())

        reqConsent.getData().setBusinessEntity(null)
        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
        ))
        reqConsent.data.setCreditors(creditors)
        def consentResponse = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), reqConsent)
        def paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentResponse.getData().getRecurringConsentId()).get()
        paymentConsentEntity.setStatus(EnumAuthorisationStatusType.AUTHORISED.name())
        paymentConsentRepository.update(paymentConsentEntity)

        def req = TestRequestDataFactory.createRecurringPixPayment(consentResponse.getData().getRecurringConsentId())
        def respPixPayment = paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), req)

        def patchReq = TestRequestDataFactory.createPatchRecurringPixPaymentRequestV1Cancelled(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())

        when:
        def response = paymentsService.patchRecurringPixPaymentV1(respPixPayment.data.getRecurringConsentId(), respPixPayment.data.getRecurringPaymentId(), patchReq)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage().contains("PAGAMENTO_NAO_PERMITE_CANCELAMENTO: Pagamento está com um status que não permite cancelamento")
    }

    def "we can retrieve a recurring pix payment by consentId"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def reqConsent = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())

        reqConsent.getData().setBusinessEntity(null)
        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
        ))
        reqConsent.data.setCreditors(creditors)
        def consentResponse = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), reqConsent)
        def paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentResponse.getData().getRecurringConsentId()).get()
        paymentConsentEntity.setStatus(EnumAuthorisationStatusType.AUTHORISED.name())
        paymentConsentRepository.update(paymentConsentEntity)

        def req = TestRequestDataFactory.createRecurringPixPayment(consentResponse.getData().getRecurringConsentId())
        req.data.date(LocalDate.now().plusMonths(2))
        paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), clientId, req)
        req.data.date(LocalDate.now().plusMonths(3))
        paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), clientId, req)
        req.data.date(LocalDate.now().plusMonths(1).plusDays(3))
        paymentsService.createRecurringPixPaymentV1(consentResponse.getData().getRecurringConsentId(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), clientId, req)

        when:
        def startDate = LocalDate.now()
        def endDate = LocalDate.now().plusMonths(5)

        def response = paymentsService.getRecurringPixPaymentByConsentIdV1(consentResponse.getData().getRecurringConsentId(), startDate, endDate, clientId)

        then:
        noExceptionThrown()
        response.data.size() == 3
    }

    def "we cannot update a payment consent with a debtor if the consent already has it"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def reqConsent = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())

        reqConsent.getData().setBusinessEntity(null)
        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
        ))
        reqConsent.data.setCreditors(creditors)
        reqConsent.data.setDebtorAccount(new DebtorAccount().accountType(EnumAccountPaymentsType.CACC).ispb("12341234").issuer("1234").number("1234567890"))
        def consentResponse = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), reqConsent)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        updatePaymentConsent.data.debtorAccount(new DebtorAccount().accountType(EnumAccountPaymentsType.CACC).ispb("123412345").issuer("1234").number("1234567000"))

        when:
        paymentConsentService.updateRecurringConsentV1(consentResponse.getData().getRecurringConsentId(), updatePaymentConsent)

        then:
        HttpStatusException e = thrown()
        e.getStatus() == HttpStatus.BAD_REQUEST
        e.message == 'Debtor account already set in initial consent'
    }

    def "we can update a payment consent with a debtor if the consent does not already have it"() {
        given:
        def clientId = UUID.randomUUID().toString()
        def reqConsent = TestRequestDataFactory.createRecurringPaymentConsentRequestV1WithSweeping(accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification())

        reqConsent.getData().setBusinessEntity(null)
        Creditors creditors = new Creditors()
        creditors.addAll(List.of(
                new Identification()
                        .name("name")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
                new Identification()
                        .name("name2")
                        .personType(EnumCreditorPersonType.NATURAL.toString())
                        .cpfCnpj(accountHolder.getDocumentIdentification()),
        ))
        reqConsent.data.setCreditors(creditors)
        def consentResponse = paymentConsentService.createRecurringConsentV1(clientId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), reqConsent)

        UpdatePaymentConsent updatePaymentConsent = TestRequestDataFactory.createPaymentConsentUpdateRequest(UpdatePaymentConsentData.StatusEnum.AUTHORISED, false)
        updatePaymentConsent.data.debtorAccount(new DebtorAccount().accountType(EnumAccountPaymentsType.CACC).ispb("123412345").issuer("1234").number("1234567890"))

        when:
        paymentConsentService.updateConsent(consentResponse.getData().getRecurringConsentId(), clientId, updatePaymentConsent)

        then:
        noExceptionThrown()
    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }

}
