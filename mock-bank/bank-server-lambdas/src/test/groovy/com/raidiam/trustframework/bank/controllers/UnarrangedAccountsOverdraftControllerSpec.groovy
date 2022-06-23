package com.raidiam.trustframework.bank.controllers

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.raidiam.trustframework.bank.AuthHelper
import com.raidiam.trustframework.bank.TestJwtSigner
import com.raidiam.trustframework.bank.services.UnarrangedAccountsOverdraftService
import com.raidiam.trustframework.bank.utils.BankLambdaUtils
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.data.model.Pageable
import io.micronaut.function.aws.proxy.MicronautLambdaContainerHandler
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.runtime.Micronaut
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject
import java.time.LocalDate

@MicronautTest(transactional = false)
class UnarrangedAccountsOverdraftControllerSpec extends Specification {

    private static Context lambdaContext = new MockLambdaContext()

    def mapper = new ObjectMapper()

    UnarrangedAccountsOverdraftService unarrangedAccountsOverdraftService = Mock(UnarrangedAccountsOverdraftService)

    MicronautLambdaContainerHandler handler

    ResponseUnarrangedAccountOverdraftContractList responseUnarrangedAccountOverdraftContractList
    ResponseUnarrangedAccountOverdraftContract responseUnarrangedAccountOverdraftContract
    ResponseUnarrangedAccountOverdraftPayments responseUnarrangedAccountOverdraftPayments
    ResponseUnarrangedAccountOverdraftInstalments responseUnarrangedAccountOverdraftInstalments
    ResponseUnarrangedAccountOverdraftWarranties responseUnarrangedAccountOverdraftWarranties

    @Inject
    BankLambdaUtils bankLambdaUtils

    @MockBean(BankLambdaUtils)
    BankLambdaUtils bankLambdaUtils() {
        Mock(BankLambdaUtils)
    }

