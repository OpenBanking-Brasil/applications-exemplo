package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.services.FinancingService;
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
import java.util.UUID;

@RolesAllowed({"FINANCINGS_READ"})
@Controller("/open-banking/financings")
public class FinancingController extends BaseBankController {

    private static final Logger LOG = LoggerFactory.getLogger(FinancingController.class);

    private final FinancingService service;

    @Value("${mockbank.max-page-size}")
    int maxPageSize;

    FinancingController(FinancingService service) {
        this.service = service;
    }

    @Get("/v2/contracts")
    public ResponseFinancingsContractList getFinancingContractsList(Pageable pageable, HttpRequest<?> request) {
        LOG.info("Getting financing contracts");
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Getting financing contracts for consent id {} v2", consentId);
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = service.getFinancingContractList(adjustedPageable, consentId);
        BankLambdaUtils.decorateResponse(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages());
        LOG.info("External client making call for FinancingsContractsList - return response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }
    @Get("/v2/contracts/{contractId}")
    public ResponseFinancingsContractV2 getFinancingContractV2(@PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
        LOG.info("Looking up financing contract {} v2", contractId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var response = service.getFinancingContractV2(consentId, contractId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("External client making call for FinancingsContract - return response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/contracts/{contractId}/payments")
    public ResponseFinancingsPaymentsV2 getFinancingPaymentsV2(@PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
        LOG.info("Looking up payments for financing contract {} v2", contractId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var response = service.getFinancingPaymentsV2(consentId, contractId);
        int paymentReleases = response.getData().getReleases().size();
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), paymentReleases, maxPageSize);
        LOG.info("External client making call for FinancingsPayments - return response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/contracts/{contractId}/scheduled-instalments")
    public ResponseFinancingsInstalmentsV2 getFinancingScheduledInstalmentsV2(@PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
        LOG.info("Looking up scheduled instalments for financing contract {} v2", contractId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var response = service.getFinancingScheduledInstalmentsV2(consentId, contractId);
        int instalmentBalloonPayments = response.getData().getBalloonPayments().size();
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), instalmentBalloonPayments, maxPageSize);
        LOG.info("External client making call for Financingsinstalments - return response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/contracts/{contractId}/warranties")
    public ResponseFinancingsWarrantiesV2 getFinancingWarrantiesV2(Pageable pageable, @PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
        LOG.info("Looking up warranties for financing contract {} v2", contractId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = service.getFinancingsWarrantiesV2(adjustedPageable, consentId, contractId);
        BankLambdaUtils.decorateResponse(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages());
        LOG.info("External client making call for FinancingsWarranties - return response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }
}

