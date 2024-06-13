package com.raidiam.trustframework.bank.domain;

import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import java.util.UUID;

@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "consent_exchanges_operation")
public class ConsentExchangeOperationEntity extends BaseEntity  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "consent_id")
    private String consentId;

    @Column(name = "operation_id")
    private UUID operationId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consent_id", referencedColumnName = "consent_id", insertable = false, nullable = false, updatable = false)
    @NotAudited
    private ConsentEntity consent;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id", referencedColumnName = "operation_id", insertable = false, nullable = false, updatable = false)
    @NotAudited
    private ExchangesOperationEntity exchangesOperationEntity;

    public ConsentExchangeOperationEntity(ConsentEntity consent, ExchangesOperationEntity exchangesOperationEntity) {
        this.consentId = consent.getConsentId();
        this.operationId = exchangesOperationEntity.getOperationId();
    }
}
