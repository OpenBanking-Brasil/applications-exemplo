package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.NationalityOtherDocument;
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
@Table(name = "personal_nationality_documents")
public class PersonalNationalityDocumentEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "type")
    private String type;

    @Column(name = "number")
    private String number;

    @Column(name = "expiration_date")
    private Date expirationDate;

    @Column(name = "issue_date")
    private Date issueDate;

    @Column(name = "country")
    private String country;

    @Column(name = "type_additional_info")
    private String typeAdditionalInfo;

    @Column(name = "personal_nationality_id")
    @Type(type = "pg-uuid")
    private UUID personalNationalityId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_nationality_id", referencedColumnName = "personal_nationality_id", insertable = false, nullable = false, updatable = false)
    private PersonalNationalityEntity nationality;

    public NationalityOtherDocument getDTO() {
        return new NationalityOtherDocument()
                .type(this.getType())
                .number(this.getNumber())
                .expirationDate(BankLambdaUtils.dateToLocalDate(this.getExpirationDate()))
                .issueDate(BankLambdaUtils.dateToLocalDate(this.getIssueDate()))
                .country(this.getCountry())
                .typeAdditionalInfo(this.getTypeAdditionalInfo());
    }
}
