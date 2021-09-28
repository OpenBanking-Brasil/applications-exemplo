package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.DebtorAccount;
import com.raidiam.trustframework.mockbank.models.generated.Identification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "debtor_accounts")
public class DebtorAccountEntity {
    @Id
    @GeneratedValue
    @Column(name = "debtor_account_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer id;

    @Column(name = "ispb")
    private String ispb;

    @Column(name = "issuer")
    private String issuer;

    @Column(name = "number")
    private String number;

    @Column(name = "account_type")
    private String accountType;

    public static DebtorAccountEntity from(DebtorAccount debtorAccount) {
        return Optional.ofNullable(debtorAccount)
                .map(a -> {
                    if(a.getAccountType() != null) {
                        var entity = new DebtorAccountEntity();
                        entity.setIspb(a.getIspb());
                        entity.setIssuer(a.getIssuer());
                        entity.setNumber(a.getNumber());
                        entity.setAccountType(a.getAccountType().toString());
                        return entity;
                    }
                    return null;
                }).orElse(null);
    }
}
