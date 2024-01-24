package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.domain.AccountEntity;
import com.raidiam.trustframework.bank.domain.AccountHolderEntity;
import com.raidiam.trustframework.bank.domain.PaymentConsentEntity;
import com.raidiam.trustframework.bank.enums.ErrorCodesEnumV1;
import com.raidiam.trustframework.bank.exceptions.ConsentRejectionException;
import com.raidiam.trustframework.bank.repository.AccountHolderRepository;
import com.raidiam.trustframework.bank.repository.AccountRepository;
import com.raidiam.trustframework.bank.services.automation.PaymentAutomations;
import com.raidiam.trustframework.bank.services.automation.PaymentAutomationsV2;
import com.raidiam.trustframework.bank.services.message.PaymentErrorMessageV1;
import com.raidiam.trustframework.bank.services.message.PaymentErrorMessageV2;
import com.raidiam.trustframework.bank.services.validate.*;
import com.raidiam.trustframework.bank.services.validate.payment.consent.rejection.PaymentConsentRejectionValidator;
import com.raidiam.trustframework.bank.services.validate.payment.consent.rejection.creditordebtor.PaymentConsentCreditorDebtorRejectionValidator;
import com.raidiam.trustframework.bank.services.validate.payment.consent.rejection.creditordebtor.PaymentConsentCreditorDebtorRejectionValidatorExceptionsV3;
import com.raidiam.trustframework.bank.services.validate.payment.consent.rejection.debtoramount.PaymentConsentDebtorAmountRejectionValidator;
import com.raidiam.trustframework.bank.services.validate.payment.consent.rejection.debtoramount.PaymentConsentDebtorAmountRejectionValidatorExceptionsV3;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
@Singleton
@Transactional
public class PaymentConsentService extends BaseBankService {

    @Value("${mockbank.mockbankUrl}")
    private String appBaseUrl;

    private final PaymentAutomations paymentAutomations;
    private final PaymentAutomationsV2 paymentAutomationsV2;

    private final List<PaymentConsentValidator> consentValidators = List.of(
            new CurrencyValidator(new PaymentErrorMessageV1()),
            new ConsentPaymentFieldValidator(),
            new ConsentCreditorFieldValidator(),
            new PaymentConsentUserDocumentPresentValidator(),
            new ConsentQrCodeValidator(new ConsentQrCodeValidationErrorsV1(new PaymentErrorMessageV1()))
    );

    private final List<PaymentConsentValidator> consentValidatorsV2 = List.of(
            new CurrencyValidator(new PaymentErrorMessageV2()),
            new ConsentQrCodeValidatorV2(),
            new ConsentPaymentFieldValidatorV2(new PaymentErrorMessageV2())
    );

    private final List<PaymentConsentValidator> consentValidatorsV3 = List.of(
            new CurrencyValidator(new PaymentErrorMessageV2()),
            new ConsentQrCodeValidatorV2(),
            new ConsentQrCodeValidator(new ConsentQrCodeValidationErrorsV1(new PaymentErrorMessageV2())),
            new ConsentPaymentFieldValidatorV2(new PaymentErrorMessageV2()),
            new ConsentDebtorAccountValidatorV3(new PaymentErrorMessageV2())
    );

    private final List<PaymentConsentRejectionValidator> consentRejectionValidatorsV3;

    private final List<PaymentConsentValidatorV4> consentValidatorsV4 = List.of(
            new CurrencyValidatorV4(new PaymentErrorMessageV2()),
            new ConsentQrCodeValidatorV4(),
            new ConsentPaymentFieldValidatorV4(new PaymentErrorMessageV2()),
            new ConsentDebtorAccountValidatorV4(new PaymentErrorMessageV2())
    );

    private final List<RecurringPaymentConsentValidatorV1> recurringPaymentConsentValidatorsV1 = List.of(
            new RecurringPaymentConsentFieldsValidatorV1(new PaymentErrorMessageV2())
    );

    private final List<RecurringPaymentConsentPatchValidatorV1> recurringConsentPatchValidatorsV1 = List.of(
            new RecurringPaymentConsentPatchFieldsValidatorV1()
    );


