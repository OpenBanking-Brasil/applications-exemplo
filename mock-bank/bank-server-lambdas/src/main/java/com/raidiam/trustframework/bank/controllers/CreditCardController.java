package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.fapi.XFapiInteractionIdRequired;
import com.raidiam.trustframework.bank.services.CreditCardAccountsService;
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

@RolesAllowed({"CREDIT_CARDS_ACCOUNTS_READ"})
@Controller("/open-banking/credit-cards-accounts")
public class CreditCardController extends BaseBankController {

    private static final Logger LOG = LoggerFactory.getLogger(CreditCardController.class);
    private final CreditCardAccountsService creditCardAccountsService;

    @Value("${mockbank.max-page-size}")
    int maxPageSize;

    CreditCardController(CreditCardAccountsService creditCardAccountsService) {
        this.creditCardAccountsService = creditCardAccountsService;
    }

    @Get("/v2/accounts")
    @XFapiInteractionIdRequired
    public ResponseCreditCardAccountsList getCreditCardAccounts(Pageable pageable, HttpRequest<?> request) {
        LOG.info("Getting all credit card accounts v2");
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = creditCardAccountsService.getCreditCardAccounts(adjustedPageable, consentId);
        BankLambdaUtils.decorateResponse(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/accounts/{creditCardAccountId}")
    @XFapiInteractionIdRequired
    public ResponseCreditCardAccountsIdentification getCreditCardAccount(@PathVariable("creditCardAccountId") String acctId, HttpRequest<?> request) {
        LOG.info("Looking up credit card account {} v2", acctId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var response = creditCardAccountsService.getCreditCardAccount(consentId, acctId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Returning credit card account identification");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/accounts/{creditCardAccountId}/transactions")
    @XFapiInteractionIdRequired
    public ResponseCreditCardAccountsTransactionsV2 getAccountTransactionsV2(Pageable pageable, @PathVariable("creditCardAccountId") String acctId, HttpRequest<?> request) {
        LOG.info("Looking up credit card account transactions for account {} v2", acctId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var payeeMCC = bankLambdaUtils.getPayeeMCCFromRequest(request).orElse(null);
        var transactionType = bankLambdaUtils.getAttributeFromRequest(request, "transactionType").orElse(null);
        var fromBookingDate = bankLambdaUtils.getDateFromRequest(request, "fromTransactionDate").orElse(LocalDate.now());
        var toBookingDate = bankLambdaUtils.getDateFromRequest(request, "toTransactionDate").orElse(LocalDate.now());
        var response = creditCardAccountsService.getTransactionsV2(adjustedPageable, consentId, fromBookingDate, toBookingDate, payeeMCC, transactionType, acctId);
        BankLambdaUtils.decorateResponseTransactionsV2(response::setLinks, response::setMeta, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages(),
                "fromTransactionDate", fromBookingDate.toString(), "toTransactionDate", toBookingDate.toString(), "transactionType", transactionType);
        BankLambdaUtils.logObject(mapper, response);
        LOG.info("Returning credit card account transactions v2");
        return response;
    }

    @Get("/v2/accounts/{creditCardAccountId}/transactions-current")
    @XFapiInteractionIdRequired
    public ResponseCreditCardAccountsTransactionsV2 getAccountTransactionsCurrent(Pageable pageable, @PathVariable("creditCardAccountId") String acctId, HttpRequest<?> request) {
        LOG.info("Looking up credit card account transactions current for account {} v2", acctId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var payeeMCC = bankLambdaUtils.getPayeeMCCFromRequest(request).orElse(null);
        var transactionType = bankLambdaUtils.getAttributeFromRequest(request, "transactionType").orElse(null);
        var fromBookingDate = bankLambdaUtils.getDateFromRequest(request, "fromTransactionDate").orElse(LocalDate.now());
        var toBookingDate = bankLambdaUtils.getDateFromRequest(request, "toTransactionDate").orElse(LocalDate.now());
        bankLambdaUtils.checkDateRange(fromBookingDate, toBookingDate);
        var response = creditCardAccountsService.getTransactionsV2(adjustedPageable, consentId, fromBookingDate, toBookingDate, payeeMCC, transactionType, acctId);
        BankLambdaUtils.decorateResponseTransactionsV2(response::setLinks, response::setMeta, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages(),
                "fromTransactionDate", fromBookingDate.toString(), "toTransactionDate", toBookingDate.toString(), "transactionType", transactionType);
        BankLambdaUtils.logObject(mapper, response);
        LOG.info("Returning credit card account transactions-current");
        return response;
    }

    @Get(value = "/v2/accounts/{creditCardAccountId}/limits")
    @XFapiInteractionIdRequired
    public ResponseCreditCardAccountsLimitsV2 getCreditCardAccountLimitsV2(@PathVariable("creditCardAccountId") String acctId, HttpRequest<?> request) {
        LOG.info("Looking up credit card account limits for account {} v2", acctId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var response = creditCardAccountsService.getCreditCardAccountLimitsV2(consentId, acctId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Returning credit card account limits");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get(value = "/v2/accounts/{creditCardAccountId}/bills")
    @XFapiInteractionIdRequired
    public ResponseCreditCardAccountsBillsV2 getCreditCardAccountsBillsV2(Pageable pageable, @PathVariable("creditCardAccountId") String acctId, HttpRequest<?> request) {
        LOG.info("Looking up credit card account bills for account {} v2", acctId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var fromDate = bankLambdaUtils.getDateFromRequest(request, "fromDueDate").orElse(LocalDate.now());
        var toDate = bankLambdaUtils.getDateFromRequest(request, "toDueDate").orElse(LocalDate.now());
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = creditCardAccountsService
                .getCreditCardAccountsBillsV2(adjustedPageable, consentId, fromDate, toDate, acctId);
        BankLambdaUtils.decorateResponse(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages());
        LOG.info("Returning credit card account bills");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get(value = "/v2/accounts/{creditCardAccountId}/bills/{billId}/transactions")
    @XFapiInteractionIdRequired
    public ResponseCreditCardAccountsBillsTransactionsV2 getCreditCardAccountBillsTransactionsV2(Pageable pageable, @PathVariable("creditCardAccountId") String acctId,
                                                                                            @PathVariable("billId") String billId,
                                                                                            HttpRequest<?> request) {
        LOG.info("Looking up credit card account bills transaction for account {} v2", acctId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        String fromTransactionDateName = "fromTransactionDate";
        String toTransactionDateName = "toTransactionDate";
        var fromDate = bankLambdaUtils.getDateFromRequest(request, fromTransactionDateName).orElse(LocalDate.now());
        var toDate = bankLambdaUtils.getDateFromRequest(request, toTransactionDateName).orElse(LocalDate.now());
        var payeeMCC = bankLambdaUtils.getPayeeMCCFromRequest(request).orElse(null);
        var transactionType = bankLambdaUtils.getAttributeFromRequest(request, "transactionType").orElse(null);

        ResponseCreditCardAccountsBillsTransactionsV2 response = creditCardAccountsService.getBillsTransactionsV2(
                adjustedPageable, consentId, fromDate, toDate, payeeMCC, transactionType, acctId, billId);

        BankLambdaUtils.decorateResponse(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(),
                adjustedPageable.getNumber(), response.getMeta().getTotalPages(),
                fromTransactionDateName, fromDate.toString(), toTransactionDateName, toDate.toString());

        BankLambdaUtils.logObject(mapper, response);
        LOG.info("Returning credit card account transactions by bill");
        return response;
    }
}
