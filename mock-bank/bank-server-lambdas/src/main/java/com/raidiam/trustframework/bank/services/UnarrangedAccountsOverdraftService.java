package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.domain.ConsentContractEntity;
import com.raidiam.trustframework.bank.domain.ContractEntity;
import com.raidiam.trustframework.bank.domain.ContractWarrantyEntity;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.data.model.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
@Transactional
public class UnarrangedAccountsOverdraftService extends ContractService {

    private static final Logger LOG = LoggerFactory.getLogger(UnarrangedAccountsOverdraftService.class);
    private static final String CONTRACT_TYPE = EnumContractType.UNARRANGED_ACCOUNT_OVERDRAFT.name();

    public ResponseUnarrangedAccountOverdraftContractList getUnarrangedOverdraftContractList(Pageable pageable, String consentId) {
        LOG.info("Getting {} Contract List response for consent id {}", CONTRACT_TYPE, consentId);
        var consentContractsPage = getPageContractList(pageable, consentId,
                CONTRACT_TYPE, EnumConsentPermissions.UNARRANGED_ACCOUNTS_OVERDRAFT_READ);
        var response = new ResponseUnarrangedAccountOverdraftContractList().data(consentContractsPage.getContent()
                .stream()
                .map(ConsentContractEntity::getContract)
                .map(ContractEntity::getOverdraftAccountsDTOList)
                .collect(Collectors.toList()));
        response.setMeta(BankLambdaUtils.getMeta(consentContractsPage));
        return response;
    }

    public ResponseUnarrangedAccountOverdraftContractV2 getUnarrangedOverdraftContractV2(String consentId, UUID contractId) {
        LOG.info("Getting {} Contract response for consent id {} and contract id {} v2", CONTRACT_TYPE, consentId, contractId);
        return new ResponseUnarrangedAccountOverdraftContractV2().data(getContractEntity(consentId, contractId,
                CONTRACT_TYPE, EnumConsentPermissions.UNARRANGED_ACCOUNTS_OVERDRAFT_READ).getOverDraftAccountsDtoV2());
    }

    public ResponseUnarrangedAccountOverdraftInstalmentsV2 getUnarrangedOverdraftScheduledInstalmentsV2(String consentId, UUID contractId) {
        LOG.info("Getting {} Instalments response for consent id {} and contract id {} v2", CONTRACT_TYPE, consentId, contractId);
        return new ResponseUnarrangedAccountOverdraftInstalmentsV2().data(getContractEntity(consentId, contractId,
                CONTRACT_TYPE, EnumConsentPermissions.UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ).getOverDraftInstalmentsDataV2());
    }

    public ResponseUnarrangedAccountOverdraftWarrantiesV2 getUnarrangedAccountsOverdraftWarrantiesV2(Pageable pageable, String consentId, UUID contractId) {
        LOG.info("Getting {} Warranties response for consent id {} and contract id {} v2", CONTRACT_TYPE, consentId, contractId);
        var warranties = getPageContractWarrantyEntity(pageable, consentId, contractId,
                CONTRACT_TYPE, EnumConsentPermissions.UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ);

        var response = new ResponseUnarrangedAccountOverdraftWarrantiesV2().data(warranties.getContent()
                .stream().map(ContractWarrantyEntity::getUnarrangedAccountOverdraftWarrantiesV2).collect(Collectors.toList()));

        response.setMeta(BankLambdaUtils.getMeta(warranties));
        return response;
    }

    public ResponseUnarrangedAccountOverdraftPaymentsV2 getUnarrangedOverdraftPaymentsV2(String consentId, UUID contractId) {
        LOG.info("Getting {} Payments response for consent id {} and contract id {}", CONTRACT_TYPE, consentId, contractId);
        return new ResponseUnarrangedAccountOverdraftPaymentsV2().data(getContractEntity(consentId, contractId,
                CONTRACT_TYPE, EnumConsentPermissions.UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ).getOverDraftPaymentsDataV2());
    }
}
