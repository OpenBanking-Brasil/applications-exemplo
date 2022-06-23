package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.domain.ConsentCreditCardAccountsEntity;
import com.raidiam.trustframework.bank.domain.CreditCardAccountsBillsEntity;
import com.raidiam.trustframework.bank.domain.CreditCardAccountsEntity;
import com.raidiam.trustframework.bank.domain.CreditCardAccountsTransactionEntity;
import com.raidiam.trustframework.bank.repository.CreditCardAccountsRepository;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public class CreditCardAccountsService extends BaseBankService {

    private static final Logger LOG = LoggerFactory.getLogger(CreditCardAccountsService.class);

    CreditCardAccountsService(CreditCardAccountsRepository creditCardAccountsRepository) {
        this.creditCardAccountsRepository = creditCardAccountsRepository;
    }

    public ResponseCreditCardAccountsList getCreditCardAccounts(Pageable pageable, String consentId) {
        LOG.info("Getting accounts response for consent id {}", consentId);
        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.CREDIT_CARDS_ACCOUNTS_READ);

        var consentCreditCardAccount = consentCreditCardAccountsRepository.findByConsentIdOrderByCreatedAtAsc(consentId, pageable);
        BankLambdaUtils.checkConsentOwnerIsCreditCardAccountOwner(consentCreditCardAccount, consentEntity);
        var response = new ResponseCreditCardAccountsList().data(consentCreditCardAccount.getContent()
                .stream()
                .map(ConsentCreditCardAccountsEntity::getCreditCardAccount)
                .map(CreditCardAccountsEntity::getCreditCardAccountsData)
                .collect(Collectors.toList()));

        response.setMeta(BankLambdaUtils.getMeta(consentCreditCardAccount, response.getData().size()));

        return response;
    }

    public ResponseCreditCardAccountsIdentification getCreditCardAccount(String consentId, String acctId) {
        LOG.info("Getting credit card account response for consent id {} and account id {}", consentId, acctId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var accountEntity = BankLambdaUtils.getCreditCardAccount(acctId, creditCardAccountsRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentCoversCreditCardAccount(consentEntity, accountEntity);
        BankLambdaUtils.checkConsentOwnerIsCreditCardAccountOwner(consentEntity, accountEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.CREDIT_CARDS_ACCOUNTS_READ);

        return new ResponseCreditCardAccountsIdentification().data(accountEntity.getCreditCardsAccountsIdentificationData());
    }

    public ResponseCreditCardAccountsTransactions getCreditCardAccountTransactions(Pageable pageable, String consentId,
                                                                                   LocalDate fromDate, LocalDate toDate,
                                                                                   BigDecimal payeeMCC, String transactionType, String acctId) {
        LOG.info("Getting credit card account transactions response for consent id {} and account id {}", consentId, acctId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var accountEntity = BankLambdaUtils.getCreditCardAccount(acctId, creditCardAccountsRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentCoversCreditCardAccount(consentEntity, accountEntity);
        BankLambdaUtils.checkConsentOwnerIsCreditCardAccountOwner(consentEntity, accountEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.CREDIT_CARDS_ACCOUNTS_TRANSACTIONS_READ);

        Page<CreditCardAccountsTransactionEntity> transactions;

        if (transactionType != null && payeeMCC != null) {
            transactions = creditCardAccountsTransactionRepository
                    .findByCreditCardAccountIdAndTransactionDateBetweenIsAndTransactionTypeAndPayeeMCCOrderByCreatedAtAsc(
                            UUID.fromString(acctId), fromDate, toDate, transactionType, payeeMCC, pageable);
        } else {
            if (transactionType == null && payeeMCC == null) {
                transactions = creditCardAccountsTransactionRepository
                        .findByCreditCardAccountIdAndTransactionDateBetweenIsOrderByCreatedAtAsc(
                                UUID.fromString(acctId), fromDate, toDate, pageable);
            } else {
                if (transactionType != null) {
                    transactions = creditCardAccountsTransactionRepository
                            .findByCreditCardAccountIdAndTransactionDateBetweenIsAndTransactionTypeOrderByCreatedAtAsc(
                                    UUID.fromString(acctId), fromDate, toDate, transactionType, pageable);
                } else {
                    transactions = creditCardAccountsTransactionRepository
                            .findByCreditCardAccountIdAndTransactionDateBetweenIsAndPayeeMCCOrderByCreatedAtAsc(
                                    UUID.fromString(acctId), fromDate, toDate, payeeMCC, pageable);
                }
            }
        }

        var response = new ResponseCreditCardAccountsTransactions().data(transactions.getContent()
                .stream()
                .map(CreditCardAccountsTransactionEntity::getDTO)
                .collect(Collectors.toList()));

        response.setMeta(BankLambdaUtils.getMeta(transactions, response.getData().size()));

        return response;
    }

    public ResponseCreditCardAccountsLimits getCreditCardAccountLimits(String consentId, String acctId) {
        LOG.info("Getting account Credit Card Limits response for consent id {} and account id {}", consentId, acctId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var accountEntity = BankLambdaUtils.getCreditCardAccount(acctId, creditCardAccountsRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentCoversCreditCardAccount(consentEntity, accountEntity);
        BankLambdaUtils.checkConsentOwnerIsCreditCardAccountOwner(consentEntity, accountEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.CREDIT_CARDS_ACCOUNTS_LIMITS_READ);

        return new ResponseCreditCardAccountsLimits().data(accountEntity.getCreditCardAccountsLimitsData());
    }

    public ResponseCreditCardAccountsBills getCreditCardAccountsBills(Pageable pageable, String consentId,
                                                                      LocalDate fromDate, LocalDate toDate, String acctId) {
        LOG.info("Getting account Credit Card bills response for consent id {} and account id {}", consentId, acctId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var accountEntity = BankLambdaUtils.getCreditCardAccount(acctId, creditCardAccountsRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentCoversCreditCardAccount(consentEntity, accountEntity);
        BankLambdaUtils.checkConsentOwnerIsCreditCardAccountOwner(consentEntity, accountEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.CREDIT_CARDS_ACCOUNTS_BILLS_READ);

        var bills = creditCardAccountsBillsRepository
                .findByCreditCardAccountIdAndDueDateBetweenIsOrderByCreatedAtAsc(accountEntity.getCreditCardAccountId(), fromDate, toDate, pageable);

        var response = new ResponseCreditCardAccountsBills().data(bills.getContent()
                .stream()
                .map(CreditCardAccountsBillsEntity::getDTO)
                .collect(Collectors.toList()));

        response.setMeta(BankLambdaUtils.getMeta(bills, response.getData().size()));

        return response;
    }


    public ResponseCreditCardAccountsTransactions getCreditCardAccountBillsTransactions(Pageable pageable, String consentId,
                                                                                        LocalDate fromDate, LocalDate toDate,
                                                                                        BigDecimal payeeMCC, String transactionType,
                                                                                        String acctId, String billId) {
        LOG.info("Getting account Credit Card Bills Transaction response for consent id {} and account id {} and bill id {}", consentId, acctId, billId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var accountEntity = BankLambdaUtils.getCreditCardAccount(acctId, creditCardAccountsRepository);
        var billEntity = BankLambdaUtils.getCreditCardAccountBill(billId, creditCardAccountsBillsRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentCoversCreditCardAccount(consentEntity, accountEntity);
        BankLambdaUtils.checkBillOwnedCreditCardAccount(billEntity, accountEntity);
        BankLambdaUtils.checkConsentOwnerIsCreditCardAccountOwner(consentEntity, accountEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, CreateConsentData.PermissionsEnum.CREDIT_CARDS_ACCOUNTS_BILLS_TRANSACTIONS_READ);

        Page<CreditCardAccountsTransactionEntity> transactions;

        if (transactionType != null && payeeMCC != null) {
            transactions = creditCardAccountsTransactionRepository
                    .findByCreditCardAccountIdAndBillIdAndTransactionDateBetweenIsAndTransactionTypeAndPayeeMCCOrderByCreatedAtAsc(
                            UUID.fromString(acctId), UUID.fromString(billId), fromDate, toDate, transactionType, payeeMCC, pageable);
        } else {
            if (transactionType == null && payeeMCC == null) {
                transactions = creditCardAccountsTransactionRepository
                        .findByCreditCardAccountIdAndBillIdAndTransactionDateBetweenIsOrderByCreatedAtAsc(
                                UUID.fromString(acctId), UUID.fromString(billId), fromDate, toDate, pageable);
            } else {
                if (transactionType != null) {
                    transactions = creditCardAccountsTransactionRepository
                            .findByCreditCardAccountIdAndBillIdAndTransactionDateBetweenIsAndTransactionTypeOrderByCreatedAtAsc(
                                    UUID.fromString(acctId), UUID.fromString(billId), fromDate, toDate, transactionType, pageable);
                } else {
                    transactions = creditCardAccountsTransactionRepository
                            .findByCreditCardAccountIdAndBillIdAndTransactionDateBetweenIsAndPayeeMCCOrderByCreatedAtAsc(
                                    UUID.fromString(acctId), UUID.fromString(billId), fromDate, toDate, payeeMCC, pageable);
                }
            }
        }

        var response = new ResponseCreditCardAccountsTransactions().data(transactions.getContent()
                .stream()
                .map(CreditCardAccountsTransactionEntity::getDTO)
                .collect(Collectors.toList()));

        response.setMeta(BankLambdaUtils.getMeta(transactions, response.getData().size()));

        return response;
    }
}
