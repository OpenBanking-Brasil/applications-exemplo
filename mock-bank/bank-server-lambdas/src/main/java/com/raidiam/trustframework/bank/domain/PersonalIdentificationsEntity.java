package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
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
@Table(name = "personal_identifications")
public class PersonalIdentificationsEntity extends BaseEntity {

    @Id
    @GeneratedValue
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
    private LocalDate birthDate;

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
    private LocalDate passportExpirationDate;

    @Column(name = "passport_issue_date")
    private LocalDate passportIssueDate;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "identification")
    private Set<PersonalCompanyCnpjEntity> companyCnpjs = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "identification")
    private Set<PersonalOtherDocumentEntity> otherDocuments = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "identification")
    private Set<PersonalNationalityEntity> nationality = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "identification")
    private Set<PersonalFiliationEntity> filiation = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "identification")
    private Set<PersonalPostalAddressEntity> postalAddresses = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "identification")
    private Set<PersonalPhoneEntity> phones = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "identification")
    private Set<PersonalEmailEntity> emails = new HashSet<>();

    public PersonalIdentificationData getDTO() {
        return new PersonalIdentificationData()
                .updateDateTime(BankLambdaUtils.dateToOffsetDate(this.getUpdatedAt()))
                .personalId(this.getPersonalIdentificationsId().toString())
                .brandName(this.getBrandName())
                .civilName(this.getCivilName())
                .socialName(this.getSocialName())
                .birthDate(this.getBirthDate())
                .maritalStatusCode(EnumMaritalStatusCode.fromValue(this.getMaritalStatusCode()))
                .maritalStatusAdditionalInfo(this.getMaritalStatusAdditionalInfo())
                .sex(EnumSex.fromValue(this.getSex()))
                .companyCnpj(this.getCompanyCnpjs().stream().map(PersonalCompanyCnpjEntity::getDTO).collect(Collectors.toList()))
                .documents(new PersonalDocument()
                        .cpfNumber(this.getCpfNumber())
                        .passportNumber(this.getPassportNumber())
                        .passportCountry(this.getPassportCountry())
                        .passportExpirationDate(this.getPassportExpirationDate())
                        .passportIssueDate(this.getPassportIssueDate()))
                .otherDocuments(this.getOtherDocuments().stream().map(PersonalOtherDocumentEntity::getDTO).collect(Collectors.toList()))
                .hasBrazilianNationality(this.isHasBrazilianNationality())
                .nationality(this.getNationality().stream().map(PersonalNationalityEntity::getDTO).collect(Collectors.toList()))
                .filiation(this.getFiliation().stream().map(PersonalFiliationEntity::getDTO).collect(Collectors.toList()))
                .contacts(new PersonalContacts()
                        .postalAddresses(this.getPostalAddresses().stream().map(PersonalPostalAddressEntity::getDTO).collect(Collectors.toList()))
                        .phones(this.getPhones().stream().map(PersonalPhoneEntity::getDTO).collect(Collectors.toList()))
                        .emails(this.getEmails().stream().map(PersonalEmailEntity::getDTO).collect(Collectors.toList())));
    }

    public PersonalIdentificationDataV2 getDtoV2() {
        return new PersonalIdentificationDataV2()
                .updateDateTime(BankLambdaUtils.dateToOffsetDate(this.getUpdatedAt()))
                .personalId(this.getPersonalIdentificationsId().toString())
                .brandName(this.getBrandName())
                .civilName(this.getCivilName())
                .socialName(this.getSocialName())
                .birthDate(this.getBirthDate())
                .maritalStatusCode(EnumMaritalStatusCode.fromValue(this.getMaritalStatusCode()))
                .maritalStatusAdditionalInfo(this.getMaritalStatusAdditionalInfo())
                .sex(EnumSexV2.fromValue(this.getSex()))
                .companiesCnpj(this.getCompanyCnpjs().stream().map(PersonalCompanyCnpjEntity::getDTO).collect(Collectors.toList()))
                .documents(new PersonalDocumentV2()
                        .cpfNumber(this.getCpfNumber())
                        .passport(new PersonalPassportV2()
                                .number(this.getPassportNumber())
                                .country(this.getPassportCountry())
                                .expirationDate(this.getPassportExpirationDate())
                                .issueDate(this.getPassportIssueDate())))
                .otherDocuments(this.getOtherDocuments().stream().map(PersonalOtherDocumentEntity::getDTOV2).collect(Collectors.toList()))
                .hasBrazilianNationality(this.isHasBrazilianNationality())
                .nationality(this.getNationality().stream().map(PersonalNationalityEntity::getDTOV2).collect(Collectors.toList()))
                .filiation(this.getFiliation().stream().map(PersonalFiliationEntity::getDTOV2).collect(Collectors.toList()))
                .contacts(new PersonalContactsV2()
                        .postalAddresses(this.getPostalAddresses().stream().map(PersonalPostalAddressEntity::getDTOV2).collect(Collectors.toList()))
                        .phones(this.getPhones().stream().map(PersonalPhoneEntity::getDTOV2).collect(Collectors.toList()))
                        .emails(this.getEmails().stream().map(PersonalEmailEntity::getDTOV2).collect(Collectors.toList())));
    }

    public static PersonalIdentificationsEntity from(CreatePersonalIdentificationData identifications, UUID accountHolderId) {
        var personalEntity = new PersonalIdentificationsEntity();
        personalEntity.setAccountHolderId(accountHolderId);
        personalEntity.setBrandName(identifications.getBrandName());
        personalEntity.setCivilName(identifications.getCivilName());
        personalEntity.setSocialName(identifications.getSocialName());
        personalEntity.setBirthDate(identifications.getBirthDate());
        personalEntity.setMaritalStatusCode(identifications.getMaritalStatusCode().toString());
        personalEntity.setMaritalStatusAdditionalInfo(identifications.getMaritalStatusAdditionalInfo());
        personalEntity.setSex(identifications.getSex().toString());
        personalEntity.setHasBrazilianNationality(identifications.isHasBrazilianNationality());
        personalEntity.setCpfNumber(identifications.getCpfNumber());
        personalEntity.setPassportNumber(identifications.getPassportNumber());
        personalEntity.setPassportCountry(identifications.getPassportCountry());
        personalEntity.setPassportExpirationDate(identifications.getPassportExpirationDate());
        personalEntity.setPassportIssueDate(identifications.getPassportIssueDate());

        if (identifications.getCompanyCnpj() != null) {
            var cnpjList = identifications.getCompanyCnpj().stream()
                    .map(c -> PersonalCompanyCnpjEntity.from(personalEntity, c))
                    .collect(Collectors.toSet());
            personalEntity.setCompanyCnpjs(cnpjList);
        }

        if (identifications.getOtherDocuments() != null) {
            var documentsList = identifications.getOtherDocuments().stream()
                    .map(o -> PersonalOtherDocumentEntity.from(personalEntity, o))
                    .collect(Collectors.toSet());
            personalEntity.setOtherDocuments(documentsList);
        }

        if (identifications.getNationality() != null) {
            var nationalityList = identifications.getNationality().stream()
                    .map(n -> PersonalNationalityEntity.from(personalEntity, n))
                    .collect(Collectors.toSet());
            personalEntity.setNationality(nationalityList);
        }

        if (identifications.getFiliation() != null) {
            var filiationList = identifications.getFiliation().stream()
                    .map(f -> PersonalFiliationEntity.from(personalEntity, f))
                    .collect(Collectors.toSet());
            personalEntity.setFiliation(filiationList);
        }

        if (identifications.getContacts() != null) {
            if (!identifications.getContacts().getPostalAddresses().isEmpty()) {
                var postalList = identifications.getContacts().getPostalAddresses().stream()
                        .map(p -> PersonalPostalAddressEntity.from(personalEntity, p))
                        .collect(Collectors.toSet());
                personalEntity.setPostalAddresses(postalList);
            }

            if (!identifications.getContacts().getPhones().isEmpty()) {
                var phonesList = identifications.getContacts().getPhones().stream()
                        .map(p -> PersonalPhoneEntity.from(personalEntity, p))
                        .collect(Collectors.toSet());
                personalEntity.setPhones(phonesList);
            }

            if (!identifications.getContacts().getEmails().isEmpty()) {
                var emailsList = identifications.getContacts().getEmails().stream()
                        .map(e -> PersonalEmailEntity.from(personalEntity, e))
                        .collect(Collectors.toSet());
                personalEntity.setEmails(emailsList);
            }
        }

        return personalEntity;
    }

    public ResponsePersonalIdentification getResponseAdminIdentifications() {
        return new ResponsePersonalIdentification().data(new ResponsePersonalIdentificationData()
                .personalId(String.valueOf(this.personalIdentificationsId))
                .brandName(this.getBrandName())
                .civilName(this.getCivilName())
                .socialName(this.getSocialName())
                .birthDate(this.getBirthDate())
                .maritalStatusCode(EnumMaritalStatusCode.fromValue(this.getMaritalStatusCode()))
                .maritalStatusAdditionalInfo(this.getMaritalStatusAdditionalInfo())
                .sex(EnumSex.fromValue(this.getSex()))
                .hasBrazilianNationality(this.isHasBrazilianNationality())
                .cpfNumber(this.getCpfNumber())
                .passportNumber(this.getPassportNumber())
                .passportCountry(this.getPassportCountry())
                .passportExpirationDate(this.getPassportExpirationDate())
                .passportIssueDate(this.getPassportIssueDate())
                .companyCnpj(this.companyCnpjs != null ? this.companyCnpjs.stream().map(PersonalCompanyCnpjEntity::getDTO).collect(Collectors.toList()) : null)
                .otherDocuments(this.otherDocuments != null ? this.otherDocuments.stream().map(PersonalOtherDocumentEntity::getDTO).collect(Collectors.toList()) : null)
                .nationality(this.nationality != null ? this.nationality.stream().map(PersonalNationalityEntity::getDTO).collect(Collectors.toList()) : null)
                .filiation(this.filiation != null ? this.filiation.stream().map(PersonalFiliationEntity::getDTO).collect(Collectors.toList()) : null)
                .contacts(getContactsDto()));
    }

    public PersonalIdentificationsEntity update(EditedPersonalIdentificationData identifications) {
        this.brandName = identifications.getBrandName();
        this.civilName = identifications.getCivilName();
        this.socialName = identifications.getSocialName();
        this.birthDate = identifications.getBirthDate();
        this.maritalStatusCode = identifications.getMaritalStatusCode().toString();
        this.maritalStatusAdditionalInfo = identifications.getMaritalStatusAdditionalInfo();
        this.sex = identifications.getSex().toString();
        this.hasBrazilianNationality = identifications.isHasBrazilianNationality();
        this.cpfNumber = identifications.getCpfNumber();
        this.passportNumber = identifications.getPassportNumber();
        this.passportCountry = identifications.getPassportCountry();
        this.passportExpirationDate = identifications.getPassportExpirationDate();
        this.passportIssueDate = identifications.getPassportIssueDate();

        var cnpjList = identifications.getCompanyCnpj().stream()
                .map(c -> PersonalCompanyCnpjEntity.from(this, c))
                .collect(Collectors.toSet());
        this.companyCnpjs.clear();
        this.companyCnpjs.addAll(cnpjList);

        var documentsList = identifications.getOtherDocuments().stream()
                .map(o -> PersonalOtherDocumentEntity.from(this, o))
                .collect(Collectors.toSet());
        this.otherDocuments.clear();
        this.otherDocuments.addAll(documentsList);

        var nationalityList = identifications.getNationality().stream()
                .map(n -> PersonalNationalityEntity.from(this, n))
                .collect(Collectors.toSet());
        this.nationality.clear();
        this.nationality.addAll(nationalityList);

        var filiationList = identifications.getFiliation().stream()
                .map(f -> PersonalFiliationEntity.from(this, f))
                .collect(Collectors.toSet());
        this.filiation.clear();
        this.filiation.addAll(filiationList);

        var postalList = identifications.getContacts().getPostalAddresses().stream()
                .map(p -> PersonalPostalAddressEntity.from(this, p))
                .collect(Collectors.toSet());
        this.postalAddresses.clear();
        this.postalAddresses.addAll(postalList);

        var phonesList = identifications.getContacts().getPhones().stream()
                .map(p -> PersonalPhoneEntity.from(this, p))
                .collect(Collectors.toSet());
        this.phones.clear();
        this.phones.addAll(phonesList);

        var emailsList = identifications.getContacts().getEmails().stream()
                .map(e -> PersonalEmailEntity.from(this, e))
                .collect(Collectors.toSet());
        this.emails.clear();
        this.emails.addAll(emailsList);

        return this;
    }

    public PersonalContacts getContactsDto() {
        PersonalContacts contacts = new PersonalContacts();
        contacts.setPostalAddresses(this.postalAddresses != null ? this.postalAddresses.stream().map(PersonalPostalAddressEntity::getDTO).collect(Collectors.toList()) : null);
        contacts.setPhones(this.phones != null ? this.phones.stream().map(PersonalPhoneEntity::getDTO).collect(Collectors.toList()) : null);
        contacts.setEmails(this.emails != null ? this.emails.stream().map(PersonalEmailEntity::getDTO).collect(Collectors.toList()) : null);
        return contacts;
    }
}
