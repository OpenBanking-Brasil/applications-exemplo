package com.raidiam.trustframework.bank.fapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.raidiam.trustframework.bank.enums.ErrorCodesEnumV1;
import com.raidiam.trustframework.bank.enums.ErrorCodesEnumV2;
import com.raidiam.trustframework.bank.jwt.JwtMediaType;
import com.raidiam.trustframework.bank.utils.JwtSigner;
import com.raidiam.trustframework.mockbank.models.generated.Meta;
import com.raidiam.trustframework.mockbank.models.generated.ResponseError;
import com.raidiam.trustframework.mockbank.models.generated.ResponseErrorErrors;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Produces
@Singleton
@Requires(classes = {ErrorResponseProcessor.class})
public class JwtSigningErrorResponseHandler implements ErrorResponseProcessor<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(JwtSigningErrorResponseHandler.class);

    private final ObjectMapper objectMapper;
    private final JwtSigner jwtSigner;
    private final String mockBankIssuer;

    public JwtSigningErrorResponseHandler(JwtSigner jwtSigner,
                                          ObjectMapper objectMapper,
                                          @Value("${trustframework.issuer}")  String mockBankIssuer) {
        this.jwtSigner = jwtSigner;
        this.objectMapper = objectMapper;
        this.mockBankIssuer = mockBankIssuer;
    }
    private static final List<HttpStatus> JSON_RESPONSE_LIST = List.of(
            HttpStatus.BAD_REQUEST,
            HttpStatus.FORBIDDEN,
            HttpStatus.TOO_MANY_REQUESTS,
            HttpStatus.NOT_FOUND,
            HttpStatus.UNAUTHORIZED,
            HttpStatus.METHOD_NOT_ALLOWED
    );

    @SneakyThrows
    @Override
    @NonNull
    public MutableHttpResponse<Object> processResponse(@NonNull ErrorContext errorContext, @NonNull MutableHttpResponse<?> response) {
        LOG.info("Processing error response");
        HttpRequest<?> request = errorContext.getRequest();
        if(request.getPath().startsWith("/open-banking/payments") || request.getPath().startsWith("/open-banking/enrollments") || request.getPath().startsWith("/open-banking/automatic-payments") ) {
            if (JSON_RESPONSE_LIST.contains(response.status())){
                LOG.info("Processing payments response as JSON due to response code");
                return buildJsonError(errorContext, response);
            }
            LOG.info("Processing payments response as JWT");
            return signedError(errorContext, response);
        }

        LOG.info("Processing non-payments response as Json");
        return buildJsonError(errorContext, response);
    }

    @SneakyThrows
    private MutableHttpResponse<Object> buildJsonError(ErrorContext errorContext, MutableHttpResponse<?> response) {
        final ResponseError error = new ResponseError();
        errorContext.getErrors().forEach(e -> error.addErrorsItem(generateResponseError(e.getMessage())));

        error.meta(new Meta()
                .requestDateTime(OffsetDateTime.now())
        );
        String json = objectMapper.writeValueAsString(error);
        response.getHeaders().remove(HttpHeaders.CONTENT_TYPE);
        response.contentType(MediaType.APPLICATION_JSON_TYPE);
        return response.body(json);
    }

    private MutableHttpResponse<Object> signedError(ErrorContext errorContext, MutableHttpResponse<?> response) {
        final ResponseError error = new ResponseError();
        errorContext.getErrors().stream()
                .forEach( e ->
                    error.addErrorsItem(
                        generateResponseError(e.getMessage())
                    )
                );

        error.meta(new Meta()
                .totalPages(1)
                .totalRecords(1)
                .requestDateTime(OffsetDateTime.now())
        );

        String jwtResponse = jsonToJwt(error, errorContext.getRequest().getAttribute("orgId").orElse("").toString());
        response.getHeaders().remove(HttpHeaders.CONTENT_TYPE);
        response.contentType(JwtMediaType.JWT_MEDIA_TYPE);

        response.contentType(JwtMediaType.JWT_MEDIA_TYPE);
        return response.body(jwtResponse);
    }

    private ResponseErrorErrors generateResponseError(String message){
        String code = generateCode(message);
        String title = generateTitle(code);
        String detail = generateDetail(message);

        return new ResponseErrorErrors()
                .code(code)
                .title(title)
                .detail(detail);
    }

    public String generateTitle(String code){
        var v1Opt = Arrays.stream(ErrorCodesEnumV1.values()).filter(error -> error.name().equals(code)).findAny().map(ErrorCodesEnumV1::getTitle);
        var v2Opt = Arrays.stream(ErrorCodesEnumV2.values()).filter(error -> error.name().equals(code)).findAny().map(ErrorCodesEnumV2::getTitle);
        return v1Opt.orElse(v2Opt.orElse(code));
    }

    public String generateDetail(String message){
        try {
            return message.split(": ")[1];
        } catch (ArrayIndexOutOfBoundsException e){
            return message.split(": ")[0];
        }
    }

    public String generateCode(String message){
        return message.split(":")[0];
    }

    private String jsonToJwt(Object body, String audience) {
        try {
            String json = objectMapper.writeValueAsString(body);
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issueTime(new Date())
                    .jwtID(UUID.randomUUID().toString())
                    .audience(audience)
                    .issuer(mockBankIssuer)
                    .build();
            return jwtSigner.sign(json, claims);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
