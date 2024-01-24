package com.raidiam.trustframework.bank.controllers

import com.raidiam.trustframework.bank.FullCreateConsentFactory
import com.raidiam.trustframework.bank.TestRequestDataFactory
import com.raidiam.trustframework.bank.enums.ContractTypeEnum
import com.raidiam.trustframework.bank.utils.BankLambdaUtils
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.http.HttpRequest
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Stepwise

import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccountHolder

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
@Testcontainers
class UnarrangedAccountsOverdraftV2ControllerSpec extends FullCreateConsentFactory {
    @Shared
    String getToken
    @Shared
    String contractId
    @Shared
    EnumContractType type = EnumContractType.UNARRANGED_ACCOUNT_OVERDRAFT
    @Shared
    ContractTypeEnum pathType = ContractTypeEnum.UNARRANGED_ACCOUNT_OVERDRAFT
    @Shared
    ResponseContractData postContractResponse
    @Shared
    ResponseContractWarranties postContractWarrantiesResponse

    def setup() {
        //ADD Account Holder via db
        def accountHolder = accountHolderRepository.save(anAccountHolder())

        //ADD Contract via V2 Controller
        def adminToken = createToken("op:admin")
        CreateContract newContract = TestRequestDataFactory.createContract(type)
        postContractResponse = client.toBlocking()
                .retrieve(HttpRequest.POST("/admin/customers/${accountHolder.getAccountHolderId().toString()}/${pathType.toString()}", mapper.writeValueAsString(newContract))
                        .header("Authorization", "Bearer ${adminToken}"), ResponseContract).getData()
        contractId = postContractResponse.getContractId().toString()

        //ADD Contract Warranties via V2 Controller
        ContractWarranties newContractWarranties = new ContractWarranties().data(List.of(TestRequestDataFactory.createWarranties()))
        postContractWarrantiesResponse = client.toBlocking()
                .retrieve(HttpRequest.POST("/admin/customers/${accountHolder.getAccountHolderId().toString()}/${pathType.toString()}/${contractId}/warranties",
                        mapper.writeValueAsString(newContractWarranties))
                        .header("Authorization", "Bearer ${adminToken}"), ResponseContractWarranties)

        //ADD Consent with permissions
        getToken = createConsentWithContractPermissions(accountHolder, contractId, pathType)

        runSetup = false
    }

    def cleanup() {
    }

    void "We can get a contract v2"() {
        when:
        def response = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/${pathType.toString()}/v2/contracts/${contractId}")
                        .header("Authorization", "Bearer ${getToken}"), ResponseUnarrangedAccountOverdraftContractV2)

        then:
        response.getData() != null
        def contract = response.getData()

