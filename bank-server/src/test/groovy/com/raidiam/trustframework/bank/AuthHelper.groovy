package com.raidiam.trustframework.bank

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper

/**
 * This class injects an OAuth token into requests, to allow us to
 * test the behaviour of our security annotations
 */
class AuthHelper {

    static ObjectMapper MAPPER = new ObjectMapper()

     static void authorize(params, AwsProxyRequestBuilder builder) {
         def scopes = params['scopes']
         def accessToken = [
                 subject: "user@bank.com",
                 scope: scopes,
                 active: true,
                 token_type: "Bearer",
                 exp: 1571660599,
                 client_id: "client1",
                 org_id: params['org_id'],
                 software_id: params['software_id']
         ]
         builder.authorizerContextValue("access_token", MAPPER.writeValueAsString(accessToken))
    }
}
