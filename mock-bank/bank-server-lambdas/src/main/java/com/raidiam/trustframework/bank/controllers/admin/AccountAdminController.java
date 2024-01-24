package com.raidiam.trustframework.bank.controllers.admin;

import com.raidiam.trustframework.bank.controllers.BaseBankController;
import com.raidiam.trustframework.bank.services.AccountsService;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;

@RolesAllowed({"ADMIN_FULL_MANAGE"})
@Controller("/admin/customers/{accountHolderId}/accounts")
public class AccountAdminController extends BaseBankController {

    private static final Logger LOG = LoggerFactory.getLogger(AccountAdminController.class);
    private final AccountsService accountsService;

    AccountAdminController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @Value("${mockbank.max-page-size}")
    int maxPageSize;

    @Post
    public ResponseAccount postAccount(@PathVariable("accountHolderId") String accountHolderId,
                                       @Body CreateAccount account) {
        LOG.info("Posting new Account for Account Holder {}", accountHolderId);
        var response = accountsService.addAccount(account.getData(), accountHolderId);
        LOG.info("Returning Admin Account response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Put("/{accountId}")
    public ResponseAccount putAccount(@PathVariable("accountHolderId") String accountHolderId,
                                      @PathVariable("accountId") String accountId,
                                      @Body EditedAccount account) {
        LOG.info("Updating Account {} for AccountHolder {}", accountId, accountHolderId);
        var response = accountsService.updateAccount(accountId, account.getData());
        LOG.info("Returning Admin Account response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Delete("/{accountId}")
    public HttpResponse<Object> deleteAccount(@PathVariable("accountHolderId") String accountHolderId,
                                              @PathVariable("accountId") String accountId) {
        LOG.info("Deleting Account {} for AccountHolder {}", accountId, accountHolderId);
        accountsService.deleteAccount(accountId);
        LOG.info("Account deletion: Done. Returning 204 No Content");
        return HttpResponse.noContent();
    }

    @Post("/{accountId}/transactions")
    public ResponseAccountTransaction postTransaction(@PathVariable("accountHolderId") String accountHolderId,
                                                      @PathVariable("accountId") String accountId,
                                                      @Body CreateAccountTransaction transaction) {
        LOG.info("Posting account transactions for account {} for AccountHolder {}", accountId, accountHolderId);
        var response = accountsService.addTransaction(accountId, transaction.getData());
        LOG.info("Returning Admin Transaction response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Put("/{accountId}/transactions/{transactionsId}")
    public ResponseAccountTransaction putAccountTransaction(@PathVariable("accountHolderId") String accountHolderId,
                                                            @PathVariable("accountId") String accountId,
                                                            @PathVariable("transactionsId") String transactionId,
                                                            @Body CreateAccountTransaction transactionDto) {
        LOG.info("Updating account transactions for accountId {}, by transactionId {} for AccountHolder {}", accountId, transactionId, accountHolderId);
        var response = accountsService.updateAccountTransaction(accountId, transactionId, transactionDto.getData());
        LOG.info("Returning Admin Transaction response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Delete("/{accountId}/transactions/{transactionsId}")
    public HttpResponse<Object> deleteAccountTransaction(@PathVariable("accountHolderId") String accountHolderId,
                                                         @PathVariable("accountId") String accountId,
                                                         @PathVariable("transactionsId") String transactionId) {
        LOG.info("Deleting Account {} by transactionsId {} for AccountHolder {}", accountId, transactionId, accountHolderId);
        accountsService.deleteTransaction(accountId, transactionId);
        LOG.info("Account Transaction deletion: Done. Returning 204 No Content");
        return HttpResponse.noContent();
    }
}
