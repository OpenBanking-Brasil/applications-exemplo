package com.raidiam.trustframework.bank.controllers.admin;

import com.raidiam.trustframework.bank.controllers.BaseBankController;
import com.raidiam.trustframework.bank.services.CustomerService;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;

@RolesAllowed("ADMIN_FULL_MANAGE")
@Controller("/admin/customers")
public class CustomerAdminController extends BaseBankController {

    private static final Logger LOG = LoggerFactory.getLogger(CustomerAdminController.class);

    private final CustomerService customerService;

    CustomerAdminController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Get
    public ResponseAccountHolderList getAccountHolders() {
        LOG.info("Getting all AccountHolders");
        var response = customerService.getAccountHolders();
        LOG.info("Returning all AccountHolders response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Post
    public ResponseAccountHolder postAccountHolder(@Body CreateAccountHolder accountHolder) {
        LOG.info("Posting new AccountHolder {}", accountHolder.getData().getAccountHolderName());
        var response = customerService.addAccountHolder(accountHolder.getData());
        LOG.info("Returning post AccountHolder response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/{accountHolderId}")
    public ResponseAccountHolder getAccountHolder(@PathVariable("accountHolderId") String accountHolderId) {
        LOG.info("Getting AccountHolder by accountHolderId {}", accountHolderId);
        var response = customerService.getAccountHolder(accountHolderId);
        LOG.info("Returning get AccountHolder by accountHolderId response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Put("/{accountHolderId}")
    public ResponseAccountHolder putAccountHolder(@PathVariable("accountHolderId") String accountHolderId,
                                                  @Body CreateAccountHolder accountHolder) {
        LOG.info("Updating AccountHolder by accountHolderId {}", accountHolderId);
        var response = customerService.updateAccountHolder(accountHolderId, accountHolder.getData());
        LOG.info("Returning put AccountHolder response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Delete("/{accountHolderId}")
    public HttpResponse<Object> deleteAccountHolder(@PathVariable("accountHolderId") String accountHolderId) {
        LOG.info("Deleting AccountHolder by accountHolderId {}", accountHolderId);
        customerService.deleteAccountHolder(accountHolderId);
        LOG.info("AccountHolder deletion: Done. Returning 204 No Content");
        return HttpResponse.noContent();
    }

    @Post("/{accountHolderId}/personal/identifications")
    public ResponsePersonalIdentification postPersonalIdentifications(@PathVariable("accountHolderId") String accountHolderId,
                                                                      @Body CreatePersonalIdentification personalIdentifications) {
        LOG.info("Posting Personal Identifications for accountHolderId {}", accountHolderId);
        var response = customerService.addPersonalIdentifications(personalIdentifications.getData(), accountHolderId);
        LOG.info("Returning admin Personal Identifications response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Put("/{accountHolderId}/personal/identifications/{personalId}")
    public ResponsePersonalIdentification putPersonalIdentifications(@PathVariable("accountHolderId") String accountHolderId,
                                                                     @PathVariable("personalId") String personalId,
                                                                     @Body EditedPersonalIdentification personalIdentifications) {
        LOG.info("Updating Personal Identifications for personalId {} for accountHolderId {}", personalId, accountHolderId);
        var response = customerService.updatePersonalIdentifications(personalId, personalIdentifications.getData());
        LOG.info("Returning admin Personal Identifications response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Delete("/{accountHolderId}/personal/identifications/{personalId}")
    public HttpResponse<Object> deletePersonalIdentifications(@PathVariable("accountHolderId") String accountHolderId,
                                                              @PathVariable("personalId") String personalId) {
        LOG.info("Deleting Personal Identifications by personalId {} for accountHolderId {}", personalId, accountHolderId);
        customerService.deletePersonalIdentifications(personalId);
        LOG.info("Personal Identifications deletion: Done. Returning 204 No Content");
        return HttpResponse.noContent();
    }

    @Post("/{accountHolderId}/personal/financial-relations")
    public PersonalFinancialRelations postPersonalFinancialRelations(@PathVariable("accountHolderId") String accountHolderId,
                                                                     @Body PersonalFinancialRelations personalFinancialRelations) {
        LOG.info("Posting Personal Financial for accountHolderId {}", accountHolderId);
        var response = customerService.addPersonalFinancialRelations(personalFinancialRelations.getData(), accountHolderId);
        LOG.info("Returning admin Personal Financial response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Put("/{accountHolderId}/personal/financial-relations")
    public PersonalFinancialRelations putPersonalFinancialRelations(@PathVariable("accountHolderId") String accountHolderId,
                                                                    @Body PersonalFinancialRelations personalFinancialRelations) {
        LOG.info("Updating Personal Financial for accountHolderId {}", accountHolderId);
        var response = customerService.updatePersonalFinancialRelations(personalFinancialRelations.getData(), accountHolderId);
        LOG.info("Returning admin Personal Financial response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Delete("/{accountHolderId}/personal/financial-relations")
    public HttpResponse<Object> deletePersonalFinancialRelations(@PathVariable("accountHolderId") String accountHolderId) {
        LOG.info("Deleting Personal Financial by accountHolderId {}", accountHolderId);
        customerService.deletePersonalFinancialRelations(accountHolderId);
        LOG.info("Personal Financial deletion: Done. Returning 204 No Content");
        return HttpResponse.noContent();
    }

    @Post("/{accountHolderId}/personal/qualifications")
    public PersonalQualifications postPersonalQualifications(@PathVariable("accountHolderId") String accountHolderId,
                                                             @Body PersonalQualifications personalQualifications) {
        LOG.info("Posting Personal Qualifications for accountHolderId {}", accountHolderId);
        var response = customerService.addPersonalQualifications(personalQualifications.getData(), accountHolderId);
        LOG.info("Returning admin Personal Qualifications response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Put("/{accountHolderId}/personal/qualifications")
    public PersonalQualifications putPersonalQualifications(@PathVariable("accountHolderId") String accountHolderId,
                                                            @Body PersonalQualifications personalQualifications) {
        LOG.info("Updating Personal Qualifications for accountHolderId {}", accountHolderId);
        var response = customerService.updatePersonalQualifications(personalQualifications.getData(), accountHolderId);
        LOG.info("Returning admin Personal Qualifications response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Delete("/{accountHolderId}/personal/qualifications")
    public HttpResponse<Object> deletePersonalQualifications(@PathVariable("accountHolderId") String accountHolderId) {
        LOG.info("Deleting Personal Qualifications by accountHolderId {}", accountHolderId);
        customerService.deletePersonalQualifications(accountHolderId);
        LOG.info("Personal Qualifications deletion: Done. Returning 204 No Content");
        return HttpResponse.noContent();
    }

    @Post("/{accountHolderId}/business/identifications")
    public ResponseBusinessIdentification postBusinessIdentifications(@PathVariable("accountHolderId") String accountHolderId,
                                                                      @Body CreateBusinessIdentification businessIdentifications) {
        LOG.info("Posting Business Identifications for accountHolderId {}", accountHolderId);
        var response = customerService.addBusinessIdentifications(businessIdentifications.getData(), accountHolderId);
        LOG.info("Returning admin Business Identifications response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Put("/{accountHolderId}/business/identifications/{businessId}")
    public ResponseBusinessIdentification putBusinessIdentifications(@PathVariable("accountHolderId") String accountHolderId,
                                                                     @PathVariable("businessId") String businessId,
                                                                     @Body EditedBusinessIdentification businessIdentifications) {
        LOG.info("Updating Business Identifications by businessId {}for accountHolderId {}", businessId, accountHolderId);
        var response = customerService.updateBusinessIdentifications(businessId, businessIdentifications.getData());
        LOG.info("Returning admin Business Identifications response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Delete("/{accountHolderId}/business/identifications/{businessId}")
    public HttpResponse<Object> deleteBusinessIdentifications(@PathVariable("accountHolderId") String accountHolderId,
                                                              @PathVariable("businessId") String businessId) {
        LOG.info("Deleting Business Identifications by businessId {}for accountHolderId {}", businessId, accountHolderId);
        customerService.deleteBusinessIdentifications(businessId);
        LOG.info("Business Identifications deletion: Done. Returning 204 No Content");
        return HttpResponse.noContent();
    }

    @Post("/{accountHolderId}/business/financial-relations")
    public BusinessFinancialRelations postBusinessFinancialRelations(@PathVariable("accountHolderId") String accountHolderId,
                                                                     @Body BusinessFinancialRelations businessFinancialRelations) {
        LOG.info("Posting Business Financial Relations for accountHolderId {}", accountHolderId);
        var response = customerService.addBusinessFinancialRelations(businessFinancialRelations.getData(), accountHolderId);
        LOG.info("Returning admin Business Financial Relations response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Put("/{accountHolderId}/business/financial-relations")
    public BusinessFinancialRelations putBusinessFinancialRelations(@PathVariable("accountHolderId") String accountHolderId,
                                                                    @Body BusinessFinancialRelations businessFinancialRelations) {
        LOG.info("Updating Business Financial Relations for accountHolderId {}", accountHolderId);
        var response = customerService.updateBusinessFinancialRelations(businessFinancialRelations.getData(), accountHolderId);
        LOG.info("Returning admin Business Financial Relations response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Delete("/{accountHolderId}/business/financial-relations")
    public HttpResponse<Object> deleteBusinessFinancialRelations(@PathVariable("accountHolderId") String accountHolderId) {
        LOG.info("Deleting Business Financial Relations for accountHolderId {}", accountHolderId);
        customerService.deleteBusinessFinancialRelations(accountHolderId);
        LOG.info("Business Financial Relations deletion: Done. Returning 204 No Content");
        return HttpResponse.noContent();
    }

    @Post("/{accountHolderId}/business/qualifications")
    public BusinessQualifications postBusinessQualifications(@PathVariable("accountHolderId") String accountHolderId,
                                                             @Body BusinessQualifications businessQualifications) {
        LOG.info("Posting Business Qualifications for accountHolderId {}", accountHolderId);
        var response = customerService.addBusinessQualifications(businessQualifications.getData(), accountHolderId);
        LOG.info("Returning admin Business Qualifications response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Put("/{accountHolderId}/business/qualifications")
    public BusinessQualifications putBusinessQualifications(@PathVariable("accountHolderId") String accountHolderId,
                                                            @Body BusinessQualifications businessQualifications) {
        LOG.info("Updating Business Qualifications for accountHolderId {}", accountHolderId);
        var response = customerService.updateBusinessQualifications(businessQualifications.getData(), accountHolderId);
        LOG.info("Returning admin Business Qualifications response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Delete("/{accountHolderId}/business/qualifications")
    public HttpResponse<Object> deleteBusinessQualifications(@PathVariable("accountHolderId") String accountHolderId) {
        LOG.info("Deleting Business Qualifications for accountHolderId {}", accountHolderId);
        customerService.deleteBusinessQualifications(accountHolderId);
        LOG.info("Business Qualifications deletion: Done. Returning 204 No Content");
        return HttpResponse.noContent();
    }
}
