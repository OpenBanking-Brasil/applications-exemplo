package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
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
@Table(name = "personal_identifications")
public class PersonalIdentificationsEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Generated(GenerationTime.INSERT)
    @Type(type = "pg-uuid")
    @Column(name = "personal_identifications_id", unique = true, nullable = false, updatable = false, columnDefinition = "uuid DEFAULT uuid_generate_v4()")
    private UUID personalIdentificationsId;

    @Column(name = "account_holder_id")
    @Type(type = "pg-uuid")
    private UUID accountHolderId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_holder_id", referencedColumnName = "account_holder_id", insertable = false, nullable = false, updatable = false)
    private AccountHolderEntity accountHolder;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "civil_name")
    private String civilName;

    @Column(name = "social_name")
    private String socialName;

    @Column(name = "birth_date")
    private Date birthDate;

    @Column(name = "marital_status_code")
    private String maritalStatusCode;

    @Column(name = "marital_status_additional_info")
    private String maritalStatusAdditionalInfo;

    @Column(name = "sex")
    private String sex;

    @Column(name = "has_brazilian_nationality")
    private boolean hasBrazilianNationality;

    @Column(name = "cpf_number")
    private String cpfNumber;

    @Column(name = "passport_number")
    private String passportNumber;

    @Column(name = "passport_country")
    private String passportCountry;

    @Column(name = "passport_expiration_date")
    private Date passportExpirationDate;

    @Column(name = "passport_issue_date")
    private Date passportIssueDate;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "identification")
    private Set<PersonalCompanyCnpjEntity> companyCnpjs;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "identification")
    private Set<PersonalOtherDocumentEntity> otherDocuments;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "identification")
    private Set<PersonalNationalityEntity> nationality;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "identification")
    private Set<PersonalFiliationEntity> filiation;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "identification")
    private Set<PersonalPostalAddressEntity> postalAddresses;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "identification")
    private Set<PersonalPhoneEntity> phones;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "identification")
    private Set<PersonalEmailEntity> emails;

    public PersonalIdentificationData getDTO() {
        return new PersonalIdentificationData()
                .updateDateTime(BankLambdaUtils.dateToOffsetDate(this.getUpdatedAt()))
                .personalId(this.getPersonalIdentificationsId().toString())
                .brandName(this.getBrandName())
                .civilName(this.getCivilName())
                .socialName(this.getSocialName())
                .birthDate(BankLambdaUtils.dateToLocalDate(this.getBirthDate()))
                .maritalStatusCode(EnumMaritalStatusCode.fromValue(this.getMaritalStatusCode()))
                .maritalStatusAdditionalInfo(this.getMaritalStatusAdditionalInfo())
                .sex(EnumSex.fromValue(this.getSex()))
                .companyCnpj(this.getCompanyCnpjs().stream().map(PersonalCompanyCnpjEntity::getDTO).collect(Collectors.toList()))
                .documents(new PersonalDocument()
                        .cpfNumber(this.getCpfNumber())
                        .passportNumber(this.getPassportNumber())
                        .passportCountry(this.getPassportCountry())
                        .passportExpirationDate(BankLambdaUtils.dateToLocalDate(this.getPassportExpirationDate()))
                        .passportIssueDate(BankLambdaUtils.dateToLocalDate(this.getPassportIssueDate())))
                .otherDocuments(this.getOtherDocuments().stream().map(PersonalOtherDocumentEntity::getDTO).collect(Collectors.toList()))
                .hasBrazilianNationality(this.isHasBrazilianNationality())
                .nationality(this.getNationality().stream().map(PersonalNationalityEntity::getDTO).collect(Collectors.toList()))
                .filiation(this.getFiliation().stream().map(PersonalFiliationEntity::getDTO).collect(Collectors.toList()))
                .contacts(new PersonalContacts()
                        .postalAddresses(this.getPostalAddresses().stream().map(PersonalPostalAddressEntity::getDTO).collect(Collectors.toList()))
                        .phones(this.getPhones().stream().map(PersonalPhoneEntity::getDTO).collect(Collectors.toList()))
                        .emails(this.getEmails().stream().map(PersonalEmailEntity::getDTO).collect(Collectors.toList())));
    }
}
