package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.services.CustomerService;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.NotNull;

@Controller("/open-banking/customers")
@RolesAllowed("CUSTOMERS_READ")
public class CustomerController extends BaseBankController {

    private static final Logger LOG = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Get("/v2/personal/identifications")
    public ResponsePersonalCustomersIdentificationV2 getPersonalIdentificationsV2(@NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Getting personal identifications for consent id {} v2", consentId);
        var response = customerService.getPersonalIdentificationsV2(consentId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), response.getData().size());
        LOG.info("Retrieved personal identifications for consent id {}", consentId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/personal/financial-relations")
    public ResponsePersonalCustomersFinancialRelationV2 getPersonalFinancialRelationsV2(@NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Getting personal financial relations for consent id {} v2", consentId);
        var response = customerService.getPersonalFinancialRelationsV2(consentId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Retrieved personal financial relations for consent id {}", consentId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/personal/qualifications")
    public ResponsePersonalCustomersQualificationV2 getPersonalQualificationsV2(@NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Getting personal qualifications for consent id {} v2", consentId);
        var response = customerService.getPersonalQualificationsV2(consentId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Retrieved personal qualifications for consent id {}", consentId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/business/identifications")
    public ResponseBusinessCustomersIdentificationV2 getBusinessIdentificationsV2(@NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Getting business identifications for consent id {} v2", consentId);
        var response = customerService.getBusinessIdentificationsV2(consentId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), response.getData().size());
        LOG.info("Retrieved business identifications for consent id {}", consentId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/business/financial-relations")
    public ResponseBusinessCustomersFinancialRelationV2 getBusinessFinancialRelationsV2(@NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Getting business financial relations for consent id {} v1", consentId);
        var response = customerService.getBusinessFinancialRelationsV2(consentId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Retrieved business financial relations for consent id {}", consentId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v2/business/qualifications")
    public ResponseBusinessCustomersQualificationV2 getBusinessQualificationsV2(@NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Getting business qualifications for consent id {} v2", consentId);
        var response = customerService.getBusinessQualificationsV2(consentId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Retrieved business qualifications for consent id {}", consentId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }
}