    @Inject
    public PaymentConsentService(PaymentAutomations paymentAutomations, PaymentAutomationsV2 paymentAutomationsV2,
                                 AccountHolderRepository accountHolderRepository,
                                 AccountRepository accountRepository) {
        this.paymentAutomations = paymentAutomations;
        this.paymentAutomationsV2 = paymentAutomationsV2;

        this.consentRejectionValidatorsV3 = List.of(
                new PaymentConsentCreditorDebtorRejectionValidator(new PaymentConsentCreditorDebtorRejectionValidatorExceptionsV3()),
                new PaymentConsentDebtorAmountRejectionValidator(new PaymentConsentDebtorAmountRejectionValidatorExceptionsV3(), accountHolderRepository, accountRepository)
        );
    }

    public ResponsePaymentConsent createConsentV1(String clientId, String idempotencyKey, String jti, CreatePaymentConsent body) {
        paymentAutomations.executeImmediateResponses(clientId, body.getData().getPayment().getAmount(), ErrorCodesEnumV1.DETALHE_PGTO_INVALIDO.toString());
        validateConsentRequest(body);
        return createConsent(clientId, idempotencyKey, jti, body).getDTO();
    }

    public ResponsePaymentConsentV2 createConsentV2(String clientId, String idempotencyKey, String jti, CreatePaymentConsent body) {
        paymentAutomationsV2.executeConsentImmediateResponses(body.getData().getPayment().getAmount());
        validateConsentRequestV2(body);
        return createConsent(clientId, idempotencyKey, jti, body).getDTOV2();
    }

    public ResponsePaymentConsentV2 createConsentV3(String clientId, String idempotencyKey, String jti, CreatePaymentConsent body) {
        paymentAutomationsV2.executeConsentImmediateResponses(body.getData().getPayment().getAmount());
        validateConsentRequestV3(body);
        PaymentConsentEntity consent = createConsent(clientId, idempotencyKey, jti, body);
        consent = validateConsentRejectionV3(consent, body);
        return consent.getDTOV2();
    }

    public ResponseRecurringConsent createRecurringConsentV1(String clientId, String idempotencyKey, String jti, CreateRecurringConsentV1 body) {
        paymentAutomationsV2.executeConsentImmediateResponses(Optional.ofNullable(body.getData().getRecurringConfiguration())
                .map(AllOfCreateRecurringConsentV1DataRecurringConfiguration::getSweeping)
                .map(SweepingSweeping::getTotalAllowedAmount).orElse("sweeping configuration is not present so i have no idea where we should take payment from"));

        paymentAutomations.executeClientRestrictions(clientId);
        validateRecurringConsentRequestV1(body);
        BankLambdaUtils.checkAndSaveJti(jtiRepository, jti);
        var loggedUserDocument = body.getData().getLoggedUser().getDocument();
        var accountHolder = getAccountHolder(loggedUserDocument.getIdentification(), loggedUserDocument.getRel());
        var accountWithDebtor = checkAndSetDebtor(body.getData().getDebtorAccount(), accountHolder);
        return paymentConsentRepository.findByIdempotencyKey(idempotencyKey)
                .orElseGet(() -> paymentConsentRepository
                        .save(PaymentConsentEntity.fromRecurringV1(body, clientId, idempotencyKey, accountWithDebtor.orElse(null), accountHolder)))
                .getRecurringDTOV1();
    }

    private PaymentConsentEntity validateConsentRejectionV3(PaymentConsentEntity consentEntity, CreatePaymentConsent request) {
        var consent = consentEntity;
        try {
            for (var paymentConsentRejectionValidator : consentRejectionValidatorsV3) {
                paymentConsentRejectionValidator.validate(request);
            }
        } catch (ConsentRejectionException e) {
            consent.setStatus(EnumConsentStatus.REJECTED.name());
            consent.setRejectReasonCode(e.getRejectionReason());
            consent.setRejectReasonDetail(e.getRejectionDetail());
            consent = paymentConsentRepository.update(consentEntity);
        }
        return consent;
    }

