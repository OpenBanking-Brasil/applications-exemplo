package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.services.UnarrangedAccountsOverdraftService;
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

@RolesAllowed({"UNARRANGED_ACCOUNTS_OVERDRAFT_READ"})
@Controller("/open-banking/unarranged-accounts-overdraft")
public class UnarrangedAccountsOverdraftController extends BaseBankController {

    private static final Logger LOG = LoggerFactory.getLogger(UnarrangedAccountsOverdraftController.class);

    private final UnarrangedAccountsOverdraftService service;

    @Value("${mockbank.max-page-size}")
    int maxPageSize;

    UnarrangedAccountsOverdraftController(UnarrangedAccountsOverdraftService service) {
        this.service = service;
    }

    @Get("/v2/contracts")
    public ResponseUnarrangedAccountOverdraftContractList getUnarrangedAccountOverdraftContractsList(Pageable pageable, HttpRequest<?> request) {
        LOG.info("Getting Unnarranged account overdraft list");
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Getting Unnarranged account overdraft contracts for consent id {} v2", consentId);
        var adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = service.getUnarrangedOverdraftContractList(adjustedPageable, consentId);
        BankLambdaUtils.decorateResponse(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages());
        LOG.info("External client making call for UnarrangedAccountOverdraftContractList - return response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/contracts/{contractId}")
    public ResponseUnarrangedAccountOverdraftContractV2 getUnarrangedAccountsOverdraftContractV2(@PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
        LOG.info("Looking up unarranged account overdraft contract {} v2", contractId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var response = service.getUnarrangedOverdraftContractV2(consentId, contractId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("External client making call for UnarrangedAccountOverdraftContractData - return response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/contracts/{contractId}/payments")
    public ResponseUnarrangedAccountOverdraftPaymentsV2 getUnarrangedAccountsOverdraftPaymentsV2(@PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
        LOG.info("Looking up payments for unarranged account overdraft contract {} v2", contractId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var response = service.getUnarrangedOverdraftPaymentsV2(consentId, contractId);
        int paymentReleases = response.getData().getReleases().size();
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), paymentReleases, maxPageSize);
        LOG.info("External client making call for UnarrangedAccountOverdraftPaymentsData - return response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/contracts/{contractId}/scheduled-instalments")
    public ResponseUnarrangedAccountOverdraftInstalmentsV2 getUnarrangedAccountsOverdraftScheduledInstalmentsV2(@PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
        LOG.info("Looking up scheduled instalments for unarranged account overdraft contract {} v2", contractId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var response = service.getUnarrangedOverdraftScheduledInstalmentsV2(consentId, contractId);
        int instalmentBalloonPayments = response.getData().getBalloonPayments().size();
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), instalmentBalloonPayments, maxPageSize);
        LOG.info("External client making call for UnarrangedAccountOverdraftInstalmentsData - return response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/contracts/{contractId}/warranties")
    public ResponseUnarrangedAccountOverdraftWarrantiesV2 getUnarrangedAccountsOverdraftWarrantiesV2(Pageable pageable, @PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
        LOG.info("Looking up scheduled instalments for unarranged account overdraft contract {} v2", contractId);
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        var adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = service.getUnarrangedAccountsOverdraftWarrantiesV2(adjustedPageable, consentId, contractId);
        BankLambdaUtils.decorateResponse(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages());
        LOG.info("External client making call for UnarrangedAccountsOverdraftContractedWarranty - return response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }
}

