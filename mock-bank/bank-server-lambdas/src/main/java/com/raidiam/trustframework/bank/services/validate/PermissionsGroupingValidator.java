package com.raidiam.trustframework.bank.services.validate;

import com.raidiam.trustframework.bank.exceptions.TrustframeworkException;
import com.raidiam.trustframework.mockbank.models.generated.CreateConsent;
import com.raidiam.trustframework.mockbank.models.generated.CreateConsentData;

import java.util.List;
import java.util.Set;

public class PermissionsGroupingValidator implements ConsentValidator {

    @Override
    public void validate(CreateConsent request) {
        List<CreateConsentData.PermissionsEnum> requestedPermissions = request.getData().getPermissions();
        for(CreateConsentData.PermissionsEnum permission: requestedPermissions) {
            ensureEntireSetIsInRequest(permission, requestedPermissions);
        }
    }

    private void ensureEntireSetIsInRequest(CreateConsentData.PermissionsEnum permission, List<CreateConsentData.PermissionsEnum> requestedPermissions) {
        for(Set<CreateConsentData.PermissionsEnum> candidate: PermissionGroups.ALL_PERMISSION_GROUPS) {
            if(candidate.contains(permission)) {
                if(requestedPermissions.containsAll(candidate)) {
                    return;
                }
            }
        }
        throw new TrustframeworkException("You must request all the permissions from a given set");
    }
}