        contract.getContractNumber() == postContractResponse.getContractNumber()
        contract.getIpocCode() == postContractResponse.getIpocCode()
        contract.getProductName() == postContractResponse.getProductName()
        contract.getProductType().toString() == postContractResponse.getProductType()
        contract.getProductSubType().toString() == postContractResponse.getProductSubType()
        contract.getContractDate() == postContractResponse.getContractDate()
        contract.getDisbursementDates().contains(postContractResponse.getDisbursementDate())
        contract.getSettlementDate() == postContractResponse.getSettlementDate()
        contract.getContractAmount() == BankLambdaUtils.formatAmountV2(postContractResponse.getContractAmount())
        contract.getCurrency() == postContractResponse.getCurrency()
        contract.getDueDate() == postContractResponse.getDueDate()
        contract.getInstalmentPeriodicity().toString() == postContractResponse.getInstalmentPeriodicity().toString()
        contract.getInstalmentPeriodicityAdditionalInfo() == postContractResponse.getInstalmentPeriodicityAdditionalInfo()
        contract.getFirstInstalmentDueDate() == postContractResponse.getFirstInstalmentDueDate()
        contract.getCET() == postContractResponse.getCet().toString()
        contract.getAmortizationScheduled().toString() == postContractResponse.getAmortizationScheduled().toString()
        contract.getAmortizationScheduledAdditionalInfo() == postContractResponse.getAmortizationScheduledAdditionalInfo()
        contract.getInterestRates().size() == postContractResponse.getInterestRates().size()
        contract.getContractedFees().size() == postContractResponse.getContractedFees().size()
        contract.getContractedFinanceCharges().size() == postContractResponse.getContractedFinanceCharges().size()

    }

    void "We can get scheduled instalments v2"() {
        when:
        def response = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/${pathType.toString()}/v2/contracts/${contractId}/scheduled-instalments")
                        .header("Authorization", "Bearer ${getToken}"), ResponseUnarrangedAccountOverdraftInstalmentsV2)

        then:
        response.getData() != null
        def scheduledInstalments = response.getData()
        response.getMeta().getTotalRecords() == scheduledInstalments.getBalloonPayments().size()

        scheduledInstalments.getTypeNumberOfInstalments().toString() == postContractResponse.getTypeNumberOfInstalments().toString()
        scheduledInstalments.getTotalNumberOfInstalments() == postContractResponse.getTotalNumberOfInstalments()
        scheduledInstalments.getTypeContractRemaining().toString() == postContractResponse.getTypeContractRemaining().toString()
        scheduledInstalments.getContractRemainingNumber() == postContractResponse.getContractRemainingNumber()
        scheduledInstalments.getPaidInstalments() == postContractResponse.getPaidInstalments()
        scheduledInstalments.getDueInstalments() == postContractResponse.getDueInstalments()
        scheduledInstalments.getPastDueInstalments() == postContractResponse.getPastDueInstalments()
        def balloonPaymentsAmount = scheduledInstalments.getBalloonPayments().first()
        def adminBalloonPaymentsAmount = postContractResponse.getBalloonPayments().stream()
                .filter { BankLambdaUtils.formatAmountV2(it.getAmount()).equals(balloonPaymentsAmount.getAmount().getAmount()) }
                .findFirst()
                .get()
        balloonPaymentsAmount.getAmount().getCurrency() == adminBalloonPaymentsAmount.getCurrency()
    }

    void "We can get warranties v2"() {
        when:
        def response = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/${pathType.toString()}/v2/contracts/${contractId}/warranties")
                        .header("Authorization", "Bearer ${getToken}"), ResponseUnarrangedAccountOverdraftWarrantiesV2)

        then:
        response.getData() != null
        def warranties = response.getData().first()
        def postWarranties = postContractWarrantiesResponse.getData().first()

        warranties.getCurrency() == postWarranties.getCurrency()
        warranties.getWarrantyType().toString() == postWarranties.getWarrantyType().toString()
        warranties.getWarrantySubType().toString() == postWarranties.getWarrantySubType().toString()
        warranties.getWarrantyAmount() == BankLambdaUtils.formatAmountV2(postWarranties.getWarrantyAmount())
    }

    void "We can get contract payments v2"() {
        when:
        def response = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/${pathType.toString()}/v2/contracts/${contractId}/payments")
                        .header("Authorization", "Bearer ${getToken}"), ResponseUnarrangedAccountOverdraftPaymentsV2)

        then:
        response.getData() != null
        def payments = response.getData()

        payments.getPaidInstalments() == postContractResponse.getPaidInstalments()
        payments.getContractOutstandingBalance() == BankLambdaUtils.formatAmountV2(postContractResponse.getContractOutstandingBalance())
        response.getMeta().getTotalRecords() == payments.getReleases().size()

        def releases = payments.getReleases().first()
        def adminReleases = postContractResponse.getReleases().stream()
                .filter { it.getInstalmentId().equals(releases.getInstalmentId()) }
                .findFirst()
                .get()
        releases.isOverParcelPayment == adminReleases.isOverParcelPayment
        releases.getPaidDate() == adminReleases.getPaidDate()
        releases.getCurrency() == adminReleases.getCurrency()
        releases.getPaidAmount() == BankLambdaUtils.formatAmountV2(adminReleases.getPaidAmount())

        def overParcelFees = releases.getOverParcel().getFees().first()
        def adminOverParcelFees = adminReleases.getOverParcelFees().first()
        overParcelFees.getFeeName() == adminOverParcelFees.getFeeName()
        overParcelFees.getFeeCode() == adminOverParcelFees.getFeeCode()
        overParcelFees.getFeeAmount() == BankLambdaUtils.formatAmountV2(adminOverParcelFees.getFeeAmount())

        def overParcelCharges = releases.getOverParcel().getCharges().first()
        def adminOverParcelCharges = adminReleases.getOverParcelCharges().first()
        overParcelCharges.getChargeType().name() == adminOverParcelCharges.getChargeType().name()
        overParcelCharges.getChargeAdditionalInfo() == adminOverParcelCharges.getChargeAdditionalInfo()
        overParcelCharges.getChargeAmount() == BankLambdaUtils.formatAmountV2(adminOverParcelCharges.getChargeAmount())

    }

    void "Returning an empty contract v2 has the correct Meta values"() {
        when:
        def accountHolder = accountHolderRepository.save(anAccountHolder())
        def adminToken = createToken("op:admin")
        CreateContract newContract = TestRequestDataFactory.createEmptyContract(type)

        postContractResponse = client.toBlocking()
                .retrieve(HttpRequest.POST("/admin/customers/${accountHolder.getAccountHolderId().toString()}/${pathType.toString()}", mapper.writeValueAsString(newContract))
                        .header("Authorization", "Bearer ${adminToken}"), ResponseContract).getData()
        contractId = postContractResponse.getContractId().toString()

        getToken = createConsentWithContractPermissions(accountHolder, contractId, pathType)

        def response = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/${pathType.toString()}/v2/contracts/${contractId}")
                        .header("Authorization", "Bearer ${getToken}"), ResponseLoansContractV2)

        then:
        response.getData() != null
        response.getMeta().getTotalRecords() == 1
        response.getMeta().getTotalPages() == 1


        when:
        response = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/${pathType.toString()}/v2/contracts/${contractId}/payments")
                        .header("Authorization", "Bearer ${getToken}"), ResponseLoansPaymentsV2)

        then:
        response.getData() != null
        response.getMeta().getTotalRecords() == 0
        response.getMeta().getTotalPages() == 0

        when:
        response = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/${pathType.toString()}/v2/contracts/${contractId}/scheduled-instalments")
                        .header("Authorization", "Bearer ${getToken}"), ResponseLoansInstalmentsV2)

        then:
        response.getData() != null
        response.getMeta().getTotalRecords() == 0
        response.getMeta().getTotalPages() == 0

        when:
        response = client.toBlocking()
                .retrieve(HttpRequest.GET("/open-banking/${pathType.toString()}/v2/contracts/${contractId}/warranties")
                        .header("Authorization", "Bearer ${getToken}"), ResponseLoansWarrantiesV2)

        then:
        response.getData() != null
        response.getMeta().getTotalRecords() == 0
        response.getMeta().getTotalPages() == 0
    }
}