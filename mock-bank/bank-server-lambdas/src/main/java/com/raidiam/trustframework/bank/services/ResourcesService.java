package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.domain.*;
import com.raidiam.trustframework.bank.enums.ResourceType;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

import static com.raidiam.trustframework.mockbank.models.generated.ResponseResourceListData.StatusEnum.*;

@Singleton
@Transactional
public class ResourcesService extends BaseBankService {

    private static final Logger LOG = LoggerFactory.getLogger(ResourcesService.class);

    private static final Map<ResourceType, ResponseResourceListData.TypeEnum> TYPE_MAP = Map.ofEntries(
            Map.entry(ResourceType.ACCOUNT, ResponseResourceListData.TypeEnum.ACCOUNT),
            Map.entry(ResourceType.CREDIT_CARD_ACCOUNT, ResponseResourceListData.TypeEnum.CREDIT_CARD_ACCOUNT),
            Map.entry(ResourceType.LOAN, ResponseResourceListData.TypeEnum.LOAN),
            Map.entry(ResourceType.FINANCING, ResponseResourceListData.TypeEnum.FINANCING),
            Map.entry(ResourceType.UNARRANGED_ACCOUNT_OVERDRAFT, ResponseResourceListData.TypeEnum.UNARRANGED_ACCOUNT_OVERDRAFT),
            Map.entry(ResourceType.INVOICE_FINANCING, ResponseResourceListData.TypeEnum.INVOICE_FINANCING),
            Map.entry(ResourceType.EXCHANGE, ResponseResourceListData.TypeEnum.EXCHANGE),
            Map.entry(ResourceType.BANK_FIXED_INCOME, ResponseResourceListData.TypeEnum.BANK_FIXED_INCOME),
            Map.entry(ResourceType.CREDIT_FIXED_INCOME, ResponseResourceListData.TypeEnum.CREDIT_FIXED_INCOME),
            Map.entry(ResourceType.TREASURE_TITLE, ResponseResourceListData.TypeEnum.TREASURE_TITLE),
            Map.entry(ResourceType.VARIABLE_INCOME, ResponseResourceListData.TypeEnum.VARIABLE_INCOME),
            Map.entry(ResourceType.FUND, ResponseResourceListData.TypeEnum.FUND)
    );

    private static final Map<ResourceType, EnumConsentPermissions> PERMISSION_MAP = Map.ofEntries(
            Map.entry(ResourceType.ACCOUNT, EnumConsentPermissions.ACCOUNTS_READ),
            Map.entry(ResourceType.CREDIT_CARD_ACCOUNT, EnumConsentPermissions.CREDIT_CARDS_ACCOUNTS_READ),
            Map.entry(ResourceType.LOAN, EnumConsentPermissions.LOANS_READ),
            Map.entry(ResourceType.FINANCING, EnumConsentPermissions.FINANCINGS_READ),
            Map.entry(ResourceType.UNARRANGED_ACCOUNT_OVERDRAFT, EnumConsentPermissions.UNARRANGED_ACCOUNTS_OVERDRAFT_READ),
            Map.entry(ResourceType.INVOICE_FINANCING, EnumConsentPermissions.INVOICE_FINANCINGS_READ),
            Map.entry(ResourceType.EXCHANGE, EnumConsentPermissions.EXCHANGES_READ),
            Map.entry(ResourceType.BANK_FIXED_INCOME, EnumConsentPermissions.BANK_FIXED_INCOMES_READ),
            Map.entry(ResourceType.CREDIT_FIXED_INCOME, EnumConsentPermissions.CREDIT_FIXED_INCOMES_READ),
            Map.entry(ResourceType.TREASURE_TITLE, EnumConsentPermissions.TREASURE_TITLES_READ),
            Map.entry(ResourceType.VARIABLE_INCOME, EnumConsentPermissions.VARIABLE_INCOMES_READ),
            Map.entry(ResourceType.FUND, EnumConsentPermissions.FUNDS_READ)
    );

