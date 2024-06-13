package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
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
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "contract")
    private Set<ContractInterestRatesEntity> interestRates = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "contract")
    private Set<ContractedFeesEntity> contractedFees = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "contract")
    private Set<ContractedFinanceChargesEntity> contractedFinanceCharges = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "contract")
    private Set<ContractReleasesEntity> contractReleases = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "contract")
    private Set<ContractWarrantyEntity> contractWarranties = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "contract")
    private Set<ContractBalloonPaymentsEntity> balloonPayments = new HashSet<>();

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

    public UnarrangedAccountOverdraftContractDataV2 getOverDraftAccountsDtoV2() {
        return new UnarrangedAccountOverdraftContractDataV2()
                .contractNumber(String.valueOf(this.contractNumber))
                .ipocCode(this.ipocCode)
                .productName(this.productName)
                .productType(ProductTypeV2.fromValue(this.productType))
                .productSubType(ProductSubTypeV2.fromValue(this.productSubType))
                .contractDate(this.contractDate)
                .disbursementDates(Collections.singletonList(this.disbursementDate))
                .settlementDate(this.settlementDate)
                .contractAmount(BankLambdaUtils.formatAmountV2(this.contractAmount))
                .currency(this.currency)
                .dueDate(this.dueDate)
                .instalmentPeriodicity(EnumContractInstalmentPeriodicityV2.valueOf(this.instalmentPeriodicity))
                .instalmentPeriodicityAdditionalInfo(this.instalmentPeriodicityAdditionalInfo)
                .firstInstalmentDueDate(this.firstInstalmentDueDate)
                .CET(BankLambdaUtils.formatRateV2(this.cet))
                .amortizationScheduled(UnarrangedAccountOverdraftContractDataV2.AmortizationScheduledEnum.valueOf(this.amortizationScheduled))
                .amortizationScheduledAdditionalInfo(this.amortizationScheduledAdditionalInfo)
                .interestRates(this.getInterestRates().stream().map(ContractInterestRatesEntity::getUnarrangedAccountsOverdraftDTOV2).collect(Collectors.toList()))
                .contractedFees(this.getContractedFees().stream().map(ContractedFeesEntity::getUnarrangedAccountOverdraftFeeDTOV2).collect(Collectors.toList()))
                .contractedFinanceCharges(this.getContractedFinanceCharges().stream().map(ContractedFinanceChargesEntity::getUnarrangedAccountsOverdraftDTOV2).collect(Collectors.toList()));
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

    public UnarrangedAccountOverdraftInstalmentsDataV2 getOverDraftInstalmentsDataV2() {
        return new UnarrangedAccountOverdraftInstalmentsDataV2()
                .dueInstalments(BigDecimal.valueOf(this.getDueInstalments()))
                .paidInstalments(BigDecimal.valueOf(this.getPaidInstalments()))
                .pastDueInstalments(BigDecimal.valueOf(this.getPastDueInstalments()))
                .totalNumberOfInstalments(BigDecimal.valueOf(this.getTotalNumberOfInstalments()))
                .typeNumberOfInstalments(EnumTypeNumberOfInstalmentsV2.fromValue(this.getTypeNumberOfInstalments()))
                .contractRemainingNumber(BigDecimal.valueOf(this.getContractRemainingNumber()))
                .typeContractRemaining(UnarrangedAccountOverdraftInstalmentsDataV2.TypeContractRemainingEnum.valueOf(this.getTypeContractRemaining()))
                .balloonPayments(this.getBalloonPayments().stream().map(ContractBalloonPaymentsEntity::getOverdraftBalloonPaymentV2).collect(Collectors.toList()));
    }

    public UnarrangedAccountOverdraftPaymentsData getOverDraftPaymentsData() {
        return new UnarrangedAccountOverdraftPaymentsData()
                .paidInstalments(BigDecimal.valueOf(this.getPaidInstalments()))
                .contractOutstandingBalance(this.getContractOutstandingBalance())
                .releases(this.getContractReleases().stream().map(ContractReleasesEntity::getUnarrangedAccountsOverdraftDTO).collect(Collectors.toList()));
    }

    public UnarrangedAccountOverdraftPaymentsDataV2 getOverDraftPaymentsDataV2() {
        return new UnarrangedAccountOverdraftPaymentsDataV2()
                .paidInstalments(BigDecimal.valueOf(this.getPaidInstalments()))
                .contractOutstandingBalance(BankLambdaUtils.formatAmountV2(this.getContractOutstandingBalance()))
                .releases(this.getContractReleases().stream().map(ContractReleasesEntity::getUnarrangedAccountsOverdraftDTOv2).collect(Collectors.toList()));
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

    public FinancingsContractV2 getFinancingsDtoV2() {
        return new FinancingsContractV2()
                .contractNumber(String.valueOf(this.contractNumber))
                .ipocCode(this.ipocCode)
                .productName(this.productName)
                .productType(EnumProductTypeV2.fromValue(this.productType))
                .productSubType(EnumProductSubTypeV2.fromValue(this.productSubType))
                .contractDate(this.contractDate)
                .disbursementDates(Collections.singletonList(this.disbursementDate))
                .settlementDate(this.settlementDate)
                .contractAmount(BankLambdaUtils.formatAmountV2(this.contractAmount))
                .currency(this.currency)
                .dueDate(this.dueDate)
                .instalmentPeriodicity(FinancingsContractV2.InstalmentPeriodicityEnum.valueOf(this.instalmentPeriodicity))
                .instalmentPeriodicityAdditionalInfo(this.instalmentPeriodicityAdditionalInfo)
                .firstInstalmentDueDate(this.firstInstalmentDueDate)
                .CET(BankLambdaUtils.formatRateV2(this.cet))
                .amortizationScheduled(FinancingsContractV2.AmortizationScheduledEnum.valueOf(this.amortizationScheduled))
                .amortizationScheduledAdditionalInfo(this.amortizationScheduledAdditionalInfo)
                .interestRates(this.getInterestRates().stream().map(ContractInterestRatesEntity::getFinancingsDTOV2).collect(Collectors.toList()))
                .contractedFees(this.getContractedFees().stream().map(ContractedFeesEntity::getFinancingsDTOV2).collect(Collectors.toList()))
                .contractedFinanceCharges(this.getContractedFinanceCharges().stream().map(ContractedFinanceChargesEntity::getFinancingsDTOV2).collect(Collectors.toList()));
    }

    public FinancingsInstalmentsV2 getFinancingInstalmentsDataV2() {
        return new FinancingsInstalmentsV2()
                .dueInstalments(BigDecimal.valueOf(this.getDueInstalments()))
                .paidInstalments(BigDecimal.valueOf(this.getPaidInstalments()))
                .pastDueInstalments(BigDecimal.valueOf(this.getPastDueInstalments()))
                .totalNumberOfInstalments(BigDecimal.valueOf(this.getTotalNumberOfInstalments()))
                .typeNumberOfInstalments(FinancingsInstalmentsV2.TypeNumberOfInstalmentsEnum.fromValue(this.getTypeNumberOfInstalments()))
                .contractRemainingNumber(BigDecimal.valueOf(this.getContractRemainingNumber()))
                .typeContractRemaining(FinancingsInstalmentsV2.TypeContractRemainingEnum.valueOf(this.getTypeContractRemaining()))
                .balloonPayments(this.getBalloonPayments().stream().map(ContractBalloonPaymentsEntity::getFinancingsBalloonPaymentV2).collect(Collectors.toList()));
    }

    public FinancingsPaymentsV2 getFinancingPaymentsDataV2() {
        return new FinancingsPaymentsV2()
                .paidInstalments(BigDecimal.valueOf(this.getPaidInstalments()))
                .contractOutstandingBalance(BankLambdaUtils.formatAmountV2(this.getContractOutstandingBalance()))
                .releases(this.getContractReleases().stream().map(ContractReleasesEntity::getFinancingsDTOv2).collect(Collectors.toList()));
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

    public InvoiceFinancingsContractV2 getInvoiceFinancingsDtoV2() {
        return new InvoiceFinancingsContractV2()
                .contractNumber(String.valueOf(this.contractNumber))
                .ipocCode(this.ipocCode)
                .productName(this.productName)
                .productType(EnumContractProductTypeInvoiceFinancingsV2.fromValue(this.productType))
                .productSubType(EnumContractProductSubTypeInvoiceFinancingsV2.fromValue(this.productSubType))
                .contractDate(this.contractDate)
                .disbursementDates(Collections.singletonList(this.disbursementDate))
                .settlementDate(this.settlementDate)
                .contractAmount(BankLambdaUtils.formatAmountV2(this.contractAmount))
                .currency(this.currency)
                .dueDate(this.dueDate)
                .instalmentPeriodicity(EnumContractInstalmentPeriodicityV2.valueOf(this.instalmentPeriodicity))
                .instalmentPeriodicityAdditionalInfo(this.instalmentPeriodicityAdditionalInfo)
                .firstInstalmentDueDate(this.firstInstalmentDueDate)
                .CET(BankLambdaUtils.formatRateV2(this.cet))
                .amortizationScheduled(EnumContractAmortizationScheduledV2.valueOf(this.amortizationScheduled))
                .amortizationScheduledAdditionalInfo(this.amortizationScheduledAdditionalInfo)
                .interestRates(this.getInterestRates().stream().map(ContractInterestRatesEntity::getInvoiceFinancingsDTOV2).collect(Collectors.toList()))
                .contractedFees(this.getContractedFees().stream().map(ContractedFeesEntity::getInvoiceFinancingsDTOV2).collect(Collectors.toList()))
                .contractedFinanceCharges(this.getContractedFinanceCharges().stream().map(ContractedFinanceChargesEntity::getInvoiceFinancingsDTOV2).collect(Collectors.toList()));
    }

    public InvoiceFinancingsInstalmentsV2 getInvoiceFinancingInstalmentsDataV2() {
        return new InvoiceFinancingsInstalmentsV2()
                .dueInstalments(BigDecimal.valueOf(this.getDueInstalments()))
                .paidInstalments(BigDecimal.valueOf(this.getPaidInstalments()))
                .pastDueInstalments(BigDecimal.valueOf(this.getPastDueInstalments()))
                .totalNumberOfInstalments(BigDecimal.valueOf(this.getTotalNumberOfInstalments()))
                .typeNumberOfInstalments(InvoiceFinancingsInstalmentsV2.TypeNumberOfInstalmentsEnum.fromValue(this.getTypeNumberOfInstalments()))
                .contractRemainingNumber(BigDecimal.valueOf(this.getContractRemainingNumber()))
                .typeContractRemaining(InvoiceFinancingsInstalmentsV2.TypeContractRemainingEnum.valueOf(this.getTypeContractRemaining()))
                .balloonPayments(this.getBalloonPayments().stream().map(ContractBalloonPaymentsEntity::getInvoiceFinancingsBalloonPaymentV2).collect(Collectors.toList()));
    }

    public InvoiceFinancingsPaymentsV2 getInvoiceFinancingPaymentsDataV2() {
        return new InvoiceFinancingsPaymentsV2()
                .paidInstalments(BigDecimal.valueOf(this.getPaidInstalments()))
                .contractOutstandingBalance(BankLambdaUtils.formatAmountV2(this.getContractOutstandingBalance()))
                .releases(this.getContractReleases().stream().map(ContractReleasesEntity::getInvoiceFinancingsDTOv2).collect(Collectors.toList()));
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

    public LoansContractV2 getLoanDtoV2() {
        return new LoansContractV2()
                .contractNumber(String.valueOf(this.contractNumber))
                .ipocCode(this.ipocCode)
                .productName(this.productName)
                .productType(EnumContractProductTypeLoansV2.fromValue(this.productType))
                .productSubType(EnumContractProductSubTypeLoansV2.fromValue(this.productSubType))
                .contractDate(this.contractDate)
                .disbursementDates(Collections.singletonList(this.disbursementDate))
                .settlementDate(this.settlementDate)
                .contractAmount(BankLambdaUtils.formatAmountV2(this.contractAmount))
                .currency(this.currency)
                .dueDate(this.dueDate)
                .instalmentPeriodicity(EnumContractInstalmentPeriodicityV2.valueOf(this.instalmentPeriodicity))
                .instalmentPeriodicityAdditionalInfo(this.instalmentPeriodicityAdditionalInfo)
                .firstInstalmentDueDate(this.firstInstalmentDueDate)
                .CET(BankLambdaUtils.formatRateV2(this.cet))
                .amortizationScheduled(EnumContractAmortizationScheduledV2.valueOf(this.amortizationScheduled))
                .amortizationScheduledAdditionalInfo(this.amortizationScheduledAdditionalInfo)
                .cnpjConsignee(this.companyCnpj)
                .interestRates(this.getInterestRates().stream().map(ContractInterestRatesEntity::getLoansDTOV2).collect(Collectors.toList()))
                .contractedFees(this.getContractedFees().stream().map(ContractedFeesEntity::getLoansDTOV2).collect(Collectors.toList()))
                .contractedFinanceCharges(this.getContractedFinanceCharges().stream().map(ContractedFinanceChargesEntity::getLoansDTOV2).collect(Collectors.toList()));
    }

    public LoansInstalmentsV2 getLoansInstalmentsDataV2() {
        return new LoansInstalmentsV2()
                .dueInstalments(BigDecimal.valueOf(this.getDueInstalments()))
                .paidInstalments(BigDecimal.valueOf(this.getPaidInstalments()))
                .pastDueInstalments(BigDecimal.valueOf(this.getPastDueInstalments()))
                .totalNumberOfInstalments(BigDecimal.valueOf(this.getTotalNumberOfInstalments()))
                .typeNumberOfInstalments(LoansInstalmentsV2.TypeNumberOfInstalmentsEnum.fromValue(this.getTypeNumberOfInstalments()))
                .contractRemainingNumber(BigDecimal.valueOf(this.getContractRemainingNumber()))
                .typeContractRemaining(LoansInstalmentsV2.TypeContractRemainingEnum.valueOf(this.getTypeContractRemaining()))
                .balloonPayments(this.getBalloonPayments().stream().map(ContractBalloonPaymentsEntity::getLoansBalloonPaymentV2).collect(Collectors.toList()));
    }

    public LoansPaymentsV2 getLoansPaymentsDataV2() {
        return new LoansPaymentsV2()
                .paidInstalments(BigDecimal.valueOf(this.getPaidInstalments()))
                .contractOutstandingBalance(BankLambdaUtils.formatAmountV2(this.getContractOutstandingBalance()))
                .releases(this.getContractReleases().stream().map(ContractReleasesEntity::getLoansDTOV2).collect(Collectors.toList()));
    }

    public AccountData createSparseAccountData() {
        return new AccountData().accountId(this.getContractId().toString()).number(this.getContractNumber());
    }

    public static ContractEntity from(CreateContractData contract, UUID accountHolderId) {
        var contractEntity = new ContractEntity();
        contractEntity.setAccountHolderId(accountHolderId);
        contractEntity.setContractNumber(contract.getContractNumber());
        contractEntity.setIpocCode(contract.getIpocCode());
        contractEntity.setProductName(contract.getProductName());
        contractEntity.setContractType(contract.getContractType().toString());
        contractEntity.setProductType(contract.getProductType());
        contractEntity.setProductSubType(contract.getProductSubType());
        contractEntity.setContractDate(contract.getContractDate());
        contractEntity.setDisbursementDate(contract.getDisbursementDate());
        contractEntity.setSettlementDate(contract.getSettlementDate());
        contractEntity.setContractAmount(contract.getContractAmount());
        contractEntity.setCurrency(contract.getCurrency());
        contractEntity.setDueDate(contract.getDueDate());
        contractEntity.setInstalmentPeriodicity(contract.getInstalmentPeriodicity().name());
        contractEntity.setInstalmentPeriodicityAdditionalInfo(contract.getInstalmentPeriodicityAdditionalInfo());
        contractEntity.setFirstInstalmentDueDate(contract.getFirstInstalmentDueDate());
        contractEntity.setCet(contract.getCet());
        contractEntity.setAmortizationScheduled(contract.getAmortizationScheduled().name());
        contractEntity.setAmortizationScheduledAdditionalInfo(contract.getAmortizationScheduledAdditionalInfo());
        contractEntity.setCompanyCnpj(contract.getCompanyCnpj());
        contractEntity.setStatus(contract.getStatus());
        contractEntity.setPaidInstalments(contract.getPaidInstalments().intValue());
        contractEntity.setContractOutstandingBalance(contract.getContractOutstandingBalance());
        contractEntity.setTypeNumberOfInstalments(contract.getTypeNumberOfInstalments().name());
        contractEntity.setTotalNumberOfInstalments(contract.getTotalNumberOfInstalments().intValue());
        contractEntity.setTypeContractRemaining(contract.getTypeContractRemaining().name());
        contractEntity.setContractRemainingNumber(contract.getContractRemainingNumber().intValue());
        contractEntity.setDueInstalments(contract.getDueInstalments().intValue());
        contractEntity.setPastDueInstalments(contract.getPastDueInstalments().intValue());

        var interestRatesEntity = contract.getInterestRates().stream()
                .map(i -> ContractInterestRatesEntity.from(contractEntity, i))
                .collect(Collectors.toSet());
        contractEntity.setInterestRates(interestRatesEntity);

        var feesEntity = contract.getContractedFees().stream()
                .map(f -> ContractedFeesEntity.from(contractEntity, f))
                .collect(Collectors.toSet());
        contractEntity.setContractedFees(feesEntity);

        var financeChargesEntity = contract.getContractedFinanceCharges().stream()
                .map(f -> ContractedFinanceChargesEntity.from(contractEntity, f))
                .collect(Collectors.toSet());
        contractEntity.setContractedFinanceCharges(financeChargesEntity);

        var balloonPaymentsEntity = contract.getBalloonPayments().stream()
                .map(b -> ContractBalloonPaymentsEntity.from(contractEntity, b))
                .collect(Collectors.toSet());
        contractEntity.setBalloonPayments(balloonPaymentsEntity);

        var releasesEntity = contract.getReleases().stream()
                .map(r -> ContractReleasesEntity.from(contractEntity, r))
                .collect(Collectors.toSet());
        contractEntity.setContractReleases(releasesEntity);

        return contractEntity;
    }

    public ResponseContractData getContractData() {
        return new ResponseContractData()
                .contractId(this.contractId)
                .contractNumber(this.contractNumber)
                .ipocCode(this.ipocCode)
                .productName(this.productName)
                .contractType(EnumContractType.valueOf(this.contractType))
                .productType(this.productType)
                .productSubType(this.productSubType)
                .contractDate(this.contractDate)
                .disbursementDate(this.disbursementDate)
                .settlementDate(this.settlementDate)
                .contractAmount(this.contractAmount)
                .currency(this.currency)
                .dueDate(this.dueDate)
                .instalmentPeriodicity(EnumContractInstalmentPeriodicity.valueOf(this.instalmentPeriodicity))
                .instalmentPeriodicityAdditionalInfo(this.instalmentPeriodicityAdditionalInfo)
                .firstInstalmentDueDate(this.firstInstalmentDueDate)
                .cet(this.cet)
                .amortizationScheduled(EnumContracttAmortizationScheduled.valueOf(this.amortizationScheduled))
                .amortizationScheduledAdditionalInfo(this.amortizationScheduledAdditionalInfo)
                .companyCnpj(this.companyCnpj)
                .status(this.status)
                .paidInstalments(BigDecimal.valueOf(this.paidInstalments))
                .contractOutstandingBalance(this.contractOutstandingBalance)
                .typeNumberOfInstalments(EnumTypeNumberOfInstalments.valueOf(this.typeNumberOfInstalments))
                .totalNumberOfInstalments(BigDecimal.valueOf(this.totalNumberOfInstalments))
                .typeContractRemaining(EnumTypeContractRemaining.valueOf(this.typeContractRemaining))
                .contractRemainingNumber(BigDecimal.valueOf(this.contractRemainingNumber))
                .dueInstalments(BigDecimal.valueOf(this.dueInstalments))
                .pastDueInstalments(BigDecimal.valueOf(this.pastDueInstalments))
                .interestRates(this.interestRates.stream().map(ContractInterestRatesEntity::getContractInterestRates).collect(Collectors.toList()))
                .contractedFees(this.contractedFees.stream().map(ContractedFeesEntity::getContractFees).collect(Collectors.toList()))
                .contractedFinanceCharges(this.contractedFinanceCharges.stream().map(ContractedFinanceChargesEntity::getContractFinanceCharge).collect(Collectors.toList()))
                .balloonPayments(this.balloonPayments.stream().map(ContractBalloonPaymentsEntity::getContractBalloonPayment).collect(Collectors.toList()))
                .releases(this.contractReleases.stream().map(ContractReleasesEntity::getContractReleases).collect(Collectors.toList()));
    }

    public ContractEntity update(EditedContractData contract) {
        this.contractNumber = contract.getContractNumber();
        this.ipocCode = contract.getIpocCode();
        this.productName = contract.getProductName();
        this.productType = contract.getProductType();
        this.productSubType = contract.getProductSubType();
        this.contractDate = contract.getContractDate();
        this.disbursementDate = contract.getDisbursementDate();
        this.settlementDate = contract.getSettlementDate();
        this.contractAmount = contract.getContractAmount();
        this.currency = contract.getCurrency();
        this.dueDate = contract.getDueDate();
        this.instalmentPeriodicity = contract.getInstalmentPeriodicity().name();
        this.instalmentPeriodicityAdditionalInfo = contract.getInstalmentPeriodicityAdditionalInfo();
        this.firstInstalmentDueDate = contract.getFirstInstalmentDueDate();
        this.cet = contract.getCet();
        this.amortizationScheduled = contract.getAmortizationScheduled().name();
        this.amortizationScheduledAdditionalInfo = contract.getAmortizationScheduledAdditionalInfo();
        this.companyCnpj = contract.getCompanyCnpj();
        this.status = contract.getStatus();
        this.paidInstalments = contract.getPaidInstalments().intValue();
        this.contractOutstandingBalance = contract.getContractOutstandingBalance();
        this.typeNumberOfInstalments = contract.getTypeNumberOfInstalments().name();
        this.totalNumberOfInstalments = contract.getTotalNumberOfInstalments().intValue();
        this.typeContractRemaining = contract.getTypeContractRemaining().name();
        this.contractRemainingNumber = contract.getContractRemainingNumber().intValue();
        this.dueInstalments = contract.getDueInstalments().intValue();
        this.pastDueInstalments = contract.getPastDueInstalments().intValue();

        var interestRatesEntity = contract.getInterestRates().stream()
                .map(i -> ContractInterestRatesEntity.from(this, i))
                .collect(Collectors.toSet());
        this.interestRates.clear();
        this.interestRates.addAll(interestRatesEntity);

        this.getContractedFees().clear();
        var feesEntity = contract.getContractedFees().stream()
                .map(f -> ContractedFeesEntity.from(this, f))
                .collect(Collectors.toSet());
        this.getContractedFees().addAll(feesEntity);

        this.getContractedFinanceCharges().clear();
        var financeChargesEntity = contract.getContractedFinanceCharges().stream()
                .map(f -> ContractedFinanceChargesEntity.from(this, f))
                .collect(Collectors.toSet());
        this.getContractedFinanceCharges().addAll(financeChargesEntity);

        this.getBalloonPayments().clear();
        var balloonPaymentsEntity = contract.getBalloonPayments().stream()
                .map(b -> ContractBalloonPaymentsEntity.from(this, b))
                .collect(Collectors.toSet());
        this.getBalloonPayments().addAll(balloonPaymentsEntity);

        this.getContractReleases().clear();
        var releasesEntity = contract.getReleases().stream()
                .map(r -> ContractReleasesEntity.from(this, r))
                .collect(Collectors.toSet());
        this.getContractReleases().addAll(releasesEntity);

        return this;
    }
}
