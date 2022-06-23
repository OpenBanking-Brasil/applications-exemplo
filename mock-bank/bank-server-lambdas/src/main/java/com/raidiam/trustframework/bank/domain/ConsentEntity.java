package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.bank.enums.AccountOrContractType;
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

    @Id
    @GeneratedValue
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Column(name = "consent_id", nullable = false, updatable = false, insertable = true)
    private String consentId;

    @OneToOne(cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "business_entity_document_id", referencedColumnName = "business_entity_document_id", nullable = true)
    private BusinessEntityDocumentEntity businessEntityDocument;

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

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @NotAudited
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "consent")
    private Set<ConsentPermissionEntity> permissions = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @NotAudited
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "consent")
    private Set<ConsentAccountEntity> consentAccounts = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @NotAudited
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "consent")
    private Set<ConsentContractEntity> consentContracts = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @NotAudited
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "consent")
    private Set<ConsentCreditCardAccountsEntity> consentCreditCardAccounts = new HashSet<>();

    @EqualsAndHashCode.Include
    private Instant expirationCompareExpirationDateTime () {
        return Optional.ofNullable(expirationDateTime).map(Date::toInstant).orElse(null);
    }

    @EqualsAndHashCode.Include
    private Instant expirationCompareTransactionFromDateTimeTime () {
        return Optional.ofNullable(transactionFromDateTime).map(Date::toInstant).orElse(null);
    }

    @EqualsAndHashCode.Include
    private Instant expirationCompareTransactionToDateTime () {
        return Optional.ofNullable(transactionToDateTime).map(Date::toInstant).orElse(null);
    }

    @EqualsAndHashCode.Include
    private Instant expirationCompareCreationDateTime () {
        return Optional.ofNullable(creationDateTime).map(Date::toInstant).orElse(null);
    }

    @EqualsAndHashCode.Include
    private Instant expirationCompareStatusUpdateDateTime () {
        return Optional.ofNullable(statusUpdateDateTime).map(Date::toInstant).orElse(null);
    }

    public static ConsentEntity fromRequest (CreateConsent req) {
        ConsentEntity entity = new ConsentEntity();
        UUID uuid = UUID.randomUUID();
        String consentId = String.format("urn:raidiambank:%s", uuid.toString());
        entity.setConsentId(consentId);
        entity.setExpirationDateTime(BankLambdaUtils.offsetDateToDate(req.getData().getExpirationDateTime()));
        entity.setTransactionFromDateTime(BankLambdaUtils.offsetDateToDate(req.getData().getTransactionFromDateTime()));
        entity.setTransactionToDateTime(BankLambdaUtils.offsetDateToDate(req.getData().getTransactionToDateTime()));
        entity.setCreationDateTime(Date.from(Instant.now()));
        entity.setStatusUpdateDateTime(Date.from(Instant.now()));
        entity.setStatus(ResponseConsentData.StatusEnum.AWAITING_AUTHORISATION.toString());
        entity.setBusinessEntityDocument(BusinessEntityDocumentEntity.from(req.getData().getBusinessEntity()));
        // needs a look later
        return entity;
    }

    public ResponseConsent toResponseConsent() {

        ResponseConsentData rcd = new ResponseConsentData()
            .creationDateTime(BankLambdaUtils.dateToOffsetDate(creationDateTime))
            .statusUpdateDateTime(BankLambdaUtils.dateToOffsetDate(statusUpdateDateTime))
            .status(ResponseConsentData.StatusEnum.fromValue(status))
            .expirationDateTime(BankLambdaUtils.dateToOffsetDate(expirationDateTime))
            .expirationDateTime(BankLambdaUtils.dateToOffsetDate(expirationDateTime))
            .transactionFromDateTime(BankLambdaUtils.dateToOffsetDate(transactionFromDateTime))
            .transactionToDateTime(BankLambdaUtils.dateToOffsetDate(transactionToDateTime))
            .consentId(consentId);

        permissions.stream()
                .map(ConsentPermissionEntity::getDTO)
                .forEach(rcd::addPermissionsItem);

        return new ResponseConsent()
                .data(rcd);
    }

    // TODO - this must be updated when Credit Card accounts are in place, and when the
    // response message has room for other contract types
    public ResponseConsentFull getDTOInternal() {

        List<ResponseConsentFullData.PermissionsEnum> thePermissions =
                permissions.stream()
                .map(p -> {
                    return ResponseConsentFullData.PermissionsEnum.fromValue(p.getPermission());
                })
                .collect(Collectors.toList());

        List<String> dtoAccountIds = consentAccounts.stream()
                .map(ConsentAccountEntity::getAccount)
                .map(AccountEntity::getAccountId)
                .map(UUID::toString)
                .collect(Collectors.toList());

        List<String> dtoCreditCardAccountIds = consentCreditCardAccounts.stream()
                .map(ConsentCreditCardAccountsEntity::getCreditCardAccount)
                .map(CreditCardAccountsEntity::getCreditCardAccountId)
                .map(UUID::toString)
                .collect(Collectors.toList());

        var contractIdByTypeMap = consentContracts.stream()
                .map(ConsentContractEntity::getContract)
                .collect(Collectors.groupingBy(ContractEntity::getContractType,
                        Collectors.mapping(ContractEntity::getContractId,
                                Collectors.mapping(UUID::toString, Collectors.toList()))));

        List<String> dtoLoanAccountIds = contractIdByTypeMap.getOrDefault(AccountOrContractType.LOAN.toString(), Collections.emptyList());

        List<String> dtoFinanceAccountIds = contractIdByTypeMap.getOrDefault(AccountOrContractType.FINANCING.toString(), Collections.emptyList());

        List<String> dtoInvoiceFinanceAccountIds = contractIdByTypeMap.getOrDefault(AccountOrContractType.INVOICE_FINANCING.toString(), Collections.emptyList());

        List<String> dtoOverdraftAccountIds = contractIdByTypeMap.getOrDefault(AccountOrContractType.UNARRANGED_ACCOUNT_OVERDRAFT.toString(), Collections.emptyList());

        ResponseConsentFullData data = new ResponseConsentFullData()
                .status(ResponseConsentFullData.StatusEnum.fromValue(status))
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
        .document(new LoggedUserDocument()
            .identification(accountHolder.getDocumentIdentification())
            .rel(accountHolder.getDocumentRel())));
        if(businessEntityDocument != null) {
            data.setBusinessEntity(new BusinessEntity()
            .document(new BusinessEntityDocument()
                    .identification(businessEntityDocument.getIdentification())
                    .rel(businessEntityDocument.getRel())
            ));
        }
        data.setSub(Optional.ofNullable(accountHolder).map(AccountHolderEntity::getUserId).orElse(null));
        return new ResponseConsentFull().data(data);
    }
}
