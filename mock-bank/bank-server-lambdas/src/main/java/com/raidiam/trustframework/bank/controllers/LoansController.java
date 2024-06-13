package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.services.LoansService;
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

@RolesAllowed({"LOANS_READ"})
@Controller("/open-banking/loans")
public class LoansController extends BaseBankController {

    private static final Logger LOG = LoggerFactory.getLogger(LoansController.class);

    private final LoansService service;

    @Value("${mockbank.max-page-size}")
    int maxPageSize;

    LoansController(LoansService service) {
        this.service = service;
    }

    @Get("/v2/contracts")
    public ResponseLoansContractList getLoansContracts(Pageable pageable, HttpRequest<?> request) {
        LOG.info("Looking up all loan contracts");
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Getting loan contracts for consent id {} v2", consentId);
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = service.getLoansContractList(adjustedPageable, consentId);
        BankLambdaUtils.decorateResponse(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages());
        LOG.info("External client making call for LoansContractList - return response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/contracts/{contractId}")
    public ResponseLoansContractV2 getLoansContractV2(@PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
        LOG.info("Looking up loan contract {} v2", contractId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var response = service.getLoanContractV2(consentId, contractId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("External client making call for LoansContract - return response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/contracts/{contractId}/payments")
    public ResponseLoansPaymentsV2 getLoansPaymentsV2(@PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
        LOG.info("Looking up payments for loan contract {} v2", contractId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var response = service.getLoanPaymentsV2(consentId, contractId);
        int paymentReleases = response.getData().getReleases().size();
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), paymentReleases, maxPageSize);
        LOG.info("External client making call for LoansPayments - return response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/contracts/{contractId}/scheduled-instalments")
    public ResponseLoansInstalmentsV2 getLoansScheduledInstalmentsV2(@PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
        LOG.info("Looking up scheduled instalments for loan contract {} v2", contractId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var response = service.getLoanScheduledInstalmentsV2(consentId, contractId);
        int instalmentBalloonPayments = response.getData().getBalloonPayments().size();
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), instalmentBalloonPayments, maxPageSize);
        LOG.info("External client making call for LoansInstalments - return response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/contracts/{contractId}/warranties")
    public ResponseLoansWarrantiesV2 getLoansWarrantiesV2(Pageable pageable, @PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
        LOG.info("Looking up warranties for loan contract {} v2", contractId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = service.getLoansWarrantiesV2(adjustedPageable, consentId, contractId);
        BankLambdaUtils.decorateResponse(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages());
        LOG.info("External client making call for LoansWarranties - return response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }
}

