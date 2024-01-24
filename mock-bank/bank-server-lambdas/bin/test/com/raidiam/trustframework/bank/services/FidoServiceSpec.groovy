package com.raidiam.trustframework.bank.services

import com.nimbusds.jose.util.Base64URL
import com.raidiam.trustframework.bank.CleanupSpecification
import com.raidiam.trustframework.bank.TestRequestDataFactory
import com.raidiam.trustframework.bank.domain.AccountEntity
import com.raidiam.trustframework.bank.domain.AccountHolderEntity
import com.raidiam.trustframework.bank.domain.FidoJwkEntity
import com.raidiam.trustframework.mockbank.models.generated.EnrollmentsConsentAuthorizationErrorCode
import com.raidiam.trustframework.mockbank.models.generated.EnrollmentsFidoRegistrationErrorCode
import com.raidiam.trustframework.mockbank.models.generated.EnumConsentStatus
import com.raidiam.trustframework.mockbank.models.generated.EnumEnrollmentStatus
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.bouncycastle.jce.provider.BouncyCastleProvider
import spock.lang.Shared
import spock.lang.Stepwise
import spock.lang.Unroll

import javax.inject.Inject
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.Security
import java.security.Signature
import java.security.spec.ECGenParameterSpec

import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccount
import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccountHolder
import static com.raidiam.trustframework.bank.controllers.EnrollmentFactory.*

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
class FidoServiceSpec extends CleanupSpecification {


    @Inject
    EnrollmentService enrollmentService

