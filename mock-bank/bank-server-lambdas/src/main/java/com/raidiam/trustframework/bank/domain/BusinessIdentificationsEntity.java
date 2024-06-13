package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.BusinessContacts;
import com.raidiam.trustframework.mockbank.models.generated.BusinessContactsV2;
import com.raidiam.trustframework.mockbank.models.generated.BusinessIdentificationData;
import com.raidiam.trustframework.mockbank.models.generated.BusinessIdentificationDataV2;
import com.raidiam.trustframework.mockbank.models.generated.CreateBusinessIdentificationData;
import com.raidiam.trustframework.mockbank.models.generated.EditedBusinessIdentificationData;
import com.raidiam.trustframework.mockbank.models.generated.ResponseBusinessIdentification;
import com.raidiam.trustframework.mockbank.models.generated.ResponseBusinessIdentificationData;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "business_identifications")
public class BusinessIdentificationsEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Type(type = "pg-uuid")
    @Column(name = "business_identifications_id", nullable = false, unique = true, updatable = false, insertable = false, columnDefinition = "uuid NOT NULL DEFAULT uuid_generate_v4()")
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
    private LocalDate incorporationDate;

    @Column(name = "cnpj_number")
    private String cnpjNumber;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "businessIdentifications")
    private Set<BusinessOtherDocumentEntity> otherDocuments = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "businessIdentifications")
    private Set<BusinessPartyEntity> parties = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "businessIdentifications")
    private Set<BusinessPostalAddressEntity> addresses = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "businessIdentifications")
    private Set<BusinessPhoneEntity> phones = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "businessIdentifications")
    private Set<BusinessEmailEntity> emails = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "businessIdentifications")
    private Set<BusinessIdentificationsCompanyCnpjEntity> companyCnpjs = new HashSet<>();

    public BusinessIdentificationData getDTO() {
        return new BusinessIdentificationData()
                .updateDateTime(BankLambdaUtils.dateToOffsetDate(this.getUpdatedAt()))
                .businessId(this.getBusinessIdentificationsId().toString())
                .brandName(this.getBrandName())
                .companyName(this.getCompanyName())
                .tradeName(this.getTradeName())
                .incorporationDate(BankLambdaUtils.localDateToOffsetDate(this.getIncorporationDate()))
                .cnpjNumber(this.getCnpjNumber())
                .companyCnpjNumber(this.getCompanyCnpjs().stream().map(BusinessIdentificationsCompanyCnpjEntity::getCompanyCnpj).collect(Collectors.toList()))
                .otherDocuments(this.getOtherDocuments().stream().map(BusinessOtherDocumentEntity::getDTO).collect(Collectors.toList()))
                .parties(this.getParties().stream().map(BusinessPartyEntity::getDTO).collect(Collectors.toList()))
                .contacts(new BusinessContacts()
                        .postalAddresses(this.getAddresses().stream().map(BusinessPostalAddressEntity::getDTO).collect(Collectors.toList()))
                        .phones(this.getPhones().stream().map(BusinessPhoneEntity::getDTO).collect(Collectors.toList()))
                        .emails(this.getEmails().stream().map(BusinessEmailEntity::getDTO).collect(Collectors.toList())));
    }

    public BusinessIdentificationDataV2 getDtoV2() {
        return new BusinessIdentificationDataV2()
                .updateDateTime(BankLambdaUtils.dateToOffsetDate(this.getUpdatedAt()))
                .businessId(this.getBusinessIdentificationsId().toString())
                .brandName(this.getBrandName())
                .companyName(this.getCompanyName())
                .tradeName(this.getTradeName())
                .incorporationDate(BankLambdaUtils.localDateToOffsetDate(this.getIncorporationDate()))
                .cnpjNumber(this.getCnpjNumber())
                .companiesCnpj(this.getCompanyCnpjs().stream().map(BusinessIdentificationsCompanyCnpjEntity::getCompanyCnpj).collect(Collectors.toList()))
                .otherDocuments(this.getOtherDocuments().stream().map(BusinessOtherDocumentEntity::getDTOV2).collect(Collectors.toList()))
                .parties(this.getParties().stream().map(BusinessPartyEntity::getDTOV2).collect(Collectors.toList()))
                .contacts(new BusinessContactsV2()
                        .postalAddresses(this.getAddresses().stream().map(BusinessPostalAddressEntity::getDTOV2).collect(Collectors.toList()))
                        .phones(this.getPhones().stream().map(BusinessPhoneEntity::getDTOV2).collect(Collectors.toList()))
                        .emails(this.getEmails().stream().map(BusinessEmailEntity::getDTOV2).collect(Collectors.toList())));
    }

    public static BusinessIdentificationsEntity from(CreateBusinessIdentificationData identificationsDto, UUID accountHolderId) {
        var businessEntity = new BusinessIdentificationsEntity();
        businessEntity.setAccountHolderId(accountHolderId);
        businessEntity.setBrandName(identificationsDto.getBrandName());
        businessEntity.setCompanyName(identificationsDto.getCompanyName());
        businessEntity.setTradeName(identificationsDto.getTradeName());
        businessEntity.setIncorporationDate(identificationsDto.getIncorporationDate().toLocalDate());
        businessEntity.setCnpjNumber(identificationsDto.getCnpjNumber());

        var cnpjList = identificationsDto.getCompanyCnpjNumber().stream()
                .map(c -> BusinessIdentificationsCompanyCnpjEntity.from(businessEntity, c))
                .collect(Collectors.toSet());
        businessEntity.setCompanyCnpjs(cnpjList);

        var documentsList = identificationsDto.getOtherDocuments().stream()
                .map(o -> BusinessOtherDocumentEntity.from(businessEntity, o))
                .collect(Collectors.toSet());
        businessEntity.setOtherDocuments(documentsList);

        var partiesList = identificationsDto.getParties().stream()
                .map(p -> BusinessPartyEntity.from(businessEntity, p))
                .collect(Collectors.toSet());
        businessEntity.setParties(partiesList);

        var postalList = identificationsDto.getContacts().getPostalAddresses().stream()
                .map(p -> BusinessPostalAddressEntity.from(businessEntity, p))
                .collect(Collectors.toSet());
        businessEntity.setAddresses(postalList);

        var phonesList = identificationsDto.getContacts().getPhones().stream()
                .map(p -> BusinessPhoneEntity.from(businessEntity, p))
                .collect(Collectors.toSet());
        businessEntity.setPhones(phonesList);

        var emailsList = identificationsDto.getContacts().getEmails().stream()
                .map(e -> BusinessEmailEntity.from(businessEntity, e))
                .collect(Collectors.toSet());
        businessEntity.setEmails(emailsList);

        return businessEntity;
    }

    public ResponseBusinessIdentification getResponseAdminBusinessIdentifications() {
        return new ResponseBusinessIdentification().data(new ResponseBusinessIdentificationData()
                .businessIdentificationsId(this.getBusinessIdentificationsId())
                .brandName(this.brandName)
                .companyName(this.companyName)
                .tradeName(this.tradeName)
                .incorporationDate(BankLambdaUtils.localDateToOffsetDate(this.incorporationDate))
                .cnpjNumber(this.cnpjNumber)
                .companyCnpjNumber(this.companyCnpjs != null ? this.companyCnpjs.stream().map(BusinessIdentificationsCompanyCnpjEntity::getDto).collect(Collectors.toList()) : null)
                .otherDocuments(this.otherDocuments != null ? this.otherDocuments.stream().map(BusinessOtherDocumentEntity::getDTO).collect(Collectors.toList()) : null)
                .parties(this.parties != null ? this.parties.stream().map(BusinessPartyEntity::getDTO).collect(Collectors.toList()) : null)
                .contacts(getContactsDto()));
    }

    public BusinessIdentificationsEntity update(EditedBusinessIdentificationData identificationsDto) {
        this.brandName = identificationsDto.getBrandName();
        this.companyName = identificationsDto.getCompanyName();
        this.tradeName = identificationsDto.getTradeName();
        this.incorporationDate = identificationsDto.getIncorporationDate().toLocalDate();
        this.cnpjNumber = identificationsDto.getCnpjNumber();

        var cnpjList = identificationsDto.getCompanyCnpjNumber().stream()
                .map(c -> BusinessIdentificationsCompanyCnpjEntity.from(this, c))
                .collect(Collectors.toSet());
        this.companyCnpjs.clear();
        this.companyCnpjs.addAll(cnpjList);

        var documentsList = identificationsDto.getOtherDocuments().stream()
                .map(o -> BusinessOtherDocumentEntity.from(this, o))
                .collect(Collectors.toSet());
        this.otherDocuments.clear();
        this.otherDocuments.addAll(documentsList);

        var partiesList = identificationsDto.getParties().stream()
                .map(p -> BusinessPartyEntity.from(this, p))
                .collect(Collectors.toSet());
        this.parties.clear();
        this.parties.addAll(partiesList);

        var postalList = identificationsDto.getContacts().getPostalAddresses().stream()
                .map(p -> BusinessPostalAddressEntity.from(this, p))
                .collect(Collectors.toSet());
        this.addresses.clear();
        this.addresses.addAll(postalList);

        var phonesList = identificationsDto.getContacts().getPhones().stream()
                .map(p -> BusinessPhoneEntity.from(this, p))
                .collect(Collectors.toSet());
        this.phones.clear();
        this.phones.addAll(phonesList);

        var emailsList = identificationsDto.getContacts().getEmails().stream()
                .map(e -> BusinessEmailEntity.from(this, e))
                .collect(Collectors.toSet());
        this.emails.clear();
        this.emails.addAll(emailsList);

        return this;
    }

    public BusinessContacts getContactsDto() {
        BusinessContacts contacts = new BusinessContacts();
        contacts.setPostalAddresses(this.addresses != null ? this.addresses.stream().map(BusinessPostalAddressEntity::getDTO).collect(Collectors.toList()) : null);
        contacts.setPhones(this.phones != null ? this.phones.stream().map(BusinessPhoneEntity::getDTO).collect(Collectors.toList()) : null);
        contacts.setEmails(this.emails != null ? this.emails.stream().map(BusinessEmailEntity::getDTO).collect(Collectors.toList()) : null);
        return contacts;
    }
}