    public ResponseResourceList getResourceList(Pageable pageable, @NotNull String consentId) {
        LOG.info("Getting resources response for consent id {}", consentId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);

        BankLambdaUtils.checkConsentPermissions(consentEntity, EnumConsentPermissions.RESOURCES_READ);

        var permissions = BankLambdaUtils.getConsentPermissions(consentEntity);
        if (checkIfOnlyCustomersGroup(permissions)) {
            return new ResponseResourceList()
                    .data(new ArrayList<>())
                    .meta(BankLambdaUtils.getMeta(null));
        }

        var accounts = consentEntity.getAccounts();
        var contracts = consentEntity.getContracts();
        var creditCardsAccounts = consentEntity.getCreditCardAccounts();
        var creditFixedAccounts = consentEntity.getCreditFixedIncomesAccounts();
        var exchangesOperations = consentEntity.getExchangesOperations();

        LOG.info("Found {} accounts to include in resource response", accounts.size());
        LOG.info("Found {} contracts to include in resource response", contracts.size());
        LOG.info("Found {} creditCardsAccounts to include in resource response", creditCardsAccounts.size());
        LOG.info("Found {} creditFixedIncomesAccounts to include in resource response", creditFixedAccounts.size());

        Map<ResponseResourceListData, UUID> responseMap = new HashMap<>();
        addAccountsToResources(accounts, consentEntity, responseMap, permissions.contains(PERMISSION_MAP.get(ResourceType.ACCOUNT)));
        addContractsToResources(contracts, consentEntity, responseMap, permissions);
        addCreditCardsAccountsToResources(creditCardsAccounts, consentEntity, responseMap, permissions.contains(PERMISSION_MAP.get(ResourceType.CREDIT_CARD_ACCOUNT)));
        addBankFixedIncomesToResources(consentEntity, responseMap, permissions.contains(PERMISSION_MAP.get(ResourceType.BANK_FIXED_INCOME)));
        addCreditFixedIncomesToResources(consentEntity, responseMap, permissions.contains(PERMISSION_MAP.get(ResourceType.CREDIT_FIXED_INCOME)));
        addVariableIncomesToResources(consentEntity, responseMap, permissions.contains(PERMISSION_MAP.get(ResourceType.VARIABLE_INCOME)));
        addTreasureTitlesToResources(consentEntity, responseMap, permissions.contains(PERMISSION_MAP.get(ResourceType.TREASURE_TITLE)));
        addFundsToResources(consentEntity, responseMap, permissions.contains(PERMISSION_MAP.get(ResourceType.FUND)));
        addExchangesToResources(exchangesOperations, consentEntity, responseMap, permissions.contains(PERMISSION_MAP.get(ResourceType.EXCHANGE)));
        var resourcePage = getPage(responseMap, pageable);
        var response = new ResponseResourceList().data(resourcePage.getContent());
        response.setMeta(BankLambdaUtils.getMeta(resourcePage));
        return response;
    }

    private void addAccountsToResources(Set<AccountEntity> accounts, ConsentEntity consent, Map<ResponseResourceListData, UUID> responseMap, boolean permitted) {
        for (var accountEntity : accounts) {
            var resourceType = TYPE_MAP.get(ResourceType.ACCOUNT);
            var data = new ResponseResourceListData();
            String resourceId = accountEntity.getAccountId().toString();
            if (permitted) {
                var resourceStatus = getStatus(accountEntity, consent);
                LOG.info("Adding account {}, status {}, resourceId {}, type {}", accountEntity.getAccountId(), resourceStatus, resourceId, resourceType);
                data.status(resourceStatus).resourceId(resourceId).type(resourceType);
                responseMap.put(data, accountEntity.getAccountId());
            } else {
                LOG.error("Account read permissions not found in consent id {}", consent.getConsentId());
            }
    }
        }

