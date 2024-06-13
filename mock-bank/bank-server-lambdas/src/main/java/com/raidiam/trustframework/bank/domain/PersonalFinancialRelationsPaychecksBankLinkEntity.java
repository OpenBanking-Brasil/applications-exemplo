package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.PaychecksBankLink;
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
@Table(name = "personal_financial_relations_paychecks_bank_link")
public class PersonalFinancialRelationsPaychecksBankLinkEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "employer_name")
    private String employerName;

    @Column(name = "employer_cnpj_cpf")
    private String employerCnpjCpf;

    @Column(name = "paycheck_bank_cnpj")
    private String paycheckBankCnpj;

    @Column(name = "paycheck_bank_ispb")
    private String paycheckBankIspb;

    @Column(name = "account_opening_date")
    private LocalDate accountOpeningDate;

    @NotNull
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_financial_relations_id", referencedColumnName = "personal_financial_relations_id", nullable = false, updatable = false)
    private PersonalFinancialRelationsEntity financialRelations;

    public PaychecksBankLink getDTO() {
        return new PaychecksBankLink()
                .employerName(this.employerName)
                .employerCnpjCpf(this.employerCnpjCpf)
                .paycheckBankCnpj(this.paycheckBankCnpj)
                .paycheckBankIspb(this.paycheckBankIspb)
                .accountOpeningDate(this.accountOpeningDate);
    }

}
