package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.services.InvoiceFinancingService;
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

@RolesAllowed({"INVOICE_FINANCINGS_READ"})
@Controller("/open-banking/invoice-financings")
public class InvoiceFinancingController extends BaseBankController {

    private static final Logger LOG = LoggerFactory.getLogger(InvoiceFinancingController.class);

    private final InvoiceFinancingService service;

    @Value("${mockbank.max-page-size}")
    int maxPageSize;

    InvoiceFinancingController(InvoiceFinancingService service) {
        this.service = service;
    }

    @Get("/v2/contracts")
    public ResponseInvoiceFinancingsContractList getInvoiceFinancingContractsList(Pageable pageable, HttpRequest<?> request) {
        LOG.info("Looking up invoice financing contract list");
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Getting invoice financing contracts for consent id {} v2", consentId);
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = service.getInvoiceFinancingContractList(adjustedPageable, consentId);
        BankLambdaUtils.decorateResponse(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages());
        LOG.info("External client making call for InvoiceFinancingsContractList - return response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/contracts/{contractId}")
    public ResponseInvoiceFinancingsContractV2 getInvoiceContractV2(@PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
        LOG.info("Looking up invoice financing contract {} v2", contractId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var response = service.getInvoiceFinancingContractV2(consentId, contractId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("External client making call for InvoiceFinancingsContract - return response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/contracts/{contractId}/payments")
    public ResponseInvoiceFinancingsPaymentsV2 getInvoicePaymentsV2(@PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
        LOG.info("Looking up payments for invoice financing contract {} v2", contractId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var response = service.getInvoiceFinancingPaymentsV2(consentId, contractId);
        int paymentReleases = response.getData().getReleases().size();
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), paymentReleases, maxPageSize);
        LOG.info("External client making call for InvoiceFinancingsPayments - return response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/contracts/{contractId}/scheduled-instalments")
    public ResponseInvoiceFinancingsInstalmentsV2 getInvoiceScheduledInstalmentsV2(@PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
        LOG.info("Looking up scheduled instalments for invoice financing contract {} v2", contractId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var response = service.getInvoiceFinancingScheduledInstalmentsV2(consentId, contractId);
        int instalmentBalloonPayments = response.getData().getBalloonPayments().size();
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), instalmentBalloonPayments, maxPageSize);
        LOG.info("External client making call for InvoiceFinancingsInstalments - return response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/contracts/{contractId}/warranties")
    public ResponseInvoiceFinancingsWarrantiesV2 getInvoiceWarrantiesV2(Pageable pageable, @PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
        LOG.info("Looking up scheduled instalments for invoice financing contract {} v2", contractId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = service.getInvoiceFinancingsWarrantiesV2(adjustedPageable, consentId, contractId);
        BankLambdaUtils.decorateResponse(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages());
        LOG.info("External client making call for InvoiceFinancingsContractedWarranty - return response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }
}

