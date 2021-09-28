package com.raidiam.trustframework.bank.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raidiam.trustframework.bank.services.ConsentService;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.context.annotation.Value;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import java.time.OffsetDateTime;
import java.util.List;

@RolesAllowed({"CONSENTS_MANAGE", "CONSENTS_FULL_MANAGE"})
@Controller("/consents/v1/consents")
public class ConsentController {

  @Inject
  private ObjectMapper mapper;

  private static final Logger LOG = LoggerFactory.getLogger(ConsentController.class);

  private final ConsentService service;

  @Value("")
  private String appBaseUrl;

  ConsentController(ConsentService service) {
    this.service = service;
  }

  @Get
  public Page<ResponseConsent> getConsents(Pageable pageable) {
    return service.getConsents(pageable);
  }

  @Post
  @Status(HttpStatus.CREATED)
  public ResponseConsent createConsent(@Body @Valid CreateConsent body, HttpRequest<?> request) {
    var callerInfo = BankLambdaUtils.getCallerInfo(request);

    String clientId = callerInfo.getClientId();
    LOG.info("Creating new consent for client {}", clientId);
    try {
      LOG.info(mapper.writeValueAsString(body));
    } catch (JsonProcessingException e) {
      LOG.error("Not JSON", e);
    }
    return decorate(service.createConsent(body, clientId), "/");
  }

  @Get("/{consentId}")
  public Object getConsent(@PathVariable("consentId") String consentId, HttpRequest<?> request) {
    LOG.info("Looking up consent {}", consentId);

    var callerInfo = BankLambdaUtils.getCallerInfo(request);
    List<String> roles = callerInfo.getRoles();
    if(roles.contains("CONSENTS_FULL_MANAGE")) {
      LOG.info("OP making call - return full response");
      Object res = decorate(service.getConsentFull(consentId), consentId);
      try {
        LOG.info(mapper.writeValueAsString(res));
      } catch (JsonProcessingException e) {
        LOG.error("Not JSON", e);
      }
      return res;
    }
    Object res = decorate(service.getConsent(consentId, callerInfo.getClientId()), consentId);
    LOG.info("External client making call - return partial response");
    try {
      LOG.info(mapper.writeValueAsString(res));
    } catch (JsonProcessingException e) {
      LOG.error("Not JSON", e);
    }
    return res;
  }

  @Put("/{consentId}")
  @RolesAllowed({"CONSENTS_FULL_MANAGE"})
  public ResponseConsentFull putConsent(@PathVariable("consentId") String consentId, @Body @Valid UpdateConsent request) {
    LOG.info("Updating consent {}", consentId);
    return service.updateConsent(consentId, request);
  }

  @Delete("/{consentId}")
  public HttpResponse<Object> delete(@PathVariable("consentId") String consentId, HttpRequest<?> request) {
    BankLambdaUtils.CallerInfo callerInfo = BankLambdaUtils.getCallerInfo(request);
    service.deleteConsent(consentId, callerInfo.getClientId());
    return HttpResponse.noContent();
  }

  private ResponseConsent decorate(ResponseConsent responseConsent, String self) {
    Links links = new Links()
            .self(self);

    Meta meta = new Meta()
            .totalPages(1)
            .totalRecords(1)
            .requestDateTime(OffsetDateTime.now());
    responseConsent.links(links);
    responseConsent.meta(meta);
    return responseConsent;
  }

  private ResponseConsentFull decorate(ResponseConsentFull responseConsent, String self) {
    Links links = new Links()
            .self(self);

    Meta meta = new Meta()
            .totalPages(1)
            .totalRecords(1)
            .requestDateTime(OffsetDateTime.now());
    responseConsent.links(links);
    responseConsent.meta(meta);
    return responseConsent;
  }
}
