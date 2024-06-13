package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.domain.*;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
@Transactional
public class InvestmentService extends BaseBankService {

    private static final Logger LOG = LoggerFactory.getLogger(InvestmentService.class);
    public ResponseBankFixedIncomesProductList getBankFixedIncomesList(Pageable pageable, @NotNull String consentId) {
        LOG.info("Getting bank fixed incomes list for consent id {}", consentId);
        checkConsentIsAuthorisedAndHasPermission(consentId, EnumConsentPermissions.BANK_FIXED_INCOMES_READ);

        var bankFixedIncomesList = bankFixedIncomesRepository.findAll(pageable);
        return new ResponseBankFixedIncomesProductList()
                .data(bankFixedIncomesList.getContent()
                        .stream()
                        .map(BankFixedIncomesEntity::getBankFixedIncomesListData)
                        .collect(Collectors.toList()))
                .meta(BankLambdaUtils.getMeta(bankFixedIncomesList));
    }

    public ResponseBankFixedIncomesProductIdentification getBankFixedIncomesById(String consentId, UUID investmentId) {
        LOG.info("Getting bank fixed incomes by id for consent id {} and investment id {}", consentId, investmentId);

        checkConsentIsAuthorisedAndHasPermission(consentId, EnumConsentPermissions.BANK_FIXED_INCOMES_READ);

        var bankFixedIncomeEntity = getBankFixedIncomes(investmentId);
        return new ResponseBankFixedIncomesProductIdentification().data(bankFixedIncomeEntity.getBankFixedIncomesProductIdentificationData());
    }

    public ResponseBankFixedIncomesBalances getBankFixedIncomesBalance(String consentId, UUID investmentId) {
        LOG.info("Getting bank fixed incomes balances response for consent id {} and investment id {}", consentId, investmentId);

        checkConsentIsAuthorisedAndHasPermission(consentId, EnumConsentPermissions.BANK_FIXED_INCOMES_READ);

        var bankFixedIncome = getBankFixedIncomes(investmentId);
        var balances = bankFixedIncomesBalancesRepository.findByInvestment(bankFixedIncome);
        if(balances.isPresent()) {
            return new ResponseBankFixedIncomesBalances().data(balances.get().getBankFixedIncomesBalancesData());
        }
        return new ResponseBankFixedIncomesBalances().data(new ResponseBankFixedIncomesBalancesData());
    }

    public ResponseBankFixedIncomesTransactions getBankFixedIncomesTransactions(String consentId, UUID investmentId, LocalDate fromTransationDate, LocalDate toTransactionDate, Pageable pageable) {
        LOG.info("Getting bank fixed incomes transactions for consent id {} and investment id {}", consentId, investmentId);
        checkConsentIsAuthorisedAndHasPermission(consentId, EnumConsentPermissions.BANK_FIXED_INCOMES_READ);
        var transactions = bankFixedIncomesTransactionsRepository.findByInvestmentIdAndTransactionDateBetween(investmentId, fromTransationDate, toTransactionDate, pageable);
        return new ResponseBankFixedIncomesTransactions()
                .data(transactions.getContent()
                        .stream()
                        .map(BankFixedIncomesTransactionsEntity::getBankFixedIncomesTransactionData)
                        .collect(Collectors.toList()))
                .meta(BankLambdaUtils.getMeta(transactions));
    }

