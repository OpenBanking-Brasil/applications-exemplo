package com.raidiam.trustframework.bank.fapi;

import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Filter(value = "/open-banking/consents/*/consents/*/extends", methods = HttpMethod.POST)
public class ConsentsExtendsPostRequestFilter implements HttpServerFilter {
    private static final Logger LOG = LoggerFactory.getLogger(ConsentsExtendsPostRequestFilter.class);
    public static final String CUSTOMER_IP_ADDRESS = "x-fapi-customer-ip-address";
    public static final String CUSTOMER_USER_AGENT = "x-customer-user-agent";


    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {

        LOG.info("Checking {}", CUSTOMER_IP_ADDRESS);
        if (!request.getHeaders().contains(CUSTOMER_IP_ADDRESS)) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, String.format("No %s in the request", CUSTOMER_IP_ADDRESS));
        }

        LOG.info("Checking {}", CUSTOMER_USER_AGENT);
        if (!request.getHeaders().contains(CUSTOMER_USER_AGENT)) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, String.format("No %s in the request", CUSTOMER_USER_AGENT));
        }

        LOG.info("{} and {} are present in the request", CUSTOMER_IP_ADDRESS, CUSTOMER_USER_AGENT);

        return chain.proceed(request);
    }

    @Override
    public int getOrder() {
        return ServerFilterPhase.SECURITY.after();
    }
}
