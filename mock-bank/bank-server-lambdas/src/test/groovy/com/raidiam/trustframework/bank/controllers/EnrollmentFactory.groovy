package com.raidiam.trustframework.bank.controllers

import com.raidiam.trustframework.mockbank.models.generated.*
import com.webauthn4j.converter.util.CborConverter
import com.webauthn4j.converter.util.ObjectConverter
import com.webauthn4j.data.attestation.AttestationObject
import com.webauthn4j.data.attestation.authenticator.*
import com.webauthn4j.data.attestation.statement.AttestationStatement
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier
import com.webauthn4j.data.attestation.statement.NoneAttestationStatement
import com.webauthn4j.data.client.ClientDataType
import com.webauthn4j.data.client.CollectedClientData
import com.webauthn4j.data.client.Origin
import com.webauthn4j.data.client.challenge.DefaultChallenge
import com.webauthn4j.data.extension.authenticator.RegistrationExtensionAuthenticatorOutput
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.RandomStringUtils

import java.security.*
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey
import java.time.LocalDate

class EnrollmentFactory {

    static CreateEnrollment createEnrollment() {
        createEnrollment(RandomStringUtils.random(11, false, true), "CPF")
    }


    static PatchEnrollment patchEnrollment(boolean isRejection) {
        patchEnrollment(isRejection, RandomStringUtils.random(11, false, true), "CPF")
    }


    static ConsentAuthorization enrollmentFidoAuthorisation() {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        def keyPair = keyPairGenerator.generateKeyPair()
        enrollmentFidoAuthorisation(keyPair.getPrivate(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), "https://test.origin.com")
    }

    static ConsentAuthorization enrollmentFidoAuthorisation(PrivateKey privateKey, String enrollmentId, String kid, String challenge, String originUrl) {
        def encodedKid = Base64.getEncoder().withoutPadding().encodeToString(kid.getBytes())
        def clientDataJson = getClientDataJson(false, challenge, originUrl)
        def authenticatorData = getAuthenticatorData()
        def signature = getSignature(clientDataJson, authenticatorData, privateKey)

        new ConsentAuthorization().data(new ConsentAuthorizationData()
                .enrollmentId(enrollmentId)
                .fidoAssertion(new ConsentAuthorizationDataFidoAssertion()
                        .id(encodedKid)
                        .rawId(encodedKid)
                        .type("public-key")
                        .response(new ConsentAuthorizationDataFidoAssertionResponse()
                                .clientDataJSON(Base64.getEncoder().withoutPadding().encodeToString(clientDataJson))
                                .authenticatorData(Base64.getEncoder().withoutPadding().encodeToString(authenticatorData))
                                .signature(signature)
                                .userHandle(UUID.randomUUID().toString())))
                .riskSignals(new ConsentAuthorizationDataRiskSignals()
                        .deviceId("5ad82a8f-37e5-4369-a1a3-be4b1fb9c034")
                        .isRootedDevice(true)
                        .screenBrightness(90.0)
                        .elapsedTimeSinceBoot(28800000)
                        .userTimeZoneOffset("-03")
                        .screenDimensions(new RiskSignalsDataScreenDimensions()
                                .width(5)
                                .height(5))
                        .osVersion("16.6")
                        .language("pt")
                        .accountTenure(LocalDate.now())))
    }

    static String getSignature(byte[] clientDataJson, byte[] authenticatorData, PrivateKey privateKey) {
        def clientDataHash = MessageDigest.getInstance("SHA-256").digest(clientDataJson)
        def payload = ArrayUtils.addAll(clientDataHash, authenticatorData)
        Signature signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(privateKey)
        signature.update(payload)
        Base64.getEncoder().withoutPadding().encodeToString(signature.sign())
    }

    static byte[] getAuthenticatorData() {
        var rpIdHash = MessageDigest.getInstance("SHA-256").digest(UUID.randomUUID().toString().getBytes())
        byte flags = Byte.parseByte("00000101", 2)
        byte[] signCount = new byte[4]
        byte[] payload = ArrayUtils.add(rpIdHash, flags)
        ArrayUtils.addAll(payload, signCount)
    }

    static EnrollmentFidoRegistration enrollmentFidoRegistration() {
        enrollmentFidoRegistration(UUID.randomUUID().toString(), "challenge", "https://test.origin.com")
    }


    static EnrollmentFidoRegistration enrollmentFidoRegistration(String keyId, String challenge, String originUrl) {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        def keyPair = keyPairGenerator.generateKeyPair()
        enrollmentFidoRegistration(keyId, challenge, originUrl, keyPair.getPublic())
    }


