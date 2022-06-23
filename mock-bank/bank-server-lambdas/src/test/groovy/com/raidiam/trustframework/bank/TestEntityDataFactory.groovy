package com.raidiam.trustframework.bank

import com.raidiam.trustframework.bank.domain.*
import com.raidiam.trustframework.bank.enums.AccountOrContractType
import com.raidiam.trustframework.mockbank.models.generated.*
import jakarta.inject.Singleton

import java.time.Duration
import java.time.Instant
import java.time.LocalDate

import static com.raidiam.trustframework.mockbank.models.generated.PartiesParticipation.PersonTypeEnum.NATURAL

// there are no tests in here, however the annotations are used so that the cleanup spec and its repos can be used
@Singleton
class TestEntityDataFactory extends CleanupSpecification {

    static AccountEntity anAccount(UUID accountHolderId) {
        AccountEntity account = new AccountEntity()
        account.setAccountHolderId(accountHolderId)
        account.setAccountType(EnumAccountType.DEPOSITO_A_VISTA.toString())
        account.setAccountSubType(EnumAccountSubType.INDIVIDUAL.toString())
        account.setCurrency("GBP")
        account.setStatus("AVAILABLE")
        account.setBrandName("Some Brand")
        account.setBranchCode("0123")
        account.setCompanyCnpj("66758567000130")
        account.setCompeCode("123")
        account.setNumber("1234567890")
        account.setCheckDigit("1")
        account.setAvailableAmount(100.00)
        account.setAvailableAmountCurrency("GBP")
        account.setBlockedAmount(10.00)
        account.setBlockedAmountCurrency("GBP")
        account.setAutomaticallyInvestedAmount(1.00)
        account.setAutomaticallyInvestedAmountCurrency("GBP")
        account.setOverdraftContractedLimit(10.00)
        account.setOverdraftContractedLimitCurrency("GBP")
        account.setOverdraftUsedLimit(10.00)
        account.setOverdraftUsedLimitCurrency("GBP")
        account.setUnarrangedOverdraftAmount(11.00)
        account.setUnarrangedOverdraftAmountCurrency("GBP")

        account
    }

    static AccountEntity anAccount() {
        anAccount(UUID.randomUUID())
    }

    static aConsent (UUID accountHolderId) {
        def consent = new ConsentEntity()
        consent.setExpirationDateTime(Date.from(Instant.now() + Duration.ofDays(10)))
        consent.setTransactionFromDateTime(Date.from(Instant.now() - Duration.ofDays(10)))
        consent.setTransactionToDateTime(Date.from(Instant.now() - Duration.ofDays(9)))
        consent.setStatus(ResponseConsentData.StatusEnum.AUTHORISED.toString())
        consent.setCreationDateTime(Date.from(Instant.now()))
        consent.setStatusUpdateDateTime(Date.from(Instant.now()))
        consent.setConsentId(String.format("urn:raidiambank:%s", UUID.randomUUID().toString()))
        consent.setAccountHolderId(accountHolderId)
        consent
    }

    static aConsentPermission(CreateConsentData.PermissionsEnum permission, String consentId){
        def consentPermission = new ConsentPermissionEntity()
        consentPermission.setPermission(permission.name())
        consentPermission.setConsentId(consentId)
        consentPermission
    }

    static anAccountHolder (String identification, String rel) {
        def accountHolder = new AccountHolderEntity()
        accountHolder.setDocumentIdentification(identification)
        accountHolder.setDocumentRel(rel)
        accountHolder.setUserId('bob@test.com')
        accountHolder
    }

    static anAccountHolder () {
        anAccountHolder("01234567890", "ABC")
    }

    static ContractBalloonPaymentsEntity aBalloonPayment(UUID contractId) {
        ContractBalloonPaymentsEntity balloonPayment = new ContractBalloonPaymentsEntity()
        balloonPayment.setDueDate(LocalDate.parse("2022-01-20"))
        balloonPayment.setCurrency("BRL")
        balloonPayment.setAmount(20.2222)
        balloonPayment.setContractId(contractId)

        balloonPayment
    }

    static ContractWarrantyEntity aWarranty(UUID contractId) {
        ContractWarrantyEntity warranty = new ContractWarrantyEntity()
        warranty.setCurrency("Bells")
        warranty.setWarrantyType("SEM_TIPO_GARANTIA")
        warranty.setWarrantySubType("CHEQUES")
        warranty.setWarrantyAmount(99.99)
        warranty.setContractId(contractId)
        warranty
    }

