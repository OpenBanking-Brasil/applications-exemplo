package com.raidiam.trustframework.bank.controllers

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.raidiam.trustframework.bank.AuthHelper
import com.raidiam.trustframework.bank.TestJwtSigner
import com.raidiam.trustframework.bank.services.InvoiceFinancingService
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
class InvoiceFinancingControllerSpec extends Specification {

    private static Context lambdaContext = new MockLambdaContext()

    def mapper = new ObjectMapper()

    InvoiceFinancingService invoiceFinancingService = Mock(InvoiceFinancingService)

    MicronautLambdaContainerHandler handler

    ResponseInvoiceFinancingsContractList responseInvoiceFinancingContractsList
    ResponseInvoiceFinancingsContract responseInvoiceFinancingsContract
    ResponseInvoiceFinancingsPayments responseInvoiceFinancingsPayments
    ResponseInvoiceFinancingsInstalments responseInvoiceFinancingsInstalments
    ResponseInvoiceFinancingsWarranties responseInvoiceFinancingsWarranties

    @Inject
    BankLambdaUtils bankLambdaUtils

    @MockBean(BankLambdaUtils)
    BankLambdaUtils bankLambdaUtils() {
        Mock(BankLambdaUtils)
    }

    def setup() {
        mapper.findAndRegisterModules()
        handler = new MicronautLambdaContainerHandler(Micronaut.build().singletons(invoiceFinancingService, bankLambdaUtils, TestJwtSigner.JWT_SIGNER))

        InvoiceFinancingsContractInterestRate invoiceFinancingsContractInterestRate = new InvoiceFinancingsContractInterestRate()

        invoiceFinancingsContractInterestRate.taxType(InvoiceFinancingsContractInterestRate.TaxTypeEnum.NOMINAL)
        invoiceFinancingsContractInterestRate.interestRateType(EnumContractInterestRateType1.SIMPLES)
        invoiceFinancingsContractInterestRate.taxPeriodicity(EnumContractTaxPeriodicity1.AM)
        invoiceFinancingsContractInterestRate.calculation(EnumContractCalculation1._21_252)
        invoiceFinancingsContractInterestRate.referentialRateIndexerType(EnumContractReferentialRateIndexerType1.SEM_TIPO_INDEXADOR)
        invoiceFinancingsContractInterestRate.referentialRateIndexerSubType(EnumContractReferentialRateIndexerSubType1.SEM_SUB_TIPO_INDEXADOR)
        invoiceFinancingsContractInterestRate.referentialRateIndexerAdditionalInfo("it's true")
        invoiceFinancingsContractInterestRate.preFixedRate(0.3)
        invoiceFinancingsContractInterestRate.postFixedRate(0.5)
        invoiceFinancingsContractInterestRate.additionalInfo("good")

        InvoiceFinancingsContractedFee invoiceFinancingsContractedFee = new InvoiceFinancingsContractedFee()

        invoiceFinancingsContractedFee.feeName("Henry")
        invoiceFinancingsContractedFee.feeCode("237db2d239d29d234jr-23d32")
        invoiceFinancingsContractedFee.feeChargeType(EnumContractFeeChargeType1.UNICA)
        invoiceFinancingsContractedFee.feeCharge(EnumContractFeeCharge1.MAXIMO)
        invoiceFinancingsContractedFee.feeAmount(9.99)
        invoiceFinancingsContractedFee.feeRate(0.0150)

        InvoiceFinancingsFinanceCharge invoiceFinancingsFinanceCharge = new InvoiceFinancingsFinanceCharge()

        invoiceFinancingsFinanceCharge.chargeType(EnumContractFinanceChargeType1.IOF_POR_ATRASO)
        invoiceFinancingsFinanceCharge.chargeAdditionalInfo("this.chargeAdditionalInfo")
        invoiceFinancingsFinanceCharge.chargeRate(19.99)

        InvoiceFinancingsContract invoiceFinancingsContract = new InvoiceFinancingsContract()

        invoiceFinancingsContract.contractNumber("237db2d239d29d234jr-23d32")
        invoiceFinancingsContract.ipocCode("341234nedfywefdy")
        invoiceFinancingsContract.productName("Bank of Nook")
        invoiceFinancingsContract.productType(EnumContractProductTypeInvoiceFinancings.DESCONTADOS)
        invoiceFinancingsContract.productSubType(EnumContractProductSubTypeInvoiceFinancings.DESCONTO_CHEQUES)
        invoiceFinancingsContract.contractDate(LocalDate.parse("2022-01-20"))
        invoiceFinancingsContract.disbursementDate(LocalDate.parse("2022-01-20"))
        invoiceFinancingsContract.settlementDate(LocalDate.parse("2022-01-20"))
        invoiceFinancingsContract.contractAmount(354.56)
        invoiceFinancingsContract.currency("Bells")
        invoiceFinancingsContract.dueDate(LocalDate.parse("2022-01-20"))
        invoiceFinancingsContract.instalmentPeriodicity(EnumContractInstalmentPeriodicity1.SEM_PERIODICIDADE_REGULAR)
        invoiceFinancingsContract.instalmentPeriodicityAdditionalInfo("yep")
        invoiceFinancingsContract.firstInstalmentDueDate(LocalDate.parse("2022-01-20"))
        invoiceFinancingsContract.CET(20.22)
        invoiceFinancingsContract.amortizationScheduled(EnumContractAmortizationScheduled1.SAC)
        invoiceFinancingsContract.amortizationScheduledAdditionalInfo("indeed")
        invoiceFinancingsContract.addInterestRatesItem(invoiceFinancingsContractInterestRate)
        invoiceFinancingsContract.addContractedFeesItem(invoiceFinancingsContractedFee)
        invoiceFinancingsContract.addContractedFinanceChargesItem(invoiceFinancingsFinanceCharge)

        InvoiceFinancingsContractData listContract = new InvoiceFinancingsContractData()
        listContract.contractId("237db2d239d29d234jr-23d32")
        listContract.brandName("Bank of Nook")
        listContract.companyCnpj("4-2332rfbd-e")
        listContract.productType(EnumContractProductTypeInvoiceFinancings.DESCONTADOS)
        listContract.productSubType(EnumContractProductSubTypeInvoiceFinancings.DESCONTO_CHEQUES)
        listContract.ipocCode("341234nedfywefdy")

        InvoiceFinancingsFeeOverParcel invoiceFinancingsFeeOverParcel = new InvoiceFinancingsFeeOverParcel()

        invoiceFinancingsFeeOverParcel.feeName("Steven")
        invoiceFinancingsFeeOverParcel.feeCode("s4ewrv3d823ed-re2d")
        invoiceFinancingsFeeOverParcel.feeAmount(30.99)

        InvoiceFinancingsChargeOverParcel invoiceFinancingsChargeOverParcel = new InvoiceFinancingsChargeOverParcel()

        invoiceFinancingsChargeOverParcel.setChargeType(InvoiceFinancingsChargeOverParcel.ChargeTypeEnum.OUTROS)
        invoiceFinancingsChargeOverParcel.setChargeAdditionalInfo("Money")
        invoiceFinancingsChargeOverParcel.setChargeAmount(19.23)

        InvoiceFinancingsReleasesOverParcel invoiceFinancingsOverParcel = new InvoiceFinancingsReleasesOverParcel()
        invoiceFinancingsOverParcel.addFeesItem(invoiceFinancingsFeeOverParcel)
        invoiceFinancingsOverParcel.addChargesItem(invoiceFinancingsChargeOverParcel)

        InvoiceFinancingsReleases invoiceFinancingsReleases = new InvoiceFinancingsReleases()

        invoiceFinancingsReleases.paymentId("s4ewrv3d823ed-re2d")
        invoiceFinancingsReleases.isOverParcelPayment(true)
        invoiceFinancingsReleases.instalmentId("e23dh72c823c-2d2323d4")
        invoiceFinancingsReleases.paidDate(LocalDate.parse("2022-01-20"))
        invoiceFinancingsReleases.currency("BRL")
        invoiceFinancingsReleases.paidAmount(20.2222)
        invoiceFinancingsReleases.overParcel(invoiceFinancingsOverParcel)

        InvoiceFinancingsPayments invoiceFinancingsPayments = new InvoiceFinancingsPayments()

        invoiceFinancingsPayments.paidInstalments(99.999999)
        invoiceFinancingsPayments.contractOutstandingBalance(4.99)
        invoiceFinancingsPayments.addReleasesItem(invoiceFinancingsReleases)


        InvoiceFinancingsContractedWarranty invoiceFinancingsWarranty = new InvoiceFinancingsContractedWarranty()

        invoiceFinancingsWarranty.currency("Bells")
        invoiceFinancingsWarranty.warrantyType(EnumWarrantyType2.BENS_ARRENDADOS)
        invoiceFinancingsWarranty.warrantySubType(EnumWarrantySubType1.CIVIL)
        invoiceFinancingsWarranty.warrantyAmount(99.99)

        InvoiceFinancingsBalloonPayment invoiceFinancingsBalloonPayment = new InvoiceFinancingsBalloonPayment()

        invoiceFinancingsBalloonPayment.dueDate(LocalDate.parse("2022-01-20"))
        invoiceFinancingsBalloonPayment.currency("BRL")
        invoiceFinancingsBalloonPayment.amount(14.44)

        InvoiceFinancingsInstalments invoiceFinancingsInstalments = new InvoiceFinancingsInstalments()

        invoiceFinancingsInstalments.typeNumberOfInstalments(InvoiceFinancingsInstalments.TypeNumberOfInstalmentsEnum.DIA)
        invoiceFinancingsInstalments.totalNumberOfInstalments(44.444)
        invoiceFinancingsInstalments.typeContractRemaining(InvoiceFinancingsInstalments.TypeContractRemainingEnum.DIA)
        invoiceFinancingsInstalments.contractRemainingNumber(44.44444)
        invoiceFinancingsInstalments.paidInstalments(44.4444)
        invoiceFinancingsInstalments.dueInstalments(4.44444)
        invoiceFinancingsInstalments.pastDueInstalments(444.44444)
        invoiceFinancingsInstalments.addBalloonPaymentsItem(invoiceFinancingsBalloonPayment)

        responseInvoiceFinancingContractsList = new ResponseInvoiceFinancingsContractList().data(List.of(listContract)).meta(new Meta().totalPages(1))
        responseInvoiceFinancingsContract = new ResponseInvoiceFinancingsContract().data(invoiceFinancingsContract)
        responseInvoiceFinancingsPayments = new ResponseInvoiceFinancingsPayments().data(invoiceFinancingsPayments)
        responseInvoiceFinancingsInstalments = new ResponseInvoiceFinancingsInstalments().data(invoiceFinancingsInstalments)
        responseInvoiceFinancingsWarranties = new ResponseInvoiceFinancingsWarranties().data(List.of(invoiceFinancingsWarranty)).meta(new Meta().totalPages(1))

        bankLambdaUtils.getConsentIdFromRequest(_ as HttpRequest<?>) >> "12345"
    }

