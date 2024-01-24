package com.raidiam.trustframework.bank.controllers.admin;

import com.raidiam.trustframework.bank.controllers.BaseBankController;
import com.raidiam.trustframework.bank.services.CreditCardAccountsService;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;

@RolesAllowed({"ADMIN_FULL_MANAGE"})
@Controller("/admin/customers/{accountHolderId}/credit-cards-accounts")
public class CreditCardAdminController extends BaseBankController {

    private static final Logger LOG = LoggerFactory.getLogger(CreditCardAdminController.class);
    private final CreditCardAccountsService creditCardAccountsService;

    @Value("${mockbank.max-page-size}")
    int maxPageSize;

    CreditCardAdminController(CreditCardAccountsService creditCardAccountsService) {
        this.creditCardAccountsService = creditCardAccountsService;
    }

    @Post
    public ResponseCreditCardAccount postCreditCardAccount(@PathVariable("accountHolderId") String accountHolderId,
                                                           @Body CreateCreditCardAccount creditCard) {
        LOG.info("Posting credit card account for account holder {}", accountHolderId);
        var response = creditCardAccountsService.addCreditCardAccount(creditCard.getData(), accountHolderId);
        LOG.info("Returning post admin card account response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Put("/{creditCardAccountId}")
    public ResponseCreditCardAccount putCreditCardAccount(@PathVariable("accountHolderId") String accountHolderId,
                                                          @PathVariable("creditCardAccountId") String creditCardAccountId,
                                                          @Body EditedCreditCardAccount creditCard) {
        LOG.info("Updating credit card account for creditCardAccountId {}", creditCardAccountId);
        var response = creditCardAccountsService.updateCreditCardAccount(creditCardAccountId, creditCard);
        LOG.info("Returning put admin card account response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Delete("/{creditCardAccountId}")
    public HttpResponse<Object> deleteCreditCardAccount(@PathVariable("accountHolderId") String accountHolderId,
                                                        @PathVariable("creditCardAccountId") String creditCardAccountId) {
        LOG.info("Deleting credit card account for creditCardAccountId {}", creditCardAccountId);
        creditCardAccountsService.deleteCreditCardAccount(creditCardAccountId);
        LOG.info("Credit card account deletion: Done. Returning 204 No Content");
        return HttpResponse.noContent();
    }

    @Post("/{creditCardAccountId}/limits")
    public ResponseCreditCardAccountLimits postCreditCardAccountLimit(@PathVariable("accountHolderId") String accountHolderId,
                                                                      @PathVariable("creditCardAccountId") String creditCardAccountId,
                                                                      @Body CreateCreditCardAccountLimits limits) {
        LOG.info("Posting credit card account limits for creditCardAccountId {}", creditCardAccountId);
        var response = creditCardAccountsService.addCreditCardAccountLimit(creditCardAccountId, limits);
        LOG.info("Returning post admin card account limits response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Put("/{creditCardAccountId}/limits")
    public ResponseCreditCardAccountLimits putCreditCardAccountLimit(@PathVariable("accountHolderId") String accountHolderId,
                                                                     @PathVariable("creditCardAccountId") String creditCardAccountId,
                                                                     @Body CreateCreditCardAccountLimits limits) {
        LOG.info("Updating credit card account limits for creditCardAccountId {}", creditCardAccountId);
        var response = creditCardAccountsService.updateCreditCardAccountLimit(creditCardAccountId, limits);
        LOG.info("Returning put admin card account limits response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Delete("/{creditCardAccountId}/limits")
    public HttpResponse<Object> deleteCreditCardAccountLimit(@PathVariable("accountHolderId") String accountHolderId,
                                                             @PathVariable("creditCardAccountId") String creditCardAccountId) {
        LOG.info("Deleting credit card account limits for creditCardAccountId {}", creditCardAccountId);
        creditCardAccountsService.deleteCreditCardAccountLimit(creditCardAccountId);
        LOG.info("Credit credit card account limits deletion: Done. Returning 204 No Content");
        return HttpResponse.noContent();
    }

    @Post("/{creditCardAccountId}/bills")
    public ResponseCreditCardAccountBill postCreditCardBill(@PathVariable("accountHolderId") String accountHolderId,
                                                            @PathVariable("creditCardAccountId") String creditCardAccountId,
                                                            @Body CreateCreditCardAccountBill bill) {
        LOG.info("Posting credit card account bills for creditCardAccountId {}", creditCardAccountId);
        var response = creditCardAccountsService.addCreditCardBill(creditCardAccountId, bill);
        LOG.info("Returning post admin card account bills response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Put("/{creditCardAccountId}/bills/{billId}")
    public ResponseCreditCardAccountBill putCreditCardBill(@PathVariable("accountHolderId") String accountHolderId,
                                                           @PathVariable("creditCardAccountId") String creditCardAccountId,
                                                           @PathVariable("billId") String billId,
                                                           @Body CreateCreditCardAccountBill bill) {
        LOG.info("Updating credit card account bills for creditCardAccountId {} by billId {}", creditCardAccountId, billId);
        var response = creditCardAccountsService.updateCreditCardBill(creditCardAccountId, billId, bill);
        LOG.info("Returning put admin card account bills response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Delete("/{creditCardAccountId}/bills/{billId}")
    public HttpResponse<Object> deleteCreditCardBill(@PathVariable("accountHolderId") String accountHolderId,
                                                     @PathVariable("creditCardAccountId") String creditCardAccountId,
                                                     @PathVariable("billId") String billId) {
        LOG.info("Deleting credit card account bills for creditCardAccountId {} by billId {}", creditCardAccountId, billId);
        creditCardAccountsService.deleteCreditCardBill(creditCardAccountId, billId);
        LOG.info("Credit credit card account bills deletion: Done. Returning 204 No Content");
        return HttpResponse.noContent();
    }

    @Post("/{creditCardAccountId}/bills/{billId}/transactions")
    public ResponseCreditCardAccountTransactionList postCreditCardTransaction(@PathVariable("accountHolderId") String accountHolderId,
                                                                              @PathVariable("creditCardAccountId") String creditCardAccountId,
                                                                              @PathVariable("billId") String billId,
                                                                              @Body CreateCreditCardAccountTransactionList transaction) {
        LOG.info("Posting credit card account bills transactions for creditCardAccountId {} by billId {}", creditCardAccountId, billId);
        var response = creditCardAccountsService.addCreditCardTransaction(creditCardAccountId, billId, transaction);
        LOG.info("Returning post admin card account transactions response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Post("/v2/{creditCardAccountId}/bills/{billId}/transactions")
    public ResponseCreditCardAccountsTransactionListV2 postCreditCardTransactionV2(@PathVariable("accountHolderId") String accountHolderId,
                                                                              @PathVariable("creditCardAccountId") String creditCardAccountId,
                                                                              @PathVariable("billId") String billId,
                                                                              @Body CreateCreditCardAccountTransactionList transaction) {
        LOG.info("Posting credit card account bills transactions for creditCardAccountId {} by billId {}", creditCardAccountId, billId);
        var response = creditCardAccountsService.addCreditCardTransactionV2(creditCardAccountId, billId, transaction);
        LOG.info("Returning post admin card account transactions response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Put("/{creditCardAccountId}/bills/{billId}/transactions/{transactionId}")
    public ResponseCreditCardAccountTransaction putCreditCardTransaction(@PathVariable("accountHolderId") String accountHolderId,
                                                                         @PathVariable("creditCardAccountId") String creditCardAccountId,
                                                                         @PathVariable("billId") String billId,
                                                                         @PathVariable("transactionId") String transactionId,
                                                                         @Body EditedCreditCardAccountTransaction transaction) {
        LOG.info("Updating credit card account bills transactions for creditCardAccountId {} by billId {} and transactionId {}", creditCardAccountId, billId, transactionId);
        var response = creditCardAccountsService.updateCreditCardTransaction(creditCardAccountId, billId, transactionId, transaction);
        LOG.info("Returning put admin card account transactions response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Delete("/{creditCardAccountId}/bills/{billId}/transactions/{transactionId}")
    public HttpResponse<Object> putCreditCardTransaction(@PathVariable("accountHolderId") String accountHolderId,
                                                         @PathVariable("creditCardAccountId") String creditCardAccountId,
                                                         @PathVariable("billId") String billId,
                                                         @PathVariable("transactionId") String transactionId) {
        LOG.info("Deleting credit card account bills transactions {} by billId {} and transactionId {}", creditCardAccountId, billId, transactionId);
        creditCardAccountsService.deleteCreditCardTransaction(creditCardAccountId, billId, transactionId);
        LOG.info("Credit credit card account bills transactions deletion: Done. Returning 204 No Content");
        return HttpResponse.noContent();
    }
}
