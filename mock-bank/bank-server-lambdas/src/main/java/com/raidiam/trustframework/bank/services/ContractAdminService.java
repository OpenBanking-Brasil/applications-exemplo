package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.domain.*;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
@Transactional
public class ContractAdminService extends BaseBankService {
    private static final Logger LOG = LoggerFactory.getLogger(ContractAdminService.class);

    public ResponseContractData addContract(String type, CreateContractData contract, String accountHolderId) {
        LOG.info("Add {} contract for holder id {}", type, accountHolderId);
        BankLambdaUtils.checkExistAccountHolder(accountHolderId, accountHolderRepository);
        return contractsRepository.save(ContractEntity.from(contract, UUID.fromString(accountHolderId))).getContractData();
    }

    public ResponseContractData updateContract(String type, String contractId, EditedContractData contract) {
        LOG.info("Update {} contract id {}", type, contractId);
        var contractEntity = BankLambdaUtils.getContract(UUID.fromString(contractId), contractsRepository, type);
        return contractsRepository.update(contractEntity.update(contract)).getContractData();
    }

    public ResponseContractData getContract(String type, String contractId) {
        LOG.info("Get {} contract id {}", type, contractId);
        var contractEntity = BankLambdaUtils.getContract(UUID.fromString(contractId), contractsRepository, type);
        return contractEntity.getContractData();
    }

    public void deleteContract(String type, String contractId) {
        LOG.info("Delete {} contract id {}", type, contractId);
        var contractEntity = BankLambdaUtils.getContract(UUID.fromString(contractId), contractsRepository, type);
        contractsRepository.delete(contractEntity);
    }

    public List<ContractWarrantiesData> addWarranties(String type, String contractId, List<ContractWarrantiesData> warranties) {
        LOG.info("Add {} warranties for contract id {}", type, contractId);
        return saveAllWarranties(UUID.fromString(contractId), warranties, type);
    }

    public List<ContractWarrantiesData> updateWarranties(String type, String contractId, List<ContractWarrantiesData> warranties) {
        LOG.info("Update {} warranties for contract id {}", type, contractId);

        var contractEntity = BankLambdaUtils.getContract(UUID.fromString(contractId), contractsRepository, type);
        contractEntity.getContractWarranties().clear();

        var warrantiesEntity = warranties.stream()
                .map(c -> ContractWarrantyEntity.from(contractEntity, c)).collect(Collectors.toSet());
        contractEntity.getContractWarranties().addAll(warrantiesEntity);

        contractsRepository.update(contractEntity);
        return saveAllWarranties(UUID.fromString(contractId), warranties, type);
    }

    public void deleteWarranties(String type, String contractId) {
        LOG.info("Delete {} warranties for contract id {}", type, contractId);
        var contractEntity = BankLambdaUtils.getContract(UUID.fromString(contractId), contractsRepository, type);
        contractEntity.getContractWarranties().clear();
        contractsRepository.update(contractEntity);
    }

    private List<ContractWarrantiesData> saveAllWarranties(UUID contractId, List<ContractWarrantiesData> warranties, String contractType) {
        var contractEntity = BankLambdaUtils.getContract(contractId, contractsRepository, contractType);
        var warrantiesEntity = warranties.stream()
                .map(w -> ContractWarrantyEntity.from(contractEntity, w))
                .collect(Collectors.toList());
        var savedWarranties = contractWarrantiesRepository.saveAll(warrantiesEntity);
        return StreamSupport.stream(savedWarranties.spliterator(), false)
                .map(ContractWarrantyEntity::getContractWarrantiesData)
                .collect(Collectors.toList());
    }
}
