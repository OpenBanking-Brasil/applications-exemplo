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
class FinancingV2ControllerSpec extends Specification {

    private static Context lambdaContext = new MockLambdaContext()

    def mapper = new ObjectMapper()

    FinancingService financingService = Mock(FinancingService)

    MicronautLambdaContainerHandler handler

    ResponseFinancingsContractList responseFinancingsContractList
    ResponseFinancingsContractV2 responseFinancingsContract
    ResponseFinancingsPaymentsV2 responseFinancingsPayments
    ResponseFinancingsInstalmentsV2 responseFinancingsInstalments
    ResponseFinancingsWarrantiesV2 responseFinancingsWarranties

    @Inject
    BankLambdaUtils bankLambdaUtils

    @MockBean(BankLambdaUtils)
    BankLambdaUtils bankLambdaUtils() {
        Mock(BankLambdaUtils)
    }

    def setup() {
        mapper.findAndRegisterModules()
        handler = new MicronautLambdaContainerHandler(Micronaut.build().singletons(financingService, bankLambdaUtils, TestJwtSigner.JWT_SIGNER))

        FinancingsContractInterestRateV2 financingsContractInterestRate = new FinancingsContractInterestRateV2()

        financingsContractInterestRate.taxType(FinancingsContractInterestRateV2.TaxTypeEnum.NOMINAL)
        financingsContractInterestRate.interestRateType(FinancingsContractInterestRateV2.InterestRateTypeEnum.SIMPLES)
        financingsContractInterestRate.taxPeriodicity(FinancingsContractInterestRateV2.TaxPeriodicityEnum.AM)
        financingsContractInterestRate.calculation(FinancingsContractInterestRateV2.CalculationEnum._21_252)
        financingsContractInterestRate.referentialRateIndexerType(FinancingsContractInterestRateV2.ReferentialRateIndexerTypeEnum.SEM_TIPO_INDEXADOR)
        financingsContractInterestRate.referentialRateIndexerSubType(FinancingsContractInterestRateV2.ReferentialRateIndexerSubTypeEnum.SEM_SUB_TIPO_INDEXADOR)
        financingsContractInterestRate.referentialRateIndexerAdditionalInfo("it's true")
        financingsContractInterestRate.preFixedRate("0.3")
        financingsContractInterestRate.postFixedRate("0.5")
        financingsContractInterestRate.additionalInfo("good")

        FinancingsContractFeeV2 financingsContractFee = new FinancingsContractFeeV2()

        financingsContractFee.feeName("Henry")
        financingsContractFee.feeCode("237db2d239d29d234jr-23d32")
        financingsContractFee.feeChargeType(FinancingsContractFeeV2.FeeChargeTypeEnum.UNICA)
        financingsContractFee.feeCharge(FinancingsContractFeeV2.FeeChargeEnum.MAXIMO)
        financingsContractFee.feeAmount("9.99")
        financingsContractFee.feeRate("0.0150")

        FinancingsFinanceChargeV2 financingsFinanceCharge = new FinancingsFinanceChargeV2()

        financingsFinanceCharge.chargeType(EnumContractFinanceChargeTypeV2.IOF_POR_ATRASO)
        financingsFinanceCharge.chargeAdditionalInfo("this.chargeAdditionalInfo")
        financingsFinanceCharge.chargeRate("19.99")

        FinancingsContractV2 financingsContract = new FinancingsContractV2()

        financingsContract.contractNumber("237db2d239d29d234jr-23d32")
        financingsContract.ipocCode("341234nedfywefdy")
        financingsContract.productName("Bank of Nook")
        financingsContract.productType(EnumProductTypeV2.FINANCIAMENTOS)
        financingsContract.productSubType(EnumProductSubTypeV2.CUSTEIO)
        financingsContract.contractDate(LocalDate.parse("2022-01-20"))
        financingsContract.settlementDate(LocalDate.parse("2022-01-20"))
        financingsContract.contractAmount("354.56")
        financingsContract.currency("Bells")
        financingsContract.dueDate(LocalDate.parse("2022-01-20"))
        financingsContract.instalmentPeriodicity(FinancingsContractV2.InstalmentPeriodicityEnum.SEM_PERIODICIDADE_REGULAR)
        financingsContract.instalmentPeriodicityAdditionalInfo("yep")
        financingsContract.firstInstalmentDueDate(LocalDate.parse("2022-01-20"))
        financingsContract.CET("20.22")
        financingsContract.amortizationScheduled(FinancingsContractV2.AmortizationScheduledEnum.SAC)
        financingsContract.amortizationScheduledAdditionalInfo("indeed")
        financingsContract.addInterestRatesItem(financingsContractInterestRate)
        financingsContract.addContractedFeesItem(financingsContractFee)
        financingsContract.addContractedFinanceChargesItem(financingsFinanceCharge)

        FinancingsListContractV2 listContract = new FinancingsListContractV2()
        listContract.contractId("237db2d239d29d234jr-23d32")
        listContract.brandName("Bank of Nook")
        listContract.companyCnpj("4-2332rfbd-e")
        listContract.productType(EnumProductTypeV2.FINANCIAMENTOS)
        listContract.productSubType(EnumProductSubTypeV2.CUSTEIO)
        listContract.ipocCode("341234nedfywefdy")

        FinancingsFeeOverParcelV2 financingsFeeOverParcel = new FinancingsFeeOverParcelV2()

        financingsFeeOverParcel.feeName("Steven")
        financingsFeeOverParcel.feeCode("s4ewrv3d823ed-re2d")
        financingsFeeOverParcel.feeAmount("30.99")

        FinancingsChargeOverParcelV2 financingsChargeOverParcel = new FinancingsChargeOverParcelV2()

        financingsChargeOverParcel.setChargeType(EnumContractFinanceChargeTypeV2.MULTA_ATRASO_PAGAMENTO)
        financingsChargeOverParcel.setChargeAdditionalInfo("Money")
        financingsChargeOverParcel.setChargeAmount("19.23")

        FinancingsOverParcelV2 financingsOverParcel = new FinancingsOverParcelV2()
        financingsOverParcel.addFeesItem(financingsFeeOverParcel)
        financingsOverParcel.addChargesItem(financingsChargeOverParcel)

        FinancingsReleasesV2 financingsReleases = new FinancingsReleasesV2()

        financingsReleases.paymentId("s4ewrv3d823ed-re2d")
        financingsReleases.isOverParcelPayment(true)
        financingsReleases.instalmentId("e23dh72c823c-2d2323d4")
        financingsReleases.paidDate(LocalDate.parse("2022-01-20"))
        financingsReleases.currency("BRL")
        financingsReleases.paidAmount("20.2222")
        financingsReleases.overParcel(financingsOverParcel)

        FinancingsPaymentsV2 financingsPayment = new FinancingsPaymentsV2()

        financingsPayment.paidInstalments(99.999999)
        financingsPayment.contractOutstandingBalance("4.99")
        financingsPayment.addReleasesItem(financingsReleases)


        FinancingsWarrantiesV2 financingsWarranties = new FinancingsWarrantiesV2()

        financingsWarranties.currency("Bells")
        financingsWarranties.warrantyType(FinancingsWarrantiesV2.WarrantyTypeEnum.BENS_ARRENDADOS)
        financingsWarranties.warrantySubType(FinancingsWarrantiesV2.WarrantySubTypeEnum.CIVIL)
        financingsWarranties.warrantyAmount("99.99")

        FinancingsBalloonPaymentV2 financingsBalloonPayment = new FinancingsBalloonPaymentV2()

        financingsBalloonPayment.dueDate(LocalDate.parse("2022-01-20"))
        financingsBalloonPayment.amount(new FinancingsBalloonPaymentAmountV2().amount("14.44").currency("BRL"))

        FinancingsInstalmentsV2 financingsInstalments = new FinancingsInstalmentsV2()

        financingsInstalments.typeNumberOfInstalments(FinancingsInstalmentsV2.TypeNumberOfInstalmentsEnum.DIA)
        financingsInstalments.totalNumberOfInstalments(44.444)
        financingsInstalments.typeContractRemaining(FinancingsInstalmentsV2.TypeContractRemainingEnum.DIA)
        financingsInstalments.contractRemainingNumber(44.44444)
        financingsInstalments.paidInstalments(44.4444)
        financingsInstalments.dueInstalments(4.44444)
        financingsInstalments.pastDueInstalments(444.44444)
        financingsInstalments.addBalloonPaymentsItem(financingsBalloonPayment)

        responseFinancingsContractList = new ResponseFinancingsContractList().data(List.of(listContract)).meta(new Meta().totalPages(1))
        responseFinancingsContract = new ResponseFinancingsContractV2().data(financingsContract)
        responseFinancingsPayments = new ResponseFinancingsPaymentsV2().data(financingsPayment)
        responseFinancingsInstalments = new ResponseFinancingsInstalmentsV2().data(financingsInstalments)
        responseFinancingsWarranties = new ResponseFinancingsWarrantiesV2().data(List.of(financingsWarranties)).meta(new Meta().totalPages(1))

        bankLambdaUtils.getConsentIdFromRequest(_ as HttpRequest<?>) >> "12345"
    }

