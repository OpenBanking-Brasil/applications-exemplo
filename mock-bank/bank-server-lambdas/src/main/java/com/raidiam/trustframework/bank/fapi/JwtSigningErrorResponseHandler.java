package com.raidiam.trustframework.bank.fapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.raidiam.trustframework.bank.jwt.JwtMediaType;
import com.raidiam.trustframework.bank.utils.JwtSigner;
import com.raidiam.trustframework.mockbank.models.generated.Meta;
import com.raidiam.trustframework.mockbank.models.generated.ResponseError;
import com.raidiam.trustframework.mockbank.models.generated.ResponseErrorErrors;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import io.micronaut.http.server.exceptions.response.HateoasErrorResponseProcessor;
import lombok.SneakyThrows;

import javax.inject.Singleton;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

@Produces
@Singleton
@Requires(classes = {ErrorResponseProcessor.class})
public class JwtSigningErrorResponseHandler implements ErrorResponseProcessor<Object> {

    private final ObjectMapper objectMapper;
    private final JwtSigner jwtSigner;
    private final String mockBankIssuer;
    private final HateoasErrorResponseProcessor delegate;

    public JwtSigningErrorResponseHandler(HateoasErrorResponseProcessor delegate, JwtSigner jwtSigner,
                                          ObjectMapper objectMapper,
                                          @Value("${trustframework.issuer}")  String mockBankIssuer) {
        this.delegate = delegate;
        this.jwtSigner = jwtSigner;
        this.objectMapper = objectMapper;
        this.mockBankIssuer = mockBankIssuer;
    }

    @SneakyThrows
    @Override
    public MutableHttpResponse<Object> processResponse(ErrorContext errorContext, MutableHttpResponse<?> response) {

        HttpRequest<?> request = errorContext.getRequest();
        if(request.getPath().startsWith("/open-banking/payments")) {
            if (response.status() == HttpStatus.BAD_REQUEST || response.status() == HttpStatus.FORBIDDEN){
                final ResponseError error = new ResponseError();
                errorContext.getErrors().forEach(e -> error.addErrorsItem(generateResponseError(e.getMessage())));

                error.meta(new Meta()
                        .totalPages(1)
                        .totalRecords(1)
                        .requestDateTime(OffsetDateTime.now())
                );
                String json = objectMapper.writeValueAsString(error);
                response.getHeaders().remove(HttpHeaders.CONTENT_TYPE);
                response.contentType(MediaType.APPLICATION_JSON_TYPE);
                return response.body(json);
            }
            return signedError(errorContext, response);
        }

        MutableHttpResponse<JsonError> jsonErrorMutableHttpResponse = delegate.processResponse(errorContext, response);

        response.contentType(MediaType.APPLICATION_JSON_TYPE);
        return response.body(jsonErrorMutableHttpResponse.body());

    }

    private MutableHttpResponse<Object> signedError(ErrorContext errorContext, MutableHttpResponse<?> response) {
        final ResponseError error = new ResponseError();
        errorContext.getErrors().stream()
                .forEach( e -> {
                    error.addErrorsItem(
                        generateResponseError(e.getMessage())
                    );
                });

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
        switch (code){
            case "PAGAMENTO_DIVERGENTE_DO_CONSENTIMENTO":
                return "Pagamento divergente do consentimento";
            case "FORMA_PGTO_INVALIDA":
                return "Forma de pagamento inválida.";
            case "DATA_PGTO_INVALIDA":
                return "Data de pagamento inválida.";
            case "DETALHE_PGTO_INVALIDO":
                return "Detalhe do pagamento inválido.";
            case "NAO_INFORMADO":
                return "Não informado.";
            default:
                return code;
        }
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
