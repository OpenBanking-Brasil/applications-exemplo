package com.raidiam.trustframework.bank.controllers

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.raidiam.trustframework.bank.AuthHelper
import com.raidiam.trustframework.bank.TestJwtSigner
import com.raidiam.trustframework.bank.services.LoansService
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
class LoansControllerSpec extends Specification {

    private static Context lambdaContext = new MockLambdaContext()

    def mapper = new ObjectMapper()

    LoansService loansService = Mock(LoansService)

    MicronautLambdaContainerHandler handler

    ResponseLoansContractList responseLoansContractList
    ResponseLoansContract responseLoansContract
    ResponseLoansPayments responseLoansPayments
    ResponseLoansInstalments responseLoansInstalments
    ResponseLoansWarranties responseLoansWarranties

    @Inject
    BankLambdaUtils bankLambdaUtils

    @MockBean(BankLambdaUtils)
    BankLambdaUtils bankLambdaUtils() {
        Mock(BankLambdaUtils)
    }

    def setup() {
        mapper.findAndRegisterModules()
        handler = new MicronautLambdaContainerHandler(Micronaut.build().singletons(loansService, bankLambdaUtils, TestJwtSigner.JWT_SIGNER))

        LoansContractInterestRate loansContractInterestRate = new LoansContractInterestRate()

        loansContractInterestRate.taxType(EnumContractTaxType.NOMINAL)
        loansContractInterestRate.interestRateType(EnumContractInterestRateType.SIMPLES)
        loansContractInterestRate.taxPeriodicity(EnumContractTaxPeriodicity.AM)
        loansContractInterestRate.calculation(EnumContractCalculation._21_252)
        loansContractInterestRate.referentialRateIndexerType(EnumContractReferentialRateIndexerType.SEM_TIPO_INDEXADOR)
        loansContractInterestRate.referentialRateIndexerSubType(EnumContractReferentialRateIndexerSubType.SEM_SUB_TIPO_INDEXADOR)
        loansContractInterestRate.referentialRateIndexerAdditionalInfo("it's true")
        loansContractInterestRate.preFixedRate(0.3)
        loansContractInterestRate.postFixedRate(0.5)
        loansContractInterestRate.additionalInfo("good")

        LoansContractedFee loansContractedFee = new LoansContractedFee()

        loansContractedFee.feeName("Henry")
        loansContractedFee.feeCode("237db2d239d29d234jr-23d32")
        loansContractedFee.feeChargeType(EnumContractFeeChargeType.UNICA)
        loansContractedFee.feeCharge(EnumContractFeeCharge.MAXIMO)
        loansContractedFee.feeAmount(9.99)
        loansContractedFee.feeRate(0.0150)

        LoansFinanceCharge loansFinanceCharge = new LoansFinanceCharge()

        loansFinanceCharge.chargeType(EnumContractFinanceChargeType.IOF_CONTRATACAO)
        loansFinanceCharge.chargeAdditionalInfo("this.chargeAdditionalInfo")
        loansFinanceCharge.chargeRate(19.99)

        LoansContract loansContract = new LoansContract()

        loansContract.contractNumber("237db2d239d29d234jr-23d32")
        loansContract.ipocCode("341234nedfywefdy")
        loansContract.productName("Bank of Nook")
        loansContract.productType(EnumContractProductTypeLoans.EMPRESTIMOS)
        loansContract.productSubType(EnumContractProductSubTypeLoans.CONTA_GARANTIDA)
        loansContract.contractDate(LocalDate.parse("2022-01-20"))
        loansContract.disbursementDate(LocalDate.parse("2022-01-20"))
        loansContract.settlementDate(LocalDate.parse("2022-01-20"))
        loansContract.contractAmount(354.56)
        loansContract.currency("Bells")
        loansContract.dueDate(LocalDate.parse("2022-01-20"))
        loansContract.instalmentPeriodicity(EnumContractInstalmentPeriodicity.SEM_PERIODICIDADE_REGULAR)
        loansContract.instalmentPeriodicityAdditionalInfo("yep")
        loansContract.firstInstalmentDueDate(LocalDate.parse("2022-01-20"))
        loansContract.CET(20.22)
        loansContract.amortizationScheduled(EnumContractAmortizationScheduled.SAC)
        loansContract.amortizationScheduledAdditionalInfo("indeed")
        loansContract.cnpjConsignee("4-2332rfbd-e")
        loansContract.addInterestRatesItem(loansContractInterestRate)
        loansContract.addContractedFeesItem(loansContractedFee)
        loansContract.addContractedFinanceChargesItem(loansFinanceCharge)

        LoansListContract listContract = new LoansListContract()
        listContract.contractId("237db2d239d29d234jr-23d32")
        listContract.brandName("Bank of Nook")
        listContract.companyCnpj("4-2332rfbd-e")
        listContract.productType(EnumContractProductTypeLoans.EMPRESTIMOS)
        listContract.productSubType(EnumContractProductSubTypeLoans.CONTA_GARANTIDA)
        listContract.ipocCode("341234nedfywefdy")

        LoansFeeOverParcel loansFeeOverParcel = new LoansFeeOverParcel()

        loansFeeOverParcel.feeName("Steven")
        loansFeeOverParcel.feeCode("s4ewrv3d823ed-re2d")
        loansFeeOverParcel.feeAmount(30.99)

        LoansChargeOverParcel loansChargeOverParcel = new LoansChargeOverParcel()

        loansChargeOverParcel.setChargeType(EnumContractFinanceChargeType.JUROS_MORA_ATRASO)
        loansChargeOverParcel.setChargeAdditionalInfo("Money")
        loansChargeOverParcel.setChargeAmount(19.23)

        LoansReleasesOverParcel loansReleasesOverParcel = new LoansReleasesOverParcel()
        loansReleasesOverParcel.addFeesItem(loansFeeOverParcel)
        loansReleasesOverParcel.addChargesItem(loansChargeOverParcel)

        LoansReleases loansReleases = new LoansReleases()

        loansReleases.paymentId("s4ewrv3d823ed-re2d")
        loansReleases.isOverParcelPayment(true)
        loansReleases.instalmentId("e23dh72c823c-2d2323d4")
        loansReleases.paidDate(LocalDate.parse("2022-01-20"))
        loansReleases.currency("BRL")
        loansReleases.paidAmount(20.2222)
        loansReleases.overParcel(loansReleasesOverParcel)

        LoansPayments loansPayment = new LoansPayments()

        loansPayment.paidInstalments(99.999999)
        loansPayment.contractOutstandingBalance(4.99)
        loansPayment.addReleasesItem(loansReleases)


        LoansWarranties loansWarranties = new LoansWarranties()

        loansWarranties.currency("Bells")
        loansWarranties.warrantyType(EnumWarrantyType.BENS_ARRENDADOS)
        loansWarranties.warrantySubType(EnumWarrantySubType.CIVIL)
        loansWarranties.warrantyAmount(99.99)

        LoansBalloonPayment loansBalloonPayment = new LoansBalloonPayment()

        loansBalloonPayment.dueDate(LocalDate.parse("2022-01-20"))
        loansBalloonPayment.currency("BRL")
        loansBalloonPayment.amount(14.44)

        LoansInstalments loansInstalments = new LoansInstalments()

        loansInstalments.typeNumberOfInstalments(LoansInstalments.TypeNumberOfInstalmentsEnum.DIA)
        loansInstalments.totalNumberOfInstalments(44.444)
        loansInstalments.typeContractRemaining(LoansInstalments.TypeContractRemainingEnum.DIA)
        loansInstalments.contractRemainingNumber(44.44444)
        loansInstalments.paidInstalments(44.4444)
        loansInstalments.dueInstalments(4.44444)
        loansInstalments.pastDueInstalments(444.44444)
        loansInstalments.addBalloonPaymentsItem(loansBalloonPayment)

        responseLoansContractList = new ResponseLoansContractList().data(List.of(listContract)).meta(new Meta().totalPages(1))
        responseLoansContract = new ResponseLoansContract().data(loansContract)
        responseLoansPayments = new ResponseLoansPayments().data(loansPayment)
        responseLoansInstalments = new ResponseLoansInstalments().data(loansInstalments)
        responseLoansWarranties = new ResponseLoansWarranties().data(List.of(loansWarranties)).meta(new Meta().totalPages(1))

        bankLambdaUtils.getConsentIdFromRequest(_ as HttpRequest<?>) >> "12345"
    }

