package com.raidiam.trustframework.bank.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.raidiam.trustframework.bank.exceptions.TrustframeworkException;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.function.aws.proxy.MicronautAwsProxyRequest;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.bind.binders.BodyArgumentBinder;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;
import io.micronaut.http.exceptions.HttpStatusException;

import java.text.ParseException;
import java.util.Optional;

public abstract class AbstractJwtBinder<T> implements TypedRequestArgumentBinder<T>, BodyArgumentBinder<T> {

    protected final ObjectMapper objectMapper;

    protected AbstractJwtBinder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ArgumentBinder.BindingResult<T> bind(ArgumentConversionContext<T> context, HttpRequest<?> source) {
        ArgumentBinder.BindingResult<T> bindingResult = deferredBinding(source, argumentType().getType());
        if(bindingResult != null) {
            return bindingResult;
        }
        Optional<MediaType> contentType = source.getContentType();
        if(!contentType.isPresent()) {
            return ArgumentBinder.BindingResult.EMPTY;
        }
        MediaType mediaType = contentType.get();
        Optional<String> bodyOpt = source.getBody(String.class);
        if(!bodyOpt.isPresent()) {
            return ArgumentBinder.BindingResult.UNSATISFIED;
        }
        String body = bodyOpt.get();
        if(mediaType.getExtension().equals("jwt")) {
            String unpacked = unpack(body);
            return bindFromJson(unpacked);
        }
        return ArgumentBinder.BindingResult.EMPTY;
    }


    @Override
    public Class getAnnotationType() {
        return Body.class;
    }

    protected BindingResult<T> bindFromJson(String body) {
        try {
            T t = doBinding(body);
            return bindingResult(t);
        } catch (JsonProcessingException e) {
            throw new TrustframeworkException("Unparseable json", e);
        }
    }

    protected abstract T doBinding(String body) throws JsonProcessingException;

    private ArgumentBinder.BindingResult<T> deferredBinding(HttpRequest<?> source, Class<T> clazz) {
        if(!(source instanceof MicronautAwsProxyRequest)) {
            return null;
        }
        MicronautAwsProxyRequest<?> request = (MicronautAwsProxyRequest<?>) source;
        if(request.isBodyDecoded()) {
            Optional<T> body = request.getBody(clazz);
            return bindingResult(body.orElseThrow(() -> new TrustframeworkException("No body present")));
        }
        return null;
    }

    private String unpack(String body) {
        try {
            SignedJWT jwt = SignedJWT.parse(body);
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Object json = Optional.ofNullable(claims.getClaim("data")).orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "JWT does not appear to be a payment consent"));
            return String.valueOf(json);
        } catch (ParseException e) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unparseable payload");
        }
    }

    protected ArgumentBinder.BindingResult<T> bindingResult(T body) {
        return () -> Optional.of(body);
    }
}