    static ContractChargeOverParcelEntity anOverParcelCharge (UUID releasesId) {
        ContractChargeOverParcelEntity overParcelCharge = new ContractChargeOverParcelEntity()
        overParcelCharge.setChargeType("IOF_CONTRATACAO")
        overParcelCharge.setChargeAdditionalInfo("Money")
        overParcelCharge.setChargeAmount(19.23)
        overParcelCharge.setReleasesId(releasesId)
        overParcelCharge
    }

    static ContractFeeOverParcelEntity anOverParcelFee (UUID releasesId) {
        ContractFeeOverParcelEntity overParcelFee = new ContractFeeOverParcelEntity()
        overParcelFee.setFeeName("Steven")
        overParcelFee.setFeeCode("s4ewrv3d823ed-re2d")
        overParcelFee.setFeeAmount(30.99)
        overParcelFee.setReleasesId(releasesId)
        overParcelFee
    }

    static ContractReleasesEntity aContractReleasesEntity (UUID contractId) {
        ContractReleasesEntity release = new ContractReleasesEntity()
        release.setOverParcelPayment(true)
        release.setInstalmentId("e23dh72c823c-2d2323d4")
        release.setPaidDate(LocalDate.parse("2022-01-20"))
        release.setCurrency("BRL")
        release.setPaidAmount(20.00)
        release.setContractId(contractId)
        release
    }

    static ContractedFinanceChargesEntity aFinanceChargeEntity (UUID contractId) {
        ContractedFinanceChargesEntity charge = new ContractedFinanceChargesEntity()
        charge.setChargeType("JUROS_REMUNERATORIOS_POR_ATRASO")
        charge.setChargeAdditionalInfo("string")
        charge.setChargeRate(3.333)
        charge.setContractId(contractId)
        charge
    }

    static ContractedFeesEntity aContractedFeesEntity (UUID contractId) {
        ContractedFeesEntity fee = new ContractedFeesEntity()
        fee.setFeeName("string")
        fee.setFeeCode("string")
        fee.setFeeCharge("MINIMO")
        fee.setFeeChargeType("UNICA")
        fee.setFeeAmount(10.50)
        fee.setFeeRate(0.0150)
        fee.setContractId(contractId)
        fee
    }

    static ContractInterestRatesEntity aContractInterestRateEntity (UUID contractId) {
        ContractInterestRatesEntity interest = new ContractInterestRatesEntity()
        interest.setTaxType("NOMINAL")
        interest.setInterestRateType("SIMPLES")
        interest.setTaxPeriodicity("AM")
        interest.setCalculation("21/252")
        interest.setReferentialRateIndexerType("SEM_TIMPO_INDEXADOR")
        interest.setReferentialRateIndexerSubType("SEM_SUB_TIPO_INDEXADOR")
        interest.setReferentialRateIndexerAdditionalInfo("Cool")
        interest.setPreFixedRate(0.8)
        interest.setPostFixedRate(0.5)
        interest.setAdditionalInfo("Beans")
        interest.setContractId(contractId)
        interest
    }

    static ContractEntity aContractEntity (UUID accountHolderId, AccountOrContractType contractType, String productType, String productSubType) {
        ContractEntity newContract = new ContractEntity()
        newContract.setContractNumber("1")
        newContract.setIpocCode("341234nedfywefdy")
        newContract.setProductName("Bank of Nook")
        newContract.setContractType(contractType.toString())
        newContract.setAccountHolderId(accountHolderId)
        newContract.setProductType(productType)
        newContract.setProductSubType(productSubType)
        newContract.setContractDate(LocalDate.parse("2022-01-20"))
        newContract.setDisbursementDate(LocalDate.parse("2022-01-20"))
        newContract.setSettlementDate(LocalDate.parse("2022-01-20"))
        newContract.setContractAmount(354.56)
        newContract.setCurrency("Bells")
        newContract.setDueDate(LocalDate.parse("2022-01-20"))
        newContract.setInstalmentPeriodicity("SEM_PERIODICIDADE_REGULAR")
        newContract.setInstalmentPeriodicityAdditionalInfo("YES")
        newContract.setFirstInstalmentDueDate(LocalDate.parse("2022-01-20"))
        newContract.setCet(4.3)
        newContract.setAmortizationScheduled("SAC")
        newContract.setAmortizationScheduledAdditionalInfo("YEP")
        newContract.setCompanyCnpj("Tom Nook")
        newContract.setPaidInstalments(3)
        newContract.setContractOutstandingBalance(4.99)
        newContract.setTypeNumberOfInstalments("DIA")
        newContract.setTotalNumberOfInstalments(10)
        newContract.setTypeContractRemaining("DIA")
        newContract.setContractRemainingNumber(15)
        newContract.setDueInstalments(44)
        newContract.setPastDueInstalments(1)
        newContract.setStatus("AVAILABLE")
        newContract
    }