    def setup() {
        mapper.findAndRegisterModules()
        handler = new MicronautLambdaContainerHandler(Micronaut.build().singletons(unarrangedAccountsOverdraftService, bankLambdaUtils, TestJwtSigner.JWT_SIGNER))

        UnarrangedAccountOverdraftContractInterestRate unarrangedAccountOverdraftInterestRate = new UnarrangedAccountOverdraftContractInterestRate()

        unarrangedAccountOverdraftInterestRate.taxType(EnumContractTaxType.NOMINAL)
        unarrangedAccountOverdraftInterestRate.interestRateType(EnumContractInterestRateType.SIMPLES)
        unarrangedAccountOverdraftInterestRate.taxPeriodicity(EnumContractTaxPeriodicity.AM)
        unarrangedAccountOverdraftInterestRate.calculation(UnarrangedAccountOverdraftContractInterestRate.CalculationEnum._21_252)
        unarrangedAccountOverdraftInterestRate.referentialRateIndexerType(EnumContractReferentialRateIndexerType.SEM_TIPO_INDEXADOR)
        unarrangedAccountOverdraftInterestRate.referentialRateIndexerSubType(EnumContractReferentialRateIndexerSubType.SEM_SUB_TIPO_INDEXADOR)
        unarrangedAccountOverdraftInterestRate.referentialRateIndexerAdditionalInfo("it's true")
        unarrangedAccountOverdraftInterestRate.preFixedRate(0.3)
        unarrangedAccountOverdraftInterestRate.postFixedRate(0.5)
        unarrangedAccountOverdraftInterestRate.additionalInfo("good")

        UnarrangedAccountOverdraftContractedFee unarrangedAccountOverdraftContractedFee = new UnarrangedAccountOverdraftContractedFee()

        unarrangedAccountOverdraftContractedFee.feeName("Henry")
        unarrangedAccountOverdraftContractedFee.feeCode("237db2d239d29d234jr-23d32")
        unarrangedAccountOverdraftContractedFee.feeChargeType(EnumContractFeeChargeType.UNICA)
        unarrangedAccountOverdraftContractedFee.feeCharge(EnumContractFeeCharge.MAXIMO)
        unarrangedAccountOverdraftContractedFee.feeAmount(9.99)
        unarrangedAccountOverdraftContractedFee.feeRate(0.0150)

        UnarrangedAccountOverdraftFinanceCharge unarrangedAccountOverdraftFinanceCharge = new UnarrangedAccountOverdraftFinanceCharge()

        unarrangedAccountOverdraftFinanceCharge.chargeType(ChargeType.IOF_POR_ATRASO)
        unarrangedAccountOverdraftFinanceCharge.chargeAdditionalInfo("this.chargeAdditionalInfo")
        unarrangedAccountOverdraftFinanceCharge.chargeRate(19.99)

        UnarrangedAccountOverdraftContractData unarrangedAccountOverdraftContract = new UnarrangedAccountOverdraftContractData()

        unarrangedAccountOverdraftContract.contractNumber("237db2d239d29d234jr-23d32")
        unarrangedAccountOverdraftContract.ipocCode("341234nedfywefdy")
        unarrangedAccountOverdraftContract.productName("Bank of Nook")
        unarrangedAccountOverdraftContract.productType(ProductType.DEPOSITANTES)
        unarrangedAccountOverdraftContract.productSubType(ProductSubType.DEPOSITANTES)
        unarrangedAccountOverdraftContract.contractDate(LocalDate.parse("2022-01-20"))
        unarrangedAccountOverdraftContract.disbursementDate(LocalDate.parse("2022-01-20"))
        unarrangedAccountOverdraftContract.settlementDate(LocalDate.parse("2022-01-20"))
        unarrangedAccountOverdraftContract.contractAmount(354.56)
        unarrangedAccountOverdraftContract.currency("Bells")
        unarrangedAccountOverdraftContract.dueDate(LocalDate.parse("2022-01-20"))
        unarrangedAccountOverdraftContract.instalmentPeriodicity(EnumContractInstalmentPeriodicity.SEM_PERIODICIDADE_REGULAR)
        unarrangedAccountOverdraftContract.instalmentPeriodicityAdditionalInfo("yep")
        unarrangedAccountOverdraftContract.firstInstalmentDueDate(LocalDate.parse("2022-01-20"))
        unarrangedAccountOverdraftContract.CET(20.22)
        unarrangedAccountOverdraftContract.amortizationScheduled(UnarrangedAccountOverdraftContractData.AmortizationScheduledEnum.SAC)
        unarrangedAccountOverdraftContract.amortizationScheduledAdditionalInfo("indeed")
        unarrangedAccountOverdraftContract.addInterestRatesItem(unarrangedAccountOverdraftInterestRate)
        unarrangedAccountOverdraftContract.addContractedFeesItem(unarrangedAccountOverdraftContractedFee)
        unarrangedAccountOverdraftContract.addContractedFinanceChargesItem(unarrangedAccountOverdraftFinanceCharge)

        UnarrangedAccountOverdraftContractListData listContract = new UnarrangedAccountOverdraftContractListData()
        listContract.contractId("237db2d239d29d234jr-23d32")
        listContract.brandName("Bank of Nook")
        listContract.companyCnpj("4-2332rfbd-e")
        listContract.productType(ProductType.DEPOSITANTES)
        listContract.productSubType(ProductSubType.DEPOSITANTES)
        listContract.ipocCode("341234nedfywefdy")

        UnarrangedAccountOverdraftFeeOverParcel unarrangedAccountOverdraftFeeOverParcel = new UnarrangedAccountOverdraftFeeOverParcel()

        unarrangedAccountOverdraftFeeOverParcel.feeName("Steven")
        unarrangedAccountOverdraftFeeOverParcel.feeCode("s4ewrv3d823ed-re2d")
        unarrangedAccountOverdraftFeeOverParcel.feeAmount(30.99)

        UnarrangedAccountOverdraftChargeOverParcel unarrangedAccountOverdraftChargeOverParcel = new UnarrangedAccountOverdraftChargeOverParcel()

        unarrangedAccountOverdraftChargeOverParcel.setChargeType(ChargeType.OUTROS)
        unarrangedAccountOverdraftChargeOverParcel.setChargeAdditionalInfo("Money")
        unarrangedAccountOverdraftChargeOverParcel.setChargeAmount(19.23)

        UnarrangedAccountOverdraftReleasesOverParcel unarrangedAccountOverdraftReleasesOverParcel = new UnarrangedAccountOverdraftReleasesOverParcel()
        unarrangedAccountOverdraftReleasesOverParcel.addFeesItem(unarrangedAccountOverdraftFeeOverParcel)
        unarrangedAccountOverdraftReleasesOverParcel.addChargesItem(unarrangedAccountOverdraftChargeOverParcel)

        UnarrangedAccountOverdraftReleases unarrangedAccountOverdraftReleases = new UnarrangedAccountOverdraftReleases()

        unarrangedAccountOverdraftReleases.paymentId("s4ewrv3d823ed-re2d")
        unarrangedAccountOverdraftReleases.isOverParcelPayment(true)
        unarrangedAccountOverdraftReleases.instalmentId("e23dh72c823c-2d2323d4")
        unarrangedAccountOverdraftReleases.paidDate(LocalDate.parse("2022-01-20"))
        unarrangedAccountOverdraftReleases.currency("BRL")
        unarrangedAccountOverdraftReleases.paidAmount(20.2222)
        unarrangedAccountOverdraftReleases.overParcel(unarrangedAccountOverdraftReleasesOverParcel)

        UnarrangedAccountOverdraftPaymentsData unarrangedAccountOverdraftPayments = new UnarrangedAccountOverdraftPaymentsData()

        unarrangedAccountOverdraftPayments.paidInstalments(99.999999)
        unarrangedAccountOverdraftPayments.contractOutstandingBalance(4.99)
        unarrangedAccountOverdraftPayments.addReleasesItem(unarrangedAccountOverdraftReleases)


        UnarrangedAccountsOverdraftContractedWarranty unarrangedAccountsOverdraftWarranty = new UnarrangedAccountsOverdraftContractedWarranty()

        unarrangedAccountsOverdraftWarranty.currency("Bells")
        unarrangedAccountsOverdraftWarranty.warrantyType(EnumWarrantyType.BENS_ARRENDADOS)
        unarrangedAccountsOverdraftWarranty.warrantySubType(EnumWarrantySubType.CIVIL)
        unarrangedAccountsOverdraftWarranty.warrantyAmount(99.99)

        UnarrangedAccountOverdraftBalloonPayment unarrangedAccountOverdraftBalloonPayment = new UnarrangedAccountOverdraftBalloonPayment()

        unarrangedAccountOverdraftBalloonPayment.dueDate(LocalDate.parse("2022-01-20"))
        unarrangedAccountOverdraftBalloonPayment.currency("BRL")
        unarrangedAccountOverdraftBalloonPayment.amount(14.44)

        UnarrangedAccountOverdraftInstalmentsData unarrangedAccountOverdraftInstalments = new UnarrangedAccountOverdraftInstalmentsData()

        unarrangedAccountOverdraftInstalments.typeNumberOfInstalments(EnumTypeNumberOfInstalments.DIA)
        unarrangedAccountOverdraftInstalments.totalNumberOfInstalments(44.444)
        unarrangedAccountOverdraftInstalments.typeContractRemaining(UnarrangedAccountOverdraftInstalmentsData.TypeContractRemainingEnum.DIA)
        unarrangedAccountOverdraftInstalments.contractRemainingNumber(44.44444)
        unarrangedAccountOverdraftInstalments.paidInstalments(44.4444)
        unarrangedAccountOverdraftInstalments.dueInstalments(4.44444)
        unarrangedAccountOverdraftInstalments.pastDueInstalments(444.44444)
        unarrangedAccountOverdraftInstalments.addBalloonPaymentsItem(unarrangedAccountOverdraftBalloonPayment)

        responseUnarrangedAccountOverdraftContractList = new ResponseUnarrangedAccountOverdraftContractList().data(List.of(listContract)).meta(new Meta().totalPages(1))
        responseUnarrangedAccountOverdraftContract = new ResponseUnarrangedAccountOverdraftContract().data(unarrangedAccountOverdraftContract)
        responseUnarrangedAccountOverdraftPayments = new ResponseUnarrangedAccountOverdraftPayments().data(unarrangedAccountOverdraftPayments)
        responseUnarrangedAccountOverdraftInstalments = new ResponseUnarrangedAccountOverdraftInstalments().data(unarrangedAccountOverdraftInstalments)
        responseUnarrangedAccountOverdraftWarranties = new ResponseUnarrangedAccountOverdraftWarranties().data(List.of(unarrangedAccountsOverdraftWarranty)).meta(new Meta().totalPages(1))

        bankLambdaUtils.getConsentIdFromRequest(_ as HttpRequest<?>) >> "12345"
    }

