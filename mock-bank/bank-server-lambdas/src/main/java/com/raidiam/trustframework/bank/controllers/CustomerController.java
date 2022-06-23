package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.services.CustomerService;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.NotNull;

@Controller("/open-banking/customers/v1")
@RolesAllowed("CUSTOMERS_READ")
public class CustomerController extends BaseBankController {

    private static final Logger LOG = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Get("/personal/identifications")
    public ResponsePersonalCustomersIdentification getPersonalIdentifications(@NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Getting personal identifications for consent id {}", consentId);
        var response = customerService.getPersonalIdentifications(consentId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), response.getData().size());
        LOG.info("Retrieved personal identifications for consent id {}", consentId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/personal/financial-relations")
    public ResponsePersonalCustomersFinancialRelation getPersonalFinancialRelations(@NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Getting personal financial relations for consent id {}", consentId);
        var response = customerService.getPersonalFinancialRelations(consentId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Retrieved personal financial relations for consent id {}", consentId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/personal/qualifications")
    public ResponsePersonalCustomersQualification getPersonalQualifications(@NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Getting personal qualifications for consent id {}", consentId);
        var response = customerService.getPersonalQualifications(consentId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Retrieved personal qualifications for consent id {}", consentId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/business/identifications")
    public ResponseBusinessCustomersIdentification getBusinessIdentifications(@NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Getting business identifications for consent id {}", consentId);
        var response = customerService.getBusinessIdentifications(consentId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), response.getData().size());
        LOG.info("Retrieved business identifications for consent id {}", consentId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/business/financial-relations")
    public ResponseBusinessCustomersFinancialRelation getBusinessFinancialRelations(@NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Getting business financial relations for consent id {}", consentId);
        var response = customerService.getBusinessFinancialRelations(consentId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Retrieved business financial relations for consent id {}", consentId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/business/qualifications")
    public ResponseBusinessCustomersQualification getBusinessQualifications(@NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Getting business qualifications for consent id {}", consentId);
        var response = customerService.getBusinessQualifications(consentId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Retrieved business qualifications for consent id {}", consentId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }
}
