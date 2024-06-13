package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.EnumPersonalOtherDocumentType;
import com.raidiam.trustframework.mockbank.models.generated.EnumPersonalOtherDocumentTypeV2;
import com.raidiam.trustframework.mockbank.models.generated.PersonalOtherDocument;
import com.raidiam.trustframework.mockbank.models.generated.PersonalOtherDocumentV2;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "personal_other_documents")
public class PersonalOtherDocumentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "type")
    private String type;

    @Column(name = "type_additional_info")
    private String typeAdditionalInfo;

    @Column(name = "number")
    private String number;

    @Column(name = "check_digit")
    private String checkDigit;

    @Column(name = "additional_info")
    private String additionalInfo;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @NotNull
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_identifications_id", referencedColumnName = "personal_identifications_id", nullable = false, updatable = false)
    private PersonalIdentificationsEntity identification;

    public PersonalOtherDocument getDTO() {
        return new PersonalOtherDocument()
                .type(EnumPersonalOtherDocumentType.fromValue(this.getType()))
                .typeAdditionalInfo(this.getTypeAdditionalInfo())
                .number(this.getNumber())
                .checkDigit(this.getCheckDigit())
                .additionalInfo(this.getAdditionalInfo())
                .expirationDate(this.getExpirationDate());
    }

    public PersonalOtherDocumentV2 getDTOV2() {
        return new PersonalOtherDocumentV2()
                .type(EnumPersonalOtherDocumentTypeV2.fromValue(this.getType()))
                .typeAdditionalInfo(this.getTypeAdditionalInfo())
                .number(this.getNumber())
                .checkDigit(this.getCheckDigit())
                .additionalInfo(this.getAdditionalInfo())
                .expirationDate(this.getExpirationDate());
    }

    public static PersonalOtherDocumentEntity from(PersonalIdentificationsEntity identification, PersonalOtherDocument personalOtherDocuments) {
        var otherDocumentsEntity = new PersonalOtherDocumentEntity();
        otherDocumentsEntity.setIdentification(identification);
        otherDocumentsEntity.setType(personalOtherDocuments.getType().name());
        otherDocumentsEntity.setTypeAdditionalInfo(personalOtherDocuments.getTypeAdditionalInfo());
        otherDocumentsEntity.setNumber(personalOtherDocuments.getNumber());
        otherDocumentsEntity.setCheckDigit(personalOtherDocuments.getCheckDigit());
        otherDocumentsEntity.setAdditionalInfo(personalOtherDocuments.getAdditionalInfo());
        otherDocumentsEntity.setExpirationDate(personalOtherDocuments.getExpirationDate());
        return otherDocumentsEntity;
    }
}
