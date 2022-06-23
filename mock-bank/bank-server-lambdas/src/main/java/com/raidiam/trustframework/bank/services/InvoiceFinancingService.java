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
public class InvoiceFinancingService extends BaseBankService {

    private static final Logger LOG = LoggerFactory.getLogger(InvoiceFinancingService.class);
    private static final String CONTRACT_TYPE = AccountOrContractType.INVOICE_FINANCING.name();

    public ResponseInvoiceFinancingsContractList getInvoiceFinancingContractList(Pageable pageable, String consentId) {
        LOG.info("Getting Invoice Financings Contract List response for consent id {}", consentId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.INVOICE_FINANCINGS_READ);

        var consentContractsPage = consentContractRepository.findByConsentIdAndContractContractTypeOrderByCreatedAtAsc(consentId, CONTRACT_TYPE, pageable);
        BankLambdaUtils.checkConsentOwnerIsContractOwner(consentContractsPage, consentEntity);
        var response = new ResponseInvoiceFinancingsContractList().data(consentContractsPage.getContent()
                .stream()
                .map(ConsentContractEntity::getContract)
                .map(ContractEntity::getInvoiceFinancingsDTOList)
                .collect(Collectors.toList()));

        response.setMeta(BankLambdaUtils.getMeta(consentContractsPage, response.getData().size()));

        return response;
    }

    public ResponseInvoiceFinancingsContract getInvoiceFinancingContract(String consentId, UUID contractId) {
        LOG.info("Getting Invoice Financings Contract response for consent id {} and contract id {}", consentId, contractId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var contractEntity = BankLambdaUtils.getContract(contractId, contractsRepository, CONTRACT_TYPE);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentCoversContract(consentEntity, contractEntity);
        BankLambdaUtils.checkConsentOwnerIsContractOwner(consentEntity, contractEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.INVOICE_FINANCINGS_READ);

        if(consentEntity.getAccountHolder() == null) {
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Consent has no associated accountholder, cannot proceed");
        }

        return new ResponseInvoiceFinancingsContract().data(contractEntity.getInvoiceFinancingsDTO());
    }

    public ResponseInvoiceFinancingsInstalments getInvoiceFinancingScheduledInstalments(String consentId, UUID contractId) {
        LOG.info("Getting Invoice Financings Instalments response for consent id {} and contract id {}", consentId, contractId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var contractEntity = BankLambdaUtils.getContract(contractId, contractsRepository, CONTRACT_TYPE);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentCoversContract(consentEntity, contractEntity);
        BankLambdaUtils.checkConsentOwnerIsContractOwner(consentEntity, contractEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ);

        return new ResponseInvoiceFinancingsInstalments().data(contractEntity.getInvoiceFinancingInstalmentsData());
    }

    public ResponseInvoiceFinancingsWarranties getInvoiceFinancingWarranties(Pageable pageable, String consentId, UUID contractId) {
        LOG.info("Getting Invoice Financings Warranties response for consent id {} and contract id {}", consentId, contractId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var contractEntity = BankLambdaUtils.getContract(contractId, contractsRepository, CONTRACT_TYPE);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentCoversContract(consentEntity, contractEntity);
        BankLambdaUtils.checkConsentOwnerIsContractOwner(consentEntity, contractEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.INVOICE_FINANCINGS_WARRANTIES_READ);

        var warranties = contractWarrantiesRepository.findByContractIdOrderByCreatedAtAsc(contractId, pageable);

        var response = new ResponseInvoiceFinancingsWarranties().data(warranties.getContent()
                .stream().map(ContractWarrantyEntity::getInvoiceFinancingDTO)
                .collect(Collectors.toList()));

        response.setMeta(BankLambdaUtils.getMeta(warranties, response.getData().size()));

        return response;
    }

    public ResponseInvoiceFinancingsPayments getInvoiceFinancingPayments(String consentId, UUID contractId) {
        LOG.info("Getting Invoice Financings Payments response for consent id {} and contract id {}", consentId, contractId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var contractEntity = BankLambdaUtils.getContract(contractId, contractsRepository, CONTRACT_TYPE);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentCoversContract(consentEntity, contractEntity);
        BankLambdaUtils.checkConsentOwnerIsContractOwner(consentEntity, contractEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.INVOICE_FINANCINGS_PAYMENTS_READ);

        return new ResponseInvoiceFinancingsPayments().data(contractEntity.getInvoiceFinancingPaymentsData());
    }
}
