package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Audited
@Table(name = "warranties")
public class ContractWarrantyEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "warranty_id", unique = true, nullable = false, updatable = false, insertable = false, columnDefinition = "uuid NOT NULL DEFAULT uuid_generate_v4()")
    private UUID warrantyId;

    @NotNull
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id")
    private ContractEntity contract;

    @NotNull
    @Column(name = "currency", nullable = false)
    private String currency;

    @NotNull
    @Column(name = "warranty_type", nullable = false)
    private String warrantyType;

    @NotNull
    @Column(name = "warranty_subtype", nullable = false)
    private String warrantySubType;

    @Column(name = "warranty_amount", nullable = false)
    private double warrantyAmount;


    public FinancingsWarrantiesV2 getFinancingsWarrantiesV2() {
        return new FinancingsWarrantiesV2()
                .currency(this.currency)
                .warrantyType(FinancingsWarrantiesV2.WarrantyTypeEnum.valueOf(this.warrantyType))
                .warrantySubType(FinancingsWarrantiesV2.WarrantySubTypeEnum.valueOf(this.warrantySubType))
                .warrantyAmount(BankLambdaUtils.formatAmountV2(this.warrantyAmount));
    }

    public InvoiceFinancingsContractedWarrantyV2 getInvoiceFinancingsWarrantiesV2() {
        return new InvoiceFinancingsContractedWarrantyV2()
                .currency(this.currency)
                .warrantyType(EnumWarrantyTypeV2.valueOf(this.warrantyType))
                .warrantySubType(EnumWarrantySubTypeV2.valueOf(this.warrantySubType))
                .warrantyAmount(BankLambdaUtils.formatAmountV2(this.warrantyAmount));
    }

    public LoansWarrantiesV2 getLoansWarrantiesV2() {
        return new LoansWarrantiesV2()
                .currency(this.currency)
                .warrantyType(EnumWarrantyTypeV2.valueOf(this.warrantyType))
                .warrantySubType(EnumWarrantySubTypeV2.valueOf(this.warrantySubType))
                .warrantyAmount(BankLambdaUtils.formatAmountV2(this.warrantyAmount));
    }

    public UnarrangedAccountsOverdraftContractedWarrantyV2 getUnarrangedAccountOverdraftWarrantiesV2() {
        return new UnarrangedAccountsOverdraftContractedWarrantyV2()
                .currency(this.currency)
                .warrantyType(EnumWarrantyTypeV2.valueOf(this.warrantyType))
                .warrantySubType(EnumWarrantySubTypeV2.valueOf(this.warrantySubType))
                .warrantyAmount(BankLambdaUtils.formatAmountV2(this.warrantyAmount));
    }

    public static ContractWarrantyEntity from(ContractEntity contract, ContractWarrantiesData warranty) {
        var warrantyEntity = new ContractWarrantyEntity();
        warrantyEntity.setContract(contract);
        warrantyEntity.setCurrency(warranty.getCurrency());
        warrantyEntity.setWarrantyType(warranty.getWarrantyType());
        warrantyEntity.setWarrantySubType(warranty.getWarrantySubType());
        warrantyEntity.setWarrantyAmount(warranty.getWarrantyAmount());
        return warrantyEntity;
    }

    public ContractWarrantiesData getContractWarrantiesData() {
        return new ContractWarrantiesData()
                .currency(this.currency)
                .warrantyType(this.warrantyType)
                .warrantySubType(this.warrantySubType)
                .warrantyAmount(this.warrantyAmount);
    }
}
