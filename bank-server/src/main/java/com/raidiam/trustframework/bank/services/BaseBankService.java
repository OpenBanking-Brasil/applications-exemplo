package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.repository.*;

import javax.inject.Inject;

abstract class BaseBankService {

  @Inject
  AccountRepository accountRepository;
  @Inject
  ConsentRepository consentRepository;
  @Inject
  ConsentPermissionsRepository permissionsRepository;
  @Inject
  PrivateAccountRepository privateAccountRepository;
  @Inject
  ConsentAccountIdRepository consentAccountIdRepository;

}
