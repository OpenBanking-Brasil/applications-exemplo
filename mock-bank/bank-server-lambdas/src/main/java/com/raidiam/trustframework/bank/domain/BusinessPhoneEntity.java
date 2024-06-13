package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.CustomerPhone;
import com.raidiam.trustframework.mockbank.models.generated.CustomerPhoneV2;
import com.raidiam.trustframework.mockbank.models.generated.EnumAreaCode;
import com.raidiam.trustframework.mockbank.models.generated.EnumAreaCodeV2;
import com.raidiam.trustframework.mockbank.models.generated.EnumCustomerPhoneType;
import com.raidiam.trustframework.mockbank.models.generated.EnumCustomerPhoneTypeV2;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "business_phones")
public class BusinessPhoneEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

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

    @NotNull
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_identifications_id", referencedColumnName = "business_identifications_id", nullable = false, updatable = false)
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

    public CustomerPhoneV2 getDTOV2() {
        return new CustomerPhoneV2()
                .isMain(this.isMain())
                .type(EnumCustomerPhoneTypeV2.fromValue(this.getType()))
                .additionalInfo(this.getAdditionalInfo())
                .countryCallingCode(this.getCountryCallingCode())
                .areaCode(EnumAreaCodeV2.fromValue(this.getAreaCode()))
                .number(this.getNumber())
                .phoneExtension(this.getPhoneExtension());
    }

    public static BusinessPhoneEntity from(BusinessIdentificationsEntity business, CustomerPhone phone) {
        var phoneEntity = new BusinessPhoneEntity();
        phoneEntity.setBusinessIdentifications(business);
        phoneEntity.setMain(phone.isIsMain());
        phoneEntity.setType(phone.getType().name());
        phoneEntity.setAdditionalInfo(phone.getAdditionalInfo());
        phoneEntity.setCountryCallingCode(phone.getCountryCallingCode());
        phoneEntity.setAreaCode(phone.getAreaCode().toString());
        phoneEntity.setNumber(phone.getNumber());
        phoneEntity.setPhoneExtension(phone.getPhoneExtension());
        return phoneEntity;
    }
}
