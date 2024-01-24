package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.EnrollmentFidoSignOptions;
import com.raidiam.trustframework.mockbank.models.generated.EnrollmentFidoSignOptionsData;
import com.raidiam.trustframework.mockbank.models.generated.EnrollmentFidoSignOptionsInput;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.security.SecureRandom;
import java.util.Base64;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "enrollments_fido_sign_options")
public class EnrollmentFidoSignOptionsEntity extends BaseEntity {

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

    @Column(name = "rp_id")
    private String rpId;

    @Column(name = "challenge")
    private String challenge;

    @Column(name = "timeout")
    private Integer timeout;

    @Column(name = "userVerification")
    private String userVerification;

    @Column(name = "extensions")
    private String extensions;

    @Column(name = "platform")
    private String platform;

    public static EnrollmentFidoSignOptionsEntity from(EnrollmentEntity enrollmentEntity, EnrollmentFidoSignOptionsInput req) {
        var data = req.getData();
        EnrollmentFidoSignOptionsEntity entity = new EnrollmentFidoSignOptionsEntity();

        entity.setEnrollmentId(enrollmentEntity.getEnrollmentId());
        entity.setEnrollmentEntity(enrollmentEntity);
        entity.setPlatform(data.getPlatform().name());
        entity.setRpId(data.getRp());

        SecureRandom random = new SecureRandom();
        entity.setChallenge(Base64.getEncoder().encodeToString(random.generateSeed(10)));

        return entity;
    }


    public EnrollmentFidoSignOptions getDTO() {
        return new EnrollmentFidoSignOptions().data(new EnrollmentFidoSignOptionsData()
                .challenge(getChallenge().getBytes())
                .rpId(getRpId())
        );
    }
}
