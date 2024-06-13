package com.raidiam.trustframework.bank.services;

import com.raidiam.trustframework.bank.domain.WebhookEntity;
import com.raidiam.trustframework.bank.exceptions.TrustframeworkException;
import com.raidiam.trustframework.bank.repository.WebhookRepository;
import com.raidiam.trustframework.mockbank.models.generated.CreateWebhook;
import com.raidiam.trustframework.mockbank.models.generated.ResponseWebhook;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Pattern;

@Singleton
@Transactional
public class WebhookAdminService {
    private static final Logger LOG = LoggerFactory.getLogger(WebhookAdminService.class);
    HttpClient client = HttpClient.newHttpClient();

    private static final String BASE_URI_REGEX = "^(https?://)?(www\\.)?([-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6})(/test-mtls/a/[-a-zA-Z0-9@:%._+~#=]{2,256})";
    private static final String OPEN_BANKING_BASE = "/open-banking/webhook/v1/payments/v4/";
    private static final String CONSENTS_URI_REGEX = OPEN_BANKING_BASE + "consents/%s";
    private static final String PAYMENTS_URI_REGEX = OPEN_BANKING_BASE + "pix/payments/%s";
    private String webhookUri;

    private final WebhookRepository webhookRepository;

    WebhookAdminService(WebhookRepository webhookRepository) {
        this.webhookRepository = webhookRepository;
    }

    public ResponseWebhook createWebhookUri(@NotNull String clientId, @NotNull CreateWebhook webhook) {
        LOG.info("Saving webhook URI for client {}", clientId);

        Optional<WebhookEntity> repositoryEntity = webhookRepository.findByClientId(clientId);
        if (repositoryEntity.isPresent()) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "There is already a webhook URI set for this client");
        }

        if (!Pattern.matches(BASE_URI_REGEX, webhook.getData().getWebhookUri())) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Webhook URI does not match the required pattern for payment or payment consent endpoint");
        }

        WebhookEntity webhookEntity = new WebhookEntity().createWebhook(clientId, webhook);
        webhookRepository.save(webhookEntity);

        return webhookRepository.findByClientId(clientId)
                .orElseThrow(() -> new TrustframeworkException("Could not find recently saved WebhookEntity"))
                .toResponseWebhook();
    }

    public ResponseWebhook getWebhookByClientId(String clientId) {
        LOG.info("Attempting to retrieve saved webhook URI for client {}", clientId);
        Optional<WebhookEntity> entity = webhookRepository.findByClientId(clientId);
        return entity.map(WebhookEntity::toResponseWebhook).orElse(null);
    }

    public void checkAndPostToPaymentWebhook(String clientId, String paymentId) {
        LOG.info("Checking for saved notification webhook URI using client ID {}", clientId);
        if (getWebhookByClientId(clientId) != null) {
            ResponseWebhook webhook = getWebhookByClientId(clientId);
            webhookUri = webhook.getData().getWebhookUri() + String.format(PAYMENTS_URI_REGEX, paymentId);
            postUpdateToWebhook(LocalDateTime.now());
        } else {
            LOG.info("No webhook URI found to update payment status");
        }
    }

    public void checkAndPostToConsentWebhook(String clientId, String consentId) {
        LOG.info("Checking for saved notification webhook URI using client ID {}", clientId);
        if (getWebhookByClientId(clientId) != null) {
            ResponseWebhook webhook = getWebhookByClientId(clientId);
            webhookUri = webhook.getData().getWebhookUri() + String.format(CONSENTS_URI_REGEX, consentId);
            postUpdateToWebhook(LocalDateTime.now());
        } else {
            LOG.info("No webhook URI found to update consent status");
        }
    }

    public void postUpdateToWebhook(LocalDateTime timestamp) {
        LOG.info("Webhook URI found - sending timestamp for status update to {}", webhookUri);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUri))
                .POST(HttpRequest.BodyPublishers.ofString(String.format("{\"data\": {\"timestamp\": \"%s\"}}",
                        timestamp.atZone(ZoneId.of("America/Sao_Paulo")).format(DateTimeFormatter.ISO_INSTANT))))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream());
    }
}
