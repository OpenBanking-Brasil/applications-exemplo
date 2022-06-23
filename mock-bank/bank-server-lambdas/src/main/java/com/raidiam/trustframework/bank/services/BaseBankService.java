package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.repository.*;

import javax.inject.Inject;

abstract class BaseBankService {

  @Inject
  AccountRepository accountRepository;
  @Inject
  AccountTransactionsRepository accountTransactionsRepository;
  @Inject
  ConsentRepository consentRepository;
  @Inject
  ConsentPermissionsRepository permissionsRepository;
  @Inject
  AccountHolderRepository accountHolderRepository;
  @Inject
  BusinessIdentificationsRepository businessIdentificationsRepository;
  @Inject
  BusinessFinancialRelationsRepository businessFinancialRelationsRepository;
  @Inject
  BusinessQualificationsRepository businessQualificationsRepository;
  @Inject
  PersonalIdentificationsRepository personalIdentificationsRepository;
  @Inject
  PersonalFinancialRelationsRepository personalFinancialRelationsRepository;
  @Inject
  PersonalQualificationsRepository personalQualificationsRepository;
  @Inject
  ContractsRepository contractsRepository;
  @Inject
  ConsentAccountRepository consentAccountRepository;
  @Inject
  ConsentCreditCardAccountsRepository consentCreditCardAccountsRepository;
  @Inject
  ConsentContractRepository consentContractRepository;
  @Inject
  ContractInterestRatesRepository contractInterestRatesRepository;
  @Inject
  ContractedFeesRepository contractedFeesRepository;
  @Inject
  ContractedFinanceChargesRepository contractedFinanceChargesRepository;
  @Inject
  ContractWarrantiesRepository contractWarrantiesRepository;
  @Inject
  ContractReleasesRepository contractReleasesRepository;
  @Inject
  ContractBalloonPaymentsRepository contractBalloonPaymentsRepository;
  @Inject
  ContractOverParcelFeesRepository contractOverParcelFeesRepository;
  @Inject
  ContractOverParcelChargesRepository contractOverParcelChargesRepository;
  @Inject
  CreditCardAccountsRepository creditCardAccountsRepository;
  @Inject
  CreditCardAccountsTransactionRepository creditCardAccountsTransactionRepository;
  @Inject
  CreditCardAccountsBillsRepository creditCardAccountsBillsRepository;
}
