package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.domain.*;
import com.raidiam.trustframework.bank.enums.ErrorCodesEnumV2;
import com.raidiam.trustframework.bank.services.automation.PaymentAutomations;
import com.raidiam.trustframework.bank.services.automation.PaymentAutomationsV2;
import com.raidiam.trustframework.bank.services.message.PaymentErrorMessage;
import com.raidiam.trustframework.bank.services.message.PaymentErrorMessageV2;
import com.raidiam.trustframework.bank.services.validate.*;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
@Transactional
public class PaymentsService extends BaseBankService {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentsService.class);

    private final CnpjVerifier cnpjVerifier;

    static final String ERROR_CODE_PATTERN = "%s: %s";

    List<EnumLocalInstrument> allowedLocalInstruments;

    private final List<PixPaymentValidator> paymentEntityValidators = List.of(
            new CurrencyValidator(new PaymentErrorMessageV2()),
            new ConsentQrCodeValidator(new ConsentQrCodeValidationErrorsV2(new PaymentErrorMessageV2()))
    );

    private final List<AutomaticPixPaymentValidator> recurringPixPaymentValidators = List.of(
            new AutomaticPaymentConsentValidator(),
            new AutomaticPaymentLimitTransactionValidator()
    );

    private final PaymentAutomations paymentAutomations;

    private final PaymentAutomationsV2 paymentAutomationsV2;

    @Inject
    public PaymentsService(CnpjVerifier cnpjVerifier, PaymentAutomations paymentAutomations, PaymentAutomationsV2 paymentAutomationsV2) {
        this.cnpjVerifier = cnpjVerifier;
        this.paymentAutomations = paymentAutomations;
        this.paymentAutomationsV2 = paymentAutomationsV2;
    }

    public ResponsePixPaymentV3 createPaymentV3(String consentId, String idempotencyKey, String jti, CreatePixPaymentV3 body, String clientId) {
        checkPayloadV2(consentId, body.getData().getTransactionIdentification(), body.getData().getCnpjInitiator(), body.getData().getLocalInstrument(), body.getData().getEndToEndId(), new PaymentErrorMessageV2());
        paymentAutomationsV2.executeClientRestrictions(clientId);
        paymentAutomationsV2.executeImmediateResponses(clientId, body.getData().getPayment().getAmount(), ErrorCodesEnumV2.DETALHE_PAGAMENTO_INVALIDO.toString());
        paymentAutomationsV2.executePostConsentCreationActions(consentId);
        paymentAutomationsV2.executePaymentInitiationActions(consentId);
        BankLambdaUtils.checkAndSaveJti(jtiRepository, jti);
        paymentConsentRepository.findByPaymentConsentId(consentId).ifPresent(this::validatePaymentRequest);
        return createPaymentEntity(consentId, idempotencyKey, body, EnumPaymentStatusTypeV2.RCVD.toString(), new PaymentErrorMessageV2()).getDTOV3();
    }

    public ResponsePixPaymentV4 createPaymentV4(String consentId, String idempotencyKey, String jti, CreatePixPaymentV4 body, String clientId) {
        BankLambdaUtils.checkAndSaveJti(jtiRepository, jti);
        paymentAutomationsV2.executeClientRestrictions(clientId);
        paymentAutomationsV2.executePostConsentCreationActions(consentId);
        paymentAutomationsV2.executePaymentInitiationActions(consentId);
        var consentEntity = paymentConsentRepository.findByPaymentConsentId(consentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "Requested pix payment has no associated consent"));

        if (BankLambdaUtils.checkConsentNotificationTrigger(consentEntity.getStatus())) {
            webhookAdminService.checkAndPostToConsentWebhook(consentEntity.getClientId(), consentId);
        }
        validateMultipleConsentsExpiration(consentEntity);

        LOG.info("Idempotency key - {}", idempotencyKey);
        var existingPaymentList = pixPaymentRepository.findByIdempotencyKey(idempotencyKey);
        var errorMessage = new PaymentErrorMessageV2();
        if (existingPaymentList.isEmpty()) {
            validateQuantity(consentEntity, body);
            List<String> e2eidList = new ArrayList<>();
            for (CreatePixPaymentDataV4 paymentData : body.getData()) {
                checkPayloadV4(consentEntity, paymentData);
                paymentAutomationsV2.executeImmediateResponses(clientId, paymentData.getPayment().getAmount(), ErrorCodesEnumV2.DETALHE_PAGAMENTO_INVALIDO.toString());
                e2eidList.add(paymentData.getEndToEndId());
            }
            // Now to check if the payments EndToEndIds dates corresponds to the scheduled dates at the payment consent.
            assertEnd2EndDates(consentEntity, e2eidList);
        } else {
            LOG.info("Existing  payments are present - {}", existingPaymentList.size());
            if (existingPaymentList.size() != body.getData().size()) {
                throw422AndSetConsentStatusToConsumed(consentId, errorMessage.getMessageVersionDiff());
            }

            for (CreatePixPaymentDataV4 paymentData : body.getData()) {
                for (var existingPayment : existingPaymentList) {
                    LOG.info("Existing  payment is present - {}", existingPayment);
                    validateSamePayload(paymentData, existingPayment, errorMessage);
                }
            }
        }

        List<ResponsePixPaymentDataV4> dtoList = new ArrayList<>();
        for(CreatePixPaymentDataV4 paymentData : body.getData()){
            dtoList.add(createPaymentEntityV4(consentEntity, idempotencyKey, paymentData).getDTOV4());
        }

        consentEntity.setStatus(ResponsePaymentConsentData.StatusEnum.CONSUMED.name());
        paymentConsentRepository.update(consentEntity);
        webhookAdminService.checkAndPostToConsentWebhook(consentEntity.getClientId(), consentId);

        return new ResponsePixPaymentV4().data(dtoList);
    }

    private void validatePaymentRequest(PaymentConsentEntity data) {
        paymentEntityValidators.forEach(v -> v.validate(data));
    }

    private void validateRecurringPixPaymentRequest(CreateRecurringPixPaymentV1Data data, PaymentConsentEntity paymentConsentEntity) {
        recurringPixPaymentValidators.forEach(v -> v.validate(data, paymentConsentEntity));
    }

    private void validateQuantity(PaymentConsentEntity consentEntity, CreatePixPaymentV4 body) {
        int paymentConsentQuantity = getPaymentConsentQuantity(consentEntity);
        if (body.getData().size() != paymentConsentQuantity) {
            throw422AndSetConsentStatusToConsumed(consentEntity.getPaymentConsentId(), ErrorCodesEnumV2.PAGAMENTO_DIVERGENTE_CONSENTIMENTO.name());
        }
    }

    private void isConsentPresent(String consentId) {
        if (consentId == null || consentId.isEmpty()) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, new PaymentErrorMessageV2().getMessageInvalidConsent("ConsentID is missing from payment request or empty"));
        }
    }

    private void isKnownCnpj(String consentId, String cnpjInitiator, PaymentErrorMessage errorMessage) {
        if (!cnpjVerifier.isKnownCnpj(cnpjInitiator)) {
            throw422AndSetConsentStatusToConsumed(consentId, errorMessage.getMessageNotInformed());
        }
    }

    private void checkPayloadV2(String consentId, String transactionIdentification,String cnpjInitiator, EnumLocalInstrument localInstrument, String endToEndId, PaymentErrorMessage errorMessage) {
        isConsentPresent(consentId);
        isKnownCnpj(consentId, cnpjInitiator, errorMessage);
        if (allowedLocalInstruments == null) {
            allowedLocalInstruments = List.of(EnumLocalInstrument.INIC, EnumLocalInstrument.QRDN, EnumLocalInstrument.QRES);
        }
        if (transactionIdentification != null && !transactionIdentification.isEmpty() && !allowedLocalInstruments.contains(localInstrument)) {
            throw422AndSetConsentStatusToConsumed(consentId, errorMessage.getMessageTransactionIdentifier());
        }
        PaymentConsentEntity paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentId).orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "Requested pix payment has no associated consent"));
        LOG.info("Validating endToEndId is the correct format and time");
        if (endToEndId != null) {
            boolean invalidEndToEndId = endToEndId.length() == 32 && !endToEndId.startsWith("1500", 17);
            if (invalidEndToEndId && paymentConsentEntity.getPaymentConsentPaymentEntity().getSchedule() != null
                    || invalidEndToEndId && paymentConsentEntity.getPaymentConsentPaymentEntity().getScheduleSingleDate() != null
                    || invalidEndToEndId && paymentConsentEntity.getPaymentConsentPaymentEntity().getScheduleDailyStartDate() != null
                    || invalidEndToEndId && paymentConsentEntity.getPaymentConsentPaymentEntity().getScheduleMonthlyStartDate() != null
                    || invalidEndToEndId && paymentConsentEntity.getPaymentConsentPaymentEntity().getScheduleWeeklyStartDate() != null
                    || invalidEndToEndId && paymentConsentEntity.getPaymentConsentPaymentEntity().getScheduleCustomDates().length > 0) {
                throw422AndSetConsentStatusToConsumed(consentId, errorMessage.getMessageInvalidParameter("endToEndId timestamp must be the correct format and match 15:00"));
            }
        }
    }

    private void checkPayloadV4(PaymentConsentEntity paymentConsentEntity, CreatePixPaymentDataV4 paymentData) {
        var errorMessage = new PaymentErrorMessageV2();
        if (paymentData.getAuthorisationFlow() != null && paymentData.getAuthorisationFlow().equals(EnumAuthorisationFlow.FIDO_FLOW)) {
            assertSame(paymentConsentEntity.getPaymentConsentId(), paymentData.getConsentId(), paymentConsentEntity.getPaymentConsentId(),
                    errorMessage.getMessagePaymentDivergent(String.format("O valor informado no consentimento não é o mesmo valor do informado no payload de pagamento. consentId consentimento - %s, consentId pagamento- %s",
                            paymentConsentEntity.getPaymentConsentId(), paymentData.getConsentId())));
        }
        if (paymentData.getProxy() != null) {
            assertSame(paymentConsentEntity.getPaymentConsentPaymentEntity().getPaymentConsentDetails().getProxy(), paymentData.getProxy(), paymentConsentEntity.getPaymentConsentId(),
                    errorMessage.getMessagePaymentDivergent(String.format("O valor informado no consentimento não é o mesmo valor do informado no payload de pagamento. proxy consentimento - %s, proxy pagamento- %s",
                            paymentConsentEntity.getPaymentConsentPaymentEntity().getPaymentConsentDetails().getProxy(), paymentData.getProxy())));
        }
        allowedLocalInstruments = List.of(EnumLocalInstrument.INIC, EnumLocalInstrument.QRDN);
        checkPayloadV2(paymentConsentEntity.getPaymentConsentId(), paymentData.getTransactionIdentification(), paymentData.getCnpjInitiator(), paymentData.getLocalInstrument(), paymentData.getEndToEndId(), errorMessage);
        validatePaymentFields(paymentConsentEntity, paymentData, errorMessage);
    }

    private void throw422AndSetConsentStatusToConsumed(String consentId, String message) {
        bankLambdaUtils.throwWithoutRollback(
                () -> paymentConsentRepository.findByPaymentConsentId(consentId).ifPresent(paymentConsent -> {
                    paymentConsent.setStatus(ResponsePaymentConsentData.StatusEnum.CONSUMED.toString());
                    paymentConsentRepository.update(paymentConsent);
                    webhookAdminService.checkAndPostToConsentWebhook(paymentConsent.getClientId(), consentId);
                }),
                new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, message));
    }

    private PixPaymentEntity createPaymentEntity(String consentId, String idempotencyKey, Object body, String paymentStatusType, PaymentErrorMessage errorMessage) {
        LOG.info("Idempotency key - {}", idempotencyKey);
        var existingPayment = pixPaymentRepository.findByIdempotencyKey(idempotencyKey);
        PixPaymentEntity paymentEntity;
        if (existingPayment.isEmpty()) {
            LOG.info("Existing  payment is not present");
            var paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentId)
                    .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "Requested pix payment has no associated consent"));
            LOG.info("Consent Entity Before Update - {}", paymentConsentEntity);
            validatePaymentFields(paymentConsentEntity, body, errorMessage);

            // continue with the payment
            paymentConsentEntity.setStatus(ResponsePaymentConsentData.StatusEnum.CONSUMED.toString());
            var updatedEntity = paymentConsentRepository.update(paymentConsentEntity);
            LOG.info("Consent Entity Before Update - {}", updatedEntity);
            PixPaymentEntity pixPaymentEntity = PixPaymentEntity.from(body, updatedEntity, paymentStatusType, idempotencyKey);
            paymentEntity = pixPaymentRepository.save(pixPaymentEntity);
            LOG.info("Saved Payment entity - {}", paymentEntity);
            return paymentEntity;
        } else {
            LOG.info("Existing  payment is present - {}", existingPayment);
            validateSamePayload(body, existingPayment.get(0), errorMessage);
            return existingPayment.get(0);
        }
    }

    private PixPaymentEntity createRecurringPaymentEntity(String consentId, String idempotencyKey, Object body) {
        LOG.info("Idempotency key - {}", idempotencyKey);
        var existingPayment = pixPaymentRepository.findByIdempotencyKey(idempotencyKey);
        PixPaymentEntity paymentEntity;
        if (existingPayment.isEmpty()) {
            LOG.info("Existing  payment is not present");
            var paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentId)
                    .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "Requested pix payment has no associated consent"));
            LOG.info("Consent Entity Before Update - {}", paymentConsentEntity);
            validatePaymentFields(paymentConsentEntity, body, new PaymentErrorMessageV2());

            var listOfPayments = pixPaymentRepository.findByPaymentConsentEntity(paymentConsentEntity);
            var recurringPixPaymentData = (CreateRecurringPixPaymentV1Data) body;
            if (paymentConsentEntity.hasTotalAllowedLimit() && BankLambdaUtils.isTotalAllowedAmountReached(paymentConsentEntity, listOfPayments, Double.parseDouble(recurringPixPaymentData.getPayment().getAmount()))) {
                updateConsentToConsumed(paymentConsentEntity);
            } else if (paymentConsentEntity.hasTotalAllowedLimit() && BankLambdaUtils.isTotalAllowedAmountExceeded(paymentConsentEntity, listOfPayments, Double.parseDouble(recurringPixPaymentData.getPayment().getAmount()))) {
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, String.format(ERROR_CODE_PATTERN, ErrorCodesEnumV2.CONSENTIMENTO_INVALIDO.name(), "O limite do pagamento excede o limite definido no consentimento"));
            }


            PixPaymentEntity pixPaymentEntity = PixPaymentEntity.from(body, paymentConsentEntity, EnumPaymentStatusTypeV2.RCVD.toString(), idempotencyKey);
            paymentEntity = pixPaymentRepository.save(pixPaymentEntity);
            LOG.info("Saved Payment entity - {}", paymentEntity);
            return paymentEntity;
        } else {
            LOG.info("Existing  payment is present - {}", existingPayment);
            validateSamePayload(body, existingPayment.get(0), new PaymentErrorMessageV2());
            return existingPayment.get(0);
        }
    }

    private PixPaymentEntity createPaymentEntityV4(PaymentConsentEntity paymentConsentEntity, String idempotencyKey, CreatePixPaymentDataV4 body) {
        PixPaymentEntity pixPaymentEntity = PixPaymentEntity.from(body, paymentConsentEntity, EnumPaymentStatusTypeV2.RCVD.toString(), idempotencyKey);
        var savedEntity = pixPaymentRepository.save(pixPaymentEntity);
        LOG.info("Saved Payment entity - {}", savedEntity);
        return savedEntity;
    }

    public ResponsePixPayment getPayment(String paymentId, String clientId) {
        paymentAutomations.executeClientRestrictions(clientId);
        paymentAutomations.executePostPaymentInitiationActions(paymentId);

        return pixPaymentRepository.findByPaymentId(paymentId)

                .map(a -> Optional.of(a.getPaymentConsentEntity())
                        .filter(b -> b.getStatus().equals(ResponsePaymentConsentData.StatusEnum.CONSUMED.toString()))
                        .map(c -> checkScheduleDate(c, paymentId))
                        .map(PixPaymentEntity::getDTO)
                        .orElseGet(a::getDTO))
                .orElse(null);
    }

    public ResponsePixPaymentV3 getPaymentV3(String paymentId, String clientId) {
        return getPixPaymentEntity(paymentId, clientId)
                .map(PixPaymentEntity::getDTOV3).orElseGet(ResponsePixPaymentV3::new);
    }

    public ResponsePixPaymentReadV4 getPaymentV4(String paymentId, String clientId) {
        ResponsePixPaymentDataV4 data = getPixPaymentEntity(paymentId, clientId)
                .map(PixPaymentEntity::getDTOV4).orElseGet(ResponsePixPaymentDataV4::new);
        return new ResponsePixPaymentReadV4().data(data);
    }

    public ResponseRecurringPixPayments getRecurringPixPaymentV1(String recurringPaymentId, String clientId) {
        paymentAutomationsV2.executeClientRestrictions(clientId);
        var  data = Optional.ofNullable(pixPaymentRepository.findByPaymentId(recurringPaymentId)
                .map(this::moveRecurringPaymentStatus)
                .map(PixPaymentEntity::getRecurringPixPaymentV1DTO)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Payment not found")))
                .orElseGet(ResponseRecurringPixPaymentsData::new);
        return new ResponseRecurringPixPayments().data(data);
    }

    public ResponseRecurringPixPaymentByConsent getRecurringPixPaymentByConsentIdV1(String recurringConsentId, String clientId) {
        paymentAutomationsV2.executeClientRestrictions(clientId);
        var paymentConsentEntity = BankLambdaUtils.getPaymentConsent(recurringConsentId, paymentConsentRepository);
        var response = new ResponseRecurringPixPaymentByConsent();

        pixPaymentRepository.findByPaymentConsentEntity(paymentConsentEntity)
                        .stream()
                        .map(this::moveRecurringPaymentStatus)
                        .map(PixPaymentEntity::getRecurringPixPaymentsListV1DTO)
                        .forEach(response::addDataItem);
        return response;
    }

    private Optional<PixPaymentEntity> getPixPaymentEntity(String paymentId, String clientId) {
        paymentAutomationsV2.executeClientRestrictions(clientId);
        paymentAutomationsV2.executePostPaymentInitiationActions(paymentId);

        pixPaymentRepository.findByPaymentId(paymentId).ifPresent(payment -> {
            if (BankLambdaUtils.checkPaymentNotificationTrigger(payment.getStatus())) {
                var paymentConsentOptional = paymentConsentRepository.findByIdempotencyKey(payment.getIdempotencyKey());
                paymentConsentOptional.ifPresent(paymentConsentEntity -> webhookAdminService.checkAndPostToPaymentWebhook(paymentConsentEntity.getClientId(), paymentId));
            }
        });

        return Optional.ofNullable(pixPaymentRepository.findByPaymentId(paymentId)
                .map(a -> Optional.of(a.getPaymentConsentEntity())
                        .filter(b -> !a.getStatus().equals(EnumPaymentStatusTypeV2.RJCT.name()) && !a.getStatus().equals(EnumPaymentStatusTypeV2.ACSC.name())
                                && b.getStatus().equals(ResponsePaymentConsentData.StatusEnum.CONSUMED.toString())
                        )
                        .map(c -> checkScheduleDateV2(c, paymentId))
                        .orElse(a)
                )
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Payment not found")));
    }

    public ResponsePixPayment updatePayment(String paymentId, UpdatePixPayment body, String clientId) {
        paymentAutomations.executeClientRestrictions(clientId);
        paymentAutomations.executePostPaymentInitiationActions(paymentId);

        return updatePixPaymentEntity(paymentId, body.getData().getStatus().toString()).getDTO();
    }

    public ResponsePixPaymentV2 updatePaymentV3(String paymentId, UpdatePixPaymentV2 body, String clientId) {
        paymentAutomationsV2.executeClientRestrictions(clientId);
        paymentAutomationsV2.executePostPaymentInitiationActions(paymentId);

        return updatePixPaymentEntity(paymentId, body.getData().getStatus().toString()).getDTOV2();
    }

    private PixPaymentEntity updatePixPaymentEntity(String paymentId, String status){
        var pixPaymentEntityOptional = pixPaymentRepository.findByPaymentId(paymentId);
        if (pixPaymentEntityOptional.isPresent()) {
            var pixPaymentEntity = pixPaymentEntityOptional.get();
            pixPaymentEntity.setStatus(status);
            pixPaymentRepository.update(pixPaymentEntity);
            return pixPaymentEntity;
        }
        throw new HttpStatusException(HttpStatus.NOT_FOUND, "Requested pix payment not found");
    }

    public ResponsePixPaymentV2 patchPaymentV3(String paymentId, PatchPaymentsV2 body) {
        if(!body.getData().getCancellation().getCancelledBy().getDocument().getRel().equals("CPF")) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, new PaymentErrorMessageV2().getMessageInvalidParameter());
        }
        Optional<PixPaymentEntity> payment = pixPaymentRepository.findByPaymentId(paymentId);
        payment.ifPresent(p -> {
            validateStatus(p);
            p.setStatus(body.getData().getStatus().toString());
            p.setCancellationFrom(Cancellation.CancelledFromEnum.INICIADORA.toString());
            if (p.getPixPaymentPaymentEntity().getAmount().equals("12345.67")) {
                p.setCancellationReason(Cancellation.ReasonEnum.PENDENCIA.toString());
            } else {
                p.setCancellationReason(Cancellation.ReasonEnum.AGENDAMENTO.toString());
            }
        });
        payment.ifPresent(pixPaymentRepository::update);

        return pixPaymentRepository.findByPaymentId(paymentId).map(PixPaymentEntity::getDTOV2).orElse(null);
    }

    public ResponsePatchPixConsentV4 patchPaymentByConsentIdV4(String consentId, PatchPixPaymentV4 body) {
        PaymentConsentEntity consentEntity = paymentConsentRepository.findByPaymentConsentId(consentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Consent does not exist"));

        var requestDocument = body.getData().getCancellation().getCancelledBy().getDocument();
        assertIdentification(consentEntity, requestDocument.getRel(), requestDocument.getIdentification());

        List<PixPaymentEntity> paymentEntities = pixPaymentRepository.findByPaymentConsentEntity(consentEntity);

        ResponsePatchPixConsentV4 response = new ResponsePatchPixConsentV4();

        LOG.info("{} payment entities will be canceled", paymentEntities.size());

        paymentEntities.stream()
                .filter(this::allowedPatchStatus)
                .map(payment -> {
                    payment.setStatus(EnumPaymentStatusTypeV2.CANC.toString());
                    webhookAdminService.checkAndPostToPaymentWebhook(consentEntity.getClientId(), payment.getPaymentId());
                    return payment;
                })
                .map(payment -> payment.setStatusUpdateDateTime(new Date()))
                .map(pixPaymentRepository::update)
                .map(PixPaymentEntity::getPatchDTOV4)
                .forEach(response::addDataItem);

        return response;
    }

    public ResponsePixPaymentReadV4 patchPaymentV4(String paymentId, PatchPixPaymentV4 body) {

        var requestDocument = body.getData().getCancellation().getCancelledBy().getDocument();

        if(!requestDocument.getRel().equals("CPF")) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, String.format("%s: cancelledBy.document.rel has incorrect format",
                    ErrorCodesEnumV2.PAGAMENTO_NAO_PERMITE_CANCELAMENTO.name()));
        }

        var paymentEntities = pixPaymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "payment not found"));

        var consentId = paymentEntities.getPaymentConsentEntity().getPaymentConsentId();
        PaymentConsentEntity consentEntity = paymentConsentRepository.findByPaymentConsentId(consentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Consent does not exist"));

        assertIdentification(consentEntity, requestDocument.getRel(), requestDocument.getIdentification());

        if (!allowedPatchStatus(paymentEntities)) {
            throw422AndSetConsentStatusToConsumed(consentId, String.format("%s: payment not on allowed status SCHD or PDNG",ErrorCodesEnumV2.PAGAMENTO_NAO_PERMITE_CANCELAMENTO.name()));
        }

        if (paymentEntities.getStatus().equals(EnumPaymentStatusTypeV2.PDNG.name())) {
            paymentEntities.setCancellationReason(EnumPaymentCancellationReasonTypeV4.PENDENCIA.toString());
        } else if (paymentEntities.getStatus().equals(EnumPaymentStatusTypeV2.SCHD.name())) {
            paymentEntities.setCancellationReason(EnumPaymentCancellationReasonTypeV4.AGENDAMENTO.toString());
        }
        paymentEntities.setCancellationFrom(EnumPaymentCancellationFromTypeV4.INICIADORA.toString());
        paymentEntities.setStatus(EnumPaymentStatusTypeV2.CANC.toString());
        paymentEntities.setStatusUpdateDateTime(new Date());
        pixPaymentRepository.update(paymentEntities);

        webhookAdminService.checkAndPostToPaymentWebhook(consentEntity.getClientId(), paymentId);
        return new ResponsePixPaymentReadV4().data(paymentEntities.getDTOV4());

    }

    public ResponseRecurringPixPayments patchRecurringPixPaymentV1(String recurringPaymentConsentId, String recurringPaymentId, RecurringPatchPixPayment body) {
        PaymentConsentEntity consentEntity = BankLambdaUtils.getPaymentConsent(recurringPaymentConsentId, paymentConsentRepository);

        var requestDocument = body.getData().getCancellation().getCancelledBy().getDocument();
        assertIdentification(consentEntity, requestDocument.getRel(), requestDocument.getIdentification());

        var paymentEntities = pixPaymentRepository.findByPaymentId(recurringPaymentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "payment not found"));

        if (!allowedPatchStatus(paymentEntities)) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, String.format(ERROR_CODE_PATTERN, ErrorCodesEnumV2.PAGAMENTO_NAO_PERMITE_CANCELAMENTO.name(), "Pagamento está com um status que não permite cancelamento"));
        }
        paymentEntities.setStatus(EnumPaymentStatusTypeV2.CANC.toString());
        paymentEntities.setStatusUpdateDateTime(new Date());
        pixPaymentRepository.update(paymentEntities);
        return new ResponseRecurringPixPayments().data(paymentEntities.getRecurringPixPaymentV1DTO());
    }

    public ResponseRecurringPixPayments createRecurringPixPaymentV1(String consentId, String idempotencyKey, String jti, String clientId, CreateRecurringPixPaymentV1 body) {
        BankLambdaUtils.checkAndSaveJti(jtiRepository, jti);
        paymentAutomationsV2.executeClientRestrictions(clientId);

        var paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "Requested pix payment has no associated consent"));

        validateMultipleConsentsExpiration(paymentConsentEntity);
        validateRecurringPixPaymentRequest(body.getData(), paymentConsentEntity);
        if (paymentConsentEntity.hasPeriodicLimit()) {
            validateSweepingPeriodicLimit(paymentConsentEntity, body.getData());
        }
        validateCreditorNumber(body.getData(), paymentConsentEntity);


        if (paymentConsentEntity.getExpirationDateTime() != null && BankLambdaUtils.isExpirationDateInPast(paymentConsentEntity.getExpirationDateTime())) {
            updateConsentToConsumed(paymentConsentEntity);
            throw new HttpStatusException(HttpStatus.UNAUTHORIZED, String.format(ERROR_CODE_PATTERN, ErrorCodesEnumV2.CONSENTIMENTO_INVALIDO.name(), "O consentimento informado encontra-se expirado"));
        }


        return new ResponseRecurringPixPayments()
                .data(createRecurringPaymentEntity(consentId, idempotencyKey, body.getData()).postRecurringPixPaymentV1DTO());
    }

    private void validateCreditorNumber(CreateRecurringPixPaymentV1Data request, PaymentConsentEntity paymentConsentEntity) {
        LOG.info("Validating recurring pix payment creditor");
        if (paymentConsentEntity.getBusinessDocumentIdentification() != null && paymentConsentEntity.getBusinessDocumentRel() != null) {
            LOG.info("Business entity is present, validating if the amount is trigger for forcing 422");
            if (request.getPayment().getAmount().equals("1200.01")) {
                LOG.info("Forced a 422 for payment request for creditor validation - business identification doesn't match with payload");
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, String.format(ERROR_CODE_PATTERN, ErrorCodesEnumV2.PAGAMENTO_DIVERGENTE_CONSENTIMENTO.name(), "O CNPJ do loggedUser (Bussiness Entity) não corresponde ao creditor do consentimento"));
            }
        } else if (request.getCreditorAccount() != null){
            LOG.info("Business entity is absent, validating creditors number belongs to cpf equal the logged user");
            var account = accountRepository.findByNumber(request.getCreditorAccount().getNumber())
                    .orElseThrow(() -> new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, String.format(ERROR_CODE_PATTERN, ErrorCodesEnumV2.PAGAMENTO_DIVERGENTE_CONSENTIMENTO.name(), "O CPF do loggedUser não corresponde ao creditor do consentimento")));

            if (!account.getAccountHolder().getDocumentIdentification().equals(paymentConsentEntity.getAccountHolder().getDocumentIdentification())) {
                LOG.info("Forced a 422 for payment request for creditor validation - cpf logged user doesn't match with payload");
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, String.format(ERROR_CODE_PATTERN, ErrorCodesEnumV2.PAGAMENTO_DIVERGENTE_CONSENTIMENTO.name(), "O CPF do loggedUser não corresponde ao creditor do consentimento"));
            }
        }
        LOG.info("recurring pix payment creditor validation complete");

    }

    private void validateSweepingPeriodicLimit(PaymentConsentEntity paymentConsentEntity, CreateRecurringPixPaymentV1Data data) {
        var listOfPayments = pixPaymentRepository.findByPaymentConsentEntity(paymentConsentEntity);
        // Daily periodic
        if (paymentConsentEntity.hasSweepingDailyPeriodicLimit()) {
            var sumOfTheDay = listOfPayments.stream()
                    .filter(p-> p.getDate().getDayOfYear() == data.getDate().getDayOfYear())
                    .mapToDouble(pp -> Double.parseDouble(pp.getPixPaymentPaymentEntity().getAmount())).sum();
            var countOfTheDay = listOfPayments.stream()
                    .filter(p-> p.getDate().getDayOfYear() == data.getDate().getDayOfYear()).count() + 1;
            var periodicDayLimit = Double.parseDouble(paymentConsentEntity.getPostSweepingRecurringConfiguration().getPeriodicLimits().getPeriodicLimitsDayTransactionLimit());
            var periodicQuantityDayLimit = paymentConsentEntity.getPostSweepingRecurringConfiguration().getPeriodicLimits().getPeriodicLimitsDayQuantityLimit();
            sumOfTheDay += Double.parseDouble(data.getPayment().getAmount());

            if(sumOfTheDay > periodicDayLimit) {
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "LIMITE_PERIODO_VALOR_EXCEDIDO: O limite diário estabelicido pelo consentimento foi excedido");
            }

            if(countOfTheDay > periodicQuantityDayLimit) {
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,  "LIMITE_PERIODO_QUANTIDADE_EXCEDIDO: O limite diário estabelicido pelo consentimento foi excedido");
            }
        }


        //Weekly periodic
        if (paymentConsentEntity.hasSweepingWeeklyPeriodicLimit()) {
            var sumOfTheWeek = listOfPayments.stream()
                    .filter(p -> p.getDate().get(ChronoField.ALIGNED_WEEK_OF_YEAR) == data.getDate().get(ChronoField.ALIGNED_WEEK_OF_YEAR))
                    .mapToDouble(pp -> Double.parseDouble(pp.getPixPaymentPaymentEntity().getAmount())).sum();
            var countOfTheWeek = listOfPayments.stream()
                    .filter(p -> p.getDate().get(ChronoField.ALIGNED_WEEK_OF_YEAR) == data.getDate().get(ChronoField.ALIGNED_WEEK_OF_YEAR)).count() + 1;
            var periodicQuantityWeekLimit = paymentConsentEntity.getPostSweepingRecurringConfiguration().getPeriodicLimits().getPeriodicLimitsWeekQuantityLimit();
            var periodicWeekLimit = Double.parseDouble(paymentConsentEntity.getPostSweepingRecurringConfiguration().getPeriodicLimits().getPeriodicLimitsWeekTransactionLimit());
            sumOfTheWeek += Double.parseDouble(data.getPayment().getAmount());

            if (sumOfTheWeek > periodicWeekLimit) {
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "LIMITE_PERIODO_VALOR_EXCEDIDO: O limite semanal estabelicido pelo consentimento foi excedido");
            }

            if (countOfTheWeek > periodicQuantityWeekLimit) {
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "LIMITE_PERIODO_QUANTIDADE_EXCEDIDO: O limite semanal estabelicido pelo consentimento foi excedido");
            }
        }

        //Monthly periodic
        if (paymentConsentEntity.hasSweepingMonthlyPeriodicLimit()) {
            var sumOfTheMonth = listOfPayments.stream()
                    .filter(p -> p.getDate().getMonth() == data.getDate().getMonth())
                    .mapToDouble(pp -> Double.parseDouble(pp.getPixPaymentPaymentEntity().getAmount())).sum();
            var countOfTheMonth = listOfPayments.stream()
                    .filter(p -> p.getDate().getMonth() == data.getDate().getMonth()).count() + 1;
            var periodicQuantityMonthLimit = paymentConsentEntity.getPostSweepingRecurringConfiguration().getPeriodicLimits().getPeriodicLimitsMonthQuantityLimit();
            var periodicMonthLimit = Double.parseDouble(paymentConsentEntity.getPostSweepingRecurringConfiguration().getPeriodicLimits().getPeriodicLimitsMonthTransactionLimit());
            sumOfTheMonth += Double.parseDouble(data.getPayment().getAmount());

            if (sumOfTheMonth > periodicMonthLimit) {
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "LIMITE_PERIODO_VALOR_EXCEDIDO: O limite mensal estabelicido pelo consentimento foi excedido");
            }
            if (countOfTheMonth > periodicQuantityMonthLimit) {
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "LIMITE_PERIODO_QUANTIDADE_EXCEDIDO: O limite mensal estabelicido pelo consentimento foi excedido");
            }
        }
    }

    private boolean allowedPatchStatus(PixPaymentEntity pixPaymentEntity) {
        return pixPaymentEntity.getStatus().equals(EnumPaymentStatusTypeV2.SCHD.name()) || pixPaymentEntity.getStatus().equals(EnumPaymentStatusTypeV2.PDNG.name());
    }

    private void assertIdentification(PaymentConsentEntity consentEntity, String requestRel, String requestIdentification) {
        String expectedIdentification = consentEntity.getAccountHolder().getDocumentIdentification();
        String expectedRel = consentEntity.getAccountHolder().getDocumentRel();

        LOG.info("verifying request rel - {} and identification - {} against consent rel - {} and identification - {}",
                requestRel, requestIdentification, expectedRel, expectedIdentification);

        if (!Objects.equals(requestIdentification, expectedIdentification) || !Objects.equals(requestRel, expectedRel)) {
            throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "unauthorized");
        }
    }
    private void validateStatus(PixPaymentEntity paymentEntity) {
        if(!paymentEntity.getStatus().equals(EnumPaymentStatusTypeV2.SCHD.name())
                && !paymentEntity.getStatus().equals(EnumPaymentStatusTypeV2.PDNG.name())
                && !paymentEntity.getStatus().equals(EnumPaymentStatusTypeV2.PATC.name())) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, ErrorCodesEnumV2.PAGAMENTO_NAO_PERMITE_CANCELAMENTO.name());
        }
    }

    private void validatePaymentFields(PaymentConsentEntity paymentConsentEntity, Object body, PaymentErrorMessage errorMessage) {
        LOG.info("Validating Payment Fields");
        PixPaymentEntity pixPaymentEntity = PixPaymentEntity.from(body, paymentConsentEntity, "", "");
        String paymentAmount = pixPaymentEntity.getPixPaymentPaymentEntity().getAmount();
        String paymentCurrency = pixPaymentEntity.getPixPaymentPaymentEntity().getCurrency();
        EnumLocalInstrument localInstrument = EnumLocalInstrument.fromValue(pixPaymentEntity.getLocalInstrument());
        String paymentQrCode = pixPaymentEntity.getQrCode();
        if (paymentConsentEntity.getExpirationDateTime() != null && Instant.now().isAfter(paymentConsentEntity.getExpirationDateTime().toInstant())) {
            String message = String.format("Requested pix payment has associated consent, but the consent is expired - %s", paymentConsentEntity.getExpirationDateTime());
            LOG.info(message);
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, message);
        }
        if (ResponsePaymentConsentData.StatusEnum.CONSUMED.toString().equals(paymentConsentEntity.getStatus())) {
            LOG.info("Requested pix payment associated consent is CONSUMED");
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    errorMessage.getMessageInvalidConsent(String.format("Consentimento inválido - consentimento encontrado, mas que expirou - %s", paymentConsentEntity.getStatus())));
        }
        if (!ResponsePaymentConsentData.StatusEnum.AUTHORISED.toString().equals(paymentConsentEntity.getStatus())) {
            String message = String.format("Requested pix payment has associated consent, but the consent status is not AUTHORISED - %s", paymentConsentEntity.getStatus());
            LOG.info(message);
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, message);
        }

        if (pixPaymentEntity.getEndToEndId() == null || !pixPaymentEntity.getEndToEndId().matches("^([E])([0-9]{8})([0-9]{4})(0[1-9]|1[0-2])(0[1-9]|[1-2][0-9]|3[0-1])(2[0-3]|[01][0-9])([0-5][0-9])([a-zA-Z0-9]{11})$")) {
            throw422AndSetConsentStatusToConsumed(paymentConsentEntity.getPaymentConsentId(),
                    errorMessage.getParameterNotInformed("endToEndId is missing or invalid"));
        }


        PaymentConsentPaymentEntity paymentConsentPaymentEntity = paymentConsentEntity.getPaymentConsentPaymentEntity();
        if (paymentConsentPaymentEntity != null) {
            String paymentConsentAmount = paymentConsentPaymentEntity.getAmount();
            if (!paymentAmount.equals(paymentConsentAmount)) {
                LOG.info("PIX Payments payment amount - {} is not the same as in the Payments Consent - {}", paymentAmount, paymentConsentAmount);
                throw422AndSetConsentStatusToConsumed(paymentConsentEntity.getPaymentConsentId(),
                        errorMessage.getMessagePaymentDivergent(String.format("O valor informado no consentimento não é o mesmo valor do informado no payload de pagamento. amount consentimento - %s, amount pagamento- %s",
                                paymentConsentAmount, paymentAmount)));
            }

            String paymentConsentCurrency = paymentConsentPaymentEntity.getCurrency();
            if (!paymentCurrency.equals(paymentConsentCurrency)) {
                LOG.info("PIX Payments payment currency - {} is not the same as in the Payments Consent - {}", paymentCurrency, paymentConsentCurrency);
                throw422AndSetConsentStatusToConsumed(paymentConsentEntity.getPaymentConsentId(), errorMessage.getMessagePaymentDivergent(String.format("O valor informado no consentimento não é o mesmo valor do informado no payload de pagamento. currency consentimento - %s, currency pagamento- %s",
                        paymentConsentCurrency, paymentCurrency)));
            }

            if (localInstrument == EnumLocalInstrument.QRES || localInstrument == EnumLocalInstrument.QRDN) {
                String paymentConsentQrCode = paymentConsentPaymentEntity.getPaymentConsentDetails().getQrCode();
                if(!paymentQrCode.equals(paymentConsentQrCode)){
                    String message = errorMessage.getMessagePaymentDivergent(String.format("Requested pix payment qrCode - %s differs from the qrCode in the associated Consent - %s",
                            paymentQrCode, paymentConsentQrCode));
                    LOG.info(message);
                    throw422AndSetConsentStatusToConsumed(paymentConsentEntity.getPaymentConsentId(), message);
                }
            }else {
                LOG.info("QrCode Validation skipped since it is only applicable for either QRES or QRDN local instruments");
            }
        }

        LOG.info("Payment Fields validation is finished");
    }
    private void validateSamePayload(Object body, PixPaymentEntity entity, PaymentErrorMessage errorMessage) {
        LOG.info("Validating the same payload - {}", entity);
        String paymentConsentId = entity.getPaymentConsentEntity().getPaymentConsentId();

        PaymentPix payment;
        CreditorAccount creditorAccount;
        String proxy;
        String qrCode;
        EnumLocalInstrument localInstrument;
        String remittanceInformation;

        if (body instanceof CreatePixPaymentV3) {
            var requestData = ((CreatePixPaymentV3) body).getData();
            payment = requestData.getPayment();
            creditorAccount = requestData.getCreditorAccount();
            proxy = requestData.getProxy();
            qrCode = requestData.getQrCode();
            localInstrument = requestData.getLocalInstrument();
            remittanceInformation = requestData.getRemittanceInformation();
        } else {
            var requestData = ((CreatePixPaymentDataV4) body);
            payment = requestData.getPayment();
            creditorAccount = requestData.getCreditorAccount();
            proxy = requestData.getProxy();
            qrCode = requestData.getQrCode();
            localInstrument = requestData.getLocalInstrument();
            remittanceInformation = requestData.getRemittanceInformation();
        }
        if (proxy != null) {
            assertSame(proxy, entity.getProxy(), paymentConsentId, errorMessage.getMessageVersionDiff());
        } else if (entity.getProxy() != null) {
            throw422AndSetConsentStatusToConsumed(paymentConsentId, errorMessage.getMessageVersionDiff());
        }
        assertSame(localInstrument.toString(), entity.getLocalInstrument(), paymentConsentId, errorMessage.getMessageVersionDiff());
        if (qrCode != null) {
            assertSame(qrCode, entity.getQrCode(), paymentConsentId, errorMessage.getMessageVersionDiff());
        } else if (entity.getQrCode() != null) {
            throw422AndSetConsentStatusToConsumed(paymentConsentId, errorMessage.getMessageVersionDiff());
        }
        assertSame(remittanceInformation, entity.getRemittanceInformation(), paymentConsentId, errorMessage.getMessageVersionDiff());

        CreditorAccountEntity creditorAccountEntity = entity.getCreditorAccountEntity();
        assertSame(creditorAccount.getAccountType().toString(), creditorAccountEntity.getAccountType(), paymentConsentId, errorMessage.getMessageVersionDiff());
        assertSame(creditorAccount.getIspb(), creditorAccountEntity.getIspb(), paymentConsentId, errorMessage.getMessageVersionDiff());
        assertSame(creditorAccount.getIssuer(), creditorAccountEntity.getIssuer(), paymentConsentId, errorMessage.getMessageVersionDiff());
        assertSame(creditorAccount.getNumber(), creditorAccountEntity.getNumber(), paymentConsentId, errorMessage.getMessageVersionDiff());

        PixPaymentPaymentEntity pixPaymentPaymentEntity = entity.getPixPaymentPaymentEntity();
        assertSame(payment.getCurrency(), pixPaymentPaymentEntity.getCurrency(), paymentConsentId, errorMessage.getMessageVersionDiff());
        assertSame(payment.getAmount(), pixPaymentPaymentEntity.getAmount(), paymentConsentId, errorMessage.getMessageVersionDiff());
    }

    private void assertSame(Object inbound, Object current, String consentId, String message) {
        if (!inbound.equals(current)) {
            throw422AndSetConsentStatusToConsumed(consentId, message);
        }
    }

    private PixPaymentEntity checkScheduleDate(PaymentConsentEntity paymentConsent, String paymentId) {
        Optional<PixPaymentEntity> payment = pixPaymentRepository.findByPaymentId(paymentId);
        Date dateOfToday = new Date();
        LocalDate currentDate = dateOfToday.toInstant().atZone(BankLambdaUtils.getBrasilZoneId()).toLocalDate();
        Date schedule = paymentConsent.getPaymentConsentPaymentEntity().getSchedule();
        LocalDate scheduleDate = BankLambdaUtils.dateToLocalDate(schedule);
        if (schedule != null) {
            if (scheduleDate.isBefore(currentDate) || scheduleDate.isEqual(currentDate)) {
                payment.ifPresent(p -> p.setStatus(EnumPaymentStatusType.ACCC.toString()));
            } else if (scheduleDate.isAfter(currentDate)) {
                payment.ifPresent(p -> p.setStatus(EnumPaymentStatusType.SASC.toString()));
            }
        }
        payment.ifPresent(pixPaymentRepository::update);

        return pixPaymentRepository.findByPaymentId(paymentId).orElse(null);
    }

    private PixPaymentEntity checkScheduleDateV2(PaymentConsentEntity paymentConsent, String paymentId) {
        var payment = pixPaymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        Date dateOfToday = new Date();
        LocalDate currentDate = dateOfToday.toInstant().atZone(BankLambdaUtils.getBrasilZoneId()).toLocalDate();
        LocalDate scheduleDate = getScheduleDate(payment, paymentConsent);
        if (scheduleDate != null && !payment.getStatus().equals(EnumPaymentStatusTypeV2.CANC.toString())) {
            if (scheduleDate.isBefore(currentDate) || scheduleDate.isEqual(currentDate)) {
                payment.setStatus(EnumPaymentStatusTypeV2.ACSC.toString());
            } else if (payment.getStatus().equals(EnumPaymentStatusTypeV2.SCHD.toString()) &&
                    payment.getPixPaymentPaymentEntity().getAmount().equals("1400.00")) {
                var currentDateTime = BankLambdaUtils.timestampToLocalDateTime(new Date());
                var paymentCreateDatePlusFiveMin = BankLambdaUtils.timestampToLocalDateTime(payment.getCreationDateTime()).plusMinutes(3);
                //Waits until 3 minutes after creation to change it for CANC, allowing more than one GET with SCHD status
                if(currentDateTime.isAfter(paymentCreateDatePlusFiveMin)) {
                    payment.setStatus(EnumPaymentStatusTypeV2.CANC.toString());
                    payment.setCancellationReason(Cancellation.ReasonEnum.AGENDAMENTO.toString());
                    payment.setCancellationFrom(Cancellation.CancelledFromEnum.DETENTORA.toString());
                }
            } else if (scheduleDate.isAfter(currentDate)) {
                payment.setStatus(EnumPaymentStatusTypeV2.SCHD.toString());
            }

            if (BankLambdaUtils.checkPaymentNotificationTrigger(payment.getStatus())) {
                webhookAdminService.checkAndPostToPaymentWebhook(paymentConsent.getClientId(), paymentId);
            }
        }
        return pixPaymentRepository.update(payment);
    }

    private PixPaymentEntity moveRecurringPaymentStatus(PixPaymentEntity pixPaymentEntity) {
        var currentDate = new Date().toInstant().atZone(BankLambdaUtils.getBrasilZoneId()).toLocalDate();
        var pixPaymentDate = pixPaymentEntity.getDate();
        if (pixPaymentEntity.getStatus().equals(EnumPaymentStatusTypeV2.RCVD.name())) {
            if (pixPaymentDate.isAfter(currentDate)) {
                //Payment is scheduled
                pixPaymentEntity.setStatus(EnumPaymentStatusTypeV2.SCHD.name());
            } else {
                //Payment has already been processed
                pixPaymentEntity.setStatus(EnumPaymentStatusTypeV2.ACSC.name());
            }

            if (BankLambdaUtils.checkPaymentNotificationTrigger(pixPaymentEntity.getStatus())) {
                var paymentConsentOptional = paymentConsentRepository.findByIdempotencyKey(pixPaymentEntity.getIdempotencyKey());
                paymentConsentOptional.ifPresent(paymentConsentEntity -> webhookAdminService.checkAndPostToPaymentWebhook(paymentConsentEntity.getClientId(), pixPaymentEntity.getPaymentId()));
            }
            pixPaymentEntity.setUpdatedAt(new Date());
            return pixPaymentRepository.update(pixPaymentEntity);
        }
        return pixPaymentEntity;
    }

    private LocalDate getScheduleDate(PixPaymentEntity pixPaymentEntity, PaymentConsentEntity paymentConsent) {
        if (paymentConsent.getPaymentConsentPaymentEntity().getSchedule() != null) {
            return BankLambdaUtils.dateToLocalDate(paymentConsent.getPaymentConsentPaymentEntity().getSchedule());
        } else if (paymentConsent.getPaymentConsentPaymentEntity().getPaymentDate() != null && pixPaymentEntity.getStatus().equals(EnumPaymentStatusTypeV2.RCVD.toString())) {
            return BankLambdaUtils.dateToLocalDate(paymentConsent.getPaymentConsentPaymentEntity().getPaymentDate());
        }
        if (paymentConsent.getPaymentConsentPaymentEntity().getScheduleSingleDate() != null
                || paymentConsent.getPaymentConsentPaymentEntity().getScheduleDailyStartDate() != null
                || paymentConsent.getPaymentConsentPaymentEntity().getScheduleMonthlyStartDate() != null
                || paymentConsent.getPaymentConsentPaymentEntity().getScheduleWeeklyStartDate() != null
                || paymentConsent.getPaymentConsentPaymentEntity().getScheduleCustomDates().length > 0) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
            return LocalDate.parse(pixPaymentEntity.getEndToEndId().substring(9, 21), formatter);
        }
        return null;
    }

    private void assertEnd2EndDates(PaymentConsentEntity paymentConsentEntity, List<String> e2eidList) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        var errorMessage = new PaymentErrorMessageV2();
        Set<LocalDate> scheduledDatesSet = scheduledDatesService.generateScheduleDates(paymentConsentEntity);
        Set<LocalDate> extractedDatesSet = e2eidList.stream().map(e -> LocalDate.parse(e.substring(9, 21), formatter)).collect(Collectors.toCollection(TreeSet::new));
        if (!scheduledDatesSet.isEmpty() && !extractedDatesSet.equals(scheduledDatesSet)) {
            LOG.info("Pix Payments e2e ID doesn't match consent dataset");
            throw422AndSetConsentStatusToConsumed(paymentConsentEntity.getPaymentConsentId(), errorMessage.getMessagePaymentDivergent("O valor informado no consentimento não é o mesmo valor do informado no payload de pagamento. As datas do E2EID não batem com as do consenimento"));
        }
    }

    private static int getPaymentConsentQuantity(PaymentConsentEntity paymentConsentEntity) {
        var paymentConsentPaymentEntity = paymentConsentEntity.getPaymentConsentPaymentEntity();
        var paymentConsentQuantity = 1;

        if (paymentConsentPaymentEntity.getScheduleDailyStartDate() != null) {
            paymentConsentQuantity = paymentConsentPaymentEntity.getScheduleDailyQuantity();
        } else if (paymentConsentPaymentEntity.getScheduleWeeklyStartDate() != null) {
            paymentConsentQuantity = paymentConsentPaymentEntity.getScheduleWeeklyQuantity();
        } else if (paymentConsentPaymentEntity.getScheduleMonthlyStartDate() != null) {
            paymentConsentQuantity = paymentConsentPaymentEntity.getScheduleMonthlyQuantity();
        } else if (paymentConsentPaymentEntity.getScheduleCustomDates() != null && paymentConsentPaymentEntity.getScheduleCustomDates().length > 0) {
            paymentConsentQuantity = paymentConsentPaymentEntity.getScheduleCustomDates().length;
        }

        return paymentConsentQuantity;
    }

    private void updateConsentToConsumed(PaymentConsentEntity paymentConsentEntity) {
        paymentConsentEntity.setStatus(UpdatePaymentConsentData.StatusEnum.CONSUMED.toString());
        paymentConsentRepository.update(paymentConsentEntity);
        webhookAdminService.checkAndPostToConsentWebhook(paymentConsentEntity.getClientId(), paymentConsentEntity.getPaymentConsentId());
    }
 
    private void validateMultipleConsentsExpiration(PaymentConsentEntity consentEntity) {
        LocalDateTime consentWaitTime =  LocalDateTime.ofInstant(consentEntity.getCreationDateTime().toInstant(), ZoneId.systemDefault()).plusMinutes(2);
        if (consentEntity.getStatus().equals(EnumAuthorisationStatusType.PARTIALLY_ACCEPTED.toString()) &&
                LocalDateTime.now().isBefore(consentWaitTime)) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "CONSENTIMENTO_PENDENTE_AUTORIZACAO");
        } else if (consentEntity.getStatus().equals(EnumAuthorisationStatusType.PARTIALLY_ACCEPTED.toString())
                && LocalDateTime.now().isAfter(consentWaitTime)) {
            consentEntity.setStatus(EnumAuthorisationStatusType.AUTHORISED.toString());
            paymentConsentRepository.update(consentEntity);
        }
    }

}
