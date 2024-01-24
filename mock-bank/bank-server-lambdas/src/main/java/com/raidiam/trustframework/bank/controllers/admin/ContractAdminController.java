package com.raidiam.trustframework.bank.controllers.admin;

import com.raidiam.trustframework.bank.controllers.BaseBankController;
import com.raidiam.trustframework.bank.services.ContractAdminService;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;

@RolesAllowed({"ADMIN_FULL_MANAGE"})
@Controller("/admin/customers/{accountHolderId}/{contractType}")
public class ContractAdminController extends BaseBankController {

    private final ContractAdminService contractsService;
    private static final Logger LOG = LoggerFactory.getLogger(ContractAdminController.class);

    public ContractAdminController(ContractAdminService contractsService) {
        this.contractsService = contractsService;
    }

    @Post
    public ResponseContract addContract(@PathVariable("accountHolderId") String accountHolderId,
                                        @PathVariable("contractType") String type,
                                        @Body CreateContract contract) {
        var contractType = BankLambdaUtils.getContractType(type);
        LOG.info("Posting Contract for account holder {} type {} for AccountHolder {}", accountHolderId, contractType, accountHolderId);
        var response = contractsService.addContract(contractType, contract.getData(), accountHolderId);
        LOG.info("Returning post admin Contract response");
        BankLambdaUtils.logObject(mapper, response);
        return new ResponseContract().data(response);
    }

    @Put("/{contractId}")
    public ResponseContract putContract(@PathVariable("accountHolderId") String accountHolderId,
                                        @PathVariable("contractType") String type,
                                        @PathVariable("contractId") String contractId,
                                        @Body EditedContract contract) {
        var contractType = BankLambdaUtils.getContractType(type);
        LOG.info("Updating Contract by contractId {} type {} for AccountHolder {}", contractId, contractType, accountHolderId);
        var response = contractsService.updateContract(contractType, contractId, contract.getData());
        LOG.info("Returning put admin Contract response");
        BankLambdaUtils.logObject(mapper, response);
        return new ResponseContract().data(response);
    }

    @Get("/{contractId}")
    public ResponseContract getContract(@PathVariable("accountHolderId") String accountHolderId,
                                        @PathVariable("contractType") String type,
                                        @PathVariable("contractId") String contractId) {
        var contractType = BankLambdaUtils.getContractType(type);
        LOG.info("Looking up admin Contract by contractId {} type {} for AccountHolder {}", contractId, contractType, accountHolderId);
        var response = contractsService.getContract(contractType, contractId);
        LOG.info("Returning get admin Contract response");
        BankLambdaUtils.logObject(mapper, response);
        return new ResponseContract().data(response);
    }

    @Delete("/{contractId}")
    public HttpResponse<Object> deleteContract(@PathVariable("accountHolderId") String accountHolderId,
                                               @PathVariable("contractType") String type,
                                               @PathVariable("contractId") String contractId) {
        var contractType = BankLambdaUtils.getContractType(type);
        LOG.info("Deleting Contract for contractId {} type {} for AccountHolder {}", contractId, contractType, accountHolderId);
        contractsService.deleteContract(contractType, contractId);
        LOG.info("Contract deletion: Done. Returning 204 No Content");
        return HttpResponse.noContent();
    }

    @Post("/{contractId}/warranties")
    public ResponseContractWarranties postWarranties(@PathVariable("accountHolderId") String accountHolderId,
                                                     @PathVariable("contractType") String type,
                                                     @PathVariable("contractId") String contractId,
                                                     @Body ContractWarranties warranties) {
        var contractType = BankLambdaUtils.getContractType(type);
        LOG.info("Posting Contract Warranties for contractId {} type {} for AccountHolder {}", contractId, contractType, accountHolderId);
        var response = contractsService.addWarranties(contractType, contractId, warranties.getData());
        LOG.info("Returning post admin Contract Warranties response");
        BankLambdaUtils.logObject(mapper, response);
        return new ResponseContractWarranties().data(response);
    }

    @Put("/{contractId}/warranties")
    public ResponseContractWarranties putWarranties(@PathVariable("accountHolderId") String accountHolderId,
                                                    @PathVariable("contractType") String type,
                                                    @PathVariable("contractId") String contractId,
                                                    @Body ContractWarranties warranties) {
        var contractType = BankLambdaUtils.getContractType(type);
        LOG.info("Updating Contract Warranties for contractId {} type {} for AccountHolder {}", contractId, contractType, accountHolderId);
        var response = contractsService.updateWarranties(contractType, contractId, warranties.getData());
        LOG.info("Returning put admin Contract Warranties response");
        BankLambdaUtils.logObject(mapper, response);
        return new ResponseContractWarranties().data(response);
    }

    @Delete("/{contractId}/warranties")
    public HttpResponse<Object> deleteWarranties(@PathVariable("accountHolderId") String accountHolderId,
                                                 @PathVariable("contractType") String type,
                                                 @PathVariable("contractId") String contractId) {
        var contractType = BankLambdaUtils.getContractType(type);
        LOG.info("Deleting Contract Warranties for contractId {} type {} for AccountHolder {}", contractId, contractType, accountHolderId);
        contractsService.deleteWarranties(contractType, contractId);
        LOG.info("Contract deletion: Done. Returning 204 No Content");
        return HttpResponse.noContent();
    }
}