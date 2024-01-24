package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.mockbank.models.generated.ConsentAuthorization;
import com.raidiam.trustframework.mockbank.models.generated.ConsentAuthorizationData;
import com.raidiam.trustframework.mockbank.models.generated.EnrollmentFidoRegistration;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class FidoService extends BaseBankService {

    @Inject
    private EnrollmentService enrollmentService;

    private static final Logger LOG = LoggerFactory.getLogger(FidoService.class);

    public void createFidoRegistration(String enrollmentId, EnrollmentFidoRegistration request) {
        try {
            enrollmentService.createFidoRegistration(enrollmentId, request);
        } catch (HttpStatusException e) {
            LOG.info("Caught exception - rejecting enrollment");
            enrollmentService.rejectEnrollment(enrollmentId);
            throw e;
        }
    }

    public void createFidoAuthorisation(String consentId, ConsentAuthorization request) {
        String enrollmentId = Optional.ofNullable(request.getData())
                .map(ConsentAuthorizationData::getEnrollmentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "Could not find enrollmentId"));
        try {
            enrollmentService.createFidoAuthorisation(consentId, enrollmentId, request);
        } catch (HttpStatusException e) {
            LOG.info("Caught exception - rejecting enrollment");
            enrollmentService.rejectEnrollment(enrollmentId);
            throw e;
        }

    }
}
