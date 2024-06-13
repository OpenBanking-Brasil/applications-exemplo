package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.domain.*;
import com.raidiam.trustframework.bank.repository.CreditCardAccountsRepository;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.context.annotation.Value;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
@Transactional
public class CreditCardAccountsService extends BaseBankService {

    @Value("${mockbank.mockbankUrl}")
    protected String appBaseUrl;

    @Value("${mockbank.max-page-size}")
    protected int maxPageSize;

    private static final Logger LOG = LoggerFactory.getLogger(CreditCardAccountsService.class);

    CreditCardAccountsService(CreditCardAccountsRepository creditCardAccountsRepository) {
        this.creditCardAccountsRepository = creditCardAccountsRepository;
    }

    public ResponseCreditCardAccountsList getCreditCardAccounts(Pageable pageable, String consentId) {
        LOG.info("Getting accounts response for consent id {}", consentId);
        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, EnumConsentPermissions.CREDIT_CARDS_ACCOUNTS_READ);

        var consentCreditCardAccount = consentCreditCardAccountsRepository.findByConsentIdOrderByCreatedAtAsc(consentId, pageable);
        BankLambdaUtils.checkConsentOwnerIsCreditCardAccountOwner(consentCreditCardAccount, consentEntity);
        var response = new ResponseCreditCardAccountsList().data(consentCreditCardAccount.getContent()
                .stream()
                .map(ConsentCreditCardAccountsEntity::getCreditCardAccount)
                .map(CreditCardAccountsEntity::getCreditCardAccountsData)
                .collect(Collectors.toList()));

        response.setMeta(BankLambdaUtils.getMeta(consentCreditCardAccount));

