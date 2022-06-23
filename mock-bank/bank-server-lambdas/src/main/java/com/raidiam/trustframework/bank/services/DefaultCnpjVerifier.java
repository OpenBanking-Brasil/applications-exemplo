package com.raidiam.trustframework.bank.services;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DefaultCnpjVerifier implements CnpjVerifier {

    @Inject
    private RolesApiClient client;

    @Override
    public boolean isKnownCnpj(String cnpj) {
        return client.roles().stream()
                .filter(r -> r.getRegistrationNumber().equals(cnpj))
                .findAny().isPresent();
    }
}