    public ResponseCreatePaymentConsentV4 createConsentV4(String clientId, String idempotencyKey, String jti, CreatePaymentConsentV4 body) {
        paymentAutomationsV2.executeConsentImmediateResponses(body.getData().getPayment().getAmount());
        paymentAutomations.executeClientRestrictions(clientId);
        BankLambdaUtils.checkAndSaveJti(jtiRepository, jti);
        validateConsentRequestV4(body);

        PaymentConsentEntity consent = saveConsentV4(clientId, idempotencyKey, body);

        return consent.getCreateDTOV4();
    }

    private PaymentConsentEntity createConsent(String clientId, String idempotencyKey, String jti, CreatePaymentConsent body) {
        paymentAutomations.executeClientRestrictions(clientId);
        BankLambdaUtils.checkAndSaveJti(jtiRepository, jti);

        var userDocument = body.getData().getLoggedUser().getDocument();
        var accountHolder = getAccountHolder(userDocument.getIdentification(), userDocument.getRel());

        var accountWithDebtor = checkAndSetDebtor(body.getData().getDebtorAccount(), accountHolder);

        return paymentConsentRepository.findByIdempotencyKey(idempotencyKey)
                .orElseGet(() -> paymentConsentRepository
                        .save(PaymentConsentEntity.from(body, clientId, idempotencyKey, accountWithDebtor.orElse(null), accountHolder)));
    }