    ContractEntity createAndSaveFullContract(UUID accountHolderId, AccountOrContractType contractType, String productType, String productSubType) {
        ContractEntity newContract = aContractEntity(accountHolderId, contractType, productType, productSubType)
        contractsRepository.save(newContract)

        ContractInterestRatesEntity interest = aContractInterestRateEntity(newContract.getContractId())
        contractInterestRatesRepository.save(interest)

        ContractedFeesEntity fee = aContractedFeesEntity(newContract.getContractId())
        contractedFeesRepository.save(fee)
        ContractedFinanceChargesEntity charge = aFinanceChargeEntity(newContract.getContractId())
        contractedFinanceChargesRepository.save(charge)

        ContractReleasesEntity release = aContractReleasesEntity(newContract.getContractId())
        contractReleasesRepository.save(release)

        ContractFeeOverParcelEntity overParcelFee = anOverParcelFee(release.getReleasesId())
        contractOverParcelFeesRepository.save(overParcelFee)

        ContractChargeOverParcelEntity overParcelCharge = anOverParcelCharge(release.getReleasesId())
        contractOverParcelChargesRepository.save(overParcelCharge)

        ContractBalloonPaymentsEntity balloonPayment = aBalloonPayment(newContract.getContractId())
        contractBalloonPaymentsRepository.save(balloonPayment)

        ContractWarrantyEntity warranty = aWarranty(newContract.getContractId())
        contractWarrantiesRepository.save(warranty)

        Optional<ContractEntity> updatedContract = contractsRepository.findById(newContract.getContractId())

        return updatedContract.get()
    }

    static aBusinessIdentificationEntity (UUID accountHolderId) {
        def testBusinessIdentifications = new BusinessIdentificationsEntity()
        testBusinessIdentifications.setCnpjNumber("27051421000106")
        testBusinessIdentifications.setBrandName("Some Brand")
        testBusinessIdentifications.setTradeName("Trade Name")
        testBusinessIdentifications.setIncorporationDate(Date.from(Instant.now()))
        testBusinessIdentifications.setAccountHolderId(accountHolderId)

        testBusinessIdentifications
    }

    static aBusinessIdentificationCompanyCnpjEntity (UUID businessIdentificationsId) {
        def testBusinessIdentificationsCompanyCnpj = new BusinessIdentificationsCompanyCnpjEntity()
        testBusinessIdentificationsCompanyCnpj.setBusinessIdentificationsId(businessIdentificationsId)
        testBusinessIdentificationsCompanyCnpj.setCompanyCnpj("41726882000154")

        testBusinessIdentificationsCompanyCnpj
    }

    static aBusinessOtherDocument (UUID businessIdId) {
        def businessOtherDocument = new BusinessOtherDocumentEntity()
        businessOtherDocument.setType("BOD1")
        businessOtherDocument.setNumber("1")
        businessOtherDocument.setCountry("AU")
        businessOtherDocument.setExpirationDate(Date.from(Instant.now()))
        businessOtherDocument.setBusinessIdentificationsId(businessIdId)

        businessOtherDocument
    }

    static aBusinessParty (UUID businessIdId) {
        def businessParty = new BusinessPartyEntity()
        businessParty.setPersonType(NATURAL.toString())
        businessParty.setType(PartiesParticipation.TypeEnum.ADMINISTRADOR.toString())
        businessParty.setCivilName("Bob Civil")
        businessParty.setSocialName("Bob Social")
        businessParty.setCompanyName("Bob Company")
        businessParty.setTradeName("Bob Trade")
        businessParty.setStartDate(Date.from(Instant.now()))
        businessParty.setShareholding("ABC")
        businessParty.setDocumentType(EnumPartiesParticipationDocumentType.CPF.toString())
        businessParty.setDocumentNumber("45677371483")
        businessParty.setDocumentAdditionalInfo("N/A")
        businessParty.setDocumentCountry("BR")
        businessParty.setDocumentExpirationDate(Date.from(Instant.now()))
        businessParty.setDocumentIssueDate(Date.from(Instant.now()))
        businessParty.setBusinessIdentificationsId(businessIdId)

        businessParty
    }

