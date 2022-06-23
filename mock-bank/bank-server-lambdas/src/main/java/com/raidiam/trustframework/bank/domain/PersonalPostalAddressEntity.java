package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.EnumCountrySubDivision;
import com.raidiam.trustframework.mockbank.models.generated.GeographicCoordinates;
import com.raidiam.trustframework.mockbank.models.generated.PersonalPostalAddress;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "personal_postal_addresses")
public class PersonalPostalAddressEntity extends BaseEntity{

    @Id
    @GeneratedValue
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

    @Column(name = "personal_identifications_id")
    @Type(type = "pg-uuid")
    private UUID personalIdentificationsId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_identifications_id", referencedColumnName = "personal_identifications_id", insertable = false, nullable = false, updatable = false)
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
}
