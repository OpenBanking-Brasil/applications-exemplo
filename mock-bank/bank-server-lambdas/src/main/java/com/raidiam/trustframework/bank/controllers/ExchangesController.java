package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.auth.AuthenticationGrant;
import com.raidiam.trustframework.bank.auth.RequiredAuthenticationGrant;
import com.raidiam.trustframework.bank.fapi.XFapiInteractionIdRequired;
import com.raidiam.trustframework.bank.services.ExchangesService;
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

@RolesAllowed({"EXCHANGES_READ"})
@Controller("/open-banking/exchanges")
public class ExchangesController extends BaseBankController {

    private static final Logger LOG = LoggerFactory.getLogger(ExchangesController.class);

    private final ExchangesService exchangesService;

    @Value("${mockbank.max-page-size}")
    int maxPageSize;

    public ExchangesController(ExchangesService service) {
        this.exchangesService = service;
    }

    @Get("/v1/operations")
    @XFapiInteractionIdRequired
    @RequiredAuthenticationGrant(AuthenticationGrant.AUTHORISATION_CODE)
    public ResponseExchangesProductList getExchangesOperations(Pageable pageable, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up all exchanges operation for consent id {}", consentId);
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = exchangesService.getOperations(adjustedPageable, consentId);
        BankLambdaUtils.decorateResponse(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v1/operations/{operationId}")
    @XFapiInteractionIdRequired
    @RequiredAuthenticationGrant(AuthenticationGrant.AUTHORISATION_CODE)
    public ResponseExchangesOperationDetails getExchangesOperationsByOperationId(@PathVariable String operationId, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up all exchanges operation for consent id {} and operation id {}", consentId, operationId);
        var response = exchangesService.getOperationsByOperationId(operationId, consentId);
        BankLambdaUtils.decorateResponseSimpleLinkMeta(response::setLinks, response::setMeta, appBaseUrl + request.getPath());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v1/operations/{operationId}/events")
    @XFapiInteractionIdRequired
    @RequiredAuthenticationGrant(AuthenticationGrant.AUTHORISATION_CODE)
    public ResponseExchangesEvents getExchangesEventsByOperationId(@PathVariable String operationId, Pageable pageable, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up all exchanges operation for consent id {} and operation id {}", consentId, operationId);
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = exchangesService.getEventsByOperationId(operationId, consentId, adjustedPageable);
        BankLambdaUtils.decorateResponse(response::setLinks, adjustedPageable.getSize(), appBaseUrl + request.getPath(), adjustedPageable.getNumber(), response.getMeta().getTotalPages());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }
}
