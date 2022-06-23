package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.CreditCardAccountsTransactionEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface CreditCardAccountsTransactionRepository extends PageableRepository<CreditCardAccountsTransactionEntity, UUID> {

    Page<CreditCardAccountsTransactionEntity> findByCreditCardAccountIdAndBillIdAndTransactionDateBetweenIsAndTransactionTypeAndPayeeMCCOrderByCreatedAtAsc(@NotNull UUID accountId,
                                                                                                                                                            @NotNull UUID billId,
                                                                                                                                                            @NotNull LocalDate from,
                                                                                                                                                            @NotNull LocalDate to,
                                                                                                                                                            @NotNull String transactionType,
                                                                                                                                                            @NotNull BigDecimal payeeMCC,
                                                                                                                                                            Pageable pageable);

    Page<CreditCardAccountsTransactionEntity> findByCreditCardAccountIdAndTransactionDateBetweenIsAndTransactionTypeAndPayeeMCCOrderByCreatedAtAsc(@NotNull UUID accountId,
                                                                                                                                                   @NotNull LocalDate from,
                                                                                                                                                   @NotNull LocalDate to,
                                                                                                                                                   @NotNull String transactionType,
                                                                                                                                                   @NotNull BigDecimal payeeMCC,
                                                                                                                                                   Pageable pageable);

    Page<CreditCardAccountsTransactionEntity> findByCreditCardAccountIdAndBillIdAndTransactionDateBetweenIsAndTransactionTypeOrderByCreatedAtAsc(@NotNull UUID accountId,
                                                                                                                                                 @NotNull UUID billId,
                                                                                                                                                 @NotNull LocalDate from,
                                                                                                                                                 @NotNull LocalDate to,
                                                                                                                                                 @NotNull String transactionType,
                                                                                                                                                 Pageable pageable);

    Page<CreditCardAccountsTransactionEntity> findByCreditCardAccountIdAndBillIdAndTransactionDateBetweenIsAndPayeeMCCOrderByCreatedAtAsc(@NotNull UUID accountId,
                                                                                                                                          @NotNull UUID billId,
                                                                                                                                          @NotNull LocalDate from,
                                                                                                                                          @NotNull LocalDate to,
                                                                                                                                          @NotNull BigDecimal payeeMCC,
                                                                                                                                          Pageable pageable);

    Page<CreditCardAccountsTransactionEntity> findByCreditCardAccountIdAndTransactionDateBetweenIsAndTransactionTypeOrderByCreatedAtAsc(@NotNull UUID accountId,
                                                                                                                                        @NotNull LocalDate from,
                                                                                                                                        @NotNull LocalDate to,
                                                                                                                                        @NotNull String transactionType,
                                                                                                                                        Pageable pageable);

    Page<CreditCardAccountsTransactionEntity> findByCreditCardAccountIdAndTransactionDateBetweenIsAndPayeeMCCOrderByCreatedAtAsc(@NotNull UUID accountId,
                                                                                                                                 @NotNull LocalDate from,
                                                                                                                                 @NotNull LocalDate to,
                                                                                                                                 @NotNull BigDecimal payeeMCC,
                                                                                                                                 Pageable pageable);

    Page<CreditCardAccountsTransactionEntity> findByCreditCardAccountIdAndBillIdAndTransactionDateBetweenIsOrderByCreatedAtAsc(@NotNull UUID accountId,
                                                                                                                               @NotNull UUID billId,
                                                                                                                               @NotNull LocalDate from,
                                                                                                                               @NotNull LocalDate to,
                                                                                                                               Pageable pageable);

    Page<CreditCardAccountsTransactionEntity> findByCreditCardAccountIdAndTransactionDateBetweenIsOrderByCreatedAtAsc(@NotNull UUID accountId,
                                                                                                                      @NotNull LocalDate from,
                                                                                                                      @NotNull LocalDate to,
                                                                                                                      Pageable pageable);
}
