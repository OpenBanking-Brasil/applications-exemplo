package com.raidiam.trustframework.bank.jwt;

import com.nimbusds.jose.jwk.JWKSet;

public interface JwksFetcher {

    JWKSet findForOrg(String orgId);

}
