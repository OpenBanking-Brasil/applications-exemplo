package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.services.InvestmentService;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.ResponseCreditFixedIncomesBalances;
import com.raidiam.trustframework.mockbank.models.generated.ResponseCreditFixedIncomesProductIdentification;
import com.raidiam.trustframework.mockbank.models.generated.ResponseCreditFixedIncomesProductList;
import com.raidiam.trustframework.mockbank.models.generated.ResponseCreditFixedIncomesTransactions;
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

@RolesAllowed({"CREDIT_FIXED_INCOMES_READ"})
@Controller("/open-banking/credit-fixed-incomes/v1/investments")
public class CreditFixedIncomesController extends BaseBankController {

    private static final Logger LOG = LoggerFactory.getLogger(CreditFixedIncomesController.class);

    private final InvestmentService investmentService;

    @Value("${mockbank.max-page-size}")
    int maxPageSize;

    public CreditFixedIncomesController(InvestmentService service) {
        this.investmentService = service;
    }

    @Get
    public ResponseCreditFixedIncomesProductList getCreditFixedIncomes(Pageable pageable, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up all Credit Fixed Incomes for consent id {}", consentId);
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = investmentService.getCreditFixedIncomesList(adjustedPageable, consentId);
        BankLambdaUtils.decorateResponse(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/{investmentId}")
    public ResponseCreditFixedIncomesProductIdentification getCreditFixedIncomesByInvestmentId(@PathVariable("investmentId") UUID investmentId, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up Credit Fixed Incomes for consent id {}", consentId);
        var response = investmentService.getCreditFixedIncomesById(consentId, investmentId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Returning credit fixed incomes identification");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/{investmentId}/balances")
    public ResponseCreditFixedIncomesBalances getCreditFixedIncomesBalanceByInvestmentId(@PathVariable("investmentId") UUID investmentId, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up Credit Fixed Incomes balances for consent id {}", consentId);
        var response = investmentService.getCreditFixedIncomesBalance(consentId, investmentId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Returning credit fixed incomes balances");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/{investmentId}/transactions")
    public ResponseCreditFixedIncomesTransactions getCreditFixedIncomesTransactionsByInvestmentId(@PathVariable("investmentId") UUID investmentId,
                                                                                              Pageable pageable, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up Credit Fixed Incomes transactions for consent id {}", consentId);
        var fromTransactionDate = bankLambdaUtils.getDateFromRequest(request, "fromTransactionDate").orElse(LocalDate.now());
        var toTransactionDate = bankLambdaUtils.getDateFromRequest(request, "toTransactionDate").orElse(LocalDate.now());
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = investmentService.getCreditFixedIncomesTransactions(consentId, investmentId, fromTransactionDate, toTransactionDate, adjustedPageable);
        BankLambdaUtils.decorateResponseTransactionsV2(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages(),
                "fromTransactionDate", fromTransactionDate.toString(), "toTransactionDate", toTransactionDate.toString());
        LOG.info("Returning credit fixed incomes transactions");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/{investmentId}/transactions-current")
    public ResponseCreditFixedIncomesTransactions getCreditFixedIncomesTransactionsCurrentByInvestmentId(@PathVariable("investmentId") UUID investmentId, Pageable pageable, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up Credit Fixed Incomes current transaction for consent id {}", consentId);
        var fromTransactionDate = bankLambdaUtils.getDateFromRequest(request, "fromTransactionDate").orElse(LocalDate.now().minusDays(7));
        var toTransactionDate = bankLambdaUtils.getDateFromRequest(request, "toTransactionDate").orElse(LocalDate.now());
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = investmentService.getCreditFixedIncomesTransactions(consentId, investmentId, fromTransactionDate, toTransactionDate, adjustedPageable);
        BankLambdaUtils.decorateResponseTransactionsV2(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages(),
                "fromTransactionDate", fromTransactionDate.toString(), "toTransactionDate", toTransactionDate.toString());
        LOG.info("Returning credit fixed incomes current transactions");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }
}
