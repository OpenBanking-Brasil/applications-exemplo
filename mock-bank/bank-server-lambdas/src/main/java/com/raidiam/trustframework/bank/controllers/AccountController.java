package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.fapi.XFapiInteractionIdRequired;
import com.raidiam.trustframework.bank.services.AccountsService;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.context.annotation.Value;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import java.time.LocalDate;

@RolesAllowed({"ACCOUNTS_READ"})
@Controller("/open-banking/accounts")
public class AccountController extends BaseBankController {

    private static final Logger LOG = LoggerFactory.getLogger(AccountController.class);
    private final AccountsService accountsService;

    AccountController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @Value("${mockbank.max-page-size}")
    int maxPageSize;

    @Get("/v2/accounts")
    @XFapiInteractionIdRequired
    public ResponseAccountList getAccounts(Pageable pageable, HttpRequest<?> request) {
        LOG.info("Getting all accounts v2");
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var accountType = bankLambdaUtils.getAttributeFromRequest(request, "accountType").orElse(null);
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = accountsService.getAccounts(adjustedPageable, consentId, accountType);
        BankLambdaUtils.decorateResponse(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/accounts/{accountId}")
    @XFapiInteractionIdRequired
    public ResponseAccountIdentification getAccount(@PathVariable("accountId") String acctId, HttpRequest<?> request) {
        LOG.info("Looking up account {} v2", acctId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var response = accountsService.getAccount(consentId, acctId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Returning account identification");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get(value = "/v2/accounts/{accountId}/balances")
    @XFapiInteractionIdRequired
    public ResponseAccountBalancesV2 getAccountBalancesV2(@PathVariable("accountId") String acctId, HttpRequest<?> request) {
        LOG.info("Looking up account balances for account {} v2", acctId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var response = accountsService.getAccountBalancesV2(consentId, acctId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Returning account balances");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/accounts/{accountId}/transactions")
    @XFapiInteractionIdRequired
    public ResponseAccountTransactionsV2 getAccountTransactionsV2(Pageable pageable, @PathVariable("accountId") String acctId, HttpRequest<?> request) {
        LOG.info("Looking up account transactions for account {} v2", acctId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var creditDebitIndicator = bankLambdaUtils.getAttributeFromRequest(request, "creditDebitIndicator").orElse(null);
        var adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var fromBookingDate = bankLambdaUtils.getDateFromRequest(request, "fromBookingDate").orElse(LocalDate.now());
        var toBookingDate = bankLambdaUtils.getDateFromRequest(request, "toBookingDate").orElse(LocalDate.now());
        var response = accountsService.getAccountTransactionsV2(adjustedPageable, consentId, fromBookingDate, toBookingDate, creditDebitIndicator, acctId);
        BankLambdaUtils.decorateResponseTransactionsV2(response::setLinks, response::setMeta, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages(),
                "fromBookingDate", fromBookingDate.toString(), "toBookingDate", toBookingDate.toString());
        LOG.info("Returning account transactions");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/accounts/{accountId}/transactions-current")
    @XFapiInteractionIdRequired
    public ResponseAccountTransactionsV2 getAccountTransactionsCurrent(Pageable pageable, @PathVariable("accountId") String acctId, HttpRequest<?> request) {
        LOG.info("Looking up account transactions current for account {} v2", acctId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var creditDebitIndicator = bankLambdaUtils.getAttributeFromRequest(request, "creditDebitIndicator").orElse(null);
        var adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var fromBookingDate = bankLambdaUtils.getDateFromRequest(request, "fromBookingDate").orElse(LocalDate.now());
        var toBookingDate = bankLambdaUtils.getDateFromRequest(request, "toBookingDate").orElse(LocalDate.now());
        bankLambdaUtils.checkDateRange(fromBookingDate, toBookingDate);
        var response = accountsService.getAccountTransactionsV2(adjustedPageable, consentId, fromBookingDate, toBookingDate, creditDebitIndicator, acctId);
        BankLambdaUtils.decorateResponseTransactionsV2(response::setLinks, response::setMeta, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages(),
                "fromBookingDate", fromBookingDate.toString(), "toBookingDate", toBookingDate.toString());
        LOG.info("Returning account transactions-current");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/accounts/{accountId}/overdraft-limits")
    @XFapiInteractionIdRequired
    public ResponseAccountOverdraftLimitsV2 getAccountOverdraftLimitsV2(@PathVariable("accountId") String acctId, HttpRequest<?> request) {
        LOG.info("Looking up account overdraft limits for account {} v2", acctId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var response = accountsService.getAccountOverdraftLimitsV2(consentId, acctId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Returning account overdraft limits");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }
}
