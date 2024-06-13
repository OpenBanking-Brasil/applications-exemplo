package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.services.InvestmentService;
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
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@RolesAllowed({"VARIABLE_INCOMES_READ"})
@Controller("/open-banking/variable-incomes/v1")
public class VariableIncomesController extends BaseBankController {

    private static final Logger LOG = LoggerFactory.getLogger(VariableIncomesController.class);

    private final InvestmentService investmentService;

    @Value("${mockbank.max-page-size}")
    int maxPageSize;

    public VariableIncomesController(InvestmentService service) {
        this.investmentService = service;
    }

    @Get("/investments")
    public ResponseVariableIncomesProductList getVariableIncomes(Pageable pageable, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up all Variable Incomes for consent id {}", consentId);
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = investmentService.getVariableIncomesList(adjustedPageable, consentId);
        BankLambdaUtils.decorateResponse(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/investments/{investmentId}")
    public ResponseVariableIncomesProductIdentification getVariableIncomesByInvestmentId(@PathVariable("investmentId") UUID investmentId, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up Variable Incomes for consent id {}", consentId);
        var response = investmentService.getVariableIncomesById(consentId, investmentId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Returning variable incomes identification");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/investments/{investmentId}/balances")
    public ResponseVariableIncomesBalance getVariableIncomesBalanceByInvestmentId(@PathVariable("investmentId") UUID investmentId, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up Variable Incomes balances for consent id {}", consentId);
        var response = investmentService.getVariableIncomesBalance(consentId, investmentId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Returning variable incomes balances");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/investments/{investmentId}/transactions")
    public ResponseVariableIncomesTransactions getVariableIncomesTransactionsByInvestmentId(@PathVariable("investmentId") UUID investmentId,
                                                                                            Pageable pageable, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up Variable Incomes transactions for consent id {}", consentId);
        var fromTransactionDate = bankLambdaUtils.getDateFromRequest(request, "fromTransactionDate").orElse(LocalDate.now());
        var toTransactionDate = bankLambdaUtils.getDateFromRequest(request, "toTransactionDate").orElse(LocalDate.now());
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = investmentService.getVariableIncomesTransactions(consentId, investmentId, fromTransactionDate, toTransactionDate, adjustedPageable);
        BankLambdaUtils.decorateResponseTransactionsV2(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages(),
                "fromTransactionDate", fromTransactionDate.toString(), "toTransactionDate", toTransactionDate.toString());
        LOG.info("Returning variable incomes transactions");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/investments/{investmentId}/transactions-current")
    public ResponseVariableIncomesTransactions getVariableIncomesTransactionsCurrentByInvestmentId(@PathVariable("investmentId") UUID investmentId, Pageable pageable, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up Variable Incomes current transactions for consent id {}", consentId);
        var fromTransactionDate = bankLambdaUtils.getDateFromRequest(request, "fromTransactionDate").orElse(LocalDate.now().minusDays(7));
        var toTransactionDate = bankLambdaUtils.getDateFromRequest(request, "toTransactionDate").orElse(LocalDate.now());
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = investmentService.getVariableIncomesTransactions(consentId, investmentId, fromTransactionDate, toTransactionDate, adjustedPageable);
        BankLambdaUtils.decorateResponseTransactionsV2(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages(),
                "fromTransactionDate", fromTransactionDate.toString(), "toTransactionDate", toTransactionDate.toString());
        LOG.info("Returning variable incomes current transactions");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/broker-notes/{brokerNoteId}")
    public ResponseVariableIncomesBroker getVariableIncomesBrokerNotesByBrokerNotesId(@PathVariable("brokerNoteId") UUID brokerNoteId, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up Variable Incomes broker notes for consent id {}", consentId);
        var response = investmentService.getVariableIncomesBroker(consentId, brokerNoteId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Returning variable incomes broker notes");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }
}
