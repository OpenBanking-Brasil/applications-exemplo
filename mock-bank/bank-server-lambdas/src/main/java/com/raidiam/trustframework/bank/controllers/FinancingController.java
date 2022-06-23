package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.services.FinancingService;
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

@RolesAllowed({"FINANCINGS_READ"})
@Controller("/open-banking/financings/v1/contracts")
public class FinancingController extends BaseBankController {

  private static final Logger LOG = LoggerFactory.getLogger(FinancingController.class);

  private final FinancingService service;

  FinancingController(FinancingService service) {
    this.service = service;
  }

  @Get
  public ResponseFinancingsContractList getFinancingContractsList(Pageable pageable, HttpRequest<?> request) {
    LOG.info("Getting financing contracts");
    var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
    LOG.info("Getting financing contracts for consent id {}", consentId);
    Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request);
    var response = service.getFinancingContractList(adjustedPageable, consentId);
    BankLambdaUtils.decorateResponse(response::setLinks, response.getMeta().getTotalPages(), appBaseUrl + request.getPath(), adjustedPageable.getNumber());
    LOG.info("External client making call for FinancingsContractsList - return response");
    BankLambdaUtils.logObject(mapper, response);
    return response;
  }

  @Get("/{contractId}")
  public ResponseFinancingsContract getFinancingContract(@PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
    LOG.info("Looking up financing contract {}", contractId);
    var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
    var response = service.getFinancingContract(consentId, contractId);
    BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
    LOG.info("External client making call for FinancingsContract - return response");
    BankLambdaUtils.logObject(mapper, response);
    return response;
  }

  @Get("/{contractId}/payments")
  public ResponseFinancingsPayments getFinancingPayments(@PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
    LOG.info("Looking up payments for financing contract {}", contractId);
    var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
    var response = service.getFinancingPayments(consentId, contractId);
    BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
    LOG.info("External client making call for FinancingsPayments - return response");
    BankLambdaUtils.logObject(mapper, response);
    return response;
  }

  @Get("/{contractId}/scheduled-instalments")
  public ResponseFinancingsInstalments getFinancingScheduledInstalments(@PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
    LOG.info("Looking up scheduled instalments for financing contract {}", contractId);
    var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
    var response = service.getFinancingScheduledInstalments(consentId, contractId);
    BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
    LOG.info("External client making call for Financingsinstalments - return response");
    BankLambdaUtils.logObject(mapper, response);
    return response;
  }

  @Get("/{contractId}/warranties")
  public ResponseFinancingsWarranties getFinancingWarranties(Pageable pageable, @PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
    LOG.info("Looking up warranties for financing contract {}", contractId);
    var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
    Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request);
    var response = service.getFinancingWarranties(adjustedPageable, consentId, contractId);
    BankLambdaUtils.decorateResponse(response::setLinks, response.getMeta().getTotalPages(), appBaseUrl + request.getPath(), adjustedPageable.getNumber());
    LOG.info("External client making call for FinancingsWarranties - return response");
    BankLambdaUtils.logObject(mapper, response);
    return response;
  }
}

