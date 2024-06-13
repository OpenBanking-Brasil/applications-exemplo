package com.raidiam.trustframework.bank.utils;

import com.google.common.io.Resources;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.raidiam.trustframework.bank.exceptions.TrustframeworkException;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;

@Singleton
public class JwtSigner {

    private final JWK signingKey;

    public JwtSigner() {
        this(loadKeys());
    }

    public JwtSigner(JWKSet jwkSet) {
        signingKey = findSigningJwk(jwkSet);
    }

    public String sign(String payload, JWTClaimsSet other) {

        try {
            JWTClaimsSet claimsSet = JWTClaimsSet.parse(payload);
            JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder(claimsSet);
            if (other != null) {
                other.getClaims().forEach(builder::claim);
            }
            claimsSet = builder.build();
            JWSHeader header = buildHeader(signingKey);
            JWSSigner signer = buildSigner(signingKey);
            return performSigning(header, claimsSet, signer);
        } catch (JOSEException | ParseException e) {
            throw new TrustframeworkException(e);
        }
    }

    private JWSSigner buildSigner(JWK signingJwk) throws JOSEException {
        if (signingJwk.getKeyType().equals(KeyType.RSA)) {
            return new RSASSASigner((RSAKey) signingJwk);
        }
        throw new TrustframeworkException("No signing key");
    }

    private JWSHeader buildHeader(JWK signingJwk) {
        Algorithm alg = signingJwk.getAlgorithm();
        if (alg == null) {
            throw new TrustframeworkException("No alg selected");
        }
        return new JWSHeader.Builder(JWSAlgorithm.parse(alg.getName())).
                type(new JOSEObjectType("JWT")).
                keyID(signingJwk.getKeyID()).
                build();
    }

    private JWK findSigningJwk(JWKSet jwkSet) {
        return jwkSet.getKeys().stream()
                .filter(k -> k.getKeyUse().equals(KeyUse.SIGNATURE))
                .findAny().orElseThrow(() -> new TrustframeworkException("Could not find a signing key"));
    }

    private String performSigning(JWSHeader header, JWTClaimsSet claimSet, JWSSigner signer) throws JOSEException {

        SignedJWT signJWT = new SignedJWT(header, claimSet);

        signJWT.sign(signer);

        return signJWT.serialize();
    }

    private static JWKSet loadKeys() {
        try {
            String jwks = Resources.toString(Resources.getResource("signing.jwks"
            ), Charset.defaultCharset());
            return JWKSet.parse(jwks);
        } catch (IOException | ParseException exception) {
            throw new TrustframeworkException("Could not load keys", exception);
        }
    }

}

