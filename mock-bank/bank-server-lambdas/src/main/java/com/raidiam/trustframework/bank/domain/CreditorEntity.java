package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.EnumCreditorPersonType;
import com.raidiam.trustframework.mockbank.models.generated.Identification;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "creditors")
public class CreditorEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "creditor_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer id;

    @Column(name = "person_type")
    @Convert(converter = CreditorPersonTypeConverter.class)
    private EnumCreditorPersonType personType;

    @Column(name = "cpf_cnpj")
    private String cpfCnpj;

    @Column(name = "name")
    private String name;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_consent_reference_id", referencedColumnName = "reference_id", nullable = false, updatable = false)
    private PaymentConsentEntity paymentConsent;


    public Identification getIdentification() {
        return new Identification()
                .personType(personType.toString())
                .cpfCnpj(cpfCnpj)
                .name(name);
    }

    public static CreditorEntity from(Identification identification, PaymentConsentEntity paymentConsentEntity) {
        return Optional.ofNullable(identification)
                .map(i -> {
                    if (i.getPersonType() != null) {
                        var entity = new CreditorEntity();
                        entity.setPersonType(EnumCreditorPersonType.fromValue(i.getPersonType()));
                        entity.setCpfCnpj(i.getCpfCnpj());
                        entity.setName(i.getName());
                        entity.setPaymentConsent(paymentConsentEntity);
                        return entity;
                    }
                    return null;
                }).orElse(null);
    }


    @Converter
    static class CreditorPersonTypeConverter implements AttributeConverter<EnumCreditorPersonType, String> {

        @Override
        public String convertToDatabaseColumn(EnumCreditorPersonType attribute) {
            return Optional.ofNullable(attribute)
                    .map(EnumCreditorPersonType::toString)
                    .orElse(null);
        }

        @Override
        public EnumCreditorPersonType convertToEntityAttribute(String dbData) {
            return Optional.ofNullable(dbData)
                    .map(EnumCreditorPersonType::fromValue)
                    .orElse(null);
        }
    }
}
