package com.raidiam.trustframework.bank.fapi;

import com.nimbusds.jose.util.Pair;
import com.raidiam.trustframework.bank.enums.ErrorCodesEnumV2;
import com.raidiam.trustframework.bank.utils.AnnotationsUtil;
import io.micronaut.context.ApplicationContext;
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

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Filter("/**")
public class FapiInteractionRequestFilter implements HttpServerFilter {

    private static final Logger LOG = LoggerFactory.getLogger(FapiInteractionRequestFilter.class);
    private static final String X_FAPI_INTERACTION_ID = "x-fapi-interaction-id";
    private static final String INTERACTION_ID_VALIDATION_REGEX = "^([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-4[0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12})$";
    private final List<Pair<HttpMethod, String>> requiredXFapiRegexes = new LinkedList<>();

    private final ApplicationContext applicationContext;

    @Inject
    public FapiInteractionRequestFilter(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    private void init() {
        AnnotationsUtil.performActionsOnControllerMethodByAnnotation(applicationContext, XFapiInteractionIdRequired.class, (fullPath, httpMethod, extractedAnnotation) -> {
            requiredXFapiRegexes.add(Pair.of(httpMethod, fullPath));
            LOG.info("Added required x-fapi-interaction-id header regex {} - {}", httpMethod, fullPath);
        });
    }

    private boolean isRequired(HttpRequest<?> request) {
        for (Pair<HttpMethod, String> requiredXFapiRule : requiredXFapiRegexes) {
            HttpMethod method = requiredXFapiRule.getLeft();
            String regex = requiredXFapiRule.getRight();
            String requestPath = request.getPath();
            if (request.getMethod() == method && requestPath.matches(regex)) {
                LOG.info("found matching pattern - {} - {} for path {}", method, regex, requestPath);
                return true;
            }
        }
        LOG.info("no matching patterns found");
        return false;
    }

    private void validate(String interactionId){
        LOG.info("Validating {} - {}", X_FAPI_INTERACTION_ID, interactionId);

        if (!interactionId.matches(INTERACTION_ID_VALIDATION_REGEX)) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, String.format("%s: x-fapi-interaction-id - %s is invalid", ErrorCodesEnumV2.PARAMETRO_INVALIDO, interactionId));
        }
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        Optional<String> interactionIdOptional = request.getHeaders().findFirst(X_FAPI_INTERACTION_ID);

        interactionIdOptional.ifPresent(this::validate);


        if (isRequired(request)) {
            LOG.info("Request path matched the required list - checking {} header", X_FAPI_INTERACTION_ID);
            interactionIdOptional.ifPresentOrElse(
                    interactionId -> LOG.info("Payment request has {} header - {}", X_FAPI_INTERACTION_ID, interactionId),
                    () -> {
                        throw new HttpStatusException(HttpStatus.BAD_REQUEST, "No x-fapi-interaction-id in the request");
                    }
            );
        } else {
            LOG.info("Request is not the payment, skipping {} header check", X_FAPI_INTERACTION_ID);
        }
        return chain.proceed(request);
    }

    @Override
    public int getOrder() {
        return ServerFilterPhase.SECURITY.after();
    }
}
