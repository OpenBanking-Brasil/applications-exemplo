package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.auth.AuthenticationGrant;
import com.raidiam.trustframework.bank.auth.RequiredAuthenticationGrant;
import com.raidiam.trustframework.bank.fapi.XFapiInteractionIdRequired;
import com.raidiam.trustframework.bank.services.EnrollmentService;
import com.raidiam.trustframework.bank.services.FidoService;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

@RolesAllowed({"PAYMENTS_MANAGE", "PAYMENTS_FULL_MANAGE"})
@Controller("/open-banking/enrollments")
public class EnrollmentController extends BaseBankController {

    private static final Logger LOG = LoggerFactory.getLogger(EnrollmentController.class);

    private final EnrollmentService enrollmentService;

    private final FidoService fidoService;

    public EnrollmentController(EnrollmentService enrollmentService, FidoService fidoService) {
        this.enrollmentService = enrollmentService;
        this.fidoService = fidoService;
    }

    @Post(value = "/v1/enrollments", consumes = {"application/jwt"}, produces = {"application/jwt", "*/*"})
    @XFapiInteractionIdRequired
    @Status(HttpStatus.CREATED)
    public ResponseCreateEnrollment createEnrollment(@Body @Valid CreateEnrollment body, HttpRequest<?> request) {
        LOG.info("Creating enrollment");
        var requestMeta = BankLambdaUtils.getRequestMeta(request);
        String clientId = requestMeta.getClientId();
        LOG.info("Creating new payment consent for client {}", clientId);
        BankLambdaUtils.logObject(mapper, body);

        String idempotencyKey = BankLambdaUtils.getIdempotencyKey(request);
        String jti = requestMeta.getJti();

        var response = enrollmentService.createEnrollment(clientId, idempotencyKey, jti, body);
        BankLambdaUtils.decorateResponseSimpleMeta(response::setLinks, response::setMeta, appBaseUrl + request.getPath() + "/" + response.getData().getEnrollmentId());
        BankLambdaUtils.logObject(mapper, response);
        return response;

    }

