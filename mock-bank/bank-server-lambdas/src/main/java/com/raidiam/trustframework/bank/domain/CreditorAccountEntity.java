package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.CreditorAccount;
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
@Table(name = "creditor_accounts")
public class CreditorAccountEntity extends BaseEntity{
    @Id
    @GeneratedValue
    @Column(name = "creditor_account_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer creditorAccountId;

    @Column(name = "ispb")
    private String ispb;

    @Column(name = "issuer")
    private String issuer;

    @Column(name = "number")
    private String number;

    @Column(name = "account_type")
    private String accountType;

    public static CreditorAccountEntity from(CreditorAccount creditorAccount) {
        return Optional.ofNullable(creditorAccount)
                .map(a -> {
                    if(a.getAccountType() != null) {
                        var entity = new CreditorAccountEntity();
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