    private void addContractsToResources(Set<ContractEntity> contracts, ConsentEntity consent, Map<ResponseResourceListData, UUID> responseMap, Set<EnumConsentPermissions> permissions) {
            for (var contractEntity : contracts) {
                var accountType = ResourceType.valueOf(contractEntity.getContractType());
                var resourceType = TYPE_MAP.get(accountType);
                var requiredPermission = PERMISSION_MAP.get(accountType);
                var data = new ResponseResourceListData();
                String resourceId = contractEntity.getContractId().toString();
                if (permissions.contains(requiredPermission)) {
                    var resourceStatus = getStatus(contractEntity, consent);
                    LOG.info("Adding contract {}, status {}, resourceId {}, type {}", contractEntity.getContractId(), resourceStatus, resourceId, resourceType);
                    data.status(resourceStatus).resourceId(resourceId).type(resourceType);
                    responseMap.put(data, contractEntity.getContractId());
                } else {
                    LOG.error("{} permissions not found in consent id {}", requiredPermission, consent.getConsentId());
                }
        }
    }

    private void addCreditCardsAccountsToResources(Set<CreditCardAccountsEntity> creditCardsAccounts, ConsentEntity consent, Map<ResponseResourceListData, UUID> responseMap, boolean permitted) {
        for (var creditCardsAccountsEntity : creditCardsAccounts) {
            var resourceType = TYPE_MAP.get(ResourceType.CREDIT_CARD_ACCOUNT);
            var data = new ResponseResourceListData();
            String resourceId = creditCardsAccountsEntity.getCreditCardAccountId().toString();
            if (permitted) {
                var resourceStatus = getStatus(creditCardsAccountsEntity, consent);
                LOG.info("Adding Credit Cards Accounts {}, status {}, resourceId {}, type {}", creditCardsAccountsEntity.getCreditCardAccountId(), resourceStatus, resourceId, resourceType);
                data.status(resourceStatus).resourceId(resourceId).type(resourceType);
                responseMap.put(data, creditCardsAccountsEntity.getCreditCardAccountId());
            } else {
                LOG.error("Credit Cards Accounts read permissions not found in consent id {}", consent.getConsentId());
            }
        }
    }

    private void addBankFixedIncomesToResources(ConsentEntity consent, Map<ResponseResourceListData, UUID> responseMap, boolean permitted) {
        var bankFixedIncomesAccounts = bankFixedIncomesRepository.findByAccountHolderId(consent.getAccountHolderId());
        for (var bankFixedAccountsEntity : bankFixedIncomesAccounts) {
            var resourceType = TYPE_MAP.get(ResourceType.BANK_FIXED_INCOME);
            var data = new ResponseResourceListData();
            String resourceId = bankFixedAccountsEntity.getInvestmentId().toString();
            if (permitted) {
                var resourceStatus = getStatus(bankFixedAccountsEntity, consent);
                LOG.info("Adding Bank Fixed Incomes {}, status {}, resourceId {}, type {}", bankFixedAccountsEntity.getInvestmentId(), resourceStatus, resourceId, resourceType);
                data.status(resourceStatus).resourceId(resourceId).type(resourceType);
                responseMap.put(data, bankFixedAccountsEntity.getInvestmentId());
            } else {
                LOG.error("Bank Fixed Incomes read permissions not found in consent id {}", consent.getConsentId());
            }
        }
    }

    private void addCreditFixedIncomesToResources(ConsentEntity consent, Map<ResponseResourceListData, UUID> responseMap, boolean permitted) {
        var creditFixedIncomesAccounts = creditFixedIncomesRepository.findByAccountHolderId(consent.getAccountHolderId());
        for (var creditFixedAccountsEntity : creditFixedIncomesAccounts) {
            var resourceType = TYPE_MAP.get(ResourceType.CREDIT_FIXED_INCOME);
            var data = new ResponseResourceListData();
            String resourceId = creditFixedAccountsEntity.getInvestmentId().toString();
            if (permitted) {
                var resourceStatus = getStatus(creditFixedAccountsEntity, consent);
                LOG.info("Adding Credit Fixed Incomes {}, status {}, resourceId {}, type {}", creditFixedAccountsEntity.getInvestmentId(), resourceStatus, resourceId, resourceType);
                data.status(resourceStatus).resourceId(resourceId).type(resourceType);
                responseMap.put(data, creditFixedAccountsEntity.getInvestmentId());
            } else {
                LOG.error("Credit Fixed Incomes read permissions not found in consent id {}", consent.getConsentId());
            }
        }
    }

