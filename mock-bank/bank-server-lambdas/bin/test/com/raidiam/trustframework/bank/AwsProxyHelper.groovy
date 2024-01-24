package com.raidiam.trustframework.bank

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jwt.JWTClaimsSet
import io.micronaut.http.HttpMethod

class AwsProxyHelper {
    def static mapper = new ObjectMapper().findAndRegisterModules()

    def static X_FAPI_INTERACTION_ID_KEY = "x-fapi-interaction-id"
    def static X_IDEMPOTENCY_KEY_KEY = "x-idempotency-key"

    static AwsProxyRequestBuilder buildCall(String path, HttpMethod httpMethod, Object entity) {
        buildProxy(path, httpMethod, "payments", entity, false, false)
    }

    static AwsProxyRequestBuilder buildPaymentsManagerCall(String path, HttpMethod httpMethod, Object entity = null, boolean addXFapiInteractionId = true) {
        buildProxy(path, httpMethod, "payments op:payments", entity, addXFapiInteractionId, false)
    }

    static AwsProxyRequestBuilder buildManagerJwtCall(String path, HttpMethod httpMethod, Object entity = null, boolean addXFapiInteractionId = true) {
        buildProxy(path, httpMethod, "payments op:payments", entity, addXFapiInteractionId, true)
    }

    static AwsProxyRequestBuilder buildJwtCall(String path, HttpMethod httpMethod, Object entity = null, boolean addXFapiInteractionId = true) {
        buildProxy(path, httpMethod, "payments", entity, addXFapiInteractionId, true)
    }
    static AwsProxyRequestBuilder buildProxy(String path, HttpMethod httpMethod, String scopes, Object entity = null, boolean addXFapiInteractionId = true, boolean isJwtCall = false) {
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder(path, httpMethod.toString())

        if(httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PATCH || httpMethod == HttpMethod.PUT)
            builder.body(entity)

        AuthHelper.authorize(scopes: scopes, org_id: "issuer", builder)
        builder.header(X_IDEMPOTENCY_KEY_KEY, UUID.randomUUID().toString())
        if(isJwtCall) {
            builder.header("Content-Type", "application/jwt")
            builder.header("Accept", "application/jwt")
        }

        if(addXFapiInteractionId)
            builder.header(X_FAPI_INTERACTION_ID_KEY, UUID.randomUUID().toString())

        builder
    }

    static String signPayload(Object entityToBeSigned) {
        String entity = mapper.writeValueAsString(entityToBeSigned)
        JWTClaimsSet otherClaims = new JWTClaimsSet.Builder()
                .audience("mockbank")
                .issuer("issuer")
                .issueTime(new Date())
                .jwtID(UUID.randomUUID().toString())
                .build()
        entity = TestJwtSigner.sign(entity, otherClaims)

        entity
    }

}
