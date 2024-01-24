package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "enrollments_fido_registration_options")
public class EnrollmentFidoRegistrationOptionsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "enrollment_id")
    private String enrollmentId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", referencedColumnName = "enrollment_id", insertable = false, nullable = false, updatable = false)
    private EnrollmentEntity enrollmentEntity;

    @Column(name = "platform")
    private String platform;

    @Column(name = "rp")
    private String rp;

    @Column(name = "challenge")
    private String challenge;

    @Column(name = "timeout")
    private Integer timeout;

    @Column(name = "attestation")
    private String attestation;

    @Column(name = "extensions")
    private String extensions;

    public static EnrollmentFidoRegistrationOptionsEntity from(EnrollmentEntity enrollmentEntity, EnrollmentFidoOptionsInput req) {
        var data = req.getData();
        EnrollmentFidoRegistrationOptionsEntity entity = new EnrollmentFidoRegistrationOptionsEntity();

        entity.setEnrollmentId(enrollmentEntity.getEnrollmentId());
        entity.setEnrollmentEntity(enrollmentEntity);
        entity.setPlatform(data.getPlatform().name());
        entity.setRp(data.getRp());

        SecureRandom random = new SecureRandom();
        entity.setChallenge(Base64.getEncoder().encodeToString(random.generateSeed(10)));

        return entity;
    }


    public EnrollmentFidoRegistrationOptions getDTO() {
        return new EnrollmentFidoRegistrationOptions().data(new EnrollmentFidoRegistrationOptionsData()
                .enrollmentId(getEnrollmentId())
                .user(new FidoUser().id(enrollmentEntity.getAccountHolder().getDocumentIdentification())
                        .name(enrollmentEntity.getAccountHolder().getAccountHolderName())
                        .displayName(enrollmentEntity.getAccountHolder().getAccountHolderName()))
                .rp(new FidoRelyingParty().id(getRp()).name(getRp()))
                .challenge(getChallenge().getBytes())
                .pubKeyCredParams(List.of(new FidoPublicKeyCredentialCreationOptions()
                        .alg(-257) // Value registered by this specification for "RS256"
                        .type("public-key")))
        );
    }
}
