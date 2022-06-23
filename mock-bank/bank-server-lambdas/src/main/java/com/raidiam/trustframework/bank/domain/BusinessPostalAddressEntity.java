package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.BusinessPostalAddress;
import com.raidiam.trustframework.mockbank.models.generated.EnumCountrySubDivision;
import com.raidiam.trustframework.mockbank.models.generated.GeographicCoordinates;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "business_postal_addresses")
public class BusinessPostalAddressEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "business_identifications_id")
    private UUID businessIdentificationsId;

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

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_identifications_id", referencedColumnName = "business_identifications_id", insertable = false, nullable = false, updatable = false)
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
}
