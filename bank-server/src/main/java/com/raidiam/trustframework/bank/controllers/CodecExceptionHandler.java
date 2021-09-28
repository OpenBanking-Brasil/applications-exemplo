package com.raidiam.trustframework.bank.controllers;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.http.codec.CodecException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

@Singleton
@Requires(classes = {ExceptionHandler.class})
public class CodecExceptionHandler implements ExceptionHandler<CodecException, HttpResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(CodecExceptionHandler.class);

    @Override
    public HttpResponse handle(HttpRequest request, CodecException exception) {
        LOG.error("JSON parsing error", exception);
        return HttpResponse.badRequest("JSON parsing error: " + exception.getMessage());
    }

}