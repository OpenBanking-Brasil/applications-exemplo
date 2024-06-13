package com.raidiam.trustframework.bank.services.automation;

import com.raidiam.trustframework.bank.domain.PaymentConsentEntity;
import com.raidiam.trustframework.bank.enums.ErrorCodesEnumV2;
import com.raidiam.trustframework.bank.repository.PaymentConsentRepository;
import com.raidiam.trustframework.bank.repository.PaymentsSimulateResponseRepository;
import com.raidiam.trustframework.bank.repository.PixPaymentRepository;
import com.raidiam.trustframework.mockbank.models.generated.*;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;

@Singleton
public class PaymentAutomationsV2 {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentAutomationsV2.class);

    @Inject
    PaymentsSimulateResponseRepository paymentsSimulateResponseRepository;

    @Inject
    PaymentConsentRepository paymentConsentRepository;

    @Inject
    PixPaymentRepository pixPaymentRepository;

    @Inject
    PaymentAutomations paymentAutomations;

    @Transactional(value=Transactional.TxType.REQUIRES_NEW, dontRollbackOn={HttpStatusException.class})
    public void executeConsentImmediateResponses(String amount) {
        LOG.info("Payment amount on payment consent request was {}, checking for immediate responses", amount);
        switch(amount) {
            case "10422.00":
                LOG.info("Throwing an HTTP 422 on payment consent request, with message \"Forced a 422 for payment consent request\"");
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Forced a 422 for payment consent request");
            case "10422.01":
                LOG.info("Throwing an HTTP 422 on payment consent request, with message \"{}\"", ErrorCodesEnumV2.DETALHE_PAGAMENTO_INVALIDO);
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, ErrorCodesEnumV2.DETALHE_PAGAMENTO_INVALIDO.name());
            case "10422.02":
                LOG.info("Throwing an HTTP 422 on payment consent request, with detail \"{}\"", ErrorCodesEnumV2.FORMA_PAGAMENTO_INVALIDA);
                throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, ErrorCodesEnumV2.FORMA_PAGAMENTO_INVALIDA.name());
            default:
                return;
        }
    }

    /**
     * Actions that occur when payments are initiated
     *
     * @param consentId the consent to trigger from
     */
    @Transactional(value=Transactional.TxType.REQUIRES_NEW, dontRollbackOn={HttpStatusException.class})
    public void executePaymentInitiationActions(String consentId) {
        LOG.info("Checking payment triggers actions for consent {}", consentId);
        var consentOpt = paymentConsentRepository.findByPaymentConsentId(consentId);
        if(consentOpt.isPresent()) {
            PaymentConsentEntity paymentConsentEntity = consentOpt.get();
            var consentAmount = consentOpt.get().getPaymentConsentPaymentEntity().getAmount();
            LOG.info("Payment amount on payment consent request was {}, checking for payment initiation actions", consentAmount);
            String rejectionMessage = "Rejecting payment initiation with 422 and message \"{}\"";
            switch (consentAmount) {
                case "20422.00":
                    LOG.info(rejectionMessage, "Forced a 422 for payment request");
                    setConsentStatusToConsumed(paymentConsentEntity);
                    throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Forced a 422 for payment request");
                case "20422.01":
                    LOG.info(rejectionMessage, ErrorCodesEnumV2.DETALHE_PAGAMENTO_INVALIDO);
                    setConsentStatusToConsumed(paymentConsentEntity);
                    throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, ErrorCodesEnumV2.DETALHE_PAGAMENTO_INVALIDO.name());
                case "20422.02":
                    LOG.info(rejectionMessage, ErrorCodesEnumV2.VALOR_INVALIDO);
                    setConsentStatusToConsumed(paymentConsentEntity);
                    throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, ErrorCodesEnumV2.VALOR_INVALIDO.name());
                case "20422.03":
                    LOG.info(rejectionMessage, ErrorCodesEnumV2.PAGAMENTO_DIVERGENTE_CONSENTIMENTO);
                    setConsentStatusToConsumed(paymentConsentEntity);
                    throw new HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, ErrorCodesEnumV2.PAGAMENTO_DIVERGENTE_CONSENTIMENTO.name());
                default:
                    // do nothing
            }
        }
        LOG.info("Finishing Checking payment triggers actions for consent {}", consentId);
    }

    @Transactional(value=Transactional.TxType.REQUIRES_NEW, dontRollbackOn={HttpStatusException.class})
    public void executePostPaymentInitiationActions(String paymentId) {
        LOG.info("Checking payment triggers actions for payment {}", paymentId);
        var paymentOpt = pixPaymentRepository.findByPaymentId(paymentId);
        if(paymentOpt.isPresent()) {
            String autoMoveMessage = "Auto-move payment status to {} after initiation";
            var payment = paymentOpt.get();
            var consentAmount = payment.getPaymentConsentEntity().getPaymentConsentPaymentEntity().getAmount();
            LOG.info("Payment amount on payment consent request was {}, checking for post payment creation actions", consentAmount);
            switch (consentAmount) {
                case "20201.00":
                    LOG.info(autoMoveMessage, "RJCT (DETALHE_PAGAMENTO_INVALIDO)");
                    payment.setStatus(EnumPaymentStatusTypeV2.RJCT.toString());
                    payment.setRejectionReason(RejectionReasonV2.CodeEnum.DETALHE_PAGAMENTO_INVALIDO.name());
                    pixPaymentRepository.save(payment);
                    break;
                case "20201.10":
                    LOG.info(autoMoveMessage, "RJCT (PAGAMENTO_RECUSADO_DETENTORA)");
                    payment.setStatus(EnumPaymentStatusTypeV2.RJCT.toString());
                    payment.setRejectionReason(RejectionReasonV2.CodeEnum.PAGAMENTO_RECUSADO_DETENTORA.name());
                    pixPaymentRepository.save(payment);
                    break;
                case "20201.20":
                    LOG.info(autoMoveMessage, "RJCT (VALOR_INVALIDO)");
                    payment.setStatus(EnumPaymentStatusTypeV2.RJCT.toString());
                    payment.setRejectionReason(RejectionReasonV2.CodeEnum.VALOR_INVALIDO.name());
                    pixPaymentRepository.save(payment);
                    break;
                case "20201.30":
                    LOG.info(autoMoveMessage, "RJCT (VALOR_ACIMA_LIMITE)");
                    payment.setStatus(EnumPaymentStatusTypeV2.RJCT.toString());
                    payment.setRejectionReason(RejectionReasonV2.CodeEnum.VALOR_ACIMA_LIMITE.name());
                    pixPaymentRepository.save(payment);
                    break;
                case "999999999.99":
                    LOG.info(autoMoveMessage, "RJCT (SALDO_INSUFICIENTE)");
                    payment.setStatus(EnumPaymentStatusTypeV2.RJCT.toString());
                    payment.setRejectionReason(RejectionReasonV2.CodeEnum.SALDO_INSUFICIENTE.name());
                    pixPaymentRepository.save(payment);
                    break;
                case "20201.50":
                    LOG.info(autoMoveMessage, "RJCT (COBRANCA_INVALIDA)");
                    payment.setStatus(EnumPaymentStatusTypeV2.RJCT.toString());
                    payment.setRejectionReason(RejectionReasonV2.CodeEnum.COBRANCA_INVALIDA.name());
                    pixPaymentRepository.save(payment);
                    break;
                case "1334.00":
                    LOG.info(autoMoveMessage, "ACPD");
                    payment.setStatus(EnumPaymentStatusTypeV2.ACPD.toString());
                    pixPaymentRepository.save(payment);
                    break;
                case "1335.00":
                    LOG.info(autoMoveMessage, "ACCP");
                    payment.setStatus(EnumPaymentStatusTypeV2.ACCP.toString());
                    pixPaymentRepository.save(payment);
                    break;
                case "1336.00":
                    LOG.info(autoMoveMessage, "ACSC");
                    payment.setStatus(EnumPaymentStatusTypeV2.ACSC.toString());
                    pixPaymentRepository.save(payment);
                    break;
                case "12345.00":
                    if (payment.getStatus().equals(EnumPaymentStatusTypeV2.RCVD.toString())) {
                        LOG.info(autoMoveMessage, "PDNG");
                        payment.setStatus(EnumPaymentStatusTypeV2.PDNG.toString());
                        pixPaymentRepository.save(payment);
                    }
                    break;
                case "12345.67":
                    if (payment.getStatus().equals(EnumPaymentStatusTypeV2.RCVD.toString())) {
                        LOG.info(autoMoveMessage, "PDNG");
                        payment.setStatus(EnumPaymentStatusTypeV2.PDNG.toString());
                    } else if (payment.getStatus().equals(EnumPaymentStatusTypeV2.PDNG.toString())) {
                        LOG.info(autoMoveMessage, "ACSC");
                        payment.setStatus(EnumPaymentStatusTypeV2.ACSC.toString());
                    }
                    pixPaymentRepository.save(payment);
                    break;
                default:
                    // do nothing
            }

            float consentAmountFloat = Float.parseFloat(consentAmount);
            if(consentAmountFloat >= 1333.00f && consentAmountFloat <= 1333.99f){
                LOG.info("Auto-move payment status to ACSC after initiation");
                payment.setStatus(EnumPaymentStatusTypeV2.ACSC.toString());
                pixPaymentRepository.save(payment);
            }
        }
        LOG.info("Finishing Checking payment triggers actions for payment {}", paymentId);
    }

    protected void setConsentStatusToConsumed(PaymentConsentEntity paymentConsentEntity) {
        LOG.info("Accepted the consent request");
        paymentConsentEntity.setStatus(ResponsePaymentConsentData.StatusEnum.CONSUMED.toString());
        paymentConsentRepository.update(paymentConsentEntity);
    }

    public void executeClientRestrictions(String clientId) {
        paymentAutomations.executeClientRestrictions(clientId);
    }

    public void executePostConsentCreationActions(String consentId) {
        paymentAutomations.executePostConsentCreationActions(consentId);
    }

    public void executeImmediateResponses(String clientId, String amount, String errorMessage) {
        paymentAutomations.executeImmediateResponses(clientId, amount, errorMessage);
    }
}
