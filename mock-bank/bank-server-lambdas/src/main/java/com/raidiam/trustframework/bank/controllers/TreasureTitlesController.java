package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.services.InvestmentService;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.ResponseTreasureTitlesBalances;
import com.raidiam.trustframework.mockbank.models.generated.ResponseTreasureTitlesProductIdentification;
import com.raidiam.trustframework.mockbank.models.generated.ResponseTreasureTitlesProductList;
import com.raidiam.trustframework.mockbank.models.generated.ResponseTreasureTitlesTransactions;
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

@RolesAllowed({"TREASURE_TITLES_READ"})
@Controller("/open-banking/treasure-titles/v1/investments")
public class TreasureTitlesController extends BaseBankController {

    private static final Logger LOG = LoggerFactory.getLogger(TreasureTitlesController.class);

    private final InvestmentService investmentService;

    @Value("${mockbank.max-page-size}")
    int maxPageSize;

    public TreasureTitlesController(InvestmentService service) {
        this.investmentService = service;
    }

    @Get
    public ResponseTreasureTitlesProductList getTreasureTitles(Pageable pageable, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up all Treasure Titles for consent id {}", consentId);
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = investmentService.getTreasureTitlesList(adjustedPageable, consentId);
        BankLambdaUtils.decorateResponse(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/{investmentId}")
    public ResponseTreasureTitlesProductIdentification getTreasureTitlesByInvestmentId(@PathVariable("investmentId") UUID investmentId, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up Treasure Titles for consent id {}", consentId);
        var response = investmentService.getTreasureTitlesById(consentId, investmentId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Returning treasure titles identification");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/{investmentId}/balances")
    public ResponseTreasureTitlesBalances getTreasureTitlesBalanceByInvestmentId(@PathVariable("investmentId") UUID investmentId, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up Treasure Titles balances for consent id {}", consentId);
        var response = investmentService.getTreasureTitlesBalance(consentId, investmentId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Returning treasure titles balances");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/{investmentId}/transactions")
    public ResponseTreasureTitlesTransactions getTreasureTitlesTransactionsByInvestmentId(@PathVariable("investmentId") UUID investmentId,
                                                                                          Pageable pageable, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up Treasure Titles transactions for consent id {}", consentId);
        var fromTransactionDate = bankLambdaUtils.getDateFromRequest(request, "fromTransactionDate").orElse(LocalDate.now());
        var toTransactionDate = bankLambdaUtils.getDateFromRequest(request, "toTransactionDate").orElse(LocalDate.now());
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = investmentService.getTreasureTitlesTransactions(consentId, investmentId, fromTransactionDate, toTransactionDate, adjustedPageable);
        BankLambdaUtils.decorateResponseTransactionsV2(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages(),
                "fromTransactionDate", fromTransactionDate.toString(), "toTransactionDate", toTransactionDate.toString());
        LOG.info("Returning treasure titles transactions");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/{investmentId}/transactions-current")
    public ResponseTreasureTitlesTransactions getTreasureTitlesTransactionsCurrentByInvestmentId(@PathVariable("investmentId") UUID investmentId, Pageable pageable, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up Treasure Titles current transactions for consent id {}", consentId);
        var fromTransactionDate = bankLambdaUtils.getDateFromRequest(request, "fromTransactionDate").orElse(LocalDate.now().minusDays(7));
        var toTransactionDate = bankLambdaUtils.getDateFromRequest(request, "toTransactionDate").orElse(LocalDate.now());
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = investmentService.getTreasureTitlesTransactions(consentId, investmentId, fromTransactionDate, toTransactionDate, adjustedPageable);
        BankLambdaUtils.decorateResponseTransactionsV2(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages(),
                "fromTransactionDate", fromTransactionDate.toString(), "toTransactionDate", toTransactionDate.toString());
        LOG.info("Returning treasure titles current transactions");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }
}
