package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.auth.AuthenticationGrant;
import com.raidiam.trustframework.bank.auth.RequiredAuthenticationGrant;
import com.raidiam.trustframework.bank.fapi.ConsentsExtendsPostRequestFilter;
import com.raidiam.trustframework.bank.fapi.XFapiInteractionIdRequired;
import com.raidiam.trustframework.bank.services.ConsentService;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.context.annotation.Value;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RolesAllowed({"CONSENTS_MANAGE", "CONSENTS_FULL_MANAGE"})
@Controller("/open-banking/consents")
public class ConsentController extends BaseBankController {
    private static final Logger LOG = LoggerFactory.getLogger(ConsentController.class);
    private final ConsentService service;

    @Value("${mockbank.max-page-size}")
    int maxPageSize;

    ConsentController(ConsentService service) {
        this.service = service;
    }

    @Post("/v2/consents")
    @Status(HttpStatus.CREATED)
    public ResponseConsentV2 createConsentV2(@Body @Valid CreateConsentV2 body, HttpRequest<?> request) {
        var callerInfo = BankLambdaUtils.getRequestMeta(request);
        String clientId = callerInfo.getClientId();
        LOG.info("Creating new consent for client {} v2", clientId);
        BankLambdaUtils.logObject(mapper, body);
        ResponseConsentV2 consentResponse = service.createConsentV2(clientId, body);
        String consentId = consentResponse.getData().getConsentId();
        BankLambdaUtils.decorateResponse(consentResponse::setLinks, consentResponse::setMeta, appBaseUrl + request.getPath() + "/" + consentId, 1);
        BankLambdaUtils.logObject(mapper, consentResponse);
        return consentResponse;
    }