    static aBusinessPostalAddress (UUID businessIdId) {
        def businessPostalAddress = new BusinessPostalAddressEntity()
        businessPostalAddress.setMain(true)
        businessPostalAddress.setAddress("1, Place")
        businessPostalAddress.setAdditionalInfo("N/A")
        businessPostalAddress.setDistrictName("District")
        businessPostalAddress.setTownName("Town")
        businessPostalAddress.setIbgeTownCode("1")
        businessPostalAddress.setCountrySubdivision("Subdivision")
        businessPostalAddress.setPostCode("PC")
        businessPostalAddress.setCountry("BR")
        businessPostalAddress.setCountryCode("BR")
        businessPostalAddress.setLatitude("1")
        businessPostalAddress.setLongitude("1")
        businessPostalAddress.setBusinessIdentificationsId(businessIdId)

        businessPostalAddress
    }

    static aBusinessPhone (UUID businessIdId) {
        def businessPhone = new BusinessPhoneEntity()
        businessPhone.setMain(true)
        businessPhone.setType(EnumCustomerPhoneType.FIXO.toString())
        businessPhone.setCountryCallingCode("+61")
        businessPhone.setAdditionalInfo("N/A")
        businessPhone.setAreaCode("11")
        businessPhone.setNumber("0000")
        businessPhone.setPhoneExtension("1")
        businessPhone.setBusinessIdentificationsId(businessIdId)

        businessPhone
    }

    static aBusinessEmail (UUID businessIdId) {
        def businessEmail = new BusinessEmailEntity()
        businessEmail.setMain(true)
        businessEmail.setEmail("bob@place")
        businessEmail.setBusinessIdentificationsId(businessIdId)

        businessEmail
    }

    static aBusinessFinancialRelations (UUID accountHolderId) {
        def businessFinancialRelations = new BusinessFinancialRelationsEntity()
        businessFinancialRelations.setStartDate(Date.from(Instant.now()))
        businessFinancialRelations.setAccountHolderId(accountHolderId)

        businessFinancialRelations
    }

    static aBusinessFinancialRelationsProductServicesType (UUID businessFinancialRelationsId) {
        def businessFinancialRelationsProductServicesType = new BusinessFinancialRelationsProductsServicesTypeEntity()
        businessFinancialRelationsProductServicesType.setType(EnumProductServiceType.CARTAO_CREDITO.toString())
        businessFinancialRelationsProductServicesType.setBusinessFinancialRelationsId(businessFinancialRelationsId)

        businessFinancialRelationsProductServicesType
    }

    static aBusinessFinancialRelationsProcurator (UUID businessFinancialRelationsId) {
        def businessFinancialRelationsProcurator = new BusinessFinancialRelationsProcuratorEntity()
        businessFinancialRelationsProcurator.setType(BusinessProcurator.TypeEnum.NAO_POSSUI.toString())
        businessFinancialRelationsProcurator.setCnpjCpfNumber("88777707753")
        businessFinancialRelationsProcurator.setCivilName("Bob Civil")
        businessFinancialRelationsProcurator.setSocialName("Bob Social")
        businessFinancialRelationsProcurator.setBusinessFinancialRelationsId(businessFinancialRelationsId)

        businessFinancialRelationsProcurator
    }

    static aBusinessQualifications (UUID accountHolderId) {
        def businessQualifications = new BusinessQualificationsEntity()
        businessQualifications.setInformedRevenueFrequency(EnumInformedRevenueFrequency.BIMESTRAL.toString())
        businessQualifications.setInformedRevenueFrequencyAdditionalInformation("EXTRAINFO")
        businessQualifications.setInformedRevenueAmount(1.0)
        businessQualifications.setInformedRevenueCurrency("GBP")
        businessQualifications.setInformedRevenueYear(1972)
        businessQualifications.setInformedPatrimonyAmount(1.0)
        businessQualifications.setInformedPatrimonyCurrency("AUD")
        businessQualifications.setInformedPatrimonyDate(Date.from(Instant.now()))
        businessQualifications.setAccountHolderId(accountHolderId)

        businessQualifications
    }

    static aBusinessQualificationsEconomicActivities (UUID businessQualificationsId) {
        def businessQualificationsEconomicActivities = new BusinessQualificationsEconomicActivitiesEntity()
        businessQualificationsEconomicActivities.setMain(true)
        businessQualificationsEconomicActivities.setCode(1)
        businessQualificationsEconomicActivities.setBusinessQualificationsId(businessQualificationsId)

        businessQualificationsEconomicActivities
    }

