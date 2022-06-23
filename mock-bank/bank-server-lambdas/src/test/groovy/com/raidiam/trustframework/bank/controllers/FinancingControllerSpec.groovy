package com.raidiam.trustframework.bank.controllers

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.raidiam.trustframework.bank.AuthHelper
import com.raidiam.trustframework.bank.TestJwtSigner
import com.raidiam.trustframework.bank.services.FinancingService
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
class FinancingControllerSpec extends Specification {

    private static Context lambdaContext = new MockLambdaContext()

    def mapper = new ObjectMapper()

    FinancingService financingService = Mock(FinancingService)

    MicronautLambdaContainerHandler handler

    ResponseFinancingsContractList responseFinancingsContractList
    ResponseFinancingsContract responseFinancingsContract
    ResponseFinancingsPayments responseFinancingsPayments
    ResponseFinancingsInstalments responseFinancingsInstalments
    ResponseFinancingsWarranties responseFinancingsWarranties

    @Inject
    BankLambdaUtils bankLambdaUtils

    @MockBean(BankLambdaUtils)
    BankLambdaUtils bankLambdaUtils() {
        Mock(BankLambdaUtils)
    }

    def setup() {
        mapper.findAndRegisterModules()
        handler = new MicronautLambdaContainerHandler(Micronaut.build().singletons(financingService, bankLambdaUtils, TestJwtSigner.JWT_SIGNER))

        FinancingsContractInterestRate financingsContractInterestRate = new FinancingsContractInterestRate()

        financingsContractInterestRate.taxType(FinancingsContractInterestRate.TaxTypeEnum.NOMINAL)
        financingsContractInterestRate.interestRateType(FinancingsContractInterestRate.InterestRateTypeEnum.SIMPLES)
        financingsContractInterestRate.taxPeriodicity(FinancingsContractInterestRate.TaxPeriodicityEnum.AM)
        financingsContractInterestRate.calculation(FinancingsContractInterestRate.CalculationEnum._21_252)
        financingsContractInterestRate.referentialRateIndexerType(FinancingsContractInterestRate.ReferentialRateIndexerTypeEnum.SEM_TIPO_INDEXADOR)
        financingsContractInterestRate.referentialRateIndexerSubType(FinancingsContractInterestRate.ReferentialRateIndexerSubTypeEnum.SEM_SUB_TIPO_INDEXADOR)
        financingsContractInterestRate.referentialRateIndexerAdditionalInfo("it's true")
        financingsContractInterestRate.preFixedRate(0.3)
        financingsContractInterestRate.postFixedRate(0.5)
        financingsContractInterestRate.additionalInfo("good")

        FinancingsContractFee financingsContractFee = new FinancingsContractFee()

        financingsContractFee.feeName("Henry")
        financingsContractFee.feeCode("237db2d239d29d234jr-23d32")
        financingsContractFee.feeChargeType(FinancingsContractFee.FeeChargeTypeEnum.UNICA)
        financingsContractFee.feeCharge(FinancingsContractFee.FeeChargeEnum.MAXIMO)
        financingsContractFee.feeAmount(9.99)
        financingsContractFee.feeRate(0.0150)

        FinancingsFinanceCharge financingsFinanceCharge = new FinancingsFinanceCharge()

        financingsFinanceCharge.chargeType(FinancingsFinanceCharge.ChargeTypeEnum.IOF_POR_ATRASO)
        financingsFinanceCharge.chargeAdditionalInfo("this.chargeAdditionalInfo")
        financingsFinanceCharge.chargeRate(19.99)

        FinancingsContract financingsContract = new FinancingsContract()

        financingsContract.contractNumber("237db2d239d29d234jr-23d32")
        financingsContract.ipocCode("341234nedfywefdy")
        financingsContract.productName("Bank of Nook")
        financingsContract.productType(EnumProductType.FINANCIAMENTOS)
        financingsContract.productSubType(EnumProductSubType.CUSTEIO)
        financingsContract.contractDate(LocalDate.parse("2022-01-20"))
        financingsContract.disbursementDate(LocalDate.parse("2022-01-20"))
        financingsContract.settlementDate(LocalDate.parse("2022-01-20"))
        financingsContract.contractAmount(354.56)
        financingsContract.currency("Bells")
        financingsContract.dueDate(LocalDate.parse("2022-01-20"))
        financingsContract.instalmentPeriodicity(FinancingsContract.InstalmentPeriodicityEnum.SEM_PERIODICIDADE_REGULAR)
        financingsContract.instalmentPeriodicityAdditionalInfo("yep")
        financingsContract.firstInstalmentDueDate(LocalDate.parse("2022-01-20"))
        financingsContract.CET(20.22)
        financingsContract.amortizationScheduled(FinancingsContract.AmortizationScheduledEnum.SAC)
        financingsContract.amortizationScheduledAdditionalInfo("indeed")
        financingsContract.addInterestRatesItem(financingsContractInterestRate)
        financingsContract.addContractedFeesItem(financingsContractFee)
        financingsContract.addContractedFinanceChargesItem(financingsFinanceCharge)

        FinancingsListContract listContract = new FinancingsListContract()
        listContract.contractId("237db2d239d29d234jr-23d32")
        listContract.brandName("Bank of Nook")
        listContract.companyCnpj("4-2332rfbd-e")
        listContract.productType(EnumProductType.FINANCIAMENTOS)
        listContract.productSubType(EnumProductSubType.CUSTEIO)
        listContract.ipocCode("341234nedfywefdy")

        FinancingsFeeOverParcel financingsFeeOverParcel = new FinancingsFeeOverParcel()

        financingsFeeOverParcel.feeName("Steven")
        financingsFeeOverParcel.feeCode("s4ewrv3d823ed-re2d")
        financingsFeeOverParcel.feeAmount(30.99)

        FinancingsChargeOverParcel financingsChargeOverParcel = new FinancingsChargeOverParcel()

        financingsChargeOverParcel.setChargeType(new FinancingsFinanceChargeType())
        financingsChargeOverParcel.setChargeAdditionalInfo("Money")
        financingsChargeOverParcel.setChargeAmount(19.23)

        FinancingsOverParcel financingsOverParcel = new FinancingsOverParcel()
        financingsOverParcel.addFeesItem(financingsFeeOverParcel)
        financingsOverParcel.addChargesItem(financingsChargeOverParcel)

        FinancingsReleases financingsReleases = new FinancingsReleases()

        financingsReleases.paymentId("s4ewrv3d823ed-re2d")
        financingsReleases.isOverParcelPayment(true)
        financingsReleases.instalmentId("e23dh72c823c-2d2323d4")
        financingsReleases.paidDate(LocalDate.parse("2022-01-20"))
        financingsReleases.currency("BRL")
        financingsReleases.paidAmount(20.2222)
        financingsReleases.overParcel(financingsOverParcel)

        FinancingsPayments financingsPayment = new FinancingsPayments()

        financingsPayment.paidInstalments(99.999999)
        financingsPayment.contractOutstandingBalance(4.99)
        financingsPayment.addReleasesItem(financingsReleases)


        FinancingsWarranties financingsWarranties = new FinancingsWarranties()

        financingsWarranties.currency("Bells")
        financingsWarranties.warrantyType(FinancingsWarranties.WarrantyTypeEnum.BENS_ARRENDADOS)
        financingsWarranties.warrantySubType(FinancingsWarranties.WarrantySubTypeEnum.CIVIL)
        financingsWarranties.warrantyAmount(99.99)

        FinancingsBalloonPayment financingsBalloonPayment = new FinancingsBalloonPayment()

        financingsBalloonPayment.dueDate(LocalDate.parse("2022-01-20"))
        financingsBalloonPayment.currency("BRL")
        financingsBalloonPayment.amount(14.44)

        FinancingsInstalments financingsInstalments = new FinancingsInstalments()

        financingsInstalments.typeNumberOfInstalments(FinancingsInstalments.TypeNumberOfInstalmentsEnum.DIA)
        financingsInstalments.totalNumberOfInstalments(44.444)
        financingsInstalments.typeContractRemaining(FinancingsInstalments.TypeContractRemainingEnum.DIA)
        financingsInstalments.contractRemainingNumber(44.44444)
        financingsInstalments.paidInstalments(44.4444)
        financingsInstalments.dueInstalments(4.44444)
        financingsInstalments.pastDueInstalments(444.44444)
        financingsInstalments.addBalloonPaymentsItem(financingsBalloonPayment)

        responseFinancingsContractList = new ResponseFinancingsContractList().data(List.of(listContract)).meta(new Meta().totalPages(1))
        responseFinancingsContract = new ResponseFinancingsContract().data(financingsContract)
        responseFinancingsPayments = new ResponseFinancingsPayments().data(financingsPayment)
        responseFinancingsInstalments = new ResponseFinancingsInstalments().data(financingsInstalments)
        responseFinancingsWarranties = new ResponseFinancingsWarranties().data(List.of(financingsWarranties)).meta(new Meta().totalPages(1))

        bankLambdaUtils.getConsentIdFromRequest(_ as HttpRequest<?>) >> "12345"
    }

