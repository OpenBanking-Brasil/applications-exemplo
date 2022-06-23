package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.services.LoansService;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
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
@Controller("/open-banking/loans/v1/contracts")
public class LoansController extends BaseBankController {

  private static final Logger LOG = LoggerFactory.getLogger(LoansController.class);

  private final LoansService service;

  LoansController(LoansService service) {
    this.service = service;
  }

  @Get
  public ResponseLoansContractList getLoansContracts(Pageable pageable, HttpRequest<?> request) {
    LOG.info("Looking up all loan contracts");
    var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
    LOG.info("Getting loan contracts for consent id {}", consentId);
    Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request);
    var response = service.getLoansContractList(adjustedPageable, consentId);
    BankLambdaUtils.decorateResponse(response::setLinks, response.getMeta().getTotalPages(), appBaseUrl + request.getPath(), adjustedPageable.getNumber());
    LOG.info("External client making call for LoansContractList - return response");
    BankLambdaUtils.logObject(mapper, response);
    return response;
  }

  @Get("/{contractId}")
  public ResponseLoansContract getLoansContract(@PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
    LOG.info("Looking up loan contract {}", contractId);
    var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
    var response = service.getLoanContract(consentId, contractId);
    BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
    LOG.info("External client making call for LoansContract - return response");
    BankLambdaUtils.logObject(mapper, response);
    return response;
  }

  @Get("/{contractId}/payments")
  public ResponseLoansPayments getLoansPayments(@PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
    LOG.info("Looking up payments for loan contract {}", contractId);
    var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
    var response = service.getLoanPayments(consentId, contractId);
    BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
    LOG.info("External client making call for LoansPayments - return response");
    BankLambdaUtils.logObject(mapper, response);
    return response;
  }

  @Get("/{contractId}/scheduled-instalments")
  public ResponseLoansInstalments getLoansScheduledInstalments(@PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
    LOG.info("Looking up scheduled instalments for loan contract {}", contractId);
    var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
    var response = service.getLoanScheduledInstalments(consentId, contractId);
    BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
    LOG.info("External client making call for LoansInstalments - return response");
    BankLambdaUtils.logObject(mapper, response);
    return response;
  }

  @Get("/{contractId}/warranties")
  public ResponseLoansWarranties getLoansWarranties(Pageable pageable, @PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
    LOG.info("Looking up warranties for loan contract {}", contractId);
    var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
    Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request);
    var response = service.getLoanWarranties(adjustedPageable, consentId, contractId);
    BankLambdaUtils.decorateResponse(response::setLinks, response.getMeta().getTotalPages(), appBaseUrl + request.getPath(), adjustedPageable.getNumber());
    LOG.info("External client making call for LoansWarranties - return response");
    BankLambdaUtils.logObject(mapper, response);
    return response;
  }
}

