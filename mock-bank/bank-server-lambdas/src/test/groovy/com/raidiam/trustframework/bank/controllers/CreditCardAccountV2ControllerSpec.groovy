package com.raidiam.trustframework.bank.controllers

import com.raidiam.trustframework.bank.FullCreateConsentFactory
import com.raidiam.trustframework.bank.TestRequestDataFactory
import com.raidiam.trustframework.bank.domain.AccountHolderEntity
import com.raidiam.trustframework.bank.utils.BankLambdaUtils
import com.raidiam.trustframework.mockbank.models.generated.*
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.apache.http.client.utils.URIBuilder
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Stepwise

import java.time.LocalDate

import static com.raidiam.trustframework.bank.TestEntityDataFactory.anAccountHolder

@Stepwise
@MicronautTest(transactional = false, environments = ["db"])
@Testcontainers
class CreditCardAccountV2ControllerSpec extends FullCreateConsentFactory {
    @Shared
    AccountHolderEntity accountHolder
    @Shared
    ResponseCreditCardAccount postCreditCardAccountResponse
    @Shared
    ResponseCreditCardAccountBill postCreditCardBillResponse
    @Shared
    ResponseCreditCardAccountLimits postCreditCardLimitsData
    @Shared
    ResponseCreditCardAccountsTransactionListV2 postCreditCardAccountTransactionResponse
    @Shared
    String getToken

    def setup() {
        if (runSetup) {
            //ADD Account Holder via db
            accountHolder = accountHolderRepository.save(anAccountHolder())

            //ADD Credit Card Account via V2 Controller
            def adminToken = createToken("op:admin")
            CreateCreditCardAccount newAccount = TestRequestDataFactory.creditCardAccount()
            postCreditCardAccountResponse = client.toBlocking()
                    .retrieve(HttpRequest.POST("/admin/customers/${accountHolder.getAccountHolderId().toString()}/credit-cards-accounts", mapper.writeValueAsString(newAccount))
                            .header("Authorization", "Bearer ${adminToken}"), ResponseCreditCardAccount)

            //ADD Credit Card Account Limits via V2 Controller
            CreateCreditCardAccountLimits accountsLimitsData = TestRequestDataFactory.createCreditCardAccountsLimits()
            postCreditCardLimitsData = client.toBlocking()
                    .retrieve(HttpRequest.POST("/admin/customers/${accountHolder.getAccountHolderId().toString()}/credit-cards-accounts/" + postCreditCardAccountResponse.getData().getCreditCardAccountId() + "/limits", mapper.writeValueAsString(List.of(accountsLimitsData)))
                            .header("Authorization", "Bearer ${adminToken}"), ResponseCreditCardAccountLimits)

            //ADD Credit Card Account Bill via V2 Controller
            CreateCreditCardAccountBill newBill = TestRequestDataFactory.creditCardBillDto()
            postCreditCardBillResponse = client.toBlocking()
                    .retrieve(HttpRequest.POST("/admin/customers/${accountHolder.getAccountHolderId().toString()}/credit-cards-accounts/" + postCreditCardAccountResponse.getData().getCreditCardAccountId() + "/bills", mapper.writeValueAsString(newBill))
                            .header("Authorization", "Bearer ${adminToken}"), ResponseCreditCardAccountBill)

            //ADD Credit Card Account Transaction via V2 Controller
            CreateCreditCardAccountTransactionData accountTransactionDto = TestRequestDataFactory.cardAccountTransactionDto()
            String transactionUrl = "/admin/customers/${accountHolder.getAccountHolderId().toString()}/credit-cards-accounts/v2/" + postCreditCardAccountResponse.getData().getCreditCardAccountId() + "/bills/" + postCreditCardBillResponse.getData().getBillId() + "/transactions"
            accountTransactionDto.setTransactionDate(LocalDate.now())
            postCreditCardAccountTransactionResponse = client.toBlocking()
                    .retrieve(HttpRequest.POST(transactionUrl, mapper.writeValueAsString(new CreateCreditCardAccountTransactionList().data(List.of(accountTransactionDto))))
                            .header("Authorization", "Bearer ${adminToken}"), ResponseCreditCardAccountsTransactionListV2)

            //ADD Consent via V2 Controller
            getToken = createConsentWithCreditCardAccountPermissions(accountHolder, postCreditCardAccountResponse.getData().getCreditCardAccountId().toString())

            runSetup = false
        }
    }