    private AccountHolderEntity getAccountHolder(String identification, String rel) {
        return accountHolderRepository
                .findByDocumentIdentificationAndDocumentRel(identification, rel)
                .stream()
                .findAny()
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, String.format("User with documentation Id %s and rel %s not found", identification, rel)));
    }

    private PaymentConsentEntity saveConsentV4(String clientId, String idempotencyKey, CreatePaymentConsentV4 body) {
        var userDocument = body.getData().getLoggedUser().getDocument();
        var accountHolder = getAccountHolder(userDocument.getIdentification(), userDocument.getRel());

        var accountWithDebtor = checkAndSetDebtor(body.getData().getDebtorAccount(), accountHolder);

        return paymentConsentRepository.findByIdempotencyKey(idempotencyKey)
                .orElseGet(() -> paymentConsentRepository
                        .save(PaymentConsentEntity.fromV4(body, clientId, idempotencyKey, accountWithDebtor.orElse(null), accountHolder)));
    }

    public ResponsePaymentConsent updateConsent(String consentId, String clientId, UpdatePaymentConsent body) {
        paymentAutomations.executeClientRestrictions(clientId);
        paymentAutomations.executePostConsentCreationActions(consentId);

        return updatePaymentConsentEntity(consentId, body);
    }

    public ResponsePaymentConsent updateRecurringConsentV1(String recurringConsentId, UpdatePaymentConsent body) {
        return updatePaymentConsentEntity(recurringConsentId, body);
    }

    private ResponsePaymentConsent updatePaymentConsentEntity(String consentId, UpdatePaymentConsent body) {
        var paymentConsentEntity = BankLambdaUtils.getPaymentConsent(consentId, paymentConsentRepository);

        validateDebtor(paymentConsentEntity, body);
        validateStatus(paymentConsentEntity, body);
        paymentConsentEntity.setStatus(body.getData().getStatus().name());
        return paymentConsentRepository.update(paymentConsentEntity).getDTO();
    }

    private void validateStatus(PaymentConsentEntity paymentConsentEntity,UpdatePaymentConsent body) {
        var status = body.getData().getStatus().name();
        if (UpdatePaymentConsentData.StatusEnum.AUTHORISED.name().equals(status) && paymentConsentEntity.getAccountEntity() == null) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Debtor account must be set in the request");
        }

        if (UpdatePaymentConsentData.StatusEnum.REJECTED.name().equals(status)) {
            paymentConsentEntity.setRejectReasonCode(EnumConsentRejectionReasonType.REJEITADO_USUARIO.name());
            paymentConsentEntity.setRejectReasonDetail("O usuário rejeitou a autorização do consentimento");
        }
    }
    private void validateDebtor(PaymentConsentEntity paymentConsentEntity,UpdatePaymentConsent body) {
        DebtorAccount debtor = body.getData().getDebtorAccount();
        if (debtor != null && paymentConsentEntity.getAccountEntity() != null) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Debtor account already set in initial consent");
        }

        if (debtor != null) {
            Optional<AccountEntity> account = checkAndSetDebtor(debtor, paymentConsentEntity.getAccountHolder());
            if (account.isPresent() && paymentConsentEntity.getAccountEntity() == null) {
                paymentConsentEntity.setAccountId(account.get().getAccountId());
                paymentConsentEntity.setAccountEntity(account.get());
            }
        }


    }
    public ResponsePaymentConsent getConsent(String consentId, String clientId) {
        paymentAutomations.executeClientRestrictions(clientId);
        paymentAutomations.executePostConsentCreationActions(consentId);

        PaymentConsentEntity paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "No payment consent with ID " + consentId + " found"));
        if (!paymentConsentEntity.getClientId().equals(clientId)) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Requested a payment consent created with a different oauth client");
        }
        ResponsePaymentConsent paymentConsentResponse = paymentConsentEntity.getDTO();
        String responseConsentId = paymentConsentResponse.getData().getConsentId();
        paymentConsentResponse.setLinks(new Links().self(appBaseUrl + "/open-banking/payments/v1/consents/" + responseConsentId));
        paymentConsentResponse.setMeta(new Meta()
                .totalRecords(1)
                .totalPages(1)
                .requestDateTime(OffsetDateTime.now())
        );
        return paymentConsentResponse;
    }

    public ResponsePaymentConsentV2 getConsentV2(String consentId, String clientId) {
        paymentAutomations.executeClientRestrictions(clientId);
        paymentAutomations.executePostConsentCreationActions(consentId);

        PaymentConsentEntity paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "No payment consent with ID " + consentId + " found"));
        if (!paymentConsentEntity.getClientId().equals(clientId)) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Requested a payment consent created with a different oauth client");
        }
        ResponsePaymentConsentV2 paymentConsentResponse = paymentConsentEntity.getDTOV2();
        String responseConsentId = paymentConsentResponse.getData().getConsentId();
        paymentConsentResponse.setLinks(new Links().self(appBaseUrl + "/open-banking/payments/v2/consents/" + responseConsentId));
        paymentConsentResponse.setMeta(new MetaOnlyRequestDateTime().requestDateTime(OffsetDateTime.now()));
        return paymentConsentResponse;
    }

    public ResponsePaymentConsentV3 getConsentV3(String consentId, String clientId) {
        paymentAutomations.executeClientRestrictions(clientId);
        paymentAutomations.executePostConsentCreationActions(consentId);
        paymentAutomations.executePostConsentCreationActionsV3(consentId);

        PaymentConsentEntity paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "No payment consent with ID " + consentId + " found"));
        if (!paymentConsentEntity.getClientId().equals(clientId)) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Requested a payment consent created with a different oauth client");
        }
        ResponsePaymentConsentV3 paymentConsentResponse = paymentConsentEntity.getDTOV3();
        String responseConsentId = paymentConsentResponse.getData().getConsentId();
        paymentConsentResponse.setLinks(new Links().self(appBaseUrl + "/open-banking/payments/v3/consents/" + responseConsentId));
        paymentConsentResponse.setMeta(new MetaOnlyRequestDateTime().requestDateTime(OffsetDateTime.now()));
        return paymentConsentResponse;
    }

    public ResponsePaymentConsentV4 getConsentV4(String consentId, String clientId) {
        paymentAutomations.executeClientRestrictions(clientId);
        paymentAutomations.executePostConsentCreationActions(consentId);
        paymentAutomations.executePostConsentCreationActionsV3(consentId);

        PaymentConsentEntity paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "No payment consent with ID " + consentId + " found"));
        if (!paymentConsentEntity.getClientId().equals(clientId)) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Requested a payment consent created with a different oauth client");
        }

        return paymentConsentEntity.getDTOV4();
    }

    public ResponsePaymentConsentFull getConsentFull(String consentId, String clientId) {
        paymentAutomations.executeClientRestrictions(clientId);
        paymentAutomations.executePostConsentCreationActions(consentId);

        return getConsentFull(consentId);
    }

    public ResponsePaymentConsentFull getConsentFull(String consentId) {
        PaymentConsentEntity paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "No payment consent with ID " + consentId + " found"));
        return paymentConsentEntity.getFullDTO();
    }

    public ResponseRecurringConsent getRecurringConsentsV1(String recurringConsentId, String clientId) {
        paymentAutomations.executeClientRestrictions(clientId);

        PaymentConsentEntity paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(recurringConsentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "No recurring payment consent with ID " + recurringConsentId + " found"));
        if (!paymentConsentEntity.getClientId().equals(clientId)) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Requested a recurring payment consent created with a different oauth client");
        }

        return paymentConsentEntity.getRecurringDTOV1();
    }

    public ResponseRecurringConsent patchRecurringConsentV1(String consentId, String clientId, PatchRecurringConsentV1 body) {
        paymentAutomations.executeClientRestrictions(clientId);
        paymentAutomations.executePostConsentCreationActions(consentId);

        PaymentConsentEntity consentEntity = paymentConsentRepository.findByPaymentConsentId(consentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "consent not found"));

        recurringConsentPatchValidatorsV1.forEach(v -> v.validate(body, consentEntity));

        var requestData = body.getData();
        Date currentTime = Date.from(Instant.now());
        switch (requestData.getStatus()) {
            case REJECTED:
                var rejection = requestData.getRejection();
                consentEntity.setRejectedBy(rejection.getRejectedBy());
                consentEntity.setRejectedFrom(rejection.getRejectedFrom());
                consentEntity.setRejectReasonCode(rejection.getReason().getCode().toString());
                consentEntity.setRejectReasonDetail(rejection.getReason().getDetail());
                consentEntity.setRejectedAt(currentTime);
                consentEntity.setStatus(EnumAuthorisationStatusType.REJECTED.toString());
                break;
            case REVOKED:
                var revocation = requestData.getRevocation();
                consentEntity.setRevokedBy(revocation.getRevokedBy());
                consentEntity.setRevokedFrom(revocation.getRevokedFrom());
                consentEntity.setRevokeReasonCode(revocation.getReason().getCode().toString());
                consentEntity.setRevokeReasonDetail(revocation.getReason().getDetail());
                consentEntity.setRevokedAt(currentTime);
                consentEntity.setStatus(EnumAuthorisationStatusType.REVOKED.toString());
                break;
        }

        consentEntity.setStatusUpdateDateTime(currentTime);
        return paymentConsentRepository.update(consentEntity)
                .getRecurringDTOV1();

    }

    private void validateConsentRequest(CreatePaymentConsent body) {
        consentValidators.forEach(v -> v.validate(body));
    }

    private void validateConsentRequestV2(CreatePaymentConsent body) {
        consentValidatorsV2.forEach(v -> v.validate(body));
    }

    private void validateConsentRequestV3(CreatePaymentConsent body) {
        consentValidatorsV3.forEach(v -> v.validate(body));
    }

    private void validateConsentRequestV4(CreatePaymentConsentV4 body) {
        consentValidatorsV4.forEach(v -> v.validate(body));
    }

    private void validateRecurringConsentRequestV1(CreateRecurringConsentV1 body) {
        recurringPaymentConsentValidatorsV1.forEach(v -> v.validate(body));
    }

    private Optional<AccountEntity> checkAndSetDebtor(DebtorAccount debtor, AccountHolderEntity accountHolder) {
        if (debtor == null) return Optional.empty();
        AccountEntity account = accountRepository.findByNumberAndAccountHolderId(debtor.getNumber(), accountHolder.getAccountHolderId())
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Account with number %s not found", debtor.getNumber())));
        if (account.setDebtorAccount(debtor) != null) {
            return Optional.of(accountRepository.update(account));
        }
        return Optional.empty();
    }
}