    static EnrollmentFidoRegistration enrollmentFidoRegistration(String keyId, String challenge, String originUrl, PublicKey publicKey) {
        String encodedKeyId = Base64.getEncoder().withoutPadding().encodeToString(keyId.getBytes())
        new EnrollmentFidoRegistration()
                .data(new EnrollmentFidoRegistrationData()
                        .id(encodedKeyId)
                        .rawId(encodedKeyId)
                        .type("public-key")
                        .authenticatorAttachment("TestAuthenticatorAttachment")
                        .clientExtensionResults(Map.of("Test", "Test"))
                        .response(new EnrollmentFidoRegistrationDataResponse()
                                .clientDataJSON(Base64.getEncoder().withoutPadding().encodeToString(getClientDataJson(true, challenge, originUrl)))
                                .attestationObject(getAttestationObject(keyId, publicKey))))
    }


    static COSEKey getCOSEKey(PublicKey publicKey) {
        if (publicKey instanceof RSAPublicKey) {
            return RSACOSEKey.create(publicKey, COSEAlgorithmIdentifier.RS256)
        }
        if (publicKey instanceof ECPublicKey) {
            return EC2COSEKey.create(publicKey, COSEAlgorithmIdentifier.ES256)
        }
        throw new IllegalArgumentException("invalid public key format")
    }

    static String getAttestationObject(String credentialId, PublicKey publicKey) {
        byte flags = Byte.parseByte("01000101", 2)
        // Bits set indicate the UP, UV, and AT flags as per the webauthn specification
        var rpIdHash = MessageDigest.getInstance("SHA-256").digest(UUID.randomUUID().toString().getBytes())


        AttestedCredentialData attestedCredentialData = new AttestedCredentialData(AAGUID.ZERO, credentialId.getBytes(), getCOSEKey(publicKey))
        AuthenticatorData<RegistrationExtensionAuthenticatorOutput> authenticatorData = new AuthenticatorData<>(rpIdHash, flags, 0, attestedCredentialData)
        AttestationStatement attestationStatement = new NoneAttestationStatement()
        AttestationObject attestationObject = new AttestationObject(authenticatorData, attestationStatement)
        CborConverter converter = new ObjectConverter().getCborConverter()
        Base64.getEncoder().withoutPadding().encodeToString(converter.writeValueAsBytes(attestationObject))
    }

    static byte[] getClientDataJson(boolean isClientRegistration, String challenge, String originUrl) {
        var converter = new ObjectConverter().getJsonConverter()
        def clientData = new CollectedClientData(
                isClientRegistration ? ClientDataType.WEBAUTHN_CREATE : ClientDataType.WEBAUTHN_GET,
                new DefaultChallenge(challenge.getBytes()),
                new Origin(originUrl),
                false,
                null
        )

        converter.writeValueAsBytes(clientData)
    }


    static PatchEnrollment patchEnrollment(boolean isRejection, String userIdentification, String userRel) {

        PatchEnrollmentDataCancellationReason reason = new PatchEnrollmentDataCancellationReason()

        if (isRejection) {
            reason.setRejectionReason(EnrollmentRejectionReason.DISPOSITIVO_INCOMPATIVEL)
        } else {
            reason.setRevocationReason(EnrollmentRevocationReason.MANUALMENTE)
        }

        PatchEnrollmentDataCancellationCancelledByDocument document = new PatchEnrollmentDataCancellationCancelledByDocument()
        document.setIdentification(userIdentification)
        document.setRel(userRel)

        PatchEnrollmentDataCancellationCancelledBy cancelledBy = new PatchEnrollmentDataCancellationCancelledBy()
        cancelledBy.setDocument(document)


        PatchEnrollmentDataCancellation cancellation = new PatchEnrollmentDataCancellation()
        cancellation.setCancelledBy(cancelledBy)
        cancellation.setReason(reason)
        cancellation.setAdditionalInformation("Additional information")

        PatchEnrollmentData data = new PatchEnrollmentData()
        data.setCancellation(cancellation)

        PatchEnrollment patchEnrollment = new PatchEnrollment()
        patchEnrollment.setData(data)
        patchEnrollment
    }

    static CreateEnrollment createEnrollment(String loggedUserIdentification, String loggedUserRel, boolean withDebtor = false) {
        CreateEnrollmentData enrollmentData = new CreateEnrollmentData()
        enrollmentData.setPermissions([EnumEnrollmentPermission.PAYMENTS_INITIATE])
        CreateEnrollment enrollmentReq = new CreateEnrollment().data(enrollmentData)
        enrollmentReq.data.businessEntity(new BusinessEntity()
                .document(new BusinessEntityDocument()
                        .identification(RandomStringUtils.random(14, false, true))
                        .rel("ASDF")))
        enrollmentReq.data.loggedUser(new LoggedUser()
                .document(new Document()
                        .identification(loggedUserIdentification)
                        .rel(loggedUserRel)
                ))
        if (withDebtor) {
            enrollmentReq.data.debtorAccount(new DebtorAccount()
                    .accountType(EnumAccountPaymentsType.CACC)
                    .ispb("12345678"))
        }


        enrollmentReq
    }
}
