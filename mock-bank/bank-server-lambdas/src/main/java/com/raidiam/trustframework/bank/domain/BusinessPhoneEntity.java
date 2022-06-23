package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.CustomerPhone;
import com.raidiam.trustframework.mockbank.models.generated.EnumAreaCode;
import com.raidiam.trustframework.mockbank.models.generated.EnumCustomerPhoneType;
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
@Table(name = "business_phones")
public class BusinessPhoneEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "business_identifications_id")
    @Type(type = "pg-uuid")
    private UUID businessIdentificationsId;

    @Column(name = "is_main")
    private boolean isMain;

    @Column(name = "type")
    private String type;

    @Column(name = "country_calling_code")
    private String countryCallingCode;

    @Column(name = "additional_info")
    private String additionalInfo;

    @Column(name = "area_code")
    private String areaCode;

    @Column(name = "number")
    private String number;

    @Column(name = "phone_extension")
    private String phoneExtension;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_identifications_id", referencedColumnName = "business_identifications_id", insertable = false, nullable = false, updatable = false)
    private BusinessIdentificationsEntity businessIdentifications;

    public CustomerPhone getDTO() {
        return new CustomerPhone()
                .isMain(this.isMain())
                .type(EnumCustomerPhoneType.fromValue(this.getType()))
                .additionalInfo(this.getAdditionalInfo())
                .countryCallingCode(this.getCountryCallingCode())
                .areaCode(EnumAreaCode.fromValue(this.getAreaCode()))
                .number(this.getNumber())
                .phoneExtension(this.getPhoneExtension());
    }
}