    void "we can GET credit card account bills v2"() {
        when:
        URI uri = new URIBuilder('/open-banking/credit-cards-accounts/v2/accounts/' + postCreditCardAccountResponse.getData().getCreditCardAccountId() + '/bills')
                .addParameter("fromDueDate", LocalDate.now().toString())
                .addParameter("toDueDate", LocalDate.now().plusDays(2).toString())
                .build()

        def response = client.toBlocking().retrieve(HttpRequest.GET(uri)
                .header("Authorization", "Bearer ${getToken}"), ResponseCreditCardAccountsBillsV2)

        then:
        response.getData() != null
        response.getData().size() == 1
        def bill = response.getData().first()
        bill.getBillId() != null
        bill.getDueDate() == postCreditCardBillResponse.getData().getDueDate()
        bill.getBillTotalAmount().getAmount() == BankLambdaUtils.formatAmountV2(postCreditCardBillResponse.getData().getBillTotalAmount())
        bill.getBillTotalAmount().getCurrency() == postCreditCardBillResponse.getData().getBillTotalAmountCurrency()
        bill.getBillMinimumAmount().getAmount() == BankLambdaUtils.formatAmountV2(postCreditCardBillResponse.getData().getBillMinimumAmount())
        bill.getBillMinimumAmount().getCurrency() == postCreditCardBillResponse.getData().getBillMinimumAmountCurrency()
        bill.isIsInstalment() == postCreditCardBillResponse.getData().isInstalment()
        bill.getFinanceCharges().first().getAmount() == BankLambdaUtils.formatAmountV2(postCreditCardBillResponse.getData().getFinanceCharges().first().getAmount())
        bill.getFinanceCharges().first().getAdditionalInfo() == postCreditCardBillResponse.getData().getFinanceCharges().first().getAdditionalInfo()
        bill.getFinanceCharges().first().getCurrency() == postCreditCardBillResponse.getData().getFinanceCharges().first().getCurrency()
        bill.getFinanceCharges().first().getType().name() == postCreditCardBillResponse.getData().getFinanceCharges().first().getType().name()
        bill.getPayments().first().getCurrency() == postCreditCardBillResponse.getData().getPayments().first().getCurrency()
        bill.getPayments().first().getAmount() == BankLambdaUtils.formatAmountV2(postCreditCardBillResponse.getData().getPayments().first().getAmount())
        bill.getPayments().first().getPaymentDate() == postCreditCardBillResponse.getData().getPayments().first().getPaymentDate()
        bill.getPayments().first().getPaymentMode().name() == postCreditCardBillResponse.getData().getPayments().first().getPaymentMode().name()
        bill.getPayments().first().getValueType().name() == postCreditCardBillResponse.getData().getPayments().first().getValueType().name()
    }

    void "we can GET credit card account limits v2"() {
        when:
        URI uri = new URIBuilder('/open-banking/credit-cards-accounts/v2/accounts/' + postCreditCardAccountResponse.getData().getCreditCardAccountId() + '/limits').build()

        def response = client.toBlocking().retrieve(HttpRequest.GET(uri).header("Authorization", "Bearer ${getToken}"), ResponseCreditCardAccountsLimitsV2)

        then:
        response.getData() != null
        response.getData().size() == 1
        def limits = response.getData().first()
        def postLimits = postCreditCardLimitsData.getData().first()
        limits.getCreditLineLimitType().name() == postLimits.getCreditLineLimitType().name()
        limits.getConsolidationType().name() == postLimits.getConsolidationType().name()
        limits.getIdentificationNumber() == postLimits.getIdentificationNumber()
        limits.getLineName().name() == postLimits.getLineName().name()
        limits.getLineNameAdditionalInfo() == postLimits.getLineNameAdditionalInfo()
        limits.isIsLimitFlexible() == postLimits.isIsLimitFlexible()
        limits.getLimitAmount().getAmount() == BankLambdaUtils.formatAmountV2(postLimits.getLimitAmount())
        limits.getLimitAmount().getCurrency() == postLimits.getLimitAmountCurrency()
        limits.getUsedAmount().getAmount() == BankLambdaUtils.formatAmountV2(postLimits.getUsedAmount())
        limits.getUsedAmount().getCurrency() == postLimits.getUsedAmountCurrency()
        limits.getAvailableAmount().getAmount() == BankLambdaUtils.formatAmountV2(postLimits.getAvailableAmount())
        limits.getAvailableAmount().getCurrency() == postLimits.getAvailableAmountCurrency()
    }

