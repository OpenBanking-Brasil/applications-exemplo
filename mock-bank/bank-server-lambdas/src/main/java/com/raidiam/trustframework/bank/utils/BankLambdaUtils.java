package com.raidiam.trustframework.bank.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raidiam.trustframework.bank.domain.*;
import com.raidiam.trustframework.bank.exceptions.TrustframeworkException;
import com.raidiam.trustframework.bank.fapi.JwtRequestFilter;
import com.raidiam.trustframework.bank.repository.*;
import com.raidiam.trustframework.mockbank.models.generated.CreateConsentData;
import com.raidiam.trustframework.mockbank.models.generated.Links;
import com.raidiam.trustframework.mockbank.models.generated.Meta;
import com.raidiam.trustframework.mockbank.models.generated.ResponseConsentData;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpParameters;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.authentication.Authentication;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Singleton
public class BankLambdaUtils {

    private static final Logger LOG = LoggerFactory.getLogger(BankLambdaUtils.class);

    public static Date offsetDateToDate(OffsetDateTime offset) {
        return Optional.ofNullable(offset).map(OffsetDateTime::toInstant).map(Date::from).orElse(null);
    }

    public static OffsetDateTime dateToOffsetDate(Date date) {
        return Optional.ofNullable(date).map(Date::toInstant).map(a -> a.atOffset(ZoneOffset.UTC)).orElse(null);
    }

    public static Date localDateToSqlDate(LocalDate localDate) {
        return Optional.ofNullable(localDate).map(java.sql.Date::valueOf).orElse(null);
    }

    public static Date localDateToDate(LocalDate localDate) {
        return java.util.Date.from(localDate.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    public static LocalDate dateToLocalDate(Date date) {
        return Optional.ofNullable(date).map(Date::getTime).map(java.sql.Date::new).map(java.sql.Date::toLocalDate).orElse(null);
    }

    public static void logObject(ObjectMapper mapper, Object res) {
        try {
            String response = mapper.writeValueAsString(res);
            LOG.info("{} - {}", res.getClass().getSimpleName(), response);
        } catch (JsonProcessingException e) {
            LOG.error("{} - Error writing object as JSON: ", res.getClass().getSimpleName(), e);
        }
    }

    public static void checkAuthorisationStatus(ConsentEntity consentEntity) {
        var status = ResponseConsentData.StatusEnum.fromValue(consentEntity.getStatus());
        if (!ResponseConsentData.StatusEnum.AUTHORISED.equals(status)) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Bad request, consent not Authorised!");
        }
    }

    public static void checkConsentCoversAccount(ConsentEntity consentEntity, AccountEntity account) {
        var accountFromConsent = consentEntity.getConsentAccounts().stream().map(ConsentAccountEntity::getAccount).filter(account::equals).findFirst();
        if (accountFromConsent.isEmpty()) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Bad request, consent does not cover this account!");
        }
    }