        return response;
    }

    public ResponseCreditCardAccountsIdentification getCreditCardAccount(String consentId, String acctId) {
        LOG.info("Getting credit card account response for consent id {} and account id {}", consentId, acctId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var accountEntity = BankLambdaUtils.getCreditCardAccount(acctId, creditCardAccountsRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, EnumConsentPermissions.CREDIT_CARDS_ACCOUNTS_READ);
        BankLambdaUtils.checkConsentCoversCreditCardAccount(consentEntity, accountEntity);
        BankLambdaUtils.checkConsentOwnerIsCreditCardAccountOwner(consentEntity, accountEntity);

        return new ResponseCreditCardAccountsIdentification().data(accountEntity.getCreditCardsAccountsIdentificationData());
    }

    public ResponseCreditCardAccountsLimits getCreditCardAccountLimits(String consentId, String acctId) {
        LOG.info("Getting account Credit Card Limits response for consent id {} and account id {} v1", consentId, acctId);
        var accountEntity = getCreditCardAccount(consentId, acctId, EnumConsentPermissions.CREDIT_CARDS_ACCOUNTS_LIMITS_READ);
        return new ResponseCreditCardAccountsLimits().data(accountEntity.getCreditCardAccountsLimitsData());
    }

    public ResponseCreditCardAccountsLimitsV2 getCreditCardAccountLimitsV2(String consentId, String acctId) {
        LOG.info("Getting account Credit Card Limits response for consent id {} and account id {} v2", consentId, acctId);
        var accountEntity = getCreditCardAccount(consentId, acctId, EnumConsentPermissions.CREDIT_CARDS_ACCOUNTS_LIMITS_READ);
        return new ResponseCreditCardAccountsLimitsV2().data(accountEntity.getCreditCardAccountsLimitsDataV2());
    }

    private CreditCardAccountsEntity getCreditCardAccount(String consentId, String acctId, EnumConsentPermissions permission) {
        LOG.info("Getting account Credit Card for consent id {} and account id {}", consentId, acctId);

        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        var accountEntity = BankLambdaUtils.getCreditCardAccount(acctId, creditCardAccountsRepository);

        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, permission);
        BankLambdaUtils.checkConsentCoversCreditCardAccount(consentEntity, accountEntity);
        BankLambdaUtils.checkConsentOwnerIsCreditCardAccountOwner(consentEntity, accountEntity);

        return accountEntity;
    }

    public ResponseCreditCardAccountsBills getCreditCardAccountsBills(Pageable pageable, String consentId,
                                                                      LocalDate fromDate, LocalDate toDate, String acctId) {
        LOG.info("Getting account Credit Card bills response for consent id {} and account id {} v1", consentId, acctId);
        var accountEntity = getCreditCardAccount(consentId, acctId, EnumConsentPermissions.CREDIT_CARDS_ACCOUNTS_BILLS_READ);
        var bills = creditCardAccountsBillsRepository
                .findByAccountAndDueDateBetweenOrderByCreatedAtAsc(accountEntity, fromDate, toDate, pageable);

        var response = new ResponseCreditCardAccountsBills().data(bills.getContent()
                .stream()
                .map(CreditCardAccountsBillsEntity::getDTO)
                .collect(Collectors.toList()));

        response.setMeta(BankLambdaUtils.getMeta(bills));

        return response;
    }

    public ResponseCreditCardAccountsBillsV2 getCreditCardAccountsBillsV2(Pageable pageable, String consentId,
                                                                          LocalDate fromDate, LocalDate toDate, String acctId) {
        LOG.info("Getting account Credit Card bills response for consent id {} and account id {} v2", consentId, acctId);
        var accountEntity = getCreditCardAccount(consentId, acctId, EnumConsentPermissions.CREDIT_CARDS_ACCOUNTS_BILLS_READ);
        var bills = creditCardAccountsBillsRepository
                .findByAccountAndDueDateBetweenOrderByCreatedAtAsc(accountEntity, fromDate, toDate, pageable);

        var response = new ResponseCreditCardAccountsBillsV2().data(bills.getContent()
                .stream()
                .map(CreditCardAccountsBillsEntity::getDtoV2)
                .collect(Collectors.toList()));

        response.setMeta(BankLambdaUtils.getMeta(bills));

        return response;
    }

    public ResponseCreditCardAccountsBillsTransactionsV2 getBillsTransactionsV2(Pageable pageable, String consentId,
                                                                                LocalDate fromDate, LocalDate toDate,
                                                                                BigDecimal payeeMCC, String transactionType,
                                                                                String acctId, String billId) {
        LOG.info("Getting account Credit Card Bills Transaction response for consent id {} and account id {} and bill id {} v2", consentId, acctId, billId);
        var accountEntity = getCreditCardAccount(consentId, acctId, EnumConsentPermissions.CREDIT_CARDS_ACCOUNTS_BILLS_TRANSACTIONS_READ);
        var billEntity = BankLambdaUtils.getCreditCardAccountBill(billId, creditCardAccountsBillsRepository);
        BankLambdaUtils.checkBillOwnedCreditCardAccount(billEntity, accountEntity);

        var from = BankLambdaUtils.localDateToOffsetDate(fromDate).withHour(0).withMinute(1);
        var to = BankLambdaUtils.localDateToOffsetDate(toDate).withHour(23).withMinute(59);

        Page<CreditCardAccountsTransactionEntity> transactions = findBillsTransactions(pageable, from, to, payeeMCC, transactionType, acctId, billId);

        var response = new ResponseCreditCardAccountsBillsTransactionsV2().data(transactions.getContent()
                .stream()
                .map(CreditCardAccountsTransactionEntity::getDtoV2)
                .collect(Collectors.toList()));

        response.setMeta(BankLambdaUtils.getMeta(transactions));

        return response;
    }

    public ResponseCreditCardAccountsTransactionsV2 getTransactionsV2(Pageable pageable, String consentId,
                                                                       LocalDate fromDate, LocalDate toDate,
                                                                       BigDecimal payeeMCC, String transactionType,
                                                                       String acctId) {
        LOG.info("Getting credit card account transactions response for consent id {} and account id {} v2", consentId, acctId);

        getCreditCardAccount(consentId, acctId, EnumConsentPermissions.CREDIT_CARDS_ACCOUNTS_TRANSACTIONS_READ);
        var from = BankLambdaUtils.localDateToOffsetDate(fromDate).withHour(0).withMinute(1);
        var to = BankLambdaUtils.localDateToOffsetDate(toDate).withHour(23).withMinute(59);

        Page<CreditCardAccountsTransactionEntity> transactions = findTransactions(pageable, from, to, payeeMCC, transactionType, acctId);

        return new ResponseCreditCardAccountsTransactionsV2().data(transactions.getContent()
                .stream()
                .map(CreditCardAccountsTransactionEntity::getDtoV2)
                .collect(Collectors.toList()))
                .meta(BankLambdaUtils.getMeta(transactions));
    }

    private Page<CreditCardAccountsTransactionEntity> findTransactions(Pageable pageable, OffsetDateTime fromDate, OffsetDateTime toDate,
                                                                       BigDecimal payeeMCC, String transactionType, String acctId) {
        Page<CreditCardAccountsTransactionEntity> transactions;
        if (transactionType != null && payeeMCC != null) {
            transactions = creditCardAccountsTransactionRepository
                    .findByCreditCardAccountIdAndTransactionDateTimeBetweenAndTransactionTypeAndPayeeMCCOrderByCreatedAtAsc(
                            UUID.fromString(acctId), fromDate, toDate, transactionType, payeeMCC, pageable);
        } else {
            if (transactionType == null && payeeMCC == null) {
                transactions = creditCardAccountsTransactionRepository
                        .findByCreditCardAccountIdAndTransactionDateTimeBetweenOrderByCreatedAtAsc(
                                UUID.fromString(acctId), fromDate, toDate, pageable);
            } else {
                if (transactionType != null) {
                    transactions = creditCardAccountsTransactionRepository
                            .findByCreditCardAccountIdAndTransactionDateTimeBetweenAndTransactionTypeOrderByCreatedAtAsc(
                                    UUID.fromString(acctId), fromDate, toDate, transactionType, pageable);
                } else {
                    transactions = creditCardAccountsTransactionRepository
                            .findByCreditCardAccountIdAndTransactionDateTimeBetweenAndPayeeMCCOrderByCreatedAtAsc(
                                    UUID.fromString(acctId), fromDate, toDate, payeeMCC, pageable);
                }
            }
        }
        return transactions;
    }

    private Page<CreditCardAccountsTransactionEntity> findBillsTransactions(Pageable pageable, OffsetDateTime fromDate, OffsetDateTime toDate,
                                                                            BigDecimal payeeMCC, String transactionType, String acctId, String billId) {
        Page<CreditCardAccountsTransactionEntity> transactions;

        if (transactionType != null && payeeMCC != null) {
            transactions = creditCardAccountsTransactionRepository
                    .findByCreditCardAccountIdAndBillIdAndTransactionDateTimeBetweenAndTransactionTypeAndPayeeMCCOrderByCreatedAtAsc(
                            UUID.fromString(acctId), UUID.fromString(billId), fromDate, toDate, transactionType, payeeMCC, pageable);
        } else {
            if (transactionType == null && payeeMCC == null) {
                transactions = creditCardAccountsTransactionRepository
                        .findByCreditCardAccountIdAndBillIdAndTransactionDateTimeBetweenOrderByCreatedAtAsc(
                                UUID.fromString(acctId), UUID.fromString(billId), fromDate, toDate, pageable);
            } else {
                if (transactionType != null) {
                    transactions = creditCardAccountsTransactionRepository
                            .findByCreditCardAccountIdAndBillIdAndTransactionDateTimeBetweenAndTransactionTypeOrderByCreatedAtAsc(
                                    UUID.fromString(acctId), UUID.fromString(billId), fromDate, toDate, transactionType, pageable);
                } else {
                    transactions = creditCardAccountsTransactionRepository
                            .findByCreditCardAccountIdAndBillIdAndTransactionDateTimeBetweenAndPayeeMCCOrderByCreatedAtAsc(
                                    UUID.fromString(acctId), UUID.fromString(billId), fromDate, toDate, payeeMCC, pageable);
                }
            }
        }
        return transactions;
    }

    /*Start Admin part*/
    public ResponseCreditCardAccount addCreditCardAccount(CreateCreditCardAccountData creditCard, String accountHolderId) {
        LOG.info("Add credit card account for holder id {}", accountHolderId);
        BankLambdaUtils.checkExistAccountHolder(accountHolderId, accountHolderRepository);
        return creditCardAccountsRepository.save(CreditCardAccountsEntity.from(creditCard, UUID.fromString(accountHolderId))).getAdminCreditCardAccountDto();
    }

    public ResponseCreditCardAccount updateCreditCardAccount(String creditCardAccountId, EditedCreditCardAccount creditCard) {
        LOG.info("Update credit card account for creditCard id {}", creditCardAccountId);
        var accountEntity = BankLambdaUtils.getCreditCardAccount(UUID.fromString(creditCardAccountId).toString(), creditCardAccountsRepository);
        return creditCardAccountsRepository.update(accountEntity.update(creditCard.getData())).getAdminCreditCardAccountDto();
    }

    public void deleteCreditCardAccount(String acctId) {
        LOG.info("Delete credit card id {}", acctId);
        var accountEntity = BankLambdaUtils.getCreditCardAccount(acctId, creditCardAccountsRepository);
        creditCardAccountsRepository.delete(accountEntity);
    }

    public ResponseCreditCardAccountLimits addCreditCardAccountLimit(String creditCardAccountId, CreateCreditCardAccountLimits limits) {
        LOG.info("Add limit for credit card id {}", creditCardAccountId);
        return saveAllCreditCardAccountLimit(creditCardAccountId, limits);
    }

    public ResponseCreditCardAccountLimits updateCreditCardAccountLimit(String creditCardAccountId, CreateCreditCardAccountLimits limits) {
        LOG.info("Update limits for credit card id {}", creditCardAccountId);
        var limitsList = creditCardAccountsLimitsRepository.findByCreditCardAccountId(UUID.fromString(creditCardAccountId));
        creditCardAccountsLimitsRepository.deleteAll(limitsList);
        return saveAllCreditCardAccountLimit(creditCardAccountId, limits);
    }

    public ResponseCreditCardAccountLimits saveAllCreditCardAccountLimit(String creditCardAccountId, CreateCreditCardAccountLimits limits) {
        LOG.info("Save All limits for credit card id {}", creditCardAccountId);
        var limitEntityList = limits.getData().stream()
                .map(l -> CreditCardAccountsLimitsEntity.from(UUID.fromString(creditCardAccountId), l))
                .collect(Collectors.toList());

        var limitsEntityIterable = creditCardAccountsLimitsRepository.saveAll(limitEntityList);
        return new ResponseCreditCardAccountLimits().data(StreamSupport.stream(limitsEntityIterable.spliterator(), false)
                .map(CreditCardAccountsLimitsEntity::getDTO)
                .collect(Collectors.toList()));
    }

    public void deleteCreditCardAccountLimit(String creditCardAccountId) {
        LOG.info("Delete limits for credit card id {}", creditCardAccountId);
        var limitsList = creditCardAccountsLimitsRepository.findByCreditCardAccountId(UUID.fromString(creditCardAccountId));
        creditCardAccountsLimitsRepository.deleteAll(limitsList);
    }

    public ResponseCreditCardAccountBill addCreditCardBill(String creditCardAccountId, CreateCreditCardAccountBill bill) {
        LOG.info("Add bill for credit card id {}", creditCardAccountId);
        var accountEntity = BankLambdaUtils.getCreditCardAccount(UUID.fromString(creditCardAccountId).toString(), creditCardAccountsRepository);
        var billEntity = CreditCardAccountsBillsEntity.from(accountEntity, bill.getData());
        accountEntity.getBills().add(billEntity);
        return creditCardAccountsBillsRepository.save(billEntity).getAdminCreditCardBillDto();
    }

    public ResponseCreditCardAccountBill updateCreditCardBill(String creditCardAccountId, String billId, CreateCreditCardAccountBill bill) {
        LOG.info("Update bill id {} for credit card id {}", billId, creditCardAccountId);
        var billEntity = BankLambdaUtils.getCreditCardAccountBill(billId, creditCardAccountsBillsRepository);
        return creditCardAccountsBillsRepository.update(billEntity.update(bill.getData())).getAdminCreditCardBillDto();
    }

    public void deleteCreditCardBill(String creditCardAccountId, String billId) {
        LOG.info("Delete bill id {} for credit card id {}", billId, creditCardAccountId);
        var billEntity = BankLambdaUtils.getCreditCardAccountBill(billId, creditCardAccountsBillsRepository);
        creditCardAccountsBillsRepository.delete(billEntity);
    }

    public ResponseCreditCardAccountTransactionList addCreditCardTransaction(String creditCardAccountId, String billId, CreateCreditCardAccountTransactionList transactions) {

        var transactionsIterable = saveCreditCardAccountTransaction(creditCardAccountId, billId, transactions);
        return new ResponseCreditCardAccountTransactionList().data(StreamSupport.stream(transactionsIterable.spliterator(), false)
                .map(CreditCardAccountsTransactionEntity::getAdminCreditCardTransactionDto)
                .collect(Collectors.toList()));
    }

    public ResponseCreditCardAccountsTransactionListV2 addCreditCardTransactionV2(String creditCardAccountId, String billId, CreateCreditCardAccountTransactionList transactions) {
        var transactionsIterable = saveCreditCardAccountTransaction(creditCardAccountId, billId, transactions);
        return new ResponseCreditCardAccountsTransactionListV2().data(StreamSupport.stream(transactionsIterable.spliterator(), false)
                .map(CreditCardAccountsTransactionEntity::getDtoV2)
                .collect(Collectors.toList()));
    }

    private Iterable<CreditCardAccountsTransactionEntity> saveCreditCardAccountTransaction(String creditCardAccountId, String billId, CreateCreditCardAccountTransactionList transactions) {
        LOG.info("Add transaction for bill id {}", billId);
        var transactionsEntity = transactions.getData().stream()
                .map(t -> CreditCardAccountsTransactionEntity.from(UUID.fromString(creditCardAccountId), UUID.fromString(billId), t))
                .collect(Collectors.toList());

        return creditCardAccountsTransactionRepository.saveAll(transactionsEntity);
    }

    public ResponseCreditCardAccountTransaction updateCreditCardTransaction(String creditCardAccountId, String billId, String transactionId, EditedCreditCardAccountTransaction transaction) {
        LOG.info("Update transaction id {} for bill id {} and credit card id {}", transactionId, billId, creditCardAccountId);
        var accountTransaction = BankLambdaUtils.getCreditCardAccountsTransaction(transactionId, creditCardAccountsTransactionRepository);
        return new ResponseCreditCardAccountTransaction().data(creditCardAccountsTransactionRepository.update(accountTransaction.update(transaction.getData())).getAdminCreditCardTransactionDto());
    }

    public void deleteCreditCardTransaction(String creditCardAccountId, String billId, String transactionId) {
        LOG.info("Delete transaction id {} for bill id {} and credit card id {}", transactionId, billId, creditCardAccountId);
        var accountTransaction = BankLambdaUtils.getCreditCardAccountsTransaction(transactionId, creditCardAccountsTransactionRepository);
        creditCardAccountsTransactionRepository.delete(accountTransaction);
    }
}