    private void checkConsentIsAuthorisedAndHasPermission(String consentId, EnumConsentPermissions permission) {
        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, permission);
    }
    private BankFixedIncomesEntity getBankFixedIncomes(UUID investmentId) {
        return bankFixedIncomesRepository.findByInvestmentId(investmentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Could not find credit fixed incomes"));
    }

    private CreditFixedIncomesEntity getCreditFixedIncomes(UUID investmentId) {
        return creditFixedIncomesRepository.findByInvestmentId(investmentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Could not find credit fixed incomes"));
    }

    public ResponseCreditFixedIncomesProductList getCreditFixedIncomesList(Pageable pageable, @NotNull String consentId) {
        LOG.info("Getting credit fixed incomes list for consent id {}", consentId);
        checkConsentIsAuthorisedAndHasPermission(consentId, EnumConsentPermissions.CREDIT_FIXED_INCOMES_READ);

        var creditFixedIncomesList = creditFixedIncomesRepository.findAll(pageable);
        return new ResponseCreditFixedIncomesProductList()
                .data(creditFixedIncomesList.getContent()
                        .stream()
                        .map(CreditFixedIncomesEntity::getCreditFixedList)
                        .collect(Collectors.toList()))
                .meta(BankLambdaUtils.getMeta(creditFixedIncomesList));
    }

    public ResponseCreditFixedIncomesProductIdentification getCreditFixedIncomesById(String consentId, UUID investmentId) {
        LOG.info("Getting credit fixed incomes by id for consent id {} and investment id {}", consentId, investmentId);

        checkConsentIsAuthorisedAndHasPermission(consentId, EnumConsentPermissions.CREDIT_FIXED_INCOMES_READ);

        var creditFixedIncomeEntity = getCreditFixedIncomes(investmentId);
        return new ResponseCreditFixedIncomesProductIdentification().data(creditFixedIncomeEntity.getCreditFixedIncomesProductIdentification());
    }

    public ResponseCreditFixedIncomesBalances getCreditFixedIncomesBalance(String consentId, UUID investmentId) {
        LOG.info("Getting credit fixed incomes balance for consent id {} and investment id {}", consentId, investmentId);

        checkConsentIsAuthorisedAndHasPermission(consentId, EnumConsentPermissions.CREDIT_FIXED_INCOMES_READ);

        var creditFixedIncome = getCreditFixedIncomes(investmentId);
        var balances = creditFixedIncomesBalancesRepository.findByInvestment(creditFixedIncome);
        if(balances.isPresent()) {
            return new ResponseCreditFixedIncomesBalances().data(balances.get().getCreditFixedIncomesBalancesData());
        }
        return new ResponseCreditFixedIncomesBalances().data(new ResponseCreditFixedIncomesBalancesData());
    }

    public ResponseCreditFixedIncomesTransactions getCreditFixedIncomesTransactions(String consentId, UUID investmentId, LocalDate fromTransationDate, LocalDate toTransactionDate, Pageable pageable) {
        LOG.info("Getting credit fixed incomes transactions for consent id {} and investment id {}", consentId, investmentId);
        checkConsentIsAuthorisedAndHasPermission(consentId, EnumConsentPermissions.CREDIT_FIXED_INCOMES_READ);
        var transactions = creditFixedIncomesTransactionsRepository.findByInvestmentIdAndTransactionDateBetween(investmentId, fromTransationDate, toTransactionDate, pageable);
        return new ResponseCreditFixedIncomesTransactions()
                .data(transactions.getContent()
                        .stream()
                        .map(CreditFixedIncomesTransactionsEntity::getCreditFixedIncomesTransactions)
                        .collect(Collectors.toList()))
                .meta(BankLambdaUtils.getMeta(transactions));
    }


    private FundsEntity getFunds(UUID investmentId) {
    return fundsRepository.findByInvestmentId(investmentId)
            .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Could not find funds"));
}

    public ResponseFundsProductList getFundsList(Pageable pageable, @NotNull String consentId) {
        LOG.info("Getting funds list for consent id {}", consentId);
        checkConsentIsAuthorisedAndHasPermission(consentId, EnumConsentPermissions.FUNDS_READ);

        var fundsList = fundsRepository.findAll(pageable);
        return new ResponseFundsProductList()
                .data(fundsList.getContent()
                        .stream()
                        .map(FundsEntity::getResponseFundsProductListData)
                        .collect(Collectors.toList()))
                .meta(BankLambdaUtils.getMeta(fundsList));
    }

    public ResponseFundsProductIdentification getFundsById(String consentId, UUID investmentId) {
        LOG.info("Getting funds by id for consent id {} and investment id {}", consentId, investmentId);

        checkConsentIsAuthorisedAndHasPermission(consentId, EnumConsentPermissions.FUNDS_READ);

        var fundsEntity = getFunds(investmentId);
        return new ResponseFundsProductIdentification().data(fundsEntity.getResponseFundsProductIdentificationData());
    }

    public ResponseFundsBalances getFundsBalance(String consentId, UUID investmentId) {
        LOG.info("Getting funds balance for consent id {} and investment id {}", consentId, investmentId);

        checkConsentIsAuthorisedAndHasPermission(consentId, EnumConsentPermissions.FUNDS_READ);

        var funds = getFunds(investmentId);
        var balances = fundsBalancesRepository.findByInvestment(funds);
        if(balances.isPresent()) {
            return new ResponseFundsBalances().data(balances.get().getResponseFundsBalancesData());
        }
        return new ResponseFundsBalances().data(new ResponseFundsBalancesData());
    }

    public ResponseFundsTransactions getFundsTransactions(String consentId, UUID investmentId, LocalDate fromTransationDate, LocalDate toTransactionDate, Pageable pageable) {
        LOG.info("Getting funds transactions for consent id {} and investment id {}", consentId, investmentId);
        checkConsentIsAuthorisedAndHasPermission(consentId, EnumConsentPermissions.FUNDS_READ);
        var transactions = fundsTransactionsRepository.findByInvestmentIdAndTransactionConversionDateBetween(investmentId, fromTransationDate, toTransactionDate, pageable);
        return new ResponseFundsTransactions()
                .data(transactions.getContent()
                        .stream()
                        .map(FundsTransactionsEntity::getResponseFundsTransactionsData)
                        .collect(Collectors.toList()))
                .meta(BankLambdaUtils.getMeta(transactions));
    }


    private TreasureTitlesEntity getTreasureTitles(UUID investmentId) {
        return treasureTitlesRepository.findByInvestmentId(investmentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Could not find treasure titles"));
    }

    public ResponseTreasureTitlesProductList getTreasureTitlesList(Pageable pageable, @NotNull String consentId) {
        LOG.info("Getting treasure title lsit for consent id {}", consentId);
        checkConsentIsAuthorisedAndHasPermission(consentId, EnumConsentPermissions.TREASURE_TITLES_READ);

        var treasureTitlesList = treasureTitlesRepository.findAll(pageable);
        return new ResponseTreasureTitlesProductList()
                .data(treasureTitlesList.getContent()
                        .stream()
                        .map(TreasureTitlesEntity::getResponseTreasureTitlesProductListData)
                        .collect(Collectors.toList()))
                .meta(BankLambdaUtils.getMeta(treasureTitlesList));
    }

    public ResponseTreasureTitlesProductIdentification getTreasureTitlesById(String consentId, UUID investmentId) {
        LOG.info("Getting treasure titles by id for consent id {} and investment id {}", consentId, investmentId);

        checkConsentIsAuthorisedAndHasPermission(consentId, EnumConsentPermissions.TREASURE_TITLES_READ);

        var treasureTitlesEntity = getTreasureTitles(investmentId);
        return new ResponseTreasureTitlesProductIdentification().data(treasureTitlesEntity.getResponseTreasureTitlesProductIdentificationData());
    }

    public ResponseTreasureTitlesBalances getTreasureTitlesBalance(String consentId, UUID investmentId) {
        LOG.info("Getting treasure titles balance for consent id {} and investment id {}", consentId, investmentId);

        checkConsentIsAuthorisedAndHasPermission(consentId, EnumConsentPermissions.TREASURE_TITLES_READ);

        var treasureTitles = getTreasureTitles(investmentId);
        var balances = treasureTitlesBalancesRepository.findByInvestment(treasureTitles);
        if (balances.isPresent()) {
            return new ResponseTreasureTitlesBalances().data(balances.get().getResponseTreasureTitlesBalancesData());
        }
        return new ResponseTreasureTitlesBalances().data(new ResponseTreasureTitlesBalancesData());
    }

    public ResponseTreasureTitlesTransactions getTreasureTitlesTransactions(String consentId, UUID investmentId, LocalDate fromTransationDate, LocalDate toTransactionDate, Pageable pageable) {
        LOG.info("Getting treasure titles transactions for consent id {} and investment id {}", consentId, investmentId);
        checkConsentIsAuthorisedAndHasPermission(consentId, EnumConsentPermissions.TREASURE_TITLES_READ);
        var transactions = treasureTitlesTransactionsRepository.findByInvestmentIdAndTransactionDateBetween(investmentId, fromTransationDate, toTransactionDate, pageable);
        return new ResponseTreasureTitlesTransactions()
                .data(transactions.getContent()
                        .stream()
                        .map(TreasureTitlesTransactionsEntity::getResponseTreasureTitlesTransactionsData)
                        .collect(Collectors.toList()))
                .meta(BankLambdaUtils.getMeta(transactions));
    }



    private VariableIncomesEntity getVariableIncomes(UUID investmentId) {
        return variableIncomesRepository.findByInvestmentId(investmentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Could not find variable incomes"));
    }

    public ResponseVariableIncomesProductList getVariableIncomesList(Pageable pageable, @NotNull String consentId) {
        LOG.info("Getting variable incomes list for consent id {}", consentId);
        checkConsentIsAuthorisedAndHasPermission(consentId, EnumConsentPermissions.VARIABLE_INCOMES_READ);

        var variableIncomesList = variableIncomesRepository.findAll(pageable);
        return new ResponseVariableIncomesProductList()
                .data(variableIncomesList.getContent()
                        .stream()
                        .map(VariableIncomesEntity::getResponseVariableIncomesProductListData)
                        .collect(Collectors.toList()))
                .meta(BankLambdaUtils.getMeta(variableIncomesList));
    }

    public ResponseVariableIncomesProductIdentification getVariableIncomesById(String consentId, UUID investmentId) {
        LOG.info("Getting variable incomes by id for consent id {} and investment id {}", consentId, investmentId);

        checkConsentIsAuthorisedAndHasPermission(consentId, EnumConsentPermissions.VARIABLE_INCOMES_READ);

        var variableIncomesEntity = getVariableIncomes(investmentId);
        return new ResponseVariableIncomesProductIdentification().data(variableIncomesEntity.getResponseVariableIncomesProductIdentificationData());
    }

    public ResponseVariableIncomesBalance getVariableIncomesBalance(String consentId, UUID investmentId) {
        LOG.info("Getting variable incomes balance for consent id {} and investment id {}", consentId, investmentId);

        checkConsentIsAuthorisedAndHasPermission(consentId, EnumConsentPermissions.VARIABLE_INCOMES_READ);

        var variableIncomes = getVariableIncomes(investmentId);
        var balances = variableIncomesBalancesRepository.findByInvestment(variableIncomes);
        if (balances.isPresent()) {
            return new ResponseVariableIncomesBalance().data(List.of(balances.get().getResponseVariableIncomesBalanceData()));
        }
        return new ResponseVariableIncomesBalance().data(List.of(new ResponseVariableIncomesBalanceData()));
    }

    public ResponseVariableIncomesTransactions getVariableIncomesTransactions(String consentId, UUID investmentId, LocalDate fromTransactionDate, LocalDate toTransactionDate, Pageable pageable) {
        LOG.info("Getting variable incomes transactions for consent id {} and investment id {}", consentId, investmentId);
        checkConsentIsAuthorisedAndHasPermission(consentId, EnumConsentPermissions.VARIABLE_INCOMES_READ);
        var transactions = variableIncomesTransactionsRepository.findByInvestmentIdAndTransactionDateBetween(investmentId, fromTransactionDate, toTransactionDate, pageable);
        return new ResponseVariableIncomesTransactions().data(transactions.getContent()
                        .stream()
                        .map(VariableIncomesTransactionsEntity::getResponseVariableIncomesTransactionsData)
                        .collect(Collectors.toList()))
                .meta(BankLambdaUtils.getMeta(transactions));
    }

    public ResponseVariableIncomesBroker getVariableIncomesBroker(String consentId, UUID brokerNoteId) {
        LOG.info("Getting variable incomes broker notes for consent id {}", consentId);

        checkConsentIsAuthorisedAndHasPermission(consentId, EnumConsentPermissions.VARIABLE_INCOMES_READ);

        var brokerNotes = variableIncomesBrokerNotesRepository.findByBrokerNoteId(brokerNoteId);
        if (brokerNotes.isPresent()) {
            return new ResponseVariableIncomesBroker().data(brokerNotes.get().getResponseVariableIncomesBrokerData());
        }
        return new ResponseVariableIncomesBroker().data(new ResponseVariableIncomesBrokerData());
    }
}
