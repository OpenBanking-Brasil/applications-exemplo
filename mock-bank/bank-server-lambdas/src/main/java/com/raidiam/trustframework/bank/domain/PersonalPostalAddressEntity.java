package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.EnumCountrySubDivision;
import com.raidiam.trustframework.mockbank.models.generated.EnumCountrySubDivisionV2;
import com.raidiam.trustframework.mockbank.models.generated.GeographicCoordinates;
import com.raidiam.trustframework.mockbank.models.generated.GeographicCoordinatesV2;
import com.raidiam.trustframework.mockbank.models.generated.PersonalPostalAddress;
import com.raidiam.trustframework.mockbank.models.generated.PersonalPostalAddressV2;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "personal_postal_addresses")
public class PersonalPostalAddressEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "is_main")
    private boolean isMain;

    @Column(name = "address")
    private String address;

    @Column(name = "additional_info")
    private String additionalInfo;

    @Column(name = "district_name")
    private String districtName;

    @Column(name = "town_name")
    private String townName;

    @Column(name = "ibge_town_code")
    private String ibgeTownCode;

    @Column(name = "country_subdivision")
    private String countrySubdivision;

    @Column(name = "post_code")
    private String postCode;

    @Column(name = "country")
    private String country;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "latitude")
    private String latitude;

    @Column(name = "longitude")
    private String longitude;

    @NotNull
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_identifications_id", referencedColumnName = "personal_identifications_id", nullable = false, updatable = false)
    private PersonalIdentificationsEntity identification;

    public PersonalPostalAddress getDTO() {
        return new PersonalPostalAddress()
                .isMain(this.isMain())
                .address(this.getAddress())
                .additionalInfo(this.getAdditionalInfo())
                .districtName(this.getDistrictName())
                .townName(this.getTownName())
                .ibgeTownCode(this.getIbgeTownCode())
                .countrySubDivision(EnumCountrySubDivision.fromValue(this.getCountrySubdivision()))
                .postCode(this.getPostCode())
                .country(this.getCountry())
                .countryCode(this.getCountryCode())
                .geographicCoordinates(new GeographicCoordinates().latitude(this.getLatitude()).longitude(this.getLongitude()));
    }

    public PersonalPostalAddressV2 getDTOV2() {
        return new PersonalPostalAddressV2()
                .isMain(this.isMain())
                .address(this.getAddress())
                .additionalInfo(this.getAdditionalInfo())
                .districtName(this.getDistrictName())
                .townName(this.getTownName())
                .ibgeTownCode(this.getIbgeTownCode())
                .countrySubDivision(EnumCountrySubDivisionV2.fromValue(this.getCountrySubdivision()))
                .postCode(this.getPostCode())
                .country(this.getCountry())
                .countryCode(this.getCountryCode())
                .geographicCoordinates(new GeographicCoordinatesV2().latitude(this.getLatitude()).longitude(this.getLongitude()));
    }

    public static PersonalPostalAddressEntity from(PersonalIdentificationsEntity identification, PersonalPostalAddress personalPostalAddresses) {
        var postalAddressesEntity = new PersonalPostalAddressEntity();
        postalAddressesEntity.setIdentification(identification);
        postalAddressesEntity.setMain(personalPostalAddresses.isIsMain());
        postalAddressesEntity.setAddress(personalPostalAddresses.getAddress());
        postalAddressesEntity.setAdditionalInfo(personalPostalAddresses.getAdditionalInfo());
        postalAddressesEntity.setDistrictName(personalPostalAddresses.getDistrictName());
        postalAddressesEntity.setTownName(personalPostalAddresses.getTownName());
        postalAddressesEntity.setIbgeTownCode(personalPostalAddresses.getIbgeTownCode());
        postalAddressesEntity.setCountrySubdivision(personalPostalAddresses.getCountrySubDivision().name());
        postalAddressesEntity.setPostCode(personalPostalAddresses.getPostCode());
        postalAddressesEntity.setCountry(personalPostalAddresses.getCountry());
        postalAddressesEntity.setCountryCode(personalPostalAddresses.getCountryCode());
        postalAddressesEntity.setLatitude(personalPostalAddresses.getGeographicCoordinates() != null ? personalPostalAddresses.getGeographicCoordinates().getLatitude() : null);
        postalAddressesEntity.setLongitude(personalPostalAddresses.getGeographicCoordinates() != null ? personalPostalAddresses.getGeographicCoordinates().getLongitude() : null);
        return postalAddressesEntity;
    }
}
