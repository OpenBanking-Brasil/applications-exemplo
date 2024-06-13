package com.raidiam.trustframework.bank

import com.raidiam.trustframework.bank.repository.*
import spock.lang.Shared

import javax.inject.Inject

class CleanupLocalStackSpecification extends AbstractLocalStackSpec {

    @Inject
    AccountRepository accountRepository
    @Inject
    AccountTransactionsRepository accountTransactionsRepository
    @Inject
    ConsentAccountRepository consentAccountRepository
    @Inject
    ConsentContractRepository consentContractRepository
    @Inject
    ConsentPermissionsRepository consentPermissionsRepository
    @Inject
    ConsentRepository consentRepository
    @Inject
    ContractBalloonPaymentsRepository contractBalloonPaymentsRepository
    @Inject
    ContractedFeesRepository contractedFeesRepository
    @Inject
    ContractedFinanceChargesRepository contractedFinanceChargesRepository
    @Inject
    ContractInterestRatesRepository contractInterestRatesRepository
    @Inject
    ContractOverParcelChargesRepository contractOverParcelChargesRepository
    @Inject
    ContractOverParcelFeesRepository contractOverParcelFeesRepository
    @Inject
    ContractReleasesRepository contractReleasesRepository
    @Inject
    ContractsRepository contractsRepository
    @Inject
    ContractWarrantiesRepository contractWarrantiesRepository
    @Inject
    CreditorAccountRepository creditorAccountRepository
    @Inject
    CreditorRepository creditorRepository
    @Inject
    JtiRepository jtiRepository
    @Inject
    PaymentConsentDetailsRepository paymentConsentDetailsRepository
    @Inject
    PaymentConsentPaymentRepository paymentConsentPaymentRepository
    @Inject
    PaymentConsentRepository paymentConsentRepository
    @Inject
    PixPaymentPaymentRepository pixPaymentPaymentRepository
    @Inject
    PixPaymentRepository pixPaymentRepository
    @Inject
    AccountHolderRepository accountHolderRepository
    @Inject
    BusinessIdentificationsRepository businessIdentificationsRepository
    @Inject
    BusinessIdentificationsCompanyCnpjRepository businessIdentificationsCompanyCnpjRepository
    @Inject
    BusinessOtherDocumentRepository businessOtherDocumentRepository
    @Inject
    BusinessEmailRepository businessEmailRepository
    @Inject
    BusinessFinancialRelationsRepository businessFinancialRelationsRepository
    @Inject
    BusinessFinancialRelationsProcuratorRepository businessFinancialRelationsProcuratorRepository
    @Inject
    BusinessFinancialRelationsProductsServicesRepository businessFinancialRelationsProductsServicesRepository
    @Inject
    BusinessPartyRepository businessPartyRepository
    @Inject
    BusinessPhoneRepository businessPhoneRepository
    @Inject
    BusinessPostalAddressRepository businessPostalAddressRepository
    @Inject
    BusinessQualificationsRepository businessQualificationsRepository
    @Inject
    BusinessQualificationsEconomicActivitiesRepository businessQualificationsEconomicActivitiesRepository
    @Inject
    PersonalCompanyCnpjRepository personalCompanyCnpjRepository
    @Inject
    PersonalEmailRepository personalEmailRepository
    @Inject
    PersonalFiliationRepository personalFiliationRepository
    @Inject
    PersonalFinancialRelationsRepository personalFinancialRelationsRepository
    @Inject
    PersonalFinancialRelationsProcuratorRepository personalFinancialRelationsProcuratorRepository
    @Inject
    PersonalFinancialRelationsProductsServicesRepository personalFinancialRelationsProductsServicesRepository
    @Inject
    PersonalNationalityDocumentRepository personalNationalityDocumentRepository
    @Inject
    PersonalNationalityRepository personalNationalityRepository
    @Inject
    PersonalOtherDocumentRepository personalOtherDocumentRepository
    @Inject
    PersonalPhoneRepository personalPhoneRepository
    @Inject
    PersonalPostalAddressRepository personalPostalAddressRepository
    @Inject
    PersonalQualificationsRepository personalQualificationsRepository
    @Inject
    PersonalIdentificationsRepository personalIdentificationsRepository
    @Inject
    ConsentCreditCardAccountsRepository consentCreditCardAccountsRepository
    @Inject
    CreditCardAccountsRepository creditCardAccountsRepository
    @Inject
    CreditCardAccountsBillsRepository creditCardAccountsBillsRepository
    @Inject
    CreditCardAccountsBillsFinanceChargeRepository creditCardAccountsBillsFinanceChargeRepository
    @Inject
    CreditCardAccountsLimitsRepository creditCardAccountsLimitsRepository
    @Inject
    CreditCardsAccountPaymentMethodRepository creditCardsAccountPaymentMethodRepository
    @Inject
    CreditCardAccountsBillsPaymentRepository creditCardAccountsBillsPaymentRepository
    @Inject
    CreditCardAccountsTransactionRepository creditCardAccountsTransactionRepository