    def cleanup() {
        handler.close()
    }

    void "we can get all loans"() {
        given:

        loansService.getLoansContractList(_ as Pageable, _ as String) >> responseLoansContractList

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/loans/v1/contracts', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "loans", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get a Loan"() {
        given:
        loansService.getLoanContract(_ as String, _ as UUID) >> responseLoansContract

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/loans/v1/contracts/${UUID.randomUUID()}", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "loans", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can't get a contract which doesn't exist"() {
        given:
        loansService.getLoanContract(_ as String, _ as UUID) >> { throw new HttpStatusException(HttpStatus.BAD_REQUEST, "") }

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/loans/v1/contracts/${UUID.randomUUID()}", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "loans", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code
    }

    void "We can get a Payment"() {
        given:
        loansService.getLoanPayments(_ as String, _ as UUID) >> responseLoansPayments

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/loans/v1/contracts/${UUID.randomUUID()}/payments", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "loans", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get scheduled instalments"() {
        given:
        loansService.getLoanScheduledInstalments(_ as String, _ as UUID) >> responseLoansInstalments

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/loans/v1/contracts/${UUID.randomUUID()}/scheduled-instalments", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "loans", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get warranties"() {
        given:
        loansService.getLoanWarranties(_ as Pageable, _ as String, _ as UUID) >> responseLoansWarranties

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/loans/v1/contracts/${UUID.randomUUID()}/warranties", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "loans", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }
}