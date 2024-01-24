package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.domain.ConsentContractEntity;
import com.raidiam.trustframework.bank.domain.ContractEntity;
import com.raidiam.trustframework.bank.domain.ContractWarrantyEntity;
import com.raidiam.trustframework.bank.enums.ContractStatusEnum;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.EnumConsentPermissions;
import com.raidiam.trustframework.mockbank.models.generated.ResponseErrorErrors;
import com.raidiam.trustframework.mockbank.models.generated.ResponseResourceListData;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.UUID;

@Singleton
@Transactional
public class ContractService extends BaseBankService {

    private static final Logger LOG = LoggerFactory.getLogger(ContractService.class);

    public Page<ConsentContractEntity> getPageContractList(Pageable pageable, String consentId,
                                                           String contractType, EnumConsentPermissions permission) {
        LOG.info("Getting {} Contract Pages for consent id {}", contractType, consentId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, permission);

        var consentContractsPage = consentContractRepository.findByConsentIdAndContractContractTypeAndContractStatusOrderByCreatedAtAsc(consentId, contractType, ContractStatusEnum.AVAILABLE.toString(), pageable);

        BankLambdaUtils.checkConsentOwnerIsContractOwner(consentContractsPage, consentEntity);

        return consentContractsPage;
    }

    public Page<ContractWarrantyEntity> getPageContractWarrantyEntity(Pageable pageable, String consentId, UUID contractId,
                                                                      String contractType, EnumConsentPermissions permission) {
        LOG.info("Getting {} Contract Warranties Pages for consent id {} and contract id {}", contractType, consentId, contractId);

        return contractWarrantiesRepository.findByContractOrderByCreatedAtAsc(getContractEntity(consentId, contractId, contractType, permission), pageable);
    }

    public ContractEntity getContractEntity(String consentId, UUID contractId, String contractType, EnumConsentPermissions permission) {
        LOG.info("Getting {} Contract for consent id {} and contract id {}", contractType, consentId, contractId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var contractEntity = BankLambdaUtils.getContract(contractId, contractsRepository, contractType);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, permission);
        BankLambdaUtils.checkConsentCoversContract(consentEntity, contractEntity);
        BankLambdaUtils.checkConsentOwnerIsContractOwner(consentEntity, contractEntity);

        if (consentEntity.getAccountHolder() == null) {
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Consent has no associated account holder, cannot proceed");
        }

        return contractEntity;
    }

    protected void validateContractStatus(String consentId, UUID contractId,
                                        String contractType, EnumConsentPermissions permission) {
        var contractEntity = getContractEntity(consentId, contractId, contractType, permission);
        if(contractEntity.getStatus().equals(ResponseResourceListData.StatusEnum.PENDING_AUTHORISATION.toString())) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, new ResponseErrorErrors()
                    .code("status_RESOURCE_PENDING_AUTHORISATION")
                    .title("Aguardando autorização de multiplas alçadas")
                    .detail("status_RESOURCE_PENDING_AUTHORISATION"));
        } else if(contractEntity.getStatus().equals(ResponseResourceListData.StatusEnum.TEMPORARILY_UNAVAILABLE.toString())) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, new ResponseErrorErrors()
                    .code("status_RESOURCE_TEMPORARILY_UNAVAILABLE")
                    .title("Recurso temporariamente indisponível")
                    .detail("status_RESOURCE_TEMPORARILY_UNAVAILABLE"));
        } else if(!contractEntity.getStatus().equals(ContractStatusEnum.AVAILABLE.toString())) {
            LOG.info("Contract status is not AVAILABLE for consent id {} and contract id {} v2", consentId, contractId);
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Contract status is not available");
        }
    }
}
