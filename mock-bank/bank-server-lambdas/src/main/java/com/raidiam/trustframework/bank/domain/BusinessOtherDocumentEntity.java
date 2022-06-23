package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.BusinessOtherDocument;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "business_other_documents")
public class BusinessOtherDocumentEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "type")
    private String type;

    @Column(name = "number")
    private String number;

    @Column(name = "country")
    private String country;

    @Column(name = "expiration_date")
    private Date expirationDate;

    @Column(name = "business_identifications_id")
    @Type(type = "pg-uuid")
    private UUID businessIdentificationsId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_identifications_id", referencedColumnName = "business_identifications_id", insertable = false, nullable = false, updatable = false)
    private BusinessIdentificationsEntity businessIdentifications;

    public BusinessOtherDocument getDTO() {
        return new BusinessOtherDocument()
                .type(this.getType())
                .number(this.getNumber())
                .country(this.getCountry())
                .expirationDate(BankLambdaUtils.dateToLocalDate(this.getExpirationDate()));
    }
}
