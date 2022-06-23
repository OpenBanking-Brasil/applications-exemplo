package com.raidiam.trustframework.bank.repository;

import com.raidiam.trustframework.bank.domain.AccountHolderEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountHolderRepository extends PageableRepository<AccountHolderEntity, Integer> {

    Optional<AccountHolderEntity> findByAccountHolderId(@NotNull UUID userId);

    List<AccountHolderEntity> findByDocumentIdentificationAndDocumentRel(@NotNull String documentId, @NotNull String documentRel);

}
