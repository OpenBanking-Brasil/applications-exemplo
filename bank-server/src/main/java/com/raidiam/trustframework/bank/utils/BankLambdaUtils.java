package com.raidiam.trustframework.bank.utils;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.authentication.Authentication;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class BankLambdaUtils {

    private static final Logger LOG = LoggerFactory.getLogger(BankLambdaUtils.class);

    private BankLambdaUtils () {}

    public static Date offsetDateToDate (OffsetDateTime offset) {
        return Optional.ofNullable(offset).map(OffsetDateTime::toInstant).map(Date::from).orElse(null);
    }
    public static OffsetDateTime dateToOffsetDate (Date date) {
        return Optional.ofNullable(date).map(Date::toInstant).map(a -> a.atOffset(ZoneOffset.UTC)).orElse(null);
    }

    public static Date localDateToDate (LocalDate localDate) {
        return Optional.ofNullable(localDate).map(java.sql.Date::valueOf).orElse(null);
    }
    public static LocalDate dateToLocalDate (Date date) {
        return Optional.ofNullable(date).map(Date::getTime).map(java.sql.Date::new).map(java.sql.Date::toLocalDate).orElse(null);
    }

    public static class CallerInfo {

        CallerInfo(List<String> roles, String consentId, String clientId) {
            this.roles = roles;
            this.consentId = consentId;
            this.clientId = clientId;
        }

        @Getter
        private List<String> roles;
        @Getter
        private String consentId;
        @Getter
        private String clientId;
    }

    public static CallerInfo getCallerInfo(HttpRequest<?> request) {
        Optional<Object> attribute = request.getAttribute("micronaut.AUTHENTICATION");
        if(attribute.isPresent()) {
            LOG.info("There is an authentication present on the request");
            Authentication authentication = (Authentication) attribute.get();
            List<String> roles = (List<String>) authentication.getAttributes().get("roles");

            Optional<Object> clientIdOpt = request.getAttribute("clientId");
            if(!clientIdOpt.isPresent()) {
                throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Access token did not contain a client ID");
            }
            String clientId = clientIdOpt.get().toString();
            String consentId = request.getAttribute("consentId").map(Object::toString).orElse(null);
            LOG.info("Roles: {}", String.join(",", roles));
            LOG.info("Request made by client id: {}", clientId);
            LOG.info("Request made with consent Id: {}", clientId);
            return new CallerInfo(roles, consentId, clientId);
        }
        LOG.info("No authentication present");
        return new CallerInfo(Collections.emptyList(), null, null);
    }
}
