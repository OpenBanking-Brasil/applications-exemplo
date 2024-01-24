package com.raidiam.trustframework.bank.utils;

import com.raidiam.trustframework.bank.exceptions.TrustframeworkException;
import com.webauthn4j.converter.exception.DataConversionException;
import com.webauthn4j.converter.util.CborConverter;
import com.webauthn4j.converter.util.JsonConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.attestation.AttestationObject;
import com.webauthn4j.data.client.CollectedClientData;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UncheckedIOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

public class FidoUtils {

    private static final Logger LOG = LoggerFactory.getLogger(FidoUtils.class);


    private FidoUtils() {
        throw new TrustframeworkException("Utility class");
    }


    public static AttestationObject decodeAttestationObject(String encodedObject) {
        LOG.info("Decoding attestation object");
        CborConverter converter = new ObjectConverter().getCborConverter();
        try {
            return converter.readValue(decode(encodedObject), AttestationObject.class);
        } catch (DataConversionException | UncheckedIOException e) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "could not decode attestation object");
        }
    }

    public static CollectedClientData decodeClientDataJson(String clientData) {
        LOG.info("Decoding client data JSON");
        JsonConverter converter = new ObjectConverter().getJsonConverter();
        try {
            return converter.readValue(new String(decode(clientData)), CollectedClientData.class);
        } catch (DataConversionException | UncheckedIOException e) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "could not decode client data");
        }
    }

    public static byte[] decode(String data) {
        LOG.info("Decoding base 64");
        try {
            return Base64.getDecoder().decode(data);
        } catch (IllegalArgumentException e) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "could not decode base64");
        }
    }

    public static boolean validateSignature(PublicKey publicKey, byte[] clientDataJsonBytes, byte[] authenticatorDataBytes, byte[] signature) throws GeneralSecurityException {
        LOG.info("Validating signature");
        byte[] clientDataHash = MessageDigest.getInstance("SHA-256").digest(clientDataJsonBytes);
        final byte[] expectedPayload = ArrayUtils.addAll(clientDataHash, authenticatorDataBytes);
        Signature instance = Signature.getInstance("SHA256withRSA");
        instance.initVerify(publicKey);
        instance.update(expectedPayload);
        return instance.verify(signature);
    }


}
