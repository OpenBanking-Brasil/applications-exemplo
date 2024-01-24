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
import java.time.LocalDate;

@RolesAllowed({"RECURRING_PAYMENTS_MANAGE", "PAYMENTS_FULL_MANAGE"})
@Controller("/open-banking/automatic-payments")
public class AutomaticPaymentsController extends BaseBankController {
    private static final Logger LOG = LoggerFactory.getLogger(AutomaticPaymentsController.class);

    private final PaymentsService paymentsService;
    private final PaymentConsentService paymentConsentService;

    AutomaticPaymentsController(PaymentsService paymentsService, PaymentConsentService paymentConsentService) {
        this.paymentsService = paymentsService;
        this.paymentConsentService = paymentConsentService;
    }

    @Post(value = "/v1/recurring-consents", consumes = {"application/jwt"}, produces = {"application/jwt", "*/*"})
    @Status(HttpStatus.CREATED)
    @RequiredAuthenticationGrant(AuthenticationGrant.CLIENT_CREDENTIALS)
    @XFapiInteractionIdRequired
    public ResponseRecurringConsent createRecurringConsentV1(@Body @Valid CreateRecurringConsentV1 body, HttpRequest<?> request) {
        LOG.info("Creating recurring payment consent for v1");
        var requestMeta = BankLambdaUtils.getRequestMeta(request);
        String clientId = requestMeta.getClientId();
        LOG.info("Creating new recurring payment consent for client {}", clientId);
        BankLambdaUtils.logObject(mapper, body);

        String idempotencyKey = BankLambdaUtils.getIdempotencyKey(request);
        String jti = requestMeta.getJti();

        var response = paymentConsentService.createRecurringConsentV1(clientId, idempotencyKey, jti, body);
        BankLambdaUtils.decorateResponseSimpleMeta(response::setLinks, response::setMeta, appBaseUrl + request.getPath() + "/" + response.getData().getRecurringConsentId());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get(value = "/v1/recurring-consents/{recurringConsentId}", consumes = {"application/jwt"}, produces = {"application/jwt", "*/*"})
    @XFapiInteractionIdRequired
    public Object getRecurringConsents(@PathVariable("recurringConsentId") String recurringConsentId, HttpRequest<?> request) {
        LOG.info("Getting a recurring payment consent for v1");
        var requestMeta = BankLambdaUtils.getRequestMeta(request);
        if (BankLambdaUtils.isPaymentFullManageCaller(request)) {
            LOG.info("OP making call - return full response for V4");
            var response = paymentConsentService.getConsentFull(recurringConsentId);
            BankLambdaUtils.logObject(mapper, response);
            return response;
        }

        String clientId = requestMeta.getClientId();
        var response = paymentConsentService.getRecurringConsentsV1(recurringConsentId, clientId);
        BankLambdaUtils.decorateResponseSimpleMeta(response::setLinks, response::setMeta, appBaseUrl + request.getPath());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Patch(value = "/v1/recurring-consents/{recurringConsentId}", consumes = {"application/jwt"}, produces = {"application/jwt", "*/*"})
    @XFapiInteractionIdRequired
    public ResponseRecurringConsent getRecurringConsents(@Body @Valid PatchRecurringConsentV1 body,
                                                             @PathVariable("recurringConsentId") String recurringConsentId,
                                                             HttpRequest<?> request) {
        LOG.info("Patching a recurring payment consent for v1");
        BankLambdaUtils.logObject(mapper, body);

        var requestMeta = BankLambdaUtils.getRequestMeta(request);
        String clientId = requestMeta.getClientId();

        var response = paymentConsentService.patchRecurringConsentV1(recurringConsentId, clientId, body);
        BankLambdaUtils.decorateResponseSimpleMeta(response::setLinks, response::setMeta, appBaseUrl + request.getPath());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Post(value = "/v1/pix/recurring-payments", consumes = {"application/jwt"}, produces = {"application/jwt", "*/*"})
    @Status(HttpStatus.CREATED)
    @RequiredAuthenticationGrant(AuthenticationGrant.AUTHORISATION_CODE)
    @XFapiInteractionIdRequired
    public ResponseRecurringPixPayments createRecurringPixPaymentV1(@Body @Valid CreateRecurringPixPaymentV1 body, HttpRequest<?> request) {
        LOG.info("Creating recurring pix payment for v1");
        var requestMeta = BankLambdaUtils.getRequestMeta(request);
        String clientId = requestMeta.getClientId();
        LOG.info("Creating new recurring pix payment for client {}", clientId);
        BankLambdaUtils.logObject(mapper, body);

        String idempotencyKey = BankLambdaUtils.getIdempotencyKey(request);
        String jti = requestMeta.getJti();

        var response = paymentsService.createRecurringPixPaymentV1(requestMeta.getConsentId(), idempotencyKey, jti, clientId, body);
        BankLambdaUtils.decorateResponseSimpleMeta(response::setLinks, response::setMeta, appBaseUrl + request.getPath() + "/" + response.getData().getRecurringPaymentId());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get(value = "/v1/pix/recurring-payments", consumes = {"application/jwt"}, produces = {"application/jwt", "*/*"})
    @RequiredAuthenticationGrant(AuthenticationGrant.CLIENT_CREDENTIALS)
    @XFapiInteractionIdRequired
    public ResponseRecurringPixPaymentByConsent getRecurringPixPaymentByConsentIdV1(HttpRequest<?> request) {
        LOG.info("Getting recurring pix payment for v1 by consentID");
        var requestMeta = BankLambdaUtils.getRequestMeta(request);
        var clientId = requestMeta.getClientId();
        var consentId = BankLambdaUtils.getRecurringConsentIdFromRequest(request);
        var fromDate = bankLambdaUtils.getDateFromRequest(request, "startDate").orElse(LocalDate.now());
        var toDate = bankLambdaUtils.getDateFromRequest(request, "endDate").orElse(LocalDate.now());

        var response = paymentsService.getRecurringPixPaymentByConsentIdV1(consentId,  fromDate, toDate, clientId);
        BankLambdaUtils.decorateResponseSimpleMeta(response::setLinks, response::setMeta, appBaseUrl + request.getPath());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get(value = "/v1/pix/recurring-payments/{recurringPaymentId}", consumes = {"application/jwt"}, produces = {"application/jwt", "*/*"})
    @RequiredAuthenticationGrant(AuthenticationGrant.CLIENT_CREDENTIALS)
    @XFapiInteractionIdRequired
    public ResponseRecurringPixPayments getRecurringPixPaymentV1(@PathVariable("recurringPaymentId") String recurringPaymentId, HttpRequest<?> request) {
        LOG.info("Getting a recurring pix payment for v1");
        var requestMeta = BankLambdaUtils.getRequestMeta(request);
        String clientId = requestMeta.getClientId();

        var response = paymentsService.getRecurringPixPaymentV1(recurringPaymentId, clientId);
        BankLambdaUtils.decorateResponseSimpleMeta(response::setLinks, response::setMeta, appBaseUrl + request.getPath());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Patch(value = "/v1/pix/recurring-payments/{recurringPaymentId}", consumes = {"application/jwt"}, produces = {"application/jwt", "*/*"})
    @RequiredAuthenticationGrant(AuthenticationGrant.CLIENT_CREDENTIALS)
    @XFapiInteractionIdRequired
    public ResponseRecurringPixPayments patchRecurringPixPaymentV1(@Body @Valid RecurringPatchPixPayment body,
                                                         @PathVariable("recurringPaymentId") String recurringPaymentId,
                                                         HttpRequest<?> request) {
        LOG.info("Patching a recurring payment consent for v1");
        BankLambdaUtils.logObject(mapper, body);

        var requestMeta = BankLambdaUtils.getRequestMeta(request);
        var response = paymentsService.patchRecurringPixPaymentV1(requestMeta.getConsentId(), recurringPaymentId, body);
        BankLambdaUtils.decorateResponseSimpleMeta(response::setLinks, response::setMeta, appBaseUrl + request.getPath());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Put(value = "/v1/recurring-consents/{recurringConsentId}", consumes = {"application/json", "application/jwt"}, produces = {"application/json", "*/*"})
    @RolesAllowed({"PAYMENTS_FULL_MANAGE"})
    public ResponsePaymentConsent putRecurringConsentV1(@PathVariable("recurringConsentId") String recurringConsentId, @Body @Valid UpdatePaymentConsent body, HttpRequest<?> request) {
        LOG.info("Updating payment consent {} for v1", recurringConsentId);
        var response = paymentConsentService.updateRecurringConsentV1(recurringConsentId, body);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Updated payment consent {}", recurringConsentId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

}
