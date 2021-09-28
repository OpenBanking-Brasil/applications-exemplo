package com.raidiam.trustframework.bank.fapi;

import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.health.HealthStatus;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.FilterChain;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.management.health.indicator.HealthResult;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

@Filter("/**")
public class FapiInteractionFilter implements HttpServerFilter {

    private static final Logger LOG = LoggerFactory.getLogger(FapiInteractionFilter.class);
    private static final String X_FAPI_INTERACTION_ID = "x-fapi-interaction-id";

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        return Publishers.map(chain.proceed(request), response -> {
            String interactionId = request.getHeaders().findFirst(X_FAPI_INTERACTION_ID).orElseGet(this::generateId);
            LOG.info("Fapi interaction id: {}", interactionId);
            response.header(X_FAPI_INTERACTION_ID, interactionId);
            return response;
        });
    }

    private String generateId() {
        LOG.info("No fapi interaction id provided: generating one");
        return UUID.randomUUID().toString();
    }

}
