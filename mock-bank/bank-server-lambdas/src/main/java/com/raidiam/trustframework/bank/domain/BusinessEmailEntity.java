package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.CustomerEmail;
import com.raidiam.trustframework.mockbank.models.generated.CustomerEmailV2;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "business_emails")
public class BusinessEmailEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "is_main")
    private boolean isMain;

    @Column(name = "email")
    private String email;

    @NotNull
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_identifications_id", referencedColumnName = "business_identifications_id", nullable = false, updatable = false)
    private BusinessIdentificationsEntity businessIdentifications;

    public CustomerEmail getDTO() {
        return new CustomerEmail()
                .isMain(this.isMain())
                .email(this.getEmail());
    }

    public CustomerEmailV2 getDTOV2() {
        return new CustomerEmailV2()
                .isMain(this.isMain())
                .email(this.getEmail());
    }

    public static BusinessEmailEntity from(BusinessIdentificationsEntity business, CustomerEmail email) {
        var emailEntity = new BusinessEmailEntity();
        emailEntity.setBusinessIdentifications(business);
        emailEntity.setMain(email.isIsMain());
        emailEntity.setEmail(email.getEmail());
        return emailEntity;
    }
}
