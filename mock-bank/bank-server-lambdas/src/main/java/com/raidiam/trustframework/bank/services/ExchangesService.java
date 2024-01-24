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
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
@Transactional
public class ExchangesService extends BaseBankService {

    private static final Logger LOG = LoggerFactory.getLogger(ExchangesService.class);
    public ResponseExchangesProductList getOperations(Pageable pageable, @NotNull String consentId) {
        LOG.info("Getting operations list for consent id {}", consentId);
        checkConsentIsAuthorisedAndHasPermission(consentId);

        var exchangesOperationList = exchangesOperationRepository.findAll(pageable);
        return new ResponseExchangesProductList()
                .data(exchangesOperationList.getContent()
                        .stream()
                        .map(ExchangesOperationEntity::getV1Data)
                        .collect(Collectors.toList()))
                .meta(BankLambdaUtils.getMeta(exchangesOperationList));
    }

    public ResponseExchangesOperationDetails getOperationsByOperationId(String operationId, @NotNull String consentId) {
        LOG.info("Getting operations list for consent id {}", consentId);
        checkConsentIsAuthorisedAndHasPermission(consentId);

        var exchangesOperationEntity = exchangesOperationRepository.findByOperationId(UUID.fromString(operationId))
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Could not find exchange operation"));

        return new ResponseExchangesOperationDetails()
                .data(exchangesOperationEntity.getV1DetailsData());
    }

    public ResponseExchangesEvents getEventsByOperationId(String operationId, @NotNull String consentId, Pageable pageable) {
        LOG.info("Getting events list for consent id {} and operation id {}", consentId, operationId);
        checkConsentIsAuthorisedAndHasPermission(consentId);

        var exchangesOperationEventList = exchangesOperationEventRepository.findAllByOperationId(UUID.fromString(operationId), pageable);

        return new ResponseExchangesEvents()
                .data(exchangesOperationEventList.getContent()
                        .stream()
                        .map(ExchangesOperationEventEntity::getV1Data)
                        .collect(Collectors.toList()))
                .meta(BankLambdaUtils.getMeta(exchangesOperationEventList));
    }

    private void checkConsentIsAuthorisedAndHasPermission(String consentId) {
        var consentEntity = BankLambdaUtils.getConsent(consentId, consentRepository);
        BankLambdaUtils.checkAuthorisationStatus(consentEntity);
        BankLambdaUtils.checkConsentPermissions(consentEntity, EnumConsentPermissions.EXCHANGES_READ);
    }
}
