package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.BusinessContacts;
import com.raidiam.trustframework.mockbank.models.generated.BusinessIdentificationData;
import lombok.*;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "business_identifications")
public class BusinessIdentificationsEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Generated(GenerationTime.INSERT)
    @Type(type = "pg-uuid")
    @Column(name = "business_identifications_id", nullable = false, unique = true, updatable = false, insertable = false, columnDefinition = "uuid DEFAULT uuid_generate_v4()")
    private UUID businessIdentificationsId;

    @Column(name = "account_holder_id")
    @Type(type = "pg-uuid")
    private UUID accountHolderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "account_holder_id", referencedColumnName = "account_holder_id", insertable = false, nullable = false, updatable = false)
    private AccountHolderEntity accountHolder;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "trade_name")
    private String tradeName;

    @Column(name = "incorporation_date")
    private Date incorporationDate;

    @Column(name = "cnpj_number")
    private String cnpjNumber;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "businessIdentifications")
    private Set<BusinessOtherDocumentEntity> otherDocuments;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "businessIdentifications")
    private Set<BusinessPartyEntity> parties;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "businessIdentifications")
    private Set<BusinessPostalAddressEntity> addresses;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "businessIdentifications")
    private Set<BusinessPhoneEntity> phones;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "businessIdentifications")
    private Set<BusinessEmailEntity> emails;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "businessIdentifications")
    private Set<BusinessIdentificationsCompanyCnpjEntity> companyCnpjs;

    public BusinessIdentificationData getDTO () {
        return new BusinessIdentificationData()
                .updateDateTime(BankLambdaUtils.dateToOffsetDate(this.getUpdatedAt()))
                .businessId(this.getBusinessIdentificationsId().toString())
                .brandName(this.getBrandName())
                .companyName(this.getCompanyName())
                .tradeName(this.getTradeName())
                .incorporationDate(BankLambdaUtils.dateToOffsetDate(this.getIncorporationDate()))
                .cnpjNumber(this.getCnpjNumber())
                .companyCnpjNumber(this.getCompanyCnpjs().stream().map(BusinessIdentificationsCompanyCnpjEntity::getCompanyCnpj).collect(Collectors.toList()))
                .otherDocuments(this.getOtherDocuments().stream().map(BusinessOtherDocumentEntity::getDTO).collect(Collectors.toList()))
                .parties(this.getParties().stream().map(BusinessPartyEntity::getDTO).collect(Collectors.toList()))
                .contacts(new BusinessContacts()
                        .postalAddresses(this.getAddresses().stream().map(BusinessPostalAddressEntity::getDTO).collect(Collectors.toList()))
                        .phones(this.getPhones().stream().map(BusinessPhoneEntity::getDTO).collect(Collectors.toList()))
                        .emails(this.getEmails().stream().map(BusinessEmailEntity::getDTO).collect(Collectors.toList())));
    }
}