    static aPersonalIdentificationEntity (UUID accountHolderId) {
        def personalIdentifications = new PersonalIdentificationsEntity()
        personalIdentifications.setBrandName("Bill Brand")
        personalIdentifications.setCivilName("Bill Civil")
        personalIdentifications.setSocialName("Bill Social")
        personalIdentifications.setBirthDate(Date.from(Instant.now()))
        personalIdentifications.setMaritalStatusCode(EnumMaritalStatusCode.CASADO.toString())
        personalIdentifications.setMaritalStatusAdditionalInfo("N/A")
        personalIdentifications.setSex(EnumSex.FEMININO.toString())
        personalIdentifications.setHasBrazilianNationality(true)
        personalIdentifications.setCpfNumber("82837319805")
        personalIdentifications.setPassportNumber("01234567890")
        personalIdentifications.setPassportCountry("UK")
        personalIdentifications.setPassportExpirationDate(Date.from(Instant.now()))
        personalIdentifications.setPassportIssueDate(Date.from(Instant.now()))

        personalIdentifications.setAccountHolderId(accountHolderId)

        personalIdentifications
    }

    static aPersonalCompanyCnpj (UUID personalIdId) {
        def personalOtherDocument = new PersonalCompanyCnpjEntity()
        personalOtherDocument.setCompanyCnpj("55881412000170")
        personalOtherDocument.setPersonalIdentificationsId(personalIdId)

        personalOtherDocument
    }

    static aPersonalOtherDocument (UUID personalIdId) {
        def personalOtherDocument = new PersonalOtherDocumentEntity()
        personalOtherDocument.setType(EnumPersonalOtherDocumentType.OUTROS.toString())
        personalOtherDocument.setTypeAdditionalInfo("N/A")
        personalOtherDocument.setNumber("1")
        personalOtherDocument.setCheckDigit("2")
        personalOtherDocument.setAdditionalInfo("N/A")
        personalOtherDocument.setExpirationDate(Date.from(Instant.now()))
        personalOtherDocument.setPersonalIdentificationsId(personalIdId)

        personalOtherDocument
    }

    static aPersonalNationality (UUID personalIdId) {
        def personalNationality = new PersonalNationalityEntity()
        personalNationality.setOtherNationalitiesInfo("Other Info")
        personalNationality.setPersonalIdentificationsId(personalIdId)

        personalNationality
    }

    static aPersonalNationalityDocument (UUID personalNationalityId) {
        def personalNationalityDocument = new PersonalNationalityDocumentEntity()
        personalNationalityDocument.setType("Passport")
        personalNationalityDocument.setNumber("1")
        personalNationalityDocument.setExpirationDate(Date.from(Instant.now()))
        personalNationalityDocument.setIssueDate(Date.from(Instant.now()))
        personalNationalityDocument.setCountry("UK")
        personalNationalityDocument.setTypeAdditionalInfo("Additional Info")
        personalNationalityDocument.setPersonalNationalityId(personalNationalityId)

        personalNationalityDocument
    }

    static aPersonalFiliation (UUID personalIdId) {
        def personalNationalityDocument = new PersonalFiliationEntity()
        personalNationalityDocument.setType(EnumFiliationType.MAE.toString())
        personalNationalityDocument.setCivilName("Ted Civil")
        personalNationalityDocument.setSocialName("Ted Social")
        personalNationalityDocument.setPersonalIdentificationsId(personalIdId)

        personalNationalityDocument
    }

    static aPersonalPostalAddress (UUID personalIdId) {
        def personalPostalAddress = new PersonalPostalAddressEntity()
        personalPostalAddress.setMain(true)
        personalPostalAddress.setAddress("1, Place")
        personalPostalAddress.setAdditionalInfo("N/A")
        personalPostalAddress.setDistrictName("District")
        personalPostalAddress.setTownName("Town")
        personalPostalAddress.setIbgeTownCode("1")
        personalPostalAddress.setCountrySubdivision("Subdivision")
        personalPostalAddress.setPostCode("PC")
        personalPostalAddress.setCountry("BR")
        personalPostalAddress.setCountryCode("BR")
        personalPostalAddress.setLatitude("1")
        personalPostalAddress.setLongitude("1")
        personalPostalAddress.setPersonalIdentificationsId(personalIdId)

        personalPostalAddress
    }

