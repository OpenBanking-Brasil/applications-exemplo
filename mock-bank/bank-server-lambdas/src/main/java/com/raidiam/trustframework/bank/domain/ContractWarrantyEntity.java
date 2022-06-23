package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@Audited
@Table(name = "warranties")
public class ContractWarrantyEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "warranty_id", unique = true, nullable = false, updatable = false, insertable = false, columnDefinition = "uuid NOT NULL DEFAULT uuid_generate_v4()")
    private UUID warrantyId;

    @Type(type = "pg-uuid")
    @Column(name = "contract_id", updatable = false)
    private UUID contractId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", referencedColumnName = "contract_id", insertable = false, nullable = false, updatable = false)
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


    public LoansWarranties getLoansDTO() {
        return new LoansWarranties()
                .currency(this.currency)
                .warrantyType(EnumWarrantyType.valueOf(this.warrantyType))
                .warrantySubType(EnumWarrantySubType.valueOf(this.warrantySubType))
                .warrantyAmount(this.warrantyAmount);
    }

    public FinancingsWarranties getFinancingDTO() {
        return new FinancingsWarranties()
                .currency(this.currency)
                .warrantyType(FinancingsWarranties.WarrantyTypeEnum.valueOf(this.warrantyType))
                .warrantySubType(FinancingsWarranties.WarrantySubTypeEnum.valueOf(this.warrantySubType))
                .warrantyAmount(this.warrantyAmount);
    }

    public UnarrangedAccountsOverdraftContractedWarranty getUnarrangedAccountOverdraftDTO() {
        return new UnarrangedAccountsOverdraftContractedWarranty()
                .currency(this.currency)
                .warrantyType(EnumWarrantyType.valueOf(this.warrantyType))
                .warrantySubType(EnumWarrantySubType.valueOf(this.warrantySubType))
                .warrantyAmount(this.warrantyAmount);
    }

    public InvoiceFinancingsContractedWarranty getInvoiceFinancingDTO() {
        return new InvoiceFinancingsContractedWarranty()
                .currency(this.currency)
                .warrantyType(EnumWarrantyType2.valueOf(this.warrantyType))
                .warrantySubType(EnumWarrantySubType1.valueOf(this.warrantySubType))
                .warrantyAmount(this.warrantyAmount);
    }
}
