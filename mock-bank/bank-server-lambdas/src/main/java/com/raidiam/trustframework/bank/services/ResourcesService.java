package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.domain.*;
import com.raidiam.trustframework.bank.enums.AccountOrContractType;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.context.annotation.Bean;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.raidiam.trustframework.mockbank.models.generated.ResponseResourceListData.StatusEnum.AVAILABLE;

@Bean
public class  ResourcesService extends BaseBankService {

    private static final Logger LOG = LoggerFactory.getLogger(ResourcesService.class);

    private static final Map<AccountOrContractType, ResponseResourceListData.TypeEnum> TYPE_MAP = Map.of(
            AccountOrContractType.ACCOUNT, ResponseResourceListData.TypeEnum.ACCOUNT,
            AccountOrContractType.CREDIT_CARD_ACCOUNT, ResponseResourceListData.TypeEnum.CREDIT_CARD_ACCOUNT,
            AccountOrContractType.LOAN, ResponseResourceListData.TypeEnum.LOAN,
            AccountOrContractType.FINANCING, ResponseResourceListData.TypeEnum.FINANCING,
            AccountOrContractType.UNARRANGED_ACCOUNT_OVERDRAFT , ResponseResourceListData.TypeEnum.UNARRANGED_ACCOUNT_OVERDRAFT,
            AccountOrContractType.INVOICE_FINANCING, ResponseResourceListData.TypeEnum.INVOICE_FINANCING
    );

    private static final Map<AccountOrContractType, CreateConsentData.PermissionsEnum> PERMISSION_MAP = Map.of(
            AccountOrContractType.ACCOUNT, CreateConsentData.PermissionsEnum.ACCOUNTS_READ,
            AccountOrContractType.CREDIT_CARD_ACCOUNT, CreateConsentData.PermissionsEnum.CREDIT_CARDS_ACCOUNTS_READ,
            AccountOrContractType.LOAN, CreateConsentData.PermissionsEnum.LOANS_READ,
            AccountOrContractType.FINANCING, CreateConsentData.PermissionsEnum.FINANCINGS_READ,
            AccountOrContractType.UNARRANGED_ACCOUNT_OVERDRAFT , CreateConsentData.PermissionsEnum.UNARRANGED_ACCOUNTS_OVERDRAFT_READ,
            AccountOrContractType.INVOICE_FINANCING, CreateConsentData.PermissionsEnum.INVOICE_FINANCINGS_READ
    );

    public ResponseResourceList getResourceList(Pageable pageable, @NotNull String consentId) {
        LOG.info("Getting resources response for consent id {}", consentId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.RESOURCES_READ);

        var permissions = BankLambdaUtils.getConsentPermissions(consentEntity);
        checkConsentGroup(permissions);
        var accounts = consentEntity.getConsentAccounts().stream().map(ConsentAccountEntity::getAccount).collect(Collectors.toList());
        var contracts = consentEntity.getConsentContracts().stream().map(ConsentContractEntity::getContract).collect(Collectors.toList());
        var creditCardsAccounts = consentEntity.getConsentCreditCardAccounts().stream().map(ConsentCreditCardAccountsEntity::getCreditCardAccount).collect(Collectors.toList());

        LOG.info("Found {} accounts to include in resource response", accounts.size());
        LOG.info("Found {} contracts to include in resource response", contracts.size());
        LOG.info("Found {} creditCardsAccounts to include in resource response", creditCardsAccounts.size());

        Map<ResponseResourceListData, Date> responseMap = new HashMap<>();
        addAccountsToResources(accounts, consentEntity, responseMap, permissions.contains(PERMISSION_MAP.get(AccountOrContractType.ACCOUNT)));
        addContractsToResources(contracts, consentEntity, responseMap, permissions);
        addCreditCardsAccountsToResources(creditCardsAccounts, consentEntity, responseMap, permissions.contains(PERMISSION_MAP.get(AccountOrContractType.CREDIT_CARD_ACCOUNT)));

        var resourcePage = getPage(responseMap, pageable);
        var response = new ResponseResourceList().data(resourcePage.getContent());
        response.setMeta(new ResponseResourceListMeta()
                .totalRecords(response.getData().size())
                .totalPages(resourcePage.getTotalPages())
                .requestDateTime(OffsetDateTime.now()));
        return response;
    }

    private void addAccountsToResources(List<AccountEntity> accounts, ConsentEntity consent, Map<ResponseResourceListData, Date> responseMap, boolean permitted) {
        for(var accountEntity : accounts) {
            var resourceType = TYPE_MAP.get(AccountOrContractType.ACCOUNT);
            var data = new ResponseResourceListData();
            if(permitted) {
                var resourceStatus = getStatus(accountEntity, consent);
                var resourceId = Optional.of(resourceStatus).filter(AVAILABLE::equals).map(a -> accountEntity.getAccountId().toString()).orElse(null);
                LOG.info("Adding account {}, status {}, resourceId {}, type {}", accountEntity.getAccountId(), resourceStatus, resourceId, resourceType);
                data.status(resourceStatus).resourceId(resourceId).type(resourceType);

            } else {
                LOG.error("Account read permissions not found in consent id {}", consent.getConsentId());
                data.status(ResponseResourceListData.StatusEnum.UNAVAILABLE).resourceId(null).type(resourceType);
            }
            responseMap.put(data, accountEntity.getCreatedAt());
        }
    }

