package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.enums.ResourceType;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "consents")
public class ConsentEntity extends BaseEntity {

    public static final String CONSENT_ID_FORMAT = "urn:raidiambank:%s";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Column(name = "consent_id", nullable = false, updatable = false, insertable = true)
    private String consentId;

    @Column(name = "account_holder_id")
    private UUID accountHolderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "account_holder_id", referencedColumnName = "account_holder_id", nullable = false, insertable = false, updatable = false)
    private AccountHolderEntity accountHolder;

    @EqualsAndHashCode.Exclude
    @Column(name = "expiration_date_time")
    private Date expirationDateTime;

    @EqualsAndHashCode.Exclude
    @Column(name = "transaction_from_date_time")
    private Date transactionFromDateTime;

    @EqualsAndHashCode.Exclude
    @Column(name = "transaction_to_date_time")
    private Date transactionToDateTime;

    @EqualsAndHashCode.Exclude
    @NotNull
    @Column(name = "creation_date_time")
    private Date creationDateTime;

    @EqualsAndHashCode.Exclude
    @NotNull
    @Column(name = "status_update_date_time")
    private Date statusUpdateDateTime;

    @NotNull
    @Column(name = "status")
    private String status;

    @Column(name = "client_id")
    private String clientId;
    @Column(name = "rejected_by")
    private String rejectedBy;
    @Column(name = "rejection_code")
    private String rejectionCode;
    @Column(name = "rejection_additional_information")
    private String rejectionAdditionalInformation;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @NotAudited
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "consent")
    private List<ConsentPermissionEntity> consentPermissions = new ArrayList<>();


    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @NotAudited
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "consent")
    private List<ConsentExtensionEntity> consentExtensions = new ArrayList<>();


    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @NotAudited
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinTable(name = "consent_accounts",
            joinColumns = @JoinColumn(name = "consent_id", referencedColumnName = "consent_id"),
            inverseJoinColumns = @JoinColumn(name = "account_id", referencedColumnName = "account_id"))
    private Set<AccountEntity> accounts = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @NotAudited
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinTable(name = "consent_contracts",
            joinColumns = @JoinColumn(name = "consent_id", referencedColumnName = "consent_id"),
            inverseJoinColumns = @JoinColumn(name = "contract_id", referencedColumnName = "contract_id"))
    private Set<ContractEntity> contracts = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @NotAudited
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinTable(name = "consent_credit_card_accounts",
            joinColumns = @JoinColumn(name = "consent_id", referencedColumnName = "consent_id"),
            inverseJoinColumns = @JoinColumn(name = "credit_card_account_id", referencedColumnName = "credit_card_account_id"))
    private Set<CreditCardAccountsEntity> creditCardAccounts = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @NotAudited
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinTable(name = "consent_investment",
            joinColumns = @JoinColumn(name = "consent_id", referencedColumnName = "consent_id"),
            inverseJoinColumns = @JoinColumn(name = "bank_fixed_income_id", referencedColumnName = "investment_id"))
    private Set<BankFixedIncomesEntity> bankFixedIncomesAccounts = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @NotAudited
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinTable(name = "consent_investment",
            joinColumns = @JoinColumn(name = "consent_id", referencedColumnName = "consent_id"),
            inverseJoinColumns = @JoinColumn(name = "credit_fixed_income_id", referencedColumnName = "investment_id"))
    private Set<CreditFixedIncomesEntity> creditFixedIncomesAccounts = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @NotAudited
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinTable(name = "consent_investment",
            joinColumns = @JoinColumn(name = "consent_id", referencedColumnName = "consent_id"),
            inverseJoinColumns = @JoinColumn(name = "variable_income_id", referencedColumnName = "investment_id"))
    private Set<VariableIncomesEntity> variableIncomesAccounts = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @NotAudited
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinTable(name = "consent_investment",
            joinColumns = @JoinColumn(name = "consent_id", referencedColumnName = "consent_id"),
            inverseJoinColumns = @JoinColumn(name = "treasure_title_id", referencedColumnName = "investment_id"))
    private Set<TreasureTitlesEntity> treasureTitlesAccounts = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @NotAudited
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinTable(name = "consent_investment",
            joinColumns = @JoinColumn(name = "consent_id", referencedColumnName = "consent_id"),
            inverseJoinColumns = @JoinColumn(name = "fund_id", referencedColumnName = "investment_id"))
    private Set<FundsEntity> fundsAccounts = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @NotAudited
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinTable(name = "consent_exchanges_operation",
            joinColumns = @JoinColumn(name = "consent_id", referencedColumnName = "consent_id"),
            inverseJoinColumns = @JoinColumn(name = "operation_id", referencedColumnName = "operation_id"))
    private Set<ExchangesOperationEntity> exchangesOperations = new HashSet<>();

    @Column(name = "business_document_identification")
    private String businessDocumentIdentification;

    @Column(name = "business_document_rel")
    private String businessDocumentRel;

    @EqualsAndHashCode.Include
    private Instant expirationCompareExpirationDateTime() {
        return Optional.ofNullable(expirationDateTime).map(Date::toInstant).orElse(null);
    }

    @EqualsAndHashCode.Include
    private Instant expirationCompareTransactionFromDateTimeTime() {
        return Optional.ofNullable(transactionFromDateTime).map(Date::toInstant).orElse(null);
    }

    @EqualsAndHashCode.Include
    private Instant expirationCompareTransactionToDateTime() {
        return Optional.ofNullable(transactionToDateTime).map(Date::toInstant).orElse(null);
    }

    @EqualsAndHashCode.Include
    private Instant expirationCompareCreationDateTime() {
        return Optional.ofNullable(creationDateTime).map(Date::toInstant).orElse(null);
    }

    @EqualsAndHashCode.Include
    private Instant expirationCompareStatusUpdateDateTime() {
        return Optional.ofNullable(statusUpdateDateTime).map(Date::toInstant).orElse(null);
    }

    public static ConsentEntity fromRequest(CreateConsent req) {
        ConsentEntity entity = new ConsentEntity();
        UUID uuid = UUID.randomUUID();
        String consentId = String.format(CONSENT_ID_FORMAT, uuid);
        entity.setConsentId(consentId);
        entity.setExpirationDateTime(BankLambdaUtils.offsetDateToDate(req.getData().getExpirationDateTime()));
        entity.setTransactionFromDateTime(BankLambdaUtils.offsetDateToDate(req.getData().getTransactionFromDateTime()));
        entity.setTransactionToDateTime(BankLambdaUtils.offsetDateToDate(req.getData().getTransactionToDateTime()));
        entity.setCreationDateTime(Date.from(Instant.now()));
        entity.setStatusUpdateDateTime(Date.from(Instant.now()));
        entity.setStatus(EnumConsentStatus.AWAITING_AUTHORISATION.toString());

        Optional.ofNullable(req.getData().getBusinessEntity())
                .map(BusinessEntity::getDocument)
                .ifPresent(d -> {
                    entity.setBusinessDocumentIdentification(d.getIdentification());
                    entity.setBusinessDocumentRel(d.getRel());
                });
        // needs a look later
        return entity;
    }

    public static ConsentEntity fromRequest(CreateConsentV2 req) {
        ConsentEntity entity = new ConsentEntity();
        entity.setConsentId(String.format(CONSENT_ID_FORMAT, UUID.randomUUID()));
        entity.setExpirationDateTime(BankLambdaUtils.offsetDateToDate(req.getData().getExpirationDateTime()));
        entity.setCreationDateTime(Date.from(Instant.now()));
        entity.setStatusUpdateDateTime(Date.from(Instant.now()));
        entity.setStatus(EnumConsentStatus.AWAITING_AUTHORISATION.toString());

        Optional.ofNullable(req.getData().getBusinessEntity())
                .map(BusinessEntity::getDocument)
                .ifPresent(d -> {
                    entity.setBusinessDocumentIdentification(d.getIdentification());
                    entity.setBusinessDocumentRel(d.getRel());
                });
        return entity;
    }

    public static ConsentEntity fromRequest(CreateConsentV3 req) {
        ConsentEntity entity = new ConsentEntity();
        entity.setConsentId(String.format(CONSENT_ID_FORMAT, UUID.randomUUID()));
        entity.setExpirationDateTime(BankLambdaUtils.offsetDateToDate(req.getData().getExpirationDateTime()));
        entity.setCreationDateTime(Date.from(Instant.now()));
        entity.setStatusUpdateDateTime(Date.from(Instant.now()));
        entity.setStatus(EnumConsentStatus.AWAITING_AUTHORISATION.toString());

        Optional.ofNullable(req.getData().getBusinessEntity())
                .map(BusinessEntity::getDocument)
                .ifPresent(d -> {
                    entity.setBusinessDocumentIdentification(d.getIdentification());
                    entity.setBusinessDocumentRel(d.getRel());
                });
        return entity;
    }

    public ResponseConsent toResponseConsent() {

        ResponseConsentData rcd = new ResponseConsentData()
                .creationDateTime(BankLambdaUtils.dateToOffsetDate(creationDateTime))
                .statusUpdateDateTime(BankLambdaUtils.dateToOffsetDate(statusUpdateDateTime))
                .status(EnumConsentStatus.fromValue(status))
                .expirationDateTime(BankLambdaUtils.dateToOffsetDate(expirationDateTime))
                .expirationDateTime(BankLambdaUtils.dateToOffsetDate(expirationDateTime))
                .transactionFromDateTime(BankLambdaUtils.dateToOffsetDate(transactionFromDateTime))
                .transactionToDateTime(BankLambdaUtils.dateToOffsetDate(transactionToDateTime))
                .consentId(consentId);

        consentPermissions.stream()
                .map(ConsentPermissionEntity::getDTO)
                .forEach(rcd::addPermissionsItem);

        return new ResponseConsent()
                .data(rcd);
    }

    public ResponseConsentV2 toResponseConsentV2() {

        ResponseConsentV2Data rcd = new ResponseConsentV2Data()
                .creationDateTime(BankLambdaUtils.dateToOffsetDate(creationDateTime))
                .statusUpdateDateTime(BankLambdaUtils.dateToOffsetDate(statusUpdateDateTime))
                .status(EnumConsentStatus.fromValue(status))
                .expirationDateTime(BankLambdaUtils.dateToOffsetDate(expirationDateTime))
                .consentId(consentId);

        consentPermissions.stream()
                .map(ConsentPermissionEntity::getDTO)
                .forEach(rcd::addPermissionsItem);

        return new ResponseConsentV2()
                .data(rcd);
    }

    public ResponseConsentV3 toResponseConsentV3() {

        ResponseConsentV3Data rcd = new ResponseConsentV3Data()
                .creationDateTime(BankLambdaUtils.dateToOffsetDate(creationDateTime))
                .statusUpdateDateTime(BankLambdaUtils.dateToOffsetDate(statusUpdateDateTime))
                .status(EnumConsentStatus.fromValue(status))
                .expirationDateTime(BankLambdaUtils.dateToOffsetDate(expirationDateTime))
                .consentId(consentId);

        consentPermissions.stream()
                .map(ConsentPermissionEntity::getDTO)
                .forEach(rcd::addPermissionsItem);

        return new ResponseConsentV3()
                .data(rcd);
    }

    public ResponseConsentReadV2 toResponseConsentReadV2() {

        ResponseConsentReadV2Data rcd = new ResponseConsentReadV2Data()
                .consentId(this.consentId)
                .creationDateTime(BankLambdaUtils.dateToOffsetDate(this.creationDateTime))
                .status(EnumConsentStatus.fromValue(this.status))
                .statusUpdateDateTime(BankLambdaUtils.dateToOffsetDate(this.statusUpdateDateTime))
                .expirationDateTime(BankLambdaUtils.dateToOffsetDate(this.expirationDateTime));

        consentPermissions.stream()
                .map(ConsentPermissionEntity::getDTO)
                .forEach(rcd::addPermissionsItem);

        if (EnumConsentStatus.fromValue(this.status).equals(EnumConsentStatus.REJECTED)){
            rcd.rejection(new ResponseConsentReadV2DataRejection()
                    .rejectedBy(EnumRejectedByV2.fromValue(this.rejectedBy))
                    .reason(new RejectedReasonV2()
                            .code(EnumReasonCodeV2.fromValue(this.rejectionCode))
                            .additionalInformation(this.rejectionAdditionalInformation)));
        }

        return new ResponseConsentReadV2().data(rcd);
    }

    public ResponseConsentReadV3 toResponseConsentReadV3() {

        ResponseConsentReadV3Data rcd = new ResponseConsentReadV3Data()
                .consentId(this.consentId)
                .creationDateTime(BankLambdaUtils.dateToOffsetDate(this.creationDateTime))
                .status(EnumConsentStatus.fromValue(this.status))
                .statusUpdateDateTime(BankLambdaUtils.dateToOffsetDate(this.statusUpdateDateTime))
                .expirationDateTime(BankLambdaUtils.dateToOffsetDate(this.expirationDateTime));

        consentPermissions.stream()
                .map(ConsentPermissionEntity::getDTO)
                .forEach(rcd::addPermissionsItem);

        if (EnumConsentStatus.fromValue(this.status).equals(EnumConsentStatus.REJECTED)){
            rcd.rejection(new ResponseConsentReadV2DataRejection()
                    .rejectedBy(EnumRejectedByV2.fromValue(this.rejectedBy))
                    .reason(new RejectedReasonV2()
                            .code(EnumReasonCodeV2.fromValue(this.rejectionCode))
                            .additionalInformation(this.rejectionAdditionalInformation)));
        }

        return new ResponseConsentReadV3().data(rcd);
    }

    public ResponseConsentFull getDTOInternal() {

        List<EnumConsentPermissions> thePermissions =
                consentPermissions.stream()
                        .map(p -> EnumConsentPermissions.fromValue(p.getPermission()))
                        .collect(Collectors.toList());

        List<String> dtoAccountIds = accounts.stream()
                .map(AccountEntity::getAccountId)
                .map(UUID::toString)
                .collect(Collectors.toList());

        List<String> dtoCreditCardAccountIds = creditCardAccounts.stream()
                .map(CreditCardAccountsEntity::getCreditCardAccountId)
                .map(UUID::toString)
                .collect(Collectors.toList());

        var contractIdByTypeMap = contracts.stream()
                .collect(Collectors.groupingBy(ContractEntity::getContractType,
                        Collectors.mapping(ContractEntity::getContractId,
                                Collectors.mapping(UUID::toString, Collectors.toList()))));

        List<String> dtoLoanAccountIds = contractIdByTypeMap.getOrDefault(ResourceType.LOAN.toString(), Collections.emptyList());

        List<String> dtoFinanceAccountIds = contractIdByTypeMap.getOrDefault(ResourceType.FINANCING.toString(), Collections.emptyList());

        List<String> dtoInvoiceFinanceAccountIds = contractIdByTypeMap.getOrDefault(ResourceType.INVOICE_FINANCING.toString(), Collections.emptyList());

        List<String> dtoOverdraftAccountIds = contractIdByTypeMap.getOrDefault(ResourceType.UNARRANGED_ACCOUNT_OVERDRAFT.toString(), Collections.emptyList());

        ResponseConsentFullData data = new ResponseConsentFullData()
                .status(EnumConsentStatus.fromValue(status))
                .clientId(clientId)
                .linkedAccountIds(dtoAccountIds)
                .linkedCreditCardAccountIds(dtoCreditCardAccountIds)
                .linkedLoanAccountIds(dtoLoanAccountIds)
                .linkedFinancingAccountIds(dtoFinanceAccountIds)
                .linkedInvoiceFinancingAccountIds(dtoInvoiceFinanceAccountIds)
                .linkedUnarrangedOverdraftAccountIds(dtoOverdraftAccountIds);
        data.setCreationDateTime(BankLambdaUtils.dateToOffsetDate(creationDateTime));
        data.setStatusUpdateDateTime(BankLambdaUtils.dateToOffsetDate(statusUpdateDateTime));
        data.setConsentId(consentId);
        data.setExpirationDateTime(BankLambdaUtils.dateToOffsetDate(expirationDateTime));
        data.setTransactionFromDateTime(BankLambdaUtils.dateToOffsetDate(transactionFromDateTime));
        data.setTransactionToDateTime(BankLambdaUtils.dateToOffsetDate(transactionToDateTime));
        data.setPermissions(thePermissions);
        data.setLoggedUser(new LoggedUser()
                .document(new Document()
                        .identification(accountHolder.getDocumentIdentification())
                        .rel(accountHolder.getDocumentRel())));
        if (businessDocumentIdentification != null) {
            data.setBusinessEntity(new BusinessEntity()
                    .document(new BusinessEntityDocument()
                            .identification(businessDocumentIdentification)
                            .rel(businessDocumentRel))
            );
        }
        data.setSub(Optional.ofNullable(accountHolder).map(AccountHolderEntity::getUserId).orElse(null));
        return new ResponseConsentFull().data(data);
    }

    public ResponseConsentFullV2 getDTOInternalV2() {

        List<EnumConsentPermissions> thePermissions =
                consentPermissions.stream()
                        .map(p -> EnumConsentPermissions.fromValue(p.getPermission()))
                        .collect(Collectors.toList());

        List<String> dtoAccountIds = accounts.stream()
                .map(AccountEntity::getAccountId)
                .map(UUID::toString)
                .collect(Collectors.toList());

        List<String> dtoCreditCardAccountIds = creditCardAccounts.stream()
                .map(CreditCardAccountsEntity::getCreditCardAccountId)
                .map(UUID::toString)
                .collect(Collectors.toList());

        var contractIdByTypeMap = contracts.stream()
                .collect(Collectors.groupingBy(ContractEntity::getContractType,
                        Collectors.mapping(ContractEntity::getContractId,
                                Collectors.mapping(UUID::toString, Collectors.toList()))));

        List<String> dtoLoanAccountIds = contractIdByTypeMap.getOrDefault(ResourceType.LOAN.toString(), Collections.emptyList());

        List<String> dtoFinanceAccountIds = contractIdByTypeMap.getOrDefault(ResourceType.FINANCING.toString(), Collections.emptyList());

        List<String> dtoInvoiceFinanceAccountIds = contractIdByTypeMap.getOrDefault(ResourceType.INVOICE_FINANCING.toString(), Collections.emptyList());

        List<String> dtoOverdraftAccountIds = contractIdByTypeMap.getOrDefault(ResourceType.UNARRANGED_ACCOUNT_OVERDRAFT.toString(), Collections.emptyList());

        ResponseConsentFullV2Data data = new ResponseConsentFullV2Data()
                .status(EnumConsentStatus.fromValue(status))
                .clientId(clientId)
                .linkedAccountIds(dtoAccountIds)
                .linkedCreditCardAccountIds(dtoCreditCardAccountIds)
                .linkedLoanAccountIds(dtoLoanAccountIds)
                .linkedFinancingAccountIds(dtoFinanceAccountIds)
                .linkedInvoiceFinancingAccountIds(dtoInvoiceFinanceAccountIds)
                .linkedUnarrangedOverdraftAccountIds(dtoOverdraftAccountIds);
        data.setCreationDateTime(BankLambdaUtils.dateToOffsetDate(creationDateTime));
        data.setStatusUpdateDateTime(BankLambdaUtils.dateToOffsetDate(statusUpdateDateTime));
        data.setConsentId(consentId);
        data.setExpirationDateTime(BankLambdaUtils.dateToOffsetDate(expirationDateTime));
        data.setPermissions(thePermissions);
        data.setLoggedUser(new LoggedUser()
                .document(new Document()
                        .identification(accountHolder.getDocumentIdentification())
                        .rel(accountHolder.getDocumentRel())));
        if (businessDocumentIdentification != null) {
            data.setBusinessEntity(new BusinessEntity()
                    .document(new BusinessEntityDocument()
                            .identification(businessDocumentIdentification)
                            .rel(businessDocumentRel))
            );
        }
        data.setSub(Optional.ofNullable(accountHolder).map(AccountHolderEntity::getUserId).orElse(null));
        return new ResponseConsentFullV2().data(data);
    }
}
