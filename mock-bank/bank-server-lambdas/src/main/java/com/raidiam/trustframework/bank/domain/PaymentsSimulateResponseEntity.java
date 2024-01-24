package com.raidiam.trustframework.bank.domain;

import io.micronaut.http.HttpStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@Audited
@Table(name = "payments_simulate_response")
public class PaymentsSimulateResponseEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", unique = true, nullable = false, insertable = false, updatable = false, columnDefinition = "uuid DEFAULT uuid_generate_v4()")
    public UUID id;

    @NotNull
    @Column(name = "client_id")
    public String userClientId;

    @Column(name = "payment_consent_id")
    public String paymentConsentId;

    @Column(name = "http_status")
    public HttpStatus httpStatus;

    @Column(name = "http_error_message")
    public String httpErrorMessage;

    @Column(name = "duration")
    public Integer duration;

    @NotNull
    @Column(name = "request_time")
    public LocalDateTime requestTime;

    @NotNull
    @Column(name = "request_end_time")
    public LocalDateTime requestEndTime;

}
