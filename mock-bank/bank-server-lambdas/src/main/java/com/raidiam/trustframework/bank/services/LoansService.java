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
public class LoansService extends ContractService {

    private static final Logger LOG = LoggerFactory.getLogger(LoansService.class);
    private static final String CONTRACT_TYPE = EnumContractType.LOAN.name();

    public ResponseLoansContractList getLoansContractList(Pageable pageable, String consentId) {
        LOG.info("Getting {} Contract List response for consent id {}", CONTRACT_TYPE, consentId);
        var consentContractsPage = getPageContractList(pageable, consentId,
                CONTRACT_TYPE, EnumConsentPermissions.LOANS_READ);
        var response = new ResponseLoansContractList().data(consentContractsPage.getContent()
                .stream()
                .map(ConsentContractEntity::getContract)
                .map(ContractEntity::getLoanDTOList)
                .collect(Collectors.toList()));
        response.setMeta(BankLambdaUtils.getMeta(consentContractsPage));
        return response;
    }

    public ResponseLoansContractV2 getLoanContractV2(String consentId, UUID contractId) {
        LOG.info("Getting {} Contract response for consent id {} and contract id {} v2", CONTRACT_TYPE, consentId, contractId);
        validateContractStatus(consentId, contractId, CONTRACT_TYPE, EnumConsentPermissions.LOANS_READ);
        return new ResponseLoansContractV2().data(getContractEntity(consentId, contractId,
                CONTRACT_TYPE, EnumConsentPermissions.LOANS_READ).getLoanDtoV2());
    }

    public ResponseLoansInstalmentsV2 getLoanScheduledInstalmentsV2(String consentId, UUID contractId) {
        LOG.info("Getting {} Instalments response for consent id {} and contract id {} v2", CONTRACT_TYPE, consentId, contractId);
        validateContractStatus(consentId, contractId, CONTRACT_TYPE, EnumConsentPermissions.LOANS_SCHEDULED_INSTALMENTS_READ);
        return new ResponseLoansInstalmentsV2().data(getContractEntity(consentId, contractId,
                CONTRACT_TYPE, EnumConsentPermissions.LOANS_SCHEDULED_INSTALMENTS_READ).getLoansInstalmentsDataV2());
    }

    public ResponseLoansWarrantiesV2 getLoansWarrantiesV2(Pageable pageable, String consentId, UUID contractId) {
        LOG.info("Getting {} Warranties response for consent id {} and contract id {} v2", CONTRACT_TYPE, consentId, contractId);
        validateContractStatus(consentId, contractId, CONTRACT_TYPE, EnumConsentPermissions.LOANS_WARRANTIES_READ);
        var warranties = getPageContractWarrantyEntity(pageable, consentId, contractId,
                CONTRACT_TYPE, EnumConsentPermissions.LOANS_WARRANTIES_READ);

        var response = new ResponseLoansWarrantiesV2().data(warranties.getContent()
                .stream().map(ContractWarrantyEntity::getLoansWarrantiesV2).collect(Collectors.toList()));

        response.setMeta(BankLambdaUtils.getMeta(warranties));
        return response;
    }

    public ResponseLoansPaymentsV2 getLoanPaymentsV2(String consentId, UUID contractId) {
        LOG.info("Getting {} Payments response for consent id {} and contract id {} v2", CONTRACT_TYPE, consentId, contractId);
        validateContractStatus(consentId, contractId, CONTRACT_TYPE, EnumConsentPermissions.LOANS_PAYMENTS_READ);
        return new ResponseLoansPaymentsV2().data(getContractEntity(consentId, contractId,
                CONTRACT_TYPE, EnumConsentPermissions.LOANS_PAYMENTS_READ).getLoansPaymentsDataV2());
    }
}
