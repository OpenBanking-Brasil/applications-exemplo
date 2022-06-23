package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.EnumPersonalOtherDocumentType;
import com.raidiam.trustframework.mockbank.models.generated.PersonalOtherDocument;
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
@Table(name = "personal_other_documents")
public class PersonalOtherDocumentEntity extends BaseEntity {

    @Id
    @GeneratedValue
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
    private Date expirationDate;

    @Column(name = "personal_identifications_id")
    @Type(type = "pg-uuid")
    private UUID personalIdentificationsId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_identifications_id", referencedColumnName = "personal_identifications_id", insertable = false, nullable = false, updatable = false)
    private PersonalIdentificationsEntity identification;

    public PersonalOtherDocument getDTO() {
        return new PersonalOtherDocument()
                .type(EnumPersonalOtherDocumentType.fromValue(this.getType()))
                .typeAdditionalInfo(this.getTypeAdditionalInfo())
                .number(this.getNumber())
                .checkDigit(this.getCheckDigit())
                .additionalInfo(this.getAdditionalInfo())
                .expirationDate(BankLambdaUtils.dateToLocalDate(this.getExpirationDate()));
    }
}
