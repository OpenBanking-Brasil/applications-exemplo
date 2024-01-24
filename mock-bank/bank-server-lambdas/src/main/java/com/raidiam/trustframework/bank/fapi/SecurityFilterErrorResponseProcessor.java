package com.raidiam.trustframework.bank.fapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raidiam.trustframework.mockbank.models.generated.Meta;
import com.raidiam.trustframework.mockbank.models.generated.ResponseError;
import com.raidiam.trustframework.mockbank.models.generated.ResponseErrorErrors;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import lombok.SneakyThrows;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.List;

@Filter("/**")
public class SecurityFilterErrorResponseProcessor implements HttpServerFilter {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityFilterErrorResponseProcessor.class);
    private final ObjectMapper objectMapper;
    private static final List<HttpStatus> ERROR_RESPONSE_LIST = List.of(
            HttpStatus.UNAUTHORIZED,
            HttpStatus.FORBIDDEN
    );

    public SecurityFilterErrorResponseProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        return Publishers.map(chain.proceed(request), response -> {
            if (response.getBody().isEmpty() && ERROR_RESPONSE_LIST.contains(response.getStatus())){
                LOG.info("Generating error response for request with incorrect scopes for endpoint: {} Code: {}", request.getPath(), response.getStatus());
                return generateError(response);
            }
            return response;
        });    }

    @SneakyThrows
    private MutableHttpResponse<?> generateError(MutableHttpResponse<?> response) {
        final ResponseError errorResponse = new ResponseError();
        final ResponseErrorErrors error = new ResponseErrorErrors()
                .code(String.valueOf(response.getStatus().getCode()))
                .title(response.getStatus().toString())
                .detail(response.getStatus().toString());

        errorResponse.addErrorsItem(error);

        errorResponse.meta(new Meta()
                .totalPages(1)
                .totalRecords(1)
                .requestDateTime(OffsetDateTime.now())
        );

        response.getHeaders().remove(HttpHeaders.CONTENT_TYPE);
        response.contentType(MediaType.APPLICATION_JSON_TYPE);

        String json = objectMapper.writeValueAsString(errorResponse);

        return response.body(json);

    }
}
