package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.services.UnarrangedAccountsOverdraftService;
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

@RolesAllowed({"UNARRANGED_ACCOUNTS_OVERDRAFT_READ"})
@Controller("/open-banking/unarranged-accounts-overdraft/v1/contracts")
public class UnarrangedAccountsOverdraftController extends BaseBankController {

  private static final Logger LOG = LoggerFactory.getLogger(UnarrangedAccountsOverdraftController.class);

  private final UnarrangedAccountsOverdraftService service;

  UnarrangedAccountsOverdraftController(UnarrangedAccountsOverdraftService service) {
    this.service = service;
  }

  @Get
  public ResponseUnarrangedAccountOverdraftContractList getUnarrangedAccountOverdraftContractsList(Pageable pageable, HttpRequest<?> request) {
    LOG.info("Getting Unnarranged account overdraft list");
    var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
    var adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request);
    var response = service.getUnarrangedOverdraftContractList(adjustedPageable, consentId);
    BankLambdaUtils.decorateResponse(response::setLinks, response.getMeta().getTotalPages(), appBaseUrl + request.getPath(), adjustedPageable.getNumber());
    LOG.info("External client making call for UnarrangedAccountOverdraftContractList - return response");
    BankLambdaUtils.logObject(mapper, response);
    return response;
  }

  @Get("/{contractId}")
  public ResponseUnarrangedAccountOverdraftContract getUnarrangedAccountsOverdraftContract(@PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
    LOG.info("Looking up unarranged account overdraft contract {}", contractId);
    var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
    var response = service.getUnarrangedOverdraftContract(consentId, contractId);
    BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
    LOG.info("External client making call for UnarrangedAccountOverdraftContractData - return response");
    BankLambdaUtils.logObject(mapper, response);
    return response;
  }

  @Get("/{contractId}/payments")
  public ResponseUnarrangedAccountOverdraftPayments getUnarrangedAccountsOverdraftPayments(@PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
    LOG.info("Looking up payments for unarranged account overdraft contract {}", contractId);
    var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
    var response = service.getUnarrangedOverdraftPayments(consentId, contractId);
    BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
    LOG.info("External client making call for UnarrangedAccountOverdraftPaymentsData - return response");
    BankLambdaUtils.logObject(mapper, response);
    return response;
  }

  @Get("/{contractId}/scheduled-instalments")
  public ResponseUnarrangedAccountOverdraftInstalments getUnarrangedAccountsOverdraftScheduledInstalments(@PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
    LOG.info("Looking up scheduled instalments for unarranged account overdraft contract {}", contractId);
    var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
    var response = service.getUnarrangedOverdraftScheduledInstalments(consentId, contractId);
    BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
    LOG.info("External client making call for UnarrangedAccountOverdraftInstalmentsData - return response");
    BankLambdaUtils.logObject(mapper, response);
    return response;
  }

  @Get("/{contractId}/warranties")
  public ResponseUnarrangedAccountOverdraftWarranties getUnarrangedAccountsOverdraftWarranties(Pageable pageable, @PathVariable("contractId") UUID contractId, HttpRequest<?> request) {
    LOG.info("Looking up scheduled instalments for unarranged account overdraft contract {}", contractId);
    var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
    var adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request);
    var response = service.getUnarrangedOverdraftWarranties(adjustedPageable, consentId, contractId);
    BankLambdaUtils.decorateResponse(response::setLinks, response.getMeta().getTotalPages(), appBaseUrl + request.getPath(), adjustedPageable.getNumber());
    LOG.info("External client making call for UnarrangedAccountsOverdraftContractedWarranty - return response");
    BankLambdaUtils.logObject(mapper, response);
    return response;
  }
}

