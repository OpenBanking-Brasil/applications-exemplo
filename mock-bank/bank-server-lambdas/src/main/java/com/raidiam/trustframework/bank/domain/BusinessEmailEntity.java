package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.CustomerEmail;
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
@Table(name = "business_emails")
public class BusinessEmailEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "is_main")
    private boolean isMain;

    @Column(name = "email")
    private String email;

    @Column(name = "business_identifications_id")
    @Type(type = "pg-uuid")
    private UUID businessIdentificationsId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_identifications_id", referencedColumnName = "business_identifications_id", insertable = false, nullable = false, updatable = false)
    private BusinessIdentificationsEntity businessIdentifications;

    public CustomerEmail getDTO() {
        return new CustomerEmail()
                .isMain(this.isMain())
                .email(this.getEmail());
    }
}