    def cleanup() {
        handler.close()
    }

    void "We can get all contracts"() {
        given:

        unarrangedAccountsOverdraftService.getUnarrangedOverdraftContractList(_ as Pageable, _ as String) >> responseUnarrangedAccountOverdraftContractList

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/unarranged-accounts-overdraft/v1/contracts', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "unarranged-accounts-overdraft", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get a contract"() {
        given:
        unarrangedAccountsOverdraftService.getUnarrangedOverdraftContract(_ as String, _ as UUID) >> responseUnarrangedAccountOverdraftContract

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/unarranged-accounts-overdraft/v1/contracts/' + UUID.randomUUID(), HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "unarranged-accounts-overdraft", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can't get a contract which doesn't exist"() {
        given:
        unarrangedAccountsOverdraftService.getUnarrangedOverdraftContract(_ as String, _ as UUID) >> { throw new HttpStatusException(HttpStatus.BAD_REQUEST, "") }

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/unarranged-accounts-overdraft/v1/contracts/${UUID.randomUUID()}", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "unarranged-accounts-overdraft", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code
    }

    void "We can get a Payment"() {
        given:
        unarrangedAccountsOverdraftService.getUnarrangedOverdraftPayments(_ as String, _ as UUID) >> responseUnarrangedAccountOverdraftPayments

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/unarranged-accounts-overdraft/v1/contracts/' + UUID.randomUUID() + "/payments", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "unarranged-accounts-overdraft", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get scheduled instalments"() {
        given:
        unarrangedAccountsOverdraftService.getUnarrangedOverdraftScheduledInstalments(_ as String, _ as UUID) >> responseUnarrangedAccountOverdraftInstalments

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/unarranged-accounts-overdraft/v1/contracts/' + UUID.randomUUID() + "/scheduled-instalments", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "unarranged-accounts-overdraft", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get warranties"() {
        given:
        unarrangedAccountsOverdraftService.getUnarrangedOverdraftWarranties(_ as Pageable, _ as String, _ as UUID) >> responseUnarrangedAccountOverdraftWarranties

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/unarranged-accounts-overdraft/v1/contracts/' + UUID.randomUUID() + "/warranties", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "unarranged-accounts-overdraft", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }
}