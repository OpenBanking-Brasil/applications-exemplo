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
@Table(name = "personal_emails")
public class PersonalEmailEntity extends BaseEntity {

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
    @JoinColumn(name = "personal_identifications_id", referencedColumnName = "personal_identifications_id", nullable = false, updatable = false)
    private PersonalIdentificationsEntity identification;

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

    public static PersonalEmailEntity from(PersonalIdentificationsEntity identification, CustomerEmail personalEmails) {
        var personalEmailsEntity = new PersonalEmailEntity();
        personalEmailsEntity.setIdentification(identification);
        personalEmailsEntity.setMain(personalEmails.isIsMain());
        personalEmailsEntity.setEmail(personalEmails.getEmail());
        return personalEmailsEntity;
    }
}