    @Get(value = "/v1/enrollments/{enrollmentId}", consumes = {"*/*"}, produces = {"application/jwt", "*/*"})
    @XFapiInteractionIdRequired
    public ResponseEnrollment getEnrollment(@PathVariable("enrollmentId") String enrollmentId, HttpRequest<?> request){
        LOG.info("Getting enrollmment Id {}", enrollmentId);
        var callerInfo = BankLambdaUtils.getRequestMeta(request);
        var clientId = callerInfo.getClientId();
        var isPaymentFullManage = callerInfo.getRoles().contains("PAYMENTS_FULL_MANAGE");
        var response = enrollmentService.getEnrollment(enrollmentId,clientId, isPaymentFullManage);
        BankLambdaUtils.decorateResponseSimpleMeta(response::setLinks, response::setMeta, appBaseUrl + request.getPath());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Patch(value = "/v1/enrollments/{enrollmentId}", consumes = {"application/jwt"}, produces = {"application/jwt", "*/*"})
    @XFapiInteractionIdRequired
    @Status(HttpStatus.NO_CONTENT)
    public HttpResponse<Object> patchEnrollment(@Body PatchEnrollment body,
                                                @PathVariable("enrollmentId") String enrollmentId) {
        LOG.info("Updating enrollment");
        BankLambdaUtils.logObject(mapper, body);

        enrollmentService.updateEnrollment(enrollmentId, body);
        return HttpResponse.noContent();

    }

    @Post(value = "/v1/enrollments/{enrollmentId}/risk-signals", consumes = {"application/jwt"}, produces = {"application/jwt", "*/*"})
    @XFapiInteractionIdRequired
    @Status(HttpStatus.NO_CONTENT)
    public HttpResponse<Object> createRiskSignals(@PathVariable("enrollmentId") String enrollmentId, @Body @Valid RiskSignals body) {
        LOG.info("Creating enrollment risk signal");
        BankLambdaUtils.logObject(mapper, body);

        enrollmentService.createRiskSignal(enrollmentId, body);
        return HttpResponse.noContent();
    }

    @Post(value = "/v1/enrollments/{enrollmentId}/fido-registration-options", consumes = {"application/jwt"}, produces = {"application/jwt", "*/*"})
    @XFapiInteractionIdRequired
    @Status(HttpStatus.CREATED)
    @RequiredAuthenticationGrant(AuthenticationGrant.AUTHORISATION_CODE)
    public EnrollmentFidoRegistrationOptions postFidoRegistrationOptions(@PathVariable("enrollmentId") String enrollmentId, @Body @Valid EnrollmentFidoOptionsInput body, HttpRequest<?> request) {
        LOG.info("Creating enrollment fido registration options");
        BankLambdaUtils.logObject(mapper, body);
        var certificateCn = bankLambdaUtils.getCertificateCNFromRequest(request);
        var response = enrollmentService.createFidoRegistrationOptions(enrollmentId, body, certificateCn);
        BankLambdaUtils.decorateResponseSimpleMeta(response::setLinks, response::setMeta, appBaseUrl + request.getPath() + "/" + response.getData().getEnrollmentId());
        BankLambdaUtils.logObject(mapper, response);

        return response;
    }

    @Put(value = "/v1/enrollments/{enrollmentId}", consumes = {"application/json", "application/jwt"}, produces = {"application/json", "*/*"})
    @XFapiInteractionIdRequired
    @RolesAllowed({"PAYMENTS_FULL_MANAGE"})
    public ResponseEnrollment putEnrollment(@PathVariable("enrollmentId") String enrollmentId, @Body @Valid UpdateEnrollment body, HttpRequest<?> request) {
        LOG.info("Updating enrollment {}", enrollmentId);
        var response = enrollmentService.updateEnrollment(enrollmentId, body);
        BankLambdaUtils.decorateResponseSimpleMeta(response::setLinks, response::setMeta, appBaseUrl + request.getPath());
        LOG.info("Updated enrollment {}", enrollmentId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }


    @Post(value = "/v1/enrollments/{enrollmentId}/fido-registration", consumes = {"application/jwt"}, produces = {"application/jwt", "*/*"})
    @XFapiInteractionIdRequired
    @Status(HttpStatus.NO_CONTENT)
    public HttpResponse<Object> postFidoRegistration(@PathVariable("enrollmentId") String enrollmentId, @Body @Valid EnrollmentFidoRegistration body) {
        LOG.info("Creating enrollment fido registration");
        BankLambdaUtils.logObject(mapper, body);
        fidoService.createFidoRegistration(enrollmentId, body);
        return HttpResponse.noContent();
    }


    @Post(value = "/v1/consents/{consentId}/authorise", consumes = {"application/jwt"}, produces = {"application/jwt", "*/*"})
    @RequiredAuthenticationGrant(AuthenticationGrant.AUTHORISATION_CODE)
    @XFapiInteractionIdRequired
    @Status(HttpStatus.NO_CONTENT)
    public HttpResponse<Object> postEnrollmentAuthorise(@PathVariable("consentId") String consentId, @Body @Valid ConsentAuthorization body) {
        LOG.info("Creating enrollment fido authorisation");
        BankLambdaUtils.logObject(mapper, body);
        fidoService.createFidoAuthorisation(consentId, body);
        return HttpResponse.noContent();
    }

    @Post(value = "/v1/enrollments/{enrollmentId}/fido-sign-options", consumes = {"application/jwt"}, produces = {"application/jwt", "*/*"})
    @XFapiInteractionIdRequired
    @Status(HttpStatus.CREATED)
    public EnrollmentFidoSignOptions postFidoSignOptions(@PathVariable("enrollmentId") String enrollmentId, @Body @Valid EnrollmentFidoSignOptionsInput body, HttpRequest<?> request) {
        LOG.info("Creating enrollment fido sign options");
        BankLambdaUtils.logObject(mapper, body);
        var certificateCn = bankLambdaUtils.getCertificateCNFromRequest(request);
        var response = enrollmentService.createFidoSignOptions(enrollmentId, body, certificateCn);
        BankLambdaUtils.decorateResponseSimpleMeta(response::setLinks, response::setMeta, appBaseUrl + request.getPath() + "/" + enrollmentId);
        BankLambdaUtils.logObject(mapper, response);

        return response;
    }
}
