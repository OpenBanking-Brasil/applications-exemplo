package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.EnumPartiesParticipationDocumentType;
import com.raidiam.trustframework.mockbank.models.generated.EnumPartiesParticipationDocumentTypeV2;
import com.raidiam.trustframework.mockbank.models.generated.PartiesParticipation;
import com.raidiam.trustframework.mockbank.models.generated.PartiesParticipationV2;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.Locale;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "business_parties")
public class BusinessPartyEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private LocalDate startDate;

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
    private LocalDate documentExpirationDate;

    @Column(name = "document_issue_date")
    private LocalDate documentIssueDate;

    @NotNull
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_identifications_id", referencedColumnName = "business_identifications_id", nullable = false, updatable = false)
    private BusinessIdentificationsEntity businessIdentifications;

    public PartiesParticipation getDTO() {
        return new PartiesParticipation()
                .personType(PartiesParticipation.PersonTypeEnum.fromValue(this.getPersonType()))
                .type(PartiesParticipation.TypeEnum.fromValue(this.getType()))
                .civilName(this.getCivilName())
                .socialName(this.getSocialName())
                .companyName(this.getCompanyName())
                .tradeName(this.getTradeName())
                .startDate(BankLambdaUtils.localDateToOffsetDate(this.getStartDate()))
                .shareholding(this.getShareholding())
                .documentType(EnumPartiesParticipationDocumentType.fromValue(this.getDocumentType()))
                .documentNumber(this.getDocumentNumber())
                .documentAdditionalInfo(this.getDocumentAdditionalInfo())
                .documentCountry(this.getDocumentCountry())
                .documentExpirationDate(this.getDocumentExpirationDate())
                .documentIssueDate(this.getDocumentIssueDate());
    }

    public PartiesParticipationV2 getDTOV2() {
        return new PartiesParticipationV2()
                .personType(PartiesParticipationV2.PersonTypeEnum.fromValue(this.getPersonType()))
                .type(PartiesParticipationV2.TypeEnum.fromValue(this.getType()))
                .civilName(this.getCivilName())
                .socialName(this.getSocialName())
                .companyName(this.getCompanyName())
                .tradeName(this.getTradeName())
                .startDate(BankLambdaUtils.localDateToOffsetDate(this.getStartDate()))
                .shareholding(BankLambdaUtils.formatRateV2(Double.parseDouble(this.getShareholding())))
                .documentType(EnumPartiesParticipationDocumentTypeV2.fromValue(this.getDocumentType()))
                .documentNumber(this.getDocumentNumber())
                .documentAdditionalInfo(this.getDocumentAdditionalInfo())
                .documentCountry(this.getDocumentCountry())
                .documentExpirationDate(this.getDocumentExpirationDate())
                .documentIssueDate(this.getDocumentIssueDate());
    }

    public static BusinessPartyEntity from(BusinessIdentificationsEntity business, PartiesParticipation parties) {
        var partiesEntity = new BusinessPartyEntity();
        partiesEntity.setBusinessIdentifications(business);
        partiesEntity.setPersonType(parties.getPersonType().toString());
        partiesEntity.setType(parties.getType().name());
        partiesEntity.setCivilName(parties.getCivilName());
        partiesEntity.setSocialName(parties.getSocialName());
        partiesEntity.setCompanyName(parties.getCompanyName());
        partiesEntity.setTradeName(parties.getTradeName());
        partiesEntity.setStartDate(parties.getStartDate().toLocalDate());
        partiesEntity.setShareholding(parties.getShareholding());
        partiesEntity.setDocumentType(parties.getDocumentType().name());
        partiesEntity.setDocumentNumber(parties.getDocumentNumber());
        partiesEntity.setDocumentAdditionalInfo(parties.getDocumentAdditionalInfo());
        partiesEntity.setDocumentCountry(parties.getDocumentCountry());
        partiesEntity.setDocumentExpirationDate(parties.getDocumentExpirationDate());
        partiesEntity.setDocumentIssueDate(parties.getDocumentIssueDate());
        return partiesEntity;
    }
}
