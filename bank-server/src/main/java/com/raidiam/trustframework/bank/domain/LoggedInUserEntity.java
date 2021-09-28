package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.LoggedUser;
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
@Table(name = "logged_in_user_entity_documents")
public class LoggedInUserEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "logged_in_user_entity_document_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer id;

    @OneToOne(mappedBy = "loggedInUserEntityDocument")
    private ConsentEntity consentEntity;

    @Column(name = "identification")
    private String identification;

    @Column(name = "rel")
    private String rel;


    public static LoggedInUserEntity from(LoggedUser loggedInUser) {
        return Optional.ofNullable(loggedInUser)
                .map(LoggedUser::getDocument)
                .map(l -> {
                    LoggedInUserEntity entity = new LoggedInUserEntity();
                    entity.setIdentification(l.getIdentification());
                    entity.setRel(l.getRel());
                    return entity;
                }).orElse(null);
    }
}
