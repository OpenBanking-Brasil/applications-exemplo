package com.raidiam.trustframework.bank.services.automation;

import com.raidiam.trustframework.bank.domain.PaymentConsentEntity;
import com.raidiam.trustframework.bank.domain.PaymentsSimulateResponseEntity;
import com.raidiam.trustframework.bank.enums.ErrorCodesEnumV1;
import com.raidiam.trustframework.bank.repository.PaymentConsentRepository;
import com.raidiam.trustframework.bank.repository.PaymentsSimulateResponseRepository;
import com.raidiam.trustframework.bank.repository.PixPaymentRepository;
import com.raidiam.trustframework.mockbank.models.generated.EnumConsentRejectionReasonType;
import com.raidiam.trustframework.mockbank.models.generated.EnumPaymentStatusType;
import com.raidiam.trustframework.mockbank.models.generated.ResponsePaymentConsentData;
import com.raidiam.trustframework.mockbank.models.generated.ResponsePaymentConsentDataV3;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Singleton
public class PaymentAutomations {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentAutomations.class);

    @Inject
    PaymentsSimulateResponseRepository paymentsSimulateResponseRepository;

    @Inject
    PaymentConsentRepository paymentConsentRepository;

    @Inject
    PixPaymentRepository pixPaymentRepository;

    /**
     * Execute immediate action based on consent amount, or save an action for a client
     *
     * 1. Immediate throw, returns a specific error code *right now*
     * 2. Timed error response for a client - a client will receive a specific code for the next x minutes
     *
     * The Transactional "magic" is to ensure DB changes made here are kept even if an exception is thrown
     *
     * @param clientId The ID of the client
     * @param amount   The amount, to base the response on
     */
    @Transactional(value = Transactional.TxType.REQUIRES_NEW, dontRollbackOn = {HttpStatusException.class})
    public void executeImmediateResponses(String clientId, String amount, String errorMessage) {
        LOG.info("Payment amount on payment consent request was {}, checking for immediate responses", amount);
        HttpStatus httpStatus = null;
        String message = null;
        switch (amount) {
            case "10422.00":
                LOG.info("Throwing an HTTP 422 on payment consent request, with message \"Forced a 422 for payment consent request\"");
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Forced a 422 for payment consent request");
            case "10422.01":
                LOG.info("Throwing an HTTP 422 on payment consent request, with message \"{}\"", errorMessage);
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, errorMessage);
            case "10429.00":
                LOG.info("Will now block requests to the payments API for 10 minutes for the specified client - With forced response being: 429");
                httpStatus = HttpStatus.TOO_MANY_REQUESTS;
                message = "Too many requests";
                break;
            case "10504.00":
                LOG.info("Will now block requests to the payments API for 10 minutes for the specified client - With forced response being: 504");
                httpStatus = HttpStatus.GATEWAY_TIMEOUT;
                message = "Gateway Timeout";
                break;
            case "10423 .00":
                LOG.info("Will now block requests to the payments API for 10 minutes for the specified client - With forced response being: 423");
                httpStatus = HttpStatus.LOCKED;
                message = "Locked";
                break;
            case "10500 .00":
                LOG.info("Will now block requests to the payments API for 10 minutes for the specified client - With forced response being: 500");
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
                message = "Internal Server Error";
                break;
            default:
                return;
        }
        saveClientError(clientId, 10, httpStatus, message);
        throw new HttpStatusException(httpStatus, message);
    }

    /**
     * Execute client actions. Check as soon as a request is made, before any processing, to see if there is a set
     * response for this client
     *
     * @param clientId The client to check against
     */
    public void executeClientRestrictions(String clientId) {
        LOG.info("Checking for client restriction actions on client {}", clientId);
        var actions = paymentsSimulateResponseRepository.findByUserClientIdAndRequestEndTimeAfter(clientId, LocalDateTime.now());
        for (PaymentsSimulateResponseEntity action : actions) {
            LOG.info("Client restriction actions found for client {}, status {}", clientId, action.getHttpStatus());
            if (action.getHttpStatus() != null) {
                throw new HttpStatusException(action.getHttpStatus(), action.getHttpErrorMessage());
            }
        }
    }

    /**
     * Actions that occur after a consent has been created.
     * <p>
     * Amounts can be added here that would change to other statuses like "REJECTED" or "REVOKED"
     *
     * @param consentId The consent to check against
     */
    @Transactional(value = Transactional.TxType.REQUIRES_NEW, dontRollbackOn = {HttpStatusException.class})
    public void executePostConsentCreationActions(String consentId) {
        var consentOpt = paymentConsentRepository.findByPaymentConsentId(consentId);
        if (consentOpt.isPresent()) {
            PaymentConsentEntity paymentConsentEntity = consentOpt.get();
            String consentAmount = getPaymentConsentAmount(paymentConsentEntity);

            LOG.info("Payment amount on payment consent request was {}, checking for post-creation actions", consentAmount);
            switch (consentAmount) {
                case "1334.00":
                case "1335.00":
                    setConsentStatusToAuthorised(paymentConsentEntity);
                    break;
                default:
                    // do nothing
            }

            float consentAmountFloat = Float.parseFloat(consentAmount);
            if (consentAmountFloat >= 1333.00f && consentAmountFloat <= 1333.99f) {
                setConsentStatusToAuthorised(paymentConsentEntity);
            }
        }
    }

    private String getPaymentConsentAmount(PaymentConsentEntity paymentConsentEntity) {
        if(paymentConsentEntity.getPaymentConsentPaymentEntity() != null) {
            return paymentConsentEntity.getPaymentConsentPaymentEntity().getAmount();
        } else {
            return paymentConsentEntity.getPostSweepingRecurringConfiguration().getAmount();
        }
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW, dontRollbackOn = {HttpStatusException.class})
    public void executePostConsentCreationActionsV3(String consentId) {
        var consentOpt = paymentConsentRepository.findByPaymentConsentId(consentId);
        if (consentOpt.isPresent()) {
            PaymentConsentEntity paymentConsentEntity = consentOpt.get();
            String consentAmount = getPaymentConsentAmount(paymentConsentEntity);

            LOG.info("Payment amount on payment consent request was {}, checking for post-creation actions", consentAmount);
            switch (consentAmount) {
                case "300.01":
                    paymentConsentEntity.setStatus(ResponsePaymentConsentDataV3.StatusEnum.REJECTED.name());
                    paymentConsentEntity.setRejectReasonCode(EnumConsentRejectionReasonType.VALOR_INVALIDO.name());
                    paymentConsentEntity.setRejectReasonDetail("O valor enviado não é válido para o QR Code informado;");
                    break;
                case "300.02":
                    paymentConsentEntity.setStatus(ResponsePaymentConsentDataV3.StatusEnum.REJECTED.name());
                    paymentConsentEntity.setRejectReasonCode(EnumConsentRejectionReasonType.NAO_INFORMADO.name());
                    paymentConsentEntity.setRejectReasonDetail("Não informada pela detentora de conta;");
                    break;
                case "300.03":
                    paymentConsentEntity.setStatus(ResponsePaymentConsentDataV3.StatusEnum.REJECTED.name());
                    paymentConsentEntity.setRejectReasonCode(EnumConsentRejectionReasonType.FALHA_INFRAESTRUTURA.name());
                    paymentConsentEntity.setRejectReasonDetail("O valor enviado não é válido para o QR Code informado;");
                    break;
                case "300.04":
                    paymentConsentEntity.setStatus(ResponsePaymentConsentDataV3.StatusEnum.REJECTED.name());
                    paymentConsentEntity.setRejectReasonCode(EnumConsentRejectionReasonType.TEMPO_EXPIRADO_CONSUMO.name());
                    paymentConsentEntity.setRejectReasonDetail("Consentimento expirou antes que o usuário pudesse confirmá-lo.");
                    break;
                case "300.05":
                    paymentConsentEntity.setStatus(ResponsePaymentConsentDataV3.StatusEnum.REJECTED.name());
                    paymentConsentEntity.setRejectReasonCode(EnumConsentRejectionReasonType.CONTA_NAO_PERMITE_PAGAMENTO.name());
                    paymentConsentEntity.setRejectReasonDetail("A conta selecionada é do tipo [salario/investimento/liquidação/outros] e não permite realizar esse pagamento.");
                    break;
                case "300.06":
                    paymentConsentEntity.setStatus(ResponsePaymentConsentDataV3.StatusEnum.REJECTED.name());
                    paymentConsentEntity.setRejectReasonCode(EnumConsentRejectionReasonType.SALDO_INSUFICIENTE.name());
                    paymentConsentEntity.setRejectReasonDetail("A conta selecionada não possui saldo suficiente para realizar o pagamento.");
                    break;
                case "300.07":
                    paymentConsentEntity.setStatus(ResponsePaymentConsentDataV3.StatusEnum.REJECTED.name());
                    paymentConsentEntity.setRejectReasonCode(EnumConsentRejectionReasonType.VALOR_ACIMA_LIMITE.name());
                    paymentConsentEntity.setRejectReasonDetail("O valor ultrapassa o limite estabelecido [na instituição/no arranjo/outro] para permitir a realização de transações pelo cliente.");
                    break;
                case "300.08":
                    paymentConsentEntity.setStatus(ResponsePaymentConsentDataV3.StatusEnum.REJECTED.name());
                    paymentConsentEntity.setRejectReasonCode(EnumConsentRejectionReasonType.QRCODE_INVALIDO.name());
                    paymentConsentEntity.setRejectReasonDetail("O QRCode utilizado para a iniciação de pagamento não é válido.");
                    break;
                default:
                    if (EnumConsentRejectionReasonType.REJEITADO_USUARIO.name().equals(paymentConsentEntity.getRejectReasonCode())) {
                        LOG.info("Consent rejection reason code is REJEITADO_USUARIO, the status wont be updated");
                        // If consent was rejected by the user we dont update the status anymore
                        break;
                    }

                    if (paymentConsentEntity.isTimeAuthorizationExpired()) {
                        paymentConsentEntity.setStatus(ResponsePaymentConsentDataV3.StatusEnum.REJECTED.name());
                        paymentConsentEntity.setRejectReasonCode(EnumConsentRejectionReasonType.TEMPO_EXPIRADO_AUTORIZACAO.name());
                        paymentConsentEntity.setRejectReasonDetail("Consentimento expirou antes que o usuário pudesse confirmá-lo.");
                        break;
                    }
            }
            paymentConsentRepository.update(paymentConsentEntity);
        }
    }

    protected void setConsentStatusToAuthorised(PaymentConsentEntity paymentConsentEntity) {
        LOG.info("Accepted the consent request");
        paymentConsentEntity.setStatus(ResponsePaymentConsentData.StatusEnum.AUTHORISED.toString());
        paymentConsentRepository.update(paymentConsentEntity);
    }

    /**
     * Actions that occur when payments are initiated
     *
     * @param consentId the consent to trigger from
     */
    public void executePaymentInitiationActions(String consentId) {
        var consentOpt = paymentConsentRepository.findByPaymentConsentId(consentId);
        if (consentOpt.isPresent()) {
            var consentAmount = consentOpt.get().getPaymentConsentPaymentEntity().getAmount();
            LOG.info("Payment amount on payment consent request was {}, checking for payment initiation actions", consentAmount);
            switch (consentAmount) {
                case "20422.00":
                    LOG.info("Rejecting payment initiation with 422 and message \"Forced a 422 for payment request\"");
                    throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Forced a 422 for payment request");
                case "20422.01":
                    LOG.info("Rejecting payment initiation with 422 and message \"{}\"", ErrorCodesEnumV1.DETALHE_PGTO_INVALIDO);
                    throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, ErrorCodesEnumV1.DETALHE_PGTO_INVALIDO.name());
                case "20422.02":
                    LOG.info("Rejecting payment initiation with 422 and message \"{}\"", ErrorCodesEnumV1.COBRANCA_INVALIDA);
                    throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, ErrorCodesEnumV1.COBRANCA_INVALIDA.name());
                case "20422.03":
                    LOG.info("Rejecting payment initiation with 422 and message \"{}\"", ErrorCodesEnumV1.PAGAMENTO_DIVERGENTE_DO_CONSENTIMENTO);
                    throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, ErrorCodesEnumV1.PAGAMENTO_DIVERGENTE_DO_CONSENTIMENTO.name());
                default:
                    // do nothing
            }
        }
    }

    /**
     * Actions that occur after payment creation
     *
     * @param paymentId The payment we're interested in
     */
    @Transactional(value = Transactional.TxType.REQUIRES_NEW, dontRollbackOn = {HttpStatusException.class})
    public void executePostPaymentInitiationActions(String paymentId) {

        var paymentOpt = pixPaymentRepository.findByPaymentId(paymentId);
        if (paymentOpt.isPresent()) {
            var payment = paymentOpt.get();
            var consent = payment.getPaymentConsentEntity();
            var consentAmount = consent.getPaymentConsentPaymentEntity().getAmount();
            LOG.info("Payment amount on payment consent request was {}, checking for post payment creation actions", consentAmount);
            switch (consentAmount) {
                case "20201.00":
                    LOG.info("Auto-move payment status to RJCT after initiation");
                    payment.setStatus(EnumPaymentStatusType.RJCT.toString());
                    pixPaymentRepository.save(payment);
                    break;
                case "1334.00":
                    LOG.info("Auto-move payment status to ACSC after initiation");
                    payment.setStatus(EnumPaymentStatusType.ACSC.toString());
                    pixPaymentRepository.save(payment);
                    break;
                case "1335.00":
                    LOG.info("Auto-move payment status to ACSP after initiation");
                    payment.setStatus(EnumPaymentStatusType.ACSP.toString());
                    pixPaymentRepository.save(payment);
                    break;
                default:
                    // do nothing
            }

            float consentAmountFloat = Float.parseFloat(consentAmount);
            if (consentAmountFloat >= 1333.00f && consentAmountFloat <= 1333.99f) {
                LOG.info("Auto-move payment status to ACCC after initiation");
                payment.setStatus(EnumPaymentStatusType.ACCC.toString());
                pixPaymentRepository.save(payment);
            }
        }
    }

    public void saveClientError(String clientId, Integer blockAmountInMinutes, HttpStatus status, String errorMessage) {
        PaymentsSimulateResponseEntity entityToSave = new PaymentsSimulateResponseEntity();
        entityToSave.setRequestEndTime(LocalDateTime.now().plusMinutes(blockAmountInMinutes));
        entityToSave.setDuration(blockAmountInMinutes);
        entityToSave.setHttpErrorMessage(errorMessage);
        entityToSave.setHttpStatus(status);
        entityToSave.setRequestTime(LocalDateTime.now());
        entityToSave.setUserClientId(clientId);
        paymentsSimulateResponseRepository.save(entityToSave);
        // use the opportunity to clean up
        paymentsSimulateResponseRepository.deleteAll(paymentsSimulateResponseRepository.findByRequestEndTimeBefore(LocalDateTime.now()));
    }
}
