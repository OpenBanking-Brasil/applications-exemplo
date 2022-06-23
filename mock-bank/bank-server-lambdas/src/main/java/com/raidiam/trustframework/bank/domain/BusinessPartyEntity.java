package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.EnumPartiesParticipationDocumentType;
import com.raidiam.trustframework.mockbank.models.generated.PartiesParticipation;
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
@Table(name = "business_parties")
public class BusinessPartyEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "person_type")
    private String personType;

    @Column(name = "type")
    private String type;

    @Column(name = "civil_name")
    private String civilName;

    @Column(name = "social_name")
    private String socialName;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "trade_name")
    private String tradeName;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "shareholding")
    private String shareholding;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "document_number")
    private String documentNumber;

    @Column(name = "document_additional_info")
    private String documentAdditionalInfo;

    @Column(name = "document_country")
    private String documentCountry;

    @Column(name = "document_expiration_date")
    private Date documentExpirationDate;

    @Column(name = "document_issue_date")
    private Date documentIssueDate;

    @Column(name = "business_identifications_id")
    @Type(type = "pg-uuid")
    private UUID businessIdentificationsId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_identifications_id", referencedColumnName = "business_identifications_id", insertable = false, nullable = false, updatable = false)
    private BusinessIdentificationsEntity businessIdentifications;

    public PartiesParticipation getDTO() {
        return new PartiesParticipation()
                .personType(PartiesParticipation.PersonTypeEnum.fromValue(this.getPersonType()))
                .type(PartiesParticipation.TypeEnum.fromValue(this.getType()))
                .civilName(this.getCivilName())
                .socialName(this.getSocialName())
                .companyName(this.getCompanyName())
                .tradeName(this.getTradeName())
                .startDate(BankLambdaUtils.dateToOffsetDate(this.getStartDate()))
                .shareholding(this.getShareholding())
                .documentType(EnumPartiesParticipationDocumentType.fromValue(this.getDocumentType()))
                .documentNumber(this.getDocumentNumber())
                .documentAdditionalInfo(this.getDocumentAdditionalInfo())
                .documentCountry(this.getDocumentCountry())
                .documentExpirationDate(BankLambdaUtils.dateToLocalDate(this.getDocumentExpirationDate()))
                .documentIssueDate(BankLambdaUtils.dateToLocalDate(this.getDocumentIssueDate()));
    }
}
