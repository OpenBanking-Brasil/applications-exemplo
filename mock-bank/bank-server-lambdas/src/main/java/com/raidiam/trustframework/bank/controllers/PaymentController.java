package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.auth.AuthenticationGrant;
import com.raidiam.trustframework.bank.auth.RequiredAuthenticationGrant;
import com.raidiam.trustframework.bank.fapi.XFapiInteractionIdRequired;
import com.raidiam.trustframework.bank.services.PaymentConsentService;
import com.raidiam.trustframework.bank.services.PaymentsService;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RolesAllowed({"PAYMENTS_MANAGE", "PAYMENTS_FULL_MANAGE"})
@Controller("/open-banking/payments")
public class PaymentController extends BaseBankController {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentsService paymentsService;
    private final PaymentConsentService paymentConsentService;

    PaymentController(PaymentsService paymentsService, PaymentConsentService paymentConsentService) {
        this.paymentsService = paymentsService;
        this.paymentConsentService = paymentConsentService;
    }

    @Post(value = "/v3/consents", consumes = {"application/jwt"}, produces = {"application/jwt", "*/*"})
    @RequiredAuthenticationGrant(AuthenticationGrant.CLIENT_CREDENTIALS)
    @XFapiInteractionIdRequired
    @Status(HttpStatus.CREATED)
    public ResponsePaymentConsentV2 createConsent(@Body @Valid CreatePaymentConsent body,
                                                  HttpRequest<?> request) {
        LOG.info("Creating payment consent for v3");
        var requestMeta = BankLambdaUtils.getRequestMeta(request);
        String clientId = requestMeta.getClientId();
        LOG.info("Creating new payment consent for client {}", clientId);
        BankLambdaUtils.logObject(mapper, body);

        String idempotencyKey = BankLambdaUtils.getIdempotencyKey(request);
        String jti = requestMeta.getJti();

        var response = paymentConsentService.createConsentV3(clientId, idempotencyKey, jti, body);
        BankLambdaUtils.decorateResponseSimpleMetaBrasilTimeZone(response::setLinks, response::setMeta, appBaseUrl + request.getPath() + "/" + response.getData().getConsentId());
        BankLambdaUtils.logObject(mapper, response);
        return response;

    }

