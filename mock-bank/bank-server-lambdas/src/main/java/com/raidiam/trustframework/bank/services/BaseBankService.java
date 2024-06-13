package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.repository.*;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;

import javax.inject.Inject;

abstract class BaseBankService {

  @Inject
  AccountRepository accountRepository;
  @Inject
  AccountTransactionsRepository accountTransactionsRepository;
  @Inject
  ConsentRepository consentRepository;
  @Inject
  ConsentExtensionRepository consentExtensionRepository;
  @Inject
  ConsentPermissionsRepository permissionsRepository;
  @Inject
  AccountHolderRepository accountHolderRepository;
  @Inject
  BusinessIdentificationsRepository businessIdentificationsRepository;
  @Inject
  BusinessIdentificationsCompanyCnpjRepository businessIdentificationsCompanyCnpjRepository;
  @Inject
  BusinessOtherDocumentRepository businessOtherDocumentRepository;
  @Inject
  BusinessPartyRepository businessPartyRepository;
  @Inject
  BusinessPostalAddressRepository businessPostalAddressRepository;
  @Inject
  BusinessPhoneRepository businessPhoneRepository;
  @Inject
  BusinessEmailRepository businessEmailRepository;
  @Inject
  BusinessFinancialRelationsRepository businessFinancialRelationsRepository;
  @Inject
  BusinessFinancialRelationsProductsServicesRepository businessFinancialRelationsProductsServicesRepository;
  @Inject
  BusinessFinancialRelationsProcuratorRepository businessFinancialRelationsProcuratorRepository;
  @Inject
  BusinessQualificationsRepository businessQualificationsRepository;
  @Inject
  BusinessQualificationsEconomicActivitiesRepository businessQualificationsEconomicActivitiesRepository;
  @Inject
  PersonalIdentificationsRepository personalIdentificationsRepository;
  @Inject
  PersonalCompanyCnpjRepository personalCompanyCnpjRepository;
  @Inject
  PersonalOtherDocumentRepository personalOtherDocumentRepository;
  @Inject
  PersonalNationalityRepository personalNationalityRepository;
  @Inject
  PersonalNationalityDocumentRepository personalNationalityDocumentRepository;
  @Inject
  PersonalFiliationRepository personalFiliationRepository;
  @Inject
  PersonalPostalAddressRepository personalPostalAddressRepository;
  @Inject
  PersonalPhoneRepository personalPhoneRepository;
  @Inject
  PersonalEmailRepository personalEmailRepository;
  @Inject
  PersonalFinancialRelationsRepository personalFinancialRelationsRepository;
  @Inject
  PersonalFinancialRelationsProductsServicesRepository personalFinancialRelationsProductsServicesRepository;
  @Inject
  PersonalFinancialRelationsProcuratorRepository personalFinancialRelationsProcuratorRepository;
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
  ConsentInvestmentRepository consentInvestmentRepository;
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
  CreditCardsAccountPaymentMethodRepository creditCardsAccountPaymentMethodRepository;
  @Inject
  CreditCardAccountsLimitsRepository creditCardAccountsLimitsRepository;
  @Inject
  CreditCardAccountsBillsFinanceChargeRepository creditCardAccountsBillsFinanceChargeRepository;
  @Inject
  CreditCardAccountsBillsPaymentRepository creditCardAccountsBillsPaymentRepository;
  @Inject
  CreditCardAccountsTransactionRepository creditCardAccountsTransactionRepository;
  @Inject
  CreditCardAccountsBillsRepository creditCardAccountsBillsRepository;
  @Inject
  PaymentConsentRepository paymentConsentRepository;
  @Inject
  PixPaymentRepository pixPaymentRepository;
  @Inject
  BankFixedIncomesRepository bankFixedIncomesRepository;
  @Inject
  BankFixedIncomesTransactionsRepository bankFixedIncomesTransactionsRepository;
  @Inject
  BankFixedIncomesBalancesRepository bankFixedIncomesBalancesRepository;
  @Inject
  CreditFixedIncomesRepository creditFixedIncomesRepository;
  @Inject
  CreditFixedIncomesTransactionsRepository creditFixedIncomesTransactionsRepository;
  @Inject
  CreditFixedIncomesBalancesRepository creditFixedIncomesBalancesRepository;
  @Inject
  FundsRepository fundsRepository;
  @Inject
  FundsBalancesRepository fundsBalancesRepository;
  @Inject
  FundsTransactionsRepository fundsTransactionsRepository;
  @Inject
  TreasureTitlesBalancesRepository treasureTitlesBalancesRepository;
  @Inject
  TreasureTitlesTransactionsRepository treasureTitlesTransactionsRepository;
  @Inject
  TreasureTitlesRepository treasureTitlesRepository;
  @Inject
  VariableIncomesBalancesRepository variableIncomesBalancesRepository;
  @Inject
  VariableIncomesRepository variableIncomesRepository;
  @Inject
  VariableIncomesBrokerNotesRepository variableIncomesBrokerNotesRepository;
  @Inject
  VariableIncomesTransactionsRepository variableIncomesTransactionsRepository;
  @Inject
  WebhookRepository webhookRepository;
  @Inject
  EnrollmentRepository enrollmentRepository;
  @Inject
  EnrollmentRiskSignalsRepository enrollmentRiskSignalsRepository;
  @Inject
  EnrollmentFidoRegistrationOptionsRepository enrollmentFidoRegistrationOptionsRepository;
  @Inject
  EnrollmentFidoSignOptionsRepository enrollmentFidoSignOptionsRepository;
  @Inject
  FidoJwkRepository fidoJwkRepository;
  @Inject
  JtiRepository jtiRepository;
  @Inject
  BankLambdaUtils bankLambdaUtils;
  @Inject
  ExchangesOperationRepository exchangesOperationRepository;
  @Inject
  ConsentExchangeOperationRepository consentExchangeOperationRepository;
  @Inject
  ExchangesOperationEventRepository exchangesOperationEventRepository;
  @Inject
  ScheduledDatesService scheduledDatesService;
  @Inject
  WebhookAdminService webhookAdminService;
}
