package com.raidiam.trustframework.bank.controllers;

import com.raidiam.trustframework.bank.services.ResourcesService;
import com.raidiam.trustframework.bank.utils.BankLambdaUtils;
import com.raidiam.trustframework.mockbank.models.generated.ResponseResourceList;
import com.raidiam.trustframework.mockbank.models.generated.ResponseResourceListLinks;
import com.raidiam.trustframework.mockbank.models.generated.ResponseResourceListMeta;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@RolesAllowed({"RESOURCES_READ"})
@Controller("/open-banking/resources/v1/resources")
public class ResourcesController extends BaseBankController {

    private static final Logger LOG = LoggerFactory.getLogger(ResourcesController.class);

    private final ResourcesService resourcesService;

    ResourcesController(ResourcesService resourcesService) {
        this.resourcesService = resourcesService;
    }

    @Get
    public ResponseResourceList getResources(Pageable pageable, @NotNull HttpRequest<?> request) {
        var consentId = bankLambdaUtils.getConsentIdFromRequest(request);
        LOG.info("Looking up all resources for consent id {}", consentId);
        Pageable adjustedPageable = BankLambdaUtils.adjustPageable(pageable, request);
        var response = resourcesService.getResourceList(adjustedPageable, consentId);
        decorateResourcesList(response, appBaseUrl + request.getPath());
        BankLambdaUtils.logObject(mapper, response);
        return response;
    }

    private void decorateResourcesList(ResponseResourceList resourceList, String self) {
        resourceList.setLinks(new ResponseResourceListLinks().self(self));
        resourceList.setMeta(new ResponseResourceListMeta().totalPages(1).totalRecords(resourceList.getData().size()).requestDateTime(OffsetDateTime.now()));
    }
}
