package com.raidiam.trustframework.bank.domain;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.security.interfaces.RSAPublicKey;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "fido_jwk")
public class FidoJwkEntity {

    private static class Base64URLConverter implements AttributeConverter<Base64URL, String> {

        @Override
        public String convertToDatabaseColumn(Base64URL attribute) {
            return attribute.toString();
        }

        @Override
        public Base64URL convertToEntityAttribute(String dbData) {
            return new Base64URL(dbData);
        }
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    @ToString.Include
    private Integer referenceId;

    @Column(name = "enrollment_id")
    private String enrollmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", referencedColumnName = "enrollment_id", insertable = false, nullable = false, updatable = false)
    private EnrollmentEntity enrollmentEntity;

    @Column(name = "kid", unique = true, nullable = false, updatable = false)
    @ToString.Include
    private String kid;

    @Column(name = "n")
    @Convert(converter = Base64URLConverter.class)
    private Base64URL n;

    @Column(name = "e")
    @Convert(converter = Base64URLConverter.class)
    private Base64URL e;


    public FidoJwkEntity(RSAKey jwk, EnrollmentEntity enrollmentEntity) {
        this.n = jwk.getModulus();
        this.e = jwk.getPublicExponent();
        this.kid = jwk.getKeyID();
        this.enrollmentEntity = enrollmentEntity;
        this.enrollmentId = enrollmentEntity.getEnrollmentId();
    }

    public JWK getJwk() {
        return new RSAKey.Builder(this.n, this.e)
                .keyID(this.kid)
                .build();
    }

    @SneakyThrows
    public RSAPublicKey getRsaPublicKey() {
        return getJwk().toRSAKey().toRSAPublicKey();
    }

}
