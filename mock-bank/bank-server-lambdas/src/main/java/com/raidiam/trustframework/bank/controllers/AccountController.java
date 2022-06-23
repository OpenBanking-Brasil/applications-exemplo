package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.services.AccountsService;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
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
@Controller("/open-banking/accounts/v1/accounts")
public class AccountController extends BaseBankController {

    private static final Logger LOG = LoggerFactory.getLogger(AccountController.class);

    private final AccountsService accountsService;

    AccountController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @Get
    public ResponseAccountList getAccounts(Pageable pageable, HttpRequest<?> request) {
        LOG.info("Getting all accounts");
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var accountType = bankLambdaUtils.getAttributeFromRequest(request, "accountType").orElse(null);
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request);
        var response =  accountsService.getAccounts(adjustedPageable, consentId, accountType);
        BankLambdaUtils.decorateResponse(response::setLinks, response.getMeta().getTotalPages(), appBaseUrl + request.getPath(), adjustedPageable.getNumber());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/{accountId}")
    public ResponseAccountIdentification getAccount(@PathVariable("accountId") String acctId, HttpRequest<?> request) {
        LOG.info("Looking up account {}", acctId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var response = accountsService.getAccount(consentId, acctId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Returning account identification");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get(value = "/{accountId}/balances")
    public ResponseAccountBalances getAccountBalances(@PathVariable("accountId") String acctId, HttpRequest<?> request) {
        LOG.info("Looking up account balances for account {}", acctId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var response = accountsService.getAccountBalances(consentId, acctId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Returning account balances");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/{accountId}/transactions")
    public ResponseAccountTransactions getAccountTransactions(Pageable pageable, @PathVariable("accountId") String acctId, HttpRequest<?> request) {
        LOG.info("Looking up account transactions for account {}", acctId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var fromDate = bankLambdaUtils.getDateFromRequest(request, "fromBookingDate").orElse(LocalDate.now());
        var toDate = bankLambdaUtils.getDateFromRequest(request, "toBookingDate").orElse(LocalDate.now());
        var creditDebitIndicator = bankLambdaUtils.getAttributeFromRequest(request, "creditDebitIndicator").orElse(null);
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request);
        var response = accountsService.getAccountTransactions(adjustedPageable, consentId, fromDate, toDate, creditDebitIndicator, acctId);
        BankLambdaUtils.decorateResponse(response::setLinks, response.getMeta().getTotalPages(), appBaseUrl + request.getPath(), adjustedPageable.getNumber());
        LOG.info("Returning account transactions");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/{accountId}/overdraft-limits")
    public ResponseAccountOverdraftLimits getAccountOverdraftLimits(@PathVariable("accountId") String acctId, HttpRequest<?> request) {
        LOG.info("Looking up account overdraft limits for account {}", acctId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var response = accountsService.getAccountOverdraftLimits(consentId, acctId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Returning account overdraft limits");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }
}
