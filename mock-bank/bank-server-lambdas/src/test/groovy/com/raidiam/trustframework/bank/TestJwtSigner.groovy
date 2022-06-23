package com.raidiam.trustframework.bank

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.io.Resources
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jwt.JWTClaimsSet
import com.raidiam.trustframework.bank.jwt.DefaultJwksFetcher
import com.raidiam.trustframework.bank.utils.JwtSigner
import com.raidiam.trustframework.mockbank.models.generated.CreatePaymentConsent
import io.micronaut.context.annotation.Replaces

import java.nio.charset.Charset

@Replaces(DefaultJwksFetcher.class)
class TestJwtSigner {

    public static final JwtSigner JWT_SIGNER = new JwtSigner(loadTestKeys()[0])
    public static JwtSigner BAD_JWT_SIGNER = new JwtSigner(loadTestKeys()[1])
    public static JWKSet JWKS
    public static JWKSet BAD_JWKS

    private static ObjectMapper objectMapper = new ObjectMapper()

    private static def loadTestKeys() {
        String jwks = Resources.toString(Resources.getResource("test.jwks"
        ), Charset.defaultCharset())
        String bad = Resources.toString(Resources.getResource("bad.jwks"
        ), Charset.defaultCharset())
        JWKS = JWKSet.parse(jwks)
        BAD_JWKS = JWKSet.parse(bad)
        [JWKS, BAD_JWKS]
    }

    static String sign(String payload, JWTClaimsSet otherClaims) {
        return JWT_SIGNER.sign(payload, otherClaims)
    }

    static String signBadly(String payload, JWTClaimsSet otherClaims) {
        return BAD_JWT_SIGNER.sign(payload, otherClaims)
    }

    static String signedPaymentConsent() {
        CreatePaymentConsent cps = TestRequestDataFactory.testPaymentConsent()
        String raw = objectMapper.writeValueAsString(cps)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("audience")
                .issuer("issuer")
                .issueTime(new Date())
                .jwtID("arandomjti")
                .build()
        sign(raw, otherClaims)
    }

}