    static aPersonalPhone (UUID personalIdId) {
        def personalPhone = new PersonalPhoneEntity()
        personalPhone.setMain(true)
        personalPhone.setType(EnumCustomerPhoneType.FIXO.toString())
        personalPhone.setCountryCallingCode("+61")
        personalPhone.setAdditionalInfo("N/A")
        personalPhone.setAreaCode("11")
        personalPhone.setNumber("0000")
        personalPhone.setPhoneExtension("1")
        personalPhone.setPersonalIdentificationsId(personalIdId)

        personalPhone
    }

    static aPersonalEmail (UUID personalIdId) {
        def personalEmail = new PersonalEmailEntity()
        personalEmail.setMain(true)
        personalEmail.setEmail("bob@place")
        personalEmail.setPersonalIdentificationsId(personalIdId)

        personalEmail
    }

    static aPersonalFinancialRelations (UUID accountHolderId) {
        def personalFinancialRelations = new PersonalFinancialRelationsEntity()
        personalFinancialRelations.setStartDate(Date.from(Instant.now()))
        personalFinancialRelations.setProductsServicesTypeAdditionalInfo("N/A")
        personalFinancialRelations.setAccountHolderId(accountHolderId)

        personalFinancialRelations
    }

    static aPersonalFinancialRelationsProductServicesType (UUID personalFinancialRelationsId) {
        def personalFinancialRelationsProductServicesType = new PersonalFinancialRelationsProductsServicesTypeEntity()
        personalFinancialRelationsProductServicesType.setType(EnumProductServiceType.CARTAO_CREDITO.toString())
        personalFinancialRelationsProductServicesType.setPersonalFinancialRelationsId(personalFinancialRelationsId)

        personalFinancialRelationsProductServicesType
    }

    static aPersonalFinancialRelationsProcurator (UUID personalFinancialRelationsId) {
        def personalFinancialRelationsProcurator = new PersonalFinancialRelationsProcuratorEntity()
        personalFinancialRelationsProcurator.setType(EnumProcuratorsTypePersonal.PROCURADOR.toString())
        personalFinancialRelationsProcurator.setCpfNumber("54119670155")
        personalFinancialRelationsProcurator.setCivilName("Alice Civil")
        personalFinancialRelationsProcurator.setSocialName("Alice Social")
        personalFinancialRelationsProcurator.setPersonalFinancialRelationsId(personalFinancialRelationsId)

        personalFinancialRelationsProcurator
    }

    static aPersonalQualifications (UUID accountHolderId) {
        def personalQualifications = new PersonalQualificationsEntity()
        personalQualifications.setCompanyCnpj("76615043000143")
        personalQualifications.setOccupationCode(EnumOccupationMainCodeType.CBO.toString())
        personalQualifications.setOccupationDescription("Occ Desc")
        personalQualifications.setInformedIncomeFrequency(EnumInformedIncomeFrequency.BIMESTRAL.toString())
        personalQualifications.setInformedIncomeAmount(1.0)
        personalQualifications.setInformedIncomeCurrency("AUD")
        personalQualifications.setInformedIncomeDate(Date.from(Instant.now()))
        personalQualifications.setInformedPatrimonyAmount(1.0)
        personalQualifications.setInformedPatrimonyCurrency("AUD")
        personalQualifications.setInformedPatrimonyYear(1986)
        personalQualifications.setAccountHolderId(accountHolderId)

        personalQualifications
    }

    static aTransaction (UUID accountId) {
        def transaction = new AccountTransactionsEntity()
        transaction.setAccountId(accountId)
        transaction.setTransactionId(UUID.randomUUID().toString())
        transaction.setCompletedAuthorisedPaymentType(EnumCompletedAuthorisedPaymentIndicator.LANCAMENTO_FUTURO.name())
        transaction.setCreditDebitType(EnumCreditDebitIndicator.DEBITO.name())
        transaction.setType(EnumTransactionTypes.BOLETO.name())
        transaction.setAmount(1.0)
        transaction.setTransactionCurrency('BRL')
        transaction.setTransactionDate(LocalDate.now())
        transaction.setTransactionName('My Transaction')
        transaction.setPartieCnpjCpf('47235211000177')
        transaction.setPartiePersonType(EnumPartiePersonType.JURIDICA.name())
        transaction.setPartieCompeCode('123')
        transaction.setPartieBranchCode('1234')
        transaction.setPartieNumber('9876543210')
        transaction.setPartieCheckDigit('1')
        transaction
    }

    static aTransaction (UUID accountId, LocalDate date) {
        def transaction = aTransaction(accountId)
        transaction.setTransactionDate(date)
        transaction
    }

