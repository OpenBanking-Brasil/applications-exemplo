package com.raidiam.trustframework.bank.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.server.exceptions.HttpStatusHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class BaseBankController {

    private static final Logger LOG = LoggerFactory.getLogger(BaseBankController.class);

    @Value("${mockbank.mockbankUrl}")
    protected String appBaseUrl;

    @Inject
    protected ObjectMapper mapper;

    @Inject
    protected BankLambdaUtils bankLambdaUtils;

    @Inject
    private HttpStatusHandler statusHandler;

    // Log out http status errors, so that we can see what happened, then pass to the default handler
    @Error(exception = HttpStatusException.class)
    public HttpResponse<?> error(HttpRequest<?> request, HttpStatusException exception) {
        LOG.info("Received exception {}, code {}, on request. Message - \"{}\"", exception.getClass(), exception.getStatus(), exception.getMessage());
        return statusHandler.handle(request, exception);
    }
}
