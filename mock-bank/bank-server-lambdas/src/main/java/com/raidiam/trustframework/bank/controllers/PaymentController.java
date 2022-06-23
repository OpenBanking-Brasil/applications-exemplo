package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.services.PaymentsService;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RolesAllowed({"PAYMENTS_MANAGE", "PAYMENTS_FULL_MANAGE"})
@Controller("/open-banking/payments/v1")
public class PaymentController extends BaseBankController {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentsService paymentsService;

    PaymentController(PaymentsService paymentsService) {
        this.paymentsService = paymentsService;
    }

    @Post(value = "/consents", consumes = {"application/jwt"}, produces = {"application/jwt", "*/*"})
    @Status(HttpStatus.CREATED)
    public ResponsePaymentConsent createConsent(@Body @Valid CreatePaymentConsent body, HttpRequest<?> request) {
        LOG.info("Creating payment consent");
        var requestMeta = BankLambdaUtils.getRequestMeta(request);
        String clientId = requestMeta.getClientId();
        LOG.info("Creating new payment consent for client {}", clientId);
        BankLambdaUtils.logObject(mapper, body);

        String idempotencyKey = Optional.ofNullable(request.getHeaders().get("x-idempotency-key"))
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "No Idempotency header"));

        String jti = requestMeta.getJti();

        var response = paymentsService.createConsent(clientId, idempotencyKey, jti, body);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath() + "/" + response.getData().getConsentId(), 1);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get(value = "/consents/{consentId}", consumes = {"*/*"}, produces = {"application/jwt", "*/*"})
    public Object getConsent(@PathVariable("consentId") String consentId, HttpRequest<?> request) {
        LOG.info("Getting payment consent Id {}", consentId);
        var callerInfo = BankLambdaUtils.getRequestMeta(request);
        List<String> roles = callerInfo.getRoles();
        if(roles.contains("PAYMENTS_FULL_MANAGE")) {
            LOG.info("OP making call - return full response");
            var response = paymentsService.getConsentFull(consentId);
            BankLambdaUtils.logObject(mapper, response);
            return response;
        }
        LOG.info("External client making call - return partial response");
        var response = paymentsService.getConsent(consentId, callerInfo.getClientId());
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Put(value = "/consents/{consentId}", consumes = {"application/json", "application/jwt"}, produces = {"application/json", "*/*"})
    @RolesAllowed({"PAYMENTS_FULL_MANAGE"})
    public ResponsePaymentConsent putConsent(@PathVariable("consentId") String consentId, @Body @Valid UpdatePaymentConsent body, HttpRequest<?> request) {
        LOG.info("Updating payment consent {}", consentId);
        var response = paymentsService.updateConsent(consentId, body);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Updated payment consent {}", consentId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Post(value = "/pix/payments", consumes = {"application/jwt"}, produces = {"application/jwt", "*/*"})
    @Status(HttpStatus.CREATED)
    public ResponsePixPayment createPayment(@Body @Valid CreatePixPayment body, HttpRequest<?> request) {
        LOG.info("Creating payment");
        BankLambdaUtils.logObject(mapper, body);
        var callerInfo = BankLambdaUtils.getRequestMeta(request);

        String idempotencyKey = Optional.ofNullable(request.getHeaders().get("x-idempotency-key"))
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "No Idempotency header"));

        String jti = callerInfo.getJti();

        var response = paymentsService.createPayment(callerInfo.getConsentId(), idempotencyKey, jti, body);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath() + "/" + response.getData().getPaymentId(), 1);
        LOG.info("Created payment {}", response.getData().getPaymentId());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get(value = "/pix/payments/{paymentId}", consumes = {"*/*"}, produces = {"application/jwt", "*/*"})
    public ResponsePixPayment getPayment(@PathVariable("paymentId") String paymentId, HttpRequest<?> request) {
        LOG.info("Getting payment {}", paymentId);
        var response = paymentsService.getPayment(paymentId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Get payment response for payment id {}", response.getData().getPaymentId());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Put(value = "/pix/payments/{paymentId}", consumes = {"application/json", "application/jwt"}, produces = {"application/json", "*/*"})
    @RolesAllowed({"PAYMENTS_FULL_MANAGE"})
    public ResponsePixPayment putPayment(@PathVariable("paymentId") String paymentId, @Body @Valid UpdatePixPayment body, HttpRequest<?> request) {
        LOG.info("Updating payment {}", paymentId);
        var response = paymentsService.updatePayment(paymentId, body);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Updated payment {}", response.getData().getPaymentId());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Patch(value = "/consents/{consentId}", consumes = {"application/jwt"}, produces = {"application/jwt", "*/*"})
    public ResponsePaymentConsent patchConsent(@PathVariable("consentId") String consentId, @Body @Valid PatchPaymentsConsent body, HttpRequest<?> request){
        LOG.info("Patching payment consents {}", consentId);
        BankLambdaUtils.logObject(mapper, body);
        var response = paymentsService.patchConsent(consentId, body);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Patched payment consent {}", response.getData().getConsentId());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }
}