    private void addVariableIncomesToResources(ConsentEntity consent, Map<ResponseResourceListData, UUID> responseMap, boolean permitted) {
        var variableIncomesAccounts = variableIncomesRepository.findByAccountHolderId(consent.getAccountHolderId());
        for (var variableIncomesAccountsEntity : variableIncomesAccounts) {
            var resourceType = TYPE_MAP.get(ResourceType.VARIABLE_INCOME);
            var data = new ResponseResourceListData();
            String resourceId = variableIncomesAccountsEntity.getInvestmentId().toString();
            if (permitted) {
                var resourceStatus = getStatus(variableIncomesAccountsEntity, consent);
                LOG.info("Adding Bank Fixed Incomes {}, status {}, resourceId {}, type {}", variableIncomesAccountsEntity.getInvestmentId(), resourceStatus, resourceId, resourceType);
                data.status(resourceStatus).resourceId(resourceId).type(resourceType);
                responseMap.put(data, variableIncomesAccountsEntity.getInvestmentId());
            } else {
                LOG.error("Bank Fixed Incomes read permissions not found in consent id {}", consent.getConsentId());
            }
        }
    }

    private void addTreasureTitlesToResources(ConsentEntity consent, Map<ResponseResourceListData, UUID> responseMap, boolean permitted) {
        var treasureTitlesAccounts = treasureTitlesRepository.findByAccountHolderId(consent.getAccountHolderId());
        for (var treasureTitlesAccountsEntity : treasureTitlesAccounts) {
            var resourceType = TYPE_MAP.get(ResourceType.TREASURE_TITLE);
            var data = new ResponseResourceListData();
            String resourceId = treasureTitlesAccountsEntity.getInvestmentId().toString();
            if (permitted) {
                var resourceStatus = getStatus(treasureTitlesAccountsEntity, consent);
                LOG.info("Adding Treasure Titles {}, status {}, resourceId {}, type {}", treasureTitlesAccountsEntity.getInvestmentId(), resourceStatus, resourceId, resourceType);
                data.status(resourceStatus).resourceId(resourceId).type(resourceType);
                responseMap.put(data, treasureTitlesAccountsEntity.getInvestmentId());
            } else {
                LOG.error("Credit Treasure Titles read permissions not found in consent id {}", consent.getConsentId());
            }
        }
    }

    private void addFundsToResources(ConsentEntity consent, Map<ResponseResourceListData, UUID> responseMap, boolean permitted) {
        var fundsAccounts = fundsRepository.findByAccountHolderId(consent.getAccountHolderId());
        for (var fundsAccountsEntity : fundsAccounts) {
            var resourceType = TYPE_MAP.get(ResourceType.FUND);
            var data = new ResponseResourceListData();
            String resourceId = fundsAccountsEntity.getInvestmentId().toString();
            if (permitted) {
                var resourceStatus = getStatus(fundsAccountsEntity, consent);
                LOG.info("Adding Funds {}, status {}, resourceId {}, type {}", fundsAccountsEntity.getInvestmentId(), resourceStatus, resourceId, resourceType);
                data.status(resourceStatus).resourceId(resourceId).type(resourceType);
                responseMap.put(data, fundsAccountsEntity.getInvestmentId());
            } else {
                LOG.error("Credit Funds read permissions not found in consent id {}", consent.getConsentId());
            }
        }
    }

    private void addExchangesToResources(Set<ExchangesOperationEntity> exchangesOperations, ConsentEntity consent, Map<ResponseResourceListData, UUID> responseMap, boolean permitted) {
        var resourceType = TYPE_MAP.get(ResourceType.EXCHANGE);
        for (var exchangesEntity : exchangesOperations) {
            var data = new ResponseResourceListData();
            String resourceId = exchangesEntity.getOperationId().toString();
            if (permitted) {
                var resourceStatus = getStatus(exchangesEntity, consent);
                LOG.info("Adding exchange {}, status {}, resourceId {}, type {}", resourceId, resourceStatus, resourceId, resourceType);
                data.status(resourceStatus).resourceId(resourceId).type(resourceType);
                responseMap.put(data, exchangesEntity.getOperationId());
            } else {
                LOG.error("Exchanges API read permissions not found in consent id {}", consent.getConsentId());
            }
        }
    }

