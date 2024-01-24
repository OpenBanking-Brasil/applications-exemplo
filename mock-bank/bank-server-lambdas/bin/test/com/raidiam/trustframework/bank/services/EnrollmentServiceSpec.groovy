package com.raidiam.trustframework.bank.services

import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.TestRequestDataFactory
import com.raidiam.trustframework.bank.controllers.ConsentFactory
import com.raidiam.trustframework.bank.domain.AccountEntity
import com.raidiam.trustframework.bank.domain.AccountHolderEntity
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Stepwise
import spock.lang.Unroll

import javax.inject.Inject
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime

import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccount
import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccountHolder
import static com.raidiam.trustframework.bank.controllers.EnrollmentFactory.createEnrollment
import static com.raidiam.trustframework.bank.controllers.EnrollmentFactory.patchEnrollment

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class EnrollmentServiceSpec extends CleanupSpecification {

    @Inject
    EnrollmentService enrollmentService
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

    def "we can create an enrollment"() {
        given:
        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())

        when:
        enrollmentService.createEnrollment(clientId, idemPotencyKey, jti, enrollmentRequest)

        then:
        noExceptionThrown()

        where:
        clientId                     | jti                          | idemPotencyKey
        UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString()

    }

    def "we can get a enrollment"() {
        given:
        def enrollment = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        ResponseCreateEnrollment response = enrollmentService.createEnrollment(clientId, idemPotencyKey, jti, enrollment)

        when:
        def foundResponse = enrollmentService.getEnrollment(response.getData().getEnrollmentId(), clientId, false)

        then:
        foundResponse.getData().getStatus() == EnumEnrollmentStatus.AWAITING_RISK_SIGNALS

        where:
        clientId                     | jti                          | idemPotencyKey
        UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString()
    }

    def "we cannot get another client's enrollment"() {
        given:
        def enrollment = createEnrollment(accountHolder.documentIdentification, accountHolder.documentRel)
        ResponseCreateEnrollment response = enrollmentService.createEnrollment(clientId, idemPotencyKey, jti, enrollment)

        when:
        def foundResponse = enrollmentService.getEnrollment(response.getData().getEnrollmentId(), UUID.randomUUID().toString(), false)

        then:
        def ex = thrown(HttpStatusException)
        ex.status == HttpStatus.FORBIDDEN

        where:
        clientId                     | jti                          | idemPotencyKey
        UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString()
    }

    def "we get a rejected enrollment after 5 minutes with AWAITING_RISK_SIGNAL status"() {
        given:
        def enrollmentRequest = createEnrollment(accountHolder.documentIdentification, accountHolder.documentRel)
        def enrollmentId = enrollmentService.createEnrollment(clientId, idemPotencyKey, jti, enrollmentRequest)
                .getData().getEnrollmentId()
        def enrollment = enrollmentRepository.findByEnrollmentId(enrollmentId)
                .orElse(null)
        assert enrollment != null

        Instant now = OffsetDateTime.now().minusMinutes(6).toInstant()
        enrollment.setCreationDateTime(Date.from(now))
        enrollmentRepository.update(enrollment)

        when:
        ResponseEnrollment responseEnrollment = enrollmentService.getEnrollment(enrollmentId, clientId, false)

        then:
        responseEnrollment.getData().getStatus() == EnumEnrollmentStatus.REJECTED
        responseEnrollment.getData().getCancellation().getReason().getRejectionReason() == EnrollmentRejectionReason.TEMPO_EXPIRADO_RISK_SIGNALS

        where:
        clientId                     | jti                          | idemPotencyKey
        UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString()
    }

    def "we get a rejected enrollment after 15 minutes with AWAITING_ACCOUNT_HOLDER_VALIDATION status"() {
        given:
        def enrollmentRequest = createEnrollment(accountHolder.documentIdentification, accountHolder.documentRel)
        def enrollmentId = enrollmentService.createEnrollment(clientId, idemPotencyKey, jti, enrollmentRequest)
                .getData().getEnrollmentId()
        def enrollment = enrollmentRepository.findByEnrollmentId(enrollmentId)
                .orElse(null)
        assert enrollment != null

        enrollment.setStatus(EnumEnrollmentStatus.AWAITING_ACCOUNT_HOLDER_VALIDATION.toString())
        Instant now = OffsetDateTime.now().minusMinutes(16).toInstant()
        enrollment.setStatusUpdateDateTime(Date.from(now))
        enrollmentRepository.update(enrollment)

        when:
        ResponseEnrollment responseEnrollment = enrollmentService.getEnrollment(enrollmentId, clientId, false)

        then:
        responseEnrollment.getData().getStatus() == EnumEnrollmentStatus.REJECTED
        responseEnrollment.getData().getCancellation().getReason().getRejectionReason() == EnrollmentRejectionReason.TEMPO_EXPIRADO_ACCOUNT_HOLDER_VALIDATION

        where:
        clientId                     | jti                          | idemPotencyKey
        UUID.randomUUID().toString() | UUID.randomUUID().toString() | UUID.randomUUID().toString()
    }

    @Unroll
    def "we can update enrollment"() {
        given: "we have an enrollment"
        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def enrollmentId = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest)
                .getData().getEnrollmentId()
        def patchRequest = patchEnrollment(isRejection)

        if (!isRejection) {
            def enrollment = enrollmentRepository.findByEnrollmentId(enrollmentId)
                    .orElse(null)
            assert enrollment != null
            enrollment.setStatus(EnumEnrollmentStatus.AUTHORISED.name())
            enrollmentRepository.update(enrollment)
        }

        when: "we send a patch request with rejection/revocation"
        enrollmentService.updateEnrollment(enrollmentId, patchRequest)
        def updatedEnrollment = enrollmentRepository.findByEnrollmentId(enrollmentId)
                .orElse(null)

        then: "status should be updated adn rejection/revocation information should be added"
        noExceptionThrown()
        updatedEnrollment != null
        def data = patchRequest.getData()
        def cancellation = data.getCancellation()
        def cancelledBy = cancellation.getCancelledBy()
        def document = cancelledBy.getDocument()
        def reason = cancellation.getReason()

        updatedEnrollment.getCancelledByDocumentIdentification() == document.getIdentification()
        updatedEnrollment.getCancelledByDocumentRel() == document.getRel()
        updatedEnrollment.getCancelledFrom() == EnumEnrollmentCancelledFrom.INICIADORA.toString()
        updatedEnrollment.getRejectedAt() != null
        updatedEnrollment.getRejectReason() == Optional.ofNullable(reason.getRejectionReason())
                .map { it.toString() }
                .orElse(null)
        updatedEnrollment.getRevocationReason() == Optional.ofNullable(reason.getRevocationReason())
                .map { it.toString() }
                .orElse(null)
        updatedEnrollment.getAdditionalInformation() == cancellation.getAdditionalInformation()
        where:
        isRejection << [true, false]
    }


    @Unroll
    def "we cannot reject/revoke an enrollment with a specific status"() {
        given: "we have an enrollment with the specific status"
        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        enrollmentRequest.data.setDebtorAccount(null)
        def enrollmentId = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest)
                .getData().getEnrollmentId()
        def patchRequest = patchEnrollment(isRejection)

        def enrollment = enrollmentRepository.findByEnrollmentId(enrollmentId)
                .orElse(null)
        assert enrollment != null
        enrollment.setStatus(originalStatus.name())
        enrollmentRepository.update(enrollment)

        when: "we send an unexpected patch request with rejection/revocation"
        enrollmentService.updateEnrollment(enrollmentId, patchRequest)

        then: "400 BAD request should bet thrown"
        def e = thrown(HttpStatusException)

        def message = isRejection ? String.format("%s: Enrollment cannot be rejected if the status is %s", EnrollmentCancelErrorCode.MOTIVO_REJEICAO, originalStatus) :
                String.format("%s: Enrollment cannot be revoked if the status is %s", EnrollmentCancelErrorCode.MOTIVO_REVOGACAO, originalStatus)

        e.getMessage() == message
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY

        where:
        isRejection | originalStatus
        false       | EnumEnrollmentStatus.AWAITING_RISK_SIGNALS
        false       | EnumEnrollmentStatus.AWAITING_ACCOUNT_HOLDER_VALIDATION
        false       | EnumEnrollmentStatus.AWAITING_ENROLLMENT
        true        | EnumEnrollmentStatus.AUTHORISED
    }


    @Unroll
    def "we cannot update an enrollment without cancellation/revocation reasons or if both reasons are provided"() {
        given: "we have an enrollment"
        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        enrollmentRequest.data.setDebtorAccount(null)
        def enrollmentId = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest)
                .getData().getEnrollmentId()
        def patchRequest = patchEnrollment(true)
        def reason = patchRequest.getData().getCancellation().getReason()
        reason.setRevocationReason(revocationReason)
        reason.setRejectionReason(rejectionReason)

        when: "we send patch request without or with both cancellation/revocation reasons"
        enrollmentService.updateEnrollment(enrollmentId, patchRequest)

        then: "400 BAD request should bet thrown"
        def e = thrown(HttpStatusException)
        e.getMessage() == "Either Rejection Reason or Revocation Reason must be provided"
        e.getStatus() == HttpStatus.BAD_REQUEST

        where:
        revocationReason                                | rejectionReason
        EnrollmentRevocationReason.FALHA_INFRAESTRUTURA | EnrollmentRejectionReason.FALHA_INFRAESTRUTURA
        null                                            | null
    }

    @Unroll
    def "we cannot update an enrollment if it does not exist"() {
        given: "we dont have an enrollment"
        def patchRequest = patchEnrollment(true)
        def nonExistentEnrollmentId = UUID.randomUUID().toString()

        when: "we send patch request"
        enrollmentService.updateEnrollment(nonExistentEnrollmentId, patchRequest)

        then: "404 not found should bet thrown"
        def e = thrown(HttpStatusException)
        e.getMessage() == String.format("Could not find Enrollment with ID - %s", nonExistentEnrollmentId)
        e.getStatus() == HttpStatus.NOT_FOUND
    }

    @Unroll
    def "we cannot create a risk signal with a specific status"() {
        given: "we have an enrollment with the specific status"
        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def enrollmentId = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest)
                .getData().getEnrollmentId()
        def riskSignalRequest = TestRequestDataFactory.createEnrollmentRiskSignal()

        def enrollment = enrollmentRepository.findByEnrollmentId(enrollmentId)
                .orElse(null)
        assert enrollment != null
        enrollment.setStatus(originalStatus.name())
        enrollmentRepository.update(enrollment)

        when: "we send an unexpected risk signal"
        enrollmentService.createRiskSignal(enrollmentId, riskSignalRequest)

        then: "422 unprocessable Entity should bet thrown"
        def e = thrown(HttpStatusException)

        e.getMessage().startsWith("STATUS_VINCULO_INVALIDO")
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY

        where:
        originalStatus << [EnumEnrollmentStatus.AWAITING_ACCOUNT_HOLDER_VALIDATION, EnumEnrollmentStatus.AWAITING_ENROLLMENT, EnumEnrollmentStatus.AUTHORISED]
    }

    def "we can create an enrollment risk signal"() {
        given:
        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def enrollmentRiskSignalRequest = TestRequestDataFactory.createEnrollmentRiskSignal()

        when:
        def enrollment = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest)
        enrollmentService.createRiskSignal(enrollment.getData().getEnrollmentId(), enrollmentRiskSignalRequest)

        then:
        noExceptionThrown()

    }

    def "we can get 422 when cn is different from rp on enrollment fido-registration-options"() {
        given:
        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def enrollmentFidoOptionsRequest = TestRequestDataFactory.createEnrollmentFidoOptionsInput()
        def enrollmentId = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest).getData().getEnrollmentId()
        def enrollment = enrollmentRepository.findByEnrollmentId(enrollmentId)
                .orElse(null)
        assert enrollment != null
        enrollment.setStatus(EnumEnrollmentStatus.AWAITING_ENROLLMENT.name())
        enrollmentRepository.update(enrollment)

        when:
        enrollmentService.createFidoRegistrationOptions(enrollmentId, enrollmentFidoOptionsRequest, Optional.of("test"))

        then: "422 unprocessable Entity should bet thrown"
        def e = thrown(HttpStatusException)

        e.getMessage().startsWith("RP_INVALIDA")
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY

    }

    def "we can create a fido registration option"() {
        given:
        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def enrollmentFidoOptionsRequest = TestRequestDataFactory.createEnrollmentFidoOptionsInput()
        def enrollmentId = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest).getData().getEnrollmentId()
        def enrollment = enrollmentRepository.findByEnrollmentId(enrollmentId)
                .orElse(null)
        assert enrollment != null
        enrollment.setStatus(EnumEnrollmentStatus.AWAITING_ENROLLMENT.name())
        enrollmentRepository.update(enrollment)

        when:
        enrollmentService.createFidoRegistrationOptions(enrollmentId, enrollmentFidoOptionsRequest, Optional.of(enrollmentFidoOptionsRequest.data.getRp()))

        then:
        noExceptionThrown()
    }

    def "we can create a fido sign option"() {
        given:
        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")

        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV3(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        def consentId = responseConsent.getData().getConsentId();

        def enrollmentFidoSignOptionsRequest = TestRequestDataFactory.createEnrollmentFidoSignOptionsInput()
        enrollmentFidoSignOptionsRequest.getData().setConsentId(consentId)

        def enrollmentId = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest).getData().getEnrollmentId()
        def enrollment = enrollmentRepository.findByEnrollmentId(enrollmentId)
                .orElse(null)
        assert enrollment != null
        enrollment.setStatus(EnumEnrollmentStatus.AUTHORISED.name())
        enrollmentRepository.update(enrollment)

        when:
        enrollmentService.createFidoSignOptions(enrollmentId, enrollmentFidoSignOptionsRequest, Optional.of(enrollmentFidoSignOptionsRequest.data.getRp()))

        then:
        noExceptionThrown()
    }

    def "we get a 422 when consent is not AWAITING_AUTHORISATION when trying to create a fido sign option"() {
        given:
        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")

        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV3(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        def consentId = responseConsent.getData().getConsentId();

        def enrollmentFidoSignOptionsRequest = TestRequestDataFactory.createEnrollmentFidoSignOptionsInput()
        enrollmentFidoSignOptionsRequest.getData().setConsentId(consentId)

        def enrollmentId = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest).getData().getEnrollmentId()
        def enrollment = enrollmentRepository.findByEnrollmentId(enrollmentId)
                .orElse(null)
        assert enrollment != null

        def consent = paymentConsentRepository.findByPaymentConsentId(consentId)
                .orElse(null)
        assert consent != null

        enrollment.setStatus(EnumEnrollmentStatus.AUTHORISED.name())
        consent.setStatus(EnumConsentStatus.AUTHORISED.name())
        enrollmentRepository.update(enrollment)
        paymentConsentRepository.update(consent)

        when:
        enrollmentService.createFidoSignOptions(enrollmentId, enrollmentFidoSignOptionsRequest, Optional.of(enrollmentFidoSignOptionsRequest.data.getRp()))

        then:
        def e = thrown(HttpStatusException)
        e.getMessage().startsWith("STATUS_CONSENTIMENTO_INVALIDO")
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
    }

    def "we can get 422 when cn is different from rp on enrollment fido-sign-options"() {
        given:
        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        CreatePaymentConsent paymentConsentRequest = TestRequestDataFactory.createPaymentConsentRequest("BID1", "REL1", "66.001.455/0001-30",
                "Bob Creditor", EnumCreditorPersonType.NATURAL, EnumAccountPaymentsType.CACC, "ispb1",
                "issuer1", "1234567890", accountHolder.getDocumentRel(), accountHolder.getDocumentIdentification(), EnumPaymentType.PIX.toString(),
                LocalDate.now(), "BRL", "100")
        ResponsePaymentConsentV2 responseConsent = paymentConsentService.createConsentV3(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), paymentConsentRequest)
        def consentId = responseConsent.getData().getConsentId();

        def enrollmentFidoSignOptionsRequest = TestRequestDataFactory.createEnrollmentFidoSignOptionsInput()
        enrollmentFidoSignOptionsRequest.getData().setConsentId(consentId)

        def enrollmentId = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest).getData().getEnrollmentId()
        def enrollment = enrollmentRepository.findByEnrollmentId(enrollmentId)
                .orElse(null)
        assert enrollment != null
        enrollment.setStatus(EnumEnrollmentStatus.AUTHORISED.name())
        enrollmentRepository.update(enrollment)

        when:
        enrollmentService.createFidoSignOptions(enrollmentId, enrollmentFidoSignOptionsRequest, Optional.of("test"))

        then: "422 unprocessable Entity should bet thrown"
        def e = thrown(HttpStatusException)

        e.getMessage().startsWith("RP_INVALIDA")
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY

    }

    def "we can update an enrollment"() {
        given:
        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def enrollmentId = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest).getData().getEnrollmentId()
        def enrollment = enrollmentRepository.findByEnrollmentId(enrollmentId)
                .orElse(null)
        assert enrollment != null
        enrollment.setStatus(EnumEnrollmentStatus.AWAITING_ACCOUNT_HOLDER_VALIDATION.name())
        enrollmentRepository.update(enrollment)
        def enrollmentUpdate = TestRequestDataFactory.createUpdateEnrollment(statusToUpdate)

        when:
        def response = enrollmentService.updateEnrollment(enrollmentId, enrollmentUpdate)

        then:
        noExceptionThrown()
        response.data.status == statusToUpdate

        where:
        statusToUpdate << [EnumEnrollmentStatus.AWAITING_ENROLLMENT, EnumEnrollmentStatus.REJECTED]
    }

    def "we can't update an enrollment with wrong status"() {
        given:
        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def enrollmentId = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest).getData().getEnrollmentId()
        def enrollment = enrollmentRepository.findByEnrollmentId(enrollmentId)
                .orElse(null)
        assert enrollment != null
        enrollment.setStatus(statusExpected.name())
        enrollmentRepository.update(enrollment)
        def enrollmentUpdate = TestRequestDataFactory.createUpdateEnrollment(statusToUpdate)

        when:
        def response = enrollmentService.updateEnrollment(enrollmentId, enrollmentUpdate)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.BAD_REQUEST

        where:
        statusExpected                           | statusToUpdate
        EnumEnrollmentStatus.AWAITING_ENROLLMENT | EnumEnrollmentStatus.AWAITING_ENROLLMENT
        EnumEnrollmentStatus.AWAITING_ENROLLMENT | EnumEnrollmentStatus.AUTHORISED
        EnumEnrollmentStatus.AWAITING_ENROLLMENT | EnumEnrollmentStatus.AWAITING_RISK_SIGNALS
        EnumEnrollmentStatus.AWAITING_ENROLLMENT | EnumEnrollmentStatus.AWAITING_ACCOUNT_HOLDER_VALIDATION
        EnumEnrollmentStatus.AWAITING_ACCOUNT_HOLDER_VALIDATION | EnumEnrollmentStatus.AUTHORISED
        EnumEnrollmentStatus.AWAITING_ACCOUNT_HOLDER_VALIDATION | EnumEnrollmentStatus.AWAITING_RISK_SIGNALS
        EnumEnrollmentStatus.AWAITING_ACCOUNT_HOLDER_VALIDATION | EnumEnrollmentStatus.AWAITING_ACCOUNT_HOLDER_VALIDATION
    }


    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }

}
