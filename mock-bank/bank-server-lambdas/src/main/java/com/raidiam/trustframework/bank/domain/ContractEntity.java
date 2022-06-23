package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@Audited
@Table(name = "contracts")
public class ContractEntity extends BaseEntity implements HasStatusInterface {

    @Id
    @GeneratedValue
    @Column(name = "contract_id", unique = true, nullable = false, updatable = false, insertable = false, columnDefinition = "uuid NOT NULL DEFAULT uuid_generate_v4()")
    private UUID contractId;

    @Column(name = "account_holder_id")
    private UUID accountHolderId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_holder_id", referencedColumnName = "account_holder_id", insertable = false, nullable = false, updatable = false)
    private AccountHolderEntity accountHolder;

    @Column(name = "contract_number")
    private String contractNumber;

    @Column(name = "contract_type")
    private String contractType;

    @Column(name = "contract_date", nullable = false)
    private LocalDate contractDate;

    @Column(name = "contract_amount", nullable = false)
    private double contractAmount;

    @Column(name = "disbursement_date")
    private LocalDate disbursementDate;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "first_instalment_due_date", nullable = false)
    private LocalDate firstInstalmentDueDate;

    @Column(name = "instalment_periodicity", nullable = false)
    private String instalmentPeriodicity;

    @Column(name = "instalment_periodicity_additional_info", nullable = false)
    private String instalmentPeriodicityAdditionalInfo;

    @Column(name = "amortization_scheduled", nullable = false)
    private String amortizationScheduled;

    @Column(name = "amortization_scheduled_additional_info", nullable = false)
    private String amortizationScheduledAdditionalInfo;

    @Column(name = "company_cnpj", nullable = false)
    private String companyCnpj;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_type", nullable = false)
    private String productType;

    @NotNull
    @Column(name = "product_sub_type", nullable = false)
    private String productSubType;

    @Column(name = "currency", nullable = false)
    private String currency;

    @NotNull
    @Column(name = "ipoc_code", nullable = false)
    private String ipocCode;

    @Column(name = "cet", nullable = false)
    private double cet;

    @Column(name = "status")
    private String status;

    @NotNull
    @Column(name = "paid_instalments", nullable = false)
    private int paidInstalments;

    @NotNull
    @Column(name = "contract_outstanding_balance", nullable = false)
    private double contractOutstandingBalance;

    @NotNull
    @Column(name = "type_number_of_instalments", nullable = false)
    private String typeNumberOfInstalments;

    @NotNull
    @Column(name = "total_number_of_instalments", nullable = false)
    private int totalNumberOfInstalments;

    @NotNull
    @Column(name = "type_contract_remaining", nullable = false)
    private String typeContractRemaining;

    @NotNull
    @Column(name = "contract_remaining_number", nullable = false)
    private int contractRemainingNumber;

    @NotNull
    @Column(name = "due_instalments", nullable = false)
    private int dueInstalments;

    @NotNull
    @Column(name = "past_due_instalments", nullable = false)
    private int pastDueInstalments;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "contract")
    private Set<ContractInterestRatesEntity> interestRates;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "contract")
    private Set<ContractedFeesEntity> contractedFees;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "contract")
    private Set<ContractedFinanceChargesEntity> contractedFinanceCharges;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "contract")
    private Set<ContractReleasesEntity> contractReleases;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "contract")
    private Set<ContractWarrantyEntity> contractWarranties;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "contract")
    private Set<ContractBalloonPaymentsEntity> balloonPayments;

    // --

    public UnarrangedAccountOverdraftContractListData getOverdraftAccountsDTOList() {
        return new UnarrangedAccountOverdraftContractListData()
            .contractId(String.valueOf(this.contractId))
            .ipocCode(this.ipocCode)
            .brandName(this.productName)
            .productType(ProductType.fromValue(this.productType))
            .productSubType(ProductSubType.fromValue(this.productSubType))
            .companyCnpj(this.companyCnpj);
    }

    public UnarrangedAccountOverdraftContractData getOverDraftAccountsDTO() {
        return new UnarrangedAccountOverdraftContractData()
                .contractNumber(String.valueOf(this.contractNumber))
                .ipocCode(this.ipocCode)
                .productName(this.productName)
                .productType(ProductType.fromValue(this.productType))
                .productSubType(ProductSubType.fromValue(this.productSubType))
                .contractDate(this.contractDate)
                .disbursementDate(this.disbursementDate)
                .settlementDate(this.settlementDate)
                .contractAmount(this.contractAmount)
                .currency(this.currency)
                .dueDate(this.dueDate)
                .instalmentPeriodicity(EnumContractInstalmentPeriodicity.valueOf(this.instalmentPeriodicity))
                .instalmentPeriodicityAdditionalInfo(this.instalmentPeriodicityAdditionalInfo)
                .firstInstalmentDueDate(this.firstInstalmentDueDate)
                .CET(BigDecimal.valueOf(this.cet))
                .amortizationScheduled(UnarrangedAccountOverdraftContractData.AmortizationScheduledEnum.valueOf(this.amortizationScheduled))
                .amortizationScheduledAdditionalInfo(this.amortizationScheduledAdditionalInfo)
                .interestRates(this.getInterestRates().stream().map(ContractInterestRatesEntity::getUnarrangedAccountsOverdraftDTO).collect(Collectors.toList()))
                .contractedFees(this.getContractedFees().stream().map(ContractedFeesEntity::getUnarrangedAccountOverdraftDTO).collect(Collectors.toList()))
                .contractedFinanceCharges(this.getContractedFinanceCharges().stream().map(ContractedFinanceChargesEntity::getUnarrangedAccountsOverdraftDTO).collect(Collectors.toList()));
    }

    public UnarrangedAccountOverdraftInstalmentsData getOverDraftInstalmentsData() {
        return new UnarrangedAccountOverdraftInstalmentsData()
                .dueInstalments(BigDecimal.valueOf(this.getDueInstalments()))
                .paidInstalments(BigDecimal.valueOf(this.getPaidInstalments()))
                .pastDueInstalments(BigDecimal.valueOf(this.getPastDueInstalments()))
                .totalNumberOfInstalments(BigDecimal.valueOf(this.getTotalNumberOfInstalments()))
                .typeNumberOfInstalments(EnumTypeNumberOfInstalments.fromValue(this.getTypeNumberOfInstalments()))
                .contractRemainingNumber(BigDecimal.valueOf(this.getContractRemainingNumber()))
                .typeContractRemaining(UnarrangedAccountOverdraftInstalmentsData.TypeContractRemainingEnum.valueOf(this.getTypeContractRemaining()))
                .balloonPayments(this.getBalloonPayments().stream().map(ContractBalloonPaymentsEntity::getUnarrangedAccountsOverdraftDTO).collect(Collectors.toList()));
    }

    public UnarrangedAccountOverdraftPaymentsData getOverDraftPaymentsData() {
        return new UnarrangedAccountOverdraftPaymentsData()
                .paidInstalments(BigDecimal.valueOf(this.getPaidInstalments()))
                .contractOutstandingBalance(this.getContractOutstandingBalance())
                .releases(this.getContractReleases().stream().map(ContractReleasesEntity::getUnarrangedAccountsOverdraftDTO).collect(Collectors.toList()));
    }

    public FinancingsListContract getFinancingsDTOList() {
        return new FinancingsListContract()
                .contractId(String.valueOf(this.contractId))
                .ipocCode(this.ipocCode)
                .brandName(this.productName)
                .productType(EnumProductType.fromValue(this.productType))
                .productSubType(EnumProductSubType.fromValue(this.productSubType))
                .companyCnpj(this.companyCnpj);
    }

    public FinancingsContract getFinancingsDTO() {
        return new FinancingsContract()
                .contractNumber(String.valueOf(this.contractNumber))
                .ipocCode(this.ipocCode)
                .productName(this.productName)
                .productType(EnumProductType.fromValue(this.productType))
                .productSubType(EnumProductSubType.fromValue(this.productSubType))
                .contractDate(this.contractDate)
                .disbursementDate(this.disbursementDate)
                .settlementDate(this.settlementDate)
                .contractAmount(this.contractAmount)
                .currency(this.currency)
                .dueDate(this.dueDate)
                .instalmentPeriodicity(FinancingsContract.InstalmentPeriodicityEnum.valueOf(this.instalmentPeriodicity))
                .instalmentPeriodicityAdditionalInfo(this.instalmentPeriodicityAdditionalInfo)
                .firstInstalmentDueDate(this.firstInstalmentDueDate)
                .CET(BigDecimal.valueOf(this.cet))
                .amortizationScheduled(FinancingsContract.AmortizationScheduledEnum.valueOf(this.amortizationScheduled))
                .amortizationScheduledAdditionalInfo(this.amortizationScheduledAdditionalInfo)
                .interestRates(this.getInterestRates().stream().map(ContractInterestRatesEntity::getFinancingsDTO).collect(Collectors.toList()))
                .contractedFees(this.getContractedFees().stream().map(ContractedFeesEntity::getFinancingsDTO).collect(Collectors.toList()))
                .contractedFinanceCharges(this.getContractedFinanceCharges().stream().map(ContractedFinanceChargesEntity::getFinancingsDTO).collect(Collectors.toList()));
    }

    public FinancingsInstalments getFinancingInstalmentsData() {
        return new FinancingsInstalments()
                .dueInstalments(BigDecimal.valueOf(this.getDueInstalments()))
                .paidInstalments(BigDecimal.valueOf(this.getPaidInstalments()))
                .pastDueInstalments(BigDecimal.valueOf(this.getPastDueInstalments()))
                .totalNumberOfInstalments(BigDecimal.valueOf(this.getTotalNumberOfInstalments()))
                .typeNumberOfInstalments(FinancingsInstalments.TypeNumberOfInstalmentsEnum.fromValue(this.getTypeNumberOfInstalments()))
                .contractRemainingNumber(BigDecimal.valueOf(this.getContractRemainingNumber()))
                .typeContractRemaining(FinancingsInstalments.TypeContractRemainingEnum.valueOf(this.getTypeContractRemaining()))
                .balloonPayments(this.getBalloonPayments().stream().map(ContractBalloonPaymentsEntity::getFinancingsDTO).collect(Collectors.toList()));
    }

    public FinancingsPayments getFinancingPaymentsData() {
        return new FinancingsPayments()
                .paidInstalments(BigDecimal.valueOf(this.getPaidInstalments()))
                .contractOutstandingBalance(this.getContractOutstandingBalance())
                .releases(this.getContractReleases().stream().map(ContractReleasesEntity::getFinancingsDTO).collect(Collectors.toList()));
    }

    public InvoiceFinancingsContractData getInvoiceFinancingsDTOList() {
        return new InvoiceFinancingsContractData()
                .contractId(String.valueOf(this.contractId))
                .ipocCode(this.ipocCode)
                .brandName(this.productName)
                .productType(EnumContractProductTypeInvoiceFinancings.fromValue(this.productType))
                .productSubType(EnumContractProductSubTypeInvoiceFinancings.fromValue(this.productSubType))
                .companyCnpj(this.companyCnpj);
    }

    public InvoiceFinancingsContract getInvoiceFinancingsDTO() {
        return new InvoiceFinancingsContract()
                .contractNumber(String.valueOf(this.contractNumber))
                .ipocCode(this.ipocCode)
                .productName(this.productName)
                .productType(EnumContractProductTypeInvoiceFinancings.fromValue(this.productType))
                .productSubType(EnumContractProductSubTypeInvoiceFinancings.fromValue(this.productSubType))
                .contractDate(this.contractDate)
                .disbursementDate(this.disbursementDate)
                .settlementDate(this.settlementDate)
                .contractAmount(this.contractAmount)
                .currency(this.currency)
                .dueDate(this.dueDate)
                .instalmentPeriodicity(EnumContractInstalmentPeriodicity1.valueOf(this.instalmentPeriodicity))
                .instalmentPeriodicityAdditionalInfo(this.instalmentPeriodicityAdditionalInfo)
                .firstInstalmentDueDate(this.firstInstalmentDueDate)
                .CET(this.cet)
                .amortizationScheduled(EnumContractAmortizationScheduled1.valueOf(this.amortizationScheduled))
                .amortizationScheduledAdditionalInfo(this.amortizationScheduledAdditionalInfo)
                .interestRates(this.getInterestRates().stream().map(ContractInterestRatesEntity::getInvoiceFinancingsDTO).collect(Collectors.toList()))
                .contractedFees(this.getContractedFees().stream().map(ContractedFeesEntity::getInvoiceFinancingsDTO).collect(Collectors.toList()))
                .contractedFinanceCharges(this.getContractedFinanceCharges().stream().map(ContractedFinanceChargesEntity::getInvoiceFinancingsDTO).collect(Collectors.toList()));
    }

    public InvoiceFinancingsInstalments getInvoiceFinancingInstalmentsData() {
        return new InvoiceFinancingsInstalments()
                .dueInstalments(BigDecimal.valueOf(this.getDueInstalments()))
                .paidInstalments(BigDecimal.valueOf(this.getPaidInstalments()))
                .pastDueInstalments(BigDecimal.valueOf(this.getPastDueInstalments()))
                .totalNumberOfInstalments(BigDecimal.valueOf(this.getTotalNumberOfInstalments()))
                .typeNumberOfInstalments(InvoiceFinancingsInstalments.TypeNumberOfInstalmentsEnum.fromValue(this.getTypeNumberOfInstalments()))
                .contractRemainingNumber(BigDecimal.valueOf(this.getContractRemainingNumber()))
                .typeContractRemaining(InvoiceFinancingsInstalments.TypeContractRemainingEnum.valueOf(this.getTypeContractRemaining()))
                .balloonPayments(this.getBalloonPayments().stream().map(ContractBalloonPaymentsEntity::getInvoiceFinancingsDTO).collect(Collectors.toList()));
    }

    public InvoiceFinancingsPayments getInvoiceFinancingPaymentsData() {
        return new InvoiceFinancingsPayments()
                .paidInstalments(BigDecimal.valueOf(this.getPaidInstalments()))
                .contractOutstandingBalance(this.getContractOutstandingBalance())
                .releases(this.getContractReleases().stream().map(ContractReleasesEntity::getInvoiceFinancingsDTO).collect(Collectors.toList()));
    }

    public LoansListContract getLoanDTOList() {
        return new LoansListContract()
                .contractId(String.valueOf(this.contractId))
                .ipocCode(this.ipocCode)
                .brandName(this.productName)
                .productType(EnumContractProductTypeLoans.fromValue(this.productType))
                .productSubType(EnumContractProductSubTypeLoans.fromValue(this.productSubType))
                .companyCnpj(this.companyCnpj);
    }

    public LoansContract getLoanDTO() {
        return new LoansContract()
                .contractNumber(String.valueOf(this.contractNumber))
                .ipocCode(this.ipocCode)
                .productName(this.productName)
                .productType(EnumContractProductTypeLoans.fromValue(this.productType))
                .productSubType(EnumContractProductSubTypeLoans.fromValue(this.productSubType))
                .contractDate(this.contractDate)
                .disbursementDate(this.disbursementDate)
                .settlementDate(this.settlementDate)
                .contractAmount(this.contractAmount)
                .currency(this.currency)
                .dueDate(this.dueDate)
                .instalmentPeriodicity(EnumContractInstalmentPeriodicity.valueOf(this.instalmentPeriodicity))
                .instalmentPeriodicityAdditionalInfo(this.instalmentPeriodicityAdditionalInfo)
                .firstInstalmentDueDate(this.firstInstalmentDueDate)
                .CET(BigDecimal.valueOf(this.cet))
                .amortizationScheduled(EnumContractAmortizationScheduled.valueOf(this.amortizationScheduled))
                .amortizationScheduledAdditionalInfo(this.amortizationScheduledAdditionalInfo)
                .cnpjConsignee(this.companyCnpj)
                .interestRates(this.getInterestRates().stream().map(ContractInterestRatesEntity::getLoansDTO).collect(Collectors.toList()))
                        .contractedFees(this.getContractedFees().stream().map(ContractedFeesEntity::getLoansDTO).collect(Collectors.toList()))
                        .contractedFinanceCharges(this.getContractedFinanceCharges().stream().map(ContractedFinanceChargesEntity::getLoansDTO).collect(Collectors.toList()));
    }

    public LoansInstalments getLoansInstalmentsData() {
        return new LoansInstalments()
                .dueInstalments(BigDecimal.valueOf(this.getDueInstalments()))
                .paidInstalments(BigDecimal.valueOf(this.getPaidInstalments()))
                .pastDueInstalments(BigDecimal.valueOf(this.getPastDueInstalments()))
                .totalNumberOfInstalments(BigDecimal.valueOf(this.getTotalNumberOfInstalments()))
                .typeNumberOfInstalments(LoansInstalments.TypeNumberOfInstalmentsEnum.fromValue(this.getTypeNumberOfInstalments()))
                .contractRemainingNumber(BigDecimal.valueOf(this.getContractRemainingNumber()))
                .typeContractRemaining(LoansInstalments.TypeContractRemainingEnum.valueOf(this.getTypeContractRemaining()))
                .balloonPayments(this.getBalloonPayments().stream().map(ContractBalloonPaymentsEntity::getLoansDTO).collect(Collectors.toList()));
    }

    public LoansPayments getLoansPaymentsData() {
        return new LoansPayments()
                .paidInstalments(BigDecimal.valueOf(this.getPaidInstalments()))
                .contractOutstandingBalance(this.getContractOutstandingBalance())
                .releases(this.getContractReleases().stream().map(ContractReleasesEntity::getLoansDTO).collect(Collectors.toList()));
    }

    public AccountData createSparseAccountData () {
        return new AccountData().accountId(this.getContractId().toString()).number(this.getContractNumber());
    }
}
