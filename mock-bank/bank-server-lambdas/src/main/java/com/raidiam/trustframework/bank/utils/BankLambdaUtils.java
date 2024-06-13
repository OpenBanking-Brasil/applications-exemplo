package com.raidiam.trustframework.bank.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.raidiam.trustframework.bank.domain.*;
import com.raidiam.trustframework.bank.enums.ContractTypeEnum;
import com.raidiam.trustframework.bank.exceptions.TrustframeworkException;
import com.raidiam.trustframework.bank.fapi.JwtRequestFilter;
import com.raidiam.trustframework.bank.repository.*;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpParameters;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.authentication.Authentication;
import lombok.Getter;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Singleton
public class BankLambdaUtils {

    private static final Logger LOG = LoggerFactory.getLogger(BankLambdaUtils.class);

    private static final String BRASIL_ZONE_ID = "America/Sao_Paulo";

    public static Date offsetDateToDate(OffsetDateTime offset) {
        return Optional.ofNullable(offset).map(OffsetDateTime::toInstant).map(Date::from).orElse(null);
    }

    public static OffsetDateTime dateToOffsetDateTimeInBrasil(Date date) {
        return Optional.ofNullable(date).map(newDate -> newDate.toInstant().atZone(getBrasilZoneId()).toOffsetDateTime()).orElse(null);
    }

    public static ZoneId getBrasilZoneId() {
        return ZoneId.of(BRASIL_ZONE_ID);
    }

    public static OffsetDateTime getOffsetDateTimeInBrasil() {
        return OffsetDateTime.now(getBrasilZoneId());
    }

    public static Instant getInstantInBrasil() {
        return Instant.now().atZone(getBrasilZoneId()).toInstant();
    }

    public static LocalDateTime timestampToLocalDateTime(Date timestamp) {
        return Optional.ofNullable(timestamp).map(Date::toInstant).map(a -> a.atZone(ZoneId.of(BRASIL_ZONE_ID)).toLocalDateTime()).orElse(null);
    }

    public static OffsetDateTime dateToOffsetDate(Date date) {
        return Optional.ofNullable(date).map(Date::toInstant).map(a -> a.atOffset(ZoneOffset.UTC)).orElse(null);
    }

    public static OffsetDateTime localDateToOffsetDate(LocalDate date) {
        return Optional.ofNullable(date).map(a -> OffsetDateTime.of(a, LocalTime.NOON, ZoneOffset.UTC)).orElse(null);
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

    public static String doubleToString(Double value) {
        return Optional.ofNullable(value).map(Objects::toString).orElse("0.0");
    }

    public static String formatDoubleToLongString(Double value) {
        DecimalFormat formatter = new DecimalFormat("#0.000000");
        return formatter.format(Optional.ofNullable(value).orElse(0.00));
    }

    public static void logObject(ObjectMapper mapper, Object res) {
        try {
            String response = mapper.writeValueAsString(res);
            LOG.info("{} - {}", res.getClass().getSimpleName(), response);
        } catch (JsonProcessingException e) {
            LOG.error("{} - Error writing object as JSON: ", res.getClass().getSimpleName(), e);
        }
    }

    public static String formatAmountV2(double amount) {
        return new DecimalFormat("0.00##", new DecimalFormatSymbols(Locale.US)).format(amount);
    }

    public static String formatRateV2(double amount) {
        return new DecimalFormat("0.000000", new DecimalFormatSymbols(Locale.US)).format(amount);
    }

    public static void checkAuthorisationStatus(ConsentEntity consentEntity) {
        var status = EnumConsentStatus.fromValue(consentEntity.getStatus());
        if (!EnumConsentStatus.AUTHORISED.equals(status)) {
            throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Bad request, consent not Authorised!");
        }
    }

    public static void checkConsentCoversAccount(ConsentEntity consentEntity, AccountEntity account) {
        var accountFromConsent = consentEntity.getAccounts().stream().filter(account::equals).findFirst();
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
        var accountFromConsent = consentEntity.getCreditCardAccounts()
                .stream()
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
        var contractFromConsent = consentEntity.getContracts()
                .stream()
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
        private final List<String> roles;
        @Getter
        private final String consentId;
        @Getter
        private final String clientId;
        @Getter
        private final String jti;
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

    public static String getIdempotencyKey(HttpRequest<?> request) {
        return Optional.ofNullable(request.getHeaders().get("x-idempotency-key"))
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "No Idempotency header"));
    }
    public static Set<EnumConsentPermissions> getConsentPermissions(ConsentEntity consent) {
        return consent.getConsentPermissions().stream()
                .map(ConsentPermissionEntity::getPermission)
                .map(EnumConsentPermissions::fromValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public static Integer getPaymentScheduleWeeklyOrdinal(String dayOfWeek) {
        ScheduleWeeklyWeekly.DayOfWeekEnum dayOfWeekValue;
        try {
            dayOfWeekValue = ScheduleWeeklyWeekly.DayOfWeekEnum.valueOf(dayOfWeek);
        } catch (IllegalArgumentException | NullPointerException e) {
            return 0;
        }
        switch (dayOfWeekValue) {
            case SEGUNDA_FEIRA:
                return 1;
            case TERCA_FEIRA:
                return 2;
            case QUARTA_FEIRA:
                return 3;
            case QUINTA_FEIRA:
                return 4;
            case SEXTA_FEIRA:
                return 5;
            case SABADO:
                return 6;
            case DOMINGO:
                return 7;
        }
        return 0;
    }

    public static void checkConsentPermissions(ConsentEntity consent, EnumConsentPermissions permissionsEnum) {
        if (!getConsentPermissions(consent).contains(permissionsEnum)) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "You do not have the correct permission");
        }
    }

