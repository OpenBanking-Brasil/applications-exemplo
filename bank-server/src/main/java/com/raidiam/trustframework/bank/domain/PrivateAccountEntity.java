package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.models.generated.Level2Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;


@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "accounts_priv")
public class PrivateAccountEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @NotNull
    @Column(name = "scheme_name")
    private String schemeName;

    @NotNull
    @Column(name = "identification")
    private String identification;

    @Column(name = "name")
    private String name;

    @Column(name = "secondary_identification")
    private String secondaryIdentification;

    @NotNull
    @Type(type = "pg-uuid")
    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;

    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "account_id", insertable = false, nullable = false, updatable = false)
    @NotAudited
    private AccountEntity account;

    public static PrivateAccountEntity fromRequest(Level2Account req, AccountEntity account) {
        PrivateAccountEntity entity = new PrivateAccountEntity();
        entity.setAccountId(account.getAccountId());
        entity.setAccount(account);
        entity.setSchemeName(req.getSchemeName());
        entity.setIdentification(req.getIdentification());
        entity.setName(req.getName());
        entity.setSecondaryIdentification(req.getSecondaryIdentification());
        return entity;
    }

    public Level2Account getDTO() {
        return new Level2Account().schemeName(schemeName)
                .identification(identification)
                .name(name)
                .secondaryIdentification(secondaryIdentification);
    }
}
