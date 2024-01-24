package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.services.InvestmentService;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.ResponseFundsBalances;
import com.raidiam.trustframework.mockbank.models.generated.ResponseFundsProductIdentification;
import com.raidiam.trustframework.mockbank.models.generated.ResponseFundsProductList;
import com.raidiam.trustframework.mockbank.models.generated.ResponseFundsTransactions;
import io.micronaut.context.annotation.Value;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@RolesAllowed({"FUNDS_READ"})
@Controller("/open-banking/funds/v1/investments")
public class FundsController extends BaseBankController {

    private static final Logger LOG = LoggerFactory.getLogger(FundsController.class);

    private final InvestmentService investmentService;

    @Value("${mockbank.max-page-size}")
    int maxPageSize;

    public FundsController(InvestmentService service) {
        this.investmentService = service;
    }

    @Get
    public ResponseFundsProductList getFunds(Pageable pageable, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up all Funds for consent id {}", consentId);
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = investmentService.getFundsList(adjustedPageable, consentId);
        BankLambdaUtils.decorateResponse(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/{investmentId}")
    public ResponseFundsProductIdentification getFundsByInvestmentId(@PathVariable("investmentId") UUID investmentId, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up Funds for consent id {}", consentId);
        var response = investmentService.getFundsById(consentId, investmentId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Returning funds identification");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/{investmentId}/balances")
    public ResponseFundsBalances getFundsBalanceByInvestmentId(@PathVariable("investmentId") UUID investmentId, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up Funds balances for consent id {}", consentId);
        var response = investmentService.getFundsBalance(consentId, investmentId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Returning funds balances");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/{investmentId}/transactions")
    public ResponseFundsTransactions getFundsTransactionsByInvestmentId(@PathVariable("investmentId") UUID investmentId,
                                                                        Pageable pageable, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up Funds transactions for consent id {}", consentId);
        var fromTransactionDate = bankLambdaUtils.getDateFromRequest(request, "fromTransactionDate").orElse(LocalDate.now());
        var toTransactionDate = bankLambdaUtils.getDateFromRequest(request, "toTransactionDate").orElse(LocalDate.now());
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = investmentService.getFundsTransactions(consentId, investmentId, fromTransactionDate, toTransactionDate, adjustedPageable);
        BankLambdaUtils.decorateResponseTransactionsV2(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages(),
                "fromTransactionDate", fromTransactionDate.toString(), "toTransactionDate", toTransactionDate.toString());
        LOG.info("Returning funds transactions");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/{investmentId}/transactions-current")
    public ResponseFundsTransactions getFundsTransactionsCurrentByInvestmentId(@PathVariable("investmentId") UUID investmentId, Pageable pageable, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up Funds current transactions for consent id {}", consentId);
        var fromTransactionDate = bankLambdaUtils.getDateFromRequest(request, "fromTransactionDate").orElse(LocalDate.now().minusDays(7));
        var toTransactionDate = bankLambdaUtils.getDateFromRequest(request, "toTransactionDate").orElse(LocalDate.now());
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = investmentService.getFundsTransactions(consentId, investmentId, fromTransactionDate, toTransactionDate, adjustedPageable);
        BankLambdaUtils.decorateResponseTransactionsV2(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages(),
                "fromTransactionDate", fromTransactionDate.toString(), "toTransactionDate", toTransactionDate.toString());
        LOG.info("Returning funds current transactions");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }
}