    @Get("/v2/consents/{consentId}")
    @RequiredAuthenticationGrant(AuthenticationGrant.CLIENT_CREDENTIALS)
    public Object getConsentV2(@PathVariable("consentId") String consentId, HttpRequest<?> request) {
        LOG.info("Looking up consent {} v2", consentId);

        var callerInfo = BankLambdaUtils.getRequestMeta(request);
        List<String> roles = callerInfo.getRoles();
        if (roles.contains("CONSENTS_FULL_MANAGE")) {
            LOG.info("OP making call - return full response");
            var response = service.getConsentFullV2(consentId);
            BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
            BankLambdaUtils.logObject(mapper, response);
            return response;
        }
        var response = service.getConsentV2(consentId, callerInfo.getClientId());
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("External client making call - return partial response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Put("/v2/consents/{consentId}")
    @RolesAllowed({"CONSENTS_FULL_MANAGE"})
    public ResponseConsentFullV2 putConsentV2(@PathVariable("consentId") String consentId, @Body @Valid UpdateConsent request) {
        LOG.info("Updating consent {} v1", consentId);
        BankLambdaUtils.logObject(mapper, request);
        var response = service.updateConsentV2(consentId, request);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Delete("/v{version}/consents/{consentId}")
    public HttpResponse<Object> delete(@PathVariable("version") @Min(1) @Max(2) int version,
                                       @PathVariable("consentId") String consentId, HttpRequest<?> request) {
        LOG.info("Deleting consent {} v{}", consentId, version);
        BankLambdaUtils.RequestMeta requestMeta = BankLambdaUtils.getRequestMeta(request);
        service.deleteConsentV2(consentId, requestMeta.getClientId());
        LOG.info("Returning 204 No Content");
        return HttpResponse.noContent();
    }

    @Delete("/v3/consents/{consentId}")
    @RequiredAuthenticationGrant(AuthenticationGrant.CLIENT_CREDENTIALS)
    @XFapiInteractionIdRequired
    public HttpResponse<Object> deleteV3(@PathVariable("consentId") String consentId, HttpRequest<?> request) {
        LOG.info("Deleting consent {} v3", consentId);
        BankLambdaUtils.RequestMeta requestMeta = BankLambdaUtils.getRequestMeta(request);
        service.deleteConsentV3(consentId, requestMeta.getClientId());
        LOG.info("Returning 204 No Content");
        return HttpResponse.noContent();
    }

    @RolesAllowed({"OPENID"})
    @Post("/v2/consents/{consentId}/extends")
    @RequiredAuthenticationGrant(AuthenticationGrant.AUTHORISATION_CODE)
    @Status(HttpStatus.CREATED)
    public ResponseConsentV2 createConsentExtends(@Body @Valid CreateConsentExtends body,
                                                  @PathVariable("consentId") String consentId,
                                                  HttpRequest<?> request) {

        LOG.info("Creating new consent extension");
        BankLambdaUtils.logObject(mapper, body);

        ResponseConsentV2 consentResponse = service.createConsentExtension(consentId, body);
        BankLambdaUtils.decorateResponse(consentResponse::setLinks, consentResponse::setMeta, appBaseUrl + request.getPath(), 1);
        BankLambdaUtils.logObject(mapper, consentResponse);
        return consentResponse;
    }

    @RolesAllowed({"OPENID"})
    @Post("/v3/consents/{consentId}/extends")
    @RequiredAuthenticationGrant(AuthenticationGrant.AUTHORISATION_CODE)
    @XFapiInteractionIdRequired
    @Status(HttpStatus.CREATED)
    public ResponseConsentV3 createConsentExtendsV3(@Body @Valid CreateConsentExtendsV3 body,
                                                  @PathVariable("consentId") String consentId,
                                                  HttpRequest<?> request) {

        LOG.info("Creating new consent extension v3");
        BankLambdaUtils.logObject(mapper, body);

        String customerIpAddress = request.getHeaders().get(ConsentsExtendsPostRequestFilter.CUSTOMER_IP_ADDRESS);
        String customerUserAgent = request.getHeaders().get(ConsentsExtendsPostRequestFilter.CUSTOMER_USER_AGENT);
        LOG.info("customerIpAddress - {} customerUserAgent - {}", customerIpAddress, customerUserAgent);

        ResponseConsentV3 consentResponse = service.createConsentExtensionV3(consentId, body, customerIpAddress, customerUserAgent);
        BankLambdaUtils.decorateResponseSimpleMeta(consentResponse::setLinks, consentResponse::setMeta, appBaseUrl + request.getPath());
        BankLambdaUtils.logObject(mapper, consentResponse);
        return consentResponse;
    }

    @Get("/v2/consents/{consentId}/extends")
    public ResponseConsentReadExtends getConsentExtends(@PathVariable("consentId") String consentId,
                                                        HttpRequest<?> request) {

        LOG.info("Looking up consent extensions. Consent ID - {}", consentId);
        ResponseConsentReadExtends response = service.getConsentExtensions(consentId);
        int totalRecords = response.getData().size();
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), totalRecords, totalRecords);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/v3/consents/{consentId}/extensions")
    @RequiredAuthenticationGrant(AuthenticationGrant.CLIENT_CREDENTIALS)
    @XFapiInteractionIdRequired
    public ResponseConsentReadExtensionsV3 getConsentExtendsV3(@PathVariable("consentId") String consentId,
                                                        Pageable pageable,
                                                        HttpRequest<?> request) {

        LOG.info("Looking up consent extensions v3. Consent ID - {}", consentId);
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request, maxPageSize);
        var response = service.getConsentExtensionsV3(consentId, adjustedPageable);
        BankLambdaUtils.decorateResponseSimpleLinkMeta(response::setLinks, response::setMeta, appBaseUrl + request.getPath());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Post("/v3/consents")
    @XFapiInteractionIdRequired
    @RequiredAuthenticationGrant(AuthenticationGrant.CLIENT_CREDENTIALS)
    @Status(HttpStatus.CREATED)
    public ResponseConsentV3 createConsentV3(@Body @Valid CreateConsentV3 body, HttpRequest<?> request) {
        var callerInfo = BankLambdaUtils.getRequestMeta(request);
        String clientId = callerInfo.getClientId();
        LOG.info("Creating new consent for client {} v3", clientId);
        BankLambdaUtils.logObject(mapper, body);
        ResponseConsentV3 consentResponse = service.createConsentV3(clientId, body);
        String consentId = consentResponse.getData().getConsentId();
        BankLambdaUtils.decorateResponseSimpleMeta(consentResponse::setLinks, consentResponse::setMeta, appBaseUrl + request.getPath() + "/" + consentId);
        BankLambdaUtils.logObject(mapper, consentResponse);
        return consentResponse;
    }

    @Get("/v3/consents/{consentId}")
    @XFapiInteractionIdRequired
    @RequiredAuthenticationGrant(AuthenticationGrant.CLIENT_CREDENTIALS)
    public Object getConsentV3(@PathVariable("consentId") String consentId, HttpRequest<?> request) {
        LOG.info("Looking up consent {} v3", consentId);

        var callerInfo = BankLambdaUtils.getRequestMeta(request);
        List<String> roles = callerInfo.getRoles();
        if (roles.contains("CONSENTS_FULL_MANAGE")) {
            LOG.info("OP making call - return full response");
            var response = service.getConsentFullV2(consentId);
            BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
            BankLambdaUtils.logObject(mapper, response);
            return response;
        }
        var response = service.getConsentV3(consentId, callerInfo.getClientId());
        BankLambdaUtils.decorateResponseSimpleMeta(response::setLinks, response::setMeta, appBaseUrl + request.getPath());
        LOG.info("External client making call - return partial response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }
}
