package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.domain.ConsentContractEntity;
import com.raidiam.trustframework.bank.domain.ContractEntity;
import com.raidiam.trustframework.bank.domain.ContractWarrantyEntity;
import com.raidiam.trustframework.bank.enums.AccountOrContractType;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public class UnarrangedAccountsOverdraftService extends BaseBankService {

    private static final Logger LOG = LoggerFactory.getLogger(UnarrangedAccountsOverdraftService.class);
    private static final String CONTRACT_TYPE = AccountOrContractType.UNARRANGED_ACCOUNT_OVERDRAFT.name();

    public ResponseUnarrangedAccountOverdraftContractList getUnarrangedOverdraftContractList(Pageable pageable, String consentId) {
        LOG.info("Getting Unarranged Account Overdraft Contract List response for consent id {}", consentId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.UNARRANGED_ACCOUNTS_OVERDRAFT_READ);

        var consentContractsPage = consentContractRepository.findByConsentIdAndContractContractTypeOrderByCreatedAtAsc(consentId, CONTRACT_TYPE, pageable);
        BankLambdaUtils.checkConsentOwnerIsContractOwner(consentContractsPage, consentEntity);
        var response = new ResponseUnarrangedAccountOverdraftContractList().data(consentContractsPage.getContent()
                .stream()
                .map(ConsentContractEntity::getContract)
                .map(ContractEntity::getOverdraftAccountsDTOList)
                .collect(Collectors.toList()));

        response.setMeta(BankLambdaUtils.getMeta(consentContractsPage, response.getData().size()));

        return response;
    }

    public ResponseUnarrangedAccountOverdraftContract getUnarrangedOverdraftContract(String consentId, UUID contractId) {
        LOG.info("Getting Unarranged Account Overdraft Contract response for consent id {} and contract id {}", consentId, contractId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var contractEntity = BankLambdaUtils.getContract(contractId, contractsRepository, CONTRACT_TYPE);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentCoversContract(consentEntity, contractEntity);
        BankLambdaUtils.checkConsentOwnerIsContractOwner(consentEntity, contractEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.UNARRANGED_ACCOUNTS_OVERDRAFT_READ);

        if(consentEntity.getAccountHolder() == null) {
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Consent has no associated accountholder, cannot proceed");
        }

        return new ResponseUnarrangedAccountOverdraftContract().data(contractEntity.getOverDraftAccountsDTO());
    }

    public ResponseUnarrangedAccountOverdraftInstalments getUnarrangedOverdraftScheduledInstalments(String consentId, UUID contractId) {
        LOG.info("Getting Unarranged Account Overdraft Instalments response for consent id {} and contract id {}", consentId, contractId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var contractEntity = BankLambdaUtils.getContract(contractId, contractsRepository, CONTRACT_TYPE);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentCoversContract(consentEntity, contractEntity);
        BankLambdaUtils.checkConsentOwnerIsContractOwner(consentEntity, contractEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ);

        return new ResponseUnarrangedAccountOverdraftInstalments().data(contractEntity.getOverDraftInstalmentsData());
    }

    public ResponseUnarrangedAccountOverdraftWarranties getUnarrangedOverdraftWarranties(Pageable pageable, String consentId, UUID contractId) {
        LOG.info("Getting Unarranged Account Overdraft Warranties response for consent id {} and contract id {}", consentId, contractId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var contractEntity = BankLambdaUtils.getContract(contractId, contractsRepository, CONTRACT_TYPE);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentCoversContract(consentEntity, contractEntity);
        BankLambdaUtils.checkConsentOwnerIsContractOwner(consentEntity, contractEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ);

        var warranties = contractWarrantiesRepository.findByContractIdOrderByCreatedAtAsc(contractId, pageable);

        var response = new ResponseUnarrangedAccountOverdraftWarranties().data(warranties.getContent()
                .stream().map(ContractWarrantyEntity::getUnarrangedAccountOverdraftDTO)
                .collect(Collectors.toList()));

        response.setMeta(BankLambdaUtils.getMeta(warranties, response.getData().size()));

        return response;
    }

    public ResponseUnarrangedAccountOverdraftPayments getUnarrangedOverdraftPayments(String consentId, UUID contractId) {
        LOG.info("Getting Unarranged Account Overdraft Payments response for consent id {} and contract id {}", consentId, contractId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var contractEntity = BankLambdaUtils.getContract(contractId, contractsRepository, CONTRACT_TYPE);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentCoversContract(consentEntity, contractEntity);
        BankLambdaUtils.checkConsentOwnerIsContractOwner(consentEntity, contractEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ);

        return new ResponseUnarrangedAccountOverdraftPayments().data(contractEntity.getOverDraftPaymentsData());
    }
}
