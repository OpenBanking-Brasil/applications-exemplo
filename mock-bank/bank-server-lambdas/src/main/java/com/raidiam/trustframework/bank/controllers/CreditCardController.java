package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.services.CreditCardAccountsService;
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

@RolesAllowed({"CREDIT_CARDS_ACCOUNTS_READ"})
@Controller("/open-banking/credit-cards-accounts/v1/accounts")
public class CreditCardController extends BaseBankController {

    private static final Logger LOG = LoggerFactory.getLogger(CreditCardController.class);

    private final CreditCardAccountsService creditCardAccountsService;

    CreditCardController(CreditCardAccountsService creditCardAccountsService) {
        this.creditCardAccountsService = creditCardAccountsService;
    }

    @Get
    public ResponseCreditCardAccountsList getCreditCardAccounts(Pageable pageable, HttpRequest<?> request) {
        LOG.info("Getting all credit card accounts");
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var response =  creditCardAccountsService.getCreditCardAccounts(pageable, consentId);
        BankLambdaUtils.decorateResponse(response::setLinks, response.getMeta().getTotalPages(), appBaseUrl + request.getPath(), pageable.getNumber());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/{creditCardAccountId}")
    public ResponseCreditCardAccountsIdentification getCreditCardAccount(@PathVariable("creditCardAccountId") String acctId, HttpRequest<?> request) {
        LOG.info("Looking up credit card account {}", acctId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var response = creditCardAccountsService.getCreditCardAccount(consentId, acctId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Returning credit card account identification");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/{creditCardAccountId}/transactions")
    public ResponseCreditCardAccountsTransactions getAccountTransactions(Pageable pageable, @PathVariable("creditCardAccountId") String acctId, HttpRequest<?> request) {
        LOG.info("Looking up credit card account transactions for account {}", acctId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var fromDate = bankLambdaUtils.getDateFromRequest(request, "fromTransactionDate").orElse(LocalDate.now());
        var toDate = bankLambdaUtils.getDateFromRequest(request, "toTransactionDate").orElse(LocalDate.now());
        var payeeMCC = bankLambdaUtils.getPayeeMCCFromRequest(request).orElse(null);
        var transactionType = bankLambdaUtils.getAttributeFromRequest(request, "transactionType").orElse(null);
        var response = creditCardAccountsService
                .getCreditCardAccountTransactions(pageable, consentId, fromDate, toDate, payeeMCC, transactionType, acctId);
        BankLambdaUtils.decorateResponse(response::setLinks, response.getMeta().getTotalPages(), appBaseUrl + request.getPath(), pageable.getNumber());
        LOG.info("Returning credit card account transactions");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get(value = "/{creditCardAccountId}/limits")
    public ResponseCreditCardAccountsLimits getCreditCardAccountLimits(@PathVariable("creditCardAccountId") String acctId, HttpRequest<?> request) {
        LOG.info("Looking up credit card account limits for account {}", acctId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var response = creditCardAccountsService.getCreditCardAccountLimits(consentId, acctId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Returning credit card account limits");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get(value = "/{creditCardAccountId}/bills")
    public ResponseCreditCardAccountsBills getCreditCardAccountsBills(Pageable pageable, @PathVariable("creditCardAccountId") String acctId, HttpRequest<?> request) {
        LOG.info("Looking up credit card account bills for account {}", acctId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var fromDate = bankLambdaUtils.getDateFromRequest(request, "fromDueDate").orElse(LocalDate.now());
        var toDate = bankLambdaUtils.getDateFromRequest(request, "toDueDate").orElse(LocalDate.now());
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request);
        var response = creditCardAccountsService
                .getCreditCardAccountsBills(adjustedPageable, consentId, fromDate, toDate, acctId);
        BankLambdaUtils.decorateResponse(response::setLinks, response.getMeta().getTotalPages(), appBaseUrl + request.getPath(), adjustedPageable.getNumber());
        LOG.info("Returning credit card account bills");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get(value = "/{creditCardAccountId}/{billId}/transactions")
    public ResponseCreditCardAccountsTransactions getCreditCardAccountBillsTransactions(Pageable pageable, @PathVariable("creditCardAccountId") String acctId,
                                                                                        @PathVariable("billId") String billId,
                                                                                        HttpRequest<?> request) {
        LOG.info("Looking up credit card account bills transaction for account {}", acctId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var fromDate = bankLambdaUtils.getDateFromRequest(request, "fromTransactionDate").orElse(LocalDate.now());
        var toDate = bankLambdaUtils.getDateFromRequest(request, "toTransactionDate").orElse(LocalDate.now());
        var payeeMCC = bankLambdaUtils.getPayeeMCCFromRequest(request).orElse(null);
        var transactionType = bankLambdaUtils.getAttributeFromRequest(request, "transactionType").orElse(null);
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request);
        var response = creditCardAccountsService
                .getCreditCardAccountBillsTransactions(adjustedPageable, consentId, fromDate, toDate, payeeMCC, transactionType, acctId, billId);
        BankLambdaUtils.decorateResponse(response::setLinks, response.getMeta().getTotalPages(), appBaseUrl + request.getPath(), adjustedPageable.getNumber());
        LOG.info("Returning credit card account bills transaction");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }
}