    def cleanup() {
        handler.close()
    }

    void "We can get all finance contracts"() {
        given:
        financingService.getFinancingContractList(_ as Pageable, _ as String) >> responseFinancingsContractList
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/financings/v2/contracts", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "financings", builder)
        when:
        def response = handler.proxy(builder.build(), lambdaContext)
        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }
    void "We can get a finance contract"() {
        given:
        financingService.getFinancingContractV2(_ as String, _ as UUID) >> responseFinancingsContract
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/financings/v2/contracts/${UUID.randomUUID()}", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "financings", builder)
        when:
        def response = handler.proxy(builder.build(), lambdaContext)
        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }
    void "We can't get a contract which doesn't exist"() {
        given:
        financingService.getFinancingContractV2(_ as String, _ as UUID) >> { throw new HttpStatusException(HttpStatus.BAD_REQUEST, "") }
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/financings/v2/contracts/${UUID.randomUUID()}", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "financings", builder)
        when:
        def response = handler.proxy(builder.build(), lambdaContext)
        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code
    }
    void "We can get a Payment v1"() {
        given:
        financingService.getFinancingPaymentsV2(_ as String, _ as UUID) >> responseFinancingsPayments
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/financings/v2/contracts/${UUID.randomUUID()}/payments", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "financings", builder)
        when:
        def response = handler.proxy(builder.build(), lambdaContext)
        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }
    void "We can get scheduled instalments"() {
        given:
        financingService.getFinancingScheduledInstalmentsV2(_ as String, _ as UUID) >> responseFinancingsInstalments
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/financings/v2/contracts/${UUID.randomUUID()}/scheduled-instalments", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "financings", builder)
        when:
        def response = handler.proxy(builder.build(), lambdaContext)
        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }
    void "We can get warranties v1"() {
        given:
        financingService.getFinancingsWarrantiesV2(_ as Pageable, _ as String, _ as UUID) >> responseFinancingsWarranties
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/open-banking/financings/v2/contracts/${UUID.randomUUID()}/warranties", HttpMethod.GET.toString())
        AuthHelper.authorize(scopes: "financings", builder)
        when:
        def response = handler.proxy(builder.build(), lambdaContext)
        then:
        response.statusCode == HttpStatus.OK.code
        response.body != null
    }
}