package com.raidiam.trustframework.bank.jwt;

import com.nimbusds.jose.jwk.JWKSet;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

@Singleton
public class DefaultJwksFetcher implements JwksFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultJwksFetcher.class);

    @Override
    public JWKSet findForOrg(String orgId) {
        String orgjwks = String.format("https://keystore.sandbox.directory.openbankingbrasil.org.br/%s/application.jwks", orgId);
        LOG.info("Looking up org level JWKS for {}", orgId);
        LOG.info("Org jwks set: {}", orgjwks);
        try {
            URL url = new URL(orgjwks);
            return JWKSet.load(url);
        } catch (MalformedURLException e) {
            LOG.error("Malformed URL", e);
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Not a url");
        } catch (ParseException e) {
            LOG.error("Unable to parse JWSK for {}", orgId, e);
            throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Unable to parse JWKS");
        } catch (IOException e) {
            LOG.error("IO exception looking up JWKS for {}", orgId, e);
            throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "IO exception loading JWKS");
        }
    }
}
