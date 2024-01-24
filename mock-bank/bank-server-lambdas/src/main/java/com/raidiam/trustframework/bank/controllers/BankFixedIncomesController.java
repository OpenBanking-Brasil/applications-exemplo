package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.services.InvestmentService;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.ResponseBankFixedIncomesBalances;
import com.raidiam.trustframework.mockbank.models.generated.ResponseBankFixedIncomesProductIdentification;
import com.raidiam.trustframework.mockbank.models.generated.ResponseBankFixedIncomesProductList;
import com.raidiam.trustframework.mockbank.models.generated.ResponseBankFixedIncomesTransactions;
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

@RolesAllowed({"BANK_FIXED_INCOMES_READ"})
@Controller("/open-banking/bank-fixed-incomes/v1/investments")
public class BankFixedIncomesController extends BaseBankController {

    private static final Logger LOG = LoggerFactory.getLogger(BankFixedIncomesController.class);

    private final InvestmentService investmentService;

    @Value("${mockbank.max-page-size}")
    int maxPageSize;

    public BankFixedIncomesController(InvestmentService service) {
        this.investmentService = service;
    }

    @Get
    public ResponseBankFixedIncomesProductList getBankFixedIncomes(Pageable pageable, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up all Bank Fixed Incomes for consent id {}", consentId);
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = investmentService.getBankFixedIncomesList(adjustedPageable, consentId);
        BankLambdaUtils.decorateResponse(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/{investmentId}")
    public ResponseBankFixedIncomesProductIdentification getBankFixedIncomesByInvestmentId(@PathVariable("investmentId") UUID investmentId, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up Bank Fixed Incomes for consent id {}", consentId);
        var response = investmentService.getBankFixedIncomesById(consentId, investmentId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Returning bank fixed incomes identification");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/{investmentId}/balances")
    public ResponseBankFixedIncomesBalances getBankFixedIncomesBalanceByInvestmentId(@PathVariable("investmentId") UUID investmentId, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up Bank Fixed Incomes balances for consent id {}", consentId);
        var response = investmentService.getBankFixedIncomesBalance(consentId, investmentId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Returning bank fixed incomes balances");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/{investmentId}/transactions")
    public ResponseBankFixedIncomesTransactions getBankFixedIncomesTransactionsByInvestmentId(@PathVariable("investmentId") UUID investmentId,
                                                                                              Pageable pageable, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up Bank Fixed Incomes transactions for consent id {}", consentId);
        var fromTransactionDate = bankLambdaUtils.getDateFromRequest(request, "fromTransactionDate").orElse(LocalDate.now());
        var toTransactionDate = bankLambdaUtils.getDateFromRequest(request, "toTransactionDate").orElse(LocalDate.now());
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = investmentService.getBankFixedIncomesTransactions(consentId, investmentId, fromTransactionDate, toTransactionDate, adjustedPageable);
        BankLambdaUtils.decorateResponseTransactionsV2(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages(),
                "fromTransactionDate", fromTransactionDate.toString(), "toTransactionDate", toTransactionDate.toString());
        LOG.info("Returning bank fixed incomes transactions");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/{investmentId}/transactions-current")
    public ResponseBankFixedIncomesTransactions getBankFixedIncomesTransactionsCurrentByInvestmentId(@PathVariable("investmentId") UUID investmentId, Pageable pageable, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up Bank Fixed Incomes transactions-current for consent id {}", consentId);
        var fromTransactionDate = bankLambdaUtils.getDateFromRequest(request, "fromTransactionDate").orElse(LocalDate.now().minusDays(7));
        var toTransactionDate = bankLambdaUtils.getDateFromRequest(request, "toTransactionDate").orElse(LocalDate.now());
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = investmentService.getBankFixedIncomesTransactions(consentId, investmentId, fromTransactionDate, toTransactionDate, adjustedPageable);
        BankLambdaUtils.decorateResponseTransactionsV2(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages(),
                "fromTransactionDate", fromTransactionDate.toString(), "toTransactionDate", toTransactionDate.toString());
        LOG.info("Returning bank fixed incomes balances");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }
}