    @Post(value = "/v4/consents", consumes = {"application/jwt"}, produces = {"application/jwt", "*/*"})
    @RequiredAuthenticationGrant(AuthenticationGrant.CLIENT_CREDENTIALS)
    @XFapiInteractionIdRequired
    @Status(HttpStatus.CREATED)
    public ResponseCreatePaymentConsentV4 createConsentV4(@Body @Valid CreatePaymentConsentV4 body,
                                                          HttpRequest<?> request) {
        LOG.info("Creating payment consent for V4");
        var requestMeta = BankLambdaUtils.getRequestMeta(request);
        String clientId = requestMeta.getClientId();
        LOG.info("Creating new payment consent for client {}", clientId);
        BankLambdaUtils.logObject(mapper, body);

        String idempotencyKey = BankLambdaUtils.getIdempotencyKey(request);
        String jti = requestMeta.getJti();

        var response = paymentConsentService.createConsentV4(clientId, idempotencyKey, jti, body);
        BankLambdaUtils.decorateResponseSimpleMetaBrasilTimeZone(response::setLinks, response::setMeta, appBaseUrl + request.getPath() + "/" + response.getData().getConsentId());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get(value = "/v3/consents/{consentId}", consumes = {"*/*"}, produces = {"application/jwt", "*/*"})
    @RequiredAuthenticationGrant(AuthenticationGrant.CLIENT_CREDENTIALS)
    @XFapiInteractionIdRequired
    public Object getConsentV3(@PathVariable("consentId") String consentId, HttpRequest<?> request) {
        LOG.info("Getting payment consent Id {} for v3", consentId);
        var callerInfo = BankLambdaUtils.getRequestMeta(request);
        var clientId = callerInfo.getClientId();
        if (BankLambdaUtils.isPaymentFullManageCaller(request)) {
            LOG.info("OP making call - return full response for V3");
            var response = paymentConsentService.getConsentFull(consentId, clientId);
            BankLambdaUtils.logObject(mapper, response);
            return response;
        }
        LOG.info("External client making call - return partial response");
        var response = paymentConsentService.getConsentV3(consentId, clientId);
        BankLambdaUtils.decorateResponseSimpleMetaBrasilTimeZone(response::setLinks, response::setMeta, appBaseUrl + request.getPath());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get(value = "/v4/consents/{consentId}", consumes = {"*/*"}, produces = {"application/jwt", "*/*"})
    @RequiredAuthenticationGrant(AuthenticationGrant.CLIENT_CREDENTIALS)
    @XFapiInteractionIdRequired
    public Object getConsentV4(@PathVariable("consentId") String consentId, HttpRequest<?> request) {
        LOG.info("Getting payment consent Id {} for v4", consentId);
        var callerInfo = BankLambdaUtils.getRequestMeta(request);
        var clientId = callerInfo.getClientId();
        if (BankLambdaUtils.isPaymentFullManageCaller(request)) {
            LOG.info("OP making call - return full response for V4");
            var response = paymentConsentService.getConsentFull(consentId, clientId);
            BankLambdaUtils.logObject(mapper, response);
            return response;
        }
        LOG.info("External client making call - return partial response");
        var response = paymentConsentService.getConsentV4(consentId, clientId);//created
        BankLambdaUtils.decorateResponseSimpleMetaBrasilTimeZone(response::setLinks, response::setMeta, appBaseUrl + request.getPath());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Put(value = "/v3/consents/{consentId}", consumes = {"application/json", "application/jwt"}, produces = {"application/json", "*/*"})
    @RolesAllowed({"PAYMENTS_FULL_MANAGE"})
    public ResponsePaymentConsent putConsent(@PathVariable("consentId") String consentId, @Body @Valid UpdatePaymentConsent body, HttpRequest<?> request) {
        var requestMeta = BankLambdaUtils.getRequestMeta(request);
        String clientId = requestMeta.getClientId();
        LOG.info("Updating payment consent {} for v3", consentId);
        var response = paymentConsentService.updateConsent(consentId, clientId, body);
        BankLambdaUtils.decorateResponseBrasilTimeZone(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Updated payment consent {}", consentId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Post(value = "/v3/pix/payments", consumes = {"application/jwt"}, produces = {"application/jwt", "*/*"})
    @RequiredAuthenticationGrant(AuthenticationGrant.AUTHORISATION_CODE)
    @XFapiInteractionIdRequired
    @Status(HttpStatus.CREATED)
    public ResponsePixPaymentV3 createPaymentV3(@Body @Valid CreatePixPaymentV3 body, HttpRequest<?> request) {
        LOG.info("Creating payment for v3");
        BankLambdaUtils.logObject(mapper, body);
        var callerInfo = BankLambdaUtils.getRequestMeta(request);

        String idempotencyKey = BankLambdaUtils.getIdempotencyKey(request);
        String jti = callerInfo.getJti();

        var response = paymentsService.createPaymentV3(callerInfo.getConsentId(), idempotencyKey, jti, body, callerInfo.getClientId());
        BankLambdaUtils.decorateResponseBrasilTimeZone(response::setLinks, response::setMeta, appBaseUrl + request.getPath() + "/" + response.getData().getPaymentId(), 1);
        LOG.info("Created payment {}", response.getData().getPaymentId());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Post(value = "/v4/pix/payments", consumes = {"application/jwt"}, produces = {"application/jwt", "*/*"})
    @RequiredAuthenticationGrant(AuthenticationGrant.AUTHORISATION_CODE)
    @XFapiInteractionIdRequired
    @Status(HttpStatus.CREATED)
    public ResponsePixPaymentV4 createPaymentV4(@Body @Valid CreatePixPaymentV4 body, HttpRequest<?> request) {
        LOG.info("Creating payment for v4");
        BankLambdaUtils.logObject(mapper, body);
        var callerInfo = BankLambdaUtils.getRequestMeta(request);

        String idempotencyKey = BankLambdaUtils.getIdempotencyKey(request);
        String jti = callerInfo.getJti();

        var response = paymentsService.createPaymentV4(bankLambdaUtils.getConsentIdFromRequest(request), idempotencyKey, jti, body, callerInfo.getClientId());

        String earliestPaymentId = null;
        LocalDateTime earliestDateTime = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

        for (ResponsePixPaymentDataV4 paymentData : response.getData()) {
            String endToEndId = paymentData.getEndToEndId();
            String dateTimePart = endToEndId.substring(9, 21);
            LocalDateTime dateTime = LocalDateTime.parse(dateTimePart, formatter);
            if (earliestDateTime == null || dateTime.isBefore(earliestDateTime)) {
                earliestDateTime = dateTime;
                earliestPaymentId = paymentData.getPaymentId();
            }
        }

        BankLambdaUtils.decorateResponseBrasilTimeZone(response::setLinks, response::setMeta, appBaseUrl + request.getPath() + "/" + earliestPaymentId, 1);
        LOG.info("Created payment {}", earliestPaymentId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get(value = "/v3/pix/payments/{paymentId}", consumes = {"*/*"}, produces = {"application/jwt", "*/*"})
    @RequiredAuthenticationGrant(AuthenticationGrant.CLIENT_CREDENTIALS)
    @XFapiInteractionIdRequired
    public ResponsePixPaymentV3 getPaymentV3(@PathVariable("paymentId") String paymentId, HttpRequest<?> request) {
        var requestMeta = BankLambdaUtils.getRequestMeta(request);
        String clientId = requestMeta.getClientId();
        LOG.info("Getting payment {} for v3", paymentId);
        var response = paymentsService.getPaymentV3(paymentId, clientId);
        BankLambdaUtils.decorateResponseBrasilTimeZone(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Get payment response for payment id {}", paymentId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get(value = "/v4/pix/payments/{paymentId}", consumes = {"*/*"}, produces = {"application/jwt", "*/*"})
    @RequiredAuthenticationGrant(AuthenticationGrant.CLIENT_CREDENTIALS)
    @XFapiInteractionIdRequired
    public ResponsePixPaymentReadV4 getPaymentV4(@PathVariable("paymentId") String paymentId, HttpRequest<?> request) {
        var requestMeta = BankLambdaUtils.getRequestMeta(request);
        String clientId = requestMeta.getClientId();
        LOG.info("Getting payment {} for v4", paymentId);
        var response = paymentsService.getPaymentV4(paymentId, clientId);
        BankLambdaUtils.decorateResponseBrasilTimeZone(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Get payment response for payment id {}", paymentId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Patch(value = "/v3/pix/payments/{paymentId}", consumes = {"application/jwt"}, produces = {"application/jwt", "*/*"})
    @RequiredAuthenticationGrant(AuthenticationGrant.CLIENT_CREDENTIALS)
    @XFapiInteractionIdRequired
    public ResponsePixPaymentV2 patchPayment(@PathVariable("paymentId") String paymentId, @Body @Valid PatchPaymentsV2 body, HttpRequest<?> request) {
        LOG.info("Patch payment {} for v3", paymentId);
        var response = paymentsService.patchPaymentV3(paymentId, body);
        BankLambdaUtils.decorateResponseBrasilTimeZone(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Patch payment response for payment id {}", response.getData().getPaymentId());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Put(value = "/v3/pix/payments/{paymentId}", consumes = {"application/json", "application/jwt"}, produces = {"application/json", "*/*"})
    @XFapiInteractionIdRequired
    @RolesAllowed({"PAYMENTS_FULL_MANAGE"})
    public ResponsePixPaymentV2 putPayment(@PathVariable("paymentId") String paymentId, @Body @Valid UpdatePixPaymentV2 body, HttpRequest<?> request) {
        var requestMeta = BankLambdaUtils.getRequestMeta(request);
        String clientId = requestMeta.getClientId();
        LOG.info("Updating payment {} for v3", paymentId);
        var response = paymentsService.updatePaymentV3(paymentId, body, clientId);
        BankLambdaUtils.decorateResponseBrasilTimeZone(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Updated payment {}", response.getData().getPaymentId());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }


    @Patch(value = "/v4/pix/payments/consents/{consentId}", consumes = {"application/jwt"}, produces = {"application/jwt", "*/*"})
    @RequiredAuthenticationGrant(AuthenticationGrant.CLIENT_CREDENTIALS)
    @XFapiInteractionIdRequired
    public ResponsePatchPixConsentV4 patchPaymentByConsentV4(@PathVariable("consentId") String consentId,
                                                             @Body @Valid PatchPixPaymentV4 body,
                                                             HttpRequest<?> request) {
        LOG.info("Canceling all payments with consent ID - {}", consentId);
        BankLambdaUtils.logObject(mapper, body);
        ResponsePatchPixConsentV4 response = paymentsService.patchPaymentByConsentIdV4(consentId, body);
        BankLambdaUtils.decorateResponseSimpleLinkMetaBrasilTimeZone(response::setLinks, response::setMeta, appBaseUrl + request.getPath());

        LOG.info("Canceled {} payments", response.getData().size());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Patch(value = "/v4/pix/payments/{paymentId}", consumes = {"application/jwt"}, produces = {"application/jwt", "*/*"})
    @RequiredAuthenticationGrant(AuthenticationGrant.CLIENT_CREDENTIALS)
    @XFapiInteractionIdRequired
    public ResponsePixPaymentReadV4 patchPaymentV4(@PathVariable("paymentId") String paymentId,
                                                   @Body @Valid PatchPixPaymentV4 body,
                                                   HttpRequest<?> request) {
        LOG.info("Canceling payments with payment ID - {}", paymentId);
        BankLambdaUtils.logObject(mapper, body);
      
        ResponsePixPaymentReadV4 response = paymentsService.patchPaymentV4(paymentId, body);
        BankLambdaUtils.decorateResponseSimpleLinkMetaBrasilTimeZone(response::setLinks, response::setMeta, appBaseUrl + request.getPath());

        LOG.info("Canceled payments with payment ID - {}", paymentId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Put(value = "/v4/consents/{consentId}", consumes = {"application/json", "application/jwt"}, produces = {"application/json", "*/*"})
    @RolesAllowed({"PAYMENTS_FULL_MANAGE"})
    public ResponsePaymentConsentV4 putConsentV4(@PathVariable("consentId") String consentId, @Body @Valid UpdatePaymentConsent body, HttpRequest<?> request) {
        var requestMeta = BankLambdaUtils.getRequestMeta(request);
        String clientId = requestMeta.getClientId();
        LOG.info("Updating payment consent {} for v4", consentId);
        var response = paymentConsentService.updateConsentV4(consentId, clientId, body);
        BankLambdaUtils.decorateResponseSimpleMetaBrasilTimeZone(response::setLinks, response::setMeta, appBaseUrl + request.getPath());
        LOG.info("Updated payment consent {}", consentId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }
}
