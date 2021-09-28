package com.raidiam.trustframework.bank.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RolesAllowed({"PAYMENTS_MANAGE", "PAYMENTS_FULL_MANAGE"})
@Controller("/payments/v1")
public class PaymentController {
    PaymentsService paymentsService;

    private static final Logger LOG = LoggerFactory.getLogger(PaymentController.class);

    @Inject
    private ObjectMapper mapper;

    PaymentController(PaymentsService paymentsService) {
        this.paymentsService = paymentsService;
    }

    @Post(value = "/consents", consumes = {"application/json", "application/jwt"}, produces = {"application/json", "application/jwt"})
    @Status(HttpStatus.CREATED)
    public ResponsePaymentConsent createConsent(@Body @Valid CreatePaymentConsent body, HttpRequest<?> request) {
        LOG.info("In payment controller");
        var callerInfo = BankLambdaUtils.getCallerInfo(request);
        String clientId = callerInfo.getClientId();
        LOG.info("Creating new payment consent for client {}", clientId);
        try {
            LOG.info(mapper.writeValueAsString(body));
        } catch (JsonProcessingException e) {
            LOG.error("Not JSON", e);
        }

        String idempotencyKey = Optional.ofNullable(request.getHeaders().get("x-idempotency-key"))
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "No Idempotency header"));

        return paymentsService.createConsent(body, clientId, idempotencyKey);
    }

    @Get(value = "/consents/{consentId}", produces = {"application/json", "application/jwt"})
    public Object getConsent(@PathVariable("consentId") String consentId, HttpRequest<?> request) {
        var callerInfo = BankLambdaUtils.getCallerInfo(request);
        List<String> roles = callerInfo.getRoles();
        if(roles.contains("PAYMENTS_FULL_MANAGE")) {
            LOG.info("OP making call - return full response");
            return paymentsService.getConsentFull(consentId, callerInfo.getClientId());
        }
        LOG.info("External client making call - return partial response");
        return paymentsService.getConsent(consentId, callerInfo.getClientId());
    }

    @Put(value = "/consents/{consentId}", consumes = {"application/json", "application/jwt"}, produces = {"application/json", "application/jwt"})
    @RolesAllowed({"PAYMENTS_FULL_MANAGE"})
    public ResponsePaymentConsent putConsent(@PathVariable("consentId") String consentId, @Body @Valid UpdatePaymentConsent body) {
        LOG.info("Updating consent {}", consentId);
        return paymentsService.updateConsent(consentId, body);
    }

    @Post(value = "/pix/payments", consumes = {"application/json", "application/jwt"}, produces = {"application/json", "application/jwt"})
    @Status(HttpStatus.CREATED)
    public ResponsePixPayment createPayment(@Body @Valid CreatePixPayment body, HttpRequest<?> request) {
        var callerInfo = BankLambdaUtils.getCallerInfo(request);

        String idempotencyKey = Optional.ofNullable(request.getHeaders().get("x-idempotency-key"))
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "No Idempotency header"));

        return paymentsService.createPayment(body, callerInfo.getConsentId(), idempotencyKey);
    }

    @Get(value = "/pix/payments/{paymentId}", produces = {"application/json", "application/jwt"})
    public ResponsePixPayment getPayment(@PathVariable("paymentId") String paymentId) {
        return paymentsService.getPayment(paymentId);
    }

    @Put(value = "/pix/payments/{paymentId}", consumes = {"application/json", "application/jwt"}, produces = {"application/json", "application/jwt"})
    @RolesAllowed({"PAYMENTS_FULL_MANAGE"})
    public ResponsePixPayment putPayment(@PathVariable("paymentId") String paymentId, @Body @Valid UpdatePixPayment body) {
        LOG.info("Updating payment {}", paymentId);
        return paymentsService.updatePayment(paymentId, body);
    }
}
