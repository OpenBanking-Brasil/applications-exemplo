package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.*;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Optional;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "account_holders")
public class AccountHolderEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public static AccountHolderEntity fromLoggedUser(LoggedUser userRequest) {
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
                new Document()
                        .identification(this.getDocumentIdentification())
                        .rel(this.getDocumentRel()));
    }

    public static AccountHolderEntity from(CreateAccountHolderData accountHolder) {
        var holder = new AccountHolderEntity();
        holder.setDocumentIdentification(accountHolder.getDocumentIdentification());
        holder.setDocumentRel(accountHolder.getDocumentRel());
        holder.setAccountHolderName(accountHolder.getAccountHolderName());
        return holder;
    }

    public ResponseAccountHolder getAccountHolderResponse() {
        return new ResponseAccountHolder().data(getAdminAccountHolderDto());
    }

    public ResponseAccountHolderData getAdminAccountHolderDto() {
        return new ResponseAccountHolderData()
                .accountHolderId(this.accountHolderId)
                .documentIdentification(this.documentIdentification)
                .documentRel(this.documentRel)
                .accountHolderName(this.accountHolderName);
    }

    public AccountHolderEntity update(CreateAccountHolderData accountHolder) {
        this.documentIdentification = accountHolder.getDocumentIdentification();
        this.documentRel = accountHolder.getDocumentRel();
        this.accountHolderName = accountHolder.getAccountHolderName();
        return this;
    }
}
