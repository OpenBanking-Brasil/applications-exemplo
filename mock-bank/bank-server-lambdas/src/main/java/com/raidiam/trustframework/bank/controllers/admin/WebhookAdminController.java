package com.raidiam.trustframework.bank.controllers.admin;

import com.raidiam.trustframework.bank.controllers.BaseBankController;
import com.raidiam.trustframework.bank.services.WebhookAdminService;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.CreateWebhook;
import com.raidiam.trustframework.mockbank.models.generated.ResponseWebhook;
import io.micronaut.http.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;

@RolesAllowed("ADMIN_FULL_MANAGE")
@Controller("/admin/webhooks")
public class WebhookAdminController extends BaseBankController {
    private static final Logger LOG = LoggerFactory.getLogger(WebhookAdminController.class);

    private final WebhookAdminService webhookService;

    WebhookAdminController(WebhookAdminService webhookService) {
        this.webhookService = webhookService;
    }

    @Post
    public ResponseWebhook postWebhookUri(String clientId,
                                          @Body CreateWebhook webhook) {
        LOG.info("Registering new webhook URI for client {}", clientId);
        var response = webhookService.createWebhookUri(clientId, webhook);
        LOG.info("Returning webhook response");
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    @Get("/{clientId}")
    public ResponseWebhook getWebhookUri(@PathVariable("clientId") String clientId) {
        var response = webhookService.getWebhookByClientId(clientId);
        LOG.info("Returning extracted webhook URI");
        return response;
    }
}
