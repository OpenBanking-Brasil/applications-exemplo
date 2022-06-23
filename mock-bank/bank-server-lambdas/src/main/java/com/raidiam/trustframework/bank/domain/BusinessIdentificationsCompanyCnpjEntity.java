package com.raidiam.trustframework.bank.domain;

import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "business_identifications_company_cnpj")
public class BusinessIdentificationsCompanyCnpjEntity {
    @Id
    @GeneratedValue
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "business_identifications_id")
    @Type(type = "pg-uuid")
    private UUID businessIdentificationsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "business_identifications_id", referencedColumnName = "business_identifications_id", insertable = false, nullable = false, updatable = false)
    private BusinessIdentificationsEntity businessIdentifications;

    @Column(name = "companyCnpj")
    private String companyCnpj;
}