    void "we can GET credit card account transactions v2"() {
        when:
        URI uri = new URIBuilder('/open-banking/credit-cards-accounts/v2/accounts/' + postCreditCardAccountResponse.getData().getCreditCardAccountId() + '/transactions')
                .addParameter("fromTransactionDate", LocalDate.now().toString())
                .addParameter("toTransactionDate", LocalDate.now().plusDays(2).toString())
                .build()

        def response = client.toBlocking().retrieve(HttpRequest.GET(uri).header("Authorization", "Bearer ${getToken}"), ResponseCreditCardAccountsTransactionsV2)

        then:
        response.getData() != null
        response.getData().size() == 1
        def transaction = response.getData().first()
        def adminTransaction = postCreditCardAccountTransactionResponse.getData().first()
        transaction.getTransactionId() != null
        transaction.getIdentificationNumber() == adminTransaction.getIdentificationNumber()
        transaction.getTransactionName() == adminTransaction.getTransactionName()
        transaction.getCreditDebitType().name() == adminTransaction.getCreditDebitType().name()
        transaction.getTransactionType().name() == adminTransaction.getTransactionType().name()
        transaction.getTransactionalAdditionalInfo() == adminTransaction.getTransactionalAdditionalInfo()
        transaction.getPaymentType().name() == adminTransaction.getPaymentType().name()
        transaction.getFeeType().name() == adminTransaction.getFeeType().name()
        transaction.getFeeTypeAdditionalInfo() == adminTransaction.getFeeTypeAdditionalInfo()
        transaction.getOtherCreditsType().name() == adminTransaction.getOtherCreditsType().name()
        transaction.getOtherCreditsAdditionalInfo() == adminTransaction.getOtherCreditsAdditionalInfo()
        transaction.getChargeIdentificator() == adminTransaction.getChargeIdentificator()
        transaction.getChargeNumber() == adminTransaction.getChargeNumber()
        transaction.getBrazilianAmount().getAmount() == adminTransaction.getBrazilianAmount().getAmount()
        transaction.getBrazilianAmount().getCurrency() == adminTransaction.getBrazilianAmount().getCurrency()
        transaction.getAmount().getAmount() == adminTransaction.getAmount().getAmount()
        transaction.getAmount().getCurrency() == adminTransaction.getAmount().getCurrency()
        transaction.getTransactionDate() == adminTransaction.getTransactionDate()
        transaction.getBillPostDate() == adminTransaction.getBillPostDate()
        transaction.getPayeeMCC() == adminTransaction.getPayeeMCC()
    }

    void "we can GET credit card account transactions current v2"() {
        when:
        URI uri = new URIBuilder('/open-banking/credit-cards-accounts/v2/accounts/' + postCreditCardAccountResponse.getData().getCreditCardAccountId() + '/transactions-current')
                .addParameter("fromTransactionDate", LocalDate.now().toString())
                .addParameter("toTransactionDate", LocalDate.now().plusDays(2).toString())
                .build()

        def response = client.toBlocking().retrieve(HttpRequest.GET(uri).header("Authorization", "Bearer ${getToken}"), ResponseCreditCardAccountsTransactionsV2)

        then:
        response.getData() != null
        response.getData().size() == 1
    }

    void "we can GET credit card account transactions by BillId v2"() {
        when:
        URI uri = new URIBuilder('/open-banking/credit-cards-accounts/v2/accounts/' + postCreditCardAccountResponse.getData().getCreditCardAccountId()
                + '/bills/' + postCreditCardBillResponse.getData().getBillId() + '/transactions')
                .addParameter("fromTransactionDate", LocalDate.now().toString())
                .addParameter("toTransactionDate", LocalDate.now().plusDays(2).toString())
                .build()

        def response = client.toBlocking().retrieve(HttpRequest.GET(uri).header("Authorization", "Bearer ${getToken}"), ResponseCreditCardAccountsTransactionsV2)

        then:
        response.getData() != null
        response.getData().size() == 1
    }

    void "we can GET throw credit card account transactions current v2"() {
        when:
        URI uri = new URIBuilder('/open-banking/credit-cards-accounts/v2/accounts/' + postCreditCardAccountResponse.getData().getCreditCardAccountId() + '/transactions-current')
                .addParameter("fromTransactionDate", fromTransactionDate.toString())
                .addParameter("toTransactionDate", toTransactionDate.toString())
                .build()

        client.toBlocking().retrieve(HttpRequest.GET(uri).header("Authorization", "Bearer ${getToken}"), ResponseCreditCardAccountsTransactionsV2)

        then:
        HttpClientResponseException e = thrown()
        e.status == status

        where:
        fromTransactionDate          | toTransactionDate            | status                          | msg
        LocalDate.now().minusDays(9) | LocalDate.now()              | HttpStatus.UNPROCESSABLE_ENTITY | "Date range must be no more than 7 days"
        LocalDate.now()              | LocalDate.now().minusDays(1) | HttpStatus.UNPROCESSABLE_ENTITY | "Wrong date period"
    }

    def "enable cleanup"() {
        //This must be the final test
        when:
        runCleanup = true

        then:
        runCleanup
    }
}
