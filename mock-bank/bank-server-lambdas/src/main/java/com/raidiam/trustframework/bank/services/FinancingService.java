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
public class FinancingService extends ContractService {

    private static final Logger LOG = LoggerFactory.getLogger(FinancingService.class);
    private static final String CONTRACT_TYPE = EnumContractType.FINANCING.name();

    public ResponseFinancingsContractList getFinancingContractList(Pageable pageable, String consentId) {
        LOG.info("Getting {} Contract List response for consent id {}", CONTRACT_TYPE, consentId);
        var consentContractsPage = getPageContractList(pageable, consentId,
                CONTRACT_TYPE, EnumConsentPermissions.FINANCINGS_READ);
        var response = new ResponseFinancingsContractList().data(consentContractsPage.getContent()
                .stream()
                .map(ConsentContractEntity::getContract)
                .map(ContractEntity::getFinancingsDTOList)
                .collect(Collectors.toList()));
        response.setMeta(BankLambdaUtils.getMeta(consentContractsPage));
        return response;
    }

    public ResponseFinancingsContractV2 getFinancingContractV2(String consentId, UUID contractId) {
        LOG.info("Getting {} Contract response for consent id {} and contract id {} v2", CONTRACT_TYPE, consentId, contractId);
        validateContractStatus(consentId, contractId, CONTRACT_TYPE, EnumConsentPermissions.FINANCINGS_READ);
        return new ResponseFinancingsContractV2().data(getContractEntity(consentId, contractId,
                CONTRACT_TYPE, EnumConsentPermissions.FINANCINGS_READ).getFinancingsDtoV2());
    }

    public ResponseFinancingsInstalmentsV2 getFinancingScheduledInstalmentsV2(String consentId, UUID contractId) {
        LOG.info("Getting {} Instalments response for consent id {} and contract id {} v2", CONTRACT_TYPE, consentId, contractId);
        validateContractStatus(consentId, contractId, CONTRACT_TYPE, EnumConsentPermissions.FINANCINGS_SCHEDULED_INSTALMENTS_READ);
        return new ResponseFinancingsInstalmentsV2().data(getContractEntity(consentId, contractId,
                CONTRACT_TYPE, EnumConsentPermissions.FINANCINGS_SCHEDULED_INSTALMENTS_READ).getFinancingInstalmentsDataV2());
    }

    public ResponseFinancingsWarrantiesV2 getFinancingsWarrantiesV2(Pageable pageable, String consentId, UUID contractId) {
        LOG.info("Getting {} Warranties response for consent id {} and contract id {} v2", CONTRACT_TYPE, consentId, contractId);
        validateContractStatus(consentId, contractId, CONTRACT_TYPE, EnumConsentPermissions.FINANCINGS_WARRANTIES_READ);
        var warranties = getPageContractWarrantyEntity(pageable, consentId, contractId,
                CONTRACT_TYPE, EnumConsentPermissions.FINANCINGS_WARRANTIES_READ);

        var response = new ResponseFinancingsWarrantiesV2().data(warranties.getContent()
                .stream().map(ContractWarrantyEntity::getFinancingsWarrantiesV2).collect(Collectors.toList()));

        response.setMeta(BankLambdaUtils.getMeta(warranties));
        return response;
    }

    public ResponseFinancingsPaymentsV2 getFinancingPaymentsV2(String consentId, UUID contractId) {
        LOG.info("Getting {} Payments response for consent id {} and contract id {}", CONTRACT_TYPE, consentId, contractId);
        validateContractStatus(consentId, contractId, CONTRACT_TYPE, EnumConsentPermissions.FINANCINGS_PAYMENTS_READ);
        return new ResponseFinancingsPaymentsV2().data(getContractEntity(consentId, contractId,
                CONTRACT_TYPE, EnumConsentPermissions.FINANCINGS_PAYMENTS_READ).getFinancingPaymentsDataV2());
    }
}