    @Inject
    FidoService fidoService

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
            Security.addProvider(new BouncyCastleProvider())
            accountHolder = accountHolderRepository.save(anAccountHolder())
            account = accountRepository.save(anAccount(accountHolder))
            runSetup = false
        }
    }


    def "we can create fido registration"() {
        given:
        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def enrollmentFidoOptionsRequest = TestRequestDataFactory.createEnrollmentFidoOptionsInput()

        def enrollment = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest)
        def enrollmentId = enrollment.getData().getEnrollmentId()


        enrollmentRepository.findByEnrollmentId(enrollmentId).ifPresent() { e ->
            e.setStatus(EnumEnrollmentStatus.AWAITING_ENROLLMENT.toString())
            enrollmentRepository.update(e)
        }

        def registrationOptions = enrollmentService.createFidoRegistrationOptions(enrollment.getData().getEnrollmentId(), enrollmentFidoOptionsRequest, Optional.ofNullable(enrollmentFidoOptionsRequest.data.getRp()))

        def challenge = new String(registrationOptions.getData().getChallenge())
        def origin = "https://test.origin.com"
        def keyId = UUID.randomUUID().toString()
        def registrationRequest = enrollmentFidoRegistration(keyId, challenge, origin)

        when:
        fidoService.createFidoRegistration(enrollmentId, registrationRequest)

        then:
        noExceptionThrown()
        def enrollmentEntity = enrollmentRepository.findByEnrollmentId(enrollmentId).orElse(null)
        def fidoJwkEntity = fidoJwkRepository.findByKid(keyId).orElse(null)
        enrollmentEntity != null
        fidoJwkEntity != null
        enrollmentEntity.getStatus() == EnumEnrollmentStatus.AUTHORISED.toString()
        fidoJwkEntity.getKid() == keyId
        fidoJwkEntity.getE() != null
        fidoJwkEntity.getN() != null
        and:
        fidoJwkEntity.getRsaPublicKey() != null
        fidoJwkEntity.getJwk() != null
    }


    def "we cant create registration if enrollment does not exist"() {
        given:
        def enrollmentId = "testId"
        def challenge = "testChallenge"
        def origin = "https://test.origin.com"
        def keyId = UUID.randomUUID().toString()
        def registrationRequest = enrollmentFidoRegistration(keyId, challenge, origin)

        when:
        fidoService.createFidoRegistration(enrollmentId, registrationRequest)

        then:
        def e = thrown(HttpStatusException)
        def enrollmentEntity = enrollmentRepository.findByEnrollmentId(enrollmentId).orElse(null)
        def fidoJwkEntity = fidoJwkRepository.findByKid(keyId).orElse(null)
        enrollmentEntity == null
        fidoJwkEntity == null
        e.getStatus() == HttpStatus.NOT_FOUND
        e.getMessage() == "Enrollment not found"
    }


    @Unroll
    def "we cant create registration if enrollment status is not AWAITING_ENROLLMENT"() {
        given:
        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def enrollmentFidoOptionsRequest = TestRequestDataFactory.createEnrollmentFidoOptionsInput()

        def enrollment = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest)
        def enrollmentId = enrollment.getData().getEnrollmentId()

        enrollmentRepository.findByEnrollmentId(enrollmentId).ifPresent() { e ->
            e.setStatus(EnumEnrollmentStatus.AWAITING_ENROLLMENT.toString())
            enrollmentRepository.update(e)
        }

        def registrationOptions = enrollmentService.createFidoRegistrationOptions(enrollment.getData().getEnrollmentId(), enrollmentFidoOptionsRequest, Optional.ofNullable(enrollmentFidoOptionsRequest.data.getRp()))

        def challenge = new String(registrationOptions.getData().getChallenge())
        def origin = "https://test.origin.com"
        def keyId = UUID.randomUUID().toString()
        def registrationRequest = enrollmentFidoRegistration(keyId, challenge, origin)

        enrollmentRepository.findByEnrollmentId(enrollmentId).ifPresent() { e ->
            e.setStatus(status.toString())
            enrollmentRepository.update(e)
        }

        when:
        fidoService.createFidoRegistration(enrollmentId, registrationRequest)

        then:
        def e = thrown(HttpStatusException)
        def enrollmentEntity = enrollmentRepository.findByEnrollmentId(enrollmentId).orElse(null)
        def fidoJwkEntity = fidoJwkRepository.findByKid(keyId).orElse(null)
        enrollmentEntity != null
        fidoJwkEntity == null
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == String.format("%s: Vinculo inválido", EnrollmentsFidoRegistrationErrorCode.STATUS_VINCULO_INVALIDO.toString())

        where:
        status << [
                EnumEnrollmentStatus.AWAITING_RISK_SIGNALS,
                EnumEnrollmentStatus.AWAITING_ACCOUNT_HOLDER_VALIDATION,
                EnumEnrollmentStatus.AUTHORISED,
                EnumEnrollmentStatus.REVOKED,
                EnumEnrollmentStatus.REJECTED
        ]
    }


    def "we cant create registration if IDs dont match"() {
        given:
        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def enrollmentFidoOptionsRequest = TestRequestDataFactory.createEnrollmentFidoOptionsInput()

        def enrollment = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest)
        def enrollmentId = enrollment.getData().getEnrollmentId()


        enrollmentRepository.findByEnrollmentId(enrollmentId).ifPresent() { e ->
            e.setStatus(EnumEnrollmentStatus.AWAITING_ENROLLMENT.toString())
            enrollmentRepository.update(e)
        }

        def registrationOptions = enrollmentService.createFidoRegistrationOptions(enrollment.getData().getEnrollmentId(), enrollmentFidoOptionsRequest, Optional.ofNullable(enrollmentFidoOptionsRequest.data.getRp()))

        def challenge = new String(registrationOptions.getData().getChallenge())
        def origin = "https://test.origin.com"
        def keyId = UUID.randomUUID().toString()
        def registrationRequest = enrollmentFidoRegistration(keyId, challenge, origin)
        registrationRequest.data
                .id(Base64.encoder.withoutPadding().encodeToString("fakeId".getBytes()))
                .rawId(Base64.encoder.withoutPadding().encodeToString("fakeId".getBytes()))

        when:
        fidoService.createFidoRegistration(enrollmentId, registrationRequest)


        then:
        def e = thrown(HttpStatusException)
        def enrollmentEntity = enrollmentRepository.findByEnrollmentId(enrollmentId).orElse(null)
        def fidoJwkEntity = fidoJwkRepository.findByKid(keyId).orElse(null)
        enrollmentEntity != null
        fidoJwkEntity == null
        e.getStatus() == HttpStatus.BAD_REQUEST
        e.getMessage() == "id must the be same as credentials ID"

        and:
        enrollmentEntity.status == EnumEnrollmentStatus.REJECTED.toString()

    }


    def "we cant create registration if challenges dont match"() {
        given:
        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def enrollmentFidoOptionsRequest = TestRequestDataFactory.createEnrollmentFidoOptionsInput()

        def enrollment = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest)
        def enrollmentId = enrollment.getData().getEnrollmentId()


        enrollmentRepository.findByEnrollmentId(enrollmentId).ifPresent() { e ->
            e.setStatus(EnumEnrollmentStatus.AWAITING_ENROLLMENT.toString())
            enrollmentRepository.update(e)
        }

        enrollmentService.createFidoRegistrationOptions(enrollment.getData().getEnrollmentId(), enrollmentFidoOptionsRequest, Optional.ofNullable(enrollmentFidoOptionsRequest.data.getRp()))

        def challenge = "fakeChallenge"
        def origin = "https://test.origin.com"
        def keyId = UUID.randomUUID().toString()
        def registrationRequest = enrollmentFidoRegistration(keyId, challenge, origin)

        when:
        fidoService.createFidoRegistration(enrollmentId, registrationRequest)


        then:
        def e = thrown(HttpStatusException)
        def enrollmentEntity = enrollmentRepository.findByEnrollmentId(enrollmentId).orElse(null)
        def fidoJwkEntity = fidoJwkRepository.findByKid(keyId).orElse(null)
        enrollmentEntity != null
        fidoJwkEntity == null
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == String.format("%s: %s", EnrollmentsFidoRegistrationErrorCode.PUBLIC_KEY_INVALIDA, EnrollmentsFidoRegistrationErrorCode.PUBLIC_KEY_INVALIDA)

        and:
        enrollmentEntity.status == EnumEnrollmentStatus.REJECTED.toString()

    }


    def "we can trigger mock origin 422 error"() {
        given:
        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def enrollmentFidoOptionsRequest = TestRequestDataFactory.createEnrollmentFidoOptionsInput()

        def enrollment = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest)
        def enrollmentId = enrollment.getData().getEnrollmentId()


        enrollmentRepository.findByEnrollmentId(enrollmentId).ifPresent() { e ->
            e.setStatus(EnumEnrollmentStatus.AWAITING_ENROLLMENT.toString())
            enrollmentRepository.update(e)
        }

        def registrationOptions = enrollmentService.createFidoRegistrationOptions(enrollment.getData().getEnrollmentId(), enrollmentFidoOptionsRequest, Optional.ofNullable(enrollmentFidoOptionsRequest.data.getRp()))

        def challenge = new String(registrationOptions.getData().getChallenge())
        def origin = "INVALID_ORIGIN"
        def keyId = UUID.randomUUID().toString()
        def registrationRequest = enrollmentFidoRegistration(keyId, challenge, origin)
        when:
        fidoService.createFidoRegistration(enrollmentId, registrationRequest)

        then:
        def e = thrown(HttpStatusException)
        def enrollmentEntity = enrollmentRepository.findByEnrollmentId(enrollmentId).orElse(null)
        def fidoJwkEntity = fidoJwkRepository.findByKid(keyId).orElse(null)
        enrollmentEntity != null
        fidoJwkEntity == null
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == String.format("%s: %s", EnrollmentsFidoRegistrationErrorCode.ORIGEM_FIDO_INVALIDA, EnrollmentsFidoRegistrationErrorCode.ORIGEM_FIDO_INVALIDA)

        and:
        enrollmentEntity.status == EnumEnrollmentStatus.REJECTED.toString()

    }


    def "we cant create registration with unsupported public key"() {
        given:
        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def enrollmentFidoOptionsRequest = TestRequestDataFactory.createEnrollmentFidoOptionsInput()

        def enrollment = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest)
        def enrollmentId = enrollment.getData().getEnrollmentId()


        enrollmentRepository.findByEnrollmentId(enrollmentId).ifPresent() { e ->
            e.setStatus(EnumEnrollmentStatus.AWAITING_ENROLLMENT.toString())
            enrollmentRepository.update(e)
        }

        def registrationOptions = enrollmentService.createFidoRegistrationOptions(enrollment.getData().getEnrollmentId(), enrollmentFidoOptionsRequest, Optional.ofNullable(enrollmentFidoOptionsRequest.data.getRp()))

        def challenge = new String(registrationOptions.getData().getChallenge())
        def origin = "https://test.origin.com"
        def keyId = UUID.randomUUID().toString()

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC")
        keyPairGenerator.initialize(new ECGenParameterSpec("prime256v1"))
        def pair = keyPairGenerator.generateKeyPair()

        def registrationRequest = enrollmentFidoRegistration(keyId, challenge, origin, pair.getPublic())
        when:
        fidoService.createFidoRegistration(enrollmentId, registrationRequest)


        then:
        def e = thrown(HttpStatusException)
        def enrollmentEntity = enrollmentRepository.findByEnrollmentId(enrollmentId).orElse(null)
        def fidoJwkEntity = fidoJwkRepository.findByKid(keyId).orElse(null)
        enrollmentEntity != null
        fidoJwkEntity == null
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == String.format("%s: unsupported public key", EnrollmentsFidoRegistrationErrorCode.PUBLIC_KEY_INVALIDA)

        and:
        enrollmentEntity.status == EnumEnrollmentStatus.REJECTED.toString()
    }


    def "we cant create fido registration with duplicated public key"() {
        given:
        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def enrollmentFidoOptionsRequest = TestRequestDataFactory.createEnrollmentFidoOptionsInput()

        def enrollment = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest)
        def enrollmentId = enrollment.getData().getEnrollmentId()


        enrollmentRepository.findByEnrollmentId(enrollmentId).ifPresent() { e ->
            e.setStatus(EnumEnrollmentStatus.AWAITING_ENROLLMENT.toString())
            enrollmentRepository.update(e)
        }

        def registrationOptions = enrollmentService.createFidoRegistrationOptions(enrollment.getData().getEnrollmentId(), enrollmentFidoOptionsRequest, Optional.ofNullable(enrollmentFidoOptionsRequest.data.getRp()))

        def challenge = new String(registrationOptions.getData().getChallenge())
        def origin = "https://test.origin.com"
        def keyId = UUID.randomUUID().toString()
        def registrationRequest = enrollmentFidoRegistration(keyId, challenge, origin)


        def duplicatedKey = new FidoJwkEntity()
        duplicatedKey.setKid(keyId)
        duplicatedKey.setE(new Base64URL("abc"))
        duplicatedKey.setN(new Base64URL("abc"))
        fidoJwkRepository.save(duplicatedKey)

        when:
        fidoService.createFidoRegistration(enrollmentId, registrationRequest)


        then:
        def e = thrown(HttpStatusException)
        def enrollmentEntity = enrollmentRepository.findByEnrollmentId(enrollmentId).orElse(null)
        def fidoJwkEntity = fidoJwkRepository.findByKid(keyId).orElse(null)
        enrollmentEntity != null
        fidoJwkEntity != null
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == String.format("%s: duplicated public key", EnrollmentsFidoRegistrationErrorCode.PUBLIC_KEY_INVALIDA)

        and:
        enrollmentEntity.status == EnumEnrollmentStatus.REJECTED.toString()
    }


    def "we can create fido authorisation"() {
        given:
        def consentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def consentResponse = paymentConsentService.createConsentV2(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), consentRequest)
        def consentId = consentResponse.getData().getConsentId()


        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def enrollmentFidoOptionsRequest = TestRequestDataFactory.createEnrollmentFidoOptionsInput()

        def enrollment = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest)
        def enrollmentId = enrollment.getData().getEnrollmentId()


        enrollmentRepository.findByEnrollmentId(enrollmentId).ifPresent() { e ->
            e.setStatus(EnumEnrollmentStatus.AWAITING_ENROLLMENT.toString())
            enrollmentRepository.update(e)
        }

        def registrationOptions = enrollmentService.createFidoRegistrationOptions(enrollment.getData().getEnrollmentId(), enrollmentFidoOptionsRequest, Optional.ofNullable(enrollmentFidoOptionsRequest.data.getRp()))

        def registrationChallenge = new String(registrationOptions.getData().getChallenge())
        def origin = "https://test.origin.com"
        def keyId = UUID.randomUUID().toString()

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        def keyPair = keyPairGenerator.generateKeyPair()

        def registrationRequest = enrollmentFidoRegistration(keyId, registrationChallenge, origin, keyPair.getPublic())
        fidoService.createFidoRegistration(enrollmentId, registrationRequest)


        def signOptionsRequest = TestRequestDataFactory.createEnrollmentFidoSignOptionsInput(consentId)

        def certificateCn = Optional.ofNullable(signOptionsRequest.getData().getRp())
        def signOptionsResponse = enrollmentService.createFidoSignOptions(enrollmentId, signOptionsRequest, certificateCn)
        def signChallenge = new String(signOptionsResponse.getData().getChallenge())

        def authorisationRequest = enrollmentFidoAuthorisation(keyPair.getPrivate(), enrollmentId, keyId, signChallenge, origin)
        when:
        fidoService.createFidoAuthorisation(consentId, authorisationRequest)
        def consentEntity = paymentConsentRepository.findByPaymentConsentId(consentId).orElse(null)

        then:
        noExceptionThrown()
        consentEntity != null
        consentEntity.getStatus() == EnumConsentStatus.AUTHORISED.toString()
    }


    def "we cant create fido authorisation if consent status is not AWAITING_AUTHORISATION"() {
        def consentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def consentResponse = paymentConsentService.createConsentV2(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), consentRequest)
        def consentId = consentResponse.getData().getConsentId()


        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def enrollmentFidoOptionsRequest = TestRequestDataFactory.createEnrollmentFidoOptionsInput()

        def enrollment = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest)
        def enrollmentId = enrollment.getData().getEnrollmentId()


        enrollmentRepository.findByEnrollmentId(enrollmentId).ifPresent() { e ->
            e.setStatus(EnumEnrollmentStatus.AWAITING_ENROLLMENT.toString())
            enrollmentRepository.update(e)
        }

        def registrationOptions = enrollmentService.createFidoRegistrationOptions(enrollment.getData().getEnrollmentId(), enrollmentFidoOptionsRequest, Optional.ofNullable(enrollmentFidoOptionsRequest.data.getRp()))

        def registrationChallenge = new String(registrationOptions.getData().getChallenge())
        def origin = "https://test.origin.com"
        def keyId = UUID.randomUUID().toString()

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        def keyPair = keyPairGenerator.generateKeyPair()

        def registrationRequest = enrollmentFidoRegistration(keyId, registrationChallenge, origin, keyPair.getPublic())
        fidoService.createFidoRegistration(enrollmentId, registrationRequest)


        def signOptionsRequest = TestRequestDataFactory.createEnrollmentFidoSignOptionsInput(consentId)

        def certificateCn = Optional.ofNullable(signOptionsRequest.getData().getRp())
        def signOptionsResponse = enrollmentService.createFidoSignOptions(enrollmentId, signOptionsRequest, certificateCn)
        def signChallenge = new String(signOptionsResponse.getData().getChallenge())

        def authorisationRequest = enrollmentFidoAuthorisation(keyPair.getPrivate(), enrollmentId, keyId, signChallenge, origin)

        def consentEntity = paymentConsentRepository.findByPaymentConsentId(consentId).orElse(null)
        assert consentEntity != null
        consentEntity.setStatus(status.toString())
        paymentConsentRepository.update(consentEntity)


        when:
        fidoService.createFidoAuthorisation(consentId, authorisationRequest)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "STATUS_CONSENTIMENTO_INVALIDO: Consentimento inválido"
        def enrollmentEntity = enrollmentRepository.findByEnrollmentId(enrollmentId).orElse(null)
        enrollmentEntity != null
        enrollmentEntity.getStatus() == EnumEnrollmentStatus.REJECTED.toString()

        where:
        status << [EnumConsentStatus.AUTHORISED, EnumConsentStatus.REJECTED]
    }


    def "we cant create fido authorisation if enrollment status is not AUTHORISED"() {
        def consentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def consentResponse = paymentConsentService.createConsentV2(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), consentRequest)
        def consentId = consentResponse.getData().getConsentId()


        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def enrollmentFidoOptionsRequest = TestRequestDataFactory.createEnrollmentFidoOptionsInput()

        def enrollment = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest)
        def enrollmentId = enrollment.getData().getEnrollmentId()


        enrollmentRepository.findByEnrollmentId(enrollmentId).ifPresent() { e ->
            e.setStatus(EnumEnrollmentStatus.AWAITING_ENROLLMENT.toString())
            enrollmentRepository.update(e)
        }

        def registrationOptions = enrollmentService.createFidoRegistrationOptions(enrollment.getData().getEnrollmentId(), enrollmentFidoOptionsRequest, Optional.ofNullable(enrollmentFidoOptionsRequest.data.getRp()))

        def registrationChallenge = new String(registrationOptions.getData().getChallenge())
        def origin = "https://test.origin.com"
        def keyId = UUID.randomUUID().toString()

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        def keyPair = keyPairGenerator.generateKeyPair()

        def registrationRequest = enrollmentFidoRegistration(keyId, registrationChallenge, origin, keyPair.getPublic())
        fidoService.createFidoRegistration(enrollmentId, registrationRequest)


        def signOptionsRequest = TestRequestDataFactory.createEnrollmentFidoSignOptionsInput(consentId)

        def certificateCn = Optional.ofNullable(signOptionsRequest.getData().getRp())
        def signOptionsResponse = enrollmentService.createFidoSignOptions(enrollmentId, signOptionsRequest, certificateCn)
        def signChallenge = new String(signOptionsResponse.getData().getChallenge())

        def authorisationRequest = enrollmentFidoAuthorisation(keyPair.getPrivate(), enrollmentId, keyId, signChallenge, origin)


        def enrollmentEntity = enrollmentRepository.findByEnrollmentId(enrollmentId).orElse(null)
        assert enrollmentEntity != null
        enrollmentEntity.setStatus(status.toString())
        enrollmentRepository.update(enrollmentEntity)

        when:
        fidoService.createFidoAuthorisation(consentId, authorisationRequest)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == "STATUS_VINCULO_INVALIDO: Vinculo inválido"
        def updatedEnrollment = enrollmentRepository.findByEnrollmentId(enrollmentId).orElse(null)
        updatedEnrollment != null
        updatedEnrollment.getStatus() == EnumEnrollmentStatus.REJECTED.toString()

        where:
        status << [
                EnumEnrollmentStatus.REJECTED,
                EnumEnrollmentStatus.REVOKED,
                EnumEnrollmentStatus.AWAITING_ENROLLMENT,
                EnumEnrollmentStatus.AWAITING_RISK_SIGNALS,
                EnumEnrollmentStatus.AWAITING_ACCOUNT_HOLDER_VALIDATION
        ]
    }


    def "we cant create fido authorisation without registered public key"() {
        given:
        def consentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def consentResponse = paymentConsentService.createConsentV2(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), consentRequest)
        def consentId = consentResponse.getData().getConsentId()


        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())

        def enrollment = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest)
        def enrollmentId = enrollment.getData().getEnrollmentId()

        enrollmentRepository.findByEnrollmentId(enrollmentId).ifPresent() { e ->
            e.setStatus(EnumEnrollmentStatus.AUTHORISED.toString())
            enrollmentRepository.update(e)
        }


        def origin = "https://test.origin.com"
        def keyId = UUID.randomUUID().toString()
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        def keyPair = keyPairGenerator.generateKeyPair()

        def signOptionsRequest = TestRequestDataFactory.createEnrollmentFidoSignOptionsInput(consentId)

        def certificateCn = Optional.ofNullable(signOptionsRequest.getData().getRp())
        def signOptionsResponse = enrollmentService.createFidoSignOptions(enrollmentId, signOptionsRequest, certificateCn)
        def signChallenge = new String(signOptionsResponse.getData().getChallenge())

        def authorisationRequest = enrollmentFidoAuthorisation(keyPair.getPrivate(), enrollmentId, keyId, signChallenge, origin)

        when:
        fidoService.createFidoAuthorisation(consentId, authorisationRequest)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == String.format("%s: could not find registered public key", EnrollmentsConsentAuthorizationErrorCode.RISCO)
        def updatedEnrollment = enrollmentRepository.findByEnrollmentId(enrollmentId).orElse(null)
        updatedEnrollment != null
        updatedEnrollment.getStatus() == EnumEnrollmentStatus.REJECTED.toString()
    }


    def "we cant create fido authorisation without sign options challenge"() {
        given:
        def consentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def consentResponse = paymentConsentService.createConsentV2(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), consentRequest)
        def consentId = consentResponse.getData().getConsentId()


        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def enrollmentFidoOptionsRequest = TestRequestDataFactory.createEnrollmentFidoOptionsInput()

        def enrollment = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest)
        def enrollmentId = enrollment.getData().getEnrollmentId()


        enrollmentRepository.findByEnrollmentId(enrollmentId).ifPresent() { e ->
            e.setStatus(EnumEnrollmentStatus.AWAITING_ENROLLMENT.toString())
            enrollmentRepository.update(e)
        }

        def registrationOptions = enrollmentService.createFidoRegistrationOptions(enrollment.getData().getEnrollmentId(), enrollmentFidoOptionsRequest, Optional.ofNullable(enrollmentFidoOptionsRequest.data.getRp()))

        def registrationChallenge = new String(registrationOptions.getData().getChallenge())
        def origin = "https://test.origin.com"
        def keyId = UUID.randomUUID().toString()

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        def keyPair = keyPairGenerator.generateKeyPair()

        def registrationRequest = enrollmentFidoRegistration(keyId, registrationChallenge, origin, keyPair.getPublic())
        fidoService.createFidoRegistration(enrollmentId, registrationRequest)

        def signChallenge = Base64.getEncoder().encodeToString(new SecureRandom().generateSeed(10))
        def authorisationRequest = enrollmentFidoAuthorisation(keyPair.getPrivate(), enrollmentId, keyId, signChallenge, origin)

        when:
        fidoService.createFidoAuthorisation(consentId, authorisationRequest)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.BAD_REQUEST
        e.getMessage() == "Could not find Sign Options challenge"
        def updatedEnrollment = enrollmentRepository.findByEnrollmentId(enrollmentId).orElse(null)
        updatedEnrollment != null
        updatedEnrollment.getStatus() == EnumEnrollmentStatus.REJECTED.toString()
    }


    def "we cant create fido authorisation if challenges dont match"() {
        given:
        def consentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def consentResponse = paymentConsentService.createConsentV2(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), consentRequest)
        def consentId = consentResponse.getData().getConsentId()


        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def enrollmentFidoOptionsRequest = TestRequestDataFactory.createEnrollmentFidoOptionsInput()

        def enrollment = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest)
        def enrollmentId = enrollment.getData().getEnrollmentId()


        enrollmentRepository.findByEnrollmentId(enrollmentId).ifPresent() { e ->
            e.setStatus(EnumEnrollmentStatus.AWAITING_ENROLLMENT.toString())
            enrollmentRepository.update(e)
        }

        def registrationOptions = enrollmentService.createFidoRegistrationOptions(enrollment.getData().getEnrollmentId(), enrollmentFidoOptionsRequest, Optional.ofNullable(enrollmentFidoOptionsRequest.data.getRp()))

        def registrationChallenge = new String(registrationOptions.getData().getChallenge())
        def origin = "https://test.origin.com"
        def keyId = UUID.randomUUID().toString()

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        def keyPair = keyPairGenerator.generateKeyPair()

        def registrationRequest = enrollmentFidoRegistration(keyId, registrationChallenge, origin, keyPair.getPublic())
        fidoService.createFidoRegistration(enrollmentId, registrationRequest)


        def signOptionsRequest = TestRequestDataFactory.createEnrollmentFidoSignOptionsInput(consentId)

        def certificateCn = Optional.ofNullable(signOptionsRequest.getData().getRp())
        enrollmentService.createFidoSignOptions(enrollmentId, signOptionsRequest, certificateCn)
        def signChallenge = Base64.getEncoder().encodeToString(new SecureRandom().generateSeed(10))

        def authorisationRequest = enrollmentFidoAuthorisation(keyPair.getPrivate(), enrollmentId, keyId, signChallenge, origin)

        when:
        fidoService.createFidoAuthorisation(consentId, authorisationRequest)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == String.format("%s: invalid challenge", EnrollmentsConsentAuthorizationErrorCode.RISCO)
        def updatedEnrollment = enrollmentRepository.findByEnrollmentId(enrollmentId).orElse(null)
        updatedEnrollment != null
        updatedEnrollment.getStatus() == EnumEnrollmentStatus.REJECTED.toString()
    }

    @Unroll
    def "we cant create fido authorisation if signature is invalid"() {
        given:
        def consentRequest = TestRequestDataFactory.testPaymentConsent(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def consentResponse = paymentConsentService.createConsentV2(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), consentRequest)
        def consentId = consentResponse.getData().getConsentId()


        def enrollmentRequest = createEnrollment(accountHolder.getDocumentIdentification(), accountHolder.getDocumentRel())
        def enrollmentFidoOptionsRequest = TestRequestDataFactory.createEnrollmentFidoOptionsInput()

        def enrollment = enrollmentService.createEnrollment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), enrollmentRequest)
        def enrollmentId = enrollment.getData().getEnrollmentId()


        enrollmentRepository.findByEnrollmentId(enrollmentId).ifPresent() { e ->
            e.setStatus(EnumEnrollmentStatus.AWAITING_ENROLLMENT.toString())
            enrollmentRepository.update(e)
        }

        def registrationOptions = enrollmentService.createFidoRegistrationOptions(enrollment.getData().getEnrollmentId(), enrollmentFidoOptionsRequest, Optional.ofNullable(enrollmentFidoOptionsRequest.data.getRp()))

        def registrationChallenge = new String(registrationOptions.getData().getChallenge())
        def origin = "https://test.origin.com"
        def keyId = UUID.randomUUID().toString()

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        def keyPair = keyPairGenerator.generateKeyPair()

        def registrationRequest = enrollmentFidoRegistration(keyId, registrationChallenge, origin, keyPair.getPublic())
        fidoService.createFidoRegistration(enrollmentId, registrationRequest)


        def signOptionsRequest = TestRequestDataFactory.createEnrollmentFidoSignOptionsInput(consentId)

        def certificateCn = Optional.ofNullable(signOptionsRequest.getData().getRp())
        def signOptionsResponse = enrollmentService.createFidoSignOptions(enrollmentId, signOptionsRequest, certificateCn)
        def signChallenge = new String(signOptionsResponse.getData().getChallenge())

        if (isDifferentPrivateKey) {
            keyPair = keyPairGenerator.generateKeyPair()
        }

        def authorisationRequest = enrollmentFidoAuthorisation(keyPair.getPrivate(), enrollmentId, keyId, signChallenge, origin)

        if (isIncorrectSignature) {
            Signature signature = Signature.getInstance("SHA256withRSA")
            signature.initSign(keyPair.getPrivate())
            signature.update("wrongPayload".getBytes())
            def wrongSignature = Base64.getEncoder().withoutPadding().encodeToString(signature.sign())
            authorisationRequest.getData().getFidoAssertion().getResponse().setSignature(wrongSignature)
        }

        if (isInvalidSignature) {
            authorisationRequest.getData().getFidoAssertion().getResponse().setSignature("invalidSignature")
        }

        when:
        fidoService.createFidoAuthorisation(consentId, authorisationRequest)

        then:
        def e = thrown(HttpStatusException)
        e.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        e.getMessage() == String.format("%s: invalid signature", EnrollmentsConsentAuthorizationErrorCode.RISCO)
        def updatedEnrollment = enrollmentRepository.findByEnrollmentId(enrollmentId).orElse(null)
        updatedEnrollment != null
        updatedEnrollment.getStatus() == EnumEnrollmentStatus.REJECTED.toString()

        where:
        isDifferentPrivateKey | isIncorrectSignature | isInvalidSignature
        true                  | false                | false
        false                 | true                 | false
        false                 | false                | true
    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }

}