    static CreditCardAccountsEntity anCreditCardAccounts(UUID accountHolderId) {
        CreditCardAccountsEntity creditCardAccount = new CreditCardAccountsEntity()
        creditCardAccount.setBrandName("Banco Bradesco S.A")
        creditCardAccount.setCompanyCnpj("60746948000112")
        creditCardAccount.setName("Cartão Pós Pago")
        creditCardAccount.setProductType(EnumCreditCardAccountsProductType.PLATINUM.name())
        creditCardAccount.setProductAdditionalInfo("NA")
        creditCardAccount.setCreditCardNetwork(EnumCreditCardAccountNetwork.VISA.name())
        creditCardAccount.setNetworkAdditionalInfo("NA")
        creditCardAccount.setStatus("AVAILABLE")
        creditCardAccount.setAccountHolderId(accountHolderId)

        creditCardAccount
    }

    static CreditCardsAccountPaymentMethodEntity anCreditCardsAccountPaymentMethod(UUID creditCardAccountId){
        CreditCardsAccountPaymentMethodEntity accountPaymentMethod = new CreditCardsAccountPaymentMethodEntity()
        accountPaymentMethod.setIdentificationNumber("5320")
        accountPaymentMethod.setMultipleCreditCard(false)
        accountPaymentMethod.setCreditCardAccountId(creditCardAccountId)

        accountPaymentMethod
    }

    static CreditCardAccountsLimitsEntity anCreditCardAccountsLimits(UUID creditCardAccountId){
        CreditCardAccountsLimitsEntity accountLimits = new CreditCardAccountsLimitsEntity()
        accountLimits.setCreditLineLimitType(EnumCreditCardAccountsLineLimitType.TOTAL.name())
        accountLimits.setConsolidationType(EnumCreditCardAccountsConsolidationType.CONSOLIDADO.name())
        accountLimits.setIdentificationNumber("5320")
        accountLimits.setLineName(EnumCreditCardAccountsLineName.CREDITO_A_VISTA.name())
        accountLimits.setLineNameAdditionalInfo("NA")
        accountLimits.setLimitFlexible(false)
        accountLimits.setLimitAmountCurrency("BRL")
        accountLimits.setLimitAmount(3000.0000)
        accountLimits.setUsedAmountCurrency("BRL")
        accountLimits.setUsedAmount(343.0400)
        accountLimits.setAvailableAmountCurrency("BRL")
        accountLimits.setAvailableAmount(2656.9600)
        accountLimits.setCreditCardAccountId(creditCardAccountId)

        accountLimits
    }

    static CreditCardAccountsBillsEntity anCreditCardAccountsBill(UUID creditCardAccountId){
        CreditCardAccountsBillsEntity creditCardAccountsBill = new CreditCardAccountsBillsEntity()
        creditCardAccountsBill.setDueDate(LocalDate.now())
        creditCardAccountsBill.setBillTotalAmount(409.2600)
        creditCardAccountsBill.setBillTotalAmountCurrency("BRL")
        creditCardAccountsBill.setBillMinimumAmount(143.8912)
        creditCardAccountsBill.setBillMinimumAmountCurrency("BRL")
        creditCardAccountsBill.setInstalment(true)
        creditCardAccountsBill.setCreditCardAccountId(creditCardAccountId)

        creditCardAccountsBill
    }

    static CreditCardAccountsBillsEntity anCreditCardAccountsBill(UUID creditCardAccountId, LocalDate dueDate){
        def bill = anCreditCardAccountsBill(creditCardAccountId)
        bill.setDueDate(dueDate)

        bill
    }

    static CreditCardAccountsBillsFinanceChargeEntity anCreditCardAccountsBillsFinanceCharge(UUID billId){
        CreditCardAccountsBillsFinanceChargeEntity billsFinanceCharge = new CreditCardAccountsBillsFinanceChargeEntity()
        billsFinanceCharge.setType(EnumCreditCardAccountsFinanceChargeType.JUROS_REMUNERATORIOS_ATRASO_PAGAMENTO_FATURA.name())
        billsFinanceCharge.setAdditionalInfo("NA")
        billsFinanceCharge.setAmount(35.4500)
        billsFinanceCharge.setCurrency("BRL")
        billsFinanceCharge.setBillId(billId)

        billsFinanceCharge
    }

