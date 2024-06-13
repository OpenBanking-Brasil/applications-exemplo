package com.raidiam.trustframework.bank.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raidiam.trustframework.mockbank.models.generated.Meta;
import com.raidiam.trustframework.mockbank.models.generated.ResponseError;
import com.raidiam.trustframework.mockbank.models.generated.ResponseErrorErrors;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.OffsetDateTime;

@Singleton
@Requires(classes = {ExceptionHandler.class})
public class CodecExceptionHandler implements ExceptionHandler<CodecException, HttpResponse<?>> {

    private static final Logger LOG = LoggerFactory.getLogger(CodecExceptionHandler.class);

    private static final String CODE = "NAO_INFORMADO";
    private static final String TITLE = "Não informado.";

    @Inject
    protected ObjectMapper objectMapper;
    @Override
    public HttpResponse<?> handle(HttpRequest request, CodecException exception) {
        LOG.error("JSON parsing error", exception);
        final ResponseError error = new ResponseError();
        error.addErrorsItem(new ResponseErrorErrors()
                .code(CODE)
                .title(TITLE)
                .detail(exception.getMessage()))
                .meta(new Meta()
                        .totalPages(1)
                        .totalRecords(1)
                        .requestDateTime(OffsetDateTime.now()));
        String json = "";
        try {
            json = objectMapper.writeValueAsString(error);
        } catch (JsonProcessingException e) {
            LOG.error("Exception creating error response", e);
        }
        return HttpResponse.badRequest(json);
    }

}