    @Shared
    boolean runSetup = true

    @Shared
    boolean runCleanup = false

    def cleanup() {
        if (runCleanup) {
            pixPaymentRepository.deleteAll()
            creditorAccountRepository.deleteAll()
            pixPaymentPaymentRepository.deleteAll()
            paymentConsentRepository.deleteAll()
            paymentConsentPaymentRepository.deleteAll()
            paymentConsentDetailsRepository.deleteAll()
            creditorRepository.deleteAll()
            contractWarrantiesRepository.deleteAll()
            contractOverParcelChargesRepository.deleteAll()
            contractOverParcelFeesRepository.deleteAll()
            contractReleasesRepository.deleteAll()
            jtiRepository.deleteAll()
            contractInterestRatesRepository.deleteAll()
            contractedFinanceChargesRepository.deleteAll()
            contractedFeesRepository.deleteAll()
            consentAccountRepository.deleteAll()
            consentContractRepository.deleteAll()
            consentCreditCardAccountsRepository.deleteAll()
            consentPermissionsRepository.deleteAll()
            consentRepository.deleteAll()
            contractBalloonPaymentsRepository.deleteAll()
            contractsRepository.deleteAll()
            businessOtherDocumentRepository.deleteAll()
            businessPartyRepository.deleteAll()
            businessPhoneRepository.deleteAll()
            businessPostalAddressRepository.deleteAll()
            businessEmailRepository.deleteAll()
            businessIdentificationsCompanyCnpjRepository.deleteAll()
            businessIdentificationsRepository.deleteAll()
            businessFinancialRelationsProcuratorRepository.deleteAll()
            businessFinancialRelationsProductsServicesRepository.deleteAll()
            businessFinancialRelationsRepository.deleteAll()
            businessQualificationsEconomicActivitiesRepository.deleteAll()
            businessQualificationsRepository.deleteAll()
            personalCompanyCnpjRepository.deleteAll()
            personalOtherDocumentRepository.deleteAll()
            personalNationalityDocumentRepository.deleteAll()
            personalNationalityRepository.deleteAll()
            personalFiliationRepository.deleteAll()
            personalPhoneRepository.deleteAll()
            personalPostalAddressRepository.deleteAll()
            personalEmailRepository.deleteAll()
            personalIdentificationsRepository.deleteAll()
            personalFinancialRelationsProcuratorRepository.deleteAll()
            personalFinancialRelationsProductsServicesRepository.deleteAll()
            personalFinancialRelationsRepository.deleteAll()
            personalQualificationsRepository.deleteAll()
            accountTransactionsRepository.deleteAll()
            accountRepository.deleteAll()
            creditCardAccountsBillsFinanceChargeRepository.deleteAll()
            creditCardAccountsBillsPaymentRepository.deleteAll()
            creditCardAccountsTransactionRepository.deleteAll()
            creditCardAccountsBillsRepository.deleteAll()
            creditCardsAccountPaymentMethodRepository.deleteAll()
            creditCardAccountsLimitsRepository.deleteAll()
            creditCardAccountsRepository.deleteAll()
            accountHolderRepository.deleteAll()
            runCleanup = false
            runSetup = true
        }
    }
}