    public static void checkConsentOwnerIsAccountOwner(ConsentEntity consentEntity, AccountEntity account) {
        if (!consentEntity.getAccountHolder().equals(account.getAccountHolder())) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Forbidden, consent owner does not match account owner!");
        }
    }

    public static void checkConsentOwnerIsAccountOwner(Page<ConsentAccountEntity> consentAccount, ConsentEntity consentEntity) {
        if(consentAccount.getContent()
                .stream()
                .map(ConsentAccountEntity::getAccount)
                .map(AccountEntity::getAccountHolderId)
                .anyMatch(accountHolderId -> !accountHolderId.equals(consentEntity.getAccountHolderId()))) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Forbidden, consent owner does not match account owner!");
        }
    }


    public static void checkConsentCoversCreditCardAccount(ConsentEntity consentEntity, CreditCardAccountsEntity account) {
        var accountFromConsent = consentEntity.getConsentCreditCardAccounts()
                .stream()
                .map(ConsentCreditCardAccountsEntity::getCreditCardAccount)
                .filter(account::equals)
                .findFirst();
        if (accountFromConsent.isEmpty()) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Bad request, consent does not cover this credit card account!");
        }
    }

    public static void checkBillOwnedCreditCardAccount(CreditCardAccountsBillsEntity bill, CreditCardAccountsEntity account) {
        var billFromAccount = account.getBills().stream().filter(bill::equals).findFirst();
        if (billFromAccount.isEmpty()) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Bad request, bill does not owned credit card account!");
        }
    }

    public static void checkConsentOwnerIsCreditCardAccountOwner(ConsentEntity consentEntity, CreditCardAccountsEntity account) {
        if (!consentEntity.getAccountHolder().equals(account.getAccountHolder())) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Forbidden, consent owner does not match credit card account owner!");
        }
    }

    public static void checkConsentOwnerIsCreditCardAccountOwner(Page<ConsentCreditCardAccountsEntity> consentCreditCardAccount, ConsentEntity consentEntity) {
        if(consentCreditCardAccount.getContent()
                .stream()
                .map(ConsentCreditCardAccountsEntity::getCreditCardAccount)
                .map(CreditCardAccountsEntity::getAccountHolderId)
                .anyMatch(accountHolderId -> !accountHolderId.equals(consentEntity.getAccountHolderId()))) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Forbidden, consent owner does not match credit card account owner!");
        }
    }

    public static void checkConsentCoversContract(ConsentEntity consentEntity, ContractEntity contract) {
        var contractFromConsent = consentEntity.getConsentContracts()
                .stream()
                .map(ConsentContractEntity::getContract)
                .filter(contract::equals)
                .findFirst();
        if (contractFromConsent.isEmpty()) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Bad request, consent does not cover this contract!");
        }
    }

    public static void checkConsentOwnerIsContractOwner(ConsentEntity consentEntity, ContractEntity contract) {
        if (!consentEntity.getAccountHolder().equals(contract.getAccountHolder())) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Forbidden, consent owner does not match contract owner!");
        }
    }

    public static void checkConsentOwnerIsContractOwner(Page<ConsentContractEntity> consentContractsPage, ConsentEntity consentEntity) {
        if(consentContractsPage.getContent()
                .stream()
                .map(ConsentContractEntity::getContract)
                .map(ContractEntity::getAccountHolderId)
                .anyMatch(accountHolderId -> !accountHolderId.equals(consentEntity.getAccountHolderId()))) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Forbidden, consent owner does not match contract owner!");
        }
    }


    public static class RequestMeta {

        RequestMeta(List<String> roles, String consentId, String clientId, String jti) {
            this.roles = roles;
            this.consentId = consentId;
            this.clientId = clientId;
            this.jti = jti;
        }

        @Getter
        private List<String> roles;
        @Getter
        private String consentId;
        @Getter
        private String clientId;
        @Getter
        private String jti;
    }

    public static RequestMeta getRequestMeta(HttpRequest<?> request) {
        LOG.info("getCallerInfo() from the request");
        String jti = null;
        try {
            Optional<Object> attribute = request.getAttribute("micronaut.AUTHENTICATION");
            Optional<Object> jtiOpt = request.getAttribute(JwtRequestFilter.JTI_ATTRIBUTE);
            jti = String.valueOf(jtiOpt.orElse(null));
            if (attribute.isPresent()) {
                LOG.info("There is an authentication present on the request");
                Authentication authentication = (Authentication) attribute.get();
                List<String> roles = (List<String>) authentication.getAttributes().get("roles");

                Optional<Object> clientIdOpt = request.getAttribute("clientId");
                if (clientIdOpt.isEmpty()) {
                    throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Access token did not contain a client ID");
                }
                String clientId = clientIdOpt.get().toString();
                String consentId = request.getAttribute("consentId").map(Object::toString).orElse(null);
                LOG.info("Roles: {}", String.join(",", roles));
                LOG.info("Request made by client id: {}", clientId);
                LOG.info("Request made with consent Id: {}", consentId);
                LOG.info("Request made with JTI: {}", jti);
                return new RequestMeta(roles, consentId, clientId, jti);
            }
        } catch (Exception e) {
            LOG.error("Exception  getting caller info. Error: ", e);
        }
        LOG.info("No authentication present");
        return new RequestMeta(Collections.emptyList(), null, null, jti);
    }

    public static Set<CreateConsentData.PermissionsEnum> getConsentPermissions(ConsentEntity consent) {
        return consent.getPermissions().stream()
                .map(ConsentPermissionEntity::getPermission)
                .map(CreateConsentData.PermissionsEnum::fromValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public static void checkConsentPermissions(ConsentEntity consent, CreateConsentData.PermissionsEnum permissionsEnum) {
        if (!getConsentPermissions(consent).contains(permissionsEnum)) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "You do not have the correct permission");
        }
    }

    public String getConsentIdFromRequest(HttpRequest<?> request) {
        return Optional.of(BankLambdaUtils.getRequestMeta(request)).map(BankLambdaUtils.RequestMeta::getConsentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.FORBIDDEN, "Request has no associated consent Id"));
    }

    private static LocalDate getLocalDateFromString (String dateValue) {
        try {
            return LocalDate.parse(dateValue);
        } catch (DateTimeParseException e) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Could not parse date from String " + dateValue);
        }
    }

    public Optional<LocalDate> getDateFromRequest(HttpRequest<?> request, String parameterName) {
        return Optional.of(request).map(HttpRequest::getParameters).map(params -> params.get(parameterName)).map(BankLambdaUtils::getLocalDateFromString);
    }

    public Optional<BigDecimal> getPayeeMCCFromRequest(HttpRequest<?> request) {
        return Optional.of(request).map(HttpRequest::getParameters).map(params -> params.get("payeeMCC")).map(BigDecimal::new);
    }

    public Optional<String> getAttributeFromRequest(HttpRequest<?> request, String attributeName) {
        return request.getAttribute(attributeName).map(Object::toString);
    }

    public static void decorateResponse(Consumer<Links> setLinks, Consumer<Meta> setMeta, String self, int records) {
        setLinks.accept(new Links().self(self));
        setMeta.accept(new Meta().totalPages(1).totalRecords(records).requestDateTime(OffsetDateTime.now()));
    }

    /**
     * Decorate a response with the correct links for the given pagination
     *
     * @param setLinks Method to use to set the links
     * @param total    The total number of pages
     * @param self     The 'self' link, unadorned with params
     * @param page     The current page - note this is 0-indexed, and the output format is 1-indexed
     */
    public static void decorateResponse(Consumer<Links> setLinks, int total, String self, int page) {
        var template = "%s?page-size=%d&page=%d";
        var links = new Links().self(self)
                .first(String.format(template, self, total, 1))
                .last(String.format(template, self, total, total));
        if (page > 0) {
            links.setPrev(String.format(template, self, total, page));
        }
        if (page < total - 1) {
            links.setNext(String.format(template, self, total, page + 2));
        }
        setLinks.accept(links);
    }

    public static ConsentEntity getConsent(String consentId, ConsentRepository consentRepository) {
        return consentRepository.findByConsentId(consentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.FORBIDDEN, "Consent Id " + consentId + " not found"));
    }

    public static AccountEntity getAccount(String accountId, AccountRepository accountRepository) {
        return accountRepository.findByAccountId(UUID.fromString(accountId))
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Could not find account"));
    }

    public static CreditCardAccountsEntity getCreditCardAccount(String creditCardAccountId, CreditCardAccountsRepository creditCardAccountsRepository) {
        return creditCardAccountsRepository.findByCreditCardAccountId(UUID.fromString(creditCardAccountId))
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Could not find credit card account"));
    }

    public static CreditCardAccountsBillsEntity getCreditCardAccountBill(String billId, CreditCardAccountsBillsRepository creditCardAccountsBillsRepository) {
        return creditCardAccountsBillsRepository.findByBillId(UUID.fromString(billId))
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Could not find credit card account bill"));
    }

    public static ContractEntity getContract(UUID contractId, ContractsRepository contractsRepository, String contractType) {
        return contractsRepository.findByContractIdAndContractType(contractId, contractType)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Could not find contract"));
    }

    public static Meta getMeta(Page<?> page, int records) {
        var meta = new Meta();
        meta.setTotalRecords(records);
        meta.setTotalPages(page.getTotalPages());
        meta.setRequestDateTime(OffsetDateTime.now());
        return meta;
    }

    /**
     * Convert an incoming pageable (1-based page numbers) to micronaut's
     * expected pageable (0-based)
     *
     * @param inboundPageable the pageable from the query
     *
     * @return A new pageable with 0-based page numbers
     */
    public static Pageable adjustPageable(Pageable inboundPageable, HttpRequest<?> request) {
        LOG.info("Incoming request page details - Page {}, Page Size {}", inboundPageable.getNumber(), inboundPageable.getSize());
        HttpParameters params = request.getParameters();
        if ((inboundPageable.getNumber() < 0)
           || (params.contains("page") && inboundPageable.getNumber() == 0)) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Pages are 1-indexed, value of " + inboundPageable.getNumber() + " not valid");
        }
        int newNumber = inboundPageable.getNumber();
        if(params.contains("page")) {
            newNumber--;
        }

        if(inboundPageable.getSize() <= 0) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Page size must be positive, value of " + inboundPageable.getNumber() + " not valid");
        }

        return Pageable.from(newNumber, inboundPageable.getSize());
    }
}
