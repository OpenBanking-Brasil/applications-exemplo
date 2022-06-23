package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.domain.*;
import com.raidiam.trustframework.bank.repository.JtiRepository;
import com.raidiam.trustframework.bank.repository.PaymentConsentRepository;
import com.raidiam.trustframework.bank.repository.PixPaymentRepository;
import com.raidiam.trustframework.bank.services.validate.*;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaymentsService extends BaseBankService {

    Logger log = LoggerFactory.getLogger(PaymentsService.class);

    private final PaymentConsentRepository paymentConsentRepository;
    private final PixPaymentRepository pixPaymentRepository;
    private final CnpjVerifier cnpjVerifier;

    private final List<PaymentConsentValidator> consentValidators = List.of(
            new CurrencyValidator(),
            new ConsentPaymentFieldValidator(),
            new ConsentCreditorFieldValidator(),
            new PaymentConsentUserDocumentPresentValidator()
    );

    private final List<PaymentPatchValidator> paymentPatchValidators = List.of(
            new PaymentPatchConsentValidator()
    );

    @Value("${mockbank.mockbankUrl}")
    private String appBaseUrl;

    @Inject
    private JtiRepository jtiRepository;

    static final String VERSION_DIFF = "New version of resource not the same";

    static final String CENTS = "cents";

    PaymentsService (PaymentConsentRepository paymentConsentRepository,
                     PixPaymentRepository pixPaymentRepository,
                     CnpjVerifier cnpjVerifier) {
        this.paymentConsentRepository = paymentConsentRepository;
        this.pixPaymentRepository = pixPaymentRepository;
        this.cnpjVerifier = cnpjVerifier;
    }

    @Transactional
    public ResponsePaymentConsent createConsent(String clientId, String idempotencyKey, String jti, CreatePaymentConsent body) {
        validateConsentRequest(body);
        checkForForcedException(body);
//        if(!cnpjVerifier.isKnownCnpj(body.getData().getBusinessEntity())) {
//           throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Unregistered CNPJ");
//        }
        checkAndSaveJti(jti);

        var userDocument = body.getData().getLoggedUser().getDocument();
        var accountHolder = accountHolderRepository
                .findByDocumentIdentificationAndDocumentRel(userDocument.getIdentification(), userDocument.getRel())
                .stream()
                .findAny()
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, String.format("User with documentation Id %s and rel %s not found", userDocument.getIdentification(), userDocument.getRel())));

        var accountWithDebtor = checkAndSetDebtor(body.getData().getDebtorAccount(), accountHolder);

        return paymentConsentRepository.findByIdempotencyKey(idempotencyKey)
                .orElseGet(() -> paymentConsentRepository
                        .save(PaymentConsentEntity.from(body, clientId, idempotencyKey, accountWithDebtor.orElse(null), accountHolder))).getDTO();
    }

    @Transactional
    public ResponsePaymentConsent updateConsent(String consentId, UpdatePaymentConsent body) {
        var paymentConsentEntityOptional = paymentConsentRepository.findByPaymentConsentId(consentId);
        var paymentConsentEntity = paymentConsentEntityOptional.orElseThrow(() ->
                new HttpStatusException(HttpStatus.NOT_FOUND, "Requested payment consent not found"));

        DebtorAccount debtor = body.getData().getDebtorAccount();
        if (debtor != null && paymentConsentEntity.getAccountEntity() != null) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Debtor account already set in initial consent");
        }

        var status = body.getData().getStatus().name();
        if (debtor != null) {
            Optional<AccountEntity> account = checkAndSetDebtor(debtor, paymentConsentEntity.getAccountHolder());
            if (account.isPresent() && paymentConsentEntity.getAccountEntity() == null) {
                paymentConsentEntity.setAccountId(account.get().getAccountId());
                paymentConsentEntity.setAccountEntity(account.get());
            }
        }

        if (UpdatePaymentConsentData.StatusEnum.AUTHORISED.name().equals(status) && paymentConsentEntity.getAccountEntity() == null) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Debtor account must be set in the request");
        }

        paymentConsentEntity.setStatus(status);
        return paymentConsentRepository.update(paymentConsentEntity).getDTO();
    }

    public ResponsePaymentConsent getConsent(String consentId, String clientId) {
        PaymentConsentEntity paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "No payment consent with ID " + consentId + " found"));
        if(!paymentConsentEntity.getClientId().equals(clientId)) {
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

    public ResponsePaymentConsentFull getConsentFull(String consentId) {
        PaymentConsentEntity paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "No payment consent with ID " + consentId + " found"));
        return paymentConsentEntity.getFullDTO();
    }

    @Transactional
    public ResponsePixPayment createPayment(String consentId, String idempotencyKey, String jti, CreatePixPayment body) {
        checkForForcedException(body);
        checkAndSaveJti(jti);

        EnumPaymentStatusType subsequentStatus = null;
        if(!cnpjVerifier.isKnownCnpj(body.getData().getCnpjInitiator())) {
            var paymentConsentOptional = paymentConsentRepository.findByPaymentConsentId(consentId);
            if(paymentConsentOptional.isPresent()) {
                var paymentConsentEntity = paymentConsentOptional.get();
                paymentConsentEntity.setStatus(ResponsePaymentConsentData.StatusEnum.CONSUMED.toString());
                paymentConsentRepository.update(paymentConsentEntity);
            }
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "NAO_INFORMADO: Não reportado/identificado pela instituição detentora de conta. CNPJ não registrado.");
        }

        if(body.getData().getTransactionIdentification() != null && !body.getData().getLocalInstrument().equals(EnumLocalInstrument.INIC)){
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "NAO_INFORMADO: Não reportado/identificado pela instituição detentora de conta. CNPJ não registrado.");
        }

        log.info("Idempotency key - {}", idempotencyKey);
        var existingPayment = pixPaymentRepository.findByIdempotencyKey(idempotencyKey);
        PixPaymentEntity paymentEntity;
        if(existingPayment.isEmpty()) {
            subsequentStatus = checkForForcedStatus(body);
            // check that there is a consent appropriate to this payment
            var paymentConsentOptional = paymentConsentRepository.findByPaymentConsentId(consentId);
            // check that it has the right status
            if (paymentConsentOptional.isEmpty()) {
                throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Requested pix payment has no associated consent");
            }

            var paymentConsentEntity = paymentConsentOptional.get();
            if(Instant.now().isAfter(paymentConsentEntity.getExpirationDateTime().toInstant())){
                throw new HttpStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Requested pix payment has associated consent, but the consent is expired - %s", paymentConsentEntity.getExpirationDateTime()));
            }
            if(ResponsePaymentConsentData.StatusEnum.CONSUMED.toString().equals(paymentConsentEntity.getStatus())) {
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        String.format("CONSENTIMENTO_INVALIDO: Consentimento inválido - consentimento encontrado, mas que expirou - %s", paymentConsentEntity.getStatus()));
            }
            if(!ResponsePaymentConsentData.StatusEnum.AUTHORISED.toString().equals(paymentConsentEntity.getStatus())) {
                throw new HttpStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Requested pix payment has associated consent, but the consent status is not AUTHORISED - %s", paymentConsentEntity.getStatus()));
            }

            if(!body.getData().getPayment().getAmount().equals(paymentConsentEntity.getPaymentConsentPaymentEntity().getAmount())){
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        String.format("VALOR_INCOMPATIVEL: O valor informado no consentimento não é o mesmo valor do informado no payload de pagamento. amount consentimento - %s, amount pagamento- %s",
                                paymentConsentEntity.getPaymentConsentPaymentEntity().getAmount(), body.getData().getPayment().getAmount()));
            }

            if(!body.getData().getPayment().getCurrency().equals(paymentConsentEntity.getPaymentConsentPaymentEntity().getCurrency())){
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        String.format("PAGAMENTO_DIVERGENTE_DO_CONSENTIMENTO: O valor informado no consentimento não é o mesmo valor do informado no payload de pagamento. currency consentimento - %s, currency pagamento- %s",
                                paymentConsentEntity.getPaymentConsentPaymentEntity().getAmount(), body.getData().getPayment().getAmount()));
            }
            // continue with the payment
            paymentConsentEntity.setStatus(ResponsePaymentConsentData.StatusEnum.CONSUMED.toString());
            var updatedEntity = paymentConsentRepository.update(paymentConsentEntity);

            var pixPaymentEntity = PixPaymentEntity.from(body.getData(), updatedEntity, idempotencyKey);
            if(subsequentStatus != null) {
                pixPaymentEntity.setStatus(subsequentStatus.toString());
            }
            paymentEntity = pixPaymentRepository.save(pixPaymentEntity);
        } else {
            validateSamePayload(body, existingPayment.get());
            paymentEntity = existingPayment.get();
        }
        ResponsePixPayment responsePixPayment = paymentEntity.getDTO();
        if(subsequentStatus != null) {
            responsePixPayment.getData().setStatus(EnumPaymentStatusType.PDNG);
        }

        return responsePixPayment;
    }
    private EnumPaymentStatusType checkForForcedStatus(CreatePixPayment body) {
        String amount = Optional.ofNullable(body)
                .map(CreatePixPayment::getData)
                .map(CreatePixPaymentData::getPayment)
                .map(PaymentPix::getAmount)
                .orElse("");

        Pattern patternOne = Pattern.compile("^20201.(?<cents>\\d{2})$");
        Matcher matcherOne = patternOne.matcher(amount);

        Pattern patternTwo = Pattern.compile("^133(?<increment>\\d).00$");
        Matcher matcherTwo = patternTwo.matcher(amount);
        if(matcherOne.matches()){
            int cents = Integer.parseInt(matcherOne.group(CENTS));
            if(cents == 0){
                log.info("Payment amount on payment request was 20201.00 - accept as pending but then mark the payment as rejected");
                return EnumPaymentStatusType.RJCT;
            }
        } else if(matcherTwo.matches()) {
            int increment = Integer.parseInt(matcherTwo.group("increment"));
            switch (increment) {
                case 3:
                    log.info("Payment amount on payment request was 1333.00 - accept as pending but then mark the payment as accepted");
                    return EnumPaymentStatusType.ACCC;
                case 4:
                    log.info("Payment amount on payment request was 1334.00 - accept as pending but then mark the payment as ACSC");
                    return EnumPaymentStatusType.ACSC;
                case 5:
                    log.info("Payment amount on payment request was 1335.00 - accept as pending but then mark the payment as ACSP");
                    return EnumPaymentStatusType.ACSP;
                default:
            }
        }
        return null;
    }

    private void checkForForcedException(CreatePixPayment body) {
        String amount = Optional.ofNullable(body)
                .map(CreatePixPayment::getData)
                .map(CreatePixPaymentData::getPayment)
                .map(PaymentPix::getAmount)
                .orElse("");
        log.info("Pix payment amount was {}", amount);
        Pattern pattern = Pattern.compile("^20422.(?<cents>\\d{2})$");
        Matcher matcher = pattern.matcher(amount);
        if(matcher.matches()) {
            int cents = Integer.parseInt(matcher.group(CENTS));
            String errorMessage = "Forced a 422 for payment request";
            switch (cents) {
                case 1:
                    errorMessage = "DETALHE_PGTO_INVALIDO";
                    break;
                case 2:
                    errorMessage = "COBRANCA_INVALIDA";
                    break;
                case 3:
                    errorMessage = "PAGAMENTO_DIVERGENTE_DO_CONSENTIMENTO";
                    break;
                default:
            }
            log.info("Payment amount on payment request was {} - throwing an HTTP 422 regardless, with message {}", amount, errorMessage);
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, errorMessage);
        }
    }

    private void checkForForcedException(CreatePaymentConsent request) {
        String amount = Optional.ofNullable(request)
                .map(CreatePaymentConsent::getData)
                .map(CreatePaymentConsentData::getPayment)
                .map(PaymentConsent::getAmount)
                .orElse("");
        Pattern pattern = Pattern.compile("^10422.(?<cents>\\d{2})$");
        Matcher matcher = pattern.matcher(amount);
        if(matcher.matches()) {
            int cents = Integer.parseInt(matcher.group(CENTS));
            String errorMessage = "Forced a 422 for payment consent request";
            switch (cents) {
                case 1:
                    errorMessage = "DETALHE_PGTO_INVALIDO";
                    break;

                default:
            }
            log.info("Payment amount on payment consent request was {} - throwing an HTTP 422 regardless, with message {}", amount, errorMessage);
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, errorMessage);
        }

    }

    private void validateSamePayload(CreatePixPayment request, PixPaymentEntity entity) {
        CreatePixPaymentData requestData = request.getData();
        if(requestData.getProxy() != null){
            assertSame(requestData.getProxy(), entity.getProxy());
        } else if(entity.getProxy() != null){
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, VERSION_DIFF);
        }
        assertSame(requestData.getLocalInstrument().toString(), entity.getLocalInstrument());
        if(requestData.getQrCode() != null){
            assertSame(requestData.getQrCode(), entity.getQrCode());
        } else if(entity.getQrCode() != null){
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, VERSION_DIFF);
        }
        assertSame(requestData.getRemittanceInformation(), entity.getRemittanceInformation());
        CreditorAccount creditorAccount = requestData.getCreditorAccount();
        CreditorAccountEntity creditorAccountEntity = entity.getCreditorAccountEntity();
        assertSame(creditorAccount.getAccountType().toString(), creditorAccountEntity.getAccountType());
        assertSame(creditorAccount.getIspb(), creditorAccountEntity.getIspb());
        assertSame(creditorAccount.getIssuer(), creditorAccountEntity.getIssuer());
        assertSame(creditorAccount.getNumber(), creditorAccountEntity.getNumber());
        PaymentPix payment = requestData.getPayment();
        PixPaymentPaymentEntity pixPaymentPaymentEntity = entity.getPixPaymentPaymentEntity();
        assertSame(payment.getCurrency(), pixPaymentPaymentEntity.getCurrency());
        assertSame(payment.getAmount(), pixPaymentPaymentEntity.getAmount());
    }

    private void assertSame(Object inbound, Object current) {
        if(!inbound.equals(current)) {
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, VERSION_DIFF);
        }
    }

    public ResponsePixPayment getPayment(String paymentId) {
        ResponsePixPayment responsePixPayment = pixPaymentRepository.findByPaymentId(paymentId).map(PixPaymentEntity::getDTO).orElse(null);
        if (responsePixPayment != null){
            var paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(responsePixPayment.getData().getConsentId());
            if (paymentConsentEntity.isPresent() && paymentConsentEntity.get().getStatus().equals(ResponsePaymentConsentData.StatusEnum.CONSUMED.toString())){
                responsePixPayment = checkScheduleDate(paymentConsentEntity.get(),paymentId);
            }
        }
        return responsePixPayment;
    }

    private ResponsePixPayment checkScheduleDate(PaymentConsentEntity paymentConsent, String paymentId){
        Optional<PixPaymentEntity> payment = pixPaymentRepository.findByPaymentId(paymentId);
        Date dateOfToday = new Date();
        LocalDate currentDate = dateOfToday.toInstant().atZone(ZoneId.of("America/Sao_Paulo")).toLocalDate();
        Date schedule = paymentConsent.getPaymentConsentPaymentEntity().getSchedule();
        LocalDate scheduleDate = BankLambdaUtils.dateToLocalDate(schedule);
            if (schedule != null){
                if(scheduleDate.isBefore(currentDate) || scheduleDate.isEqual(currentDate)){
                    payment.ifPresent(p -> p.setStatus(EnumPaymentStatusType.ACCC.toString()));
            }   else if(scheduleDate.isAfter(currentDate)){
                    payment.ifPresent(p -> p.setStatus(EnumPaymentStatusType.SASC.toString()));
            }
        }
        payment.ifPresent(pixPaymentRepository::update);
        return  pixPaymentRepository.findByPaymentId(paymentId).map(PixPaymentEntity::getDTO).orElse(null);
    }

    public ResponsePixPayment updatePayment(String paymentId, UpdatePixPayment body) {
        var pixPaymentEntityOptional = pixPaymentRepository.findByPaymentId(paymentId);
        if (pixPaymentEntityOptional.isPresent()) {
            var pixPaymentEntity = pixPaymentEntityOptional.get();
            pixPaymentEntity.setStatus(body.getData().getStatus().toString());
            pixPaymentRepository.update(pixPaymentEntity);
            return pixPaymentEntity.getDTO();
        }
        throw new HttpStatusException(HttpStatus.NOT_FOUND, "Requested pix payment not found");
    }

    private void validateConsentRequest(CreatePaymentConsent body) {
        consentValidators.forEach(v -> v.validate(body));
    }

    @Transactional
    public ResponsePaymentConsent patchConsent(String consentId, PatchPaymentsConsent body){
        paymentPatchValidators.forEach(v -> v.validate(body));
        Optional<PaymentConsentEntity> paymentConsentEntityOptional = paymentConsentRepository.findByPaymentConsentId(consentId);
        PaymentConsentEntity paymentConsentEntity = paymentConsentEntityOptional.orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Requested payment consent not found"));

        if (!UpdatePaymentConsentData.StatusEnum.CONSUMED.equals(UpdatePaymentConsentData.StatusEnum.valueOf(paymentConsentEntity.getStatus()))){
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "OPERACAO_NAO_PERMITIDA_STATUS");
        }
        if (paymentConsentEntity.getPaymentConsentPaymentEntity().getSchedule() == null){
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "OPERACAO_NAO_SUPORTADA_TIPO_CONSENTIMENTO");
        }
        Optional<PixPaymentEntity> paymentEntityOptional = pixPaymentRepository.findByPaymentConsentEntity(paymentConsentEntity);
        PixPaymentEntity paymentEntity = paymentEntityOptional.orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Requested payment not found"));


        LocalDate scheduleDate = BankLambdaUtils.dateToLocalDate(paymentConsentEntity.getPaymentConsentPaymentEntity().getSchedule());
        validateScheduleDate(scheduleDate);
        String paymentStatus = paymentEntity.getStatus();
        EnumPaymentStatusType[] test = new EnumPaymentStatusType[]{EnumPaymentStatusType.PDNG, EnumPaymentStatusType.SASP, EnumPaymentStatusType.SASC};
        if (Arrays.asList(test).contains(EnumPaymentStatusType.fromValue(paymentStatus))) {
            paymentEntity.setStatus(EnumPaymentStatusType.RJCT.toString());
            pixPaymentRepository.update(paymentEntity);
            paymentConsentEntity.setStatus(body.getData().getStatus().toString());
        }
        paymentConsentRepository.update(paymentConsentEntity);
        return paymentConsentEntity.getDTO();
    }

    public void validateScheduleDate(LocalDate scheduledDate){
        Date dateOfToday = new Date();
        LocalDate currentDate = dateOfToday.toInstant().atZone(ZoneId.of("America/Sao_Paulo")).toLocalDate();
        if (currentDate.equals(scheduledDate) || currentDate.isAfter(scheduledDate)){
            throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "TEMPO_LIMITE_REVOGACAO_EXCEDIDO");
        }
    }

    private void checkAndSaveJti(String jti) {
        if(jtiRepository.getByJti(jti).isPresent()) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "DETALHE_PGTO_INVALIDO: Detalhe do pagamento inválido. JTI Reutilizada.");
        }

        JtiEntity jtiEntity = new JtiEntity();
        jtiEntity.setJti(jti);
        jtiRepository.save(jtiEntity);
    }

    private Optional<AccountEntity> checkAndSetDebtor(DebtorAccount debtor, AccountHolderEntity accountHolder){
        if (debtor == null) return Optional.empty();
        AccountEntity account = accountHolder
                .getAccountByNumber(debtor.getNumber())
                .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST,
                        String.format("User with number %s not found", debtor.getNumber())));
        if (account.setDebtorAccount(debtor) != null) {
            return Optional.of(accountRepository.update(account));
        }
        return Optional.empty();
    }
}
