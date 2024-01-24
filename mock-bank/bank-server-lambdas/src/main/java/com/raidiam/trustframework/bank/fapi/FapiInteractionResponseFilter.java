package com.raidiam.trustframework.bank.fapi;

import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.order.Ordered;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Filter("/**")
public class FapiInteractionResponseFilter implements HttpServerFilter {

    private static final Logger LOG = LoggerFactory.getLogger(FapiInteractionResponseFilter.class);
    private static final String X_FAPI_INTERACTION_ID = "x-fapi-interaction-id";
    private static final String INTERACTION_ID_VALIDATION_REGEX = "^([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-4[0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12})$";

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        return Publishers.map(chain.proceed(request), response -> {
            String interactionId = request.getHeaders().findFirst(X_FAPI_INTERACTION_ID).orElse(generateId());

            if(!interactionId.matches(INTERACTION_ID_VALIDATION_REGEX)){
                LOG.info("fapi interaction is invalid - generating a new one");
                interactionId = generateId();
            }

            LOG.info("Fapi interaction id: {}", interactionId);
            response.header(X_FAPI_INTERACTION_ID, interactionId);
            return response;
        });
    }

    private String generateId() {
        LOG.info("No fapi interaction id provided: generating one");
        return UUID.randomUUID().toString();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
