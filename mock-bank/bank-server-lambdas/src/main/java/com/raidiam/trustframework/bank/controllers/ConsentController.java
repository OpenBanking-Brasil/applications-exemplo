package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.services.ConsentService;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.CreateConsent;
import com.raidiam.trustframework.mockbank.models.generated.ResponseConsent;
import com.raidiam.trustframework.mockbank.models.generated.ResponseConsentFull;
import com.raidiam.trustframework.mockbank.models.generated.UpdateConsent;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import java.util.List;

@RolesAllowed({"CONSENTS_MANAGE", "CONSENTS_FULL_MANAGE"})
@Controller("/open-banking/consents/v1/consents")
public class ConsentController extends BaseBankController {

  private static final Logger LOG = LoggerFactory.getLogger(ConsentController.class);

  private final ConsentService service;

  ConsentController(ConsentService service) {
    this.service = service;
  }

  @Post
  @Status(HttpStatus.CREATED)
  public ResponseConsent createConsent(@Body @Valid CreateConsent body, HttpRequest<?> request) {
    var callerInfo = BankLambdaUtils.getRequestMeta(request);
    String clientId = callerInfo.getClientId();
    LOG.info("Creating new consent for client {}", clientId);
    BankLambdaUtils.logObject(mapper, body);
    ResponseConsent consentResponse = service.createConsent(clientId, body);
    String consentId = consentResponse.getData().getConsentId();
    BankLambdaUtils.decorateResponse(consentResponse::setLinks, consentResponse::setMeta, appBaseUrl + request.getPath() + "/" + consentId, 1);
    BankLambdaUtils.logObject(mapper, consentResponse);
    return consentResponse;
  }

  @Get("/{consentId}")
  public Object getConsent(@PathVariable("consentId") String consentId, HttpRequest<?> request) {
    LOG.info("Looking up consent {}", consentId);

    var callerInfo = BankLambdaUtils.getRequestMeta(request);
    List<String> roles = callerInfo.getRoles();
    if(roles.contains("CONSENTS_FULL_MANAGE")) {
      LOG.info("OP making call - return full response");
      var response = service.getConsentFull(consentId);
      BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
      BankLambdaUtils.logObject(mapper, response);
      return response;
    }
    var response = service.getConsent(consentId, callerInfo.getClientId());
    BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
    LOG.info("External client making call - return partial response");
    BankLambdaUtils.logObject(mapper, response);
    return response;
  }

  @Put("/{consentId}")
  @RolesAllowed({"CONSENTS_FULL_MANAGE"})
  public ResponseConsentFull putConsent(@PathVariable("consentId") String consentId, @Body @Valid UpdateConsent request) {
    LOG.info("Updating consent {}", consentId);
    BankLambdaUtils.logObject(mapper, request);
    var response = service.updateConsent(consentId, request);
    BankLambdaUtils.logObject(mapper, response);
    return response;
  }

  @Delete("/{consentId}")
  public HttpResponse<Object> delete(@PathVariable("consentId") String consentId, HttpRequest<?> request) {
    LOG.info("Deleting consent {}", consentId);
    BankLambdaUtils.RequestMeta requestMeta = BankLambdaUtils.getRequestMeta(request);
    service.deleteConsent(consentId, requestMeta.getClientId());
    LOG.info("Returning 204 No Content");
    return HttpResponse.noContent();
  }
}
