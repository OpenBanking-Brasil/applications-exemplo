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
public class LoansService extends BaseBankService {

    private static final Logger LOG = LoggerFactory.getLogger(LoansService.class);
    private static final String CONTRACT_TYPE = AccountOrContractType.LOAN.name();

    public ResponseLoansContractList getLoansContractList(Pageable pageable, String consentId) {
        LOG.info("Getting Loans Contract List response for consent id {}", consentId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.LOANS_READ);

        var consentContractsPage = consentContractRepository.findByConsentIdAndContractContractTypeOrderByCreatedAtAsc(consentId, CONTRACT_TYPE, pageable);
        BankLambdaUtils.checkConsentOwnerIsContractOwner(consentContractsPage, consentEntity);
        var response = new ResponseLoansContractList().data(consentContractsPage.getContent()
                .stream()
                .map(ConsentContractEntity::getContract)
                .map(ContractEntity::getLoanDTOList)
                .collect(Collectors.toList()));

        response.setMeta(BankLambdaUtils.getMeta(consentContractsPage, response.getData().size()));

        return response;
    }

    public ResponseLoansContract getLoanContract(String consentId, UUID contractId) {
        LOG.info("Getting Loans Contract response for consent id {} and contract id {}", consentId, contractId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var contractEntity = BankLambdaUtils.getContract(contractId, contractsRepository, CONTRACT_TYPE);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentCoversContract(consentEntity, contractEntity);
        BankLambdaUtils.checkConsentOwnerIsContractOwner(consentEntity, contractEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.LOANS_READ);

        if(consentEntity.getAccountHolder() == null) {
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Consent has no associated accountholder, cannot proceed");
        }

        return new ResponseLoansContract().data(contractEntity.getLoanDTO());
    }

    public ResponseLoansInstalments getLoanScheduledInstalments(String consentId, UUID contractId) {
        LOG.info("Getting Loans Instalments response for consent id {} and contract id {}", consentId, contractId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var contractEntity = BankLambdaUtils.getContract(contractId, contractsRepository, CONTRACT_TYPE);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentCoversContract(consentEntity, contractEntity);
        BankLambdaUtils.checkConsentOwnerIsContractOwner(consentEntity, contractEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.LOANS_SCHEDULED_INSTALMENTS_READ);

        return new ResponseLoansInstalments().data(contractEntity.getLoansInstalmentsData());
    }

    public ResponseLoansWarranties getLoanWarranties(Pageable pageable, String consentId, UUID contractId) {
        LOG.info("Getting Loans Warranties response for consent id {} and contract id {}", consentId, contractId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var contractEntity = BankLambdaUtils.getContract(contractId, contractsRepository, CONTRACT_TYPE);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentCoversContract(consentEntity, contractEntity);
        BankLambdaUtils.checkConsentOwnerIsContractOwner(consentEntity, contractEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.LOANS_WARRANTIES_READ);

        var warranties = contractWarrantiesRepository.findByContractIdOrderByCreatedAtAsc(contractId, pageable);

        var response = new ResponseLoansWarranties().data(warranties.getContent()
                .stream().map(ContractWarrantyEntity::getLoansDTO)
                .collect(Collectors.toList()));

        response.setMeta(BankLambdaUtils.getMeta(warranties, response.getData().size()));

        return response;
    }

    public ResponseLoansPayments getLoanPayments(String consentId, UUID contractId) {
        LOG.info("Getting Loans Payments response for consent id {} and contract id {}", consentId, contractId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var contractEntity = BankLambdaUtils.getContract(contractId, contractsRepository, CONTRACT_TYPE);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentCoversContract(consentEntity, contractEntity);
        BankLambdaUtils.checkConsentOwnerIsContractOwner(consentEntity, contractEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.LOANS_PAYMENTS_READ);

        return new ResponseLoansPayments().data(contractEntity.getLoansPaymentsData());
    }
}
