package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.NationalityOtherDocument;
import com.raidiam.trustframework.mockbank.models.generated.NationalityOtherDocumentV2;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "personal_nationality_documents")
public class PersonalNationalityDocumentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "type")
    private String type;

    @Column(name = "number")
    private String number;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "country")
    private String country;

    @Column(name = "type_additional_info")
    private String typeAdditionalInfo;

    @NotNull
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_nationality_id", referencedColumnName = "personal_nationality_id", nullable = false, updatable = false)
    private PersonalNationalityEntity nationality;

    public NationalityOtherDocument getDTO() {
        return new NationalityOtherDocument()
                .type(this.getType())
                .number(this.getNumber())
                .expirationDate(this.getExpirationDate())
                .issueDate(this.getIssueDate())
                .country(this.getCountry())
                .typeAdditionalInfo(this.getTypeAdditionalInfo());
    }

    public NationalityOtherDocumentV2 getDTOV2() {
        return new NationalityOtherDocumentV2()
                .type(this.getType())
                .number(this.getNumber())
                .expirationDate(this.getExpirationDate())
                .issueDate(this.getIssueDate())
                .country(this.getCountry())
                .additionalInfo(this.getTypeAdditionalInfo());
    }

    public static PersonalNationalityDocumentEntity from(PersonalNationalityEntity nationality, NationalityOtherDocument personalNationalityDocuments) {
        var ntionalityDocumentsEntity = new PersonalNationalityDocumentEntity();
        ntionalityDocumentsEntity.setNationality(nationality);
        ntionalityDocumentsEntity.setType(personalNationalityDocuments.getType());
        ntionalityDocumentsEntity.setNumber(personalNationalityDocuments.getNumber());
        ntionalityDocumentsEntity.setExpirationDate(personalNationalityDocuments.getExpirationDate());
        ntionalityDocumentsEntity.setIssueDate(personalNationalityDocuments.getIssueDate());
        ntionalityDocumentsEntity.setCountry(personalNationalityDocuments.getCountry());
        ntionalityDocumentsEntity.setTypeAdditionalInfo(personalNationalityDocuments.getTypeAdditionalInfo());
        return ntionalityDocumentsEntity;
    }
}