    def cleanup() {
        handler.close()
    }

    void "We can get all contracts"() {
        given:

        invoiceFinancingService.getInvoiceFinancingContractList(_ as Pageable, _ as String) >> responseInvoiceFinancingContractsList

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/invoice-financings/v1/contracts', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "invoice-financings", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get a contract"() {
        given:
        invoiceFinancingService.getInvoiceFinancingContract(_ as String, _ as UUID) >> responseInvoiceFinancingsContract

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/invoice-financings/v1/contracts/${UUID.randomUUID()}", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "invoice-financings", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can't get a contract which doesn't exist"() {
        given:
        invoiceFinancingService.getInvoiceFinancingContract(_ as String, _ as UUID) >> { throw new HttpStatusException(HttpStatus.BAD_REQUEST, "") }

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/invoice-financings/v1/contracts/${UUID.randomUUID()}", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "invoice-financings", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code
    }

    void "We can get a Payment"() {
        given:
        invoiceFinancingService.getInvoiceFinancingPayments(_ as String, _ as UUID) >> responseInvoiceFinancingsPayments

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/invoice-financings/v1/contracts/${UUID.randomUUID()}/payments", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "invoice-financings", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get scheduled instalments"() {
        given:
        invoiceFinancingService.getInvoiceFinancingScheduledInstalments(_ as String, _ as UUID) >> responseInvoiceFinancingsInstalments

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/invoice-financings/v1/contracts/${UUID.randomUUID()}/scheduled-instalments", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "invoice-financings", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get warranties"() {
        given:
        invoiceFinancingService.getInvoiceFinancingWarranties(_ as Pageable, _ as String, _ as UUID) >> responseInvoiceFinancingsWarranties

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/invoice-financings/v1/contracts/${UUID.randomUUID()}/warranties", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "invoice-financings", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }
}