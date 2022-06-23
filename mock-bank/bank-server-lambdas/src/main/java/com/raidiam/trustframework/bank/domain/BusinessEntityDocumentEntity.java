package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.BusinessEntity;
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
@Table(name = "business_entity_documents")
public class BusinessEntityDocumentEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "business_entity_document_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer id;

    @OneToOne(mappedBy = "businessEntityDocument")
    private ConsentEntity consentEntity;

    @Column(name = "identification")
    private String identification;

    @Column(name = "rel")
    private String rel;


    public static BusinessEntityDocumentEntity from(BusinessEntity businessEntity) {
        return Optional.ofNullable(businessEntity)
                .map(BusinessEntity::getDocument)
                .map(d -> {
                    BusinessEntityDocumentEntity entity = new BusinessEntityDocumentEntity();
                    entity.setIdentification(d.getIdentification());
                    entity.setRel(d.getRel());
                    return entity;
                }).orElse(null);
    }
}
