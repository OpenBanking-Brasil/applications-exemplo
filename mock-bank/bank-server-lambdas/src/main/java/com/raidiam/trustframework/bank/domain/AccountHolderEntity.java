package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.LoggedUser;
import com.raidiam.trustframework.mockbank.models.generated.LoggedUserDocument;
import lombok.*;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "account_holders")
public class AccountHolderEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Generated(GenerationTime.INSERT)
    @Type(type = "pg-uuid")
    @Column(name = "account_holder_id", unique = true, nullable = false, updatable = false, columnDefinition = "uuid DEFAULT uuid_generate_v4()")
    private UUID accountHolderId;

    @Column(name = "document_identification")
    private String documentIdentification;

    @Column(name = "document_rel")
    private String documentRel;

    @Column(name = "account_holder_name")
    private String accountHolderName;

    //this field is the user id queried by the OP, mapping to sub
    @Column(name = "user_id")
    private String userId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "accountHolder")
    private Set<AccountEntity> accounts;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "accountHolder")
    private Set<CreditCardAccountsEntity> creditCardAccounts;

    public static AccountHolderEntity fromLoggedUser (LoggedUser userRequest) {
        return Optional.ofNullable(userRequest)
                .map(LoggedUser::getDocument)
                .map(l -> {
                    AccountHolderEntity entity = new AccountHolderEntity();
                    entity.setDocumentIdentification(l.getIdentification());
                    entity.setDocumentRel(l.getRel());
                    return entity;
                }).orElse(null);
    }

    public LoggedUser getLoggedUser() {
        return new LoggedUser().document(
                new LoggedUserDocument()
                        .identification(this.getDocumentIdentification())
                        .rel(this.getDocumentRel()));
    }

    public Optional<AccountEntity> getAccountByNumber(String number) {
        return this.accounts.stream().filter(a -> a.getNumber().equals(number)).findFirst();
    }
}
