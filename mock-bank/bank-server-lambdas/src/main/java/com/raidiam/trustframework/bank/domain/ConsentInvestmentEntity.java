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
@Table(name = "consent_investment")
public class ConsentInvestmentEntity extends BaseEntity  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "consent_id")
    private String consentId;

    @Column(name = "bank_fixed_income_id")
    private UUID bankFixedIncomeId;

    @Column(name = "credit_fixed_income_id")
    private UUID creditFixedIncomeId;

    @Column(name = "variable_income_id")
    private UUID variableIncomeId;

    @Column(name = "treasure_title_id")
    private UUID treasureTitleId;

    @Column(name = "fund_id")
    private UUID fundId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consent_id", referencedColumnName = "consent_id", insertable = false, nullable = false, updatable = false)
    @NotAudited
    private ConsentEntity consent;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_fixed_income_id", referencedColumnName = "investment_id", insertable = false, nullable = false, updatable = false)
    @NotAudited
    private BankFixedIncomesEntity bankFixedIncomesEntity;

    public ConsentInvestmentEntity(ConsentEntity consent){
        this.consentId = consent.getConsentId();
    }

    public ConsentInvestmentEntity setInvestmentsId(BaseEntity entity){
        if(entity instanceof BankFixedIncomesEntity) {
            this.bankFixedIncomeId = ((BankFixedIncomesEntity) entity).getInvestmentId();
        } else if(entity instanceof CreditFixedIncomesEntity) {
            this.creditFixedIncomeId = ((CreditFixedIncomesEntity) entity).getInvestmentId();
        } else if(entity instanceof VariableIncomesEntity) {
            this.variableIncomeId = ((VariableIncomesEntity) entity).getInvestmentId();
        } else if(entity instanceof TreasureTitlesEntity) {
            this.treasureTitleId = ((TreasureTitlesEntity) entity).getInvestmentId();
        } else if(entity instanceof FundsEntity) {
            this.fundId = ((FundsEntity) entity).getInvestmentId();
        }
        return this;
    }
}