    def cleanup() {
        handler.close()
    }

    void "We can get all finance contracts"() {
        given:

        financingService.getFinancingContractList(_ as Pageable, _ as String) >> responseFinancingsContractList

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/open-banking/financings/v1/contracts', HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "financings", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get a finance contract"() {
        given:
        financingService.getFinancingContract(_ as String, _ as UUID) >> responseFinancingsContract

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/financings/v1/contracts/${UUID.randomUUID()}", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "financings", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can't get a contract which doesn't exist"() {
        given:
        financingService.getFinancingContract(_ as String, _ as UUID) >> { throw new HttpStatusException(HttpStatus.BAD_REQUEST, "") }

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/financings/v1/contracts/${UUID.randomUUID()}", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "financings", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code
    }

    void "We can get a Payment"() {
        given:
        financingService.getFinancingPayments(_ as String, _ as UUID) >> responseFinancingsPayments

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/financings/v1/contracts/${UUID.randomUUID()}/payments", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "financings", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get scheduled instalments"() {
        given:
        financingService.getFinancingScheduledInstalments(_ as String, _ as UUID) >> responseFinancingsInstalments

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/financings/v1/contracts/${UUID.randomUUID()}/scheduled-instalments", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "financings", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }

    void "We can get warranties"() {
        given:
        financingService.getFinancingWarranties(_ as Pageable, _ as String, _ as UUID) >> responseFinancingsWarranties

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/financings/v1/contracts/${UUID.randomUUID()}/warranties", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "financings", builder)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }
}