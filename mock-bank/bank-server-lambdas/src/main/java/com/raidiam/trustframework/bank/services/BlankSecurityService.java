package com.raidiam.trustframework.bank.services;

import io.micronaut.context.annotation.Requires;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.utils.SecurityService;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
@Requires(missingBeans = io.micronaut.security.utils.SecurityService.class)
public class BlankSecurityService implements SecurityService {
    @Override
    public Optional<String> username() {
        return Optional.of("Micronaut Security Disabled User");
    }

    @Override
    public Optional<Authentication> getAuthentication() {
        return Optional.empty();
    }

    @Override
    public boolean isAuthenticated() {
        return false;
    }

    @Override
    public boolean hasRole(String role) {
        return false;
    }
}
