package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.models.generated.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "accounts")
public class AccountEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Generated(GenerationTime.INSERT)
    @Type(type = "pg-uuid")
    @Column(name = "account_id", nullable = false, updatable = false, insertable = false, columnDefinition = "uuid NOT NULL DEFAULT uuid_generate_v4()")
    private UUID accountId;

    @Column(name = "status")
    private String status;

    @EqualsAndHashCode.Exclude
    @Column(name = "status_update_date_time")
    private Date statusUpdateDateTime;

    @Column(name = "currency")
    private String currency;

    @Column(name = "account_type")
    private String accountType;

    @Column(name = "account_sub_type")
    private String accountSubType;

    @Column(name = "description")
    private String description;

    @Column(name = "nickname")
    private String nickname;

    @EqualsAndHashCode.Exclude
    @Column(name = "opening_date")
    private Date openingDate;

    @EqualsAndHashCode.Exclude
    @Column(name = "maturity_date")
    private Date maturityDate;

    @Column(name = "switch_status")
    private String switchStatus;

    @Column(name = "servicer_scheme_name")
    private String servicerSchemeName;

    @Column(name = "servicer_identification")
    private String servicerIdentification;

    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "account")
    private List<PrivateAccountEntity> account = new ArrayList<>();

    @EqualsAndHashCode.Include
    private Instant dateCompareStatusUpdateTime () {
        return Optional.ofNullable(statusUpdateDateTime).map(Date::toInstant).orElse(null);
    }

    @EqualsAndHashCode.Include
    private Instant dateCompareOpeningDate () {
        return Optional.ofNullable(openingDate).map(Date::toInstant).orElse(null);
    }

    @EqualsAndHashCode.Include
    private Instant dateCompareMaturityDate () {
        return Optional.ofNullable(maturityDate).map(Date::toInstant).orElse(null);
    }

    public static AccountEntity fromRequest (Level1Account req) {
        AccountEntity entity = new AccountEntity();
        entity.setStatus(req.getStatus().toString());
        entity.setStatusUpdateDateTime(Date.from(req.getStatusUpdateDateTime().toInstant()));
        entity.setCurrency(req.getCurrency());
        entity.setAccountType(req.getAccountType().toString());
        entity.setAccountSubType(req.getAccountSubType().toString());
        entity.setDescription(req.getDescription());
        entity.setNickname(req.getNickname());
        entity.setOpeningDate(Date.from(req.getOpeningDate().toInstant()));
        entity.setMaturityDate(Date.from(req.getMaturityDate().toInstant()));
        entity.setSwitchStatus(req.getSwitchStatus().toString());
        entity.setServicerSchemeName(req.getServicer().getSchemeName());
        entity.setServicerIdentification(req.getServicer().getIdentification());
        return entity;
    }

    public Level1Account getDTO() {
        return new Level1Account().accountId(accountId.toString())
                .status(Level1AccountStatus.fromValue(status))
                .statusUpdateDateTime(statusUpdateDateTime.toInstant().atOffset(ZoneOffset.UTC))
                .currency(currency)
                .accountType(AccountType.fromValue(accountType))
                .accountSubType(AccountSubType.fromValue(accountSubType))
                .description(description)
                .nickname(nickname)
                .openingDate(openingDate.toInstant().atOffset(ZoneOffset.UTC))
                .maturityDate(Optional.of(maturityDate).map(Date::toInstant).map(a -> a.atOffset(ZoneOffset.UTC)).orElse(null))
                .switchStatus(Level1Account.SwitchStatusEnum.fromValue(switchStatus))
                .account(null)
                .servicer(null);
    }

    public Level1Account getDTOWithPrivateFields() {
        Level2Accounts l2Accounts = new Level2Accounts();
        account.stream().map(PrivateAccountEntity::getDTO).forEach(l2Accounts::add);

        return this.getDTO()
                .account(l2Accounts)
                .servicer(new Servicer().schemeName(servicerSchemeName).identification(servicerIdentification));
    }
}
