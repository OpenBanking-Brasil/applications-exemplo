package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.BusinessPostalAddress;
import com.raidiam.trustframework.mockbank.models.generated.BusinessPostalAddressV2;
import com.raidiam.trustframework.mockbank.models.generated.EnumCountrySubDivision;
import com.raidiam.trustframework.mockbank.models.generated.EnumCountrySubDivisionV2;
import com.raidiam.trustframework.mockbank.models.generated.GeographicCoordinates;
import com.raidiam.trustframework.mockbank.models.generated.GeographicCoordinatesV2;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "business_postal_addresses")
public class BusinessPostalAddressEntity extends BaseEntity {

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

    @Column(name = "country")
    private String country;

    @Column(name = "post_code")
    private String postCode;

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
    @JoinColumn(name = "business_identifications_id", referencedColumnName = "business_identifications_id", nullable = false, updatable = false)
    private BusinessIdentificationsEntity businessIdentifications;

    public BusinessPostalAddress getDTO() {
        return new BusinessPostalAddress()
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

    public BusinessPostalAddressV2 getDTOV2() {
        return new BusinessPostalAddressV2()
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

    public static BusinessPostalAddressEntity from(BusinessIdentificationsEntity business, BusinessPostalAddress postalAddress) {
        var postalAddressEntity = new BusinessPostalAddressEntity();
        postalAddressEntity.setBusinessIdentifications(business);
        postalAddressEntity.setMain(postalAddress.isIsMain());
        postalAddressEntity.setAddress(postalAddress.getAddress());
        postalAddressEntity.setAdditionalInfo(postalAddress.getAdditionalInfo());
        postalAddressEntity.setDistrictName(postalAddress.getDistrictName());
        postalAddressEntity.setTownName(postalAddress.getTownName());
        postalAddressEntity.setIbgeTownCode(postalAddress.getIbgeTownCode());
        postalAddressEntity.setCountrySubdivision(postalAddress.getCountrySubDivision().name());
        postalAddressEntity.setPostCode(postalAddress.getPostCode());
        postalAddressEntity.setCountry(postalAddress.getCountry());
        postalAddressEntity.setCountryCode(postalAddress.getCountryCode());
        postalAddressEntity.setLatitude(postalAddress.getGeographicCoordinates() != null ? postalAddress.getGeographicCoordinates().getLatitude() : null);
        postalAddressEntity.setLongitude(postalAddress.getGeographicCoordinates() != null ? postalAddress.getGeographicCoordinates().getLongitude() : null);
        return postalAddressEntity;
    }
}
