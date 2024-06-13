package com.raidiam.trustframework.bank

import com.raidiam.trustframework.bank.domain.*
import com.raidiam.trustframework.bank.enums.ContractStatusEnum
import com.raidiam.trustframework.bank.enums.ResourceType
import com.raidiam.trustframework.mockbank.models.generated.*
import jakarta.inject.Singleton

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime

import static com.raidiam.trustframework.mockbank.models.generated.PartiesParticipation.PersonTypeEnum.NATURAL

// there are no tests in here, however the annotations are used so that the cleanup spec and its repos can be used
@Singleton
class TestEntityDataFactory extends CleanupSpecification {

    static AccountEntity anAccount(AccountHolderEntity accountHolder, UUID accountHolderId) {
        AccountEntity account = new AccountEntity()
        account.setAccountHolder(accountHolder)
        account.setAccountHolderId(accountHolderId)
        account.setAccountType(EnumAccountTypeCustomersV2.DEPOSITO_A_VISTA.toString())
        account.setAccountSubType(EnumAccountSubTypeV2.INDIVIDUAL.toString())
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

    static AccountEntity anAccount(AccountHolderEntity accountHolder) {
        return anAccount(accountHolder, accountHolder.getAccountHolderId())
    }

    static AccountEntity anAccount(UUID accountHolderId) {
        return anAccount(null, accountHolderId)
    }

    static AccountEntity anAccount() {
        anAccount(null, UUID.randomUUID())
    }

    static aConsent (UUID accountHolderId) {
        def consent = new ConsentEntity()
        consent.setExpirationDateTime(Date.from(Instant.now() + Duration.ofDays(10)))
        consent.setTransactionFromDateTime(Date.from(Instant.now() - Duration.ofDays(10)))
        consent.setTransactionToDateTime(Date.from(Instant.now() - Duration.ofDays(9)))
        consent.setStatus(EnumConsentStatus.AUTHORISED.toString())
        consent.setCreationDateTime(Date.from(Instant.now()))
        consent.setStatusUpdateDateTime(Date.from(Instant.now()))
        consent.setConsentId(String.format("urn:raidiambank:%s", UUID.randomUUID().toString()))
        consent.setAccountHolderId(accountHolderId)
        consent
    }

    static aConsentPermission(EnumConsentPermissions permission, String consentId){
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
        long number = (long) Math.floor(Math.random() * 90000000000L) + 10000000000L
        anAccountHolder(Long.toString(number), "ABC")
    }

    static ContractBalloonPaymentsEntity aBalloonPayment(ContractEntity contract) {
        ContractBalloonPaymentsEntity balloonPayment = new ContractBalloonPaymentsEntity()
        balloonPayment.setDueDate(LocalDate.parse("2022-01-20"))
        balloonPayment.setCurrency("BRL")
        balloonPayment.setAmount(20.2222)
        balloonPayment.setContract(contract)

        balloonPayment
    }

    static ContractWarrantyEntity aWarranty(ContractEntity contract) {
        ContractWarrantyEntity warranty = new ContractWarrantyEntity()
        warranty.setCurrency("Bells")
        warranty.setWarrantyType("PENHOR")
        warranty.setWarrantySubType("CHEQUES")
        warranty.setWarrantyAmount(99.99)
        warranty.setContract(contract)
        warranty
    }

    static ContractChargeOverParcelEntity anOverParcelCharge (ContractReleasesEntity releases) {
        ContractChargeOverParcelEntity overParcelCharge = new ContractChargeOverParcelEntity()
        overParcelCharge.setChargeType("IOF_CONTRATACAO")
        overParcelCharge.setChargeAdditionalInfo("Money")
        overParcelCharge.setChargeAmount(19.23)
        overParcelCharge.setReleases(releases)
        overParcelCharge
    }

    static ContractFeeOverParcelEntity anOverParcelFee (ContractReleasesEntity releases) {
        ContractFeeOverParcelEntity overParcelFee = new ContractFeeOverParcelEntity()
        overParcelFee.setFeeName("Steven")
        overParcelFee.setFeeCode("s4ewrv3d823ed-re2d")
        overParcelFee.setFeeAmount(30.99)
        overParcelFee.setReleases(releases)
        overParcelFee
    }

    static ContractReleasesEntity aContractReleasesEntity (ContractEntity contract) {
        ContractReleasesEntity release = new ContractReleasesEntity()
        release.setOverParcelPayment(true)
        release.setInstalmentId("e23dh72c823c-2d2323d4")
        release.setPaidDate(LocalDate.parse("2022-01-20"))
        release.setCurrency("BRL")
        release.setPaidAmount(20.00)
        release.setContract(contract)
        release
    }

    static ContractedFinanceChargesEntity aFinanceChargeEntity (ContractEntity contract) {
        ContractedFinanceChargesEntity charge = new ContractedFinanceChargesEntity()
        charge.setChargeType("JUROS_REMUNERATORIOS_POR_ATRASO")
        charge.setChargeAdditionalInfo("string")
        charge.setChargeRate(3.333)
        charge.setContract(contract)
        charge
    }

    static ContractedFeesEntity aContractedFeesEntity (ContractEntity contract) {
        ContractedFeesEntity fee = new ContractedFeesEntity()
        fee.setFeeName("string")
        fee.setFeeCode("string")
        fee.setFeeCharge("MINIMO")
        fee.setFeeChargeType("UNICA")
        fee.setFeeAmount(10.50)
        fee.setFeeRate(0.0150)
        fee.setContract(contract)
        fee
    }

    static ContractInterestRatesEntity aContractInterestRateEntity (ContractEntity contract) {
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
        interest.setContract(contract)
        interest
    }

    static ContractEntity aContractEntity (UUID accountHolderId, ResourceType contractType, String productType, String productSubType, String status) {
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
        newContract.setStatus(status)
        newContract
    }

    ContractEntity createAndSaveFullContract(UUID accountHolderId, ResourceType contractType, String productType, String productSubType) {
        ContractEntity newContract = aContractEntity(accountHolderId, contractType, productType, productSubType, ContractStatusEnum.AVAILABLE.toString())

        newContract.setInterestRates(Set.of(aContractInterestRateEntity(newContract)))
        newContract.setContractedFees(Set.of(aContractedFeesEntity(newContract)))
        newContract.setContractedFinanceCharges(Set.of(aFinanceChargeEntity(newContract)))
        newContract.setBalloonPayments(Set.of(aBalloonPayment(newContract)))

        ContractReleasesEntity release = aContractReleasesEntity(newContract)
        release.setFees(Set.of(anOverParcelFee(release)))
        release.setCharges(Set.of(anOverParcelCharge(release)))
        newContract.setContractReleases(Set.of(release))

        def contractEntity = contractsRepository.save(newContract)

        contractWarrantiesRepository.save(aWarranty(contractEntity))

        Optional<ContractEntity> updatedContract = contractsRepository.findByContractIdAndContractType(contractEntity.getContractId(), contractEntity.getContractType())

        return updatedContract.get()
    }

    ExchangesOperationEntity createAndSaveExchangeOperation(UUID accountHolderId) {
        def newExchangeOperation = aExchangesOperationEntity()
        newExchangeOperation.setAccountHolderId(accountHolderId)
        def exchangeOperationEntity = exchangesOperationRepository.save(newExchangeOperation)

        exchangeOperationEntity
    }

    ExchangesOperationEntity createAndSaveUnavailableExchangeOperation(UUID accountHolderId) {
        def newExchangeOperation = aExchangesOperationEntity()
        newExchangeOperation.setAccountHolderId(accountHolderId)
        newExchangeOperation.setStatus("UNAVAILABLE")
        def exchangeOperationEntity = exchangesOperationRepository.save(newExchangeOperation)

        return exchangeOperationEntity
    }

    BankFixedIncomesEntity createAndSaveBankFixedIncome(UUID accountHolderId) {
        BankFixedIncomesEntity newBankFixedIncome = new BankFixedIncomesEntity()

        newBankFixedIncome.setAccountHolderId(accountHolderId)
        newBankFixedIncome.setBrandName("test Bank Fixed Income")
        newBankFixedIncome.setStatus("AVAILABLE")

        def bankFixedIncometEntity = bankFixedIncomesRepository.save(newBankFixedIncome)

        Optional<BankFixedIncomesEntity> updatedBankFixedIncome = bankFixedIncomesRepository.findByInvestmentId(bankFixedIncometEntity.getInvestmentId())

        return updatedBankFixedIncome.get()
    }

    CreditFixedIncomesEntity createAndSaveCreditFixedIncome(UUID accountHolderId) {
        CreditFixedIncomesEntity newCreditFixedIncome = new CreditFixedIncomesEntity()

        newCreditFixedIncome.setAccountHolderId(accountHolderId)
        newCreditFixedIncome.setBrandName("test Bank Fixed Income")
        newCreditFixedIncome.setStatus("AVAILABLE")

        def creditFixedIncometEntity = creditFixedIncomesRepository.save(newCreditFixedIncome)

        Optional<CreditFixedIncomesEntity> updatedCreditFixedIncome = creditFixedIncomesRepository.findByInvestmentId(creditFixedIncometEntity.getInvestmentId())

        return updatedCreditFixedIncome.get()
    }

    VariableIncomesEntity createAndSaveVariableIncome(UUID accountHolderId) {
        VariableIncomesEntity newVariableIncome = new VariableIncomesEntity()

        newVariableIncome.setAccountHolderId(accountHolderId)
        newVariableIncome.setBrandName("test Bank Fixed Income")
        newVariableIncome.setStatus("AVAILABLE")

        def variableIncometEntity = variableIncomesRepository.save(newVariableIncome)

        Optional<VariableIncomesEntity> updatedVariableIncome = variableIncomesRepository.findByInvestmentId(variableIncometEntity.getInvestmentId())

        return updatedVariableIncome.get()
    }

    TreasureTitlesEntity createAndSaveTreasureTitle(UUID accountHolderId) {
        TreasureTitlesEntity newTreasureTitle = new TreasureTitlesEntity()

        newTreasureTitle.setAccountHolderId(accountHolderId)
        newTreasureTitle.setBrandName("test Bank Fixed Income")
        newTreasureTitle.setStatus("AVAILABLE")

        def treasureTitleEntity = treasureTitlesRepository.save(newTreasureTitle)

        Optional<TreasureTitlesEntity> updatedTreasureTitle = treasureTitlesRepository.findByInvestmentId(treasureTitleEntity.getInvestmentId())

        return updatedTreasureTitle.get()
    }

    FundsEntity createAndSaveFund(UUID accountHolderId) {
        FundsEntity newFund = new FundsEntity()

        newFund.setAccountHolderId(accountHolderId)
        newFund.setBrandName("test Bank Fixed Income")
        newFund.setStatus("AVAILABLE")

        def fundEntity = fundsRepository.save(newFund)

        Optional<FundsEntity> updatedFund = fundsRepository.findByInvestmentId(fundEntity.getInvestmentId())

        return updatedFund.get()
    }

    ContractEntity createAndSaveFullContractUnavailable(UUID accountHolderId, ResourceType contractType, String productType, String productSubType) {
        ContractEntity newContract = aContractEntity(accountHolderId, contractType, productType, productSubType, ContractStatusEnum.UNAVAILABLE.toString())

        newContract.setInterestRates(Set.of(aContractInterestRateEntity(newContract)))
        newContract.setContractedFees(Set.of(aContractedFeesEntity(newContract)))
        newContract.setContractedFinanceCharges(Set.of(aFinanceChargeEntity(newContract)))
        newContract.setBalloonPayments(Set.of(aBalloonPayment(newContract)))

        ContractReleasesEntity release = aContractReleasesEntity(newContract)
        release.setFees(Set.of(anOverParcelFee(release)))
        release.setCharges(Set.of(anOverParcelCharge(release)))
        newContract.setContractReleases(Set.of(release))

        def contractEntity = contractsRepository.save(newContract)

        contractWarrantiesRepository.save(aWarranty(contractEntity))

        Optional<ContractEntity> updatedContract = contractsRepository.findByContractIdAndContractType(contractEntity.getContractId(), contractEntity.getContractType())

        return updatedContract.get()
    }

    static aBusinessIdentificationEntity (UUID accountHolderId) {
        def testBusinessIdentifications = new BusinessIdentificationsEntity()
        testBusinessIdentifications.setCnpjNumber("27051421000106")
        testBusinessIdentifications.setBrandName("Some Brand")
        testBusinessIdentifications.setTradeName("Trade Name")
        testBusinessIdentifications.setIncorporationDate(LocalDate.now())
        testBusinessIdentifications.setAccountHolderId(accountHolderId)

        testBusinessIdentifications
    }

    static aBusinessIdentificationCompanyCnpjEntity (BusinessIdentificationsEntity businessIdentifications) {
        def testBusinessIdentificationsCompanyCnpj = new BusinessIdentificationsCompanyCnpjEntity()
        testBusinessIdentificationsCompanyCnpj.setBusinessIdentifications(businessIdentifications)
        testBusinessIdentificationsCompanyCnpj.setCompanyCnpj("41726882000154")

        testBusinessIdentificationsCompanyCnpj
    }

    static aBusinessOtherDocument (BusinessIdentificationsEntity businessIdentifications) {
        def businessOtherDocument = new BusinessOtherDocumentEntity()
        businessOtherDocument.setType("BOD1")
        businessOtherDocument.setNumber("1")
        businessOtherDocument.setCountry("AU")
        businessOtherDocument.setExpirationDate(LocalDate.now())
        businessOtherDocument.setBusinessIdentifications(businessIdentifications)

        businessOtherDocument
    }

    static aBusinessParty (BusinessIdentificationsEntity businessIdentifications) {
        def businessParty = new BusinessPartyEntity()
        businessParty.setPersonType(NATURAL.toString())
        businessParty.setType(PartiesParticipation.TypeEnum.ADMINISTRADOR.toString())
        businessParty.setCivilName("Bob Civil")
        businessParty.setSocialName("Bob Social")
        businessParty.setCompanyName("Bob Company")
        businessParty.setTradeName("Bob Trade")
        businessParty.setStartDate(LocalDate.now())
        businessParty.setShareholding("123")
        businessParty.setDocumentType(EnumPartiesParticipationDocumentType.CPF.toString())
        businessParty.setDocumentNumber("45677371483")
        businessParty.setDocumentAdditionalInfo("N/A")
        businessParty.setDocumentCountry("BR")
        businessParty.setDocumentExpirationDate(LocalDate.now())
        businessParty.setDocumentIssueDate(LocalDate.now())
        businessParty.setBusinessIdentifications(businessIdentifications)

        businessParty
    }

    static aBusinessPostalAddress (BusinessIdentificationsEntity businessIdentifications) {
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
        businessPostalAddress.setBusinessIdentifications(businessIdentifications)

        businessPostalAddress
    }

    static aBusinessPhone (BusinessIdentificationsEntity businessIdentifications) {
        def businessPhone = new BusinessPhoneEntity()
        businessPhone.setMain(true)
        businessPhone.setType(EnumCustomerPhoneType.FIXO.toString())
        businessPhone.setCountryCallingCode("+61")
        businessPhone.setAdditionalInfo("N/A")
        businessPhone.setAreaCode("11")
        businessPhone.setNumber("0000")
        businessPhone.setPhoneExtension("1")
        businessPhone.setBusinessIdentifications(businessIdentifications)

        businessPhone
    }

    static aBusinessEmail (BusinessIdentificationsEntity businessIdentifications) {
        def businessEmail = new BusinessEmailEntity()
        businessEmail.setMain(true)
        businessEmail.setEmail("bob@place")
        businessEmail.setBusinessIdentifications(businessIdentifications)

        businessEmail
    }

    static aBusinessFinancialRelations (UUID accountHolderId) {
        def businessFinancialRelations = new BusinessFinancialRelationsEntity()
        businessFinancialRelations.setStartDate(LocalDate.now())
        businessFinancialRelations.setAccountHolderId(accountHolderId)

        businessFinancialRelations

    }

    static aBusinessFinancialRelationsProductServicesType (BusinessFinancialRelationsEntity businessFinancialRelations) {
        def businessFinancialRelationsProductServicesType = new BusinessFinancialRelationsProductsServicesTypeEntity()
        businessFinancialRelationsProductServicesType.setType(EnumProductServiceType.CARTAO_CREDITO.toString())
        businessFinancialRelationsProductServicesType.setFinancialRelations(businessFinancialRelations)

        businessFinancialRelationsProductServicesType
    }

    static aBusinessFinancialRelationsProcurator (BusinessFinancialRelationsEntity businessFinancialRelations) {
        def businessFinancialRelationsProcurator = new BusinessFinancialRelationsProcuratorEntity()
        businessFinancialRelationsProcurator.setType(BusinessProcurator.TypeEnum.NAO_POSSUI.toString())
        businessFinancialRelationsProcurator.setCnpjCpfNumber("88777707753")
        businessFinancialRelationsProcurator.setCivilName("Bob Civil")
        businessFinancialRelationsProcurator.setSocialName("Bob Social")
        businessFinancialRelationsProcurator.setFinancialRelations(businessFinancialRelations)

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
        businessQualifications.setInformedPatrimonyDate(LocalDate.now())
        businessQualifications.setAccountHolderId(accountHolderId)

        businessQualifications
    }

    static aBusinessQualificationsEconomicActivities (BusinessQualificationsEntity businessQualifications) {
        def businessQualificationsEconomicActivities = new BusinessQualificationsEconomicActivitiesEntity()
        businessQualificationsEconomicActivities.setMain(true)
        businessQualificationsEconomicActivities.setCode(1)
        businessQualificationsEconomicActivities.setQualification(businessQualifications)

        businessQualificationsEconomicActivities
    }

    static aPersonalIdentificationEntity (UUID accountHolderId) {
        def personalIdentifications = new PersonalIdentificationsEntity()
        personalIdentifications.setBrandName("Bill Brand")
        personalIdentifications.setCivilName("Bill Civil")
        personalIdentifications.setSocialName("Bill Social")
        personalIdentifications.setBirthDate(LocalDate.now())
        personalIdentifications.setMaritalStatusCode(EnumMaritalStatusCode.CASADO.toString())
        personalIdentifications.setMaritalStatusAdditionalInfo("N/A")
        personalIdentifications.setSex(EnumSex.FEMININO.toString())
        personalIdentifications.setHasBrazilianNationality(true)
        personalIdentifications.setCpfNumber("82837319805")
        personalIdentifications.setPassportNumber("01234567890")
        personalIdentifications.setPassportCountry("UK")
        personalIdentifications.setPassportExpirationDate(LocalDate.now())
        personalIdentifications.setPassportIssueDate(LocalDate.now())

        personalIdentifications.setAccountHolderId(accountHolderId)

        personalIdentifications
    }

    static aPersonalCompanyCnpj (PersonalIdentificationsEntity personal) {
        def personalCompanyCnpjEntity = new PersonalCompanyCnpjEntity()
        personalCompanyCnpjEntity.setCompanyCnpj("55881412000170")
        personalCompanyCnpjEntity.setIdentification(personal)

        personalCompanyCnpjEntity
    }

    static aPersonalOtherDocument (PersonalIdentificationsEntity personal) {
        def personalOtherDocument = new PersonalOtherDocumentEntity()
        personalOtherDocument.setType(EnumPersonalOtherDocumentType.OUTROS.toString())
        personalOtherDocument.setTypeAdditionalInfo("N/A")
        personalOtherDocument.setNumber("1")
        personalOtherDocument.setCheckDigit("2")
        personalOtherDocument.setAdditionalInfo("N/A")
        personalOtherDocument.setExpirationDate(LocalDate.now())
        personalOtherDocument.setIdentification(personal)

        personalOtherDocument
    }

    static aPersonalNationality (PersonalIdentificationsEntity personal) {
        def personalNationality = new PersonalNationalityEntity()
        personalNationality.setOtherNationalitiesInfo("Other Info")
        personalNationality.setIdentification(personal)

        personalNationality
    }

    static aPersonalNationalityDocument (PersonalNationalityEntity personalNationality) {
        def personalNationalityDocument = new PersonalNationalityDocumentEntity()
        personalNationalityDocument.setType("Passport")
        personalNationalityDocument.setNumber("1")
        personalNationalityDocument.setExpirationDate(LocalDate.now())
        personalNationalityDocument.setIssueDate(LocalDate.now())
        personalNationalityDocument.setCountry("UK")
        personalNationalityDocument.setTypeAdditionalInfo("Additional Info")
        personalNationalityDocument.setNationality(personalNationality)

        personalNationalityDocument
    }

    static aPersonalFiliation (PersonalIdentificationsEntity personal) {
        def filiationEntity = new PersonalFiliationEntity()
        filiationEntity.setType(EnumFiliationType.MAE.toString())
        filiationEntity.setCivilName("Ted Civil")
        filiationEntity.setSocialName("Ted Social")
        filiationEntity.setIdentification(personal)

        filiationEntity
    }

    static aPersonalPostalAddress (PersonalIdentificationsEntity personal) {
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
        personalPostalAddress.setIdentification(personal)

        personalPostalAddress
    }

    static aPersonalPhone (PersonalIdentificationsEntity personal) {
        def personalPhone = new PersonalPhoneEntity()
        personalPhone.setMain(true)
        personalPhone.setType(EnumCustomerPhoneType.FIXO.toString())
        personalPhone.setCountryCallingCode("+61")
        personalPhone.setAdditionalInfo("N/A")
        personalPhone.setAreaCode("11")
        personalPhone.setNumber("0000")
        personalPhone.setPhoneExtension("1")
        personalPhone.setIdentification(personal)

        personalPhone
    }

    static aPersonalEmail (PersonalIdentificationsEntity personal) {
        def personalEmail = new PersonalEmailEntity()
        personalEmail.setMain(true)
        personalEmail.setEmail("bob@place")
        personalEmail.setIdentification(personal)

        personalEmail
    }

    static aPersonalFinancialRelations (UUID accountHolderId) {
        def personalFinancialRelations = new PersonalFinancialRelationsEntity()
        personalFinancialRelations.setStartDate(LocalDate.now())
        personalFinancialRelations.setProductsServicesTypeAdditionalInfo("N/A")
        personalFinancialRelations.setAccountHolderId(accountHolderId)

        personalFinancialRelations
    }

    static aPersonalFinancialRelationsProductServicesType (PersonalFinancialRelationsEntity personalFinancialRelations) {
        def personalFinancialRelationsProductServicesType = new PersonalFinancialRelationsProductsServicesTypeEntity()
        personalFinancialRelationsProductServicesType.setType(EnumProductServiceType.CARTAO_CREDITO.toString())
        personalFinancialRelationsProductServicesType.setFinancialRelations(personalFinancialRelations)

        personalFinancialRelationsProductServicesType
    }

    static aPersonalFinancialRelationsProcurator (PersonalFinancialRelationsEntity personalFinancialRelations) {
        def personalFinancialRelationsProcurator = new PersonalFinancialRelationsProcuratorEntity()
        personalFinancialRelationsProcurator.setType(EnumProcuratorsTypePersonal.PROCURADOR.toString())
        personalFinancialRelationsProcurator.setCpfNumber("54119670155")
        personalFinancialRelationsProcurator.setCivilName("Alice Civil")
        personalFinancialRelationsProcurator.setSocialName("Alice Social")
        personalFinancialRelationsProcurator.setFinancialRelations(personalFinancialRelations)

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
        personalQualifications.setInformedIncomeDate(LocalDate.now())
        personalQualifications.setInformedPatrimonyAmount(1.0)
        personalQualifications.setInformedPatrimonyCurrency("AUD")
        personalQualifications.setInformedPatrimonyYear(1986)
        personalQualifications.setAccountHolderId(accountHolderId)

        personalQualifications
    }

    static aTransaction (UUID accountId) {
        def transaction = new AccountTransactionsEntity()
        transaction.setAccountId(accountId)
        transaction.setCompletedAuthorisedPaymentType(EnumCompletedAuthorisedPaymentIndicator.LANCAMENTO_FUTURO.name())
        transaction.setCreditDebitType(EnumCreditDebitIndicator.DEBITO.name())
        transaction.setType(EnumTransactionTypes.BOLETO.name())
        transaction.setAmount(1.0)
        transaction.setTransactionCurrency('BRL')
        transaction.setTransactionDateTime(OffsetDateTime.now())
        transaction.setTransactionName('My Transaction')
        transaction.setPartieCnpjCpf('47235211000177')
        transaction.setPartiePersonType(EnumPartiePersonType.JURIDICA.name())
        transaction.setPartieCompeCode('123')
        transaction.setPartieBranchCode('1234')
        transaction.setPartieNumber('9876543210')
        transaction.setPartieCheckDigit('1')
        transaction
    }

    static aTransaction (UUID accountId, OffsetDateTime date) {
        def transaction = aTransaction(accountId)
        transaction.setTransactionDateTime(date)
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

    static CreditCardsAccountPaymentMethodEntity anCreditCardsAccountPaymentMethod(CreditCardAccountsEntity account){
        CreditCardsAccountPaymentMethodEntity accountPaymentMethod = new CreditCardsAccountPaymentMethodEntity()
        accountPaymentMethod.setIdentificationNumber("5320")
        accountPaymentMethod.setMultipleCreditCard(false)
        accountPaymentMethod.setAccount(account)

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

    static CreditCardAccountsBillsEntity anCreditCardAccountsBill(CreditCardAccountsEntity account){
        CreditCardAccountsBillsEntity creditCardAccountsBill = new CreditCardAccountsBillsEntity()
        creditCardAccountsBill.setDueDate(LocalDate.now())
        creditCardAccountsBill.setBillTotalAmount(409.2600)
        creditCardAccountsBill.setBillTotalAmountCurrency("BRL")
        creditCardAccountsBill.setBillMinimumAmount(143.8912)
        creditCardAccountsBill.setBillMinimumAmountCurrency("BRL")
        creditCardAccountsBill.setInstalment(true)
        creditCardAccountsBill.setAccount(account)

        creditCardAccountsBill
    }

    static CreditCardAccountsBillsEntity anCreditCardAccountsBill(CreditCardAccountsEntity account, LocalDate dueDate){
        def bill = anCreditCardAccountsBill(account)
        bill.setDueDate(dueDate)

        bill
    }

    static CreditCardAccountsBillsFinanceChargeEntity anCreditCardAccountsBillsFinanceCharge(CreditCardAccountsBillsEntity bill){
        CreditCardAccountsBillsFinanceChargeEntity billsFinanceCharge = new CreditCardAccountsBillsFinanceChargeEntity()
        billsFinanceCharge.setType(EnumCreditCardAccountsFinanceChargeType.JUROS_REMUNERATORIOS_ATRASO_PAGAMENTO_FATURA.name())
        billsFinanceCharge.setAdditionalInfo("NA")
        billsFinanceCharge.setAmount(35.4500)
        billsFinanceCharge.setCurrency("BRL")
        billsFinanceCharge.setBill(bill)

        billsFinanceCharge
    }

    static CreditCardAccountsBillsPaymentEntity anCreditCardAccountsBillsPayment(CreditCardAccountsBillsEntity bill){
        CreditCardAccountsBillsPaymentEntity billsPayment = new CreditCardAccountsBillsPaymentEntity()
        billsPayment.setValueType(EnumCreditCardAccountsBillingValueType.OUTRO_VALOR_PAGO_FATURA.name())
        billsPayment.setPaymentDate(LocalDate.parse("2021-06-21"))
        billsPayment.setPaymentMode(EnumCreditCardAccountsPaymentMode.DEBITO_CONTA_CORRENTE.name())
        billsPayment.setAmount(1990.0000)
        billsPayment.setCurrency("BRL")
        billsPayment.setBill(bill)

        billsPayment
    }

    static CreditCardAccountsTransactionEntity anCreditCardAccountsTransaction(UUID billId, UUID creditCardAccountId){
        CreditCardAccountsTransactionEntity accountTransaction = new CreditCardAccountsTransactionEntity()
        accountTransaction.setIdentificationNumber("5320")
        accountTransaction.setLineName(EnumCreditCardAccountsLineName.CREDITO_A_VISTA.name())
        accountTransaction.setTransactionName("ARMAZEM ")
        accountTransaction.setBillId(billId)
        accountTransaction.setCreditDebitType(EnumCreditDebitIndicator.CREDITO.name())
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
        accountTransaction.setTransactionDateTime(OffsetDateTime.now())
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
        accountTransaction.setCreditDebitType(EnumCreditDebitIndicator.CREDITO.name())
        accountTransaction.setTransactionType(transactionType)
        accountTransaction.setTransactionalAdditionalInfo("NA")
        accountTransaction.setPaymentType(EnumCreditCardAccountsPaymentType.VISTA.name())
        accountTransaction.setFeeType(EnumCreditCardAccountFee.SMS.name())
        accountTransaction.setFeeTypeAdditionalInfo("NA")
        accountTransaction.setOtherCreditsType(EnumCreditCardAccountsOtherCreditType.CREDITO_ROTATIVO.name())
        accountTransaction.setOtherCreditsAdditionalInfo("NA")
        accountTransaction.setChargeIdentificator("1")
        accountTransaction.setChargeNumber(new BigDecimal(1))
        accountTransaction.setBrazilianAmount(2043.0400)
        accountTransaction.setAmount(0.0000)
        accountTransaction.setCurrency("BRL")
        accountTransaction.setTransactionDateTime(OffsetDateTime.now())
        accountTransaction.setBillPostDate(LocalDate.now())
        accountTransaction.setPayeeMCC(payeeMCC)
        accountTransaction.setCreditCardAccountId(creditCardAccountId)

        accountTransaction
    }

    static CreditCardAccountsTransactionEntity anCreditCardAccountsTransaction(UUID billId, UUID creditCardAccountId, OffsetDateTime transactionDate){
        def transaction = anCreditCardAccountsTransaction(billId, creditCardAccountId)
        transaction.setTransactionDateTime(transactionDate)

        transaction
    }

    static BankFixedIncomesEntity aBankFixedIncomesEntity() {
        BankFixedIncomesEntity bankFixedIncomesEntity = new BankFixedIncomesEntity()
        bankFixedIncomesEntity.setBrandName("Banco do Brasil S.A")
        bankFixedIncomesEntity.setCompanyCnpj("00000000000191")
        bankFixedIncomesEntity.setInvestmentType("CDB")
        bankFixedIncomesEntity.setIsinCode("BBBRT4CTF001")
        bankFixedIncomesEntity.setDueDate(LocalDate.of(2023, 02,  15))
        bankFixedIncomesEntity.setIssueDate(LocalDate.of(2023, 02,  15))
        bankFixedIncomesEntity.setClearingCode("CDB421GPXXX")
        bankFixedIncomesEntity.setPurchaseDate(LocalDate.of(2023, 02,  15))
        bankFixedIncomesEntity.setGracePeriodDate(LocalDate.of(2023, 02,  15))

        bankFixedIncomesEntity
    }

    static BankFixedIncomesBalanceEntity aBankFixedIncomesBalanceEntity(BankFixedIncomesEntity bankFixedIncomesEntity) {
        BankFixedIncomesBalanceEntity bankFixedIncomesBalanceEntity = new BankFixedIncomesBalanceEntity()
        bankFixedIncomesBalanceEntity.setInvestment(bankFixedIncomesEntity)
        bankFixedIncomesBalanceEntity.setQuantity(1000.0004)
        bankFixedIncomesBalanceEntity.setPreFixedRate(0.300000)
        bankFixedIncomesBalanceEntity.setPostFixedIndexerPercentage(1.000000)
        bankFixedIncomesBalanceEntity.setReferenceDateTime(LocalDate.of(2023, 04,  21))

        bankFixedIncomesBalanceEntity
    }

    static BankFixedIncomesTransactionsEntity aBankFixedIncomesTransactionsEntity(BankFixedIncomesEntity bankFixedIncomesEntity) {
        BankFixedIncomesTransactionsEntity bankFixedIncomesBalanceEntity = new BankFixedIncomesTransactionsEntity()
        bankFixedIncomesBalanceEntity.setInvestmentId(bankFixedIncomesEntity.getInvestmentId())
        bankFixedIncomesBalanceEntity.setInvestment(bankFixedIncomesEntity)
        bankFixedIncomesBalanceEntity.setTransactionType("APLICACAO")
        bankFixedIncomesBalanceEntity.setType("ENTRADA")
        bankFixedIncomesBalanceEntity.setTransactionDate(LocalDate.now())
        bankFixedIncomesBalanceEntity.setTransactionQuantity(42.25)

        bankFixedIncomesBalanceEntity
    }

    static CreditFixedIncomesEntity aCreditFixedIncomesEntity() {
        CreditFixedIncomesEntity creditFixedIncomesEntity = new CreditFixedIncomesEntity()
        creditFixedIncomesEntity.setBrandName("Banco do Brasil S.A")
        creditFixedIncomesEntity.setCompanyCnpj("00000000000191")
        creditFixedIncomesEntity.setInvestmentType("CDB")
        creditFixedIncomesEntity.setIsinCode("BBBRT4CTF001")
        creditFixedIncomesEntity.setDueDate(LocalDate.of(2023, 02,  15))
        creditFixedIncomesEntity.setIssueDate(LocalDate.of(2023, 02,  15))
        creditFixedIncomesEntity.setClearingCode("CDB421GPXXX")
        creditFixedIncomesEntity.setPurchaseDate(LocalDate.of(2023, 02,  15))
        creditFixedIncomesEntity.setGracePeriodDate(LocalDate.of(2023, 02,  15))

        creditFixedIncomesEntity
    }

    static CreditFixedIncomesBalanceEntity aCreditFixedIncomesBalanceEntity(CreditFixedIncomesEntity creditFixedIncomesEntity) {
        CreditFixedIncomesBalanceEntity creditFixedIncomesBalanceEntity = new CreditFixedIncomesBalanceEntity()
        creditFixedIncomesBalanceEntity.setInvestment(creditFixedIncomesEntity)
        creditFixedIncomesBalanceEntity.setQuantity(1000.0004)
        creditFixedIncomesBalanceEntity.setPreFixedRate(0.300000)
        creditFixedIncomesBalanceEntity.setPostFixedIndexerPercentage(1.000000)
        creditFixedIncomesBalanceEntity.setReferenceDateTime(LocalDate.of(2023, 04,  21))

        creditFixedIncomesBalanceEntity
    }

    static CreditFixedIncomesTransactionsEntity aCreditFixedIncomesTransactionsEntity(CreditFixedIncomesEntity creditFixedIncomesEntity) {
        CreditFixedIncomesTransactionsEntity creditFixedIncomesBalanceEntity = new CreditFixedIncomesTransactionsEntity()
        creditFixedIncomesBalanceEntity.setInvestmentId(creditFixedIncomesEntity.getInvestmentId())
        creditFixedIncomesBalanceEntity.setInvestment(creditFixedIncomesEntity)
        creditFixedIncomesBalanceEntity.setTransactionType("APLICACAO")
        creditFixedIncomesBalanceEntity.setType("ENTRADA")
        creditFixedIncomesBalanceEntity.setTransactionDate(LocalDate.now())
        creditFixedIncomesBalanceEntity.setTransactionQuantity(42.25)

        creditFixedIncomesBalanceEntity
    }

    static FundsEntity aFundsEntity() {
        FundsEntity fundsEntity = new FundsEntity()
        fundsEntity.setBrandName("Banco do Brasil S.A")
        fundsEntity.setCompanyCnpj("00000000000191")
        fundsEntity.setIsinCode("BBBRT4CTF001")
        fundsEntity.setAnbimaCategory("RENDA_FIXA")
        fundsEntity.setAnbimaClass("Renda Fixa")
        fundsEntity.setAnbimaSubclass("Longo Prazo")

        return fundsEntity
    }

    static FundsBalanceEntity aFundsBalanceEntity(FundsEntity fundsEntity) {
        FundsBalanceEntity fundsBalanceEntity = new FundsBalanceEntity()
        fundsBalanceEntity.setInvestment(fundsEntity)
        fundsBalanceEntity.setQuotaQuantity(11.1)
        fundsBalanceEntity.setReferenceDate(LocalDate.of(2023, 04, 21))

        return fundsBalanceEntity
    }

    static FundsTransactionsEntity aFundsTransactionsEntity(FundsEntity fundsEntity) {
        FundsTransactionsEntity fundsTransactionsEntity = new FundsTransactionsEntity()
        fundsTransactionsEntity.setInvestmentId(fundsEntity.getInvestmentId())
        fundsTransactionsEntity.setInvestment(fundsEntity)
        fundsTransactionsEntity.setTransactionType("APLICACAO")
        fundsTransactionsEntity.setType("ENTRADA")
        fundsTransactionsEntity.setTransactionConversionDate(LocalDate.now())

        return fundsTransactionsEntity
    }

    static TreasureTitlesEntity aTreasureTitlesEntity() {
        TreasureTitlesEntity treasureTitlesEntity = new TreasureTitlesEntity()
        treasureTitlesEntity.setBrandName("Banco do Brasil S.A")
        treasureTitlesEntity.setCompanyCnpj("00000000000191")
        treasureTitlesEntity.setIsinCode("BBBRT4CTF001")
        treasureTitlesEntity.setDueDate(LocalDate.of(2023, 02, 15))
        treasureTitlesEntity.setPurchaseDate(LocalDate.of(2023, 02, 15))

        return treasureTitlesEntity
    }

    static TreasureTitlesBalanceEntity aTreasureTitlesBalanceEntity(TreasureTitlesEntity treasureTitlesEntity) {
        TreasureTitlesBalanceEntity treasureTitlesBalanceEntity = new TreasureTitlesBalanceEntity()
        treasureTitlesBalanceEntity.setInvestment(treasureTitlesEntity)
        treasureTitlesBalanceEntity.setQuantity(1000.0004)
        treasureTitlesBalanceEntity.setReferenceDateTime(LocalDate.of(2023, 04, 21))

        return treasureTitlesBalanceEntity
    }

    static TreasureTitlesTransactionsEntity aTreasureTitlesTransactionsEntity(TreasureTitlesEntity treasureTitlesEntity) {
        TreasureTitlesTransactionsEntity treasureTitlesTransactionsEntity = new TreasureTitlesTransactionsEntity()
        treasureTitlesTransactionsEntity.setInvestmentId(treasureTitlesEntity.getInvestmentId())
        treasureTitlesTransactionsEntity.setInvestment(treasureTitlesEntity)
        treasureTitlesTransactionsEntity.setTransactionType("APLICACAO")
        treasureTitlesTransactionsEntity.setType("ENTRADA")
        treasureTitlesTransactionsEntity.setTransactionDate(LocalDate.now())
        treasureTitlesTransactionsEntity.setTransactionQuantity(42.25)

        return treasureTitlesTransactionsEntity
    }

    static VariableIncomesEntity aVariableIncomesEntity() {
        VariableIncomesEntity variableIncomesEntity = new VariableIncomesEntity()
        variableIncomesEntity.setBrandName("Banco do Brasil S.A")
        variableIncomesEntity.setCompanyCnpj("00000000000191")
        variableIncomesEntity.setIsinCode("BBBRT4CTF001")

        return variableIncomesEntity
    }

    static VariableIncomesBalanceEntity aVariableIncomesBalanceEntity(VariableIncomesEntity variableIncomesEntity) {
        VariableIncomesBalanceEntity variableIncomesBalanceEntity = new VariableIncomesBalanceEntity()
        variableIncomesBalanceEntity.setInvestment(variableIncomesEntity)
        variableIncomesBalanceEntity.setQuantity(1000.0004)

        return variableIncomesBalanceEntity
    }

    static VariableIncomesBrokerNotesEntity aVariableIncomesBrokerNotesEntity() {
        VariableIncomesBrokerNotesEntity variableIncomesBrokerNotes = new VariableIncomesBrokerNotesEntity()
        variableIncomesBrokerNotes.setBrokerNoteNumber("11111111111")

        return variableIncomesBrokerNotes
    }

    static VariableIncomesTransactionsEntity aVariableIncomesTransactionsEntity(VariableIncomesEntity variableIncomesEntity, UUID brokerNoteId) {
        VariableIncomesTransactionsEntity variableIncomesTransactionsEntity = new VariableIncomesTransactionsEntity()
        variableIncomesTransactionsEntity.setInvestmentId(variableIncomesEntity.getInvestmentId())
        variableIncomesTransactionsEntity.setInvestment(variableIncomesEntity)
        variableIncomesTransactionsEntity.setTransactionType("APLICACAO")
        variableIncomesTransactionsEntity.setType("ENTRADA")
        variableIncomesTransactionsEntity.setTransactionDate(LocalDate.now())
        variableIncomesTransactionsEntity.setTransactionQuantity(42.25)
        variableIncomesTransactionsEntity.setBrokerNoteId(brokerNoteId)

        return variableIncomesTransactionsEntity
    }

    static ExchangesOperationEntity aExchangesOperationEntity() {
        ExchangesOperationEntity entity = new ExchangesOperationEntity()
        entity.setBrandName("Banco do Brasil S.A")
        entity.setCompanyCnpj("00000000000191")
        entity.setIntermediaryInstitutionCnpjNumber("00000000000192")
        entity.setIntermediaryInstitutionName("Banco do Brasil S.A")
        entity.setStatus("AVAILABLE")

        return entity
    }

    static ExchangesOperationEventEntity aExchangesOperationEventEntity(operationId) {
        ExchangesOperationEventEntity entity = new ExchangesOperationEventEntity()
        entity.setEventSequenceNumber("Test2")
        entity.setEventType(EnumExchangesEventType._1)
        entity.setDeliveryForeignCurrency(EnumExchangesDeliveryForeignCurrency.CONTA_DEPOSITO)
        entity.setOperationId(operationId)
        entity.setForeignPartieCountryCode("ZA")
        entity.setForeignPartieName("José da Silva")
        entity.setRelationshipCode("50")

        return entity
    }

}
