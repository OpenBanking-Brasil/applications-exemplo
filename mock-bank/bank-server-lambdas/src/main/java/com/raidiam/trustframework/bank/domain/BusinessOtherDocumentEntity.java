package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.BusinessOtherDocument;
import com.raidiam.trustframework.mockbank.models.generated.BusinessOtherDocumentV2;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "business_other_documents")
public class BusinessOtherDocumentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "type")
    private String type;

    @Column(name = "number")
    private String number;

    @Column(name = "country")
    private String country;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @NotNull
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_identifications_id", referencedColumnName = "business_identifications_id", nullable = false, updatable = false)
    private BusinessIdentificationsEntity businessIdentifications;

    public BusinessOtherDocument getDTO() {
        return new BusinessOtherDocument()
                .type(this.getType())
                .number(this.getNumber())
                .country(this.getCountry())
                .expirationDate(this.getExpirationDate());
    }

    public BusinessOtherDocumentV2 getDTOV2() {
        return new BusinessOtherDocumentV2()
                .type(this.getType())
                .number(this.getNumber())
                .country(this.getCountry())
                .expirationDate(this.getExpirationDate());
    }

    public static BusinessOtherDocumentEntity from(BusinessIdentificationsEntity business, BusinessOtherDocument otherDocuments) {
        var otherDocumentsEntity = new BusinessOtherDocumentEntity();
        otherDocumentsEntity.setBusinessIdentifications(business);
        otherDocumentsEntity.setType(otherDocuments.getType());
        otherDocumentsEntity.setNumber(otherDocuments.getNumber());
        otherDocumentsEntity.setCountry(otherDocuments.getCountry());
        otherDocumentsEntity.setExpirationDate(otherDocuments.getExpirationDate());
        return otherDocumentsEntity;
    }
}