    private void addContractsToResources(List<ContractEntity> contracts, ConsentEntity consent, Map<ResponseResourceListData, Date> responseMap, Set<CreateConsentData.PermissionsEnum> permissions) {
        for(var contractEntity : contracts) {
            var accountType = AccountOrContractType.valueOf(contractEntity.getContractType());
            var resourceType = TYPE_MAP.get(accountType);
            var requiredPermission = PERMISSION_MAP.get(accountType);

            var data = new ResponseResourceListData();
            if(permissions.contains(requiredPermission)) {
                var resourceStatus = getStatus(contractEntity, consent);
                var resourceId = Optional.of(resourceStatus).filter(AVAILABLE::equals).map(a -> contractEntity.getContractId().toString()).orElse(null);
                LOG.info("Adding contract {}, status {}, resourceId {}, type {}", contractEntity.getContractId(), resourceStatus, resourceId, resourceType);
                data.status(resourceStatus).resourceId(resourceId).type(resourceType);
            } else {
                LOG.error("{} permissions not found in consent id {}", requiredPermission, consent.getConsentId());
                data.status(ResponseResourceListData.StatusEnum.UNAVAILABLE).resourceId(null).type(resourceType);
            }
            responseMap.put(data, contractEntity.getCreatedAt());
        }
    }

    private void addCreditCardsAccountsToResources(List<CreditCardAccountsEntity> creditCardsAccounts, ConsentEntity consent, Map<ResponseResourceListData, Date> responseMap, boolean permitted) {
        for(var creditCardsAccountsEntity : creditCardsAccounts) {
            var resourceType = TYPE_MAP.get(AccountOrContractType.CREDIT_CARD_ACCOUNT);
            var data = new ResponseResourceListData();
            if(permitted) {
                var resourceStatus = getStatus(creditCardsAccountsEntity, consent);
                var resourceId = Optional.of(resourceStatus).filter(AVAILABLE::equals).map(a -> creditCardsAccountsEntity.getCreditCardAccountId().toString()).orElse(null);
                LOG.info("Adding Credit Cards Accounts {}, status {}, resourceId {}, type {}", creditCardsAccountsEntity.getCreditCardAccountId(), resourceStatus, resourceId, resourceType);
                data.status(resourceStatus).resourceId(resourceId).type(resourceType);
            } else {
                LOG.error("Credit Cards Accounts read permissions not found in consent id {}", consent.getConsentId());
                data.status(ResponseResourceListData.StatusEnum.UNAVAILABLE).resourceId(null).type(resourceType);
            }
            responseMap.put(data, creditCardsAccountsEntity.getCreatedAt());
        }
    }

    private ResponseResourceListData.StatusEnum getStatus(HasStatusInterface accountOrContract, ConsentEntity consent) {
        if (AVAILABLE.toString().equals(accountOrContract.getStatus())) {
            switch (ResponseConsentData.StatusEnum.fromValue(consent.getStatus())) {
                case AUTHORISED:
                    return AVAILABLE;
                case AWAITING_AUTHORISATION:
                    return ResponseResourceListData.StatusEnum.PENDING_AUTHORISATION;
                case REJECTED:
                default:
                    return ResponseResourceListData.StatusEnum.UNAVAILABLE;
            }
        } else {
            return ResponseResourceListData.StatusEnum.UNAVAILABLE;
        }
    }

    private static Page<ResponseResourceListData> getPage(Map<ResponseResourceListData, Date> responseMap, Pageable pageable) {
        //Sort by create date and convert to list
        List<ResponseResourceListData> list = responseMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (Pageable.unpaged().equals(pageable)) return Page.of(list, pageable, list.size());

        int page = pageable.getNumber();
        int pageSize = pageable.getSize();

        if (pageSize <= 0 || page < 0) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Bad Request page size: " + pageSize +
                                                                              " and page: " + page);
        }

        int fromIndex = page * pageSize;
        if (list.isEmpty() || list.size() < fromIndex) {
            return Page.of(Collections.emptyList(), pageable, 0);
        }

        var slice = list.subList(fromIndex, Math.min(fromIndex + pageSize, list.size()));
        return Page.of(slice, pageable, list.size());
    }

    // this may change in later revisions of the BR standards, for now this should be enough.
    private void checkConsentGroup(Set<CreateConsentData.PermissionsEnum> permissions) {
        // If none of these are present, we return a 404, as per errata document
        if (!permissions.contains(CreateConsentData.PermissionsEnum.ACCOUNTS_READ)
                && !permissions.contains(CreateConsentData.PermissionsEnum.CREDIT_CARDS_ACCOUNTS_READ)
                && !permissions.contains(CreateConsentData.PermissionsEnum.FINANCINGS_READ)
                && !permissions.contains(CreateConsentData.PermissionsEnum.INVOICE_FINANCINGS_READ)
                && !permissions.contains(CreateConsentData.PermissionsEnum.LOANS_READ)
                && !permissions.contains(CreateConsentData.PermissionsEnum.UNARRANGED_ACCOUNTS_OVERDRAFT_READ)){
            throw new HttpStatusException(HttpStatus.NOT_FOUND, "Resource not found, no appropriate permissions attached to consent");
        }
    }
}