    public void checkDateRange(LocalDate fromDate, LocalDate toDate) {
        var maxDateRange = 7;
        if (fromDate.isAfter(toDate)) throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Wrong date period");
        long daysBetween = ChronoUnit.DAYS.between(fromDate, toDate);
        if (daysBetween > maxDateRange) throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Date range must be no more than " + maxDateRange + " days");
    }

    public String getConsentIdFromRequest(HttpRequest<?> request) {
        return Optional.of(BankLambdaUtils.getRequestMeta(request)).map(BankLambdaUtils.RequestMeta::getConsentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.FORBIDDEN, "Request has no associated consent Id"));
    }

    public String getConsentIdFromRequest(RequestMeta requestMeta) {
        return Optional.of(requestMeta).map(BankLambdaUtils.RequestMeta::getConsentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.FORBIDDEN, "Request has no associated consent Id"));
    }

    public static String getRecurringConsentIdFromRequest(HttpRequest<?> request) {
        return Optional.of(request).map(HttpRequest::getParameters).map(params -> params.get("recurringConsentId")).map(String::new)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.UNAUTHORIZED, "Request has no associated recurring consent Id"));
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

    public Optional<String> getCertificateCNFromRequest(HttpRequest<?> request) {
        var certificateString = Optional.ofNullable(request.getParameters().get("BANK-TLS-Certificate"));
        if(certificateString.isPresent()) {
            try {
                byte[] certificateBytes = certificateString.get().getBytes();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(certificateBytes);

                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(byteArrayInputStream);

                String commonName = "";
                if (certificate != null) {
                    X500Name x500name = X500Name.getInstance(certificate.getSubjectX500Principal().getEncoded());
                    RDN[] cnArray = x500name.getRDNs(BCStyle.CN);
                    if (cnArray.length == 0) {
                        return Optional.empty();
                    }
                    commonName = IETFUtils.valueToString(cnArray[0].getFirst().getValue());
                }
                if (Strings.isNullOrEmpty(commonName)) {
                    return Optional.empty();
                }
                LOG.info("CN extracted: {}", commonName);
                return Optional.of(commonName);
            } catch (CertificateException err) {
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not parse the certificate");
            }
        }
        return Optional.empty();
    }

    public Optional<String> getAttributeFromRequest(HttpRequest<?> request, String attributeName) {
        return request.getAttribute(attributeName).map(Object::toString);
    }

    public static void decorateResponse(Consumer<Links> setLinks, Consumer<Meta> setMeta, String self, int records) {
        setLinks.accept(new Links().self(self));
        setMeta.accept(new Meta().totalPages(1).totalRecords(records).requestDateTime(OffsetDateTime.now()));
    }

    public static void decorateResponseBrasilTimeZone(Consumer<Links> setLinks, Consumer<Meta> setMeta, String self, int records) {
        setLinks.accept(new Links().self(self));
        setMeta.accept(new Meta().totalPages(1).totalRecords(records).requestDateTime(OffsetDateTime.now(BankLambdaUtils.getBrasilZoneId())));
    }

    public static void decorateResponse(Consumer<Links> setLinks, Consumer<Meta> setMeta, String self, int records, int pageSize) {
        setLinks.accept(new Links().self(self));
        var page = pageSize <= 0 ? 0 : (int) Math.ceil((double) records / pageSize);
        setMeta.accept(new Meta().totalPages(page).totalRecords(records).requestDateTime(OffsetDateTime.now()));
    }

    public static String buildTransactionSelfLink(HttpRequest<?> request, String appBaseUrl) {
        var selfLink = appBaseUrl + request.getPath();

        selfLink += Optional.of(request).map(HttpRequest::getParameters).map(params -> {
            List<String> q = new ArrayList<>();
            params.forEach(p -> q.add(p.getKey() + "=" + p.getValue().get(0)));

            return q.isEmpty() ? "" : "?" + String.join("&", q);
        }).orElse("");

        return selfLink;
    }
    public static void decorateResponseSimpleMeta(Consumer<Links> setLinks, Consumer<MetaOnlyRequestDateTime> setMeta, String self) {
        setLinks.accept(new Links().self(self));
        setMeta.accept(new MetaOnlyRequestDateTime().requestDateTime(OffsetDateTime.now()));
    }

    public static void decorateResponseSimpleMetaBrasilTimeZone(Consumer<Links> setLinks, Consumer<MetaOnlyRequestDateTime> setMeta, String self) {
        setLinks.accept(new Links().self(self));
        setMeta.accept(new MetaOnlyRequestDateTime().requestDateTime(OffsetDateTime.now(BankLambdaUtils.getBrasilZoneId())));
    }

    public static boolean isPaymentFullManageCaller(HttpRequest<?> request) {
        var callerInfo = getRequestMeta(request);
        return callerInfo.getRoles().contains("PAYMENTS_FULL_MANAGE");
    }

    /**
     * Decorate a response with the correct links for the given pagination
     *
     * @param setLinks      Method to use to set the links
     * @param pageSize      The size of the page
     * @param self          The self link
     * @param pageNumber    The current page - note this is 0-indexed, and the output format is 1-indexed
     */

    private static final String PAGE_TEMPLATE = "%s?page-size=%d&page=%d";
    public static void decorateResponse(Consumer<Links> setLinks, int pageSize, String self, int pageNumber, int totalNumberOfPages) {
        decorate(setLinks, pageSize, self, pageNumber, totalNumberOfPages, PAGE_TEMPLATE);
    }

    public static void decorateResponseSimpleMeta(Consumer<Links> setLinks, Consumer<Meta> setMeta, int pageSize, String self, int pageNumber, int totalNumberOfPages) {
        decorate(setLinks, pageSize, self, pageNumber, totalNumberOfPages, PAGE_TEMPLATE);
        setMeta.accept(new Meta().requestDateTime(OffsetDateTime.now()).totalRecords(null).totalRecords(null));
    }

    public static void decorateResponseSimpleLinkMeta(Consumer<Links> setLinks, Consumer<Meta> setMeta, String self) {
        setLinks.accept(new Links().self(self));
        setMeta.accept(new Meta().requestDateTime(OffsetDateTime.now()).totalRecords(null).totalRecords(null));
    }

    public static void decorateResponseSimpleLinkMetaBrasilTimeZone(Consumer<Links> setLinks, Consumer<Meta> setMeta, String self) {
        setLinks.accept(new Links().self(self));
        setMeta.accept(new Meta().requestDateTime(OffsetDateTime.now(BankLambdaUtils.getBrasilZoneId())).totalRecords(null).totalRecords(null));
    }

    public static void decorateResponse(Consumer<Links> setLinks, int pageSize, String self, int pageNumber, int totalNumberOfPages,
                                        String... dateNames) {
        if(dateNames.length != 4){
            throw new TrustframeworkException("dateNames parameter should contain the following - fromDateName, fromDate, toDateName, toDate strings");
        }

        var datesParameters = String.format("&%s=%s&%s=%s", dateNames[0], dateNames[1], dateNames[2], dateNames[3]);

        decorate(setLinks, pageSize, self, pageNumber, totalNumberOfPages, PAGE_TEMPLATE.concat(datesParameters));
    }

    public static void decorateResponseTransactionsV2(Consumer<TransactionsLinksV2> setLinks, int pageSize, String self, int pageNumber, int totalNumberOfPages,
                                        String... dateNames) {
        if(dateNames.length != 4){
            throw new TrustframeworkException("dateNames parameter should contain the following - fromDateName, fromDate, toDateName, toDate strings");
        }

        var datesParameters = String.format("&%s=%s&%s=%s", dateNames[0], dateNames[1], dateNames[2], dateNames[3]);

        decorateTransactionsV2(setLinks, pageSize, self, pageNumber, totalNumberOfPages, PAGE_TEMPLATE.concat(datesParameters));
    }

    public static void decorateResponseTransactionsV2(Consumer<TransactionsLinksV2> setLinks, Consumer<Meta> setMeta, int pageSize, String self, int pageNumber, int totalNumberOfPages,
                                                      String... dateNames) {

        var datesParameters = String.format("&%s=%s&%s=%s", dateNames[0], dateNames[1], dateNames[2], dateNames[3]);
        decorateTransactionsV2(setLinks, pageSize, self, pageNumber, totalNumberOfPages, PAGE_TEMPLATE.concat(datesParameters));

        setMeta.accept(new Meta().requestDateTime(OffsetDateTime.now()).totalRecords(null).totalRecords(null));
    }

    private static void decorate(Consumer<Links> setLinks, int pageSize, String self, int pageNumber, int totalNumberOfPages, String template) {
        var links = new Links()
                .self(String.format(template, self, pageSize, pageNumber + 1))
                .first(String.format(template, self, pageSize, 1))
                .last(String.format(template, self, pageSize, totalNumberOfPages == 0 ? 1 : totalNumberOfPages));
        if (pageNumber > 0) {
            links.setPrev(String.format(template, self, pageSize, pageNumber));
        }
        if (pageNumber < totalNumberOfPages - 1) {
            links.setNext(String.format(template, self, pageSize, pageNumber + 2));
        }
        setLinks.accept(links);
    }

    private static void decorateTransactionsV2(Consumer<TransactionsLinksV2> setLinks, int pageSize, String self, int pageNumber, int totalNumberOfPages, String template) {
        var links = new TransactionsLinksV2()
                .self(String.format(template, self, pageSize, pageNumber + 1))
                .first(String.format(template, self, pageSize, 1));
        if (pageNumber > 0) {
            links.setPrev(String.format(template, self, pageSize, pageNumber));
        }
        if (pageNumber < totalNumberOfPages - 1) {
            links.setNext(String.format(template, self, pageSize, pageNumber + 2));
        }
        setLinks.accept(links);
    }
    public static ConsentEntity getConsent(String consentId, ConsentRepository consentRepository) {
        return consentRepository.findByConsentId(consentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Consent Id " + consentId + " not found"));
    }

    public static PaymentConsentEntity getPaymentConsent(String paymentConsentId, PaymentConsentRepository consentRepository) {
        return consentRepository.findByPaymentConsentId(paymentConsentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Payment Consent Id " + paymentConsentId + " not found"));
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

    public static CreditCardAccountsTransactionEntity getCreditCardAccountsTransaction(String transactionId, CreditCardAccountsTransactionRepository creditCardAccountsTransactionRepository) {
        return creditCardAccountsTransactionRepository.findById(UUID.fromString(transactionId))
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Could not find credit card account transaction"));
    }

    public static ContractEntity getContract(UUID contractId, ContractsRepository contractsRepository, String contractType) {
        return contractsRepository.findByContractIdAndContractType(contractId, contractType)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Could not find contract"));
    }

    public static AccountHolderEntity getAccountHolder(String accountHolderId, AccountHolderRepository accountHolderRepository) {
        return accountHolderRepository.findByAccountHolderId(UUID.fromString(accountHolderId))
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Could not find account holder"));
    }

    public static void checkExistAccountHolder(String accountHolderId, AccountHolderRepository accountHolderRepository) {
        if (accountHolderId == null) throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Account Holder Id is null");
        Optional<AccountHolderEntity> entity = accountHolderRepository.findByAccountHolderId(UUID.fromString(accountHolderId));
        if (entity.isEmpty()) throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Account Holder not exist");
    }

    public static AccountHolderEntity getAccountHolderByUser(LoggedUser loggedUser, AccountHolderRepository accountHolderRepository){
        var userDocument = loggedUser.getDocument();
        return accountHolderRepository.findByDocumentIdentificationAndDocumentRel(userDocument.getIdentification(), userDocument.getRel()).stream().findAny()
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, String.format("User with documentation Id %s and rel %s not found",
                        userDocument.getIdentification(), userDocument.getRel())));
    }

    public static PersonalIdentificationsEntity getPersonalIdentifications(String personalId, PersonalIdentificationsRepository personalIdentificationsRepository) {
        return personalIdentificationsRepository.findByPersonalIdentificationsId(UUID.fromString(personalId))
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Could not find personal identifications"));
    }

    public static PersonalFinancialRelationsEntity getPersonalFinancialRelations(String accountHolderId, PersonalFinancialRelationsRepository personalFinancialRelationsRepository) {
        return personalFinancialRelationsRepository.findByAccountHolderAccountHolderId(UUID.fromString(accountHolderId))
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Could not find personal financial relations"));
    }

    public static void checkExistPersonalFinancialRelations(String accountHolderId, PersonalFinancialRelationsRepository personalFinancialRelationsRepository) {
        Optional<PersonalFinancialRelationsEntity> entity = personalFinancialRelationsRepository.findByAccountHolderAccountHolderId(UUID.fromString(accountHolderId));
        if (entity.isPresent()) throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Personal financial relations is exist");
    }

    public static PersonalQualificationsEntity getPersonalQualifications(String accountHolderId, PersonalQualificationsRepository personalQualificationsRepository) {
        return personalQualificationsRepository.findByAccountHolderAccountHolderId(UUID.fromString(accountHolderId))
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Could not find personal qualifications"));
    }

    public static void checkExistPersonalQualifications(String accountHolderId, PersonalQualificationsRepository personalQualificationsRepository) {
        Optional<PersonalQualificationsEntity> entity = personalQualificationsRepository.findByAccountHolderAccountHolderId(UUID.fromString(accountHolderId));
        if (entity.isPresent()) throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Personal qualifications is exist");
    }

    public static BusinessIdentificationsEntity getBusinessIdentifications(String businesslId, BusinessIdentificationsRepository businessIdentificationsRepository) {
        return businessIdentificationsRepository.findByBusinessIdentificationsId(UUID.fromString(businesslId))
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Could not find business identifications"));
    }

    public static BusinessFinancialRelationsEntity getBusinessFinancialRelations(String accountHolderId, BusinessFinancialRelationsRepository businessFinancialRelationsRepository) {
        return businessFinancialRelationsRepository.findByAccountHolderAccountHolderId(UUID.fromString(accountHolderId))
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Could not find business financial relations"));
    }

    public static void checkExistBusinessFinancialRelations(String accountHolderId, BusinessFinancialRelationsRepository businessFinancialRelationsRepository) {
        Optional<BusinessFinancialRelationsEntity> entity = businessFinancialRelationsRepository.findByAccountHolderAccountHolderId(UUID.fromString(accountHolderId));
        if (entity.isPresent()) throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Business financial relations is exist");
    }

    public static BusinessQualificationsEntity getBusinessQualifications(String accountHolderId, BusinessQualificationsRepository businessQualificationsRepository) {
        return businessQualificationsRepository.findByAccountHolderAccountHolderId(UUID.fromString(accountHolderId))
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Could not find business qualifications"));
    }

    public static void checkExistBusinessQualifications(String accountHolderId, BusinessQualificationsRepository businessQualificationsRepository) {
        Optional<BusinessQualificationsEntity> entity = businessQualificationsRepository.findByAccountHolderAccountHolderId(UUID.fromString(accountHolderId));
        if (entity.isPresent()) throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Business qualifications is exist");
    }

    public static String getContractType(String type) {
        return Optional.of(Objects.requireNonNull(ContractTypeEnum.fromValue(type)).name())
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "Wrong contract type"));
    }

    public static boolean isExpirationDateInPast(Date expirationDate) {
        return expirationDate.before(Date.from(BankLambdaUtils.getInstantInBrasil()));
    }

    public static boolean isTotalAllowedAmountReached(PaymentConsentEntity paymentConsentEntity, List<PixPaymentEntity> recurringPayments, Double amount) {
        return sumTotalAmounts(recurringPayments, amount) == Double.parseDouble(paymentConsentEntity.getPostSweepingRecurringConfiguration().getAmount());
    }

    public static boolean isTotalAllowedAmountExceeded(PaymentConsentEntity paymentConsentEntity, List<PixPaymentEntity> recurringPayments, Double amount) {
        return sumTotalAmounts(recurringPayments, amount) > Double.parseDouble(paymentConsentEntity.getPostSweepingRecurringConfiguration().getAmount());
    }

    private static double sumTotalAmounts(List<PixPaymentEntity> recurringPayments, Double amount) {
        double sum = 0;
        sum += amount;

        for (PixPaymentEntity recurringPayment : recurringPayments) {
            sum += Double.parseDouble(recurringPayment.getDTO().getData().getPayment().getAmount());
        }
        return sum;
    }

    public static Meta getMeta(Page<?> page) {
        var meta = new Meta();
        if (page != null) {
            meta.setTotalRecords((int) page.getTotalSize());
            meta.setTotalPages(page.getTotalPages());
        } else {
            meta.setTotalRecords(0);
            meta.setTotalPages(0);
        }
        meta.setRequestDateTime(OffsetDateTime.now());
        return meta;
    }

    public static String formatTransactionDateTime(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
    }

    /**
     * Convert an incoming pageable (1-based page numbers) to micronaut's
     * expected pageable (0-based)
     *
     * @param inboundPageable the pageable from the query
     * @param maxPageSize the maximum page size
     * @return A new pageable with 0-based page numbers
     */
    public static Pageable adjustPageable(Pageable inboundPageable, HttpRequest<?> request, int maxPageSize) {
        LOG.info("Incoming request page details - Page {}, Page Size {}", inboundPageable.getNumber(), inboundPageable.getSize());
        HttpParameters params = request.getParameters();
        if ((inboundPageable.getNumber() < 0)
                || (params.contains("page") && inboundPageable.getNumber() == 0)) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, String.format("Pages are 1-indexed, value of %d not valid", inboundPageable.getNumber()));
        }
        int newNumber = inboundPageable.getNumber();
        if(params.contains("page")) {
            newNumber--;
        }

        if(inboundPageable.getSize() <= 0) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, String.format("Page size must be positive, value of %d not valid", inboundPageable.getSize()));
        }


        if(inboundPageable.getSize() > 1000) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, String.format("Page size must not be over 1000, value of %d not valid", inboundPageable.getSize()));
        }
        int size = Math.min(inboundPageable.getSize(), maxPageSize);
        return Pageable.from(newNumber, size);
    }

    public static void checkAndSaveJti(JtiRepository jtiRepository, String jti) {
        if (jtiRepository.getByJti(jti).isPresent()) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "DETALHE_PGTO_INVALIDO: Detalhe do pagamento inválido. JTI Reutilizada.");
        }

        JtiEntity jtiEntity = new JtiEntity();
        jtiEntity.setJti(jti);
        jtiRepository.save(jtiEntity);
    }

    public static boolean checkConsentNotificationTrigger(String status){
        String[] statusList = {"REJECTED", "CONSUMED"};

        return Arrays.stream(statusList).collect(Collectors.toList()).contains(status);
    }

    public static boolean checkPaymentNotificationTrigger(String status){
        String[] statusList = {"ACSC", "RJCT", "CANC", "PDNG", "SCHD"};

        return Arrays.stream(statusList).collect(Collectors.toList()).contains(status);
    }

    /**
     * Special sub-transaction.
     *
     * This can complete an action, like a database update, and then throw without unwinding that action.
     *
     * @param action    The action to run
     * @param exception The exception to throw after the action completes
     *
     * @throws HttpStatusException at the end of the process
     */
    @Transactional(value=Transactional.TxType.REQUIRES_NEW, dontRollbackOn={HttpStatusException.class})
    public void throwWithoutRollback(Runnable action, HttpStatusException exception) throws HttpStatusException {
        action.run();
        throw exception;
    }
}
