package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.CreditCardAccountsTransactionEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public interface CreditCardAccountsTransactionRepository extends PageableRepository<CreditCardAccountsTransactionEntity, UUID> {

    Page<CreditCardAccountsTransactionEntity> findByCreditCardAccountIdAndBillIdAndTransactionDateTimeBetweenAndTransactionTypeAndPayeeMCCOrderByCreatedAtAsc(@NotNull UUID accountId,
                                                                                                                                                            @NotNull UUID billId,
                                                                                                                                                            @NotNull OffsetDateTime from,
                                                                                                                                                            @NotNull OffsetDateTime to,
                                                                                                                                                            @NotNull String transactionType,
                                                                                                                                                            @NotNull BigDecimal payeeMCC,
                                                                                                                                                            Pageable pageable);

    Page<CreditCardAccountsTransactionEntity> findByCreditCardAccountIdAndTransactionDateTimeBetweenAndTransactionTypeAndPayeeMCCOrderByCreatedAtAsc(@NotNull UUID accountId,
                                                                                                                                                   @NotNull OffsetDateTime from,
                                                                                                                                                   @NotNull OffsetDateTime to,
                                                                                                                                                   @NotNull String transactionType,
                                                                                                                                                   @NotNull BigDecimal payeeMCC,
                                                                                                                                                   Pageable pageable);

    Page<CreditCardAccountsTransactionEntity> findByCreditCardAccountIdAndBillIdAndTransactionDateTimeBetweenAndTransactionTypeOrderByCreatedAtAsc(@NotNull UUID accountId,
                                                                                                                                                 @NotNull UUID billId,
                                                                                                                                                 @NotNull OffsetDateTime from,
                                                                                                                                                 @NotNull OffsetDateTime to,
                                                                                                                                                 @NotNull String transactionType,
                                                                                                                                                 Pageable pageable);

    Page<CreditCardAccountsTransactionEntity> findByCreditCardAccountIdAndBillIdAndTransactionDateTimeBetweenAndPayeeMCCOrderByCreatedAtAsc(@NotNull UUID accountId,
                                                                                                                                          @NotNull UUID billId,
                                                                                                                                          @NotNull OffsetDateTime from,
                                                                                                                                          @NotNull OffsetDateTime to,
                                                                                                                                          @NotNull BigDecimal payeeMCC,
                                                                                                                                          Pageable pageable);

    Page<CreditCardAccountsTransactionEntity> findByCreditCardAccountIdAndTransactionDateTimeBetweenAndTransactionTypeOrderByCreatedAtAsc(@NotNull UUID accountId,
                                                                                                                                        @NotNull OffsetDateTime from,
                                                                                                                                        @NotNull OffsetDateTime to,
                                                                                                                                        @NotNull String transactionType,
                                                                                                                                        Pageable pageable);

    Page<CreditCardAccountsTransactionEntity> findByCreditCardAccountIdAndTransactionDateTimeBetweenAndPayeeMCCOrderByCreatedAtAsc(@NotNull UUID accountId,
                                                                                                                                 @NotNull OffsetDateTime from,
                                                                                                                                 @NotNull OffsetDateTime to,
                                                                                                                                 @NotNull BigDecimal payeeMCC,
                                                                                                                                 Pageable pageable);

    Page<CreditCardAccountsTransactionEntity> findByCreditCardAccountIdAndBillIdAndTransactionDateTimeBetweenOrderByCreatedAtAsc(@NotNull UUID accountId,
                                                                                                                               @NotNull UUID billId,
                                                                                                                               @NotNull OffsetDateTime from,
                                                                                                                               @NotNull OffsetDateTime to,
                                                                                                                               Pageable pageable);

    Page<CreditCardAccountsTransactionEntity> findByCreditCardAccountIdAndTransactionDateTimeBetweenOrderByCreatedAtAsc(@NotNull UUID accountId,
                                                                                                                      @NotNull OffsetDateTime from,
                                                                                                                      @NotNull OffsetDateTime to,
                                                                                                                      Pageable pageable);
}
