package com.raidiam.trustframework.bank.domain;

import com.raidiam.trustframework.mockbank.models.generated.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode(callSuper=false)
@Entity
@Table(name = "client_webhook_uri")
public class WebhookEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reference_id", unique = true, nullable = false, updatable = false, insertable = false)
    private Integer referenceId;

    @Column(name = "client_id", nullable = false, updatable = false)
    private String clientId;

    @Column(name = "webhook_uri", nullable = false)
    private String webhookUri;

    public ResponseWebhook toResponseWebhook() {
        return new ResponseWebhook().data(new ResponseWebhookData()
                .clientId(this.clientId)
                .webhookUri(this.webhookUri)
        );
    }

    public WebhookEntity createWebhook(String clientId, CreateWebhook webhook) {
        var webhookEntity =  new WebhookEntity();
        webhookEntity.setClientId(clientId);
        webhookEntity.setWebhookUri(webhook.getData().getWebhookUri());

        return webhookEntity;
    }
}
