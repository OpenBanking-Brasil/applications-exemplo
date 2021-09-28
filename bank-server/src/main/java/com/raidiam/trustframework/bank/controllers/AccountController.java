package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.models.generated.Level1Account;
import com.raidiam.trustframework.bank.services.AccountsService;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

@RolesAllowed({"ACCOUNTS_READ", "ACCOUNTS_WRITE"})
@Controller("/accounts/v1/accounts")
public class AccountController {
    AccountsService accountsService;

    AccountController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    // for demo/test purposes only?
    @Post
    @Status(HttpStatus.CREATED)
    @RolesAllowed({"ACCOUNTS_WRITE"})
    public Level1Account createAccount( @Body @Valid Level1Account body) {
        return accountsService.createAccount(body);
    }

    @Get
    public Page<Level1Account> getAccounts(Pageable pageable) {
        return accountsService.getAccounts(pageable);
    }

    @Get("/{accountId}")
    public Page<Level1Account> getAccount(@PathVariable("accountId") String acctId) {
        return accountsService.getAccount(acctId);
    }
}
