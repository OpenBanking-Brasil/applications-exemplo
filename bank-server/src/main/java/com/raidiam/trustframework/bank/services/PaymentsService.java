package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.domain.DebtorAccountEntity;
import com.raidiam.trustframework.bank.domain.PaymentConsentEntity;
import com.raidiam.trustframework.bank.domain.PixPaymentEntity;
import com.raidiam.trustframework.bank.repository.PaymentConsentRepository;
import com.raidiam.trustframework.bank.repository.PixPaymentRepository;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.Transactional;
import java.time.Instant;

public class PaymentsService extends BaseBankService {

    Logger log = LoggerFactory.getLogger(PaymentsService.class);

    private final PaymentConsentRepository paymentConsentRepository;
    private final PixPaymentRepository pixPaymentRepository;
    private final CnpjVerifier cnpjVerifier;

    PaymentsService (PaymentConsentRepository paymentConsentRepository,
                     PixPaymentRepository pixPaymentRepository,
                     CnpjVerifier cnpjVerifier) {
        this.paymentConsentRepository = paymentConsentRepository;
        this.pixPaymentRepository = pixPaymentRepository;
        this.cnpjVerifier = cnpjVerifier;
    }

    @Transactional
    public ResponsePaymentConsent createConsent(CreatePaymentConsent body, String clientId, String idempotencyKey) {
//        if(!cnpjVerifier.isKnownCnpj(body.getData().getBusinessEntity())) {
//           throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Unregistered CNPJ");
//        }
        var paymentConsentEntity = paymentConsentRepository.findByIdempotencyKey(idempotencyKey)
                .orElseGet(() -> paymentConsentRepository.save(PaymentConsentEntity.from(body, clientId, idempotencyKey)));
        return paymentConsentEntity.getDTO();
    }

    @Transactional
    public ResponsePaymentConsent updateConsent(String consentId, UpdatePaymentConsent body) {
        DebtorAccount debtorAccount = body.getData().getDebtorAccount();
        var paymentConsentEntityOptional = paymentConsentRepository.findByPaymentConsentId(consentId);
        var paymentConsentEntity = paymentConsentEntityOptional.orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Requested payment consent not found"));
        if(debtorAccount != null && paymentConsentEntity.getDebtorAccountEntity() != null) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Debtor account already set in initial consent");
        }
        paymentConsentEntity.setStatus(body.getData().getStatus().toString());
        if(debtorAccount != null) {
            paymentConsentEntity.setDebtorAccountEntity(DebtorAccountEntity.from(debtorAccount));
        }
        paymentConsentRepository.update(paymentConsentEntity);
        return paymentConsentEntity.getDTO();
    }

    public ResponsePaymentConsent getConsent(String consentId, String clientId) {
        PaymentConsentEntity paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "No payment consent with ID " + consentId + " found"));
        if(!paymentConsentEntity.getClientId().equals(clientId)) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Requested a payment consent created with a different oauth client");
        }
        return paymentConsentEntity.getDTO();
    }

    public ResponsePaymentConsentFull getConsentFull(String consentId, String clientId) {
        PaymentConsentEntity paymentConsentEntity = paymentConsentRepository.findByPaymentConsentId(consentId)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "No payment consent with ID " + consentId + " found"));
        return paymentConsentEntity.getFullDTO();
    }

    @Transactional
    public ResponsePixPayment createPayment(CreatePixPayment body, String consentId, String idempotencyKey) {
        if(!cnpjVerifier.isKnownCnpj(body.getData().getCnpjInitiator())) {
            var paymentConsentOptional = paymentConsentRepository.findByPaymentConsentId(consentId);
            if(paymentConsentOptional.isPresent()) {
                var paymentConsentEntity = paymentConsentOptional.get();
                paymentConsentEntity.setStatus(ResponsePaymentConsentData.StatusEnum.CONSUMED.toString());
                paymentConsentRepository.update(paymentConsentEntity);
            }
           throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Unregistered CNPJ");
        }
        log.info("IDEM KEY - {}", idempotencyKey);
        var existingPayment = pixPaymentRepository.findByIdempotencyKey(idempotencyKey);
        PixPaymentEntity paymentEntity;
        if(existingPayment.isEmpty()) {
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
                throw new HttpStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Requested pix payment has associated consent, but the consent was already consumed - %s", paymentConsentEntity.getStatus()));
            }

            if(!ResponsePaymentConsentData.StatusEnum.AUTHORISED.toString().equals(paymentConsentEntity.getStatus())) {
                throw new HttpStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Requested pix payment has associated consent, but the consent status is not AUTHORISED - %s", paymentConsentEntity.getStatus()));
            }

            if(!body.getData().getPayment().getAmount().equals(paymentConsentEntity.getPaymentConsentPaymentEntity().getAmount())){
                throw new HttpStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Requested pix payment amount - %s, does not match the consent amount - %s",
                                body.getData().getPayment().getAmount(), paymentConsentEntity.getPaymentConsentPaymentEntity().getAmount()));
            }

            if(!body.getData().getPayment().getCurrency().equals(paymentConsentEntity.getPaymentConsentPaymentEntity().getCurrency())){
                throw new HttpStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Requested pix payment currency - %s, does not match the consent currency - %s",
                                body.getData().getPayment().getCurrency(), paymentConsentEntity.getPaymentConsentPaymentEntity().getCurrency()));
            }

            // continue with the payment
            paymentConsentEntity.setStatus(ResponsePaymentConsentData.StatusEnum.CONSUMED.toString());
            var updatedEntity = paymentConsentRepository.update(paymentConsentEntity);

            var pixPaymentEntity = PixPaymentEntity.from(body.getData(), updatedEntity, idempotencyKey);
            paymentEntity = pixPaymentRepository.save(pixPaymentEntity);
        } else {
            paymentEntity = existingPayment.get();
        }
        return paymentEntity.getDTO();
    }

    public ResponsePixPayment getPayment(String paymentId) {
        return pixPaymentRepository.findByPaymentId(paymentId).map(PixPaymentEntity::getDTO).orElse(null);
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
}
