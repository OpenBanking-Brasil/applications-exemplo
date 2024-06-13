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
public class InvoiceFinancingService extends ContractService {

    private static final Logger LOG = LoggerFactory.getLogger(InvoiceFinancingService.class);
    private static final String CONTRACT_TYPE = EnumContractType.INVOICE_FINANCING.name();

    public ResponseInvoiceFinancingsContractList getInvoiceFinancingContractList(Pageable pageable, String consentId) {
        LOG.info("Getting {} Contract List response for consent id {}", CONTRACT_TYPE, consentId);
        var consentContractsPage = getPageContractList(pageable, consentId,
                CONTRACT_TYPE, EnumConsentPermissions.INVOICE_FINANCINGS_READ);
        var response = new ResponseInvoiceFinancingsContractList().data(consentContractsPage.getContent()
                .stream()
                .map(ConsentContractEntity::getContract)
                .map(ContractEntity::getInvoiceFinancingsDTOList)
                .collect(Collectors.toList()));
        response.setMeta(BankLambdaUtils.getMeta(consentContractsPage));
        return response;
    }

    public ResponseInvoiceFinancingsContractV2 getInvoiceFinancingContractV2(String consentId, UUID contractId) {
        LOG.info("Getting {} Contract response for consent id {} and contract id {} v2", CONTRACT_TYPE, consentId, contractId);
        validateContractStatus(consentId, contractId, CONTRACT_TYPE, EnumConsentPermissions.INVOICE_FINANCINGS_READ);
        return new ResponseInvoiceFinancingsContractV2().data(getContractEntity(consentId, contractId,
                CONTRACT_TYPE, EnumConsentPermissions.INVOICE_FINANCINGS_READ).getInvoiceFinancingsDtoV2());
    }

    public ResponseInvoiceFinancingsInstalmentsV2 getInvoiceFinancingScheduledInstalmentsV2(String consentId, UUID contractId) {
        LOG.info("Getting {} Instalments response for consent id {} and contract id {} v2", CONTRACT_TYPE, consentId, contractId);
        validateContractStatus(consentId, contractId, CONTRACT_TYPE, EnumConsentPermissions.INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ);
        return new ResponseInvoiceFinancingsInstalmentsV2().data(getContractEntity(consentId, contractId,
                CONTRACT_TYPE, EnumConsentPermissions.INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ).getInvoiceFinancingInstalmentsDataV2());
    }


    public ResponseInvoiceFinancingsWarrantiesV2 getInvoiceFinancingsWarrantiesV2(Pageable pageable, String consentId, UUID contractId) {
        LOG.info("Getting {} Warranties response for consent id {} and contract id {} v2", CONTRACT_TYPE, consentId, contractId);
        validateContractStatus(consentId, contractId, CONTRACT_TYPE, EnumConsentPermissions.INVOICE_FINANCINGS_WARRANTIES_READ);
        var warranties = getPageContractWarrantyEntity(pageable, consentId, contractId,
                CONTRACT_TYPE, EnumConsentPermissions.INVOICE_FINANCINGS_WARRANTIES_READ);

        var response = new ResponseInvoiceFinancingsWarrantiesV2().data(warranties.getContent()
                .stream().map(ContractWarrantyEntity::getInvoiceFinancingsWarrantiesV2).collect(Collectors.toList()));

        response.setMeta(BankLambdaUtils.getMeta(warranties));
        return response;
    }
    
    public ResponseInvoiceFinancingsPaymentsV2 getInvoiceFinancingPaymentsV2(String consentId, UUID contractId) {
        LOG.info("Getting {} Payments response for consent id {} and contract id {}", CONTRACT_TYPE, consentId, contractId);
        validateContractStatus(consentId, contractId, CONTRACT_TYPE, EnumConsentPermissions.INVOICE_FINANCINGS_PAYMENTS_READ);
        return new ResponseInvoiceFinancingsPaymentsV2().data(getContractEntity(consentId, contractId,
                CONTRACT_TYPE, EnumConsentPermissions.INVOICE_FINANCINGS_PAYMENTS_READ).getInvoiceFinancingPaymentsDataV2());
    }
}
