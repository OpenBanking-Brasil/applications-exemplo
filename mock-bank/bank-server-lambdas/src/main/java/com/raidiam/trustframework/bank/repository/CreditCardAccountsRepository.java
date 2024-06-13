package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.CreditCardAccountsEntity;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.annotation.EntityGraph;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditCardAccountsRepository extends PageableRepository<CreditCardAccountsEntity, UUID> {

    @EntityGraph(attributePaths = {"paymentMethods", "transactions", "bills", "bills.financeCharges", "bills.payments",
                                   "bills.transactions", "limits"})
    Optional<CreditCardAccountsEntity> findByCreditCardAccountId(@NotNull UUID accountId);

    @Join(value="accountHolder", type = Join.Type.FETCH)
    List<CreditCardAccountsEntity> findByAccountHolderUserId(@NotNull String userId);
}
