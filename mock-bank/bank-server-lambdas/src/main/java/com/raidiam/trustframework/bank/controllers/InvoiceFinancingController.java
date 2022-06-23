package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.services.InvoiceFinancingService;
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

@RolesAllowed({"INVOICE_FINANCINGS_READ"})
@Controller("/open-banking/invoice-financings/v1/contracts")
public class InvoiceFinancingController extends BaseBankController {

  private static final Logger LOG = LoggerFactory.getLogger(InvoiceFinancingController.class);

  private final InvoiceFinancingService service;

  InvoiceFinancingController(InvoiceFinancingService service) {
    this.service = service;
  }

  @Get
  public ResponseInvoiceFinancingsContractList getInvoiceFinancingContractsList(Pageable pageable, HttpRequest<?> request) {
    LOG.info("Looking up invoice financing contract list");
    var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
    LOG.info("Getting invoice financing contracts for consent id {}", consentId);
    Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request);
    var response = service.getInvoiceFinancingContractList(adjustedPageable, consentId);
    BankLambdaUtils.decorateResponse(response::setLinks, response.getMeta().getTotalPages(), appBaseUrl + request.getPath(), adjustedPageable.getNumber());
    LOG.info("External client making call for InvoiceFinancingsContractList - return response");
    BankLambdaUtils.logObject(mapper, response);
    return response;
  }

  @Get("/{contractId}")
  public ResponseInvoiceFinancingsContract getInvoiceContract(@PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
    LOG.info("Looking up invoice financing contract {}", contractId);
    var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
    var response = service.getInvoiceFinancingContract(consentId, contractId);
    BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
    LOG.info("External client making call for InvoiceFinancingsContract - return response");
    BankLambdaUtils.logObject(mapper, response);
    return response;
  }

  @Get("/{contractId}/payments")
  public ResponseInvoiceFinancingsPayments getInvoicePayments(@PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
    LOG.info("Looking up payments for invoice financing contract {}", contractId);
    var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
    var response = service.getInvoiceFinancingPayments(consentId, contractId);
    BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
    LOG.info("External client making call for InvoiceFinancingsPayments - return response");
    BankLambdaUtils.logObject(mapper, response);
    return response;
  }

  @Get("/{contractId}/scheduled-instalments")
  public ResponseInvoiceFinancingsInstalments getInvoiceScheduledInstalments(@PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
    LOG.info("Looking up scheduled instalments for invoice financing contract {}", contractId);
    var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
    var response = service.getInvoiceFinancingScheduledInstalments(consentId, contractId);
    BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
    LOG.info("External client making call for InvoiceFinancingsInstalments - return response");
    BankLambdaUtils.logObject(mapper, response);
    return response;
  }

  @Get("/{contractId}/warranties")
  public ResponseInvoiceFinancingsWarranties getInvoiceWarranties(Pageable pageable, @PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
    LOG.info("Looking up scheduled instalments for invoice financing contract {}", contractId);
    var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
    Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request);
    var response = service.getInvoiceFinancingWarranties(adjustedPageable, consentId, contractId);
    BankLambdaUtils.decorateResponse(response::setLinks, response.getMeta().getTotalPages(), appBaseUrl + request.getPath(), adjustedPageable.getNumber());
    LOG.info("External client making call for InvoiceFinancingsContractedWarranty - return response");
    BankLambdaUtils.logObject(mapper, response);
    return response;
  }
}