    static CreditCardAccountsBillsPaymentEntity anCreditCardAccountsBillsPayment(UUID billId){
        CreditCardAccountsBillsPaymentEntity billsPayment = new CreditCardAccountsBillsPaymentEntity()
        billsPayment.setValueType(EnumCreditCardAccountsBillingValueType.OUTRO_VALOR_PAGO_FATURA.name())
        billsPayment.setPaymentDate(LocalDate.parse("2021-06-21"))
        billsPayment.setPaymentMode(EnumCreditCardAccountsPaymentMode.DEBITO_CONTA_CORRENTE.name())
        billsPayment.setAmount(1990.0000)
        billsPayment.setCurrency("BRL")
        billsPayment.setBillId(billId)

        billsPayment
    }

    static CreditCardAccountsTransactionEntity anCreditCardAccountsTransaction(UUID billId, UUID creditCardAccountId){
        CreditCardAccountsTransactionEntity accountTransaction = new CreditCardAccountsTransactionEntity()
        accountTransaction.setIdentificationNumber("5320")
        accountTransaction.setLineName(EnumCreditCardAccountsLineName.CREDITO_A_VISTA.name())
        accountTransaction.setTransactionName("ARMAZEM ")
        accountTransaction.setBillId(billId)
        accountTransaction.setCreditDebitType(EnumCreditDebitIndicator1.CREDITO.name())
        accountTransaction.setTransactionType(EnumCreditCardTransactionType.OPERACOES_CREDITO_CONTRATADAS_CARTAO.name())
        accountTransaction.setTransactionalAdditionalInfo("NA")
        accountTransaction.setPaymentType(EnumCreditCardAccountsPaymentType.VISTA.name())
        accountTransaction.setFeeType(EnumCreditCardAccountFee.SMS.name())
        accountTransaction.setFeeTypeAdditionalInfo("NA")
        accountTransaction.setOtherCreditsType(EnumCreditCardAccountsOtherCreditType.CREDITO_ROTATIVO.name())
        accountTransaction.setOtherCreditsAdditionalInfo("NA")
        accountTransaction.setChargeIdentificator("PARCELA_UNICA")
        accountTransaction.setChargeNumber(new BigDecimal(1))
        accountTransaction.setBrazilianAmount(2043.0400)
        accountTransaction.setAmount(0.0000)
        accountTransaction.setCurrency("BRL")
        accountTransaction.setTransactionDate(LocalDate.now())
        accountTransaction.setBillPostDate(LocalDate.now())
        accountTransaction.setPayeeMCC(new BigDecimal(5912))
        accountTransaction.setCreditCardAccountId(creditCardAccountId)

        accountTransaction
    }

    static CreditCardAccountsTransactionEntity anCreditCardAccountsTransaction(UUID billId, UUID creditCardAccountId,
                                                                               BigDecimal payeeMCC, String transactionType) {
        CreditCardAccountsTransactionEntity accountTransaction = new CreditCardAccountsTransactionEntity()
        accountTransaction.setIdentificationNumber("5320")
        accountTransaction.setLineName(EnumCreditCardAccountsLineName.CREDITO_A_VISTA.name())
        accountTransaction.setTransactionName("ARMAZEM ")
        accountTransaction.setBillId(billId)
        accountTransaction.setCreditDebitType(EnumCreditDebitIndicator1.CREDITO.name())
        accountTransaction.setTransactionType(transactionType)
        accountTransaction.setTransactionalAdditionalInfo("NA")
        accountTransaction.setPaymentType(EnumCreditCardAccountsPaymentType.VISTA.name())
        accountTransaction.setFeeType(EnumCreditCardAccountFee.SMS.name())
        accountTransaction.setFeeTypeAdditionalInfo("NA")
        accountTransaction.setOtherCreditsType(EnumCreditCardAccountsOtherCreditType.CREDITO_ROTATIVO.name())
        accountTransaction.setOtherCreditsAdditionalInfo("NA")
        accountTransaction.setChargeIdentificator("PARCELA_UNICA")
        accountTransaction.setChargeNumber(new BigDecimal(1))
        accountTransaction.setBrazilianAmount(2043.0400)
        accountTransaction.setAmount(0.0000)
        accountTransaction.setCurrency("BRL")
        accountTransaction.setTransactionDate(LocalDate.now())
        accountTransaction.setBillPostDate(LocalDate.now())
        accountTransaction.setPayeeMCC(payeeMCC)
        accountTransaction.setCreditCardAccountId(creditCardAccountId)

        accountTransaction
    }

    static CreditCardAccountsTransactionEntity anCreditCardAccountsTransaction(UUID billId, UUID creditCardAccountId, LocalDate transactionDate){
        def transaction = anCreditCardAccountsTransaction(billId, creditCardAccountId)
        transaction.setTransactionDate(transactionDate)

        transaction
    }
}
