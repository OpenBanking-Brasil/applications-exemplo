package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "personal_financial_relations_portabilities_received")
public class PersonalFinancialRelationsPortabilitiesReceivedEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "employer_name")
    private String employerName;

    @Column(name = "employer_cnpj_cpf")
    private String employerCnpjCpf;

    @Column(name = "paycheck_bank_detainer_cnpj")
    private String paycheckBankDetainerCnpj;

    @Column(name = "paycheck_bank_detainer_ispb")
    private String paycheckBankDetainerIspb;

    @Column(name = "portability_approval_date")
    private LocalDate portabilityApprovalDate;

    @NotNull
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_financial_relations_id", referencedColumnName = "personal_financial_relations_id", nullable = false, updatable = false)
    private PersonalFinancialRelationsEntity financialRelations;

    public PortabilitiesReceived getDTO() {
        return new PortabilitiesReceived()
                .employerName(this.employerName)
                .employerCnpjCpf(this.employerCnpjCpf)
                .paycheckBankDetainerCnpj(this.paycheckBankDetainerCnpj)
                .paycheckBankDetainerIspb(this.paycheckBankDetainerIspb)
                .portabilityApprovalDate(this.portabilityApprovalDate);
    }
}
