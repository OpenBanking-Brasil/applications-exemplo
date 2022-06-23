package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.enums.AccountOrContractType;
import com.raidiam.trustframework.bank.services.UserService;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.ResponseAccountList;
import com.raidiam.trustframework.mockbank.models.generated.ResponseCreditCardAccountsList;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

@RolesAllowed("OP_QUERY_ROLE")
@Controller("/user/{userId}")
public class UserController extends BaseBankController {
    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    @Inject
    private UserService userService;

    @Get(value = "/credit-card-accounts", produces = {"application/json"})
    public ResponseCreditCardAccountsList getCreditCardAccounts(@PathVariable("userId") String userId, HttpRequest<?> request) {
        LOG.info("Getting credit card accounts for user id {}", userId);
        var response =  userService.getCreditCardAccounts(userId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Retrieved credit card accounts for user id {}", userId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get(value = "/loans", produces = {"application/json"})
    public ResponseAccountList getLoans(@PathVariable("userId") String userId, HttpRequest<?> request) {
        LOG.info("Getting loans for user id {}", userId);
        var response =  userService.getContractList(userId, AccountOrContractType.LOAN);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Retrieved loans for user id {}", userId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get(value = "/accounts", produces = {"application/json"})
    public ResponseAccountList getAccounts(@PathVariable("userId") String userId, HttpRequest<?> request) {
        LOG.info("Getting accounts for user id {}", userId);
        var response =  userService.getAccounts(userId);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Retrieved accounts for user id {}", userId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get(value = "/financings", produces = {"application/json"})
    public ResponseAccountList getFinancings(@PathVariable("userId") String userId, HttpRequest<?> request) {
        LOG.info("Getting financings for user id {}", userId);
        var response =  userService.getContractList(userId, AccountOrContractType.FINANCING);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Retrieved financings for user id {}", userId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get(value = "/invoice-financings", produces = {"application/json"})
    public ResponseAccountList getInvoiceFinancings(@PathVariable("userId") String userId, HttpRequest<?> request) {
        LOG.info("Getting invoice-financings for user id {}", userId);
        var response =  userService.getContractList(userId, AccountOrContractType.INVOICE_FINANCING);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Retrieved invoice-financings for user id {}", userId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get(value = "/unarranged-accounts-overdraft", produces = {"application/json"})
    public ResponseAccountList getUnarrangedAccountsOverdraft(@PathVariable("userId") String userId, HttpRequest<?> request) {
        LOG.info("Getting unarranged-accounts-overdraft for user id {}", userId);
        var response =  userService.getContractList(userId, AccountOrContractType.UNARRANGED_ACCOUNT_OVERDRAFT);
        BankLambdaUtils.decorateResponse(response::setLinks, response::setMeta, appBaseUrl + request.getPath(), 1);
        LOG.info("Retrieved unarranged-accounts-overdraft for user id {}", userId);
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }
}