    public void checkStatusAvailable(HasStatusInterface accountOrContract, ConsentEntity consent) {
        if(getStatus(accountOrContract, consent).equals(ResponseResourceListData.StatusEnum.PENDING_AUTHORISATION)) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, new ResponseErrorErrors()
                    .code("status_RESOURCE_PENDING_AUTHORISATION")
                    .title("Aguardando autorização de multiplas alçadas")
                    .detail("status_RESOURCE_PENDING_AUTHORISATION"));
        }
        if(getStatus(accountOrContract, consent).equals(ResponseResourceListData.StatusEnum.TEMPORARILY_UNAVAILABLE)) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, new ResponseErrorErrors()
                    .code("status_RESOURCE_TEMPORARILY_UNAVAILABLE")
                    .title("Recurso temporariamente indisponível")
                    .detail("status_RESOURCE_TEMPORARILY_UNAVAILABLE"));
        }
    }
    private ResponseResourceListData.StatusEnum getStatus(HasStatusInterface accountOrContract, ConsentEntity consent) {
        if (AVAILABLE.toString().equals(accountOrContract.getStatus())) {
            switch (EnumConsentStatus.fromValue(consent.getStatus())) {
                case AUTHORISED:
                    return AVAILABLE;
                case AWAITING_AUTHORISATION:
                    return ResponseResourceListData.StatusEnum.PENDING_AUTHORISATION;
                case REJECTED:
                default:
                    return ResponseResourceListData.StatusEnum.UNAVAILABLE;
            }
        } else if (PENDING_AUTHORISATION.toString().equals(accountOrContract.getStatus())) {
            return ResponseResourceListData.StatusEnum.PENDING_AUTHORISATION;
        } else if (TEMPORARILY_UNAVAILABLE.toString().equals(accountOrContract.getStatus())) {
            return ResponseResourceListData.StatusEnum.TEMPORARILY_UNAVAILABLE;
        } else {
            return ResponseResourceListData.StatusEnum.UNAVAILABLE;
        }
    }

    private static Page<ResponseResourceListData> getPage(Map<ResponseResourceListData, UUID> responseMap, Pageable pageable) {
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

    private boolean checkIfOnlyCustomersGroup(Set<EnumConsentPermissions> permissions) {
        if (permissions.contains(EnumConsentPermissions.ACCOUNTS_READ)
                || permissions.contains(EnumConsentPermissions.CREDIT_CARDS_ACCOUNTS_READ)
                || permissions.contains(EnumConsentPermissions.FINANCINGS_READ)
                || permissions.contains(EnumConsentPermissions.INVOICE_FINANCINGS_READ)
                || permissions.contains(EnumConsentPermissions.LOANS_READ)
                || permissions.contains(EnumConsentPermissions.UNARRANGED_ACCOUNTS_OVERDRAFT_READ)
                || permissions.contains(EnumConsentPermissions.BANK_FIXED_INCOMES_READ)
                || permissions.contains(EnumConsentPermissions.CREDIT_FIXED_INCOMES_READ)
                || permissions.contains(EnumConsentPermissions.VARIABLE_INCOMES_READ)
                || permissions.contains(EnumConsentPermissions.TREASURE_TITLES_READ)
                || permissions.contains(EnumConsentPermissions.FUNDS_READ)
                || permissions.contains(EnumConsentPermissions.EXCHANGES_READ)
        ) {
            return false;
        }

        if ((permissions.contains(EnumConsentPermissions.CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ)
                && permissions.contains(EnumConsentPermissions.CUSTOMERS_PERSONAL_ADITTIONALINFO_READ))
                || (permissions.contains(EnumConsentPermissions.CUSTOMERS_BUSINESS_IDENTIFICATIONS_READ)
                && permissions.contains(EnumConsentPermissions.CUSTOMERS_BUSINESS_ADITTIONALINFO_READ))) {
            return true;
        }

        throw new HttpStatusException(HttpStatus.NOT_FOUND, "Resource not found, no appropriate permissions attached to consent");
    }
}
